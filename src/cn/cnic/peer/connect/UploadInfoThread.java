package cn.cnic.peer.connect;

import java.io.BufferedWriter;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.peer.MainActivity;

import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

import cn.cnic.peer.cons.Constant;

public class UploadInfoThread implements Runnable {
	private String peerID;
	private BufferedWriter writer;
	
	public UploadInfoThread(String peerID, BufferedWriter writer) {
		this.peerID = peerID;
		this.writer = writer;
	}

	public void run() {
		while(true) {
			doSubmitWork();
			try {
				Thread.sleep(Constant.UPLOAD_INFO_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	//�����Ե�ִ���ϴ���Ϣ����
	public void doSubmitWork() {
		while(true) {
			//
			//
			//
			//
			//
			//
			//
			//
			//
			//
			//
			//
			//
			//
			//
			try {
				Thread.sleep(Constant.UPLOAD_INFO_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	

	/**
	 * Peer�ڵ��ȡ����ʱ���ӵ�Peer�ڵ���
	 */
	private void submitPeerCnt(int peerCnt, String infoHash, String timeStart, String timeEnd) {
		try {
			JSONObject json = new JSONObject();
			json.put("peerID", Constant.PEER_ID_VALUE);
			json.put("infoHash", infoHash);
			json.put("peerCnt", peerCnt);
			json.put("timeStart", timeStart);
			json.put("timeEnd", timeEnd);
			TCPThread.send(json, "/api/peer/taskpeercnt", writer);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Peer�ڵ�ý����������NAT��Խ�ɹ���
	 * @param infoHash
	 * @param NATSuccessRate
	 * @param timeStart
	 * @param timeEnd
	 */
	private void submitNATSuccessRate(String infoHash, int NATSuccessRate, String timeStart, String timeEnd) {
		try {
			JSONObject json = new JSONObject();
			json.put("peerID", Constant.PEER_ID_VALUE);
			json.put("infoHash", infoHash);
			json.put("NATSuccessRate", NATSuccessRate);
			json.put("timeStart", timeStart);
			json.put("timeEnd", timeEnd);
			TCPThread.send(json, "/api/peer/natsuccrate", writer);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Peer�ڵ����ϵͳ����ʱ��
	 * @param infoHash
	 * @param schedulingDelay
	 */
	private void submitServiceDelay(String infoHash, int schedulingDelay) {
		try {
			JSONObject json = new JSONObject();
			json.put("peerID", Constant.PEER_ID_VALUE);
			json.put("infoHash", infoHash);
			json.put("schedulingDelay", schedulingDelay);
			json.put("logTime", getCurrentTime());
			TCPThread.send(json, "/api/peer/schedulingdelay", writer);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ��Ƭ����ʱ��
	 * @param infoHash
	 * @param sliceDelay
	 */
	private void submitPieceDelay(String infoHash, int sliceDelay) {
		try {
			JSONObject json = new JSONObject();
			json.put("peerID", Constant.PEER_ID_VALUE);
			json.put("infoHash", infoHash);
			json.put("sliceDelay", sliceDelay);
			json.put("logTime", getCurrentTime());
			TCPThread.send(json, "/api/peer/slicedelay", writer);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Peer�ڵ㱱�����С���������
	 * @param uploadVolume
	 * @param downloadVolume
	 * @param timeStart
	 * @param timeEnd
	 */
	private void submitVolume(int uploadVolume, int downloadVolume, String timeStart, String timeEnd) {
		try {
			JSONObject json = new JSONObject();
			json.put("PeerID", Constant.PEER_ID_VALUE);
			json.put("uploadVolume", uploadVolume);
			json.put("downloadVolume", downloadVolume);
			json.put("timeStart", timeStart);
			json.put("timeEnd", timeEnd);
			TCPThread.send(json, "/api/peer/peakrate", writer);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Peer�ڵ㱱�����С����д����ֵ
	 * @param uploadPeakRate
	 * @param downloadPeakRate
	 * @param timeStart
	 * @param timeEnd
	 */
	private void submitPeakRate(int uploadPeakRate, int downloadPeakRate, String timeStart, String timeEnd) {
		try {
			JSONObject json = new JSONObject();
			json.put("PeerID", Constant.PEER_ID_VALUE);
			json.put("uploadPeakRate", uploadPeakRate);
			json.put("downloadPeakRate", downloadPeakRate);
			json.put("timeStart", timeStart);
			json.put("timeEnd", timeEnd);
			TCPThread.send(json, "/api/peer/peakrate", writer);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Peer���ش洢���������ô洢����
	 * @param dTotal
	 * @param dUsage
	 */
	private void submitCPUUseRate(int dTotal, int dUsage) {
		try {
			JSONObject json = new JSONObject();
			json.put("type", "Peer");
			json.put("deviceID", Constant.PEER_ID_VALUE);
			json.put("dTotal", dTotal);
			json.put("dUsage", dUsage);
			json.put("logTime", getCurrentTime());
			TCPThread.send(json, "/api/device/disk", writer);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Peer�ڵ�CPU���ڴ�ռ����
	 * @param cpu
	 * @param mem
	 */
	private void submitCPUUseInfo(int cpu, int mem) {
		try {
			JSONObject json = new JSONObject();
			json.put("type", "Peer");
			json.put("deviceID", Constant.PEER_ID_VALUE);
			json.put("cpu", cpu);
			json.put("mem", mem);
			json.put("logTime", getCurrentTime());
			TCPThread.send(json, "/api/device/info", writer);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/** 
	* ���SD���ܴ�С 
	* 
	* @return 
	*/
	private long getSDTotalSize() { 
	  File path = Environment.getExternalStorageDirectory(); 
	  StatFs stat = new StatFs(path.getPath()); 
	  long blockSize = stat.getBlockSize(); 
	  long totalBlocks = stat.getBlockCount(); 
	  return blockSize * totalBlocks; 
	} 
	/** 
	* ���sd��ʣ�������������ô�С 
	* 
	* @return 
	*/
	private long getSDAvailableSize() { 
	  File path = Environment.getExternalStorageDirectory(); 
	  StatFs stat = new StatFs(path.getPath()); 
	  long blockSize = stat.getBlockSize(); 
	  long availableBlocks = stat.getAvailableBlocks(); 
	  return blockSize * availableBlocks; 
	} 
	/** 
	* ��û����ڴ��ܴ�С 
	* 
	* @return 
	*/
	private long getRomTotalSize() { 
	  File path = Environment.getDataDirectory(); 
	  StatFs stat = new StatFs(path.getPath()); 
	  long blockSize = stat.getBlockSize(); 
	  long totalBlocks = stat.getBlockCount(); 
	  return blockSize * totalBlocks; 
	} 
	/** 
	* ��û�������ڴ� 
	* 
	* @return 
	*/
	private long getRomAvailableSize() { 
	  File path = Environment.getDataDirectory(); 
	  StatFs stat = new StatFs(path.getPath()); 
	  long blockSize = stat.getBlockSize(); 
	  long availableBlocks = stat.getAvailableBlocks(); 
	  return blockSize * availableBlocks; 
	}
	
	private String getCurrentTime() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}
}
