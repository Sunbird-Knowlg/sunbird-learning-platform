package com.ilimi.common.util;

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
		te.setEid("BE_CONTENT_LIFECYCLE");
		te.setEts(unixTime);
		te.setVer("2.0");
		te.setPdata("org.ekstep.content.platform", "", "1.0", "");
		te.setEdata(contentId, metadata.get("status"), metadata.get("prevState"),
				metadata.get("size"),metadata.get("pkgVersion"),
				metadata.get("concepts"), metadata.get("flags"), metadata.get("downloadUrl"));
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
