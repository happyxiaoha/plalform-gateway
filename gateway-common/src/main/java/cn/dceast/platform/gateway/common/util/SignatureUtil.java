package cn.dceast.platform.gateway.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.dceast.platform.gateway.common.security.Base64;
import cn.dceast.platform.gateway.common.security.HMACSHA1;

/**
 * 生成签名工具
 * @author zhang
 *
 */
public class SignatureUtil {
	public static final String SIGN_PREFIX="dc";
	public static final String MSG_SPLITOR="\n";
	
	/**
	 * 生成需要签名的数据
	 * @param userParams 用户参数
	 * @param date 日期格式：yyyyMMddHHmissSSS
	 * @return
	 */
	public static String buildSignData(String userParams,String date){
		
		try {
			SimpleDateFormat sFormat=new SimpleDateFormat("yyyyMMddHHmmssSSS");
			sFormat.parse(date);
		} catch (ParseException e) {
			//向外抛
			throw new RuntimeException("The date is not valid!");
		}
		
		StringBuffer sbBuffer=new StringBuffer();
		sbBuffer.append(userParams).append(MSG_SPLITOR)
		        .append(date).append(MSG_SPLITOR);
		
		return sbBuffer.toString();
		
	}
	
	/**
	 * 生成电子签名
	 * @param appkey 公钥
	 * @param secretkey 私钥
	 * @param data 签名数据。由buildSignData方法生成。
	 * @return 签名字符串
	 */
	public static String buildSignature(String appkey,String secretkey,String data){
		
		String encodeData=Base64.encode(HMACSHA1.encode(data, secretkey));
		String sign=String.format("%s:%s:%s", SIGN_PREFIX,appkey,encodeData);
		
		return sign;
	}
	
	public static void main(String[] args){
		
		
		//while(true)
		//System.out.println(sFormat.format(new Date()));
	}
	
}
