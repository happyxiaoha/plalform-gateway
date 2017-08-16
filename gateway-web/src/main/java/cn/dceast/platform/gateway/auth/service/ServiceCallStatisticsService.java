package cn.dceast.platform.gateway.auth.service;

import cn.dceast.platform.gateway.auth.data.entity.CallerRequestInfo;
import cn.dceast.platform.gateway.auth.util.DateUtil;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;
import com.mongodb.BasicDBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;

/**
 * 成员方法没有被调用，此类已废弃2017年5月13日13:30:18
 * api调用统计表相关操作
 * Created by owen on 2016/8/15.
 */
public class ServiceCallStatisticsService {
    private static Logger logger = LoggerFactory.getLogger(ServiceCallStatisticsService.class);

    private static final String SERVICE_CALL_STATISTICS = "service_call_statistics";
    /**
     * 将访问记录写入服务调用统计表
     *
     * @param callerRequestInfo
     */
    public static void writeCallStatistics(CallerRequestInfo callerRequestInfo){
        /*
         * 先通取得当前日期，格式为yyyyMMdd,类型转换为int，方便比较大小
         *
         * 更新统计表分两步操作：
         *  step 1. 更新属于当前调用者，api为当前调用api，最后一次更新日期小于当前日期数据：
         *      a. 当天调用次数（countOfDay）更新为零
         *      b. 更新时间（dUpdate）更新为当前时间
         *      c. 更新日期（updateDate）更新为当前日期
         *  step 2. 更新属于当前调用者，api为当前调用api，最后一次更新日期大于等于当前日期数据：
         *      a. count 加一 (通过mongo $inc保证原子性)
         *      b. countOfDay 加一 (通过mongo $inc保证原子性)
         *      c. 更新时间（dUpdate）更新为当前时间
         *      d. 更新日期（updateDate）更新为当前日期
         */
        try{
            Integer currentDate = DateUtil.getCurrentDate();
            resetCountOfDay(callerRequestInfo, currentDate);
            increaseCount(callerRequestInfo, currentDate);
        }catch (Exception e){
            logger.error("更新服务调用次数发生错误"  + callerRequestInfo, e);
        }
    }

    /**
     * 将访问失败记录写入服务调用统计表,处理逻辑类似访问记录统计
     *
     * @param callerRequestInfo
     */
    public static void writeCallFailedStatistics(CallerRequestInfo callerRequestInfo){
        try{
            Integer currentDate = DateUtil.getCurrentDate();
            resetCountOfDay(callerRequestInfo, currentDate);
            increaseFailedCount(callerRequestInfo, currentDate);
        }catch (Exception e){
            logger.error("更新服务调用次数发生错误"  + callerRequestInfo, e);
        }
    }

    /**
     * 更新小于当前日期数据的countOfDay & failedCountOfDay为0，如果不存在符合条件的数据不做操作
     * @param callerRequestInfo
     * @param currentDate
     */
    private static void resetCountOfDay(CallerRequestInfo callerRequestInfo, Integer currentDate){
        BasicDBObject query = new BasicDBObject("callerName", callerRequestInfo.getCallerName())
//                .append("appName", callerRequestInfo.getAppName())
//                .append("apiUrl", callerRequestInfo.getApiUrl())
                .append("updateDate", new BasicDBObject("$lt", currentDate));

        BasicDBObject update = new BasicDBObject("countOfDay", 0)
                .append("failedCountOfDay", 0)
                .append("dUpdate", new Date())
                .append("updateDate", currentDate);

        MongoDBUtil.getColl(SERVICE_CALL_STATISTICS).update(query, new BasicDBObject("$set", update), false, false);
    }

    /**
     * 更新大于等于当前日期数据：countOfDay + 1，count + 1，如果不存在符合条件的数据，做插入操作
     * @param callerRequestInfo
     * @param currentDate
     */
    private static void increaseCount(CallerRequestInfo callerRequestInfo, Integer currentDate){
        BasicDBObject query = new BasicDBObject()
                .append("callerName", callerRequestInfo.getCallerName())
//                .append("appName", callerRequestInfo.getAppName())
//                .append("apiUrl", callerRequestInfo.getApiUrl())
                .append("updateDate", new BasicDBObject("$gte", currentDate));

        BasicDBObject update = new BasicDBObject()
                .append("$inc", new BasicDBObject("count", 1).append("countOfDay", 1))
                .append("$set", new BasicDBObject("dUpdate", new Date())
                        .append("updateDate", currentDate));

        MongoDBUtil.getColl(SERVICE_CALL_STATISTICS).update(query, update, true, false);
    }

    /**
     * 更新大于等于当前日期数据：countOfDay + 1，count + 1，如果不存在符合条件的数据，做插入操作
     * @param callerRequestInfo
     * @param currentDate
     */
    private static void increaseFailedCount(CallerRequestInfo callerRequestInfo, Integer currentDate){
        BasicDBObject query = new BasicDBObject()
                .append("callerName", callerRequestInfo.getCallerName())
//                .append("appName", callerRequestInfo.getAppName())
//                .append("apiUrl", callerRequestInfo.getApiUrl())
                .append("updateDate", new BasicDBObject("$gte", currentDate));

        BasicDBObject update = new BasicDBObject()
                .append("$inc", new BasicDBObject("failedCount", 1).append("failedCountOfDay", 1))
                .append("$set", new BasicDBObject("dUpdate", new Date())
                .append("updateDate", currentDate));

        MongoDBUtil.getColl(SERVICE_CALL_STATISTICS).update(query, update, true, false);
    }

}
