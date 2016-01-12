package org.ekstep.literacy.entity;

import java.util.List;

public class ParagraphComplexity {

	private String text;
	private Double meanOrthoComplexity;
	private Double totalOrthoComplexity;
	private Double meanPhonicComplexity;
	private Double totalPhonicComplexity;
	private List<WordComplexity> wordMeasures;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Double getMeanOrthoComplexity() {
		return meanOrthoComplexity;
	}

	public void setMeanOrthoComplexity(Double meanOrthoComplexity) {
		this.meanOrthoComplexity = meanOrthoComplexity;
	}

	public Double getTotalOrthoComplexity() {
		return totalOrthoComplexity;
	}

	public void setTotalOrthoComplexity(Double totalOrthoComplexity) {
		this.totalOrthoComplexity = totalOrthoComplexity;
	}

	public Double getMeanPhonicComplexity() {
		return meanPhonicComplexity;
	}

	public void setMeanPhonicComplexity(Double meanPhonicComplexity) {
		this.meanPhonicComplexity = meanPhonicComplexity;
	}

	public Double getTotalPhonicComplexity() {
		return totalPhonicComplexity;
	}

	public void setTotalPhonicComplexity(Double totalPhonicComplexity) {
		this.totalPhonicComplexity = totalPhonicComplexity;
	}

	public List<WordComplexity> getWordMeasures() {
		return wordMeasures;
	}

	public void setWordMeasures(List<WordComplexity> wordMeasures) {
		this.wordMeasures = wordMeasures;
	}

}
