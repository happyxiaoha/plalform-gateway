package cn.dceast.platform.gateway.auth.executor.task;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;

import com.alibaba.fastjson.JSONObject;
import com.digitalchina.resttemplate.ribbon.retryable.RetryableLoadbalancedRestTemplateUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

public class UserInfoService implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(UserInfoService.class);

	private RetryableLoadbalancedRestTemplateUtil restTemplate;

	private String eurekaAddress;

	private String userId;

	public UserInfoService(RetryableLoadbalancedRestTemplateUtil restTemplate, String eurekaAddress, String userId) {
		this.restTemplate = restTemplate;
		this.eurekaAddress = eurekaAddress;
		this.userId = userId;
	}

	@Override
	public void run() {
		getUserInfo(restTemplate, eurekaAddress, userId);
	}

	private void getUserInfo(RetryableLoadbalancedRestTemplateUtil restTemplate, String eurekaAddress, String userId) {
		BasicDBObject stats = new BasicDBObject("userId", userId);
		DBCursor dBCursor = MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_KEY).find(stats);
		if (dBCursor.count() == 0) {
			String result = restTemplate.get(eurekaAddress, "/user/info/api/key.do?userId=" + userId, null);
			JSONObject jso = new JSONObject().parseObject(result);
			Map<String, String> returnMap = JSONObject.toJavaObject(jso, Map.class);
			if (InnerConstants.RETURN_CODE_SUCC.equals(returnMap.get("code"))) {
				JSONObject jso1 = new JSONObject().parseObject(String.valueOf(returnMap.get("result")));
				Map<String, String> userMap = JSONObject.toJavaObject(jso1, Map.class);
				for (Map.Entry<String, String> uMap : userMap.entrySet())
					stats.append(uMap.getKey(), uMap.getValue());
				MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_KEY).insert(stats);
				if (logger.isErrorEnabled())
					logger.debug("获取用户信息: [" + userId + "]");
			}
		}
	}

	/**
	 * 调用<网关服务接口文档V1.3.doc>中的3.8获取用户信息接口，根据apiKey查询userName
	 */
	public String getuserName(String userId) {
		String result = restTemplate.get(eurekaAddress, "/user/info/api/key.do?userId=" + userId, null);
		JSONObject jso = new JSONObject().parseObject(result);
		Map<String, String> returnMap = JSONObject.toJavaObject(jso, Map.class);
		if (InnerConstants.RETURN_CODE_SUCC.equals(returnMap.get("code"))) {
			JSONObject jso1 = new JSONObject().parseObject(String.valueOf(returnMap.get("result")));
			Map<String, String> userMap = JSONObject.toJavaObject(jso1, Map.class);
			String userName = userMap.get("userName").toString();
			logger.debug("getuserName: [" + userName + "]");
			return userName;
		}
		return null;
	};
}
