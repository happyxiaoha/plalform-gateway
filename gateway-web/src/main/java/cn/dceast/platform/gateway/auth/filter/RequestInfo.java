package cn.dceast.platform.gateway.auth.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.TooManyListenersException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.dceast.platform.gateway.auth.AppConfig;
import cn.dceast.platform.gateway.auth.data.entity.Api;
import cn.dceast.platform.gateway.auth.data.entity.App;
import cn.dceast.platform.gateway.auth.data.entity.Appkey;
import cn.dceast.platform.gateway.auth.util.ApplicationUtil;
import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;
import cn.dceast.platform.gateway.auth.util.UrlUtil;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.QueryOperators;

/**
 * @author liycq 通过用户request解析出请求信息
 */
public class RequestInfo {
	private static Logger logger = LoggerFactory.getLogger(RequestInfo.class);

	public static final String APP_SUFFIX = "-ab";

	public static String getIpAddr(HttpServletRequest request) {

		String ip = request.getHeader("X-Real-IP");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			// X-Real-IP中没有,则从X-Forwarded-For中取
			ip = request.getHeader("X-Forwarded-For");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			// X-Forwarded-For中没有则从Proxy-Client-IP中取
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		if (ip != null && ip.equals("127.0.0.1")) {
			// 本地转发,FIXME 好像是推广的方式吧.
			ip = request.getHeader("Second-Auth-Addr");
		}
		return ip;
	}

	/**
	 * 比如www.baidu.com/phone/getPhone?ticket=123这种,则return
	 * "X-Original-URI"字段值为nginx中设置 "/phone/getPhone?ticket=123"
	 * 
	 * @param request
	 * @return
	 */
	public static String getUri(HttpServletRequest request) {
		String tempURI = (String) request.getAttribute("X-Original-URI");
		if (tempURI != null) {
			return tempURI;
		} else {
			String uri = request.getHeader("X-Original-URI");
			return uri;
		}
	}

