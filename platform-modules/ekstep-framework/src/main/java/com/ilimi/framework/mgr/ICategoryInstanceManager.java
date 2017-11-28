package com.ilimi.framework.mgr;

import java.util.Map;

import com.ilimi.common.dto.Response;

public interface ICategoryInstanceManager {

	Response createCategoryInstance(String identifier, Map<String,Object> request);

	Response readCategoryInstance(String categoryInstanceId);

	Response updateCategoryInstance(String categoryInstanceId, Map<String, Object> map);

	Response searchCategoryInstance(Map<String, Object> map);
	
	Response retireCategoryInstance(String categoryInstanceId);
}
