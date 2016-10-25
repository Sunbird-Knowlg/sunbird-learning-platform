package org.ekstep.graph.service.factory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.IGraphDatabaseService;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.ekstep.graph.service.impl.Neo4JBoltImpl;
import org.ekstep.graph.service.impl.Neo4JEmbeddedImpl;

import com.ilimi.common.exception.ClientException;

public class GraphServiceFactory {

	private static Logger LOGGER = LogManager.getLogger(GraphServiceFactory.class.getName());

	static IGraphDatabaseService bolt = new Neo4JBoltImpl();
	static IGraphDatabaseService embedded = new Neo4JEmbeddedImpl();

	public static IGraphDatabaseService getDatabaseService(String databasePolicy) {
		if (StringUtils.isBlank(databasePolicy))
			throw new ClientException(DACErrorCodeConstants.INVALID_POLICY.name(),
					DACErrorMessageConstants.ERROR_INVALID_POLICY_ID + " | [Policy Id " + databasePolicy + "]");
		IGraphDatabaseService service = embedded;

		LOGGER.info("Graph Database Poilicy: " + databasePolicy);

		switch (databasePolicy.toUpperCase()) {
		case "EMBEDDED":
			service = embedded;
			break;

		case "BOLT":
			service = bolt;
			break;

		default:
			throw new ClientException(DACErrorCodeConstants.INVALID_POLICY.name(),
					DACErrorMessageConstants.ERROR_INVALID_POLICY_ID + " | [Policy Id " + databasePolicy + "]");
		}

		return service;
	}

}
