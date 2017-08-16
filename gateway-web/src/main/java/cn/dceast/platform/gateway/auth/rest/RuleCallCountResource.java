package cn.dceast.platform.gateway.auth.rest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.QueryOperators;
import com.mongodb.util.Args;
import com.sun.xml.rpc.processor.modeler.j2ee.xml.remoteType;

/**
 * 
 * @author liycq
 *
 */
@RestController
public class RuleCallCountResource {
	private Logger logger = LoggerFactory.getLogger(RuleCallCountResource.class);

	public static SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/*
	 * 根据 秘钥&服务标识&用户标识生成ticket
	 */
	@RequestMapping(value = "/getCallsPerHour", method = { RequestMethod.POST, RequestMethod.GET })
	public String executeTicket(HttpServletRequest request, HttpServletResponse response, String data)
			throws ServletException, IOException {

		if (data == null) {
			logger.error("/getCallsPerHour data == null");
			return "/getCallsPerHour data == null";
		}
		logger.info("enter RuleCallCountResource " + "data value: " + data.toString());
		JSONArray jsonArray = new JSONArray().parseArray(data);
		String resourceId = null;
		String resourceType = null;
		String areacode = null;
		String reportDate = null;
		List<Map<String, String>> orderList = JSONObject.toJavaObject(jsonArray, List.class);
		// 计数数组
		int[] countArrays = initCountArray();

		for (Map<String, String> singleOrder : orderList) {// 仅循环一次
			// 取出值
			resourceId = singleOrder.get("resourceId");
			resourceType = singleOrder.get("resourceType");
			areacode = singleOrder.get("areacode");
			reportDate = singleOrder.get("reportDate");
			// 查询service_call_log表
			BasicDBObject basicDBObject = new BasicDBObject().append("resourceId", resourceId)
					.append("resourceType", resourceType).append("areacode", areacode);
			DBCursor service_call_log_dbCursor = MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_CALL_LOG)
					.find(basicDBObject);
			// 遍历
			if (service_call_log_dbCursor.size() == 0) {
				// 无相关记录
				logger.error("resourceId: " + resourceId + " resourceType: " + resourceType + " areacode: " + areacode
						+ " no any logs.");
				return "resourceId: " + resourceId + " resourceType: " + resourceType + " areacode: " + areacode
						+ " no any logs.";
			}
			while (service_call_log_dbCursor.hasNext()) {
				BasicDBObject tempObject = (BasicDBObject) service_call_log_dbCursor.next();
				/* 依照格式将获取的Date转为String
				 * 库中存储的callTime是ISODate格式，存储的时间比本地实际时间少8个小时。而利用如下api取出callTime值，
				 * mongoDB.api将ISODate日期转为本地实际日期，所以，如下代码逻辑无问题。
				*/
				String callTime = sFormat.format(tempObject.getDate("callTime"));
				if (callTime.contains(reportDate)) {
					int number = Integer.parseInt(callTime.substring(11, 13));
					countArrays[number]++;
				}
			}
			return constructReturnMessage(countArrays, reportDate);
		}
		return null;
	}

	/**
	 * 
	 * @param arrays
	 * @return
	 */
	private String constructReturnMessage(int[] arrays, String reportDate) {
		List<ReturnData> returnDataList = new ArrayList<ReturnData>();
		for (int i = 0; i <= 23; i++) {
			ReturnData returnData = new ReturnData();
			if (i < 10) {
				returnData.setAccessCount(arrays[i]);
				returnData.setReportDateTime(reportDate + " " + "0" + i);
			} else {
				returnData.setAccessCount(arrays[i]);
				returnData.setReportDateTime(reportDate + " " + i);
			}
			returnDataList.add(returnData);
		}
		Map returnMap = new HashMap();
		returnMap.put("result", returnDataList);
		returnMap.put("code", InnerConstants.RETURN_CODE_SUCC);
		returnMap.put("message", "success");
		returnMap.put("status", "ok");
		return new JSONObject().toJSONString(returnMap);
	}

	/**
	 * @return
	 */
	private int[] initCountArray() {
		int[] countArray = null;
		countArray = new int[24];
		return countArray;
	}

	public static void main(String[] args) {
		String xxxxxx = "2017-04-26 04:18:09";

		System.out.println(Integer.parseInt(xxxxxx.substring(11, 13)));
	}

}

class ReturnData {
	private String reportDateTime = null;
	private int accessCount = 0;

	public String getReportDateTime() {
		return reportDateTime;
	}

	public void setReportDateTime(String reportDateTime) {
		this.reportDateTime = reportDateTime;
	}

	public int getAccessCount() {
		return accessCount;
	}

	public void setAccessCount(int accessCount) {
		this.accessCount = accessCount;
	}
}
