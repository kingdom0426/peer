package cn.cnic.peer.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.cnic.peer.entity.Piece;
import cn.cnic.peer.entity.Segment;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class Global {

	
	public static List<IHTTPSession> TCP_REQUESTS = new ArrayList<IHTTPSession>();		//����TCP�߳�ȥ����Ƿ��в���������
	
	public static List<Segment> UDP_SEGMENTS = new ArrayList<Segment>();				//����UDP�߳�ȥ����Ƿ�����������
	
	public static Map<String, Integer> UDP_QUERY_TOTAL = new HashMap<String, Integer>();//���ڼ�¼�ܹ���Ҫ����ٸ�peer��������
	
	public static Map<String, Integer> UDP_QUERY_CUR = new HashMap<String, Integer>();	//���ڼ�¼��ǰ�յ��˶��ٸ�peer������������Ӧ
	
	public static Map<String, List<Piece>> UDP_DATA_RECEIVE = new HashMap<String, List<Piece>>();//���ڼ�¼��ǰ���յ���������Ӧ
	
	public static Map<String, Integer> UDP_DATA_TOTAL = new HashMap<String, Integer>();	//���ڼ�¼�ܹ�����ٸ�peer��������������
	
	public static Map<String, Integer> UDP_DATA_CUR = new HashMap<String, Integer>();	//���ڼ�¼�ܹ��յ��˶��ٸ�peer������������Ӧ
	
	public static String FIRST_TS_FROM_CDN = "";
	
}
