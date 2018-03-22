package org.ekstep.sync.tool.shell;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.ekstep.sync.tool.service.CompositeIndexSyncManager;
import org.ekstep.sync.tool.util.CSVFileParser;
import org.ekstep.sync.tool.util.DateRangeDataFetcher;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

@Component
public class SyncCommand implements CommandMarker{

	CompositeIndexSyncManager compositeIndexSyncManager = new CompositeIndexSyncManager();
	
	@CliCommand(value = "syncbyid", help = "Sync data from Neo4j to Elastic Search by Id")
	public void syncById(@CliOption(key = { "id" }, mandatory = true, help = "Unique Id of node object") final String id) {
		List<String> identifiers = new ArrayList<>();
		identifiers.add(id);
		try {
			compositeIndexSyncManager.syncNode("domain", identifiers);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@CliCommand(value = "syncbyfileandobjtype", help = "Sync data from Neo4j to Elastic Search by Id and objectType listed in a file")
	public void syncByFile(@CliOption(key = {"filePath"}, mandatory = true, help = "File Path of the csv file") String filePath,
						  @CliOption(key = {"objectType"}, mandatory = true, help = "Object type needs to be validated") String objectType){
		CSVFileParser parser = new CSVFileParser();
		try {
			List<String> identifiers = parser.csvData(filePath, objectType);
			compositeIndexSyncManager.syncNode("domain", identifiers);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@CliCommand(value = "syncbyfile", help = "Sync data from Neo4j to Elastic Search by Id listed in a file")
	public void syncByFile(@CliOption(key = {"filePath"}, mandatory = true, help = "File Path of the csv file") String filePath){
		CSVFileParser parser = new CSVFileParser();
		try {
			List<String> identifiers = parser.csvData(filePath);
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  //TODO: Remove this line
			LocalDateTime now = LocalDateTime.now();  //TODO: Remove this line
			System.out.println("START_TIME" + dtf.format(now));  //TODO: Remove this line
			compositeIndexSyncManager.syncNode("domain", identifiers);
			now = LocalDateTime.now(); //TODO: Remove this line
			System.out.println("END_TIME: "+dtf.format(now));//TODO: Remove this line
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@CliCommand(value = "syncbyobjecttype", help = "Sync data from Neo4j to Elastic Search by the given object type")
	public void syncByObjectType(@CliOption(key = {"objectType"}, mandatory = true, help = "Object type needs to be validated") String objectType){
		
		try {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  //TODO: Remove this line
			LocalDateTime now = LocalDateTime.now();  //TODO: Remove this line
			System.out.println("START_TIME" + dtf.format(now));  //TODO: Remove this line
			
			compositeIndexSyncManager.syncNode("domain", objectType);
			
			
			now = LocalDateTime.now(); //TODO: Remove this line
			System.out.println("END_TIME: "+dtf.format(now));//TODO: Remove this line
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@CliCommand(value = "syncbydaterange", help = "Sync data from Neo4j to Elastic Search by the given date range")
	public void syncByDateRange(@CliOption(key = {"startDate"}, mandatory = true, help = "Start date of the data to be synced") String startDate,
			@CliOption(key = {"endDate"}, mandatory = true, help = "End date of the data to be synced") String endDate, 
			@CliOption(key = {"objectType"}, mandatory = true, help = "Object type needs to be validated") String objectType) throws Exception{
		DateRangeDataFetcher fetcher = new DateRangeDataFetcher();
		List<String> ids = fetcher.neo4jData(objectType, startDate, endDate);
		System.out.println("Ids are: " +ids);
	}
}
