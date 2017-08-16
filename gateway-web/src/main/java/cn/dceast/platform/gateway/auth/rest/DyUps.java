package cn.dceast.platform.gateway.auth.rest;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.table.TableModel;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.jcajce.provider.asymmetric.dsa.DSASigner.noneDSA;
import org.slf4j.LoggerFactory;

import cn.dceast.platform.common.http.HttpSender;
import cn.dceast.platform.gateway.auth.data.entity.Message;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dc.appengine.router.nginxparser.antlr.NginxBaseListener;

import cn.dceast.platform.gateway.auth.service.ServiceApi;
import cn.dceast.platform.gateway.auth.util.HttpClientUtil;
import cn.dceast.platform.gateway.auth.util.InnerConstants;
import redis.clients.jedis.Jedis;

@RestController
public class DyUps {
	private static Logger logger = LoggerFactory.getLogger(DyUps.class);

	@Value("${nginx.dyups.address}")
	public String address;

	@Value("${gateway.type}")
	public String type;

	@Value("${gateway.address}")
	public String gatewayAddress;

	@Value("${redis.host}")
	public String redisHost;

	private static CloseableHttpClient httpClient = null;
	// public static boolean isInit = false;

	public static final String API = "api";
	public static final String DATA = "data";
	public static final String SERVICE = "service";
	public static final String SAAS = "saas";
	public static final String REDISPASSWORD = "123";

