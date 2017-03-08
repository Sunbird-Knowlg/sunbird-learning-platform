package com.ilimi.graph.common;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONUtils {

	private static Logger LOGGER = LogManager.getLogger(JSONUtils.class.getName());

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
					LOGGER.error("Something Went Wrong While Converting JSON String ('" + value + "') to JSON Object.",
							e);
				}
			}
		}
		return null;
	}
}
