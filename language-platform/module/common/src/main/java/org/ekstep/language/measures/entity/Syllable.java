package org.ekstep.language.measures.entity;

import java.util.List;

import org.ekstep.language.measures.meta.SyllableMap;

/**
 * The Class Syllable is a bean used to store unicodes along with char of
 * syllable
 *
 * @author rayulu
 */
public class Syllable {

	/** The internal code. */
	private String internalCode;

	/** The code. */
	private String code;

	/** The unicodes. */
	private List<String> unicodes;

	/** The chars. */
	private List<String> chars;

	/**
	 * Instantiates a new syllable.
	 *
	 * @param internalCode
	 *            the internal code
	 * @param unicodes
	 *            the unicodes
	 */
	public Syllable(String internalCode, List<String> unicodes) {
		this.internalCode = internalCode;
		this.unicodes = unicodes;
		if (null != internalCode && internalCode.trim().length() > 0) {
			this.code = this.internalCode.replaceAll(SyllableMap.VOWEL_SIGN_CODE, SyllableMap.VOWEL_CODE)
					.replaceAll(SyllableMap.HALANT_CODE, "")
					.replaceAll(SyllableMap.CLOSE_VOWEL_CODE, SyllableMap.VOWEL_CODE);
		}
	}

	/**
	 * Gets the internal code.
	 *
	 * @return the internal code
	 */
	public String getInternalCode() {
		return internalCode;
	}

	/**
	 * Sets the internal code.
	 *
	 * @param internalCode
	 *            the new internal code
	 */
	public void setInternalCode(String internalCode) {
		this.internalCode = internalCode;
	}

	/**
	 * Gets the code.
	 *
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Sets the code.
	 *
	 * @param code
	 *            the new code
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * Gets the unicodes.
	 *
	 * @return the unicodes
	 */
	public List<String> getUnicodes() {
		return unicodes;
	}

	/**
	 * Sets the unicodes.
	 *
	 * @param unicodes
	 *            the new unicodes
	 */
	public void setUnicodes(List<String> unicodes) {
		this.unicodes = unicodes;
	}

	/**
	 * Gets the chars.
	 *
	 * @return the chars
	 */
	public List<String> getChars() {
		return chars;
	}

	/**
	 * Sets the chars.
	 *
	 * @param chars
	 *            the new chars
	 */
	public void setChars(List<String> chars) {
		this.chars = chars;
	}

}
