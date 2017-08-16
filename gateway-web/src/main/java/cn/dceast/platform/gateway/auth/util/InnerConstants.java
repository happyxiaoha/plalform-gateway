package cn.dceast.platform.gateway.auth.util;

public interface InnerConstants {

	
	
	/*
	 * cookie中ticket的名称
	 */
	public static final String COOKIE_TICKET = "ticket";

	
	/*
	 * cookie中token的名称
	 */
	public static final String COOKIE_TOKEN="microToken";
	
	
	
	/*
	 * 在test_pass_auth中的url将跳过执行鉴权链
	 */
	public static final String COLL_TEST_PASS_AUTH="test_pass_auth";
	/*
	 * IP、appkey、api黑名单定义表。网关层使用此表来做黑名单过滤
	 */
	public static final String COLL_ACCESS_CTRL_BLACKLIST="access_ctrl_blacklist";
	/*
	 * 白名单表，内部api调用，关联service_api表 isAuth=2
	 */
	public static final String COLL_ACCESS_CTRL_WHITELIST="access_ctrl_whitelist";
	/*
	 * 统计api接口被调用次数。此表数据由网关写入
	 */
	public static final String COLL_SERVICE_CALL_STATISTICS = "service_call_statistics";
	/*
	 * 服务接口管理  参数表
	 */
	public static final String COLL_SERVICE_API="service_api";
	/*
	 * 管理调用者可调用的api接口。
	 */
	public static final String COLL_SERVICE_CALL="service_call";
	/*
	 * 微服务serviceUrl和ticket和token的关系宝
	 */
	public static final String COLL_TICKET_TOKEN="service_ticket_token";
	/*
	 * 新建站点初始化表,城市站点和代理用户的对应关系表
	 */
	public static final String COLL_CITYUSERMAPPING="cityUserMapping";
	/*
	 * 测试表
	 */
	public static final String COLL_TESTMANAGER="testManager";
	/*
	 * 存储调用的调用公私钥信息
	 */
	public static final String COLL_SERVICE_KEY="service_key";
	public static final String COLL_API_DAILY_REQUEST_STATICS = "api_daily_request_statics";
	public static final String COLL_ORDER_RULE_LIST_HISTORY = "order_rule_list_history";
	public static final String COLL_ORDER_RULE_LIST = "order_rule_list";
	public static final String COLL_SERVICE_CALL_LOG = "service_call_log";
	
	public static final String COLL_SERVICE="service";
	public static final String COLL_API="api";
	public static final String COLL_COMPONENT_LIST ="component_list";//废弃
	public static final String COLL_USER_PRODUCT = "user_product";//废弃
	
	public static final String RESOURCETYPE_API = "01";
	public static final String RESOURCETYPE_DATA = "02";
	public static final String RESOURCETYPE_MICROSVC = "03";
	public static final String RESOURCETYPE_SAAS = "04";
	
	public static final String CHARRULETYPE_TRY = "0";
	public static final String CHARRULETYPE_TIME = "1";
	public static final String CHARRULETYPE_COUNT = "2";
	
	
	/*
	 * 返回信息
	 */
	public static final String RETURN_CODE_SUCC = "000000";
	public static final String RETURN_CODE_FAIL = "999999";
	//套餐用完
	public static final String RETURN_CODE_USEUP = "900001";
	
	public static final String CREDIT_INQUIRY = "credit_inquiry";
	public static final String BUSINESS_PEDIGREE ="business_pedigree";
	public static final String FINANCIAL_DIAGNOSIS = "financial_diagnosis";
	public static final String BUSINESS_DIG ="business_dig";
	public static final String MONITOR="monitor";
	
	
	
	
	
}
