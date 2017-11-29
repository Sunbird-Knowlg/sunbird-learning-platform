package com.ilimi.taxonomy.mgr;

import java.util.Map;

import com.ilimi.common.dto.Response;

public interface ICategoryManager {
	
	Response createCategory(Map<String,Object> request);

	Response readCategory(String categoryId);

	Response updateCategory(String categoryId, Map<String, Object> map);

	Response searchCategory(Map<String, Object> map);
	
	Response retireCategory(String categoryId);
	
}
