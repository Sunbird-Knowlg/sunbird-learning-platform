package org.sunbird.taxonomy.common;

import java.util.HashMap;
import java.util.Map;

public class LanguageCodeMap {

	private static Map<String, String> languageMap = new HashMap<String, String>();
	
	static {
		languageMap.put("assamese", "as");
		languageMap.put("bengali", "bn");
		languageMap.put("english", "en");
		languageMap.put("gujarati", "gu");
		languageMap.put("hindi", "hi");
		languageMap.put("kannada", "ka");
		languageMap.put("marathi", "mr");
		languageMap.put("odia", "or");
		languageMap.put("tamil", "ta");
		languageMap.put("telugu", "te");
	}
	
	public static String getLanguageCode(String language) {
		return languageMap.get(language);
	}
	
}
