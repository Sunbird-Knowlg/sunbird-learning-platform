package org.sunbird.graph.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.sunbird.common.dto.Property;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.dto.ResponseParams;
import org.sunbird.common.dto.ResponseParams.StatusType;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.cache.mgr.impl.NodeCacheManager;
import org.sunbird.graph.cache.mgr.impl.SetCacheManager;
import org.sunbird.graph.common.enums.GraphEngineParams;
import org.sunbird.graph.common.enums.GraphHeaderParams;
import org.sunbird.graph.common.mgr.BaseGraphManager;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.RelationTypes;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.enums.SystemProperties;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.graph.dac.model.SearchCriteria;
import org.sunbird.graph.enums.ImportType;
import org.sunbird.graph.exception.GraphEngineErrorCodes;
import org.sunbird.graph.importer.ImportData;
import org.sunbird.graph.importer.InputStreamValue;
import org.sunbird.graph.importer.OutputStreamValue;
import org.sunbird.graph.model.cache.DefinitionCache;
import org.sunbird.graph.model.node.DataNode;
import org.sunbird.graph.model.node.DefinitionDTO;
import org.sunbird.graph.model.node.DefinitionNode;
import org.sunbird.graph.model.node.MetadataDefinition;
import org.sunbird.graph.model.relation.RelationHandler;
import org.sunbird.graph.reader.CSVImportMessageHandler;
import org.sunbird.graph.reader.GraphReader;
import org.sunbird.graph.reader.GraphReaderFactory;
import org.sunbird.graph.reader.JsonGraphReader;
import org.sunbird.graph.writer.GraphWriterFactory;
import org.sunbird.graph.writer.RDFGraphWriter;
import org.sunbird.telemetry.logger.TelemetryManager;

