package org.sunbird.graph.service.request.validator;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.exception.ClientException;
import org.sunbird.graph.cache.util.RedisStoreUtil;
import org.sunbird.graph.common.DateUtils;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.service.common.DACConfigurationConstants;
import org.sunbird.graph.service.common.DACErrorCodeConstants;
import org.sunbird.graph.service.common.DACErrorMessageConstants;
import org.sunbird.graph.service.common.NodeUpdateMode;
import org.sunbird.graph.service.util.DefinitionNodeUtil;

public class Neo4JBoltDataVersionKeyValidator extends Neo4JBoltBaseValidator {

	private String graphPassportKey = Platform.config.getString(DACConfigurationConstants.PASSPORT_KEY_BASE_PROPERTY);

	public boolean validateUpdateOperation(String graphId, Node node, String versionCheckMode, String graphVersionKey) {
		boolean isValidUpdateOperation = false;
		String lastUpdateOn = null;

		if (StringUtils.isBlank(versionCheckMode)) {
			// Fetching Version Check Mode ('OFF', 'STRICT', 'LENIENT')
			versionCheckMode = DefinitionNodeUtil.getMetadataValue(graphId, node.getObjectType(),
					GraphDACParams.versionCheckMode.name());
			// Checking if the 'versionCheckMode' Property is not specified,
			// then default Mode is OFF
			if (StringUtils.isBlank(versionCheckMode))
				versionCheckMode = NodeUpdateMode.OFF.name();
			RedisStoreUtil.saveNodeProperty(graphId, node.getObjectType(), GraphDACParams.versionCheckMode.name(),
					versionCheckMode);
		}

		// Checking of Node Update Version Checking is either 'STRICT'
		// or 'LENIENT'.
		// If Number of Modes are increasing then the Condition should
		// be checked for 'OFF' Mode Only.
		if (StringUtils.equalsIgnoreCase(NodeUpdateMode.STRICT.name(), versionCheckMode)
				|| StringUtils.equalsIgnoreCase(NodeUpdateMode.LENIENT.name(), versionCheckMode)) {

			if (StringUtils.isBlank(graphVersionKey)) {
				// Fetching Neo4J Node
				Map<String, Object> neo4jNode = getNeo4jNodeProperty(graphId, node.getIdentifier());
				// New node... not found in the graph
				if (null == neo4jNode)
					return true;

				graphVersionKey = (String) neo4jNode.get(GraphDACParams.versionKey.name());
				lastUpdateOn = (String) neo4jNode.get(GraphDACParams.lastUpdatedOn.name());
			}

			isValidUpdateOperation = isValidVersionKey(graphId, node, graphVersionKey, lastUpdateOn);

			if (!isValidUpdateOperation) {
				// Checking for Strict Mode
				if (StringUtils.equalsIgnoreCase(NodeUpdateMode.STRICT.name(), versionCheckMode))
					throw new ClientException(DACErrorCodeConstants.ERR_STALE_VERSION_KEY.name(),
							DACErrorMessageConstants.INVALID_VERSION_KEY_ERROR + " | [Unable to Update the Data.]");

				// Checking for Lenient Mode
				if (StringUtils.equalsIgnoreCase(NodeUpdateMode.LENIENT.name(), versionCheckMode))
					node.getMetadata().put(GraphDACParams.NODE_UPDATE_STATUS.name(),
							GraphDACParams.STALE_DATA_UPDATED.name());
			}
		}

		return isValidUpdateOperation;
	}

	private boolean isValidVersionKey(String graphId, Node node, String graphVersionKey, String lastUpdateOn) {
		String versionKey = (String) node.getMetadata().get(GraphDACParams.versionKey.name());

		if (StringUtils.isBlank(versionKey))
			throw new ClientException(DACErrorCodeConstants.BLANK_VERSION.name(),
					DACErrorMessageConstants.BLANK_VERSION_KEY_ERROR + " | [Node Id: " + node.getIdentifier() + "]");

		if (StringUtils.equals(graphPassportKey, versionKey)) {
			node.getMetadata().put(GraphDACParams.SYS_INTERNAL_LAST_UPDATED_ON.name(),
					DateUtils.formatCurrentDate());
			return true;
		} else {

			if (StringUtils.isBlank(graphVersionKey)) {
				if (StringUtils.isBlank(lastUpdateOn))
					throw new ClientException(DACErrorCodeConstants.INVALID_TIMESTAMP.name(),
							DACErrorMessageConstants.INVALID_LAST_UPDATED_ON_TIMESTAMP + " | [Node Id: "
									+ node.getIdentifier() + "]");
				graphVersionKey = String.valueOf(DateUtils.parse(lastUpdateOn).getTime());
			}
			if(!StringUtils.equalsIgnoreCase(versionKey, graphVersionKey)){
				graphVersionKey = getNeo4jNodeVersionKey(graphId, node.getIdentifier());
			}

			node.getMetadata().remove(GraphDACParams.SYS_INTERNAL_LAST_UPDATED_ON.name());
			return StringUtils.equalsIgnoreCase(versionKey, graphVersionKey);
		}
	}

	private String getNeo4jNodeVersionKey(String graphId, String identifier) {
		Map<String, Object> neo4jNode = getNeo4jNodeProperty(graphId, identifier);
		return (String) neo4jNode.get(GraphDACParams.versionKey.name());
	}
}
