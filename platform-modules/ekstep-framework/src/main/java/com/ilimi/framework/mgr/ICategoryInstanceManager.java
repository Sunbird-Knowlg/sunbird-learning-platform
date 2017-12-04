package com.ilimi.framework.mgr;

import java.util.Map;

import com.ilimi.common.dto.Response;

public interface ICategoryInstanceManager {

	Response createCategoryInstance(String identifier, Map<String,Object> request);

	Response readCategoryInstance(String identifier, String categoryInstanceId);

	Response searchCategoryInstance(String identifier, Map<String, Object> map);
	
	Response retireCategoryInstance(String identifier, String categoryInstanceId);

	Response updateCategoryInstance(String identifier, String categoryInstanceId, Map<String, Object> map);

	boolean validateScopeId(String identifier);
}
