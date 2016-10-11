package cn.cnic.peer.connect;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Environment;
import android.util.Log;

import cn.cnic.peer.cons.Constant;
import cn.cnic.peer.download.Download;
import cn.cnic.peer.entity.Http;
import cn.cnic.peer.entity.Peer;
import cn.cnic.peer.entity.Segment;
import cn.cnic.peer.sha1.SHA1;
import cn.cnic.peer.util.Global;

public class TCPThread implements Runnable {
	
	public void run() {
		Log.d("peer", "������TCP�̣߳�������TRACKER��ͨ");
		
		try {

			Socket tcp = new Socket(Constant.TRACKER_IP, Constant.TRACKER_TCP_PORT);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(tcp.getOutputStream()));
			//�����ϴ���Ϣ�̣߳��������ϴ�����
			new Thread(new UploadInfoThread(Constant.PEER_ID_VALUE, writer)).start();
			
			//���ϻ�ȡtracker����������
			boolean isEnd = false;
			while (!isEnd) {
				String data = receive(tcp);
				Log.d("tcpmessage", data);
				if (data!=null && !"".equals(data)) {
					Http http = parseData(data);
					String jsonData = http.getJson();
					if (!jsonData.equals("") && jsonData != null) {
						JSONObject json = new JSONObject(jsonData);
						if (json.has(Constant.PEER_LIST)) {
							JSONArray array = json.getJSONArray(Constant.PEER_LIST);
							List<Peer> peerList = new ArrayList<Peer>();
							for (int i = 0; i < array.length(); i++) {
								Peer peer = new Peer();
								JSONObject peerJson = new JSONObject(array.get(i).toString());
								peer.setPeerID(peerJson.getString(Constant.PEER_ID));
								peer.setUdpIp(peerJson.getString(Constant.UDP_IP));
								peer.setUdpPort(peerJson.getInt(Constant.UDP_PORT));
								peerList.add(peer);
							}

							//���tracker�д��ڣ��Ͱ�tracker��ָʾȥ����
							if (peerList.size() > 0) {
								Segment seg = new Segment();
								seg.setContentHash(json.getString(Constant.CONTENT_HASH));
								seg.setUrlHash(json.getString(Constant.URL_HASH));
								seg.setPeerList(peerList);
								Global.UDP_SEGMENTS.add(seg);
								Global.UDP_QUERY_TOTAL.put(json.getString(Constant.CONTENT_HASH),peerList.size());
								Global.UDP_QUERY_CUR.put(json.getString(Constant.CONTENT_HASH),0);
							}

							//����������в����ڣ��ʹ�cdn������
							else {
								String url = json.getString(Constant.URL_HASH);
								Download.downloadAll(url, json.getString(Constant.CONTENT_HASH), Constant.SAVE_PATH);
							}
						}
					}
				}
				if(Global.TCP_REQUESTS.size() > 0) {
					for(int i = 0; i < Global.TCP_REQUESTS.size(); i++) {
						//����URL����
						queryPeerList(Constant.PEER_ID_VALUE, Global.TCP_REQUESTS.get(i).getParms().get("srcURL"), writer);
						
						//���б�������˼�¼
						Global.TCP_REQUESTS.remove(i);
					}
				}
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
	private void queryPeerList(String peerID, String url, BufferedWriter writer) {
		String urlHash = null;
		try {
			urlHash = SHA1.sha1(url);
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		if ("08f12c16dd8742974f539ad374e108f7c5cb914e".equals(urlHash)) {
			try {
				JSONObject json = new JSONObject();
				json.put(Constant.PEER_ID, peerID);
				json.put(Constant.URL_HASH, urlHash);
				send(json, "/get_peerlist", writer);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			Download.downloadAll(url, "290c6e19fbe81c535d58c7ea7283f95c2339b70a", Environment.getExternalStorageDirectory().getPath() + "/");
		}
	}
	
	public static void send(JSONObject json, String url, BufferedWriter writer) {
		StringBuffer sb = new StringBuffer();
		sb.append("POST " + url + " HTTP/1.1\r\n");
		sb.append("Host: " + Constant.LOCAL_SERVER_IP + "\r\n");
		sb.append("Content-Length: "+json.toString().length()+"\r\n");
		sb.append("Connection: Keep-Alive\r\n\r\n");
		sb.append(json.toString());
		try {
			writer.write(sb.toString());
			writer.flush();
			Log.d("tcpsend", sb.toString());
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
	
	public Http parseData(String data) {
		Http http = new Http();
		String[] datas = data.split("\r\n");
		http.setProtocol(datas[0]);
		http.setServer(datas[1]);
		http.setContentLength(datas[2]);
		http.setConnection(datas[3]);
		http.setJson(datas[5]);
		return http;
	}
}
