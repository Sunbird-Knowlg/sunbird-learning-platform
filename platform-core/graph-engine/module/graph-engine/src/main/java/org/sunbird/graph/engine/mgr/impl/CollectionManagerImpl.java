package org.sunbird.graph.engine.mgr.impl;

import java.lang.reflect.Method;
import java.util.List;

import org.sunbird.common.dto.Request;
import org.sunbird.common.exception.ClientException;
import org.sunbird.graph.common.enums.GraphHeaderParams;
import org.sunbird.graph.common.mgr.BaseGraphManager;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.engine.mgr.ICollectionManager;
import org.sunbird.graph.engine.router.GraphEngineActorPoolMgr;
import org.sunbird.graph.engine.router.GraphEngineManagers;
import org.sunbird.graph.exception.GraphEngineErrorCodes;
import org.sunbird.graph.model.ICollection;
import org.sunbird.graph.model.collection.CollectionHandler;
import org.sunbird.graph.model.collection.Sequence;
import org.sunbird.graph.model.collection.Set;

import akka.actor.ActorRef;

public class CollectionManagerImpl extends BaseGraphManager implements ICollectionManager {

    protected void invokeMethod(Request request, ActorRef parent) {
        String methodName = request.getOperation();
        try {
            Method method = GraphEngineActorPoolMgr.getMethod(GraphEngineManagers.COLLECTION_MANAGER, methodName);
            if (null == method) {
                throw new ClientException("ERR_GRAPH_INVALID_OPERATION", "Operation '" + methodName + "' not found");
            } else {
                method.invoke(this, request);
            }
        } catch (Exception e) {
            ERROR(e, parent);
        }
    }

    @Override
    public void createSequence(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        try {
            ICollection sequence = new Sequence(this, graphId, null);
            sequence.create(request);
        } catch (Exception e) {
            handleException(e, getSender());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void createSet(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        List<String> memberIds = (List<String>) request.get(GraphDACParams.members.name());
        String setObjectType = (String) request.get(GraphDACParams.object_type.name());
        String memberObjectType = (String) request.get(GraphDACParams.member_type.name());
        Node node = (Node) request.get(GraphDACParams.node.name());
        try {
            if (!validateRequired(node, setObjectType)) 
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_CREATE_SET_MISSING_REQ_PARAMS.name(),
                        "Required parameters are missing...");
            Set set = null;
            if (validateRequired(memberIds)) {
                set = new Set(this, graphId, null, setObjectType, memberObjectType, node.getMetadata(), memberIds);
            } else {
				set = new Set(this, graphId, null, setObjectType, node.getMetadata());
            }
            set.setInRelations(node.getInRelations());
            set.setOutRelations(node.getOutRelations());
            set.create(request);
        } catch (Exception e) {
            handleException(e, getSender());
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void updateSet(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        List<String> memberIds = (List<String>) request.get(GraphDACParams.members.name());
        String setObjectType = (String) request.get(GraphDACParams.object_type.name());
        String memberObjectType = (String) request.get(GraphDACParams.member_type.name());
        Node node = (Node) request.get(GraphDACParams.node.name());
        try {
            if (!validateRequired(node, setObjectType)) 
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_UPDATE_SET_MISSING_REQ_PARAMS.name(),
                        "Required parameters are missing...");
            Set set = null;
            if (validateRequired(memberIds)) {
                set = new Set(this, graphId, node.getIdentifier(), setObjectType, memberObjectType, node.getMetadata(), memberIds);
            } else {
				set = new Set(this, graphId, node.getIdentifier(), setObjectType, node.getMetadata());
            }
            set.setInRelations(node.getInRelations());
            set.setOutRelations(node.getOutRelations());
            set.updateMembership(request);
        } catch (Exception e) {
            handleException(e, getSender());
        }
    }
    
    @Override
    public void getSet(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String setId = (String) request.get(GraphDACParams.collection_id.name());
        if(!validateRequired(setId))
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_COLLECTION_ID_MISSING.name(), "Collection Id is missing...");
        Set set = new Set(this, graphId, setId, null);
        set.getNode(request);
    }
    
    @Override
    public void getSetCardinality(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String setId = (String) request.get(GraphDACParams.collection_id.name());
        if(!validateRequired(setId))
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_COLLECTION_ID_MISSING.name(), "Collection Id is missing...");
        Set set = new Set(this, graphId, setId, null);
        set.getCardinality(request);
    }

