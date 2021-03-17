package org.sunbird.cassandra.store;

/*
 * @author pradyumna
 */
public interface Constants {
  
	//CONSTANT
	public static final String IDENTIFIER="id";
	public static final String SUCCESS="SUCCESS";
	public static final String RESPONSE="response";
	public static final String SESSION_IS_NULL="cassandra session is null for this ";
	public static final String CLUSTER_IS_NULL="cassandra cluster value is null for this ";
	public static final String QUE_MARK="?";
	public static final String INSERT_INTO="INSERT INTO ";
	public static final String OPEN_BRACE_WITH_SPACE=" (";
	public static final String DOT=".";
	public static final String VALUES_WITH_BRACE=") VALUES (";
	public static final String COMMA_WITH_SPACE=", ";
	public static final String CLOSING_BRACE=");";
	public static final String OPEN_BRACE="(";
	public static final String COMMA=",";
	public static final String COMMA_BRAC= "),";
	public static final String UPDATE="UPDATE ";
	public static final String SET=" SET ";
	public static final String WHERE=" where ";
	public static final String FROM=" FROM ";
	public static final String INCORRECT_DATA = "Incorrect Data";
	public static final String EQUAL=" = ";
	public static final String WHERE_ID = "where id";
	public static final String EQUAL_WITH_QUE_MARK= " = ? ";
	public static final String SEMICOLON = ";";
	public static final String IF_EXISTS = " IF EXISTS;";
	public static final String IF_NOT_EXISTS = " IF NOT EXISTS;";
	public static final String SELECT = "SELECT";
	public static final String IN = "IN";
}
