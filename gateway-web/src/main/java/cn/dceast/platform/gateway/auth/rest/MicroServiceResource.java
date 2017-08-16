package cn.dceast.platform.gateway.auth.rest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.dceast.platform.gateway.auth.AppConfig;
import cn.dceast.platform.gateway.auth.filter.FilterMatcher;
import cn.dceast.platform.gateway.auth.filter.RequestInfo;
import cn.dceast.platform.gateway.auth.service.CityUserMappingService;
import cn.dceast.platform.gateway.auth.util.ApplicationUtil;
import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;
import cn.dceast.platform.gateway.auth.util.UrlUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.QueryOperators;

/**
 * 
 * @author liycq
 *
 */
@RestController
public class MicroServiceResource {
	private Logger logger = LoggerFactory.getLogger(MicroServiceResource.class);

	public static SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/*
	 * 根据 秘钥&服务标识&用户标识生成ticket
	 */
	@RequestMapping(value = "/getTicket", method = { RequestMethod.POST, RequestMethod.GET })
	public String executeTicket(HttpServletRequest request, HttpServletResponse response, String data)
			throws ServletException, IOException {

		AppConfig ac = (AppConfig) ApplicationUtil.getSingleton().getContext().getBean("appConfig");
		String abbr_citycode = null;

		if (data == null) {
			logger.error("/getTicket data == null");
			return "http://404notfound.com";
		}

		logger.info("enter MicroServiceResource " + "data value: " + data.toString());

		JSONArray jso = new JSONObject().parseArray(data);
		List<Map<String, String>> orderList = JSONObject.toJavaObject(jso, List.class);
		// 来自配置中心,ac.getServiceDomain()的值是service.areacode.com:48011
		String serviceDomain = ac.getServiceDomain();
		String context = null;
		String ticket = UUID.randomUUID().toString().replaceAll("-", "");// 生成随机ticket
		for (Map<String, String> singleOrder : orderList) {// 理论上,仅循环一次
			context = singleOrder.get("serviceUrl");
			String serviceUrl = UrlUtil.getUriNoParams(context);// context和serviceUrl的值一致
			String areacode = singleOrder.get("areacode");
			String apiKey = singleOrder.get("apiKey");
			String userId = null;
			String firstContext = UrlUtil.getFirstContext(serviceUrl);

			//////////////// 根据areacode查询城市简码并更改serviceDomain
			Map<String, String> returnMap = CityUserMappingService.getValueFromCityUserMapping(areacode, null, null);
			abbr_citycode = returnMap.get("abbr_citycode");
			if (abbr_citycode == null) {
				logger.error("areacode:" + areacode + " abbr_citycode is null");
				return "http://404notfound.com";
			}
			String[] results = serviceDomain.split(";");
			for (int i = 0; i < results.length; i++) {
				if (results[i].contains(abbr_citycode)) {
					serviceDomain = results[i];
				}
			}
			/////////////// --根据areacode查询城市简码
			if (apiKey == null) {
				logger.error("get ticket context [" + context + "] apikey is null");
				return "http://404notfound.com";
			}

			/*
			 * 根据apiKey查库获取userId
			 */
			BasicDBObject service_key = new BasicDBObject("apiKey", apiKey);
			DBCursor service_key_cursor = MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_KEY).find(service_key);
			while (service_key_cursor.hasNext()) {
				BasicDBObject ob = (BasicDBObject) service_key_cursor.next();
				userId = ob.getString("userId");
				break;
			}

			/*
			 * apiKey黑名单过来
			 */
			if (FilterMatcher.isExistsBlackOfAppkey(apiKey)) {
				logger.error("appKey author fail: " + apiKey);
				return "http://404notfound.com";
			}

			/*
			 * 查service_api表进行订单校验
			 */
			BasicDBObject service_api = new BasicDBObject().append("url", firstContext)
					.append("resourceType", InnerConstants.RESOURCETYPE_MICROSVC).append("areacode", areacode);
			// 查询路由表
			DBCursor service_api_dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_API).find(service_api);
			if (service_api_dbCursor.size() == 0) {
				logger.error("[apiKey:" + apiKey + "] not in service_api table; url[" + serviceUrl + "]");
				return "http://404notfound.com";
			}
			while (service_api_dbCursor.hasNext()) {// 根据查出来的路由,查询oder_rule_list表的订单
				BasicDBObject ob = (BasicDBObject) service_api_dbCursor.next();

				BasicDBObject tempOb = new BasicDBObject().append("resourceId", ob.getString("resourceId"))
						.append("resourceType", InnerConstants.RESOURCETYPE_MICROSVC).append("areacode", areacode)
						.append("userId", userId)
						.append("endTime", new BasicDBObject(QueryOperators.GTE, sFormat.format(new Date())))
						.append("startTime", new BasicDBObject(QueryOperators.LTE, sFormat.format(new Date())));
				// 查询order_rule_list
				DBCursor order_cursor = MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).find(tempOb);
				if (order_cursor.size() < 1) {
					// 没有订单
					logger.error("unuseful apiKey:[" + apiKey + "] areacode:[" + areacode + "]  url:[" + serviceUrl
							+ " userId:[" + userId + "]");
				}
			}

			/*
			 * 生成ticket
			 */
			long now = new Date().getTime();
			long timeOut = Long.valueOf(ac.getTicketTimeout());// 获取配置中心ticket生命周期
			BasicDBObject stats = new BasicDBObject().append("serviceContext", firstContext)
					.append("serviceSecondDomain", serviceDomain).append("areacode", areacode).append("userId", userId);
			DBCursor cursor = MongoDBUtil.getColl(InnerConstants.COLL_TICKET_TOKEN).find(stats);
			if (cursor.size() == 0) {// 没找到,则表中新增ticket
				stats.append("ticket_createTime", now).append("ticket", ticket);
				MongoDBUtil.getColl(InnerConstants.COLL_TICKET_TOKEN).insert(stats);
			} else {
				// 找到了旧的tikcet
				MongoDBUtil.getColl(InnerConstants.COLL_TICKET_TOKEN).insert(stats);// FIXME
																					// 不明白为啥这里有个插入动作
				stats.append("ticket_createTime", new BasicDBObject(QueryOperators.GTE, now - timeOut));
				DBCursor dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_TICKET_TOKEN).find(stats);
				if (dbCursor.size() == 0) {// 存在旧的ticket 过期
					stats.remove("ticket_createTime");
					// 放入新的ticket
					BasicDBObject update = new BasicDBObject().append("$set",
							new BasicDBObject("ticket_createTime", now).append("ticket", ticket));
					MongoDBUtil.getColl(InnerConstants.COLL_TICKET_TOKEN).update(stats, update);
				} else {
					// 存在 没过期,拿到旧的ticket
					while (dbCursor.hasNext()) {
						BasicDBObject new_dbCursor = (BasicDBObject) dbCursor.next();
						ticket = new_dbCursor.getString("ticket");
						break;
					}
				}
			}
		}

		// 返回微服务地址和ticket
		return "http://" + serviceDomain + context + "?ticket=" + ticket;
	}
}
