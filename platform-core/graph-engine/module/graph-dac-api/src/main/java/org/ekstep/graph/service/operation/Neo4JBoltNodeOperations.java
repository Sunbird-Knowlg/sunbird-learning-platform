package org.ekstep.graph.service.operation;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.ekstep.graph.service.common.Neo4JOperation;
import org.ekstep.graph.service.util.DriverUtil;
import org.ekstep.graph.service.util.QueryUtil;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.exceptions.ClientException;

import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.graph.dac.model.Node;

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

		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				LOGGER.info("Session Initialised. | [Graph Id: " + graphId + "]");

				StatementResult result = session.run(QueryUtil.getQuery(Neo4JOperation.CREATE_NODE, node));
				for (Record record : result.list())
					System.out.println(record);

			}

		}

		return node;
	}

	public com.ilimi.graph.dac.model.Node addNode(String graphId, com.ilimi.graph.dac.model.Node node,
			Request request) {
		
		return node;
	}

	public com.ilimi.graph.dac.model.Node updateNode(String graphId, com.ilimi.graph.dac.model.Node node,
			Request request) {
		
		return node;
	}

	public void importNodes(String graphId, List<com.ilimi.graph.dac.model.Node> nodes, Request request) {

	}

	public void updatePropertyValue(String graphId, String nodeId, Property property, Request request) {
	}

	public void updatePropertyValues(String graphId, String nodeId, Map<String, Object> metadata, Request request) {
	}

	public void removePropertyValue(String graphId, String nodeId, String key, Request request) {
	}

	public void removePropertyValues(String graphId, String nodeId, List<String> keys, Request request) {
	}

	public void deleteNode(String graphId, String nodeId, Request request) {
	}

	public Node upsertRootNode(String graphId, Request request) {
		
		return null;
	}

}
