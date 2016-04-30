package com.ilimi.taxonomy.content.util;

import com.ilimi.taxonomy.content.entity.Content;

public class ECRFConversionUtility {
	
	public Content getECRF(String strContent) {
		Content content = new Content();
		XMLContentParser parser = new XMLContentParser();
		content = parser.parseContent(strContent);
		return content;
	}

}
