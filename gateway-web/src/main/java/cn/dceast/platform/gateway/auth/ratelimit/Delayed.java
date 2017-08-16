package cn.dceast.platform.gateway.auth.ratelimit;

import java.util.concurrent.TimeUnit;

/**
 * A mix-in style interface for marking objects that should be
 * acted upon after a given delay.
 *
 * copy from java.util.concurrent.Delayed
 *
 * 区别是没有继承java.lang.Comparable接口，此接口被不排序有界延迟队列（cn.dceast.platform.gateway.auth.ratelimit.UnSortDelayQueue）使用
 * 所以不需要实现Comparable接口
 *
 * Created by hongkai on 2016/7/19.
 */
public interface Delayed {

    /**
     * Returns the remaining delay associated with this object, in the
     * given time unit.
     *
     * @param unit the time unit
     * @return the remaining delay; zero or negative values indicate
     * that the delay has already elapsed
     */
    long getDelay(TimeUnit unit);
}
