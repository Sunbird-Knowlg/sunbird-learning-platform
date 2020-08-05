package org.ekstep.sync.tool.shell;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ekstep.sync.tool.mgr.AssessmentItemSyncManager;
import org.ekstep.sync.tool.mgr.CassandraESSyncManager;
import org.ekstep.sync.tool.mgr.ISyncManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

@Component
public class SyncShellCommands implements CommandMarker {

	@Autowired
	@Qualifier("neo4jESSyncManager")
	ISyncManager indexSyncManager;

	@Autowired
	CassandraESSyncManager syncManager;
	
	@Autowired
	AssessmentItemSyncManager assessmentItemSyncManager;

	@CliCommand(value = "syncbyids", help = "Sync data from Neo4j to Elastic Search by Id(s)")
	public void syncByIds(@CliOption(key = {
			"graphId" }, mandatory = false, unspecifiedDefaultValue = "domain", help = "graphId of the object") final String graphId,
			@CliOption(key = { "id", "ids" }, mandatory = true, help = "Unique Id of node object") final String[] ids)
			throws Exception {

		long startTime = System.currentTimeMillis();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime start = LocalDateTime.now();
		indexSyncManager.syncByIds(graphId, new ArrayList<>(Arrays.asList(ids)));
		long endTime = System.currentTimeMillis();
		long exeTime = endTime - startTime;
		System.out.println("Total time of execution: " + exeTime + "ms");
		LocalDateTime end = LocalDateTime.now();
		System.out.println("START_TIME: " + dtf.format(start) + ", END_TIME: " + dtf.format(end));
	}
	@CliCommand(value = "syncbybookmarkids", help = "Sync units from cassandra to Elastic Search by Id(s)")
	public void syncByBookmark(
			@CliOption(key = {"graphId"}, mandatory = false, unspecifiedDefaultValue = "domain", help = "graphId of the object") final String graphId,
			@CliOption(key = {"id","ids"}, mandatory = true, help = "Unique Id of node object") final String[] ids,
			@CliOption(key = {"bookmarkId", "bookmarkIds"}, mandatory = false, help = "Unique Id of node object") final String[] bookmarkIds) throws Exception {
		long startTime = System.currentTimeMillis();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime start = LocalDateTime.now();
		if(bookmarkIds != null)
			syncManager.syncAllIds(graphId, new ArrayList<>(Arrays.asList(ids)), new ArrayList<>(Arrays.asList(bookmarkIds)));
		else
			syncManager.syncAllIds(graphId, new ArrayList<>(Arrays.asList(ids)), null);

		long endTime = System.currentTimeMillis();
		long exeTime = endTime - startTime;
		System.out.println("Total time of execution: " + exeTime + "ms");
		LocalDateTime end = LocalDateTime.now();
		System.out.println("START_TIME: " + dtf.format(start) + ", END_TIME: " + dtf.format(end));
	}

	@CliCommand(value = "syncleafnodescountbyids", help = "Refresh leafNodesCount by Id(s) in both Cassandra and Elastic Search")
	public void syncLeafNodesCountByIds(
			@CliOption(key = {"graphId"}, mandatory = false, unspecifiedDefaultValue = "domain", help = "graphId of the object") final String graphId,
			@CliOption(key = {"id","ids"}, mandatory = true, help = "Unique Id of node object") final String[] ids) throws Exception {
		long startTime = System.currentTimeMillis();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime start = LocalDateTime.now();

		syncManager.syncLeafNodesCountByIds(graphId, new ArrayList<>(Arrays.asList(ids)));

		long endTime = System.currentTimeMillis();
		long exeTime = endTime - startTime;
		System.out.println("Total time of execution: " + exeTime + "ms");
		LocalDateTime end = LocalDateTime.now();
		System.out.println("START_TIME: " + dtf.format(start) + ", END_TIME: " + dtf.format(end));
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
		indexSyncManager.syncByObjectType(graphId, objectType);
		long endTime = System.currentTimeMillis();
		long exeTime = endTime - startTime;
		System.out.println("Total time of execution: " + exeTime + "ms");
		LocalDateTime end = LocalDateTime.now();
		System.out.println("START_TIME: " + dtf.format(start) + ", END_TIME: " + dtf.format(end));
	}

