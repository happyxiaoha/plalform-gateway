package com.dc.appengine.router.nginxparser.handler;

import com.dc.appengine.router.nginxparser.NgxBlock;
import com.dc.appengine.router.nginxparser.service.NgxService;

public abstract class Handler {
	
	protected Handler nextHandler = null;
	
	public void setNextHandler(Handler nextHandler){
		this.nextHandler = nextHandler;
	}
	public Handler getNextHandler() {
		return nextHandler;
	}
	
	public abstract void handleNgxConf(NgxService service,NgxBlock conf,RouteItem item,boolean isOldNgx);

}
