package com.ilimi.taxonomy.mgr;

import com.ilimi.common.dto.Response;

public interface IConceptManager {

    Response getConcepts(String id, String relationType, int depth, String taxonomyId, String[] cfields, boolean isHierarchical);
}
