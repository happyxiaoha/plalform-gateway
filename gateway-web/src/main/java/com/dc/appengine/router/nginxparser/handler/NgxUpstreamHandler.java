package com.dc.appengine.router.nginxparser.handler;

import com.dc.appengine.router.config.RouterConfig;
import com.dc.appengine.router.nginxparser.NgxBlock;
import com.dc.appengine.router.nginxparser.service.NgxService;

public class NgxUpstreamHandler extends Handler {

	@Override
	public void handleNgxConf(NgxService service, NgxBlock conf,
			RouteItem item, boolean isOldNgx) {
		String port = item.getInPort();
		String protocol = item.getProtocol().name().toLowerCase();
		String monitorType = item.getMonitorType();
		String monitorString = item.getMonitorString();
		NgxHandlerUtil ngxHandlerUtil = NgxHandlerUtil.getInstance();
		String mapName = ngxHandlerUtil.getMapName(item);
		String upstreamName = ngxHandlerUtil.getUpstreamName(item);
		String backServerInfo = ngxHandlerUtil
				.getBackServerInfo(item, isOldNgx);

		// if (monitorType == null || monitorString == null) {
		// //// 不加map块
		//
		// }else {
		//
		// }

		if (protocol.equals("https")) {
			// 将http和https统一处理
			protocol = "http";
		}

		// 读取配置信息
		// 2016-9-21 15:46:56 config.properties中已无nginx.upstream此项
		RouterConfig routerConfig = RouterConfig.getInstance();
		String upstream = routerConfig.getProperty("nginx.upstream");

		// ??? 向upstream块中新增的一些参数
		String[] paramters = null;

		// /////////////////////
		if (upstream != null) {
			// 执行向nginx.conf的upstream块中添加信息的准备工作
			String[] options = upstream.split(";");
			if (options.length > 0) {
				paramters = new String[options.length + 1];
				System.arraycopy(options, 0, paramters, 1, options.length);
			}
		} else {
			// ？？？ config.properties中无配置信息，则仅增加一条后端服务器内容
			paramters = new String[1];
		}
		// //////////////////////

		//
		paramters[0] = backServerInfo;

		switch (item.getOp()) {
		case ADD:
			boolean isSticky = item.isSticky();
			// 向protocol块中名为upstreamName的upstream信息块添加信息
			if (protocol.equals(NgxService.TCP) && !isOldNgx) {
				// 处理Nginx1.10的tcp模块
				service.addOrupdateUpstreamBlock(conf,
						NgxService.NEWNGXTCPPROTOCOLNAME, upstreamName,
						isSticky, false, paramters);
			} else {
				String upstreamNameInMap = service.getUpstreamInMap(conf,
						protocol, monitorType, monitorString, mapName);
				if (upstreamNameInMap != null) {
					service.addOrupdateMapUpstreamBlock(conf, protocol,
							upstreamNameInMap, isSticky, isOldNgx, monitorType,
							mapName, paramters);
				} else {
					// 处理无map块时upstream添加
					service.addOrupdateUpstreamBlock(conf, protocol,
							upstreamName, isSticky, isOldNgx, paramters);
				}
			}
			// 执行处理链上的下一个类
			if (getNextHandler() != null) {
				getNextHandler().handleNgxConf(service, conf, item, isOldNgx);
			}
			break;
		case DELETE:
			// 删掉protocol下的upstreamName块下的parameters
			if (protocol.equals(NgxService.TCP) && !isOldNgx) {

				service.deleteUpstreamEntry(conf,
						NgxService.NEWNGXTCPPROTOCOLNAME, upstreamName,
						isOldNgx, paramters);
			} else {
				String upstreamNameInMap = service.getUpstreamInMap(conf,
						protocol, monitorType, monitorString, mapName);
				if (upstreamNameInMap != null) {
					service.deleteUpstreamEntry(conf, protocol,
							upstreamNameInMap, isOldNgx, paramters);
				} else {
					// 处理无map块时upstream删除
					service.deleteUpstreamEntry(conf, protocol, upstreamName,
							isOldNgx, paramters);
				}
			}
			if (getNextHandler() != null) {
				getNextHandler().handleNgxConf(service, conf, item, isOldNgx);
			}
			break;
		}

	}

}
