package cn.dceast.platform.gateway.auth.filter.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.dceast.platform.gateway.auth.data.entity.Appkey;
import cn.dceast.platform.gateway.auth.filter.AuthFilter;
import cn.dceast.platform.gateway.auth.filter.FilterMatcher;
import cn.dceast.platform.gateway.auth.filter.FilterResponseMessage;
import cn.dceast.platform.gateway.auth.filter.MicroServiceUtil;
import cn.dceast.platform.gateway.auth.filter.RequestInfo;
import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;
import cn.dceast.platform.gateway.auth.util.UrlUtil;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

/*
 * 1:对套餐表order_rule_list表中currentUsed=number的数据进行转存
 * 2:日期过期的数据进行转存
 */
public class PackageToHistoryFilter extends AuthFilter {

	private static Logger logger = LoggerFactory.getLogger(PackageToHistoryFilter.class);

	DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Override
	public boolean doFilter(HttpServletRequest request, HttpServletResponse response) throws Exception {

		logger.info("enter PackageToHistoryFilter");
		/*
		 * 主要工作 用userid，areacode，resourceid，resourcetype等字段查询order_rule_list表。
		 * 如果“总数和已调用的数相等”或“订单结束时间过期” 存入历史表 删除order_rule_list表中当前记录 用户设为不可接入
		 */

		///////////////////////////
		String areacode = FilterMatcher.getAreacode(request,false);
		Map<String, String> resourceMap = FilterMatcher.getParameterFromRequest(request);
		Appkey appkeyInfo = getAppkeyInfo(request);
		List<String> subDomainList = FilterMatcher.getSecondDomainList(request);
		String userId = null;
		if (!subDomainList.get(1).equals(InnerConstants.RESOURCETYPE_MICROSVC)) {
			// 不是微服务
			userId = appkeyInfo == null ? null : appkeyInfo.getUserId();
		} else {
			// 微服务
			userId = RequestInfo.getUserId(request);
			// 设置response中的userId
			response.setHeader("userId", userId);
			if (userId == null) {
				logger.info("get microService userId fail.");
				return false;
			}
			logger.info("userId :" + userId);
		}

		///////////////////////////// 待查询订单信息
		BasicDBObject query = new BasicDBObject();
		query.append("userId", userId).append("areacode", areacode).append("resourceId", resourceMap.get("resourceId"))
				.append("resourceType", resourceMap.get("resourceType"));
		// .append("count",new BasicDBObject("$eq", new
		// BasicDBObject("countused")));
		//////////////////////////////

		DBCursor dbCursor = null;
		try {
			// 是否可接入
			boolean existAccess = false;
			dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).find(query);
			while (dbCursor.hasNext()) {
				BasicDBObject stats = (BasicDBObject) dbCursor.next();
				try {
					if (stats.getString("number").equals("-1")) {
						// 不限次数,可接入
						existAccess = true;
					} else if (stats.getString("number").equals(stats.getString("currentUsed")) // 总数和已调用的数相等
							|| new Date().after(fmt.parse(stats.getString("endTime")))) {// 订单结束时间过期
						// 不可接入
						MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST_HISTORY).save(stats);// 存入历史表
						MongoDBUtil.getColl(InnerConstants.COLL_ORDER_RULE_LIST).remove(stats);// 删除order_rule_list表记录
					} else {
						// 可接入
						existAccess = true;
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			// 如果无法接入或库中没有，报错返回,不进行下面鉴权
			if (!existAccess) {
				setErrorMessageOfJson(request, response, FilterResponseMessage.CODE_104101);
				logger.error("areacode: "+areacode+"| resourceId " + resourceMap.get("resourceId") + "| userId " + userId
						+ " :Buy this product first");
				return false;
			}
		} finally {
			if (dbCursor != null) {
				dbCursor.close();
			}
		}
		return true;
	}

}
