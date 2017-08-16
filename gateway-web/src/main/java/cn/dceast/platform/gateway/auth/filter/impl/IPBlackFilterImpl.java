package cn.dceast.platform.gateway.auth.filter.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.dceast.platform.common.string.StringUtil;
import cn.dceast.platform.gateway.auth.filter.AuthFilter;
import cn.dceast.platform.gateway.auth.filter.FilterMatcher;
import cn.dceast.platform.gateway.auth.filter.FilterResponseMessage;
import cn.dceast.platform.gateway.auth.listener.AuthContextListener;

/**
 * ip黑名单过滤，在ip黑名单中的所有ip均不能继续访问
 * @author zhang
 *
 */
public class IPBlackFilterImpl extends AuthFilter{
	private Logger logger = LoggerFactory.getLogger(IPBlackFilterImpl.class);
	@Override
	public boolean doFilter(HttpServletRequest request,
			HttpServletResponse response) {
		logger.info("enter IPBlackFilterImpl");
		String ip=getIpAddr(request);
		//ip是否为空
		if(StringUtils.isEmpty(ip)){
			setErrorMessageOfJson(request,response, FilterResponseMessage.CODE_103101);
			logger.info(String.format("The Ip is null!"));
			return false;
		}
		//ip是否在黑名单中
		if(FilterMatcher.isExistsBlackOfIP(ip)){
			setErrorMessageOfJson(request,response, FilterResponseMessage.CODE_101103);
			logger.info(String.format("The Ip '%s' is in Ip black list!", ip));
			return false;
		}
		
		return true;
	}
	

}
