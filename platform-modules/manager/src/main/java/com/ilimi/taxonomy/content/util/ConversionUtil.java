package com.ilimi.taxonomy.content.util;

import java.io.IOException;
import java.util.Map;

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
	
	

}
