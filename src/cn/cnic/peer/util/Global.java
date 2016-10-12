package cn.cnic.peer.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.cnic.peer.entity.Piece;
import cn.cnic.peer.entity.Segment;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class Global {

	
	public static List<IHTTPSession> TCP_REQUESTS = new ArrayList<IHTTPSession>();		//用于TCP线程去监测是否有播放器请求
	
	public static List<Segment> UDP_SEGMENTS = new ArrayList<Segment>();				//用于UDP线程去监测是否有数据请求
	
	public static Map<String, Integer> UDP_QUERY_TOTAL = new HashMap<String, Integer>();//用于记录总共需要向多少个peer发送请求
	
	public static Map<String, Integer> UDP_QUERY_CUR = new HashMap<String, Integer>();	//用于记录当前收到了多少个peer发来的请求响应
	
	public static Map<String, List<Piece>> UDP_DATA_RECEIVE = new HashMap<String, List<Piece>>();//用于记录当前所收到的所有响应
	
	public static Map<String, Integer> UDP_DATA_TOTAL = new HashMap<String, Integer>();	//用于记录总共向多少个peer发出了数据请求
	
	public static Map<String, Integer> UDP_DATA_CUR = new HashMap<String, Integer>();	//用于记录总共收到了多少个peer发来的数据响应
	
	public static String FIRST_TS_FROM_CDN = "";
	
}
