package org.ekstep.language.model;

public class CitationBean {
	
	private String word;
	private String rootWord;
	private String pos;
	private String date;
	private String sourceType;
	private String source;
	private String grade;
	private String fileName;
	
	public CitationBean(String word, String rootWord, String pos, String date,
			String sourceType, String source, String grade, String fileName) {
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

	public CitationBean() {
		super();
	}
	
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
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
	public String getPos() {
		return pos;
	}
	public void setPos(String position) {
		this.pos = position;
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

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	
}
