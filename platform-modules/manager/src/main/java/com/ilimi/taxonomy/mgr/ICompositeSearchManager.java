package com.ilimi.taxonomy.mgr;

import com.ilimi.common.dto.Response;

public interface ICompositeSearchManager {

	Response sync(String graphId, String objectType, Integer start, Integer total);
	
	Response syncObject(String graphId, String identifier);
}
