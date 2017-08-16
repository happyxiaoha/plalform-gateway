package cn.dceast.platform.gateway.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.dceast.platform.gateway.auth.data.entity.CallerRequestInfo;
import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;
import cn.dceast.platform.gateway.auth.util.ObjectToDBObjectUtil;

import com.mongodb.BasicDBObject;

/**
 * api调用日志相关操作
 */
public class ServiceCallLogService {
    private static Logger logger = LoggerFactory.getLogger(ServiceCallLogService.class);

    /**
     * 更新服务调用日志表
     *
     * @param callerRequestInfo
     */
    public static void updateCallLog(CallerRequestInfo callerRequestInfo){
    	try {
            BasicDBObject query = new BasicDBObject().append("flownum", callerRequestInfo.getFlownum());

		    BasicDBObject update = new BasicDBObject()
		            .append("$set", new BasicDBObject("code", callerRequestInfo.getCode())
						            .append("message", callerRequestInfo.getMessage())
						            .append("status", callerRequestInfo.getStatus())
						            .append("result", callerRequestInfo.getResult()));
		
		    MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_CALL_LOG).update(query, update, true, false);
    	} catch (Exception e) {
    		logger.error("保存服务调用失败日志发生错误"  + callerRequestInfo, e);
    	}
    }
    
    /**
     * 写流水
     * @param callerRequestInfo
     */
    public static void writeCallLog(CallerRequestInfo callerRequestInfo){
        try {
            MongoDBUtil.getColl(InnerConstants.COLL_SERVICE_CALL_LOG).insert(ObjectToDBObjectUtil.convertToDBObject(callerRequestInfo));
        } catch (Exception e) {
            logger.error("保存服务调用日志发生错误"  + callerRequestInfo, e);
        }
    }
}
