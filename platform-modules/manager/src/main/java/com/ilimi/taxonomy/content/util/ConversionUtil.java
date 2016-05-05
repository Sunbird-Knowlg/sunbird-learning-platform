package com.ilimi.taxonomy.content.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.*;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ConversionUtil {
	
	public static String convertMapToJSON(Map<String, String> map) {
		String jsonResp = "";
		ObjectMapper mapperObj = new ObjectMapper();
        try {
             jsonResp = mapperObj.writeValueAsString(map);
            return jsonResp;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonResp;
	}
	
	public static Map<String, Object> convertJsonStringToMap(String jsonString) throws JSONException{
		JSONObject jsonObject = new JSONObject(jsonString);
		return jsonToMap(jsonObject);
	}
	
	public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
	    Map<String, Object> map = new HashMap<String, Object>();
	    if(json != JSONObject.NULL) {
	        map = toMap(json);
	    }
	    return map;
	}

	public static Map<String, Object> toMap(JSONObject object) throws JSONException {
	    Map<String, Object> map = new HashMap<String, Object>();
	    Iterator<String> keysItr = object.keys();
	    while(keysItr.hasNext()) {
	        String key = keysItr.next();
	        Object value = object.get(key);
	        if(value instanceof JSONArray) {
	            value = toList((JSONArray) value);
	        } else if(value instanceof JSONObject) {
	            value = toMap((JSONObject) value);
	        }
	        map.put(key, value);
	    }
	    return map;
	}

	public static List<Object> toList(JSONArray array) throws JSONException {
	    List<Object> list = new ArrayList<Object>();
	    for(int i = 0; i < array.length(); i++) {
	        Object value = array.get(i);
	        if(value instanceof JSONArray) {
	            value = toList((JSONArray) value);
	        } else if(value instanceof JSONObject) {
	            value = toMap((JSONObject) value);
	        }
	        list.add(value);
	    }
	    return list;
	}
}
