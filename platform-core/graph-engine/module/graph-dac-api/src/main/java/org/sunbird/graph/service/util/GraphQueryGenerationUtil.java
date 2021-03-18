package org.sunbird.graph.service.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Request;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.common.DateUtils;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.RelationTypes;
import org.sunbird.graph.dac.enums.SystemProperties;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.service.common.CypherQueryConfigurationConstants;
import org.sunbird.graph.service.common.DACErrorCodeConstants;
import org.sunbird.graph.service.common.DACErrorMessageConstants;
import org.sunbird.graph.service.common.GraphOperation;
import org.sunbird.graph.service.common.RelationshipDirection;
import org.sunbird.telemetry.logger.TelemetryManager;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.driver.v1.types.Relationship;

public class GraphQueryGenerationUtil extends BaseQueryGenerationUtil {

	public static String generateCreateUniqueConstraintCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Create Graph Unique Contraint' Operation Failed.]");

			String indexProperty = (String) parameterMap.get(GraphDACParams.indexProperty.name());
			if (StringUtils.isBlank(indexProperty))
				throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
						DACErrorMessageConstants.INVALID_INDEX_PROPERTY_KEY_LIST
								+ " | ['Create Graph Unique Contraint' Operation Failed.]");

			query.append("CREATE CONSTRAINT ON (n:" + graphId + ") ASSERT n." + indexProperty + " IS UNIQUE")
					.append(CypherQueryConfigurationConstants.BLANK_SPACE);
		}

		TelemetryManager.log("Returning Create Unique Constraint Cypher Query: " + query);
		return query.toString();
	}

	public static String generateCreateIndexCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Create Graph Index' Query Generation Failed.]");

			String indexProperty = (String) parameterMap.get(GraphDACParams.indexProperty.name());
			if (StringUtils.isBlank(indexProperty))
				throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
						DACErrorMessageConstants.INVALID_INDEX_PROPERTY_KEY_LIST
								+ " | ['Create Graph Index' Operation Failed.]");

			query.append("CREATE INDEX ON :" + graphId + "(" + indexProperty + ")")
					.append(CypherQueryConfigurationConstants.BLANK_SPACE);
		}

		TelemetryManager.log("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

	public static String generateDeleteGraphCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Delete Graph' Query Generation Failed.]");
			query.append("MATCH (n) REMOVE n:" + graphId);

		}

		TelemetryManager.log("Returning Create Node Cypher Query: " + query);
		return query.toString();
	}

	@SuppressWarnings("unchecked")
	public static String generateCreateRelationCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		try {
			if (null != parameterMap) {
				String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
				if (StringUtils.isBlank(graphId))
					throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
							DACErrorMessageConstants.INVALID_GRAPH_ID
									+ " | ['Create Relation' Query Generation Failed.]");

				String startNodeId = (String) parameterMap.get(GraphDACParams.startNodeId.name());
				if (StringUtils.isBlank(startNodeId))
					throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
							DACErrorMessageConstants.INVALID_START_NODE_ID
									+ " | ['Create Relation' Query Generation Failed.]");

				String endNodeId = (String) parameterMap.get(GraphDACParams.endNodeId.name());
				if (StringUtils.isBlank(endNodeId))
					throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
							DACErrorMessageConstants.INVALID_END_NODE_ID
									+ " | ['Create Relation' Query Generation Failed.]");

				String relationType = (String) parameterMap.get(GraphDACParams.relationType.name());
				if (StringUtils.isBlank(relationType))
					throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
							DACErrorMessageConstants.INVALID_RELATION_TYPE
									+ " | ['Create Relation' Query Generation Failed.]");
				Integer index = null;
				Request request = (Request) parameterMap.get(GraphDACParams.request.name());
				Map<String, Object> requestMetadata = (Map<String, Object>) request.get(GraphDACParams.metadata.name());
				if (null != requestMetadata) {
					try {
						Object obj = requestMetadata.get(SystemProperties.IL_SEQUENCE_INDEX.name());
						if (null != obj)
							index = Integer.parseInt(obj.toString());
					} catch (Exception e) {
					}
				}
					
				Map<String, Object> metadata = new HashMap<String, Object>();
				if (null == index && StringUtils.equalsIgnoreCase(RelationTypes.SEQUENCE_MEMBERSHIP.relationName(),
						relationType)) {
					index = 0;
					TelemetryManager.log("Given Relation: " + "'SEQUENCE_MEMBERSHIP' | [Graph Id: " + graphId + "]");
					// Fetch all the Relationships
					List<Integer> allottedIndices = new ArrayList<Integer>();
					allottedIndices.add(0);
					List<Relationship> relationships = getAllRelationships(graphId, startNodeId,
							RelationshipDirection.OUTGOING);
					for (Relationship relationship : relationships) {
						if (StringUtils.equalsIgnoreCase(relationship.type(),
								RelationTypes.SEQUENCE_MEMBERSHIP.relationName())) {
							try {
								Object strIndex = relationship.get(SystemProperties.IL_SEQUENCE_INDEX.name())
										.asObject();
								if (null != strIndex)
									allottedIndices.add(Integer.parseInt(strIndex.toString()));
							} catch (Exception e) {
							}
						}
					}
					index = Collections.max(allottedIndices) + 1;
				}

				if (null != request)
					metadata = (Map<String, Object>) request.get(GraphDACParams.metadata.name());

				query.append(getCreateRelationCypherQuery(graphId, startNodeId, endNodeId, relationType,
						CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT,
						CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT_II, metadata,
						RelationshipDirection.OUTGOING, index, parameterMap));
			}
		} catch (Exception e) {
			throw new ServerException(DACErrorCodeConstants.SERVER_ERROR.name(),
					"Error! Something went wrong wile generating the query for relation creation.", e);
		}

		return query.toString();
	}

	@SuppressWarnings("unchecked")
	public static String generateUpdateRelationCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Update Relation' Query Generation Failed.]");

			String startNodeId = (String) parameterMap.get(GraphDACParams.startNodeId.name());
			if (StringUtils.isBlank(startNodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_START_NODE_ID
								+ " | ['Update Relation' Query Generation Failed.]");

			String endNodeId = (String) parameterMap.get(GraphDACParams.endNodeId.name());
			if (StringUtils.isBlank(endNodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_END_NODE_ID
								+ " | ['Update Relation' Query Generation Failed.]");

			String relationType = (String) parameterMap.get(GraphDACParams.relationType.name());
			if (StringUtils.isBlank(relationType))
				throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
						DACErrorMessageConstants.INVALID_RELATION_TYPE
								+ " | ['Update Relation' Query Generation Failed.]");

			Request request = (Request) parameterMap.get(GraphDACParams.request.name());
			if (null == request)
				throw new ClientException(DACErrorCodeConstants.INVALID_REQUEST.name(),
						DACErrorMessageConstants.INVALID_REQUEST + " | ['Update Relation' Query Generation Failed.]");

			Map<String, Object> metadata = (Map<String, Object>) request.get(GraphDACParams.metadata.name());
			query.append(getUpdateRelationCypherQuery(graphId, startNodeId, endNodeId, relationType,
					CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT,
					CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT_II, metadata,
					RelationshipDirection.OUTGOING, parameterMap));
		}

		return query.toString();
	}

	public static String generateDeleteRelationCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Delete Relation' Query Generation Failed.]");

			String startNodeId = (String) parameterMap.get(GraphDACParams.startNodeId.name());
			if (StringUtils.isBlank(startNodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_START_NODE_ID
								+ " | ['Delete Relation' Query Generation Failed.]");

			String endNodeId = (String) parameterMap.get(GraphDACParams.endNodeId.name());
			if (StringUtils.isBlank(endNodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_END_NODE_ID
								+ " | ['Delete Relation' Query Generation Failed.]");

			String relationType = (String) parameterMap.get(GraphDACParams.relationType.name());
			if (StringUtils.isBlank(relationType))
				throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
						DACErrorMessageConstants.INVALID_RELATION_TYPE
								+ " | ['Delete Relation' Query Generation Failed.]");

			query.append(getDeleteRelationCypherQuery(graphId, startNodeId, endNodeId, relationType,
					CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT,
					CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT_II, RelationshipDirection.OUTGOING));
		}

		TelemetryManager.log("'Delete Relation' Cypher Query: " + query);
		return query.toString();
	}

	@SuppressWarnings("unchecked")
	public static String generateCreateIncomingRelationCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Create Incoming Relations' Query Generation Failed.]");

			List<String> startNodeIds = (List<String>) parameterMap.get(GraphDACParams.startNodeIds.name());
			if (null == startNodeIds || startNodeIds.size() <= 0)
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_START_NODE_ID_LIST
								+ " | ['Create Incoming Relations' Operation Failed.]");

			String endNodeId = (String) parameterMap.get(GraphDACParams.endNodeId.name());
			if (StringUtils.isBlank(endNodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_END_NODE_ID
								+ " | ['Create Incoming Relations' Query Generation Failed.]");

			String relationType = (String) parameterMap.get(GraphDACParams.relationType.name());
			if (StringUtils.isBlank(relationType))
				throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
						DACErrorMessageConstants.INVALID_RELATION_TYPE
								+ " | ['Create Incoming Relations' Query Generation Failed.]");

			Request request = (Request) parameterMap.get(GraphDACParams.request.name());
			Map<String, Object> metadata = new HashMap<String, Object>();
			if (null != request)
				metadata = (Map<String, Object>) request.get(GraphDACParams.metadata.name());

			int index = 0;
			for (String startNodeId : startNodeIds)
				query.append(getCreateRelationCypherQueryForCollection(graphId, startNodeId, endNodeId, relationType,
						getString(index++), getString(index++), metadata, RelationshipDirection.INCOMING, null));

		}

		TelemetryManager.log("'Create Incoming Relations' Cypher Query: " + query);
		return query.toString();
	}

	@SuppressWarnings("unchecked")
	public static String generateCreateOutgoingRelationCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Create Outgoing Relations' Query Generation Failed.]");

			String startNodeId = (String) parameterMap.get(GraphDACParams.startNodeId.name());
			if (StringUtils.isBlank(startNodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_END_NODE_ID
								+ " | ['Create Outgoing Relations' Query Generation Failed.]");

			List<String> endNodeIds = (List<String>) parameterMap.get(GraphDACParams.endNodeIds.name());
			if (null == endNodeIds || endNodeIds.size() <= 0)
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_START_NODE_ID_LIST
								+ " | ['Create Outgoing Relations' Operation Failed.]");

			String relationType = (String) parameterMap.get(GraphDACParams.relationType.name());
			if (StringUtils.isBlank(relationType))
				throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
						DACErrorMessageConstants.INVALID_RELATION_TYPE
								+ " | ['Create Outgoing Relations' Query Generation Failed.]");

			Request request = (Request) parameterMap.get(GraphDACParams.request.name());
			Map<String, Object> metadata = new HashMap<String, Object>();
			if (null != request)
				metadata = (Map<String, Object>) request.get(GraphDACParams.metadata.name());

			int index = 0;
			for (String endNodeId : endNodeIds)
				query.append(getCreateRelationCypherQueryForCollection(graphId, startNodeId, endNodeId, relationType,
						getString(index++), getString(index++), metadata, RelationshipDirection.OUTGOING, null));
		}

		TelemetryManager.log("'Create Outgoing Relations' Cypher Query: " + query);
		return query.toString();
	}

	@SuppressWarnings("unchecked")
	public static String generateDeleteIncomingRelationCypherQuery(Map<String, Object> parameterMap) {

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Delete Incoming Relations' Query Generation Failed.]");

			List<String> startNodeIds = (List<String>) parameterMap.get(GraphDACParams.startNodeIds.name());
			if (null == startNodeIds || startNodeIds.size() <= 0)
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_START_NODE_ID_LIST
								+ " | ['Delete Incoming Relations' Operation Failed.]");

			String endNodeId = (String) parameterMap.get(GraphDACParams.endNodeId.name());
			if (StringUtils.isBlank(endNodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_END_NODE_ID
								+ " | ['Delete Incoming Relations' Query Generation Failed.]");

			String relationType = (String) parameterMap.get(GraphDACParams.relationType.name());
			if (StringUtils.isBlank(relationType))
				throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
						DACErrorMessageConstants.INVALID_RELATION_TYPE
								+ " | ['Delete Incoming Relations' Query Generation Failed.]");

			int index = 0;
			for (String startNodeId : startNodeIds)
				query.append(getDeleteRelationCypherQuery(graphId, startNodeId, endNodeId, relationType,
						getString(index++), getString(index++), RelationshipDirection.INCOMING));
		}

		TelemetryManager.log("'Delete Incoming Relations' Cypher Query: " + query);
		return query.toString();
	}

	@SuppressWarnings("unchecked")
	public static String generateDeleteOutgoingRelationCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Delete Outgoing Relations' Query Generation Failed.]");

			String startNodeId = (String) parameterMap.get(GraphDACParams.startNodeId.name());
			if (StringUtils.isBlank(startNodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_END_NODE_ID
								+ " | ['Delete Outgoing Relations' Query Generation Failed.]");

			List<String> endNodeIds = (List<String>) parameterMap.get(GraphDACParams.endNodeIds.name());
			if (null == endNodeIds || endNodeIds.size() <= 0)
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_START_NODE_ID_LIST
								+ " | ['Delete Outgoing Relations' Operation Failed.]");

			String relationType = (String) parameterMap.get(GraphDACParams.relationType.name());
			if (StringUtils.isBlank(relationType))
				throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
						DACErrorMessageConstants.INVALID_RELATION_TYPE
								+ " | ['Delete Outgoing Relations' Query Generation Failed.]");

			int index = 0;
			for (String endNodeId : endNodeIds)
				query.append(getDeleteRelationCypherQuery(graphId, startNodeId, endNodeId, relationType,
						getString(index++), getString(index++), RelationshipDirection.INCOMING));
		}

		TelemetryManager.log("'Delete Outgoing Relations' Cypher Query: " + query);
		return query.toString();
	}

	public static String generateRemoveRelationMetadataCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Remove Relation Metadata' Query Generation Failed.]");

			String startNodeId = (String) parameterMap.get(GraphDACParams.startNodeId.name());
			if (StringUtils.isBlank(startNodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_START_NODE_ID
								+ " | ['Remove Relation Metadata' Query Generation Failed.]");

			String endNodeId = (String) parameterMap.get(GraphDACParams.endNodeId.name());
			if (StringUtils.isBlank(endNodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_END_NODE_ID
								+ " | ['Remove Relation Metadata' Query Generation Failed.]");

			String relationType = (String) parameterMap.get(GraphDACParams.relationType.name());
			if (StringUtils.isBlank(relationType))
				throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
						DACErrorMessageConstants.INVALID_RELATION_TYPE
								+ " | ['Remove Relation Metadata' Query Generation Failed.]");

			String key = (String) parameterMap.get(GraphDACParams.key.name());
			if (StringUtils.isBlank(key))
				throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
						DACErrorMessageConstants.INVALID_PROPERTY_KEY
								+ " | ['Remove Relation Metadata' Query Generation Failed.]");

			query.append(getRemoveRelationMetadataCypherQuery(graphId, startNodeId, endNodeId, relationType, key,
					CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT,
					CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT_II, RelationshipDirection.OUTGOING));
		}

		TelemetryManager.log("Returning 'Create Relation' Cypher Query: " + query);
		return query.toString();
	}

	@SuppressWarnings("unchecked")
	public static String generateCreateCollectionCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Create Collection' Query Generation Failed.]");

			String collectionId = (String) parameterMap.get(GraphDACParams.collectionId.name());
			if (StringUtils.isBlank(collectionId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_COLLECTION_NODE_ID
								+ " | ['Create Collection' Query Generation Failed.]");

			Node collection = (Node) parameterMap.get(GraphDACParams.collection.name());
			if (null == collection)
				throw new ClientException(DACErrorCodeConstants.INVALID_NODE.name(),
						DACErrorMessageConstants.INVALID_COLLECTION_NODE
								+ " | ['Create Collection' Query Generation Failed.]");

			String relationType = (String) parameterMap.get(GraphDACParams.relationType.name());
			if (StringUtils.isBlank(relationType))
				throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
						DACErrorMessageConstants.INVALID_RELATION_TYPE
								+ " | ['Create Collection' Query Generation Failed.]");

			List<String> members = (List<String>) parameterMap.get(GraphDACParams.members.name());
			if (null == members || members.size() <= 0)
				throw new ClientException(DACErrorCodeConstants.INVALID_MEMBERS.name(),
						DACErrorMessageConstants.INVALID_COLLECTION_MEMBERS
								+ " | ['Create Collection' Query Generation Failed.]");

			String indexProperty = (String) parameterMap.get(GraphDACParams.indexProperty.name());
			if (StringUtils.isBlank(indexProperty))
				throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
						DACErrorMessageConstants.INVALID_INDEX_PROPERTY
								+ " | ['Create Collection' Query Generation Failed.]");

			String date = DateUtils.formatCurrentDate();

			query.append(GraphDACParams.MERGE.name())
					.append(CypherQueryConfigurationConstants.OPEN_COMMON_BRACKETS_WITH_NODE_OBJECT_VARIABLE)
					.append(graphId).append(CypherQueryConfigurationConstants.OPEN_CURLY_BRACKETS)
					.append(getPropertyObjectAttributeString(collection))
					.append(CypherQueryConfigurationConstants.CLOSE_CURLY_BRACKETS)
					.append(CypherQueryConfigurationConstants.CLOSE_COMMON_BRACKETS)
					.append(CypherQueryConfigurationConstants.BLANK_SPACE);

			// Adding 'ON CREATE SET n.created=timestamp()' Clause
			query.append(getOnCreateSetString(CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT, date,
					collection)).append(CypherQueryConfigurationConstants.BLANK_SPACE);

			// Adding 'ON MATCH SET' Clause
			query.append(
					getOnMatchSetString(CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT, date, collection))
					.append(CypherQueryConfigurationConstants.BLANK_SPACE);

			int index = 1;
			for (String memeber : members) {
				Map<String, Object> metadata = new HashMap<String, Object>();
				metadata.put(indexProperty, index);
				query.append(getCreateRelationCypherQueryForCollection(graphId, collectionId, memeber, relationType,
						getString(index++), getString(index++), metadata, RelationshipDirection.OUTGOING, null));
			}

			// Return Node
			query.append(CypherQueryConfigurationConstants.BLANK_SPACE).append(GraphDACParams.RETURN.name())
					.append(CypherQueryConfigurationConstants.BLANK_SPACE)
					.append(CypherQueryConfigurationConstants.DEFAULT_CYPHER_NODE_OBJECT)
					.append(CypherQueryConfigurationConstants.BLANK_SPACE);
		}

		TelemetryManager.log("Returning 'Create Collection' Cypher Query: " + query);
		return query.toString();
	}

	public static String generateDeleteCollectionCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Delete Collection' Query Generation Failed.]");

			String collectionId = (String) parameterMap.get(GraphDACParams.collectionId.name());
			if (StringUtils.isBlank(collectionId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_COLLECTION_NODE_ID
								+ " | ['Delete Collection' Query Generation Failed.]");

			query.append("MATCH (a:" + graphId + " {" + SystemProperties.IL_UNIQUE_ID.name() + ": '" + collectionId
					+ "'}) DETACH DELETE a");
		}

		TelemetryManager.log("Returning 'Delete Collection' Cypher Query: " + query);
		return query.toString();
	}

	public static String generateImportGraphCypherQuery(Map<String, Object> parameterMap) {
		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Delete Collection' Query Generation Failed.]");

			String taskId = (String) parameterMap.get(GraphDACParams.taskId.name());
			if (StringUtils.isBlank(taskId))
				throw new ClientException(DACErrorCodeConstants.INVALID_TASK.name(),
						DACErrorMessageConstants.INVALID_TASK_ID + " | ['Import Graph' Query Generation Failed.]");

			String input = (String) parameterMap.get(GraphDACParams.input.name());
			if (null == input)
				throw new ClientException(DACErrorCodeConstants.INVALID_DATA.name(),
						DACErrorMessageConstants.INVALID_IMPORT_DATA + " | ['Import Graph' Query Generation Failed.]");

			query.append("");
		}

		TelemetryManager.log("Returning 'Import Data' Cypher Query: " + query);
		return query.toString();
	}

	@SuppressWarnings("unchecked")
	private static String getCreateRelationCypherQuery(String graphId, String startNodeId, String endNodeId,
			String relationType, String startNodeObjectVariableName, String endNodeObjectVariableName,
			Map<String, Object> metadata, RelationshipDirection direction, Integer index,
			Map<String, Object> parameterMap) {

		StringBuilder query = new StringBuilder();
		if (StringUtils.isNotBlank(graphId) && StringUtils.isNotBlank(startNodeId) && StringUtils.isNotBlank(endNodeId)
				&& StringUtils.isNotBlank(relationType) && StringUtils.isNotBlank(startNodeObjectVariableName)
				&& StringUtils.isNotBlank(endNodeObjectVariableName)) {

			String relationship = "";
			if (direction == RelationshipDirection.OUTGOING)
				relationship = "-[r:" + relationType + "]->";
			else if (direction == RelationshipDirection.INCOMING)
				relationship = "<-[r:" + relationType + "]-";
			else if (direction == RelationshipDirection.BIDIRECTIONAL)
				relationship = "-[r:" + relationType + "]-";

			query.append("MATCH (" + startNodeObjectVariableName + ":" + graphId + " { "
					+ SystemProperties.IL_UNIQUE_ID.name() + ": '" + startNodeId + "' }),(" + endNodeObjectVariableName
					+ ":" + graphId + " { " + SystemProperties.IL_UNIQUE_ID.name() + ": '" + endNodeId + "' }) MERGE ("
					+ startNodeObjectVariableName + ")" + relationship + "(" + endNodeObjectVariableName + ") ");

			if (null == metadata)
				metadata = new HashMap<String, Object>();

			// ON CREATE clause
			Map<String, Object> createMetadata = new HashMap<String, Object>();
			if (null != index)
				createMetadata.put(SystemProperties.IL_SEQUENCE_INDEX.name(), index);
			createMetadata.putAll(metadata);
			if (null != createMetadata && !createMetadata.isEmpty()) {
				Map<String, Object> metadataQueryMap = getMetadataCypherQueryMap("r", createMetadata);
				query.append("ON CREATE SET ").append(metadataQueryMap.get(GraphDACParams.query.name()))
						.append(CypherQueryConfigurationConstants.BLANK_SPACE);

				parameterMap.put(GraphDACParams.paramValueMap.name(),
						metadataQueryMap.get(GraphDACParams.paramValueMap.name()));

			}

			// ON MATCH CLAUSE
			if (null != metadata && !metadata.isEmpty()) {
				Map<String, Object> metadataQueryMap = getMetadataCypherQueryMap("r", metadata);
				query.append("ON MATCH SET ").append(metadataQueryMap.get(GraphDACParams.query.name()))
						.append(CypherQueryConfigurationConstants.BLANK_SPACE);

				Map<String, Object> mpm = (Map<String, Object>) metadataQueryMap
						.get(GraphDACParams.paramValueMap.name());
				if (parameterMap.get(GraphDACParams.paramValueMap.name()) != null)
					mpm.putAll((Map<String, Object>) parameterMap.get(GraphDACParams.paramValueMap.name()));
				parameterMap.put(GraphDACParams.paramValueMap.name(), mpm);
			}
			parameterMap.put(GraphDACParams.query.name(), query.toString());
			TelemetryManager.log("Returning 'Create Relation' Cypher Query: " + query);
		}
		return "";
	}

	private static String getCreateRelationCypherQueryForCollection(String graphId, String startNodeId,
			String endNodeId, String relationType, String startNodeObjectVariableName, String endNodeObjectVariableName,
			Map<String, Object> metadata, RelationshipDirection direction, Integer index) {

		StringBuilder query = new StringBuilder();
		if (StringUtils.isNotBlank(graphId) && StringUtils.isNotBlank(startNodeId) && StringUtils.isNotBlank(endNodeId)
				&& StringUtils.isNotBlank(relationType) && StringUtils.isNotBlank(startNodeObjectVariableName)
				&& StringUtils.isNotBlank(endNodeObjectVariableName)) {

			String relationship = "";
			if (direction == RelationshipDirection.OUTGOING)
				relationship = "-[r:" + relationType + "]->";
			else if (direction == RelationshipDirection.INCOMING)
				relationship = "<-[r:" + relationType + "]-";
			else if (direction == RelationshipDirection.BIDIRECTIONAL)
				relationship = "-[r:" + relationType + "]-";

			query.append("MATCH (" + startNodeObjectVariableName + ":" + graphId + " { "
					+ SystemProperties.IL_UNIQUE_ID.name() + ": '" + startNodeId + "' }),(" + endNodeObjectVariableName
					+ ":" + graphId + " { " + SystemProperties.IL_UNIQUE_ID.name() + ": '" + endNodeId + "' }) MERGE ("
					+ startNodeObjectVariableName + ")" + relationship + "(" + endNodeObjectVariableName + ")");

			if (null == metadata)
				metadata = new HashMap<String, Object>();

			// ON CREATE clause
			Map<String, Object> createMetadata = new HashMap<String, Object>();
			if (null != index)
				createMetadata.put(SystemProperties.IL_SEQUENCE_INDEX.name(), index);
			createMetadata.putAll(metadata);
			if (null != createMetadata && !createMetadata.isEmpty())
				query.append("ON CREATE SET ").append(getMetadataStringForCypherQuery("r", createMetadata))
						.append(CypherQueryConfigurationConstants.BLANK_SPACE);

			// ON MATCH CLAUSE
			if (null != metadata && !metadata.isEmpty()) {
				query.append("ON MATCH SET ").append(getMetadataStringForCypherQuery("r", metadata))
						.append(CypherQueryConfigurationConstants.BLANK_SPACE);
			}
		}
		return query.toString();
	}

	private static String getUpdateRelationCypherQuery(String graphId, String startNodeId, String endNodeId,
			String relationType, String startNodeObjectVariableName, String endNodeObjectVariableName,
			Map<String, Object> metadata, RelationshipDirection direction, Map<String, Object> parameterMap) {

		StringBuilder query = new StringBuilder();
		if (StringUtils.isNotBlank(graphId) && StringUtils.isNotBlank(startNodeId) && StringUtils.isNotBlank(endNodeId)
				&& StringUtils.isNotBlank(relationType) && StringUtils.isNotBlank(startNodeObjectVariableName)
				&& StringUtils.isNotBlank(endNodeObjectVariableName) && null != metadata && !metadata.isEmpty()) {

			String relationship = "";
			if (direction == RelationshipDirection.OUTGOING)
				relationship = "-[r:" + relationType + "]->";
			else if (direction == RelationshipDirection.INCOMING)
				relationship = "<-[r:" + relationType + "]-";
			else if (direction == RelationshipDirection.BIDIRECTIONAL)
				relationship = "-[r:" + relationType + "]-";

			query.append("MATCH (" + startNodeObjectVariableName + ":" + graphId + " { "
					+ SystemProperties.IL_UNIQUE_ID.name() + ": '" + startNodeId + "' })" + relationship + "("
					+ endNodeObjectVariableName + ":" + graphId + " { " + SystemProperties.IL_UNIQUE_ID.name() + ": '"
					+ endNodeId + "' }) ");

			// SET CLAUSE
			if (null != metadata && !metadata.isEmpty()) {
				Map<String, Object> metadataQueryMap = getMetadataCypherQueryMap("r", metadata);
				query.append("SET ").append(metadataQueryMap.get(GraphDACParams.query.name()))
						.append(CypherQueryConfigurationConstants.BLANK_SPACE);

				parameterMap.put(GraphDACParams.paramValueMap.name(),
						metadataQueryMap.get(GraphDACParams.paramValueMap.name()));
			}
			parameterMap.put(GraphDACParams.query.name(), query.toString());
			TelemetryManager.log("'Update Relation' Cypher Query: " + query);
		}
		return "";
	}

	private static String getDeleteRelationCypherQuery(String graphId, String startNodeId, String endNodeId,
			String relationType, String startNodeObjectVariableName, String endNodeObjectVariableName,
			RelationshipDirection direction) {

		StringBuilder query = new StringBuilder();
		if (StringUtils.isNotBlank(graphId) && StringUtils.isNotBlank(startNodeId) && StringUtils.isNotBlank(endNodeId)
				&& StringUtils.isNotBlank(relationType) && StringUtils.isNotBlank(startNodeObjectVariableName)
				&& StringUtils.isNotBlank(endNodeObjectVariableName)) {

			String relationship = "";
			if (direction == RelationshipDirection.OUTGOING)
				relationship = "-[r:" + relationType + "]->";
			else if (direction == RelationshipDirection.INCOMING)
				relationship = "<-[r:" + relationType + "]-";
			else if (direction == RelationshipDirection.BIDIRECTIONAL)
				relationship = "-[r:" + relationType + "]-";

			query.append("MATCH (a:" + graphId + " {" + SystemProperties.IL_UNIQUE_ID.name() + ": '" + startNodeId
					+ "'})" + relationship + "(b:" + graphId + " {" + SystemProperties.IL_UNIQUE_ID.name() + ": '"
					+ endNodeId + "'}) DELETE r").append(CypherQueryConfigurationConstants.BLANK_SPACE);
		}
		return query.toString();
	}

	private static String getRemoveRelationMetadataCypherQuery(String graphId, String startNodeId, String endNodeId,
			String relationType, String key, String startNodeObjectVariableName, String endNodeObjectVariableName,
			RelationshipDirection direction) {

		StringBuilder query = new StringBuilder();
		if (StringUtils.isNotBlank(graphId) && StringUtils.isNotBlank(startNodeId) && StringUtils.isNotBlank(endNodeId)
				&& StringUtils.isNotBlank(relationType) && StringUtils.isNotBlank(key)
				&& StringUtils.isNotBlank(startNodeObjectVariableName)
				&& StringUtils.isNotBlank(endNodeObjectVariableName)) {

			String relationship = "";
			if (direction == RelationshipDirection.OUTGOING)
				relationship = "-[r:" + relationType + "]->";
			else if (direction == RelationshipDirection.INCOMING)
				relationship = "<-[r:" + relationType + "]-";
			else if (direction == RelationshipDirection.BIDIRECTIONAL)
				relationship = "-[r:" + relationType + "]-";

			query.append("MATCH (a:" + graphId + " {" + SystemProperties.IL_UNIQUE_ID.name() + ": '" + startNodeId
					+ "'})" + relationship + "(b:" + graphId + " {" + SystemProperties.IL_UNIQUE_ID.name() + ": '"
					+ endNodeId + "'}) REMOVE r." + key).append(CypherQueryConfigurationConstants.BLANK_SPACE);
		}
		return query.toString();
	}

	private static List<Relationship> getAllRelationships(String graphId, String startNodeId,
			RelationshipDirection direction) {
		List<Relationship> relationships = new ArrayList<Relationship>();
		if (StringUtils.isNotBlank(graphId) && StringUtils.isNotBlank(startNodeId)) {
			Driver driver = DriverUtil.getDriver(graphId, GraphOperation.READ);
			TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				TelemetryManager.log("Session Initialised. | [Graph Id: " + graphId + "]");

				StatementResult result = session
						.run(generateGetAllRelationsCypherQuery(graphId, startNodeId, direction));
				for (Record record : result.list()) {
					relationships.add(record.get("r").asRelationship());
				}
			}
		}
		return relationships;
	}

	private static String generateGetAllRelationsCypherQuery(String graphId, String startNodeId,
			RelationshipDirection direction) {
		StringBuilder query = new StringBuilder();
		if (StringUtils.isNotBlank(graphId) && StringUtils.isNotBlank(startNodeId)) {
			if (direction == RelationshipDirection.INCOMING)
				query.append("MATCH (ee:" + graphId + " {" + SystemProperties.IL_UNIQUE_ID.name() + ": '" + startNodeId
						+ "'})<-[r]-() RETURN r");
			else if (direction == RelationshipDirection.OUTGOING)
				query.append("MATCH (ee:" + graphId + " {" + SystemProperties.IL_UNIQUE_ID.name() + ": '" + startNodeId
						+ "'})-[r]->() RETURN r");
			else if (direction == RelationshipDirection.BIDIRECTIONAL)
				query.append("MATCH (ee:" + graphId + " {" + SystemProperties.IL_UNIQUE_ID.name() + ": '" + startNodeId
						+ "'})-[r]-() RETURN r");
		}
		return query.toString();
	}

}
