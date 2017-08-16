package cn.dceast.platform.gateway.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
	
	
	@Value("${redis.host}")
	public String redisHost;
	
	public String getRedisHost() {
		return redisHost;
	}

	public void setRedisHost(String redisHost) {
		this.redisHost = redisHost;
	}
	@Value("${cache.cacheRequestUrl}")
	public String cacheRequestUrl;

	/**
	 * 网关过滤器
	 */
//	public static String gateWayFilters;
	
	public String getCacheRequestUrl() {
		return cacheRequestUrl;
	}

	public void setCacheRequestUrl(String cacheRequestUrl) {
		this.cacheRequestUrl = cacheRequestUrl;
	}
	/**
	 * mongodb host
	 */
	@Value("${mongodb.oss.host}")
	public String mongoHost;
	
	/**
	 * mongo dbname
	 */
	@Value("${mongodb.oss.dbName}")
	public String mongodbName;
	
	/**
	 * mongo userName
	 */
	@Value("${mongodb.oss.userName}")
	public String mongoUserName;
	
	/**
	 * mongo password
	 */
	@Value("${mongodb.oss.password}")
	public String mongoPassword;

	/**
	 * 最小连接数
	 */
	@Value("${mongodb.oss.minConnectionsPerHost}")
	public int mongoMinConnectionsPerHost;
	/**
	 * 最大连接数
	 */
	@Value("${mongodb.oss.connectionsPerHost}")
	public int mongoConnectionsPerHost;
	/**
	 * 线程最大等待可用连接时间（2分钟）
	 */
	@Value("${mongodb.oss.maxWaitTime}")
	public int mongoMaxWaitTime;
	/**
	 * 连接最大空闲时间
	 */
	@Value("${mongodb.oss.maxConnectionIdleTime}")
	public int mongoMaxConnectionIdleTime;
	/**
	 * 连接超时时间
	 */
	@Value("${mongodb.oss.connectTimeout}")
	public int mongoConnectTimeout;
	/**
	 * 最大阻塞线程连接倍数（乘数）
	 */
	@Value("${mongodb.oss.threadsAllowedToBlockForConnectionMultiplier}")
	public int mongoThreadsAllowedToBlockForConnectionMultiplier = 5;
	
	/**
	 * 定时任务时间表达式
	 */
//	public static String cronExpression;

	/**
	 * 过滤器默认查找包
	 */
//	public static String defaultPackageOfFilter;
	
	/**
	 * 无需鉴权过滤器
	 */
