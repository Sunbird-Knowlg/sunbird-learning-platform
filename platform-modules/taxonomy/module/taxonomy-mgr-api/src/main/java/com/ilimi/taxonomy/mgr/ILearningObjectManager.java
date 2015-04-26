package com.ilimi.taxonomy.mgr;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.Response;

public interface ILearningObjectManager {

    Response findAll(String taxonomyId, String objectType, Integer offset, Integer limit, String[] gfields);

    Response find(String id, String taxonomyId, String[] gfields);

    Response create(String taxonomyId, Request request);

    Response update(String id, String taxonomyId, Request request);

    Response delete(String id, String taxonomyId);

    Response deleteRelation(String startLobId, String relationType, String endLobId, String taxonomyId);
}
