package org.ekstep.language.common;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class LanguageSourceTypeMap is to load Language source types into map for
 * reference
 *
 * @author rayulu
 */
public class LanguageSourceTypeMap {

    /** The Language source type map. */
    private static Map<String, String> LanguageSourceTypeMap = new HashMap<String, String>();

    static {
    	LanguageSourceTypeMap.put("iwn", "IndoWordNet");
    	LanguageSourceTypeMap.put("wikisionary", "Wikisionary");
    	LanguageSourceTypeMap.put("wn", "WordNet");
    }

    /**
     * Contains source type.
     *
     * @param sourceId the source id
     * @return true, if successful
     */
    public static boolean containsSourceType(String sourceId) {
        return LanguageSourceTypeMap.containsKey(sourceId);
    }

    /**
     * Gets the source type.
     *
     * @param sourceId the source id
     * @return the source type
     */
    public static String getSourceType(String sourceId) {
        return LanguageSourceTypeMap.get(sourceId);
    }
}
