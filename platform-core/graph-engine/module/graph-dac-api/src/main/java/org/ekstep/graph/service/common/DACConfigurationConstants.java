package org.ekstep.graph.service.common;

public class DACConfigurationConstants {
	
	public static final String DEFAULT_DATABASE_POLICY = "Embedded";
	
	public static final String ACTIVE_DATABASE_POLICY = "Embedded";
	
	public static final String PASSPORT_KEY_BASE_PROPERTY = "graph.passport.key.base";
	
	public static final boolean IS_PASSPORT_AUTHENTICATION_ENABLED = true;
	
	private DACConfigurationConstants() {
		  throw new AssertionError();
	}

}
