package com.ilimi.taxonomy.mgr;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;

public interface ICompositeSearchSyncManager {

	Response sync(String graphId, String objectType, Request request);
}
