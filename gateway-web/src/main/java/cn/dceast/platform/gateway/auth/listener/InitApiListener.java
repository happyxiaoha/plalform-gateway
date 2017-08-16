package cn.dceast.platform.gateway.auth.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.netflix.loadbalancer.Server;

import cn.dceast.platform.gateway.auth.service.ServiceApi;

/**
 * @author liycq
 * 外网tomcat启动时,向nginx中写入mongodb初始化数据
 */
@Component
public class InitApiListener implements CommandLineRunner {

	@Value("${nginx.dyups.address}")
	public String address;
	
	@Value("${gateway.type}")
	public String type;
	
	@Value("${redis.host}")
	public String redisHost;
	
	private static Logger logger = LoggerFactory.getLogger(InitApiListener.class);
	
	@Override
	public void run(String... arg0) throws Exception {
		logger.info("initApi begin: "+"type value:"+type+" address value:"+address);
		if (type!=null && type.equals("server")) {
			ServiceApi.initApiOrServiceOrData(address,redisHost);
			logger.info("finish init api");
		}
	}
}
