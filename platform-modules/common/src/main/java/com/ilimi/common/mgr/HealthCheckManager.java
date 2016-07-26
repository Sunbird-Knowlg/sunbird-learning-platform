package com.ilimi.common.mgr;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.graph.common.mgr.Configuration;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.engine.router.GraphEngineManagers;
public abstract class HealthCheckManager extends BaseManager{

	public abstract Response getAllServiceHealth() throws Exception;
	
	public Response registerGraph(String graphId){
		Configuration.registerNewGraph(graphId);
		return OK();
	}

	protected Map<String, Object> checkGraphHealth(String graphId , Logger LOGGER){
		Map<String, Object> check = new HashMap<String, Object>();
		check.put("name", graphId + " graph");

		try{
			Request createReq = getRequest(graphId, GraphEngineManagers.NODE_MANAGER, "upsertRootNode");
			Response res = getResponse(createReq, LOGGER);
			if (checkError(res)) {
	    		check.put("healthy", false);
	    		check.put("err", ""); // error code, if any
	            check.put("errmsg", getErrorMessage(res)); // default English error message 				
			} else {
				check.put("healthy", true);
			}

		}catch (Exception e) {
    		check.put("healthy", false);
    		check.put("err", ""); // error code, if any
            check.put("errmsg", e.getMessage()); // default English error message 
        }
		
		return check;
	}
}
