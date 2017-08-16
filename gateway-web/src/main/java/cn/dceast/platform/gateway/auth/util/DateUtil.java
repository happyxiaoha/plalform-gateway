package cn.dceast.platform.gateway.auth.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by owen on 2016/8/17.
 */
public class DateUtil {
    /**
     * 取当前日期
     *
     * @return 格式：yyyyMMdd， 数值类型
     */
    public static Integer getCurrentDate(){
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        String format = sf.format(new Date());
        return Integer.valueOf(format);
    }
}
