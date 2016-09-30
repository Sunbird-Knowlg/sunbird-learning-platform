package org.ekstep.language.measures.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Map;

/**
 * WordComplexity class is a bean for storing complexity measures of a word.
 * 
 * @author Rayulu
 * 
 */
public class WordComplexity implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -837291979119278370L;

	/** The word. */
	private String word;

	/** The rts. */
	private String rts;

	/** The count. */
	private int count;

	/** The notation. */
	private String notation;

	/** The unicode. */
	private String unicode;

	/** The ortho vec. */
	private Integer[] orthoVec;

	/** The phonic vec. */
	private Integer[] phonicVec;

	/** The ortho complexity. */
	private Double orthoComplexity;

	/** The phonic complexity. */
	private Double phonicComplexity;

	/** The unicode type map. */
	private Map<String, String> unicodeTypeMap;

	/**
	 * Gets the unicode type map.
	 *
	 * @return the unicode type map
	 */
	public Map<String, String> getUnicodeTypeMap() {
		return unicodeTypeMap;
	}

	/**
	 * Sets the unicode type map.
	 *
	 * @param unicodeTypeMap
	 *            the unicode type map
	 */
	public void setUnicodeTypeMap(Map<String, String> unicodeTypeMap) {
		this.unicodeTypeMap = unicodeTypeMap;
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
	 * Gets the count.
	 *
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Sets the count.
	 *
	 * @param count
	 *            the new count
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * Gets the notation.
	 *
	 * @return the notation
	 */
	public String getNotation() {
		return notation;
	}

	/**
	 * Sets the notation.
	 *
	 * @param notation
	 *            the new notation
	 */
	public void setNotation(String notation) {
		this.notation = notation;
	}

	/**
	 * Gets the orthographic vec.
	 *
	 * @return the orthographic vec
	 */
	public Integer[] getOrthoVec() {
		return orthoVec;
	}

	/**
	 * Sets the orthographic vec.
	 *
	 * @param orthoVec
	 *            the new orthographic vec
	 */
	public void setOrthoVec(Integer[] orthoVec) {
		this.orthoVec = orthoVec;
	}

	/**
	 * Gets the phonologic vec.
	 *
	 * @return the phonologic vec
	 */
	public Integer[] getPhonicVec() {
		return phonicVec;
	}

	/**
	 * Sets the phonologic vec.
	 *
	 * @param phonicVec
	 *            the new phonologic vec
	 */
	public void setPhonicVec(Integer[] phonicVec) {
		this.phonicVec = phonicVec;
	}

	/**
	 * Gets the orthographic complexity.
	 *
	 * @return the orthographic complexity
	 */
	public Double getOrthoComplexity() {
		return orthoComplexity;
	}

	/**
	 * Sets the orthographic complexity.
	 *
	 * @param orthoComplexity
	 *            the new orthographic complexity
	 */
	public void setOrthoComplexity(Double orthoComplexity) {
		if (null != orthoComplexity) {
			BigDecimal bd = new BigDecimal(orthoComplexity);
			bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
			orthoComplexity = bd.doubleValue();
		}
		this.orthoComplexity = orthoComplexity;
	}

	/**
	 * Gets the phonologic complexity.
	 *
	 * @return the phonologic complexity
	 */
	public Double getPhonicComplexity() {
		return phonicComplexity;
	}

	/**
	 * Sets the phonologic complexity.
	 *
	 * @param phonicComplexity
	 *            the new phonologic complexity
	 */
	public void setPhonicComplexity(Double phonicComplexity) {
		if (null != phonicComplexity) {
			BigDecimal bd = new BigDecimal(phonicComplexity);
			bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
			phonicComplexity = bd.doubleValue();
		}
		this.phonicComplexity = phonicComplexity;
	}

	/**
	 * Gets the unicode.
	 *
	 * @return the unicode
	 */
	public String getUnicode() {
		return unicode;
	}

	/**
	 * Sets the unicode.
	 *
	 * @param unicode
	 *            the new unicode
	 */
	public void setUnicode(String unicode) {
		this.unicode = unicode;
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

	/** Comparator for comparing WordComplexity objects. */
	public static Comparator<WordComplexity> wordComparator = new Comparator<WordComplexity>() {
		public int compare(WordComplexity o1, WordComplexity o2) {
			String word1 = o1.getWord();
			String word2 = o2.getWord();
			if (null != word1)
				return word1.compareTo(word2);
			return -1;
		};
	};

	/**
	 * Comparator for comparing WordComplexity objects based on their phonologic
	 * complexity values.
	 */
	public static Comparator<WordComplexity> phonicComplexityComparator = new Comparator<WordComplexity>() {
		public int compare(WordComplexity o1, WordComplexity o2) {
			Double phonicComplexity1 = o1.getPhonicComplexity();
			Double phonicComplexity2 = o2.getPhonicComplexity();
			if (null != phonicComplexity1)
				return phonicComplexity1.compareTo(phonicComplexity2);
			return -1;
		};
	};

	/**
	 * Gets the measures.
	 *
	 * @return the measures
	 */
	public ComplexityMeasures getMeasures() {
		return new ComplexityMeasures(orthoComplexity, phonicComplexity);
	}
}
