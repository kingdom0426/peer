package com.example.peer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import cn.cnic.peer.cons.Constant;
import cn.cnic.peer.download.Download;
import cn.cnic.peer.m3u8.M3U8;
import cn.cnic.peer.sqlite.DB;
import cn.cnic.peer.util.Global;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class VideoServer extends NanoHTTPD {
	
    public static final String TAG = VideoServer.class.getSimpleName();
    private Context ctx;
    
    public VideoServer(Context ctx) {
        super(Constant.LOCAL_SERVER_PORT);
        this.ctx = ctx;
    }
    
    public static boolean over = false;
    
    @Override
    public Response serve(IHTTPSession session) {
    	
    	//例如：159.226.22.22:8088
    	String srcAddress = session.getHeaders().get("X-Forwarded-For");
    	
    	//例如：/VODS/1092287_0032222153.m3u8或/VODS/1092287_142222153_0002222153_0000000000_0001143039.ts
    	String url = session.getUri();
    	
    	/**
    	 * 例如：	http://111.39.226.112:8114/VODS/1092287_0032222153.m3u8?Fsv_filetype=
    	 * 			1&Fsv_cid=1092287&Fsv_FirstSegID=32222153&Provider_id=gslb/program
    	 * 			&Pcontent_id=_ahbyfh-1_/FDN/FDNB2132171/prime.m3u8&Fsv_otype=0&FvS
    	 * 			eid=54e0e9c78502314b&AuthInfo=&version=
    	 * 
    	 * 或：		http://111.39.226.112:8114/VODS/1092287_142222153_0002222153_0000000000
    	 * 			_0001143039.ts?Fsv_Sd=10&Fsv_filetype=2&Provider_id=gslb/program&Pconte
    	 * 			nt_id=_ahbyfh-1_/FDN/FDNB2132171/prime.m3u8&FvOPid=_ahbyfh-1_/FDN/FDNB2
    	 * 			132171/prime.m3u8&Fsv_MBt=0&FvHlsIdx=3&UserID=&Fsv_otype=0&FvSeid=54e0e9c78502314b
    	 */
    	String srcURL = "http://" + srcAddress + url + "?" + session.getQueryParameterString();
    	
    	//如果发来的是m3u8请求,先返回第一个ts视频
    	if(url.endsWith(".m3u8")) {
    		Global.FIRST_TS_FROM_CDN = "";
    		List<String> tsRequests = M3U8.parseSubM3u8(session);
    		String fileName = System.currentTimeMillis() + "";
    		String firstTSRequest = tsRequests.get(0);
    		Download.downloadAll(firstTSRequest, fileName, Constant.SAVE_PATH);
    		Global.FIRST_TS_FROM_CDN = firstTSRequest;
    		return responseVideoStream(fileName);
    		
    	/**
    	 * 如果发来的是ts请求,先判断是不是第一个ts
    	 * 如果是，就不返回（因为在收到m3u8的时候已经反回了）
    	 * 如果不是，就去返回该视频
    	 */
    	} else if(url.endsWith(".ts")) {
    		//如果不是第一个ts文件
    		if(!srcURL.equals(Global.FIRST_TS_FROM_CDN)) {
    			String fileName = System.currentTimeMillis() + "";
    			Download.downloadAll(srcURL, fileName, Constant.SAVE_PATH);
    		}
    		
    	//如果是其他，就不做任何操作
    	} else {
    		return null;
    	}
    	return null;
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
}
