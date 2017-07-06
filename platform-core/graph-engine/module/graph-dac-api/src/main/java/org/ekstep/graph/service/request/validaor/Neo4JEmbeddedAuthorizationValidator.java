package org.ekstep.graph.service.request.validaor;

import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;


public class Neo4JEmbeddedAuthorizationValidator {
	
	private static ILogger LOGGER = new PlatformLogger(Neo4JEmbeddedAuthorizationValidator.class.getName());
	
	public boolean isAuthorizedToken() {
		LOGGER.log("Checking with Authorization.");
		return false;
	}

}
