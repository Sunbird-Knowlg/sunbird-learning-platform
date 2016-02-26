package org.ekstep.language.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DictionaryObject {
	List<SynsetModel> lstSynset = null;
	List<WordModel> lstWord = null;
	Map<String, Object> metaData = new HashMap<String, Object>();
	
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
	
	public Object get(String key) {
		return metaData.get(key);
	}
	public void put(String key, Object value) {
		this.metaData.put(key, value);
	}
} 