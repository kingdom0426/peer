package com.example.peer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import cn.cnic.peer.connect.TCPThread;
import cn.cnic.peer.connect.UDPThread;
import cn.cnic.peer.cons.Constant;
import cn.cnic.peer.download.Download;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView mTipsTextView;
	private VideoServer mVideoServer;
	public static Map<String, Boolean> map = new HashMap<String, Boolean>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTipsTextView = (TextView) findViewById(R.id.TipsTextView);
		mVideoServer = new VideoServer();
		mTipsTextView.setText("请在远程浏览器中输入:\n\n" + getLocalIpStr(this) + ":"	+ Constant.LOCAL_SERVER_PORT);
		
		File file = new File(Constant.SAVE_PATH + "40dc2249423e63484e291ad0eeef2c0ba870b027");
		if(file.exists()) {
			file.delete();
		}
		
		//判断PEER_ID_FILE是否存在，如果不存在，则创建，写入ID值，最后将值赋给PEER_ID_VALUE
		String peerID = getPeerId();
		if(peerID == null || "".equals(peerID)) {
			writePeerId("qinyifangpeer");
		}
		Constant.PEER_ID_VALUE = getPeerId();
		
		
		Constant.LOCAL_SERVER_IP = getLocalIpStr(this);
		
		//启动TCP连接tracker，用于与tracker通信
		TCPThread tcp = new TCPThread();
		Thread t1 = new Thread(tcp);
		t1.start();
		
		//启动UDP线程，用于peer间数据通信
		UDPThread udp = new UDPThread();
		Thread t3 = new Thread(udp);
		t3.start();
		
		try {
			mVideoServer.start();
		} catch (IOException e) {
			e.printStackTrace();
			mTipsTextView.setText(e.getMessage());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
    protected void onDestroy() {
        mVideoServer.stop();
        super.onDestroy();
    }

    public static String getLocalIpStr(Context context) {        
        WifiManager wifiManager=(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();          
        return intToIpAddr(wifiInfo.getIpAddress());
    }
    
    private static String intToIpAddr(int ip) {
        return (ip & 0xff) + "." + ((ip>>8)&0xff) + "." + ((ip>>16)&0xff) + "." + ((ip>>24)&0xff);
    }
    
    public void writePeerId(String peerID) {
		FileOutputStream out = null;
		BufferedWriter writer = null;
		try {
			out = openFileOutput("PEER_ID_FILE.txt", Context.MODE_PRIVATE);
			writer = new BufferedWriter(new OutputStreamWriter(out));
			writer.write(peerID);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(writer!=null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
    
    public String getPeerId() {
    	FileInputStream in = null;
    	BufferedReader reader = null;
    	StringBuffer content = new StringBuffer();
    	try {
    		File folder = new File(Constant.SAVE_PATH+"files");
    		if(!folder.exists()) {
    			folder.mkdirs();
    		}
    		File file = new File(folder.getPath()+"/PEER_ID_FILE.txt");
    		if(!file.exists()) {
    			file.createNewFile();
    		}
			in = openFileInput("PEER_ID_FILE.txt");
			reader = new BufferedReader(new InputStreamReader(in));
			String line = "";
			while((line = reader.readLine()) != null) {
				content.append(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    	return content.toString();
    }
}
