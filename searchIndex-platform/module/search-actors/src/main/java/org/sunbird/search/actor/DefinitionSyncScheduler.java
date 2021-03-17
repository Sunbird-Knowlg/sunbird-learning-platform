package org.sunbird.search.actor;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.sunbird.searchindex.util.ObjectDefinitionCache;
import org.sunbird.telemetry.logger.TelemetryManager;

public class DefinitionSyncScheduler {
	
	

	public static void init() {
		Timer time = new Timer(); // Instantiate Timer Object
		ScheduledTask st = new ScheduledTask(); // Instantiate SheduledTask class
		TelemetryManager.log("Initialising definition sync scheduler");
		time.schedule(st, 3600000, 3600000); // Create Repetitively task for every 1 hour
	}
}

class ScheduledTask extends TimerTask {

	
	
	@SuppressWarnings("rawtypes")
	public void run() {
		try {
			Map<String, Map> map = ObjectDefinitionCache.getDefinitionMap();
			if (null != map && !map.isEmpty()) {
				String graphId = "domain";
				for (String objectType : map.keySet()) {
					TelemetryManager.log("Syncing definition : " + objectType);
					ObjectDefinitionCache.resyncDefinition(objectType, graphId);
				}
			}
		} catch(Exception e) {
		}
	}
}
