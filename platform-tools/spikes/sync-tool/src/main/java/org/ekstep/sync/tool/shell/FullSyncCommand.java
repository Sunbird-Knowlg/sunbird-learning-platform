package org.ekstep.sync.tool.shell;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.sync.tool.mgr.HierarchySyncManager;
import org.ekstep.sync.tool.mgr.ISyncManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

@Component
public class FullSyncCommand  implements CommandMarker {

	@Autowired
	@Qualifier("neo4jESSyncManager") 
	ISyncManager indexSyncManager;

	@Autowired
	@Qualifier("hierarchySyncManager")
	HierarchySyncManager hierarchySyncManager;

	
	@CliCommand(value = "sync", help = "Sync data from Neo4j to Elastic Search by Id(s)")
	public void syncByIds(@CliOption(key = { "type" }, mandatory = false, unspecifiedDefaultValue = "full", help = "Sync type.") final String type,
			@CliOption(key = {
			"graph" }, mandatory = true, help = "graphId of the object") final String graphId,
			@CliOption(key = {
			"delay" }, mandatory = false, unspecifiedDefaultValue = "10", help = "time gap between each batch") final Integer delay,
			@CliOption(key = {
			"objectType" }, mandatory = false, help = "time gap between each batch") final String[] objectType)
			throws Exception {
		System.out.println("Fetching data from graph: "+ graphId + ".");
		System.out.println("-----------------------------------------");
		if(StringUtils.equalsIgnoreCase("hierarchy", type)){
			hierarchySyncManager.syncHierarchy(graphId);
		}else {
			indexSyncManager.syncGraph(graphId, delay, objectType);
		}
		System.out.println("-----------------------------------------");
	}
	
}
