package org.ekstep.manager;

import java.util.List;
import java.util.Map;

public interface IHealthCheckManager {

	public List<Map<String, Object>> getAllServiceHealth();
	
}
