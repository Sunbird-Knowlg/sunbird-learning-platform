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
	 * @param strContent the Content(xml)
	 * @return Plugin
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
	 * @param strContent the Content(Json)
	 * @return Plugin
	 */
	public Plugin getEcrfFromJson(String strContent) {
		Plugin content = new Plugin();
		JSONContentParser parser = new JSONContentParser();
		content = parser.parseContent(strContent);
		return content;
	}

}
