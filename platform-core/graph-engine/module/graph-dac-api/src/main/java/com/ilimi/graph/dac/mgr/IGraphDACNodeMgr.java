package com.ilimi.graph.dac.mgr;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;

public interface IGraphDACNodeMgr {

	Response upsertNode(Request request);
    
	Response addNode(Request request);
    
	Response updateNode(Request request);

	Response importNodes(Request request);

	Response updatePropertyValue(Request request);

	Response updatePropertyValues(Request request);

	Response removePropertyValue(Request request);

	Response removePropertyValues(Request request);

	Response deleteNode(Request request);
    
	Response upsertRootNode(Request request);
}
