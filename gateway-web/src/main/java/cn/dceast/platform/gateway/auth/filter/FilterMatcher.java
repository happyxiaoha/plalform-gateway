package cn.dceast.platform.gateway.auth.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.dceast.platform.gateway.auth.AppConfig;
import cn.dceast.platform.gateway.auth.data.entity.App;
import cn.dceast.platform.gateway.auth.data.entity.Appkey;
import cn.dceast.platform.gateway.auth.util.ApplicationUtil;
import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;
import cn.dceast.platform.gateway.auth.util.UrlUtil;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.QueryOperators;

/**
 * 主要做黑白名单、appkey等比较工作
 * 
 * @author zhang
 *
 */
public class FilterMatcher {

	private static Logger logger = LoggerFactory.getLogger(FilterMatcher.class);

	/**
	 * api黑名单是否存在
	 * 
	 * @param appName
	 * @param api
	 * @return
	 */
	public static boolean isExistsBlackOfApi(String appName, String api) {
		BasicDBObject query = new BasicDBObject();
		query.append("type", "api").append("content", getUriFromAppNameAndApi(appName, api));

		return result(query, InnerConstants.COLL_ACCESS_CTRL_BLACKLIST);
	}

	/**
	 * api黑名单是否存在
	 * 
	 * @param uri
	 * @return
	 */
	public static boolean isExistsBlackOfApi(String uri) {
		BasicDBObject query = new BasicDBObject();
		query.append("type", "api").append("content", uri);

		return result(query, InnerConstants.COLL_ACCESS_CTRL_BLACKLIST);
	}

	/**
	 * 生成match结果
	 * 
	 * @param query
	 * @param collection
	 * @return
	 */
	private static boolean result(BasicDBObject query, String collection) {
		long count = MongoDBUtil.getColl(collection).count(query);

		if (count > 0) {
			return true;
		}

		return false;
	}

	private static String getUriFromAppNameAndApi(String appName, String api) {
		if (StringUtils.isEmpty(appName) || StringUtils.isEmpty(api)) {
			return "";
		}

		return "/" + appName + api;

	}

	/**
	 * 公共权限的api是否存在
	 * 
	 * @param appName
	 * @param api
	 * @return
	 */
	// public static boolean isExistsPublicAuthOfApi(String appName,String api){
	// BasicDBObject query=new BasicDBObject();
	// query.append("auth","public")
	// .append("appName", appName)
	// .append("url", api);
	//
	// return result(query, InnerConstants.COLL_SERVICE_API);
	// }

	/**
	 * 用户是不需要鉴权的api记录是否存在
	 * 
	 * @param callerName
	 * @param appName
	 * @param api
	 * @return
	 */
	// public static boolean isExistsAppCallOfCaller(String callerName,String
	// appName,String api){
	// BasicDBObject query=new BasicDBObject();
	// query.append("callerName", callerName)
	// .append("appName", appName)
	// .append("apiUrl", api);
	//
	// return result(query, InnerConstants.COLL_SERVICE_CALL);
	// }

	/**
	 * appkey是否在黑名单中
	 * 
	 * @param appkey
	 * @return
	 */
	public static boolean isExistsBlackOfAppkey(String appkey) {
		BasicDBObject query = new BasicDBObject();
		query.append("type", "appKey").append("content", appkey);

		return result(query, InnerConstants.COLL_ACCESS_CTRL_BLACKLIST);
	}

	/**
	 * IP是否在黑名单中
	 * 
	 * @param ip
	 * @return
	 */
	public static boolean isExistsBlackOfIP(String ip) {
		BasicDBObject query = new BasicDBObject();
		query.append("type", "ip").append("content", ip);

		return result(query, InnerConstants.COLL_ACCESS_CTRL_BLACKLIST);
	}

