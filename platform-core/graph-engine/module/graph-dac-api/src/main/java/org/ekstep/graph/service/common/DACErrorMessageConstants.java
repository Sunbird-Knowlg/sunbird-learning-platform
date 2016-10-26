package org.ekstep.graph.service.common;

public class DACErrorMessageConstants {
	
	public static final String ERROR_INVALID_POLICY_ID = "Error! Policy identifier is either 'null' or Invalid.";
	
	public static final String ERROR_MISSING_DEFINTION = "Error! Missing Definition Node for given Object Type and Graph Id.";
	
	public static final String ERROR_INVALID_VERSION_KEY = "Error! Invalid Data Version Key.";
	
	public static final String ERROR_BLANK_VERSION_KEY = "Error! Version Key cannot be Blank.";
	
	public static final String WARNING_STALE_DATA_UPDATED = "Warning! Object has been Updated with Stale Data.";
	
	private DACErrorMessageConstants() {
		  throw new AssertionError();
	}

}
