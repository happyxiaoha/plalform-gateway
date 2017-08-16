package cn.dceast.platform.gateway.auth.ratelimit;

import java.util.concurrent.TimeUnit;

/**
 * 请求历史
 *
 * Created by hongkai on 2016/7/14.
 */
public class RequestHistory implements Delayed {

    private Long delayTime;

    public RequestHistory(){
        delayTime = TimeUnit.NANOSECONDS.convert(1L, TimeUnit.SECONDS) + System.nanoTime();
    }

    /**
     * 返回还有多久元素过期
     *
     * @param unit the time unit
     * @return
     */
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(delayTime - System.nanoTime(),  TimeUnit.NANOSECONDS);
    }

}
