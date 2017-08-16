package cn.dceast.platform.gateway.auth.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.dceast.platform.gateway.auth.listener.InitApiListener;
import cn.dceast.platform.gateway.auth.rest.CallAPIFailedStatisticResource;
import cn.dceast.platform.gateway.auth.rest.DyUps;
import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.sun.xml.rpc.processor.modeler.j2ee.xml.resAuthType;

public class ServiceApi {

	private static Logger logger = LoggerFactory.getLogger(ServiceApi.class);

	/**
	 * 向MongoDB插入信息
	 * 
	 * @param data
	 * @param appName
	 * @param upstream
	 */
	public static void addApi(String data, String appName, String upstream, String trueContext,
			String urlStringBuilder,boolean isHttps) {
		JSONObject jso = new JSONObject().parseObject(data);
		Map<String, Object> orderMap = JSONObject.toJavaObject(jso, Map.class);
		MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_API)
				.insert(constructBasicDBOject(appName, upstream, orderMap, trueContext, urlStringBuilder,isHttps));
	}

	/**
	 * 构建向MongoDB插入的数据实体
	 * 
	 * @param appName
	 * @param upstream
	 * @param orderMap
	 * @return
	 */
	private static BasicDBObject constructBasicDBOject(String appName, String upstream, Map<String, Object> orderMap,
			String trueContext, String urlStringBuilder,boolean isHttps) {
		BasicDBObject stats = new BasicDBObject();
		String areacode = (String) orderMap.get("areacode");
		String resourceType = (String) orderMap.get("resourceType");
		stats.append("areacode", areacode);
		
		if (isHttps) {
			stats.append("isHttps", "1");
		}else {
			stats.append("isHttps", "0");
		}
		
		if (resourceType.equals(InnerConstants.RESOURCETYPE_MICROSVC)) {
			stats.append("urls", urlStringBuilder);
		}

		switch (resourceType) {
		// resourcetype 01-api 02-数据 03-微服务 04-SaaS服务
		case "01":
			stats.append("domain", DyUps.API);
			break;
		case "02":
			stats.append("domain", DyUps.DATA);
			break;
		case "03":
			stats.append("domain", DyUps.SERVICE);
			break;
		case "04":
			stats.append("domain", DyUps.SAAS);
		default:
			break;
		}
		String url = (String) orderMap.get("context");
		stats.append("url", url);
		stats.append("trueContext", trueContext);
		stats.append("appName", appName);
		stats.append("upstream", upstream);
		String resourceId = (String) orderMap.get("resourceId");
		stats.append("resourceId", resourceId);
		stats.append("resourceType", resourceType);
		String jsonAppName = (String) orderMap.get("appName");
		stats.append("jsonAppName", jsonAppName);
		///////////////////
		String isAuth = (String) orderMap.get("isAuth");
		if (isAuth == null) {
			stats.append("isAuth", "0");
		} else {
			stats.append("isAuth", isAuth);
		}
		//////////////////
		String owncity = (String) orderMap.get("owncity");
		stats.append("owncity", owncity);
		String sourcecity = (String) orderMap.get("sourcecity");
		stats.append("sourcecity", sourcecity);
		return stats;
	}

	/**
	 * 删除MongoDB中的某条信息
	 * 
	 * @param data
	 * @param appName
	 * @param upstream
	 * @param value
	 */
	public static void removeApi(String data, String appName, String upstream, String value, String urlStringBuilder,boolean isHttps) {
		JSONObject jso = new JSONObject().parseObject(data);
		Map<String, Object> orderMap = JSONObject.toJavaObject(jso, Map.class);
		MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_API)
				.remove(constructBasicDBOject(appName, upstream, orderMap, value, urlStringBuilder,isHttps));
	}

	/**
	 * 删除MongoDB中的某条信息
	 * 
	 * @param stats
	 */
	public static void removeApi(BasicDBObject stats) {
		MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_API).remove(stats);
	}

	/**
	 * 参考ServiceStatisticsService类的writeSuccStatistics方法 初始化路由信息
	 */
	public static void initApiOrServiceOrData(String nginxDyupsAddress, String redisAddress) {
		DBCursor dbCursor = null;
		DyUps dyUps = new DyUps();

		try {
			dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_API).find();
			// 遍历MongoDb所有数据
			if (dbCursor.size() != 0) {
				while (dbCursor.hasNext()) {
					BasicDBObject stats = (BasicDBObject) dbCursor.next();
					///// 写入redis
					String url = stats.getString("url");
					String trueContext = stats.getString("trueContext");
					String resourceType = stats.getString("resourceType");
					// 微服务特殊，将urls中的内容写入
					if (resourceType.equals(InnerConstants.RESOURCETYPE_MICROSVC)) {
						String urls = stats.getString("urls");
						//////
						if (urls != null && !urls.equals("")) {
							String[] urlsArray = urls.split(";");
							for (int i = 0; i < urlsArray.length; i++) {
								// 写redis
								dyUps.initRedis(url + urlsArray[i], urlsArray[i], redisAddress);
								logger.info("init urls write into redis " + "key: " + url + urlsArray[i] + " value: "
										+ urlsArray[i]);
							}
						}
					}
					//////
					if (trueContext == null) {
						trueContext = "";
					}
					dyUps.initRedis(url, trueContext, redisAddress);
					logger.info("Redis Init " + url + " value:" + trueContext + " success");
					//// --写入redis

					String appName = stats.getString("appName");
					String upstream = stats.getString("upstream");
					// 写外网nginx
					String ret = dyUps.addAppRouter(appName, upstream, nginxDyupsAddress);

					////////////// 判断返回结果
					if (ret.contains("success")) {
						// 写入成功
						logger.info("Init " + appName + " success");
					} else {
						// 写入失败
						logger.info("fail to init " + appName + " return value: " + ret);
					}
					///////////// --判断返回结果

					try {
						// 防止高频向nginx 58088端口写入内容处理不过来
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			// --遍历MongoDb所有数据
		} finally {
			if (dbCursor != null) {
				dbCursor.close();
			}
		}
	}

	/**
	 * 更新MongoDB中数据 
	 * 参考initApi FIXME 哪个iniApi? 是initApiOrServiceOrData()这个方法？
	 * 
	 */
	public static void updateApi(String data, String appName, String upstream, String value, String urlStringBuilder,boolean isHttps) {
		DBCursor dbCursor = null;
		try {
			dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_API).find();
			if (dbCursor.size() != 0) {
				//删掉原有字段
				while (dbCursor.hasNext()) {
					BasicDBObject stats = (BasicDBObject) dbCursor.next();
					String mongodbAppName = stats.getString("appName");
					if (mongodbAppName.equals(appName)) {
						// removeApi(data, mongodbAppName, mongodbUpstream);
						removeApi(stats);
					}
				}
				addApi(data, appName, upstream, value, urlStringBuilder,isHttps);
			}
		} finally {
			if (dbCursor != null) {
				dbCursor.close();
			}
		}
		logger.info("update MongoDB success " + "appName:" + appName + " upstream:" + upstream);

	}

}
