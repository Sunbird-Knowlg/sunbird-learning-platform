package org.ekstep.language.util;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.TelemetryBEEvent;

public class LogWordEventUtil {

	private static Logger LOGGER = LogManager.getLogger(LogWordEventUtil.class.getName());
	private static final Logger wordEventLogger = LogManager.getLogger("WordEventLogger");
	private static ObjectMapper mapper = new ObjectMapper();

	public static String logWordLifecycleEvent(String wordId, Map<String, Object> metadata) {
		TelemetryBEEvent te = new TelemetryBEEvent();
		long unixTime = System.currentTimeMillis() / 1000L;
		te.setEid("BE_WORD_LIFECYCLE");
		te.setEts(unixTime);
		te.setVer("2.0");
		te.setPdata("org.ekstep.language.platform", "", "1.0", "");
		te.setEdata(wordId, metadata.get("status"), metadata.get("prevState"), metadata.get("lemma"));
		String jsonMessage = null;
		try {
			jsonMessage = mapper.writeValueAsString(te);
			if (StringUtils.isNotBlank(jsonMessage))
				wordEventLogger.info(jsonMessage);
		} catch (Exception e) {
			LOGGER.error("Error logging BE_WORD_LIFECYCLE event", e);
		}
		return jsonMessage;
	}

	/*public static String logWordPublishEvent(String query, Object filters, Object sort, String correlationId, int size) {
		TelemetryBEEvent te = new TelemetryBEEvent();
		long unixTime = System.currentTimeMillis() / 1000L;
		te.setEid("BE_WORD_PUBLISH");
		te.setEts(unixTime);
		te.setVer("2.0");
		te.setPdata("org.ekstep.language.platform", "", "1.0", "");
		te.setEdata(query, filters, sort, correlationId, size);
		String jsonMessage = null;
		try {
			jsonMessage = mapper.writeValueAsString(te);
			if (StringUtils.isNotBlank(jsonMessage))
				wordEventLogger.info(jsonMessage);
		} catch (Exception e) {
			LOGGER.error("Error logging BE_WORD_PUBLISH event", e);
		}
		return jsonMessage;
	}*/
}
