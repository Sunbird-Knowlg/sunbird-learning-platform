package org.sunbird.sync.tool.shell;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

import org.sunbird.sync.tool.mgr.CassandraESSyncManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

@Component
public class SyncECMLContent implements CommandMarker{
	
	@Autowired
	CassandraESSyncManager syncManager;

	@CliCommand(value = "syncecml", help = "It syncs ecml content external link to cassandra table." )
	public void syncByIds(
			@CliOption(key = { "id", "ids" }, mandatory = true, help = "Unique Id of node object") final String[] ids)
	throws Exception {

		long startTime = System.currentTimeMillis();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime start = LocalDateTime.now();
		syncManager.syncECMLContent(new ArrayList<>(Arrays.asList(ids)));
		long endTime = System.currentTimeMillis();
		long exeTime = endTime - startTime;
		System.out.println("Total time of execution: " + exeTime + "ms");
		LocalDateTime end = LocalDateTime.now();
		System.out.println("START_TIME: " + dtf.format(start) + ", END_TIME: " + dtf.format(end));
	}

}
