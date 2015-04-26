package com.ilimi.graph.engine.mgr.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.dispatch.OnComplete;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.dto.BaseValueObjectList;
import com.ilimi.graph.common.dto.BaseValueObjectMap;
import com.ilimi.graph.common.dto.Identifier;
import com.ilimi.graph.common.dto.StringValue;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.exception.ClientException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.FilterDTO;
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

    @Override
    @SuppressWarnings("unchecked")
    public void searchRelations(Request request) {
        BaseValueObjectList<FilterDTO> nodeFilter = (BaseValueObjectList<FilterDTO>) request.get(GraphDACParams.START_NODE_FILTER.name());
        final StringValue relationType = (StringValue) request.get(GraphDACParams.RELATION_TYPE.name());
        BaseValueObjectList<FilterDTO> relatedNodeFilter = (BaseValueObjectList<FilterDTO>) request.get(GraphDACParams.RELATED_NODE_FILTER
                .name());
        BaseValueObjectList<StringValue> nodeFields = (BaseValueObjectList<StringValue>) request.get(GraphDACParams.START_NODE_FIELDS
                .name());
        BaseValueObjectList<StringValue> relatedNodeFields = (BaseValueObjectList<StringValue>) request
                .get(GraphDACParams.RELATED_NODE_FIELDS.name());
        Identifier direction = (Identifier) request.get(GraphDACParams.DIRECTION.name());
        if (!validateRequired(nodeFilter, relationType)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_TRAVERSAL.name(), "Required parameters are missing...");
        } else {
            String graphId = (String) request.getContext().get(GraphHeaderParams.GRAPH_ID.name());
            try {
                int dir = RelationTraversal.DIRECTION_OUT;
                if (validateRequired(direction)) {
                    if (direction.getId().intValue() == RelationTraversal.DIRECTION_IN)
                        dir = RelationTraversal.DIRECTION_IN;
                }
                StringBuilder sb = new StringBuilder();
                Map<String, Object> params = new HashMap<String, Object>();
                int pIndex = 1;
                sb.append("MATCH (n:NODE) WHERE ( ");
                pIndex = getFilterQuery(nodeFilter, sb, params, "n", pIndex);
                sb.append(" ) WITH n OPTIONAL MATCH (n)");
                if (dir == RelationTraversal.DIRECTION_IN) {
                    sb.append("<-[:").append(relationType.getId()).append("]-(r) ");
                } else {
                    sb.append("-[:").append(relationType.getId()).append("]->(r) ");
                }
                if (validateRequired(relatedNodeFilter)) {
                    sb.append("WHERE ( ");
                    pIndex = getFilterQuery(relatedNodeFilter, sb, params, "r", pIndex);
                    sb.append(" ) ");
                }
                sb.append(" RETURN n.").append(SystemProperties.IL_UNIQUE_ID.name()).append(", r.")
                        .append(SystemProperties.IL_UNIQUE_ID.name());
                if (validateRequired(nodeFields)) {
                    sb.append(", ");
                    getReturnFieldsQuery(nodeFields, sb, "n");
                }
                if (validateRequired(relatedNodeFields)) {
                    sb.append(", ");
                    getReturnFieldsQuery(relatedNodeFields, sb, "r");
                }
                Graph graph = new Graph(this, graphId);
                final ActorRef parent = getSender();
                Future<List<Map<String, Object>>> future = graph.executeQuery(request, sb.toString(), params);
                future.onComplete(new OnComplete<List<Map<String, Object>>>() {
                    @Override
                    public void onComplete(Throwable arg0, List<Map<String, Object>> arg1) throws Throwable {
                        if (null != arg0) {
                            ERROR(arg0, parent);
                        } else {
                            List<BaseValueObjectMap<Object>> resultList = new ArrayList<BaseValueObjectMap<Object>>();
                            if (null != arg1 && !arg1.isEmpty()) {
                                Map<String, Map<String, Object>> nodeMap = new HashMap<String, Map<String, Object>>();
                                for (Map<String, Object> map : arg1) {
                                    if (null != map && !map.isEmpty()) {
                                        String nodeId = (String) map.get("n." + SystemProperties.IL_UNIQUE_ID.name());
                                        Map<String, Object> attrMap = nodeMap.get(nodeId);
                                        if (null == attrMap) {
                                            attrMap = new HashMap<String, Object>();
                                            attrMap.put("id", nodeId);
                                            List<Map<String, Object>> relList = new ArrayList<Map<String, Object>>();
                                            attrMap.put(relationType.getId(), relList);
                                            nodeMap.put(nodeId, attrMap);
                                        }
                                        Map<String, Object> relMap = new HashMap<String, Object>();
                                        for (Entry<String, Object> entry : map.entrySet()) {
                                            if (StringUtils.startsWith(entry.getKey(), "n.")) {
                                                attrMap.put(entry.getKey().substring(2), entry.getValue());
                                            } else if (StringUtils.startsWith(entry.getKey(), "r.")) {
                                                if (null != entry.getValue()) {
                                                    relMap.put(entry.getKey().substring(2), entry.getValue());
                                                }
                                            }
                                        }
                                        attrMap.remove(SystemProperties.IL_UNIQUE_ID.name());
                                        if (!relMap.isEmpty()) {
                                            if (relMap.containsKey(SystemProperties.IL_UNIQUE_ID.name()))
                                                relMap.remove(SystemProperties.IL_UNIQUE_ID.name());
                                            List<Map<String, Object>> relList = (List<Map<String, Object>>) attrMap.get(relationType
                                                    .getId());
                                            relList.add(relMap);
                                        }
                                    }
                                }
                                for (Map<String, Object> val : nodeMap.values()) {
                                    resultList.add(new BaseValueObjectMap<Object>(val));
                                }
                            }
                            OK(GraphDACParams.RESULTS.name(), new BaseValueObjectList<BaseValueObjectMap<Object>>(resultList), parent);
                        }
                    }
                }, getContext().dispatcher());

            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    private int getFilterQuery(BaseValueObjectList<FilterDTO> filters, StringBuilder sb, Map<String, Object> params, String index,
            int pIndex) {
        for (int i = 0; i < filters.getValueObjectList().size(); i++) {
            FilterDTO filter = filters.getValueObjectList().get(i);
            sb.append(" ").append(index).append(".").append(filter.getProperty()).append(" = {").append(pIndex).append("} ");
            params.put("" + pIndex, filter.getValue());
            pIndex += 1;
            if (i < filters.getValueObjectList().size() - 1) {
                sb.append("AND ");
            }
        }
        return pIndex;
    }

    private void getReturnFieldsQuery(BaseValueObjectList<StringValue> fields, StringBuilder sb, String index) {
        for (int i = 0; i < fields.getValueObjectList().size(); i++) {
            String field = fields.getValueObjectList().get(i).getId();
            sb.append(index).append(".").append(field);
            if (i < fields.getValueObjectList().size() - 1) {
                sb.append(", ");
            }
        }
    }

}
