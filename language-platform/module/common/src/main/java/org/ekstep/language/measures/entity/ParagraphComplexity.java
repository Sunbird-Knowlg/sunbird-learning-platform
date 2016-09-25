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
	private Double meanWordComplexity;
    private Double totalWordComplexity;
    private Double meanComplexity;
	private Map<String, ComplexityMeasures> wordMeasures = new HashMap<String, ComplexityMeasures>();
	private Map<String, Double> wordComplexityMap = new HashMap<String, Double>();
	private Map<String, Integer> wordFrequency = new HashMap<String, Integer>();
	private Map<String, Integer> syllableCountMap = new HashMap<String, Integer>();

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

    public Map<String, Integer> getWordFrequency() {
        return wordFrequency;
    }

    public void setWordFrequency(Map<String, Integer> wordFrequency) {
        this.wordFrequency = wordFrequency;
    }

    public Double getMeanWordComplexity() {
        return meanWordComplexity;
    }

    public void setMeanWordComplexity(Double meanWordComplexity) {
        this.meanWordComplexity = meanWordComplexity;
    }

    public Double getTotalWordComplexity() {
        return totalWordComplexity;
    }

    public void setTotalWordComplexity(Double totalWordComplexity) {
        this.totalWordComplexity = totalWordComplexity;
    }

    public Map<String, Double> getWordComplexityMap() {
        return wordComplexityMap;
    }

    public void setWordComplexityMap(Map<String, Double> wordComplexityMap) {
        this.wordComplexityMap = wordComplexityMap;
    }

    public Map<String, Integer> getSyllableCountMap() {
        return syllableCountMap;
    }

    public void setSyllableCountMap(Map<String, Integer> syllableCountMap) {
        this.syllableCountMap = syllableCountMap;
    }

	public Double getMeanComplexity() {
		return meanComplexity;
	}

	public void setMeanComplexity(Double meanComplexity) {
		this.meanComplexity = meanComplexity;
	}

}
