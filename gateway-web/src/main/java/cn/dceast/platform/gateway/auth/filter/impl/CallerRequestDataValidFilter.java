package cn.dceast.platform.gateway.auth.filter.impl;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.dceast.platform.gateway.auth.filter.AuthFilter;
import cn.dceast.platform.gateway.auth.filter.FilterMatcher;
import cn.dceast.platform.gateway.auth.filter.FilterResponseMessage;
import cn.dceast.platform.gateway.auth.util.InnerConstants;

/**
 * 调用者请求数据合法性校验
 * 
 * @author zhang
 *
 */
public class CallerRequestDataValidFilter extends AuthFilter {
	private Logger logger = LoggerFactory.getLogger(CallerRequestDataValidFilter.class);

	@Override
	public boolean doFilter(HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.info("enter CallerRequestDataValidFilter");

		// 0.ip不为空
		String ip = getIpAddr(request);
		if (StringUtils.isEmpty(ip)) {
			setErrorMessageOfJson(request, response, FilterResponseMessage.CODE_103101);
			logger.info(String.format("The Ip is null!"));
			return false;
		}

		// 1.context不能为空
		String api = getUriNoParams(request);
		if (StringUtils.isEmpty(api)) {
			setErrorMessageOfJson(request, response, FilterResponseMessage.CODE_100102);
			logger.error(String.format("The context is null!"));
			return false;
		}

		// 3. appkey不能为空
		String appkey = getAppkey(request);
		List<String> subDomainList = FilterMatcher.getSecondDomainList(request);
		if (!subDomainList.get(1).equals(InnerConstants.RESOURCETYPE_MICROSVC)) {
			/*
			 * 如果在表service_api中,isAuth=1 或者2。不校验appKey等用户信息
			 */
			// StringUtils.isEmpty(appkey) &&
			boolean validateInvoker = validateInvoke(request);
			if (validateInvoker) {
				if (StringUtils.isEmpty(appkey)) {
					setErrorMessageOfJson(request, response, FilterResponseMessage.CODE_102101);
					logger.error("The appKey is null!");
					return false;
				} else {
					logger.info("The appKey is not null");
					return true;
				}
			} else {
				//不需校验appkey，直接返回true
				return true;
			}
		}

		/*
		 * resourceId resourceType检验
		 */
		Map<String, String> reourceMap = FilterMatcher.getParameterFromRequest(request);
		if (reourceMap.get("resourceType") == null) {
			setErrorMessageOfJson(request, response, FilterResponseMessage.CODE_400103);
			logger.error("resourceType is null!");
			return false;

		}
		if (reourceMap.get("resourceId") == null) {
			setErrorMessageOfJson(request, response, FilterResponseMessage.CODE_400104);
			logger.error("resourceId is null!");
			return false;
		}
		//////////////////////////////

		return true;
	}

}
