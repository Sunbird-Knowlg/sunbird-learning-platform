package com.ilimi.framework.mgr;

import java.util.Map;

import com.ilimi.common.dto.Response;

public interface ICategoryManager {
	
	Response createCategory(Map<String,Object> request);

	Response readCategory(String channelId);

	Response updateCategory(String channelId, Map<String, Object> map);

	Response searchCategory(Map<String, Object> map);
	
	Response retireCategory(String categoryId);
	
}