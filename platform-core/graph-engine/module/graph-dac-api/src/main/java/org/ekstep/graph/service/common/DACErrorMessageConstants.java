package org.ekstep.graph.service.common;

public class DACErrorMessageConstants {
	
	public static final String INVALID_POLICY_ID_ERROR = "Error! Policy identifier is either 'null' or Invalid.";
	
	public static final String MISSING_DEFINTION_ERROR = "Error! Missing Definition Node for given Object Type and Graph Id.";
	
	public static final String INVALID_VERSION_KEY_ERROR = "Error! Invalid Data Version Key.";
	
	public static final String BLANK_VERSION_KEY_ERROR = "Error! Version Key cannot be Blank.";
	
	public static final String OBJECT_CASTING_ERROR = "Error! Something went wrong while casting the Objects.";
	
	public static final String STALE_DATA_UPDATED_WARNING = "Warning! Object has been Updated with Stale Data.";
	
	public static final String INVALID_NODE = "Error! Invalid Node (Object).";
	
	public static final String INVALID_GRAPH_PASSPORT_KEY_BASE = "Error! Invalid Graph Passport Key Base in Graph Configuration.";
	
	public static final String INVALID_LAST_UPDATED_ON_TIMESTAMP = "Error! Invalid 'lastUpdatedOn' Time Stamp.";
	
	private DACErrorMessageConstants() {
		  throw new AssertionError();
	}

}
