package cn.cnic.peer.m3u8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import cn.cnic.peer.cons.Constant;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class M3U8 {

	public static List<String> parseSubM3u8(IHTTPSession session) {
		String srcAddress = "111.39.226.112:8114";
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet("http://" + srcAddress + session.getUri() + "?" + session.getQueryParameterString());
		List<String> tsRequests = new ArrayList<String>();
		try {
			HttpResponse response = client.execute(httpGet);
			InputStream in = response.getEntity().getContent();
			File folder = new File(Constant.M3U8_PATH);
			if(!folder.exists()) {
				folder.mkdirs();
			}
			String fileName = getFileName(session.getUri());
			File file = new File(Constant.M3U8_PATH + fileName);
			if(!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream out = new FileOutputStream(file);
		    byte[] b = new byte[1024];
		    int len = 0;
		    while((len=in.read(b))!= -1){
	    		out.write(b,0,len);
			}
			in.close();
			out.close();
			tsRequests = parseTsRequest(Constant.M3U8_PATH + fileName);
		}catch (Exception e){
			e.printStackTrace();
		}
        return tsRequests;
    }  
	
	public static List<String> parseTsRequest(String fileName) {
		BufferedReader br = null;
		List<String> returnList = new ArrayList<String>();
		try {
			br = new BufferedReader(new FileReader(fileName));
			boolean isEnd = false;
			while(!isEnd) {
				String line = br.readLine();
				if(!"".equals(line) && line != null) {
					if(!line.startsWith("#")) {
						returnList.add(line);
					}
				} else {
					isEnd = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return returnList;
	}
	
	public static String getFileName(String url) {
		String fileName = url.substring(url.lastIndexOf("/"), url.length());
		return fileName;
	}
}
