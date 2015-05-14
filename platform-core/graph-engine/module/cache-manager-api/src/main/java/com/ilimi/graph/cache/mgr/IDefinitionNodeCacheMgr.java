package com.ilimi.graph.cache.mgr;

import java.util.List;

import com.ilimi.common.dto.Request;

public interface IDefinitionNodeCacheMgr {

    void saveDefinitionNode(Request request);

    List<String> getRequiredMetadataFields(Request request);

    List<String> getIndexedMetadataFields(Request request);

    List<String> getNonIndexedMetadataFields(Request request);

    List<String> getOutRelationObjectTypes(Request request);

    List<String> getInRelationObjectTypes(Request request);
}
