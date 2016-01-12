package org.ekstep.literacy.entity;

import java.util.List;

import org.ekstep.literacy.meta.TeluguSyllables;

public class Syllable {

	private String internalCode;
	private String code;
	private List<String> unicodes;
	private List<String> chars;

	public Syllable(String internalCode, List<String> unicodes) {
		this.internalCode = internalCode;
		this.unicodes = unicodes;
		if (null != internalCode && internalCode.trim().length() > 0) {
			this.code = this.internalCode.replaceAll(TeluguSyllables.VOWEL_SIGN_CODE, TeluguSyllables.VOWEL_CODE)
					.replaceAll(TeluguSyllables.HALANT_CODE, "").replaceAll(TeluguSyllables.CLOSE_VOWEL_CODE, TeluguSyllables.VOWEL_CODE);
		}
	}

	public String getInternalCode() {
		return internalCode;
	}

	public void setInternalCode(String internalCode) {
		this.internalCode = internalCode;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public List<String> getUnicodes() {
		return unicodes;
	}

	public void setUnicodes(List<String> unicodes) {
		this.unicodes = unicodes;
	}

	public List<String> getChars() {
		return chars;
	}

	public void setChars(List<String> chars) {
		this.chars = chars;
	}

}
