package com.ilimi.graph.dac.mgr;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;

public interface IGraphDACGraphMgr {

	Response createGraph(Request request);
    
	Response createUniqueConstraint(Request request);
    
	Response createIndex(Request request);

	Response deleteGraph(Request request);

	Response addRelation(Request request);

	Response deleteRelation(Request request);

	Response updateRelation(Request request);

	Response removeRelationMetadata(Request request);

	Response importGraph(Request request);

	Response createCollection(Request request);

	Response deleteCollection(Request request);

	Response addOutgoingRelations(Request request);

	Response addIncomingRelations(Request request);
    
	Response deleteIncomingRelations(Request request);
    
	Response deleteOutgoingRelations(Request request);
    
	Response bulkUpdateNodes(Request request);
}
