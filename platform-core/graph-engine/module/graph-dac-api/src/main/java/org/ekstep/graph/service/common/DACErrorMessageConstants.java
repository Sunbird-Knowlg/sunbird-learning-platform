package org.ekstep.graph.service.common;

public class DACErrorMessageConstants {
	
	public static final String INVALID_POLICY_ID_ERROR = "Error! Policy identifier is either 'null' or Invalid.";
	
	public static final String MISSING_DEFINTION_ERROR = "Error! Missing Definition Node for given Object Type and Graph Id.";
	
	public static final String INVALID_VERSION_KEY_ERROR = "Error! Invalid Data Version Key.";
	
	public static final String BLANK_VERSION_KEY_ERROR = "Error! Version Key cannot be Blank.";
	
	public static final String OBJECT_CASTING_ERROR = "Error! Something went wrong while casting the Objects.";
	
	public static final String STALE_DATA_UPDATED_WARNING = "Warning! Object has been Updated with Stale Data.";
	
	public static final String INVALID_GRAPH_ID = "Error! Invalid Graph Id.";
	
	public static final String INVALID_USERNAME = "Error! Invalid Username.";
	
	public static final String INVALID_PASSWORD = "Error! Invalid Password.";
	
	public static final String INVALID_PRINCIPAL = "Error! Invalid Principal.";
	
	public static final String INVALID_REALM = "Error! Invalid Realm.";
	
	public static final String INVALID_SCHEME = "Error! Invalid Scheme.";
	
	public static final String INVALID_CONFIGURATION = "Error! Something is wrong with the Database Configuration Values.";
	
	public static final String INVALID_NODE = "Error! Invalid Node (Object).";
	
	public static final String INVALID_PATTERN_NODE = "Error! Invalid Pattern Node (Object).";
	
	public static final String INVALID_DRIVER_TYPE = "Error! Invalid Database Driver Type.";
	
	public static final String INVALID_OPERATION = "Error! Invalid Database Operation Type.";
	
	public static final String INVALID_IDENTIFIER = "Error! Invalid Node (Object) identifier.";
	
	public static final String SYSTEM_METADATA_CREATION_ERROR = "Error! While Creating System Metadata.";
	
	public static final String INVALID_PARAMETER_MAP = "Error! Invalid Parameter Map.";
	
	public static final String INVALID_NODE_LIST = "Error! Invalid Node List (either 'null' or Empty).";
	
	public static final String INVALID_PROPERTY = "Error! Invalid Property (either 'null' or Empty).";
	
	public static final String INVALID_PROPERTY_LIST = "Error! Invalid Property (Key) List (either 'null' or Empty).";
	
	private DACErrorMessageConstants() {
		  throw new AssertionError();
	}

}
