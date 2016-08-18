package com.ilimi.manager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Response;
import com.ilimi.common.mgr.HealthCheckManager;
import com.ilimi.graph.common.mgr.Configuration;
import com.ilimi.orchestrator.dac.service.IOrchestratorDataService;
import com.ilimi.util.DatabasePropertiesUtil;

@Component
public class LearningHealthCheckManager extends HealthCheckManager {

	private static Logger LOGGER = LogManager.getLogger(LearningHealthCheckManager.class.getName());
	private static final int MAX_THREAD_NUM = 10;

	@Autowired
	private IOrchestratorDataService orchestratorService;

	@Override
	public Response getAllServiceHealth() throws Exception {
		List<Map<String, Object>> checks = new ArrayList<Map<String, Object>>();
		boolean overallHealthy = true;
		ExecutorService executor = Executors.newFixedThreadPool(MAX_THREAD_NUM);
		List<FutureTask<Map<String, Object>>> taskList = new ArrayList<FutureTask<Map<String, Object>>>();
		FutureTask<Map<String, Object>> futureTask_graphs;
		List<String> graphIds = Configuration.graphIds;
		if (null != graphIds && graphIds.size() > 0) {
			for (final String id : graphIds) {
				futureTask_graphs = new FutureTask<Map<String, Object>>(new Callable<Map<String, Object>>() {
					@Override
					public Map<String, Object> call() {
						return checkGraphHealth(id, LOGGER);
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

		FutureTask<Map<String, Object>> futureTask_Mongo = new FutureTask<Map<String, Object>>(
				new Callable<Map<String, Object>>() {
					@Override
					public Map<String, Object> call() {
						return checkMongoDBHealth();
					}
				});
		taskList.add(futureTask_Mongo);
		executor.execute(futureTask_Mongo);

		FutureTask<Map<String, Object>> futureTask_MySQL = new FutureTask<Map<String, Object>>(
				new Callable<Map<String, Object>>() {
					@Override
					public Map<String, Object> call() {
						return checkMySQLHealth();
					}
				});
		taskList.add(futureTask_MySQL);
		executor.execute(futureTask_MySQL);

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

	private Map<String, Object> checkMongoDBHealth() {
		Map<String, Object> check = new HashMap<String, Object>();
		check.put("name", "MongoDB");

		try {
			boolean status = orchestratorService.isCollectionExist();
			check.put("healthy", status);
			if (!status) {
				check.put("err", "404"); // error code, if any
				check.put("errmsg", " MongoDB collection is not available"); 
			}
		} catch (Throwable e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			check.put("healthy", false);
			check.put("err", "503"); // error code, if any
			check.put("errmsg", " MongoDB is not available"); 
		}

		return check;
	}

	private static Map<String, Object> checkMySQLHealth() {
		Map<String, Object> check = new HashMap<String, Object>();
		check.put("name", "My SQL");
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection(DatabasePropertiesUtil.getProperty("db.url"),
					DatabasePropertiesUtil.getProperty("db.username"),
					DatabasePropertiesUtil.getProperty("db.password"));
			Statement stmt = (Statement) con.createStatement();
			ResultSet rs = stmt.executeQuery("select 1");
			if (rs.next())
				check.put("healthy", true);
			else {
				check.put("healthy", false);
				check.put("err", ""); // error code, if any
				check.put("errmsg", "DB is not available"); 
			}
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			check.put("healthy", false);
			check.put("err", "503"); // error code, if any
			check.put("errmsg", e.getMessage()); // default English error
													// message
		}

		return check;
	}
}
