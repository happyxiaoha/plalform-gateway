package cn.dceast.platform.common.fuyun;

import cn.dceast.platform.common.security.HMACSHA1;
import sun.misc.BASE64Encoder;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class SignatureUtil {
    public static final String SIGN_PREFIX = "dc";
    public static final String MSG_SPLITOR = "\n";

    public SignatureUtil() {
    }

    public static String buildSignData(String userParams, String date) {
        try {
            SimpleDateFormat sbBuffer = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            sbBuffer.parse(date);
        } catch (ParseException var3) {
            throw new RuntimeException("The date is not valid!");
        }

        StringBuffer sbBuffer1 = new StringBuffer();
        sbBuffer1.append(userParams).append("\n").append(date).append("\n");
        return sbBuffer1.toString();
    }

    public static String buildSignature(String appkey, String secretkey, String data) {
        String encodeData = new BASE64Encoder().encode(HMACSHA1.encode(data, secretkey).getBytes());
        String sign = String.format("%s:%s:%s", new Object[]{"dc", appkey, encodeData});
        return sign;
    }

    public static void main(String[] args) {
    }
}