	/**
	 * 获取client端请求的body
	 * 
	 * @param request
	 * @return
	 */
	public static String getRequestBody(HttpServletRequest request) {
		BufferedReader br;
		try {
			br = request.getReader();
			String str, wholeStr = "";
			while ((str = br.readLine()) != null) {
				wholeStr += str;
			}
			return wholeStr;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 比如www.baidu.com/phone/getPhone?ticket=123这种,则return "/phone/getPhone"
	 * 
	 * @param request
	 * @return
	 */
	public static String getUriNoParams(HttpServletRequest request) {
		String uri = getUri(request);
		return UrlUtil.getUriNoParams(uri);
	}

	/**
	 * 请求中获取reosurceId
	 * 
	 * @param request
	 * @return
	 */
	public static String getParams4ResourceId(HttpServletRequest request) {
		String uri = getUri(request);
		return UrlUtil.getValue4KeyFromURL(uri, "resourceId");
	}

	/**
	 * uri格式为: /appName/api/...../xx,返回appName uri格式为: /appName,返回appName
	 * 
	 * @param request
	 * @return
	 */
	public static String getAppName(HttpServletRequest request) {

		// 返回的uri格式：/appName/api/...../xx
		String uri = getUriNoParams(request);

		if (StringUtils.isEmpty(uri)) {
			return null;
		}

		try {
			String appName = null;
			if (uri.lastIndexOf("/") == 0) {
				appName = uri.substring(1, uri.length());
			} else {
				////////////////// 将appName截取出来,仅适合/appName/api/..../xx这种形式
				int index1 = uri.indexOf("/");//
				uri = uri.substring(index1 + 1);
				int index2 = uri.indexOf('/');
				appName = uri.substring(0, index2);
				/////////////////
			}

			// ab发布时候，需要能够访问最新的b应用
			if (appName.endsWith(APP_SUFFIX)) {
				appName = appName.substring(0, appName.indexOf(APP_SUFFIX));
			}
			return appName;
		} catch (Exception e) {
			logger.error(String.format("uri:[%s] is not a valid uri! ", uri));
		}

		return null;
	}

	/**
	 * uri的初始格式为/appName/api/...../xx?pram1=xxxx
	 * 
	 * @param request
	 * @return 去掉参数和appName的剩余部分
	 */
	public static String getApi(HttpServletRequest request) {
		String uri = getUriNoParams(request);

		if (StringUtils.isEmpty(uri)) {
			return null;
		}

		try {
			int index1 = uri.indexOf("/");
			uri = uri.substring(index1 + 1);
			int index2 = uri.indexOf('/');

			String api = uri.substring(index2);

			// 此处的api需要做处理，url后面去掉参数部分
			api = UrlUtil.getUriNoParams(api);

			return api;
		} catch (Exception e) {
			logger.error(String.format("uri:[%s] is not a valid uri! ", uri));
		}

		return null;
	}

	/**
	 * 分解请求中的authorization字段的值获取Appkey(即返回authorization字段value的第二项)
	 * 
	 * @param request
	 * @return
	 */
	public static String getAppkey(HttpServletRequest request) {
		String authorization = request.getHeader("authorization") == null
				? UrlUtil.getValue4KeyFromURL(getUri(request), "authorization") : request.getHeader("authorization");
		logger.info("getAppkey authorization: " + authorization);
		if (StringUtils.isEmpty(authorization)) {
			return null;
		}

		String temp[] = authorization.split(":");
		if (temp.length != 3) {
			return null;
		}
		return temp[1];
	}

	public static String getAppkeyByUri(HttpServletRequest request) {
		String uri = getUri(request);

		return getAppkeyByUri(uri);

	}

	/**
	 * 
	 * 
	 * 
	 * @param uri
	 * @return
	 */
	public static String getAppkeyByUri(String uri) {
		try {
			// 获取uri中appkey
			String params = uri.substring(uri.indexOf('?') + 1);
			String[] paramArr = params.split("&");

			String appkeyValue = "";
			for (int i = 0; i < paramArr.length; i++) {
				String keyValue = paramArr[i];
				String[] keyValueArr = keyValue.split("=");
				if ("dceast-appkey".equals(keyValueArr[0].trim())) {
					appkeyValue = keyValueArr[1].trim();
					break;
				}

			}

			return appkeyValue;

		} catch (Exception e) {
			return null;
		}

	}

	/**
	 * 从url中获取Ticket
	 * 
	 * @return
	 */
	public static String getServiceTicket(HttpServletRequest request) {
		String uri = getUri(request);
		try {
			// 获取uri中appkey
			String params = uri.substring(uri.indexOf('?') + 1);
			String[] paramArr = params.split("&");

			String ticketValue = null;
			for (int i = 0; i < paramArr.length; i++) {
				String keyValue = paramArr[i];
				String[] keyValueArr = keyValue.split("=");
				if ("ticket".equals(keyValueArr[0].trim())) {
					ticketValue = keyValueArr[1].trim();
					break;
				}
			}

			if (ticketValue == null) {
				ticketValue = getCookieValue(InnerConstants.COOKIE_TICKET, request);
			}
			return ticketValue;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 根据key拿到请求cookie中key对应的value.
	 * 
	 * @param key
	 * @return
	 */
	public static String getCookieValue(String key, HttpServletRequest request) {
		Cookie[] cookie = request.getCookies();
		if (cookie == null) {
			logger.info("this is no cookie!");
			return null;
		}
		String value = null;
		for (Cookie cookie2 : cookie) {
			if (cookie2.getName().equals(key)) {
				value = cookie2.getValue();
				if (logger.isDebugEnabled()) {
					logger.debug("RequestInfo.getCookieValue " + key + " value : " + value);
				}
				break;
			}
		}
		return value;
	}

	/**
	 * 当为微服务,根据request ticket或token查InnerConstants.COLL_TICKET_TOKEN获取UserId
	 * 
	 * @param request
	 * @return
	 */
	public static String getUserId(HttpServletRequest request) {

		AppConfig ac = (AppConfig) ApplicationUtil.getSingleton().getContext().getBean("appConfig");
		long now = new Date().getTime();
		long timeOut = Long.valueOf(ac.getTicketTimeout());// 获取配置中心ticket生命周期
		String ticket = null;
		String token = null;
		String userId = null;
		ticket = RequestInfo.getServiceTicket(request);
		token = RequestInfo.getCookieValue(InnerConstants.COOKIE_TOKEN, request);// 取cookie

		/////////////
		if (token == null && ticket == null) {
			logger.info("token ticket both null");
			return null;
		}
		if (ticket != null && token == null) {
			// 第一次访问
			BasicDBObject statsTicket = new BasicDBObject().append(InnerConstants.COOKIE_TICKET, ticket);
			// now-timeOut大于等于ticket_createTime
			statsTicket.append("ticket_createTime", new BasicDBObject(QueryOperators.GTE, now - timeOut));
			DBCursor dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_TICKET_TOKEN).find(statsTicket);
			if (dbCursor.size() == 0) {// 存在 过期
				logger.info("ticket: " + ticket + " out data");
				return null;
			} else {
				// 存在 不过期
				DBCursor dbCursorTempTicket = MongoDBUtil.getColl(InnerConstants.COLL_TICKET_TOKEN).find(statsTicket);
				while (dbCursorTempTicket.hasNext()) {
					BasicDBObject dbOjbectTempTicket = (BasicDBObject) dbCursorTempTicket.next();
					userId = dbOjbectTempTicket.getString("userId");
				}
			}
		} else {
			// 不是第一次访问,根据token查询
			BasicDBObject statsToken = new BasicDBObject().append(InnerConstants.COOKIE_TOKEN, token);
			DBCursor dbCursorToken = MongoDBUtil.getColl(InnerConstants.COLL_TICKET_TOKEN).find(statsToken);

			while (dbCursorToken.hasNext()) {
				BasicDBObject dbOjbectToken = (BasicDBObject) dbCursorToken.next();
				long token_createTime = dbOjbectToken.getLong("microToken_createTime");
				if (now - token_createTime >= Long.valueOf(ac.getTokenTimeout())) {
					// token超时
					logger.info("token: " + token + " out data");
					return null;
				} else {
					// token不超时
					userId = dbOjbectToken.getString("userId");
				}
			}
		}
		return userId;
	}

	/**
	 * 当为微服务,根据ticket或token查InnerConstants.COLL_TICKET_TOKEN获取serviceContext
	 * serviceContext形如/realname_auth，即为调用/dyups接口时传入的context字段
	 * 
	 * @param request
	 * @return
	 */
	public static String getServiceContext(HttpServletRequest request) {

		AppConfig ac = (AppConfig) ApplicationUtil.getSingleton().getContext().getBean("appConfig");
		long now = new Date().getTime();
		long timeOut = Long.valueOf(ac.getTicketTimeout());// 获取配置中心ticket生命周期
		String ticket = null;
		String token = null;
		String serviceContext = null;
		ticket = RequestInfo.getServiceTicket(request);
		// ticket = RequestInfo.getCookieValue(InnerConstants.COOKIE_TICKET,
		// request);// 取cookie
		token = RequestInfo.getCookieValue(InnerConstants.COOKIE_TOKEN, request);// 取cookie

		/////////////
		if (token == null && ticket == null) {
			return null;
		}
		if (ticket != null && token == null) {
			// 第一次访问
			BasicDBObject statsTicket = new BasicDBObject().append(InnerConstants.COOKIE_TICKET, ticket);
			// now-timeOut大于等于ticket_createTime
			statsTicket.append("ticket_createTime", new BasicDBObject(QueryOperators.GTE, now - timeOut));
			DBCursor dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_TICKET_TOKEN).find(statsTicket);
			if (dbCursor.size() == 0) {// 存在 过期
				return null;
			} else {
				// 存在 不过期
				DBCursor dbCursorTempTicket = MongoDBUtil.getColl(InnerConstants.COLL_TICKET_TOKEN).find(statsTicket);
				while (dbCursorTempTicket.hasNext()) {
					BasicDBObject dbOjbectTempTicket = (BasicDBObject) dbCursorTempTicket.next();
					serviceContext = dbOjbectTempTicket.getString("serviceContext");
				}
			}
		} else {
			// 不是第一次访问,根据token查询
			BasicDBObject statsToken = new BasicDBObject().append(InnerConstants.COOKIE_TOKEN, token);
			DBCursor dbCursorToken = MongoDBUtil.getColl(InnerConstants.COLL_TICKET_TOKEN).find(statsToken);

			while (dbCursorToken.hasNext()) {
				BasicDBObject dbOjbectToken = (BasicDBObject) dbCursorToken.next();
				long token_createTime = dbOjbectToken.getLong("microToken_createTime");
				if (now - token_createTime >= Long.valueOf(ac.getTokenTimeout())) {
					// token超时
					return null;
				} else {
					// token不超时
					serviceContext = dbOjbectToken.getString("serviceContext");
				}
			}
		}
		return serviceContext;
	}

	/**
	 * @param request
	 * @return
	 */
	public static Appkey getAppkeyInfoByUri(HttpServletRequest request) {
		String appkey = getAppkeyByUri(request);

		return getAppkeyInfo(appkey, request);
	}

	/**
	 * 获得AppKey信息
	 * 
	 * @param request
	 * @return
	 */
	public static Appkey getAppkeyInfo(HttpServletRequest request) {
		// appkey
		// 通过authorization字段的值获取Appkey
		String appkey = getAppkey(request);
		return getAppkeyInfo(appkey, request);
	}

	/**
	 * 
	 * 
	 * @param appkey
	 * @param request
	 * @return
	 */
	public static Appkey getAppkeyInfo(String appkey, HttpServletRequest request) {
		// _AF_appkey是appkey的一个在request的缓存
		Appkey appkeyInfo = (Appkey) request.getAttribute("_AF_appkey");
		if (appkeyInfo != null) {
			return appkeyInfo;
		}

		appkeyInfo = FilterMatcher.getAppkey(appkey);
		request.setAttribute("_AF_appkey", appkeyInfo);

		return appkeyInfo;
	}

	// public static Api getApiInfo(HttpServletRequest request){
	//// String appName=getAppName(request);
	//// String api=getApi(request);
	// String api = getUriNoParams(request);
	//
	// Api apiInfo=(Api)request.getAttribute("_AF_api");
	// if(apiInfo!=null){
	// return apiInfo;
	// }
	//
	// apiInfo=FilterMatcher.getApi(null, api);
	// request.setAttribute("_AF_api", apiInfo);
	//
	// return apiInfo;
	// }

	public static App getAppInfo(HttpServletRequest request) {
		App service = (App) request.getAttribute("_AF_service");
		if (service != null) {
			return service;
		}

		service = FilterMatcher.getApp(getAppName(request));
		request.setAttribute("_AF_service", service);

		return service;
	}

	public static Integer getGatewayInstanceCount(HttpServletRequest request) {
		Integer rateLimiterConfig = (Integer) request.getAttribute("_AF_gateway_instance");
		if (rateLimiterConfig != null) {
			return rateLimiterConfig;
		}

		rateLimiterConfig = FilterMatcher.getGatewayInstanceCount();
		request.setAttribute("_AF_gateway_instance", rateLimiterConfig);

		return rateLimiterConfig;
	}

	public static String getHttpContentType(HttpServletRequest request) {
		return request.getHeader("Content-Type");
	}

	public static String getUserAgent(HttpServletRequest request) {
		return request.getHeader("User-Agent");
	}

	/**
	 * 根据request字段或配置中心返回日最大访问次数字段
	 * 
	 * @param request
	 * @return
	 */
	public static Integer getApiDailyLimitCount(HttpServletRequest request) {
		// String appName = getAppName(request);
		// String api = getApi(request);
		// String callerName = getAppkeyInfo(request).getOwnerName();

		Integer apiDailyLimitCount = (Integer) request.getAttribute("_AF_api_daily_limit_count");
		if (apiDailyLimitCount != null) {
			return apiDailyLimitCount;
		}

		apiDailyLimitCount = FilterMatcher.getApiDailyLimitCount();
		request.setAttribute("_AF_api_daily_limit_count", apiDailyLimitCount);

		return apiDailyLimitCount;
	}

	/**
	 * 查询service_api表,如果这个url没有isAuth1的且没有isAuth2的，则返回true... 否则返回false
	 * 
	 * @param request
	 * @return
	 */
	public static boolean validateInvoke(HttpServletRequest request) {

		try {
			String url = RequestInfo.getUriNoParams(request);
			String areacode = FilterMatcher.getAreacode(request, false);
			List<String> domainList = FilterMatcher.getSecondDomainList(request);
			// isAuth 0(需要鉴权) 1（不需要鉴权 验证码回调之类的） 2(不需要鉴权 业务系统接口)
			BasicDBObject query = new BasicDBObject("areacode", areacode).append("url", url)
					.append("resourceType", domainList.get(1)).append("isAuth", "1");
			long count1 = MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_API).count(query);
			query.append("isAuth", "2");
			long count2 = MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_API).count(query);
			return count1 == 0 && count2 == 0 ? true : false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//
		return false;
	}

	/**
	 * @Description: 查询InnerConstants.COLL_SERVICE_API表获得“upstream字段的值”
	 *               插入功能：根据upstream字段中是否包含":443"，判断业务系统应使用https或http，供nginx.
	 *               conf的第二个server使用
	 * @param @param
	 *            request
	 * @param @return
	 * @param @throws
	 *            Exception
	 * @return String
	 */
	public static String getUpstreamServer(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String mongDBareacode = null;
		String token = RequestInfo.getCookieValue(InnerConstants.COOKIE_TOKEN, request);
		String areacode = FilterMatcher.getAreacode(request, false);
		List<String> domainList = FilterMatcher.getSecondDomainList(request);
		String url = getUriNoParams(request);
		BasicDBObject query = null;
		////////////////// 单独处理微服务
		if (domainList.get(1).equals(InnerConstants.RESOURCETYPE_MICROSVC)) {
			if (response.getHeader("serviceContext") != null) {
				// 则为/lib/app.js app.css这类请求
				//////////////////////////////
				/*
				 * 在推广资源场景中，比如用户从北京网关跳转到全国网关，最后访问业务系统 在访问到全国网关时，String url =
				 * getUriNoParams(request);取出来的url为北京网关路由的url，有问题。
				 * 所以，应该根据getUriNoParams(request)
				 * 中取出的url查询service_api表获知trueContext赋值给url。 步骤：
				 * 根据token或ticket查询出areacode，如果areacode比较，不一致，则为第二次(即上例中的全国网关)。
				 * 则查询出trueContext， 更改url值
				 */
				BasicDBObject queryToken = new BasicDBObject().append(InnerConstants.COOKIE_TOKEN, token);
				// 根据token查询ticket_token表查询
				DBCursor dbCursorToken = MongoDBUtil.getColl(InnerConstants.COLL_TICKET_TOKEN).find(queryToken);
				while (dbCursorToken.hasNext()) {
					BasicDBObject dbOjbectToken = (BasicDBObject) dbCursorToken.next();
					mongDBareacode = dbOjbectToken.getString("areacode");
				}
				if (!mongDBareacode.equals(areacode)) {
					// 查询出trueContext
					url = "/" + response.getHeader("serviceContext");
					BasicDBObject subQuery = new BasicDBObject("url", url).append("resourceType", domainList.get(1))
							.append("sourcecity", areacode);
					Object subBasicDBObject = MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_API).findOne(subQuery);// 查找一个
					if (subBasicDBObject != null) {
						BasicDBObject dbObject = (BasicDBObject) subBasicDBObject;
						String trueContext = dbObject.getString("trueContext");
						url = "/" + trueContext;
						logger.info("url: " + url + " resourceType: " + domainList.get(1) + " sourcecity: " + areacode
								+ " query success");
						/*
						 * 上例中的第二次访问时，serviceContext字段的值可知是有误的，
						 * 但这个serviceContext又是nginx.
						 * conf中拼接业务系统upstreamName的重要字段， 所以这里要重写一下。
						 */
						response.setHeader("serviceContext", trueContext);
					} else {
						logger.error("url: " + url + " resourceType: " + domainList.get(1) + " sourcecity: " + areacode
								+ " query trueContextNull");
					}
					////////////////////////
				} else {
					url = "/" + response.getHeader("serviceContext");
				}
			} else {
				// /realname_auth/mobile/auth.do
				url = UrlUtil.getFirstContext(url);
			}
		}
		//////////////////
		query = new BasicDBObject("areacode", areacode).append("url", url).append("resourceType", domainList.get(1));
		Object basicDBObject = MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_API).findOne(query);// 查找一个

		if (basicDBObject != null) {
			BasicDBObject dbObject = (BasicDBObject) basicDBObject;
			String upstream = dbObject.getString("upstream");
			//////////////////////////
			// 根据某条路由中是否有isHttps字段判断业务系统应使用https或http，供nginx.conf的第二个server使用
			String isHttps = dbObject.getString("isHttps");
			if (isHttps==null || isHttps.equals("0")) {
				response.setHeader("trueprotocol", "http://");
			}else {
				response.setHeader("trueprotocol", "https://");
			}
			/////////////////////////
			String listServer = upstream.split(";")[0].split("server")[1].trim();// 取出结果中的第一个分号前的域名
			for (int i = 0; i < listServer.length(); i++) { // 循环遍历字符串
				if (Character.isLetter(listServer.charAt(i))) {
					return listServer;
				}
			}
		}
		return "";
	}

	public static void main(String[] args) {
		String uri = "test-ab";
		uri = uri.substring(0, uri.indexOf("-ab"));
		System.out.println(uri);
	}

}