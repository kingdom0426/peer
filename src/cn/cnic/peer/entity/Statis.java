package cn.cnic.peer.entity;

public class Statis {

	private String peerID;
	private String infohash;//����hash
	private String peerCnt;//peer���ӵĽڵ���
	private String NATSuccessRate;//��͸�ɹ���
	private String schedulingDelay;//������ʱ
	private String sliceDelay;//��Ƭ��ʱ
	
	private String uploadVolume;//��������
	private String downloadVolume;//��������
	
	private String uploadPeakRate;//�ϴ������ֵ
	private String downloadPeakRate; //���д����ֵ
	
	private String logTime;//��¼ʱ��
	
	private String timeStart;//ͳ�ƿ�ʼʱ��
	private String timeEnd;//ͳ�ƽ���ʱ��
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
