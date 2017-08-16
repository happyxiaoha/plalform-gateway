package cn.dceast.platform.gateway.auth.filter.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.dceast.platform.common.string.StringUtil;
import cn.dceast.platform.gateway.auth.filter.AuthFilter;
import cn.dceast.platform.gateway.auth.filter.FilterMatcher;
import cn.dceast.platform.gateway.auth.filter.FilterResponseMessage;

/**
 * 黑名单
 * @author zhang
 *
 */
public class AppkeyBlackAuthFilterImpl extends AuthFilter{
	private Logger logger = LoggerFactory.getLogger(AppkeyBlackAuthFilterImpl.class);
	
	@Override
	public boolean doFilter(HttpServletRequest request,
			HttpServletResponse response) {
		logger.info("enter AppkeyBlackAuthFilterImpl");
		String appkey=getAppkey(request);
		if(FilterMatcher.isExistsBlackOfAppkey(appkey)){
			setErrorMessageOfJson(request,response, FilterResponseMessage.CODE_101102);
			logger.info(String.format("The appkey '%s' is in appkey black list!", appkey));
			return false;
		}
		
		return true;
		
	}

}
