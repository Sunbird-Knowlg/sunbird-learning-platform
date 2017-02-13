package org.ekstep.graph.service.util;

import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.ekstep.graph.service.common.Neo4JOperation;
import org.neo4j.driver.v1.exceptions.ClientException;

public class CypherUtil {

	private static Logger LOGGER = LogManager.getLogger(CypherUtil.class.getName());

	public static String getQuery(Neo4JOperation operation, Map<String, Object> parameterMap) {
		LOGGER.debug("Neo4J Operation: ", operation);
		LOGGER.debug("Parameter Map: ", parameterMap);

		LOGGER.debug("Validating Database (Neo4J) Operation against 'null'.");
		if (null == operation)
			throw new ClientException(DACErrorCodeConstants.INVALID_OPERATION.name(),
					DACErrorMessageConstants.INVALID_OPERATION + " | [Query Generation Failed.]");

		LOGGER.debug("Validating Graph Engine Node against 'null'.");
		if (null == parameterMap)
			throw new ClientException(DACErrorCodeConstants.INVALID_PARAMETER.name(),
					DACErrorMessageConstants.INVALID_PARAMETER_MAP + " | [Query Generation Failed.]");

		String query = "";
		query = generateQuery(operation, parameterMap);
		return query;

	}

	private static String generateQuery(Neo4JOperation operation, Map<String, Object> parameterMap) {
		LOGGER.debug("Neo4J Operation: ", operation);
		LOGGER.debug("Parameter Map: ", parameterMap);

		String query = "";
		if (null != operation && null != parameterMap && !parameterMap.isEmpty())
			query = getCypherQuery(operation, parameterMap);

		LOGGER.debug("Returning Generated Cypher Query: " + query);
		return query;
	}

