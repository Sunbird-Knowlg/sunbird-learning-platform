package org.ekstep.language.model;

public class WordInfoBean {
	
	private String word;
	private String rootWord;
	private String pos;
	private String gender;
	private String number;
	private String pers;
	private String grammaticalCase;
	private String inflection;
	private String rts;
	
	public WordInfoBean(String word, String rootWord, String pos,
			String gender, String number, String pers, String wordCase,
			String inflection, String rts) {
		super();
		this.word = word;
		this.rootWord = rootWord;
		this.pos = pos;
		this.gender = gender;
		this.number = number;
		this.pers = pers;
		this.grammaticalCase = wordCase;
		this.inflection = inflection;
		this.rts = rts;
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
	public void setPos(String pos) {
		this.pos = pos;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getPers() {
		return pers;
	}
	public void setPers(String pers) {
		this.pers = pers;
	}

	public String getGrammaticalCase() {
		return grammaticalCase;
	}
	public void setGrammaticalCase(String grammaticalCase) {
		this.grammaticalCase = grammaticalCase;
	}
	public String getInflection() {
		return inflection;
	}
	public void setInflection(String inflection) {
		this.inflection = inflection;
	}
	public String getRts() {
		return rts;
	}
	public void setRts(String rts) {
		this.rts = rts;
	}
}
