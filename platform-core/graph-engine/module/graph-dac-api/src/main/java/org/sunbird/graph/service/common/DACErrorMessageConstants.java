package org.sunbird.graph.service.common;

public class DACErrorMessageConstants {

	public static final String MISSING_DEFINTION_ERROR = "Error! Missing Definition Node for given Object Type and Graph Id.";
	
	public static final String INVALID_VERSION_KEY_ERROR = "Error! Invalid Data Version Key.";
	
	public static final String BLANK_VERSION_KEY_ERROR = "Error! Version Key cannot be Blank.";

	public static final String STALE_DATA_UPDATED_WARNING = "Warning! Object has been Updated with Stale Data.";
	
	public static final String INVALID_GRAPH_ID = "Error! Invalid Graph Id.";
	
	public static final String INVALID_USERNAME = "Error! Invalid Username.";
	
	public static final String INVALID_PASSWORD = "Error! Invalid Password.";
	
	public static final String INVALID_PRINCIPAL = "Error! Invalid Principal.";
	
	public static final String INVALID_REALM = "Error! Invalid Realm.";
	
	public static final String INVALID_SCHEME = "Error! Invalid Scheme.";
	
	public static final String INVALID_CONFIGURATION = "Error! Something is wrong with the Database Configuration Values.";
	
	public static final String INVALID_NODE = "Error! Invalid Node (Object).";

	public static final String INVALID_IDENTIFIER = "Error! Invalid Node (Object) identifier.";
	
	public static final String INVALID_NODE_LIST = "Error! Invalid Node List (either 'null' or Empty).";
	
	public static final String INVALID_PROPERTY = "Error! Invalid Property (either 'null' or Empty).";

	public static final String INVALID_ROOT_NODE = "Error! Invalid Root Node.";
	
	public static final String INVALID_PROPERTIES = "Error! Invalid Properties.";
	
	public static final String INVALID_PROPERTY_KEY = "Error! Invalid Property Key (either 'null' or Empty).";
	
	public static final String INVALID_PROPERTY_KEY_LIST = "Error! Invalid Property Key List (either 'null' or Empty).";
	
	public static final String INVALID_INDEX_PROPERTY_KEY_LIST = "Error! Invalid Index Property Key List (either 'null' or Empty).";
	
	public static final String INVALID_START_NODE_ID = "Error! Invalid Start Node Id.";
	
	public static final String INVALID_END_NODE_ID = "Error! Invalid End Node Id.";
	
	public static final String INVALID_RELATION_TYPE = "Error! Invalid Relation Type.";
	
	public static final String INVALID_START_NODE_ID_LIST = "Error! Invalid Start Node Id List (either 'null' or Empty).";
	
	public static final String INVALID_END_NODE_ID_LIST = "Error! Invalid End Node Id List (either 'null' or Empty).";
	
	public static final String INVALID_COLLECTION_NODE_ID = "Error! Invalid Collection Node Id.";
	
	public static final String INVALID_COLLECTION_NODE = "Error! Invalid Collection Node (Object).";
	
	public static final String INVALID_COLLECTION_MEMBERS = "Error! Invalid Collection Members (Object) [Atleast one needed].";
	
	public static final String INVALID_INDEX_PROPERTY = "Error! Invalid Index Property.";
	
	public static final String INVALID_TASK_ID = "Error! Invalid Task Id.";
	
	public static final String INVALID_IMPORT_DATA = "Error! Invalid Import Data.";

	public static final String INVALID_REQUEST = "Error! Invalid Request (Object).";
	
	public static final String INVALID_NODE_ID = "Error! Invalid Node Id.";
	
	public static final String INVALID_SEARCH_CRITERIA = "Error! Invalid Search Criteria.";
	
	public static final String INVALID_QUERY = "Error! Invalid Query.";
	
	public static final String INVALID_PARAM_MAP = "Error! Invalid Parameter Map For Query.";
	
	public static final String INVALID_TRAVERSER = "Error! Invalid Traverser.";
	
	public static final String INVALID_DEPTH = "Error! Invalid Depth.";
	
	public static final String NODE_NOT_FOUND = "Error! Node(s) doesn't Exists.";

	public static final String CONNECTION_PROBLEM = "Error! Driver connection problem";
	
	public static final String INVALID_LAST_UPDATED_ON_TIMESTAMP = "Error! Invalid 'lastUpdatedOn' Time Stamp.";
	
	public static final String CACHE_ERROR = "Error! Redis cache error.";
	
	private DACErrorMessageConstants() {
		  throw new AssertionError();
	}

}
