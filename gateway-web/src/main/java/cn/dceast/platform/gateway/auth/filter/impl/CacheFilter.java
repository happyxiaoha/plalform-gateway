package cn.dceast.platform.gateway.auth.filter.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

import cn.dceast.platform.gateway.auth.AppConfig;
import cn.dceast.platform.gateway.auth.filter.AuthFilter;
import cn.dceast.platform.gateway.auth.filter.FilterMatcher;
import cn.dceast.platform.gateway.auth.filter.RequestInfo;
import cn.dceast.platform.gateway.auth.rest.AuthResource;
import cn.dceast.platform.gateway.auth.rest.DyUps;
import cn.dceast.platform.gateway.auth.util.ApplicationUtil;
import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;
import cn.dceast.platform.gateway.auth.util.UrlUtil;
import redis.clients.jedis.Jedis;

public class CacheFilter extends AuthFilter {

	private static Logger logger = LoggerFactory.getLogger(CacheFilter.class);

	/**
	 * 判断某请求是否有缓存
	 */
	@Override
	public boolean doFilter(HttpServletRequest request, HttpServletResponse response) {
		logger.info("enter CacheFilter Last subStep");
		String code = null;
		try {
			AppConfig ac = (AppConfig) ApplicationUtil.getSingleton().getContext().getBean("appConfig");
			//////////////////////////////////////
			// BasicDBObject testPassAuth = new BasicDBObject().append("url",
			// RequestInfo.getUriNoParams(request));
			// // 查询test_pass_auth表(用于测试)
			// DBCursor testPassAuthdbCursor =
			// MongoDBUtil.getColl(InnerConstants.COLL_TEST_PASS_AUTH).find(testPassAuth);
			/////////////////////////////////////
			List<String> subDomainList = FilterMatcher.getSecondDomainList(request);
			// 仅API
			if (subDomainList.get(1).equals(InnerConstants.RESOURCETYPE_API)) {
				String requestDes = null;
				// cahce接口地址
				String cacheRequestUrl = ac.getCacheRequestUrl();
				String redisHost = ac.getRedisHost();
				String requestType = request.getMethod();
				String x_original_uri = request.getHeader("x-original-uri");
				String url = UrlUtil.assembleTargetUrl(request.getRequestURL().toString(), x_original_uri);
				Enumeration<String> enumeration = request.getHeaderNames();
				////// 读取client端请求头
				Map<String, String> headerValues = new HashMap<String, String>();
				for (Enumeration<String> e = enumeration; e.hasMoreElements();) {
					String thisName = e.nextElement().toString();
					headerValues.put(thisName, request.getHeader(thisName));
				}
				////// --读取client端请求头
				Map<String, Object> requestMap = new HashMap<String, Object>();
				requestMap.put("url", url);
				requestMap.put("requestType", requestType);
				Map<String, String> tempParametersMap = UrlUtil.getParameterMap(x_original_uri);
				if (tempParametersMap == null) {
					Map<String, String> tempMap = new HashMap<String, String>();
					tempMap.put("parameters", "parameters");
					requestMap.put("parameters", tempMap);
				} else {
					requestMap.put("parameters", tempParametersMap);
				}
				requestMap.put("headers", headerValues);
				requestMap.put("body", RequestInfo.getRequestBody(request));
				String requestJsonString = JSON.toJSONString(requestMap);
				logger.info("requestJsonString: " + requestJsonString);
				// 请求Cache接口
				String returnJsonString = postApiCache(cacheRequestUrl + "/cacheInfo/getApiCache.do",
						requestJsonString);
				logger.info("CacheFilter returnString: " + returnJsonString);
				Map resultMap = JSON.parseObject(returnJsonString, new TypeReference<Map>() {
				});
				code = resultMap.get("code").toString();
				if (code.equals("000002")) {
					// code为000002，缓存接口返回的requestDes与code为000000不同
					Map resultMap2 = JSON.parseObject(resultMap.get("result").toString(), new TypeReference<Map>() {
					});
					requestDes = resultMap2.get("requestDes").toString();
					// 这个不一致
					requestDes = requestDes + "|" + url;
					response.setHeader("requestDes", requestDes);
				}
				if (code.equals("000000")) {
					// code为000000，缓存接口返回requestDes
					Map resultMap2 = JSON.parseObject(resultMap.get("result").toString(), new TypeReference<Map>() {
					});
					requestDes = resultMap2.get("requestDes").toString();
					response.setHeader("requestDes", requestDes);
					// 缓存命中，则需请求CacheResource接口
					response.setHeader("realHost", "127.0.0.1:48081");
					writeRedis(redisHost, url, returnJsonString);
					response.setHeader("cacheUrl", url);
				}
				response.setHeader("cacheCode", code);
				return true;
			}
		} catch (Exception e) {
			logger.error("CacheFilter's exception");
			e.printStackTrace();
			code = "000003";
			// 统一设置cacheCode
			response.setHeader("cacheCode", code);
		}
		
		return true;

	}

	/**
	 * 缓存命中，将命中返回内容写入Redis
	 * 
	 * @param key
	 * @param value
	 */
	public void writeRedis(String redisHost, String key, String value) {
		Jedis jedis = new Jedis(redisHost);
		jedis.auth(DyUps.REDISPASSWORD);
		jedis.select(4);
		jedis.set(key, value);
		jedis.close();
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
			logger.info("CacheFilter resp.getStatusLine(): " + resp.getStatusLine().toString());
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

	public static void main(String[] args) {
		String returnJsonString = null;
		Map resultMap = JSON.parseObject(returnJsonString, new TypeReference<Map>() {
		});
		System.out.println(resultMap);
	}

}
