package cn.dceast.platform.gateway.auth.filter.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xml.rpc.processor.modeler.j2ee.xml.deploymentExtensionType;

import cn.dceast.platform.gateway.auth.data.entity.Appkey;
import cn.dceast.platform.gateway.auth.filter.AuthFilter;
import cn.dceast.platform.gateway.auth.filter.FilterMatcher;
import cn.dceast.platform.gateway.auth.filter.FilterResponseMessage;
import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.UrlUtil;
import cn.dceast.platform.gateway.common.util.SignatureUtil;

/**
 * 鉴权_appkey_签名验证
 * 
 * @author liycq
 *
 */
public class AppKeyAuthFilterImpl extends AuthFilter {
	private Logger logger = LoggerFactory.getLogger(AppKeyAuthFilterImpl.class);

	@Override
	public boolean doFilter(HttpServletRequest request, HttpServletResponse response) {
		logger.info("enter AppKeyAuthFilterImpl");

		try {
			List<String> resourceList = FilterMatcher.getSecondDomainList(request);
			String resourceType = resourceList.get(1);
			if (InnerConstants.RESOURCETYPE_MICROSVC.equals(resourceType)) {
				// 微服务不走鉴权_appkey_签名验证
				return true;
			}
		} catch (Exception e2) {
			e2.printStackTrace();
		}

		// 拿到request中authorization:
		// dc:E1A4274CCF2151CF:M2MzNDQ1NTQwYWMwOTBlZTc0YTg1ZTkyYmUzMjc0NDFjMTE2ZWZmOQ=="中的第二项
		String appkey = getAppkey(request);
		Appkey appkeyObject = getAppkeyInfo(request);
		if (appkeyObject == null) {
			setErrorMessageOfJson(request, response, FilterResponseMessage.CODE_102102);
			logger.info(String.format("The appkey %s is not a valid key!", appkey));
			return false;
		}

		/**
		 * 2. 鉴权
		 */
		///////// 获取参数及参数解码
		String tempUserParams = UrlUtil.getParams(getUri(request));
		if (tempUserParams != null) {
			try {
				tempUserParams = URLDecoder.decode(UrlUtil.getParams(getUri(request)), "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
				setErrorMessageOfJson(request, response, FilterResponseMessage.CODE_600102);
				return false;
			}
		}
		/////////

		// user-aprams为自定义头,nginx中没传.
		String userParams = request.getHeader("user-params") == null ? tempUserParams
				: request.getHeader("user-params");
		//user-date为自定义头,nginx中没传
		String date = request.getHeader("user-date") == null ? UrlUtil.getValue4KeyFromURL(getUri(request), "user-date")
				: request.getHeader("user-date");
		//client端传入的
		String authorization = request.getHeader("authorization") == null
				? UrlUtil.getValue4KeyFromURL(getUri(request), "authorization") : request.getHeader("authorization");

		///////// 生成sData
		String sData = null;
		try {
			sData = SignatureUtil.buildSignData(userParams, date);
		} catch (Exception e) {
			String extraMessage = "The params [user-date] is not valid!";
			setErrorMessageOfJson(request, response, FilterResponseMessage.CODE_102104, extraMessage);
			logger.info(extraMessage);
			return false;
		}
		///////// --生成sData

		// 生成签名
		String sign = SignatureUtil.buildSignature(appkey, appkeyObject.getSecretkey(), sData);

		// 比对client端sign和server端sign
		if (!sign.equals(authorization)) {
			logger.info("sign:" + sign + " authorization: " + authorization);
			logger.info("userParams: " + userParams + " |user-date: " + date + " |appkey:" + appkey + " |secretKey: "
					+ appkeyObject.getSecretkey());
			setErrorMessageOfJson(request, response, FilterResponseMessage.CODE_102104);
			logger.error("The signature is not valid!");
			return false;
		}
		return true;
	}
	
	
	public static void main(String[] args){
		String temp = SignatureUtil.buildSignData(null, "20170417163319094");
		System.out.println(temp);
	}
	
	
}