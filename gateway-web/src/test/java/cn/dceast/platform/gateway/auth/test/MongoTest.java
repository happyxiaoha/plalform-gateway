package cn.dceast.platform.gateway.auth.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

import cn.dceast.platform.common.properties.Prop;
import cn.dceast.platform.common.properties.PropKit;
import cn.dceast.platform.gateway.auth.AppConfig;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;

public class MongoTest {

	private static final String CONFIG_FILE = "applicationTest.properties";
	private static final String COLL_TEST = "app";
	private static List<BasicDBObject> data = null;

	public static void main(String[] args) throws InterruptedException {

		// 单线程读取一张表测试
		initConfig();
		// MongoDBUtil.init();

		// 生成10000条app数据
		// genTestDataIntoApp();

		testMongo();

		MongoDBUtil.destroy();
	}

	private static void testMongo() throws InterruptedException {
		File file = new File("/opt/result_" + System.currentTimeMillis() + ".log");
		redirectOut2File(file);

		data = getAppInfoList();

		/**
		 * 1. 单线程测试
		 */
		testOneThread(100);

		testOneThread(1000);

		testOneThread(10000);

		testOneThread(100000);

		testOneThread(1000000);

		/**
		 * 2. 多线程测试
		 */

		List<Thread> threadList = new ArrayList<Thread>();
		System.out.print("======muti thread!===================");

		String batchId = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		for (int i = 0; i < 100; i++) {
			Thread ttThread = getTimerTask(batchId, 100, 1000);
			threadList.add(ttThread);
			ttThread.start();
		}

		boolean isAllDead = false;
		while (!isAllDead) {

			isAllDead = true;

			for (Thread tt : threadList) {
				if (tt.isAlive()) {
					isAllDead = false;
					break;
				}
			}

			Thread.currentThread().sleep(1000);

		}
	}

	private static void redirectOut2File(File file) {
		try {
			System.setOut(new PrintStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static Thread getTimerTask(final String batchId, final long sleepTime, final int count) {

		Thread tt = new Thread(new Runnable() {

			@Override
			public void run() {

				List<Long> result = new ArrayList<Long>();

				// System.out.println("Start Thread:"+
				// Thread.currentThread().getName());

				for (int i = 0; i < count; i++) {
					Double posd = (Math.random()) * (data.size());
					int pos = posd.intValue();

					long time1 = System.currentTimeMillis();
					readDB(data.get(pos).getString("name"));
					long time2 = System.currentTimeMillis();

					result.add(time2 - time1);

					insertMutiThreadReadInfo(batchId, Thread.currentThread().getName(), i, time2 - time1);

					try {
						Thread.currentThread().sleep(sleepTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}

				calculate(Thread.currentThread().getName(), result);

			}
		});

		return tt;
	}

	private static void insertMutiThreadReadInfo(String batchId, String name, int count, long time) {
		BasicDBObject fields = new BasicDBObject();
		fields.append("batchId", batchId).append("name", name).append("count", count).append("time", time)
				.append("dCreate", new Date());

		MongoDBUtil.getColl("test_log").insert(fields);
	}

	private static void readDB(String appName) {
		BasicDBObject query = new BasicDBObject();
		query.append("name", appName);
		BasicDBObject basicDBObject = (BasicDBObject) MongoDBUtil.getColl(COLL_TEST).findOne(query);
	}

	private static void testOneThread(int count) {
		// 存放统计时间
		List<Long> time = new ArrayList<Long>();

		// 读取app表
		for (int i = 0; i < count; i++) {
			Double posd = (Math.random()) * (data.size());
			int pos = posd.intValue();

			String appName = data.get(pos).getString("name");

			long time1 = System.currentTimeMillis();
			readDB(appName);
			long time2 = System.currentTimeMillis();

			time.add((time2 - time1));
		}

		calculate("", time);
	}

	private static Map<String, BasicDBObject> getAppInfoMap() {
		Map<String, BasicDBObject> map = new HashMap<String, BasicDBObject>();

		DBCursor dbCursor = MongoDBUtil.getColl(COLL_TEST).find();

		while (dbCursor.hasNext()) {
			BasicDBObject basicDBObject = (BasicDBObject) dbCursor.next();
			map.put(basicDBObject.getString("name"), basicDBObject);
		}

		dbCursor.close();

		return map;

	}

	private static List<BasicDBObject> getAppInfoList() {
		Map<String, BasicDBObject> map = getAppInfoMap();

		List<BasicDBObject> list = new ArrayList<BasicDBObject>();

		if (map != null) {
			Iterator<Entry<String, BasicDBObject>> iterator = map.entrySet().iterator();

			while (iterator.hasNext()) {
				Entry<String, BasicDBObject> entry = iterator.next();

				list.add(entry.getValue());
			}

		}

		return list;
	}

	private static void initConfig() {

		String path = CONFIG_FILE;

		Prop pro = PropKit.use(path);

		// AppConfig.mongoHost=pro.get("mongodb.oss.host");
		// AppConfig.mongodbName=pro.get("mongodb.oss.dbName");
		// AppConfig.mongoUserName=pro.get("mongodb.oss.userName");
		// AppConfig.mongoPassword=pro.get("mongodb.oss.password");
	}

	private static void calculate(String name, List<Long> list) {
		long maxTime = 0;
		long avgTime = 0;
		long sumTime = 0;

		for (Long l : list) {
			// System.out.println("cccc:"+l);
			sumTime = sumTime + l;

			if (maxTime < l) {
				maxTime = l;
			}

		}

		avgTime = (sumTime) / list.size();

		if (name == null || "".equals(name)) {
			name = Thread.currentThread().getName();
		}

		System.out.println(name + "==The test count is: " + list.size() + "=======================");
		System.out.println(name + "==The sumTime is: " + sumTime);
		System.out.println(name + "==The maxTime is: " + maxTime);
		System.out.println(name + "==The avgTime is: " + avgTime);
	}

	// 生成10000条应用
	private static void genTestDataIntoApp() {

		for (int i = 0; i < 10000; i++) {
			BasicDBObject field = new BasicDBObject();

			field.append("name", "performent_" + i).append("displayName", "performent_" + i).append("image", "")
					.append("memory", 256).append("instance", 0).append("status", "0").append("description", "test")
					.append("picId", "d11a2cb68d2842d795574b946660546d").append("catId", "2").append("appType", "URL")
					.append("url", "").append("healthAddress", "").append("healthContactEMail", "")
					.append("createrName", "").append("dCreate", new Date()).append("updaterName", null)
					.append("dUpdate", null).append("applyType", "regist").append("suggestion", "processing")
					.append("remark", "").append("offline", false).append("nodeType", "app");

			MongoDBUtil.getColl(COLL_TEST).insert(field);

		}

	}

}
