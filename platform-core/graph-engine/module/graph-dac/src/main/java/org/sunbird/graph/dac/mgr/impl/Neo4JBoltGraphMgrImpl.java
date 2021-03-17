package org.sunbird.graph.dac.mgr.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.graph.common.enums.GraphHeaderParams;
import org.sunbird.graph.common.exception.GraphEngineErrorCodes;
import org.sunbird.graph.common.mgr.BaseDACMgr;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.exception.GraphDACErrorCodes;
import org.sunbird.graph.dac.mgr.IGraphDACGraphMgr;
import org.sunbird.graph.importer.ImportData;
import org.sunbird.graph.service.operation.Neo4JBoltGraphOperations;

public class Neo4JBoltGraphMgrImpl extends BaseDACMgr implements IGraphDACGraphMgr {

	@Override
	public Response createGraph(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		if (StringUtils.isBlank(graphId)) {
			throw new ClientException(GraphEngineErrorCodes.ERR_INVALID_GRAPH_ID.name(), "Graph Id cannot be blank");
		} else {
			try {
				return OK(GraphDACParams.graph_id.name(), graphId);
			} catch (Exception e) {
				return ERROR(e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response createUniqueConstraint(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		List<String> indexProperties = (List<String>) request.get(GraphDACParams.property_keys.name());
		if (!validateRequired(indexProperties)) {
			throw new ClientException(GraphDACErrorCodes.ERR_CREATE_UNIQUE_CONSTRAINT_MISSING_REQ_PARAMS.name(),
					"Required Parameters are missing");
		} else {
			try {
				Neo4JBoltGraphOperations.createGraphUniqueContraint(graphId, indexProperties, request);
				return OK(GraphDACParams.graph_id.name(), graphId);
			} catch (Exception e) {
				return ERROR(e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response createIndex(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		List<String> indexProperties = (List<String>) request.get(GraphDACParams.property_keys.name());
		if (!validateRequired(indexProperties)) {
			throw new ClientException(GraphDACErrorCodes.ERR_CREATE_INDEX_MISSING_REQ_PARAMS.name(),
					"Required Parameters are missing");
		} else {
			try {
				Neo4JBoltGraphOperations.createIndex(graphId, indexProperties, request);
				return OK(GraphDACParams.graph_id.name(), graphId);
			} catch (Exception e) {
				return ERROR(e);
			}
		}
	}

	@Override
	public Response deleteGraph(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		if (StringUtils.isBlank(graphId)) {
			throw new ClientException(GraphEngineErrorCodes.ERR_INVALID_GRAPH_ID.name(), "Graph Id cannot be blank");
		} else {
			try {
				Neo4JBoltGraphOperations.deleteGraph(graphId, request);
				return OK(GraphDACParams.graph_id.name(), graphId);
			} catch (Exception e) {
				return ERROR(e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Response addOutgoingRelations(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
		String relationType = (String) request.get(GraphDACParams.relation_type.name());
		List<String> endNodeIds = (List<String>) request.get(GraphDACParams.end_node_id.name());
		if (!validateRequired(startNodeId, relationType, endNodeIds)) {
			throw new ClientException(GraphDACErrorCodes.ERR_CREATE_RELATION_MISSING_REQ_PARAMS.name(),
					"Required Parameters are missing");
		} else {
			try {
				Neo4JBoltGraphOperations.createOutgoingRelations(graphId, startNodeId, endNodeIds, relationType,
						request);
				return OK();
			} catch (Exception e) {
				return ERROR(e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Response addIncomingRelations(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		List<String> startNodeIds = (List<String>) request.get(GraphDACParams.start_node_id.name());
		String relationType = (String) request.get(GraphDACParams.relation_type.name());
		String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
		if (!validateRequired(startNodeIds, relationType, endNodeId)) {
			throw new ClientException(GraphDACErrorCodes.ERR_CREATE_RELATION_MISSING_REQ_PARAMS.name(),
					"Required Parameters are missing");
		} else {
			try {
				Neo4JBoltGraphOperations.createIncomingRelations(graphId, startNodeIds, endNodeId, relationType,
						request);
				return OK();
			} catch (Exception e) {
				return ERROR(e);
			}
		}
	}

	@Override
	public Response addRelation(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
		String relationType = (String) request.get(GraphDACParams.relation_type.name());
		String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
		if (!validateRequired(startNodeId, relationType, endNodeId)) {
			throw new ClientException(GraphDACErrorCodes.ERR_CREATE_RELATION_MISSING_REQ_PARAMS.name(),
					"Required Parameters are missing");
		} else {
			try {
				Neo4JBoltGraphOperations.createRelation(graphId, startNodeId, endNodeId, relationType, request);
				return OK(GraphDACParams.graph_id.name(), graphId);
			} catch (Exception e) {
				return ERROR(e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response deleteIncomingRelations(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		List<String> startNodeIds = (List<String>) request.get(GraphDACParams.start_node_id.name());
		String relationType = (String) request.get(GraphDACParams.relation_type.name());
		String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
		if (!validateRequired(startNodeIds, relationType, endNodeId)) {
			throw new ClientException(GraphDACErrorCodes.ERR_DELETE_RELATION_MISSING_REQ_PARAMS.name(),
					"Required Parameters are missing");
		} else {
			try {
				Neo4JBoltGraphOperations.deleteIncomingRelations(graphId, startNodeIds, endNodeId, relationType,
						request);
				return OK();
			} catch (Exception e) {
				return ERROR(e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response deleteOutgoingRelations(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
		String relationType = (String) request.get(GraphDACParams.relation_type.name());
		List<String> endNodeIds = (List<String>) request.get(GraphDACParams.end_node_id.name());
		if (!validateRequired(startNodeId, relationType, endNodeIds)) {
			throw new ClientException(GraphDACErrorCodes.ERR_DELETE_RELATION_MISSING_REQ_PARAMS.name(),
					"Required Parameters are missing");
		} else {
			try {
				Neo4JBoltGraphOperations.deleteOutgoingRelations(graphId, startNodeId, endNodeIds, relationType,
						request);
				return OK();
			} catch (Exception e) {
				return ERROR(e);
			}
		}
	}

	@Override
	public Response deleteRelation(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
		String relationType = (String) request.get(GraphDACParams.relation_type.name());
		String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
		if (!validateRequired(startNodeId, relationType, endNodeId)) {
			throw new ClientException(GraphDACErrorCodes.ERR_DELETE_RELATION_MISSING_REQ_PARAMS.name(),
					"Required Variables are missing");
		} else {
			try {
				Neo4JBoltGraphOperations.deleteRelation(graphId, startNodeId, endNodeId, relationType, request);
				return OK();
			} catch (Exception e) {
				return ERROR(e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response updateRelation(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
		String relationType = (String) request.get(GraphDACParams.relation_type.name());
		String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
		Map<String, Object> metadata = (Map<String, Object>) request.get(GraphDACParams.metadata.name());
		if (!validateRequired(startNodeId, relationType, endNodeId, metadata)) {
			throw new ClientException(GraphDACErrorCodes.ERR_UPDATE_RELATION_MISSING_REQ_PARAMS.name(),
					"Required Variables are missing");
		} else if (null != metadata && metadata.size() > 0) {
			try {
				Neo4JBoltGraphOperations.updateRelation(graphId, startNodeId, endNodeId, relationType, request);
				return OK();
			} catch (Exception e) {
				return ERROR(e);
			}
		} else {
			return OK();
		}
	}

	@Override
	public Response removeRelationMetadata(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
		String relationType = (String) request.get(GraphDACParams.relation_type.name());
		String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
		String key = (String) request.get(GraphDACParams.property_key.name());
		if (!validateRequired(startNodeId, relationType, endNodeId, key)) {
			throw new ClientException(GraphDACErrorCodes.ERR_UPDATE_RELATION_MISSING_REQ_PARAMS.name(),
					"Required Variables are missing");
		} else if (StringUtils.isNotBlank(key)) {
			try {
				Neo4JBoltGraphOperations.removeRelationMetadataByKey(graphId, startNodeId, endNodeId, relationType, key,
						request);
				return OK();
			} catch (Exception e) {
				return ERROR(e);
			}
		} else {
			return OK();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response createCollection(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String collectionId = (String) request.get(GraphDACParams.collection_id.name());
		org.sunbird.graph.dac.model.Node collection = (org.sunbird.graph.dac.model.Node) request
				.get(GraphDACParams.node.name());
		String relationType = (String) request.get(GraphDACParams.relation_type.name());
		List<String> members = (List<String>) request.get(GraphDACParams.members.name());
		String indexProperty = (String) request.get(GraphDACParams.index.name());
		if (!validateRequired(collectionId, members)) {
			throw new ClientException(GraphDACErrorCodes.ERR_CREATE_COLLECTION_MISSING_REQ_PARAMS.name(),
					"Required Variables are missing");
		} else {
			try {
				Neo4JBoltGraphOperations.createCollection(graphId, collectionId, collection, relationType, members,
						indexProperty, request);
				return OK();
			} catch (Exception e) {
				return ERROR(e);
			}
		}
	}

	public Response deleteCollection(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String collectionId = (String) request.get(GraphDACParams.collection_id.name());
		if (!validateRequired(collectionId)) {
			throw new ClientException(GraphDACErrorCodes.ERR_DELETE_COLLECTION_MISSING_REQ_PARAMS.name(),
					"Required Variables are missing");
		} else {
			try {
				Neo4JBoltGraphOperations.deleteCollection(graphId, collectionId, request);
				return OK();
			} catch (Exception e) {
				return ERROR(e);
			}
		}
	}

	@Override
	public Response importGraph(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String taskId = request.get(GraphDACParams.task_id.name()) == null ? null
				: (String) request.get(GraphDACParams.task_id.name());
		ImportData input = (ImportData) request.get(GraphDACParams.import_input_object.name());
		if (StringUtils.isBlank(graphId)) {
			throw new ClientException(GraphEngineErrorCodes.ERR_INVALID_GRAPH_ID.name(), "Graph Id cannot be blank");
		} else {
			try {
				Map<String, List<String>> messages = Neo4JBoltGraphOperations.importGraph(graphId, taskId, input,
						request);
				return OK(GraphDACParams.messages.name(), messages);
			} catch (Exception e) {
				return ERROR(e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response bulkUpdateNodes(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		List<Map<String, Object>> newNodes = (List<Map<String, Object>>) request.get(GraphDACParams.newNodes.name());
		List<Map<String, Object>> modifiedNodes = (List<Map<String, Object>>) request.get(GraphDACParams.modifiedNodes.name());
		List<Map<String, Object>> addOutRelations = (List<Map<String, Object>>) request.get(GraphDACParams.addedOutRelations.name());
		List<Map<String, Object>> removeOutRelations = (List<Map<String, Object>>) request.get(GraphDACParams.removedOutRelations.name());
		List<Map<String, Object>> addInRelations = (List<Map<String, Object>>) request.get(GraphDACParams.addedInRelations.name());
		List<Map<String, Object>> removeInRelations = (List<Map<String, Object>>) request.get(GraphDACParams.removedInRelations.name());
		if (StringUtils.isBlank(graphId)) {
			throw new ClientException(GraphEngineErrorCodes.ERR_INVALID_GRAPH_ID.name(), "Graph Id cannot be blank");
		} else {
			try {
				Neo4JBoltGraphOperations.bulkUpdateNodes(graphId, newNodes, modifiedNodes, addOutRelations,
						removeOutRelations, addInRelations, removeInRelations);
				return OK();
			} catch (Exception e) {
				return ERROR(e);
			}
		}
	}

}