	/**
	 * IP白名单
	 * 
	 * @param ip
	 * @return
	 */
	// public static boolean isExistsWhiteOfIP(String ip,String apiUri){
	// BasicDBObject query=new BasicDBObject();
	// query.append("ip", ip)
	// .append("apiUri", apiUri);
	//// .append("appName", appName);
	//
	// return result(query, InnerConstants.COLL_ACCESS_CTRL_WHITELIST);
	// }
	/**
	 * 根据appkey查表service_key获取对应的userId apiKey等信息,并封装到Appkey实体中
	 * 
	 * @param appkey
	 * @return
	 */
	public static Appkey getAppkey(String appkey) {
		BasicDBObject query = new BasicDBObject();
		query.append("apiKey", appkey);
		// .append("activeFlag", "1");

		////////////////////
		BasicDBObject basicDBObject = (BasicDBObject) MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_KEY)
				.findOne(query);
		if (basicDBObject != null) {
			Appkey appkeyObject = new Appkey();
			appkeyObject.setAppkey(basicDBObject.getString("apiKey"));
			appkeyObject.setOwnerName(basicDBObject.getString("userName"));
			appkeyObject.setSecretkey(basicDBObject.getString("secretKey"));
			appkeyObject.setUserId(basicDBObject.getString("userId"));
			return appkeyObject;
		}
		////////////////////

