package org.ekstep.language.model;

public class CitationBean {
	
	private String word;
	private String rootWord;
	private String position;
	private String date;
	private String sourceType;
	private String grade;
	
	public CitationBean(String word, String rootWord, String position,
			String date, String sourceType, String grade) {
		super();
		this.word = word;
		this.rootWord = rootWord;
		this.position = position;
		this.date = date;
		this.sourceType = sourceType;
		this.grade = grade;
	}

	public CitationBean() {
		super();
	}
	
	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public String getRootWord() {
		return rootWord;
	}
	public void setRootWord(String rootWord) {
		this.rootWord = rootWord;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getSourceType() {
		return sourceType;
	}
	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}
	
	
}
