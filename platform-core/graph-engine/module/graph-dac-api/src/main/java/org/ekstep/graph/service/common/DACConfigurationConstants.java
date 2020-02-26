package org.ekstep.graph.service.common;

public class DACConfigurationConstants {
	
	public static final String NEO4J_SERVER_USERNAME = "neo4j";
	
	public static final String NEO4J_SERVER_PASSWORD = "neo4j";
	
	public static final String NEO4J_SERVER_AUTH_TYPE = "basic";
	
	public static final String NEO4J_SERVER_AUTH_REALM = "";
	
	public static final String NEO4J_SERVER_AUTH_PRINCIPAL = "";
	
	public static final String NEO4J_SERVER_AUTH_SCHEME = "";
	
	public static final String DEFAULT_ROUTE_PROP_PREFIX = "route.bolt.";
	
	public static final String DEFAULT_NEO4J_BOLT_ROUTE_ID = "all";
	
	public static final String PASSPORT_KEY_BASE_PROPERTY = "graph.passport.key.base";
	
	public static final String DOT = ".";
	
	public static final String UNDERSCORE = "_";

	public static final int NEO4J_SERVER_MAX_IDLE_SESSION = 20;

	private DACConfigurationConstants() {
		  throw new AssertionError();
	}

}
