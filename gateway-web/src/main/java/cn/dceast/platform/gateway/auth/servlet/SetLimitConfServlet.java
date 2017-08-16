package cn.dceast.platform.gateway.auth.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dc.appengine.router.nginxparser.NgxBlock;
import com.dc.appengine.router.nginxparser.NgxConfig;
import com.dc.appengine.router.nginxparser.NgxDumper;
import com.dc.appengine.router.nginxparser.NgxEntry;
import com.dc.appengine.router.nginxparser.NgxParam;
import com.dc.appengine.router.nginxparser.service.NgxService;

public class SetLimitConfServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8786506397251706952L;
	private final String LOCATIONNAME = "~* \"^/([^/]+)/(.*)$\"";

	public static void main(String[] args) {
		SetLimitConfServlet setLimitConfServlet = new SetLimitConfServlet();
		try {
			setLimitConfServlet.service(null, null);
		} catch (ServletException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String limitRate = null;
		String limitConn = null;
		String limitReq = null;
		// String limitRate=req.getHeader("Limit-Rate");
		// String limitConn=req.getHeader("Limit-Conn");
		// String limitReq=req.getHeader("Limit-Req");
		// limitRate = "123";
		limitConn = "456";

		System.setProperty("com.dc.install_path",
				"D:/SmartPaaS2016��10��13��172439/AppRouter/configs/router_conf");
		final NgxService ngxService = new NgxService();
		NgxConfig ngxConfig = null;
		try {
			ngxConfig = ngxService.parse("D:/nested/nginx.conf");
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (limitRate != null) {
			NgxBlock server = ngxService.findServerBlock(ngxConfig, "http",
					"8011");
			if (server == null) {
				return;
			}
			List<NgxEntry> locations = server.findAll(NgxConfig.BLOCK,
					NgxService.LOCATION);
			// /////////
			for (NgxEntry entry : locations) {
				NgxBlock block = (NgxBlock) entry;
				if (LOCATIONNAME.equals(block.getValue())) {
					Iterator<NgxEntry> iter = block.iterator();
					while (iter.hasNext()) {
						NgxEntry tempEntry = iter.next();
						if (tempEntry.toString().contains(NgxService.LIMITRATE)) {
							block.remove(tempEntry);
						}
					}
					NgxParam param = new NgxParam();
					String newValue = NgxService.SET + " "
							+ NgxService.LIMITRATE + " " + limitRate;
					param.addValue(newValue);
					block.addEntryAtLast(param);
					new NgxDumper(ngxConfig).dump(System.out);
				}
			}
			// ///////////
		}

		/*
		 */
		if (limitReq != null) {
			NgxBlock protocolBlock = ngxService.findProtocolBlock(ngxConfig,
					"http");
			NgxParam param = new NgxParam();
			String newValue = NgxService.LIMITREQZONE + " "
					+ "$http_dceast_appkey zone=one:20m rate=" + limitReq
					+ "r/s";
			param.addValue(newValue);
			protocolBlock.updateEntry(param, NgxService.LIMITREQZONE);

			NgxBlock server = ngxService.findServerBlock(ngxConfig, "http",
					"8011");
			if (server == null) {
				return;
			}
			List<NgxEntry> locations = server.findAll(NgxConfig.BLOCK,
					NgxService.LOCATION);
			// /////////
			for (NgxEntry entry : locations) {
				NgxBlock block = (NgxBlock) entry;
				if (LOCATIONNAME.equals(block.getValue())) {
					Iterator<NgxEntry> iter = block.iterator();
					while (iter.hasNext()) {
						NgxEntry tempEntry = iter.next();
						if (tempEntry.toString().contains(NgxService.LIMITRATE)) {
							block.remove(tempEntry);
						}
					}
					NgxParam limitReqParam = new NgxParam();
					String limitReqNewValue = NgxService.LIMITREQ + " "+"one"+" "
							+ limitReq;
					limitReqParam.addValue(limitReqNewValue);
					block.addEntryAtFirst(limitReqParam);
					new NgxDumper(ngxConfig).dump(System.out);
				}
			}
			// ///////////
		}
		if (limitConn != null) {
			NgxBlock protocolBlock = ngxService.findProtocolBlock(ngxConfig,
					"http");
			NgxParam param = new NgxParam();
			String newValue = NgxService.LIMITCONNZONE + " "
					+ "$server_name zone=perserver:10m";
			param.addValue(newValue);
			protocolBlock.updateEntry(param, NgxService.LIMITCONNZONE);

			NgxBlock server = ngxService.findServerBlock(ngxConfig, "http",
					"8011");
			if (server == null) {
				return;
			}
			List<NgxEntry> locations = server.findAll(NgxConfig.BLOCK,
					NgxService.LOCATION);
			// /////////
			for (NgxEntry entry : locations) {
				NgxBlock block = (NgxBlock) entry;
				if (LOCATIONNAME.equals(block.getValue())) {
					Iterator<NgxEntry> iter = block.iterator();
					while (iter.hasNext()) {
						NgxEntry tempEntry = iter.next();
						if (tempEntry.toString().contains(NgxService.LIMITRATE)) {
							block.remove(tempEntry);
						}
					}
					NgxParam limitConnParam = new NgxParam();
					String limitConnNewValue = NgxService.LIMITCONN + " "+NgxService.LIMITNAME+
							" "+ limitConn;
					limitConnParam.addValue(limitConnNewValue);
					block.addEntryAtFirst(limitConnParam);
					new NgxDumper(ngxConfig).dump(System.out);
				}
			}
			// ///////////

		}

		// reload

		PrintWriter pw = resp.getWriter();
		pw.println("update limit Msg");
		pw.close();
	}

	@Override
	public void destroy() {
		System.out.println("======= destroy ======");
		super.destroy();
	}

	@Override
	public void init() throws ServletException {
		System.out.println("======= init without paramets ======");
		super.init();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		System.out.println("======= init with paramets ======");
		super.init(config);
	}
	
}
