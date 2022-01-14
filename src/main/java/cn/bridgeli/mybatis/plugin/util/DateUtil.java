package cn.bridgeli.mybatis.plugin.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author bridgeli on 2022/1/15 1:02 AM
 */
public class DateUtil {

    public static String date2Str(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }
}
