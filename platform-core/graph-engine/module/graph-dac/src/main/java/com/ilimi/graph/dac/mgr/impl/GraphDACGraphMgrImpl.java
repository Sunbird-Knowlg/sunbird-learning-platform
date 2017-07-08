package com.ilimi.graph.dac.mgr.impl;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.graph.service.IGraphDatabaseService;
import org.ekstep.graph.service.common.DACConfigurationConstants;
import org.ekstep.graph.service.factory.GraphServiceFactory;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.exception.GraphEngineErrorCodes;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.exception.GraphDACErrorCodes;
import com.ilimi.graph.dac.mgr.IGraphDACGraphMgr;
import com.ilimi.graph.dac.router.GraphDACActorPoolMgr;
import com.ilimi.graph.dac.router.GraphDACManagers;
import com.ilimi.graph.dac.util.Neo4jGraphFactory;
import com.ilimi.graph.importer.ImportData;

import akka.actor.ActorRef;

public class GraphDACGraphMgrImpl extends BaseGraphManager implements IGraphDACGraphMgr {

	private static ILogger LOGGER = PlatformLogManager.getLogger();

	static IGraphDatabaseService service;
	static {
		String databasePolicy = DACConfigurationConstants.ACTIVE_DATABASE_POLICY;

		LOGGER.log("Active Database Policy Id:" , databasePolicy);

		if (StringUtils.isBlank(databasePolicy))
			databasePolicy = DACConfigurationConstants.DEFAULT_DATABASE_POLICY;

		LOGGER.log("Creating Database Connection Using Policy Id:" , databasePolicy);

		service = GraphServiceFactory.getDatabaseService(databasePolicy);
	}

	protected void invokeMethod(Request request, ActorRef parent) {
		String methodName = request.getOperation();
		try {
			Method method = GraphDACActorPoolMgr.getMethod(GraphDACManagers.DAC_GRAPH_MANAGER, methodName);
			if (null == method) {
				throw new ClientException(GraphDACErrorCodes.ERR_GRAPH_INVALID_OPERATION.name(),
						"Operation '" + methodName + "' not found");
			} else {
				method.invoke(this, request);
			}
		} catch (Exception e) {
			ERROR(e, parent);
		}
	}

