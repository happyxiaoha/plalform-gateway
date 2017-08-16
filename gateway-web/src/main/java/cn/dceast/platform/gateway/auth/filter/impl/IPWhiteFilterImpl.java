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
import cn.dceast.platform.gateway.auth.util.UrlUtil;

import com.mongodb.BasicDBObject;

/**
 * IP白名单设置（设置不鉴权(此filter后的鉴权代码不执行)的请求）
 * 验证码类、支付回调的请求，应不鉴权,直接返回（需对接口加一个访问控制，我们做对应修改）
 */
public class IPWhiteFilterImpl extends AuthFilter {
	private Logger logger = LoggerFactory.getLogger(IPWhiteFilterImpl.class);

	@Override
	public boolean doFilter(HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.info("enter IPWhiteFilterImpl");
		String url = RequestInfo.getUriNoParams(request);
		String areacode = FilterMatcher.getAreacode(request,false);
		List<String> subDomainList = FilterMatcher.getSecondDomainList(request);
		String firstContext = UrlUtil.getFirstContext(url);
		String secondContext = UrlUtil.getUriNoParams(UrlUtil.getSecondContext(url));

		// 微服务
		if (subDomainList.get(1).equals(InnerConstants.RESOURCETYPE_MICROSVC)) {
			// isAuth 0(需鉴权) 1（不鉴权 验证码回调之类） 2(不鉴权 业务系统接口)
			BasicDBObject serviceQuery = new BasicDBObject("areacode", areacode).append("url", firstContext)
					.append("resourceType", subDomainList.get(1)).append("isAuth", "1");

			if (MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_API).count(serviceQuery) != 0) {
				// isAuth =1 验证码类不需要鉴权
				request.setAttribute("iPWhite", "true");
				return true;
			} else {
				// 查询白名单表判断是否为业务系统,不需要鉴权,
				serviceQuery.append("isAuth", "2");
				if (MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_API).count(serviceQuery) != 0) {
					/*
					 * 关联白名单表查询对应ip的资源
					 */
					serviceQuery.remove("isAuth");
					// 返回一个不需要鉴权的object实体,查询白名单表
					BasicDBObject object = (BasicDBObject) MongoDBUtil
							.getColl(InnerConstants.COLL_ACCESS_CTRL_WHITELIST).findOne(serviceQuery);
					if (object != null) {
						String ip = object.getString("ip");
						List<String> ipList = Arrays.asList(ip.split(";"));
						String currentIp = getIpAddr(request);
						if (ipList.contains(currentIp)) {
							// access_ctrl_whitelist中是否包含请求ip,如包含则设置iPWhite,
							request.setAttribute("iPWhite", "true");
							return true;
						}
					}
				}
			}

			/////////////////////// 包含在service_api表的urls字段中,则不需计费
			serviceQuery.append("isAuth", "0");
			BasicDBObject object = (BasicDBObject) MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_API)
					.findOne(serviceQuery);
			if (object != null && secondContext != null) {
				String urls = (String) object.get("urls");
				if (!urls.contains(secondContext)) {
					request.setAttribute("notMinusMoney", "true");
					return true;
				}
			}
			///////////////////////
			
			//访问微服务首页时
			if (secondContext == null) {
				request.setAttribute("notMinusMoney", "true");
			}
		}
		// API DATA
		else {

			// isAuth 0(需要鉴权) 1（不需要鉴权 验证码回调之类的） 2(不需要鉴权 业务系统接口)
			BasicDBObject query = new BasicDBObject("areacode", areacode).append("url", url)
					.append("resourceType", subDomainList.get(1)).append("isAuth", "1");

			// 是否为验证码类的不需要鉴权
			if (MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_API).count(query) != 0) {
				request.setAttribute("iPWhite", "true");
			} else {
				// 是否为业务系统的不需要鉴权
				query.append("isAuth", "2");
				if (MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_API).count(query) != 0) {
					/*
					 * 关联白名单表查询对应ip的资源
					 */
					query.remove("isAuth");
					// 返回一个不需要鉴权的object实体,查询白名单表
					BasicDBObject object = (BasicDBObject) MongoDBUtil
							.getColl(InnerConstants.COLL_ACCESS_CTRL_WHITELIST).findOne(query);
					if (object != null) {
						String ip = object.getString("ip");
						List<String> ipList = Arrays.asList(ip.split(";"));
						String currentIp = getIpAddr(request);
						logger.info("requestIp: "+currentIp);
						if (ipList.contains(currentIp)) {
							// access_ctrl_whitelist中是否包含请求的ip地址,如果包含则设置iPWhite字段
							request.setAttribute("iPWhite", "true");
						}
					}
				}
			}
			return true;
		}
		return true;
	}
}