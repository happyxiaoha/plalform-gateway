package cn.dceast.platform.gateway.auth.filter;

import java.util.HashMap;
import java.util.Map;

/**
 * 只支持英文消息
 * @author zhang
 *
 */
public class FilterResponseMessage {
	public static Map<String, String> message=new HashMap<String, String>();
	
	public final static String CODE_100101="100101";
	public final static String CODE_100102="100102";
	public final static String CODE_100103="100103";
	
	public final static String CODE_101101="101101";
	public final static String CODE_101102="101102";
	public final static String CODE_101103="101103";
	public final static String CODE_101104="101104";
	public final static String CODE_101105="101105";
	public final static String CODE_101106="101106";
	public final static String CODE_101107="101107";
	public final static String CODE_101108="101108";
	
	public final static String CODE_102101="102101";
	public final static String CODE_102102="102102";
	public final static String CODE_102103="102103";
	public final static String CODE_102104="102104";
	
	public final static String CODE_103101="103101";

	public final static String CODE_104101="104101";
	public final static String CODE_104102="104102";

	public final static String CODE_300101="300101";

	public final static String CODE_300102="300102";
	
	public final static String CODE_400101="400101";
	public final static String CODE_400102="400102";
	public final static String CODE_400103="400103";
	public final static String CODE_400104="400104";

	public final static String CODE_500100="500100";
	public final static String CODE_500101="500101";
	public final static String CODE_500102="500102";
	public final static String CODE_500103="500103";

	public final static String CODE_600101="600101";
	public final static String CODE_600102="600102";
	public final static String CODE_600103="600103";
	
	static{
		message.put(CODE_100101, "API cannot be resolved");
		message.put(CODE_100102, "API cannot be NULL");
		message.put(CODE_100103, "API does not exist");
		
		message.put(CODE_101101, "API is in blacklist");
		message.put(CODE_101102, "dceast-appkey is in blacklist");
		message.put(CODE_101103, "IP is in blacklist");
		message.put(CODE_101104, "User call overrun per day");
		message.put(CODE_101105, "User call overrun per second");
		message.put(CODE_101106, "app call overrun per second");
		message.put(CODE_101107, "IP is not in whitelist");
		message.put(CODE_101108, "API is not exist");

		message.put(CODE_102101, "appKey cannot be NULL");
		message.put(CODE_102102, "appKey does not exist");
		message.put(CODE_102103, "You cannot access this API");
		message.put(CODE_102104, "Signature cannot be resolved");
		
		message.put(CODE_103101, "Access denied");

		message.put(CODE_104101,"Buy this product first");
		message.put(CODE_104102,"You are out of money");
		
		message.put(CODE_300101, "Internal error");

		message.put(CODE_300102, "writeSuccStatistics error");
		
		message.put(CODE_400101, "CallAPIFailedStatisticServlet flownu is null");
		
		message.put(CODE_400102, "userId is null!");
		message.put(CODE_400103, "the second domain is incorrect,resourceType is null!");
		message.put(CODE_400104, "the second domain is incorrect,resourceId is null!");
		
		message.put(CODE_500100, "token is null");
		message.put(CODE_500101, "token out-of-date");
		message.put(CODE_500102, "ticket is out-of-date");
		message.put(CODE_500103, "token is not matching with userId");
		
		message.put(CODE_600101, "the firstDomain cannot find the mapping areacode");
		message.put(CODE_600102, "the arguments of url is not utf-8,UnsupportedEncodingException");
		message.put(CODE_600103, "ununiform secondDomain");
	}
	
	public static String getMessage(String code){
		return message.get(code);
	}
	
}
