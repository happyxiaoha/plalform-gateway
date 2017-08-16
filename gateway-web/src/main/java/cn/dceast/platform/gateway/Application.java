package cn.dceast.platform.gateway;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import cn.dceast.platform.gateway.auth.util.ApplicationUtil;

@Configuration
@ComponentScan(basePackages={"cn.dceast.platform.gateway", "com.digitalchina"})
@EnableAutoConfiguration
@EnableDiscoveryClient
public class Application extends SpringBootServletInitializer{
	
	private static Logger logger = LoggerFactory.getLogger(Application.class);
	
	
	@Override  
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {  
        return application.sources(Application.class);  
    }
	
	@Override
	protected WebApplicationContext createRootApplicationContext(ServletContext servletContext) {
		WebApplicationContext rootAppContext = super.createRootApplicationContext(servletContext);
		ApplicationUtil.getSingleton().setContext(rootAppContext);
		return rootAppContext;
	}
	
	@Bean
	@LoadBalanced
	RestTemplate retryableLoadbalancedRestTemplate() {
		return new RestTemplate();
	}
	
//	public static void main(String[] args){
//		logger.debug("========== Application Startin ==========");
//		SpringApplication.run(Application.class, args);
//	}
}
