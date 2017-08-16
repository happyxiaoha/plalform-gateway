package cn.dceast.platform.gateway.auth.rest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.BasicDBObject;

import cn.dceast.platform.gateway.auth.filter.FilterChain;
import cn.dceast.platform.gateway.auth.filter.RequestInfo;
import cn.dceast.platform.gateway.auth.util.InnerConstants;

/**
 * 鉴权类
  * @ClassName: AuthResource
  * @Description: TODO
  * @author Cobot-lych
  * @date 2017年3月16日 下午9:33:00
  *
  */
@RestController
public class AuthResource {
	private Logger logger = LoggerFactory.getLogger(AuthResource.class);

	/**
	 * auth鉴权
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@RequestMapping(value="/auth", method={RequestMethod.POST, RequestMethod.GET})
	public void auth(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		long startTime=System.currentTimeMillis();
		logger.info(String.format("The request url is '%s' and the request ip is '%s'!", getRequestUrl(request),RequestInfo.getIpAddr(request)));
		
		try{
			//走校验链
			FilterChain.doFilter(request, response);
		}catch(Exception e){
			e.printStackTrace();
		}
		long endTime=System.currentTimeMillis();
		logger.info(String.format("GateWay is return httpStatus:%s。Filter cost time is %s",response.getStatus(),(endTime-startTime)));
		logger.info("=================================This is one request======================================");
	}
	
	/**
	 * 获取请求IP地址
	 * 已废弃
	 * @param request
	 * @return
	 */
	public static String getRequestIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
		ip = request.getHeader("Proxy-Client-IP");
		}
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
		ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
		ip = request.getRemoteAddr();
		}
		return ip;
	}
	
	/**
	 * 获取请求url
	 * @param request
	 * @return
	 */
	public static String getRequestUrl(HttpServletRequest request){
		// uri格式：/appName/api/...../xx?pram1=xxxx
		String uri=request.getHeader("X-Original-URI");
		
		return uri;
	}
	
}
