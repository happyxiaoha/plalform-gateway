package cn.dceast.platform.gateway.auth.executor.task;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cn.dceast.platform.gateway.auth.AppConfig;
import cn.dceast.platform.gateway.auth.util.ApplicationUtil;
import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;

import com.alibaba.fastjson.JSONObject;
import com.digitalchina.resttemplate.ribbon.retryable.RetryableLoadbalancedRestTemplateUtil;
import com.mongodb.BasicDBObject;

@Component
@Configurable
@EnableScheduling
public class ForbiddenPartnerId {
	
	private static Logger logger = LoggerFactory.getLogger(ForbiddenPartnerId.class);
	
	@Autowired
	@Qualifier("retryableLoadbalancedRestTemplateUtil")
	RetryableLoadbalancedRestTemplateUtil restTemplate;
	
	/*
	 * 3.9.	查询禁用合作伙伴id列表
	 */
	@Scheduled(cron = "00 00 00 * * ?")
	public void execute() {
		AppConfig appConfig = ((AppConfig)ApplicationUtil.getSingleton().getContext().getBean("appConfig"));
		String result = null;
		try{
			result=restTemplate.get(appConfig.getDataCenterEurekAaddress(), "/user/statics/query/disable/partner/ids.do",null);
		}catch(Exception e){
			e.printStackTrace();
			logger.debug("================定时查询合作伙伴id列表异常 ："+e.getMessage()+"==============");
		}
		JSONObject jso = new JSONObject().parseObject(result);
		Map<String,String> returnMap = JSONObject.toJavaObject(jso, Map.class);
		if(InnerConstants.RETURN_CODE_SUCC.equals(returnMap.get("code"))){
			JSONObject jso1 = new JSONObject().parseObject(String.valueOf(returnMap.get("result")));
			String[] userArray = JSONObject.toJavaObject(jso1, String[].class);
			BasicDBObject stats = new BasicDBObject();
			for(String userId : userArray){
				stats.append("userId", userId);
				MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_KEY).remove(stats);
				if(logger.isErrorEnabled())
					logger.debug("查询禁用合作伙伴的id ["+userId+"]");
			}
		}
	}}
