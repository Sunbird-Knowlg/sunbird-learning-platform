package org.ekstep.language.measures.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ParagraphComplexity implements Serializable {

    private static final long serialVersionUID = -2895861316069551961L;
    private String text;
    private int wordCount;
    private int syllableCount;
	private Double meanOrthoComplexity;
	private Double totalOrthoComplexity;
	private Double meanPhonicComplexity;
	private Double totalPhonicComplexity;
	private Map<String, ComplexityMeasures> wordMeasures = new HashMap<String, ComplexityMeasures>();

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

	public Map<String, ComplexityMeasures> getWordMeasures() {
		return wordMeasures;
	}

	public void setWordMeasures(Map<String, ComplexityMeasures> wordMeasures) {
		this.wordMeasures = wordMeasures;
	}
	
	public ComplexityMeasures measures() {
	    return new ComplexityMeasures(meanOrthoComplexity, meanPhonicComplexity);
	}

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public int getSyllableCount() {
        return syllableCount;
    }

    public void setSyllableCount(int syllableCount) {
        this.syllableCount = syllableCount;
    }

}