	private static String getCypherQuery(Neo4JOperation operation, Map<String, Object> parameterMap) {
		LOGGER.debug("Neo4J Operation: ", operation);
		LOGGER.debug("Parameter Map: ", parameterMap);

		String query = "";
		if (null != operation && null != parameterMap) {
			String opt = operation.name();
			switch (opt) {
			case "CREATE_NODE":
				query = NodeQueryGenerationUtil.generateCreateNodeCypherQuery(parameterMap);
				break;
			case "UPSERT_NODE":
				query = NodeQueryGenerationUtil.generateUpsertNodeCypherQuery(parameterMap);
				break;
			case "UPDATE_NODE":
				query = NodeQueryGenerationUtil.generateUpdateNodeCypherQuery(parameterMap);
				break;
			case "IMPORT_NODES":
				query = NodeQueryGenerationUtil.generateImportNodesCypherQuery(parameterMap);
				break;
			case "UPDATE_PROPERTY":
				query = NodeQueryGenerationUtil.generateUpdatePropertyValueCypherQuery(parameterMap);
				break;
			case "UPDATE_PROPERTIES":
				query = NodeQueryGenerationUtil.generateUpdatePropertyValuesCypherQuery(parameterMap);
				break;
			case "REMOVE_PROPERTY":
				query = NodeQueryGenerationUtil.generateRemovePropertyValueCypherQuery(parameterMap);
				break;
			case "REMOVE_PROPERTIES":
				query = NodeQueryGenerationUtil.generateRemovePropertyValuesCypherQuery(parameterMap);
				break;
			case "DELETE_NODE":
				query = NodeQueryGenerationUtil.generateDeleteNodeCypherQuery(parameterMap);
				break;
			case "UPSERT_ROOT_NODE":
				query = NodeQueryGenerationUtil.generateUpsertRootNodeCypherQuery(parameterMap);
				break;
			case "CREATE_UNIQUE_CONSTRAINT":
				query = GraphQueryGenerationUtil.generateCreateUniqueConstraintCypherQuery(parameterMap);
				break;
			case "CREATE_INDEX":
				query = GraphQueryGenerationUtil.generateCreateIndexCypherQuery(parameterMap);
				break;
			case "DELETE_GRAPH":
				query = GraphQueryGenerationUtil.generateDeleteGraphCypherQuery(parameterMap);
				break;
			case "CREATE_RELATION":
				query = GraphQueryGenerationUtil.generateCreateRelationCypherQuery(parameterMap);
				break;
			case "UPDATE_RELATION":
				query = GraphQueryGenerationUtil.generateUpdateRelationCypherQuery(parameterMap);
				break;
			case "DELETE_RELATION":
				query = GraphQueryGenerationUtil.generateDeleteRelationCypherQuery(parameterMap);
				break;
			case "CREATE_INCOMING_RELATIONS":
				query = GraphQueryGenerationUtil.generateCreateIncomingRelationCypherQuery(parameterMap);
				break;
			case "CREATE_OUTGOING_RELATIONS":
				query = GraphQueryGenerationUtil.generateCreateOutgoingRelationCypherQuery(parameterMap);
				break;
			case "DELETE_INCOMING_RELATIONS":
				query = GraphQueryGenerationUtil.generateDeleteIncomingRelationCypherQuery(parameterMap);
				break;
			case "DELETE_OUTGOING_RELATIONS":
				query = GraphQueryGenerationUtil.generateDeleteOutgoingRelationCypherQuery(parameterMap);
				break;
			case "REMOVE_RELATION_METADATA":
				query = GraphQueryGenerationUtil.generateRemoveRelationMetadataCypherQuery(parameterMap);
				break;
			case "CREATE_COLLECTION":
				query = GraphQueryGenerationUtil.generateCreateCollectionCypherQuery(parameterMap);
				break;
			case "DELETE_COLLECTION":
				query = GraphQueryGenerationUtil.generateDeleteCollectionCypherQuery(parameterMap);
				break;
			case "IMPORT_GRAPH":
				query = GraphQueryGenerationUtil.generateImportGraphCypherQuery(parameterMap);
				break;
			case "GET_NODE_BY_ID":
				query = SearchQueryGenerationUtil.generateGetNodeByIdCypherQuery(parameterMap);
				break;
			case "GET_NODE_BY_UNIQUE_ID":
				query = SearchQueryGenerationUtil.generateGetNodeByUniqueIdCypherQuery(parameterMap);
				break;
			case "GET_NODES_BY_PROPERTY":
				query = SearchQueryGenerationUtil.generateGetNodesByPropertyCypherQuery(parameterMap);
				break;
			case "GET_NODES_BY_SEARCH_CRITERIA":
				query = SearchQueryGenerationUtil.generateGetNodeByUniqueIdsCypherQuery(parameterMap);
				break;
			case "GET_NODE_PROPERTY":
				query = SearchQueryGenerationUtil.generateGetNodePropertyCypherQuery(parameterMap);
				break;
			case "GET_ALL_NODES":
				query = SearchQueryGenerationUtil.generateGetAllNodesCypherQuery(parameterMap);
				break;
			case "GET_ALL_RELATIONS":
				query = SearchQueryGenerationUtil.generateGetAllRelationsCypherQuery(parameterMap);
				break;
			case "GET_RELATION_PROPERTY":
				query = SearchQueryGenerationUtil.generateGetRelationPropertyCypherQuery(parameterMap);
				break;
			case "GET_RELATION_BY_ID":
				query = SearchQueryGenerationUtil.generateGetRelationByIdCypherQuery(parameterMap);
				break;
			case "GET_RELATION":
				query = SearchQueryGenerationUtil.generateGetRelationCypherQuery(parameterMap);
				break;
			case "CHECK_CYCLIC_LOOP":
				query = SearchQueryGenerationUtil.generateCheckCyclicLoopCypherQuery(parameterMap);
				break;
			case "EXECUTE_QUERY":
				query = SearchQueryGenerationUtil.generateExecuteQueryCypherQuery(parameterMap);
				break;
			case "SEARCH_NODES":
				query = SearchQueryGenerationUtil.generateSearchNodesCypherQuery(parameterMap);
				break;
			case "GET_NODES_COUNT":
				query = SearchQueryGenerationUtil.generateGetNodesCountCypherQuery(parameterMap);
				break;
			case "TRAVERSE":
				query = SearchQueryGenerationUtil.generateTraverseCypherQuery(parameterMap);
				break;
			case "TRAVERSE_SUB_GRAPH":
				query = SearchQueryGenerationUtil.generateTraverseSubGraphCypherQuery(parameterMap);
				break;
			case "GET_SUB_GRAPH":
				query = SearchQueryGenerationUtil.generateGetSubGraphCypherQuery(parameterMap);
				break;

			default:
				LOGGER.warn("Invalid Neo4J Operation !");
				break;
			}
		}

		LOGGER.debug("Returning Cypher Query For Operation - " + operation.name() + " | Query - " + query);
		return query;
	}

}
