package cn.dceast.platform.gateway.auth.filter.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.dceast.platform.gateway.auth.data.entity.Appkey;
import cn.dceast.platform.gateway.auth.filter.AuthFilter;
import cn.dceast.platform.gateway.auth.filter.FilterResponseMessage;
import cn.dceast.platform.gateway.auth.filter.RequestInfo;
import cn.dceast.platform.gateway.auth.util.UrlUtil;

/**
 * api调用权限
 * @author zhang
 *
 */
public class APICallAuthFilterImpl extends AuthFilter{
	private Logger logger = LoggerFactory.getLogger(APICallAuthFilterImpl.class);
	
	@Override
	public boolean doFilter(HttpServletRequest request,
			HttpServletResponse response) {
		logger.info("enter APICallAuthFilterImpl");
//		String appName=getAppName(request);
		Appkey appkeyObject=getAppkeyInfo(request);
		if(appkeyObject==null){
			setErrorMessageOfJson(request,response, FilterResponseMessage.CODE_102102);
			logger.info("The appkey info is not exists!");
			return false;
		}
//		String api=getApi(request);
//		String ownerName=appkeyObject.getOwnerName();
//		if(!FilterMatcher.isExistsAppCallOfCaller(ownerName, appName, api)){
//			setErrorMessageOfJson(request,response, FilterResponseMessage.CODE_102103);
//			logger.info("Not have appcall authority!");
//			return false;
//		}
		
		return true;
	}

}
