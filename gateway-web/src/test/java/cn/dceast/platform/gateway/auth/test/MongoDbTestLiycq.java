package cn.dceast.platform.gateway.auth.test;

import java.util.Date;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.QueryOperators;

import cn.dceast.platform.common.BusinessException;
import cn.dceast.platform.common.mongo.DBUtil;
import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;

public class MongoDbTestLiycq {

	public static void main(String[] args) {
		DBUtil dbUtil = new DBUtil();
		dbUtil.setHost("172.16.49.68");
		dbUtil.setDbName("gateway");
		dbUtil.setUsername("platform-oss");
		dbUtil.setPassword("dc202152");
		dbUtil.setMinConnectionsPerHost(0);
		dbUtil.setConnectionsPerHost(100);
		dbUtil.setConnectTimeout(10000);
		dbUtil.setMaxConnectionIdleTime(370000);
		dbUtil.setMaxWaitTime(120000);
		dbUtil.setThreadsAllowedToBlockForConnectionMultiplier(5);
		try {
			dbUtil.init();
		} catch (Exception e) {
			throw new BusinessException("init mongodb fail! detail:" + e.getMessage());
		}

		BasicDBObject query = new BasicDBObject();
		query.append("userId", "9224").append("resourceId", "912").append("resourceType", "02")
				.append("endTime", "2014-01-01 00:00:00").append("startTime", "2012-01-01 00:00:00");

		DBCursor dbCursor = dbUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).find(query).limit(1)
				.sort(new BasicDBObject("startTime", 1));// 免费套餐/体验套餐

		while (dbCursor.hasNext()) {
			BasicDBObject stats = (BasicDBObject) dbCursor.next();
			System.out.println(stats.getString("orderDetailId"));
		}
		// System.out.println(dbCursor);
	}

}
