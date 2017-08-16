package cn.dceast.platform.gateway.auth.rest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

import cn.dceast.platform.common.mongo.Mongo;
import cn.dceast.platform.gateway.auth.data.entity.Message;
import cn.dceast.platform.gateway.auth.data.entity.ODIRResult;
import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;

/**
 * 简单查库并返回值
 * 
 * @author liycq
 *
 */
@RestController
public class OrderDetailIdResource {

	private static Logger logger = LoggerFactory.getLogger(OrderDetailIdResource.class);
	public static SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@RequestMapping(value = "/getorderDetailIdCount", method = { RequestMethod.POST, RequestMethod.GET })
	public String execute(HttpServletRequest request, HttpServletResponse response, String data) throws Exception {

		if (data == null) {
			logger.info("/getorderDetailIdCount data==null");
			return constructReturnMessage("null", InnerConstants.RETURN_CODE_FAIL, "data null", "false");
		}
		logger.info("one request start==================================");
		logger.info("enter OrderDetailIdResource " + "data value: " + data.toString());

		JSONArray json = new JSONObject().parseArray(data);
		List<Map<String, Object>> orderList = JSONObject.toJavaObject(json, List.class);
		List<ODIRResult> list = new ArrayList<ODIRResult>();
		String resourceId = null;
		String resourceType = null;
		String userId = null;
		String areacode = null;
		int orderDetailId = 0;
		String endTime = null;

		for (Map<String, Object> singleOrder : orderList) {
			resourceId = (String) singleOrder.get("resourceId");
			resourceType = (String) singleOrder.get("resourceType");
			userId = (String) singleOrder.get("userId");
			areacode = (String) singleOrder.get("areacode");
			orderDetailId = Integer.parseInt(singleOrder.get("orderDetailId").toString());
			BasicDBObject basicDBObject = new BasicDBObject().append("resourceId", resourceId)
					.append("resourceType", resourceType).append("userId", userId).append("areacode", areacode)
					.append("orderDetailId", orderDetailId);
			DBCursor dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).find(basicDBObject);
			if (dbCursor.size() == 0) {
				// order_rule_list表中不存在
				DBCursor dbCursor2 = MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST_HISTORY)
						.find(basicDBObject);
				if (dbCursor2.size() == 0) {
					logger.error("resourceId: " + resourceId + " userId: " + userId + " areacode: " + areacode
							+ "  orderDetailId:" + orderDetailId + " resourceType:" + resourceType
							+ " not in order_rule_list table and not in history table");
					list.add(constructOrderDetailIdResourceResult(
							"not in order_rule_list table and not in history table", resourceId, orderDetailId, 0,
							userId, areacode, resourceType, null, null, null));
				} else {
					while (dbCursor2.hasNext()) {
						/*
						 * 注意，这个while中的代码和下面else中的while代码一模一样
						 */
						BasicDBObject basicDBObject2 = (BasicDBObject) dbCursor2.next();
						String left = null;
						String number = basicDBObject2.getString("number");
						String currentUsed = basicDBObject2.getString("currentUsed");
						endTime = basicDBObject2.getString("endTime");
						String timeLeft = calculateTime(endTime);
						logger.info(
								"resourceId: " + resourceId + " orderDetailId:" + orderDetailId + " userId:" + userId);
						if (number.equals("-1")) {// 不限次数
							left = "-1";
						} else {// 限次
							left = Integer.parseInt(number) - Integer.parseInt(currentUsed) + "";
						}
						list.add(constructOrderDetailIdResourceResult(null, resourceId, orderDetailId,
								Integer.parseInt(currentUsed), userId, areacode, resourceType, number, left, timeLeft));
					} // while (dbCursor.hasNext())
				}

			} else {
				// 根据查出来的信息,查询oder_rule_list表的订单, 仅循环一次
				while (dbCursor.hasNext()) {
					BasicDBObject basicDBObject2 = (BasicDBObject) dbCursor.next();
					String left = null;
					String number = basicDBObject2.getString("number");
					String currentUsed = basicDBObject2.getString("currentUsed");
					endTime = basicDBObject2.getString("endTime");
					String timeLeft = calculateTime(endTime);
					logger.info("resourceId: " + resourceId + " orderDetailId:" + orderDetailId + " userId:" + userId);
					if (number.equals("-1")) {// 不限次数
						left = "-1";
					} else {// 限次
						left = Integer.parseInt(number) - Integer.parseInt(currentUsed) + "";
					}
					list.add(constructOrderDetailIdResourceResult(null, resourceId, orderDetailId,
							Integer.parseInt(currentUsed), userId, areacode, resourceType, number, left, timeLeft));
				} // while (dbCursor.hasNext())
			}
		} // for (Map<String, Object> singleOrder : orderList)
		return constructReturnMessage(list, InnerConstants.RETURN_CODE_SUCC, "success", "OK");
	}

	/**
	 * (已废弃)构建返回消息 因返回内容如下，result指向的这个数组后加了双引号：
	 * {"code":"000000","message":"success","result":
	 * "[{\"areacode\":\"520100\",\"left\":\"-1\",\"number\":\"-1\",\"orderDetailId\":1109,\"resourceId\":\"21\",\"resourceType\":\"01\",\"timeLeft\":\"730\",\"userId\":\"440100000002798\"}]"
	 * ,"status":"OK"}
	 * 
	 * @Title: constructReturnMessage
	 * @param @param
	 *            code 返回代码
	 * @param @param
	 *            message 返回信息
	 * @param @return
	 * @return String
	 */
	private String constructReturnMessage(String result, String code, String message, String status) {
		Message msg = new Message();
		msg.setResult(result);
		msg.setCode(code);
		msg.setMessage(message);
		msg.setStatus(status);
		return new JSONObject().toJSONString(msg);
	}

	/**
	 * 
	 */
	private String constructReturnMessage(List<ODIRResult> result, String code, String messge, String status) {
		Map returnMap = new HashMap();
		returnMap.put("result", result);
		returnMap.put("code", code);
		returnMap.put("message", messge);
		returnMap.put("status", status);
		return new JSONObject().toJSONString(returnMap);
	}

	private ODIRResult constructOrderDetailIdResourceResult(String error, String resourceId, int orderDetailId,
			int currentUsed, String userId, String areacode, String resourceType, String number, String left,
			String timeLeft) {

		ODIRResult orderDetailIdResourceResult = new ODIRResult();
		orderDetailIdResourceResult.setError(error);
		orderDetailIdResourceResult.setAreacode(areacode);
		orderDetailIdResourceResult.setOrderDetailId(orderDetailId);
		orderDetailIdResourceResult.setResourceId(resourceId);
		orderDetailIdResourceResult.setResourceType(resourceType);
		orderDetailIdResourceResult.setUserId(userId);
		orderDetailIdResourceResult.setLeft(left);
		orderDetailIdResourceResult.setNumber(number);
		orderDetailIdResourceResult.setTimeLeft(timeLeft);
		orderDetailIdResourceResult.setCurrentUsed(currentUsed);
		return orderDetailIdResourceResult;
	}

	/**
	 * 计算当前时间距离endTime之间的时间差
	 * 
	 * @param endTime
	 * @return
	 * @throws Exception
	 */
	private String calculateTime(String endTime) throws Exception {
		String nowTime = sFormat.format(new Date());
		Calendar cal = Calendar.getInstance();
		// cal.setTime(sFormat.parse(nowTime));
		long now = sFormat.parse(nowTime).getTime();
		long end = sFormat.parse(endTime).getTime();
		// 当前时间与库中endTime的差值
		int day = (int) (Math.abs(end - now) / (1000 * 60 * 60 * 24));
		// 当now和end在同一天内时,如上算法得到的day为0,因此,因业务需要,需执行day++
		day++;
		logger.info("nowTime: " + nowTime + " endTime:" + endTime + " day:" + day);
		return day + "";
	}
}
