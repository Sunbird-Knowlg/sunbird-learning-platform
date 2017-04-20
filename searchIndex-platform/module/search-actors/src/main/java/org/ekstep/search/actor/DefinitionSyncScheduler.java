package org.ekstep.search.actor;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.searchindex.util.ObjectDefinitionCache;

public class DefinitionSyncScheduler {
	
	private static Logger LOGGER = LogManager.getLogger(DefinitionSyncScheduler.class.getName());

	public static void init() {
		Timer time = new Timer(); // Instantiate Timer Object
		ScheduledTask st = new ScheduledTask(); // Instantiate SheduledTask class
		LOGGER.info("Initialising definition sync scheduler");
		time.schedule(st, 3600000, 3600000); // Create Repetitively task for every 1 hour
	}
}

class ScheduledTask extends TimerTask {

	private static Logger LOGGER = LogManager.getLogger(DefinitionSyncScheduler.class.getName());
	
	@SuppressWarnings("rawtypes")
	public void run() {
		try {
			Map<String, Map> map = ObjectDefinitionCache.getDefinitionMap();
			if (null != map && !map.isEmpty()) {
				String graphId = "domain";
				for (String objectType : map.keySet()) {
					LOGGER.info("Syncing definition : " + objectType);
					ObjectDefinitionCache.resyncDefinition(objectType, graphId);
				}
			}
		} catch(Exception e) {
		}
	}
}
