package cn.cnic.peer.connect;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.example.peer.VideoServer;

import android.os.Environment;
import android.provider.MediaStore.Video;
import android.util.Log;

import cn.cnic.peer.cons.Constant;
import cn.cnic.peer.download.Download;
import cn.cnic.peer.entity.Peer;
import cn.cnic.peer.entity.Piece;
import cn.cnic.peer.entity.Segment;
import cn.cnic.peer.merge.Merge;

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
	
	/**
	 * 下面这两个map用来判断报文请求是否都已全部返回
	 * 如果都已经返回，就可以向用户输出视频
	 */
	public static Map<String, Integer> dataMapTotal = new HashMap<String, Integer>();
	public static Map<String, Integer> dataMapCurrent = new HashMap<String, Integer>();
	
	public void run() {
		Log.d("peer", "已启动UDP线程，用于PEER之间进行通信");
		try {
			DatagramSocket ds = new DatagramSocket(Constant.TRACKER_UDP_PORT);

			// 启动心跳线程
			new Thread(new HeartThread(ds, Constant.PEER_ID_VALUE)).start();

			// 循环接收
			boolean isEnd = false;
			while (!isEnd) {
				byte[] buf = new byte[2048];
				DatagramPacket rp = new DatagramPacket(buf, 2048);
				ds.receive(rp);
				// 取出信息
				String content = "";
				int endIndex = 0;
				for(int i = 0; i < buf.length; i++) {
					if((char)buf[i] == '}') {
						if(buf[i+1] == 0) {
							endIndex = i;
							break;
						}
						
						if((char)buf[i+1] == '$' && (char)buf[i+2] == '$') {
							endIndex = i;
							break;
						}
					}
				}
				content = new String(buf, 0, endIndex+1);
				Log.d("udp", content);
				JSONObject json = new JSONObject(content);
				Log.d("json", json.toString());
				String action = json.getString(Constant.ACTION);
				
				//Tracker返回Peer的UDP心跳响应，内容包含Peer的公网IP地址和端口，Peer可以依据此信息判断自己的网络类型（如是否为NAT，即PubIP不等于LocalIP，暂时不适用）
				if(action.equals(Constant.ACTION_HEARTBEAT_RESPONSE)) {
					Constant.PEER_PUBLIC_IP = json.getString(Constant.PUBLIC_UDP_IP);
					Constant.PEER_PUBLIC_PORT = json.getInt(Constant.PUBLIC_UDP_PORT);
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
					String contentHash = json.getString(Constant.CONTENT_HASH);
//					List<Piece> pieces = DB.getPiecesByContentHash(contentHash);
					List<Piece> pieces = new ArrayList<Piece>();
					Piece p = new Piece();
					p.setContentHash(contentHash);
					p.setOffset(0);
					p.setLength(467556);
					Peer peer = new Peer();
					peer.setPeerID(Constant.PEER_ID_VALUE);
					peer.setUdpIp(Constant.LOCAL_SERVER_IP);
					peer.setUdpPort(Constant.PEER_UDP_PORT);
					p.setPeer(peer);
					pieces.add(p);
					UDP.submitP2PhandShakeResponse(ds, contentHash, pieces, json.getString(Constant.PUBLIC_UDP_IP), json.getInt(Constant.PUBLIC_UDP_PORT));
				} 
				
				//收到握手响应后，判断是否已全部返回，如果是，则对视频进行拼接
				else if(action.equals(Constant.ACTION_P2P_HANDSHAKE_RESPONSE)) {
					UploadInfoThread.NATSuccessCount ++;
					String contentHash = json.getString(Constant.CONTENT_HASH);
//					mapCurrent.put(contentHash, mapCurrent.get(contentHash) + 1);
					mapCurrent.put(contentHash, mapCurrent.get(contentHash) + 1);
					JSONArray pieces = new JSONArray(json.get(Constant.PIECES).toString());
					if(!mapPiece.containsKey(contentHash)) {
						mapPiece.put(contentHash, new ArrayList<Piece>());
					}
					for(int i = 0; i < pieces.length(); i++) {
						Piece p = new Piece();
						p.setContentHash(contentHash);
						p.setLength(new JSONObject(pieces.get(i).toString()).getInt(Constant.LENGTH));
						p.setOffset(new JSONObject(pieces.get(i).toString()).getInt(Constant.OFFSET));
						Peer peer = new Peer();
						peer.setPeerID(json.getString(Constant.PEER_ID));
						peer.setUdpIp(json.getString(Constant.PUBLIC_UDP_IP));
						peer.setUdpPort(json.getInt(Constant.PUBLIC_UDP_PORT));
						p.setPeer(peer);
						mapPiece.get(contentHash).add(p);
					}
					int total = mapTotal.get(contentHash);
					int current = mapCurrent.get(contentHash);
					//全部返回
//					if(total == current) {
						//文件总大小（单位：b）
						int fileSize = 467556;
						//进行视频拼接，得到需要请求的视频片段
						List<Piece> resultPieces = generateFinalPieces(mapPiece.get(contentHash), fileSize, ds, contentHash);
						
						//拼接完成后将记录从mapPiece中删除
						mapPiece.remove(contentHash);
						
						dataMapTotal.put(contentHash, resultPieces.size());
						dataMapCurrent.put(contentHash, 0);
						//依次发送数据请求
						for(Piece p : resultPieces) {
							UDP.submitP2PPieceRequest(ds, contentHash, p.getOffset(), p.getLength(), json.getString(Constant.PUBLIC_UDP_IP), json.getInt(Constant.PUBLIC_UDP_PORT));
						}
						
						//请求发送完毕后，将此任务从两个map中移走
//						mapTotal.remove(contentHash);
//						mapCurrent.remove(contentHash);
//					}
				} 
				
				//收到报文请求后，向请求方发送报文数据
				else if(action.equals(Constant.ACTION_P2P_PIECE_REQUEST)) {
					String contentHash = json.getString(Constant.CONTENT_HASH);
					int offset = json.getInt(Constant.REQUEST_OFFSET);
					int length = json.getInt(Constant.REQUEST_LENGTH);
					DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(Environment.getExternalStorageDirectory()+"/" + contentHash)));
					fis.skip(offset);
					byte[] data = new byte[1024];
					int i = 0;
					while(i <= length) {
						if(length - i > data.length) {
							fis.read(data, 0, data.length);
							UDP.submitP2PPieceResponse(ds, contentHash, offset + i, data.length, json.getString(Constant.PUBLIC_UDP_IP), json.getInt(Constant.PUBLIC_UDP_PORT), data);
						} else {
							fis.read(data, 0, length - i);
							UDP.submitP2PPieceResponse(ds, contentHash, offset + i, length - i, json.getString(Constant.PUBLIC_UDP_IP), json.getInt(Constant.PUBLIC_UDP_PORT), data);
						}
						i += data.length;
						Thread.sleep(20);
					}
					fis.close();
				}
				
				//接收到对端peer传来的数据响应后
				else if(action.equals(Constant.ACTION_P2P_PIECE_RESPONSE)) {
					String contentHash = json.getString(Constant.CONTENT_HASH);
//					dataMapCurrent.put(contentHash, dataMapCurrent.get(contentHash) + 1);
					//以contentHash作为文件名，如果不存在，就创建
					File f = new File(Environment.getExternalStorageDirectory() + "/" + contentHash);
					if(!f.exists()) {
						f.createNewFile();
					}
					
					//将数据写入文件中
//					DataOutputStream fileOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f, true)));
					RandomAccessFile raf = new RandomAccessFile(f, "rw");
					
//					byte[] bytes = new byte[buf.length - endIndex - json.getInt(Constant.DATA_LENGTH) -1];
					byte[] bytes = new byte[json.getInt(Constant.DATA_LENGTH)];
					int n = 0;
					for(int i = endIndex + 3; i < endIndex + 3 + bytes.length; i++) {
						bytes[n++] = buf[i];
					}
//					fileOut.write(bytes);
//					fileOut.close();
					raf.seek(json.getInt(Constant.DATA_OFFSET));
					raf.write(bytes);
					raf.close();
					
//					int total = dataMapTotal.get(contentHash);
//					int current = dataMapCurrent.get(contentHash);
//					if(total == current) {
//						dataMapCurrent.remove(contentHash);
//						dataMapTotal.remove(contentHash);
//						VideoServer.over = true;
//					}
					Log.d("fileLength", f.length()+"");
					
					//更新本地数据库中的记录
//					DB.updatePiece(contentHash, (Integer)json.get(Constant.DATA_OFFSET), (Integer)json.get(Constant.DATA_LENGTH));
				} 
				
				//其他
				else {
					
				}
				
				
				//如果有需要获取的数据，就向peer进行请求
				if(segments.size() > 0) {
					for(int i = 0; i < segments.size(); i++) {
						Segment s = segments.get(i);
						
						//依次遍历tracker返回的每一个peer
						for(int j = 0; j < s.getPeerList().size(); j ++) {
							Peer p = s.getPeerList().get(j);
							//peer向tracker发送协助穿透请求
							UDP.submitNATTraversalAssist(ds, p.getPeerID(), s.getContentHash());
							//新建一个线程，在线程中延迟两秒，再去发送握手请求（延迟两秒的目的是让对端的peer节点有时间去进行打洞）
							new Thread(new HandShakeThread(ds, Constant.PEER_ID_VALUE, p.getUdpIp(), p.getUdpPort(), s.getContentHash())).start();
						}
						segments.remove(i);
					}
				}
//				Thread.sleep(Constant.RECEIVE_INTERVAL);
			}
			ds.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void makeHole(DatagramSocket ds, String targetPeerIP, int targetPeerPort) {
		try {
			JSONObject json = new JSONObject();
			json.put(Constant.ACTION, "makeHole");
			json.put("info", "打洞");
			json.put("targetPeerIP", targetPeerIP);
			json.put("targetPeerPort", targetPeerPort);
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
	public List<Piece> generateFinalPieces(List<Piece> sourcePieces, int fileSize, DatagramSocket ds, String contentHash) {
		//确定需从CDN上下载的片段list：先获取到本地局域网中的片段并集，剩下的就是需要从CDN上获取的
		List<Piece> existPieces = new Merge().merge(sourcePieces);
		for(int i = 0; i < existPieces.size(); i++) {
			if(existPieces.get(i).getOffset() != 0) {
				if(i == 0) {
					Download.download("url", 0, existPieces.get(i).getOffset(), contentHash, Constant.SAVE_PATH);
				} else {
					Download.download("url", existPieces.get(i - 1).getOffset() + existPieces.get(i - 1).getLength(), existPieces.get(i).getOffset(), contentHash, Constant.SAVE_PATH);
				}
			}
		}
		Piece lastPiece = existPieces.get(existPieces.size() - 1);
		int end = lastPiece.getOffset() + lastPiece.getLength();
		if(end < fileSize) {
			Download.download("url", end, fileSize - end, contentHash, Constant.SAVE_PATH);
		}
		
		//从peer节点中下载的片段list
		return new Merge().mergePeer(sourcePieces);
	}
}
