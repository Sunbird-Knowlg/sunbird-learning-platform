package org.ekstep.graph.service.factory;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.graph.service.IGraphDatabaseService;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.ekstep.graph.service.impl.Neo4JBoltImpl;
import org.ekstep.graph.service.impl.Neo4JEmbeddedImpl;

import com.ilimi.common.exception.ClientException;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.common.util.PlatformLogger;

public class GraphServiceFactory {

	private static ILogger LOGGER = PlatformLogManager.getLogger();

	static IGraphDatabaseService bolt = new Neo4JBoltImpl();
	static IGraphDatabaseService embedded = new Neo4JEmbeddedImpl();
	static IGraphDatabaseService cachedWithJournaling = new Neo4JEmbeddedImpl();

	public static IGraphDatabaseService getDatabaseService(String databasePolicy) {
		if (StringUtils.isBlank(databasePolicy))
			throw new ClientException(DACErrorCodeConstants.INVALID_POLICY.name(),
					DACErrorMessageConstants.INVALID_POLICY_ID_ERROR + " | [Policy Id " + databasePolicy + "]");
		IGraphDatabaseService service = embedded;

		LOGGER.log("Graph Database Poilicy: " , databasePolicy);

		switch (databasePolicy.toUpperCase()) {
		case "EMBEDDED":
			service = embedded;
			break;

		case "BOLT":
			service = bolt;
			break;
			
		case "CACHED_WITH_JOURNALING":
			service = cachedWithJournaling;
			break;

		default:
			throw new ClientException(DACErrorCodeConstants.INVALID_POLICY.name(),
					DACErrorMessageConstants.INVALID_POLICY_ID_ERROR + " | [Policy Id " + databasePolicy + "]");
		}

		return service;
	}

}