import akka.dispatch.Futures;
import akka.util.Timeout;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class Graph extends AbstractDomainObject {

	public static final String ERROR_MESSAGES = "ERROR_MESSAGES";
	public static Timeout WAIT_TIMEOUT = new Timeout(Duration.create(30, TimeUnit.SECONDS));

	public Graph(BaseGraphManager manager, String graphId) {
		super(manager, graphId);
	}

	public void create(Request req) {
		try {
			Request request = new Request(req);
			Response response = graphMgr.createGraph(request);
			manager.returnResponse(Futures.successful(response), getParent());
		} catch (Exception e) {
			throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_CREATE_GRAPH_UNKNOWN_ERROR.name(), e.getMessage(),
					e);
		}
	}

	public void createUniqueConstraint(Request req) {
		try {
			Request request = new Request(req);
			request.copyRequestValueObjects(req.getRequest());
			Future<Object> response = Futures.successful(graphMgr.createUniqueConstraint(request));
			manager.returnResponse(response, getParent());
		} catch (Exception e) {
			throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_CREATE_UNIQUE_CONSTRAINT_UNKNOWN_ERROR.name(),
					e.getMessage(), e);
		}
	}

	public void createIndex(Request req) {
		try {
			Request request = new Request(req);
			request.copyRequestValueObjects(req.getRequest());
			Future<Object> response = Futures.successful(graphMgr.createIndex(request));
			manager.returnResponse(response, getParent());
		} catch (Exception e) {
			throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_CREATE_INDEX_UNKNOWN_ERROR.name(), e.getMessage(),
					e);
		}
	}

	public List<Node> getAllSetObjects(Request req) throws Exception {

		int batch = 1000;
		int start = 0;
		boolean found = true;
		List<Node> allNodes = new ArrayList<>();
		while (found) {
			List<Node> nodes = getSetNodes(req, start, batch);
			if (null != nodes && !nodes.isEmpty()) {
				allNodes.addAll(nodes);
				start += batch;
			} else {
				found = false;
				break;
			}
		}
		return allNodes;
	}

	@SuppressWarnings("unchecked")
	private List<Node> getSetNodes(Request req, int startPosition, int batchSize) throws Exception {
		SearchCriteria sc = new SearchCriteria();
		sc.setNodeType(SystemNodeTypes.SET.name());
		sc.setResultSize(batchSize);
		sc.setStartPosition(startPosition);

		final Request setNodesReq = new Request(req);
		setNodesReq.put(GraphDACParams.search_criteria.name(), sc);
		Response response = searchMgr.searchNodes(setNodesReq);
		if (!manager.checkError(response)) {
			List<Node> nodes = (List<Node>) response.get(GraphDACParams.node_list.name());
			return nodes;
		} else {
			throw new ResourceNotFoundException(GraphEngineErrorCodes.ERR_GRAPH_LOAD_GRAPH_UNKNOWN_ERROR.name(),
					 					"Nodes not found: " + graphId);
		}
	}

	@SuppressWarnings("unchecked")
	public void load(Request req) {
		try {

			// get all sets
			List<Node> setNodes = getAllSetObjects(req);
			if (null != setNodes && !setNodes.isEmpty()) {
				for (Node node : setNodes) {
					List<String> memberIds = new ArrayList<String>();
					if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
						for (Relation rel : node.getOutRelations()) {
							if (StringUtils.equalsIgnoreCase(RelationTypes.SET_MEMBERSHIP.relationName(),
									rel.getRelationType())) {
								memberIds.add(rel.getEndNodeId());
							}
						}
					}
					if (null != memberIds && !memberIds.isEmpty()) {
						String setId = node.getIdentifier();
						SetCacheManager.createSet(graphId, setId, memberIds);
					}
				}
			}

			// get all definition nodes
			final Request defNodesReq = new Request(req);
			Property defNodeProperty = new Property(SystemProperties.IL_SYS_NODE_TYPE.name(),
					SystemNodeTypes.DEFINITION_NODE.name());
			defNodesReq.put(GraphDACParams.metadata.name(), defNodeProperty);
			Response res = searchMgr.getNodesByProperty(defNodesReq);

			if (manager.checkError(res)) {
				manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_LOAD_GRAPH_UNKNOWN_ERROR.name(),
						"Failed to get definition nodes", ResponseCode.SERVER_ERROR, getParent());
			} else {
				List<Node> defNodes = (List<Node>) res.get(GraphDACParams.node_list.name());
				if (null != defNodes && !defNodes.isEmpty()) {
					for (Node defNode : defNodes) {
						DefinitionNode node = new DefinitionNode(manager, defNode);
						node.loadToCache(defNodesReq);
					}
					manager.OK(getParent());
				} else {
					manager.OK(getParent());
				}
			}

		} catch (Exception e) {
			throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_CREATE_GRAPH_UNKNOWN_ERROR.name(), e.getMessage(),
					e);
		}
	}

	public void validate(final Request req) {
		Map<String, List<String>> validationMap = validateGraph(req);
		manager.OK(GraphDACParams.messages.name(), validationMap, getParent());
	}

	private Map<String, List<String>> validateGraph(final Request req) {
		try {

			// get all definition nodes
			Request defNodesReq = new Request(req);
			Property defNodeProperty = new Property(SystemProperties.IL_SYS_NODE_TYPE.name(),
					SystemNodeTypes.DEFINITION_NODE.name());
			defNodesReq.put(GraphDACParams.metadata.name(), defNodeProperty);
			Response defNodesResponse = searchMgr.getNodesByProperty(defNodesReq);

			// get all data nodes
			Request request = new Request(req);
			Property property = new Property(SystemProperties.IL_SYS_NODE_TYPE.name(),
					SystemNodeTypes.DATA_NODE.name());
			request.put(GraphDACParams.metadata.name(), property);
			Response dataNodesResponse = searchMgr.getNodesByProperty(request);

			// get all relations
			Request relsRequest = new Request(req);
			Response relationsResponse = searchMgr.getAllRelations(relsRequest);

			List<Map<String, List<String>>> validationMessages = new ArrayList<Map<String, List<String>>>();

			// Promise to get all relation validation messages
			Map<String, List<String>> relationMessages = getRelationValidationsFuture(relationsResponse, relsRequest);
			validationMessages.add(relationMessages);

			// get future of all node validation messages
			Map<String, List<String>> nodeMessages = getNodesValidationsFuture(defNodesResponse, dataNodesResponse,
					request);
			validationMessages.add(nodeMessages);
			Map<String, List<String>> errorMap = new HashMap<String, List<String>>();
			if (null != validationMessages) {
				for (Map<String, List<String>> map : validationMessages) {
					if (null != map && !map.isEmpty()) {
						for (Entry<String, List<String>> entry : map.entrySet()) {
							if (null != entry.getValue() && !entry.getValue().isEmpty()) {
								List<String> list = errorMap.get(entry.getKey());
								if (null == list) {
									list = new ArrayList<String>();
									errorMap.put(entry.getKey(), list);
								}
								list.addAll(entry.getValue());
							}
						}
					}
				}
			}
			return errorMap;

		} catch (Exception e) {
			throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_VALIDATE_GRAPH_UNKNOWN_ERROR.name(),
					e.getMessage(), e);
		}
	}

	public void delete(Request req) {
		try {
			Request request = new Request(req);
			Future<Object> response = Futures.successful(graphMgr.deleteGraph(request));
			manager.returnResponse(response, getParent());
		} catch (Exception e) {
			throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_DELETE_GRAPH_UNKNOWN_ERROR.name(), e.getMessage(),
					e);
		}
	}

	public void createTaskNode(Request request) throws Exception {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		Node node = new Node();
		node.setIdentifier(graphId + "_task_" + System.currentTimeMillis());
		node.setNodeType(SystemNodeTypes.DATA_NODE.name());
		node.setGraphId(graphId);
		Map<String, Object> metadata = new HashMap<String, Object>();
		metadata.put(GraphEngineParams.status.name(), GraphEngineParams.Pending.name());
		node.setMetadata(metadata);

		final Request req = new Request();
		req.put(GraphDACParams.node.name(), node);
		req.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
		Response response = nodeMgr.addNode(request);
		String taskId = (String) response.get(GraphDACParams.node_id.name());
		manager.OK(GraphEngineParams.task_id.name(), taskId, getParent());
	}

	public void addOutRelations(Request request) {
		try {
			Future<Object> response = Futures.successful(graphMgr.addOutgoingRelations(request));
			manager.returnResponse(response, getParent());
		} catch (Exception e) {
			throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_CREATE_RELATION_NODE_FAILED.name(),
					e.getMessage(), e);
		}
	}

	public void addInRelations(Request request) {
		try {
			Future<Object> response = Futures.successful(graphMgr.addIncomingRelations(request));
			manager.returnResponse(response, getParent());
		} catch (Exception e) {
			throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_CREATE_RELATION_NODE_FAILED.name(),
					e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public void importGraph(final Request request) {
		try {
			final String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
			final String format = (String) request.get(GraphEngineParams.format.name());
			final String taskId = request.get(GraphEngineParams.task_id.name()) == null ? null
					: (String) request.get(GraphEngineParams.task_id.name());
			final InputStreamValue inputStream = (InputStreamValue) request.get(GraphEngineParams.input_stream.name());
			// final String taskId = createTaskNode(graphId);
			if (StringUtils.isBlank(graphId)) {
				throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_IMPORT_INVALID_GRAPH_ID.name(),
						"GraphId is missing");
			}
			if (!manager.validateRequired(inputStream)) {
				throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_IMPORT_INVALID_INPUTSTREAM.name(),
						"Import stream is missing");
			} else {
				// Get byte array.
				try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
					IOUtils.copy(inputStream.getInputStream(), baos);
					byte[] bytes = baos.toByteArray();
					inputStream.setInputStream(new ByteArrayInputStream(bytes));
					final ByteArrayInputStream byteInputStream = new ByteArrayInputStream(bytes);

					// Fetch Definition Nodes
					final Request defNodesReq = new Request(request);
					Property defNodeProperty = new Property(SystemProperties.IL_SYS_NODE_TYPE.name(),
							SystemNodeTypes.DEFINITION_NODE.name());
					defNodesReq.put(GraphDACParams.metadata.name(), defNodeProperty);

					Response res = searchMgr.getNodesByProperty(defNodesReq);

					if (manager.checkError(res)) {
						manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_IMPORT_UNKNOWN_ERROR.name(),
								manager.getErrorMessage(res), res.getResponseCode(), getParent());
					} else {
						Map<String, Map<String, MetadataDefinition>> propertyDataMap = new HashMap<String, Map<String, MetadataDefinition>>();
						List<Node> defNodes = (List<Node>) res.get(GraphDACParams.node_list.name());
						List<DefinitionNode> defNodesList = new ArrayList<DefinitionNode>();
						for (Node defNode : defNodes) {
							DefinitionNode node = new DefinitionNode(manager, defNode);
							defNodesList.add(node);
						}
						for (DefinitionNode node : defNodesList) {
							String objectType = node.getFunctionalObjectType();
							Map<String, MetadataDefinition> propMap = new HashMap<String, MetadataDefinition>();
							List<MetadataDefinition> indexedMeta = node.getIndexedMetadata();
							if (indexedMeta != null) {
								for (MetadataDefinition propDef : indexedMeta) {
									propMap.put(propDef.getTitle(), propDef);
									propMap.put(propDef.getPropertyName(), propDef);
								}
							}
							List<MetadataDefinition> nonIndexedMeta = node.getNonIndexedMetadata();
							if (nonIndexedMeta != null) {
								for (MetadataDefinition propDef : nonIndexedMeta) {
									propMap.put(propDef.getTitle(), propDef);
									propMap.put(propDef.getPropertyName(), propDef);
								}
							}
							propertyDataMap.put(objectType, propMap);
						}

						final ImportData importData = GraphReaderFactory.getObject(getManager(), format, graphId,
								inputStream.getInputStream(), propertyDataMap);
						request.put(GraphDACParams.import_input_object.name(), importData);
						request.put(GraphDACParams.task_id.name(), taskId);
						// Use ImportData object and import Graph.
						Response importResponse = graphMgr.importGraph(request);

						ResponseParams params = (ResponseParams) importResponse.getParams();
						if (StatusType.failed.name().equals(params.getStatus())) {
							getParent().tell(importResponse, manager.getSelf());
						} else {
							final Map<String, List<String>> importMsgMap = (Map<String, List<String>>) importResponse
									.get(GraphDACParams.messages.name());
							CSVImportMessageHandler msgHandler = new CSVImportMessageHandler(byteInputStream);
							OutputStream outputStream = msgHandler.getOutputStream(importMsgMap);
							Map<String, Object> outputMap = new HashMap<String, Object>();
							outputMap.put(GraphEngineParams.output_stream.name(), new OutputStreamValue(outputStream));
							outputMap.put(GraphEngineParams.task_id.name(), taskId);
							manager.OK(outputMap, getParent());
						}

					}
				} catch (Exception e) {
					manager.ERROR(e, GraphEngineParams.task_id.name(), taskId, getParent());
				}
			}

		} catch (Exception e) {
			throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_IMPORT_UNKNOWN_ERROR.name(), e.getMessage(), e);
		}
	}

	public void searchNodes(Request req) {
		try {
			Request request = new Request(req);
			request.copyRequestValueObjects(req.getRequest());
			Future<Object> response = Futures.successful(searchMgr.searchNodes(request));
			manager.returnResponse(response, getParent());
		} catch (Exception e) {
			throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODES_UNKNOWN_ERROR.name(), e.getMessage(),
					e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> executeQuery(Request req, String query, Map<String, Object> params) {
		try {
			Request request = new Request(req);
			request.put(GraphDACParams.query.name(), query);
			if (null != params && !params.isEmpty())
				request.put(GraphDACParams.params.name(), params);
			Response res = searchMgr.executeQuery(request);
			List<Map<String, Object>> resultMap = (List<Map<String, Object>>) res
					.get(GraphDACParams.results.name());
			List<Map<String, Object>> result = null;
			if (null != resultMap && !resultMap.isEmpty()) {
				result = new ArrayList<Map<String, Object>>();
				for (Map<String, Object> map : resultMap) {
					if (null != map && !map.isEmpty())
						result.add(map);
				}
			}
			return result;
		} catch (Exception e) {
			throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODES_UNKNOWN_ERROR.name(), e.getMessage(),
					e);
		}
	}

	public Response executeUpdateQuery(Request req, String query, Map<String, Object> params) {
		Request request = new Request(req);
		request.put(GraphDACParams.query.name(), query);
		if (null != params && !params.isEmpty())
			request.put(GraphDACParams.params.name(), params);
		Response response = searchMgr.executeQuery(request);
		return response;
	}

	@SuppressWarnings("unchecked")
	public void getNodesByObjectType(Request req) {
		String objectType = (String) req.get(GraphDACParams.object_type.name());
		if (!manager.validateRequired(objectType)) {
			throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODES_MISSING_REQ_PARAMS.name(),
					"Object Type is required for GetNodesByObjectType API");
		} else {
			try {
				Request request = new Request(req);
				request.copyRequestValueObjects(req.getRequest());
				Property property = new Property(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), objectType);
				request.put(GraphDACParams.metadata.name(), property);
				Response res = searchMgr.getNodesByProperty(request);

				if (manager.checkError(res)) {
					manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_UNKNOWN_ERROR.name(),
							manager.getErrorMessage(res), res.getResponseCode(), getParent());
				} else {
					List<Node> nodes = (List<Node>) res.get(GraphDACParams.node_list.name());
					if (null != nodes && !nodes.isEmpty()) {
						List<Node> nodeList = new ArrayList<Node>();
						for (Node node : nodes) {
							if (null != node && StringUtils.isNotBlank(node.getNodeType()) && StringUtils
									.equalsIgnoreCase(SystemNodeTypes.DATA_NODE.name(), node.getNodeType())) {
								nodeList.add(node);
							}
						}
						manager.OK(GraphDACParams.node_list.name(), nodeList, getParent());
					} else {
						manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODE_NOT_FOUND.name(),
								"Failed to get data nodes", ResponseCode.RESOURCE_NOT_FOUND, getParent());
					}

				}
			} catch (Exception e) {
				throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODES_UNKNOWN_ERROR.name(),
						e.getMessage(), e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void getNodesByProperty(Request req) {
		try {
			Request request = new Request(req);
			request.copyRequestValueObjects(req.getRequest());
			Response res = searchMgr.getNodesByProperty(request);

			if (manager.checkError(res)) {
				manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_UNKNOWN_ERROR.name(), manager.getErrorMessage(res),
						res.getResponseCode(), getParent());
			} else {
				List<Node> nodes = (List<Node>) res.get(GraphDACParams.node_list.name());
				if (null != nodes && !nodes.isEmpty()) {
					List<Node> nodeList = new ArrayList<Node>();
					for (Node node : nodes) {
						if (null != node && StringUtils.isNotBlank(node.getNodeType())) {
							nodeList.add(node);
						}
					}
					manager.OK(GraphDACParams.node_list.name(), nodeList, getParent());
				} else {
					manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODE_NOT_FOUND.name(),
							"Failed to get data nodes", ResponseCode.RESOURCE_NOT_FOUND, getParent());
				}

			}
		} catch (Exception e) {
			throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODES_UNKNOWN_ERROR.name(), e.getMessage(),
					e);
		}
	}

	public void getDataNode(Request req) {
		try {
			Request request = new Request(req);
			request.copyRequestValueObjects(req.getRequest());

			Response res = searchMgr.getNodeByUniqueId(request);
			if (manager.checkError(res)) {
				manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_UNKNOWN_ERROR.name(), manager.getErrorMessage(res),
						res.getResponseCode(), getParent());
			} else {
				Node node = (Node) res.get(GraphDACParams.node.name());
				if (null == node || StringUtils.isBlank(node.getNodeType())
						|| !StringUtils.equalsIgnoreCase(SystemNodeTypes.DATA_NODE.name(), node.getNodeType())) {
					manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODE_NOT_FOUND.name(),
							"Failed to get data node", ResponseCode.RESOURCE_NOT_FOUND, getParent());
				} else {
					manager.OK(GraphDACParams.node.name(), node, getParent());
				}

			}

		} catch (Exception e) {
			throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODES_UNKNOWN_ERROR.name(), e.getMessage(),
					e);
		}
	}

	public void getDataNodes(Request req) {
		try {
			Request request = new Request(req);
			request.copyRequestValueObjects(req.getRequest());
			Future<Object> response = Futures.successful(searchMgr.getNodesByUniqueIds(request));
			manager.returnResponse(response, getParent());
		} catch (Exception e) {
			throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODES_UNKNOWN_ERROR.name(), e.getMessage(),
					e);
		}
	}

	public void executeQueryForProps(Request req) {
		try {
			Request request = new Request(req);
			request.copyRequestValueObjects(req.getRequest());
			Future<Object> response = Futures.successful(searchMgr.executeQueryForProps(request));
			manager.returnResponse(response, getParent());
		} catch (Exception e) {
			throw new ServerException(GraphEngineErrorCodes.ERR_EXECUTE_QUERY_FOR_NODES_UNKNOWN_ERROR.name(),
					e.getMessage(), e);
		}
	}

	public void getDefinitionNode(Request req) {
		try {
			String objectType = (String) req.get(GraphDACParams.object_type.name());
			String graphId = (String) req.getContext().get(GraphHeaderParams.graph_id.name());
			DefinitionDTO definition = DefinitionCache.getDefinitionNode(graphId, objectType);
			if (null != definition)
				manager.OK(GraphDACParams.definition_node.name(), definition, getParent());
			else {
				manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODE_NOT_FOUND.name(),
						"Failed to get definition node", ResponseCode.RESOURCE_NOT_FOUND, getParent());
			}
		} catch (Exception e) {
			throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODES_UNKNOWN_ERROR.name(), e.getMessage(),
					e);
		}
	}

	public void getDefinitionFromCache(Request req) {
		req.getContext().get(GraphDACParams.graph_id.name());
		String objectType = (String) req.get(GraphDACParams.object_type.name());
		DefinitionDTO node = (DefinitionDTO) NodeCacheManager.getDefinitionNode(graphId, objectType);
		if (null != node) {
			manager.OK(GraphDACParams.definition_node.name(), node, getParent());
		} else {
			manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODE_NOT_FOUND.name(), "Failed to get definition node",
					ResponseCode.RESOURCE_NOT_FOUND, getParent());
		}
	}

	@SuppressWarnings("unchecked")
	public void getDefinitionNodes(final Request req) {
		try {
			Request request = new Request(req);
			request.copyRequestValueObjects(req.getRequest());
			Response res = searchMgr.searchNodes(request);

			if (manager.checkError(res)) {
				manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_UNKNOWN_ERROR.name(), manager.getErrorMessage(res),
						res.getResponseCode(), getParent());
			} else {
				List<Node> nodes = (List<Node>) res.get(GraphDACParams.node_list.name());
				if (null != nodes && !nodes.isEmpty()) {
					List<DefinitionDTO> definitions = new ArrayList<DefinitionDTO>();
					for (Node node : nodes) {
						DefinitionNode defNode = new DefinitionNode(manager, node);
						DefinitionDTO definition = defNode.getValueObject();
						defNode.loadToCache(req);
						definitions.add(definition);
					}
					manager.OK(GraphDACParams.definition_nodes.name(), definitions, getParent());
				} else {
					manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODE_NOT_FOUND.name(),
							"Failed to get definition node", ResponseCode.RESOURCE_NOT_FOUND, getParent());
				}

			}

		} catch (Exception e) {
			throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODES_UNKNOWN_ERROR.name(), e.getMessage(),
					e);
		}
	}

	public void getNodesCount(Request req) {
		try {
			Request request = new Request(req);
			request.copyRequestValueObjects(req.getRequest());
			Response response = searchMgr.getNodesCount(request);
			manager.returnResponse(Futures.successful(response), getParent());
		} catch (Exception e) {
			throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODES_UNKNOWN_ERROR.name(), e.getMessage(),
					e);
		}
	}

	public void traverse(Request req) {
		try {
			Request request = new Request(req);
			request.copyRequestValueObjects(req.getRequest());
			Future<Object> response = Futures.successful(searchMgr.traverse(request));
			manager.returnResponse(response, getParent());
		} catch (Exception e) {
			throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_TRAVERSAL_UNKNOWN_ERROR.name(), e.getMessage(),
					e);
		}
	}

	public void traverseSubGraph(Request req) {
		try {
			Request request = new Request(req);
			request.copyRequestValueObjects(req.getRequest());
			Future<Object> response = Futures.successful(searchMgr.traverseSubGraph(request));
			manager.returnResponse(response, getParent());
		} catch (Exception e) {
			throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_TRAVERSAL_UNKNOWN_ERROR.name(), e.getMessage(),
					e);
		}
	}

	public void getSubGraph(Request req) {
		try {
			Request request = new Request(req);
			request.copyRequestValueObjects(req.getRequest());
			Future<Object> response = Futures.successful(searchMgr.getSubGraph(request));
			manager.returnResponse(response, getParent());
		} catch (Exception e) {
			throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_TRAVERSAL_UNKNOWN_ERROR.name(), e.getMessage(),
					e);
		}
	}

	public void importDefinitions(final Request request) {
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String json = (String) request.get(GraphEngineParams.input_stream.name());
		if (!manager.validateRequired(json)) {
			throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_SAVE_DEF_NODE_MISSING_REQ_PARAMS.name(),
					"Input JSON is blank");
		} else {
			ObjectMapper mapper = new ObjectMapper();
			GraphReader graphReader = null;
			try {
				graphReader = new JsonGraphReader(manager, mapper, graphId, json);
			} catch (Exception e) {
				throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_INVALID_REQUEST_FORMAT.name(),
						"Error! Invalid Request Format", e);
			}
			try {
				if (graphReader.getValidations().size() > 0) {
					String validations = mapper.writeValueAsString(graphReader.getValidations());
					throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_IMPORT_VALIDATION_FAILED.name(),
							validations);
				}
				ImportData inputData = new ImportData(graphReader.getDefinitionNodes(), graphReader.getDataNodes(),
						graphReader.getRelations(), graphReader.getTagMembersMap());
				final List<Node> nodes = inputData.getDefinitionNodes();
				if (null == nodes || nodes.isEmpty()) {
					manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_SAVE_DEF_NODE_MISSING_REQ_PARAMS.name(),
							"Definition nodes list is empty", ResponseCode.CLIENT_ERROR, getParent());
				} else {
					Map<String, List<String>> messageMap = new HashMap<String, List<String>>();
					final List<DefinitionNode> defNodes = new ArrayList<DefinitionNode>();
					for (Node node : nodes) {
						node.setGraphId(graphId);
						DefinitionNode defNode = new DefinitionNode(manager, node);
						defNodes.add(defNode);
						List<String> defNodeValidation = defNode.validateDefinitionNode();
						if (null != defNodeValidation && !defNodeValidation.isEmpty()) {
							messageMap.put(defNode.getNodeId(), defNodeValidation);
						}
					}
					if (null == messageMap || messageMap.isEmpty()) {
						final Request req = new Request(request);
						req.put(GraphDACParams.node_list.name(), nodes);

						Response response = nodeMgr.importNodes(req);
						if (manager.checkError(response)) {
							manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_SAVE_DEF_NODE_FAILED_TO_CREATE.name(),
									manager.getErrorMessage(response), response.getResponseCode(), getParent());
						} else {
							for (DefinitionNode defNode : defNodes) {
								defNode.loadToCache(request);
							}
							manager.OK(getParent());
						}
					} else {
						manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_SAVE_DEF_NODE_VALIDATION_FAILED.name(),
								"Definition nodes validation error", ResponseCode.CLIENT_ERROR,
								GraphDACParams.messages.name(), messageMap, getParent());
					}
				}
			} catch (Exception e) {
				throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_PROCESSING_ERROR.name(),
						"Error! Something went wrong while validating the request", e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void exportGraph(final Request request) {
		OutputStream outputStream = null;
		try {
			final String format = (String) request.get(GraphEngineParams.format.name());
			SearchCriteria sc = null;
			if (null != request.get(GraphEngineParams.search_criteria.name()))
				sc = (SearchCriteria) request.get(GraphEngineParams.search_criteria.name());

			Response nodesResponse = null;
			if (null == sc) {
				Request nodesReq = new Request(request);
				nodesResponse = searchMgr.getAllNodes(nodesReq);
			} else {
				Request nodesReq = new Request(request);
				nodesReq.put(GraphDACParams.search_criteria.name(), sc);
				nodesReq.put(GraphDACParams.get_tags.name(), true);
				nodesResponse = searchMgr.searchNodes(nodesReq);
			}

			Response relationsResponse = null;
			if (!StringUtils.equalsIgnoreCase(ImportType.CSV.name(), format)) {
				Request relationsReq = new Request(request);
				relationsResponse = searchMgr.getAllRelations(relationsReq);
			} else {
				Response blankResponse = new Response();
				relationsResponse = blankResponse;
			}

			if (manager.checkError(nodesResponse)) {
				String msg = manager.getErrorMessage(nodesResponse);
				if (StringUtils.isNotBlank(msg)) {
					manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_EXPORT_UNKNOWN_ERROR.name(), msg,
							nodesResponse.getResponseCode(), getParent());
				}
			}

			if (manager.checkError(relationsResponse)) {
				String msg = manager.getErrorMessage(relationsResponse);
				if (StringUtils.isNotBlank(msg)) {
					manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_EXPORT_UNKNOWN_ERROR.name(), msg,
							relationsResponse.getResponseCode(), getParent());
				}
			}

			List<Node> nodes = (List<Node>) nodesResponse.get(GraphDACParams.node_list.name());
			List<Relation> relations = (List<Relation>) relationsResponse.get(GraphDACParams.relations.name());
			outputStream = new ByteArrayOutputStream();
			outputStream = GraphWriterFactory.getData(format, nodes, relations);
			Response response = new Response();
			ResponseParams params = new ResponseParams();
			params.setErr("0");
			params.setStatus(StatusType.successful.name());
			params.setErrmsg("Operation successful");
			response.setParams(params);
			response.put(GraphEngineParams.output_stream.name(), new OutputStreamValue(outputStream));
			manager.returnResponse(Futures.successful(response), getParent());

		} catch (Exception e) {
			throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_EXPORT_UNKNOWN_ERROR.name(), e.getMessage(), e);
		} finally {
			try {
				if (null != outputStream)
					outputStream.close();
			} catch (IOException e) {
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, List<String>> getNodesValidationsFuture(Response defNodesResponse, final Response nodesResponse,
			final Request request) {

		final Map<String, List<String>> messages = new HashMap<String, List<String>>();
		messages.put(ERROR_MESSAGES, new ArrayList<String>());

		if (manager.checkError(defNodesResponse)) {
			messages.get(ERROR_MESSAGES).add(manager.getErrorMessage(defNodesResponse));
		} else {
			List<Node> defNodes = (List<Node>) defNodesResponse.get(GraphDACParams.node_list.name());
			final Map<String, Node> defNodeMap = new HashMap<String, Node>();
			if (null != defNodes && !defNodes.isEmpty()) {
				for (Node n : defNodes) {
					defNodeMap.put(n.getObjectType(), n);
				}
			}

			if (manager.checkError(nodesResponse)) {
				messages.get(ERROR_MESSAGES).add(manager.getErrorMessage(nodesResponse));
			} else {
				List<Node> nodes = (List<Node>) nodesResponse.get(GraphDACParams.node_list.name());
				if (null != nodes && !nodes.isEmpty()) {
					for (Node node : nodes) {
						try {
							DataNode datanode = new DataNode(getManager(), getGraphId(), node);
							List<String> validationMsgs = datanode.validateNode(defNodeMap);
							if (null != validationMsgs && !validationMsgs.isEmpty()) {
								List<String> list = messages.get(node.getIdentifier());
								if (null == list) {
									list = new ArrayList<String>();
									messages.put(node.getIdentifier(), list);
								}
								list.addAll(validationMsgs);
							}
						} catch (Exception e) {
							List<String> list = messages.get(node.getIdentifier());
							if (null == list) {
								list = new ArrayList<String>();
								messages.put(node.getIdentifier(), list);
							}
							list.add(e.getMessage());
						}
					}
				}
			}

		}

		return messages;
	}

	@SuppressWarnings("unchecked")
	private Map<String, List<String>> getRelationValidationsFuture(Response relationsResponse, final Request request) {
		final Map<String, List<String>> messages = new HashMap<String, List<String>>();
		messages.put(ERROR_MESSAGES, new ArrayList<String>());

		if (manager.checkError(relationsResponse)) {
			messages.get(ERROR_MESSAGES).add(manager.getErrorMessage(relationsResponse));
		} else {
			List<Relation> rels = (List<Relation>) relationsResponse.get(GraphDACParams.relations.name());
			if (null != rels && !rels.isEmpty()) {
				List<Map<String, List<String>>> msgFutures = new ArrayList<Map<String, List<String>>>();
				for (final Relation rel : rels) {
					try {
						IRelation iRel = RelationHandler.getRelation(getManager(), rel.getGraphId(),
								rel.getStartNodeId(), rel.getRelationType(), rel.getEndNodeId(), rel.getMetadata());
						Map<String, List<String>> validationMsgs = iRel.validateRelation(request);
						msgFutures.add(validationMsgs);
					} catch (Exception e) {
						List<String> list = messages.get(rel.getStartNodeId());
						if (null == list) {
							list = new ArrayList<String>();
							messages.put(rel.getStartNodeId(), list);
						}
						list.add(e.getMessage());
					}
				}

				if (null != msgFutures) {
					for (Map<String, List<String>> map : msgFutures) {
						if (null != map) {
							for (Entry<String, List<String>> entry : map.entrySet()) {
								List<String> list = messages.get(entry.getKey());
								if (null == list) {
									list = new ArrayList<String>();
									messages.put(entry.getKey(), list);
								}
								list.addAll(entry.getValue());
							}
						}
					}
				}
			}
		}

		return messages;
	}

	public void exportNode(Request req) {
		Request request = new Request(req);
		request.copyRequestValueObjects(req.getRequest());
		Response res = searchMgr.getNodeByUniqueId(request);

		if (manager.checkError(res)) {
			manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_EXPORT_NODE_UNKNOWN_ERROR.name(),
					manager.getErrorMessage(res), res.getResponseCode(), getParent());
		} else {
			Node node = (Node) res.get(GraphDACParams.node.name());
			if (null == node || StringUtils.isBlank(node.getNodeType())
					|| !StringUtils.equalsIgnoreCase(SystemNodeTypes.DATA_NODE.name(), node.getNodeType())) {
				manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_EXPORT_NODE_NOT_FOUND.name(), "Failed to export node",
						ResponseCode.RESOURCE_NOT_FOUND, getParent());
			} else {
				RDFGraphWriter rdfWriter = new RDFGraphWriter();
				try (InputStream is = rdfWriter.getRDF(node)) {
					manager.OK(GraphEngineParams.input_stream.name(), new InputStreamValue(is), getParent());
				} catch (Exception e) {
					throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_EXPORT_NODE_UNKNOWN_ERROR.name(),
							e.getMessage(), e);
				}
			}

		}
	}

	public void upsertRootNode(Request req) {
		try {
			Request request = new Request(req);
			request.copyRequestValueObjects(req.getRequest());
			Response response = nodeMgr.upsertRootNode(request);
			manager.returnResponse(Futures.successful(response), getParent());
		} catch (Exception e) {
			throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_ADD_NODE_UNKNOWN_ERROR.name(), e.getMessage(), e);
		}
	}

	public void getProxyNode(Request req) {
		try {
			Request request = new Request(req);
			request.copyRequestValueObjects(req.getRequest());
			Response res = searchMgr.getNodeByUniqueId(request);

			if (manager.checkError(res)) {
				manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_UNKNOWN_ERROR.name(), manager.getErrorMessage(res),
						res.getResponseCode(), getParent());
			} else {
				Node node = (Node) res.get(GraphDACParams.node.name());
				if (null == node || StringUtils.isBlank(node.getNodeType())
						|| !StringUtils.equalsIgnoreCase(SystemNodeTypes.PROXY_NODE.name(), node.getNodeType())) {
					manager.ERROR(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODE_NOT_FOUND.name(),
							"Failed to get proxy node", ResponseCode.RESOURCE_NOT_FOUND, getParent());
				} else {
					manager.OK(GraphDACParams.node.name(), node, getParent());
				}
			}

		} catch (Exception e) {
			throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODES_UNKNOWN_ERROR.name(), e.getMessage(),
					e);
		}
	}

	@SuppressWarnings("unchecked")
	public void bulkUpdateNodes(Request request) {
		try {
			List<Node> nodes = (List<Node>) request.get(GraphDACParams.nodes.name());
			List<Map<String, Object>> newNodes = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> modifiedNodes = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> addOutRelations = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> removeOutRelations = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> addInRelations = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> removeInRelations = new ArrayList<Map<String, Object>>();
			for (Node node : nodes) {
				Map<String, Object> map = new HashMap<String, Object>();
				Boolean isNew = (Boolean) node.getMetadata().get("isNew");
				if (BooleanUtils.isTrue(isNew)) {
					node.getMetadata().remove("isNew");
					map.put(SystemProperties.IL_UNIQUE_ID.name(), node.getIdentifier());
					map.put(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), node.getObjectType());
					map.put(SystemProperties.IL_SYS_NODE_TYPE.name(), SystemNodeTypes.DATA_NODE.name());
					map.putAll(node.getMetadata());
					newNodes.add(map);
				} else {
					if (null != node.getMetadata() && !node.getMetadata().isEmpty()) {
						map.put(SystemProperties.IL_UNIQUE_ID.name(), node.getIdentifier());
						map.put("metadata", node.getMetadata());
						modifiedNodes.add(map);
					}
				}
				if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
					for (Relation rel : node.getOutRelations()) {
						if (StringUtils.isNotBlank(rel.getEndNodeId())) {
							Map<String, Object> relation = new HashMap<String, Object>();
							relation.put("from", node.getIdentifier());
							relation.put("to", rel.getEndNodeId());
							relation.put("type", rel.getRelationType());
							relation.put("metadata",
									null == rel.getMetadata() ? new HashMap<String, Object>() : rel.getMetadata());
							addOutRelations.add(relation);
						}
						if (StringUtils.isNotBlank(rel.getEndNodeObjectType())) {
							Map<String, Object> relation = new HashMap<String, Object>();
							relation.put(SystemProperties.IL_UNIQUE_ID.name(), node.getIdentifier());
							relation.put("type", rel.getRelationType());
							relation.put("objectType", rel.getEndNodeObjectType());
							removeOutRelations.add(relation);
						}
					}
				}
				if (null != node.getInRelations() && !node.getInRelations().isEmpty()) {
					for (Relation rel : node.getInRelations()) {
						if (StringUtils.isNotBlank(rel.getStartNodeId())) {
							Map<String, Object> relation = new HashMap<String, Object>();
							relation.put("from", rel.getStartNodeId());
							relation.put("to", node.getIdentifier());
							relation.put("type", rel.getRelationType());
							relation.put("metadata",
									null == rel.getMetadata() ? new HashMap<String, Object>() : rel.getMetadata());
							addInRelations.add(relation);
						}
						if (StringUtils.isNotBlank(rel.getStartNodeObjectType())) {
							Map<String, Object> relation = new HashMap<String, Object>();
							relation.put(SystemProperties.IL_UNIQUE_ID.name(), node.getIdentifier());
							relation.put("type", rel.getRelationType());
							relation.put("objectType", rel.getStartNodeObjectType());
							removeInRelations.add(relation);
						}
					}
				}
			}

			request.put(GraphDACParams.newNodes.name(), newNodes);
			request.put(GraphDACParams.modifiedNodes.name(), modifiedNodes);
			request.put(GraphDACParams.addedOutRelations.name(), addOutRelations);
			request.put(GraphDACParams.removedOutRelations.name(), removeOutRelations);
			request.put(GraphDACParams.addedInRelations.name(), addInRelations);
			request.put(GraphDACParams.removedInRelations.name(), removeInRelations);
			Future<Object> response = Futures.successful(graphMgr.bulkUpdateNodes(request));
			manager.returnResponse(response, getParent());
		} catch (Exception e) {
			throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_CREATE_RELATION_NODE_FAILED.name(),
					e.getMessage(), e);
		}
	}

}
