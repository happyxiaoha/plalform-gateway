package cn.dceast.platform.gateway.auth.util;

import cn.dceast.platform.common.BusinessException;
import cn.dceast.platform.common.mongo.DBUtil;
import cn.dceast.platform.gateway.auth.AppConfig;

import com.mongodb.DBCollection;

public class MongoDBUtil {
	private static DBUtil dbUtil;
	
	public static void init(AppConfig appConfig){
		if(dbUtil!=null){
			return;
		}
		
		dbUtil=new DBUtil();
		dbUtil.setHost(appConfig.mongoHost);
		dbUtil.setDbName(appConfig.mongodbName);
		dbUtil.setUsername(appConfig.mongoUserName);
		dbUtil.setPassword(appConfig.mongoPassword);
		dbUtil.setMinConnectionsPerHost(appConfig.mongoMinConnectionsPerHost);
		dbUtil.setConnectionsPerHost(appConfig.mongoConnectionsPerHost);
		dbUtil.setConnectTimeout(appConfig.mongoConnectTimeout);
		dbUtil.setMaxConnectionIdleTime(appConfig.mongoMaxConnectionIdleTime);
		dbUtil.setMaxWaitTime(appConfig.mongoMaxWaitTime);
		dbUtil.setThreadsAllowedToBlockForConnectionMultiplier(appConfig.mongoThreadsAllowedToBlockForConnectionMultiplier);

		try{
		dbUtil.init();
		}catch(Exception e){
			throw new BusinessException("init mongodb fail! detail:"+e.getMessage());
		}
	}
	
	public static DBCollection getColl(String name) {
		return dbUtil.getColl(name);
	}

	public static int nextValue(String counterName) {
		return dbUtil.nextValue(counterName);
	}
	
	public static void destroy(){
		if(dbUtil!=null){
			dbUtil.destroy();
		}
	}
}
