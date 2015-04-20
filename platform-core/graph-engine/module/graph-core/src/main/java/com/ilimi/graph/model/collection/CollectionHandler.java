package com.ilimi.graph.model.collection;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.graph.common.exception.ClientException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.enums.CollectionTypes;
import com.ilimi.graph.exception.GraphEngineErrorCodes;
import com.ilimi.graph.model.ICollection;

public class CollectionHandler {

    public static ICollection getCollection(BaseGraphManager manager, String graphId, String collectionId, String collectionType) {

        if (StringUtils.isNotBlank(collectionType) && CollectionTypes.isValidCollectionType(collectionType)) {
            if (StringUtils.equals(CollectionTypes.SEQUENCE.name(), collectionType)) {
                return new Sequence(manager, graphId, collectionId);
            } else if (StringUtils.equals(CollectionTypes.SET.name(), collectionType)) {
                return new Set(manager, graphId, collectionId);
            } else if (StringUtils.equals(CollectionTypes.TAG.name(), collectionType)) {
                return new Tag(manager, graphId, collectionId);
            }
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_INVALID_COLLECTION_TYPE.name(), "Invalid Collection Type: "
                    + collectionType);
        }
        throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_INVALID_COLLECTION_TYPE.name(), "Invalid Collection Type: "
                + collectionType);
    }
}
