package org.ekstep.graph.service.common;

public enum Neo4JOperation {
	
	CREATE_NODE(0, "Create New Node."),
	UPSERT_NODE(1, "Upsert Node."),
	UPDATE_NODE(2, "Update Node."),
	IMPORT_NODES(3, "Import Index."),
	UPDATE_PROPERTY(4, "Update Property Value."),
	UPDATE_PROPERTIES(5, "Update Property Values (Bulk Update Operation)."),
	REMOVE_PROPERTY(6, "Remove Property Value."),
	REMOVE_PROPERTIES(7, "Remove Property Values (Bulk Remove Operation)."),
	DELETE_NODE(8, "Delete Node."),
	UPSERT_ROOT_NODE(9, "Upsert Root Node."),
	CREATE_UNIQUE_CONSTRAINT(10, "Create Unique Node."),
	CREATE_INDEX(11, "Create Index."),
	DELETE_GRAPH(12, "Delete Graph."),
	CREATE_RELATION(13, "Create Relation."),
	UPDATE_RELATION(14, "Update Relation."),
	DELETE_RELATION(15, "Delete Relation."),
	CREATE_INCOMING_RELATIONS(16, "Create Incoming Relations."),
	CREATE_OUTGOING_RELATIONS(17, "Create Outgoing Relations."),
	DELETE_INCOMING_RELATIONS(18, "Delete Incoming Relations."),
	DELETE_OUTGOING_RELATIONS(19, "Delete Outgoing Relations."),
	REMOVE_RELATION_METADATA(20, "Remove Relation Metadata."),
	CREATE_COLLECTION(21, "Create Collection."),
	DELETE_COLLECTION(22, "Delete Collection."),
	IMPORT_GRAPH(23, "Import Graph."),
	GET_NODE_BY_ID(24, "Get Node By Node Identifier."),
	GET_NODE_BY_UNIQUE_ID(25, "Get Node By Unique Id."),
	GET_NODES_BY_PROPERTY(26, "Get Nodes By Property."),
	GET_NODES_BY_SEARCH_CRITERIA(27, "Get Nodes By Search Criteria."),
	GET_NODE_PROPERTY(28, "Get Node Property."),
	GET_ALL_NODES(29, "Get All Nodes."),
	GET_ALL_RELATIONS(30, "Get All Relations."),
	GET_RELATION_PROPERTY(31, "Get Relation Property."),
	GET_RELATION(32, "Get Relation."),
	CHECK_CYCLIC_LOOP(33, "Check Cyclic Loop."),
	EXECUTE_QUERY(34, "Execute Query."),
	SEARCH_NODES(35, "Search Nodes."),
	GET_NODES_COUNT(36, "Get Nodes Count."),
	TRAVERSE(37, "Traverse."),
	TRAVERSE_SUB_GRAPH(38, "Traverse Sub Graph."),
	GET_SUB_GRAPH(39, "Get Sub Graph."),
	GET_RELATION_BY_ID(40, "Get Relation By Id.");
	
	
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