		return null;
	}

	/**
	 * 查询api信息
	 * 
	 * @param appName
	 * @param api
	 * @return
	 */
	// public static Api getApi(String appName,String api){
	// BasicDBObject query=new BasicDBObject("url", api);
	//
	// BasicDBObject
	// basicDBObject=(BasicDBObject)MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_API).findOne(query);
	// if(basicDBObject!=null){
	// Api apiObject=new Api();
	// apiObject.setAppName(appName);
	// apiObject.setApiName(basicDBObject.getString("apiName"));
	// apiObject.setUrl(api);
	// apiObject.setMaxCountOfDay(basicDBObject.getInt("maxCountOfDay", -1));
	// apiObject.setNotAuth(basicDBObject.getBoolean("notAuth",false));
	//
	// return apiObject;
	//
	// }
	//
	// return null;
	// }

	public static App getApp(String appName) {
		BasicDBObject query = new BasicDBObject();
		query.append("name", appName).append("offline", new BasicDBObject(QueryOperators.NE, "true"));

		BasicDBObject basicDBObject = (BasicDBObject) MongoDBUtil.getColl(InnerConstants.COLL_SERVICE).findOne(query);

		if (basicDBObject != null) {
			App service = new App();
			service.setName(appName);
			service.setSerType(basicDBObject.getString("serType"));
			service.setUrl(basicDBObject.getString("url"));
			String maxTps = basicDBObject.getString("maxTps");
			if (!StringUtils.isEmpty(maxTps)) {
				service.setMaxTps(Integer.valueOf(maxTps));
			}
			return service;
		}

		return null;
	}

	public static Integer getGatewayInstanceCount() {
		BasicDBObject query = new BasicDBObject("component", "gateway");
		long gatewayCount = MongoDBUtil.getColl(InnerConstants.COLL_COMPONENT_LIST).count(query);
		return (int) gatewayCount;
	}

	/**
	 * @param query
	 * @param field
	 * @return
	 */
	public static BasicDBObject getUserProductInfo(BasicDBObject query, BasicDBObject field) {
		BasicDBObject product = (BasicDBObject) MongoDBUtil.getColl(InnerConstants.COLL_USER_PRODUCT).findOne(query,
				field);
		return product;
	}

	public static int calculateUserProductBalance(BasicDBObject usrProduct) {
		String owner = usrProduct.getString("owner");
		// 每个产品，有m个api需要去检索累加调用次数
		BasicDBList apiList = (BasicDBList) usrProduct.get("productApi");
		BasicDBObject query = new BasicDBObject();
		String[] urlArray;
		String urlStr = "";
		for (Object apiObj : apiList) {
			BasicDBObject api = (BasicDBObject) apiObj;
			urlStr += api.getString("apiUri") + ",";
			query = new BasicDBObject("appName", api.getString("serviceName"));
		}
		urlStr = urlStr.substring(0, urlStr.length() - 1);
		urlArray = urlStr.split(",");
		query.append("callerName", owner).append("apiUrl", new BasicDBObject(QueryOperators.IN, urlArray));
		BasicDBObject field = new BasicDBObject("count", 1).append("failedCount", 1);
		DBCursor dbCursor = null;
		int count = 0, failedCount = 0;
		try {
			dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_CALL_STATISTICS).find(query, field);
			while (dbCursor.hasNext()) {
				BasicDBObject stats = (BasicDBObject) dbCursor.next();
				count += stats.getInt("count", 0);
				failedCount += stats.getInt("failedCount", 0);
			}
		} finally {
			if (dbCursor != null) {
				dbCursor.close();
			}
		}
		int sumCount = usrProduct.getInt("sumCount");
		int sumConsume = count - failedCount;
		return sumCount - sumConsume;
	}

	/**
	 * 查配置中心api日最大限额
	 * 
	 * @param appName
	 *            废弃
	 * @param apiUrl
	 *            废弃
	 * @param callerName
	 * @return
	 */
	public static Integer getApiDailyLimitCount() {
		// BasicDBObject query = new BasicDBObject();
		// query.append("appName", appName);
		// query.append("apiUrl", apiUrl);
		// query.append("callerName", callerName);
		// BasicDBObject limitInfo = (BasicDBObject)
		// MongoDBUtil.getColl("api_daily_request_limit").findOne(query);
		// if(limitInfo == null){
		// return null;
		// }
		// return limitInfo.getInt("limitCount");
		return ((AppConfig) ApplicationUtil.getSingleton().getContext().getBean("appConfig")).apiDailyMaxLimit;
	}

	/**
	 * 查service_api表获取resourceType和resourceId
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public static Map<String, String> getParameterFromRequest(HttpServletRequest request) throws Exception {
		Map<String, String> map = new HashMap();

		List<String> secondDomainList = getSecondDomainList(request);
		String resourceType = secondDomainList.get(1);// resourceType
		String resourceId = null;

		////////////////////////////////// 查service_api表获取resourceId
		if (InnerConstants.RESOURCETYPE_DATA.equals(resourceType)) {
			// data
			// 请求中获取reosurceId
			resourceId = RequestInfo.getParams4ResourceId(request);
			resourceType = InnerConstants.RESOURCETYPE_DATA;
			map.put("resourceId", resourceId);
		} else if (InnerConstants.RESOURCETYPE_MICROSVC.equals(resourceType)) {
			// 微服务
			// 请求中无法获取,从service_api表中获取
			String url = RequestInfo.getUriNoParams(request);
			String firstContext = UrlUtil.getFirstContext(url);
			String areacode = getAreacode(request, false);
			BasicDBObject object = new BasicDBObject("url", firstContext).append("areacode", areacode)
					.append("resourceType", resourceType);
			DBCursor dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_API).find(object);
			if (dbCursor.hasNext()) {
				BasicDBObject stats = (BasicDBObject) dbCursor.next();
				map.put("resourceId", stats.getString("resourceId"));
			}
		} else {
			// api
			// 从service_api表中获取
			String url = RequestInfo.getUriNoParams(request);
			String areacode = getAreacode(request, false);
			BasicDBObject object = new BasicDBObject("url", url).append("areacode", areacode).append("resourceType",
					resourceType);
			DBCursor dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_API).find(object);
			if (dbCursor.hasNext()) {
				BasicDBObject stats = (BasicDBObject) dbCursor.next();
				map.put("resourceId", stats.getString("resourceId"));
			}
		}
		////////////////////////
		map.put("resourceType", resourceType);
		return map;
	}

	/**
	 * 根据reqeust简码查询数据库cityUserMapping表获取区域标识
	 * 
	 * @param request
	 *            请求信息实体类
	 * @isCheckPromote 是否为checkResourcePromote()方法调用如下方法。(此字段暂未启用)
	 * 
	 */
	public static String getAreacode(HttpServletRequest request, boolean isCheckPromote) throws Exception {

		List<String> secondDomain = getSecondDomainList(request);
		BasicDBObject object = null;
		object = new BasicDBObject("abbr_citycode", secondDomain.get(0));
		BasicDBObject dbCursor = (BasicDBObject) MongoDBUtil.getColl(InnerConstants.COLL_CITYUSERMAPPING)
				.findOne(object);
		if (dbCursor != null) {
			// 查cityUserMapping表返回areacode
			return dbCursor.getString("areacode");
		}
		return null;
	}

	/**
	 * 获取二级域名 qg-api.citysdk.cn
	 */
	public static List<String> getSecondDomainList(HttpServletRequest request) throws Exception {

		String host = request.getHeader("host");
		return getSecondDomainList(host);
	}

	/**
	 * 获取二级域名信息 //当为qg-api时,secondDomains[0]为qg, secondeDomain[1]为01
	 */
	public static List<String> getSecondDomainList(String host) throws Exception {

		String[] domains = host.split("\\.");
		// 拆分qg-api这个字符串
		String[] secondDomains = domains[0].split("-");
		if (secondDomains.length != 2) {
			// //如为saas门户http://sp.eastdc.cn:85/zhangsan情况
			// if (secondDomains[0].equals("sp")) {
			// List<String> secondDomainsList1 = new ArrayList();
			// secondDomainsList1.add("sp");
			// secondDomainsList1.add("sp");
			// return secondDomainsList1;
			// }else {
			logger.error("host value: " + host);
			throw new Exception("second domain illegal");
			// }
		}
		// 获取qg-api中的api这项
		String resourceType = secondDomains[1];

		/// 获取配置中心数据
		AppConfig ac = (AppConfig) ApplicationUtil.getSingleton().getContext().getBean("appConfig");
		String apiSecondDomain = ac.getApiSecondDomain();
		String dataSecondDomain = ac.getDataSecondDomain();
		String serviceSecondDomain = ac.getServiceSecondDomain();

		// 设置resourceType
		if (resourceType.equals(apiSecondDomain)) {
			resourceType = InnerConstants.RESOURCETYPE_API;
		} else if (resourceType.equals(dataSecondDomain)) {
			resourceType = InnerConstants.RESOURCETYPE_DATA;
		} else if (resourceType.equals(serviceSecondDomain)) {
			resourceType = InnerConstants.RESOURCETYPE_MICROSVC;
		}

		List<String> secondDomainsList = new ArrayList();
		// 当为qg-api时,secondDomains[0]为qg
		secondDomainsList.add(secondDomains[0]);
		// 当为qg-api时,resourceType为api
		secondDomainsList.add(resourceType);
		return secondDomainsList;
	}

	/*
	 * 根据service_api中upstream中的server获取sourcecity
	 */
	public static String getUpstreamServerAreacode(HttpServletRequest request) throws Exception {

		String areacode = getAreacode(request, false);
		String url = RequestInfo.getUriNoParams(request);
		List<String> secondDomain = getSecondDomainList(request);
		BasicDBObject object = new BasicDBObject("areacode", areacode).append("url", url).append("resourceType",
				secondDomain.get(1));
		BasicDBObject dbCursor = (BasicDBObject) MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_API).findOne(object);
		if (dbCursor != null)
			return dbCursor.getString("sourcecity");
		return null;

	}

	/**
	 * 判断是否为推广类资源
	 */
	public static boolean checkResourcePromote(HttpServletRequest request) throws Exception {
		/////////////////////////
		String areacode = getAreacode(request, true);
		String resultPromote = request.getHeader("isPromote");
		// 来自nginx请求是否标记为推广
		boolean isPromote = false;
		// 来自库中查库是否标记为推广
		boolean isPromoteFromDB = false;
		/////////////////////////
		if (resultPromote != null) {
			isPromote = true;
		}
		String url = RequestInfo.getUriNoParams(request);
		List<String> secondDomain = getSecondDomainList(request);
		BasicDBObject object = new BasicDBObject("areacode", areacode).append("url", url).append("resourceType",
				secondDomain.get(1));
		BasicDBObject dbCursor = (BasicDBObject) MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_API).findOne(object);
		if (dbCursor != null) {
			// areacode与查询出的sourcecity不一致,则是推广类
			if (!areacode.equals(dbCursor.getString("sourcecity"))) {
				isPromoteFromDB = true;
			}
		}

		logger.info("url:" + url + " areacode:" + areacode + " isPromote:" + isPromote + " isPromoteFromDB:"
				+ isPromoteFromDB);
		//////////////////////////////// 四种情况，规则详见:资源推广四种情况分析.xmind
		if (isPromote && isPromoteFromDB) {
			return true;
		} else if (isPromote && !isPromoteFromDB) {
			return false;
		} else if (!isPromote && isPromoteFromDB) {
			return true;
		} else {
			return false;
		}
		////////////////////////////////
	}

}
