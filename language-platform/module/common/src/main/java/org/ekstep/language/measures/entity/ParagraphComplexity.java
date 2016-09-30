package org.ekstep.language.measures.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class ParagraphComplexity is a bean used to store mean and total
 * Ortho/Phonic complexities of a paragraph
 *
 * @author rayulu
 */
public class ParagraphComplexity implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2895861316069551961L;

	/** The text. */
	private String text;

	/** The word count. */
	private int wordCount;

	/** The syllable count. */
	private int syllableCount;

	/** The mean ortho complexity. */
	private Double meanOrthoComplexity;

	/** The total ortho complexity. */
	private Double totalOrthoComplexity;

	/** The mean phonic complexity. */
	private Double meanPhonicComplexity;

	/** The total phonic complexity. */
	private Double totalPhonicComplexity;

	/** The mean word complexity. */
	private Double meanWordComplexity;

	/** The total word complexity. */
	private Double totalWordComplexity;

	/** The mean complexity. */
	private Double meanComplexity;

	/** The word measures. */
	private Map<String, ComplexityMeasures> wordMeasures = new HashMap<String, ComplexityMeasures>();

	/** The word complexity map. */
	private Map<String, Double> wordComplexityMap = new HashMap<String, Double>();

	/** The word frequency. */
	private Map<String, Integer> wordFrequency = new HashMap<String, Integer>();

	/** The syllable count map. */
	private Map<String, Integer> syllableCountMap = new HashMap<String, Integer>();

	/**
	 * Gets the text.
	 *
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the text.
	 *
	 * @param text
	 *            the new text
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Gets the mean ortho complexity.
	 *
	 * @return the mean ortho complexity
	 */
	public Double getMeanOrthoComplexity() {
		return meanOrthoComplexity;
	}

	/**
	 * Sets the mean ortho complexity.
	 *
	 * @param meanOrthoComplexity
	 *            the new mean ortho complexity
	 */
	public void setMeanOrthoComplexity(Double meanOrthoComplexity) {
		this.meanOrthoComplexity = meanOrthoComplexity;
	}

	/**
	 * Gets the total ortho complexity.
	 *
	 * @return the total ortho complexity
	 */
	public Double getTotalOrthoComplexity() {
		return totalOrthoComplexity;
	}

	/**
	 * Sets the total ortho complexity.
	 *
	 * @param totalOrthoComplexity
	 *            the new total ortho complexity
	 */
	public void setTotalOrthoComplexity(Double totalOrthoComplexity) {
		this.totalOrthoComplexity = totalOrthoComplexity;
	}

	/**
	 * Gets the mean phonic complexity.
	 *
	 * @return the mean phonic complexity
	 */
	public Double getMeanPhonicComplexity() {
		return meanPhonicComplexity;
	}

	/**
	 * Sets the mean phonic complexity.
	 *
	 * @param meanPhonicComplexity
	 *            the new mean phonic complexity
	 */
	public void setMeanPhonicComplexity(Double meanPhonicComplexity) {
		this.meanPhonicComplexity = meanPhonicComplexity;
	}

	/**
	 * Gets the total phonic complexity.
	 *
	 * @return the total phonic complexity
	 */
	public Double getTotalPhonicComplexity() {
		return totalPhonicComplexity;
	}

	/**
	 * Sets the total phonic complexity.
	 *
	 * @param totalPhonicComplexity
	 *            the new total phonic complexity
	 */
	public void setTotalPhonicComplexity(Double totalPhonicComplexity) {
		this.totalPhonicComplexity = totalPhonicComplexity;
	}

	/**
	 * Gets the word measures.
	 *
	 * @return the word measures
	 */
	public Map<String, ComplexityMeasures> getWordMeasures() {
		return wordMeasures;
	}

	/**
	 * Sets the word measures.
	 *
	 * @param wordMeasures
	 *            the word measures
	 */
	public void setWordMeasures(Map<String, ComplexityMeasures> wordMeasures) {
		this.wordMeasures = wordMeasures;
	}

	/**
	 * Measures.
	 *
	 * @return the complexity measures
	 */
	public ComplexityMeasures measures() {
		return new ComplexityMeasures(meanOrthoComplexity, meanPhonicComplexity);
	}

	/**
	 * Gets the word count.
	 *
	 * @return the word count
	 */
	public int getWordCount() {
		return wordCount;
	}

	/**
	 * Sets the word count.
	 *
	 * @param wordCount
	 *            the new word count
	 */
	public void setWordCount(int wordCount) {
		this.wordCount = wordCount;
	}

	/**
	 * Gets the syllable count.
	 *
	 * @return the syllable count
	 */
	public int getSyllableCount() {
		return syllableCount;
	}

	/**
	 * Sets the syllable count.
	 *
	 * @param syllableCount
	 *            the new syllable count
	 */
	public void setSyllableCount(int syllableCount) {
		this.syllableCount = syllableCount;
	}

	/**
	 * Gets the word frequency.
	 *
	 * @return the word frequency
	 */
	public Map<String, Integer> getWordFrequency() {
		return wordFrequency;
	}

	/**
	 * Sets the word frequency.
	 *
	 * @param wordFrequency
	 *            the word frequency
	 */
	public void setWordFrequency(Map<String, Integer> wordFrequency) {
		this.wordFrequency = wordFrequency;
	}

	/**
	 * Gets the mean word complexity.
	 *
	 * @return the mean word complexity
	 */
	public Double getMeanWordComplexity() {
		return meanWordComplexity;
	}

	/**
	 * Sets the mean word complexity.
	 *
	 * @param meanWordComplexity
	 *            the new mean word complexity
	 */
	public void setMeanWordComplexity(Double meanWordComplexity) {
		this.meanWordComplexity = meanWordComplexity;
	}

	/**
	 * Gets the total word complexity.
	 *
	 * @return the total word complexity
	 */
	public Double getTotalWordComplexity() {
		return totalWordComplexity;
	}

	/**
	 * Sets the total word complexity.
	 *
	 * @param totalWordComplexity
	 *            the new total word complexity
	 */
	public void setTotalWordComplexity(Double totalWordComplexity) {
		this.totalWordComplexity = totalWordComplexity;
	}

	/**
	 * Gets the word complexity map.
	 *
	 * @return the word complexity map
	 */
	public Map<String, Double> getWordComplexityMap() {
		return wordComplexityMap;
	}

	/**
	 * Sets the word complexity map.
	 *
	 * @param wordComplexityMap
	 *            the word complexity map
	 */
	public void setWordComplexityMap(Map<String, Double> wordComplexityMap) {
		this.wordComplexityMap = wordComplexityMap;
	}

	/**
	 * Gets the syllable count map.
	 *
	 * @return the syllable count map
	 */
	public Map<String, Integer> getSyllableCountMap() {
		return syllableCountMap;
	}

	/**
	 * Sets the syllable count map.
	 *
	 * @param syllableCountMap
	 *            the syllable count map
	 */
	public void setSyllableCountMap(Map<String, Integer> syllableCountMap) {
		this.syllableCountMap = syllableCountMap;
	}

	/**
	 * Gets the meanComplexity.
	 *
	 * @param meanComplexity
	 *            meanComplexity
	 */
	public Double getMeanComplexity() {
		return meanComplexity;
	}

	/**
	 * Sets the meanComplexity.
	 *
	 * @param meanComplexity
	 *            meanComplexity
	 */
	public void setMeanComplexity(Double meanComplexity) {
		this.meanComplexity = meanComplexity;
	}


}
