package cn.dceast.platform.common.http;

import cn.dceast.platform.common.properties.Prop;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * 用于简化httpclient的获取，及性能提升.
 * 可以根据项目需要定制连接池大小，默认maxPerRoute 20, maxTotal 200.
 * httpConfig.properties 链接池的配置文件
 * httpClient.maxPerRoute = 20
 * httpClient.maxTotal = 200
 */
public class HttpClientUtil {

    public static CloseableHttpClient getHttpClient() {
        return HttClientProxy.client;
    }

    private static CloseableHttpClient buildHttpClient() {
        int maxPerRoute = 20;
        int maxTotal = 200;
        Prop prop = null;
        try {
            prop = new Prop("httpConfig.properties");
            if(prop.get("httpClient.maxPerRoute")!=null){
                maxPerRoute = Integer.parseInt(prop.get("httpClient.maxPerRoute"));
            }
            if(prop.get("httpClient.maxTotal")!=null){
                maxTotal = Integer.parseInt(prop.get("httpClient.maxTotal"));
            }
        } catch (IllegalArgumentException e) {
            System.out.println("INFO:httpConfig.properties file not found in classpath, so we will use the default size.");
        }

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setDefaultMaxPerRoute(maxPerRoute);
        cm.setMaxTotal(maxTotal);
        return HttpClients.custom().setConnectionManager(cm).build();
    }

    static class HttClientProxy {
        private static CloseableHttpClient client = buildHttpClient();
    }
}
