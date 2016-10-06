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
	 * ����������map�����ж϶Զ�peerlist�ǲ��Ƕ��ѷ��ؽ��
	 * ����Ѷ����أ�total == current�����Ϳɽ�����Ƶƴ�ӣ�Ȼ�����peer����Ƭ�����󣬲���map��ɾ����contentHash
	 */
	public static Map<String, Integer> mapTotal = new HashMap<String, Integer>();
	public static Map<String, Integer> mapCurrent = new HashMap<String, Integer>();
	
	//���map������¼contentHash������Ƭ��
	private Map<String, List<Piece>> mapPiece = new HashMap<String, List<Piece>>();
	
	public void run() {
		Log.d("peer", "������UDP�̣߳�����PEER֮�����ͨ��");
		try {
			DatagramSocket ds = new DatagramSocket(Constant.TRACKER_UDP_PORT);

			// ���������߳�
			new Thread(new HeartThread(ds, Constant.PEER_ID_VALUE)).start();

			// ѭ������
			byte[] buf = new byte[1024];
			DatagramPacket rp = new DatagramPacket(buf, 1024);
			boolean isEnd = false;
			while (!isEnd) {
				ds.receive(rp);
				// ȡ����Ϣ
				String content = new String(rp.getData(), 0, rp.getLength());
				JSONObject json = new JSONObject(content);
				String contentHash = json.getString(Constant.CONTENT_HASH);
				String action = json.getString(Constant.ACTION);
				
				//Tracker����Peer��UDP������Ӧ�����ݰ���Peer�Ĺ���IP��ַ�Ͷ˿ڣ�Peer�������ݴ���Ϣ�ж��Լ����������ͣ����Ƿ�ΪNAT����PubIP������LocalIP����ʱ�����ã�
				if(action.equals(Constant.ACTION_HEARTBEAT_RESPONSE)) {
					
				} 
				
				//UDP��͸��Ӧ���յ�����tracker�Ĵ�͸��Ӧ��peer��Զ�peer���д򶴣��ڴ˽������δ�
				else if(action.equals(Constant.ACTION_NAT_TRAVERSAL_ORDER)) {
					String targetPeerIP = json.getString(Constant.TARGET_PEER_IP);
					String targetPeerPort = json.getString(Constant.TARGET_PEER_PORT);
					makeHole(ds, targetPeerIP, Integer.parseInt(targetPeerPort));
					makeHole(ds, targetPeerIP, Integer.parseInt(targetPeerPort));
					makeHole(ds, targetPeerIP, Integer.parseInt(targetPeerPort));
				} 
				
				//�յ���������󣬷��ر�����ӵ�еı���Ƭ�Σ�������������Ӧ
				else if(action.equals(Constant.ACTION_P2P_HANDSHAKE_REQUEST)) {
//					List<Piece> pieces = DB.getPiecesByContentHash(contentHash);
//					submitP2PhandShakeResponse(ds, contentHash, pieces, ds.getInetAddress().getHostName(), ds.getPort());
				} 
				
				//�յ�������Ӧ���ж��Ƿ���ȫ�����أ�����ǣ������Ƶ����ƴ��
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
					//ȫ������
					if(total == current) {
						//�ļ��ܴ�С����λ��kb��
						int size = 10000;
						//������Ƶƴ�ӣ��õ���Ҫ�������ƵƬ��
						List<Piece> resultPieces = generateFinalPieces(mapPiece.get(contentHash), size, ds, contentHash);
						
						//ƴ����ɺ󽫼�¼��mapPiece��ɾ��
						mapPiece.remove(contentHash);
						
						//���η�����������
						for(Piece p : resultPieces) {
							UDP.submitP2PPieceRequest(ds, contentHash, p.getOffset(), p.getLength(), p.getPeer().getUdpIp(), p.getPeer().getUdpPort());
						}
						
						//��������Ϻ󣬽������������map������
						mapTotal.remove(contentHash);
						mapCurrent.remove(contentHash);
					}
				} 
				
				//�յ���������������󷽷��ͱ�������
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
				
				//���յ��Զ�peer������������Ӧ��
				else if(action.equals(Constant.ACTION_P2P_PIECE_RESPONSE)) {
					
					//��contentHash��Ϊ�ļ�������������ڣ��ʹ���
					File f = new File(Constant.SAVE_PATH + contentHash);
					if(!f.exists()) {
						f.createNewFile();
					}
					
					//������д���ļ���
					DataOutputStream fileOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
					fileOut.write(json.get(Constant.DATA).toString().getBytes(), (Integer)json.get(Constant.DATA_OFFSET), (Integer)json.get(Constant.DATA_LENGTH));
					fileOut.close();
					
					//���±������ݿ��еļ�¼
					DB.updatePiece(contentHash, (Integer)json.get(Constant.DATA_OFFSET), (Integer)json.get(Constant.DATA_LENGTH));
				} 
				
				//����
				else {
					
				}
				
				
				//�������Ҫ��ȡ�����ݣ�����peer��������
				if(segments.size() > 0) {
					for(int i = 0; i < segments.size(); i++) {
						Segment s = segments.get(i);
						
						//���α���tracker���ص�ÿһ��peer
						for(int j = 0; j < s.getPeerList().size(); i ++) {
							Peer p = s.getPeerList().get(j);
							//peer��tracker����Э����͸����
							UDP.submitNATTraversalAssist(ds, p.getPeerID(), s.getContentHash());
							//�½�һ���̣߳����߳����ӳ����룬��ȥ�������������ӳ������Ŀ�����öԶ˵�peer�ڵ���ʱ��ȥ���д򶴣�
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
			json.put("info", "��");
			String data = json.toString();
			DatagramPacket p = new DatagramPacket(data.getBytes(), data.getBytes().length, InetAddress.getByName(targetPeerIP), targetPeerPort);
			ds.send(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ###########��Ƶƴ���㷨#############
	 * 1.�ҳ����һ�����ݣ�����
	 * 
	 * ###################################
	 * @param sourcePieces peer�ṩ������Ƭ��
	 * @param size �ļ���С
	 * @return ������Ҫ���������Ƭ��
	 */
	public List<Piece> generateFinalPieces(List<Piece> sourcePieces, int size, DatagramSocket ds, String contentHash) {
		//ȷ�����CDN�����ص�Ƭ��list���Ȼ�ȡ�����ؾ������е�Ƭ�β�����ʣ�µľ�����Ҫ��CDN�ϻ�ȡ��
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
		
		//��peer�ڵ������ص�Ƭ��list
		return new Merge().mergePeer(sourcePieces);
	}
	
	public void downloadFromCDN(String url, int offset, int length) {
		
	}
}
