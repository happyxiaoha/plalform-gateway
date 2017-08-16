package cn.dceast.platform.gateway.auth.ratelimit;

import cn.dceast.platform.gateway.auth.data.RateLimiterData;

/**
 * 速度限制工具
 *
 * 名词解释：
 *  requestPerSecond 每秒允许的请求数
 *  UnSortDelayQueue 无序有界阻塞队列，队列中元素有过期属性，只有过期的元素才能移出队列
 *  RequestHistory 请求历史，UnSortDelayQueue所接受的元素类型，包含获取过期时间方法
 *
 * 用法：
 *
 *  限流器初始化与开始限流：
 *      一定是要分两步进行，首先初始化一个限流器，然后调用开始方法开启限流；原因是在并发情况下，
 *      初始化出来的限流器不一定会最终采用，确认采用初始化出来的限流器后再调用开始方法。
 *      调用 cn.dceast.platform.gateway.auth.ratelimit.RateLimiter#init(int) 方法初始化一个限流器
 *      调用 cn.dceast.platform.gateway.auth.ratelimit.RateLimiter#start() 方法开启限流
 *
 *  流量判断：
 *      判断是否达到请求速度上限之前调用acquire()方法，如果返回true说明没有超过请求速度上限，可以接受请求，反之则已达到请求速度上限
 *
 *  速率修改：
 *      当请求速度有调整时，调用setRate(int requestPerSecond)方法修改请求速度上限
 *
 * 原理：
 *  RateLimiter内部维护一个有界队列 delayQueue， 队列大小(capacity)为请求速度上限，当队列已满（size == capacity），
 *  无法向队列中成功添加元素，说明已经达到请求速度上限。
 *
 *  队列接受的元素类型必须实现java.util.concurrent.Delayed接口
 *
 *  队列通过调用Delayed接口的getDelay（）方法判断元素还有多久过期，只有过期的元素才能成功从队列移除
 *
 *  从队列移除过期元素的方法是take()，如果没有过期元素，此方法会被阻塞，直到有可移除的元素线程才被唤醒
 *
 *  通过 cn.dceast.platform.gateway.auth.ratelimit.RateLimiter#init(int) 方法初始化一个队列
 *  通过 cn.dceast.platform.gateway.auth.ratelimit.RateLimiter#start() 方法开始限流控制，此方法
 *  会向清除过期数据任务列表(RateLimterData.getCallerRequestQueue())中添加新建的队列
 *
 *  cn.dceast.platform.gateway.auth.listener.RateLimteCleanExpiredDataListener 会监听清除过期数据任务列表
 *  如果发现有新的队列产生，就会起一根新的线程来清除队列中的过期元素
 *
 * Created by hongkai on 2016/7/18.
 */
public class RateLimiter {

    private UnSortDelayQueue delayQueue;

    private RateLimiter(int requestPerSecond){
        delayQueue = new UnSortDelayQueue(requestPerSecond);
    }

    /**
     * 初始化限流器
     *
     * @param requestPerSecond
     * @return
     */
    public static RateLimiter init(int requestPerSecond){
        return new RateLimiter(requestPerSecond);
    }

    /**
     * 开启限流
     */
    public void start(){
        RateLimiterData.getCallerRequestQueue().add(delayQueue);
    }

    public boolean acquire(){
        return delayQueue.add(new RequestHistory());
    }

    public Integer getRate(){
        return delayQueue.getCapacity();
    }

    public void setRate(int requestPerSecond){
        delayQueue.setCapacity(requestPerSecond);
    }
}
