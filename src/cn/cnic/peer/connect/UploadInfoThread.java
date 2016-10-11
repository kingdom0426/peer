package cn.cnic.peer.connect;

import java.io.BufferedWriter;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Environment;
import android.os.StatFs;
import cn.cnic.peer.cons.Constant;
import cn.cnic.peer.entity.Statis;

public class UploadInfoThread implements Runnable {
	private String peerID;
	private BufferedWriter writer;
	
	public static List<Statis>  peerConnectList = null;
	public static int NATAllCount = 0;
	public static int NATSuccessCount = 0;

	public static List<Statis>  uploadVolumeList = null;
	public static List<Statis>  uploadVolumetList = null;
	
	public static byte uploadPeakRate = 0;//每个ts上传的容量/时间
	public static byte downloadPeakRate = 0;//每个ts下载的容量/时间
	
	private void initParma(){
		peerConnectList = new ArrayList<Statis>();
		NATAllCount = 0;
		NATSuccessCount = 0;

		uploadVolumeList = new ArrayList<Statis>();
		uploadVolumetList = new ArrayList<Statis>();
		
		uploadPeakRate = 0;
		downloadPeakRate = 0;
	}
	
	public UploadInfoThread(String peerID, BufferedWriter writer) {
		this.peerID = peerID;
		this.writer = writer;
	}

	public void run() {
		while(true) {
			doSubmitWork();
		}
	}
	
	//周期性地执行上传信息任务
	public void doSubmitWork() {
			try {
				//submitNATSuccessRate(infoHash,NATSuccessRate,timeStart,timeEnd)
				initParma();
				Thread.sleep(Constant.UPLOAD_INFO_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}
	

	/**
	 * Peer节点获取内容时连接的Peer节点数
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
	 * Peer节点媒体数据连接NAT穿越成功率
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
			json.put("NATSuccessRate", "");
			TCPThread.send(json, "/api/peer/natsuccrate", writer);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Peer节点调度系统服务时延
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
	 * 切片请求时延
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
	 * Peer节点北向上行、下行流量
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
	 * Peer节点北向上行、下行带宽峰值
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
	 * Peer本地存储总量、可用存储总量
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
	 * Peer节点CPU、内存占用率
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
	* 获得SD卡总大小 
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
	* 获得sd卡剩余容量，即可用大小 
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
	* 获得机身内存总大小 
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
	* 获得机身可用内存 
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
