package org.ekstep.framework.mgr;

import java.util.Map;

import org.ekstep.common.dto.Response;

/**
 * 
 * @author mahesh
 *
 */

public interface IFrameworkTypeManager {

	public Response create(Map<String, Object> request) throws Exception;
	
	public Response update(String id, Map<String, Object> map) throws Exception;
	
	public Response list(Map<String, Object> map, boolean updateCache) throws Exception;
	
	public Map<String, Object> getAll();

}
