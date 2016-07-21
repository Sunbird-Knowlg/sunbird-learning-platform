package org.ekstep.manager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface IHealthCheckManager {

	public List<Map<String, Object>> getAllServiceHealth() throws Exception;
	
}
