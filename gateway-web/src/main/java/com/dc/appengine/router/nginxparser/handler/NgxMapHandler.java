package com.dc.appengine.router.nginxparser.handler;

import com.dc.appengine.router.nginxparser.NgxBlock;
import com.dc.appengine.router.nginxparser.service.NgxService;

public class NgxMapHandler extends Handler {

	@Override
	public void handleNgxConf(NgxService service, NgxBlock conf,
			RouteItem item, boolean isOldNgx) {
		String monitorType = item.getMonitorType();
		String monitorString = item.getMonitorString();
		NgxHandlerUtil ngxHandlerUtil = NgxHandlerUtil.getInstance();
		String mapName = ngxHandlerUtil.getMapName(item);

		if (monitorType == null || monitorString == null) {
			// 不加map块
			if (getNextHandler() != null) {
				getNextHandler().handleNgxConf(service, conf, item, isOldNgx);
			}
		} else {
			String protocol = NgxService.HTTP;
			switch (item.getOp()) {
			case ADD:
				service.addOrupdateMapBlock(conf, protocol, monitorType,
						mapName, monitorString, mapName);
				if (getNextHandler() != null) {
					getNextHandler().handleNgxConf(service, conf, item,
							isOldNgx);
				}
				break;
			case DELETE:
				if (getNextHandler() != null) {
					// 先去删upstream
					getNextHandler().handleNgxConf(service, conf, item,
							isOldNgx);
				}
				String upstreamName = service.getUpstreamInMap(conf, protocol,
						monitorType, monitorString, mapName);
				if (service.findUpstreamBlock(conf, protocol, upstreamName) == null) {
					
					service.deleteMapEntry(conf, protocol, monitorType
							+ NgxService.SPACE + mapName, upstreamName);
				}
				break;
			}
		}

	}

}