    @Override
    public void addMember(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String collectionId = (String) request.get(GraphDACParams.collection_id.name());
        String collectionType = (String) request.get(GraphDACParams.collection_type.name());
        String memberId = (String) request.get(GraphDACParams.member_id.name());
        if (!validateRequired(collectionId, collectionType, memberId)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_COLLECTION_ADD_MEMBER_MISSING_REQ_PARAMS.name(),
                    "Required parameters are missing...");
        } else {
            try {
                ICollection coll = CollectionHandler.getCollection(this, graphId, collectionId, collectionType);
                coll.addMember(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public void addMembers(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String collectionId = (String) request.get(GraphDACParams.collection_id.name());
        String collectionType = (String) request.get(GraphDACParams.collection_type.name());
        List<String> members= (List<String>) request.get(GraphDACParams.members.name());
        if (!validateRequired(collectionId, collectionType, members)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_COLLECTION_ADD_MEMBER_MISSING_REQ_PARAMS.name(),
                    "Required parameters are missing...");
        } else {
            try {
                ICollection coll = CollectionHandler.getCollection(this, graphId, collectionId, collectionType);
                coll.addMembers(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    @Override
    public void removeMember(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String collectionId = (String) request.get(GraphDACParams.collection_id.name());
        String collectionType = (String) request.get(GraphDACParams.collection_type.name());
        String memberId = (String) request.get(GraphDACParams.member_id.name());
        if (!validateRequired(collectionId, collectionType, memberId)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_COLLECTION_REMOVE_MEMBER_MISSING_REQ_PARAMS.name(),
                    "Required parameters are missing...");
        } else {
            try {
                ICollection coll = CollectionHandler.getCollection(this, graphId, collectionId, collectionType);
                coll.removeMember(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }
    
    @SuppressWarnings("unchecked")
	public void removeMembers(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String collectionId = (String) request.get(GraphDACParams.collection_id.name());
        String collectionType = (String) request.get(GraphDACParams.collection_type.name());
        List<String> members = (List<String>) request.get(GraphDACParams.members.name());
        if (!validateRequired(collectionId, collectionType, members)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_COLLECTION_REMOVE_MEMBER_MISSING_REQ_PARAMS.name(),
                    "Required parameters are missing...");
        } else {
            try {
                ICollection coll = CollectionHandler.getCollection(this, graphId, collectionId, collectionType);
                coll.removeMembers(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    @Override
    public void dropCollection(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String collectionId = (String) request.get(GraphDACParams.collection_id.name());
        String collectionType = (String) request.get(GraphDACParams.collection_type.name());
        if (!validateRequired(collectionId, collectionType)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_DROP_COLLECTION_MISSING_REQ_PARAMS.name(),
                    "Required parameters are missing...");
        } else {
            try {
                ICollection coll = CollectionHandler.getCollection(this, graphId, collectionId, collectionType);
                coll.delete(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    @Override
    public void getCollectionMembers(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String collectionId = (String) request.get(GraphDACParams.collection_id.name());
        String collectionType = (String) request.get(GraphDACParams.collection_type.name());
        if (!validateRequired(collectionId, collectionType)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_COLLECTION_GET_MEMBERS_MISSING_REQ_PARAMS.name(),
                    "Required parameters are missing...");
        } else {
            try {
                ICollection coll = CollectionHandler.getCollection(this, graphId, collectionId, collectionType);
                coll.getMembers(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }
}
