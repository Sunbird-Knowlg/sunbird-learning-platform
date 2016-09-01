package org.ekstep.language.model;

/**
 * The Class WordIndexBean facilitates in creating, updating or accessing a Word
 * index document to/from ElasticSearch.
 * 
 * @author Amarnath
 * 
 */
public class WordIndexBean {

	/** The word. */
	private String word;

	/** The root word. */
	private String rootWord;

	/** The id. */
	private String id;

	/**
	 * Instantiates a new word index bean.
	 */
	public WordIndexBean() {
		super();
	}

	/**
	 * Instantiates a new word index bean.
	 *
	 * @param word
	 *            the word
	 * @param rootWord
	 *            the root word
	 * @param wordIdentifier
	 *            the word identifier
	 */
	public WordIndexBean(String word, String rootWord, String wordIdentifier) {
		super();
		this.word = word;
		this.rootWord = rootWord;
		this.id = wordIdentifier;
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
	 * Gets the word identifier.
	 *
	 * @return the word identifier
	 */
	public String getWordIdentifier() {
		return id;
	}

	/**
	 * Sets the word identifier.
	 *
	 * @param wordIdentifier
	 *            the new word identifier
	 */
	public void setWordIdentifier(String wordIdentifier) {
		this.id = wordIdentifier;
	}
}
