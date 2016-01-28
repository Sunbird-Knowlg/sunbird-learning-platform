package org.ekstep.language.models;

import java.util.List;

public class DictionaryObject {
	List<SynsetModel> lstSynset = null;
	List<WordModel> lstWord = null;
	
	public List<SynsetModel> getLstSynset() {
		return lstSynset;
	}
	public void setLstSynset(List<SynsetModel> lstSynset) {
		this.lstSynset = lstSynset;
	}
	public List<WordModel> getLstWord() {
		return lstWord;
	}
	public void setLstWord(List<WordModel> lstWord) {
		this.lstWord = lstWord;
	}
} 