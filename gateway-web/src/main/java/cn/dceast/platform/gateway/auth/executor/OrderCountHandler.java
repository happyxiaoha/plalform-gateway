package cn.dceast.platform.gateway.auth.executor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.dceast.platform.gateway.auth.data.entity.CallerRequestInfo;
import cn.dceast.platform.gateway.auth.filter.FilterMatcher;
import cn.dceast.platform.gateway.auth.filter.RequestInfo;
import cn.dceast.platform.gateway.auth.service.ServiceStatisticsService;
import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryOperators;
import com.mongodb.WriteResult;

/**
 * 订单次数(计费)处理器，供CallerRequestFilter类调用
 * 
 * @author liycq
 *
 */
public class OrderCountHandler {

	private static Logger logger = LoggerFactory.getLogger(OrderCountHandler.class);

	public static SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * 执行计数(写成功统计)
	 * 
	 * @param callerRequestInfo
	 * @return orderDetailId
	 */
	public synchronized static String writeSuccStatistics(CallerRequestInfo callerRequestInfo,
			HttpServletRequest request) {

		String orderDetailId = null;
		List<String> subDomainList = null;
		String userId = callerRequestInfo.getUserId();
		try {
			subDomainList = FilterMatcher.getSecondDomainList(request);
			if (subDomainList.get(1).equals(InnerConstants.RESOURCETYPE_MICROSVC)) {
				// 如果是微服务
				userId = RequestInfo.getUserId(request);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		////// 待查询对象
		BasicDBObject query = new BasicDBObject();
		String areacode = callerRequestInfo.getAreacode();
		query.append("userId", userId).append("areacode", areacode)
				.append("resourceId",
						callerRequestInfo.getResourceId() == null ? null : callerRequestInfo.getResourceId())
				.append("resourceType", callerRequestInfo.getResourceType())
				.append("endTime", new BasicDBObject(QueryOperators.GTE, sFormat.format(new Date())))
				.append("startTime", new BasicDBObject(QueryOperators.LTE, sFormat.format(new Date())));
		//////
		DBCursor dbCursor = null;
		// 是否为免费
		boolean isFree = false;
		try {
			// /*
			// * 重新查询需扣除的订单
			// */
			// // query.remove("used");
			query.append("charRuleType", "0");// 免费套餐/体验套餐
			// limit(1)查询出一条数据
			dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).find(query).limit(1)
					.sort(new BasicDBObject("startTime", 1));
			while (dbCursor.hasNext()) {// 免费套餐/体验套餐
				logger.info("areacode [" + areacode + "]userId  [" + userId + "]resourceId ["
						+ callerRequestInfo.getResourceId() + "] follow fire");
				isFree = true;
				BasicDBObject stats = (BasicDBObject) dbCursor.next();
				orderDetailId = stats.getString("orderDetailId");
				BasicDBObject newObject = new BasicDBObject().append("$inc", new BasicDBObject("currentUsed", 1));
				// .append("$set", new BasicDBObject("used", "yes"));
				// 状态更新
				MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).update(stats, newObject);
				break;
			}

			// 是否按时计费
			boolean isFollowTime = false;
			// 是否按次计费
			boolean isFollowCount = false;
			if (!isFree) {// 不免费
				query.append("charRuleType", "1");
				dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).find(query).limit(1)
						.sort(new BasicDBObject("startTime", 1));// 按时计费
				if (dbCursor.size() >= 1) {// 按时计费
					while (dbCursor.hasNext()) {
						logger.info("areacode [" + areacode + "]userId  [" + userId + "]resourceId ["
								+ callerRequestInfo.getResourceId() + "] follow time");
						isFollowTime = true;
						BasicDBObject stats = (BasicDBObject) dbCursor.next();
						orderDetailId = stats.getString("orderDetailId");
						BasicDBObject newObject = new BasicDBObject().append("$inc",
								new BasicDBObject("currentUsed", 1));
						// .append("$set", new BasicDBObject("used", "yes"));
						MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).update(stats, newObject);
						break;
					}
				} else {// 按次计费
					query.append("charRuleType", "2");
					dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).find(query).limit(1)
							.sort(new BasicDBObject("startTime", 1));
					while (dbCursor.hasNext()) {
						logger.info("areacode [" + areacode + "]userId  [" + userId + "]resourceId ["
								+ callerRequestInfo.getResourceId() + "] follow count");
						isFollowCount = true;
						BasicDBObject stats = (BasicDBObject) dbCursor.next();
						orderDetailId = stats.getString("orderDetailId");
						BasicDBObject newObject = new BasicDBObject().append("$inc",
								new BasicDBObject("currentUsed", 1));
						// .append("$set", new BasicDBObject("used", "yes"));
						MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).update(stats, newObject);
						break;
					}
				}
			}

			// SaaS按接入量计费
			boolean isFollowInsertCount = false;
			if (!isFree && !isFollowTime && !isFollowCount) {// 按接入量
				query.append("charRuleType", "3");
				dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).find(query).limit(1)
						.sort(new BasicDBObject("endTime", 1));
				while (dbCursor.hasNext()) {
					logger.info("areacode [" + areacode + "]userId  [" + userId + "]resourceId ["
							+ callerRequestInfo.getResourceId() + "] follow SaaSInsertCount");
					isFollowInsertCount = true;
					BasicDBObject stats = (BasicDBObject) dbCursor.next();
					orderDetailId = stats.getString("orderDetailId");
					BasicDBObject newObject = new BasicDBObject().append("$inc", new BasicDBObject("currentUsed", 1));
					// .append("$set", new BasicDBObject("used", "yes"));
					MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).update(stats, newObject);
					break;
				}
			}

			if (!isFree && !isFollowTime && !isFollowCount && !isFollowInsertCount) {// 无可用套餐
				logger.info("areacode [" + areacode + "]userId  [" + userId + "]resourceId ["
						+ callerRequestInfo.getResourceId() + "] is illegal!");
				throw new RejectedExecutionException();
			}
		} finally {
			if (dbCursor != null) {
				dbCursor.close();
			}
		}
		return orderDetailId;
	}

	/**
	 * 已废弃 失败处理 currentUsed-1 failNumber+1
	 */
	public synchronized static void writeFailStatistics(CallerRequestInfo callerRequestInfo) {

		BasicDBObject query = new BasicDBObject("orderDetailId", callerRequestInfo.getOrderDetailId());

		BasicDBObject update = new BasicDBObject().append("$inc",
				new BasicDBObject("currentUsed", -1).append("failNumber", 1));

		WriteResult wc = MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).update(query, update, true, false);
		if (wc.getN() == 0) {// 查找历史表
			WriteResult wccurrent = MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST_HISTORY).update(query,
					update, true, false);
			if (wccurrent.getN() != 0) {
				DBObject backOrder = MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST_HISTORY).findOne(query);
				MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).insert(backOrder);
				MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST_HISTORY).remove(backOrder);
			}
		}
	}
}
