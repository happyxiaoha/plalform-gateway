package cn.dceast.platform.common.fuyun;


import cn.dceast.platform.common.http.HttpSender;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 浮云网api调用工具
 *
 * Created by hongkai on 2016/1/21.
 */
public class ApiCallUtil {

    private static Logger logger = LoggerFactory.getLogger(ApiCallUtil.class);

    private static final String POST = "post";
    private static final String GET = "get";
    //已废弃
    public static String post(String url, String appKey, String secretKey, Map<String,String> parameters, String encoding) throws Exception {
        Header[] headers = generateHeaders(appKey, secretKey, POST);
        return HttpSender.post(url, 5000, getParams(parameters), headers, encoding);
    }
    //已废弃
    public static String get(String url, String appKey, String secretKey) throws Exception {
        Header[] headers = generateHeaders(appKey, secretKey, GET);
        return HttpSender.get(url, headers,null);
    }

	private static Header[] generateHeaders(String appKey, String secretKey, String method){
        String params = "";
        params = new BASE64Encoder().encode(params.getBytes());
        Header[] headers = new Header[6];
        String contentType = "application/x-www-form-urlencoded";
        SimpleDateFormat sFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String date = sFormat.format(new Date());

        headers[0] = new BasicHeader("method", method);
        headers[1] = new BasicHeader("Content-Type", contentType);
        headers[2] = new BasicHeader("user-params", params);
        headers[3] = new BasicHeader("user-date", date);
        headers[4] = new BasicHeader("dceast-appkey", appKey);
        /**
         * 生成签名
         */
        String signData = SignatureUtil.buildSignData(params, date);
        String sign = SignatureUtil.buildSignature(appKey, secretKey, signData);
        headers[5] = new BasicHeader("authorization", sign);
        return headers;
    }

    private static List<NameValuePair> getParams(Map<String,String> parameters){
        List<NameValuePair> result = new ArrayList<>();
        for(final Map.Entry<String, String> entry : parameters.entrySet()){
            result.add(new NameValuePair() {
                @Override
                public String getName() {
                    return entry.getKey();
                }

                @Override
                public String getValue() {
                    return entry.getValue();
                }
            });
        }

        return result;
    }
}
