package org.sunbird.graph.dac.mgr.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.sunbird.common.Platform;
import org.sunbird.common.dto.Property;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.graph.common.enums.GraphHeaderParams;
import org.sunbird.graph.common.mgr.BaseDACMgr;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.exception.GraphDACErrorCodes;
import org.sunbird.graph.dac.mgr.IGraphDACSearchMgr;
import org.sunbird.graph.dac.model.Filter;
import org.sunbird.graph.dac.model.Graph;
import org.sunbird.graph.dac.model.MetadataCriterion;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.graph.dac.model.SearchConditions;
import org.sunbird.graph.dac.model.SearchCriteria;
import org.sunbird.graph.dac.model.SubGraph;
import org.sunbird.graph.dac.model.Traverser;
import org.sunbird.graph.service.operation.Neo4JBoltSearchOperations;

public class Neo4JBoltSearchMgrImpl extends BaseDACMgr implements IGraphDACSearchMgr {
	
    @Override
	public Response getNodeById(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        Long nodeId = (Long) request.get(GraphDACParams.node_id.name());
        Boolean getTags = (Boolean) request.get(GraphDACParams.get_tags.name());
        if (!validateRequired(nodeId))
            throw new ClientException(GraphDACErrorCodes.ERR_GET_NODE_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        try {
			Node node = Neo4JBoltSearchOperations.getNodeById(graphId, nodeId, getTags, request);
			return OK(GraphDACParams.node.name(), node);
        } catch (Exception e) {
			return ERROR(e);
        }
    }

    @Override
	public Response getNodeByUniqueId(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String nodeId = (String) request.get(GraphDACParams.node_id.name());
        Boolean getTags = (Boolean) request.get(GraphDACParams.get_tags.name());
        if (!validateRequired(nodeId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_GET_NODE_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            try {
				Node node = Neo4JBoltSearchOperations.getNodeByUniqueId(graphId, nodeId, getTags, request);
				return OK(GraphDACParams.node.name(), node);
            } catch (Exception e) {
				return ERROR(e);
            }
        }
    }
    
	@SuppressWarnings("unchecked")
	public Response executeQueryForProps(Request request) {
    		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
    		String query = (String) request.get(GraphDACParams.query.name());
    		List<String> propKeys = (List<String>) request.get(GraphDACParams.property_keys.name());
    		if (!validateRequired(graphId, query, propKeys)) {
    			throw new ClientException(GraphDACErrorCodes.ERR_GRAPH_QUERY_NOT_FOUND.name(), "Query is missing");
    		} else {
    			try {
				List<Map<String, Object>> nodes = Neo4JBoltSearchOperations.executeQueryForProps(graphId, query,
						propKeys);
				return OK(GraphDACParams.properties.name(), nodes);
            } catch (Exception e) {
				return ERROR(e);
            }
    		}
    }

    @Override
	public Response getNodesByProperty(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        Property property = (Property) request.get(GraphDACParams.metadata.name());
        Boolean getTags = (Boolean) request.get(GraphDACParams.get_tags.name());
        if (!validateRequired(property)) {
            throw new ClientException(GraphDACErrorCodes.ERR_GET_NODE_LIST_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            try {
				List<Node> nodeList = Neo4JBoltSearchOperations.getNodesByProperty(graphId, property, getTags, request);
				return OK(GraphDACParams.node_list.name(), nodeList);
            } catch (Exception e) {
				return ERROR(e);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
	@Override
	public Response getNodesByUniqueIds(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		List<String> nodeIds = (List<String>) request.get(GraphDACParams.node_ids.name());
		if (!validateRequired(nodeIds)) {
			throw new ClientException(GraphDACErrorCodes.ERR_GET_NODE_LIST_MISSING_REQ_PARAMS.name(),
					"Required parameters are missing");
		} else {
			SearchCriteria searchCriteria = new SearchCriteria();
			MetadataCriterion mc = null;
			if (nodeIds.size() == 1)
				mc = MetadataCriterion
						.create(Arrays.asList(new Filter("identifier", SearchConditions.OP_EQUAL, nodeIds.get(0))));
			else
				mc = MetadataCriterion.create(Arrays.asList(new Filter("identifier", SearchConditions.OP_IN, nodeIds)));
			
			searchCriteria.addMetadata(mc);
			searchCriteria.setCountQuery(false);
            try {
				List<Node> nodes = Neo4JBoltSearchOperations.getNodeByUniqueIds(graphId, searchCriteria, request);
				return OK(GraphDACParams.node_list.name(), nodes);
            } catch (Exception e) {
				return ERROR(e);
            }
		}
	}

    @Override
	public Response getNodeProperty(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String nodeId = (String) request.get(GraphDACParams.node_id.name());
        String key = (String) request.get(GraphDACParams.property_key.name());
        if (!validateRequired(nodeId, key)) {
            throw new ClientException(GraphDACErrorCodes.ERR_GET_NODE_PROPERTY_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            try {
				Property property = Neo4JBoltSearchOperations.getNodeProperty(graphId, nodeId, key, request);
				return OK(GraphDACParams.property.name(), property);
            } catch (Exception e) {
				return ERROR(e);
            }
        }
    }

    @Override
	public Response getAllNodes(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        try {
			List<Node> nodes = Neo4JBoltSearchOperations.getAllNodes(graphId, request);
			return OK(GraphDACParams.node_list.name(), nodes);
        } catch (Exception e) {
			return ERROR(e);
        }
    }

    @Override
	public Response getAllRelations(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        try {
			List<Relation> relations = Neo4JBoltSearchOperations.getAllRelations(graphId, request);
			return OK(GraphDACParams.relations.name(), relations);
        } catch (Exception e) {
			return ERROR(e);
        }
    }

    @Override
	public Response getRelationProperty(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
        String relationType = (String) request.get(GraphDACParams.relation_type.name());
        String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
        String key = (String) request.get(GraphDACParams.property_key.name());
        if (!validateRequired(startNodeId, relationType, endNodeId, key)) {
            throw new ClientException(GraphDACErrorCodes.ERR_GET_RELATIONS_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            try {
				Property property = Neo4JBoltSearchOperations.getRelationProperty(graphId, startNodeId, relationType,
						endNodeId, key, request);
				return OK(GraphDACParams.property.name(), property);
            } catch (Exception e) {
				return ERROR(e);
            }
        }
    }

    @Override
	public Response getRelation(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
        String relationType = (String) request.get(GraphDACParams.relation_type.name());
        String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
        if (!validateRequired(startNodeId, relationType, endNodeId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_GET_RELATIONS_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            try {
				Relation relation = Neo4JBoltSearchOperations.getRelation(graphId, startNodeId, relationType, endNodeId,
						request);
				return OK(GraphDACParams.relation.name(), relation);
            } catch (Exception e) {
				return ERROR(e);
            }
        }
    }

    @Override
	public Response checkCyclicLoop(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
        String relationType = (String) request.get(GraphDACParams.relation_type.name());
        String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
        if (!validateRequired(startNodeId, relationType, endNodeId)) {
            throw new ClientException(GraphDACErrorCodes.ERR_CHECK_LOOP_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            try {
				Map<String, Object> voMap = Neo4JBoltSearchOperations.checkCyclicLoop(graphId, startNodeId,
						relationType, endNodeId, request);
				return OK(voMap);
            } catch (Exception e) {
				return ERROR(e);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
	public Response executeQuery(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String query = (String) request.get(GraphDACParams.query.name());
        Map<String, Object> paramMap = (Map<String, Object>) request.get(GraphDACParams.params.name());
        if (!validateRequired(query)) {
            throw new ClientException(GraphDACErrorCodes.ERR_SEARCH_NODES_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            try {
				List<Map<String, Object>> resultList = Neo4JBoltSearchOperations.executeQuery(graphId, query, paramMap,
						request);
				return OK(GraphDACParams.results.name(), resultList);
            } catch (Exception e) {
				return ERROR(e);
            }
        }
    }

    @Override
	public Response searchNodes(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        SearchCriteria searchCriteria = (SearchCriteria) request.get(GraphDACParams.search_criteria.name());
        Boolean getTags = (Boolean) request.get(GraphDACParams.get_tags.name());
        if (!validateRequired(searchCriteria)) {
            throw new ClientException(GraphDACErrorCodes.ERR_SEARCH_NODES_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            try {
				List<Node> nodes = Neo4JBoltSearchOperations.searchNodes(graphId, searchCriteria, getTags, request);
                boolean isrRelativePathEnabled = Platform.config.hasPath("cloudstorage.metadata.replace_absolute_path")?Platform.config.getBoolean("cloudstorage.metadata.replace_absolute_path"):false;
                if(isrRelativePathEnabled) updateAbsolutePath(nodes);
				return OK(GraphDACParams.node_list.name(), nodes);
            } catch (Exception e) {
				return ERROR(e);
            }
        }
    }

    @Override
	public Response getNodesCount(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        SearchCriteria searchCriteria = (SearchCriteria) request.get(GraphDACParams.search_criteria.name());
        if (!validateRequired(searchCriteria)) {
            throw new ClientException(GraphDACErrorCodes.ERR_SEARCH_NODES_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            try {
				Long count = Neo4JBoltSearchOperations.getNodesCount(graphId, searchCriteria, request);
				return OK(GraphDACParams.count.name(), count);
            } catch (Exception e) {
				return ERROR(e);
            }
        }
    }

    @Override
	public Response traverse(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        Traverser traverser = (Traverser) request.get(GraphDACParams.traversal_description.name());
        if (!validateRequired(traverser)) {
            throw new ClientException(GraphDACErrorCodes.ERR_TRAVERSAL_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            try {
				SubGraph subGraph = Neo4JBoltSearchOperations.traverse(graphId, traverser, request);
				return OK(GraphDACParams.sub_graph.name(), subGraph);
            } catch (Exception e) {
				return ERROR(e);
            }
        }
    }
    
    @Override
	public Response traverseSubGraph(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        Traverser traverser = (Traverser) request.get(GraphDACParams.traversal_description.name());
        if (!validateRequired(traverser)) {
            throw new ClientException(GraphDACErrorCodes.ERR_TRAVERSAL_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            try {
				Graph subGraph = Neo4JBoltSearchOperations.traverseSubGraph(graphId, traverser, request);
				return OK(GraphDACParams.sub_graph.name(), subGraph);
            } catch (Exception e) {
				return ERROR(e);
            }
        }
    }

    @Override
	public Response getSubGraph(Request request) {
        String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
        String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
        String relationType = (String) request.get(GraphDACParams.relation_type.name());
        Integer depth = (Integer) request.get(GraphDACParams.depth.name());
        if (!validateRequired(startNodeId, relationType)) {
            throw new ClientException(GraphDACErrorCodes.ERR_TRAVERSAL_MISSING_REQ_PARAMS.name(), "Required parameters are missing");
        } else {
            try {
				Graph subGraph = Neo4JBoltSearchOperations.getSubGraph(graphId, startNodeId, relationType, depth,
						request);
				return OK(GraphDACParams.sub_graph.name(), subGraph);
            } catch (Exception e) {
				return ERROR(e);
            }
        }
    }

    private Node updateAbsolutePath(Node node) {
        Map<String, Object> metadata = updateAbsolutePath(node.getMetadata());
        node.setMetadata(metadata);
        return node;
    }

    private java.util.List<Node> updateAbsolutePath(java.util.List<Node> nodes) {
        for(Node node: nodes) {
            updateAbsolutePath(node);
        }
        return nodes;
    }

    private Map<String, Object> updateAbsolutePath(Map<String, Object> data) {
        String relativePathPrefix = Platform.config.getString("cloudstorage.relative_path_prefix");
        List<String> cspMeta = Platform.config.getStringList("cloudstorage.metadata.list");
        String absolutePath = Platform.config.getString("cloudstorage.read_base_path") + java.io.File.separator + Platform.config.getString("cloud_storage_container");
        if (data !=null && !data.isEmpty()) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if(cspMeta.contains(entry.getKey())) {
                    if(entry.getValue() instanceof String) {
                        data.replace(entry.getKey(), ((String) entry.getValue()).replaceAll(relativePathPrefix, absolutePath));
                    }
                }
            }
        }
        return data;
    }


}
