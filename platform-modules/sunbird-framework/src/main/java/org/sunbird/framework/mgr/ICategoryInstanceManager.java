package org.sunbird.framework.mgr;

import java.util.Map;

import org.sunbird.common.dto.Response;

/**
 * The Interface ICategoryInstanceManager is the Contract for the operations that can be
 * perform on categoryInstance Node in the Graph. Including all Low (CRUD) Level and
 * high-level operations.
 * 
 * @author rashmi
 * 
 * @see CategoryInstanceManager
 *
 */
public interface ICategoryInstanceManager {

	Response createCategoryInstance(String identifier, Map<String,Object> request);

	Response readCategoryInstance(String identifier, String categoryInstanceId);

	Response searchCategoryInstance(String identifier, Map<String, Object> map);
	
	Response retireCategoryInstance(String identifier, String categoryInstanceId) throws Exception;

	Response updateCategoryInstance(String identifier, String categoryInstanceId, Map<String, Object> map) throws Exception;

	boolean validateScopeId(String identifier);
}
