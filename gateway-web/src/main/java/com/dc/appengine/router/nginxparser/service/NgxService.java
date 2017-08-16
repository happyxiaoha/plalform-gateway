package com.dc.appengine.router.nginxparser.service;

import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.formula.functions.Count;

import com.dc.appengine.router.nginxparser.NgxBlock;
import com.dc.appengine.router.nginxparser.NgxConfig;
import com.dc.appengine.router.nginxparser.NgxEntry;
import com.dc.appengine.router.nginxparser.NgxParam;
import com.dc.appengine.router.test.ParseTestBase;

import edu.emory.mathcs.backport.java.util.Collections;

public class NgxService extends ParseTestBase implements NgxOperate {
	public static final String MAP = "map";
	public static final String UPSTREAM = "upstream";
	public static final String LOCATION = "location";
	public static final String SERVER = "server";
	public static final String LISTEN = "listen";
	public static final String HTTP = "http";
	public static final String TCP = "tcp";
	public static final String HTTPS = "https";
	public static final String PROXY = "proxy_pass";
	public static final String NEWNGXTCPPROTOCOLNAME = "stream";
	public static final String FORWARDSLASH = "forwardsplash";
	public static final String LIMITCONN="limit_conn";
	public static final String LIMITCONNZONE="limit_conn_zone";
	public static final String LIMITREQ="limit_req";
	public static final String LIMITREQZONE="limit_req_zone";
	public static final String LIMITRATE="$limit_rate";
	public static final String SET="set";
	public static final String LIMITNAME="perserver";
	
	public static final String COLON = ":";
	public static final String SPACE = " ";
	public static final String SLASH = "/";
	public static final String DSLASH = "//";
	public static final String QUOTA = ";";
	public static final String DOLLAR = "$";

	@Override
	public synchronized void addOrupdateProtocolBlock(NgxBlock conf,
			String protocol, String... entrys) {
		// 找一下是否存在Protocol块
		NgxBlock protocolBlock = findProtocolBlock(conf, protocol);
		if (protocolBlock == null) {
			// 不存在Protocol块
			protocolBlock = new NgxBlock();
			protocolBlock.addValue(protocol);
			conf.addEntryAtLast(protocolBlock);
		}
		// 传入的entrys为null
		for (String entry : entrys) {
			NgxParam item = addEntry(entry);
			protocolBlock.addEntryAtLast(item);
		}
	}

	public synchronized void addOrupdateServerBlock(NgxBlock conf,
			String protocol, String port, String... entrys) {

		NgxBlock serverBlock = findServerBlock(conf, protocol, port);
		if (serverBlock == null) {
			NgxBlock proBlock = findProtocolBlock(conf, protocol);
			serverBlock = new NgxBlock();
			serverBlock.addValue(SERVER);
			NgxParam param = addEntry(LISTEN + SPACE + port);
			serverBlock.addEntryAtLast(param);

			proBlock.addEntryAtLast(serverBlock);
		}

		for (String entry : entrys) {
			NgxParam item = addEntry(entry);
			serverBlock.addEntryAtLast(item);
		}
	}

	public synchronized void addOrupdateLocationBlock(NgxBlock conf,
			String protocol, String port, String locationValue,
			String... entrys) {

		//
		String value = SLASH + locationValue;
		NgxBlock server = findServerBlock(conf, protocol, port);
		if (server == null)
			return;
		// 找出所有location块
		List<NgxEntry> locations = server.findAll(NgxConfig.BLOCK, LOCATION);
		// 如果已有location则仅添加parameter
		for (NgxEntry entry : locations) {
			NgxBlock block = (NgxBlock) entry;
			if (value.equals(block.getValue())) {
				for (String e : entrys) {
					NgxParam param = new NgxParam();
					param.addValue(e);
					block.addEntryAtLast(param);
				}
				return;
			}
		}

		// 添加新的Location块
		NgxBlock locationBlock = new NgxBlock();
		// locationBlock.addValue(LOCATION + SPACE + value);
		locationBlock.addValue(LOCATION);
		locationBlock.addValue(value);
		for (String entry : entrys) {
			NgxParam param = addEntry(entry);
			locationBlock.addEntryAtLast(param);
		}
		server.addEntryAtLast(locationBlock);
		//
	}

	/**
	 * 新建NgxParam，并在其中加入entry
	 */
	private NgxParam addEntry(String entry) {
		NgxParam param = new NgxParam();
		param.addValue(entry);
		return param;
	}

