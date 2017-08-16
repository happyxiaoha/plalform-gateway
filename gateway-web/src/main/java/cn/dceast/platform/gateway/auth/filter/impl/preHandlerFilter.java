package cn.dceast.platform.gateway.auth.filter.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.QueryOperators;

import cn.dceast.platform.gateway.auth.AppConfig;
import cn.dceast.platform.gateway.auth.filter.AuthFilter;
import cn.dceast.platform.gateway.auth.filter.FilterMatcher;
import cn.dceast.platform.gateway.auth.filter.RequestInfo;
import cn.dceast.platform.gateway.auth.util.ApplicationUtil;
import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;
import cn.dceast.platform.gateway.auth.util.UrlUtil;

/**
 * 数据预处理
 * 
 * @author liycq
 *
 */
public class preHandlerFilter extends AuthFilter {

	private static Logger logger = LoggerFactory.getLogger(preHandlerFilter.class);

	public static void main(String[] args) {
		String firstContext = "/yourtest";
		String finalString = firstContext.substring(1,firstContext.length());
		System.out.println(finalString);
	}

	/** 
	 * 
	 */
	@Override
	public boolean doFilter(HttpServletRequest request, HttpServletResponse response) throws Exception {

		List<String> subDomainList = FilterMatcher.getSecondDomainList(request);
		
//		if (subDomainList.get(0).equals("sp")) {
//			//saas门户登录
//			String firstContext = UrlUtil.getFirstContext(RequestInfo.getUriNoParams(request));
//			String saasCacheValue = firstContext.substring(1, firstContext.length());
//			response.setHeader("saasCacheValue", saasCacheValue);
//			return true;
//		}
		// 是微服务
		if (subDomainList.get(1).equals(InnerConstants.RESOURCETYPE_MICROSVC)) {
			AppConfig ac = (AppConfig) ApplicationUtil.getSingleton().getContext().getBean("appConfig");
			// 取出配置中心文件类型
			String skipAuthFileType = ac.getSkipAuth();
			// url
			String urlNoParams = RequestInfo.getUriNoParams(request);
			String lastContext = UrlUtil.getLastContext(urlNoParams);
			String requestFileType = null;
			// 获得请求的文件类型,如果请求为/appName_auth/lib/js/app.js则,tempArray[tempArray.length-1]存储为js
			if (!lastContext.contains(".")) {
				// 请求为/bank/auth这种形式的url，当做/bank/auth.do来处理
				requestFileType = "do"; 
			}else{
				String[] tempArray = lastContext.split("\\.");
				requestFileType = tempArray[tempArray.length - 1];
			}
			
			////////////////
			if (isMicroName(request)) {
				// FIXME 未判断/realname_auth/lib/app.js这种情况
				// 是/realname_auth/sms/send.do这种形式
				// 是/realname_auth这种形式
				// 不需设置serviceContext(serviceContext供Nginx寻找业务系统upsteram使用)
				return true;
			} else if (!skipAuthFileType.contains(requestFileType)) {
				// 处理http://qg-service.areacode.com:48011/sms/auth.do这种形式调用 需要鉴权
				String serviceContext = RequestInfo.getServiceContext(request);
				logger.info("RequestInfo.getServiceContext(request) value " + serviceContext);
				request.setAttribute("X-Original-URI", serviceContext);
				serviceContext = serviceContext.substring(1, serviceContext.length());
				response.setHeader("serviceContext", serviceContext);
				return true;
			} else {
				// 处理http://qg-service.areacode.com:48011/lib/app.js这种形式调用js
				// css等形式
				/////////////////
				String ticket = null;
				String token = null;
				String serviceContext = null;
				long nowTime = new Date().getTime();
				// 获取配置中心ticket生命周期
				long ticketLifetime = Long.valueOf(ac.getTicketTimeout());
				// 取出ticket和token
				ticket = RequestInfo.getServiceTicket(request);
				token = RequestInfo.getCookieValue(InnerConstants.COOKIE_TOKEN, request);

				if (ticket == null && token == null) {
					return false;
				}

				if (token != null) {
					// 不是第一次访问
					BasicDBObject statsToken = new BasicDBObject().append(InnerConstants.COOKIE_TOKEN, token);
					// 根据token查询ticket_token表查询
					DBCursor dbCursorToken = MongoDBUtil.getColl(InnerConstants.COLL_TICKET_TOKEN).find(statsToken);
					while (dbCursorToken.hasNext()) {
						BasicDBObject dbOjbectToken = (BasicDBObject) dbCursorToken.next();
						long token_createTime = dbOjbectToken.getLong("microToken_createTime");
						if (nowTime - token_createTime >= Long.valueOf(ac.getTokenTimeout())) {
							// token超时，鉴权失败
							return false;
						} else {
							// token不超时
							// 设置返回头
							serviceContext = dbOjbectToken.getString("serviceContext");
							serviceContext = serviceContext.substring(1, serviceContext.length());
							response.setHeader("serviceContext", serviceContext);
							response.setHeader("microToken", token);
							return true;
						}
					}

				} else {
					// token == null 是第一次,根据ticket表获取微服务名
					// 第一次访问,根据ticket查询ticket_token表
					BasicDBObject statsTicket = new BasicDBObject().append(InnerConstants.COOKIE_TICKET, ticket);
					////////// now-timeOut大于等于ticket_createTime,查询某个ticket
					statsTicket.append("ticket_createTime",
							new BasicDBObject(QueryOperators.GTE, nowTime - ticketLifetime));
					DBCursor dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_TICKET_TOKEN).find(statsTicket);
					////////// ----

					if (dbCursor.size() == 0) {// ticket存在且过期
						return false;
					} else {
						// 存在ticket 不过期
						// DBCursor dbCursorTempTicket =
						// MongoDBUtil.getColl(InnerConstants.COLL_TICKET_TOKEN)
						// .find(statsTicket);
						while (dbCursor.hasNext()) {
							// FIXME 这个while执行了几遍?
							// logger.info("count1");
							BasicDBObject dbOjbectTempTicket = (BasicDBObject) dbCursor.next();
							serviceContext = dbOjbectTempTicket.getString("serviceContext");
							serviceContext = serviceContext.substring(1, serviceContext.length());
							response.setHeader("serviceContext", serviceContext);

							/////////////////////// 写入Token
							token = UUID.randomUUID().toString().replaceAll("-", "");
							BasicDBObject update = new BasicDBObject("$set", new BasicDBObject()
									.append("microToken", token).append("microToken_createTime", nowTime));
							MongoDBUtil.getColl(InnerConstants.COLL_TICKET_TOKEN).update(statsTicket, update);
							response.setHeader("microToken", token);
							/////////////////////// --写入Token
							response.setHeader("microToken", token);
							return true;
						}
						return false;
					}
				}
				// --//
				// --处理http://qg-service.areacode.com:48011/lib/app.js这种形式调用js

			}
		}
		// 非微服务，直接返回true
		return true;
	}

	/**
	 * 根据firstContext查询service_api表
	 * 
	 * @param firstContext
	 * @return 是微服务 return true 不是微服务 return false
	 */
	public boolean isMicroName(HttpServletRequest request) {
		try {
			String urlNoParams = RequestInfo.getUriNoParams(request);
			String areacode = FilterMatcher.getAreacode(request,false);
			String firstContext = UrlUtil.getFirstContext(urlNoParams);
			BasicDBObject service_api = new BasicDBObject().append("url", firstContext)
					.append("resourceType", InnerConstants.RESOURCETYPE_MICROSVC).append("areacode", areacode);
			// 查询路由表
			DBCursor service_api_dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_API).find(service_api);
			if (service_api_dbCursor.size() == 0) {
				logger.info("firstcontext: " + firstContext + " not a microName");
			} else {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
