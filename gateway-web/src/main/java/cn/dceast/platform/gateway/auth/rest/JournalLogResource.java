package cn.dceast.platform.gateway.auth.rest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import cn.dceast.platform.gateway.auth.data.entity.Message;
import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.QueryOperators;

@RestController
public class JournalLogResource {

	private static Logger logger = LoggerFactory.getLogger(JournalLogResource.class);
	
	DateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/*
	 * 3.7 获取资源使用信息
	 * {
	   "result" : {
	      "list" : [
	         {
	            " operationType" : "2-调用",
	            " resourceId" : "000001000002330",
	            " resourceType" : "01",
	            " userId" : "zyptest9",
	            "callTime" : "2015-01-01 12:12:12",
	            "userName" : "xuchaoj"
	         }
	      ],
	      "resTime" : "xxx"
   }
}
	 */
	@RequestMapping(value="/getLog", method={RequestMethod.POST, RequestMethod.GET})
	public String getLog(HttpServletRequest request, HttpServletResponse response,String data) {
		
		List returnList = new ArrayList();
		Date now = new Date();
		if(data == null){
			/*
			 * 3.7 获取资源使用信息 
			 * 1. timePoint 非必填，如果没有传入这个参数，返回<=当前系统时间的所有记录
			 */
			BasicDBObject query = new BasicDBObject();
			query.append("callTime",new BasicDBObject(QueryOperators.LTE, now));
			
			DBCursor dbCursor = null;
			try {
				dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_CALL_LOG).find(query).limit(50);
				query.clear();
				while(dbCursor.hasNext()){
					Map<String,String> map = new HashMap();
					BasicDBObject stats = (BasicDBObject)dbCursor.next();
					map.put("resourceId", stats.getString("resourceId"));
					map.put("resourceType", stats.getString("resourceType"));
					map.put("activityType", "2");
					map.put("userId", stats.getString("userId"));
					map.put("userName", stats.getString("callerName"));
					map.put("activityTime", stats.getDate("callTime")==null?null:sFormat.format(stats.getDate("callTime")));
					map.put("areacode", stats.getString("areacode"));
					returnList.add(map);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			
			/*
			 * 2. 如果传入了这个参数，返回timePoint < callTime <= 当前系统时间的所有记录
			 */
			JSONArray jso = new JSONObject().parseArray(data);
			List<Map<String,Object>> orderList = JSONObject.toJavaObject(jso, List.class);
			
			BasicDBObject query = new BasicDBObject();
			for(Map<String,Object> orderMap : orderList){
				query.clear();
				query.append("areacode", orderMap.get("areacode"));
				if(orderMap.get("timePoint")==null || "".equals(orderMap.get("timePoint")==null)){
					query.append("callTime",new BasicDBObject(QueryOperators.LTE,now));
				}else{
					try {
						if(sFormat.parse(String.valueOf(orderMap.get("timePoint"))).after(now)){
							Message msg = new Message();
							msg.setCode(InnerConstants.RETURN_CODE_FAIL);
							msg.setMessage("timePoint cannot older than now!");
							msg.setStatus("ERROR");
							return new JSONObject().toJSONString(msg);
						}
						query.put("callTime", new BasicDBObject(QueryOperators.GT,sFormat.parse(String.valueOf(orderMap.get("timePoint")))).append(QueryOperators.LTE,now));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				DBCursor dbCursor = null;
				try {
					dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_CALL_LOG).find(query).limit(orderMap.get("count")!=null?Integer.valueOf(String.valueOf(orderMap.get("count"))):50);
					while(dbCursor.hasNext()){
						Map<String,String> map = new HashMap();
						BasicDBObject stats = (BasicDBObject)dbCursor.next();
						map.put("resourceId", stats.getString("resourceId"));
						map.put("resourceType", stats.getString("resourceType"));
						map.put("activityType", "2");
						map.put("userId", stats.getString("userId"));
						map.put("userName", stats.getString("callerName"));
						map.put("activityTime", stats.getDate("callTime")==null?null:sFormat.format(stats.getDate("callTime")));
						map.put("areacode", stats.getString("areacode"));
						returnList.add(map);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		Map tempMap = new HashMap();
		tempMap.put("list", returnList);
		tempMap.put("resTime", sFormat.format(now));
		Map returnMap = new HashMap();
		returnMap.put("result", tempMap);
		returnMap.put("message", "success");
		returnMap.put("code", InnerConstants.RETURN_CODE_SUCC);
		returnMap.put("status", "OK");
		return new JSONObject().toJSONString(returnMap);
	}
}
