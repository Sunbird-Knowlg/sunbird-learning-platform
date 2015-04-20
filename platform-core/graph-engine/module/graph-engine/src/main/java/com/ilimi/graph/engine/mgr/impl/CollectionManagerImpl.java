package com.ilimi.graph.engine.mgr.impl;

import java.lang.reflect.Method;
import java.util.List;

import akka.actor.ActorRef;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.dto.BaseValueObjectList;
import com.ilimi.graph.common.dto.StringValue;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.exception.ClientException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.engine.mgr.ICollectionManager;
import com.ilimi.graph.engine.router.GraphEngineActorPoolMgr;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.exception.GraphEngineErrorCodes;
import com.ilimi.graph.model.ICollection;
import com.ilimi.graph.model.collection.CollectionHandler;
import com.ilimi.graph.model.collection.Sequence;
import com.ilimi.graph.model.collection.Set;
import com.ilimi.graph.model.collection.SetCriteria;
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
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
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
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        SetCriteria criteria = (SetCriteria) request.get(GraphDACParams.CRITERIA.name());
        BaseValueObjectList<StringValue> memberIds = (BaseValueObjectList<StringValue>) request.get(GraphDACParams.MEMBERS.name());
        try {
            if (validateRequired(memberIds)) {
                ICollection set = new Set(this, graphId, null, memberIds.getValueObjectList());
                set.create(request);
            } else {
                ICollection set = new Set(this, graphId, null, criteria);
                set.create(request);
            }
        } catch (Exception e) {
            handleException(e, getSender());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void createTag(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue tagName = (StringValue) request.get(GraphDACParams.TAG_NAME.name());
        StringValue attributeName = (StringValue) request.get(GraphDACParams.ATTRIBUTE_NAME.name());
        BaseValueObjectList<StringValue> memberIds = (BaseValueObjectList<StringValue>) request.get(GraphDACParams.MEMBERS.name());
        try {
            String tag = null;
            if (validateRequired(tagName))
                tag = tagName.getId();
            String attribute = null;
            if (validateRequired(attributeName))
                attribute = attributeName.getId();
            List<StringValue> members = null;
            if (validateRequired(memberIds))
                members = memberIds.getValueObjectList();
            ICollection tagNode = new Tag(this, graphId, tag, attribute, members);
            tagNode.create(request);
        } catch (Exception e) {
            handleException(e, getSender());
        }
    }

    @Override
    public void addMember(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue collectionId = (StringValue) request.get(GraphDACParams.COLLECTION_ID.name());
        StringValue collectionType = (StringValue) request.get(GraphDACParams.COLLECTION_TYPE.name());
        StringValue memberId = (StringValue) request.get(GraphDACParams.MEMBER_ID.name());
        if (!validateRequired(collectionId, collectionType, memberId)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_COLLECTION_ADD_MEMBER.name(), "Required parameters are missing...");
        } else {
            try {
                ICollection coll = CollectionHandler.getCollection(this, graphId, collectionId.getId(), collectionType.getId());
                coll.addMember(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    @Override
    public void removeMember(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue collectionId = (StringValue) request.get(GraphDACParams.COLLECTION_ID.name());
        StringValue collectionType = (StringValue) request.get(GraphDACParams.COLLECTION_TYPE.name());
        StringValue memberId = (StringValue) request.get(GraphDACParams.MEMBER_ID.name());
        if (!validateRequired(collectionId, collectionType, memberId)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_COLLECTION_REMOVE_MEMBER.name(), "Required parameters are missing...");
        } else {
            try {
                ICollection coll = CollectionHandler.getCollection(this, graphId, collectionId.getId(), collectionType.getId());
                coll.removeMember(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    @Override
    public void dropCollection(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue collectionId = (StringValue) request.get(GraphDACParams.COLLECTION_ID.name());
        StringValue collectionType = (StringValue) request.get(GraphDACParams.COLLECTION_TYPE.name());
        if (!validateRequired(collectionId, collectionType)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_DROP_COLLECTION.name(), "Required parameters are missing...");
        } else {
            try {
                ICollection coll = CollectionHandler.getCollection(this, graphId, collectionId.getId(), collectionType.getId());
                coll.delete(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    @Override
    public void getCollectionMembers(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue collectionId = (StringValue) request.get(GraphDACParams.COLLECTION_ID.name());
        StringValue collectionType = (StringValue) request.get(GraphDACParams.COLLECTION_TYPE.name());
        if (!validateRequired(collectionId, collectionType)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_COLLECTION_GET_MEMBERS.name(), "Required parameters are missing...");
        } else {
            try {
                ICollection coll = CollectionHandler.getCollection(this, graphId, collectionId.getId(), collectionType.getId());
                coll.getMembers(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    @Override
    public void addTag(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        try {
            ICollection tag = new Tag(this, graphId, null);
            tag.addMember(request);
        } catch (Exception e) {
            handleException(e, getSender());
        }
    }

    @SuppressWarnings("unchecked")
    public void addTags(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        StringValue nodeId = (StringValue) request.get(GraphDACParams.NODE_ID.name());
        BaseValueObjectList<StringValue> tags = (BaseValueObjectList<StringValue>) request.get(GraphDACParams.TAGS.name());
        if (!validateRequired(nodeId, tags)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_ADD_TAGS.name(), "Required parameters are missing...");
        } else {
            try {
                DataNode node = new DataNode(this, graphId, nodeId.getId(), null, null);
                node.addTags(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

}
