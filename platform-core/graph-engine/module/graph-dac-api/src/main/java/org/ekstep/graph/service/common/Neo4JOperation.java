package org.ekstep.graph.service.common;

public enum Neo4JOperation {
	
	CREATE_NODE(0, "Create New Node."),
	UPSERT_NODE(1, "Upsert Node."),
	UPDATE_NODE(2, "Update Node."),
	SEARCH_NODE(3, "Search Node."),
	CREATE_UNIQUE(4, "Create Unique Node."),
	CREATE_INDEX(5, "Create Index."),
	IMPORT_NODES(6, "Import Index."),
	UPDATE_PROPERTY(7, "Update Property Value."),
	UPDATE_PROPERTIES(8, "Update Property Values (Bulk Update Operation)."),
	REMOVE_PROPERTY(9, "Remove Property Value."),
	REMOVE_PROPERTIES(10, "Remove Property Values (Bulk Remove Operation)."),
	DELETE_NODE(11, "Delete Node."),
	UPSERT_ROOTNODE(12, "Upsert Root Node.");
	
	private final int code;
	private final String description;

	private Neo4JOperation(int code, String description) {
		    this.code = code;
		    this.description = description;
		  }

	public String getDescription() {
		return description;
	}

	public int getCode() {
		return code;
	}

	@Override
	public String toString() {
		return code + ": " + description;
	}
}
