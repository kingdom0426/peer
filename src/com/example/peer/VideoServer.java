package com.example.peer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.os.Environment;
import android.util.Log;

import cn.cnic.peer.connect.TCPThread;
import cn.cnic.peer.cons.Constant;
import cn.cnic.peer.download.Download;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class VideoServer extends NanoHTTPD {
	
    public static final String TAG = VideoServer.class.getSimpleName();
    
    public VideoServer() {
        super(Constant.LOCAL_SERVER_PORT);
    }
    
    @Override
    public Response serve(IHTTPSession session) {
//    	String tsId = getTsId(session);
//    	int fileSize = getFileSize(session);
    	
    	//如果本地不存在该文件，或文件大小不完整，就去tracker中请求
//    	if(!DB.isLocalExist(tsId, fileSize)) {
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
    	String contentHash = "40dc2249423e63484e291ad0eeef2c0ba870b027";
    	File file = new File(Environment.getExternalStorageDirectory().getPath()+"/" + contentHash);
    	if(!file.exists()) {
    		TCPThread.sessions.add(session);
    		try {
    			Thread.sleep(30000);
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
    	}
        return responseVideoStream(contentHash);
    }
    
    public Response responseVideoStream(String contentHash) {
        FileInputStream fis = null;
		try {
			fis = new FileInputStream(Environment.getExternalStorageDirectory().getPath()+"/" + contentHash);
			Log.d("filePath", Environment.getExternalStorageDirectory().getPath());
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
