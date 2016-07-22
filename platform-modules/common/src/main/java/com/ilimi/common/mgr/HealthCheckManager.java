package com.ilimi.common.mgr;

import com.ilimi.common.dto.Response;
import com.ilimi.graph.common.mgr.Configuration;

public abstract class HealthCheckManager extends BaseManager{

	public abstract Response getAllServiceHealth() throws Exception;
	
	public Response registerGraph(String graphId){
		Configuration.registerNewGraph(graphId);
		return OK();
	}

}
