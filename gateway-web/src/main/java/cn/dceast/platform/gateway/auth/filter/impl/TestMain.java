package cn.dceast.platform.gateway.auth.filter.impl;

import com.sun.msv.util.StringPair;

import cn.dceast.platform.gateway.auth.util.UrlUtil;

public class TestMain {
	public static void main(String[] args){
		String url = "/liycq";
		System.out.println(UrlUtil.getUriNoParams(UrlUtil.getSecondContext(url)));
		
	}
}
