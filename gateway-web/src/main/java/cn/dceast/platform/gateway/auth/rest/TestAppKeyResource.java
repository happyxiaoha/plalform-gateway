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
import com.mongodb.QueryOperators;


@RestController
public class TestAppKeyResource {
	
	private static Logger logger = LoggerFactory.getLogger(TestAppKeyResource.class);
	
	DateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@RequestMapping(value="/addTestData", method={RequestMethod.POST, RequestMethod.GET})
	public String addWhiteList(HttpServletRequest request, HttpServletResponse response,
		 String data) {
		
		Message msg = new Message();
		if(data == null){
			msg.setCode(InnerConstants.RETURN_CODE_FAIL);
			msg.setMessage("/addTestData fail data null");
			msg.setStatus("ERROR");
			return new JSONObject().toJSONString(msg);
		}
		logger.info("enter /addTestData, value = "+data);
		JSONArray jso = new JSONObject().parseArray(data);
		List<Map<String,Object>> orderList = JSONObject.toJavaObject(jso, List.class);
		
		BasicDBObject stats = new BasicDBObject();
		for(Map<String,Object> singleOrder : orderList){
			stats.clear();
			for(Map.Entry<String, Object> map : singleOrder.entrySet())
				stats.append(map.getKey(),map.getValue());
			MongoDBUtil.getColl(InnerConstants.COLL_TESTMANAGER).insert(stats);
			stats.clear();
		}
		msg.setCode(InnerConstants.RETURN_CODE_SUCC);
		msg.setMessage("/addTestData Success");
		msg.setStatus("OK");
		return new JSONObject().toJSONString(msg);
	}
	
	@RequestMapping(value="/updateTestData", method={RequestMethod.POST, RequestMethod.GET})
	public String updateWhiteList(HttpServletRequest request, HttpServletResponse response,
			String data) {
		
		Message msg = new Message();
		if(data == null){
			msg.setCode(InnerConstants.RETURN_CODE_FAIL);
			msg.setMessage("/updateTestData fail data null");
			msg.setStatus("ERROR");
			return new JSONObject().toJSONString(msg);
		}
		logger.info("enter /updateTestData, value = "+data);
		JSONArray jso = new JSONObject().parseArray(data);
		List<Map<String,Object>> orderList = JSONObject.toJavaObject(jso, List.class);
		
		BasicDBObject stats = new BasicDBObject();
		BasicDBObject update = new BasicDBObject();
		for(Map<String,Object> singleOrder : orderList){
			stats.append("ip",singleOrder.get("ip"))
				 .append("areacode", singleOrder.get("areacode"));
			BasicDBObject dbObject = (BasicDBObject) MongoDBUtil.getColl(InnerConstants.COLL_TESTMANAGER).findOne(stats);
			if(dbObject!=null){
				String oldAppKey = dbObject.getString("appKey");
				String oldAppKeySeq = dbObject.getString("oldAppKey");
				String tempSeq = "";
				if(oldAppKeySeq==null){
					tempSeq = oldAppKey;
				}else{
					tempSeq = oldAppKey + ";" + oldAppKeySeq;
				}
				update.append("appKey",singleOrder.get("appKey")).append("oldAppKey", tempSeq);
				MongoDBUtil.getColl(InnerConstants.COLL_TESTMANAGER).update(stats,new BasicDBObject("$set", update));
			}else{
				msg.setCode(InnerConstants.RETURN_CODE_FAIL);
				msg.setMessage("/updateTestData target Object not exist");
				msg.setStatus("OK");
				return new JSONObject().toJSONString(msg);
			}
			stats.clear();
			update.clear();
		}
		msg.setCode(InnerConstants.RETURN_CODE_SUCC);
		msg.setMessage("/updateTestData sucess");
		msg.setStatus("OK");
		return new JSONObject().toJSONString(msg);
	}

	/*
	 * 统计测试调用次数
	 */
	@RequestMapping(value="/getTestSuccInvokeCount", method={RequestMethod.POST, RequestMethod.GET})
	public String getBlackListInvokeCount(HttpServletRequest request, HttpServletResponse response,
			String data) {
		
		List<Map> returnList = new ArrayList();
		Message msg = new Message();
		if(data == null){
			msg.setCode(InnerConstants.RETURN_CODE_FAIL);
			msg.setMessage("/getTestSuccInvokeCount data null");
			msg.setStatus("ERROR");
			return new JSONObject().toJSONString(msg);
		}
		logger.info("enter /getTestSuccInvokeCount, value = "+data);
		JSONArray jso = new JSONObject().parseArray(data);
		List<Map<String,String>> orderList = JSONObject.toJavaObject(jso, List.class);
		
		BasicDBObject query = new BasicDBObject();
		for(Map<String,String> resMap : orderList){
			
			/*
			 * 查询appKey的合法性
			 * 
			 */
			long total = 0;
			query.clear();
			query.append("areacode", resMap.get("areacode"))
				 .append("appKey", resMap.get("appKey"));
			BasicDBObject basicDbObject = (BasicDBObject) MongoDBUtil.getColl(InnerConstants.COLL_TESTMANAGER).findOne(query);
			if(basicDbObject != null){
				try {
					String ips = basicDbObject.getString("ip");
					List<String> ipList = Arrays.asList(ips.split(";"));
					if(ipList.contains(resMap.get("ip"))){
						query.append("callerIP", resMap.get("ip"))
							 .append("resourceType", resMap.get("resourceType"))
							 .append("uri", resMap.get("uri"))
							 .append("code", "200");
						if(resMap.get("timePoint")!=null)
							query.append("callTime", new BasicDBObject(QueryOperators.GTE,sFormat.parse(resMap.get("timePoint"))));
						total = MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_CALL_LOG).count(query);
						/*
						 * 拆分多个appKey查询
						 */
						String oldAppKey = basicDbObject.getString("oldAppKey");
						if(oldAppKey!=null){
							List<String> oldAppKeyList = Arrays.asList(oldAppKey.split(";"));
							for(String tempAppKey : oldAppKeyList){
								query.append("appKey", tempAppKey);
								total += MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_CALL_LOG).count(query);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			Map<String,Object> map = new HashMap();
			map.put("resourceType", resMap.get("resourceType"));
			map.put("appKey", resMap.get("appKey"));
			map.put("areacode", resMap.get("areacode"));
			map.put("uri", resMap.get("uri"));
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
}
