package org.ekstep.language.common;

import java.util.HashMap;
import java.util.Map;

public class LanguageMap {

    private static Map<String, String> languageMap = new HashMap<String, String>();

    static {
        languageMap.put("hi", "hindi");
        languageMap.put("en", "english");
        languageMap.put("te", "telugu");
        languageMap.put("kan", "kannada");
    }

    public static boolean containsLanguage(String languageId) {
        return languageMap.containsKey(languageId);
    }

    public static String getLanguage(String languageId) {
        return languageMap.get(languageId);
    }
}
