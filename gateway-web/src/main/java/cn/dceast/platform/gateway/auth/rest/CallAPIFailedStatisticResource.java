package cn.dceast.platform.gateway.auth.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;

import cn.dceast.platform.gateway.auth.AppConfig;
import cn.dceast.platform.gateway.auth.data.entity.CallerRequestInfo;
import cn.dceast.platform.gateway.auth.filter.FilterResponseMessage;
import cn.dceast.platform.gateway.auth.filter.RequestInfo;
import cn.dceast.platform.gateway.auth.service.ServiceCallLogService;
import cn.dceast.platform.gateway.auth.service.ServiceStatisticsService;
import cn.dceast.platform.gateway.auth.util.ApplicationUtil;

@RestController
public class CallAPIFailedStatisticResource {
	private static Logger logger = LoggerFactory.getLogger(CallAPIFailedStatisticResource.class);

	@RequestMapping(value = "/apiCallFailedProcess", method = { RequestMethod.POST, RequestMethod.GET })
	public void apiCallFailedProcess(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String flownum = req.getHeader("flownum");
		if (flownum == null) {
			resp.setHeader("errCode", FilterResponseMessage.CODE_400101);
			resp.setHeader("errMsg", FilterResponseMessage.getMessage(FilterResponseMessage.CODE_400101));
			logger.error("flownum null");
			return;
		}
		logger.info("flownum:" + flownum);

		if (req.getHeader("orderDetailId") == null || req.getHeader("orderDetailId") == "") {
			if (logger.isErrorEnabled())
				logger.error("fail orderDetailId null flownum: " + flownum);
			return;
		}
		long orderDetailId = Long.valueOf(req.getHeader("orderDetailId"));// 订单号
		String code = req.getHeader("code") == null ? req.getHeader("responsestatus") : req.getHeader("code");
		String message = req.getHeader("message") == null ? "fail" : req.getHeader("message");
		String status = req.getHeader("status") == null ? "error" : req.getHeader("status");
		String result = req.getHeader("result") == null ? "fail" : req.getHeader("result");
		String request_uri = req.getHeader("request_uri");
		logger.info("code: " + code + " message: " + message + " request_uri: " + request_uri);

		CallerRequestInfo callerRequestInfo = new CallerRequestInfo();
		callerRequestInfo.setFlownum(flownum);
		// callerRequestInfo.setAreacode(areacode);
		callerRequestInfo.setCode(code);
		callerRequestInfo.setMessage(message);
		callerRequestInfo.setStatus(status);
		callerRequestInfo.setResult(result);
		callerRequestInfo.setOrderDetailId(orderDetailId);
		callerRequestInfo.setUri(request_uri);

		logger.info("CallAPIFailedStatisticResource success，callerRequestInfo:" + callerRequestInfo);
		// 更新service_call_log表
		ServiceCallLogService.updateCallLog(callerRequestInfo);
		// 调用失败处理，操作order_rule_list表
		ServiceStatisticsService.writeFailStatistics(callerRequestInfo);
		// 更新api_daily_request_statics表
		ServiceStatisticsService.updateCallerMaxVisitOfDay(callerRequestInfo);
	}
}
