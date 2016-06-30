package com.ilimi.taxonomy.content.enums;

public enum ContentErrorCodeConstants {

	MULTIPLE_MANIFEST(0, "More than one Manifest Section."), 
	XML_TRANSFORM(1, "Error while xml transformation."),
	INVALID_MEDIA(2, "Invalid Media Element."),
	EXPECTED_JSON_OBJECT(3, "Expected JSON Object for the given Element."),
	EXPECTED_JSON_ARRAY(4, "Expected JSON Array for the given Element.");

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
