package org.ekstep.language.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * The Class WordInfoBean facilitates in creating, updating or accessing a Word
 * info index document to/from ElasticSearch.
 * 
 * @author Amarnath
 * 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WordInfoBean {

	/** The word. */
	private String word;

	/** The root word. */
	private String rootWord;

	/** The pos. */
	private String pos;

	/** The category. */
	private String category;

	/** The gender. */
	private String gender;

	/** The number. */
	private String number;

	/** The pers. */
	private String pers;

	/** The grammatical case. */
	private String grammaticalCase;

	/** The inflection. */
	private String inflection;

	/** The rts. */
	private String rts;

	/**
	 * Instantiates a new word info bean.
	 *
	 * @param word
	 *            the word
	 * @param rootWord
	 *            the root word
	 * @param pos
	 *            the pos
	 * @param category
	 *            the category
	 * @param gender
	 *            the gender
	 * @param number
	 *            the number
	 * @param pers
	 *            the pers
	 * @param grammaticalCase
	 *            the grammatical case
	 * @param inflection
	 *            the inflection
	 * @param rts
	 *            the rts
	 */
	public WordInfoBean(String word, String rootWord, String pos, String category, String gender, String number,
			String pers, String grammaticalCase, String inflection, String rts) {
		super();
		this.word = word;
		this.rootWord = rootWord;
		this.pos = pos;
		this.category = category;
		this.gender = gender;
		this.number = number;
		this.pers = pers;
		this.grammaticalCase = grammaticalCase;
		this.inflection = inflection;
		this.rts = rts;
	}

	/**
	 * Instantiates a new word info bean.
	 */
	public WordInfoBean() {
		super();
	}

	/**
	 * Gets the category.
	 *
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * Sets the category.
	 *
	 * @param category
	 *            the new category
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * Gets the word.
	 *
	 * @return the word
	 */
	public String getWord() {
		return word;
	}

	/**
	 * Sets the word.
	 *
	 * @param word
	 *            the new word
	 */
	public void setWord(String word) {
		this.word = word;
	}

	/**
	 * Gets the root word.
	 *
	 * @return the root word
	 */
	public String getRootWord() {
		return rootWord;
	}

	/**
	 * Sets the root word.
	 *
	 * @param rootWord
	 *            the new root word
	 */
	public void setRootWord(String rootWord) {
		this.rootWord = rootWord;
	}

	/**
	 * Gets the pos.
	 *
	 * @return the pos
	 */
	public String getPos() {
		return pos;
	}

	/**
	 * Sets the pos.
	 *
	 * @param pos
	 *            the new pos
	 */
	public void setPos(String pos) {
		this.pos = pos;
	}

	/**
	 * Gets the gender.
	 *
	 * @return the gender
	 */
	public String getGender() {
		return gender;
	}

	/**
	 * Sets the gender.
	 *
	 * @param gender
	 *            the new gender
	 */
	public void setGender(String gender) {
		this.gender = gender;
	}

	/**
	 * Gets the number.
	 *
	 * @return the number
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * Sets the number.
	 *
	 * @param number
	 *            the new number
	 */
	public void setNumber(String number) {
		this.number = number;
	}

	/**
	 * Gets the pers.
	 *
	 * @return the pers
	 */
	public String getPers() {
		return pers;
	}

	/**
	 * Sets the pers.
	 *
	 * @param pers
	 *            the new pers
	 */
	public void setPers(String pers) {
		this.pers = pers;
	}

	/**
	 * Gets the grammatical case.
	 *
	 * @return the grammatical case
	 */
	public String getGrammaticalCase() {
		return grammaticalCase;
	}

	/**
	 * Sets the grammatical case.
	 *
	 * @param grammaticalCase
	 *            the new grammatical case
	 */
	public void setGrammaticalCase(String grammaticalCase) {
		this.grammaticalCase = grammaticalCase;
	}

	/**
	 * Gets the inflection.
	 *
	 * @return the inflection
	 */
	public String getInflection() {
		return inflection;
	}

	/**
	 * Sets the inflection.
	 *
	 * @param inflection
	 *            the new inflection
	 */
	public void setInflection(String inflection) {
		this.inflection = inflection;
	}

	/**
	 * Gets the rts.
	 *
	 * @return the rts
	 */
	public String getRts() {
		return rts;
	}

	/**
	 * Sets the rts.
	 *
	 * @param rts
	 *            the new rts
	 */
	public void setRts(String rts) {
		this.rts = rts;
	}
}