	@RequestMapping(value = "/dyups", method = { RequestMethod.POST, RequestMethod.GET })
	public String dyups(HttpServletRequest req, HttpServletResponse resp, String data)
			throws ServletException, IOException {
		Message msg = new Message();

		if (data == null) {
			msg.setCode(InnerConstants.RETURN_CODE_FAIL);
			msg.setMessage("更新路由信息失败-参数为空");
			logger.info("data ==null");
			return new JSONObject().toJSONString(msg);
		}
		// data = URLDecoder.decode(data, "utf-8");2017年5月13日13:28:23 调用端不再加密
		logger.info("enter dyups " + "data value: " + data.toString());

		// 用于server端(外网)，格式化client端(内网)传入数据
		if (data.contains("%20")) {
			data = data.replaceAll("%20", " ");
		}

		//////////// 初始化数据
		boolean isHttps = false;
		String upstream = null;
		String appName = null;
		String op = null;
		String jsonAppName = null;
		String inContext = null;
		String trueContext = null;
		String resourceType = null;
		String areacode = null;
		JSONArray jso = new JSONObject().parseArray(data);
		List<Map<String, Object>> orderList = JSONObject.toJavaObject(jso, List.class);
		/////////// --初始化数据

		// Server端主要工作：更新Nginx
		if (type.contains("server")) {
			// 更新路由
			for (Map<String, Object> singleOrder : orderList) {

				////// 初始化
				areacode = (String) singleOrder.get("areacode");
				resourceType = (String) singleOrder.get("resourceType");
				inContext = (String) singleOrder.get("context");
				inContext = inContext.trim();
				appName = getAppName(inContext, resourceType, areacode);
				jsonAppName = (String) singleOrder.get("appName");
				List<String> upstreamList = JSONObject.toJavaObject((JSONArray) singleOrder.get("upstream"),
						List.class);
				StringBuilder stringBuilder = new StringBuilder();
				///// --初始化

				///// for
				for (int i = 0; i < upstreamList.size(); i++) {
					String oneUpstream = upstreamList.get(i);
					if (oneUpstream != null) {
						if (oneUpstream.contains("http://")) {
							oneUpstream = oneUpstream.replaceAll("http://", "");
						} else {
							oneUpstream = oneUpstream.replaceAll("https://", "");
						}
						oneUpstream = oneUpstream + ";";
						// 查找oneUpstream中第一个/位置
						int beginIndex = oneUpstream.indexOf("/");

						if (beginIndex == -1) {
							oneUpstream = oneUpstream.substring(0, oneUpstream.length() - 1);
						} else {
							// 实际待添加的oneUpstream
							String tempOneUpstream = oneUpstream.substring(0, beginIndex);
							trueContext = oneUpstream.substring(beginIndex + 1, oneUpstream.length() - 1);
							oneUpstream = tempOneUpstream;
						}
					} //
					stringBuilder.append(oneUpstream + ";");
				}
				///// --for

				///////
				upstream = stringBuilder.toString();
				op = (String) singleOrder.get("op");
				if (op == null) {
					op = "add";
				}
				///////

				if (op.equals("add") || op.equals("update")) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// 向nginx中增加路由
					String ret = addAppRouter(appName, upstream, req, resp);
					logger.info("Server dyups Sync appName result: " + ret);
					// 返回用户端的结果
					msg.setCode("555555");
					msg.setMessage(ret);
				} else {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// 删除ningx中路由信息
					String ret = clearAppRouter(appName, address);
					logger.info("Server dyups clearAppRouter name: " + appName);
					// 返回结果
					if (ret.contains("success")) {
						msg.setCode("555555");
						msg.setMessage("删除成功");
					} else {
						msg.setCode("555555");
						msg.setMessage("删除失败");
					}
				}
			}
		} else {

			// client端 主要工作：入库，向Server端(外网网关)发数据
			for (Map<String, Object> singleOrder : orderList) {
				resourceType = (String) singleOrder.get("resourceType");
				inContext = (String) singleOrder.get("context");
				areacode = (String) singleOrder.get("areacode");
				inContext = inContext.trim();
				appName = getAppName(inContext, resourceType, areacode);
				jsonAppName = (String) singleOrder.get("appName");

				///////////////////
				// 存储URLS数组
				// 如果是微服务的话,就得向redis中存储数据.
				StringBuilder urlStringBuilder = new StringBuilder();
				if (resourceType.equals(InnerConstants.RESOURCETYPE_MICROSVC)) {
					List<String> urlList = JSONObject.toJavaObject((JSONArray) singleOrder.get("urls"), List.class);
					// 一个一个遍历放入
					// ,形如/sms/getDetail.do;/email/callback.do;/sms/other.do
					for (int i = 0; i < urlList.size(); i++) {
						String oneFromUrls = urlList.get(i);
						urlStringBuilder.append(oneFromUrls + ";");
						// 写redis
						logger.info("urls write into redis " + "key: " + inContext + oneFromUrls + " value: "
								+ oneFromUrls);
						// 为了处理当入网关url为/realname_auth/sms/getDetail.do形式时，Nginx将相对url转为/sms/getDetail.do
						writeRedis(inContext + oneFromUrls, oneFromUrls);
					}
				}
				///////////////////

				List<String> upstreamList = JSONObject.toJavaObject((JSONArray) singleOrder.get("upstream"),
						List.class);
				StringBuilder stringBuilder = new StringBuilder();

				////// for
				for (int i = 0; i < upstreamList.size(); i++) {
					String oneUpstream = upstreamList.get(i);
					if (oneUpstream != null) {
						if (oneUpstream.contains("http://")) {
							oneUpstream = oneUpstream.replaceAll("http://", "");
						} else {
							oneUpstream = oneUpstream.replaceAll("https://", "");
							isHttps=true;
						}
						oneUpstream = oneUpstream + ";";
						// 查找oneUpstream中第一个/位置
						int beginIndex = oneUpstream.indexOf("/");
						if (beginIndex == -1) {
							oneUpstream = oneUpstream.substring(0, oneUpstream.length() - 1);
						} else {
							// 实际待添加的oneUpstream
							String tempOneUpstream = oneUpstream.substring(0, beginIndex);
							if (beginIndex == oneUpstream.length() - 1) {
								// 处理这种：server
								// www.fuyunwang.com/这种形式的oneUpstream
								trueContext = "/";
							} else {
								trueContext = oneUpstream.substring(beginIndex + 1, oneUpstream.length() - 1);
							}
							oneUpstream = tempOneUpstream;
						}
					}
					stringBuilder.append(oneUpstream + ";");
				}
				// upstream为待入库的upstream字段值
				upstream = stringBuilder.toString();
				////////////////////
				op = (String) singleOrder.get("op");
				if (op == null) {
					op = "add";
				}
				////// MongoDb Redis操作
				if (op.equals("add")) {
					// add
					// 操作mongo
					/*
					 * FIXME 2017年5月9日15:29:48
					 * 现有代码的问题：当a记录已存库，而调用/dyups接口时传入同一条a记录(op为"add")，
					 * 则库中将存在两条相同数据
					 */
					ServiceApi.addApi(new JSONObject().toJSONString(singleOrder), appName, upstream, trueContext,
							urlStringBuilder.toString(),isHttps);
					if (trueContext != null) {
						// 写redis
					}
					writeRedis(inContext, trueContext);
				} else if (op.equals("update")) {
					// 更新MongoDB
					ServiceApi.updateApi(new JSONObject().toJSONString(singleOrder), appName, upstream, trueContext,
							urlStringBuilder.toString(),isHttps);
					if (trueContext != null) {
						// 写redis
						writeRedis(inContext, trueContext);
					}
				} else {
					// 删除mongodb数据
					ServiceApi.removeApi(new JSONObject().toJSONString(singleOrder), appName, upstream, trueContext,
							urlStringBuilder.toString(),isHttps);
					delRedisValue(inContext);
				}
				///// --MongoDb redis操作
			}
			////// --for

			// 调用Server端更新nginx
			String result = requestServerDyUps(data);

			///// 检查Server端返回结果
			if (result.contains("删除成功") || result.contains("success")) {
				msg.setCode(InnerConstants.RETURN_CODE_SUCC);
				msg.setMessage("更新路由信息成功");
			} else if (result.contains("初始化")) {
				msg.setCode(InnerConstants.RETURN_CODE_SUCC);
				msg.setMessage("初始化路由信息成功");
			} else {
				msg.setCode(InnerConstants.RETURN_CODE_FAIL);
				msg.setMessage("更新路由信息失败");
			}
			/////
		}
		return new JSONObject().toJSONString(msg);
	}

	/**
	 * 获取Nginx.conf中实际添加的Upstream Name
	 * 
	 * @param tempAppName
	 * @return
	 */
	public String getAppName(String tempAppName, String resourceType, String areacode) {
		String appName;
		// 删除首部/
		if (tempAppName.indexOf("/") == 0) {
			tempAppName = tempAppName.replaceFirst("/", "");
		}
		// 删除尾部/
		if (tempAppName.lastIndexOf("/") == tempAppName.length() - 1) {
			tempAppName = tempAppName.substring(0, tempAppName.length() - 1);
		}

		if (!resourceType.equals(InnerConstants.RESOURCETYPE_MICROSVC)) {
			// appName = tempAppName.substring(0,tempAppName.indexOf("/"));
			appName = tempAppName.replaceAll("/", "splash");
		} else {
			appName = tempAppName;
		}
		switch (resourceType) {
		// resourcetype 01-api 02-数据 03-微服务 04-SaaS服务
		case "01":
			appName = DyUps.API + appName;
			break;
		case "02":
			appName = DyUps.DATA + appName;
			break;
		case "03":
			appName = DyUps.SERVICE + appName;
			break;
		case "04":
			appName = DyUps.SAAS + appName;
		default:
			break;
		}
		// 真实向nginx中添加的upstream Name
		appName = areacode + appName;
		return appName;
	}

	/**
	 * 删除server端某条路由
	 * @param appName
	 * @param url
	 * @return
	 */
	public String clearAppRouter(String appName, String url) {
		HttpDelete delete = new HttpDelete("http://" + url + "/upstream/" + appName);
		String ret = null;
		try {
			HttpResponse res = HttpClientUtil.getHttpClient().execute(delete);
			ret = EntityUtils.toString(res.getEntity());
			System.out.println("Clear appName result: " + ret);
		} catch (IOException e) {
			System.out.println("Clear router for {} failed " + appName + " " + e);
		}
		return ret;
	}

	/**
	 * Server端向Nginx动态增加路由信息
	 * @param appName
	 * @param upstream
	 * @param req
	 * @param resp
	 * @return
	 */
	public String addAppRouter(String appName, String upstream, HttpServletRequest req, HttpServletResponse resp) {
		String ret = null;
		Map<String, String> resultMap = new HashMap<String, String>();
		HttpPost post = new HttpPost("http://" + address + "/upstream/" + appName);
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

	/**
	 * Server端向Nginx动态增加路由信息
	 * @param appName
	 *            待写入的upstreamName
	 * @param upstream
	 *            待写入的upstream中的内容
	 * @param nginxDyupsAddress
	 *            nginx地址
	 * @return
	 */
	public String addAppRouter(String appName, String upstream, String nginxDyupsAddress) {
		String ret = null;
		Map<String, String> resultMap = new HashMap<String, String>();
		HttpPost post = new HttpPost("http://" + nginxDyupsAddress + "/upstream/" + appName);
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

	/**
	 * client端向Server端发送http请求,nginx中添加路由信息
	 * 
	 * @param data
	 * @return
	 */
	public String requestServerDyUps(String data) {
		httpClient = HttpClients.createDefault();
		String resultBody = null;
		List<NameValuePair> params = new ArrayList();
		data = data.replaceAll(" ", "%20");
		NameValuePair param = new BasicNameValuePair("data", data);
		params.add(param);
		String str = null;
		CloseableHttpResponse httpResponse = null;
		try {
			HttpGet httpGet = new HttpGet("http://" + gatewayAddress + "/gateway-web-1.8.0" + "/dyups");
			str = EntityUtils.toString(new UrlEncodedFormEntity(params));
			httpGet.setURI(new URI(httpGet.getURI().toString() + "?" + str));
			// 发送请求
			httpResponse = httpClient.execute(httpGet);
			// 获取返回数据
			HttpEntity entity = httpResponse.getEntity();
			resultBody = EntityUtils.toString(entity);
			// if (entity != null) {
			// entity.consumeContent();
			// }
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} finally {
			if (httpResponse != null) {
				try {
					httpResponse.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return resultBody;
	}

	/**
	 * 写Redis
	 * 
	 * @return
	 */
	public void writeRedis(String key, String value) {
		Jedis jedis = new Jedis(redisHost, 6379);
		jedis.auth(DyUps.REDISPASSWORD);
		jedis.select(3);
		jedis.set(key, value);// 向key-->name中放入了value-->xinxin
		jedis.close();
	}

	public void initRedis(String key, String value, String initRedisHost) {
		Jedis jedis = new Jedis(initRedisHost, 6379);
		jedis.auth(DyUps.REDISPASSWORD);
		jedis.select(3);
		jedis.set(key, value);// 向key-->name中放入了value-->xinxin
		jedis.close();
	}

	/**
	 * 删除Redis中某value
	 * 
	 * @param key
	 */
	public void delRedisValue(String key) {
		Jedis jedis = new Jedis(redisHost, 6379);
		jedis.auth(DyUps.REDISPASSWORD);
		jedis.select(3);
		jedis.del(key);
		jedis.close();
	}

}