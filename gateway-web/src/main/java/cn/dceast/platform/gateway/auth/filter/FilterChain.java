package cn.dceast.platform.gateway.auth.filter;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import cn.dceast.platform.common.BusinessException;
import cn.dceast.platform.gateway.auth.AppConfig;
import cn.dceast.platform.gateway.auth.data.entity.Appkey;
import cn.dceast.platform.gateway.auth.data.entity.CallerRequestInfo;
import cn.dceast.platform.gateway.auth.data.entity.FilterModel;
import cn.dceast.platform.gateway.auth.data.entity.Message;
import cn.dceast.platform.gateway.auth.executor.task.UserInfoService;
import cn.dceast.platform.gateway.auth.filter.impl.CacheFilter;
import cn.dceast.platform.gateway.auth.rest.AuthResource;
import cn.dceast.platform.gateway.auth.service.CityUserMappingService;
import cn.dceast.platform.gateway.auth.service.ServiceCallLogService;
import cn.dceast.platform.gateway.auth.util.ApplicationUtil;
import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;
import cn.dceast.platform.gateway.auth.util.UrlUtil;

import com.alibaba.fastjson.JSON;
import com.digitalchina.resttemplate.ribbon.retryable.RetryableLoadbalancedRestTemplateUtil;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext.Basic;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

/**
 * @author liycq
 *
 */
@Component
public class FilterChain {

	private static Logger logger = LoggerFactory.getLogger(FilterChain.class);
	public static final String FILTER_PACKAGE = "cn.dceast.platform.gateway.auth.filter.impl";

	private static RetryableLoadbalancedRestTemplateUtil restTemplate;

	@Autowired
	@Qualifier("retryableLoadbalancedRestTemplateUtil")
	public void setRetryableLoadbalancedRestTemplateUtil(RetryableLoadbalancedRestTemplateUtil restTemplateUtil) {
		FilterChain.restTemplate = restTemplateUtil;
	}

	private static List<AuthFilter> chains = new CopyOnWriteArrayList();

	/**
	 * 初始化
	 */
	public static void init() {
		chains.clear();
		// 解析handlerFilterApplication.xml
		List<FilterModel> listModel = loadHandlerFilter();
		buildFilterChain(listModel);

	}

	/**
	 * 解析handlerFilterApplication.xml
	 */
	public static List<FilterModel> loadHandlerFilter() {
		List<FilterModel> listModel = new ArrayList();
		InputStream stream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("handlerFilterApplication.xml");

		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(stream);
			Element employees = document.getRootElement();
			List<Element> steps = employees.elements();
			for (Element step : steps) {
				FilterModel model = new FilterModel();
				List<DefaultAttribute> attr = step.attributes();
				for (DefaultAttribute e : attr) {
					model.getStep().put(e.getName(), e.getValue());
				}
				List<Element> substeps = step.elements();
				for (Element substep : substeps) {
					Map<String, String> map = new HashMap();
					List<DefaultAttribute> subAttr = substep.attributes();
					for (DefaultAttribute e : subAttr) {
						map.put(e.getName(), e.getValue());
					}
					model.getSubStep().add(map);
				}
				listModel.add(model);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return listModel;
	}

	/**
	 * 无需鉴权过滤器链 设置鉴权链
	 * 
	 * @return
	 */
	public static List<AuthFilter> buildFilterChain(List<FilterModel> listModel) {

		try {
			if (!listModel.isEmpty()) {
				for (FilterModel list : listModel) {
					List<Map<String, String>> subList = list.getSubStep();
					for (Map map : subList) {
						if (map.get("isskip") != null && !Boolean.valueOf(map.get("isskip").toString())) {
							String sClass = FILTER_PACKAGE + "." + map.get("classname");
							logger.info("AuthChain Add filter:" + sClass);
							AuthFilter filter = (AuthFilter) Class.forName(sClass).newInstance();
							chains.add(filter);
						}
					}
				}
			}
		} catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}

		return chains;
	}

