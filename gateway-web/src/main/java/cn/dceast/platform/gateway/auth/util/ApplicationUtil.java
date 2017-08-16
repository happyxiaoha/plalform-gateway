package cn.dceast.platform.gateway.auth.util;

import org.springframework.web.context.WebApplicationContext;

public class ApplicationUtil {

	private WebApplicationContext context;
	private volatile static ApplicationUtil singleton;

	private ApplicationUtil() {
	}

	public static ApplicationUtil getSingleton() {
		if (singleton == null) {
			synchronized (ApplicationUtil.class) {
				if (singleton == null) {
					singleton = new ApplicationUtil();
				}
			}
		}
		return singleton;
	}

	public WebApplicationContext getContext() {
		return context;
	}

	public void setContext(WebApplicationContext context) {
		this.context = context;
	}
	
}
