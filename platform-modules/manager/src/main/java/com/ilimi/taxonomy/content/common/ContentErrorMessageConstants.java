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
	
	public static final String INVALID_CONTENT_PACKAGE_FILE_MIME_TYPE_ERROR = "Error! Invalid Content Package Mime Type.";
	
	public static final String INVALID_CONTENT_PACKAGE_STRUCTURE_ERROR = "Error! Invalid Content Package File Structure.";
	
	public static final String INVALID_CONTENT_PACKAGE_SIZE_ERROR = "Error! Content exceeds the file size limit.";
	
	public static final String CONTENT_PACKAGE_FILE_OPERATION_ERROR = "Error! Unable to read Package file.";
	
	public static final String CONTENT_PACKAGE_VALIDATOR_ERROR = "Error! Unable to validate Package file.";
	
	public static final String INVALID_UPLOADED_FILE_EXTENSION_ERROR = "Error! Invalid File Extension.";
	
	public static final String MORE_THAN_ONE_MANIFEST_SECTION_ERROR = "Error! Only One Manifest Section Allowed.";
	
	public static final String XML_TRANSFORMATION_ERROR = "Error! While transforming the XML Object.";
	
	public static final String JSON_OBJECT_EXPECTED = "Error! JSON Object is Expected for the Element -- .";
	
	public static final String JSON_ARRAY_EXPECTED = "Error! JSON Array is Expected for the Element -- .";
	
	public static final String INVALID_MEDIA = "Error! Invalid Media Entry.";
	
	public static final String INVALID_CWP_INIT_PARAM = "Error! Invalid Initializer Parameter.";
	
	public static final String INVALID_CWP_FINALIZE_PARAM = "Error! Invalid Finalizer Parameter.";
	
	public static final String INVALID_CWP_CONST_PARAM = "Error! Invalid Constructor Parameter.";
	
	public static final String ZIP_EXTRACTION_ERROR = "Error! While Extracting the ZIP Package.";
	
	public static final String MULTIPLE_ECML_FILES_FOUND = "Error! Multiple ECML Files found.";
	
	public static final String ECML_FILE_READ_ERROR = "Error! While reading ECML File.";
	
	public static final String FILE_UPLOAD_ERROR = "Error! While uploading File.";
	
	public static final String MISSING_ASSETS_ERROR = "Error! Missing Asset.";
	
	public static final String PROCESSOR_ERROR = "Error! While Processing in Processor.";
	
	public static final String CONTROLLER_FILE_READ_ERROR = "Error! While Controller JSON File.";
	
	public static final String ASSET_UPLOAD_ERROR = "Error! While Uploading the Assets.";
	
	public static final String INVALID_ASSET_MIMETYPE = "Error! Invalid Asset Mime-Type.";
	
	public static final String ASSET_FILE_SIZE_LIMIT_EXCEEDS = "Error! File Size Exceeded the Limit.";
	
	private ContentErrorMessageConstants(){
	  throw new AssertionError();
	}

}
