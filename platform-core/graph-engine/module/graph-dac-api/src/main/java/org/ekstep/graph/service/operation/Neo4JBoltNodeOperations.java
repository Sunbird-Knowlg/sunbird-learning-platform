package org.ekstep.graph.service.operation;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.ekstep.graph.service.util.DriverUtil;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.exceptions.ClientException;

import com.ilimi.common.dto.Request;

public class Neo4JBoltNodeOperations {

	private static Logger LOGGER = LogManager.getLogger(Neo4JBoltNodeOperations.class.getName());

	public com.ilimi.graph.dac.model.Node upsertNode(String graphId, com.ilimi.graph.dac.model.Node node,
			Request request) {
		LOGGER.debug("Graph Id: " + graphId);
		LOGGER.debug("Graph Engine Node: " + node);
		LOGGER.debug("Request: " + request);

		if (StringUtils.isBlank(graphId))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(),
					DACErrorMessageConstants.INVALID_GRAPH_ID + " | [Upsert Node Operation Failed.]");

		if (null == node)
			throw new ClientException(DACErrorCodeConstants.INVALID_NODE.name(),
					DACErrorMessageConstants.INVALID_NODE + " | [Upsert Node Operation Failed.]");
		
		try(Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Upsert Node Operation Failed.]");
			
		}

		return node;
	}

}
