package org.ekstep.graph.common;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.logger.PlatformLogger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONUtils {

	@SuppressWarnings("unchecked")
	public static Object convertJSONString(String value) {
		if (StringUtils.isNotBlank(value)) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				Map<Object, Object> map = mapper.readValue(value, Map.class);
				return map;
			} catch (Exception e) {
				try {
					List<Object> list = mapper.readValue(value, List.class);
					return list;
				} catch (Exception ex) {
					PlatformLogger.log("Something Went Wrong While Converting JSON String ('" + value + "') to JSON Object.", null, e);
				}
			}
		}
		return null;
	}
}
