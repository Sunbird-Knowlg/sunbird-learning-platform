package org.sunbird.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.sunbird.cassandra.connector.util.CassandraConnector;
import org.sunbird.common.dto.Response;
import org.sunbird.common.mgr.HealthCheckManager;
import org.sunbird.telemetry.logger.TelemetryManager;
import org.springframework.stereotype.Component;

@Component
public class LearningHealthCheckManager extends HealthCheckManager {

	
	private static final int MAX_THREAD_NUM = 10;

	@Override
	public Response getAllServiceHealth() throws Exception {
		List<Map<String, Object>> checks = new ArrayList<Map<String, Object>>();
		boolean overallHealthy = true;
		ExecutorService executor = Executors.newFixedThreadPool(MAX_THREAD_NUM);
		List<FutureTask<Map<String, Object>>> taskList = new ArrayList<FutureTask<Map<String, Object>>>();
		FutureTask<Map<String, Object>> futureTask_graphs;
		List<String> graphIds = Arrays.asList("domain");
		if (null != graphIds && graphIds.size() > 0) {
			for (final String id : graphIds) {
				futureTask_graphs = new FutureTask<Map<String, Object>>(new Callable<Map<String, Object>>() {
					@Override
					public Map<String, Object> call() {
						return checkGraphHealth(id);
					}
				});
				taskList.add(futureTask_graphs);
				executor.execute(futureTask_graphs);
			}
		}

		FutureTask<Map<String, Object>> futureTask_Redis = new FutureTask<Map<String, Object>>(
				new Callable<Map<String, Object>>() {
					@Override
					public Map<String, Object> call() {
						return checkRedisHealth();
					}
				});
		taskList.add(futureTask_Redis);
		executor.execute(futureTask_Redis);

		FutureTask<Map<String, Object>> futureTask_Cassandra = new FutureTask<Map<String, Object>>(
				new Callable<Map<String, Object>>() {
					@Override
					public Map<String, Object> call() throws Exception {
						return checkCassandraHealth();
					}
				});
		taskList.add(futureTask_Cassandra);
		executor.execute(futureTask_Cassandra);
		
		for (int j = 0; j < taskList.size(); j++) {
			FutureTask<Map<String, Object>> futureTask = taskList.get(j);
			Map<String, Object> check = futureTask.get();
			if ((boolean) check.get("healthy") == false) {
				overallHealthy = false;
			}
			checks.add(check);
		}
		executor.shutdown();

		Response response = OK("checks", checks);
		response.put("healthy", overallHealthy);

		return response;
	}

	private Map<String, Object> checkCassandraHealth(){
		Map<String, Object> check = new HashMap<String, Object>();
		check.put("name", "cassandra db");
		
		try {
			boolean status = CassandraConnector.testCassandraConnection();
			check.put("healthy", status);
			if(!status) {
				check.put("err", "404"); // error code, if any
				check.put("errmsg", " Cassandra connection is not available"); 
			}
		}catch(Exception e) {
			e.printStackTrace();
			TelemetryManager.error("Exception: "+ e.getMessage(), e);
    			check.put("healthy", false);
    			check.put("err", "503"); // error code, if any
    			check.put("errmsg", "Cassandra connection is not available"); 
		}
		return check;
	}
}
