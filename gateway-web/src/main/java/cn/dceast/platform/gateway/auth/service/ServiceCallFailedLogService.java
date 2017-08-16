package cn.dceast.platform.gateway.auth.service;

import cn.dceast.platform.gateway.auth.data.entity.CallerRequestInfo;
import cn.dceast.platform.gateway.auth.util.DateUtil;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;
import cn.dceast.platform.gateway.auth.util.ObjectToDBObjectUtil;
import com.mongodb.BasicDBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * api调用失败日志相关操作
 * Created by owen on 2016/8/15.
 */
public class ServiceCallFailedLogService {
    private static Logger logger = LoggerFactory.getLogger(ServiceCallFailedLogService.class);

    private static final String SERVICE_CALL_FAILED_LOG = "service_call_failed_log";

    /**
     * 写入服务调用失败日志表
     *
     * @param callerRequestInfo
     */
    public static void writeCallFailedLog(CallerRequestInfo callerRequestInfo){
        try {
            MongoDBUtil.getColl(SERVICE_CALL_FAILED_LOG).insert(ObjectToDBObjectUtil.convertToDBObject(callerRequestInfo));
        } catch (Exception e) {
            logger.error("保存服务调用失败日志发生错误"  + callerRequestInfo, e);
        }
    }
}
