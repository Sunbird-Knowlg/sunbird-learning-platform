package com.ilimi.graph.engine.mgr.impl;

import java.lang.reflect.Method;

import akka.actor.ActorRef;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.dto.BaseValueObjectList;
import com.ilimi.graph.common.dto.Identifier;
import com.ilimi.graph.common.dto.StringValue;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.exception.ClientException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.RelationTraversal;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.Traverser;
import com.ilimi.graph.engine.mgr.ISearchManager;
import com.ilimi.graph.engine.router.GraphEngineActorPoolMgr;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.exception.GraphEngineErrorCodes;
import com.ilimi.graph.model.Graph;

public class SearchManagerImpl extends BaseGraphManager implements ISearchManager {

    protected void invokeMethod(Request request, ActorRef parent) {
        String methodName = request.getOperation();
        try {
            Method method = GraphEngineActorPoolMgr.getMethod(GraphEngineManagers.SEARCH_MANAGER, methodName);
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
    public void getAllDefinitions(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
        try {
            Graph graph = new Graph(this, graphId);
            SearchCriteria sc = new SearchCriteria();
            sc.add(SearchConditions.eq(SystemProperties.IL_SYS_NODE_TYPE.name(), SystemNodeTypes.DEFINITION_NODE.name()));
            request.put(GraphDACParams.SEARCH_CRITERIA.name(), sc);
            graph.getDefinitionNodes(request);
        } catch (Exception e) {
            handleException(e, getSender());
        }
    }

    @Override
    public void getNodeDefinition(Request request) {
        StringValue objectType = (StringValue) request.get(GraphDACParams.OBJECT_TYPE.name());
        if (!validateRequired(objectType)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_ERROR.name(),
                    "GetNodeDefinition: Required parameters are missing...");
        } else {
            String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
            try {
                Graph graph = new Graph(this, graphId);
                SearchCriteria sc = new SearchCriteria();
                sc.add(SearchConditions.eq(SystemProperties.IL_SYS_NODE_TYPE.name(), SystemNodeTypes.DEFINITION_NODE.name())).add(
                        SearchConditions.eq(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), objectType.getId()));
                sc.limit(1);
                request.put(GraphDACParams.SEARCH_CRITERIA.name(), sc);
                graph.getDefinitionNode(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    @Override
    public void getDataNode(Request request) {
        StringValue nodeId = (StringValue) request.get(GraphDACParams.NODE_ID.name());
        if (!validateRequired(nodeId)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_ERROR.name(),
                    "GetDataNode: Required parameters are missing...");
        } else {
            String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
            try {
                Graph graph = new Graph(this, graphId);
                graph.getDataNode(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void getDataNodes(Request request) {
        BaseValueObjectList<StringValue> nodeIds = (BaseValueObjectList<StringValue>) request.get(GraphDACParams.NODE_IDS.name());
        if (!validateRequired(nodeIds)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_ERROR.name(),
                    "GetDataNode: Required parameters are missing...");
        } else {
            String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
            try {
                Graph graph = new Graph(this, graphId);
                graph.getDataNodes(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    @Override
    public void getNodesByObjectType(Request request) {
        StringValue objectType = (StringValue) request.get(GraphDACParams.OBJECT_TYPE.name());
        if (!validateRequired(objectType)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_ERROR.name(),
                    "GetNodesByObjectType: Required parameters are missing...");
        } else {
            String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
            try {
                Graph graph = new Graph(this, graphId);
                graph.getNodesByObjectType(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    @Override
    public void getChildren(Request request) {
        StringValue nodeId = (StringValue) request.get(GraphDACParams.NODE_ID.name());
        Identifier depth = (Identifier) request.get(GraphDACParams.DEPTH.name());
        if (!validateRequired(nodeId)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_GET_CHILDREN.name(), "Required parameters are missing...");
        } else {
            String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
            try {
                Graph graph = new Graph(this, graphId);
                Traverser traverser = new Traverser(graphId, nodeId.getId());
                traverser.traversal(Traverser.DEPTH_FIRST_TRAVERSAL).traverseRelation(
                        new RelationTraversal(RelationTypes.HIERARCHY.relationName(), RelationTraversal.DIRECTION_OUT));
                if (null != depth && null != depth.getId() && depth.getId().intValue() > 0)
                    traverser.toDepth(depth.getId());
                request.put(GraphDACParams.TRAVERSAL_DESCRIPTION.name(), traverser);
                graph.traverse(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    @Override
    public void getDescendants(Request request) {
        StringValue nodeId = (StringValue) request.get(GraphDACParams.NODE_ID.name());
        StringValue relation = (StringValue) request.get(GraphDACParams.RELATION.name());
        Identifier depth = (Identifier) request.get(GraphDACParams.DEPTH.name());
        if (!validateRequired(nodeId, relation)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_GET_DESCENDANTS.name(), "Required parameters are missing...");
        } else {
            String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
            try {
                Graph graph = new Graph(this, graphId);
                Traverser traverser = new Traverser(graphId, nodeId.getId());
                traverser.traversal(Traverser.DEPTH_FIRST_TRAVERSAL).traverseRelation(
                        new RelationTraversal(relation.getId(), RelationTraversal.DIRECTION_OUT));
                if (null != depth && null != depth.getId() && depth.getId().intValue() > 0)
                    traverser.toDepth(depth.getId());
                request.put(GraphDACParams.TRAVERSAL_DESCRIPTION.name(), traverser);
                graph.traverse(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }

    }

    @Override
    public void searchNodes(Request request) {
        SearchCriteria sc = (SearchCriteria) request.get(GraphDACParams.SEARCH_CRITERIA.name());
        if (!validateRequired(sc)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODES.name(), "Required parameters are missing...");
        } else {
            String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
            try {
                Graph graph = new Graph(this, graphId);
                graph.searchNodes(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    @Override
    public void getNodesCount(Request request) {
        SearchCriteria sc = (SearchCriteria) request.get(GraphDACParams.SEARCH_CRITERIA.name());
        if (!validateRequired(sc)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODES.name(), "Required parameters are missing...");
        } else {
            String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
            try {
                Graph graph = new Graph(this, graphId);
                graph.getNodesCount(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    @Override
    public void traverse(Request request) {
        Traverser traverser = (Traverser) request.get(GraphDACParams.TRAVERSAL_DESCRIPTION.name());
        if (!validateRequired(traverser)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_TRAVERSAL.name(), "Required parameters are missing...");
        } else {
            String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
            try {
                Graph graph = new Graph(this, graphId);
                graph.traverse(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    @Override
    public void getSubGraph(Request request) {
        StringValue startNodeId = (StringValue) request.get(GraphDACParams.START_NODE_ID.name());
        StringValue relationType = (StringValue) request.get(GraphDACParams.RELATION_TYPE.name());
        if (!validateRequired(startNodeId, relationType)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_TRAVERSAL.name(), "Required parameters are missing...");
        } else {
            String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
            try {
                Graph graph = new Graph(this, graphId);
                graph.getSubGraph(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

}
