package org.ekstep.searchindex.util;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.ilimi.common.logger.PlatformLogger;

public class LogAsyncGraphEvent {

	private static final Logger graphEventLogger = LogManager.getLogger("GraphEventLogger");
	private static ObjectMapper mapper = new ObjectMapper();
	
	
	public static void pushMessageToLogger(List<Map<String, Object>> messages) {
		if (null == messages || messages.size() <= 0) return; 
		for (Map<String, Object> message : messages) {
			try{
				String jsonMessage = mapper.writeValueAsString(message);
				PlatformLogger.log("Logging kafka message to graph_event.log"+ jsonMessage);
				if (StringUtils.isNotBlank(jsonMessage))
					graphEventLogger.info(jsonMessage);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
