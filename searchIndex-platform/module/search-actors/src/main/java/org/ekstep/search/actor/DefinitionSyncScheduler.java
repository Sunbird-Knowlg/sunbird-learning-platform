package org.ekstep.search.actor;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.ekstep.searchindex.util.ObjectDefinitionCache;

import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogManager;

public class DefinitionSyncScheduler {
	
	private static ILogger LOGGER = PlatformLogManager.getLogger();

	public static void init() {
		Timer time = new Timer(); // Instantiate Timer Object
		ScheduledTask st = new ScheduledTask(); // Instantiate SheduledTask class
		LOGGER.log("Initialising definition sync scheduler");
		time.schedule(st, 3600000, 3600000); // Create Repetitively task for every 1 hour
	}
}

class ScheduledTask extends TimerTask {

	private static ILogger LOGGER = PlatformLogManager.getLogger();
	
	@SuppressWarnings("rawtypes")
	public void run() {
		try {
			Map<String, Map> map = ObjectDefinitionCache.getDefinitionMap();
			if (null != map && !map.isEmpty()) {
				String graphId = "domain";
				for (String objectType : map.keySet()) {
					LOGGER.log("Syncing definition : " , objectType);
					ObjectDefinitionCache.resyncDefinition(objectType, graphId);
				}
			}
		} catch(Exception e) {
		}
	}
}
