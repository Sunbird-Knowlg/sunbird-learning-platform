package org.sunbird.sync.tool.shell;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;
import org.sunbird.sync.tool.mgr.CSPMigrationMessageGenerator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class MigrateCSPDataCommand implements CommandMarker {

	@Autowired
	CSPMigrationMessageGenerator cspMsgGenerator;

	@CliCommand(value = "migratecspdata", help = "Generate CSP Data Migration Event")
	public void migrateCSPData(
					@CliOption(key = {"graphId"}, mandatory = false, unspecifiedDefaultValue = "domain", help = "graphId of the object") final String graphId,
					@CliOption(key = {"objectType"}, mandatory = true, help = "Object Type is Required") final String[] objectType,
					@CliOption(key = {"mimeType"}, mandatory = false, help = "mimeTypes can be provided") final String[] mimeType,
					@CliOption(key = {"status"}, mandatory = false, help = "Specific Status can be passed") final String[] status,
					@CliOption(key = {"migrationVersion"}, mandatory = false, unspecifiedDefaultValue = "0", help = "Specific migration version can be passed") final double migrationVersion,
					@CliOption(key = {"limit"}, mandatory = false, unspecifiedDefaultValue = "0", help = "Specific Limit can be passed") final Integer limit,
					@CliOption(key = {"delay"}, mandatory = false, unspecifiedDefaultValue = "10", help = "time gap between each batch") final Integer delay)
					throws Exception {

		long startTime = System.currentTimeMillis();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime start = LocalDateTime.now();
		cspMsgGenerator.generateMgrMsg(graphId, objectType, mimeType, status, migrationVersion, limit, delay);
		long endTime = System.currentTimeMillis();
		long exeTime = endTime - startTime;
		System.out.println("Total time of execution: " + exeTime + "ms");
		LocalDateTime end = LocalDateTime.now();
		System.out.println("START_TIME: " + dtf.format(start) + ", END_TIME: " + dtf.format(end));
	}


}
