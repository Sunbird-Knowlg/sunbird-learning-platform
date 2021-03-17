package org.sunbird.framework.mgr;

import java.util.Map;

import org.sunbird.common.dto.Response;

/**
 * The Interface ICategoryManager is the Contract for the operations that can be
 * perform on category Node in the Graph. Including all Low (CRUD) Level and
 * high-level operations.
 * 
 * @author rashmi
 * 
 * @see CategoryManager
 *
 */
public interface ICategoryManager {
	
	Response createCategory(Map<String,Object> request);

	Response readCategory(String channelId);

	Response updateCategory(String channelId, Map<String, Object> map);

	Response searchCategory(Map<String, Object> map);
	
	Response retireCategory(String categoryId);
	
}