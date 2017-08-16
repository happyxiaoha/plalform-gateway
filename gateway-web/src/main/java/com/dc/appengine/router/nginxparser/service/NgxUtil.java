package com.dc.appengine.router.nginxparser.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;

import org.iso_relax.dispatcher.IslandSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dc.appengine.router.config.RouterConfig;
import com.dc.appengine.router.nginxparser.NgxConfig;
import com.dc.appengine.router.nginxparser.NgxDumper;
import com.dc.appengine.router.nginxparser.handler.Handler;
import com.dc.appengine.router.nginxparser.handler.NgxLocationHandler;
import com.dc.appengine.router.nginxparser.handler.NgxProtocolHandler;
import com.dc.appengine.router.nginxparser.handler.NgxServerHandler;
import com.dc.appengine.router.nginxparser.handler.NgxUpstreamHandler;
import com.dc.appengine.router.nginxparser.handler.RouteItem;

public class NgxUtil {
	private static final Logger log = LoggerFactory.getLogger(NgxUtil.class);

	private Handler h1;
	private NgxService service;

	private NgxUtil() {
		service = new NgxService();

		h1 = new NgxProtocolHandler();
		Handler h2 = new NgxServerHandler();
		Handler h3 = new NgxUpstreamHandler();
		Handler h4 = new NgxLocationHandler();

		h1.setNextHandler(h2);
		h2.setNextHandler(h3);
		h3.setNextHandler(h4);

	}

	private static NgxUtil instance = new NgxUtil();

	public static NgxUtil getIntance() {
		return instance;
	}

	/**
	 * 
	 * @param path
	 * @throws NgxHandleException
	 */
	public synchronized void reloadRouteTable(String path) throws NgxHandleException {

		// 执行cmd脚本
		StringBuffer cmd = new StringBuffer(path);
		cmd.append("/sbin/nginx -s reload");

		if (log.isDebugEnabled()) {
			log.debug("running the shell:" + cmd.toString());
		}

		try {
			Process proc = Runtime.getRuntime().exec(cmd.toString());
			InputStream stderr = proc.getErrorStream();
			InputStreamReader isr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null)
				if (log.isDebugEnabled()) {
					log.debug("shell output:" + line);
				}
			int exitVal = proc.waitFor();
			if (log.isDebugEnabled()) {
				log.debug("reload nginx result:" + (exitVal == 0));
			}

		} catch (Exception e) {
			throw new NgxHandleException("reload nginx error", e);
		}
	}

	/**
	 * 当AppRouter重启时更新路由表的方法
	 * 
	 * @param item
	 * @param path
	 * @param isOldNgx
	 * @throws NgxHandleException
	 * @throws IOException
	 */
	public synchronized void updateRouteTableWhenStartRouter(RouteItem item, String path, boolean isOldNgx)
			throws NgxHandleException, IOException {
		// nginx现用配置文件
		String nowFileString = path + File.separatorChar + "conf" + File.separatorChar + "nginx.conf";
		// 处理请求
		try {
			NgxConfig conf = service.parse(nowFileString);
			h1.handleNgxConf(service, conf, item, isOldNgx);
			// 导入
			new NgxDumper(conf).dump(new FileOutputStream(nowFileString));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * nio实现将sourcefile覆盖复制到destfile中
	 * 
	 * @param sourceFile
	 * @param destFile
	 * @return
	 * @throws IOException
	 */
	public void fileTransfer(File sourceFile, File destFile) throws IOException {
		int length = 2097152;
		FileInputStream sourceFileInputStream = new FileInputStream(sourceFile);
		FileOutputStream sourceFileOutputStream = new FileOutputStream(destFile);
		FileChannel inC = sourceFileInputStream.getChannel();
		FileChannel outC = sourceFileOutputStream.getChannel();
		int i = 0;
		while (true) {
			if (inC.position() == inC.size()) {
				inC.close();
				outC.close();
				return;
			}
			if ((inC.size() - inC.position()) < 20971520)
				length = (int) (inC.size() - inC.position());
			else {
				length = 20971520;
			}
			inC.transferTo(inC.position(), length, outC);
			inC.position(inC.position() + length);
			i++;
		}
	}
	

	public synchronized void updateRouteTable(String path, NgxConfig ngxConfig) {
		// nginx现用配置文件
		String file = path + File.separatorChar + "conf" + File.separatorChar + "nginx.conf";
		// nginx临时文件
		String tmp = path + File.separatorChar + "conf" + File.separatorChar + "nginx.tmp";
		// nginx备份文件
		File bak = new File(path + File.separatorChar + "conf" + File.separatorChar + "nginx.conf.bak");
		File ngxfile = new File(file);
		File tmpfile = new File(tmp);
		// /////////////////////////////////////////
		try {
			new NgxDumper(ngxConfig).dump(new FileOutputStream(tmp));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 当成功生成了配置文件时
		if (tmpfile.exists()) {
			// 删掉原有的备份文件
			if (bak.exists()) {
				bak.delete();
			}
			// 将现nginx.conf重命名为bak
			ngxfile.renameTo(bak);
			// 将nginx临时文件重命名为
			tmpfile.renameTo(ngxfile);
		} else {
			// 未成功生成配置文件时
			if (log.isDebugEnabled())
				log.debug("tmp file is not found");
		}

	}

	public synchronized void updateRouteTable(RouteItem item, String path, boolean isOldNgx) throws NgxHandleException {
		try {

			// ///////////////////////////////////////
			// nginx现用配置文件
			String file = path + File.separatorChar + "conf" + File.separatorChar + "nginx.conf";
			// nginx临时文件
			String tmp = path + File.separatorChar + "conf" + File.separatorChar + "nginx.tmp";
			// nginx备份文件
			File bak = new File(path + File.separatorChar + "conf" + File.separatorChar + "nginx.conf.bak");
			File ngxfile = new File(file);
			File tmpfile = new File(tmp);
			// /////////////////////////////////////////

			// Ngx配置块
			NgxConfig conf = service.parse(file);
			// 处理请求
			h1.handleNgxConf(service, conf, item, isOldNgx);
			new NgxDumper(conf).dump(new FileOutputStream(tmp));

			// 当成功生成了配置文件时
			if (tmpfile.exists()) {
				// 删掉原有的备份文件
				if (bak.exists()) {
					bak.delete();
				}
				// 将现nginx.conf重命名为bak
				ngxfile.renameTo(bak);
				// 将nginx临时文件重命名为
				tmpfile.renameTo(ngxfile);
			} else {
				// 未成功生成配置文件时
				if (log.isDebugEnabled())
					log.debug("tmp file is not found");
			}
		} catch (Exception e) {
			throw new NgxHandleException("nginx update error", e);
		}
	}

	/**
	 * 测试类，谁写的啊。。。2016年10月21日19:28:50
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		NgxUtil util = NgxUtil.getIntance();
		// util.updateRouteTable(null, "c:");

		// util.reloadRouteTable("D:/nginx-1.8.0");
		String must = "xxoo";
		String server = RouterConfig.getInstance().getProperty("nginx.xx");
		String[] paramters = null;
		if (server != null) {
			String[] options = server.split(";");
			if (options.length > 0) {
				paramters = new String[options.length + 1];
				System.arraycopy(options, 0, paramters, 1, options.length);

			}
		} else {
			paramters = new String[1];
		}
		paramters[0] = must;
		System.out.println(paramters.length);
	}
}
