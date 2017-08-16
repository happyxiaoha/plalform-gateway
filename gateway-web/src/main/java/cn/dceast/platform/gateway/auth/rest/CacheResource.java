package cn.dceast.platform.gateway.auth.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

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
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import cn.dceast.platform.gateway.auth.AppConfig;
import cn.dceast.platform.gateway.auth.filter.AuthFilter;
import cn.dceast.platform.gateway.auth.filter.RequestInfo;
import cn.dceast.platform.gateway.auth.util.ApplicationUtil;
import cn.dceast.platform.gateway.auth.util.UrlUtil;
import redis.clients.jedis.Jedis;

/**
 * @author liycq
 * 当get Cache接口命中(code 000000)时，由此类将命中内容返回client端
 */
@RestController
public class CacheResource {

	private static Logger logger = LoggerFactory.getLogger(CacheResource.class);

	/**
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/cacheFilter", method = { RequestMethod.POST, RequestMethod.GET })
	public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {

		logger.info("CacheResource enter CacheFilter");

		AppConfig ac = (AppConfig) ApplicationUtil.getSingleton().getContext().getBean("appConfig");
		String redisHost = ac.getRedisHost();
		// cahce接口地址
		// String cacheRequestUrl = ac.getCacheRequestUrl();

		// String requestType = request.getMethod();
		// String x_original_uri=request.getHeader("x-original-uri");
		// String url =
		// UrlUtil.assembleTargetUrl(request.getRequestURL().toString(),
		// x_original_uri);
		// Enumeration<String> enumeration = request.getHeaderNames();
		////// 读取client端请求头
		// Map<String, String> headerValues = new HashMap<String, String>();
		// for (Enumeration<String> e = enumeration; e.hasMoreElements();) {
		// String thisName = e.nextElement().toString();
		// headerValues.put(thisName, request.getHeader(thisName));
		// }
		////// --读取client端请求头
		// Map<String, Object> requestMap = new HashMap<String, Object>();
		// requestMap.put("url", url);
		// requestMap.put("requestType", requestType);
		// requestMap.put("parameters",
		// UrlUtil.getParameterMap(x_original_uri));
		// requestMap.put("headers", headerValues);
		// requestMap.put("body", RequestInfo.getRequestBody(request));
		// String requestJsonString = JSON.toJSONString(requestMap);
		// logger.info("CacheResource requestJsonString: " + requestJsonString);
		// 请求Cache接口
		// String returnJsonString = postApiCache(cacheRequestUrl,
		// requestJsonString);

		// Redis中取出数据
		String url = request.getHeader("cacheUrl");
		logger.info("url: " + url);
		String returnJsonString = getRedisValue(redisHost, url);
		logger.info("CacheResource return JsonString from Redis: " + returnJsonString);
		// 解析Cache json
		return parseJsonString(returnJsonString, response);
	}

	/**
	 * @param redisHost
	 * @param key
	 * @return
	 */
	public String getRedisValue(String redisHost, String key) {
		Jedis jedis = new Jedis(redisHost, 6379);
		jedis.auth(DyUps.REDISPASSWORD);
		jedis.select(4);
		String returnValue = jedis.get(key);
		jedis.close();
		return returnValue;
	}

	/**
	 * 
	 * 设置responseHeaders和response Body
	 * 
	 * @param jsonString
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public String parseJsonString(String jsonString, HttpServletResponse response) throws Exception {
		String returnBody = null;
		Map firstMap = JSON.parseObject(jsonString, new TypeReference<Map>() {
		});
		// String returnBody = firstMap.get("body").toString();
		String result = firstMap.get("result").toString();
		String code = firstMap.get("code").toString();
		Map secondResultMap = JSON.parseObject(result, new TypeReference<Map>() {
		});
		Object tempReturnBody = secondResultMap.get("body");
		if (tempReturnBody == null) {
			return returnBody;
		} else {
			returnBody = secondResultMap.get("body").toString();
		}
		String responseHeadersString = secondResultMap.get("responseHeaders").toString();
		Map thirdResultHeadersMap = JSON.parseObject(responseHeadersString, new TypeReference<Map>() {
		});
		// 遍历thirdResultHeadersMap这个，设置response的头。
		for (Object key : thirdResultHeadersMap.keySet()) {
			response.setHeader(key.toString(), thirdResultHeadersMap.get(key).toString());
		}
		return returnBody;
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
			logger.info("/cacheFilter resp.getStatusLine(): " + resp.getStatusLine().toString());
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
