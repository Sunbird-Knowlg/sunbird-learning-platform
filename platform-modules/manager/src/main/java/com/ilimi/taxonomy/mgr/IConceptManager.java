package com.ilimi.taxonomy.mgr;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;

public interface IConceptManager {

    Response findAll(String taxonomyId, String[] cfields);

    Response find(String id, String taxonomyId, String[] cfields);

    Response create(String taxonomyId, Request request);

    Response update(String id, String taxonomyId, Request request);

    Response delete(String id, String taxonomyId);

    Response deleteRelation(String startConceptId, String relationType, String endConceptId, String taxonomyId);
    
    Response getConcepts(String id, String relationType, int depth, String taxonomyId, String[] cfields, boolean isHierarchical);
    
    Response getConceptsGames(String taxonomyId, String[] cfields, String[] gfields);
}
