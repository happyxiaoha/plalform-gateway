package com.dc.appengine.router.nginxparser.handler;

import com.dc.appengine.router.config.RouterConfig;
import com.dc.appengine.router.nginxparser.service.NgxService;

public class NgxHandlerUtil {

	private static NgxHandlerUtil ngxHandlerUtil = null;

	public static NgxHandlerUtil getInstance() {
		if (ngxHandlerUtil != null) {
			return ngxHandlerUtil;
		} else {
			ngxHandlerUtil = new NgxHandlerUtil();
			return ngxHandlerUtil;
		}
	}

	public String getUpstreamName(RouteItem routeItem) {
		// "/printlog"
		// "printlog/"
		// "/"
		String inContext = routeItem.getInContext();
		String inPort = routeItem.getInPort();
		String upStreamName = null;

		if (inContext.contains("/")) {
			if (inContext.indexOf("/") == 0) {
				if (inContext.length() == 1) {
					// 当为"/".暂时只有http协议才会出现这种情况，tcp不会有这种情况
					upStreamName = inPort + NgxService.FORWARDSLASH;
				} else {
					// inContext "/printlog"
					inContext = inContext.substring(1);
					upStreamName = inPort + NgxService.FORWARDSLASH + inContext;
				}
			} else {
				// cputest1/
				upStreamName = inPort
						+ inContext.substring(0, inContext.length() - 1)
						+ NgxService.FORWARDSLASH;
			}
		} else {
			upStreamName = inPort + inContext;
		}
		return upStreamName;
	}

	public String getLocationName(RouteItem routeItem) {

		String inContext = routeItem.getInContext();
		String locationName = null;

		if (inContext.indexOf("/") == 0) {
			if (inContext.length() == 1) {
				// 当inContext为"/".暂时只有http协议才会出现这种情况，tcp不会有这种情况
				locationName = "";
			} else {
				// 当inContext为"/cputest1"时,变成"cputest1"
				locationName = inContext.substring(1);
			}
		} else {
			locationName = inContext;
		}
		return locationName;
	}

	/**
	 * 
	 * @param routeItem
	 * @return
	 */
	public String getProxyPassValue(RouteItem routeItem) {
		String proxyPass = null;
		String protocol = routeItem.getProtocol().name().toLowerCase();
		String outContext = routeItem.getOutContext();
		String monitorType = routeItem.getMonitorType();
		String monitorString = routeItem.getMonitorString();

		if (monitorString != null && monitorType != null) {
			proxyPass = NgxService.PROXY + NgxService.SPACE + protocol
					+ NgxService.COLON + NgxService.DSLASH
					+ getMapName(routeItem);
		} else {
			if (protocol.equals(NgxService.TCP)) {
				proxyPass = NgxService.PROXY + NgxService.SPACE
						+ getUpstreamName(routeItem);
			} else {
				String path = null;
				if (outContext.indexOf("/") == 0) {
					if (outContext.length() == 1) {
						path = NgxService.SLASH;
					} else {
						path = outContext;
					}
				} else {
					// NgxService.SLASH是正斜线
					path = NgxService.SLASH + outContext;
				}
				proxyPass = NgxService.PROXY + NgxService.SPACE + protocol
						+ NgxService.COLON + NgxService.DSLASH
						+ getUpstreamName(routeItem) + path;
			}
		}

		return proxyPass;
	}

	/**
	 * @param routeItem
	 * @return
	 */
	public String getMapName(RouteItem routeItem) {
		return NgxService.DOLLAR + getUpstreamName(routeItem);
	}

	public String getBackServerInfo(RouteItem routeItem, boolean isOldNgx) {
		String protocol = routeItem.getProtocol().name().toLowerCase();
		String outPort = routeItem.getOutPort();
		String outIp = routeItem.getOutIp();
		String weight = routeItem.getWeight();

		// ////////////2016年9月21日15:51:09
		RouterConfig routerConfig = RouterConfig.getInstance();
		String maxFails = "max_fails=" + routerConfig.getProperty("max_fails");
		String failTimeout = "fail_timeout="
				+ routerConfig.getProperty("fail_timeout");
		// ////////////--2016年9月21日15:51:09
		// ////////////// backServerInfo的值(示例)：server 127.0.0.1:9080 weight=2
		// max_fails=3
		// fail_timeout=60s
		String backServerInfo = null;
		if (protocol.equals("tcp") && isOldNgx == true) {
			backServerInfo = NgxService.SERVER + NgxService.SPACE + outIp
					+ NgxService.COLON + outPort;
		} else {
			backServerInfo = NgxService.SERVER + NgxService.SPACE + outIp
					+ NgxService.COLON + outPort + NgxService.SPACE + weight
					+ NgxService.SPACE + maxFails + NgxService.SPACE
					+ failTimeout;
		}
		// //////////////////

		return backServerInfo;
	}

}
