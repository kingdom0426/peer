package cn.cnic.peer.sqlite;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import cn.cnic.peer.entity.Piece;

public class DB {
	public static final String URL_CONTENT_TABLE = "url_content_table";
	public static final String PIECE_TABLE = "piece_table";
	
	public static void initDB(Context ctx) {
		
		SQLiteDatabase db = ctx.openOrCreateDatabase("peer.db", ctx.MODE_PRIVATE, null);
		
		//创建表  url_content_table
		db.execSQL("create table if not exists " + URL_CONTENT_TABLE + "(contentHash varchar(40) primary key, urlHash varchar(40), complete boolean);");
		
		//创建表piece_table
		db.execSQL("create table if not exists " + PIECE_TABLE + "(contentHash varchar(40), offset int(20), length int(20));");
		
		db.close();
	}
	
	public static void insertUrlContentMap(Context ctx, String urlHash, String contentHash, boolean complete) {
		SQLiteDatabase db = ctx.openOrCreateDatabase("peer.db", ctx.MODE_PRIVATE, null);
    	ContentValues cv = new ContentValues();
    	cv.put("contentHash", "dddd");
    	cv.put("urlHash", "sers");
    	cv.put("complete", true);
    	db.insert(DB.URL_CONTENT_TABLE, null, cv);
	}
	
	public static List<Piece> getPiecesByContentHash(Context ctx, String contentHash) {
		List<Piece> pieces = new ArrayList<Piece>();
		SQLiteDatabase db = ctx.openOrCreateDatabase("peer.db", ctx.MODE_PRIVATE, null);
    	Cursor cursor = db.query(
    			DB.PIECE_TABLE, 
    			new String[]{"contentHash", "urlHash", "complete"}, 
    			"conentHash = ?", 
    			new String[] {contentHash}, 
    			null, 
    			null, 
    			null);
    	if(cursor != null) {
    		while (cursor.moveToNext()) {
    			Piece p = new Piece();
    			p.setContentHash(cursor.getString(cursor.getColumnIndex("contentHash")));
    			p.setOffset(cursor.getInt(cursor.getColumnIndex("offset")));
    			p.setLength(cursor.getInt(cursor.getColumnIndex("length")));
    			pieces.add(p);
			}
    	}
    	db.close();
    	return pieces;
	}
}