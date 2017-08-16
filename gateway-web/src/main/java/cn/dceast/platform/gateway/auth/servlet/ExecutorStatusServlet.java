package cn.dceast.platform.gateway.auth.servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cn.dceast.platform.gateway.auth.executor.ExecutorHolder;

import com.alibaba.fastjson.JSONObject;

/**
 * 查询异步任务执行器状态
 *
 * Created by hongkai on 2016/8/15.
 */
@RestController
public class ExecutorStatusServlet{

	@RequestMapping(value="/executorStatus", method={RequestMethod.POST, RequestMethod.GET})
    public String executorStatus(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String noticeExecutorStatus = ExecutorHolder.callerRequestRecordExecutor.toString();
        JSONObject result = new JSONObject();
        result.put("callerRequestRecordExecutor status", noticeExecutorStatus);
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
        result.put("queryTime", sf.format(new Date()));
        return (result.toJSONString());
    }
}
