package org.ekstep.language.measures.meta;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * The Class SyllableMap is meta class holding each language akshara/unicode with its
 * type (vowel, consonant, etc)
 *
 * @author rayulu
 */
public class SyllableMap {

	/** The syllable map. */
	private static Map<String, Map<String, String>> syllableMap = new HashMap<String, Map<String, String>>();

	/** The default vowel map. */
	private static Map<String, String> defaultVowelMap = new HashMap<String, String>();

	/** The mapper. */
	private static ObjectMapper mapper = new ObjectMapper();

	/** The Constant MODIFIER_SUFFIX. */
	public static final String MODIFIER_SUFFIX = "m";

	/** The Constant CONSONANT_CODE. */
	public static final String CONSONANT_CODE = "C";

	/** The Constant VOWEL_CODE. */
	public static final String VOWEL_CODE = "V";

	/** The Constant HALANT_CODE. */
	public static final String HALANT_CODE = "H";

	/** The Constant VOWEL_SIGN_CODE. */
	public static final String VOWEL_SIGN_CODE = "S";

	/** The Constant CLOSE_VOWEL_CODE. */
	public static final String CLOSE_VOWEL_CODE = "O";

	/**
	 * Checks if is language enabled.
	 *
	 * @param language
	 *            the language
	 * @return true, if is language enabled
	 */
	public static boolean isLanguageEnabled(String language) {
		if (StringUtils.isNotBlank(language)) {
			return syllableMap.containsKey(language.toLowerCase().trim());
		}
		return false;
	}

	/**
	 * Gets the syllable type.
	 *
	 * @param language
	 *            the language
	 * @param s
	 *            the s
	 * @return the syllable type
	 */
	public static String getSyllableType(String language, String s) {
		if (null != s && s.trim().length() > 0) {
			if (syllableMap.containsKey(language.toLowerCase().trim()))
				if (syllableMap.get(language.toLowerCase().trim()).containsKey(s.toUpperCase()))
					return syllableMap.get(language.toLowerCase().trim()).get(s.toUpperCase());
		}
		return "";
	}

	/**
	 * Skip word.
	 *
	 * @param language
	 *            the language
	 * @param s
	 *            the s
	 * @return true, if successful
	 */
	public static boolean skipWord(String language, String s) {
		if (null != s && s.trim().length() > 0) {
			if (!syllableMap.containsKey(language.toLowerCase().trim())) {
				return true;
			} else {
				if (!syllableMap.get(language.toLowerCase().trim()).containsKey(s.toUpperCase()))
					return true;
			}
		}
		return false;
	}

	/**
	 * Gets the default vowel.
	 *
	 * @param language
	 *            the language
	 * @return the default vowel
	 */
	public static String getDefaultVowel(String language) {
		if (defaultVowelMap.containsKey(language.toLowerCase().trim())) {
			return defaultVowelMap.get(language.toLowerCase().trim());
		}
		return "";
	}

	/**
	 * Load syllables.
	 *
	 * @param language
	 *            the language
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void loadSyllables(String language) {
		language = language.toLowerCase().trim();
		try (InputStream is = SyllableMap.class.getResourceAsStream("/" + language + "/" + "Syllables.json")) {
			Map map = mapper.readValue(is, Map.class);
			defaultVowelMap.put(language, (String) map.get("default_vowel"));

			Map<String, String> languageMap = syllableMap.get(language);
			if (null == languageMap) {
				languageMap = new HashMap<String, String>();
				syllableMap.put(language, languageMap);
			}
			List<String> vowels = (List<String>) map.get("vowels");
			addToLanguageMap(languageMap, vowels, VOWEL_CODE);
			List<String> consonants = (List<String>) map.get("consonants");
			addToLanguageMap(languageMap, consonants, CONSONANT_CODE);
			List<String> vowel_signs = (List<String>) map.get("vowel_signs");
			addToLanguageMap(languageMap, vowel_signs, VOWEL_SIGN_CODE);
			List<String> close_vowels = (List<String>) map.get("close_vowels");
			addToLanguageMap(languageMap, close_vowels, CLOSE_VOWEL_CODE);
			List<String> halants = (List<String>) map.get("halants");
			addToLanguageMap(languageMap, halants, HALANT_CODE);

			OrthographicVectors.load(language);
			PhonologicVectors.load(language);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds the to language map.
	 *
	 * @param languageMap
	 *            the language map
	 * @param unicodes
	 *            the unicodes
	 * @param code
	 *            the code
	 */
	private static void addToLanguageMap(Map<String, String> languageMap, List<String> unicodes, String code) {
		if (null != unicodes && !unicodes.isEmpty()) {
			for (String uc : unicodes) {
				languageMap.put(uc.toUpperCase().trim(), code);
			}
		}
	}

}
