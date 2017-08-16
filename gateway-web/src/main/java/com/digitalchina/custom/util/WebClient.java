package com.digitalchina.custom.util;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;


public class WebClient {
 
	public static String get(Request request){
		String url=request.getUrl();
		if(url==null){
			request.setResult("none url");
			return "none url";
		}
		DefaultHttpClient httpClient = new DefaultHttpClient(); 
		String result=null;
		HttpGet method = new HttpGet(url);
		try { 
			  HttpResponse response =httpClient.execute(method);
			// 打印服务器返回的状态
			int code=response.getStatusLine().getStatusCode();
			request.setStatus(code);

            result= EntityUtils.toString(response.getEntity()); 
            request.setResult(result);
		} catch (IOException e) {
			System.out.println("request cadvisor failed");
			return null;
		}
		//request.setResult(result);
		return result;
	}
	public static String postJSON(Request request) {
		String url=request.getUrl();
		if(url==null){
			request.setResult("none url");
			return "none url";
		}
		String parameters=request.getData().toString();
		DefaultHttpClient httpClient = new DefaultHttpClient(); 
		HttpPost method=new HttpPost(url);  
        String body = null;
        int statusCode=0;
        if (method != null & parameters != null  
                && !"".equals(parameters.trim())) {  
            try {
                method.addHeader("Content-type","application/json; charset=utf-8");  
                method.setHeader("Accept", "application/json");  
                method.setEntity(new StringEntity(parameters, Charset.forName("UTF-8")));  
                HttpResponse response = httpClient.execute(method);  
               
                statusCode = response.getStatusLine().getStatusCode();    
                if (statusCode != HttpStatus.SC_OK) {  
                	request.setResult(response.getStatusLine().toString());
                    return body; 
                }
                // Read the response body  
                body = EntityUtils.toString(response.getEntity());  
            } catch (IOException e) {
            	System.out.println("request cadvisor failed");
    			return null;
            }
  
        }  
        return body;  
    }
	public static String sendPost(String url, String param) throws Exception {
	   PrintWriter out = null;
	    BufferedReader in = null;
	    String result = "";
        URL realUrl = new URL(url);
        // 打开和URL之间的连接
        URLConnection conn = realUrl.openConnection();
        // 设置通用的请求属性
        conn.setRequestProperty("accept", "*/*");
        conn.setRequestProperty("connection", "Keep-Alive");
        conn.setRequestProperty("user-agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        // 发送POST请求必须设置如下两行
        conn.setDoOutput(true);
        conn.setDoInput(true);
        // 获取URLConnection对象对应的输出流
        out = new PrintWriter(conn.getOutputStream());
        // 发送请求参数
        out.print(param);
        // flush输出流的缓冲
        out.flush();
        // 定义BufferedReader输入流来读取URL的响应
        in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            result += line;
        }
    //使用finally块来关闭输出流、输入流

        if(out!=null){
            out.close();
        }
        if(in!=null){
            in.close();
        }
	    return result;
	}  

}