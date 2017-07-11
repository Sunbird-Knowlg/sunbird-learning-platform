package org.ekstep.graph.service.request.validator;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.common.NodeUpdateMode;
import org.ekstep.graph.service.request.validator.cache.LocalCache;

import com.ilimi.graph.cache.util.RedisStoreUtil;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;

public class Neo4jBoltGenericValidator extends Neo4JBoltDataVersionKeyValidator {

	private static Logger LOGGER = LogManager.getLogger(Neo4jBoltGenericValidator.class.getName());

	public boolean validateUpdateOperation(String graphId, Node node) {

		String nodeId = node.getIdentifier();
		String nodeType = node.getNodeType();
		String nodeObjType = node.getObjectType();

		// return as validate if node type is other than data node
		if (!nodeType.equalsIgnoreCase(SystemNodeTypes.DATA_NODE.name()))
			return true;

		String versionCheckMode = LocalCache.get(graphId, nodeObjType, GraphDACParams.versionCheckMode.name());
		LOGGER.info(
				"Version Check Mode in Local Cache: " + versionCheckMode + " for Object Type: " + node.getObjectType());

		if (StringUtils.isNotBlank(versionCheckMode)) {// from Local cache
			// versionCheckMode is from Local cache, check versionKey in Redis
			// or graph
			if (!StringUtils.equalsIgnoreCase(NodeUpdateMode.OFF.name(), versionCheckMode)) {
				String storedVersionKey = RedisStoreUtil.getNodeProperty(graphId, nodeId,
						GraphDACParams.versionKey.name());
				return validateUpdateOperation(graphId, node, versionCheckMode, storedVersionKey);
			}
		} else {
			versionCheckMode = RedisStoreUtil.getNodeProperty(graphId, nodeObjType,
					GraphDACParams.versionCheckMode.name());
			if (StringUtils.isNotBlank(versionCheckMode)) { // from Redis cache
				// versionCheckMode is from Redis cache, check versionKey in
				// Redis or graph
				// store versionCheckMode in local cache
				LocalCache.set(graphId, nodeObjType, GraphDACParams.versionCheckMode.name(), versionCheckMode);
				if (!StringUtils.equalsIgnoreCase(NodeUpdateMode.OFF.name(), versionCheckMode)) {
					String storedVersionKey = RedisStoreUtil.getNodeProperty(graphId, nodeId,
							GraphDACParams.versionKey.name());
					return validateUpdateOperation(graphId, node, versionCheckMode, storedVersionKey);
				}
			} else { // from graph - fall back
				// check both versionCheckMode and versionKey in graph
				return validateUpdateOperation(graphId, node, versionCheckMode, null);
			}
		}

		return true;
	}

}
