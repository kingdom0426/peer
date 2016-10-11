package cn.cnic.peer.connect;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.example.peer.VideoServer;

import android.os.Environment;
import android.util.Log;

import cn.cnic.peer.cons.Constant;
import cn.cnic.peer.download.Download;
import cn.cnic.peer.entity.Peer;
import cn.cnic.peer.entity.Piece;
import cn.cnic.peer.entity.Segment;
import cn.cnic.peer.merge.Merge;
import cn.cnic.peer.util.Global;

public class UDPThread implements Runnable {
	
	public void run() {
		Log.d("peer", "������UDP�̣߳�����PEER֮�����ͨ��");
		try {
			DatagramSocket ds = new DatagramSocket(Constant.TRACKER_UDP_PORT);

			// ���������߳�
			new Thread(new HeartThread(ds, Constant.PEER_ID_VALUE)).start();

			// ѭ������
			boolean isEnd = false;
			while (!isEnd) {
				byte[] buf = new byte[2048];
				DatagramPacket rp = new DatagramPacket(buf, 2048);
				ds.receive(rp);
				// ȡ����Ϣ
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
				
				//Tracker����Peer��UDP������Ӧ�����ݰ���Peer�Ĺ���IP��ַ�Ͷ˿ڣ�Peer�������ݴ���Ϣ�ж��Լ����������ͣ����Ƿ�ΪNAT����PubIP������LocalIP����ʱ�����ã�
				if(action.equals(Constant.ACTION_HEARTBEAT_RESPONSE)) {
					Constant.PEER_PUBLIC_IP = json.getString(Constant.PUBLIC_UDP_IP);
					Constant.PEER_PUBLIC_PORT = json.getInt(Constant.PUBLIC_UDP_PORT);
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
				
				//�յ�������Ӧ���ж��Ƿ���ȫ�����أ�����ǣ������Ƶ����ƴ��
				else if(action.equals(Constant.ACTION_P2P_HANDSHAKE_RESPONSE)) {
					UploadInfoThread.NATSuccessCount ++;
					String contentHash = json.getString(Constant.CONTENT_HASH);
					Global.UDP_QUERY_CUR.put(contentHash, Global.UDP_QUERY_CUR.get(contentHash) + 1);
					JSONArray pieces = new JSONArray(json.get(Constant.PIECES).toString());
					if(!Global.UDP_DATA_RECEIVE.containsKey(contentHash)) {
						Global.UDP_DATA_RECEIVE.put(contentHash, new ArrayList<Piece>());
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
						Global.UDP_DATA_RECEIVE.get(contentHash).add(p);
					}
					int total = Global.UDP_QUERY_TOTAL.get(contentHash);
					int current = Global.UDP_QUERY_CUR.get(contentHash);
					//ȫ������
					if(total == current) {
						//�ļ��ܴ�С����λ��b��
						int fileSize = 467556;
						//������Ƶƴ�ӣ��õ���Ҫ�������ƵƬ��
						List<Piece> resultPieces = generateFinalPieces(Global.UDP_DATA_RECEIVE.get(contentHash), fileSize, ds, contentHash);
						
						//ƴ����ɺ󽫼�¼��mapPiece��ɾ��
						Global.UDP_DATA_RECEIVE.remove(contentHash);
						
						Global.UDP_DATA_TOTAL.put(contentHash, resultPieces.size());
						Global.UDP_DATA_CUR.put(contentHash, 0);
						//���η�����������
						for(Piece p : resultPieces) {
							UDP.submitP2PPieceRequest(ds, contentHash, p.getOffset(), p.getLength(), json.getString(Constant.PUBLIC_UDP_IP), json.getInt(Constant.PUBLIC_UDP_PORT));
						}
						
						//��������Ϻ󣬽������������map������
						Global.UDP_QUERY_TOTAL.remove(contentHash);
						Global.UDP_QUERY_CUR.remove(contentHash);
					}
				} 
				
				//�յ���������������󷽷��ͱ�������
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
				
				//���յ��Զ�peer������������Ӧ��
				else if(action.equals(Constant.ACTION_P2P_PIECE_RESPONSE)) {
					String contentHash = json.getString(Constant.CONTENT_HASH);
					//��contentHash��Ϊ�ļ�������������ڣ��ʹ���
					File f = new File(Environment.getExternalStorageDirectory() + "/" + contentHash);
					if(!f.exists()) {
						f.createNewFile();
					}
					
					//������д���ļ���
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
					
					Global.UDP_DATA_CUR.put(contentHash, Global.UDP_DATA_CUR.get(contentHash) + 1);
					int total = Global.UDP_DATA_TOTAL.get(contentHash);
					int current = Global.UDP_DATA_CUR.get(contentHash);
					if(total == current) {
						Global.UDP_DATA_CUR.remove(contentHash);
						Global.UDP_DATA_TOTAL.remove(contentHash);
						VideoServer.over = true;
					}
					Log.d("fileLength", f.length()+"");
					
					//���±������ݿ��еļ�¼
//					DB.updatePiece(contentHash, (Integer)json.get(Constant.DATA_OFFSET), (Integer)json.get(Constant.DATA_LENGTH));
				} 
				
				//����
				else {
					
				}
				
				
				//�������Ҫ��ȡ�����ݣ�����peer��������
				if(Global.UDP_SEGMENTS.size() > 0) {
					for(int i = 0; i < Global.UDP_SEGMENTS.size(); i++) {
						Segment s = Global.UDP_SEGMENTS.get(i);
						
						//���α���tracker���ص�ÿһ��peer
						for(int j = 0; j < s.getPeerList().size(); j ++) {
							Peer p = s.getPeerList().get(j);
							//peer��tracker����Э����͸����
							UDP.submitNATTraversalAssist(ds, p.getPeerID(), s.getContentHash());
							//�½�һ���̣߳����߳����ӳ����룬��ȥ�������������ӳ������Ŀ�����öԶ˵�peer�ڵ���ʱ��ȥ���д򶴣�
							new Thread(new HandShakeThread(ds, Constant.PEER_ID_VALUE, p.getUdpIp(), p.getUdpPort(), s.getContentHash())).start();
						}
						Global.UDP_SEGMENTS.remove(i);
					}
				}
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
			json.put("info", "��");
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
	 * ###########��Ƶƴ���㷨#############
	 * 1.�ҳ����һ�����ݣ�����
	 * 
	 * ###################################
	 * @param sourcePieces peer�ṩ������Ƭ��
	 * @param size �ļ���С
	 * @return ������Ҫ���������Ƭ��
	 */
	public List<Piece> generateFinalPieces(List<Piece> sourcePieces, int fileSize, DatagramSocket ds, String contentHash) {
		//ȷ�����CDN�����ص�Ƭ��list���Ȼ�ȡ�����ؾ������е�Ƭ�β�����ʣ�µľ�����Ҫ��CDN�ϻ�ȡ��
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
		
		//��peer�ڵ������ص�Ƭ��list
		return new Merge().mergePeer(sourcePieces);
	}
}
