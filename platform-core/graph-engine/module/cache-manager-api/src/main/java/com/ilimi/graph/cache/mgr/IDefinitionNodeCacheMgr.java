package com.ilimi.graph.cache.mgr;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.dto.BaseValueObjectList;
import com.ilimi.graph.common.dto.StringValue;

public interface IDefinitionNodeCacheMgr {

    void saveDefinitionNode(Request request);

    BaseValueObjectList<StringValue> getRequiredMetadataFields(Request request);

    BaseValueObjectList<StringValue> getIndexedMetadataFields(Request request);

    BaseValueObjectList<StringValue> getNonIndexedMetadataFields(Request request);

    BaseValueObjectList<StringValue> getOutRelationObjectTypes(Request request);

    BaseValueObjectList<StringValue> getInRelationObjectTypes(Request request);
}
