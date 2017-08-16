package cn.dceast.platform.gateway.auth.data;

import cn.dceast.platform.gateway.auth.ratelimit.UnSortDelayQueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 限速工具队列数据过期处理任务列表
 *
 * Created by hongkai on 2016/7/18.
 */
public class RateLimiterData {

    public static BlockingQueue<UnSortDelayQueue> getCallerRequestQueue(){
        return QueueHolder.callerRequestQueue;
    }

    private static class QueueHolder{
        private static BlockingQueue<UnSortDelayQueue> callerRequestQueue = new LinkedBlockingQueue<>();
    }
}
