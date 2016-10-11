package com.example.peer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import android.os.Environment;
import android.util.Log;

import cn.cnic.peer.connect.TCPThread;
import cn.cnic.peer.cons.Constant;
import cn.cnic.peer.download.Download;
import cn.cnic.peer.m3u8.M3U8;
import cn.cnic.peer.util.Global;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class VideoServer extends NanoHTTPD {
	
    public static final String TAG = VideoServer.class.getSimpleName();
    
    public VideoServer() {
        super(Constant.LOCAL_SERVER_PORT);
    }
    
    public static boolean over = false;
    
    @Override
    public Response serve(IHTTPSession session) {
    	String tsId = getTsId(session);
    	int fileSize = getFileSize(session);
    	
    	//如果本地不存在该文件，或文件大小不完整，就去tracker中请求
//    	if(!DB.isLocalExist(tsId, fileSize)) {
//    	}
    	MainActivity.map.put(session.getUri(), false);
    	while(!MainActivity.map.get(session.getUri())) {
    		try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    	
    	Download.downloadAll("http://111.39.226.112:8114/VODS/1092287_142222153_0002222153_0000000000_0001143039.ts?Fsv_Sd=10&Fsv_filetype=2&Provider_id=gslb/program&Pcontent_id=_ahbyfh-1_/FDN/FDNB2132171/prime.m3u8&FvOPid=_ahbyfh-1_/FDN/FDNB2132171/prime.m3u8&Fsv_MBt=0&FvHlsIdx=3&UserID=&Fsv_otype=0&FvSeid=54e0e9c78502314b", 
				"40dc2249423e63484e291ad0eeef2c0ba870b027", Constant.SAVE_PATH);
    	over = true;
    	String srcAddress = session.getHeaders().get("X-Forwarded-For");
    	String url = session.getUri();
    	
    	//如果发来的是m3u8请求
    	if(url.endsWith(".m3u8")) {
    		List<String> tsRequests = M3U8.parseSubM3u8(session);
    		
    	//如果发来的是ts请求
    	} else if(url.endsWith(".ts")) {
    		
    		
    	//如果是其他，就不做任何操作
    	} else {
    		
    	}
    	String srcURL = session.getParms().get("srcURL");
    	String contentHash = "";
    	if(srcURL.equals("http://192.168.1.130/fileSequence0.ts")) {
    		contentHash = "40dc2249423e63484e291ad0eeef2c0ba870b027";
    	} else {
    		contentHash = "290c6e19fbe81c535d58c7ea7283f95c2339b70a";
    	}
    	File file = new File(Environment.getExternalStorageDirectory().getPath()+"/" + contentHash);
    	if(!file.exists()) {
    		Global.TCP_REQUESTS.add(session);
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
