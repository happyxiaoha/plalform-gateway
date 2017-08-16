package cn.dceast.platform.gateway.auth.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.QueryOperators;

import cn.dceast.platform.gateway.auth.AppConfig;
import cn.dceast.platform.gateway.auth.data.entity.Message;
import cn.dceast.platform.gateway.auth.service.CityUserMappingService;
import cn.dceast.platform.gateway.auth.util.ApplicationUtil;
import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;
import cn.dceast.platform.gateway.auth.util.UrlUtil;

/**
 * demo所用接口
 * 
 * @author liycq
 */
@RestController
public class MicroServiceResourceForDemo {
	private Logger logger = LoggerFactory.getLogger(MicroServiceResourceForDemo.class);
	public static SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * 
	 * @param request
	 * @param response
	 * @param serviceUrl
	 * @param apiKey
	 * @param areacode
	 * @throws IOException
	 */
	@RequestMapping(value = "/getTicketForDemo", method = { RequestMethod.GET })
	public void executeFunction(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("serviceUrl") String serviceUrl, @RequestParam("apiKey") String apiKey,
			@RequestParam("areacode") String areacode) throws IOException {
		////////////////// 初始化变量
		String abbr_citycode = null;
		AppConfig ac = (AppConfig) ApplicationUtil.getSingleton().getContext().getBean("appConfig");
		Map<String, Object> map = new HashMap<String, Object>();
		List<Message> returnList = new ArrayList<Message>();
		Message message = new Message();
		String userId = null;
		String firstContext = UrlUtil.getFirstContext(serviceUrl);
		String ticket = UUID.randomUUID().toString().replaceAll("-", "");// 生成随机ticket
		String serviceDomain = ac.getServiceDomain();
		String context = serviceUrl;
		/////////////////
		logger.info("enter MicroServiceResourceForDemo " + "$$$serviceUrl: " + serviceUrl + "$$$apiKey: " + apiKey
				+ "$$$areacode: " + areacode);

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
		 * 查service_api表进行订单校验
		 */
		BasicDBObject service_api = new BasicDBObject().append("url", firstContext)
				.append("resourceType", InnerConstants.RESOURCETYPE_MICROSVC).append("areacode", areacode);
		// 查询路由表
		DBCursor service_api_dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_API).find(service_api);
		if (service_api_dbCursor.size() == 0) {
			logger.info("[apiKey:" + apiKey + "] not in service_api table; url[" + serviceUrl + "]");
			message.setCode("999999");// 出错
			message.setMessage("[apiKey:" + apiKey + "] not in service_api table; url[" + serviceUrl + "]");
			returnList.add(message);
			map.put("returnResult", returnList);
			String jsonString = JSON.toJSONString(map);
			String jj = "successCallback(" + jsonString + ")";
			logger.info("return value: " + jj);
			InputStream is = new ByteArrayInputStream(jj.toString().getBytes("UTF-8"));
			IOUtils.copy(is, response.getOutputStream());
			response.flushBuffer();
			return;
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
				logger.info("unuseful apiKey:[" + apiKey + "] areacode:[" + areacode + "]  url:[" + serviceUrl
						+ " userId:[" + userId + "]");
				message.setMessage("unuseful apiKey:[" + apiKey + "] areacode:[" + areacode + "]  url:[" + serviceUrl
						+ " userId:[" + userId + "]");
				returnList.add(message);
				map.put("returnResult", returnList);
				String jsonString = JSON.toJSONString(map);
				String jj = "successCallback(" + jsonString + ")";
				logger.info("return value: " + jj);
				InputStream is = new ByteArrayInputStream(jj.toString().getBytes("UTF-8"));
				IOUtils.copy(is, response.getOutputStream());
				response.flushBuffer();
				return;
			}
		}

		/*
		 * 生成ticket
		 */
		long now = new Date().getTime();
		long timeOut = Long.valueOf("100000");// 获取配置中心ticket生命周期
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

		message.setCode("000000");
		//////////////// 根据areacode查询城市简码并更改serviceDomain
		Map<String, String> returnMap = CityUserMappingService.getValueFromCityUserMapping(areacode, null, null);
		abbr_citycode = returnMap.get("abbr_citycode");
		if (abbr_citycode == null) {
			logger.error("areacode:" + areacode + " abbr_citycode is null");
			return;
		}
		if (serviceDomain.contains(";")) {
			//SaaS情况，单一网关需集成多个网关
			String[] results = serviceDomain.split(";");
			for (int i = 0; i < results.length; i++) {
				if (results[i].contains(abbr_citycode)) {
					serviceDomain = results[i];
				}
			}
		}
		/////////////// --根据areacode查询城市简码并更改serviceDomain
		
		
		
		message.setMessage("http://" + serviceDomain + context);
		message.setResult(ticket);
		returnList.add(message);
		map.put("returnResult", returnList);
		String jsonString = JSON.toJSONString(map);
		String jj = "successCallback(" + jsonString + ")";
		logger.info("return value: " + jj);
		InputStream is = new ByteArrayInputStream(jj.toString().getBytes("UTF-8"));
		IOUtils.copy(is, response.getOutputStream());
		response.flushBuffer();
		return;

	}// executeFunction()
}
