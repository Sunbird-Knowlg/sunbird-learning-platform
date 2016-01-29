package org.ekstep.language.model;

public class WordPojo {

	String word;
	String rootWord;

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

	public WordPojo() {
		super();
	}

	public WordPojo(String word, String rootWord) {
		super();
		this.word = word;
		this.rootWord = rootWord;
	}

	public String[] getStringArray() {
		String[] data = new String[2];
		data[0] = word;
		data[1] = rootWord;
		return data;
	}

	@Override
	public int hashCode() {
		return 37 * this.word.hashCode() * this.rootWord.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
	    if(o == null) return false;
	    if(!(o instanceof WordPojo)) return false;

	    WordPojo other = (WordPojo) o;
	    if(! this.word.equals(other.word)) return false;
	    if(! this.rootWord.equals(other.rootWord))   return false;

	    return true;
	  }
}
