package com.ilimi.taxonomy.content.util;

import com.ilimi.taxonomy.content.entity.Plugin;

public class ECRFConversionUtility {
	
	public Plugin getECRF(String strContent) {
		Plugin content = new Plugin();
		XMLContentParser parser = new XMLContentParser();
		content = parser.parseContent(strContent);
		return content;
	}
	
	public Plugin getEcrfFromJson(String strContent) {
		Plugin content = new Plugin();
		JSONContentParser parser = new JSONContentParser();
		content = parser.parseContent(strContent);
		return content;
	}

}
