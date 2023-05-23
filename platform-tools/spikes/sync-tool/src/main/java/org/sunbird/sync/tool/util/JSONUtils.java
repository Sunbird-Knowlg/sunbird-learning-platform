package org.sunbird.sync.tool.util;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.List;
import java.util.Map;

public class JSONUtils {
	
	private static ObjectMapper mapper = new ObjectMapper();;

	public static String serialize(Object object) throws Exception {
			return mapper.writeValueAsString(object);
	}

	public static Object convertJSONString(String value) {
		if (StringUtils.isNotBlank(value)) {
			com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
			try {
				Map<Object, Object> map = mapper.readValue(value, Map.class);
				return map;
			} catch (Exception e) {
				try {
					List<Object> list = mapper.readValue(value, List.class);
					return list;
				} catch (Exception ex) {
					//suppress error due to invalid map while converting JSON and return null
				}
			}
		}
		return null;
	}
}