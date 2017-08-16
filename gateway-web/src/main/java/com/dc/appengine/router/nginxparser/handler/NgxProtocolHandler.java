package com.dc.appengine.router.nginxparser.handler;

import com.dc.appengine.router.nginxparser.NgxBlock;
import com.dc.appengine.router.nginxparser.handler.RouteItem.ProtocolType;
import com.dc.appengine.router.nginxparser.service.NgxService;

/**
 * @author liycq
 * 处理http块、stream块\tcp块
 */
public class NgxProtocolHandler extends Handler {

	@Override
	public void handleNgxConf(NgxService service, NgxBlock conf, RouteItem item,boolean isOldNgx) {
		switch (item.getOp()) {
		case ADD:
			String protocol = null;
			// 获取RouteItem中待添加协议类型
			ProtocolType type = item.getProtocol();
			
			///////////
			if (type.equals(ProtocolType.HTTP)
					|| type.equals(ProtocolType.HTTPS)) {
				protocol = NgxService.HTTP;
			} else {
				protocol = NgxService.TCP;
			}
			//////////
			
			if (service.findProtocolBlock(conf, protocol) == null) {
				// 原nginx.conf中无protocol对应的块
				if(protocol.equals(NgxService.TCP) && isOldNgx==false){
					//新版nginx，添加的tcp协议块为名称为stream
					service.addOrupdateProtocolBlock(conf, NgxService.NEWNGXTCPPROTOCOLNAME);
				}else {
					//
					service.addOrupdateProtocolBlock(conf, protocol);
				}
			}
			// 执行下一个Handler
			if (getNextHandler() != null) {
				getNextHandler().handleNgxConf(service, conf, item,isOldNgx);
			}
			break;
		case DELETE:
			// 执行下一个删除Handler
			if (getNextHandler() != null) {
				getNextHandler().handleNgxConf(service, conf, item,isOldNgx);
			}
			break;
		}
	}

}
