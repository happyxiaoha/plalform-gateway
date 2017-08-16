package cn.dceast.platform.gateway.auth.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import cn.dceast.platform.gateway.auth.util.HttpClientUtil;

public class DyUpsServlet extends HttpServlet {

	
	public static final String ADDRESS = "http://127.0.0.1:8088";

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String upstream = "server 127.0.0.1:666;server 127.0.0.1:777;server 127.0.0.1:888;";
		String appName = "ttlsa1";
		String op = "add";
		appName = req.getHeader("appName");
		upstream = req.getHeader("apiMsg");
		op = req.getHeader("dyupsop");
		if (op != null && appName != null && upstream != null) {
			if (op.equals("add")) {
				String ret = addAppRouter(appName, upstream, req, resp);
				System.out.println("Sync appName result: " + ret);
				System.out.println("======= service ======");
			} else {
				clearAppRouter(appName, ADDRESS);
			}
		}

		// PrintWriter pw = resp.getWriter();
		// pw.println("hello world.");
		// pw.close();
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

	private void clearAppRouter(String appName, String url) {
		HttpDelete delete = new HttpDelete(url + "/upstream/" + appName);
		String ret;
		try {
			HttpResponse res = HttpClientUtil.getHttpClient().execute(delete);
			ret = EntityUtils.toString(res.getEntity());
			System.out.println("Clear appName result: " + ret);
		} catch (IOException e) {
			System.out.println("Clear router for {} failed " + appName + " " + e);
		}
	}

	private String addAppRouter(String appName, String upstream, HttpServletRequest req, HttpServletResponse resp) {

		String ret = null;
		Map<String, String> resultMap = new HashMap<String, String>();
		HttpPost post = new HttpPost(ADDRESS + "/upstream/" + appName);
		try {
			post.setEntity(new StringEntity(upstream));
			resultMap.put(appName, upstream);
			HttpResponse response = HttpClientUtil.getHttpClient().execute(post);
			ret = EntityUtils.toString(response.getEntity());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

}