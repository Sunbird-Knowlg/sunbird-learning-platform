package org.sunbird.graph.service.operation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Property;
import org.sunbird.common.dto.Request;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.MiddlewareException;
import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.cache.mgr.impl.NodeCacheManager;
import org.sunbird.graph.cache.util.RedisStoreUtil;
import org.sunbird.graph.common.DateUtils;
import org.sunbird.graph.common.Identifier;
import org.sunbird.graph.dac.enums.AuditProperties;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.GraphDacErrorParams;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.enums.SystemProperties;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.service.common.CypherQueryConfigurationConstants;
import org.sunbird.graph.service.common.DACConfigurationConstants;
import org.sunbird.graph.service.common.DACErrorCodeConstants;
import org.sunbird.graph.service.common.DACErrorMessageConstants;
import org.sunbird.graph.service.common.GraphOperation;
import org.sunbird.graph.service.request.validator.Neo4jBoltValidator;
import org.sunbird.graph.service.util.DriverUtil;
import org.sunbird.graph.service.util.NodeQueryGenerationUtil;
import org.sunbird.telemetry.logger.TelemetryManager;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Value;

public class Neo4JBoltNodeOperations {

	private final static String DEFAULT_CYPHER_NODE_OBJECT = "ee";
	private static Neo4jBoltValidator versionValidator = new Neo4jBoltValidator();

