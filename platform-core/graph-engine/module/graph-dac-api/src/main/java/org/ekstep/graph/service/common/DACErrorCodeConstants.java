package org.ekstep.graph.service.common;

public enum DACErrorCodeConstants {
	
	INVALID_POLICY(0, "Invalid Policy Identifier.");
	
	private final int code;
	private final String description;

	private DACErrorCodeConstants(int code, String description) {
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
