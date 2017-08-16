package cn.dceast.platform.gateway.auth.filter.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.antlr.v4.runtime.atn.LookaheadEventInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.dceast.platform.gateway.auth.data.entity.CallerRequestInfo;
import cn.dceast.platform.gateway.auth.filter.AuthFilter;
import cn.dceast.platform.gateway.auth.service.ServiceCallLogService;


/*
 * 流水生成 记录流水
 */
public class LogFilterImpl extends AuthFilter{
	
	private Logger logger = LoggerFactory.getLogger(LogFilterImpl.class);

	@Override
	public boolean doFilter(HttpServletRequest request,
			HttpServletResponse response) throws Exception{
		logger.info("enter LogFilterImpl");
        //构造调用信息对象
        CallerRequestInfo callInfo = getCallerRequestInfo(request);
        //写一下log表 写流水
		ServiceCallLogService.writeCallLog(callInfo);
		return true;
	}
	
	

}
