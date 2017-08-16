package cn.dceast.platform.gateway.auth.filter;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.dceast.platform.gateway.auth.data.entity.App;
import cn.dceast.platform.gateway.auth.data.entity.Appkey;
import cn.dceast.platform.gateway.auth.data.entity.CallerRequestInfo;

/**
 * 封装了RequestInfo中部分方法
 * 
 * @author liycq
 */
public abstract class AuthFilter {

	public abstract boolean doFilter(HttpServletRequest request, HttpServletResponse response) throws Exception;

	/**
	 * 获取请求的ip地址
	 * 
	 * @param request
	 * @return
	 */
	protected String getIpAddr(HttpServletRequest request) {
		return RequestInfo.getIpAddr(request);
	}

	/**
	 * 获取request_url, 包括context和parameters
	 * 
	 * @param request
	 * @return
	 */
	protected String getUri(HttpServletRequest request) {
		return RequestInfo.getUri(request);
	}

	/**
	 * 比如www.baidu.com/phone/getPhone这种,则return "/phone/getPhone"
	 * 
	 * @param request
	 * @return
	 */
	protected String getUriNoParams(HttpServletRequest request) {
		return RequestInfo.getUriNoParams(request);
	}

	protected String getAppName(HttpServletRequest request) {
		return RequestInfo.getAppName(request);
	}

	protected String getApi(HttpServletRequest request) {
		return RequestInfo.getApi(request);
	}

	protected String getAppkey(HttpServletRequest request) {
		return RequestInfo.getAppkey(request);
	}

	/**
	 * 查库获取Appkey信息实体类
	 * 详见:FilterMatcher.getAppkey(appkey)方法
	 * @param request
	 * @return
	 */
	protected Appkey getAppkeyInfo(HttpServletRequest request) {
		return RequestInfo.getAppkeyInfo(request);

	}

	// protected Api getApiInfo(HttpServletRequest request){
	// return RequestInfo.getApiInfo(request);
	// }

	/**
	 * 根据request字段或配置中心返回日最大访问次数字段
	 * 
	 * @param request
	 * @return
	 */
	protected Integer getApiDailyLimitCount(HttpServletRequest request) {
		return RequestInfo.getApiDailyLimitCount(request);
	}

	protected App getAppInfo(HttpServletRequest request) {
		return RequestInfo.getAppInfo(request);
	}

	protected String getHttpContentType(HttpServletRequest request) {
		return RequestInfo.getHttpContentType(request);
	}

	protected String getUserAgent(HttpServletRequest request) {
		return RequestInfo.getUserAgent(request);
	}

	protected void setErrorMessageOfJson(HttpServletRequest request, HttpServletResponse response, String errCode) {
		request.setAttribute("errCode", errCode);

	}

	protected void setErrorMessageOfJson(HttpServletRequest request, HttpServletResponse response, String errCode,
			String extraMessage) {
		request.setAttribute("errCode", errCode);
		request.setAttribute("extraMessage", extraMessage);

	}

	/**
	 * 查询service_api表,如果这个url没有isAuth1的且没有isAuth2的，则返回true...需校验appkey
	 * @param request
	 * @return
	 */
	protected boolean validateInvoke(HttpServletRequest request) {
		return RequestInfo.validateInvoke(request);
	}

	/**
	 * 从request中取得调用者信息、API信息、http请求信息封装成
	 * {@link cn.dceast.platform.gateway.auth.data.entity.CallerRequestInfo}
	 * 对象并返回
	 *
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public CallerRequestInfo getCallerRequestInfo(HttpServletRequest request) throws Exception {

		Map<String, String> reourceMap = FilterMatcher.getParameterFromRequest(request);
		String resourceId = reourceMap.get("resourceId");
		String resourceType = reourceMap.get("resourceType");

		String callerIP = getIpAddr(request);// 调用者IP
		String uri = getUriNoParams(request);

		CallerRequestInfo info = new CallerRequestInfo(uri, null, new Date(), callerIP);
		info.setResourceId(resourceId);
		info.setResourceType(resourceType);
		info.setFlownum(String.valueOf(request.getAttribute("flownum")));
		Appkey appkeyInfo = getAppkeyInfo(request);
		if (appkeyInfo != null) {
			info.setUserId(appkeyInfo.getUserId());
			info.setAppKey(appkeyInfo.getAppkey());
			info.setCallerName(appkeyInfo.getOwnerName());
		} else {
			info.setAppKey(getAppkey(request));
		}
		String areacode = FilterMatcher.getAreacode(request,false);
		info.setAreacode(areacode);

		info.setStatus("ok");
		info.setCode("200");
		info.setMessage("success");
		info.setResult("success");
		return info;
	}

	/**
	 * 测试
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("/1/2/3/4".substring(1));
	}
}
