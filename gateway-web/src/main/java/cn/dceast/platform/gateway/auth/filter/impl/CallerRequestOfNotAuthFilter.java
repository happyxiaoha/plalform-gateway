package cn.dceast.platform.gateway.auth.filter.impl;

import cn.dceast.platform.gateway.auth.data.entity.CallerRequestInfo;
import cn.dceast.platform.gateway.auth.executor.ExecutorHolder;
import cn.dceast.platform.gateway.auth.executor.task.CallerRequestTask;
import cn.dceast.platform.gateway.auth.filter.AuthFilter;
import cn.dceast.platform.gateway.auth.filter.FilterResponseMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Date;
import java.util.concurrent.RejectedExecutionException;

/**
 * 记录api调用信息 已废弃
 *
 * Created by hongkai on 2016/1/5.
 */
public class CallerRequestOfNotAuthFilter extends AuthFilter {

    private static Logger logger = LoggerFactory.getLogger(CallerRequestOfNotAuthFilter.class);

    @Override
    public boolean doFilter(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //构造调用信息对象
        CallerRequestInfo callInfo = getCallerRequestInfo(request);

        try{
            //异步执行任务
            ExecutorHolder.callerRequestRecordExecutor.execute(new CallerRequestTask(callInfo));
        } catch (RejectedExecutionException e) {
            //队列已满，拒绝请求
            setErrorMessageOfJson(request, response, FilterResponseMessage.CODE_300102);
            logger.error(String.format("caller request record executor is too busy, reject %s", callInfo));
            return false;
        } catch (Exception e) {
            setErrorMessageOfJson(request, response, FilterResponseMessage.CODE_300102);
            logger.error("unknown error", e);
            return false;
        }
        return true;
    }

}
