package cn.dceast.platform.common.http;

import cn.dceast.platform.common.string.StringUtil;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * 发送http请求工具
 *
 * Created by hongkai on 2016/5/5.
 */
public class HttpSender {

	private static Logger logger = LoggerFactory.getLogger(HttpSender.class);

	/**
	 * 默认编码方式
	 */
	private final static String DEFAULT_ENCODING = "utf-8";

	/**
	 * 默认超时时间
	 */
	private final static int DEFAULT_TIMEOUT = 3000;

	/**
	 * 发送post请求
	 *
	 * @param url
	 *            连接地址
	 * @param params
	 *            参数
	 * @return
	 */
	public static String post(String url, List<NameValuePair> params) {
		return post(url, DEFAULT_TIMEOUT, params, null, DEFAULT_ENCODING);
	}

	/**
	 * 发送post请求
	 *
	 * @param url
	 *            连接地址
	 * @param httpConnectionTimeout
	 *            超时时间
	 * @param params
	 *            参数
	 * @return
	 */
	public static String post(String url, int httpConnectionTimeout, List<NameValuePair> params) {
		return post(url, httpConnectionTimeout, params, null, DEFAULT_ENCODING);
	}

	/**
	 * 发送post请求
	 *
	 * @param url
	 *            连接地址
	 * @param httpConnectionTimeout
	 *            超时时间
	 * @param params
	 *            参数
	 * @param headers
	 *            请求头
	 * @param encoding
	 *            编码格式
	 * @return
	 */
	public static String post(String url, int httpConnectionTimeout, List<NameValuePair> params, Header[] headers,
			String encoding) {
		HttpResponse httpResponse = postResponse(url, httpConnectionTimeout, params, headers, encoding);
		try {
			return EntityUtils.toString(httpResponse.getEntity(), encoding);
		} catch (IOException e) {
			logger.error("", e);
			throw new RuntimeException("系统异常", e);
		}
	}

	/**
	 * 发送post请求
	 *
	 * @param url
	 *            连接地址
	 * @param httpConnectionTimeout
	 *            超时时间
	 * @param params
	 *            参数
	 * @param headers
	 *            请求头
	 * @param encoding
	 *            编码格式
	 * @return
	 */
	public static HttpResponse postResponse(String url, int httpConnectionTimeout, List<NameValuePair> params,
			Header[] headers, String encoding) {
		CloseableHttpResponse response = null;
		HttpPost httpPost = null;
		// 默认参数
		UrlEncodedFormEntity uefEntity;
		try {
			CloseableHttpClient httpClient = HttpClientUtil.getHttpClient();

			httpPost = new HttpPost(url);

			if (params == null) {
				params = new ArrayList<NameValuePair>();
			}

			uefEntity = new UrlEncodedFormEntity(params, StringUtil.isEmpty(encoding) ? DEFAULT_ENCODING : encoding);
			httpPost.setEntity(uefEntity);

			// 设置http header信息
			if (headers != null && headers.length != 0) {
				httpPost.setHeaders(headers);
			}

			// 设置请求和传输超时时间
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(httpConnectionTimeout)
					.setConnectTimeout(httpConnectionTimeout).build();
			httpPost.setConfig(requestConfig);
			return httpClient.execute(httpPost);
		} catch (ConnectTimeoutException e) {
			logger.error("http connection time out", e);
			throw new RuntimeException("http connection time out", e);
		} catch (UnsupportedEncodingException e) {
			logger.error("unsupported encoding exception", e);
			throw new RuntimeException("unsupported encoding exception", e);
		} catch (ClientProtocolException e) {
			logger.error("client protocol exception", e);
			throw new RuntimeException("client protocol exception", e);
		} catch (IOException e) {
			logger.error("io exception", e);
			throw new RuntimeException("io exception", e);
		} finally {
			try {
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				logger.warn("close response error", e);
			}
			try {
				if (httpPost != null) {
					httpPost.releaseConnection();
				}
			} catch (Exception e) {
				logger.warn("release http connection error", e);
			}
		}
	}

	/**
	 * 发送get请求
	 *
	 * @param url
	 * @param headers
	 * @return
	 */
	public static String get(String url, Header[] headers,HttpParams httpParams) {
		return get(url, DEFAULT_TIMEOUT, headers,httpParams);
	}

	/**
	 * 发送get请求
	 * 
	 * @param url
	 * @param httpConnectionTimeout
	 * @param headers
	 * @return
	 */
	public static String get(String url, int httpConnectionTimeout, Header[] headers, HttpParams httpParams) {
		return get(url, httpConnectionTimeout, headers, httpParams, DEFAULT_ENCODING);
	}

	/**
	 * 发送get请求
	 * 
	 * @param url
	 * @param httpConnectionTimeout
	 * @param headers
	 * @param encoding
	 * @return
	 */
	public static String get(String url, int httpConnectionTimeout, Header[] headers, HttpParams httpParams,
			String encoding) {
		HttpResponse response = getResponse(url, httpConnectionTimeout, headers, httpParams);
		try {
			return EntityUtils.toString(response.getEntity(), encoding);
		} catch (IOException e) {
			logger.error("", e);
			throw new RuntimeException("系统异常", e);
		}
	}

	/**
	 * 发送get请求
	 * 
	 * @param url
	 * @param httpConnectionTimeout
	 * @param headers
	 * @return
	 */
	public static HttpResponse getResponse(String url, int httpConnectionTimeout, Header[] headers, HttpParams params) {
		CloseableHttpClient httpClient = HttpClientUtil.getHttpClient();
		HttpGet httpget = null;
		CloseableHttpResponse response = null;
		try {
			httpget = new HttpGet(url);

			// 设置请求和传输超时时间
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(httpConnectionTimeout)
					.setConnectTimeout(httpConnectionTimeout).build();

			httpget.setConfig(requestConfig);

			// 设置http header信息
			if (headers != null && headers.length != 0) {
				httpget.setHeaders(headers);
			}
			if(params!=null){
				httpget.setParams(params);
			}
			

			response = httpClient.execute(httpget);
			return response;
		} catch (ConnectTimeoutException e) {
			logger.error("http connection time out", e);
			throw new RuntimeException("http connection time out", e);
		} catch (UnsupportedEncodingException e) {
			logger.error("unsupported encoding exception", e);
			throw new RuntimeException("unsupported encoding exception", e);
		} catch (ClientProtocolException e) {
			logger.error("client protocol exception", e);
			throw new RuntimeException("client protocol exception", e);
		} catch (IOException e) {
			logger.error("io exception", e);
			throw new RuntimeException("io exception", e);
		} finally {
			try {
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				logger.warn("close response error", e);
			}
			try {
				if (httpget != null) {
					httpget.releaseConnection();
				}
			} catch (Exception e) {
				logger.warn("release http connection error", e);
			}
		}
	}

}
