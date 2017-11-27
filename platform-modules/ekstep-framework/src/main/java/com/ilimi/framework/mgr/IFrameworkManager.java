package com.ilimi.framework.mgr;

import java.util.Map;

import com.ilimi.common.dto.Response;

/**
 * The Interface IFrameworkManager is the Contract for the operations that can be
 * perform on Framework Node in the Graph. Including all Low (CRUD) Level and
 * high-level operations.
 * 
 * @author gauraw
 * 
 * @see FrameworkManagerImpl
 *
 */
public interface IFrameworkManager {

	Response createFramework(Map<String, Object> map) throws Exception;
	
	Response readFramework(String graphId, String frameworkId) throws Exception;
	
	Response updateFramework(String frameworkId, Map<String, Object> map) throws Exception;
	
	Response listFramework(Map<String, Object> map) throws Exception;
	
	Response retireFramework(Map<String, Object> map) throws Exception;
}