/**
 * 
 */
package common;

import java.lang.Character.UnicodeBlock;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;

/**
 * @author pradyumna
 *
 */
public class DetectLanguage {
	
	Map<String, Object> languageMap = new HashMap<>();

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public DetectLanguage() {
		languageMap = (Map<String, Object>) Platform.config.getAnyRef("language.map");
	}

	public String getlanguageCode(String text) {
		char unicodeChar = text.toCharArray()[0];
         String language = null;
         UnicodeBlock charBlock = UnicodeBlock.of(unicodeChar);
         if (charBlock.equals(Character.UnicodeBlock.DEVANAGARI_EXTENDED) || charBlock.equals(Character.UnicodeBlock.DEVANAGARI)) {
             language = "HINDI";
         }
         else if(charBlock.equals(Character.UnicodeBlock.BASIC_LATIN)){
             language = "ENGLISH";
         }
         else{
             language = charBlock.toString();
         }
         language = StringUtils.capitalize(language.toLowerCase());
         
         
		String id = (String) languageMap.get(language);
		return id;
	 }

	public boolean isValidLangId(String languageId) {
		return languageMap.containsValue(languageId);
	}

	public Map<String, Object> getLanguageMap() {
		return languageMap;
	}
}
