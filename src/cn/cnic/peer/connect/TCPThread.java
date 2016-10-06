package cn.cnic.peer.connect;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.cnic.peer.cons.Constant;
import cn.cnic.peer.download.Download;
import cn.cnic.peer.entity.Peer;
import cn.cnic.peer.entity.Segment;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class TCPThread implements Runnable {
	
	private Socket tcp;
	private BufferedWriter writer;
	private String peerID;
	
	//���ڴ洢����������
	public static List<IHTTPSession> sessions = new ArrayList<IHTTPSession>();
	
	public TCPThread(String peerID) {
		try {
			tcp = new Socket(Constant.TRACKER_IP, Constant.TRACKER_TCP_PORT);
			writer = new BufferedWriter(new OutputStreamWriter(tcp.getOutputStream()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.peerID = peerID;
	}

	public void run() {
		System.out.println("������TCP�̣߳�������TRACKER��ͨ");
		
		try {
			//�����ϴ���Ϣ�̣߳��������ϴ�����
			new Thread(new UploadInfoThread(peerID)).start();
			
			//���ϻ�ȡtracker����������
			boolean isEnd = false;
			while (!isEnd) {
				String data = receive(tcp);
				if(!data.equals("") && data != null) {
					JSONObject json = new JSONObject(data);
					if(json.has(Constant.PEER_LIST)) {
						JSONArray array = json.getJSONArray(Constant.PEER_LIST);
						List<Peer> peerList = new ArrayList<Peer>();
						for(int i = 0; i < array.length(); i ++) {
							Peer peer = new Peer();
							JSONObject peerJson = new JSONObject(array.get(i).toString());
							peer.setPeerID(peerJson.getString(Constant.PEER_ID));
							peer.setUdpIp(peerJson.getString(Constant.UDP_IP));
							peer.setUdpPort(peerJson.getInt(Constant.UDP_PORT));
							peerList.add(peer);
						}
						
						//���tracker�д��ڣ��Ͱ�tracker��ָʾȥ����
						if(peerList.size() > 0) {
							Segment seg = new Segment();
							seg.setContentHash(json.getString(Constant.CONTENT_HASH));
							seg.setUrlHash(json.getString(Constant.URL_HASH));
							seg.setPeerList(peerList);
							UDPThread.segments.add(seg);
							UDPThread.mapTotal.put(json.getString(Constant.CONTENT_HASH), peerList.size());
							UDPThread.mapCurrent.put(json.getString(Constant.CONTENT_HASH), 0);
						}
						
						//����������в����ڣ��ʹ�cdn������
						else {
							String url = json.getString(Constant.URL_HASH);
							Download.downloadAll(url, url.substring(url.lastIndexOf("/"), url.length()));
						}
					}
				}
				
				if(sessions.size() > 0) {
					for(int i = 0; i < sessions.size(); i++) {
						//����URL����
						queryPeerList(peerID, sessions.get(i).getUri());
						
						//���б�������˼�¼
						sessions.remove(i);
					}
				}
				
				Thread.sleep(Constant.RECEIVE_INTERVAL);
			}
			tcp.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Peer��Tracker���͵�����ָ��
	 * @param peerID Peer����������ÿ��Peer�ڵ�Ψһ�������ڳ�ʼ������ɣ�֮��һֱ���ã�һ����SHA1��ϣ�㷨��ȡ20�ֽ�ֵ����40�ֽڿɴ�ӡ�ַ�����
	 * @param URLHash Peer���������������URL��ϣֵ
	 */
	private void queryPeerList(String peerID, String URLHash) {
		try {
			JSONObject json = new JSONObject();
			json.put(Constant.PEER_ID, peerID);
			json.put(Constant.URL_HASH, URLHash);
			send(json);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void send(JSONObject json) {
		try {
			writer.write(json.toString());
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String receive(Socket socket) {
		String data = "";
		try {
			DataInputStream input = new DataInputStream(socket.getInputStream());    
			byte[] buffer;
			buffer = new byte[input.available()];
			if(buffer.length != 0){
				input.read(buffer);
				data = new String(buffer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}
}