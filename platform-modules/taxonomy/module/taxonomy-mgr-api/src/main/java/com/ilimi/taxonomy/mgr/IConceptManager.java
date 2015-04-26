package com.ilimi.taxonomy.mgr;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.Response;

public interface IConceptManager {

    Response findAll(String taxonomyId);

    Response find(String id, String taxonomyId);

    Response create(String taxonomyId, Request request);

    Response update(String id, String taxonomyId, Request request);

    Response delete(String id, String taxonomyId);

    Response deleteRelation(String startConceptId, String relationType, String endConceptId, String taxonomyId);
    
    Response getConcepts(String id, String relationType, int depth, String taxonomyId);
    
    Response getConceptsGames(String taxonomyId);
}
