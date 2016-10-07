package com.example.peer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import cn.cnic.peer.connect.TCPThread;
import cn.cnic.peer.cons.Constant;
import cn.cnic.peer.sqlite.DB;


import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class VideoServer extends NanoHTTPD {
    
    public static final String TAG = VideoServer.class.getSimpleName();
    
    public VideoServer() {
        super(Constant.LOCAL_SERVER_PORT);
    }
    
    @Override
    public Response serve(IHTTPSession session) {
    	String tsId = getTsId(session);
    	int fileSize = getFileSize(session);
    	
    	//如果本地不存在该文件，或文件大小不完整，就去tracker中请求
    	if(!DB.isLocalExist(tsId, fileSize)) {
    		TCPThread.sessions.add(session);
    	}
//        return responseVideoStream(tsId);
    	return null;
    }
    
    public Response responseVideoStream(String tsId) {
        FileInputStream fis = null;
		try {
			fis = new FileInputStream(Constant.SAVE_PATH + File.separator + tsId);
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
    	Map<String, String> map = session.getParms();
    	return Integer.parseInt(map.get("fileSize"));
    }
}
