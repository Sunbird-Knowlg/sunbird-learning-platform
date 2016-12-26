package org.ekstep.graph.service.util;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.neo4j.driver.v1.exceptions.ClientException;

import com.ilimi.common.dto.Property;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.Traverser;

public class SearchQueryGenerationUtil {

	private static Logger LOGGER = LogManager.getLogger(SearchQueryGenerationUtil.class.getName());

	public static String generateGetNodeByIdCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			LOGGER.info("Fetching the Parameters From Parameter Map");
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Get Node By Id' Query Generation Failed.]");

			Long nodeId = (long) parameterMap.get(GraphDACParams.nodeId.name());
			if (nodeId == 0)
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_NODE_ID + " | ['Get Node By Id' Query Generation Failed.]");

			query.append("");

		}

		LOGGER.info("Returning Get Node By Id Cypher Query: " + query);
		return query.toString();
	}

	public static String generateGetNodeByUniqueIdCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			LOGGER.info("Fetching the Parameters From Parameter Map");
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Get Node By Id' Query Generation Failed.]");

			String nodeId = (String) parameterMap.get(GraphDACParams.nodeId.name());
			if (StringUtils.isBlank(nodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_IDENTIFIER
								+ " | ['Get Node By Unique Id' Query Generation Failed.]");

			query.append("");

		}

		LOGGER.info("Returning Get Node By Unique Id Cypher Query: " + query);
		return query.toString();
	}

	public static String generateGetNodesByPropertyCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			LOGGER.info("Fetching the Parameters From Parameter Map");
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Get Nodes By Property' Query Generation Failed.]");

			Property property = (Property) parameterMap.get(GraphDACParams.property.name());
			if (null == property)
				throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
						DACErrorMessageConstants.INVALID_PROPERTY
								+ " | ['Get Nodes By Property' Query Generation Failed.]");

			query.append("");
		}

		LOGGER.info("Returning Get Nodes By Property Cypher Query: " + query);
		return query.toString();
	}

	public static String generateGetNodeByUniqueIdsCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			LOGGER.info("Fetching the Parameters From Parameter Map");
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Get Nodes By Search Criteria' Query Generation Failed.]");

			SearchCriteria searchCriteria = (SearchCriteria) parameterMap.get(GraphDACParams.searchCriteria.name());
			if (null == searchCriteria)
				throw new ClientException(DACErrorCodeConstants.INVALID_CRITERIA.name(),
						DACErrorMessageConstants.INVALID_SEARCH_CRITERIA
								+ " | ['Get Nodes By Search Criteria' Query Generation Failed.]");

			query.append("");
		}

		LOGGER.info("Returning Get Node By Unique Ids Cypher Query: " + query);
		return query.toString();
	}

	public static String generateGetNodePropertyCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			LOGGER.info("Fetching the Parameters From Parameter Map");
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Get Node Property' Query Generation Failed.]");

			String nodeId = (String) parameterMap.get(GraphDACParams.nodeId.name());
			if (StringUtils.isBlank(nodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_IDENTIFIER
								+ " | ['Get Node Property' Query Generation Failed.]");

			String key = (String) parameterMap.get(GraphDACParams.key.name());
			if (StringUtils.isBlank(key))
				throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
						DACErrorMessageConstants.INVALID_PROPERTY_KEY
								+ " | ['Get Node Property' Query Generation Failed.]");
		}

		LOGGER.info("Returning Get Node Property Cypher Query: " + query);
		return query.toString();
	}

	public static String generateAllNodesCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			LOGGER.info("Fetching the Parameters From Parameter Map");
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Get All Nodes' Query Generation Failed.]");
		}

		LOGGER.info("Returning Get All Nodes Cypher Query: " + query);
		return query.toString();
	}

	public static String generateAllRelationsCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			LOGGER.info("Fetching the Parameters From Parameter Map");
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Get All Relations' Query Generation Failed.]");
		}

		LOGGER.info("Returning Get All Relations Cypher Query: " + query);
		return query.toString();
	}

	public static String generateGetRelationPropertyCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			LOGGER.info("Fetching the Parameters From Parameter Map");
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Get Relation Property' Query Generation Failed.]");

			String startNodeId = (String) parameterMap.get(GraphDACParams.startNodeId.name());
			if (StringUtils.isBlank(startNodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_START_NODE_ID
								+ " | ['Get Relation Property' Query Generation Failed.]");

			String relationType = (String) parameterMap.get(GraphDACParams.relationType.name());
			if (StringUtils.isBlank(relationType))
				throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
						DACErrorMessageConstants.INVALID_RELATION_TYPE
								+ " | ['Get Relation Property' Query Generation Failed.]");

			String endNodeId = (String) parameterMap.get(GraphDACParams.endNodeId.name());
			if (StringUtils.isBlank(endNodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_END_NODE_ID
								+ " | ['Get Relation Property' Query Generation Failed.]");

			String key = (String) parameterMap.get(GraphDACParams.key.name());
			if (StringUtils.isBlank(key))
				throw new ClientException(DACErrorCodeConstants.INVALID_PROPERTY.name(),
						DACErrorMessageConstants.INVALID_PROPERTY_KEY
								+ " | ['Get Relation Property' Query Generation Failed.]");
		}

		LOGGER.info("Returning Get Relation Property Cypher Query: " + query);
		return query.toString();
	}
	
	public static String generateGetRelationCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			LOGGER.info("Fetching the Parameters From Parameter Map");
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Get Relation' Query Generation Failed.]");

			String startNodeId = (String) parameterMap.get(GraphDACParams.startNodeId.name());
			if (StringUtils.isBlank(startNodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_START_NODE_ID
								+ " | ['Get Relation' Query Generation Failed.]");

			String relationType = (String) parameterMap.get(GraphDACParams.relationType.name());
			if (StringUtils.isBlank(relationType))
				throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
						DACErrorMessageConstants.INVALID_RELATION_TYPE
								+ " | ['Get Relation' Query Generation Failed.]");

			String endNodeId = (String) parameterMap.get(GraphDACParams.endNodeId.name());
			if (StringUtils.isBlank(endNodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_END_NODE_ID
								+ " | ['Get Relation' Query Generation Failed.]");

		}

		LOGGER.info("Returning Get Relation Cypher Query: " + query);
		return query.toString();
	}
	
	public static String generateCheckCyclicLoopCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			LOGGER.info("Fetching the Parameters From Parameter Map");
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Check Cyclic Loop' Query Generation Failed.]");

			String startNodeId = (String) parameterMap.get(GraphDACParams.startNodeId.name());
			if (StringUtils.isBlank(startNodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_START_NODE_ID
								+ " | ['Check Cyclic Loop' Query Generation Failed.]");

			String relationType = (String) parameterMap.get(GraphDACParams.relationType.name());
			if (StringUtils.isBlank(relationType))
				throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
						DACErrorMessageConstants.INVALID_RELATION_TYPE
								+ " | ['Check Cyclic Loop' Query Generation Failed.]");

			String endNodeId = (String) parameterMap.get(GraphDACParams.endNodeId.name());
			if (StringUtils.isBlank(endNodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_END_NODE_ID
								+ " | ['Check Cyclic Loop' Query Generation Failed.]");

		}

		LOGGER.info("Returning Check Cyclic Loop Cypher Query: " + query);
		return query.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static String generateSearchNodeCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			LOGGER.info("Fetching the Parameters From Parameter Map");
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID + " | ['Execute Query' Query Generation Failed.]");
			
			String cypherQuery = (String) parameterMap.get(GraphDACParams.cypherQuery.name());
			if (StringUtils.isBlank(cypherQuery))
				throw new ClientException(DACErrorCodeConstants.INVALID_QUERY.name(),
						DACErrorMessageConstants.INVALID_QUERY + " | ['Execute Query' Query Generation Failed.]");
			
			Map<String, Object> paramMap = (Map<String, Object>) parameterMap.get(GraphDACParams.paramMap.name());
			if (null == paramMap || paramMap.isEmpty())
				throw new ClientException(DACErrorCodeConstants.INVALID_PARAMETER.name(),
						DACErrorMessageConstants.INVALID_PARAM_MAP + " | ['Execute Query' Query Generation Failed.]");

		}

		LOGGER.info("Returning Get Node Cypher Query: " + query);
		return query.toString();
	}

	public static String generateSearchNodesCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			LOGGER.info("Fetching the Parameters From Parameter Map");
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Search Nodes' Query Generation Failed.]");

			SearchCriteria searchCriteria = (SearchCriteria) parameterMap.get(GraphDACParams.searchCriteria.name());
			if (null == searchCriteria)
				throw new ClientException(DACErrorCodeConstants.INVALID_CRITERIA.name(),
						DACErrorMessageConstants.INVALID_SEARCH_CRITERIA
								+ " | ['Search Nodes' Query Generation Failed.]");
		}

		LOGGER.info("Returning search Nodes Cypher Query: " + query);
		return query.toString();
	}
	
	public static String generateNodesCountCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			LOGGER.info("Fetching the Parameters From Parameter Map");
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Nodes Count' Query Generation Failed.]");

			SearchCriteria searchCriteria = (SearchCriteria) parameterMap.get(GraphDACParams.searchCriteria.name());
			if (null == searchCriteria)
				throw new ClientException(DACErrorCodeConstants.INVALID_CRITERIA.name(),
						DACErrorMessageConstants.INVALID_SEARCH_CRITERIA
								+ " | ['Nodes Count' Query Generation Failed.]");
		}

		LOGGER.info("Returning Nodes Count Cypher Query: " + query);
		return query.toString();
	}
	
	public static String generateTraverseCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			LOGGER.info("Fetching the Parameters From Parameter Map");
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Traverse' Query Generation Failed.]");

			Traverser traverser = (Traverser) parameterMap.get(GraphDACParams.traverser.name());
			if (null == traverser)
				throw new ClientException(DACErrorCodeConstants.INVALID_TRAVERSER.name(),
						DACErrorMessageConstants.INVALID_TRAVERSER
								+ " | ['Traverse' Query Generation Failed.]");
		}

		LOGGER.info("Returning Traverse Cypher Query: " + query);
		return query.toString();
	}

	public static String generateTraverseSubGraphCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			LOGGER.info("Fetching the Parameters From Parameter Map");
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Traverse Sub Graph' Query Generation Failed.]");

			Traverser traverser = (Traverser) parameterMap.get(GraphDACParams.traverser.name());
			if (null == traverser)
				throw new ClientException(DACErrorCodeConstants.INVALID_TRAVERSER.name(),
						DACErrorMessageConstants.INVALID_TRAVERSER
								+ " | ['Traverse Sub Graph' Query Generation Failed.]");
		}

		LOGGER.info("Returning Traverse Sub Graph Cypher Query: " + query);
		return query.toString();
	}
	
	public static String generateSubGraphCypherQuery(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);

		StringBuilder query = new StringBuilder();
		if (null != parameterMap) {
			LOGGER.info("Fetching the Parameters From Parameter Map");
			String graphId = (String) parameterMap.get(GraphDACParams.graphId.name());
			if (StringUtils.isBlank(graphId))
				throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
						DACErrorMessageConstants.INVALID_GRAPH_ID
								+ " | ['Get Sub Graph' Query Generation Failed.]");
			
			String startNodeId = (String) parameterMap.get(GraphDACParams.startNodeId.name());
			if (StringUtils.isBlank(startNodeId))
				throw new ClientException(DACErrorCodeConstants.INVALID_IDENTIFIER.name(),
						DACErrorMessageConstants.INVALID_START_NODE_ID
								+ " | ['Get Sub Graph' Query Generation Failed.]");
			
			String relationType = (String) parameterMap.get(GraphDACParams.relationType.name());
			if (StringUtils.isBlank(relationType))
				throw new ClientException(DACErrorCodeConstants.INVALID_RELATION.name(),
						DACErrorMessageConstants.INVALID_RELATION_TYPE
								+ " | ['Get Sub Graph' Query Generation Failed.]");
			
			int depth = (int) parameterMap.get(GraphDACParams.graphId.name());
			if (depth <= 0)
				throw new ClientException(DACErrorCodeConstants.INVALID_DEPTH.name(),
						DACErrorMessageConstants.INVALID_DEPTH
								+ " | ['Get Sub Graph' Query Generation Failed.]");
		}

		LOGGER.info("Returning Get Sub Graph Cypher Query: " + query);
		return query.toString();
	}

}
