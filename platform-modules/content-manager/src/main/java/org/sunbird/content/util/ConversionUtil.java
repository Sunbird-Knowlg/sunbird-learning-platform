package org.sunbird.content.util;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * The Class ConversionUtil  is a utility 
 * used to convert any given map<String, String> to JSON
 * 
 * @params Map of type String Map<String String> 
 * @return Json
 */
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
}
