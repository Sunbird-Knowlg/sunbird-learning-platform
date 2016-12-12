package org.ekstep.graph.service.common;

public class DACConfigurationConstants {
	
	public static final String DEFAULT_DATABASE_POLICY = "Embedded";
	
	public static final String ACTIVE_DATABASE_POLICY = "Embedded";
	
	public static final String NEO4J_SERVER_USERNAME = "neo4j";
	
	public static final String NEO4J_SERVER_PASSWORD = "neo4j";
	
	public static final String NEO4J_SERVER_AUTH_TYPE = "basic";
	
	public static final String NEO4J_SERVER_AUTH_REALM = "";
	
	public static final String NEO4J_SERVER_AUTH_PRINCIPAL = "";
	
	public static final String NEO4J_SERVER_AUTH_SCHEME = "";
	
	public static final String NEO4J_SERVER_CONNECTION_TRUST_STRATEGY = "all";
	
	public static final String NEO4J_SERVER_CONNECTION_TRUST_STRATEGY_CERTIFICATE_FILE = "";
	
	public static final int NEO4J_SERVER_MAX_IDLE_SESSION = 20;
	
	public static final boolean IS_NEO4J_SERVER_CONNECTION_ENCRYPTION_ALLOWED = true;
	
	public static final boolean IS_SETTING_NEO4J_SERVER_MAX_IDLE_SESSION_ENABLED = true;
	
	public static final boolean IS_NEO4J_SERVER_TRUST_STRATEGY_ENABLED = true;
	
	private DACConfigurationConstants() {
		  throw new AssertionError();
	}

}
