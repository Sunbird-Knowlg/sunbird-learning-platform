package com.ilimi.taxonomy.content.common;

public class ContentErrorMessageConstants {
	
	public static final String XML_PARSE_CONFIG_ERROR = "Parse configuration error while parsing Content XML file.";
	
	public static final String XML_NOT_WELL_FORMED_ERROR = "Content XML is not well formed.";
	
	public static final String XML_IO_ERROR = "Input/Output Error while reading XML file.";

	public static final String XML_OBJECT_CONVERSION_CASTING_ERROR = "Something went wrong while converting Content XML Objects.";
	
	public static final String CONTROLLER_ASSESSMENT_ITEM_JSON_OBJECT_CONVERSION_CASTING_ERROR = "Invalid JSON !!! Something went wrong while converting Assessment Item JSON Objects.";
	
	public static final String ASSESSMENT_MANAGER_REQUEST_OBJECT_CREATION_ERROR = "Error! While Creating Assessment Manager Object.";
	
	public static final String ASSESSMENT_ITEM_CREATOR_PROCESSOR_ERROR = "Something went wrong while creating the Assessment Items.";
	
	public static final String ASSET_CREATOR_PROCESSOR_ERROR = "Something went wrong while creating the Assets.";
	
	public static final String FILE_DOES_NOT_EXIST = "File doesn't exist.";
	
	public static final String INVALID_JSON = "Invalid JSON.";
	
	public static final String FILE_READ_ERROR = "Error! While reading file.";
	
	private ContentErrorMessageConstants(){
	  throw new AssertionError();
	}

}
