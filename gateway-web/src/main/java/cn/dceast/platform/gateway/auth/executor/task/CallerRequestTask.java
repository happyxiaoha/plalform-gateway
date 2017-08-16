package cn.dceast.platform.gateway.auth.executor.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.dceast.platform.gateway.auth.data.entity.CallerRequestInfo;
import cn.dceast.platform.gateway.auth.service.ServiceCallLogService;
import cn.dceast.platform.gateway.auth.service.ServiceStatisticsService;

/**
 * CallerRequestFilter中调用CallerRequestTask
 * 记录调用信息任务
 *  1. 记录调用流水
 *  2. 记录调用统计信息
 *	2017年3月21日14:15:47 已废弃
 * Created by hongkai on 2016/9/22.
 */
public class CallerRequestTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CallerRequestTask.class);

    private CallerRequestInfo callInfo;
    
//    private static final String SERVICE_CALL_LOG = "service_call_log";

    public CallerRequestTask(CallerRequestInfo callInfo){
        this.callInfo = callInfo;
    }

    @Override
    public void run() {
        writeDB(callInfo);
    }

    /**
     * 写入数据库
     *
     * @param callerRequestInfo
     */
    private void writeDB(CallerRequestInfo callerRequestInfo){
//    	ServiceCallLogService.writeCallLog(callerRequestInfo);
//        ServiceCallStatisticsService.writeCallStatistics(callerRequestInfo);
    	ServiceStatisticsService.writeSuccStatistics(callerRequestInfo);
    }

    /**
     * 写入服务调用日志表
     *
     * @param callerRequestInfo
     */
/*    private void writeCallLog(CallerRequestInfo callerRequestInfo){
        try {
            MongoDBUtil.getColl(SERVICE_CALL_LOG).insert(ObjectToDBObjectUtil.convertToDBObject(callerRequestInfo));
        } catch (Exception e) {
            logger.error("保存服务调用日志发生错误"  + callerRequestInfo, e);
        }
    }*/
}