//	public static String notAuthFilters;

	/**
	 * 调用信息记录执行器核心线程数
	 */
	@Value("${caller.request.record.core.count}")
	public Integer callerRequestRecordExecutorCoreCount;
	/**
	 * 调用信息记录执行器最大线程数
	 */
	@Value("${caller.request.record.max.count}")
	public Integer callerRequestRecordExecutorMaxCount;
	/**
	 * 调用信息记录执行器等待队列大小
	 */
	@Value("${caller.request.record.queue.size}")
	public Integer callerRequestRecordExecutorQueueSize;
	/**
	 * 调用信息记录执行器是否允许回收核心线程
	 */
	@Value("${caller.request.record.allow.core.thread.time.out}")
	public Boolean callerRequestRecordExecutorAllowCoreThreadTimeOut;
	/**
	 * 调用信息记录执行器线程最大空闲时间
	 */
	@Value("${caller.request.record.thread.time.out}")
	public Long callerRequestRecordExecutorThreadTimeOutSeconds;
	/**
	 * 调用信息记录执行器服务关闭时核心线程数
	 */
	@Value("${caller.request.record.shutting.down.core.count}")
	public Integer callerRequestRecordExecutorShuttingDownCoreCount;
	/**
	 * api日最大限额
	 */
	@Value("${api_daily_max_limit}")
	public Integer apiDailyMaxLimit;
	/**
	 * 数据中心eureka地址
	 */
	@Value("${datacenter.eureka.address}")
	public String dataCenterEurekAaddress;
	/**
	 * 数据2级域名地址
	 */
	@Value("${data.secondDomain}")
	public String dataSecondDomain;
	/**
	 * api2级域名地址
	 */
	@Value("${api.secondDomain}")
	public String apiSecondDomain;
	/**
	 * 微服务2级域名地址
	 */
	@Value("${service.secondDomain}")
	public String serviceSecondDomain;
	/**
	 * 数据中心eureka地址
	 */
	@Value("${service.domain}")
	public String serviceDomain;
	/**
	 * 微服务token的有效时间(毫秒)
	 */
	@Value("${token.timeout}")
	public String tokenTimeout;
	/**
	 * 微服务ticket的有效时间(毫秒)
	 */
	@Value("${ticket.timeout}")
	public String ticketTimeout;
	
	
	/**
	 * 跳过鉴权的文件类型
	 */
	@Value("${auth.skipAuth}")
	public String skipAuth;
	
	public String getSkipAuth(){
		return skipAuth;
	}
	
	public void setSkipAuth(String skipAuth){
		this.skipAuth =skipAuth;
	}
	
	
	
	public String getMongoHost() {
		return mongoHost;
	}
	public void setMongoHost(String mongoHost) {
		this.mongoHost = mongoHost;
	}
	public String getMongodbName() {
		return mongodbName;
	}
	public void setMongodbName(String mongodbName) {
		this.mongodbName = mongodbName;
	}
	public String getMongoUserName() {
		return mongoUserName;
	}
	public void setMongoUserName(String mongoUserName) {
		this.mongoUserName = mongoUserName;
	}
	public String getMongoPassword() {
		return mongoPassword;
	}
	public void setMongoPassword(String mongoPassword) {
		this.mongoPassword = mongoPassword;
	}
	public int getMongoMinConnectionsPerHost() {
		return mongoMinConnectionsPerHost;
	}
	public void setMongoMinConnectionsPerHost(int mongoMinConnectionsPerHost) {
		this.mongoMinConnectionsPerHost = mongoMinConnectionsPerHost;
	}
	public int getMongoConnectionsPerHost() {
		return mongoConnectionsPerHost;
	}
	public void setMongoConnectionsPerHost(int mongoConnectionsPerHost) {
		this.mongoConnectionsPerHost = mongoConnectionsPerHost;
	}
	public int getMongoMaxWaitTime() {
		return mongoMaxWaitTime;
	}
	public void setMongoMaxWaitTime(int mongoMaxWaitTime) {
		this.mongoMaxWaitTime = mongoMaxWaitTime;
	}
	public int getMongoMaxConnectionIdleTime() {
		return mongoMaxConnectionIdleTime;
	}
	public void setMongoMaxConnectionIdleTime(int mongoMaxConnectionIdleTime) {
		this.mongoMaxConnectionIdleTime = mongoMaxConnectionIdleTime;
	}
	public int getMongoConnectTimeout() {
		return mongoConnectTimeout;
	}
	public void setMongoConnectTimeout(int mongoConnectTimeout) {
		this.mongoConnectTimeout = mongoConnectTimeout;
	}
	public int getMongoThreadsAllowedToBlockForConnectionMultiplier() {
		return mongoThreadsAllowedToBlockForConnectionMultiplier;
	}
	public void setMongoThreadsAllowedToBlockForConnectionMultiplier(
			int mongoThreadsAllowedToBlockForConnectionMultiplier) {
		this.mongoThreadsAllowedToBlockForConnectionMultiplier = mongoThreadsAllowedToBlockForConnectionMultiplier;
	}
	public Integer getCallerRequestRecordExecutorCoreCount() {
		return callerRequestRecordExecutorCoreCount;
	}
	public void setCallerRequestRecordExecutorCoreCount(
			Integer callerRequestRecordExecutorCoreCount) {
		this.callerRequestRecordExecutorCoreCount = callerRequestRecordExecutorCoreCount;
	}
	public Integer getCallerRequestRecordExecutorMaxCount() {
		return callerRequestRecordExecutorMaxCount;
	}
	public void setCallerRequestRecordExecutorMaxCount(
			Integer callerRequestRecordExecutorMaxCount) {
		this.callerRequestRecordExecutorMaxCount = callerRequestRecordExecutorMaxCount;
	}
	public Integer getCallerRequestRecordExecutorQueueSize() {
		return callerRequestRecordExecutorQueueSize;
	}
	public void setCallerRequestRecordExecutorQueueSize(
			Integer callerRequestRecordExecutorQueueSize) {
		this.callerRequestRecordExecutorQueueSize = callerRequestRecordExecutorQueueSize;
	}
	public Boolean getCallerRequestRecordExecutorAllowCoreThreadTimeOut() {
		return callerRequestRecordExecutorAllowCoreThreadTimeOut;
	}
	public void setCallerRequestRecordExecutorAllowCoreThreadTimeOut(
			Boolean callerRequestRecordExecutorAllowCoreThreadTimeOut) {
		this.callerRequestRecordExecutorAllowCoreThreadTimeOut = callerRequestRecordExecutorAllowCoreThreadTimeOut;
	}
	public Long getCallerRequestRecordExecutorThreadTimeOutSeconds() {
		return callerRequestRecordExecutorThreadTimeOutSeconds;
	}
	public void setCallerRequestRecordExecutorThreadTimeOutSeconds(
			Long callerRequestRecordExecutorThreadTimeOutSeconds) {
		this.callerRequestRecordExecutorThreadTimeOutSeconds = callerRequestRecordExecutorThreadTimeOutSeconds;
	}
	public Integer getCallerRequestRecordExecutorShuttingDownCoreCount() {
		return callerRequestRecordExecutorShuttingDownCoreCount;
	}
	public void setCallerRequestRecordExecutorShuttingDownCoreCount(
			Integer callerRequestRecordExecutorShuttingDownCoreCount) {
		this.callerRequestRecordExecutorShuttingDownCoreCount = callerRequestRecordExecutorShuttingDownCoreCount;
	}
	public Integer getApiDailyMaxLimit() {
		return apiDailyMaxLimit;
	}
	public void setApiDailyMaxLimit(Integer apiDailyMaxLimit) {
		this.apiDailyMaxLimit = apiDailyMaxLimit;
	}
	public String getDataCenterEurekAaddress() {
		return dataCenterEurekAaddress;
	}
	public void setDataCenterEurekAaddress(String dataCenterEurekAaddress) {
		this.dataCenterEurekAaddress = dataCenterEurekAaddress;
	}
	public String getDataSecondDomain() {
		return dataSecondDomain;
	}
	public void setDataSecondDomain(String dataSecondDomain) {
		this.dataSecondDomain = dataSecondDomain;
	}
	public String getApiSecondDomain() {
		return apiSecondDomain;
	}
	public void setApiSecondDomain(String apiSecondDomain) {
		this.apiSecondDomain = apiSecondDomain;
	}
	public String getServiceSecondDomain() {
		return serviceSecondDomain;
	}
	public void setServiceSecondDomain(String serviceSecondDomain) {
		this.serviceSecondDomain = serviceSecondDomain;
	}
	public String getServiceDomain() {
		return serviceDomain;
	}
	public void setServiceDomain(String serviceDomain) {
		this.serviceDomain = serviceDomain;
	}
	public String getTokenTimeout() {
		return tokenTimeout;
	}
	public void setTokenTimeout(String tokenTimeout) {
		this.tokenTimeout = tokenTimeout;
	}
	public String getTicketTimeout() {
		return ticketTimeout;
	}
	public void setTicketTimeout(String ticketTimeout) {
		this.ticketTimeout = ticketTimeout;
	}
	
}
