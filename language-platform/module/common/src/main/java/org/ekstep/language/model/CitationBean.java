package org.ekstep.language.model;

/**
 * The Class CitationBean is used as DTO for word citations being stored or
 * accessed from Elasticsearch.
 * 
 * @author Amarnath
 */
public class CitationBean {

	/** The word. */
	private String word;

	/** The root word. */
	private String rootWord;

	/** The pos. */
	private String pos;

	/** The date. */
	private String date;

	/** The source type. */
	private String sourceType;

	/** The source. */
	private String source;

	/** The grade. */
	private String grade;

	/** The file name. */
	private String fileName;

	/**
	 * Instantiates a new citation bean.
	 *
	 * @param word
	 *            the word
	 * @param rootWord
	 *            the root word
	 * @param pos
	 *            the pos
	 * @param date
	 *            the date
	 * @param sourceType
	 *            the source type
	 * @param source
	 *            the source
	 * @param grade
	 *            the grade
	 * @param fileName
	 *            the file name
	 */
	public CitationBean(String word, String rootWord, String pos, String date, String sourceType, String source,
			String grade, String fileName) {
		super();
		this.word = word;
		this.rootWord = rootWord;
		this.pos = pos;
		this.date = date;
		this.sourceType = sourceType;
		this.source = source;
		this.grade = grade;
		this.fileName = fileName;
	}

	/**
	 * Instantiates a new citation bean.
	 */
	public CitationBean() {
		super();
	}

	/**
	 * Gets the source.
	 *
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Sets the source.
	 *
	 * @param source
	 *            the new source
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * Gets the grade.
	 *
	 * @return the grade
	 */
	public String getGrade() {
		return grade;
	}

	/**
	 * Sets the grade.
	 *
	 * @param grade
	 *            the new grade
	 */
	public void setGrade(String grade) {
		this.grade = grade;
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
	 * @param position
	 *            the new pos
	 */
	public void setPos(String position) {
		this.pos = position;
	}

	/**
	 * Gets the date.
	 *
	 * @return the date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * Sets the date.
	 *
	 * @param date
	 *            the new date
	 */
	public void setDate(String date) {
		this.date = date;
	}

	/**
	 * Gets the source type.
	 *
	 * @return the source type
	 */
	public String getSourceType() {
		return sourceType;
	}

	/**
	 * Sets the source type.
	 *
	 * @param sourceType
	 *            the new source type
	 */
	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	/**
	 * Gets the file name.
	 *
	 * @return the file name
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Sets the file name.
	 *
	 * @param fileName
	 *            the new file name
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
