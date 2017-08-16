package cn.dceast.platform.common.mongo;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.*;

/**
 * MongoDB util inited by spring ioc container,and destroyed by container close
 * invoking destroy method
 * 
 * @author ruijf
 * 
 */
public class Mongo {
	public static Mongo ME=new Mongo();
	// private Logger logger = Logger.getLogger(DBUtil.class);

	private MongoClient mongoClient;

	private DB db;

	// @Value("#{ T(com.patsnap.classification.util.ConfigMap).getHost() }")
	private String host;

	// @Value("#{ T(com.patsnap.classification.util.ConfigMap).getDbName() }")
	private String dbName;

	// @Value("#{ T(com.patsnap.classification.util.ConfigMap).getUsername() }")
	private String username;

	// @Value("#{ T(com.patsnap.classification.util.ConfigMap).getPassword() }")
	private String password;

	private int minConnectionsPerHost;

	private int connectionsPerHost;

	private int maxWaitTime;

	private int maxConnectionIdleTime;

	private int connectTimeout;

	private int threadsAllowedToBlockForConnectionMultiplier = 5;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public void init() throws Exception {
		String[] hostArr = host.split("\\,");
		List<ServerAddress> serverList = new ArrayList<ServerAddress>();
		for (String h : hostArr) {
			serverList.add(new ServerAddress(h));
		}
		// ServerAddress[] addressList = {new
		// ServerAddress("192.168.1.205:27017"),new
		// ServerAddress("192.168.1.205:27018")};
		
		List<MongoCredential> credentialsList= null;
		if(username!=null && username.trim().length() > 0 &&
				password!=null && password.trim().length() >0){
			credentialsList = new ArrayList<MongoCredential>();
			credentialsList.add(MongoCredential.createCredential(username, dbName, password.toCharArray()));
		}

		MongoClientOptions options=MongoClientOptions.builder()
				.minConnectionsPerHost(minConnectionsPerHost)//default=0
				.connectionsPerHost(connectionsPerHost)//默认值100
				.threadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlockForConnectionMultiplier)//default=5。最大可阻塞的线程数倍数。最大可阻塞的线程数 connectionsPerHost*threadsAllowedToBlockForConnectionMultiplier
				.maxWaitTime(maxWaitTime)//default=12000。线程最大等待一个可用链接时间
				.maxConnectionIdleTime(maxConnectionIdleTime)//10分钟。
				.maxConnectionLifeTime(0)//链接最大的生命周期。0不限制
				.connectTimeout(connectTimeout)//链接超时时间。默认10秒
				.socketTimeout(0)//socket timeout.default=0不限制
				.socketKeepAlive(false)//default is false
				.build();

		mongoClient = new MongoClient(serverList,credentialsList, options);
		db = mongoClient.getDB(dbName);
	}

	public void destroy() {
		if (mongoClient != null) {
			/*
			 * if (logger.isDebugEnabled()) {
			 * logger.debug("closing the db collection...closed"); }
			 */
			mongoClient.close();
		}
	}

	public DBCollection getColl(String name) {
		return db.getCollection(name);
	}

	public int nextValue(String counterName) {
		BasicDBObject query = new BasicDBObject("name", counterName);
		BasicDBObject fields = new BasicDBObject("value", true);
		BasicDBObject update = new BasicDBObject("$inc", new BasicDBObject("value", 1));
		BasicDBObject newObj = (BasicDBObject) db.getCollection("counter")
				.findAndModify(query, fields, null, false, update, true, true);
		return newObj.getInt("value");
	}

	public static void main(String[] args)throws Exception {
		Mongo dbUtil = new Mongo();
		dbUtil.setHost("localhost");
		dbUtil.setUsername(null);
		dbUtil.setPassword(null);
		dbUtil.setDbName("oss");
		dbUtil.init();
		System.out.println(dbUtil.nextValue("menu"));
	}

	public int getMinConnectionsPerHost() {
		return minConnectionsPerHost;
	}

	public void setMinConnectionsPerHost(int minConnectionsPerHost) {
		this.minConnectionsPerHost = minConnectionsPerHost;
	}

	public int getConnectionsPerHost() {
		return connectionsPerHost;
	}

	public void setConnectionsPerHost(int connectionsPerHost) {
		this.connectionsPerHost = connectionsPerHost;
	}

	public int getMaxWaitTime() {
		return maxWaitTime;
	}

	public void setMaxWaitTime(int maxWaitTime) {
		this.maxWaitTime = maxWaitTime;
	}

	public int getMaxConnectionIdleTime() {
		return maxConnectionIdleTime;
	}

	public void setMaxConnectionIdleTime(int maxConnectionIdleTime) {
		this.maxConnectionIdleTime = maxConnectionIdleTime;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getThreadsAllowedToBlockForConnectionMultiplier() {
		return threadsAllowedToBlockForConnectionMultiplier;
	}

	public void setThreadsAllowedToBlockForConnectionMultiplier(int threadsAllowedToBlockForConnectionMultiplier) {
		this.threadsAllowedToBlockForConnectionMultiplier = threadsAllowedToBlockForConnectionMultiplier;
	}
}
