package org.ekstep.language.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SynsetData {

	private int synset_id;
	
	private int english_synset_id;

	private byte[] synset;

	private byte[] gloss;

	private String category;

	protected Map<String, List<SynsetDataLite>> translations = new HashMap<String, List<SynsetDataLite>>();
	
	protected Map<String, List<SynsetDataLite>> relations = new HashMap<String, List<SynsetDataLite>>();

	public SynsetData() {
		super();
	}
	
	public Map<String, List<SynsetDataLite>> getRelations() {
		return relations;
	}

	public void setRelations(Map<String, List<SynsetDataLite>> relations) {
		this.relations = relations;
	}

	
	public int getEnglish_synset_id() {
		return english_synset_id;
	}

	public void setEnglish_synset_id(int english_synset_id) {
		this.english_synset_id = english_synset_id;
	}

	public int getSynset_id() {
		return synset_id;
	}

	public void setSynset_id(int synset_id) {
		this.synset_id = synset_id;
	}

	public byte[] getSynset() {
		return synset;
	}

	public void setSynset(byte[] synset) {
		this.synset = synset;
	}

	public byte[] getGloss() {
		return gloss;
	}

	public void setGloss(byte[] gloss) {
		this.gloss = gloss;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Map<String, List<SynsetDataLite>> getTranslations() {
		return translations;
	}

	public void setTranslations(Map<String, List<SynsetDataLite>> translations) {
		this.translations = translations;
	}

}
