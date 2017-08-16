package cn.dceast.platform.gateway.auth.filter.impl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.dceast.platform.gateway.auth.data.entity.Appkey;
import cn.dceast.platform.gateway.auth.filter.AuthFilter;
import cn.dceast.platform.gateway.auth.filter.FilterMatcher;
import cn.dceast.platform.gateway.auth.filter.FilterResponseMessage;
import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;

import com.mongodb.BasicDBObject;

/**
 * 
 * (每日调用最大限额过滤)用户最大日访问量过滤。此过滤器建议放在统计过滤器(即CallerRequestFilter)后面
 * 
 * @author zhang
 *
 */
public class CallerMaxVisitOfDayFilter extends AuthFilter {
	private static Logger logger = LoggerFactory.getLogger(CallerMaxVisitOfDayFilter.class);

	@Override
	public boolean doFilter(HttpServletRequest request, HttpServletResponse response) {

		logger.info("enter CallerMaxVisitOfDayFilter");
		try {
			// 根据request字段或配置中心返回日最大访问次数字段
			Integer maxCountOfDay = getApiDailyLimitCount(request);
			
			//不限制次数
			if (maxCountOfDay == null || maxCountOfDay.intValue() == -1) {
				return true;
			}
			
			Map<String, String> resourceMap = FilterMatcher.getParameterFromRequest(request);
			String resourceId = resourceMap.get("resourceId");
			String resourceType = resourceMap.get("resourceType");
			String areacode = FilterMatcher.getAreacode(request,false);
			// Integer currentDate = DateUtil.getCurrentDate();
			Appkey appkeyInfo = getAppkeyInfo(request);

			
			
			
			///////////////////访问次数+1
			BasicDBObject query = new BasicDBObject();
			query.append("appKey", appkeyInfo == null ? null : appkeyInfo.getAppkey()).append("areacode", areacode)
					.append("resourceType", resourceType).append("resourceId", resourceId);
			// .append("apiUrl", api)
			// .append("appName", appName)
			// .append("updateDate", new BasicDBObject("$gte", currentDate));

			BasicDBObject field = new BasicDBObject();
			field.append("$inc", new BasicDBObject("countOfDay", 1));
			// field.append("appName", 1)
			// .append("apiUrl", 1)
			// .append("callerName", 1)
			// .append("count", 1)
			// .append("countOfDay", 1);
			
			//最大访问次数+1
			MongoDBUtil.getColl(InnerConstants.COLL_API_DAILY_REQUEST_STATICS).update(query, field, true, false);
			///////////////////
			
	
			
			BasicDBObject stat = (BasicDBObject) MongoDBUtil.getColl(InnerConstants.COLL_API_DAILY_REQUEST_STATICS)
					.findOne(query);
			//默认设置为用户第一次访问
			long countOfDay = 1;
			if (stat != null) {
				//返回表中数据
				countOfDay = stat.getLong("countOfDay", 1);
			}

			if (countOfDay >= maxCountOfDay) {
				//超过最大访问次数,鉴权失败
				setErrorMessageOfJson(request, response, FilterResponseMessage.CODE_101104);
				logger.error(String.format("Your are over the maxCountOfDay![%s>%s]", countOfDay, maxCountOfDay));
				return false;
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
