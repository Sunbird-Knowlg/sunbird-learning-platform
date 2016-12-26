package org.ekstep.graph.service.request.validaor;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.ekstep.graph.service.common.NodeUpdateMode;
import org.ekstep.graph.service.util.DefinitionNodeUtil;
import org.ekstep.graph.service.util.DriverUtil;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.common.DateUtils;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;

public class Neo4JBoltDataVersionKeyValidator {

	private static Logger LOGGER = LogManager.getLogger(Neo4JBoltDataVersionKeyValidator.class.getName());

	public boolean validateUpdateOperation(String graphId, Node node) {
		LOGGER.debug("Graph Engine Node: ", node);

		boolean isValidUpdateOperation = false;

		// Fetching Version Check Mode ('OFF', 'STRICT', 'LENIENT')
		String versionCheckMode = DefinitionNodeUtil.getMetadataValue(graphId, node.getObjectType(),
				GraphDACParams.versionCheckMode.name());
		LOGGER.info("Version Check Mode in Definition Node: " + versionCheckMode + " for Object Type: "
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
			boolean isValidVersionKey = isValidVersionKey(graphId, node);
			LOGGER.info("Is Valid Version Key ? " + isValidVersionKey);

			if (!isValidVersionKey) {
				// Checking for Strict Mode
				LOGGER.info("Checking for Node Update Operation Mode is 'STRICT' for Node Id: " + node.getIdentifier());
				if (StringUtils.equalsIgnoreCase(NodeUpdateMode.STRICT.name(), versionCheckMode))
					throw new ClientException(DACErrorCodeConstants.INVALID_VERSION.name(),
							DACErrorMessageConstants.INVALID_VERSION_KEY_ERROR + " | [Unable to Update the Data.]");

				// Checking for Lenient Mode
				LOGGER.info(
						"Checking for Node Update Operation Mode is 'LENIENT' for Node Id: " + node.getIdentifier());
				if (StringUtils.equalsIgnoreCase(NodeUpdateMode.LENIENT.name(), versionCheckMode))
					node.getMetadata().put(GraphDACParams.NODE_UPDATE_STATUS.name(),
							GraphDACParams.STALE_DATA_UPDATED.name());

				// Update Operation is Valid
				isValidUpdateOperation = true;
				LOGGER.info("Update Operation is Valid for Node Id: " + node.getIdentifier());
			}
		}
		LOGGER.info("Is Valid Update Operation ? " + isValidUpdateOperation);

		return isValidUpdateOperation;
	}

	private boolean isValidVersionKey(String graphId, Node node) {
		LOGGER.debug("Node: ", node);

		boolean isValidVersionKey = false;

		String versionKey = (String) node.getMetadata().get(GraphDACParams.versionKey.name());
		LOGGER.info("Data Node Version Key Value: " + versionKey + " | [Node Id: '" + node.getIdentifier() + "']");

		// Fetching Neo4J Node
		Map<String, Object> neo4jNode = getNeo4jNodeProperty(graphId, node.getIdentifier());
		LOGGER.info("Fetched the Neo4J Node Id: " + neo4jNode.get(GraphDACParams.identifier.name()) + " | [Node Id: '"
				+ node.getIdentifier() + "']");

		// Reading Last Updated On time stamp from Neo4J Node
		String lastUpdateOn = (String) neo4jNode.get(GraphDACParams.lastUpdatedOn.name());
		LOGGER.info("Fetched 'lastUpdatedOn' Property from the Neo4J Node Id: "
				+ neo4jNode.get(GraphDACParams.identifier.name()) + " as 'lastUpdatedOn': " + lastUpdateOn
				+ " | [Node Id: '" + node.getIdentifier() + "']");

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

	private Map<String, Object> getNeo4jNodeProperty(String graphId, String identifier) {
		Map<String, Object> prop = null;
		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				try (Transaction tx = session.beginTransaction()) {
					String query = "match (n:" + graphId + "{identifier:'" + identifier + "'}) return (n) as result";
					StatementResult result = tx.run(query);
					if (result.hasNext()) {
						Record record = result.next();
						InternalNode node = (InternalNode) record.values().get(0).asObject();
						prop = node.asMap();
					}
					tx.success();
					tx.close();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					session.close();
				}
			}
		}
		return prop;

	}

}
