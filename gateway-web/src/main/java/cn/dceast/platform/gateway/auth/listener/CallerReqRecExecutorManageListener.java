package cn.dceast.platform.gateway.auth.listener;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import cn.dceast.platform.gateway.auth.AppConfig;
import cn.dceast.platform.gateway.auth.executor.ExecutorHolder;
import cn.dceast.platform.gateway.auth.util.ApplicationUtil;

/**
 * 记录调用请求信息执行器管理 listener
 * 本listener有两个功能：
 *  1. servlet 初始化时，初始化一个ThreadPoolExecutor
 *  2. servlet 销毁时，停止ThreadPoolExecutor（不接收新的任务；把已有任务执行完后关闭线程池中的线程）
 *
 * ThreadPoolExecutor初始化：
 *  基于配置文件（application.properties）中配置：
 *  线程池核心线程数、最大线程数、等待队列大小、是否允许回收空闲的核心线程、线程最大空闲时间
 *
 * ThreadPoolExecutor停止：
 *  因为servlet销毁过程中，不会接收新的请求，系统线程资源相比于系统正常提供服务时相对宽裕，为了加快系统停止的速度，此时会扩展ThreadPoolExecutor可用的
 *  线程数量，基于（application.properties）中配置：
 *  服务关闭时核心线程数
 *
 *  * Created by hongkai on 2016/9/22.
 */
@Component
public class CallerReqRecExecutorManageListener implements CommandLineRunner{
    private static Logger logger = LoggerFactory.getLogger(CallerReqRecExecutorManageListener.class);

    @Autowired
    private AppConfig appConfig;
    
	@Override
	public void run(String... args) throws Exception {
        ExecutorHolder.callerRequestRecordExecutor = initTaskExecutor(appConfig.callerRequestRecordExecutorCoreCount,
        		appConfig.callerRequestRecordExecutorMaxCount,
        		appConfig.callerRequestRecordExecutorQueueSize,
        		appConfig.callerRequestRecordExecutorAllowCoreThreadTimeOut,
        		appConfig.callerRequestRecordExecutorThreadTimeOutSeconds);

        logger.info(String.format("caller request record executor initiated, core thread count %d," +
                        " max thread count %d, task queue size is %d, allow core thread timeout %b, thread timeout(s) %d",
                        appConfig.callerRequestRecordExecutorCoreCount,
                        appConfig.callerRequestRecordExecutorMaxCount,
                        appConfig.callerRequestRecordExecutorQueueSize,
                        appConfig.callerRequestRecordExecutorAllowCoreThreadTimeOut,
                        appConfig.callerRequestRecordExecutorThreadTimeOutSeconds));
    }

	@PreDestroy
    public void contextDestroyed() {
        try {
            shutdownTaskExecutor();
        } catch (InterruptedException e) {
            logger.error("listener destroyed failed", e);
            throw new RuntimeException("listener destroyed failed", e);
        }
    }

    /**
     * 停止任务执行器
     *
     * @throws InterruptedException
     */
    private void shutdownTaskExecutor() throws InterruptedException {
        ExecutorHolder.callerRequestRecordExecutor.shutdown();

        /*
         *服务器停止时，增加核心线程数，加快待执行任务的处理速度
         * 此处扩展core pool size 和 maximum pool size 相同是因为shutting down 时，不会有新的请求进来
         * 等待队列只能一直变小，不会再变大，maximum 设置的再大也没有用
         */
        ExecutorHolder.callerRequestRecordExecutor.setCorePoolSize(appConfig.callerRequestRecordExecutorShuttingDownCoreCount);
        ExecutorHolder.callerRequestRecordExecutor.setMaximumPoolSize(appConfig.callerRequestRecordExecutorShuttingDownCoreCount);
        logger.info(String.format("caller request record executor core thread count expand from %d to %d",
        		appConfig.callerRequestRecordExecutorCoreCount,
        		appConfig.callerRequestRecordExecutorShuttingDownCoreCount));

        logger.info(String.format("caller request record executor max thread count expand from %d to %d",
        		appConfig.callerRequestRecordExecutorMaxCount,
        		appConfig.callerRequestRecordExecutorShuttingDownCoreCount));

        awaitTermination(false);
    }

    private void awaitTermination(boolean isMarketingTerminated) throws InterruptedException {
        if(!isMarketingTerminated){
            ExecutorHolder.callerRequestRecordExecutor.awaitTermination(60L, TimeUnit.SECONDS);
            if(!ExecutorHolder.callerRequestRecordExecutor.isTerminated()){
                logger.warn(String.format("caller request record executor is not terminated: %s",
                        ExecutorHolder.callerRequestRecordExecutor.toString()));
            } else {
                logger.info(String.format("caller request record executor is terminated: %s",
                        ExecutorHolder.callerRequestRecordExecutor.toString()));
                isMarketingTerminated = true;
            }
        }
        if(isMarketingTerminated){
            return;
        }
        awaitTermination(isMarketingTerminated);
    }

    /**
     * 初始化任务执行器
     *
     * @param coreThreadCount  核心线程数量
     * @param maxThreadCount   最大线程数量
     * @param queueSize        线程忙碌时，任务排队等待的队列大小
     * @param allowCoreThreadTimeOut   是否允许空闲时回收核心线程
     * @param threadTimeOutSeconds     超过多少秒的空闲时间运行线程被销毁
     * @return
     */
    private ThreadPoolExecutor initTaskExecutor(Integer coreThreadCount, Integer maxThreadCount, Integer queueSize,
                                                boolean allowCoreThreadTimeOut, long threadTimeOutSeconds){
        BlockingQueue<Runnable> queue;
        if(queueSize == null){
            queue = new LinkedBlockingQueue<>();
        }else {
            queue = new LinkedBlockingQueue<>(queueSize);
        }

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(coreThreadCount, maxThreadCount,
                threadTimeOutSeconds, TimeUnit.SECONDS,
                queue);

        threadPoolExecutor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);

        return threadPoolExecutor;
    }

}