	@SuppressWarnings("unchecked")
	public static Node upsertNode(String graphId, Node node, Request request) {

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | [Upsert Node Operation Failed.]");

		if (null == node)
			throw new ClientException(DACErrorCodeConstants.INVALID_NODE.name(),
					DACErrorMessageConstants.INVALID_NODE + " | [Upsert Node Operation Failed.]");

		TelemetryManager.log("Applying the Consumer Authorization Check for Node Id: " + node.getIdentifier());
		setRequestContextToNode(node, request);
		validateAuthorization(graphId, node, request);
		TelemetryManager.log("Consumer is Authorized for Node Id: " + node.getIdentifier());

		TelemetryManager.log("Validating the Update Operation for Node Id: " + node.getIdentifier());
		versionValidator.validateUpdateOperation(graphId, node);
		node.getMetadata().remove(GraphDACParams.versionKey.name());
		TelemetryManager.log("Node Update Operation has been Validated for Node Id: " + node.getIdentifier());

		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.WRITE);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			TelemetryManager.log("Session Initialised. | [Graph Id: " + graphId + "]");
			node.setGraphId(graphId);
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.node.name(), node);
			parameterMap.put(GraphDACParams.request.name(), request);

			NodeQueryGenerationUtil.generateUpsertNodeCypherQuery(parameterMap);
			Map<String, Object> queryMap = (Map<String, Object>) parameterMap
					.get(GraphDACParams.queryStatementMap.name());
			for (Entry<String, Object> entry : queryMap.entrySet()) {
				String statementTemplate = StringUtils.removeEnd(
						(String) ((Map<String, Object>) entry.getValue()).get(GraphDACParams.query.name()),
						CypherQueryConfigurationConstants.COMMA);
				Map<String, Object> statementParameters = (Map<String, Object>) ((Map<String, Object>) entry.getValue())
						.get(GraphDACParams.paramValueMap.name());
				try (Transaction tx = session.beginTransaction()) {
					// StatementResult result = session.run(statementTemplate, statementParameters);
					StatementResult result = tx.run(statementTemplate, statementParameters);
					tx.success();
					for (Record record : result.list()) {
						try {
							org.neo4j.driver.v1.types.Node neo4JNode = record.get(DEFAULT_CYPHER_NODE_OBJECT).asNode();
							String versionKey = (String) neo4JNode.get(GraphDACParams.versionKey.name()).asString();
							String identifier = (String) neo4JNode.get(SystemProperties.IL_UNIQUE_ID.name()).asString();
							node.setIdentifier(identifier);
							if (StringUtils.isNotBlank(versionKey))
								node.getMetadata().put(GraphDACParams.versionKey.name(), versionKey);
							try {
								updateRedisCache(graphId, neo4JNode, node.getIdentifier(), node.getNodeType());
							} catch (Exception e) {
								throw new ServerException(DACErrorCodeConstants.CACHE_ERROR.name(),
										DACErrorMessageConstants.CACHE_ERROR + " | " + e.getMessage());
							}
						} catch (Exception e) {
							//suppress exception happened when versionKey is null
						}
					}
				}
			}
		} catch (Exception e) {
			if (!(e instanceof MiddlewareException)) {
				throw new ServerException(DACErrorCodeConstants.CONNECTION_PROBLEM.name(),
						DACErrorMessageConstants.CONNECTION_PROBLEM + " | " + e.getMessage());
			} else {
				throw e;
			}
		}
		return node;
	}

	@SuppressWarnings("unchecked")
	public static Node addNode(String graphId, Node node, Request request) {

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | [Create Node Operation Failed.]");

		if (null == node)
			throw new ClientException(DACErrorCodeConstants.INVALID_NODE.name(),
					DACErrorMessageConstants.INVALID_NODE + " | [Create Node Operation Failed.]");

		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.WRITE);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			TelemetryManager.log("Session Initialised. | [Graph Id: " + graphId + "]");
			node.setGraphId(graphId);

			setRequestContextToNode(node, request);

			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.node.name(), node);
			parameterMap.put(GraphDACParams.request.name(), request);

			NodeQueryGenerationUtil.generateCreateNodeCypherQuery(parameterMap);

			Map<String, Object> queryMap = (Map<String, Object>) parameterMap
					.get(GraphDACParams.queryStatementMap.name());
			for (Entry<String, Object> entry : queryMap.entrySet()) {
				String statementTemplate = StringUtils.removeEnd(
						(String) ((Map<String, Object>) entry.getValue()).get(GraphDACParams.query.name()),
						CypherQueryConfigurationConstants.COMMA);
				Map<String, Object> statementParameters = (Map<String, Object>) ((Map<String, Object>) entry.getValue())
						.get(GraphDACParams.paramValueMap.name());
				try (Transaction tx = session.beginTransaction()) {
					// StatementResult result = session.run(statementTemplate, statementParameters);
					StatementResult result = tx.run(statementTemplate, statementParameters);
					tx.success();
					for (Record record : result.list()) {
						try {
							org.neo4j.driver.v1.types.Node neo4JNode = record.get(DEFAULT_CYPHER_NODE_OBJECT).asNode();
							String versionKey = (String) neo4JNode.get(GraphDACParams.versionKey.name()).asString();
							String identifier = (String) neo4JNode.get(SystemProperties.IL_UNIQUE_ID.name()).asString();
							node.setIdentifier(identifier);
							if (StringUtils.isNotBlank(versionKey))
								node.getMetadata().put(GraphDACParams.versionKey.name(), versionKey);
							try {
								updateRedisCache(graphId, neo4JNode, node.getIdentifier(), node.getNodeType());
							} catch (Exception e) {
								throw new ServerException(DACErrorCodeConstants.CACHE_ERROR.name(),
										DACErrorMessageConstants.CACHE_ERROR + " | " + e.getMessage());
							}
						} catch (Exception e) {
							//suppress exception happened when versionKey is null
						}
					}

				} catch (Exception e) {
					if (e instanceof org.neo4j.driver.v1.exceptions.ClientException) {
						org.neo4j.driver.v1.exceptions.ClientException ex = (org.neo4j.driver.v1.exceptions.ClientException) e;
						if (StringUtils.equalsIgnoreCase("Neo.ClientError.Schema.ConstraintValidationFailed", ex.code()))
							throw new ClientException(GraphDacErrorParams.CONSTRAINT_VALIDATION_FAILED.name(),
								"Object already exists with identifier: " + node.getIdentifier());
						else
							throw new ServerException(ex.code(), e.getMessage());
					}
						
				}
			}
		} catch (Exception e) {
			if (!(e instanceof MiddlewareException)) {
				throw new ServerException(DACErrorCodeConstants.CONNECTION_PROBLEM.name(),
						DACErrorMessageConstants.CONNECTION_PROBLEM + " | " + e.getMessage());
			} else {
				throw e;
			}
		}
		return node;
	}

	@SuppressWarnings("unchecked")
	public static Node updateNode(String graphId, Node node, Request request) {

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | [Update Node Operation Failed.]");
		if (null == node)
			throw new ClientException(DACErrorCodeConstants.INVALID_NODE.name(),
					DACErrorMessageConstants.INVALID_NODE + " | [Update Node Operation Failed.]");

		TelemetryManager.log("Applying the Consumer Authorization Check for Node Id: " + node.getIdentifier());
		validateAuthorization(graphId, node, request);
		node.getMetadata().remove(GraphDACParams.consumerId.name());
		TelemetryManager.log("Consumer is Authorized for Node Id: " + node.getIdentifier());

		TelemetryManager.log("Validating the Update Operation for Node Id: " + node.getIdentifier());
		versionValidator.validateUpdateOperation(graphId, node);
		String version = (String) node.getMetadata().get(GraphDACParams.versionKey.name());
		if (!StringUtils.equalsIgnoreCase(
				Platform.config.getString(DACConfigurationConstants.PASSPORT_KEY_BASE_PROPERTY), version)) {
			node.getMetadata().remove(GraphDACParams.versionKey.name());
		}
		TelemetryManager.log("Node Update Operation has been Validated for Node Id: " + node.getIdentifier());

		// Adding Consumer and App Id
		setRequestContextToNode(node, request, GraphDACParams.UPDATE.name());

		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.WRITE);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {

			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.node.name(), node);
			parameterMap.put(GraphDACParams.request.name(), request);

			NodeQueryGenerationUtil.generateUpdateNodeCypherQuery(parameterMap);
			Map<String, Object> queryMap = (Map<String, Object>) parameterMap
					.get(GraphDACParams.queryStatementMap.name());
			for (Entry<String, Object> entry : queryMap.entrySet()) {
				String statementTemplate = StringUtils.removeEnd(
						(String) ((Map<String, Object>) entry.getValue()).get(GraphDACParams.query.name()),
						CypherQueryConfigurationConstants.COMMA);
				Map<String, Object> statementParameters = (Map<String, Object>) ((Map<String, Object>) entry.getValue())
						.get(GraphDACParams.paramValueMap.name());
				try (Transaction tx = session.beginTransaction()) {
					// StatementResult result = session.run(statementTemplate, statementParameters);
					StatementResult result = tx.run(statementTemplate, statementParameters);
					tx.success();
					if (null == result || !result.hasNext())
						throw new ResourceNotFoundException(DACErrorCodeConstants.NOT_FOUND.name(),
								DACErrorMessageConstants.NODE_NOT_FOUND + " | " + node.getIdentifier(),
								node.getIdentifier());
					for (Record record : result.list()) {
						try {
							org.neo4j.driver.v1.types.Node neo4JNode = record.get(DEFAULT_CYPHER_NODE_OBJECT).asNode();

							String versionKey = (String) neo4JNode.get(GraphDACParams.versionKey.name()).asString();
							String identifier = (String) neo4JNode.get(SystemProperties.IL_UNIQUE_ID.name()).asString();
							node.setIdentifier(identifier);
							if (StringUtils.isNotBlank(versionKey))
								node.getMetadata().put(GraphDACParams.versionKey.name(), versionKey);
							try {
								updateRedisCache(graphId, neo4JNode, node.getIdentifier(), node.getNodeType());
							} catch (Exception e) {
								throw new ServerException(DACErrorCodeConstants.CACHE_ERROR.name(),
										DACErrorMessageConstants.CACHE_ERROR + " | " + e.getMessage());
							}
						} catch (Exception e) {
							//suppress exception happened when versionKey is null
						}
					}
				}
			}

		} catch (Exception e) {
			if (!(e instanceof MiddlewareException)) {
				throw new ServerException(DACErrorCodeConstants.CONNECTION_PROBLEM.name(),
						DACErrorMessageConstants.CONNECTION_PROBLEM + " | " + e.getMessage());
			} else {
				throw e;
			}
		}
		return node;
	}

	public static void importNodes(String graphId, List<Node> nodes, Request request) {

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | [Import Nodes Operation Failed.]");
		if (null == nodes || nodes.size() <= 0)
			throw new ClientException(DACErrorCodeConstants.INVALID_NODE.name(),
					DACErrorMessageConstants.INVALID_NODE_LIST + " | [Import Nodes Operation Failed.]");
		for (Node node : nodes) {
			if (null == node.getMetadata())
				node.setMetadata(new HashMap<String, Object>());
			node.getMetadata().put(SystemProperties.IL_UNIQUE_ID.name(), node.getIdentifier());
			node.getMetadata().put(SystemProperties.IL_SYS_NODE_TYPE.name(), node.getNodeType());
			if (StringUtils.isNotBlank(node.getObjectType()))
				node.getMetadata().put(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), node.getObjectType());
			upsertNode(graphId, node, request);
		}
	}

	public static void updatePropertyValue(String graphId, String nodeId, Property property, Request request) {

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | [Update Property Value Operation Failed.]");

		if (StringUtils.isBlank(nodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_IDENTIFIER + " | [Update Property Value Operation Failed.]");

		if (null == property)
			throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
					DACErrorMessageConstants.INVALID_PROPERTY + " | [Update Property Value Operation Failed.]");

		// CHECK - write driver and session created for this - END
		TelemetryManager.log("Session Initialised. | [Graph Id: " + graphId + "]");
		String date = DateUtils.formatCurrentDate();
		Node node = new Node();
		node.setGraphId(graphId);
		node.setIdentifier(nodeId);
		node.setMetadata(new HashMap<String, Object>());
		node.getMetadata().put(property.getPropertyName(), property.getPropertyValue());
		node.getMetadata().put(AuditProperties.lastUpdatedOn.name(), date);
		if (!StringUtils.isBlank(date))
			node.getMetadata().put(GraphDACParams.versionKey.name(), Long.toString(DateUtils.parse(date).getTime()));
		updateNode(graphId, node, request);
		// CHECK - write driver and session created for this - END
	}

	public static void updatePropertyValues(String graphId, String nodeId, Map<String, Object> metadata,
			Request request) {
		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | [Update Property Value Operation Failed.]");

		if (StringUtils.isBlank(nodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_IDENTIFIER + " | [Update Property Value Operation Failed.]");

		if (null == metadata)
			throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
					DACErrorMessageConstants.INVALID_PROPERTY + " | [Update Property Value Operation Failed.]");

		// CHECK - write driver and session created for this - START
		String date = DateUtils.formatCurrentDate();
		Node node = new Node();
		node.setGraphId(graphId);
		node.setIdentifier(nodeId);
		node.setMetadata(new HashMap<String, Object>());
		node.getMetadata().putAll(metadata);
		node.getMetadata().put(AuditProperties.lastUpdatedOn.name(), date);
		if (!StringUtils.isBlank(date))
			node.getMetadata().put(GraphDACParams.versionKey.name(), Long.toString(DateUtils.parse(date).getTime()));
		updateNode(graphId, node, request);
		// CHECK - write driver and session created for this - END
	}

	public static void removePropertyValue(String graphId, String nodeId, String key, Request request) {

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | [Remove Property Value Operation Failed.]");

		if (StringUtils.isBlank(nodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_IDENTIFIER + " | [Remove Property Value Operation Failed.]");

		if (StringUtils.isBlank(key))
			throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
					DACErrorMessageConstants.INVALID_PROPERTY + " | [Remove Property Value Operation Failed.]");

		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.WRITE);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.nodeId.name(), nodeId);
			parameterMap.put(GraphDACParams.key.name(), key);
			parameterMap.put(GraphDACParams.request.name(), request);

			try (Transaction tx = session.beginTransaction()) {
				StatementResult result = tx
						.run(NodeQueryGenerationUtil.generateRemovePropertyValueCypherQuery(parameterMap));
				tx.success();
				for (Record record : result.list())
					TelemetryManager.log("Remove Property Value Operation | ", record.asMap());
			}

			NodeCacheManager.deleteDataNode(graphId, nodeId);

		} catch (Exception e) {
			throw new ServerException(DACErrorCodeConstants.CONNECTION_PROBLEM.name(),
					DACErrorMessageConstants.CONNECTION_PROBLEM + " | " + e.getMessage());
		}
	}

	public static void removePropertyValues(String graphId, String nodeId, List<String> keys, Request request) {

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | [Remove Property Values Operation Failed.]");

		if (StringUtils.isBlank(nodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_IDENTIFIER + " | [Remove Property Values Operation Failed.]");

		if (null == keys || keys.size() <= 0)
			throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
					DACErrorMessageConstants.INVALID_PROPERTY + " | [Remove Property Values Operation Failed.]");

		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.WRITE);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.nodeId.name(), nodeId);
			parameterMap.put(GraphDACParams.keys.name(), keys);
			parameterMap.put(GraphDACParams.request.name(), request);

			try (Transaction tx = session.beginTransaction()) {
				StatementResult result = tx
						.run(NodeQueryGenerationUtil.generateRemovePropertyValuesCypherQuery(parameterMap));
				tx.success();
				for (Record record : result.list())
					TelemetryManager.log("Update Property Values Operation | ", record.asMap());
			}

			NodeCacheManager.deleteDataNode(graphId, nodeId);
		} catch (Exception e) {
			throw new ServerException(DACErrorCodeConstants.CONNECTION_PROBLEM.name(),
					DACErrorMessageConstants.CONNECTION_PROBLEM + " | " + e.getMessage());
		}
	}

	public static void deleteNode(String graphId, String nodeId, Request request) {

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | [Remove Property Values Operation Failed.]");

		if (StringUtils.isBlank(nodeId))
			throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
					DACErrorMessageConstants.INVALID_IDENTIFIER + " | [Remove Property Values Operation Failed.]");

		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.WRITE);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.nodeId.name(), nodeId);
			parameterMap.put(GraphDACParams.request.name(), request);

			try (Transaction tx = session.beginTransaction()) {
				StatementResult result = tx.run(NodeQueryGenerationUtil.generateDeleteNodeCypherQuery(parameterMap));
				tx.success();
				for (Record record : result.list())
					TelemetryManager.log("Delete Node Operation | ", record.asMap());
			}

			NodeCacheManager.deleteDataNode(graphId, nodeId);
			try {
				RedisStoreUtil.deleteNodeProperties(graphId, nodeId);
			} catch (Exception e) {
			}

		} catch (Exception e) {
			throw new ServerException(DACErrorCodeConstants.CONNECTION_PROBLEM.name(),
					DACErrorMessageConstants.CONNECTION_PROBLEM + " | " + e.getMessage());
		}
	}

	public static Node upsertRootNode(String graphId, Request request) {

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | [Upsert Root Node Operation Failed.]");

		Node node = new Node();
		node.setMetadata(new HashMap<String, Object>());
		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.WRITE);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			TelemetryManager.log("Session Initialised. | [Graph Id: " + graphId + "]");

			// Generating Root Node Id
			String rootNodeUniqueId = Identifier.getIdentifier(graphId, SystemNodeTypes.ROOT_NODE.name());
			TelemetryManager.log("Generated Root Node Id: " + rootNodeUniqueId);

			node.setGraphId(graphId);
			node.setNodeType(SystemNodeTypes.ROOT_NODE.name());
			node.setIdentifier(rootNodeUniqueId);
			node.getMetadata().put(SystemProperties.IL_UNIQUE_ID.name(), rootNodeUniqueId);
			node.getMetadata().put(SystemProperties.IL_SYS_NODE_TYPE.name(), SystemNodeTypes.ROOT_NODE.name());
			node.getMetadata().put(AuditProperties.createdOn.name(), DateUtils.formatCurrentDate());
			node.getMetadata().put(GraphDACParams.nodesCount.name(), 0);
			node.getMetadata().put(GraphDACParams.relationsCount.name(), 0);

			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(GraphDACParams.graphId.name(), graphId);
			parameterMap.put(GraphDACParams.rootNode.name(), node);
			parameterMap.put(GraphDACParams.request.name(), request);

			try (Transaction tx = session.beginTransaction()) {
				StatementResult result = tx
						.run(NodeQueryGenerationUtil.generateUpsertRootNodeCypherQuery(parameterMap));
				tx.success();
				for (Record record : result.list())
					TelemetryManager.log("Upsert Root Node Operation | ", record.asMap());
			}
		} catch (Exception e) {
			throw new ServerException(DACErrorCodeConstants.CONNECTION_PROBLEM.name(),
					DACErrorMessageConstants.CONNECTION_PROBLEM + " | " + e.getMessage());
		}
		return node;
	}

	static Object convert(Value value) {
		switch (value.type().name()) {
		case "PATH":
			return value.asList(Neo4JBoltNodeOperations::convert);
		case "NODE":
			return value.asNode();
		case "RELATIONSHIP":
			return value.asRelationship();
		}
		return value.asObject();
	}

	private static void setRequestContextToNode(Node node, Request request, String operation) {
		if(!StringUtils.equalsIgnoreCase(operation, GraphDACParams.UPDATE.name()))
			setRequestContextToNode(node, request);
		else
			if (null != request && null != request.getContext()) {
				String consumerId = (String) request.getContext().get(GraphDACParams.CONSUMER_ID.name());
				TelemetryManager.log("ConsumerId from request: " + consumerId + " for content: " + node.getIdentifier());
				if (StringUtils.isNotBlank(consumerId))
					node.getMetadata().put(GraphDACParams.consumerId.name(), consumerId);

				String appId = (String) request.getContext().get(GraphDACParams.APP_ID.name());
				TelemetryManager.log("App Id from request: " + appId + " for content: " + node.getIdentifier());
				if (StringUtils.isNotBlank(appId))
					node.getMetadata().put(GraphDACParams.appId.name(), appId);
			}
	}
	
	private static void setRequestContextToNode(Node node, Request request) {
		if (null != request && null != request.getContext()) {
			String channel = (String) request.getContext().get(GraphDACParams.CHANNEL_ID.name());
			TelemetryManager.log("Channel from request: " + channel + " for content: " + node.getIdentifier());
			if (StringUtils.isNotBlank(channel))
				node.getMetadata().put(GraphDACParams.channel.name(), channel);

			String consumerId = (String) request.getContext().get(GraphDACParams.CONSUMER_ID.name());
			TelemetryManager.log("ConsumerId from request: " + consumerId + " for content: " + node.getIdentifier());
			if (StringUtils.isNotBlank(consumerId))
				node.getMetadata().put(GraphDACParams.consumerId.name(), consumerId);

			String appId = (String) request.getContext().get(GraphDACParams.APP_ID.name());
			TelemetryManager.log("App Id from request: " + appId + " for content: " + node.getIdentifier());
			if (StringUtils.isNotBlank(appId))
				node.getMetadata().put(GraphDACParams.appId.name(), appId);
		}
	}

	private static void validateAuthorization(String graphId, Node node, Request request) {
		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | [Invalid or 'null' Graph Id.]");
		if (null == node)
			throw new ClientException(DACErrorCodeConstants.INVALID_NODE.name(),
					DACErrorMessageConstants.INVALID_NODE + " | [Invalid or 'null' Node.]");
		if (null == request)
			throw new ClientException(DACErrorCodeConstants.INVALID_REQUEST.name(),
					DACErrorMessageConstants.INVALID_REQUEST + " | [Invalid or 'null' Request Object.]");
	}


	private static void updateRedisCache(String graphId, org.neo4j.driver.v1.types.Node neo4JNode, String nodeId,
										 String nodeType) {

		if (!graphId.equalsIgnoreCase("domain"))
			return;

		if (!nodeType.equalsIgnoreCase(SystemNodeTypes.DATA_NODE.name()))
			return;

		Map<String, Object> cacheMap = new HashMap<>();
		if (StringUtils.isNotBlank(neo4JNode.get(GraphDACParams.versionKey.name()).asString()))
			cacheMap.put(GraphDACParams.versionKey.name(), neo4JNode.get(GraphDACParams.versionKey.name()).asString());

		if (cacheMap.size() > 0)
			RedisStoreUtil.saveNodeProperties(graphId, nodeId, cacheMap);
		NodeCacheManager.deleteDataNode(graphId, nodeId);
	}

}
