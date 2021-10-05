package org.sunbird.graph.common;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.telemetry.logger.TelemetryManager;

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
					//suppress error due to invalid map while converting JSON and return null
				}
			}
		}
		return null;
	}
}
