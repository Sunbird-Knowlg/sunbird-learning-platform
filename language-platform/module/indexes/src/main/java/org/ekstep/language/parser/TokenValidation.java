package org.ekstep.language.parser;

import java.util.Map.Entry;
import java.util.TreeMap;

public class TokenValidation {

	private static TreeMap<String, String> wordUnicode = new TreeMap<String, String>();

	static {
		wordUnicode.put("0041", "en");
		wordUnicode.put("005A", null);
		wordUnicode.put("0061", "en");
		wordUnicode.put("007A", null);
		wordUnicode.put("0900", "hi");
		wordUnicode.put("097F", null);
		wordUnicode.put("0C80", "ka");
		wordUnicode.put("0CFF", null);
		wordUnicode.put("0C00", "te");
		wordUnicode.put("0C7F", null);
		wordUnicode.put("0B80", "ta");
		wordUnicode.put("0BFF", null);
	}

	private static <K, V> V mappedValue(TreeMap<K, V> map, K key) {
		Entry<K, V> e = map.floorEntry(key);
		if (e != null && e.getValue() == null) {
			e = map.lowerEntry(key);
		}
		return e == null ? null : e.getValue();
	}

	static Boolean getUnicode(String s, String languageId) {
		String output = "";
		Boolean exists = false;
		char input[] = s.toCharArray();
		for (char c : input) {
			output = toUnicode(c);
			String langRes = mappedValue(wordUnicode, output);
			if (langRes == languageId) {
				exists = true;
			}
		}
		return exists;
	}

	// to calculate unicode of a char
	private static String toUnicode(char ch) {
		return String.format("%04x", (int) ch);
	}
}