	@Override
	public void createGraph(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		if (StringUtils.isBlank(graphId)) {
			throw new ClientException(GraphEngineErrorCodes.ERR_INVALID_GRAPH_ID.name(), "Graph Id cannot be blank");
		} else if (Neo4jGraphFactory.graphExists(graphId)) {
			throw new ClientException(GraphDACErrorCodes.ERR_GRAPH_ALREADY_EXISTS.name(),
					"Graph '" + graphId + "' already exists");
		} else {
			try {
				Neo4jGraphFactory.createGraph(graphId);
				Neo4jGraphFactory.getGraphDb(graphId, request);
				OK(GraphDACParams.graph_id.name(), graphId, getSender());
			} catch (Exception e) {
				ERROR(e, getSender());
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void createUniqueConstraint(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		List<String> indexProperties = (List<String>) request.get(GraphDACParams.property_keys.name());
		if (!validateRequired(indexProperties)) {
			throw new ClientException(GraphDACErrorCodes.ERR_CREATE_UNIQUE_CONSTRAINT_MISSING_REQ_PARAMS.name(),
					"Required Parameters are missing");
		} else {
			try {
				service.createGraphUniqueContraint(graphId, indexProperties, request);
				OK(GraphDACParams.graph_id.name(), graphId, getSender());
			} catch (Exception e) {
				ERROR(e, getSender());
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void createIndex(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		List<String> indexProperties = (List<String>) request.get(GraphDACParams.property_keys.name());
		if (!validateRequired(indexProperties)) {
			throw new ClientException(GraphDACErrorCodes.ERR_CREATE_INDEX_MISSING_REQ_PARAMS.name(),
					"Required Parameters are missing");
		} else {
			try {
				service.createIndex(graphId, indexProperties, request);
				OK(GraphDACParams.graph_id.name(), graphId, getSender());
			} catch (Exception e) {
				ERROR(e, getSender());
			}
		}
	}

	@Override
	public void deleteGraph(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		if (StringUtils.isBlank(graphId)) {
			throw new ClientException(GraphEngineErrorCodes.ERR_INVALID_GRAPH_ID.name(), "Graph Id cannot be blank");
		} else if (!Neo4jGraphFactory.graphExists(graphId)) {
			throw new ClientException(GraphDACErrorCodes.ERR_GRAPH_NOT_FOUND.name(),
					"Graph '" + graphId + "' not found to delete.");
		} else {
			try {
				service.deleteGraph(graphId, request);
				OK(GraphDACParams.graph_id.name(), graphId, getSender());
			} catch (Exception e) {
				ERROR(e, getSender());
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void addOutgoingRelations(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
		String relationType = (String) request.get(GraphDACParams.relation_type.name());
		List<String> endNodeIds = (List<String>) request.get(GraphDACParams.end_node_id.name());
		if (!validateRequired(startNodeId, relationType, endNodeIds)) {
			throw new ClientException(GraphDACErrorCodes.ERR_CREATE_RELATION_MISSING_REQ_PARAMS.name(),
					"Required Parameters are missing");
		} else {
			try {
				service.createOutgoingRelations(graphId, startNodeId, endNodeIds, relationType, request);
				OK(getSender());
			} catch (Exception e) {
				ERROR(e, getSender());
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void addIncomingRelations(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		List<String> startNodeIds = (List<String>) request.get(GraphDACParams.start_node_id.name());
		String relationType = (String) request.get(GraphDACParams.relation_type.name());
		String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
		if (!validateRequired(startNodeIds, relationType, endNodeId)) {
			throw new ClientException(GraphDACErrorCodes.ERR_CREATE_RELATION_MISSING_REQ_PARAMS.name(),
					"Required Parameters are missing");
		} else {
			try {
				service.createIncomingRelations(graphId, startNodeIds, endNodeId, relationType, request);
				OK(getSender());
			} catch (Exception e) {
				ERROR(e, getSender());
			}
		}
	}

	@Override
	public void addRelation(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
		String relationType = (String) request.get(GraphDACParams.relation_type.name());
		String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
		if (!validateRequired(startNodeId, relationType, endNodeId)) {
			throw new ClientException(GraphDACErrorCodes.ERR_CREATE_RELATION_MISSING_REQ_PARAMS.name(),
					"Required Parameters are missing");
		} else {
			try {
				service.createRelation(graphId, startNodeId, endNodeId, relationType, request);
				OK(GraphDACParams.graph_id.name(), graphId, getSender());
			} catch (Exception e) {
				ERROR(e, getSender());
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deleteIncomingRelations(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		List<String> startNodeIds = (List<String>) request.get(GraphDACParams.start_node_id.name());
		String relationType = (String) request.get(GraphDACParams.relation_type.name());
		String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
		if (!validateRequired(startNodeIds, relationType, endNodeId)) {
			throw new ClientException(GraphDACErrorCodes.ERR_DELETE_RELATION_MISSING_REQ_PARAMS.name(),
					"Required Parameters are missing");
		} else {
			try {
				service.deleteIncomingRelations(graphId, startNodeIds, endNodeId, relationType, request);
				OK(getSender());
			} catch (Exception e) {
				ERROR(e, getSender());
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deleteOutgoingRelations(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
		String relationType = (String) request.get(GraphDACParams.relation_type.name());
		List<String> endNodeIds = (List<String>) request.get(GraphDACParams.end_node_id.name());
		if (!validateRequired(startNodeId, relationType, endNodeIds)) {
			throw new ClientException(GraphDACErrorCodes.ERR_DELETE_RELATION_MISSING_REQ_PARAMS.name(),
					"Required Parameters are missing");
		} else {
			try {
				service.deleteOutgoingRelations(graphId, startNodeId, endNodeIds, relationType, request);
				OK(getSender());
			} catch (Exception e) {
				ERROR(e, getSender());
			}
		}
	}

	@Override
	public void deleteRelation(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String startNodeId = (String) request.get(GraphDACParams.start_node_id.name());
		String relationType = (String) request.get(GraphDACParams.relation_type.name());
		String endNodeId = (String) request.get(GraphDACParams.end_node_id.name());
		if (!validateRequired(startNodeId, relationType, endNodeId)) {
			throw new ClientException(GraphDACErrorCodes.ERR_DELETE_RELATION_MISSING_REQ_PARAMS.name(),
					"Required Variables are missing");
		} else {
			try {
				service.deleteRelation(graphId, startNodeId, endNodeId, relationType, request);
				OK(getSender());
			} catch (Exception e) {
				ERROR(e, getSender());
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateRelation(Request request) {
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
				service.updateRelation(graphId, startNodeId, endNodeId, relationType, request);
				OK(getSender());
			} catch (Exception e) {
				ERROR(e, getSender());
			}
		} else {
			OK(getSender());
		}
	}

	@Override
	public void removeRelationMetadata(Request request) {
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
				service.removeRelationMetadataByKey(graphId, startNodeId, endNodeId, relationType, key, request);
				OK(getSender());
			} catch (Exception e) {
				ERROR(e, getSender());
			}
		} else {
			OK(getSender());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void createCollection(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String collectionId = (String) request.get(GraphDACParams.collection_id.name());
		com.ilimi.graph.dac.model.Node collection = (com.ilimi.graph.dac.model.Node) request
				.get(GraphDACParams.node.name());
		String relationType = (String) request.get(GraphDACParams.relation_type.name());
		List<String> members = (List<String>) request.get(GraphDACParams.members.name());
		String indexProperty = (String) request.get(GraphDACParams.index.name());
		if (!validateRequired(collectionId, members)) {
			throw new ClientException(GraphDACErrorCodes.ERR_CREATE_COLLECTION_MISSING_REQ_PARAMS.name(),
					"Required Variables are missing");
		} else {
			try {
				service.createCollection(graphId, collectionId, collection, relationType, members, indexProperty,
						request);
				OK(getSender());
			} catch (Exception e) {
				ERROR(e, getSender());
			}
		}
	}

	public void deleteCollection(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String collectionId = (String) request.get(GraphDACParams.collection_id.name());
		if (!validateRequired(collectionId)) {
			throw new ClientException(GraphDACErrorCodes.ERR_DELETE_COLLECTION_MISSING_REQ_PARAMS.name(),
					"Required Variables are missing");
		} else {
			try {
				service.deleteCollection(graphId, collectionId, request);
				OK(getSender());
			} catch (Exception e) {
				ERROR(e, getSender());
			}
		}
	}

	@Override
	public void importGraph(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String taskId = request.get(GraphDACParams.task_id.name()) == null ? null
				: (String) request.get(GraphDACParams.task_id.name());
		ImportData input = (ImportData) request.get(GraphDACParams.import_input_object.name());
		if (StringUtils.isBlank(graphId)) {
			throw new ClientException(GraphEngineErrorCodes.ERR_INVALID_GRAPH_ID.name(), "Graph Id cannot be blank");
		} else {
			try {
				Map<String, List<String>> messages = service.importGraph(graphId, taskId, input, request);
				OK(GraphDACParams.messages.name(), messages, getSender());
			} catch (Exception e) {
				ERROR(e, getSender());
			}
		}
	}

}
