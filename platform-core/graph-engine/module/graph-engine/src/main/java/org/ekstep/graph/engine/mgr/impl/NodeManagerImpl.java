package org.ekstep.graph.engine.mgr.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.graph.common.DateUtils;
import org.ekstep.graph.common.enums.GraphHeaderParams;
import org.ekstep.graph.common.mgr.BaseGraphManager;
import org.ekstep.graph.dac.enums.AuditProperties;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.enums.SystemNodeTypes;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.graph.engine.mgr.INodeManager;
import org.ekstep.graph.engine.router.GraphEngineActorPoolMgr;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.graph.exception.GraphEngineErrorCodes;
import org.ekstep.graph.model.Graph;
import org.ekstep.graph.model.collection.Set;
import org.ekstep.graph.model.node.DataNode;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.graph.model.node.DefinitionNode;
import org.ekstep.graph.model.node.MetadataDefinition;
import org.ekstep.graph.model.node.ProxyNode;

import akka.actor.ActorRef;
import akka.dispatch.Futures;
import akka.dispatch.OnComplete;
import akka.dispatch.OnSuccess;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

/**
 * The Class NodeManagerImpl.
 * 
 * @author Mohammad Azharuddin
 */
public class NodeManagerImpl extends BaseGraphManager implements INodeManager {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.graph.common.mgr.BaseGraphManager#invokeMethod(org.ekstep.common.dto
	 * .Request, akka.actor.ActorRef)
	 */
	protected void invokeMethod(Request request, ActorRef parent) {
		String methodName = request.getOperation();
		try {
			Method method = GraphEngineActorPoolMgr.getMethod(GraphEngineManagers.NODE_MANAGER, methodName);
			if (null == method) {
				throw new ClientException("ERR_GRAPH_INVALID_OPERATION", "Operation '" + methodName + "' not found");
			} else {
				method.invoke(this, request);
			}
		} catch (Exception e) {
			ERROR(e.getCause(), parent);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.graph.engine.mgr.INodeManager#saveDefinitionNode(org.ekstep.common.
	 * dto.Request)
	 */
	@Override
	public void saveDefinitionNode(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		DefinitionDTO definition = (DefinitionDTO) request.get(GraphDACParams.definition_node.name());
		if (!validateRequired(definition) || StringUtils.isBlank(definition.getObjectType())) {
			throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_SAVE_DEF_NODE_MISSING_REQ_PARAMS.name(),
					"Required parameters are missing...");
		} else {
			try {
				List<MetadataDefinition> indexedMetadata = new ArrayList<MetadataDefinition>();
				List<MetadataDefinition> nonIndexedMetadata = new ArrayList<MetadataDefinition>();
				if (null != definition.getProperties() && !definition.getProperties().isEmpty()) {
					for (MetadataDefinition def : definition.getProperties()) {
						if (def.isIndexed()) {
							indexedMetadata.add(def);
						} else {
							nonIndexedMetadata.add(def);
						}
					}
				}
				DefinitionNode node = new DefinitionNode(this, graphId, definition.getObjectType(), indexedMetadata,
						nonIndexedMetadata, definition.getInRelations(), definition.getOutRelations(),
						definition.getSystemTags());
				node.setMetadata(definition.getMetadata());
				node.create(request);
			} catch (Exception e) {
				handleException(e, getSender());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.graph.engine.mgr.INodeManager#updateDefinition(org.ekstep.common.dto
	 * .Request)
	 */
	@SuppressWarnings("unchecked")
	public void updateDefinition(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String objectType = (String) request.get(GraphDACParams.object_type.name());
		List<MetadataDefinition> definitions = (List<MetadataDefinition>) request
				.get(GraphDACParams.metadata_definitions.name());
		if (!validateRequired(objectType, definitions)) {
			throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_SAVE_DEF_NODE_MISSING_REQ_PARAMS.name(),
					"Required parameters are missing...");
		} else {
			try {
				DefinitionNode defNode = new DefinitionNode(this, graphId, objectType, null, null, null, null, null);
				defNode.update(request);
			} catch (Exception e) {
				handleException(e, getSender());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.graph.engine.mgr.INodeManager#createDataNode(org.ekstep.common.dto.
	 * Request)
	 */
	@Override
	public void createDataNode(final Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		final ActorRef parent = getSender();
		final Node node = (Node) request.get(GraphDACParams.node.name());
		Boolean skipValidations = (Boolean) request.get(GraphDACParams.skip_validations.name());
		if (!validateRequired(node)) {
			throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_ADD_NODE_MISSING_REQ_PARAMS.name(),
					"Required parameters are missing...");
		} else {
			try {
				if (null == skipValidations)
					skipValidations = false;
				final DataNode datanode = new DataNode(this, graphId, node);
				final ExecutionContext ec = getContext().dispatcher();
				final List<String> messages = new ArrayList<String>();
				// validate the node
				if (null == skipValidations || !skipValidations) {
					Map<String, List<String>> validationMap = datanode.validateNode(request);
					for (List<String> list : validationMap.values()) {
						if (null != list && !list.isEmpty()) {
							for (String msg : list) {
								messages.add(msg);
							}
						}
					}
				}
				if (messages.isEmpty()) {
					datanode.removeExternalFields();
					// create the node object
					Response createRes = datanode.createNode(request);
					if (null != createRes && checkError(createRes)) {
						sendResponse(createRes, parent);
					} else {
						// if node is created successfully,
						// create relations and tags
						List<Relation> addRels = datanode.getNewRelationList();
						updateRelations(parent, node, datanode, request, ec, addRels, null);
					}
				} else {
					ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_NODE_VALIDATION_FAILED.name(), "Validation Errors",
							ResponseCode.CLIENT_ERROR, GraphDACParams.messages.name(), messages, parent);
				}
			} catch (Exception e) {
				handleException(e, getSender());
			}
		}
	}

	/**
	 * Update relations and tags.
	 *
	 * @param parent
	 *            the parent
	 * @param node
	 *            the node
	 * @param datanode
	 *            the datanode
	 * @param request
	 *            the request
	 * @param ec
	 *            the ec
	 * @param addRels
	 *            the add rels
	 * @param delRels
	 *            the del rels
	 * @param addTags
	 *            the add tags
	 * @param delTags
	 *            the del tags
	 */
	private void updateRelations(final ActorRef parent, Node node, final DataNode datanode, final Request request,
			final ExecutionContext ec, final List<Relation> addRels, final List<Relation> delRels) {
		Future<List<String>> deleteRelsFuture = null;
		List<String> msgs = new ArrayList<String>();
		try {
			deleteRelsFuture = datanode.deleteRelations(request, ec, delRels);
		} catch (Exception e) {
			msgs.add(e.getMessage());
			deleteRelsFuture = Futures.successful(msgs);
		}
		deleteRelsFuture.onSuccess(new OnSuccess<List<String>>() {
			@Override
			public void onSuccess(List<String> msgs) throws Throwable {
				List<Future<List<String>>> validationFutures = new ArrayList<Future<List<String>>>();
				List<String> messages = new ArrayList<String>();
				if (null == msgs || msgs.isEmpty()) {
					try {
						Future<List<String>> relsFuture = datanode.createRelations(request, ec, addRels);
						validationFutures.add(relsFuture);
					} catch (Exception e) {
						messages.add(e.getMessage());
						validationFutures.add(Futures.successful(messages));
					}
				} else {
					validationFutures.add(Futures.successful(msgs));
				}
				Futures.sequence(validationFutures, ec).onComplete(new OnComplete<Iterable<List<String>>>() {
					@Override
					public void onComplete(Throwable arg0, Iterable<List<String>> arg1) throws Throwable {
						if (null != arg0) {
							ERROR(arg0, parent);
						} else {
							List<String> msgs = new ArrayList<String>();
							if (null != arg1) {
								for (List<String> list : arg1) {
									if (null != list && !list.isEmpty())
										msgs.addAll(list);
								}
							}
							if (msgs.isEmpty()) {
								Map<String, Object> responseMap = new HashMap<String, Object>();
								responseMap.put(GraphDACParams.node_id.name(), datanode.getNodeId());
								responseMap.put(GraphDACParams.versionKey.name(), datanode.getVersionKey());
								OK(responseMap, parent);
							} else {
								ERROR(GraphEngineErrorCodes.ERR_GRAPH_UPDATE_NODE_VALIDATION_FAILED.name(),
										"Failed to update relations and tags", ResponseCode.CLIENT_ERROR,
										GraphDACParams.messages.name(), msgs, parent);
							}
						}
					}
				}, ec);
			}
		}, ec);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.graph.engine.mgr.INodeManager#validateNode(org.ekstep.common.dto.
	 * Request)
	 */
	@Override
	public void validateNode(Request request) {
		final ActorRef parent = getSender();
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		final Node node = (Node) request.get(GraphDACParams.node.name());
		if (!validateRequired(node)) {
			throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_VALIDATE_NODE_MISSING_REQ_PARAMS.name(),
					"Required parameters are missing...");
		} else {
			try {
				final DataNode datanode = new DataNode(this, graphId, node);
				Map<String, List<String>> map = datanode.validateNode(request);
				if (null == map || map.isEmpty()) {
					OK(parent);
				} else {
					List<String> messages = new ArrayList<String>();
					for (Entry<String, List<String>> entry : map.entrySet()) {
						if (null != entry.getValue() && !entry.getValue().isEmpty()) {
							messages.addAll(entry.getValue());
						}
					}
					if (messages.isEmpty()) {
						OK(parent);
					} else {
						ERROR(GraphEngineErrorCodes.ERR_GRAPH_NODE_VALIDATION_FAILED.name(), "Node validation failed",
								ResponseCode.CLIENT_ERROR, GraphDACParams.messages.name(), messages, parent);
					}
				}
			} catch (Exception e) {
				ERROR(GraphEngineErrorCodes.ERR_GRAPH_NODE_VALIDATION_FAILED.name(), "Error while validating node",
						ResponseCode.CLIENT_ERROR, GraphDACParams.messages.name(), e.getMessage(), parent);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.graph.engine.mgr.INodeManager#updateDataNode(org.ekstep.common.dto.
	 * Request)
	 */
	@Override
	public void updateDataNode(final Request request) {
		final ActorRef parent = getSender();
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String nodeId = (String) request.get(GraphDACParams.node_id.name());
		final Node node = (Node) request.get(GraphDACParams.node.name());
		final Boolean skipValidations = (Boolean) request.get(GraphDACParams.skip_validations.name());
		if (!validateRequired(nodeId, node)) {
			throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_UPDATE_NODE_MISSING_REQ_PARAMS.name(),
					"Required parameters are missing...");
		} else {
			final ExecutionContext ec = getContext().dispatcher();
			node.setIdentifier(nodeId);
			final DataNode datanode = new DataNode(this, graphId, node);
			final List<String> messages = new ArrayList<String>();
			final List<Relation> addRels = new ArrayList<Relation>();
			final List<Relation> delRels = new ArrayList<Relation>();
			final List<Node> dbNodes = new ArrayList<Node>();
			String date = DateUtils.formatCurrentDate();

			Node dbNode = datanode.getNodeObject(request);

			if (null != dbNode && StringUtils.equals(SystemNodeTypes.DATA_NODE.name(), dbNode.getNodeType())) {
				if (null == datanode.getMetadata()) {
					datanode.setMetadata(new HashMap<String, Object>());
				}
				Map<String, Object> dbMetadata = dbNode.getMetadata();
				if (null != dbMetadata && !dbMetadata.isEmpty()) {
					dbMetadata.remove(GraphDACParams.versionKey.name());
					dbMetadata.remove(GraphDACParams.lastUpdatedBy.name());
					// add lastStatusChangedOn if status got changed
					String currentStatus = (String) node.getMetadata().get(GraphDACParams.status.name());
					String previousStatus = (String) dbMetadata.get(GraphDACParams.status.name());
					if (StringUtils.isNotBlank(currentStatus) && !StringUtils.equalsIgnoreCase(currentStatus, previousStatus)) {
						datanode.getMetadata().put(AuditProperties.lastStatusChangedOn.name(), date);
						datanode.getMetadata().put(AuditProperties.prevStatus.name(), previousStatus);
					}
					for (Entry<String, Object> entry : dbMetadata.entrySet()) {
						if (!datanode.getMetadata().containsKey(entry.getKey()))
							datanode.getMetadata().put(entry.getKey(), entry.getValue());
					}
				}
				getRelationsDelta(addRels, delRels, dbNode, datanode);
				dbNodes.add(dbNode);
			}
			if (messages.isEmpty()) {
				// validate the node
				if (null == skipValidations || !skipValidations) {
					Map<String, List<String>> validationMap = datanode.validateNode(request);
					if (!validationMap.isEmpty()) {
						for (List<String> list : validationMap.values()) {
							if (null != list && !list.isEmpty()) {
								for (String msg : list) {
									messages.add(msg);
								}
							}
						}
					}
				}
				if (messages.isEmpty()) {
					Response updateResponse = null;
					datanode.removeExternalFields();
					if (null == dbNodes || dbNodes.isEmpty())
						updateResponse = datanode.createNode(request);
					else
						updateResponse = datanode.updateNode(request);

					if (null != updateResponse && checkError(updateResponse)) {
						sendResponse(updateResponse, parent);
					} else {
						// if node metadata is
						// updated
						// successfully,
						// update relations and tags
						updateRelations(parent, node, datanode, request, ec, addRels, delRels);
					}
				} else {
					ERROR(GraphEngineErrorCodes.ERR_GRAPH_UPDATE_NODE_VALIDATION_FAILED.name(),
							"Node Metadata validation failed", ResponseCode.CLIENT_ERROR,
							GraphDACParams.messages.name(), messages, parent);
				}
			} else {
				ERROR(GraphEngineErrorCodes.ERR_GRAPH_UPDATE_NODE_NOT_FOUND.name(), "Node Not Found",
						ResponseCode.RESOURCE_NOT_FOUND, GraphDACParams.messages.name(), messages, parent);
			}

		}
	}

	@SuppressWarnings("unchecked")
	public void updateDataNodes(final Request request) {
		final ActorRef parent = getSender();
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		List<String> nodeIds = (List<String>) request.get(GraphDACParams.node_ids.name());
		Map<String, Object> metadata = (Map<String, Object>) request.get(GraphDACParams.metadata.name());

		if (!validateRequired(nodeIds, metadata)) {
			throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_UPDATE_NODE_MISSING_REQ_PARAMS.name(),
					"Required parameters are missing...");
		} else {
			StringBuilder sb = new StringBuilder();
			Map<String, Object> params = new HashMap<String, Object>();
			int pIndex = 1;
			sb.append("MATCH (n:" + graphId + ") WHERE n.IL_UNIQUE_ID IN {1} SET");
			params.put("" + pIndex, nodeIds);
			pIndex += 1;
			pIndex = getFilterQuery(metadata, sb, params, "n", pIndex);
			sb.append(" ;");
			Graph graph = new Graph(this, graphId);
			Response response = graph.executeUpdateQuery(request, sb.toString(), params);
			sendResponse(response, parent);
		}

	}

	/**
	 * Gets the relations delta.
	 *
	 * @param addRels
	 *            the add rels
	 * @param delRels
	 *            the del rels
	 * @param dbNode
	 *            the db node
	 * @param datanode
	 *            the datanode
	 * @return the relations delta
	 */
	private void getRelationsDelta(List<Relation> addRels, List<Relation> delRels, Node dbNode, DataNode datanode) {
		if (null == datanode.getInRelations()) {
			datanode.setInRelations(dbNode.getInRelations());
		} else {
			getNewRelationsList(dbNode.getInRelations(), datanode.getInRelations(), addRels, delRels);
		}
		if (null == datanode.getOutRelations()) {
			datanode.setOutRelations(dbNode.getOutRelations());
		} else {
			getNewRelationsList(dbNode.getOutRelations(), datanode.getOutRelations(), addRels, delRels);
		}
	}

	/**
	 * Gets the new relations list.
	 *
	 * @param dbRelations
	 *            the db relations
	 * @param newRelations
	 *            the new relations
	 * @param addRels
	 *            the add rels
	 * @param delRels
	 *            the del rels
	 * @return the new relations list
	 */
	private void getNewRelationsList(List<Relation> dbRelations, List<Relation> newRelations, List<Relation> addRels,
			List<Relation> delRels) {
		List<String> relList = new ArrayList<String>();
		for (Relation rel : newRelations) {
			addRels.add(rel);
			String relKey = rel.getStartNodeId() + rel.getRelationType() + rel.getEndNodeId();
			if (!relList.contains(relKey))
				relList.add(relKey);
		}
		if (null != dbRelations && !dbRelations.isEmpty()) {
			for (Relation rel : dbRelations) {
				String relKey = rel.getStartNodeId() + rel.getRelationType() + rel.getEndNodeId();
				if (!relList.contains(relKey))
					delRels.add(rel);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.graph.engine.mgr.INodeManager#deleteDataNode(org.ekstep.common.dto.
	 * Request)
	 */
	@Override
	public void deleteDataNode(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String nodeId = (String) request.get(GraphDACParams.node_id.name());
		if (!validateRequired(nodeId)) {
			throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_REMOVE_NODE_MISSING_REQ_PARAMS.name(),
					"Required parameters are missing...");
		} else {
			try {
				DataNode node = new DataNode(this, graphId, nodeId, null, null);
				node.delete(request);
			} catch (Exception e) {
				handleException(e, getSender());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.graph.engine.mgr.INodeManager#deleteDefinition(org.ekstep.common.dto
	 * .Request)
	 */
	@Override
	public void deleteDefinition(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String objectType = (String) request.get(GraphDACParams.object_type.name());
		if (!validateRequired(objectType)) {
			throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_REMOVE_NODE_MISSING_REQ_PARAMS.name(),
					"Required parameters are missing...");
		} else {
			try {
				DefinitionNode node = new DefinitionNode(this, graphId, objectType, null, null, null, null, null);
				node.delete(request);
			} catch (Exception e) {
				handleException(e, getSender());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.graph.engine.mgr.INodeManager#importDefinitions(org.ekstep.common.
	 * dto.Request)
	 */
	@Override
	public void importDefinitions(final Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		try {
			Graph graph = new Graph(this, graphId);
			graph.importDefinitions(request);
		} catch (Exception e) {
			handleException(e, getSender());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.graph.engine.mgr.INodeManager#exportNode(org.ekstep.common.dto.
	 * Request)
	 */
	public void exportNode(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String nodeId = (String) request.get(GraphDACParams.node_id.name());
		if (!validateRequired(nodeId)) {
			throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_EXPORT_NODE_MISSING_REQ_PARAMS.name(),
					"Required parameters are missing...");
		} else {
			try {
				Graph graph = new Graph(this, graphId);
				graph.exportNode(request);
			} catch (Exception e) {
				handleException(e, getSender());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.graph.engine.mgr.INodeManager#upsertRootNode(org.ekstep.common.dto.
	 * Request)
	 */
	@Override
	public void upsertRootNode(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		if (StringUtils.isBlank(graphId)) {
			throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_ADD_NODE_MISSING_REQ_PARAMS.name(),
					"Required parameters are missing...");
		} else {
			try {
				Graph graph = new Graph(this, graphId);
				graph.upsertRootNode(request);
			} catch (Exception e) {
				handleException(e, getSender());
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.graph.engine.mgr.INodeManager#createProxyNode(org.ekstep.common.dto.
	 * Request)
	 */
	@Override
	public void createProxyNode(final Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		final ActorRef parent = getSender();
		final Node node = (Node) request.get(GraphDACParams.node.name());
		Boolean skipValidations = (Boolean) request.get(GraphDACParams.skip_validations.name());
		if (!validateRequired(node)) {
			throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_ADD_NODE_MISSING_REQ_PARAMS.name(),
					"Required parameters are missing...");
		} else {
			try {
				if (null == skipValidations)
					skipValidations = false;
				final ProxyNode proxyNode = new ProxyNode(this, graphId, node);
				final List<String> messages = new ArrayList<String>();
				// validate the node
				Map<String, List<String>> nodeValidation = null;
				if (!skipValidations)
					nodeValidation = proxyNode.validateNode(request);

				if (null != nodeValidation && !nodeValidation.isEmpty()) {
					for (List<String> list : nodeValidation.values()) {
						if (null != list && !list.isEmpty()) {
							for (String msg : list) {
								messages.add(msg);
							}
						}
					}
				}

				if (messages.isEmpty()) {
					// create the node object
					String createProxyNode = proxyNode.createNode(request);

					if (StringUtils.isNotBlank(createProxyNode)) {
						messages.add(createProxyNode);
						ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_NODE_UNKNOWN_ERROR.name(), "Node Creation Error",
								ResponseCode.CLIENT_ERROR, GraphDACParams.messages.name(), messages, parent);
					}
				} else {
					ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_NODE_VALIDATION_FAILED.name(), "Validation Errors",
							ResponseCode.CLIENT_ERROR, GraphDACParams.messages.name(), messages, parent);
				}
			} catch (Exception e) {
				handleException(e, getSender());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.graph.engine.mgr.INodeManager#createProxyNodeAndTranslation(com.
	 * ilimi.common.dto.Request)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void createProxyNodeAndTranslation(Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		final ActorRef parent = getSender();
		final Node node = (Node) request.get(GraphDACParams.node.name());
		final Node translationNode = (Node) request.get(GraphDACParams.translationSet.name());
		final boolean create = (boolean) request.get("create");
		final boolean proxy = (boolean) request.get("proxy");

		List<String> memberIds = (List<String>) request.get(GraphDACParams.members.name());
		String setObjectType = (String) request.get(GraphDACParams.object_type.name());
		String memberObjectType = (String) request.get(GraphDACParams.member_type.name());

		if (!validateRequired(node) || !validateRequired(translationNode)) {
			throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_ADD_NODE_MISSING_REQ_PARAMS.name(),
					"Required parameters are missing...");
		} else {
			try {

				final ExecutionContext ec = getContext().dispatcher();
				final List<String> messages = new ArrayList<String>();

				final ProxyNode proxyNode = new ProxyNode(this, graphId, node);
				final Set set = new Set(this, graphId, null, setObjectType, memberObjectType,
						translationNode.getMetadata(), memberIds);
				set.setInRelations(translationNode.getInRelations());
				set.setOutRelations(translationNode.getOutRelations());
				if (!proxy) {
					String createProxyNode = proxyNode.createNode(request);

					if (StringUtils.isNotBlank(createProxyNode)) {
						messages.add(createProxyNode);
						ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_NODE_UNKNOWN_ERROR.name(), "Node Creation Error",
								ResponseCode.CLIENT_ERROR, GraphDACParams.messages.name(), messages, parent);
					} else {
						if (messages.isEmpty()) {
							// create the node object
							request.put(GraphDACParams.node.name(), translationNode);
							if (create) {
								set.createSetNode(request, ec);
							} else {
								set.addMembers(request);
							}
						} else {
							ERROR(GraphEngineErrorCodes.ERR_GRAPH_ADD_NODE_VALIDATION_FAILED.name(),
									"Validation Errors", ResponseCode.CLIENT_ERROR, GraphDACParams.messages.name(),
									messages, parent);
						}
					}
				} else {
					request.put(GraphDACParams.node.name(), translationNode);
					if (create) {
						set.createSetNode(request, ec);
					} else {
						set.addMembers(request);
					}
				}
			} catch (Exception e) {
				handleException(e, getSender());
			}
		}
	}

	private int getFilterQuery(Map<String, Object> metadata, StringBuilder sb, Map<String, Object> params, String index,
			int pIndex) {
		int i = 0;
		for (String key : metadata.keySet()) {
			sb.append(" ").append(index).append(".").append(key).append(" = {").append(pIndex).append("} ");
			params.put("" + pIndex, metadata.get(key));
			pIndex += 1;
			if (i < metadata.size() - 1) {
				sb.append(", ");
				i++;
			}
		}
		return pIndex;
	}
}
