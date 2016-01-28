package com.ilimi.graph.common;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class DateUtils {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public static String format(Date date) {
        if (null != date) {
            try {
                return sdf.format(date);
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static Date parse(String dateStr) {
        if (StringUtils.isNotBlank(dateStr)) {
            try {
                return sdf.parse(dateStr);
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static String formatCurrentDate() {
        return format(new Date());
    }

    public static void main(String[] args) {
        //String[] v = new String[]{"as", "df"};
        List<Object> l = new ArrayList<Object>();
        l.add("ace");
        l.add(2);
        try {
            Object v = l.toArray();
            System.out.println(v instanceof Object[]);
            System.out.println(Array.getLength(v));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
