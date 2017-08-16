package cn.dceast.platform.gateway.auth.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;

import cn.dceast.platform.gateway.auth.AppConfig;
import cn.dceast.platform.gateway.auth.filter.RequestInfo;
import cn.dceast.platform.gateway.auth.util.ApplicationUtil;

@RestController
public class SetCacheResource {
	private static Logger logger = LoggerFactory.getLogger(SetCacheResource.class);
	public static SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@RequestMapping(value = "/setCacheResource", method = { RequestMethod.POST, RequestMethod.GET })
	public void apiCallFailedProcess(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		logger.info("enter /setCacheResource");
		// 更新缓存
		updateCache(req);
	}

	/**
	 * 更新缓存
	 */
	public void updateCache(HttpServletRequest req) {
		String cacheCode = req.getHeader("cacheCode");
		String requestDes = req.getHeader("requestDes");
		if (cacheCode == null || cacheCode.equals("") || requestDes == null || requestDes.equals("")) {
			if (logger.isErrorEnabled())
				logger.error("cacheCode error or requestDes error");
			return;
		}
		/*
		 * api-error-report2.lua请求/setCacheResource接口，业务系统返回的body则通过api-error-
		 * report2.lua的请求代码发送到本接口
		 */
		String respBody = RequestInfo.getRequestBody(req);
		Map<String, Object> requestMap = new HashMap<String, Object>();
		AppConfig ac = (AppConfig) ApplicationUtil.getSingleton().getContext().getBean("appConfig");
		// cahce接口地址
		String cacheRequestUrl = ac.getCacheRequestUrl();
		Enumeration<String> enumeration = req.getHeaderNames();
		////// 读取client端请求头
		Map<String, String> headerValues = new HashMap<String, String>();
		for (Enumeration<String> e = enumeration; e.hasMoreElements();) {
			String thisName = e.nextElement().toString();
			headerValues.put(thisName, req.getHeader(thisName));
		}
		headerValues.put("no-time", sFormat.format(new Date()));
		////// --读取client端请求头

		// code是000003或000000不设置缓存
		if (cacheCode.equals("000002")) {
			String[] tempArray = requestDes.split("\\|");
			requestMap.put("requestDes", tempArray[0]);
			requestMap.put("url", tempArray[1]);
			requestMap.put("body", respBody);
			requestMap.put("responseHeaders", headerValues);
			String requestJsonString = JSON.toJSONString(requestMap);
			logger.info("/SetCacheResource requestJsonString: " + requestJsonString);
			// 设置缓存
			String returnJsonString = postApiCache(cacheRequestUrl + "/cacheInfo/setApiCache.do", requestJsonString);
			logger.info("/SetCacheResource returnJsonString: " + returnJsonString);
		}
	}

	/**
	 * @param url
	 * @param json
	 * @return
	 */
	public String postApiCache(String url, String json) {
		StringBuilder sb = new StringBuilder();
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse resp = null;
		HttpPost httpPost = new HttpPost(url);
		StringEntity stringEntity = new StringEntity(json, "utf-8");
		stringEntity.setContentEncoding("UTF-8");
		httpPost.setHeader("Content-type", "application/json");
		httpPost.setEntity(stringEntity);
		try {
			httpClient = HttpClientBuilder.create().build();
			resp = httpClient.execute(httpPost);
			HttpEntity entity = resp.getEntity();
			logger.info("/SetCacheResource resp.getStatusLine(): " + resp.getStatusLine().toString());
			char[] buf = new char[8192];
			int tempoff = 0;
			BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "utf-8"));
			while (-1 != (tempoff = reader.read(buf, 0, 1024))) {
				sb.append(buf, 0, tempoff);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				resp.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

}