	/**
	 * 实际鉴权代码
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	public static void doFilter(HttpServletRequest request, HttpServletResponse response) throws Exception {

		// 生成流水号
		String flownum = UUID.randomUUID().toString().replaceAll("-", "");
		request.setAttribute("flownum", flownum);
		// --生成流水号

		response.setHeader("errmsg", "");
		response.setStatus(200);

		//////////////////////////
		AppConfig ac = (AppConfig) ApplicationUtil.getSingleton().getContext().getBean("appConfig");
		BasicDBObject testPassAuth = new BasicDBObject().append("url", RequestInfo.getUriNoParams(request));
		// 查询test_pass_auth表(用于测试)
		DBCursor testPassAuthdbCursor = MongoDBUtil.getColl(InnerConstants.COLL_TEST_PASS_AUTH).find(testPassAuth);
		if (testPassAuthdbCursor.size() != 0) {
			response.setHeader("trueprotocol", "http://");
			// 跳过鉴权链
			if (logger.isErrorEnabled()) {
				logger.error("skip auth chains");
				CacheFilter cacheFilter = new CacheFilter();
				cacheFilter.doFilter(request, response);
			}
		} else {// 鉴权
			///////////////////////// 微服务相关
			String skipAuthFileType = ac.getSkipAuth();
			String urlNoParams = RequestInfo.getUriNoParams(request);
			String lastContext = UrlUtil.getLastContext(urlNoParams);
			String[] tempArray = lastContext.split("\\.");
			String tempFileType = tempArray[tempArray.length - 1];
			boolean isNotAuthFile = false;
			isNotAuthFile = skipAuthFileType.contains(tempFileType);
			////////////////////////

			// 遍历鉴权链
			for (AuthFilter filter : chains) {
				boolean isFit = filter.doFilter(request, response);
				if (!isFit) {
					// 如果某个filter返回false,则鉴权失败, 记录鉴权失败信息
					logger.info(filter.getClass().getName() + " auth fail!");
					setLogAndMirror(request, response);
					break;
				}
				// 如为context为/lib/js/app.js此类
				if (response.getHeader("serviceContext") != null && isNotAuthFile) {
					// 跳过preHandlerFilter后所有鉴权
					break;
				}
				// if (response.getHeader("saasCacheValue") != null) {
				// break;
				// }
				// 如果在白名单中,则不再执行后面的所有鉴权
				if ("true".equals(request.getAttribute("iPWhite"))) {
					break;
				}
				if ("true".equals(request.getAttribute("isTestAppKey"))) {
					//测试账号鉴权放行
					break;
				}
			}
		}
		/////////////////////////

		//////////////////////// 短信服务和支付服务设置useraccount
		String requestUrl = AuthResource.getRequestUrl(request);
		String url = UrlUtil.getUriNoParams(requestUrl);
		// logger.info("for query service_api, url: "+url);
		BasicDBObject basicDBObject = new BasicDBObject("url", url);
		// 查询
		DBCursor dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_API).find(basicDBObject);
		if (dbCursor.size() != 0) {
			logger.info("mscx_pay_service or mscx_sms_service function url: " + url);
			while (dbCursor.hasNext()) {
				BasicDBObject tempOb = (BasicDBObject) dbCursor.next();
				String targetDomain = tempOb.getString("upstream");
				if (targetDomain.contains("mscx_pay_service") || targetDomain.contains("mscx_sms_service")) {
					UserInfoService userInfoService = new UserInfoService(restTemplate, ac.getDataCenterEurekAaddress(),
							null);
					Appkey appkey = RequestInfo.getAppkeyInfo(request);
					response.setHeader("useraccount", userInfoService.getuserName(appkey.getUserId()));
				}
			}
		}
		/////////////////////// --短信服务和支付服务设置useraccount

		response.setHeader("flownum", (String) request.getAttribute("flownum"));
		response.setHeader("orderDetailId",
				request.getAttribute("orderDetailId") == null ? null : (String) request.getAttribute("orderDetailId"));
		String realHost = null;
		realHost = RequestInfo.getUpstreamServer(request, response);
		if (response.getHeader("realHost") == null) {
			// 缓存命中时，即realHost对应value有值，则不再设置realHost
			response.setHeader("realHost", realHost);
		}
		String now_areacode = FilterMatcher.getAreacode(request, false);
		response.setHeader("areacode", now_areacode);
		///////////// 微服务返回信息
		List<String> subDomainList = FilterMatcher.getSecondDomainList(request);
		if (subDomainList.get(1).equals(InnerConstants.RESOURCETYPE_MICROSVC)) {
			setMicroServiceUniqueInfo(request, response);
		}
		///////////// --微服务返回信息

		
		
		if (!"".equals(realHost)) {// realHost非ip地址情况
			// 检查是否推广类，微服务中，类似/lib/appjs.js这种请求时，isPromote返回为false
			boolean isPromote = FilterMatcher.checkResourcePromote(request);
			if (isPromote) {// 是推广类
				// 证明接收此次请求的网关为中转站点
				response.setHeader("isPromote", "true");
				List<String> domainList = FilterMatcher.getSecondDomainList(realHost);
				// 根据service_api中upstream中的server获取sourcecity
				String upstreamSeverAreacode = FilterMatcher.getUpstreamServerAreacode(request);
				Map<String, String> returnMap = CityUserMappingService
						.getValueFromCityUserMapping(upstreamSeverAreacode, null, null);
				// header中的authorization替换request中的authorization
				response.setHeader("authorization",
						replaceAppkey(request, returnMap == null ? null : returnMap.get("appKey")));
			}
		}
		///////////////////////////

		logger.error(String.format("The errMsg is [%s]", response.getHeader("errMsg")));
		logger.info(String.format("The flownum is [%s]", response.getHeader("flownum")));

	}

	/**
	 * 在response中设置nginxresourcetype Appkey Context userId这四个值
	 */
	public static void setMicroServiceUniqueInfo(HttpServletRequest request, HttpServletResponse response) {
		// 设置userId
		String userId = RequestInfo.getUserId(request);
		logger.info("setMicroServiceUniqueInfo() userId from RequestInfo.getUserId(): " + userId);
		response.setHeader("userId", userId);

		//////////////////// 根据userId查库获取Appkey
		String appKey = null;
		BasicDBObject service_key = new BasicDBObject("userId", userId);
		DBCursor service_key_cursor = MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_KEY).find(service_key);
		while (service_key_cursor.hasNext()) {
			BasicDBObject ob = (BasicDBObject) service_key_cursor.next();
			appKey = ob.getString("apiKey");
			break;
		}
		response.setHeader("appKey", appKey);
		//////////////////////////////

