package com.dc.appengine.router.test;

import com.dc.appengine.router.nginxparser.NgxConfig;
import com.dc.appengine.router.nginxparser.NgxDumper;
import com.dc.appengine.router.nginxparser.handler.Handler;
import com.dc.appengine.router.nginxparser.handler.NgxLocationHandler;
import com.dc.appengine.router.nginxparser.handler.NgxMapHandler;
import com.dc.appengine.router.nginxparser.handler.NgxProtocolHandler;
import com.dc.appengine.router.nginxparser.handler.NgxServerHandler;
import com.dc.appengine.router.nginxparser.handler.NgxUpstreamHandler;
import com.dc.appengine.router.nginxparser.handler.RouteItem;
import com.dc.appengine.router.nginxparser.handler.RouteItem.Opnum;
import com.dc.appengine.router.nginxparser.handler.RouteItem.ProtocolType;
import com.dc.appengine.router.nginxparser.service.NgxService;

/**
 * @author liycq
 * 测试限流的
 */
public class Client {
	public static void main(String[] args) throws Exception {

		// /////////////读取配置文件及初始化
		// 设置com.dc.install_path属性值，否则单独运行client.java报错
		System.setProperty("com.dc.install_path",
				"D:/SmartPaaS2016年10月13日172439/AppRouter/configs/router_conf");
		final NgxService ngxService = new NgxService();
		// 读入nginx.conf中信息块头信息
		final NgxConfig ngxConfig = ngxService
				.parse("D:/nested/nginx830new.conf");
		// ////////////--读取配置文件及初始化

		// /////////////////////////////设置处理链
		final Handler h1 = new NgxProtocolHandler();
		// 处理Server块
		Handler h2 = new NgxServerHandler();
		Handler h5 = new NgxMapHandler();
		// 处理Upstream块
		Handler h3 = new NgxUpstreamHandler();
		// 处理Location块
		Handler h4 = new NgxLocationHandler();
		h1.setNextHandler(h2);
		h2.setNextHandler(h5);
		h5.setNextHandler(h3);
		h3.setNextHandler(h4);
		// /////////////////////////////--设置处理链

		// 当inContext为printlog/（即一个正常字符串加上斜线后），构建了如下错误的server块
		// 2016年9月13日21:46:32
		// 上下文为printlog/时
		// server {
		// listen 23456;
		// proxy_set_header Host $host;
		// client_max_body_size 10m;
		// client_body_buffer_size 128k;
		// location /rintlog/ { //这里应该是/printlog/，而不是/rintlog/
		// proxy_pass http://23456rintlog//printlog/;
		// root html;
		// index index.html index.htm;
		// }
		// }
		// upstream 23456rintlog/ {
		// server 10.126.3.86:7005 weight=1;
		// }

		final RouteItem item6 = new RouteItem(Opnum.ADD, ProtocolType.HTTPS,
				"8798", "/cpu", "7003", "127.0.0.2", "/cpu", "weight=1",
				"$http_user_agent", "deploying", "2000");
		item6.setSticky(true);

		final RouteItem item62 = new RouteItem(Opnum.ADD, ProtocolType.HTTP,
				"8798", "/cpu", "7003", "127.0.0.3", "/cpu", "weight=1",
				"$http_user_agent", "deploying", null);

		final RouteItem item7 = new RouteItem(Opnum.DELETE, ProtocolType.HTTP,
				"8798", "/cpu", "7003", "127.0.0.3", "/cpu", "weight=1",
				"$http_user_agent", "deploying", null);
		item6.setSticky(true);

		// final RouteItem item8 = new RouteItem(Opnum.DELETE,
		// ProtocolType.HTTP,
		// "8798", "printlog/", "7001", "127.0.0.2", "printlog/",
		// "weight=1",null,null);
		// item8.setSticky(true);

		// ////////////////// 利用新线程添加TCP/HTTP协议信息块
		for (int i = 0; i < 1; i++)
			new Thread(new Runnable() {
				@Override
				public void run() {
					// h1.handleNgxConf(ngxService, ngxConfig, item6, false);
					// h1.handleNgxConf(ngxService, ngxConfig, item62, false);
					h1.handleNgxConf(ngxService, ngxConfig, item6, false);
					// h1.handleNgxConf(ngxService, ngxConfig, item8, false);
				}
			}).start();
		// /////////////////--利用新线程添加TCP/HTTP协议信息块

		// 等待子线程处理完成
		Thread.sleep(5000);
		// 控制台输出最新配置信息
		new NgxDumper(ngxConfig).dump(System.out);

		String str = "server 127.0.0.1:8778 weight=10";
	}
}
