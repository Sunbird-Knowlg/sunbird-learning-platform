package org.ekstep.sync.tool.shell;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.ekstep.sync.tool.service.CompositeIndexSyncManager;
import org.ekstep.sync.tool.util.CSVFileParserUtil;
import org.ekstep.sync.tool.util.DateRangeDataFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

@Component
public class SyncCommand implements CommandMarker {

	@Autowired
	CompositeIndexSyncManager compositeIndexSyncManager;

	@CliCommand(value = "syncbyids", help = "Sync data from Neo4j to Elastic Search by Id(s)")
	public void syncByIds(@CliOption(key = {
			"graphId" }, mandatory = false, unspecifiedDefaultValue = "domain", help = "graphId of the object") final String graphId,
			@CliOption(key = { "id","ids" }, mandatory = true, help = "Unique Id of node object") final String[] ids)
			throws Exception {
		
		long startTime = System.currentTimeMillis();
		List<String> identifiers = Arrays.asList(ids);
		compositeIndexSyncManager.syncNode(graphId, identifiers);
		long endTime = System.currentTimeMillis();
        long exeTime = endTime - startTime;
        System.out.println("Total time of execution: "+exeTime +"ms");
	}

	@CliCommand(value = "syncbyfile", help = "Sync data from Neo4j to Elastic Search by Id and objectType(optional) listed in a file")
	public void syncByFile(@CliOption(key = {
			"graphId" }, mandatory = false, unspecifiedDefaultValue = "domain", help = "graphId of the object") final String graphId,
			@CliOption(key = { "filePath" }, mandatory = true, help = "File Path of the csv file") String filePath,
			@CliOption(key = { "objectType" }, mandatory = false, help = "Object type ") String objectType)
			throws Exception {
		
		long startTime = System.currentTimeMillis();
		List<String> identifiers;
		identifiers = CSVFileParserUtil.getIdentifiers(filePath, objectType);
		compositeIndexSyncManager.syncNode(graphId, identifiers);
		long endTime = System.currentTimeMillis();
        long exeTime = endTime - startTime;
        System.out.println("Total time of execution: "+exeTime +"ms");
	}

	@CliCommand(value = "syncbyobjecttype", help = "Sync data from Neo4j to Elastic Search by the given object type")
	public void syncByObjectType(@CliOption(key = {
			"graphId" }, mandatory = false, unspecifiedDefaultValue = "domain", help = "graphId of the object") final String graphId,
			@CliOption(key = {
					"objectType" }, mandatory = true, help = "Object type needs to be validated") String objectType)
			throws Exception {

		long startTime = System.currentTimeMillis();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime start = LocalDateTime.now();
		compositeIndexSyncManager.syncNode(graphId, objectType);
		long endTime = System.currentTimeMillis();
        long exeTime = endTime - startTime;
        System.out.println("Total time of execution: "+exeTime +"ms");
        LocalDateTime end = LocalDateTime.now();
		System.out.println("START_TIME" + dtf.format(start)+"END_TIME: " + dtf.format(end));
	}

	@CliCommand(value = "syncbydaterange", help = "Sync data from Neo4j to Elastic Search by the given date range")
	public void syncByDateRange(@CliOption(key = {
			"graphId" }, mandatory = false, unspecifiedDefaultValue = "domain", help = "graphId of the object") final String graphId,
			@CliOption(key = {
					"startDate" }, mandatory = true, help = "Start date of the data to be synced") String startDate,
			@CliOption(key = {
					"endDate" }, mandatory = true, help = "End date of the data to be synced") String endDate,
			@CliOption(key = {
					"objectType" }, mandatory = true, help = "Object type needs to be validated") String objectType)
			throws Exception {
		
		DateRangeDataFetcher fetcher = new DateRangeDataFetcher();
		List<String> ids = fetcher.neo4jData(objectType, startDate, endDate);
		System.out.println("Ids are: " + ids);
	}
}
