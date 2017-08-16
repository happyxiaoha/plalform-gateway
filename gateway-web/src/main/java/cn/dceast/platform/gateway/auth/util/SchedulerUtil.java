package cn.dceast.platform.gateway.auth.util;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import cn.dceast.platform.common.BusinessException;

/**
 * 任务调度工具类
 * @author 未来
 *
 */
public class SchedulerUtil {
	private static Scheduler scheduler;
	
	public static void init(){
		if(scheduler!=null){
			return;
		}
		
		SchedulerFactory sf = new StdSchedulerFactory();
		try {
			scheduler = sf.getScheduler();
		} catch (SchedulerException e) {
			throw new BusinessException(e.getMessage());
		}
	}
	
	public static Scheduler getScheduler(){
		return scheduler;
	}
	
	public static void destroy(){
		if(scheduler!=null){
			try {
				scheduler.shutdown();
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
		}
	}
}
