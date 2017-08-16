package cn.dceast.platform.gateway.auth.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.dceast.platform.gateway.auth.data.entity.Message;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dc.appengine.router.nginxparser.NgxBlock;
import com.dc.appengine.router.nginxparser.NgxConfig;
import com.dc.appengine.router.nginxparser.NgxDumper;
import com.dc.appengine.router.nginxparser.NgxEntry;
import com.dc.appengine.router.nginxparser.NgxParam;
import com.dc.appengine.router.nginxparser.service.NgxHandleException;
import com.dc.appengine.router.nginxparser.service.NgxService;
import com.dc.appengine.router.nginxparser.service.NgxUtil;

@RestController
public class SetLimitConf {

	public static void main(String[] args) {

		SetLimitConf setLimitConf = new SetLimitConf();
		try {
			setLimitConf.setlimitconf(null, null, null);
		} catch (ServletException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private static Logger logger = LoggerFactory.getLogger(SetLimitConf.class);
	private static final long serialVersionUID = -8786506397251706952L;
	// 待更新Lcation
	private final String LOCATIONNAME = "~* \"^/([^/]+)/(.*)$\"";

	@RequestMapping(value = "/setlimitconf", method = { RequestMethod.POST, RequestMethod.GET })
	protected String setlimitconf(HttpServletRequest req, HttpServletResponse resp, String data)
			throws ServletException, IOException {

		Message msg = new Message();
		// String limitRate = req.getHeader("Limit-Rate");
		// String limitConn = req.getHeader("Limit-Conn");
		// String limitReq = req.getHeader("Limit-Req");

		String limitRate = null;
		String limitConn = null;
		String limitReq = null;
		if (data == null || data.equals("")) {
			msg.setCode("999999");
			msg.setMessage("未设置信息");
			msg.setResult("faied");
			msg.setStatus("faied");
			return new JSONObject().toJSONString(msg);
		}

		JSONArray jso = new JSONObject().parseArray(data);
		List<Map<String, Object>> orderList = JSONObject.toJavaObject(jso, List.class);
		for (Map<String, Object> singleOrder : orderList) {
			limitRate = ((String) singleOrder.get("limitRate"));
			limitConn = ((String) singleOrder.get("limitConn"));
			limitReq = ((String) singleOrder.get("limitReq"));
		}

		//////////////////////

		//////////////////////

		System.setProperty("com.dc.install_path", "/router_conf");
		final NgxService ngxService = new NgxService();
		String nginxLocation = "/home/liycq/lua/nginx1.10.2";
		String nginxConfPath = nginxLocation + "/conf/nginx.conf";
		NgxConfig ngxConfig = null;
		try {
			ngxConfig = ngxService.parse(nginxConfPath);
		} catch (Exception e) {
			e.printStackTrace();
		}

		/////////////////////////////////// 处理LimitRate
		if (limitRate != null) {
			// 找到listen 48011的server
			NgxBlock server = ngxService.findServerBlock(ngxConfig, "http", "48011");
			if (server == null) {
				msg.setCode("999999");
				msg.setMessage("未设置相关Server");
				msg.setResult("faied");
				msg.setStatus("faied");
				return new JSONObject().toJSONString(msg);
			}
			// 得到Server中所有locations集合
			List<NgxEntry> locations = server.findAll(NgxConfig.BLOCK, NgxService.LOCATION);
			// /////////
			for (NgxEntry entry : locations) {
				NgxBlock block = (NgxBlock) entry;
				// 找到目标location
				if (LOCATIONNAME.equals(block.getValue())) {
					Iterator<NgxEntry> iter = block.iterator();
					//////// 遍历此location中所有内容并删掉原$limit_rate内容
					while (iter.hasNext()) {
						NgxEntry tempEntry = iter.next();
						if (tempEntry.toString().contains(NgxService.LIMITRATE)) {
							iter.remove();
							// block.remove(tempEntry);
						}
					}
					if (!limitRate.equals("0")) {
						//////// 加入新的
						NgxParam param = new NgxParam();
						String newValue = NgxService.SET + " " + NgxService.LIMITRATE + " " + limitRate;
						param.addValue(newValue);
						block.addEntryAtLast(param);
					}

					///////
				}
			}
			// ///////////
		}
		//////////////////////////////////

		/*
		 */
		////////////////////////
		if (limitReq != null) {
			/////////// 找到http块中limit_req_zone字段并更新，Nginx.conf中必须预设limit_req_zone字段
			NgxBlock protocolBlock = ngxService.findProtocolBlock(ngxConfig, "http");
			NgxParam param = new NgxParam();
			String newValue = NgxService.LIMITREQZONE + " " + "$http_dceast_appkey zone=one:20m rate=" + limitReq
					+ "r/s";
			param.addValue(newValue);
			protocolBlock.updateEntry(param, NgxService.LIMITREQZONE);
			//////////

			NgxBlock server = ngxService.findServerBlock(ngxConfig, "http", "48011");
			if (server == null) {
				msg.setCode("999999");
				msg.setMessage("未设置相关Server");
				msg.setResult("faied");
				msg.setStatus("faied");
				return new JSONObject().toJSONString(msg);
			}
			List<NgxEntry> locations = server.findAll(NgxConfig.BLOCK, NgxService.LOCATION);
			// /////////
			for (NgxEntry entry : locations) {
				NgxBlock block = (NgxBlock) entry;
				if (LOCATIONNAME.equals(block.getValue())) {
					Iterator<NgxEntry> iter = block.iterator();
					// 删掉原来的$limit_rate字段
					while (iter.hasNext()) {
						NgxEntry tempEntry = iter.next();
						if (tempEntry.toString().contains(NgxService.LIMITREQ)) {
							// block.remove(tempEntry);
							iter.remove();
						}
					}
					if (!limitReq.equals("0")) {
						NgxParam limitReqParam = new NgxParam();
						String limitReqNewValue = NgxService.LIMITREQ + " " + "one" + " " + limitReq;
						limitReqParam.addValue(limitReqNewValue);
						block.addEntryAtFirst(limitReqParam);
					}
				}
			}
			// ///////////
		}
		//////////////////////////////////////

		////////////////////////////////////////
		if (limitConn != null) {
			//////////////////
			NgxBlock protocolBlock = ngxService.findProtocolBlock(ngxConfig, "http");
			NgxParam param = new NgxParam();
			String newValue = NgxService.LIMITCONNZONE + " " + "$server_name zone=perserver:10m";
			param.addValue(newValue);
			protocolBlock.updateEntry(param, NgxService.LIMITCONNZONE);
			//////////////////

			NgxBlock server = ngxService.findServerBlock(ngxConfig, "http", "48011");
			if (server == null) {
				msg.setCode("999999");
				msg.setMessage("未设置相关Server");
				msg.setResult("faied");
				msg.setStatus("faied");
				return new JSONObject().toJSONString(msg);
			}
			List<NgxEntry> locations = server.findAll(NgxConfig.BLOCK, NgxService.LOCATION);
			// /////////
			for (NgxEntry entry : locations) {
				NgxBlock block = (NgxBlock) entry;
				if (LOCATIONNAME.equals(block.getValue())) {
					Iterator<NgxEntry> iter = block.iterator();
					while (iter.hasNext()) {
						NgxEntry tempEntry = iter.next();
						if (tempEntry.toString().contains(NgxService.LIMITCONN)) {
							// block.remove(tempEntry);
							iter.remove();
						}
					}

					if (!limitConn.equals("0")) {
						NgxParam limitConnParam = new NgxParam();
						String limitConnNewValue = NgxService.LIMITCONN + " " + NgxService.LIMITNAME + " " + limitConn;
						limitConnParam.addValue(limitConnNewValue);
						block.addEntryAtFirst(limitConnParam);
					}
				}
			}
			// ///////////
		}
		///////////////////////////////////////

		// new NgxDumper(ngxConfig).dump(System.out);
		// reload
		try {
			NgxUtil.getIntance().updateRouteTable(nginxLocation, ngxConfig);
			NgxUtil.getIntance().reloadRouteTable(nginxLocation);
		} catch (Exception e) {
			msg.setCode("999999");
			msg.setMessage("更新路由表失败");
			msg.setResult("faied");
			msg.setStatus("faied");
			e.printStackTrace();
		}

		msg.setCode("000000");
		msg.setMessage("限流设置更新成功");
		msg.setResult("success");
		msg.setStatus("success");
		return new JSONObject().toJSONString(msg);
	}
}