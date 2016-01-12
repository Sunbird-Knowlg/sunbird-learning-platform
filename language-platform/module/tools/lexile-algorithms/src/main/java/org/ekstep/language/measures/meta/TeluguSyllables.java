package org.ekstep.language.measures.meta;

import java.util.HashMap;
import java.util.Map;

public class TeluguSyllables {

	private static Map<String, String> syllables = new HashMap<String, String>();

	public static final String DEFAULT_VOWEL = "0C05a";
	public static final String MODIFIER_SUFFIX = "m";

	public static final String CONSONANT_CODE = "C";
	public static final String VOWEL_CODE = "V";
	public static final String HALANT_CODE = "H";
	public static final String VOWEL_SIGN_CODE = "S";
	public static final String CLOSE_VOWEL_CODE = "O";

	public static String getSyllableType(String s) {
		if (null != s && s.trim().length() > 0) {
			if (syllables.containsKey(s.toUpperCase()))
				return syllables.get(s.toUpperCase());
		}
		return "";
	}

	public static boolean skipWord(String s) {
		if (null != s && s.trim().length() > 0) {
			if (!syllables.containsKey(s.toUpperCase()))
				return true;
		}
		return false;
	}

	static {
		syllables.put("0C05", VOWEL_CODE);
		syllables.put("0C06", VOWEL_CODE);
		syllables.put("0C07", VOWEL_CODE);
		syllables.put("0C08", VOWEL_CODE);
		syllables.put("0C09", VOWEL_CODE);
		syllables.put("0C0A", VOWEL_CODE);
		syllables.put("0C0B", VOWEL_CODE);
		syllables.put("0C60", VOWEL_CODE);
		syllables.put("0C0E", VOWEL_CODE);
		syllables.put("0C0F", VOWEL_CODE);
		syllables.put("0C10", VOWEL_CODE);
		syllables.put("0C12", VOWEL_CODE);
		syllables.put("0C13", VOWEL_CODE);
		syllables.put("0C14", VOWEL_CODE);

		syllables.put("0C02", CLOSE_VOWEL_CODE);
		syllables.put("0C03", CLOSE_VOWEL_CODE);

		syllables.put("0C15", CONSONANT_CODE);
		syllables.put("0C16", CONSONANT_CODE);
		syllables.put("0C17", CONSONANT_CODE);
		syllables.put("0C18", CONSONANT_CODE);
		syllables.put("0C19", CONSONANT_CODE);
		syllables.put("0C1A", CONSONANT_CODE);
		syllables.put("0C1B", CONSONANT_CODE);
		syllables.put("0C1C", CONSONANT_CODE);
		syllables.put("0C1D", CONSONANT_CODE);
		syllables.put("0C1E", CONSONANT_CODE);
		syllables.put("0C1F", CONSONANT_CODE);
		syllables.put("0C20", CONSONANT_CODE);
		syllables.put("0C21", CONSONANT_CODE);
		syllables.put("0C22", CONSONANT_CODE);
		syllables.put("0C23", CONSONANT_CODE);
		syllables.put("0C24", CONSONANT_CODE);
		syllables.put("0C25", CONSONANT_CODE);
		syllables.put("0C26", CONSONANT_CODE);
		syllables.put("0C27", CONSONANT_CODE);
		syllables.put("0C28", CONSONANT_CODE);
		syllables.put("0C2A", CONSONANT_CODE);
		syllables.put("0C2B", CONSONANT_CODE);
		syllables.put("0C2C", CONSONANT_CODE);
		syllables.put("0C2D", CONSONANT_CODE);
		syllables.put("0C2E", CONSONANT_CODE);
		syllables.put("0C2F", CONSONANT_CODE);
		syllables.put("0C30", CONSONANT_CODE);
		syllables.put("0C31", CONSONANT_CODE);
		syllables.put("0C32", CONSONANT_CODE);
		syllables.put("0C33", CONSONANT_CODE);
		syllables.put("0C35", CONSONANT_CODE);
		syllables.put("0C36", CONSONANT_CODE);
		syllables.put("0C37", CONSONANT_CODE);
		syllables.put("0C38", CONSONANT_CODE);
		syllables.put("0C39", CONSONANT_CODE);

		syllables.put("0C3E", VOWEL_SIGN_CODE);
		syllables.put("0C3F", VOWEL_SIGN_CODE);
		syllables.put("0C40", VOWEL_SIGN_CODE);
		syllables.put("0C41", VOWEL_SIGN_CODE);
		syllables.put("0C42", VOWEL_SIGN_CODE);
		syllables.put("0C43", VOWEL_SIGN_CODE);
		syllables.put("0C44", VOWEL_SIGN_CODE);
		syllables.put("0C46", VOWEL_SIGN_CODE);
		syllables.put("0C47", VOWEL_SIGN_CODE);
		syllables.put("0C48", VOWEL_SIGN_CODE);
		syllables.put("0C4A", VOWEL_SIGN_CODE);
		syllables.put("0C4B", VOWEL_SIGN_CODE);
		syllables.put("0C4C", VOWEL_SIGN_CODE);

		syllables.put("0C4D", HALANT_CODE);
	}
}
