package org.ekstep.sync.tool.shell;

import java.util.List;

import org.ekstep.sync.tool.service.CompositeIndexSyncManager;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

@Component
public class SyncCommand implements CommandMarker{

	@CliCommand(value = "syncbyid", help = "Sync data from Neo4j to Elastic Search by Id")
	public void syncById(@CliOption(key = { "id" }, mandatory = true, help = "Unique Id of node object") final String id,
			@CliOption(key = { "type" }, mandatory = true, help = "Object type of node object") final String type) {
		System.out.println("Id: " + id);
		System.out.println("Type: " + type);
		CompositeIndexSyncManager compositeIndexSyncManager = new CompositeIndexSyncManager();
		try {
			compositeIndexSyncManager.syncNode("domain", id);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@CliCommand(value = "syncByFileObjType", help = "Sync data from Neo4j to Elastic Search by Id listed in a file")
	public void syncByFile(@CliOption(key = {"filePath"}, mandatory = true, help = "File Path of the csv file") String filePath,
			@CliOption(key = {"objectType"}, mandatory = true, help = "Object type needs to be validated") String objectType) throws Exception{
		csvFileParser parser = new csvFileParser();
		List<String> ids = parser.csvData(filePath, objectType);
		System.out.println("Ids are: " +ids);
	}
	
	@CliCommand(value = "syncByFileId", help = "Sync data from Neo4j to Elastic Search by Id listed in a file")
	public void syncByFile(@CliOption(key = {"filePath"}, mandatory = true, help = "File Path of the csv file") String filePath) throws Exception{
		csvFileParser parser = new csvFileParser();
		List<String> ids = parser.csvData(filePath);
		System.out.println("Ids are: " +ids);
	}
}
