package cn.dceast.platform.gateway.auth.filter.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.dceast.platform.gateway.auth.data.entity.App;
import cn.dceast.platform.gateway.auth.data.entity.Appkey;
import cn.dceast.platform.gateway.auth.filter.AuthFilter;

public class SetResponseFilter extends AuthFilter {
	static final String SERTYPE_PENETRATION="PENETRATION";
	@Override
	public boolean doFilter(HttpServletRequest request,
			HttpServletResponse response) {
		App app=getAppInfo(request);
		String host=request.getHeader("host");
		String serType=app.getSerType();
		
		if(SERTYPE_PENETRATION.equals(serType)){
			host=getHostFromUrl(app.getUrl());
		}

		response.setHeader("sertype",serType);
		response.setHeader("userhost", host);
		
		Appkey appkeyInfo=getAppkeyInfo(request);
		if(appkeyInfo!=null){
			String ownerName=appkeyInfo.getOwnerName();
			response.setHeader("useraccount", ownerName);
		}
		return true;
	}
	
	private static String getHostFromUrl(String url){
		//去掉http:// https://
		if(url==null){
			return url;
		}
		
		url=url.replaceAll("(?i)(http://|https://)", "");
		int pos=url.indexOf("/");
		if(pos>0){
			url=url.substring(0,pos);
		}
		
		return url;
	}
}
