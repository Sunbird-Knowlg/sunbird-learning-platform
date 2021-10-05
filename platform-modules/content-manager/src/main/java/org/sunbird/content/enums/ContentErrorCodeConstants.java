package org.sunbird.content.enums;

import org.sunbird.content.common.ContentErrorMessageConstants;

/**
 *  ContentErrorCodeConstants Enum holds all the ErrorCodeConstants
 *  used in the ContentModel
 *
 *  @author Mohammad Azharuddin
 *
 *  @see ContentErrorMessageConstants
 */
public enum ContentErrorCodeConstants {

	MULTIPLE_MANIFEST(0, "More than one Manifest Section."),
	XML_TRANSFORM(1, "Error while xml transformation."),
	INVALID_MEDIA(2, "Invalid Media Element."),
	EXPECTED_JSON_OBJECT(3, "Expected JSON Object for the given Element."),
	EXPECTED_JSON_ARRAY(4, "Expected JSON Array for the given Element."),
	INVALID_PARAMETER(5, "Invalid Parameter(s)."),
	ZIP_EXTRACTION(6, "Error while extracting the ZIP file."),
	MULTIPLE_ECML(6, "Multiple ECML files found."),
	ECML_FILE_READ(6, "Error while reading ECML file."),
	UPLOAD_ERROR(7, "Error while uploading a file."),
	MISSING_ASSETS(8, "Missing Assets."),
	PROCESSOR_ERROR(9, "Error in Processor."),
	CONTROLLER_FILE_READ(10, "Error while reading controller file."),
	ASSET_UPLOAD_ERROR(11, "Error while reading controller file."),
	INVALID_MIME_TYPE(12, "Invalid Mime-Type."),
	FILE_SIZE_EXCEEDS_LIMIT(13, "File Size Exceeded the Limit."),
	ASSET_FILE_READ(13, "Eror while reading Asset file."),
	DUPLICATE_ASSET_ID(14, "Duplicate Asset Id used in manifest."),
	INVALID_CONTROLLER(15, "Invalid Controller Element."),
	DUPLICATE_CONTROLLER_ID(16, "Duplicate Controller Id used in ECML body."),
	MISSING_CONTROLLER_FILE(17, "Missing Controller File."),
	DOWNLOAD_ERROR(18, "Error while downloading a file."),
	SEARCH_ERROR(19, "Error while searching."),
	PROCESSOR_CONC_OP_ERROR(20, "Error in concurrent operation."),
	INVALID_BODY(21, "Invalid Content Body."),
	EMPTY_BODY(22, "Empty Content Body."),
	EMPTY_ECML(23, "Invalid or Empty ECML String."),
	INVALID_ECML_TYPE(24, "Invalid ECML Type."),
	ECML_FILE_WRITE(25, "Error while writing ECML File."),
	MANIFEST_FILE_WRITE(26, "Error while writing Manifest File."),
	BUNDLE_FILE_WRITE(27, "Error while writing Bundle File."),
	MISSING_CONTENT(28, "Missing one or more Content for Bundling."),
	OPERATION_DENIED(29, "The Content Operation cannot be Performed."),
	VALIDATOR_ERROR(30, "Invalid Content."),
	MANIFEST_FILE_READ(31, "Error while reading Manifest file."),
	INVALID_CONTENT(32, "Invalid Content (Node) Object."),
	INVALID_OPERATION(33, "Invalid Content Operation."),
	PUBLISH_ERROR(34, "Publish Error."),
	IMAGE_NODE_CREATION_ERROR(35, "Error while Create Content Image Node."),
	INVALID_LIBRARY(36, "Error while fetching the library files."),
	FILE_DELETE_ERROR(37, "Error while deleting the file."),
	INVALID_YOUTUBE_MEDIA(38, "Invalid Youtube MEDIA");

	private final int code;
	private final String description;

	ContentErrorCodeConstants(int code, String description) {
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
