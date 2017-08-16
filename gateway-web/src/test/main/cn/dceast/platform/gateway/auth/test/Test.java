package cn.dceast.platform.gateway.auth.test;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import cn.dceast.platform.gateway.auth.filter.FilterChain;

public class Test {
	public static void main(String[] args){
//		String bString=new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
//		
//		System.out.println(bString);
		

		InputStream stream = FilterChain.class.getResourceAsStream("handlerFilterApplication.xml");
		DocumentBuilderFactory df = javax.xml.parsers.DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = df.newDocumentBuilder();
			Document doc = db.parse(stream);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	
		
	}
}
