package cn.cnic.peer.sqlite;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import cn.cnic.peer.cons.Constant;
import cn.cnic.peer.entity.Piece;
import cn.cnic.peer.entity.UrlContentMap;
import cn.cnic.peer.merge.Merge;

public class DB {
	private static Connection conn = null;

	public static Connection openConnection() {
		try {
			if (null == conn || conn.isClosed()) {
				Class.forName("org.sqlite.JDBC");
				conn = DriverManager.getConnection("jdbc:sqlite:peer.db");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	public static void closeConnection() {
		try {
			if (null != conn) {
				conn.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn = null;
			System.gc();
		}
	}
	
	public static void insertPiece(Piece p) {
		try {
			Connection conn = DB.openConnection();
			String sql = "INSERT INTO ts (contentHash,offset,length) VALUES (?,?,?);"; 
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, p.getContentHash());
			stmt.setInt(2, p.getOffset());
			stmt.setInt(3, p.getLength());
			stmt.executeUpdate();
			stmt.close();
			DB.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void insertUrlContentMap(UrlContentMap umap) {
		try {
			Connection conn = DB.openConnection();
			String sql = "INSERT INTO url_content_map (url,contentHash) VALUES (?,?);"; 
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, umap.getUrlHash());
			stmt.setString(2, umap.getContentHash());
			stmt.executeUpdate();
			stmt.close();
			DB.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static UrlContentMap getUrlContentMap(String urlHash) {
		UrlContentMap map = null;
		try {
			Connection conn = DB.openConnection();
			String sql = "SELECT * FROM url_content_map where urlHash = ?;";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, urlHash);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				map = new UrlContentMap();
				map.setUrlHash(rs.getString("urlHash"));
				map.setContentHash(rs.getString("contentHash"));
			}
			rs.close();
			stmt.close();
			DB.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return map;
	}
	
	public static List<Piece> getPiecesByContentHash(String contentHash) {
		List<Piece> pieces = new ArrayList<Piece>();
		try {
			Connection conn = DB.openConnection();
			String sql = "SELECT * FROM ts where contentHash = ?;";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, contentHash);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Piece p = new Piece();
				p.setContentHash(rs.getString("contentHash"));
				p.setOffset(rs.getInt("offset"));
				p.setLength(rs.getInt("length"));
				pieces.add(p);
			}
			rs.close();
			stmt.close();
			DB.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return pieces;
	}
	
	public static List<Piece> deletePiecesByContentHash(String contentHash) {
		List<Piece> pieces = new ArrayList<Piece>();
		try {
			Connection conn = DB.openConnection();
			String sql = "delete FROM ts where contentHash = ?;";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, contentHash);
			stmt.executeUpdate();
			stmt.close();
			DB.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return pieces;
	}
	
	public static void updatePiece(String contentHash, int offset, int length) {
		Piece p = new Piece();
		p.setContentHash(contentHash);
		p.setLength(length);
		p.setOffset(offset);
		List<Piece> pieces = DB.getPiecesByContentHash(contentHash);
		List<Piece> newPieces = new Merge().merge(pieces);
		deletePiecesByContentHash(contentHash);
		for(Piece piece : newPieces) {
			insertPiece(piece);
		}
	}
	
	//判断某个ts文件在本地是否完整
	public static boolean isLocalExist(String tsId, int fileSize) {
		File f = new File(Constant.SAVE_PATH + File.separator + tsId);
		if(!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(f.length() == fileSize) {
				return true;
			}
		}
		return false;
	}
}