		// 获取和设置Context
		String url = RequestInfo.getUriNoParams(request);
		String firstContext = UrlUtil.getFirstContext(url);
		response.setHeader("context", firstContext);

		// 设置nginxresourcetype
		response.setHeader("nginxresourcetype", InnerConstants.RESOURCETYPE_MICROSVC);

	}

	/**
	 * 设置鉴权失败返回信息
	 * 
	 * @param request
	 * @param response
	 */
	private static void setErrorResponse(HttpServletRequest request, HttpServletResponse response) {
		String errCode = (String) request.getAttribute("errCode");
		String extraMessage = (String) request.getAttribute("extraMessage");

		PrintWriter out = null;
		try {
			String errMsg = FilterResponseMessage.getMessage(errCode);

			Message message = new Message();
			message.setCode(errCode);

			if (!StringUtils.isEmpty(extraMessage)) {
				errMsg = errMsg + " {detail:" + extraMessage + "}";
			}
			message.setMessage(errMsg);
			message.setStatus("ERROR");

			response.setHeader("errmsg", JSON.toJSONString(message));

		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	/**
	 * 鉴权失败记录流水
	 * 就是调用了ServiceCallLogService.updateCallLog(callerRequestInfo)这个方法，更新logb表
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	private static void setLogAndMirror(HttpServletRequest request, HttpServletResponse response) throws Exception {

		// 鉴权失败记录流水
		response.setStatus(401);
		setErrorResponse(request, response);

		String uri = RequestInfo.getUriNoParams(request);
		String callerIP = RequestInfo.getIpAddr(request);// 调用者IP
		CallerRequestInfo callerRequestInfo = new CallerRequestInfo(uri, null, new Date(), callerIP);
		Appkey appkeyInfo = RequestInfo.getAppkeyInfo(request);
		if (null != appkeyInfo) {
			callerRequestInfo.setAppKey(appkeyInfo.getAppkey());
			callerRequestInfo.setCallerName(appkeyInfo.getOwnerName());
			callerRequestInfo.setUserId(appkeyInfo.getUserId());
		}

		Map<String, String> resourceMap = FilterMatcher.getParameterFromRequest(request);

		callerRequestInfo.setResourceType(resourceMap.get("resourceType"));
		callerRequestInfo.setResourceId(resourceMap.get("resourceId"));
		String code = (String) request.getAttribute("errCode");
		callerRequestInfo.setCode(code);
		String msg = code == null
				? request.getAttribute("extraMessage") == null ? null
						: String.valueOf(request.getAttribute("extraMessage"))
				: FilterResponseMessage.message.get(code);
		callerRequestInfo.setStatus("error");
		callerRequestInfo.setResult("failed");
		callerRequestInfo.setMessage(msg);
		callerRequestInfo.setFlownum((String) request.getAttribute("flownum"));
		String areacode = FilterMatcher.getAreacode(request, false);
		callerRequestInfo.setAreacode(areacode);

		ServiceCallLogService.updateCallLog(callerRequestInfo);

	}

	/**
	 * 如果请求中有authorization字段，则用新的appKey替换掉旧的
	 * 
	 * @param request
	 * @param new_appKey
	 * @return
	 * @throws Exception
	 */
	public static String replaceAppkey(HttpServletRequest request, String new_appKey) throws Exception {
		String authorization = request.getHeader("authorization") == null
				? UrlUtil.getValue4KeyFromURL(request.getHeader("X-Original-URI"), "authorization")
				: request.getHeader("authorization");
		List<String> secondDomainList = FilterMatcher.getSecondDomainList(request);

		if (StringUtils.isEmpty(authorization)) {
			if (!secondDomainList.get(1).equals(InnerConstants.RESOURCETYPE_MICROSVC)) {
				// 不是微服务
				return null;
			} else {
				// 微服务，用于第二个站点获取appKey
				return "xxx:" + new_appKey + ":xxx";
			}
		}

		String temp[] = authorization.split(":");
		if (temp.length != 3) {
			return null;
		}
		StringBuffer sb = new StringBuffer("");
		sb.append(temp[0]).append(":").append(new_appKey).append(":").append(temp[2]);
		return sb.toString();
	}

	public static void main(String[] args) {
		System.out.println(StringUtils.isEmpty(null));
	}

}
