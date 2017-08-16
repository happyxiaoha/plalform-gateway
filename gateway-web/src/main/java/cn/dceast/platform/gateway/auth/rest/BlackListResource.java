package cn.dceast.platform.gateway.auth.rest;

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

@RestController
public class BlackListResource {

	private static Logger logger = LoggerFactory.getLogger(BlackListResource.class);
	
	@RequestMapping(value="/addBlackList", method={RequestMethod.POST, RequestMethod.GET})
	public String addblackList(HttpServletRequest request, HttpServletResponse response,String data) {
		
		Message msg = new Message();
		if(data == null){
			msg.setCode(InnerConstants.RETURN_CODE_FAIL);
			msg.setMessage("新增黑名单失败-参数为空");
			return new JSONObject().toJSONString(msg);
		}
		JSONArray jso = new JSONObject().parseArray(data);
		List<Map<String,Object>> orderList = JSONObject.toJavaObject(jso, List.class);
		
		BasicDBObject stats = new BasicDBObject();
		for(Map<String,Object> singleOrder : orderList){
			for(Map.Entry<String, Object> map : singleOrder.entrySet())
				stats.append(map.getKey(),map.getValue());
			MongoDBUtil.getColl(InnerConstants.COLL_ACCESS_CTRL_BLACKLIST).insert(stats);
			stats.clear();
		}
		msg.setCode(InnerConstants.RETURN_CODE_SUCC);
		msg.setMessage("新增黑名单成功");
		
		return new JSONObject().toJSONString(msg);
	}
	
	@RequestMapping(value="/updateBlackList", method={RequestMethod.POST, RequestMethod.GET})
	public String updateBlackList(HttpServletRequest request, HttpServletResponse response,String data) {
		
		Message msg = new Message();
		if(data == null){
			msg.setCode(InnerConstants.RETURN_CODE_FAIL);
			msg.setMessage("更新黑名单失败-参数为空");
			return new JSONObject().toJSONString(msg);
		}
		JSONArray jso = new JSONObject().parseArray(data);
		List<Map<String,Object>> orderList = JSONObject.toJavaObject(jso, List.class);
		
		BasicDBObject stats = new BasicDBObject();
		BasicDBObject update = new BasicDBObject();
		for(Map<String,Object> singleOrder : orderList){
			stats.append("createrName",singleOrder.get("createrName"))
				 .append("createrId", singleOrder.get("createrId"));
			for(Map.Entry<String, Object> map : singleOrder.entrySet()){
				update.append(map.getKey(),map.getValue());
			}
			MongoDBUtil.getColl(InnerConstants.COLL_ACCESS_CTRL_BLACKLIST).update(stats,new BasicDBObject("$set", update));
			stats.clear();
			update.clear();
		}
		msg.setCode(InnerConstants.RETURN_CODE_SUCC);
		msg.setMessage("更新黑名单成功");
		
		return new JSONObject().toJSONString(msg);
	}
	
	@RequestMapping(value="/removeBlackList", method={RequestMethod.POST, RequestMethod.GET})
	public String deleteBlackList(HttpServletRequest request, HttpServletResponse response,String data) {
		
		Message msg = new Message();
		if(data == null){
			msg.setCode(InnerConstants.RETURN_CODE_FAIL);
			msg.setMessage("删除黑名单失败-参数为空");
			return new JSONObject().toJSONString(msg);
		}
		JSONArray jso = new JSONObject().parseArray(data);
		List<Map<String,String>> orderList = JSONObject.toJavaObject(jso, List.class);
		
		BasicDBObject stats = new BasicDBObject();
		for(Map<String,String> singleOrder : orderList){
			stats.append("content",singleOrder.get("content"))
			      .append("type", singleOrder.get("type"));
			MongoDBUtil.getColl(InnerConstants.COLL_ACCESS_CTRL_BLACKLIST).remove(stats);
			stats.clear();
		}
		msg.setCode(InnerConstants.RETURN_CODE_SUCC);
		msg.setMessage("删除黑名单成功");
		
		return new JSONObject().toJSONString(msg);
	}
	
}
