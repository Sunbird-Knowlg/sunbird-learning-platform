package org.ekstep.manager.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.ekstep.manager.IHealthCheckManager;
import org.ekstep.utils.HealthCheckUtil;

public class HealthCheckManager implements IHealthCheckManager {

	private static final int MAX_THREAD_NUM = 10;
	
	@Override
	public List<Map<String, Object>> getAllServiceHealth() {
		
		
		ExecutorService executor = Executors.newFixedThreadPool(MAX_THREAD_NUM);
        List<FutureTask<Integer>> taskList = new ArrayList<FutureTask<Integer>>();

        FutureTask<Map<String, Object>> futureTask_1 = new FutureTask<Map<String, Object>>(new Callable<Map<String, Object>>() {
            @Override
            public Map<String, Object> call() {
                return HealthCheckUtil.checkRedis();
            }
        });

        FutureTask<Map<String, Object>> futureTask_3 = new FutureTask<Map<String, Object>>(new Callable<Map<String, Object>>() {
            @Override
            public Map<String, Object> call() {
                return HealthCheckUtil.checkMongoDB();
            }
        });
		return null;
	}

}
