package com.ilimi.taxonomy.content.common;

public class ContentConfigurationConstants {
	
	public static final String CONTENT_BASE_PATH = "/data/contentBundle/";
	
	public static final String BUCKET_NAME = "ekstep-public";
	
	public static final String FOLDER_NAME = "content";
	
	public static final String ITEM_CONTROLLER_FILE_EXTENSION = ".json";
	
	public static final String GRAPH_ID = "domain";
	
	public static final String URL_PATH_SEPERATOR = "/";
	
	public static final String FILENAME_EXTENSION_SEPERATOR = ".";
	
	public static final String DEFAULT_CONTENT_OWNER = "EkStep";
	
	public static final String DEFAULT_CONTENT_BODY = "<content></content>";
	
	public static final String DEFAULT_CONTENT_CODE_PREFIX = "org.ekstep.content.";
	
	public static final int AWS_UPLOAD_RESULT_URL_INDEX = 1;
	
	public static final int DEFAULT_CONTENT_PACKAGE_VERSION = 1;
	
	private ContentConfigurationConstants(){
	  throw new AssertionError();
	}

}
