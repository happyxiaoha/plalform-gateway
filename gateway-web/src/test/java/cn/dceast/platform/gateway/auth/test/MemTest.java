package cn.dceast.platform.gateway.auth.test;

import junit.framework.TestCase;

import org.junit.BeforeClass;
import org.junit.Test;

import cn.dceast.platform.common.BusinessException;
import cn.dceast.platform.common.mongo.DBUtil;
import cn.dceast.platform.gateway.auth.rest.DyUps;

public class MemTest {
	private static DBUtil dbUtil;

	@BeforeClass
	public static void initMongoDb() {
		if (dbUtil != null) {
			return;
		}
		dbUtil = new DBUtil();
		dbUtil.setHost("172.16.49.68");
		dbUtil.setDbName("gateway");
		dbUtil.setUsername("platform-oss");
		dbUtil.setPassword("dc202152");
		dbUtil.setMinConnectionsPerHost(0);
		dbUtil.setConnectionsPerHost(100);
		dbUtil.setConnectTimeout(10000);
		dbUtil.setMaxConnectionIdleTime(3600000);
		dbUtil.setMaxWaitTime(120000);
		dbUtil.setThreadsAllowedToBlockForConnectionMultiplier(5);
		try {
			dbUtil.init();
		} catch (Exception e) {
			throw new BusinessException("init mongodb fail! detail:" + e.getMessage());
		}
		DyUps dyUps =new DyUps();
		dyUps.type ="client";
		
		
	}

	public void testApp() {
		System.out.println("Hello World");
	}

	@Test
	public void Hello() {
		DyUps dyUps =new DyUps();
		String helloAppName = dyUps.getAppName("/phone/getPhoneNo", "03", "guangzhou");
		System.out.println(helloAppName);
		System.out.println("double");
	}

	@Test
	public void Hello2() {
		System.out.println("Hello2");
	}
}
