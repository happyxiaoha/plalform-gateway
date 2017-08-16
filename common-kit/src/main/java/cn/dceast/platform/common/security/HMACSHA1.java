package cn.dceast.platform.common.security;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HMACSHA1 {
	private static final String HMAC_SHA1 = "HmacSHA1"; 
	private static final String ENCODING="utf-8";
	  
    /** 
     * 生成签名数据 
     *  
     * @param data 待加密的数据 
     * @param key  加密使用的key 
     * @return 生成MD5编码的字符串  
     * @throws InvalidKeyException 
     * @throws NoSuchAlgorithmException 
     */  
    public static String encode(String data, String key) {  
    	 try{
           byte[] keyBytes=key.getBytes(ENCODING);  
           SecretKeySpec signingKey = new SecretKeySpec(keyBytes, HMAC_SHA1);     
           Mac mac = Mac.getInstance(HMAC_SHA1);     
           mac.init(signingKey);     
           byte[] rawHmac = mac.doFinal(data.getBytes(ENCODING));  
           StringBuilder sb=new StringBuilder();  
           for(byte b:rawHmac){  
            sb.append(byteToHexString(b));  
           }  
           
           return sb.toString(); 
    	 }catch(Exception e){
        	 e.printStackTrace();
         }
         
         return null;
       
    }
    	 
	 private static String byteToHexString(byte ib){  
	     char[] Digit={  
	       '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'  
	     };  
	     char[] ob=new char[2];  
	     ob[0]=Digit[(ib>>>4)& 0X0f];  
	     ob[1]=Digit[ib & 0X0F];  
	     String s=new String(ob);  
	     return s;           
	    }   
    
    public static void main(String[] args) throws Exception{
    	System.out.println(HMACSHA1.encode("张未来text他eeeeeeeeeeeeeeeeeeeeeeeeeeeee", "te2323232323223232st"));
    	//System.out.println(HMACSHA1.encode("123456", "test"));
    	
    	System.out.println(HMACSHA1.encode("张未来text他eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeerrrrrrrrrr", "te2323232323223232st"));
    }
      
}