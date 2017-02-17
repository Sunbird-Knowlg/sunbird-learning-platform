package org.ekstep.graph.service.request.validaor;

import static com.ilimi.graph.dac.util.Neo4jGraphUtil.getNodeByUniqueId;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.common.DACConfigurationConstants;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.ekstep.graph.service.common.NodeUpdateMode;
import org.ekstep.graph.service.util.DefinitionNodeUtil;
import org.ekstep.graph.service.util.PassportUtil;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.common.DateUtils;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.util.Neo4jGraphFactory;

public class Neo4JEmbeddedDataVersionKeyValidator {

	private static Logger LOGGER = LogManager.getLogger(Neo4JEmbeddedDataVersionKeyValidator.class.getName());

	public boolean validateUpdateOperation(String graphId, org.neo4j.graphdb.Node neo4jNode,
			com.ilimi.graph.dac.model.Node node, Request request) {
		LOGGER.debug("Graph Engine Node: ", node);
		LOGGER.debug("Neo4J Node: ", neo4jNode);

		boolean isValidUpdateOperation = false;

		// Fetching Version Check Mode ('OFF', 'STRICT', 'LINIENT')
		String versionCheckMode = DefinitionNodeUtil.getMetadataValue(graphId, node.getObjectType(),
				GraphDACParams.versionCheckMode.name(), request);
		LOGGER.debug("Version Check Mode in Definition Node: " + versionCheckMode + " for Object Type: "
				+ node.getObjectType());

		// Checking if the 'versionCheckMode' Property is not specified,
		// then default Mode is OFF
		if (StringUtils.isBlank(versionCheckMode)
				|| StringUtils.equalsIgnoreCase(SystemNodeTypes.DEFINITION_NODE.name(), node.getNodeType()))
			versionCheckMode = NodeUpdateMode.OFF.name();

		// Checking of Node Update Version Checking is either 'STRICT'
		// or 'LENIENT'.
		// If Number of Modes are increasing then the Condition should
		// be checked for 'OFF' Mode Only.
		if (StringUtils.equalsIgnoreCase(NodeUpdateMode.STRICT.name(), versionCheckMode)
				|| StringUtils.equalsIgnoreCase(NodeUpdateMode.LENIENT.name(), versionCheckMode)) {
			boolean isValidVersionKey = isValidVersionKey(neo4jNode, node);
			LOGGER.debug("Is Valid Version Key ? " + isValidVersionKey);

			if (!isValidVersionKey) {
				// Checking for Strict Mode
				LOGGER.debug("Checking for Node Update Operation Mode is 'STRICT' for Node Id: " + node.getIdentifier());
				if (StringUtils.equalsIgnoreCase(NodeUpdateMode.STRICT.name(), versionCheckMode))
					throw new ClientException(DACErrorCodeConstants.INVALID_VERSION.name(),
							DACErrorMessageConstants.INVALID_VERSION_KEY_ERROR + " | [Unable to Update the Data.]");

				// Checking for Lenient Mode
				LOGGER.debug(
						"Checking for Node Update Operation Mode is 'LENIENT' for Node Id: " + node.getIdentifier());
				if (StringUtils.equalsIgnoreCase(NodeUpdateMode.LENIENT.name(), versionCheckMode))
					node.getMetadata().put(GraphDACParams.NODE_UPDATE_STATUS.name(),
							GraphDACParams.STALE_DATA_UPDATED.name());

				// Update Operation is Valid
				isValidUpdateOperation = true;
				LOGGER.debug("Update Operation is Valid for Node Id: " + node.getIdentifier());
			}
		}

		LOGGER.debug("Is Valid Update Operation ? " + isValidUpdateOperation);
		return isValidUpdateOperation;
	}

