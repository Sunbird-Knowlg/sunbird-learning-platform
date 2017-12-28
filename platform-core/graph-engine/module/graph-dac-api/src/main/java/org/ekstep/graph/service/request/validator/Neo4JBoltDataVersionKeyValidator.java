package org.ekstep.graph.service.request.validator;

import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.exception.ClientException;
import org.ekstep.graph.cache.util.RedisStoreUtil;
import org.ekstep.graph.common.DateUtils;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.service.common.DACConfigurationConstants;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.ekstep.graph.service.common.NodeUpdateMode;
import org.ekstep.graph.service.util.DefinitionNodeUtil;
import org.ekstep.graph.service.util.PassportUtil;
import org.ekstep.telemetry.logger.PlatformLogger;

public class Neo4JBoltDataVersionKeyValidator extends Neo4JBoltBaseValidator {

	public boolean validateUpdateOperation(String graphId, Node node, String versionCheckMode, String graphVersionKey) {

		boolean isValidUpdateOperation = false;
		String lastUpdateOn = null;

		if (StringUtils.isBlank(versionCheckMode)) {
			// Fetching Version Check Mode ('OFF', 'STRICT', 'LENIENT')
			versionCheckMode = DefinitionNodeUtil.getMetadataValue(graphId, node.getObjectType(),
					GraphDACParams.versionCheckMode.name());
			PlatformLogger.log("Version Check Mode in Definition Node: " + versionCheckMode + " for Object Type: "
					+ node.getObjectType());
			// Checking if the 'versionCheckMode' Property is not specified,
			// then default Mode is OFF
			if (StringUtils.isBlank(versionCheckMode))
				versionCheckMode = NodeUpdateMode.OFF.name();
			RedisStoreUtil.saveNodeProperty(graphId, node.getObjectType(), GraphDACParams.versionCheckMode.name(),
					versionCheckMode);
			PlatformLogger.log("Saving " + versionCheckMode + "for object type" + node.getObjectType() + ", graphId "
					+ graphId + " into Redis cache");

		}

		// Checking of Node Update Version Checking is either 'STRICT'
		// or 'LENIENT'.
		// If Number of Modes are increasing then the Condition should
		// be checked for 'OFF' Mode Only.
		PlatformLogger.log("versionCheckMode " + versionCheckMode + "for object type" + node.getObjectType()
				+ ", object id" + node.getIdentifier());
		if (StringUtils.equalsIgnoreCase(NodeUpdateMode.STRICT.name(), versionCheckMode)
				|| StringUtils.equalsIgnoreCase(NodeUpdateMode.LENIENT.name(), versionCheckMode)) {

			if (StringUtils.isBlank(graphVersionKey)) {
				// Fetching Neo4J Node
				Map<String, Object> neo4jNode = getNeo4jNodeProperty(graphId, node.getIdentifier());
				// New node... not found in the graph
				if (null == neo4jNode)
					return true;
				PlatformLogger.log("Fetched the Neo4J Node Id: " + neo4jNode.get(GraphDACParams.identifier.name())
						+ " | [Node Id: '" + node.getIdentifier() + "']");

				graphVersionKey = (String) neo4jNode.get(GraphDACParams.versionKey.name());
				lastUpdateOn = (String) neo4jNode.get(GraphDACParams.lastUpdatedOn.name());
				// RedisStoreUtil.saveNodeProperty(graphId,
				// node.getIdentifier(), GraphDACParams.versionKey.name(),
				// graphVersionKey);
			}

			boolean isValidVersionKey = isValidVersionKey(graphId, node, graphVersionKey, lastUpdateOn);
			PlatformLogger.log("Is Valid Version Key ? " + isValidVersionKey);

			if (!isValidVersionKey) {
				// Checking for Strict Mode
				PlatformLogger.log(
						"Checking for Node Update Operation Mode is 'STRICT' for Node Id: " + node.getIdentifier());
				if (StringUtils.equalsIgnoreCase(NodeUpdateMode.STRICT.name(), versionCheckMode))
					throw new ClientException(DACErrorCodeConstants.ERR_STALE_VERSION_KEY.name(),
							DACErrorMessageConstants.INVALID_VERSION_KEY_ERROR + " | [Unable to Update the Data.]");

				// Checking for Lenient Mode
				PlatformLogger.log(
						"Checking for Node Update Operation Mode is 'LENIENT' for Node Id: " + node.getIdentifier());
				if (StringUtils.equalsIgnoreCase(NodeUpdateMode.LENIENT.name(), versionCheckMode))
					node.getMetadata().put(GraphDACParams.NODE_UPDATE_STATUS.name(),
							GraphDACParams.STALE_DATA_UPDATED.name());

				PlatformLogger.log("Update Operation is Valid for Node Id: " + node.getIdentifier());
			}
		}

		// Update Operation is Valid
		isValidUpdateOperation = true;

		PlatformLogger.log("Is Valid Update Operation ? " + isValidUpdateOperation);

		return isValidUpdateOperation;
	}

	private boolean isValidVersionKey(String graphId, Node node, String graphVersionKey, String lastUpdateOn) {

		boolean isValidVersionKey = false;
		String versionKey = (String) node.getMetadata().get(GraphDACParams.versionKey.name());
		PlatformLogger
				.log("Data Node Version Key Value: " + versionKey + " | [Node Id: '" + node.getIdentifier() + "']");
		if (StringUtils.isBlank(versionKey))
			throw new ClientException(DACErrorCodeConstants.BLANK_VERSION.name(),
					DACErrorMessageConstants.BLANK_VERSION_KEY_ERROR + " | [Node Id: " + node.getIdentifier() + "]");

		if (StringUtils.isBlank(graphVersionKey)) {

			if (StringUtils.isBlank(lastUpdateOn))
				throw new ClientException(DACErrorCodeConstants.INVALID_TIMESTAMP.name(),
						DACErrorMessageConstants.INVALID_LAST_UPDATED_ON_TIMESTAMP + " | [Node Id: "
								+ node.getIdentifier() + "]");

			PlatformLogger.log("Fetched 'lastUpdatedOn' Property from  Node Id: " + node.getIdentifier()
					+ " as 'lastUpdatedOn': " + lastUpdateOn);

			graphVersionKey = String.valueOf(DateUtils.parse(lastUpdateOn).getTime());
			PlatformLogger.log(
					"'lastUpdatedOn' Time Stamp: " + graphVersionKey + " | [Node Id: '" + node.getIdentifier() + "']");

		}

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

}
