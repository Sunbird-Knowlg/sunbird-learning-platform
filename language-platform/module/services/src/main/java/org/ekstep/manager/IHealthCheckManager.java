package org.ekstep.manager;

import com.ilimi.common.dto.Response;

public interface IHealthCheckManager {

	public Response getAllServiceHealth() throws Exception;
	
	public Response registerGraph(String graphId);
}
