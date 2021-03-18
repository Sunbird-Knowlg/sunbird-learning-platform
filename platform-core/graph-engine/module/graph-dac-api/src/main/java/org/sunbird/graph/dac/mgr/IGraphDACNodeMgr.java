package org.sunbird.graph.dac.mgr;

import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;

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
