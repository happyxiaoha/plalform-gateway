package cn.dceast.platform.common.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class DBUtil
{
  private MongoClient mongoClient;
  private DB db;
  private String host;
  private String dbName;
  private String username;
  private String password;
	private int minConnectionsPerHost;

	private int connectionsPerHost;

	private int maxWaitTime;

	private int maxConnectionIdleTime;

	private int connectTimeout;

	private int threadsAllowedToBlockForConnectionMultiplier = 5;
  public String getHost()
  {
    return this.host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getDbName() {
    return this.dbName;
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
	
  public void init() throws Exception {
//    String[] hostArr = this.host.split("\\,");
//    List serverList = new ArrayList();
//    String[] arr$ = hostArr; int len$ = arr$.length; for (int i$ = 0; i$ < len$; ++i$) { String h = arr$[i$];
//      serverList.add(new ServerAddress(h));
//    }

//    List credentialsList = null;
//    if ((this.username != null) && (this.username.trim().length() > 0) && (this.password != null) && (this.password.trim().length() > 0))
//    {
//      credentialsList = new ArrayList();
//      credentialsList.add(MongoCredential.createCredential(this.username, this.dbName, this.password.toCharArray()));
//    }

	String[] hostArr = host.split("\\,");
	List<ServerAddress> serverList = new ArrayList<ServerAddress>();
	for (String h : hostArr) {
		serverList.add(new ServerAddress(h));
	}
		
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
//    this.mongoClient = new MongoClient(serverList, credentialsList);
	mongoClient = new MongoClient(serverList,credentialsList, options);	
    this.db = this.mongoClient.getDB(this.dbName);
  }

  public void destroy() {
    if (this.mongoClient != null)
    {
      this.mongoClient.close();
    }
  }

  public DBCollection getColl(String name) {
    return this.db.getCollection(name);
  }

  public int nextValue(String counterName) {
    BasicDBObject query = new BasicDBObject("name", counterName);
    BasicDBObject fields = new BasicDBObject("value", Boolean.valueOf(true));
    BasicDBObject update = new BasicDBObject("$inc", new BasicDBObject("value", Integer.valueOf(1)));
    BasicDBObject newObj = (BasicDBObject)this.db.getCollection("counter").findAndModify(query, fields, null, false, update, true, true);

    return newObj.getInt("value");
  }

  /**
   * 测试类..
 * @param args
 * @throws Exception
 */
public static void main(String[] args) throws Exception {
    DBUtil dbUtil = new DBUtil();
    dbUtil.setHost("localhost");
    dbUtil.setUsername(null);
    dbUtil.setPassword(null);
    dbUtil.setDbName("oss");
    dbUtil.init();
    System.out.println(dbUtil.nextValue("menu"));
  }
}