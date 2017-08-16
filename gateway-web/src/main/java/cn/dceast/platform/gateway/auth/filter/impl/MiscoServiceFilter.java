package cn.dceast.platform.gateway.auth.filter.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.dceast.platform.gateway.auth.filter.AuthFilter;
import cn.dceast.platform.gateway.auth.filter.FilterMatcher;
import cn.dceast.platform.gateway.auth.filter.MicroServiceUtil;
import cn.dceast.platform.gateway.auth.util.InnerConstants;

/**
 * @author liycq
 * 微服务鉴权ticket token等的校验
 */
public class MiscoServiceFilter extends AuthFilter{
	
	private Logger logger = LoggerFactory.getLogger(MiscoServiceFilter.class);

	@Override
	public boolean doFilter(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		logger.info("enter MiscoServiceFilter");
		
		List<String> resourceList = FilterMatcher.getSecondDomainList(request);
		String resourceType = resourceList.get(1);
		if(InnerConstants.RESOURCETYPE_MICROSVC.equals(resourceType)){
			String returnMsg = MicroServiceUtil.microServiceAuth(request,response);
			if(returnMsg!=null){
				//出错
				if(logger.isErrorEnabled())
					logger.error("microService auth fail, returnMsg:" + returnMsg);
				request.setAttribute("extraMessage", "microService auth fail: " + returnMsg);
				return false;
			}
		}
		
		return true;
	}
	
	

}
