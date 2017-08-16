package cn.dceast.platform.common.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import cn.dceast.platform.common.string.StringUtil;

/**
 * http 发送工具包
 * @author zhang
 *
 */
public class SimpleHttpSender {
	
	public static final String HTTP_METH_POST="POST";
	public static final String HTTP_METH_GET="GET";
	
	public static final String default_encoding="utf-8";
	
	/**
	 * http请求
	 * @param url 请求地址
	 * @param method 请求方法
	 * @param params 参数
	 * @param headers 请求头
	 * @param encoding 编码
	 * @param timeout 链接超时时间（毫秒）
	 * @return
	 * @throws IOException
	 */
	public static String request(String url,
			                     String method,
			                     String params,
			                     Map<String, Object> headers,
			                     String encoding,
			                     int timeout){
		
		OutputStream out = null;
		InputStream in = null;
		ByteArrayOutputStream bos = null;
		try{
			URL u = new URL(url);
		      URLConnection uc = u.openConnection();
		      HttpURLConnection connection = (HttpURLConnection) uc;
		      connection.setDoOutput(true);
		      connection.setDoInput(true);
		      connection.setRequestMethod(method);
		      connection.setConnectTimeout(timeout);
		      
		      if(headers!=null && headers.size()>0){
		    	  Iterator<Entry<String, Object>> iter=headers.entrySet().iterator();
		    	  
		    	  while(iter.hasNext()){
		    		  Entry<String,Object> entry=iter.next();
		    		  connection.setRequestProperty(entry.getKey(),entry.getValue()==null?"":entry.getValue().toString());
		    	  }
		      }
		      
		      
		      out = connection.getOutputStream();
		      
		      if(params!=null){
		    	  out.write(params.getBytes(StringUtil.isEmpty(encoding)?default_encoding:encoding));  
		      }
		      
		      out.flush();
		      
		      int code=connection.getResponseCode();
		      
		      if(code>=400){
		    	  in = connection.getErrorStream();
		      }else{
		    	  in =  connection.getInputStream(); 
		      }
		      
		      bos= new ByteArrayOutputStream();
		      byte[]b= new byte[1024];
		      int i;
		      while (( i=in.read(b))!=-1) bos.write(b, 0, i);
		      
			  return new String(bos.toByteArray(),encoding);
		}catch(Exception e){
			  e.printStackTrace();
			  throw new RuntimeException("Net exception:"+e.getMessage());
		}finally{
		  closeOutputStream(out);
		  closeOutputStream(bos);
		  closeInputStream(in);
		}
	}
	
	public static void closeReader(Reader in){
		try {
			if(in!=null){
				in.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void closeInputStream(InputStream in){
		try {
			if(in!=null){
				in.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void closeOutputStream(OutputStream out){
		try {
			if(out!=null){
				out.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