	public boolean validateUpdateOperation(String graphId, Node node, Request request) {
		LOGGER.debug("Graph Engine Node: ", node);

		boolean isValidUpdateOperation = false;

		// Fetching Version Check Mode ('OFF', 'STRICT', 'LINIENT')
		String versionCheckMode = DefinitionNodeUtil.getMetadataValue(graphId, node.getObjectType(),
				GraphDACParams.versionCheckMode.name(), request);
		LOGGER.debug("Version Check Mode in Definition Node: " + versionCheckMode + " for Object Type: "
				+ node.getObjectType());

		// Checking if the 'versionCheckMode' Property is not specified,
		// then default Mode is OFF
		if (StringUtils.isBlank(versionCheckMode))
			versionCheckMode = NodeUpdateMode.OFF.name();

		// Checking of Node Update Version Checking is either 'STRICT'
		// or 'LENIENT'.
		// If Number of Modes are increasing then the Condition should
		// be checked for 'OFF' Mode Only.
		if (StringUtils.equalsIgnoreCase(NodeUpdateMode.STRICT.name(), versionCheckMode)
				|| StringUtils.equalsIgnoreCase(NodeUpdateMode.LENIENT.name(), versionCheckMode)) {
			boolean isValidVersionKey = isValidVersionKey(graphId, node, request);
			LOGGER.debug("Is Valid Version Key ? " + isValidVersionKey);

			if (!isValidVersionKey) {
				// Checking for Strict Mode
				LOGGER.debug("Checking for Node Update Operation Mode is 'STRICT' for Node Id: " + node.getIdentifier());
				if (StringUtils.equalsIgnoreCase(NodeUpdateMode.STRICT.name(), versionCheckMode))
					throw new ClientException(DACErrorCodeConstants.INVALID_VERSION.name(),
							DACErrorMessageConstants.INVALID_VERSION_KEY_ERROR + " | [Unable to Update the Data.]");

				// Checking for Lenient Mode
				LOGGER.debug(
						"Checking for Node Update Operation Mode is 'LENIENT' for Node Id: " + node.getIdentifier());
				if (StringUtils.equalsIgnoreCase(NodeUpdateMode.LENIENT.name(), versionCheckMode))
					node.getMetadata().put(GraphDACParams.NODE_UPDATE_STATUS.name(),
							GraphDACParams.STALE_DATA_UPDATED.name());

				// Update Operation is Valid
				isValidUpdateOperation = true;
				LOGGER.debug("Update Operation is Valid for Node Id: " + node.getIdentifier());
			}
		}
		LOGGER.debug("Is Valid Update Operation ? " + isValidUpdateOperation);

		return isValidUpdateOperation;
	}

	private boolean isValidVersionKey(String graphId, Node node, Request request) {
		LOGGER.debug("Node: ", node);

		boolean isValidVersionKey = false;

		String versionKey = (String) node.getMetadata().get(GraphDACParams.versionKey.name());
		LOGGER.debug("Data Node Version Key Value: " + versionKey + " | [Node Id: '" + node.getIdentifier() + "']");

		// Fetching Neo4J Node
		org.neo4j.graphdb.Node neo4jNode = getNeo4jNode(graphId, node.getIdentifier(), request);
		LOGGER.debug("Fetched the Neo4J Node Id: " + neo4jNode.getId() + " | [Node Id: '" + node.getIdentifier() + "']");

		// Reading Last Updated On time stamp from Neo4J Node
		String lastUpdateOn = (String) neo4jNode.getProperty(GraphDACParams.lastUpdatedOn.name());
		LOGGER.debug("Fetched 'lastUpdatedOn' Property from the Neo4J Node Id: " + neo4jNode.getId()
				+ " as 'lastUpdatedOn': " + lastUpdateOn + " | [Node Id: '" + node.getIdentifier() + "']");
		if (StringUtils.isBlank(lastUpdateOn))
			throw new ClientException(DACErrorCodeConstants.INVALID_TIMESTAMP.name(),
					DACErrorMessageConstants.INVALID_LAST_UPDATED_ON_TIMESTAMP + " | [Node Id: " + node.getIdentifier()
							+ "]");

		// Converting 'lastUpdatedOn' to milli seconds of type Long
		String graphVersionKey = (String) neo4jNode.getProperty(GraphDACParams.versionKey.name());
		if (StringUtils.isBlank(graphVersionKey))
			graphVersionKey = String.valueOf(DateUtils.parse(lastUpdateOn).getTime());
		LOGGER.debug("'lastUpdatedOn' Time Stamp: " + graphVersionKey + " | [Node Id: '" + node.getIdentifier() + "']");

		// Compare both the Time Stamp
		if (StringUtils.equals(versionKey, graphVersionKey))
			isValidVersionKey = true;

		// Remove 'SYS_INTERNAL_LAST_UPDATED_ON' property
		node.getMetadata().remove(GraphDACParams.SYS_INTERNAL_LAST_UPDATED_ON.name());

		// May be the Given 'versionKey' is a Passport Key.
		// Check for the Valid Passport Key
		if (BooleanUtils.isFalse(isValidVersionKey)
				&& BooleanUtils.isTrue(DACConfigurationConstants.IS_PASSPORT_AUTHENTICATION_ENABLED)) {
			isValidVersionKey = PassportUtil.isValidPassportKey(versionKey);
			if (BooleanUtils.isTrue(isValidVersionKey))
				node.getMetadata().put(GraphDACParams.SYS_INTERNAL_LAST_UPDATED_ON.name(),
						DateUtils.formatCurrentDate());
		}

		return isValidVersionKey;
	}

