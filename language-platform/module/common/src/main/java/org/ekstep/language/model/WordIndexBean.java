package org.ekstep.language.model;

public class WordIndexBean {
	private String word;
	private String rootWord;
	private String id;
	
	public WordIndexBean() {
		super();
	}
	
	public WordIndexBean(String word, String rootWord, String wordIdentifier) {
		super();
		this.word = word;
		this.rootWord = rootWord;
		this.id = wordIdentifier;
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
	public String getWordIdentifier() {
		return id;
	}
	public void setWordIdentifier(String wordIdentifier) {
		this.id = wordIdentifier;
	}
}
