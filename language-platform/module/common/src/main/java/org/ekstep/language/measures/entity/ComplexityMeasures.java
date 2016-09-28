package org.ekstep.language.measures.entity;

import java.io.Serializable;

/**
 * The Class ComplexityMeasures is a bean used to store both orthographic and
 * phonologic complexities of a word.
 * 
 * @author Rayulu
 * 
 */
public class ComplexityMeasures implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 423982555916654298L;

	/** The orthographic complexity. */
	private Double orthographic_complexity;

	/** The phonologic complexity. */
	private Double phonologic_complexity;

	/**
	 * Instantiates a new complexity measures.
	 */
	public ComplexityMeasures() {

	}

	/**
	 * Instantiates a new complexity measures.
	 *
	 * @param orthographic
	 *            the orthographic complexity value
	 * @param phonologic
	 *            the phonologic complexity value
	 */
	public ComplexityMeasures(Double orthographic, Double phonologic) {
		this.orthographic_complexity = orthographic;
		this.phonologic_complexity = phonologic;
	}

	/**
	 * Gets the orthographic complexity.
	 *
	 * @return the orthographic complexity
	 */
	public Double getOrthographic_complexity() {
		return orthographic_complexity;
	}

	/**
	 * Sets the orthographic complexity.
	 *
	 * @param orthographic_complexity
	 *            the new orthographic complexity
	 */
	public void setOrthographic_complexity(Double orthographic_complexity) {
		this.orthographic_complexity = orthographic_complexity;
	}

	/**
	 * Gets the phonologic complexity.
	 *
	 * @return the phonologic complexity
	 */
	public Double getPhonologic_complexity() {
		return phonologic_complexity;
	}

	/**
	 * Sets the phonologic complexity.
	 *
	 * @param phonologic_complexity
	 *            the new phonologic complexity
	 */
	public void setPhonologic_complexity(Double phonologic_complexity) {
		this.phonologic_complexity = phonologic_complexity;
	}
}
