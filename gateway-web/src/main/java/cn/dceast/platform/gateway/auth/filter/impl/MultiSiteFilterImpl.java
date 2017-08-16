package cn.dceast.platform.gateway.auth.filter.impl;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.dceast.platform.gateway.auth.filter.AuthFilter;
import cn.dceast.platform.gateway.auth.filter.FilterMatcher;
import cn.dceast.platform.gateway.auth.filter.RequestInfo;
import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;

import com.mongodb.BasicDBObject;

/**
 * 检查是否是多站点推广进入
 * ，比如，用户访问北京网关鉴权，再跳转到全国网关。在全国网关进入此步，发现为多站点推广，直接设置iPWhite字段，不走下面的鉴权了。
 */
public class MultiSiteFilterImpl extends AuthFilter {
	private Logger logger = LoggerFactory.getLogger(MultiSiteFilterImpl.class);

	@Override
	public boolean doFilter(HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.info("enter MultiSiteFilterImpl");
		String appKey = RequestInfo.getAppkey(request);
		String areacode = FilterMatcher.getAreacode(request, false);
		String abbr_citycode = FilterMatcher.getSecondDomainList(request).get(0);
		logger.info("MultiSiteFilterImpl " + " appKey: " + appKey + " areacode: " + areacode
				+ " abbr_citycode: " + abbr_citycode);
		BasicDBObject query = new BasicDBObject("appKey", appKey).append("areacode", areacode).append("abbr_citycode",
				abbr_citycode);
		BasicDBObject dbObject = (BasicDBObject) MongoDBUtil.getColl(InnerConstants.COLL_CITYUSERMAPPING)
				.findOne(query);
		if (dbObject != null) {
			String ips = dbObject.getString("ip");
			if (ips != null) {
				String currentIp = RequestInfo.getIpAddr(request);
				List<String> ipList = Arrays.asList(ips.split(";"));
				if (ipList.contains(currentIp)) {
					logger.info("MultiSiteFilterImpl ip : " + ips);
					request.setAttribute("iPWhite", "true");
				}
			}
		}
		return true;
	}
}
