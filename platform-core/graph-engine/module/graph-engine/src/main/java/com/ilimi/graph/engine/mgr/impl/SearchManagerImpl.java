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

import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Filter;
import com.ilimi.graph.dac.model.RelationTraversal;
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
            ERROR(e.getCause(), parent);
        }
    }

    @Override
    public void getAllDefinitions(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        try {
            Graph graph = new Graph(this, graphId);
            SearchCriteria sc = new SearchCriteria();
            sc.setNodeType(SystemNodeTypes.DEFINITION_NODE.name());
            request.put(GraphDACParams.search_criteria.name(), sc);
            graph.getDefinitionNodes(request);
        } catch (Exception e) {
            handleException(e, getSender());
        }
    }

    @Override
    public void getNodeDefinition(Request request) {
        String objectType = (String) request.get(GraphDACParams.object_type.name());
        if (!validateRequired(objectType)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_MISSING_REQ_PARAMS.name(),
                    "GetNodeDefinition: Required parameters are missing...");
        } else {
            String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
            try {
                Graph graph = new Graph(this, graphId);
                SearchCriteria sc = new SearchCriteria();
                sc.setNodeType(SystemNodeTypes.DEFINITION_NODE.name());
                sc.setObjectType(objectType);
                sc.setResultSize(1);
                request.put(GraphDACParams.search_criteria.name(), sc);
                graph.getDefinitionNode(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    @Override
    public void getDataNode(Request request) {
        String nodeId = (String) request.get(GraphDACParams.node_id.name());
        if (!validateRequired(nodeId)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_MISSING_REQ_PARAMS.name(),
                    "GetDataNode: Required parameters are missing...");
        } else {
            String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
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
        List<String> nodeIds = (List<String>) request.get(GraphDACParams.node_ids.name());
        if (!validateRequired(nodeIds)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_MISSING_REQ_PARAMS.name(),
                    "GetDataNode: Required parameters are missing...");
        } else {
            String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
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
        String objectType = (String) request.get(GraphDACParams.object_type.name());
        if (!validateRequired(objectType)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_UNKNOWN_ERROR.name(),
                    "GetNodesByObjectType: Required parameters are missing...");
        } else {
            String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
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
        String nodeId = (String) request.get(GraphDACParams.node_id.name());
        Integer depth = (Integer) request.get(GraphDACParams.depth.name());
        if (!validateRequired(nodeId)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_GET_CHILDREN_MISSING_REQ_PARAMS.name(),
                    "Required parameters are missing...");
        } else {
            String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
            try {
                Graph graph = new Graph(this, graphId);
                Traverser traverser = new Traverser(graphId, nodeId);
                traverser.traversal(Traverser.DEPTH_FIRST_TRAVERSAL).traverseRelation(
                        new RelationTraversal(RelationTypes.HIERARCHY.relationName(), RelationTraversal.DIRECTION_OUT));
                if (null != depth && depth.intValue() > 0)
                    traverser.toDepth(depth);
                request.put(GraphDACParams.traversal_description.name(), traverser);
                graph.traverse(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    @Override
    public void getDescendants(Request request) {
        String nodeId = (String) request.get(GraphDACParams.node_id.name());
        String relation = (String) request.get(GraphDACParams.relation.name());
        Integer depth = (Integer) request.get(GraphDACParams.depth.name());
        if (!validateRequired(nodeId, relation)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_GET_DESCENDANTS_MISSING_REQ_PARAMS.name(),
                    "Required parameters are missing...");
        } else {
            String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
            try {
                Graph graph = new Graph(this, graphId);
                Traverser traverser = new Traverser(graphId, nodeId);
                traverser.traversal(Traverser.DEPTH_FIRST_TRAVERSAL)
                        .traverseRelation(new RelationTraversal(relation, RelationTraversal.DIRECTION_OUT));
                if (null != depth && depth.intValue() > 0)
                    traverser.toDepth(depth);
                request.put(GraphDACParams.traversal_description.name(), traverser);
                graph.traverse(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    @Override
    public void searchNodes(Request request) {
        SearchCriteria sc = (SearchCriteria) request.get(GraphDACParams.search_criteria.name());
        if (!validateRequired(sc)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODES_MISSING_REQ_PARAMS.name(),
                    "Required parameters are missing...");
        } else {
            String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
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
        SearchCriteria sc = (SearchCriteria) request.get(GraphDACParams.search_criteria.name());
        if (!validateRequired(sc)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODES_MISSING_REQ_PARAMS.name(),
                    "Required parameters are missing...");
        } else {
            String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
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
        Traverser traverser = (Traverser) request.get(GraphDACParams.traversal_description.name());
        if (!validateRequired(traverser)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_TRAVERSAL_MISSING_REQ_PARAMS.name(),
                    "Required parameters are missing...");
        } else {
            String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
            try {
                Graph graph = new Graph(this, graphId);
                graph.traverse(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void traverseSubGraph(Request request) {
        String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
        List<String> relations = (List<String>) request.get(GraphDACParams.relations.name());
        Integer depth = (Integer) request.get(GraphDACParams.depth.name());
        if (!validateRequired(startNodeId)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_TRAVERSAL_MISSING_REQ_PARAMS.name(),
                    "Required parameters are missing...");
        } else {
            String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
            try {
                Graph graph = new Graph(this, graphId);
                Traverser traverser = new Traverser(graphId, startNodeId);
                if (null != relations && !relations.isEmpty()) {
                    for (String relation : relations) {
                        traverser.traversal(Traverser.DEPTH_FIRST_TRAVERSAL)
                                .traverseRelation(new RelationTraversal(relation, RelationTraversal.DIRECTION_OUT));
                    }
                }
                if (null != depth && depth.intValue() > 0)
                    traverser.toDepth(depth);
                request.put(GraphDACParams.traversal_description.name(), traverser);
                graph.traverseSubGraph(request);
            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    @Override
    public void getSubGraph(Request request) {
        String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
        String relationType = (String) request.get(GraphDACParams.relation_type.name());
        if (!validateRequired(startNodeId, relationType)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_TRAVERSAL_MISSING_REQ_PARAMS.name(),
                    "Required parameters are missing...");
        } else {
            String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
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
        List<Filter> nodeFilter = (List<Filter>) request.get(GraphDACParams.start_node_filter.name());
        final String relationType = (String) request.get(GraphDACParams.relation_type.name());
        List<Filter> relatedNodeFilter = (List<Filter>) request.get(GraphDACParams.related_node_filter.name());
        List<String> nodeFields = (List<String>) request.get(GraphDACParams.start_node_fields.name());
        List<String> relatedNodeFields = (List<String>) request.get(GraphDACParams.related_node_fields.name());
        Integer direction = (Integer) request.get(GraphDACParams.direction.name());
        if (!validateRequired(nodeFilter, relationType)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_TRAVERSAL_MISSING_REQ_PARAMS.name(),
                    "Required parameters are missing...");
        } else {
            String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
            try {
                int dir = RelationTraversal.DIRECTION_OUT;
                if (validateRequired(direction)) {
                    if (direction.intValue() == RelationTraversal.DIRECTION_IN)
                        dir = RelationTraversal.DIRECTION_IN;
                }
                StringBuilder sb = new StringBuilder();
                Map<String, Object> params = new HashMap<String, Object>();
                int pIndex = 1;
                sb.append("MATCH (n:NODE) WHERE ( ");
                pIndex = getFilterQuery(nodeFilter, sb, params, "n", pIndex);
                sb.append(" ) WITH n OPTIONAL MATCH (n)");
                if (dir == RelationTraversal.DIRECTION_IN) {
                    sb.append("<-[:").append(relationType).append("]-(r) ");
                } else {
                    sb.append("-[:").append(relationType).append("]->(r) ");
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
                            List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
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
                                            attrMap.put(relationType, relList);
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
                                            List<Map<String, Object>> relList = (List<Map<String, Object>>) attrMap
                                                    .get(relationType);
                                            relList.add(relMap);
                                        }
                                    }
                                }
                                for (Map<String, Object> val : nodeMap.values()) {
                                    resultList.add(val);
                                }
                            }
                            OK(GraphDACParams.results.name(), resultList, parent);
                        }
                    }
                }, getContext().dispatcher());

            } catch (Exception e) {
                handleException(e, getSender());
            }
        }
    }

    private int getFilterQuery(List<Filter> filters, StringBuilder sb, Map<String, Object> params, String index,
            int pIndex) {
        for (int i = 0; i < filters.size(); i++) {
            Filter filter = filters.get(i);
            sb.append(" ").append(index).append(".").append(filter.getProperty()).append(" = {").append(pIndex)
                    .append("} ");
            params.put("" + pIndex, filter.getValue());
            pIndex += 1;
            if (i < filters.size() - 1) {
                sb.append("AND ");
            }
        }
        return pIndex;
    }

    private void getReturnFieldsQuery(List<String> fields, StringBuilder sb, String index) {
        for (int i = 0; i < fields.size(); i++) {
            String field = fields.get(i);
            sb.append(index).append(".").append(field);
            if (i < fields.size() - 1) {
                sb.append(", ");
            }
        }
    }

    @Override
	public void getNodesByProperty(Request request) {
		Property prop = (Property) request.get(GraphDACParams.metadata.name());
		if (!validateRequired(prop)) {
			throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_MISSING_REQ_PARAMS.name(),
					"GetDataNode: Required parameters are missing...");
		} else {
			String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
			try {
				Graph graph = new Graph(this, graphId);
				graph.getNodesByProperty(request);
			} catch (Exception e) {
				handleException(e, getSender());
			}
		}
	}
}
