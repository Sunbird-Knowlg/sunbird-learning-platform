package org.ekstep.graph.service.request.validaor;

import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.common.DACConfigurationConstants;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.ekstep.graph.service.common.NodeUpdateMode;
import org.ekstep.graph.service.util.DefinitionNodeUtil;
import org.ekstep.graph.service.util.DriverUtil;
import org.ekstep.graph.service.util.PassportUtil;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.common.DateUtils;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Node;

public class Neo4JBoltDataVersionKeyValidator {

	private static Logger LOGGER = LogManager.getLogger(Neo4JBoltDataVersionKeyValidator.class.getName());

	public boolean validateUpdateOperation(String graphId, Node node) {
		LOGGER.debug("Graph Engine Node: ", node);

		// Fetching Neo4J Node
		Map<String, Object> neo4jNode = getNeo4jNodeProperty(graphId, node.getIdentifier());
		// New node... not found in the graph
		if (null == neo4jNode)
			return true;
		LOGGER.info("Fetched the Neo4J Node Id: " + neo4jNode.get(GraphDACParams.identifier.name()) + " | [Node Id: '"
				+ node.getIdentifier() + "']");

		boolean isValidUpdateOperation = false;

		// Fetching Version Check Mode ('OFF', 'STRICT', 'LENIENT')
		String objectType = (String) neo4jNode.get(SystemProperties.IL_FUNC_OBJECT_TYPE.name());
		String nodeType = (String) neo4jNode.get(SystemProperties.IL_SYS_NODE_TYPE.name());
		String versionCheckMode = null;
		if (StringUtils.isNotBlank(objectType))
			versionCheckMode = DefinitionNodeUtil.getMetadataValue(graphId, objectType,
					GraphDACParams.versionCheckMode.name());
		LOGGER.info("Version Check Mode in Definition Node: " + versionCheckMode + " for Object Type: "
				+ node.getObjectType());

		// Checking if the 'versionCheckMode' Property is not specified,
		// then default Mode is OFF
		if (StringUtils.isBlank(versionCheckMode)
				|| StringUtils.equalsIgnoreCase(SystemNodeTypes.DEFINITION_NODE.name(), nodeType))
			versionCheckMode = NodeUpdateMode.OFF.name();

		// Checking of Node Update Version Checking is either 'STRICT'
		// or 'LENIENT'.
		// If Number of Modes are increasing then the Condition should
		// be checked for 'OFF' Mode Only.
		LOGGER.info(versionCheckMode);
		if (StringUtils.equalsIgnoreCase(NodeUpdateMode.STRICT.name(), versionCheckMode)
				|| StringUtils.equalsIgnoreCase(NodeUpdateMode.LENIENT.name(), versionCheckMode)) {
			boolean isValidVersionKey = isValidVersionKey(graphId, node, neo4jNode);
			LOGGER.info("Is Valid Version Key ? " + isValidVersionKey);

			if (!isValidVersionKey) {
				// Checking for Strict Mode
				LOGGER.debug("Checking for Node Update Operation Mode is 'STRICT' for Node Id: " + node.getIdentifier());
				if (StringUtils.equalsIgnoreCase(NodeUpdateMode.STRICT.name(), versionCheckMode))
					throw new ClientException(DACErrorCodeConstants.ERR_STALE_VERSION_KEY.name(),
							DACErrorMessageConstants.INVALID_VERSION_KEY_ERROR + " | [Unable to Update the Data.]");

				// Checking for Lenient Mode
				LOGGER.debug(
						"Checking for Node Update Operation Mode is 'LENIENT' for Node Id: " + node.getIdentifier());
				if (StringUtils.equalsIgnoreCase(NodeUpdateMode.LENIENT.name(), versionCheckMode))
					node.getMetadata().put(GraphDACParams.NODE_UPDATE_STATUS.name(),
							GraphDACParams.STALE_DATA_UPDATED.name());

				LOGGER.debug("Update Operation is Valid for Node Id: " + node.getIdentifier());
			}
		}

		// Update Operation is Valid
		isValidUpdateOperation = true;

		LOGGER.debug("Is Valid Update Operation ? " + isValidUpdateOperation);

		return isValidUpdateOperation;
	}

	private boolean isValidVersionKey(String graphId, Node node, Map<String, Object> neo4jNode) {
		LOGGER.debug("Node: ", node);

		boolean isValidVersionKey = false;
		String versionKey = (String) node.getMetadata().get(GraphDACParams.versionKey.name());
		LOGGER.debug("Data Node Version Key Value: " + versionKey + " | [Node Id: '" + node.getIdentifier() + "']");
		if (StringUtils.isBlank(versionKey))
			throw new ClientException(DACErrorCodeConstants.BLANK_VERSION.name(),
					DACErrorMessageConstants.BLANK_VERSION_KEY_ERROR + " | [Node Id: " + node.getIdentifier() + "]");

		// Reading Last Updated On time stamp from Neo4J Node
		String lastUpdateOn = (String) neo4jNode.get(GraphDACParams.lastUpdatedOn.name());
		LOGGER.debug("Fetched 'lastUpdatedOn' Property from the Neo4J Node Id: "
				+ neo4jNode.get(GraphDACParams.identifier.name()) + " as 'lastUpdatedOn': " + lastUpdateOn
				+ " | [Node Id: '" + node.getIdentifier() + "']");
		if (StringUtils.isBlank(lastUpdateOn))
			throw new ClientException(DACErrorCodeConstants.INVALID_TIMESTAMP.name(),
					DACErrorMessageConstants.INVALID_LAST_UPDATED_ON_TIMESTAMP + " | [Node Id: " + node.getIdentifier()
							+ "]");

		String graphVersionKey = (String) neo4jNode.get(GraphDACParams.versionKey.name());
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

	private Map<String, Object> getNeo4jNodeProperty(String graphId, String identifier) {
		Map<String, Object> prop = null;
		Driver driver = DriverUtil.getDriver(graphId);
		LOGGER.debug("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			try (Transaction tx = session.beginTransaction()) {
				String query = "match (n:" + graphId + "{IL_UNIQUE_ID:'" + identifier + "'}) return (n) as result";
				StatementResult result = tx.run(query);
				if (result.hasNext()) {
					Record record = result.next();
					InternalNode node = (InternalNode) record.values().get(0).asObject();
					prop = node.asMap();
				}
				tx.success();
				tx.close();
			} catch (Exception e) {
				throw new ServerException(DACErrorCodeConstants.CONNECTION_PROBLEM.name(),
						DACErrorMessageConstants.CONNECTION_PROBLEM + " | " + e.getMessage());
			}
		}
		return prop;

	}

}
