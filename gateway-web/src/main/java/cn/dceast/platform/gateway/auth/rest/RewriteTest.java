package cn.dceast.platform.gateway.auth.rest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dc.appengine.router.nginxparser.service.NgxHandleException;
import com.dc.appengine.router.nginxparser.service.NgxUtil;

import cn.dceast.platform.gateway.auth.data.entity.Message;

@RestController
public class RewriteTest {

	//
	public static final String NGINXLOCATION = "/home/liycq/lua/nginx1.10.2";

	@RequestMapping(value = "/testdyups", method = { RequestMethod.POST, RequestMethod.GET })
	public String dyups(HttpServletRequest req, HttpServletResponse resp, String data) {

		Message msg = new Message();
		if (data == null || data.equals("")) {
			msg.setCode("999999");
			msg.setMessage("未设置Rewrite信息");
			msg.setResult("faied");
			msg.setStatus("faied");
			return new JSONObject().toJSONString(msg);
		}

		List<String> oldList = null;
		try {
			oldList = readIfConf(NGINXLOCATION);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// 解析出data内容
		JSONArray jso = new JSONObject().parseArray(data);
		List<Map<String, Object>> orderList = JSONObject.toJavaObject(jso, List.class);
		List<String> newList = new ArrayList<String>();
		for (Map<String, Object> singleOrder : orderList) {
			// "rule": "false",
			String rule = (String) singleOrder.get("rule");
			String flag = (String) singleOrder.get("flag");

			for (int i = 0; i < oldList.size(); i++) {
				String oldLineString = oldList.get(i);
				if (oldLineString.contains(flag)) {
					oldList.remove(i);
					break;
				}
			}
			newList.add("if ($http_" + flag + " = " + rule + ") { set $" + flag + " \"false\"; }");
			// 写入文件
		}

		newList.addAll(oldList);
		try {
			writeFile(NGINXLOCATION, newList);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// reload
		try {
			NgxUtil.getIntance().reloadRouteTable(NGINXLOCATION);
		} catch (NgxHandleException e) {
			e.printStackTrace();
		}

		msg.setCode("000000");
		msg.setMessage("设置成功");
		msg.setResult("success");
		msg.setStatus("success");
		return new JSONObject().toJSONString(msg);

	}

	/**
	 * 
	 * @param nginxLocation
	 * @return
	 * @throws FileNotFoundException
	 */
	public synchronized List<String> readIfConf(String nginxLocation) throws FileNotFoundException {

		File nowFile = new File(nginxLocation + File.separatorChar + "conf" + File.separatorChar + "if.conf");
		// 遍历一遍现文件，读入List
		List<String> oldList = new ArrayList<String>();
		FileReader fr = new FileReader(nowFile);
		BufferedReader br = new BufferedReader(fr);

		try {
			String tempLine = br.readLine();
			while (tempLine != null) {
				// System.out.println(tempLine);
				oldList.add(tempLine);
				tempLine = br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
				if (fr != null) {
					fr.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return oldList;
	}

	/**
	 * 
	 * @param nginxLocation
	 * @param newList
	 * @throws IOException
	 */
	static void writeFile(String nginxLocation, List<String> newList) throws IOException {
		// 清空文件
		File nowFile = new File(nginxLocation + File.separatorChar + "conf" + File.separatorChar + "if.conf");
		FileWriter fw = new FileWriter(nowFile);
		fw.write("");
		//

		BufferedWriter bw = new BufferedWriter(fw);
		for (int i = 0; i < newList.size(); i++) {
			String str = newList.get(i);
			StringBuilder sBuilder = new StringBuilder();
			sBuilder.append(str + "\n");
			fw.write(sBuilder.toString());
		}
		fw.close();
		bw.close();
	}
}
