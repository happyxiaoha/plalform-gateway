package cn.dceast.platform.common.http;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.Test;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hongkai on 2016/10/27.
 */
public class HttpSenderTest {

	
	
	public static void main(String[] args) throws Exception{
		
		 File file = new File("D:\\testdyups.txt");
		 FileInputStream inputStream = new FileInputStream(file);
		 // 读入到ch
		 byte[] ch = new byte[(int) file.length()];
		 // 读入
		 inputStream.read(ch);
		 String newString = new String(ch);
		
		List<NameValuePair> params = new ArrayList();
		// String data = "[{ \"ip\" : \"10.129.30.69\",\"apiUri\" : \"phone\",
		// \"appName\" : \"/getPhoneNo\",\"remark\" : \"fasf\"}]";
		NameValuePair param = new BasicNameValuePair("data", newString);
		System.out.println("param " + param);
		params.add(param);
		String post = HttpSender.post("101.200.52.215:58080" + "/gateway-web-1.8.0" + "/dyups", 3000, params);
		System.out.println("post result: " + post);
		
	}
	
	
//    @Test
//    public void post() throws Exception {
//        String post = HttpSender.post("http://www.fuyunwang.com", 3000, null);
//        System.out.println(post);
//    }
//
//    @Test
//    public void post1() throws Exception {
//        String post = HttpSender.post("http://www.fuyunwang.com", 3000, null, null, "utf-8");
//        System.out.println(post);
//    }
//
//    @Test
//    public void postResponse() throws Exception {
//        HttpResponse response = HttpSender.postResponse("http://www.fuyunwang.com", 3000, null, null, "utf-8");
//        if(response != null){
//            StatusLine statusLine = response.getStatusLine();
//            System.out.println(statusLine);
//            InputStream content = response.getEntity().getContent();
//            byte[] temp = new byte[512];
//            content.read(temp);
//            System.out.println(new String(temp));
//        }
//    }
//
//    @Test
//    public void get() throws Exception {
//        String get = HttpSender.get("http://www.fuyunwang.com", 3000, null);
//        System.out.println(get);
//    }
//
//    @Test
//    public void get1() throws Exception {
//        String get = HttpSender.get("http://www.fuyunwang.com", 3000, null, "utf-8");
//        System.out.println(get);
//    }
//
//    @Test
//    public void getResponse() throws Exception {
//        HttpResponse response = HttpSender.getResponse("http://www.fuyunwang.com", 3000, null);
//        if(response != null){
//            StatusLine statusLine = response.getStatusLine();
//            System.out.println(statusLine);
//            InputStream content = response.getEntity().getContent();
//            byte[] temp = new byte[512];
//            content.read(temp);
//            System.out.println(new String(temp));
//        }
//    }

}