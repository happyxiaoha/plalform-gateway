package com.dc.appengine.router.nginxparser.handler;

import com.dc.appengine.router.config.RouterConfig;
import com.dc.appengine.router.nginxparser.NgxBlock;
import com.dc.appengine.router.nginxparser.service.NgxService;

/**
 * 处理http块、tcp/stream块中的server信息块
 */
public class NgxServerHandler extends Handler {

	@Override
	public void handleNgxConf(NgxService service, NgxBlock conf,
			RouteItem item, boolean isOldNgx) {

		NgxHandlerUtil ngxHandlerUtil = NgxHandlerUtil.getInstance();
		String upstreamName = ngxHandlerUtil.getUpstreamName(item);
		String limitConn = item.getlimitConn();
		String port = item.getInPort();
		// 待处理协议
		String protocol = item.getProtocol().name().toLowerCase();

		// ///// 读取存储在cofig.properties中的有关server附加信息
		String server = null;
		if (protocol.equals("http")) {
			server = RouterConfig.getInstance().getProperty(
					"nginx.server." + protocol);
		} else if (protocol.equals("https")) {
			if (item.isTwoway()) {
				// https为双向认证
				// 读取config.properties配置信息
				server = RouterConfig.getInstance().getProperty(
						"nginx.server." + protocol + ".twoway");
			} else {
				// 单向认证
				server = RouterConfig.getInstance().getProperty(
						"nginx.server." + protocol + ".singleway");
			}
		} else {
			// 读取有关tcp协议的配置文件
			if (isOldNgx) {
				// nginx 1.8.1
				server = RouterConfig.getInstance().getProperty(
						"nginx.server." + protocol);
			} else {
				// nginx 1.10.1
				server = RouterConfig.getInstance().getProperty(
						"nginx.server.stream." + protocol);
				// 值：so_keepalive=on;
				String listenAppend = RouterConfig.getInstance().getProperty(
						"nginx.server.stream.listen");

				// ////nginx 1.10.1 tcp端口很特别，不仅仅是一个数字，而是数字加一段字符串形式
				if (listenAppend != null) {
					listenAppend = listenAppend.substring(0,
							listenAppend.length() - 1);
					// 示例：12345 so_keepalive=on
					port = port + NgxService.SPACE + listenAppend;
				}
				// ////
			}
		}
		// ///// --读取存储在cofig.properties中的有关server附加信息

		// ///
		if (protocol.equals("https")) {
			protocol = "http";
		}
		// //

		// Server块中待添加参数
		String[] paramters = null;
		// 存储server块中的配置信息
		String[] options = null;
		if (server != null) {
			// 分割读取的server块中的配置信息
			options = server.split(";");
			// 限制并发连接数
			if (limitConn != null) {
				paramters = new String[options.length + 1];
				System.arraycopy(options, 0, paramters, 1, options.length);
				paramters[0] = NgxService.LIMITCONN + NgxService.SPACE
						+ NgxService.LIMITNAME + NgxService.SPACE + limitConn;
				options = paramters;
			}
			// --限制并发连接数
		}

		// //// 处理TCP信息块
		if (NgxService.TCP.equals(protocol)) {
			// Server块中的proxy_pass字段
			String proxypass = null;
			// 示例： proxy_pass 12345InContext
			proxypass = ngxHandlerUtil.getProxyPassValue(item);

			// ///构建tcp块所有附加信息
			if (options.length > 0) {
				paramters = new String[options.length + 1];
				System.arraycopy(options, 0, paramters, 1, options.length);
			} else {
				// server块中无其他配置信息
				paramters = new String[1];
			}
			paramters[0] = proxypass;
			options = paramters;
			// ///--构建tcp块所有附加信息
		}
		// ////--处理TCP信息块

		switch (item.getOp()) {
		case ADD:
			if (service.findServerBlock(conf, protocol, port) == null) {
				// / 向http块或tcp块(stream块)的server块中添加config.properties中读取的配置信息
				if (options == null) {
					if (protocol.equals(NgxService.TCP) && !isOldNgx) {
						// tcp协议且为新版nginx，向stream块中添加server块
						service.addOrupdateServerBlock(conf,
								NgxService.NEWNGXTCPPROTOCOLNAME, port);
					} else {
						// 向protocol块中添加server块
						service.addOrupdateServerBlock(conf, protocol, port);
					}

				} else {
					if (protocol.equals(NgxService.TCP) && !isOldNgx) {
						// tcp协议且为新版nginx，向stream块中添加server块
						service.addOrupdateServerBlock(conf,
								NgxService.NEWNGXTCPPROTOCOLNAME, port, options);
					} else {
						// 向protocol块中添加server块
						service.addOrupdateServerBlock(conf, protocol, port,
								options);
					}
				}
				// /
			}
			// 执行下一个处理类
			if (getNextHandler() != null) {
				getNextHandler().handleNgxConf(service, conf, item, isOldNgx);
			}
			break;
		case DELETE:
			// 先执行下一个Handler
			if (getNextHandler() != null) {
				getNextHandler().handleNgxConf(service, conf, item, isOldNgx);
			}

			// 处理tcp协议
			if (NgxService.TCP.equals(protocol)) {
				// 如果找不到某个Server对应的Upstream块，则删掉这个Server块
				if (isOldNgx) {
					if (service.findUpstreamBlock(conf, protocol, upstreamName) == null) {
						// 在Protocol块中找不到某个Server对应的Upstream块，则删掉这个Server块
						service.deleteServerBlock(conf, protocol, port);
					}
				} else {
					// 在stream块中找不到某个Server对应的Upstream块，则删掉这个Server块
					if (service.findUpstreamBlock(conf,
							NgxService.NEWNGXTCPPROTOCOLNAME, upstreamName) == null) {
						service.deleteServerBlock(conf,
								NgxService.NEWNGXTCPPROTOCOLNAME, port);
					}
				}

			} else {
				// 处理对HTTP中Server块的删除工作
				// 本Server块中Location块为0个且没有upStreamName对应的upstream块
				if (service.findLocationBlock(conf, protocol, port).size() == 0
						&& service.findUpstreamBlock(conf, protocol,
								upstreamName) == null) {
					// 根据inPort删掉Server块
					service.deleteServerBlock(conf, protocol, port);
				}
			}
			break;
		}
	}
}