	/* 
	 * 
	 */
	@Override
	public synchronized void addOrupdateUpstreamBlock(NgxBlock conf,
			String protocol, String upstreamValue, boolean isSticky,
			boolean isOldNgx, String... entrys) {
		// 找出nginx.conf中所有位于protocol下的所有UPSTREAM块
		List<NgxEntry> upstreams = conf.findAll(NgxConfig.BLOCK, protocol,
				UPSTREAM);
		// //////////////////////遍历nginx.conf中所有UPSTREAM块，查找每个块的块名是否与upstreamValue(形参)相同
		// FIXME 原代码中servers中有重复项！
		for (NgxEntry entry : upstreams) {
			NgxBlock block = (NgxBlock) entry;
			if (upstreamValue.equals(block.getValue())) {
				for (String value : entrys) {
					// 如原nginx.conf中已存在某upstream，则在此upstream块中加入RouteItem中定义的内容
					NgxParam param = new NgxParam();
					param.addValue(value);
					block.addEntryAtLast(param);
				}
				return;
			}
		}
		// ////////////////////

		// //////根据RouteItem的定义，向nginx.conf中加入新的upstream块
		NgxBlock upstreamBlock = new NgxBlock();
		// upstreamBlock.addValue(UPSTREAM + SPACE + upstreamValue);
		upstreamBlock.addValue(UPSTREAM);
		upstreamBlock.addValue(upstreamValue);
		if (isSticky) {
			NgxParam stickyParam = null;
			if (isOldNgx) {
				// 如果是nginx1.8
				stickyParam = addEntry("sticky");
			} else {
				if (protocol.equals(NgxService.NEWNGXTCPPROTOCOLNAME)) {
					stickyParam = addEntry("hash $remote_addr");
				} else {
					// nginx 1.10的http模块
					stickyParam = addEntry("ip_hash");
				}
			}
			upstreamBlock.addEntryAtLast(stickyParam);
		}
		for (String entry : entrys) {
			NgxParam param = addEntry(entry);
			upstreamBlock.addEntryAtLast(param);
		}
		findProtocolBlock(conf, protocol).addEntryAtLast(upstreamBlock);
		// ///////
	}

	/**
	 * 
	 * @param conf
	 * @param protocol
	 * @param mapValue
	 * @param upstreamName
	 */
	@Override
	public synchronized void deleteMapEntry(NgxBlock conf, String protocol,
			String mapValue, String upstreamName) {
		//找出所有map块
		List<NgxEntry> maps = conf.findAll(NgxConfig.BLOCK, protocol, MAP);
		for (NgxEntry entry : maps) {
			NgxBlock block = (NgxBlock) entry;
			if (mapValue.equals(block.getValue())) {
				//item是map中某项内容
				Iterator<NgxEntry> it = block.iterator();
				// ////////////////遍历某个map块中的所有内容
				while (it.hasNext()) {
					NgxEntry item = it.next();
					if (item.toString().contains(upstreamName)) {
						it.remove();
					}
				}
				// ////////////////-遍历某个map块中的所有内容
				//map块中内容为空，则删掉此map
				int size = block.getEntries().size();
				if (size == 0) {
					conf.remove(entry);
				}
			}
		}
	}

	/*
	 * 2016年10月13日19:20:23 删除upstream实体
	 */
	@Override
	public synchronized void deleteUpstreamEntry(NgxBlock conf,
			String protocol, String upstreamValue, boolean isOldNgx,
			String... entrys) {
		// 找出nginx.conf中所有位于protocol下的UPSTREAM块
		List<NgxEntry> upstreams = conf.findAll(NgxConfig.BLOCK, protocol,
				UPSTREAM);

		// ///////////////////////删除UPSTREAM块
		for (NgxEntry entry : upstreams) {
			NgxBlock block = (NgxBlock) entry;
			if (upstreamValue.equals(block.getValue())) {
				Iterator<NgxEntry> it = block.iterator();

				// ////////////////遍历某个upstream块中的所有负载
				while (it.hasNext()) {
					NgxEntry item = it.next();

					// ////
					if (entrys.length == 0) {
						conf.remove(entry);
						return;
					}
					// ///
					// item为原始文件中的内容，entrys[0]则记录着待删除的内容。
					// 假设entrys[0]为 "server 127.0.0.1:28888 weight=1"，则如下语句为：
					// 当item中包含"server 127.0.0.1:28888"这部分内容，则将这个item给remove掉。
					if (entrys[0].contains("weight")) {
						if (item.toString().contains(
								entrys[0].substring(0,
										entrys[0].indexOf("weight") - 1))) {
							it.remove();
						}
					} else {

						if (item.toString().contains(entrys[0])) {
							it.remove();
						}
					}
				}
				// ////////////////-遍历某个upstream块中的所有负载

				int size = block.getEntries().size();
				boolean mark = false;

				if (size == 1) {
					Iterator<NgxEntry> ngit = block.getEntries().iterator();
					NgxEntry ngitem = ngit.next();
					if (isOldNgx) {
						if ("sticky;".equals(ngitem.toString())) {
							mark = true;
						}
					} else {
						// nginx1.10
						if ("ip_hash;".equals(ngitem.toString())
								&& protocol.equals(NgxService.HTTP)) {
							mark = true;
						} else {
							if ("hash $remote_addr;".equals(ngitem.toString())
									&& protocol
											.equals(NgxService.NEWNGXTCPPROTOCOLNAME)) {
								mark = true;
							}
						}
					}
				}
				//
				// 如果upstream块下已无任何负载 或者nginx
				if (size == 0 || mark) {
					conf.remove(entry);
					// deletelocationBlock(conf,protocol,upstreamValue);
				}
			}
		}
		// ///////////////////

	}

