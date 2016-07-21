package org.ekstep.manager.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.ekstep.manager.IHealthCheckManager;
import org.ekstep.utils.HealthCheckUtil;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Response;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.common.mgr.Configuration;

@Component
public class HealthCheckManager extends BaseManager implements IHealthCheckManager {

	private static final int MAX_THREAD_NUM = 10;
	
	@Override
	public Response getAllServiceHealth() throws InterruptedException, ExecutionException {
		
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
		                return HealthCheckUtil.checkNeo4jGraph(id);
		            }
		        });
		        taskList.add(futureTask_graphs);
		        executor.execute(futureTask_graphs);
			}
		}

		FutureTask<Map<String, Object>> futureTask_Redis = new FutureTask<Map<String, Object>>(new Callable<Map<String, Object>>() {
            @Override
            public Map<String, Object> call() {
                return HealthCheckUtil.checkRedis();
            }
        });
        taskList.add(futureTask_Redis);
        executor.execute(futureTask_Redis);

        FutureTask<Map<String, Object>> futureTask_Mongo = new FutureTask<Map<String, Object>>(new Callable<Map<String, Object>>() {
            @Override
            public Map<String, Object> call() {
                return HealthCheckUtil.checkMongoDB();
            }
        });
        taskList.add(futureTask_Mongo);
        executor.execute(futureTask_Mongo);

        for (int j = 0; j < taskList.size(); j++) {
            FutureTask<Map<String, Object>> futureTask = taskList.get(j);
            Map<String, Object> check = futureTask.get();
            if((boolean)check.get("healthy") == false){
            	overallHealthy = false;
            }
            checks.add(check);
        }
        executor.shutdown();

        Response response = OK("checks", checks);
        response.put("healthy", overallHealthy);

		return response;
	}

	@Override
	public Response registerGraph(String graphId) {
		Configuration.registerNewGraph(graphId);
		return OK();
	}

}
