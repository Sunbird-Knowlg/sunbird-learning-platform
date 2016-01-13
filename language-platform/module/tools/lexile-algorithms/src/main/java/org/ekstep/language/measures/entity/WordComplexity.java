package org.ekstep.language.measures.entity;

import java.io.Serializable;
import java.util.Comparator;

public class WordComplexity implements Serializable {

    private static final long serialVersionUID = -837291979119278370L;
    private String word;
	private String rts;
	private int count;
	private String notation;
	private String unicode;
	private Integer[] orthoVec;
	private Integer[] phonicVec;
	private Double orthoComplexity;
	private Double phonicComplexity;

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getNotation() {
		return notation;
	}

	public void setNotation(String notation) {
		this.notation = notation;
	}

	public Integer[] getOrthoVec() {
		return orthoVec;
	}

	public void setOrthoVec(Integer[] orthoVec) {
		this.orthoVec = orthoVec;
	}

	public Integer[] getPhonicVec() {
		return phonicVec;
	}

	public void setPhonicVec(Integer[] phonicVec) {
		this.phonicVec = phonicVec;
	}

	public Double getOrthoComplexity() {
		return orthoComplexity;
	}

	public void setOrthoComplexity(Double orthoComplexity) {
		this.orthoComplexity = orthoComplexity;
	}

	public Double getPhonicComplexity() {
		return phonicComplexity;
	}

	public void setPhonicComplexity(Double phonicComplexity) {
		this.phonicComplexity = phonicComplexity;
	}

	public String getUnicode() {
		return unicode;
	}

	public void setUnicode(String unicode) {
		this.unicode = unicode;
	}

	public String getRts() {
		return rts;
	}

	public void setRts(String rts) {
		this.rts = rts;
	}

	public static Comparator<WordComplexity> wordComparator = new Comparator<WordComplexity>() {
		public int compare(WordComplexity o1, WordComplexity o2) {
			String word1 = o1.getWord();
			String word2 = o2.getWord();
			if (null != word1)
				return word1.compareTo(word2);
			return -1;
		};
	};

	public static Comparator<WordComplexity> phonicComplexityComparator = new Comparator<WordComplexity>() {
		public int compare(WordComplexity o1, WordComplexity o2) {
			Double phonicComplexity1 = o1.getPhonicComplexity();
			Double phonicComplexity2 = o2.getPhonicComplexity();
			if (null != phonicComplexity1)
				return phonicComplexity1.compareTo(phonicComplexity2);
			return -1;
		};
	};
	
	public ComplexityMeasures getMeasures() {
        return new ComplexityMeasures(orthoComplexity, phonicComplexity);
    }
}
