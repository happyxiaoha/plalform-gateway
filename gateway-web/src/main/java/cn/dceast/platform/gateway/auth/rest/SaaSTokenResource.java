package cn.dceast.platform.gateway.auth.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCursor;
import com.mongodb.QueryOperators;
import com.quantum.auth.Authcode;

import cn.dceast.platform.gateway.auth.data.entity.Message;
import cn.dceast.platform.gateway.auth.data.entity.RequestSaaSBean;
import cn.dceast.platform.gateway.auth.data.entity.RequestSaaSRuleList;
import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;

/**
 * 获取SaaS访问所需的Token
 * 
 * @author liycq
 */
@RestController
public class SaaSTokenResource {

	private static Logger logger = LoggerFactory.getLogger(SaaSTokenResource.class);
	public static SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static CloseableHttpClient httpClient = null;
	public String modelType = null;
	public String returnUrl = null;

	/**
	 * 
	 * getSaaSToken
	 * 
	 * @Title: getSaaSToken
	 * @param @param
	 *            request
	 * @param @param
	 *            response
	 * @param @param
	 *            data
	 * @param @return
	 * @param @throws
	 *            Exception
	 * @return String
	 */
	@RequestMapping(value = "/getSaaSToken", produces = "application/json;charset=utf-8", method = { RequestMethod.POST,
			RequestMethod.GET })
	public String getSaaSToken(HttpServletRequest request, HttpServletResponse response, String data) throws Exception {

		if (data == null) {
			logger.info("/getSaaSToken data ==null");
			return constructReturnMessage(InnerConstants.RETURN_CODE_FAIL, "SaaS 获取Token失败", null);
		}
		logger.info("one request start==================================================");
		logger.info("enter SaaSTokenResource " + "data value: " + data.toString());

		JSONArray json = new JSONObject().parseArray(data);
		List<Map<String, Object>> orderList = JSONObject.toJavaObject(json, List.class);
		String appKey = null;
		String userId = null;
		String identify = null;
		String areacode = null;
		String username = null;
		String account = null;
		String resourceId = null;

		List<RequestSaaSRuleList> requestRuleLists = new ArrayList<RequestSaaSRuleList>();

		// 从业务上讲这个循环只循环一次
		for (Map<String, Object> singleOrder : orderList) {
			////// 初始化

			identify = (String) singleOrder.get("identify");
			modelType = getModelType(identify);
			appKey = (String) singleOrder.get("appKey");
			userId = (String) singleOrder.get("userId");
			areacode = (String) singleOrder.get("areacode");
			username = (String) singleOrder.get("name");
			account = (String) singleOrder.get("account");
			BasicDBObject serviceApiObject = new BasicDBObject().append("url", identify)
					.append("resourceType", InnerConstants.RESOURCETYPE_SAAS).append("areacode", areacode);
			// 查询路由表
			DBCursor serviceApiObjectdbCursor = MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_API)
					.find(serviceApiObject);
			// service_api中无信息
			if (serviceApiObjectdbCursor.size() == 0) {
				logger.info("appKey: " + appKey + " userId: " + userId + " identify: " + identify
						+ " not in service_api table");
				return constructReturnMessage(InnerConstants.RETURN_CODE_FAIL, "SaaS 无效路由", null);
			}

			// 根据查出来的路由,查询oder_rule_list表的订单, 仅循环一次
			while (serviceApiObjectdbCursor.hasNext()) {
				BasicDBObject ob = (BasicDBObject) serviceApiObjectdbCursor.next();
				resourceId = ob.getString("resourceId");
				//////////////////// 构造返回url
				String tempUrl = ob.getString("upstream");
				returnUrl = tempUrl.substring(7, tempUrl.length() - 1);
				String trueContext = ob.getString("trueContext");
				if (trueContext != null) {
					returnUrl = returnUrl + "/" + trueContext;
				}
				logger.info("returnUrl: " + returnUrl);
				///////////////////

				// endTime大于当前时间 startTime小于当前时间
				BasicDBObject tempOb = new BasicDBObject().append("resourceId", resourceId)
						.append("resourceType", InnerConstants.RESOURCETYPE_SAAS).append("areacode", areacode)
						.append("userId", userId)
						.append("endTime", new BasicDBObject(QueryOperators.GTE, sFormat.format(new Date())))
						.append("startTime", new BasicDBObject(QueryOperators.LTE, sFormat.format(new Date())));
				// 查询order_rule_list表,order_cursor为查询结果
				DBCursor orderCursor = MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).find(tempOb);
				if (orderCursor.size() < 1) {
					// 订单数量为0
					logger.info("appKey: " + appKey + " userId: " + userId + " identify: " + identify + " order zero");
					return constructReturnMessage(InnerConstants.RETURN_CODE_FAIL, "相关订单数量为0", null);
				}
				logger.info("order count: " + orderCursor.size());
				while (orderCursor.hasNext()) {
					// order_rule_list中查询结果
					BasicDBObject orderObject = (BasicDBObject) orderCursor.next();
					// 订单是否过期或已到上限
					if (orderObject.getString("number").equals(orderObject.getString("currentUsed")) // 总数和已调用的数相等
							|| new Date().after(sFormat.parse(orderObject.getString("endTime")))) {// 订单结束时间过期
						logger.info("order outofdate." + "userId: " + userId + " resourceId: "
								+ ob.getString("resourceId"));
						return constructReturnMessage(InnerConstants.RETURN_CODE_FAIL, "订单结束时间过期或已到上限", null);
					}

					String isFirst = orderObject.getString("isFirst");
					if (isFirst != null && !isFirst.equals("")) {
						// 非第一次
						logger.info("null jsonObject , not first");
						// requestRuleLists.add(constructRuleListObject(null));
					} else {// 第一次
						logger.info("first getSaaSToken");
						BasicDBObject updateObject = new BasicDBObject(orderObject.toMap());
						updateObject.append("isFirst", "false");
						// 更新数据库表，更新为第二次
						MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).update(orderObject, updateObject);
						// 构建请求jsonObject
						requestRuleLists.add(constructRequestRuleListObject(orderObject));
					}
				} // while (orderCursor.hasNext())

			} // while (serviceApiObjectdbCursor.hasNext())

		} // for (Map<String, Object> singleOrder : orderList)

		String requestObject = constructRequestSaaSObject(requestRuleLists, areacode, resourceId, userId, username,
				account);
		// String sdkResponse = requestSDK(requestObject);
		String sdkResponse = postSDK("http://219.143.213.112/saasSdk/account/getLoginCode", requestObject);
		String returnToken = writeOrderRuleListAndgetToken(requestRuleLists, sdkResponse, userId, resourceId);
		if (returnToken == null) {
			return constructReturnMessage(InnerConstants.RETURN_CODE_FAIL, getFailReturnMessage(sdkResponse), null);
		}
		return constructReturnMessage(InnerConstants.RETURN_CODE_SUCC, returnToken, returnUrl);
	}

	/**
	 * SDK端不返回Token时，根据resCode映射成对应message信息返回前端 
	 * @Description: TODO
	 * @param @return
	 * @return String
	 */
	private String getFailReturnMessage(String sdkResponse) {
		JSONObject jsonObject = new JSONObject().parseObject(sdkResponse);
		String resCode = jsonObject.getString("resCode");
		if (resCode.equals("999991")) {
			return "没有有效套餐";
		} else if (resCode.equals("999999")) {
			return "请求SDK失败";
		}
		return "未识别返回码";
	}

	/**
	 * @param identify
	 * @return
	 */
	public String getModelType(String identify) {
		String finalIdentify = identify.substring(1, identify.length());
		String returnValue = null;
		switch (finalIdentify) {
		case InnerConstants.CREDIT_INQUIRY:
			returnValue = "1";
			break;
		case InnerConstants.BUSINESS_PEDIGREE:
			returnValue = "2";
			break;
		case InnerConstants.FINANCIAL_DIAGNOSIS:
			returnValue = "3";
			break;
		case InnerConstants.BUSINESS_DIG:
			returnValue = "4";
			break;
		case InnerConstants.MONITOR:
			returnValue = "5";
			break;
		default:
			break;
		}
		return returnValue;
	}

	/**
	 * 根据SDK返回内容更新order_rule_list表并解析Token
	 * 
	 * @param sdkResponse
	 * @throws Exception
	 */
	public String writeOrderRuleListAndgetToken(List<RequestSaaSRuleList> requestRuleLists, String sdkResponse,
			String userId, String resourceId) throws Exception {
		String key = "ashjs8u^5&(^%$HJJHYUT$%^**%GHGHHUJI^**(^&GGY^&&as$#";
		JSONObject jsonObject = new JSONObject().parseObject(sdkResponse);
		String resCode = jsonObject.getString("resCode");
		String code = jsonObject.getString("code");
		if (!resCode.equals("000000")) {
			// 向SDK请求失败
			/*
			 * 根据<接口文档.doc>，如果resCode为999991，
			 * 则将向SDK发送请求的ruleList数组中的所有订单的isFirst字段均置为"" 从而保证下次对此订单请求时，继续向SDK同步
			 */
			// 注意orderDetailId在order_rule_list中为int类型，JsonString中为String类型

			if (resCode.equals("999991")) {
				for (int i = 0; i < requestRuleLists.size(); i++) {
					int orderDetailId = Integer.parseInt(requestRuleLists.get(i).getOrderDetailId());
					// 利用orderDetailId查询表
					BasicDBObject basicDBObject = new BasicDBObject().append("orderDetailId", orderDetailId);
					DBCursor dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).find(basicDBObject);
					if (dbCursor.size() != 1) {
						// orderDetailId非唯一的
						throw new Exception("order_rule_list orderDetailId exception, dbCursor.size(): "
								+ dbCursor.size() + " orderDetailId: " + orderDetailId);
					}
					while (dbCursor.hasNext()) {
						BasicDBObject updateDBObject = new BasicDBObject();
						updateDBObject.append("$set", new BasicDBObject("isFirst", ""));
						BasicDBObject originalDBObject = (BasicDBObject) dbCursor.next();
						MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).update(originalDBObject,
								updateDBObject);
						logger.info("orderDetailId: " + orderDetailId + " reset success");
					}
				} // for (int i = 0; i < requestRuleLists.size(); i++)
			}
			return null;
		}
		String rules = jsonObject.getString("rules");
		String account = jsonObject.getString("account");
		String username = jsonObject.getString("username");
		JSONArray json = new JSONObject().parseArray(rules);
		List<Map<String, Object>> orderList = JSONObject.toJavaObject(json, List.class);

		for (Map<String, Object> singleOrder : orderList) {

			////// 初始化
			// 根据SDK端返回值构建写入MongoDB实体
			BasicDBObject searchObject = new BasicDBObject();
			userId = (String) singleOrder.get("userId");
			resourceId = (String) singleOrder.get("resourceId");
			searchObject.append("userId", singleOrder.get("userId"))
					.append("orderDetailId", singleOrder.get("orderDetailId"))
					.append("resourceType", singleOrder.get("resourceType"))
					.append("areacode", singleOrder.get("areaCode"));
			BasicDBObject updateObject = new BasicDBObject();
			updateObject.append("areacode", singleOrder.get("areaCode"))
					.append("startTime", singleOrder.get("beginTime")).append("number", singleOrder.get("count"))
					.append("endTime", singleOrder.get("endTime"))
					.append("orderDetailId", singleOrder.get("orderDetailId"))
					.append("resourceId", singleOrder.get("resourceId"))
					.append("resourceType", singleOrder.get("resourceType"))
					.append("charRuleType", singleOrder.get("type")).append("currentUsed", singleOrder.get("used"))
					.append("userId", singleOrder.get("userId")).append("isFirst", "false");
			// 更新
			MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).update(searchObject, updateObject);
		}

		// 生成token
		Authcode authcode = new Authcode();

		Map<String, Object> temp = new HashMap<String, Object>();
		temp.put("userId", userId);
		temp.put("account", account);
		temp.put("code", code);
		temp.put("username", username);
		temp.put("resourceId", resourceId);
		temp.put("type", modelType);
		logger.info("userId:" + userId + "{{{account:" + account + "{{{code:" + code + "{{{username:" + username
				+ "{{{resourceId:" + resourceId + "{{{type:" + modelType);
		String token = null;
		String tokenTemp = null;
		try {
			token = authcode.encode(URLEncoder.encode(JSON.toJSONString(temp), "UTF-8"), key);
			tokenTemp = URLEncoder.encode(token, "UTF-8");
			logger.info("token=" + tokenTemp);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return tokenTemp;

	}

	/**
	 * 构建返回消息
	 * 
	 * @Title: constructReturnMessage
	 * @param @param
	 *            code
	 * @param @param
	 *            message
	 * @param @return
	 * @return String
	 */
	private String constructReturnMessage(String code, String message, String returnUrl) {
		Message msg = new Message();
		msg.setCode(code);
		msg.setMessage(message);
		if (returnUrl != null) {
			msg.setResult(returnUrl);
		}
		return new JSONObject().toJSONString(msg);
	}

	/**
	 * 向SDK端发送http请求
	 * 
	 * @param data
	 * @return
	 */
	public String requestSDK(String jsonString) {
		httpClient = HttpClients.createDefault();
		String resultBody = null;
		List<NameValuePair> params = new ArrayList();
		NameValuePair param = new BasicNameValuePair("data", jsonString);
		params.add(param);
		String str = null;
		CloseableHttpResponse httpResponse = null;
		try {
			HttpGet httpGet = new HttpGet("http://219.143.213.112/saasSdk/account/getLoginCode");
			str = EntityUtils.toString(new UrlEncodedFormEntity(params));
			httpGet.setURI(new URI(httpGet.getURI().toString() + "?" + str));
			// 发送请求
			httpResponse = httpClient.execute(httpGet);
			// 获取返回数据
			HttpEntity entity = httpResponse.getEntity();
			resultBody = EntityUtils.toString(entity);
			logger.info("sdk resultBody: " + resultBody);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (httpResponse != null) {
				try {
					httpResponse.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return resultBody;
	}

	/**
	 * 
	 * @param orderObject
	 * @return
	 */
	public RequestSaaSRuleList constructRequestRuleListObject(BasicDBObject orderObject) {
		int number = orderObject.getInt("number");
		if (number == -1) {
			number = Integer.MAX_VALUE;
		}
		RequestSaaSRuleList requestSaaSRuleList = new RequestSaaSRuleList();
		requestSaaSRuleList.setOrderDetailId(orderObject.getString("orderDetailId"));
		requestSaaSRuleList.setAreaCode(orderObject.getString("areacode"));
		requestSaaSRuleList.setBeginTime(orderObject.getString("startTime"));
		requestSaaSRuleList.setCount(number);
		requestSaaSRuleList.setEndTime(orderObject.getString("endTime"));
		requestSaaSRuleList.setResourceId(orderObject.getString("resourceId"));
		requestSaaSRuleList.setResourceType(orderObject.getString("resourceType"));
		requestSaaSRuleList.setType(orderObject.getString("charRuleType"));
		requestSaaSRuleList.setUserId(orderObject.getString("userId"));
		return requestSaaSRuleList;
	}

	/**
	 * 构建向SDK发送的消息实体
	 * 
	 * @param orderObject
	 * @return
	 */
	public String constructRequestSaaSObject(List<RequestSaaSRuleList> requestSaaSRuleLists, String areacode,
			String resourceId, String userId, String username, String account) {

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("areaCode", areacode);
		map.put("channel", "szsm");
		map.put("resourceId", resourceId);
		map.put("account", account);
		map.put("userId", userId);
		map.put("username", username);
		map.put("ruleList", requestSaaSRuleLists);
		String jsonString = JSON.toJSONString(map);
		logger.info("constructRequestSaaSObject: " + jsonString);
		return jsonString;
	}

	/**
	 * @param url
	 * @param json
	 * @return
	 */
	public String postSDK(String url, String json) {
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
			logger.info("resp.getStatusLine(): " + resp.getStatusLine().toString());
			char[] buf = new char[8192];
			int tempoff = 0;
			BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "utf-8"));
			while (-1 != (tempoff = reader.read(buf, 0, 1024))) {
				sb.append(buf, 0, tempoff);
			}
			// 失败情况：{"resCode":"999991","resMsg":"同步套餐数据失败"}
			// 成功情况：sdk return:
			// {"code":"f60c6a19bfd04393a72ae3cfdaba0ab1","resCode":"000000","rules":[],"account":"usersm","username":"岳子丰"}
			logger.info("sdk return: " + sb.toString());
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

}