	private boolean isValidVersionKey(org.neo4j.graphdb.Node neo4jNode, com.ilimi.graph.dac.model.Node node) {
		LOGGER.debug("Graph Node: ", node);
		LOGGER.debug("Neo4J Node: ", neo4jNode);

		boolean isValidVersionKey = false;

		String versionKey = (String) node.getMetadata().get(GraphDACParams.versionKey.name());
		LOGGER.debug("Data Node Version Key Value: " + versionKey + " | [Node Id: '" + node.getIdentifier() + "']");

		if (StringUtils.isBlank(versionKey))
			throw new ClientException(DACErrorCodeConstants.BLANK_VERSION.name(),
					DACErrorMessageConstants.BLANK_VERSION_KEY_ERROR + " | [Node Id: " + node.getIdentifier() + "]");

		LOGGER.debug("Fetched the Neo4J Node Id: " + neo4jNode.getId() + " | [Node Id: '" + node.getIdentifier() + "']");

		// Reading Last Updated On time stamp from Neo4J Node
		String lastUpdateOn = (String) neo4jNode.getProperty(GraphDACParams.lastUpdatedOn.name());
		LOGGER.debug("Fetched 'lastUpdatedOn' Property from the Neo4J Node Id: " + neo4jNode.getId()
				+ " as 'lastUpdatedOn': " + lastUpdateOn + " | [Node Id: '" + node.getIdentifier() + "']");
		if (StringUtils.isBlank(lastUpdateOn))
			throw new ClientException(DACErrorCodeConstants.INVALID_TIMESTAMP.name(),
					DACErrorMessageConstants.INVALID_LAST_UPDATED_ON_TIMESTAMP + " | [Node Id: " + node.getIdentifier()
							+ "]");

		// Converting 'lastUpdatedOn' to milli seconds of type Long
		String graphVersionKey = (String) neo4jNode.getProperty(GraphDACParams.versionKey.name());
		if (StringUtils.isBlank(graphVersionKey))
			graphVersionKey = String.valueOf(DateUtils.parse(lastUpdateOn).getTime());
		LOGGER.debug("'lastUpdatedOn' Time Stamp: " + graphVersionKey + " | [Node Id: '" + node.getIdentifier() + "']");

		// Compare both the Time Stamp
		if (StringUtils.equals(versionKey, graphVersionKey))
			isValidVersionKey = true;

		// Remove 'SYS_INTERNAL_LAST_UPDATED_ON' property
		node.getMetadata().remove(GraphDACParams.SYS_INTERNAL_LAST_UPDATED_ON.name());

		// May be the Given 'versionKey' is a Passport Key.
		// Check for the Valid Passport Key
		if (BooleanUtils.isFalse(isValidVersionKey)
				&& BooleanUtils.isTrue(DACConfigurationConstants.IS_PASSPORT_AUTHENTICATION_ENABLED)) {
			isValidVersionKey = PassportUtil.isValidPassportKey(versionKey);
			if (BooleanUtils.isTrue(isValidVersionKey))
				node.getMetadata().put(GraphDACParams.SYS_INTERNAL_LAST_UPDATED_ON.name(),
						DateUtils.formatCurrentDate());
		}

		return isValidVersionKey;
	}

	private org.neo4j.graphdb.Node getNeo4jNode(String graphId, String identifier, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			LOGGER.debug("Transaction Started For 'getNeo4jNode' Operation. | [Node ID: '" + identifier + "']");
			org.neo4j.graphdb.Node neo4jNode = getNodeByUniqueId(graphDb, identifier);

			tx.success();
			LOGGER.debug("Transaction For Operation 'getNeo4jNode' Completed Successfully. | [Node ID: '" + identifier
					+ "']");

			LOGGER.debug("Returning the Neo4J Node. | [Node ID: '" + identifier + "']");
			return neo4jNode;
		}
	}

}
