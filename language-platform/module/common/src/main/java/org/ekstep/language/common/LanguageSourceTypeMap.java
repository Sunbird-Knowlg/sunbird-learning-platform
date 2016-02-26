package org.ekstep.language.common;

import java.util.HashMap;
import java.util.Map;

public class LanguageSourceTypeMap {

    private static Map<String, String> LanguageSourceTypeMap = new HashMap<String, String>();

    static {
    	LanguageSourceTypeMap.put("iwn", "IndoWordNet");
    	LanguageSourceTypeMap.put("wikisionary", "Wikisionary");
    	LanguageSourceTypeMap.put("wn", "WordNet");
    }

    public static boolean containsSourceType(String sourceId) {
        return LanguageSourceTypeMap.containsKey(sourceId);
    }

    public static String getSourceType(String sourceId) {
        return LanguageSourceTypeMap.get(sourceId);
    }
}