	@CliCommand(value = "syncbydaterange", help = "Sync data from Neo4j to Elastic Search by the given date range")
	public void syncByDateRange(@CliOption(key = {
			"graphId" }, mandatory = false, unspecifiedDefaultValue = "domain", help = "graphId of the object") final String graphId,
			@CliOption(key = {
					"startDate" }, mandatory = true, help = "Start date of the data to be synced") String startDate,
			@CliOption(key = {
					"endDate" }, mandatory = true, help = "End date of the data to be synced") String endDate,
			@CliOption(key = { "objectType" }, mandatory = false, help = "Object type ") String objectType)
			throws Exception {

		long startTime = System.currentTimeMillis();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime start = LocalDateTime.now();
		indexSyncManager.syncByDateRange(graphId, startDate, endDate, objectType);
		long endTime = System.currentTimeMillis();
		long exeTime = endTime - startTime;
		System.out.println("Total time of execution: " + exeTime + "ms");
		LocalDateTime end = LocalDateTime.now();
		System.out.println("START_TIME: " + dtf.format(start) + ", END_TIME: " + dtf.format(end));
	}

	@CliCommand(value = "syncleafnodesbyids", help = "Refresh leafNodes by Id(s) for Collection MimeTypes")
	public void syncLeafNodesByIds(
			@CliOption(key = {"id","ids"}, mandatory = true, help = "Unique Id of node object") final String[] ids) throws Exception {
		long startTime = System.currentTimeMillis();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime start = LocalDateTime.now();

		syncManager.syncLeafNodesByIds(new ArrayList<>(Arrays.asList(ids)));

		long endTime = System.currentTimeMillis();
		long exeTime = endTime - startTime;
		System.out.println("Total time of execution: " + exeTime + "ms");
		LocalDateTime end = LocalDateTime.now();
		System.out.println("START_TIME: " + dtf.format(start) + ", END_TIME: " + dtf.format(end));
	}
	
	@CliCommand(value = "syncdialcodes", help = "Refresh leafNodes by Id(s) for Collection MimeTypes")
	public void syncDialcodes(
			@CliOption(key = {"id","ids"}, mandatory = false, help = "Unique Id of node object") final String[] ids) throws Exception {
		long startTime = System.currentTimeMillis();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime start = LocalDateTime.now();

		syncManager.syncDialcodesByIds(new ArrayList<String>(Arrays.asList(ids)));

		long endTime = System.currentTimeMillis();
		long exeTime = endTime - startTime;
		System.out.println("Total time of execution: " + exeTime + "ms");
		LocalDateTime end = LocalDateTime.now();
		System.out.println("START_TIME: " + dtf.format(start) + ", END_TIME: " + dtf.format(end));
		
	}
	
	@CliCommand(value = "migratequestionextproperties", help = "Migrate AssessmentItems external properties")
	public void migrateQuestionExternalProperties(
			@CliOption(key = {"objectType"}, mandatory = true, help = "Object Type of assessmentItem") final String objectType,
			@CliOption(key = {"ids"}, mandatory = false, help = "Unique Ids of assessmentItem") final String[] ids) throws Exception {
		long startTime = System.currentTimeMillis();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime start = LocalDateTime.now();

		List<String> listOfIds = null;
		if(null != ids)
			listOfIds = Arrays.asList(ids);
		assessmentItemSyncManager.syncAssessmentExternalProperties("domain", objectType, listOfIds, 1000);

		long endTime = System.currentTimeMillis();
		long exeTime = endTime - startTime;
		System.out.println("Total time of execution: " + exeTime + "ms");
		LocalDateTime end = LocalDateTime.now();
		System.out.println("START_TIME: " + dtf.format(start) + ", END_TIME: " + dtf.format(end));
		}
}
