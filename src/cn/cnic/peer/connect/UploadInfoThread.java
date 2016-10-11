package cn.cnic.peer.connect;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
	
	//周期性地执行上传信息任务
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
			submitCPUUseRate(getSDTotalSize(), getSDAvailableSize());//Peer本地存储总量、可用存储总量
			submitCPUUseInfo(getProcessCpuRate(), (int)((float)getPeerRomSize()/getRomTotalSize()*100));//Peer节点CPU、内存占用率
			try {
				Thread.sleep(Constant.UPLOAD_INFO_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
	private void submitCPUUseRate(long dTotal, long dUsage) {
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
	private void submitCPUUseInfo(float cpu, int mem) {
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
	
	/**
	 * 获取节点内存占用大小
	 * @return
	 */
	private long getPeerRomSize() {
		return Runtime.getRuntime().totalMemory();
	}
	
	public static float getProcessCpuRate()
    {
        
        float totalCpuTime1 = getTotalCpuTime();
        float processCpuTime1 = getAppCpuTime();
        try
        {
            Thread.sleep(1000);
            
        }
        catch (Exception e)
        {
        }
        
        float totalCpuTime2 = getTotalCpuTime();
        float processCpuTime2 = getAppCpuTime();
        
        float cpuRate = 100 * (processCpuTime2 - processCpuTime1)
                / (totalCpuTime2 - totalCpuTime1);
        
        return cpuRate;
    }
	
	private static long getTotalCpuTime() { // 获取系统总CPU使用时间
	    String[] cpuInfos = null;
	    try {
	        BufferedReader reader = new BufferedReader(new InputStreamReader(
	                new FileInputStream("/proc/stat")), 1000);
	        String load = reader.readLine();
	        reader.close();
	        cpuInfos = load.split(" ");
	    } catch (IOException ex) {
	        ex.printStackTrace();
	    }
	    long totalCpu = Long.parseLong(cpuInfos[2])
	            + Long.parseLong(cpuInfos[3]) + Long.parseLong(cpuInfos[4])
	            + Long.parseLong(cpuInfos[6]) + Long.parseLong(cpuInfos[5])
	            + Long.parseLong(cpuInfos[7]) + Long.parseLong(cpuInfos[8]);
	    return totalCpu;
	}
	 
	private static long getAppCpuTime() { // 获取应用占用的CPU时间
	    String[] cpuInfos = null;
	    try {
	        int pid = android.os.Process.myPid();
	        BufferedReader reader = new BufferedReader(new InputStreamReader(
	                new FileInputStream("/proc/" + pid + "/stat")), 1000);
	        String load = reader.readLine();
	        reader.close();
	        cpuInfos = load.split(" ");
	    } catch (IOException ex) {
	        ex.printStackTrace();
	    }
	    long appCpuTime = Long.parseLong(cpuInfos[13])
	            + Long.parseLong(cpuInfos[14]) + Long.parseLong(cpuInfos[15])
	            + Long.parseLong(cpuInfos[16]);
	    return appCpuTime;
	}
	
	private String getCurrentTime() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}
}
