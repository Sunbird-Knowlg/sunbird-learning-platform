package org.sunbird.content.common;

/** ContentErrorMessageConstants Class holds all the ErrorMessageConstants
 *  used in the ContentModel
 */
public class ContentErrorMessageConstants {

	public static final String XML_PARSE_CONFIG_ERROR = "Parse configuration error while parsing Content XML file.";

	public static final String JSON_PARSE_CONFIG_ERROR = "Parse configuration error while parsing Content JSON file.";

	public static final String MANIFEST_PARSE_CONFIG_ERROR = "Parse configuration error while parsing Manifest file.";

	public static final String INVALID_PLUGIN_ID_ERROR = "'id' in manifest.json is not same as the plugin identifier.";

	public static final String INVALID_PLUGIN_VER_ERROR = "'ver' is not specified in the plugin manifest.json.";

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

	public static final String INVALID_CWP_OP_INIT_PARAM = "Error! Invalid Operation Initializer Parameter.";

	public static final String INVALID_CWP_FINALIZE_PARAM = "Error! Invalid Finalizer Parameter.";

	public static final String INVALID_CWP_OP_FINALIZE_PARAM = "Error! Invalid Operation Finalizer Parameter.";

	public static final String INVALID_CWP_CONST_PARAM = "Error! Invalid Constructor Parameter.";

	public static final String ZIP_EXTRACTION_ERROR = "Error! While Extracting the ZIP Package.";

	public static final String MULTIPLE_ECML_FILES_FOUND = "Error! Multiple ECML Files found.";

	public static final String ECML_FILE_READ_ERROR = "Error! While reading ECML File.";

	public static final String MANIFEST_FILE_READ_ERROR = "Error! While reading Manifest File.";

	public static final String FILE_UPLOAD_ERROR = "Error! While uploading File.";

	public static final String MISSING_ASSETS_ERROR = "Error! Missing Asset.";

	public static final String PROCESSOR_ERROR = "Error! While Processing in Processor.";

	public static final String CONTROLLER_FILE_READ_ERROR = "Error! While reading Controller JSON File.";

	public static final String ASSET_UPLOAD_ERROR = "Error! While Uploading the Assets.";

	public static final String INVALID_ASSET_MIMETYPE = "Error! Invalid Asset Mime-Type.";

	public static final String ASSET_FILE_SIZE_LIMIT_EXCEEDS = "Error! File Size Exceeded the Limit.";

	public static final String ASSET_FILE_READ_ERROR = "Error! While reading Asset File.";

	public static final String DUPLICATE_ASSET_ID_ERROR = "Error! Duplicate Asset Id used in the manifest.";

	public static final String DUPLICATE_CONTROLLER_ID_ERROR = "Error! Duplicate Controller Id used in the ECML.";

	public static final String MISSING_CONTROLLER_FILES_ERROR = "Error! Missing Controller file.";

	public static final String APP_ICON_DOWNLOAD_ERROR = "Error! While downloading App Icon.";

	public static final String STAGE_ICON_DOWNLOAD_ERROR = "Error! While downloading Stage Icon.";

	public static final String ASSET_CONCURRENT_DOWNLOAD_ERROR = "Error! While downloading assets concurrently.";

	public static final String INVALID_CONTENT_BODY = "Error! Invalid Content Body.";

	public static final String EMPTY_CONTENT_BODY = "Error! Content Body is either 'null' or Empty.";

	public static final String EMPTY_ECML_STRING = "Error! Empty ECML String.";

	public static final String INVALID_ECML_TYPE = "Error! Invalid ECML Type.";

	public static final String ECML_FILE_WRITE_ERROR = "Error! While writing ECML File.";

	public static final String MANIFEST_FILE_WRITE_ERROR = "Error! While writing Manifest File.";

	public static final String NO_FILES_TO_BUNDLE = "Error! No Files to Create Bundle.";

	public static final String INVALID_BUNDLE_FILE_NAME = "Error! Invalid or 'null' Bundle File Name.";

	public static final String BUNDLE_FILE_WRITE_ERROR = "Error! While writing Bundle File.";

	public static final String MISSING_BUNDLE_CONTENT = "Error! Missing One or More Content for Bundling.";

	public static final String UNABLE_TO_PUBLISH_OR_BUNDLE_CONTENT = "Error! Content cannot be Bundled or Published.";

	public static final String CONTENT_NODE_VALIDATION_ERROR = "Error! While validating the Content Object.";

	public static final String INVALID_CONTENT_METADATA = "Error! Content Object Metadata is either 'null' or Invalid.";

	public static final String MISSING_REQUIRED_FIELDS = "Error! Missing One or More Required Fields in Content Object.";

	public static final String STRING_WRITER_AUTO_CLOSE_ERROR = "Error! While closing the StringWriter.";

	public static final String INVALID_CONTENT = "Error! Invalid Content.";

	public static final String INVALID_OPERATION = "Error! Invalid Content Operation.";

	public static final String INVALID_ASYNC_OPERATION_PARAMETER_MAP = "Error! Invalid Async Content Operation Parameter Map.";

	public static final String INVALID_CONTENT_MIMETYPE = "Error! Invalid Content MimeType.";

	public static final String INVALID_PARAMETER_MAP = "Error! Invalid Parameter Map.";

	public static final String CONTENT_IMAGE_MIGRATION_ERROR = "Error! While Migrating the Metadata.";

	public static final String CONTENT_BODY_MIGRATION_ERROR = "Error! While Migrating the Content Body.";

	public static final String INVALID_YOUTUBE_URL = "Error! Invalid youtube Url";

	public static final String MISSING_YOUTUBE_URL = "Error! Missing Youtube Url";

	public static final String MISSING_DOC_LINK = "Error! Missing doc/pdf Link";
	
	public static final String INVALID_H5P_LIBRARY = "Error! H5P Library Package File is Invalid.";
	
	public static final String INVALID_URL = "Error! Invalid/Missing Url";

	public static final String FILE_DELETE_ERROR = "Error! While Deleting the File";

	public static final String LICENSE_NOT_SUPPORTED = "Error! License Not Supported.";

	private ContentErrorMessageConstants(){
	  throw new AssertionError();
	}
}
