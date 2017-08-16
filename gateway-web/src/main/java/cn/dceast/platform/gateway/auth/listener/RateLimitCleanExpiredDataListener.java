package cn.dceast.platform.gateway.auth.listener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import cn.dceast.platform.gateway.auth.data.RateLimiterData;
import cn.dceast.platform.gateway.auth.ratelimit.UnSortDelayQueue;

/**
 * 清理过期数据
 *
 * Created by hongkai on 2016/7/18.
 */
@Component
public class RateLimitCleanExpiredDataListener implements CommandLineRunner {

    private Logger logger = LoggerFactory.getLogger(RateLimitCleanExpiredDataListener.class);


    private ExecutorService executorService = null;

    @Override
	public void run(String... args) throws Exception {
        executorService = Executors.newCachedThreadPool();
        watch();
    }

    private void watch(){
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    while(!Thread.currentThread().isInterrupted()){
                        UnSortDelayQueue unSortDelayQueue = RateLimiterData.getCallerRequestQueue().take();
                        cleanExpiredData(unSortDelayQueue);
                    }
                } catch (InterruptedException ex) {
                    logger.error("线程被中断", ex.getMessage());
                } catch (Exception e){
                    logger.error("清理过期请求历史发生错误", e);
                }
            }
        });
    }

    private void cleanExpiredData(final UnSortDelayQueue unSortDelayQueue){
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    while(!Thread.currentThread().isInterrupted()){
                        unSortDelayQueue.take();
                    }
                }catch (InterruptedException ex) {
                    logger.error("线程被中断", ex.getMessage());
                } catch (Exception e){
                    logger.error("清理过期请求历史发生错误", e);
                }
            }
        });
    }


    @PreDestroy
    public void contextDestroyed() {
        logger.info("Destroy RateLimitCleanExpiredDataListener!");
        if(executorService != null){
            executorService.shutdownNow();
            try {
                boolean result = executorService.awaitTermination(60, TimeUnit.SECONDS);
                while(!result){
                    result = executorService.awaitTermination(60, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                logger.error("", e);
            }
        }
    }
}
