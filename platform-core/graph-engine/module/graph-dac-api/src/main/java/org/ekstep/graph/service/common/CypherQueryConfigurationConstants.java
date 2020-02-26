package org.ekstep.graph.service.common;

public class CypherQueryConfigurationConstants {

	public final static String OPEN_CURLY_BRACKETS = "{";

	public final static String CLOSE_CURLY_BRACKETS = "}";

	public final static String OPEN_COMMON_BRACKETS_WITH_NODE_OBJECT_VARIABLE = "(ee:";

	public final static String OPEN_COMMON_BRACKETS = "(";

	public final static String CLOSE_COMMON_BRACKETS = ")";

	public final static String COLON = ": ";

	public final static String SINGLE_QUOTE = "'";

	public final static String COMMA = ", ";

	public final static String BLANK_SPACE = " ";

	public final static String EQUALS = "=";

	public final static String DEFAULT_CYPHER_NODE_OBJECT = "ee";

	public final static String DEFAULT_CYPHER_NODE_OBJECT_II = "aa";

	public final static String DOT = ".";

	public final static String DASH = "-";

	public final static String DEFAULT_CYPHER_RELATION_OBJECT = "r";

	public final static String DEFAULT_CYPHER_START_NODE_OBJECT = "__startNode";

	public final static String DEFAULT_CYPHER_END_NODE_OBJECT = "__endNode";

	public final static String DEFAULT_CYPHER_COUNT_OBJECT = "__count";

	public final static String OPEN_SQUARE_BRACKETS = "[";

	public final static String CLOSE_SQUARE_BRACKETS = "]";

	private CypherQueryConfigurationConstants() {
		throw new AssertionError();
	}

}
