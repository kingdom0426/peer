package cn.cnic.peer.connect;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

import org.json.JSONObject;

import cn.cnic.peer.cons.Constant;
import cn.cnic.peer.entity.Piece;

public class UDP {

	public static void submitNATTraversalAssist(DatagramSocket ds, String targetPeerID, String contentHash) {
		try {
			JSONObject json = new JSONObject();
			json.put(Constant.ACTION, Constant.ACTION_NAT_TRAVERSAL_ASSIST);
			json.put(Constant.PEER_ID, Constant.PEER_ID_VALUE);
			json.put(Constant.TARGET_PEER_ID, targetPeerID);
			json.put(Constant.CONTENT_HASH, contentHash);
			String data = json.toString();
			DatagramPacket p = new DatagramPacket(data.getBytes(), data.getBytes().length, InetAddress.getByName(Constant.TRACKER_IP), Constant.TRACKER_UDP_PORT);
			ds.send(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void submitNATTraversalOrder(DatagramSocket ds, String targetPeerID, String targetPeerIP, int targetPeerPort) {
		try {
			JSONObject json = new JSONObject();
			json.put(Constant.ACTION, Constant.ACTION_NAT_TRAVERSAL_ORDER);
			json.put(Constant.TARGET_PEER_ID, targetPeerID);
			json.put(Constant.TARGET_PEER_IP, targetPeerIP);
			json.put(Constant.TARGET_PEER_PORT, targetPeerPort);
			String data = json.toString();
			DatagramPacket p = new DatagramPacket(data.getBytes(), data.getBytes().length, InetAddress.getByName(targetPeerIP), targetPeerPort);
			ds.send(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void submitP2PhandShakeRequest(DatagramSocket ds, String contentHash, String targetPeerIP, int targetPeerPort) {
		try {
			JSONObject json = new JSONObject();
			json.put(Constant.ACTION, Constant.ACTION_P2P_HANDSHAKE_REQUEST);
			json.put(Constant.PEER_ID, Constant.PEER_ID_VALUE);
			json.put(Constant.CONTENT_HASH, contentHash);
			String data = json.toString();
			DatagramPacket p = new DatagramPacket(data.getBytes(), data.getBytes().length, InetAddress.getByName(targetPeerIP), targetPeerPort);
			ds.send(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void submitP2PhandShakeResponse(DatagramSocket ds, String contentHash, List<Piece> pieces, String targetPeerIP, int targetPeerPort) {
		try {
			JSONObject json = new JSONObject();
			json.put(Constant.ACTION, Constant.ACTION_P2P_HANDSHAKE_RESPONSE);
			json.put(Constant.PEER_ID, Constant.PEER_ID_VALUE);
			json.put(Constant.CONTENT_HASH, contentHash);
			json.put(Constant.PIECES, pieces);
			String data = json.toString();
			DatagramPacket p = new DatagramPacket(data.getBytes(), data.getBytes().length, InetAddress.getByName(targetPeerIP), targetPeerPort);
			ds.send(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void submitP2PPieceRequest(DatagramSocket ds, String contentHash, int requestOffset, int requestLength, String targetPeerIP, int targetPeerPort) {
		try {
			JSONObject json = new JSONObject();
			json.put(Constant.ACTION, Constant.ACTION_P2P_PIECE_REQUEST);
			json.put(Constant.PEER_ID, Constant.PEER_ID_VALUE);
			json.put(Constant.CONTENT_HASH, contentHash);
			json.put(Constant.REQUEST_OFFSET, requestOffset);
			json.put(Constant.REQUEST_LENGTH, requestLength);
			String data = json.toString();
			DatagramPacket p = new DatagramPacket(data.getBytes(), data.getBytes().length, InetAddress.getByName(targetPeerIP), targetPeerPort);
			ds.send(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void submitP2PPieceResponse(DatagramSocket ds, String contentHash, int dataOffset, int dataLength, String targetPeerIP, int targetPeerPort, byte[] bytes) {
		try {
			JSONObject json = new JSONObject();
			json.put(Constant.ACTION, Constant.ACTION_P2P_PIECE_RESPONSE);
			json.put(Constant.PEER_ID, Constant.PEER_ID_VALUE);
			json.put(Constant.CONTENT_HASH, contentHash);
			json.put(Constant.DATA_OFFSET, dataOffset);
			json.put(Constant.DATA_LENGTH, dataLength);
			json.put(Constant.DATA, bytes);
			String data = json.toString();
			DatagramPacket p = new DatagramPacket(data.getBytes(), data.getBytes().length, InetAddress.getByName(targetPeerIP), targetPeerPort);
			ds.send(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
