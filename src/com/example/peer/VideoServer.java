package com.example.peer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import cn.cnic.peer.cons.Constant;
import cn.cnic.peer.sqlite.DB;


import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class VideoServer extends NanoHTTPD {
    
    public static final int DEFAULT_SERVER_PORT = 8080;
    public static final String TAG = VideoServer.class.getSimpleName();
    
    public VideoServer(int port) {
        super(DEFAULT_SERVER_PORT);
    }
    
    @Override
    public Response serve(IHTTPSession session) {
    	String tsId = getTsId(session);
    	int fileSize = getFileSize(session);
    	
    	//������ز����ڸ��ļ������ļ���С����������ȥtracker������
    	if(!DB.isLocalExist(tsId, fileSize)) {
//    		TCPThread.sessions.add(session);
    	}
        return responseVideoStream(tsId);
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
