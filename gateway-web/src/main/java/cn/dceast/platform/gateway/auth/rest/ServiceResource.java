package cn.dceast.platform.gateway.auth.rest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.dceast.platform.gateway.auth.data.entity.Message;
import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.QueryOperators;

/**
 * @author liycq
 *
 */
@RestController
public class ServiceResource {

	private static Logger logger = LoggerFactory.getLogger(ServiceResource.class);

	DateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/*
	 * 3.1 在有效时间范围内，获取资源可使用次数
	 * 
	 * @userId 用户标识，查询用户的 id
	 * 
	 * @resourceId 资源标识，查询资源的 id
	 * 
	 * @timePoint 时间点，查询的时间点
	 */
	@RequestMapping(value = "/getAvailableCount", method = { RequestMethod.POST, RequestMethod.GET })
	public String getResourceAvailableCount(HttpServletRequest request, HttpServletResponse response, String data) {

		List<Map<String, String>> returnList = new ArrayList();
		Message msg = new Message();
		if (data == null) {
			msg.setCode(InnerConstants.RETURN_CODE_FAIL);
			msg.setMessage("获取资源可使用次数-参数为空");
			msg.setStatus("ERROR");
			return new JSONObject().toJSONString(msg);
		}
		JSONArray jso = new JSONObject().parseArray(data);
		List<Map<String, String>> orderList = JSONObject.toJavaObject(jso, List.class);
		BasicDBObject query = new BasicDBObject();
		for (Map<String, String> singleOrder : orderList) {
			// 未使用的次数
			long unused = 0;
			query.append("userId", singleOrder.get("userId")).append("resourceId", singleOrder.get("resourceId"))
					.append("resourceType", singleOrder.get("resourceType"))
					.append("endTime", new BasicDBObject(QueryOperators.GTE, singleOrder.get("timePoint")))
					.append("startTime", new BasicDBObject(QueryOperators.LTE, singleOrder.get("timePoint")))
					.append("areacode", singleOrder.get("areacode")).append("number", -1);

			DBCursor dbCursor = null;
			// 查询不限次的订单
			dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).find(query);
			if (dbCursor.size() > 0) {
				unused = -1;
			} else {
				// 查询其他类型的订单
				query.remove("number");
				dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).find(query);
				while (dbCursor.hasNext()) {
					// 只能查询出一条内容
					BasicDBObject stats = (BasicDBObject) dbCursor.next();
					// 未使用的次数
					unused += stats.getLong("number") - stats.getLong("currentUsed");
				}
			}

			///////////// 返回数据
			Map<String, String> map = new HashMap<String, String>();
			map.put("userId", singleOrder.get("userId"));
			map.put("resourceId", singleOrder.get("resourceId"));
			map.put("resourceType", singleOrder.get("resourceType"));
			map.put("timePoint", singleOrder.get("timePoint"));
			map.put("unused", String.valueOf(unused));
			map.put("areacode", singleOrder.get("areacode"));
			returnList.add(map);
			/////////////

