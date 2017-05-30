package org.ekstep.graph.service.request.validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Neo4JEmbeddedAuthorizationValidator {
	
	private static Logger LOGGER = LogManager.getLogger(Neo4JEmbeddedAuthorizationValidator.class.getName());
	
	public boolean isAuthorizedToken() {
		LOGGER.info("Checking with Authorization.");
		return false;
	}

}