	public synchronized void deletelocationBlock(NgxBlock conf,
			String protocol, String port, String locationValue) {
		String value = SLASH + locationValue;
		NgxBlock server = findServerBlock(conf, protocol, port);
		if (server == null)
			return;
		List<NgxEntry> locations = server.findAll(NgxConfig.BLOCK, LOCATION);
		for (NgxEntry entry : locations) {
			NgxBlock block = (NgxBlock) entry;
			if (value.equals(block.getValue())) {
				server.remove(entry);
			}
		}

	}

	/* 
	 * 
	 */
	@Override
	public synchronized NgxBlock findServerBlock(NgxBlock conf,
			String protocol, String port) {

		if (port == null) {
			// port==null为找Protocol块
			NgxEntry pro = conf.find(NgxConfig.BLOCK, protocol);
			return (NgxBlock) pro;
		}

		// 待查找字符串
		String str = LISTEN + SPACE + port + QUOTA;
		// 找出nginx.conf中所有位于protocol下的所有SERVER块
		List<NgxEntry> servers = conf
				.findAll(NgxConfig.BLOCK, protocol, SERVER);
		// //////////////////////遍历nginx.conf中所有SERVER块，查找每个块的监听端口是否与port(形参)相同
		// FIXME 原代码中servers中有重复项！
		for (NgxEntry entry : servers) {
			NgxBlock block = (NgxBlock) entry;
			Iterator<NgxEntry> it = block.iterator();

			while (it.hasNext()) {
				NgxEntry item = it.next();
				if (item instanceof NgxParam) {
					NgxParam param = (NgxParam) item;

					if (str.equals(param.toString())) {

						return block;

					}
				}
			}
		}
		// /////////////////////////////
		// 如果加载的nginx.conf中没listen port端口，则return null
		return null;
	}

	/*
	 * 找是否存在此Protocol块
	 */
	@Override
	public synchronized NgxBlock findProtocolBlock(NgxBlock conf,
			String protocol) {

		return findServerBlock(conf, protocol, null);
	}

	@Override
	public synchronized void deleteServerBlock(NgxBlock conf, String protocol,
			String port) {
		NgxBlock server = findServerBlock(conf, protocol, port);
		if (server == null)
			return;
		conf.remove(server);

	}

	/**
	 * haowen写的测试类
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		NgxService s = new NgxService();
		NgxConfig conf = s.parse("D:/nested/c2.conf");
		// s.addOrupdateServerBlock(conf, "http", "666", "server_name
		// 123.23.12", "ssl off", "proxy_set_header Host $host");
		// s.addOrupdateServerBlock(conf, "http", "666", "server_name
		// 123.23.12", "ssl off", "proxy_set_header Host 123");
		// s.addOrupdateProtocolBlock(conf, "http", "keepalive_timeout
		// 65","tcp_nodelay on");
		// s.addOrupdateLocationBlock(conf, "http", "666", "haowen", "memc_pass
		// 127.0.0.1:11211","set $memc_cmd xxx");
		// s.addOrupdateUpstreamBlock(conf, "tcp", "test3", "server
		// 127.0.0.1:8084");
		// s.deleteServerBlock(conf, "http", "444");
		System.out.println(s.findLocationBlock(conf, "http", "443").size());
		// System.out.println(new NgxDumper(conf).dump());
	}

	/* 
	 * 
	 */
	@Override
	public synchronized List<NgxEntry> findLocationBlock(NgxBlock conf,
			String protocol, String port) {
		// 找到port对应的server块
		NgxBlock server = findServerBlock(conf, protocol, port);
		if (server == null) {
			// return null
			return Collections.emptyList();
		}
		// 找到某server块下的所有location
		List<NgxEntry> locations = server.findAll(NgxConfig.BLOCK, LOCATION);
		return locations;
	}

