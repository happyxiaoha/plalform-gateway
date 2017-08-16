package cn.dceast.platform.gateway.auth.filter.impl;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;

import cn.dceast.platform.gateway.auth.filter.AuthFilter;
import cn.dceast.platform.gateway.auth.filter.FilterMatcher;
import cn.dceast.platform.gateway.auth.filter.RequestInfo;
import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;

/**
 *  测试数据
 */
public class TestDataFilterImpl extends AuthFilter{
	private Logger logger = LoggerFactory.getLogger(TestDataFilterImpl.class);
	
	@Override
	public boolean doFilter(HttpServletRequest request,
			HttpServletResponse response) throws Exception{
		logger.info("enter TestDataFilterImpl");
		String areacode = FilterMatcher.getAreacode(request,false);
		String appKey= RequestInfo.getAppkey(request);
		
		BasicDBObject query=new BasicDBObject("appKey", appKey).append("areacode", areacode);
		BasicDBObject dbObject = (BasicDBObject) MongoDBUtil.getColl(InnerConstants.COLL_TESTMANAGER).findOne(query);
		if(dbObject!=null){
			String ips = dbObject.getString("ip");
			if(ips!=null){
				String currentIp = RequestInfo.getIpAddr(request);
				List<String> ipList = Arrays.asList(ips.split(";"));
				if(ipList.contains(currentIp)){
					logger.info("test ip : ip [ "+ips+" ]");
					request.setAttribute("isTestAppKey", "true");
				}
			}
		}
		return true;
	}

}
