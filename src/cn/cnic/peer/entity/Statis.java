package cn.cnic.peer.entity;

public class Statis {

	private String peerID;
	private String infohash;//内容hash
	private String peerCnt;//peer连接的节点数
	private String NATSuccessRate;//穿透成功率
	private String schedulingDelay;//调度延时
	private String sliceDelay;//分片延时
	
	private String uploadVolume;//上行流量
	private String downloadVolume;//下行流量
	
	private String uploadPeakRate;//上传带宽峰值
	private String downloadPeakRate; //下行带宽峰值
	
	private String logTime;//记录时间
	
	private String timeStart;//统计开始时间
	private String timeEnd;//统计结束时间
	public String getPeerID() {
		return peerID;
	}
	public void setPeerID(String peerID) {
		this.peerID = peerID;
	}
	public String getInfohash() {
		return infohash;
	}
	public void setInfohash(String infohash) {
		this.infohash = infohash;
	}
	public String getPeerCnt() {
		return peerCnt;
	}
	public void setPeerCnt(String peerCnt) {
		this.peerCnt = peerCnt;
	}
	public String getNATSuccessRate() {
		return NATSuccessRate;
	}
	public void setNATSuccessRate(String nATSuccessRate) {
		NATSuccessRate = nATSuccessRate;
	}
	public String getSchedulingDelay() {
		return schedulingDelay;
	}
	public void setSchedulingDelay(String schedulingDelay) {
		this.schedulingDelay = schedulingDelay;
	}
	public String getSliceDelay() {
		return sliceDelay;
	}
	public void setSliceDelay(String sliceDelay) {
		this.sliceDelay = sliceDelay;
	}
	public String getUploadVolume() {
		return uploadVolume;
	}
	public void setUploadVolume(String uploadVolume) {
		this.uploadVolume = uploadVolume;
	}
	public String getDownloadVolume() {
		return downloadVolume;
	}
	public void setDownloadVolume(String downloadVolume) {
		this.downloadVolume = downloadVolume;
	}
	public String getUploadPeakRate() {
		return uploadPeakRate;
	}
	public void setUploadPeakRate(String uploadPeakRate) {
		this.uploadPeakRate = uploadPeakRate;
	}
	public String getDownloadPeakRate() {
		return downloadPeakRate;
	}
	public void setDownloadPeakRate(String downloadPeakRate) {
		this.downloadPeakRate = downloadPeakRate;
	}
	public String getLogTime() {
		return logTime;
	}
	public void setLogTime(String logTime) {
		this.logTime = logTime;
	}
	public String getTimeStart() {
		return timeStart;
	}
	public void setTimeStart(String timeStart) {
		this.timeStart = timeStart;
	}
	public String getTimeEnd() {
		return timeEnd;
	}
	public void setTimeEnd(String timeEnd) {
		this.timeEnd = timeEnd;
	}
	
	public void baseSet(String peerID,String timeStart,String timeEnd){
		this.peerID = peerID;
		this.timeStart = timeStart;
		this.timeEnd = timeEnd;
	}
	
	public void volumeSet(String uploadVolume,String downloadVolume){
		this.uploadVolume = uploadVolume;
		this.downloadVolume = downloadVolume;
	}
}
