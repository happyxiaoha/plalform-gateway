package com.dc.appengine.router.nginxparser.handler;

import com.dc.appengine.router.config.RouterConfig;
import com.dc.appengine.router.nginxparser.NgxBlock;
import com.dc.appengine.router.nginxparser.service.NgxService;

public class NgxLocationHandler extends Handler {

	@Override
	public void handleNgxConf(NgxService service, NgxBlock conf,
			RouteItem item, boolean isOldNgx) {
		String port = item.getInPort();
		String protocol = item.getProtocol().name().toLowerCase();
		NgxHandlerUtil ngxHandlerUtil = NgxHandlerUtil.getInstance();
		String upstreamName = ngxHandlerUtil.getUpstreamName(item);
		String locationValue = ngxHandlerUtil.getLocationName(item);
		String proxyPass = ngxHandlerUtil.getProxyPassValue(item);

		// ////////////TCP协议不添加location块
		if (protocol.equals(NgxService.TCP)) {
			if (getNextHandler() != null) {
				getNextHandler().handleNgxConf(service, conf, item, isOldNgx);
			}
			return;
		}
		// /////////////

		if (protocol.equals("https")) {
			// http与https统一处理
			protocol = "http";
		}

		// 值为root html;index index.html index.htm;
		String location = RouterConfig.getInstance().getProperty(
				"nginx.location");
		String[] paramters = null;
		if (location != null) {
			String[] options = location.split(";");
			if (options.length > 0) {
				// parameters[0]为must值 parameter[1]为root html
				// parameter[2]为index.html index.htm
				paramters = new String[options.length + 1];
				// 将options中内容复制到parameters中
				System.arraycopy(options, 0, paramters, 1, options.length);
			}
		} else {
			// location为null，则parameters只存must
			paramters = new String[1];
		}
		paramters[0] = proxyPass;

		switch (item.getOp()) {
		case ADD:
			service.addOrupdateLocationBlock(conf, protocol, port,
					locationValue, paramters);
			if (getNextHandler() != null) {
				getNextHandler().handleNgxConf(service, conf, item, isOldNgx);
			}
			break;
		case DELETE:
			if (getNextHandler() != null) {
				getNextHandler().handleNgxConf(service, conf, item, isOldNgx);
			}
			if (service.findUpstreamBlock(conf, protocol, upstreamName) == null) {
				service.deletelocationBlock(conf, protocol, port, locationValue);
			}
			break;
		}
	}
}
