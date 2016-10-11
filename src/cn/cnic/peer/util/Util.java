package cn.cnic.peer.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {

	public static String formatTimeNow(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return sdf.format(new Date());
	}
	
}
