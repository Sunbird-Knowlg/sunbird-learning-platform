package org.ekstep.language.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SynsetData {

	private int synset_id;

	private byte[] synset;

	private byte[] gloss;

	private String category;

	protected List<SynsetDataLite> hypernyms = new ArrayList<>();

	protected List<SynsetDataLite> hyponyms = new ArrayList<>();

	protected List<SynsetDataLite> meronyms = new ArrayList<>();

	protected List<SynsetDataLite> holonyms = new ArrayList<>();

	protected List<SynsetDataLite> antonyms = new ArrayList<>();

	protected List<SynsetDataLite> actionObjects = new ArrayList<>();
	
	protected List<SynsetDataLite> actions = new ArrayList<>();

	protected Map<String, SynsetDataLite> translations = new HashMap<String, SynsetDataLite>();

	public SynsetData() {
		super();
	}

	public List<SynsetDataLite> getActions() {
		return actions;
	}

	public void setActions(List<SynsetDataLite> actions) {
		this.actions = actions;
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


	public List<SynsetDataLite> getHypernyms() {
		return hypernyms;
	}

	public void setHypernyms(List<SynsetDataLite> hypernyms) {
		this.hypernyms = hypernyms;
	}

	public List<SynsetDataLite> getHyponyms() {
		return hyponyms;
	}

	public void setHyponyms(List<SynsetDataLite> hyponyms) {
		this.hyponyms = hyponyms;
	}

	public List<SynsetDataLite> getMeronyms() {
		return meronyms;
	}

	public void setMeronyms(List<SynsetDataLite> meronyms) {
		this.meronyms = meronyms;
	}

	public List<SynsetDataLite> getHolonyms() {
		return holonyms;
	}

	public void setHolonyms(List<SynsetDataLite> holonyms) {
		this.holonyms = holonyms;
	}

	public List<SynsetDataLite> getAntonyms() {
		return antonyms;
	}

	public void setAntonyms(List<SynsetDataLite> antonyms) {
		this.antonyms = antonyms;
	}

	public List<SynsetDataLite> getActionObjects() {
		return actionObjects;
	}

	public void setActionObjects(List<SynsetDataLite> actionObjects) {
		this.actionObjects = actionObjects;
	}

	public Map<String, SynsetDataLite> getTranslations() {
		return translations;
	}

	public void setTranslations(Map<String, SynsetDataLite> translations) {
		this.translations = translations;
	}

}
