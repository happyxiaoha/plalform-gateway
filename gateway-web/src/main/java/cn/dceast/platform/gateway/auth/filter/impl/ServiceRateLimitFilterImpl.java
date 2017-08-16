package cn.dceast.platform.gateway.auth.filter.impl;

import cn.dceast.platform.gateway.auth.data.entity.App;
import cn.dceast.platform.gateway.auth.filter.AuthFilter;
import cn.dceast.platform.gateway.auth.filter.FilterResponseMessage;
import cn.dceast.platform.gateway.auth.filter.RequestInfo;
import cn.dceast.platform.gateway.auth.ratelimit.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务限流过滤器
 *
 * Created by hongkai on 2016/7/18.
 */
public class ServiceRateLimitFilterImpl extends AuthFilter {

    private Logger logger = LoggerFactory.getLogger(ServiceRateLimitFilterImpl.class);

    //key -> 服务名称， value -> 服务限流器
    private ConcurrentHashMap<String, RateLimiter> limiterMap = new ConcurrentHashMap<>();

    @Override
    public boolean doFilter(HttpServletRequest request, HttpServletResponse response) {
        App appInfo = RequestInfo.getAppInfo(request);

        //非法数据处理：实例数小于等于零，不做速率限制
        int gatewayInstanceCount = RequestInfo.getGatewayInstanceCount(request);
        if(gatewayInstanceCount <= 0){
            return true;
        }

        //没有设置maxTps不做速率限制
        if(appInfo.getMaxTps() == null){
            return true;
        }

        //计算最大访问速率
        Integer maxTps = calcMaxTps(appInfo.getMaxTps(), gatewayInstanceCount);

        //初始化RateLimiter
        RateLimiter limiter = initRateLimiter(appInfo.getName(), maxTps);

        //判断是否超过maxTps
        boolean acquire = limiter.acquire();

        if(!acquire){
            setErrorMessageOfJson(request,response, FilterResponseMessage.CODE_101106);
            logger.info(String.format("The app %s call rate over the max tps %d per instance (total instance %d)",
                    appInfo.getName(),
                    maxTps,
                    gatewayInstanceCount));
            return false;
        }
        return true;
    }

    /**
     * 初始化一个RateLimiter，保证并发条件下一个服务对应一个RateLimiter
     *  1. 从limiterMap中根据服务名称获取RateLimiter
     *  2. 如果获取到结果为空，根据maxTps初始化一个新的RateLimiter
     *  3. 调用limiterMap#putIfAbsent()方法把初始化的RateLimiter放入map
     *  4. 判断是否放入成功，如果没有成功，说明有其他线程在当前线程之前已经初始化了RateLimiter,
     *      再次从limiterMap中根据服务名称获取RateLimiter（因为确认其他线程成功初始化，此处一定拿的到RateLimiter）
     *  5. 判断limiter的速率与最新配置的maxTps是否相等，如果不等，更新成新的速率
     *  6. 返回当前应用的RateLimiter
     *
     * @param appName
     * @param maxTps
     * @return
     */
    private RateLimiter initRateLimiter(String appName, Integer maxTps) {
        //如果没有RateLimiter，新初始化一个
        RateLimiter limiter = limiterMap.get(appName);
        if(limiter == null){
            limiter = RateLimiter.init(maxTps);
            RateLimiter previous = limiterMap.putIfAbsent(appName, limiter);
            //如果previous != null，说明有其他线程已经成功放入，取map中的UnSortDelayQueue
            if(previous != null){
                limiter = limiterMap.get(appName);
            } else {
                //如果 previous == null，说明当前线程初始化的限流器是当前服务的限流器，开始限流
                limiter.start();
            }
        }

        //如果UnSortDelayQueue 速率设置与最新设置不同，更新新的速率
        if(limiter.getRate() != maxTps){
            //修改如果RateLimiter的访问速率，当前被限制的线程无法感知最新的速率，依然按照旧的速率来处理
            limiter.setRate(maxTps);
        }

        return limiter;
    }


    /**
     * 计算当前网关的maxTps：服务的maxTps 除网关实例数
     *
     * 除法操作只取整数部分，忽略小数部分
     *
     * 比如服务限制maxTps为10，网关实例数为3，那么每个网关分配的maxTps为3
     *
     * @param maxTps
     * @param gatewayInstanceCount
     * @return
     */
    private Integer calcMaxTps(Integer maxTps, int gatewayInstanceCount) {
        return maxTps/gatewayInstanceCount;
    }
}
