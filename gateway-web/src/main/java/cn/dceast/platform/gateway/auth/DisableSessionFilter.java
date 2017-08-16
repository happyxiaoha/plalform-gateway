package cn.dceast.platform.gateway.auth;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 禁用session过滤器
 * 通过包装request对象，重写getSession方法，在任何地方通过getSession方法获取session取到的都是空
 *
 * Created by hongkai on 2016/6/24.
 */
public class DisableSessionFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        //包装request，覆盖获取session方法，返回空
        chain.doFilter(new HttpServletRequestWrapper((HttpServletRequest) request) {
            @Override
            public HttpSession getSession() {
                return null;
            }
            @Override
            public HttpSession getSession(boolean create) {
                return null;
            }
        }, response);
    }

    @Override
    public void destroy() {

    }
}
