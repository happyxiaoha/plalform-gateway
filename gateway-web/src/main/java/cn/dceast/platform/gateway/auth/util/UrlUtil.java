package cn.dceast.platform.gateway.auth.util;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

/**
 * 仅处理String类型的url
 * 
 * @author liycq
 *
 */
public class UrlUtil {

	/**
	 * 传入/test1/api1/get.do?ticket=123，返回3
	 * 
	 * @param url
	 * @return number
	 */
	public static int getUriLength(String url) {
		String noParamsUri = getUriNoParams(url);
		String[] str1 = noParamsUri.split("/");
		int returnCount = str1.length;
		for (int i = 0; i <= str1.length - 1; i++) {
			if (str1[i].equals("")) {
				returnCount--;
			}
		}
		return returnCount;
	}

	/***
	 * @param url
	 *            传入参数/phone/getPhoneNo?ticket=xxx这种形式
	 * @return 返回/phone/getPhoneNo这种形式
	 */
	public static String getUriNoParams(String url) {
		// 判空
		if (StringUtils.isNotEmpty(url)) {
			int index = url.indexOf("?");
			if (index >= 0) {
				url = url.substring(0, index);
			}

		}
		return url;
	}

	/**
	 * @param url
	 *            形如http://qg-api.citysdk.cn/gateway-web-1.8.0/auth
	 * @param x_original_uri
	 *            形如/taxpayer/info?name=%E790%E5%8B8&year=2015
	 * @return http://qg-api.citysdk.cn/taxpayer/info
	 */
	public static String assembleTargetUrl(String url, String x_original_uri) {
		String context = getUriNoParams(x_original_uri);
		// url形式为http://xxxxxxxxx/gateway-web-1.8.0/auth,其中"除了xxxxxxxxx部分外，其他均固定"
		String tempDomain = url.substring(0, url.indexOf("/gateway-web-1.8.0/auth"));
		return tempDomain + context;
	}

	public static void main(String[] args) {
		String temp = assembleTargetUrl("http://qg-api.citysdk.cn/gateway-web-1.8.0/auth","/taxpayer/info?name=%E790%E5%8B8&year=2015");
		System.out.println(temp);
	}
	
	
	
	
	/**
	 * url为/test这种形式,直接返回 url为/test/context1这种形式,返回/test
	 * 
	 * @param url
	 *            /phone/getPhone这种形式
	 * @return
	 */
	public static String getFirstContext(String url) {
		if (url.lastIndexOf("/") == 0) {
			return url;
		} else {
			String firstContext = url.substring(0, url.indexOf("/", 1));
			return firstContext;
		}
	}

	/**
	 * url为/test这种形式,返回/test url为/test/get.do这种形式返回/get.do
	 * 
	 * @param url
	 * @return
	 */
	public static String getLastContext(String url) {
		int lastIndex = url.lastIndexOf("/");
		String returnValue = url.substring(lastIndex, url.length());
		return returnValue;
	}

	/**
	 * 获得除掉第一级url后剩余部分. url为/test/context这种形式,返回/context url为/test这种形式 返回null
	 * url为/test/context/phone这种形式,返回/context/phone
	 * 
	 * @param url
	 *            /phone/getPhone这种形式
	 * @return
	 */
	public static String getSecondContext(String url) {
		if (url.lastIndexOf("/") == 0) {
			return null;
		} else {
			String secondContext = url.substring(url.indexOf("/", 1), url.length());
			return secondContext;
		}
	}

	/**
	 * 从url中获取key指示的value
	 * 
	 * @param url
	 * @param key
	 * @return
	 */
	public static String getValue4KeyFromURL(String url, String key) {
		if (StringUtils.isNotEmpty(url)) {
			int index = url.indexOf("?");
			if (index >= 0) {
				String[] params = url.substring(index + 1, url.length()).split("&");
				for (String resourceId : params) {
					if (resourceId.split("=")[0].equals(key)) {
						return resourceId.split("=")[1];
					}
				}
			}

		}

		return null;
	}

	/**
	 * 获取请求中的参数.当为qg-service.areacode.com:48011/realname_auth?ticket=
	 * 5bbb7e0a7bf64d0888ad8bcfae0ffbd5时,
	 * 返回ticket=5bbb7e0a7bf64d0888ad8bcfae0ffbd5
	 * 
	 * @param url
	 * @return
	 */
	public static String getParams(String url) {
		if (StringUtils.isNotEmpty(url)) {
			int index = url.indexOf("?");
			if (index >= 0) {
				return url.substring(index + 1, url.length());
			}
		}
		return null;
	}

	/**
	 * 当传入字符串为：/taxpayer/info?name=%E7%A5%9ECE5%8F%B8&year=2015
	 * 返回key分别为name和year的Map
	 * 
	 * @param url
	 * @return
	 */
	public static Map<String, String> getParameterMap(String url) {

		if (url.equals("") || url == null) {
			return null;
		}
		Map<String, String> map = new HashMap<String, String>();
		String parameters = getParams(url);
		if (parameters==null) {
			return null;
		}
		String[] keyValues = parameters.split("&");
		for (int i = 0; i < keyValues.length; i++) {
			String[] tempArray = keyValues[i].split("=");
			String key = tempArray[0];
			String value = tempArray[1];
			map.put(key, value);
		}
		return map;
	}
}
