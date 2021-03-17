package org.sunbird.graph.model.collection;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.ClientException;
import org.sunbird.graph.common.mgr.BaseGraphManager;
import org.sunbird.graph.enums.CollectionTypes;
import org.sunbird.graph.exception.GraphEngineErrorCodes;
import org.sunbird.graph.model.ICollection;

public class CollectionHandler {
    
    public static ICollection getCollection(BaseGraphManager manager, String graphId, String collectionId, String collectionType, Map<String, Object> metadata) {

        if (StringUtils.isNotBlank(collectionType) && CollectionTypes.isValidCollectionType(collectionType)) {
            if (StringUtils.equals(CollectionTypes.SEQUENCE.name(), collectionType)) {
                return new Sequence(manager, graphId, collectionId);
            } else if (StringUtils.equals(CollectionTypes.SET.name(), collectionType)) {
                return new Set(manager, graphId, collectionId, metadata);
            }
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_INVALID_COLLECTION_TYPE.name(), "Invalid Collection Type: "
                    + collectionType);
        }
        throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_INVALID_COLLECTION_TYPE.name(), "Invalid Collection Type: "
                + collectionType);
    }
    
    public static ICollection getCollection(BaseGraphManager manager, String graphId, String collectionId, String collectionType) {
        return getCollection(manager, graphId, collectionId, collectionType, null);
    }
}
