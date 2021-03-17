package org.sunbird.graph.cache.util;

import java.util.HashSet;
import java.util.Set;

public class RedisPropValueUtil {

    public static Set<String> getStringifiedValue(Object value) {
        Set<String> list = new HashSet<String>();
        if (null != value) {
            if (value instanceof Integer) {
                list.add("Integer");
                list.add(value.toString());
            } else if (value instanceof Boolean) {
                list.add("Boolean");
                list.add(value.toString());
            } else if (value instanceof Long) {
                list.add("Long");
                list.add(value.toString());
            } else if (value instanceof Float) {
                list.add("Float");
                list.add(value.toString());
            } else if (value instanceof Double) {
                list.add("Double");
                list.add(value.toString());
            } else if (value instanceof String) {
                list.add("String");
                list.add(value.toString());
            } else if (value instanceof int[]) {
                list.add("int[]");
                int[] arr = (int[]) value;
                for (int i : arr) {
                    list.add("" + i);
                }
            } else if (value instanceof boolean[]) {
                list.add("boolean[]");
                boolean[] arr = (boolean[]) value;
                for (boolean i : arr) {
                    list.add("" + i);
                }
            } else if (value instanceof long[]) {
                list.add("long[]");
                long[] arr = (long[]) value;
                for (long i : arr) {
                    list.add("" + i);
                }
            } else if (value instanceof float[]) {
                list.add("float[]");
                float[] arr = (float[]) value;
                for (float i : arr) {
                    list.add("" + i);
                }
            } else if (value instanceof double[]) {
                list.add("double[]");
                double[] arr = (double[]) value;
                for (double i : arr) {
                    list.add("" + i);
                }
            } else if (value instanceof String[]) {
                list.add("String[]");
                String[] arr = (String[]) value;
                for (String i : arr) {
                    list.add(i);
                }
            }
        }
        return list;
    }

    public static void checkValue(Set<String> list, Object value) {
        if (null != list && !list.isEmpty()) {

        }
    }

    public static String[] convertSetToArray(Set<String> list) {
        if (null != list && !list.isEmpty()) {
            String[] array = new String[list.size()];
            int index = 0;
            for (String str : list) {
                array[index] = str;
                index += 1;
            }
            return array;
        }
        return null;
    }
}
