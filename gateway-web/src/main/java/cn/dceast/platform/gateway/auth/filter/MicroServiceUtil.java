package cn.dceast.platform.gateway.auth.filter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.dceast.platform.gateway.auth.AppConfig;
import cn.dceast.platform.gateway.auth.data.entity.Appkey;
import cn.dceast.platform.gateway.auth.util.ApplicationUtil;
import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;
import cn.dceast.platform.gateway.auth.util.UrlUtil;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

public class MicroServiceUtil {
	private static Logger logger = LoggerFactory.getLogger(MicroServiceUtil.class);

	/**
	 * 微服务鉴权, 只做了一个验证token的动作
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public static String microServiceAuth(HttpServletRequest request, HttpServletResponse response) throws Exception {

		long now = new Date().getTime();
		// 拿到配置中心配置
		AppConfig ac = (AppConfig) ApplicationUtil.getSingleton().getContext().getBean("appConfig");
		String ticket = RequestInfo.getServiceTicket(request);
		String token = null;
		token = RequestInfo.getCookieValue(InnerConstants.COOKIE_TOKEN, request);
		String userId = null;
		Appkey appkeyInfo = RequestInfo.getAppkeyInfo(request);
		String url = RequestInfo.getUriNoParams(request);
		String firstContext = UrlUtil.getFirstContext(url);

		//////////////////////// 获取userId
		List<String> subDomainList = FilterMatcher.getSecondDomainList(request);
		if (!subDomainList.get(1).equals(InnerConstants.RESOURCETYPE_MICROSVC)) {
			// 不是微服务
			userId = appkeyInfo == null ? null : appkeyInfo.getUserId();
		} else {
			// 为微服务
			userId = RequestInfo.getUserId(request);
			if (userId == null) {
				return null;
			}
		}
		//////////////////////

		if (ticket == null && token == null) {
			return "ticket is null && token is null";
		}

		if (token != null) {
			// 非第一次访问微服务,验证token token不为空
			// if (token == null) {
			// response.setHeader("code", FilterResponseMessage.CODE_500100);
			// return "token is null";
			// } else {

			BasicDBObject db = new BasicDBObject(InnerConstants.COOKIE_TOKEN, token).append("userId", userId)
					.append("serviceContext", firstContext);
			DBCursor dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_TICKET_TOKEN).find(db);
			if (dbCursor.size() == 0) {
				response.setHeader("code", FilterResponseMessage.CODE_500103);
				logger.info("token could not match userId or serviceContext. serviceContext value:" + firstContext
						+ " userId: " + userId);
				return checkTicket(response, now, ac, ticket, userId, firstContext);
				// return "token could not match userId or serviceContext.
				// serviceContext value:" + firstContext
				// + " userId: " + userId;
			}
			while (dbCursor.hasNext()) {
				// 检查token是否过期
				BasicDBObject dbOjbect = (BasicDBObject) dbCursor.next();
				long token_createTime = dbOjbect.getLong("microToken_createTime");
				if (now - token_createTime > Long.valueOf(ac.getTokenTimeout())) {
					response.setHeader("code", FilterResponseMessage.CODE_500101);
					return "ticket is null,token is outofdate";
				}
			}
			response.setHeader("microToken", token);
		} else if (ticket != null) {
			return checkTicket(response, now, ac, ticket, userId, firstContext);
		}
		// 正常
		return null;
	}

	/**
	 * 
	 * @param response
	 * @param now
	 * @param ac
	 * @param ticket
	 * @param userId
	 * @return
	 */
	private static String checkTicket(HttpServletResponse response, long now, AppConfig ac, String ticket,
			String userId, String firstContext) {
		String token;
		// 有ticket则为第一次访问，此时token为空,需生成token
		BasicDBObject db = new BasicDBObject("ticket", ticket).append("userId", userId).append("serviceContext",
				firstContext);
		DBCursor dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_TICKET_TOKEN).find(db);
		if (dbCursor.size() == 0) {
			response.setHeader("code", FilterResponseMessage.CODE_500103);
			return "ticket not match userId or serviceContext" + " userId:" + userId + " serviceContext:" + firstContext
					+ " ticket:" + ticket;
		}
		while (dbCursor.hasNext()) {
			// 遍历查出来的ticket
			BasicDBObject dbOjbect = (BasicDBObject) dbCursor.next();
			long ticket_createTime = dbOjbect.getLong("ticket_createTime");
			if (now - ticket_createTime > Long.valueOf(ac.getTicketTimeout())) {
				// 与库中记录比较，检查ticket是否过期
				response.setHeader("code", FilterResponseMessage.CODE_500102);
				return "ticket value: " + ticket + "outdate";
			}
			// token指向库中microToken
			token = dbOjbect.getString("microToken");
			if (token == null) {
				// 库中无token,则生成
				token = UUID.randomUUID().toString().replaceAll("-", "");
				BasicDBObject update = new BasicDBObject("$set",
						new BasicDBObject().append("microToken", token).append("microToken_createTime", now));
				MongoDBUtil.getColl(InnerConstants.COLL_TICKET_TOKEN).update(db, update);
				response.setHeader("microToken", token);
			} else {
				// 库中有token
				long token_createTime = dbOjbect.getLong("microToken_createTime");
				if (now - token_createTime >= Long.valueOf(ac.getTokenTimeout())) {
					// token超时，则重新生成
					token = UUID.randomUUID().toString().replaceAll("-", "");
					BasicDBObject update = new BasicDBObject("$set",
							new BasicDBObject().append("microToken", token).append("microToken_createTime", now));
					// 替换掉超时token
					MongoDBUtil.getColl(InnerConstants.COLL_TICKET_TOKEN).update(db, update);
					response.setHeader("microToken", token);
					return null;
				} else {
					// token不超时，不重新生成
					response.setHeader("microToken", token);
					return null;
				}
			}
		}
		return null;
	}
}
