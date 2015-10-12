package com.ilimi.graph.common;

import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

public class JSONUtils {

    @SuppressWarnings("unchecked")
    public static Object convertJSONString(String value) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<Object, Object> map = mapper.readValue(value, Map.class);
            return map;
        } catch (Exception e) {
            try {
                List<Object> list = mapper.readValue(value, List.class);
                return list;
            } catch (Exception ex) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
