package cn.cnic.peer.md5;

import java.security.MessageDigest;

public class MD5 {
    public static String getMD5(String str) {  
    	byte[] result = null;
        MessageDigest digest = null;  
        try {  
            digest = MessageDigest.getInstance("MD5");  
            result = digest.digest(str.getBytes());
        } catch (Exception e) {  
            e.printStackTrace();  
            return null;  
        }  
        return result.toString();
    }  
}
