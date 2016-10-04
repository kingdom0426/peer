package com.example.peer;

import java.io.IOException;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String DEFAULT_FILE_PATH = Environment
			.getExternalStorageDirectory() + "/1.mp4";
	private static final int VIDEO_WIDTH = 320;
	private static final int VIDEO_HEIGHT = 240;

	private TextView mTipsTextView;
	private VideoServer mVideoServer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTipsTextView = (TextView) findViewById(R.id.TipsTextView);
		mVideoServer = new VideoServer(DEFAULT_FILE_PATH, VIDEO_WIDTH,
				VIDEO_HEIGHT, VideoServer.DEFAULT_SERVER_PORT);
		mTipsTextView.setText("����Զ�������������:\n\n" + getLocalIpStr(this) + ":"
				+ VideoServer.DEFAULT_SERVER_PORT);
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
}
