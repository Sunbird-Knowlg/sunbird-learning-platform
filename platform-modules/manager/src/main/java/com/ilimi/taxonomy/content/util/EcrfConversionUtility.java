package com.ilimi.taxonomy.content.util;

import com.ilimi.taxonomy.content.entity.Content;

public class EcrfConversionUtility {
	
	public Content getECRF(String strContent) {
		Content content = new Content();
		XmlContentParser parser = new XmlContentParser();
		content = parser.parseContent(strContent);
		return content;
	}
	
	public Content getEcrfFromJson(String strContent) {
		Content content = new Content();
		JsonContentParser parser = new JsonContentParser();
		content = parser.parseContent(strContent);
		return content;
	}

}
