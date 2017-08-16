package cn.dceast.platform.gateway.auth.filter;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 利用ctrl+shift+g未发现有调用
  * @ClassName: FilterDetectTask
  * @Description: TODO
  * @author Cobot-lych
  * @date 2017年3月16日 下午9:21:43
  *
  */
public class FilterDetectTask implements Runnable{

	private static Logger logger = LoggerFactory.getLogger(FilterDetectTask.class);
	
	@Override
	public void run() {
		while(true){
			URL stream = Thread.currentThread().getContextClassLoader().getResource("handlerFilterApplication.xml");
			try {
				String fileTime = String.valueOf(new File(java.net.URLDecoder.decode(stream.getFile(),"UTF-8")).lastModified());
				if(ThreadLocalUtil.getThreadLocalValue()==null || !ThreadLocalUtil.getThreadLocalValue().equals(fileTime)){
					ThreadLocalUtil.setThreadLocalValue(fileTime);
					FilterChain.init();
					logger.info("handlerFilterApplication.xml load invoke FilterChain.init()==");
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
}
