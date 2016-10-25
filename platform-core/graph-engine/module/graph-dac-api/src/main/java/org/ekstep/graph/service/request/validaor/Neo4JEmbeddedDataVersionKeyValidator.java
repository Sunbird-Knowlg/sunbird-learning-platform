package org.ekstep.graph.service.request.validaor;

import static com.ilimi.graph.dac.util.Neo4jGraphUtil.getNodeByUniqueId;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.ekstep.graph.service.util.GraphUtil;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.common.DateUtils;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.util.Neo4jGraphFactory;

public class Neo4JEmbeddedDataVersionKeyValidator {

	private static Logger LOGGER = LogManager.getLogger(Neo4JEmbeddedDataVersionKeyValidator.class.getName());

	public boolean isValidVersionKey(Node node, Request request) {
		LOGGER.debug("Node: ", node);

		boolean isValidVersionKey = false;

		String versionKey = (String) node.getMetadata().get(GraphDACParams.versionKey.name());
		LOGGER.info("Data Node Version Key Value: " + versionKey + " | [Node Id: '" + node.getIdentifier() + "']");

		// Fetching Neo4J Node
		org.neo4j.graphdb.Node neo4jNode = getNeo4jNode(GraphUtil.getGraphId(), node.getIdentifier(), request);
		LOGGER.info("Fetched the Neo4J Node Id: " + neo4jNode.getId() + " | [Node Id: '" + node.getIdentifier() + "']");

		// Reading Last Updated On time stamp from Neo4J Node
		String lastUpdateOn = (String) neo4jNode.getProperty(GraphDACParams.versionKey.name());
		LOGGER.info("Fetched 'lastUpdatedOn' Property from the Neo4J Node Id: " + neo4jNode.getId()
				+ " as 'lastUpdatedOn': " + lastUpdateOn + " | [Node Id: '" + node.getIdentifier() + "']");

		if (StringUtils.isNotBlank(versionKey) && StringUtils.isNotBlank(lastUpdateOn)) {
			// Converting versionKey to milli seconds of type Long
			long versionKeyTS = Long.parseLong(versionKey);
			LOGGER.info("'versionKey' Time Stamp: " + versionKeyTS + " | [Node Id: '" + node.getIdentifier() + "']");

			// Converting 'lastUpdatedOn' to milli seconds of type Long
			long lastUpdatedOnTS = DateUtils.parse(lastUpdateOn).getTime();
			LOGGER.info(
					"'lastUpdatedOn' Time Stamp: " + lastUpdatedOnTS + " | [Node Id: '" + node.getIdentifier() + "']");

			// Compare both the Time Stamp
			if (versionKeyTS == lastUpdatedOnTS)
				isValidVersionKey = true;
		}

		return isValidVersionKey;
	}

	public boolean isValidVersionKey(org.neo4j.graphdb.Node neo4jNode, com.ilimi.graph.dac.model.Node node) {
		LOGGER.debug("Graph Node: ", node);
		LOGGER.debug("Neo4J Node: ", neo4jNode);

		boolean isValidVersionKey = false;

		String versionKey = (String) node.getMetadata().get(GraphDACParams.versionKey.name());
		LOGGER.info("Data Node Version Key Value: " + versionKey + " | [Node Id: '" + node.getIdentifier() + "']");

		if (StringUtils.isBlank(versionKey))
			throw new ClientException(DACErrorCodeConstants.BLANK_VERSION.name(),
					DACErrorMessageConstants.ERROR_BLANK_VERSION_KEY + " | [Node Id: " + node.getIdentifier() + "]");

		LOGGER.info("Fetched the Neo4J Node Id: " + neo4jNode.getId() + " | [Node Id: '" + node.getIdentifier() + "']");

		// Reading Last Updated On time stamp from Neo4J Node
		String lastUpdateOn = (String) neo4jNode.getProperty(GraphDACParams.versionKey.name());
		LOGGER.info("Fetched 'lastUpdatedOn' Property from the Neo4J Node Id: " + neo4jNode.getId()
				+ " as 'lastUpdatedOn': " + lastUpdateOn + " | [Node Id: '" + node.getIdentifier() + "']");

		if (StringUtils.isNotBlank(versionKey) && StringUtils.isNotBlank(lastUpdateOn)) {
			// Converting versionKey to milli seconds of type Long
			long versionKeyTS = Long.parseLong(versionKey);
			LOGGER.info("'versionKey' Time Stamp: " + versionKeyTS + " | [Node Id: '" + node.getIdentifier() + "']");

			// Converting 'lastUpdatedOn' to milli seconds of type Long
			long lastUpdatedOnTS = DateUtils.parse(lastUpdateOn).getTime();
			LOGGER.info(
					"'lastUpdatedOn' Time Stamp: " + lastUpdatedOnTS + " | [Node Id: '" + node.getIdentifier() + "']");

			// Compare both the Time Stamp
			if (versionKeyTS == lastUpdatedOnTS)
				isValidVersionKey = true;
		}

		return isValidVersionKey;
	}

	private org.neo4j.graphdb.Node getNeo4jNode(String graphId, String identifier, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			LOGGER.info("Transaction Started For 'getNeo4jNode' Operation. | [Node ID: '" + identifier + "']");
			org.neo4j.graphdb.Node neo4jNode = getNodeByUniqueId(graphDb, identifier);

			tx.success();
			LOGGER.info("Transaction For Operation 'getNeo4jNode' Completed Successfully. | [Node ID: '" + identifier
					+ "']");

			LOGGER.info("Returning the Neo4J Node. | [Node ID: '" + identifier + "']");
			return neo4jNode;
		}
	}

}
