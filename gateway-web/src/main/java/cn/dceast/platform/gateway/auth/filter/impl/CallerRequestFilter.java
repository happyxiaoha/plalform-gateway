package cn.dceast.platform.gateway.auth.filter.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.QueryOperators;

import cn.dceast.platform.gateway.auth.data.entity.CallerRequestInfo;
import cn.dceast.platform.gateway.auth.executor.OrderCountHandler;
import cn.dceast.platform.gateway.auth.filter.AuthFilter;
import cn.dceast.platform.gateway.auth.filter.FilterResponseMessage;
import cn.dceast.platform.gateway.auth.filter.RequestInfo;
import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;

/**
 * 用来计数(计费) 记录api调用信息 基于异步任务方式来记录调用信息，缩短客户端响应时间。
 * 技术上基于ThreadPoolExecutor来做异步任务执行，doFilter方法中会向ThreadPoolExecutor提交一个异步任务（
 * CallerRequestTask）。 如果成功提交，任务的执行时间依赖于ThreadPoolExecutor的调度机制。
 * 如果无法成功提交，说明ThreadPoolExecutor的所有线程都非常繁忙且等待队列已满，则拒绝客户端的请求。
 *
 * ThreadPoolExecutor 的初始化和销毁逻辑在cn.dceast.platform.gateway.auth.listener.
 * CallerReqRecExecutorManageListener中
 *
 */
public class CallerRequestFilter extends AuthFilter {

	private static Logger logger = LoggerFactory.getLogger(CallerRequestFilter.class);

	public static SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Override
	public boolean doFilter(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		logger.info("enter CallerRequestFilter");
		
		if (request.getAttribute("notMinusMoney") != null) {
			// 微服务不计费
			String orderDetailId = null;
			CallerRequestInfo callerRequestInfo = getCallerRequestInfo(request);

			String userId = RequestInfo.getUserId(request);
			BasicDBObject query = new BasicDBObject();
			query.append("userId", userId).append("areacode", callerRequestInfo.getAreacode())
					.append("resourceId",
							callerRequestInfo.getResourceId() == null ? null : callerRequestInfo.getResourceId())
					.append("resourceType", callerRequestInfo.getResourceType())
					.append("endTime", new BasicDBObject(QueryOperators.GTE, sFormat.format(new Date())))
					.append("startTime", new BasicDBObject(QueryOperators.LTE, sFormat.format(new Date())));

			// limit(1)查询出一条数据
			DBCursor dbCursor = null;
			dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).find(query).limit(1)
					.sort(new BasicDBObject("startTime", 1));
			while (dbCursor.hasNext()) {
				BasicDBObject stats = (BasicDBObject) dbCursor.next();
				orderDetailId = stats.getString("orderDetailId");
				break;
			}

			if (orderDetailId != null && orderDetailId != "") {
				request.setAttribute("orderDetailId", orderDetailId);
				logger.info("notMinusMoney orderDetailId: " + orderDetailId);
				return true;
			} else {
				if (dbCursor == null) {
					logger.info("notMinusMoney search Mongo null");
				} else {
					logger.info("orderDetailId error , value: " + orderDetailId);
				}
				return false;
			}
		} else {
			/*
			 * 计费
			 */
			// 构造调用信息对象
			CallerRequestInfo callInfo = getCallerRequestInfo(request);
			try {
				// 执行计数(写成功统计)
				// ExecutorHolder.callerRequestRecordExecutor.execute(new
				// CallerRequestTask(callInfo));
				String orderDetailId = OrderCountHandler.writeSuccStatistics(callInfo, request);
				if (orderDetailId == null) {
					setErrorMessageOfJson(request, response, "the orderDetailId is null");
					logger.error("the orderDetailId is null");
					return false;
				}
				request.setAttribute("orderDetailId", orderDetailId);
				/*
				 * 日志表更新订单id
				 */
				BasicDBObject query = new BasicDBObject().append("flownum", request.getAttribute("flownum"));
				BasicDBObject update = new BasicDBObject("$set", new BasicDBObject("orderDetailId", orderDetailId));

				MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_CALL_LOG).update(query, update, true, false);
				// } catch (RejectedExecutionException e) {
				// //队列已满，拒绝请求
				// setErrorMessageOfJson(request, response,
				// FilterResponseMessage.CODE_300102);
				// logger.error(String.format("caller request record executor is
				// too
				// busy, reject %s", callInfo));
				// return false;
			} catch (Exception e) {
				setErrorMessageOfJson(request, response, FilterResponseMessage.CODE_300102);
				logger.error("writeSuccStatistics error", e);
				return false;
			}
			return true;
		}
	}
}
