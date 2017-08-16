package cn.dceast.platform.gateway.auth.listener;

import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import cn.dceast.platform.common.properties.Prop;
import cn.dceast.platform.common.properties.PropKit;
import cn.dceast.platform.gateway.auth.AppConfig;
import cn.dceast.platform.gateway.auth.filter.FilterChain;
import cn.dceast.platform.gateway.auth.filter.FilterDetectTask;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;

@Component
@Order(value = 1)
public class AuthContextListener implements CommandLineRunner {

	private Logger logger = LoggerFactory.getLogger(AuthContextListener.class);

	private static final String CONFIG_FILE = "application.properties";

	@Autowired
	AppConfig appConfig;
	
	@PreDestroy
	public void destroy() {
		logger.info("Destroy mongodb!");
		MongoDBUtil.destroy();
	}

	@Override
	public void run(String... args) throws Exception {
//		initConfig();

		MongoDBUtil.init(appConfig);
		FilterChain.init();

//		Executors.newCachedThreadPool().execute(new FilterDetectTask());
	}

//	private void initConfig() {
//		logger.info("init config!");
//
//		Prop pro = PropKit.use(CONFIG_FILE);
//
//		// appConfig.gateWayFilters=pro.get("gateway.black.filters");
//
//		/**
//		 * mongo 相关配置
//		 */
//		appConfig.mongoHost = pro.get("mongodb.oss.host");
//		logger.info("mongodb.oss.host=="+appConfig.mongoHost);
//		appConfig.mongodbName = pro.get("mongodb.oss.dbName");
//		logger.info("mongodb.oss.host=="+appConfig.mongodbName);
//		appConfig.mongoUserName = pro.get("mongodb.oss.userName");
//		logger.info("mongodb.oss.host=="+appConfig.mongoUserName);
//		appConfig.mongoPassword = pro.get("mongodb.oss.password");
//		logger.info("mongodb.oss.host=="+appConfig.mongoPassword);
//		appConfig.mongoMinConnectionsPerHost = Integer.valueOf(pro.get(
//				"mongodb.oss.minConnectionsPerHost").trim());
//		appConfig.mongoConnectionsPerHost = Integer.valueOf(pro.get(
//				"mongodb.oss.connectionsPerHost").trim());
//		appConfig.mongoMaxWaitTime = Integer.valueOf(pro.get(
//				"mongodb.oss.maxWaitTime").trim());
//		appConfig.mongoMaxConnectionIdleTime = Integer.valueOf(pro.get(
//				"mongodb.oss.maxConnectionIdleTime").trim());
//		appConfig.mongoConnectTimeout = Integer.valueOf(pro.get(
//				"mongodb.oss.connectTimeout").trim());
//		appConfig.mongoThreadsAllowedToBlockForConnectionMultiplier = Integer
//				.valueOf(pro
//						.get("mongodb.oss.threadsAllowedToBlockForConnectionMultiplier")
//						.trim());
//
////		appConfig.cronExpression = pro.get("scheduler.cronExpression");
////		appConfig.defaultPackageOfFilter = pro
////				.get("gateway.black.filters.package.default");
//
////		appConfig.notAuthFilters = pro.get("gateway.notAuth.filters");
//
//		/**
//		 * 调用信息记录执行器相关配置
//		 */
//		appConfig.callerRequestRecordExecutorCoreCount = Integer.valueOf(pro
//				.get("caller.request.record.core.count").trim());
//		appConfig.callerRequestRecordExecutorMaxCount = Integer.valueOf(pro
//				.get("caller.request.record.max.count").trim());
//		appConfig.callerRequestRecordExecutorQueueSize = Integer.valueOf(pro
//				.get("caller.request.record.queue.size").trim());
//		appConfig.callerRequestRecordExecutorAllowCoreThreadTimeOut = Boolean
//				.valueOf(pro.get(
//						"caller.request.record.allow.core.thread.time.out")
//						.trim());
//		appConfig.callerRequestRecordExecutorThreadTimeOutSeconds = Long
//				.valueOf(pro.get("caller.request.record.thread.time.out")
//						.trim());
//		appConfig.callerRequestRecordExecutorShuttingDownCoreCount = Integer
//				.valueOf(pro.get(
//						"caller.request.record.shutting.down.core.count")
//						.trim());
//		
//		appConfig.apiDailyMaxLimit = Integer.valueOf(pro.get("api_daily_max_limit").trim());
//		appConfig.dataCenterEurekAaddress = pro.get("datacenter.eureka.address").trim();
//		
//	}

}
