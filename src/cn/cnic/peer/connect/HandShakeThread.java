package cn.cnic.peer.connect;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.json.JSONObject;

import cn.cnic.peer.cons.Constant;

public class HandShakeThread implements Runnable {
	
	private String contentHash;
	private String targetPeerIP;
	private int targetPeerPort;
	private String peerID;
	private DatagramSocket ds;
	
	public HandShakeThread(DatagramSocket ds, String peerID, String targetPeerIP, int targetPeerPort, String contentHash) {
		this.ds = ds;
		this.peerID = peerID;
		this.targetPeerIP = targetPeerIP;
		this.targetPeerPort = targetPeerPort;
		this.contentHash = contentHash;
	}

	public void run() {
		try {
			Thread.sleep(2000);
			JSONObject json = new JSONObject();
			json.put(Constant.ACTION, Constant.ACTION_P2P_HANDSHAKE_REQUEST);
			json.put(Constant.PEER_ID, peerID);
			json.put(Constant.CONTENT_HASH, contentHash);
			json.put(Constant.PUBLIC_UDP_IP, Constant.PEER_PUBLIC_IP);
			json.put(Constant.PUBLIC_UDP_PORT, Constant.PEER_PUBLIC_PORT);
			String data = json.toString();
			DatagramPacket p = new DatagramPacket(data.getBytes(), data.getBytes().length, InetAddress.getByName(targetPeerIP), targetPeerPort);
			ds.send(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
