package com.ilimi.common.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.TelemetryBEEvent;

public class LogTelemetryEventUtil {

	private static Logger LOGGER = LogManager.getLogger(LogTelemetryEventUtil.class.getName());
	private static final Logger telemetryEventLogger = LogManager.getLogger("TelemetryEventLogger");
	private static ObjectMapper mapper = new ObjectMapper();

	public static String logContentLifecycleEvent(String contentId, Map<String, Object> metadata) {
		TelemetryBEEvent te = new TelemetryBEEvent();
		long unixTime = System.currentTimeMillis();
		Map<String,Object> data = new HashMap<String,Object>();
		te.setEid("BE_CONTENT_LIFECYCLE");
		te.setEts(unixTime);
		te.setVer("2.0");
		te.setPdata("org.ekstep.content.platform", "", "1.0", "");
		data.put("cid", contentId);
		data.put("size", metadata.get("size"));
		data.put("pkgVersion", metadata.get("pkgVersion"));
		data.put("concepts", metadata.get("concepts"));
		data.put("state", metadata.get("status"));
		data.put("prevState", metadata.get("prevState"));
		data.put("downloadUrl", metadata.get("downloadUrl"));
		data.put("contentType", metadata.get("contentType"));
		data.put("mediaType", metadata.get("mediaType"));
		data.put("flags",metadata.get("flags"));
		te.setEdata(data);
		
		String jsonMessage = null;
		try {
			jsonMessage = mapper.writeValueAsString(te);
			if (StringUtils.isNotBlank(jsonMessage))
				telemetryEventLogger.info(jsonMessage);
		} catch (Exception e) {
			LOGGER.error("Error logging BE_CONTENT_LIFECYCLE event", e);
		}
		return jsonMessage;
	}

	public static String logContentSearchEvent(String query, Object filters, Object sort, String correlationId, int size) {
		TelemetryBEEvent te = new TelemetryBEEvent();
		long unixTime = System.currentTimeMillis();
		te.setEid("BE_CONTENT_SEARCH");
		te.setEts(unixTime);
		te.setVer("2.0");
		te.setPdata("org.ekstep.search.platform", "", "1.0", "");
		te.setEdata(query, filters, sort, correlationId, size);
		String jsonMessage = null;
		try {
			jsonMessage = mapper.writeValueAsString(te);
			if (StringUtils.isNotBlank(jsonMessage))
				telemetryEventLogger.info(jsonMessage);
		} catch (Exception e) {
			LOGGER.error("Error logging BE_CONTENT_LIFECYCLE event", e);
		}
		return jsonMessage;
	}
}
