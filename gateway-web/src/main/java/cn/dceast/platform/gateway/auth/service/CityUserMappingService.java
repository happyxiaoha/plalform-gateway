package cn.dceast.platform.gateway.auth.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;

import com.mongodb.BasicDBObject;

public class CityUserMappingService {

	private static Logger logger = LoggerFactory.getLogger(CityUserMappingService.class);

	
	
	
	
	
	
	/**
	 * 根据形参查询出cityUserMapping表中Value
	 * @param areacode
	 * @param appKey
	 * @param abbr_citycode
	 * @return
	 */
	public static Map<String, String> getValueFromCityUserMapping(String areacode, String appKey, String abbr_citycode) {
		try {
			BasicDBObject query = new BasicDBObject();
			if (areacode != null)
				query.append("areacode", areacode);
			if (appKey != null)
				query.append("appKey", appKey);
			 if(abbr_citycode!=null)
			 query.append("abbr_citycode", abbr_citycode);
			logger.info("areacode: " + areacode + " appKey: " + appKey + " abbr_citycode:" + abbr_citycode);
			BasicDBObject basicDBObject = (BasicDBObject) MongoDBUtil.getColl(InnerConstants.COLL_CITYUSERMAPPING)
					.findOne(query);
			if (basicDBObject != null) {
				Map<String, String> returnMap = new HashMap();
				for (Map.Entry<String, Object> map : basicDBObject.entrySet())
					returnMap.put(map.getKey(), String.valueOf(map.getValue()));
				return returnMap;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("根据areacode查询value异常", e);
		}
		return null;
	}

	
	
	
	
	
}
