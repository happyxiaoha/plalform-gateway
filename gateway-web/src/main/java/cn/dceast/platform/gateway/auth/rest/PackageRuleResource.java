package cn.dceast.platform.gateway.auth.rest;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.dceast.platform.gateway.auth.AppConfig;
import cn.dceast.platform.gateway.auth.data.entity.Message;
import cn.dceast.platform.gateway.auth.executor.ExecutorHolder;
import cn.dceast.platform.gateway.auth.executor.task.UserInfoService;
import cn.dceast.platform.gateway.auth.service.ServiceStatisticsService;
import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.digitalchina.resttemplate.ribbon.retryable.RetryableLoadbalancedRestTemplateUtil;
import com.mongodb.BasicDBObject;

@RestController
public class PackageRuleResource {

	private static Logger logger = LoggerFactory.getLogger(PackageRuleResource.class);

	@Autowired
	private AppConfig appConfig;

	@Autowired
	@Qualifier("retryableLoadbalancedRestTemplateUtil")
	RetryableLoadbalancedRestTemplateUtil restTemplate;

	/*
	 * 3.3. 推送网关资源访问的时间，次数规则
	 */
	@RequestMapping(value = "/addRule", method = { RequestMethod.POST, RequestMethod.GET })
	public String addPackageRule(HttpServletRequest request, HttpServletResponse response, String data) {

		logger.info("enter /addRule " + "data value: " + data.toString());

		Message msg = new Message();
		if (data == null) {
			msg.setCode(InnerConstants.RETURN_CODE_FAIL);
			msg.setMessage("新增套餐失败-参数为空");
			msg.setResult("false");
			msg.setStatus("ERROR");
			return new JSONObject().toJSONString(msg);
		}
		JSONArray jso = new JSONObject().parseArray(data);
		List<Map<String, Object>> orderList = JSONObject.toJavaObject(jso, List.class);

		/*
		 * 存入order_rule_list表，只是将某个订单简单执行了insert操作，没有判断订单表中是否已存在
		 */
		BasicDBObject stats = new BasicDBObject();
		for (Map<String, Object> singleOrder : orderList) {
			for (Map.Entry<String, Object> map : singleOrder.entrySet()){
				//将请求中的key和value写入stats
				stats.append(map.getKey(), map.getValue());
			}
			stats.append("currentUsed", 0);
			stats.append("failNumber", 0);
			MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).insert(stats);
			stats.clear();
			logger.info(new Date() + " add order_rule_list success [" + singleOrder.get("orderDetailId") + "]");
			if (singleOrder.get("userId") != null) {
				/*
				 * 3.8 get(appName, uri, HttpHeaders) post(url,params,headers)
				 * 拉取用户信息 插入 serivice_key
				 */
				// 异步执行任务 FIXME 异步执行这个任务有什么用？
				try {
					ExecutorHolder.callerRequestRecordExecutor.execute(new UserInfoService(restTemplate,
							appConfig.getDataCenterEurekAaddress(), String.valueOf(singleOrder.get("userId"))));
				} catch (Exception e) {
					e.printStackTrace();
					logger.error(String.format("gateway get userinfo exception %s", e.getMessage()));
				}
			}
			Map returnMap = new HashMap();
			returnMap.put("code", InnerConstants.RETURN_CODE_SUCC);
			returnMap.put("message", "success新增套餐成功");
			returnMap.put("status", "OK");
			singleOrder.put("result", "true");
			singleOrder.put("message", returnMap);
		}

		return new JSONObject().toJSONString(orderList);
	}

	@RequestMapping(value = "/updateRule", method = { RequestMethod.POST, RequestMethod.GET })
	public String updatePackageRule(HttpServletRequest request, HttpServletResponse response, String data) {
		logger.info("enter /updateRule " + "data value: " + data.toString());
		Message msg = new Message();
		if (data == null) {
			msg.setCode(InnerConstants.RETURN_CODE_FAIL);
			msg.setMessage("更新套餐失败-参数为空");
			msg.setStatus("ERROR");
			return new JSONObject().toJSONString(msg);
		}
		JSONArray jso = new JSONObject().parseArray(data);
		List<Map<String, Object>> orderList = JSONObject.toJavaObject(jso, List.class);

		/*
		 * 存入order_rule_list表
		 */
		BasicDBObject stats = new BasicDBObject();
		BasicDBObject update = new BasicDBObject();
		for (Map<String, Object> singleOrder : orderList) {
			stats.append("orderDetailId", singleOrder.get("orderDetailId"));
			for (Map.Entry<String, Object> map : singleOrder.entrySet()) {
				update.append(map.getKey(), map.getValue());
			}
			MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).update(stats, new BasicDBObject("$set", update));
			stats.clear();
			update.clear();
			logger.info(new Date() + " update order_rule_list success [" + singleOrder.get("orderDetailId") + "]");
		}
		msg.setCode(InnerConstants.RETURN_CODE_SUCC);
		msg.setMessage("更新套餐成功");
		msg.setStatus("OK");

		return new JSONObject().toJSONString(msg);
	}

	@RequestMapping(value = "/delRule", method = { RequestMethod.POST, RequestMethod.GET })
	public String deletePackageRule(HttpServletRequest request, HttpServletResponse response, String orderDetailId) {
		logger.info("enter /delRule " + "orderDetailId value: " +orderDetailId);
		Message msg = new Message();
		if (orderDetailId == null) {
			msg.setCode(InnerConstants.RETURN_CODE_FAIL);
			msg.setMessage("删除套餐失败-参数为空");
			msg.setStatus("ERROR");
			return new JSONObject().toJSONString(msg);
		}
		JSONArray jso = new JSONObject().parseArray(orderDetailId);
		List<String> orderList = JSONObject.toJavaObject(jso, List.class);

		BasicDBObject stats = new BasicDBObject();
		for (String singleOrder : orderList) {
			stats.append("orderDetailId", singleOrder);
			MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).remove(stats);
			stats.clear();
			logger.info(new Date() + " delete order_rule_list success orderDetailId :[" + orderDetailId + "]");
		}
		msg.setCode(InnerConstants.RETURN_CODE_SUCC);
		msg.setMessage("删除套餐成功");
		msg.setStatus("OK");

		return new JSONObject().toJSONString(msg);
	}

}
