package cn.dceast.platform.gateway.auth.service;

import cn.dceast.platform.gateway.auth.data.entity.CallerRequestInfo;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;
import cn.dceast.platform.gateway.auth.util.ObjectToDBObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * api调用失败日志相关操作
 * Created by owen on 2016/8/15.
 */
public class ServiceAuthFailedLogService {
    private static Logger logger = LoggerFactory.getLogger(ServiceAuthFailedLogService.class);

    private static final String SERVICE_AUTH_FAILED_LOG = "service_auth_failed_log";

    /**
     * 写入服务调用失败日志表
     *
     * @param callerRequestInfo
     */
    public static void writeAuthFailedLog(CallerRequestInfo callerRequestInfo){
        try {
            MongoDBUtil.getColl(SERVICE_AUTH_FAILED_LOG).insert(ObjectToDBObjectUtil.convertToDBObject(callerRequestInfo));
        } catch (Exception e) {
            logger.error("保存服务鉴权失败日志时发生错误"  + callerRequestInfo, e);
        }
    }
}
