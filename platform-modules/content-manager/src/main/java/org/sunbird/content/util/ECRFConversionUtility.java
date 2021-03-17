package org.sunbird.content.util;

import org.sunbird.content.entity.Plugin;

/**
 * The Class ECRFConversionUtility  is a utility 
 * used to convert Content(XML/JSON) to ECRF
 */
public class ECRFConversionUtility {
	
	/**
	 * gets the ECRF object from Content
	 * 
	 * @param content the Content(xml)
	 * @return ECRF
	 */
	public Plugin getECRF(String strContent) {
		Plugin content = new Plugin();
		XMLContentParser parser = new XMLContentParser();
		content = parser.parseContent(strContent);
		return content;
	}
	
	/**
	 * gets the ECRF object from Content
	 * 
	 * @param content the Content(Json)
	 * @return ERCF
	 */
	public Plugin getEcrfFromJson(String strContent) {
		Plugin content = new Plugin();
		JSONContentParser parser = new JSONContentParser();
		content = parser.parseContent(strContent);
		return content;
	}

}
