package com.example.peer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cn.cnic.peer.connect.TCPThread;
import cn.cnic.peer.connect.UDPThread;
import cn.cnic.peer.cons.Constant;

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
    
//    public String getPeerID() {
//    	BufferedReader br = null;
//    	BufferedWriter bw = null;
//    	String peerID = "";
//    	try {
//			File f = new File(Constant.SAVE_PATH);
//			if(!f.exists()) {
//				f.createNewFile();
//			}
//			br = new BufferedReader(new FileReader(f));
//			bw = new BufferedWriter(new FileWriter(f));
//			peerID = br.readLine();
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				bw.close();
//				br.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//    	return peerID;
//    }
}