	/* 
	 * 
	 */
	@Override
	public synchronized NgxBlock findUpstreamBlock(NgxBlock conf,
			String protocol, String upstreamValue) {
		if (upstreamValue==null) {
			return null;
		}
		// 找出所有
		List<NgxEntry> upstreams = conf.findAll(NgxConfig.BLOCK, protocol,
				UPSTREAM);
		for (NgxEntry entry : upstreams) {
			NgxBlock block = (NgxBlock) entry;
			if (upstreamValue.equals(block.getValue())) {
				return block;
			}
		}
		return null;
	}

	@Override
	public NgxBlock findMapBolock(NgxBlock conf, String protocol,
			String mapValue) {
		// 待查找字符串
		String str = mapValue;
		// 找出nginx.conf中所有位于protocol下的MAP块
		List<NgxEntry> maps = conf.findAll(NgxConfig.BLOCK, protocol, MAP);
		for (NgxEntry entry : maps) {
			NgxBlock block = (NgxBlock) entry;
			if (str.equals(block.getValue())) {
				return block;
			}
		}
		// 未找到
		return null;
	}

	/* 
	 * 
	 */
	@Override
	public synchronized String getUpstreamInMap(NgxBlock conf, String protocol,
			String monitorType, String monitorString, String mapName) {
		String mapValue = monitorType + SPACE + mapName;
		// 根据mapValue找到目标map
		NgxBlock map = findMapBolock(conf, protocol, mapValue);

		if (map!=null) {
			Iterator<NgxEntry> it = map.iterator();
			// 遍历目标map内容
			while (it.hasNext()) {
				NgxEntry item = it.next();
				if (item instanceof NgxParam) {
					NgxParam param = (NgxParam) item;
					String paramString = param.toString();
					if (paramString.contains(monitorString)) {
						// paramString内容为:"deploying "$8798forwardsplashcpu0";"
						String[] tempArray = paramString.split(" ");
						// 当tempArray[1]为如下"deploying "$8798forwardsplashcpu0";"这个字符串时，则返回内容为"8798forwardsplashcpu0"
						return tempArray[1].substring(2, tempArray[1].length() - 2);
					}
				}
			}
		}
		return null;
	}

	/* 
	 * 
	 */
	@Override
	public synchronized void addOrupdateMapBlock(NgxBlock conf,
			String protocol, String monitorType, String mapName,
			String monitorString, String upstreamName) {
		
		String mapValue = monitorType + SPACE + mapName;
		// 找出那个mapBlock
		NgxBlock mapBlock = findMapBolock(conf, protocol, mapValue);
		if (mapBlock == null) {
			//无map块，则加入
			NgxBlock proBlock = findProtocolBlock(conf, protocol);
			mapBlock = new NgxBlock();
			// 加入一个map
			mapBlock.addValue(MAP);
			// 加入上个map的value，
			mapBlock.addValue(mapValue);
			//构建map中value并加入
			String entry = monitorString + SPACE + "\"" + upstreamName + 0
					+ "\"";
			NgxParam param = addEntry(entry);
			mapBlock.addEntryAtLast(param);
			proBlock.addEntryAtLast(mapBlock);
		} else {
			//已有map
			//找是map种是否有相同monitorString
			Iterator<NgxEntry> it = mapBlock.iterator();
			while (it.hasNext()) {
				NgxEntry item = it.next();
				if (item.toString().contains(monitorString)) {
					return;
				}
			}
			// map中entry个数
			int counter = mapBlock.getEntries().size();
			String entry = monitorString + SPACE + "\"" + upstreamName
					+ counter + "\"";
			NgxParam param = addEntry(entry);
			// 当待加入的param已存在时，addEntry(param)无法将其加入
			mapBlock.addEntryAtLast(param);
		}

	}

	@Override
	public void addOrupdateMapUpstreamBlock(NgxBlock conf, String protocol,
			String upstreamValue, boolean isSticky, boolean isOldNgx,
			String monitorType, String mapName, String... entrys) {
		addOrupdateUpstreamBlock(conf, protocol, upstreamValue, isSticky,
				isOldNgx, entrys);
	}

}
