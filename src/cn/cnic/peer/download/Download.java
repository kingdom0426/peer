package cn.cnic.peer.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import cn.cnic.peer.cons.Constant;

public class Download {

	public static void download(String url, int offset, int length, String tsId) {
		int BUFFER = 1024;

		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = client.execute(httpGet);
			InputStream in = response.getEntity().getContent();
			in.skip(offset);
			FileOutputStream out = new FileOutputStream(new File(Constant.SAVE_PATH + File.separator + tsId));
		    byte[] b = new byte[BUFFER];
		    int len = 0;
		    while((len=in.read(b))!= -1){
		    	length -= BUFFER;
		    	if(length > 0) {
		    		out.write(b,0,len);
		    	} else {
		    		out.write(b, 0, length);
		    	}
			}
			in.close();
			out.close();
			
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public static void downloadAll(String url, String tsId) {
		int BUFFER = 1024;

		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = client.execute(httpGet);
			InputStream in = response.getEntity().getContent();
			FileOutputStream out = new FileOutputStream(new File(Constant.SAVE_PATH + File.separator + tsId));
		    byte[] b = new byte[BUFFER];
		    int len = 0;
		    while((len=in.read(b))!= -1){
	    		out.write(b,0,len);
			}
			in.close();
			out.close();
			
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}
