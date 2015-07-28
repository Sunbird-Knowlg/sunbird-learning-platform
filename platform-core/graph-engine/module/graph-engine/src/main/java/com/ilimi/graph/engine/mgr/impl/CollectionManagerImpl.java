package com.ilimi.graph.engine.mgr.impl;

import java.lang.reflect.Method;
import java.util.List;

import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.dispatch.OnComplete;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.engine.mgr.ICollectionManager;
import com.ilimi.graph.engine.router.GraphEngineActorPoolMgr;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.exception.GraphEngineErrorCodes;
import com.ilimi.graph.model.ICollection;
import com.ilimi.graph.model.collection.CollectionHandler;
import com.ilimi.graph.model.collection.Sequence;
import com.ilimi.graph.model.collection.Set;
import com.ilimi.graph.model.collection.Tag;
import com.ilimi.graph.model.node.DataNode;

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
        SearchCriteria criteria = (SearchCriteria) request.get(GraphDACParams.criteria.name());
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
                set = new Set(this, graphId, null, setObjectType, node.getMetadata(), criteria);
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
        SearchCriteria criteria = (SearchCriteria) request.get(GraphDACParams.criteria.name());
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
                set = new Set(this, graphId, node.getIdentifier(), setObjectType, node.getMetadata(), criteria);
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

    @SuppressWarnings("unchecked")
    @Override
    public void createTag(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String tagName = (String) request.get(GraphDACParams.tag_name.name());
        String attributeName = (String) request.get(GraphDACParams.attribute_name.name());
        List<String> memberIds = (List<String>) request.get(GraphDACParams.members.name());
        try {
            ICollection tagNode = new Tag(this, graphId, tagName, attributeName, memberIds);
            tagNode.create(request);
        } catch (Exception e) {
            handleException(e, getSender());
        }
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

    @Override
    public void addTag(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        try {
            ICollection tag = new Tag(this, graphId, null);
            tag.addMember(request);
        } catch (Exception e) {
            handleException(e, getSender());
        }
    }

    @SuppressWarnings("unchecked")
    public void addTags(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String nodeId = (String) request.get(GraphDACParams.node_id.name());
        List<String> tags = (List<String>) request.get(GraphDACParams.tags.name());
        if (!validateRequired(nodeId, tags)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_ADD_TAGS_MISSING_REQ_PARAMS.name(),
                    "Required parameters are missing...");
        } else {
            try {
                DataNode node = new DataNode(this, graphId, nodeId, null, null);
                Future<List<String>> tagsFuture = node.addTags(request, tags);
                tagsFuture.onComplete(new OnComplete<List<String>>() {
                    @Override
                    public void onComplete(Throwable arg0, List<String> arg1) throws Throwable {
                        if (null != arg0) {
                            handleException(arg0, getSender());
                        } else {
                            if (null != arg1 && !arg1.isEmpty()) {
                                ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_TAGS_UNKNOWN_ERROR.name(), "Error adding tags",
                                        ResponseCode.CLIENT_ERROR, GraphDACParams.messages.name(), arg1, getSender());
                            } else {
                                OK(getSender());
                            }
                        }
                    }
                }, getContext().dispatcher());
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

}
