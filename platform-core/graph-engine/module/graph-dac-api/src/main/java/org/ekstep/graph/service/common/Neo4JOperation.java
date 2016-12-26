package org.ekstep.graph.service.common;

public enum Neo4JOperation {
	
	CREATE_NODE(0, "Create New Node."),
	UPSERT_NODE(1, "Upsert Node."),
	UPDATE_NODE(2, "Update Node."),
	SEARCH_NODE(3, "Search Node."),
	CREATE_UNIQUE_CONSTRAINT(4, "Create Unique Node."),
	CREATE_INDEX(5, "Create Index."),
	IMPORT_NODES(6, "Import Index."),
	UPDATE_PROPERTY(7, "Update Property Value."),
	UPDATE_PROPERTIES(8, "Update Property Values (Bulk Update Operation)."),
	REMOVE_PROPERTY(9, "Remove Property Value."),
	REMOVE_PROPERTIES(10, "Remove Property Values (Bulk Remove Operation)."),
	DELETE_NODE(11, "Delete Node."),
	UPSERT_ROOT_NODE(12, "Upsert Root Node."),
	DELETE_GRAPH(13, "Delete Graph."),
	CREATE_RELATION(14, "Create Relation."),
	UPDATE_RELATION(15, "Update Relation."),
	DELETE_RELATION(16, "Delete Relation."),
	CREATE_INCOMING_RELATIONS(17, "Create Incoming Relations."),
	CREATE_OUTGOING_RELATIONS(18, "Create Outgoing Relations."),
	DELETE_INCOMING_RELATIONS(19, "Delete Incoming Relations."),
	DELETE_OUTGOING_RELATIONS(20, "Delete Outgoing Relations."),
	REMOVE_RELATION_METADATA(21, "Remove Relation Metadata."),
	CREATE_COLLECTION(22, "Create Collection."),
	DELETE_COLLECTION(23, "Delete Collection."),
	IMPORT_GRAPH(24, "Import Graph."),
	GET_NODE_BY_ID(25, "Get Node By Node Identifier."),
	GET_NODE_BY_UNIQUE_ID(26, "Get Node By Unique Id."),
	GET_NODES_BY_PROPERTY(27, "Get Nodes By Property."),
	GET_NODES_BY_SEARCH_CRITERIA(28, "Get Nodes By Search Criteria."),
	GET_NODE_PROPERTY(29, "Get Node Property."),
	GET_ALL_NODES(30, "Get All Nodes."),
	GET_ALL_RELATIONS(31, "Get All Relations."),
	GET_RELATION_PROPERTY(32, "Get Relation Property."),
	GET_RELATION(33, "Get Relation."),
	CHECK_CYCLIC_LOOP(34, "Check Cyclic Loop."),
	EXECUTE_QUERY(35, "Execute Query."),
	SEARCH_NODES(36, "Search Nodes."),
	GET_NODES_COUNT(37, "Get Nodes Count."),
	TRAVERSE(38, "Traverse."),
	TRAVERSE_SUB_GRAPH(39, "Traverse Sub Graph."),
	GET_SUB_GRAPH(40, "Get Sub Graph.");
	
	
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
