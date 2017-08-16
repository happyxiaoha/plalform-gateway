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
public class WhiteListResource {

	private static Logger logger = LoggerFactory.getLogger(WhiteListResource.class);
	
	@RequestMapping(value="/addWhiteList", method={RequestMethod.POST, RequestMethod.GET})
	public String addWhiteList(HttpServletRequest request, HttpServletResponse response,
		 String data) {
		
		Message msg = new Message();
		if(data == null){
			msg.setCode(InnerConstants.RETURN_CODE_FAIL);
			msg.setMessage("新增失败-参数为空");
			msg.setStatus("ERROR");
			return new JSONObject().toJSONString(msg);
		}
		JSONArray jso = new JSONObject().parseArray(data);
		List<Map<String,Object>> orderList = JSONObject.toJavaObject(jso, List.class);
		
		BasicDBObject stats = new BasicDBObject();
		for(Map<String,Object> singleOrder : orderList){
			for(Map.Entry<String, Object> map : singleOrder.entrySet())
				stats.append(map.getKey(),map.getValue());
			MongoDBUtil.getColl(InnerConstants.COLL_ACCESS_CTRL_WHITELIST).insert(stats);
			stats.clear();
		}
		msg.setCode(InnerConstants.RETURN_CODE_SUCC);
		msg.setMessage("新增成功");
		msg.setStatus("OK");
		return new JSONObject().toJSONString(msg);
	}
	
	@RequestMapping(value="/updateWhiteList", method={RequestMethod.POST, RequestMethod.GET})
	public String updateWhiteList(HttpServletRequest request, HttpServletResponse response,
			String data) {
		
		Message msg = new Message();
		if(data == null){
			msg.setCode(InnerConstants.RETURN_CODE_FAIL);
			msg.setMessage("更新白名单失败-参数为空");
			msg.setStatus("ERROR");
			return new JSONObject().toJSONString(msg);
		}
		JSONArray jso = new JSONObject().parseArray(data);
		List<Map<String,Object>> orderList = JSONObject.toJavaObject(jso, List.class);
		
		BasicDBObject stats = new BasicDBObject();
		BasicDBObject update = new BasicDBObject();
		for(Map<String,Object> singleOrder : orderList){
			stats.append("ip",singleOrder.get("ip"))
				 .append("apiUri", singleOrder.get("apiUri"));
			for(Map.Entry<String, Object> map : singleOrder.entrySet()){
				update.append(map.getKey(),map.getValue());
			}
			MongoDBUtil.getColl(InnerConstants.COLL_ACCESS_CTRL_WHITELIST).update(stats,new BasicDBObject("$set", update));
			stats.clear();
			update.clear();
		}
		msg.setCode(InnerConstants.RETURN_CODE_SUCC);
		msg.setMessage("更新白名单成功");
		msg.setStatus("OK");
		
		return new JSONObject().toJSONString(msg);
	}
	
	@RequestMapping(value="/removeWhiteList", method={RequestMethod.POST, RequestMethod.GET})
	public String deleteWhiteList(HttpServletRequest request, HttpServletResponse response,
			String data) {
		
		Message msg = new Message();
		if(data == null){
			msg.setCode(InnerConstants.RETURN_CODE_FAIL);
			msg.setMessage("删除白名单失败-参数为空");
			msg.setStatus("ERROR");
			return new JSONObject().toJSONString(msg);
		}
		JSONArray jso = new JSONObject().parseArray(data);
		List<Map<String,String>> orderList = JSONObject.toJavaObject(jso, List.class);
		
		BasicDBObject stats = new BasicDBObject();
		for(Map<String,String> singleOrder : orderList){
			stats.append("ip",singleOrder.get("ip"))
			      .append("apiUri", singleOrder.get("apiUri"))
				  .append("appName", singleOrder.get("appName"));
			MongoDBUtil.getColl(InnerConstants.COLL_ACCESS_CTRL_WHITELIST).remove(stats);
			stats.clear();
		}
		msg.setCode(InnerConstants.RETURN_CODE_SUCC);
		msg.setMessage("删除白名单成功");
		msg.setStatus("OK");
		
		return new JSONObject().toJSONString(msg);
	}
	
	
}
