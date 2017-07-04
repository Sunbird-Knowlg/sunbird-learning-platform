package org.ekstep.graph.service.request.validator;

import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.common.DACConfigurationConstants;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.dac.enums.GraphDACParams;

public class Neo4JBoltAuthorizationValidator extends Neo4JBoltBaseValidator {

	private static Logger LOGGER = LogManager.getLogger(Neo4JBoltAuthorizationValidator.class.getName());

	public void validateAuthorization(String graphId, com.ilimi.graph.dac.model.Node node, Request request) {
		LOGGER.debug("Graph Id: ", graphId);
		LOGGER.debug("Graph Engine Node: ", node);
		LOGGER.debug("Request: ", request);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | [Invalid or 'null' Graph Id.]");
		if (null == node)
			throw new ClientException(DACErrorCodeConstants.INVALID_NODE.name(),
					DACErrorMessageConstants.INVALID_NODE + " | [Invalid or 'null' Node.]");
		if (null == request)
			throw new ClientException(DACErrorCodeConstants.INVALID_REQUEST.name(),
					DACErrorMessageConstants.INVALID_REQUEST + " | [Invalid or 'null' Request Object.]");

		if (BooleanUtils.isFalse(isAuthorized(graphId, node, request)))
			throw new ClientException(DACErrorCodeConstants.NOT_FOUND.name(),
					DACErrorMessageConstants.NODE_NOT_FOUND + " | [Node (Object) not found.]");

	}

	private boolean isAuthorized(String graphId, com.ilimi.graph.dac.model.Node node, Request request) {
		boolean isAuthorized = true;

		// Checking if Authorization Check Required, If not then the request is
		// authorized
		if (BooleanUtils.isTrue(isAuthorizationCheckRequired(request)) && StringUtils.isNotBlank(graphId)
				&& null != node && null != node.getMetadata() && null != request) {
			String consumerId = (String) request.getContext().get(GraphDACParams.CONSUMER_ID.name());
			LOGGER.info("Consumer Id: " + consumerId);

			LOGGER.info("Fetching the Neo4J Node Metadata.");
			Map<String, Object> neo4jNode = getNeo4jNodeProperty(graphId, node.getIdentifier());
			if (null != neo4jNode && !neo4jNode.isEmpty()) {
				LOGGER.info("Fetched the Neo4J Node Id: " + neo4jNode.get(GraphDACParams.identifier.name())
						+ " | [Node Id: '" + node.getIdentifier() + "']");

				String neo4jNodeConsumerId = (String) neo4jNode.get(GraphDACParams.consumerId.name());
				LOGGER.info("Neo4J Node Consumer Id: " + neo4jNodeConsumerId);

				if (!StringUtils.equals(consumerId, neo4jNodeConsumerId))
					isAuthorized = false;
			} else {
				// Setting the 'consumerId' for node since node doesn't exist in
				// Neo4J
				LOGGER.info("Setting the 'consumerId' Property Since it's a node creation operation.");
				node.getMetadata().put(GraphDACParams.consumerId.name(), consumerId);
			}

		}

		LOGGER.info("Is Authorized (For Node Id : '" + node.getIdentifier() + "') ? " + isAuthorized);
		return isAuthorized;
	}

	private boolean isAuthorizationCheckRequired(Request request) {
		boolean isCheckRequired = true;
		if (StringUtils.isBlank((String) request.getContext().get(GraphDACParams.CONSUMER_ID.name()))
				|| BooleanUtils.isFalse(DACConfigurationConstants.IS_USER_AUTHORIZATION_ENABLED))
			isCheckRequired = false;

		LOGGER.info("Authorization Check Required ? " + isCheckRequired);
		return isCheckRequired;
	}

}
