package cn.dceast.platform.gateway.auth.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用于状态检测
 * @author zhang
 *
 */
@RestController
public class PingServlet{

	@RequestMapping(value="/ping", method={RequestMethod.POST, RequestMethod.GET})
	public void ping(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		PrintWriter out=resp.getWriter();
		out.print("ok");
		out.flush();
		out.close();
	}

}
