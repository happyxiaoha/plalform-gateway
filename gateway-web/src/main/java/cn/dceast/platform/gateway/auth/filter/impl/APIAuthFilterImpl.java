package cn.dceast.platform.gateway.auth.filter.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.dceast.platform.gateway.auth.filter.AuthFilter;
import cn.dceast.platform.gateway.auth.filter.FilterMatcher;
import cn.dceast.platform.gateway.auth.filter.FilterResponseMessage;

/**
 * api黑名单过来
 * @author zhang
 *
 */
public class APIAuthFilterImpl extends AuthFilter{
	private Logger logger = LoggerFactory.getLogger(APIAuthFilterImpl.class);
	
	@Override
	public boolean doFilter(HttpServletRequest request,
			HttpServletResponse response) {
		
		logger.info("enter APIAuthFilterImpl");
		
		/**
		 * api 黑名单(uri=/appName/api)
		 */
		String api=getUriNoParams(request);
		
		if(FilterMatcher.isExistsBlackOfApi(api)){
			setErrorMessageOfJson(request,response, FilterResponseMessage.CODE_101101);
			logger.info(String.format("The api '%s' is in api black list!",api));
			return false;
		}
		
		
		/**
		 * 公共权限的api是否存在
		 */
//		if(!FilterMatcher.isExistsPublicAuthOfApi(getAppName(request), getApi(request))){
//			setErrorMessageOfJson(request,response, FilterResponseMessage.CODE_100103);
//			logger.info(String.format("The api '%s' does not exist(not public auth)",api));
//			return false;
//		}
		
		return true;
	}
	
}
