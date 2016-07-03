package com.ilimi.taxonomy.content.enums;

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
	MISSING_CONTROLLER_FILE(17, "Missing Controller File.");

	private final int code;
	private final String description;

	private ContentErrorCodeConstants(int code, String description) {
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
