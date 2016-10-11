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
		Log.d("peer", "已启动TCP线程，用于与TRACKER沟通");
		
		try {

			Socket tcp = new Socket(Constant.TRACKER_IP, Constant.TRACKER_TCP_PORT);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(tcp.getOutputStream()));
			//启动上传信息线程，周期性上传数据
			new Thread(new UploadInfoThread(Constant.PEER_ID_VALUE, writer)).start();
			
			//不断获取tracker发来的数据
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

							//如果tracker中存在，就按tracker的指示去下载
							if (peerList.size() > 0) {
								Segment seg = new Segment();
								seg.setContentHash(json.getString(Constant.CONTENT_HASH));
								seg.setUrlHash(json.getString(Constant.URL_HASH));
								seg.setPeerList(peerList);
								Global.UDP_SEGMENTS.add(seg);
								Global.UDP_QUERY_TOTAL.put(json.getString(Constant.CONTENT_HASH),peerList.size());
								Global.UDP_QUERY_CUR.put(json.getString(Constant.CONTENT_HASH),0);
							}

							//如果局域网中不存在，就从cdn中下载
							else {
								String url = json.getString(Constant.URL_HASH);
								Download.downloadAll(url, json.getString(Constant.CONTENT_HASH), Constant.SAVE_PATH);
							}
						}
					}
				}
				if(Global.TCP_REQUESTS.size() > 0) {
					for(int i = 0; i < Global.TCP_REQUESTS.size(); i++) {
						//发送URL请求
						queryPeerList(Constant.PEER_ID_VALUE, Global.TCP_REQUESTS.get(i).getParms().get("srcURL"), writer);
						
						//从列表中清除此记录
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
	 * Peer给Tracker发送的请求指令
	 * @param peerID Peer的特征串，每个Peer节点唯一，可以在初始随机生成，之后一直沿用（一般用SHA1哈希算法获取20字节值，即40字节可打印字符串）
	 * @param URLHash Peer所请求的下载任务URL哈希值
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
