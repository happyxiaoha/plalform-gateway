package cn.dceast.platform.gateway.auth.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.dceast.platform.common.mongo.Mongo;
import cn.dceast.platform.gateway.auth.data.entity.CallerRequestInfo;
import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;
import cn.dceast.platform.gateway.auth.util.UrlUtil;
import javassist.runtime.Inner;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.QueryOperators;

public class ServiceStatisticsService {

	private static Logger logger = LoggerFactory.getLogger(ServiceStatisticsService.class);

	public static SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * 将访问记录写入服务调用统计表 已废弃
	 * 
	 * @param callerRequestInfo
	 */
	public static void writeSuccStatistics(CallerRequestInfo callerRequestInfo) {

		BasicDBObject query = new BasicDBObject();
		query.append("userId", callerRequestInfo.getUserId()).append("resourceId", callerRequestInfo.getResourceId())
				.append("resourceType", callerRequestInfo.getResourceType())
				.append("endTime", new BasicDBObject(QueryOperators.GTE, sFormat.format(new Date())))
				.append("startTime", new BasicDBObject(QueryOperators.LTE, sFormat.format(new Date())))
				.append("used", "yes");

		DBCursor dbCursor = null;
		boolean hasFree = false;
		try {

			dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).find(query)
					.sort(new BasicDBObject("startTime", 1));
			if (dbCursor.size() != 0) {
				/*
				 * 有标记used的优先使用
				 */
				while (dbCursor.hasNext()) {
					BasicDBObject stats = (BasicDBObject) dbCursor.next();
					BasicDBObject newObject = new BasicDBObject().append("$inc", new BasicDBObject("currentUsed", 1));

					MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).update(stats, newObject);
					break;
				}
			} else {
				/*
				 * 重新查询需扣除的订单
				 */
				query.remove("used");
				query.append("charRuleType", "0");// 免费套餐/体验套餐
				dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).find(query).limit(1)
						.sort(new BasicDBObject("startTime", 1));// 免费套餐/体验套餐
				while (dbCursor.hasNext()) {
					hasFree = true;
					BasicDBObject stats = (BasicDBObject) dbCursor.next();
					BasicDBObject newObject = new BasicDBObject().append("$inc", new BasicDBObject("currentUsed", 1))
							.append("$set", new BasicDBObject("used", "yes"));

					MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).update(stats, newObject);
					break;
				}
				boolean hasTime = false;
				if (!hasFree) {// 按时套餐
					query.append("charRuleType", "1");
					dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).find(query).limit(1)
							.sort(new BasicDBObject("startTime", 1));// 按时计费
					while (dbCursor.hasNext()) {
						hasTime = true;
						BasicDBObject stats = (BasicDBObject) dbCursor.next();
						BasicDBObject newObject = new BasicDBObject()
								.append("$inc", new BasicDBObject("currentUsed", 1))
								.append("$set", new BasicDBObject("used", "yes"));

						MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).update(stats, newObject);
						break;
					}
				}
				boolean hasCount = false;
				if (!hasFree && !hasTime) {// 按次套餐
					query.append("charRuleType", "2");
					dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).find(query).limit(1)
							.sort(new BasicDBObject("endTime", 1));// 按次计费
					while (dbCursor.hasNext()) {// 按次计费
						hasCount = true;
						BasicDBObject stats = (BasicDBObject) dbCursor.next();
						BasicDBObject newObject = new BasicDBObject()
								.append("$inc", new BasicDBObject("currentUsed", 1))
								.append("$set", new BasicDBObject("used", "yes"));
						MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).update(stats, newObject);
						break;
					}
				}
				if (!hasFree && !hasTime && !hasCount) {// 无可用套餐
					if (logger.isDebugEnabled())
						logger.debug("用户 [" + callerRequestInfo.getCallerName() + "]资源id ["
								+ callerRequestInfo.getResourceId() + "]无可用套餐!");
					throw new RejectedExecutionException();
				}
			}
		} finally {
			if (dbCursor != null) {
				dbCursor.close();
			}
		}
	}

	/**
	 * 失败处理 currentUsed-1 failNumber+1
	 */
	public static void writeFailStatistics(CallerRequestInfo callerRequestInfo) {

		String resourceId = null;
		String resourceType = null;
		String areacode = null;
		String urls = null;
		// 查询order_rule_list表。如果为IPWhiteFilterImpl中的notMinusMoney微服务，因currentUsed没有-1,所以这里只是做failNumber+1
		BasicDBObject query_order_detail_list = new BasicDBObject("orderDetailId",
				callerRequestInfo.getOrderDetailId());
		DBCursor dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).find(query_order_detail_list);
		while (dbCursor.hasNext()) {
			BasicDBObject object = (BasicDBObject) dbCursor.next();
			resourceId = object.getString("resourceId");
			resourceType = object.getString("resourceType");
			areacode = object.getString("areacode");
		}

		logger.info("writeFailStatistics() resourceId: " + resourceId + "resourceType: " + resourceType + " areacode: "
				+ areacode);
		if (resourceType.equals(InnerConstants.RESOURCETYPE_MICROSVC)) {
			BasicDBObject query_service_api = new BasicDBObject().append("resourceId", resourceId)
					.append("resourceType", resourceType).append("areacode", areacode);
			DBCursor dbCursor2 = MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_API).find(query_service_api);
			while (dbCursor2.hasNext()) {
				BasicDBObject object2 = (BasicDBObject) dbCursor.next();
				urls = object2.getString("urls");
			}
			//
			String secondContext = UrlUtil.getSecondContext(UrlUtil.getUriNoParams(callerRequestInfo.getUri()));
			if (!urls.contains(secondContext)) {
				// 仅failNumber+1
				BasicDBObject query = new BasicDBObject("orderDetailId", callerRequestInfo.getOrderDetailId());
				BasicDBObject update = new BasicDBObject().append("failNumber", 1);
				MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).update(query, update, true, false);
			} else {
				// 失败处理 currentUsed-1 failNumber+1
				BasicDBObject query = new BasicDBObject("orderDetailId", callerRequestInfo.getOrderDetailId());
				BasicDBObject update = new BasicDBObject().append("$inc",
						new BasicDBObject("currentUsed", -1).append("failNumber", 1));
				MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).update(query, update, true, false);
			}
		} else {
			// 非微服务 失败处理 currentUsed-1 failNumber+1
			BasicDBObject query = new BasicDBObject("orderDetailId", callerRequestInfo.getOrderDetailId());
			BasicDBObject update = new BasicDBObject().append("$inc",
					new BasicDBObject("currentUsed", -1).append("failNumber", 1));
			MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).update(query, update, true, false);
		}
	}

	/**
	 * 调用失败,每日上限额-1
	 */
	public static void updateCallerMaxVisitOfDay(CallerRequestInfo callerRequestInfo) {
		String flownum = callerRequestInfo.getFlownum();
		BasicDBObject db = (BasicDBObject) MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_CALL_LOG)
				.findOne(new BasicDBObject("flownum", flownum));
		if (db != null) {
			BasicDBObject query = new BasicDBObject().append("appKey", db.getString("appKey"))
					.append("areacode", db.getString("areacode")).append("resourceType", db.getString("resourceType"))
					.append("resourceId", db.getString("resourceId"));

			BasicDBObject update = new BasicDBObject("$inc", new BasicDBObject("countOfDay", -1));
			MongoDBUtil.getColl(InnerConstants.COLL_API_DAILY_REQUEST_STATICS).update(query, update, true, false);

		}
	}

}