			query.clear();
			if (logger.isDebugEnabled())
				logger.debug(
						"获取资源可使用次数 userId[" + singleOrder.get("userId") + "]resourceId[" + singleOrder.get("resourceId")
								+ "]timePoint[" + singleOrder.get("timePoint") + "]unused" + String.valueOf(unused));
		}
		Map returnMap = new HashMap();
		returnMap.put("result", returnList);
		returnMap.put("message", "success");
		returnMap.put("code", InnerConstants.RETURN_CODE_SUCC);
		returnMap.put("status", "OK");
		return new JSONObject().toJSONString(returnMap);
	}

	/**
	 * 3.2. 获取资源的总使用次数、总剩余次数
	 */
	@RequestMapping(value = "/getTotalCount", method = { RequestMethod.POST, RequestMethod.GET })
	public String gettotalCount(HttpServletRequest request, HttpServletResponse response, String data) {

		List<Map<String, String>> returnList = new ArrayList();
		Message msg = new Message();
		if (data == null) {
			msg.setCode(InnerConstants.RETURN_CODE_FAIL);
			msg.setMessage("获取资源总使用次数、总剩余次数-参数为空");
			msg.setStatus("ERROR");
			return new JSONObject().toJSONString(msg);
		}
		JSONArray jso = new JSONObject().parseArray(data);
		List<Map<String, String>> orderList = JSONObject.toJavaObject(jso, List.class);

		//
		boolean hasNoLimit = false;
		BasicDBObject query = new BasicDBObject();
		for (Map<String, String> orderMap : orderList) {
			// 已使用次数
			long used = 0;
			// 未使用次数
			long unused = 0;
			query.append("userId", orderMap.get("userId")).append("areacode", orderMap.get("areacode"))
					.append("resourceId", orderMap.get("resourceId"))
					.append("resourceType", orderMap.get("resourceType"));

			DBCursor dbCursor = null;
			try {
				// 查询订单历史表
				dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST_HISTORY).find(query);// 历史表
				while (dbCursor.hasNext()) {
					BasicDBObject stats = (BasicDBObject) dbCursor.next();
					used += stats.getLong("currentUsed");
				}
				dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).find(query);
				while (dbCursor.hasNext()) {
					BasicDBObject stats = (BasicDBObject) dbCursor.next();
					used += stats.getLong("currentUsed");
					if (stats.getLong("number") == -1)
						hasNoLimit = true;
					if (!hasNoLimit)
						unused += stats.getLong("number") - stats.getLong("currentUsed");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			Map<String, String> map = new HashMap();
			map.put("userId", orderMap.get("userId"));
			map.put("resourceId", orderMap.get("resourceId"));
			map.put("resourceType", orderMap.get("resourceType"));
			map.put("used", String.valueOf(used));
			map.put("unused", hasNoLimit ? "-1" : String.valueOf(unused));
			map.put("areacode", orderMap.get("areacode"));
			returnList.add(map);
			query.clear();
		}
		Map returnMap = new HashMap();
		returnMap.put("result", returnList);
		returnMap.put("message", "success");
		returnMap.put("code", InnerConstants.RETURN_CODE_SUCC);
		returnMap.put("status", "OK");
		return new JSONObject().toJSONString(returnMap);
	}

	
	
	/**
	 * 3.5.获取API调用成功次数, 查询历史表和订单表, 并将这两个表查询出的总次数相加
	 * 
	 * 从日志表获取service_call_log
	 */
	@RequestMapping(value = "/getInvokeCount", method = { RequestMethod.POST, RequestMethod.GET })
	public String getInvokeCount(HttpServletRequest request, HttpServletResponse response, String data) {

		List<Map> returnList = new ArrayList();
		Message msg = new Message();
		if (data == null) {
			msg.setCode(InnerConstants.RETURN_CODE_FAIL);
			msg.setMessage("获取调用成功次数-参数为空");
			msg.setStatus("ERROR");
			return new JSONObject().toJSONString(msg);
		}

		JSONArray jso = new JSONObject().parseArray(data);
		List<Map<String, String>> orderList = JSONObject.toJavaObject(jso, List.class);

		for (Map<String, String> resMap : orderList) {
			//总调用次数
			long total = 0;
			BasicDBObject query = new BasicDBObject("areacode", resMap.get("areacode")).append("resourceId",
					resMap.get("resourceId"));
			DBCursor dbCursor = null;
			try {
				//先查历史表,查询出被使用和失败的次数
				dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST_HISTORY).find(query);// 历史表
				while (dbCursor.hasNext()) {
					BasicDBObject stats = (BasicDBObject) dbCursor.next();
					total += stats.getLong("currentUsed") + stats.getLong("failNumber");
				}
				
				//再查订单表
				dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).find(query);
				while (dbCursor.hasNext()) {
					BasicDBObject stats = (BasicDBObject) dbCursor.next();
					total += stats.getLong("currentUsed") + stats.getLong("failNumber");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			Map<String, Object> map = new HashMap();
			map.put("resourceId", resMap.get("resourceId"));
			map.put("areacode", resMap.get("areacode"));
			map.put("total", total);
			returnList.add(map);

		}
		
		
		
		Map returnMap = new HashMap();
		returnMap.put("result", returnList);
		returnMap.put("message", "success");
		returnMap.put("code", InnerConstants.RETURN_CODE_SUCC);
		returnMap.put("status", "OK");
		return new JSONObject().toJSONString(returnMap);
	}

	/*
	 * 统计推广api的成功调用次数
	 */
	@RequestMapping(value = "/getSuccInvokeCount", method = { RequestMethod.POST, RequestMethod.GET })
	public String getBlackListInvokeCount(HttpServletRequest request, HttpServletResponse response, String data) {

		List<Map> returnList = new ArrayList();
		Message msg = new Message();
		if (data == null) {
			msg.setCode(InnerConstants.RETURN_CODE_FAIL);
			msg.setMessage("获取API调用次数-参数为空");
			msg.setStatus("ERROR");
			return new JSONObject().toJSONString(msg);
		}

		JSONArray jso = new JSONObject().parseArray(data);
		List<Map<String, String>> orderList = JSONObject.toJavaObject(jso, List.class);

		BasicDBObject query = new BasicDBObject();
		for (Map<String, String> resMap : orderList) {
			long total = 0;

			try {
				if (checkResourcePromote(resMap)) {
					query.clear();
					query.append("uri", resMap.get("context")).append("callerIP", resMap.get("ip"))
							.append("resourceType", resMap.get("resourceType"))
							.append("areacode", resMap.get("sourcecity")).append("code", "200");
					if (resMap.get("timePoint") != null)
						query.append("callTime",
								new BasicDBObject(QueryOperators.GTE, sFormat.parse(resMap.get("timePoint"))));
					total = MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_CALL_LOG).count(query);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			Map<String, Object> map = new HashMap();
			map.put("sourcecity", resMap.get("sourcecity"));
			map.put("areacode", resMap.get("areacode"));
			map.put("context", resMap.get("context"));
			map.put("resourceType", resMap.get("resourceType"));
			map.put("ip", resMap.get("ip"));
			map.put("total", total);
			returnList.add(map);

		}
		Map returnMap = new HashMap();
		returnMap.put("result", returnList);
		returnMap.put("message", "success");
		returnMap.put("code", InnerConstants.RETURN_CODE_SUCC);
		returnMap.put("status", "OK");
		return new JSONObject().toJSONString(returnMap);
	}

	
	
	
	/**
	 * 检查某个请求是否为推广
	 * @param resMap
	 * @return
	 */
	public boolean checkResourcePromote(Map<String, String> resMap) {
		BasicDBObject object = new BasicDBObject("areacode", resMap.get("areacode"))
				.append("url", resMap.get("context")).append("resourceType", resMap.get("resourceType"));
		BasicDBObject dbCursor = (BasicDBObject) MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_API).findOne(object);
		if (dbCursor != null) {
			if (!resMap.get("areacode").equals(dbCursor.getString("sourcecity"))) {
				//仅根据areacode查询cityUserMapping表
				BasicDBObject ipObject = new BasicDBObject("areacode", resMap.get("areacode"));
				BasicDBObject ipCursor = (BasicDBObject) MongoDBUtil.getColl(InnerConstants.COLL_CITYUSERMAPPING)
						.findOne(ipObject);
				if (ipCursor != null) {
					//判断resMap.get("ip")是否在cityUserMapping表的记录中
					String ip = ipCursor.getString("ip");
					List<String> ipList = Arrays.asList(ip.split(";"));
					if (ipList.contains(resMap.get("ip")))
						return true;
				}
			}
		}
		return false;
	}
}
