package com.example.peer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import cn.cnic.peer.connect.TCPThread;
import cn.cnic.peer.cons.Constant;
import cn.cnic.peer.download.Download;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class VideoServer extends NanoHTTPD {
	
	public static boolean over = false;
    
    public static final String TAG = VideoServer.class.getSimpleName();
    
    public VideoServer() {
        super(Constant.LOCAL_SERVER_PORT);
    }
    
    @Override
    public Response serve(IHTTPSession session) {
//    	String tsId = getTsId(session);
//    	int fileSize = getFileSize(session);
    	
    	//������ز����ڸ��ļ������ļ���С����������ȥtracker������
//    	if(!DB.isLocalExist(tsId, fileSize)) {
		TCPThread.sessions.add(session);
//    	}
//    	MainActivity.map.put(session.getUri(), false);
//    	while(!MainActivity.map.get(session.getUri())) {
//    		try {
//				Thread.sleep(2000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//    	}
    	
//    	Download.downloadAll("http://111.39.226.112:8114/VODS/1092287_142222153_0002222153_0000000000_0001143039.ts?Fsv_Sd=10&Fsv_filetype=2&Provider_id=gslb/program&Pcontent_id=_ahbyfh-1_/FDN/FDNB2132171/prime.m3u8&FvOPid=_ahbyfh-1_/FDN/FDNB2132171/prime.m3u8&Fsv_MBt=0&FvHlsIdx=3&UserID=&Fsv_otype=0&FvSeid=54e0e9c78502314b", 
//				"40dc2249423e63484e291ad0eeef2c0ba870b027", Constant.SAVE_PATH);
//    	over = true;
		while(!over) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		over = false;
    	
        return responseVideoStream("40dc2249423e63484e291ad0eeef2c0ba870b027");
    }
    
    public Response responseVideoStream(String contentHash) {
        FileInputStream fis = null;
		try {
			fis = new FileInputStream(Constant.SAVE_PATH + contentHash);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        return new NanoHTTPD.Response(Status.OK, "video/mp4", fis);
    }
    
    public static String getTsId(IHTTPSession session) {
    	String url = session.getUri();
    	String tsId = url.substring(url.lastIndexOf("/"), url.length());
    	return tsId;
    }
    
    public static int getFileSize(IHTTPSession session) {
//    	Map<String, String> map = session.getParms();
//    	return Integer.parseInt(map.get("fileSize"));
    	
    	return 123;
    }
}
