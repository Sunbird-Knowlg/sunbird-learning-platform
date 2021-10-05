package org.sunbird.graph.common;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

public class DateUtils {


    public static String format(Date date) {
        SimpleDateFormat sdf = getDateFormat();
        if (null != date) {
            try {
                return sdf.format(date);
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static Date parse(String dateStr) {
        SimpleDateFormat sdf = getDateFormat();
        if (StringUtils.isNotBlank(dateStr)) {
            try {
                return sdf.parse(dateStr);
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static SimpleDateFormat getDateFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        return sdf;
    }
    public static String formatCurrentDate() {
        return format(new Date());
    }
}
