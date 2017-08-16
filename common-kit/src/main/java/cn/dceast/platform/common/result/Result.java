package cn.dceast.platform.common.result;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Result {
	public static final String I18NCOOKIE_NAME = "i18n";

	private enum Status {
		OK, ERROR
	}

	private static SerializeConfig config = new SerializeConfig();
	private String status;
	private String message;
	private Object result;
	private Integer code;

	static {

	}

	public Result(int code, String status, String message, Object result) {
		super();
		this.status = status;
		this.message = message;
		if (result == null) {
			this.result = "";
		} else {
			this.result = result;
		}
		this.code = code;
	}

	public Result(int code, String status, String message) {
		super();
		this.status = status;
		this.message = message;
		this.code = code;
	}

	public Result() {
		super();
	}

	public static String error(Integer code, Object result) {
		String msg = ApplicationConextUtil.getAc().getMessage(code + "", null,
				getLocale());

		return new Result(code, Status.ERROR.name(), msg, result).toString();
	}

	public static String error(Integer code) {
		String msg = ApplicationConextUtil.getAc().getMessage(code + "", null,
				getLocale());

		return new Result(code, Status.ERROR.name(), msg, null).toString();
	}

	public static String error(String errMsg, Object result) {
		return new Result(1000, Status.ERROR.name(), errMsg, result).toString();
	}

	public static String ok(Integer code, Object result) {
		String msg = ApplicationConextUtil.getAc().getMessage(code + "", null,
				getLocale());
		return new Result(code, Status.OK.name(), msg, result).toString();
	}

	public static String ok(Integer code) {
		String msg = ApplicationConextUtil.getAc().getMessage(code + "", null,
				getLocale());
		return new Result(code, Status.OK.name(), msg, null).toString();
	}

	public static String ok(String msg, Object result) {
		return new Result(0, Status.OK.name(), msg, result).toString();
	}

	public static String ok(Object result) {
//		String msg = ApplicationConextUtil.getAc().getMessage(0 + "", null, getLocale());
		return new Result(0, Status.OK.name(), "", result).toString();
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	private static Locale getLocale() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes()).getRequest();
		Locale locale;
		Cookie cookie = WebUtils.getCookie(request, I18NCOOKIE_NAME);
		if (cookie != null) {
			locale = StringUtils.parseLocaleString(cookie.getValue());
			if (locale != null) {
				return locale;
			}
		}
		locale = (Locale) request.getLocale();
		if (locale != null) {
			return locale;
		}

		return locale;
	}

	@Override
	public String toString() {
		return JSONObject.toJSONString(this, config,
				SerializerFeature.WriteMapNullValue,
				SerializerFeature.WriteNullListAsEmpty,
				SerializerFeature.WriteNullStringAsEmpty,
				SerializerFeature.WriteDateUseDateFormat,
				SerializerFeature.DisableCircularReferenceDetect);
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		if (status != null) {
			map.put("status", status);
		}
		if (message != null) {
			map.put("message", message);
		}
		if (result != null) {
			map.put("result", result);
		}
		if (code != null) {
			map.put("code", code);
		}
		return map;
	}
}
