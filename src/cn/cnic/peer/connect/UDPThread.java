package cn.cnic.peer.connect;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;


import cn.cnic.peer.cons.Constant;
import cn.cnic.peer.entity.Peer;
import cn.cnic.peer.entity.Piece;
import cn.cnic.peer.entity.Segment;
import cn.cnic.peer.merge.Merge;
import cn.cnic.peer.sqlite.DB;

public class UDPThread implements Runnable {
	
	public static List<Segment> segments = new ArrayList<Segment>();
	
	/**
	 * 下面这两个map用来判断对端peerlist是不是都已返回结果
	 * 如果已都返回（total == current），就可进行视频拼接，然后向各peer发送片段请求，并在map中删除此contentHash
	 */
	public static Map<String, Integer> mapTotal = new HashMap<String, Integer>();
	public static Map<String, Integer> mapCurrent = new HashMap<String, Integer>();
	
	//这个map用来记录contentHash的所有片段
	private Map<String, List<Piece>> mapPiece = new HashMap<String, List<Piece>>();
	
	public void run() {
		Log.d("peer", "已启动UDP线程，用于PEER之间进行通信");
		try {
			DatagramSocket ds = new DatagramSocket(Constant.TRACKER_UDP_PORT);

			// 启动心跳线程
			new Thread(new HeartThread(ds, Constant.PEER_ID_VALUE)).start();

			// 循环接收
			byte[] buf = new byte[1024];
			DatagramPacket rp = new DatagramPacket(buf, 1024);
			boolean isEnd = false;
			while (!isEnd) {
				ds.receive(rp);
				// 取出信息
				String content = new String(rp.getData(), 0, rp.getLength());
				JSONObject json = new JSONObject(content);
				String contentHash = json.getString(Constant.CONTENT_HASH);
				String action = json.getString(Constant.ACTION);
				
				//Tracker返回Peer的UDP心跳响应，内容包含Peer的公网IP地址和端口，Peer可以依据此信息判断自己的网络类型（如是否为NAT，即PubIP不等于LocalIP，暂时不适用）
				if(action.equals(Constant.ACTION_HEARTBEAT_RESPONSE)) {
					
				} 
				
				//UDP穿透响应，收到来自tracker的穿透响应后，peer向对端peer进行打洞，在此进行三次打洞
				else if(action.equals(Constant.ACTION_NAT_TRAVERSAL_ORDER)) {
					String targetPeerIP = json.getString(Constant.TARGET_PEER_IP);
					String targetPeerPort = json.getString(Constant.TARGET_PEER_PORT);
					makeHole(ds, targetPeerIP, Integer.parseInt(targetPeerPort));
					makeHole(ds, targetPeerIP, Integer.parseInt(targetPeerPort));
					makeHole(ds, targetPeerIP, Integer.parseInt(targetPeerPort));
				} 
				
				//收到握手请求后，返回本地所拥有的报文片段，并进行握手响应
				else if(action.equals(Constant.ACTION_P2P_HANDSHAKE_REQUEST)) {
//					List<Piece> pieces = DB.getPiecesByContentHash(contentHash);
//					submitP2PhandShakeResponse(ds, contentHash, pieces, ds.getInetAddress().getHostName(), ds.getPort());
				} 
				
				//收到握手响应后，判断是否已全部返回，如果是，则对视频进行拼接
				else if(action.equals(Constant.ACTION_P2P_HANDSHAKE_RESPONSE)) {
					JSONArray pieces = new JSONArray(json.get(Constant.PIECES).toString());
					if(!mapPiece.containsKey(contentHash)) {
						mapPiece.put(contentHash, new ArrayList<Piece>());
					}
					for(int i = 0; i < pieces.length(); i++) {
						mapPiece.get(contentHash).add((Piece)pieces.get(i));
					}
					int total = mapTotal.get(contentHash);
					int current = mapCurrent.get(contentHash);
					//全部返回
					if(total == current) {
						//文件总大小（单位：kb）
						int size = 10000;
						//进行视频拼接，得到需要请求的视频片段
						List<Piece> resultPieces = generateFinalPieces(mapPiece.get(contentHash), size, ds, contentHash);
						
						//拼接完成后将记录从mapPiece中删除
						mapPiece.remove(contentHash);
						
						//依次发送数据请求
						for(Piece p : resultPieces) {
							UDP.submitP2PPieceRequest(ds, contentHash, p.getOffset(), p.getLength(), p.getPeer().getUdpIp(), p.getPeer().getUdpPort());
						}
						
						//请求发送完毕后，将此任务从两个map中移走
						mapTotal.remove(contentHash);
						mapCurrent.remove(contentHash);
					}
				} 
				
				//收到报文请求后，向请求方发送报文数据
				else if(action.equals(Constant.ACTION_P2P_PIECE_REQUEST)) {
					JSONArray array = new JSONArray(json.get(Constant.PIECES).toString());
					for(int i = 0; i < array.length(); i++) {
						DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream("filePath")));
						Piece p = (Piece)array.get(i);
						int count = p.getLength()/1000;
						for(int j = 0; j < count; j++) {
							byte[] data = new byte[1024];
							fis.read(data, 0, 1000);
							UDP.submitP2PPieceResponse(ds, contentHash, p.getOffset()+j*1000, 1000, ds.getInetAddress().getHostName(), ds.getPort(), data);
						}
						if(p.getLength()%1000 != 0) {
							int size = p.getLength()%1000;
							byte[] data = new byte[size];
							fis.read(data, 0, 1000);
							UDP.submitP2PPieceResponse(ds, contentHash, p.getLength()/1000*1000, size, ds.getInetAddress().getHostName(), ds.getPort(), data);
						}
						fis.close();
					}
				}
				
				//接收到对端peer传来的数据相应后
				else if(action.equals(Constant.ACTION_P2P_PIECE_RESPONSE)) {
					
					//以contentHash作为文件名，如果不存在，就创建
					File f = new File(Constant.SAVE_PATH + contentHash);
					if(!f.exists()) {
						f.createNewFile();
					}
					
					//将数据写入文件中
					DataOutputStream fileOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
					fileOut.write(json.get(Constant.DATA).toString().getBytes(), (Integer)json.get(Constant.DATA_OFFSET), (Integer)json.get(Constant.DATA_LENGTH));
					fileOut.close();
					
					//更新本地数据库中的记录
					DB.updatePiece(contentHash, (Integer)json.get(Constant.DATA_OFFSET), (Integer)json.get(Constant.DATA_LENGTH));
				} 
				
				//其他
				else {
					
				}
				
				
				//如果有需要获取的数据，就向peer进行请求
				if(segments.size() > 0) {
					for(int i = 0; i < segments.size(); i++) {
						Segment s = segments.get(i);
						
						//依次遍历tracker返回的每一个peer
						for(int j = 0; j < s.getPeerList().size(); i ++) {
							Peer p = s.getPeerList().get(j);
							//peer向tracker发送协助穿透请求
							UDP.submitNATTraversalAssist(ds, p.getPeerID(), s.getContentHash());
							//新建一个线程，在线程中延迟两秒，再去发送握手请求（延迟两秒的目的是让对端的peer节点有时间去进行打洞）
							new Thread(new HandShakeThread(ds, s.getContentHash(), p.getUdpIp(), p.getUdpPort())).start();
						}
					}
				}
				Thread.sleep(Constant.RECEIVE_INTERVAL);
			}
			ds.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void makeHole(DatagramSocket ds, String targetPeerIP, int targetPeerPort) {
		try {
			JSONObject json = new JSONObject();
			json.put("info", "打洞");
			String data = json.toString();
			DatagramPacket p = new DatagramPacket(data.getBytes(), data.getBytes().length, InetAddress.getByName(targetPeerIP), targetPeerPort);
			ds.send(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ###########视频拼接算法#############
	 * 1.找出最长的一段数据，采用
	 * 
	 * ###################################
	 * @param sourcePieces peer提供的数据片段
	 * @param size 文件大小
	 * @return 最终需要请求的数据片段
	 */
	public List<Piece> generateFinalPieces(List<Piece> sourcePieces, int size, DatagramSocket ds, String contentHash) {
		//确定需从CDN上下载的片段list：先获取到本地局域网中的片段并集，剩下的就是需要从CDN上获取的
		List<Piece> existPieces = new Merge().merge(sourcePieces);
		for(int i = 0; i < existPieces.size(); i++) {
			if(existPieces.get(i).getOffset() != 0) {
				if(i == 0) {
					downloadFromCDN("", 0, existPieces.get(i).getOffset());
				} else {
					downloadFromCDN("", existPieces.get(i - 1).getOffset() + existPieces.get(i - 1).getLength(), existPieces.get(i).getOffset());
				}
			}
		}
		Piece lastPiece = existPieces.get(existPieces.size() - 1);
		int end = lastPiece.getOffset() + lastPiece.getLength();
		if(end < size) {
			downloadFromCDN("", end, size - end);
		}
		
		//从peer节点中下载的片段list
		return new Merge().mergePeer(sourcePieces);
	}
	
	public void downloadFromCDN(String url, int offset, int length) {
		
	}
}
