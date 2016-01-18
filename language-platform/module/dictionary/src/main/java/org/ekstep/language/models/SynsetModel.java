package org.ekstep.language.models;

public class SynsetModel {
	String identifier = null;
	String languageId = null;
	String wordMember = null;
	String sourceType = null;
	String idInSource = null;
	String usage = null;
	String meaning = null;
	String antonymSynsetId = null;
	String hyponymSynsetId = null;
	String hypernymSynsetId = null;
	String holonymSynsetId = null;
	String meronymSynsetId = null;
	String partOfSpeech = null;
	
	public String getPartOfSpeech() {
		return partOfSpeech;
	}

	public void setPartOfSpeech(String partOfSpeech) {
		this.partOfSpeech = partOfSpeech;
	}

	public String getIdentifier () {
		return identifier;
	}
	
	public void setIdentifier (String identifier) {
		this.identifier = identifier;
	}
	
	public String getLanguageId () {
		return languageId;
	}
	
	public void setLanguageId (String languageId) {
		this.languageId = languageId;
	}
	
	public String getWordMember () {
		return wordMember;
	}
	
	public void setWordMember (String wordMember) {
		this.wordMember = wordMember;
	}
	
	public String getSourceType () {
		return sourceType;
	}
	
	public void setSourceType (String sourceType) {
		this.sourceType = sourceType;
	}
	
	public String getIdInSource () {
		return idInSource;
	}

	public String getUsage () {
		return usage;
	}
	
	public void setUsage (String usage) {
		this.usage = usage;
	}
	
	public String getMeaning () {
		return meaning;
	}
	
	public void setMeaning (String meaning) {
		this.meaning = meaning;
	}
	
	public void setIdInSource (String idInSource) {
		this.idInSource = idInSource;
	}
	
	public String getAntonymSynsetId () {
		return antonymSynsetId;
	}
	
	public void setAntonymSynsetId (String antonymSynsetId) {
		this.antonymSynsetId = antonymSynsetId;
	}
		
	public String getHyponymSynsetId () {
		return hyponymSynsetId;
	}
	
	public void setHyponymSynsetId (String hyponymSynsetId) {
		this.hyponymSynsetId = hyponymSynsetId;
	}
	
	public String getHypernymSynsetId () {
		return hypernymSynsetId;
	}
	
	public void setHypernymSynsetId (String hypernymSynsetId) {
		this.hypernymSynsetId = hypernymSynsetId;
	}
	
	public String getHolonymSynsetId () {
		return holonymSynsetId;
	}
	
	public void setHolonymSynsetId (String holonymSynsetId) {
		this.holonymSynsetId = holonymSynsetId;
	}
	
	public String getMeronymSynsetId () {
		return meronymSynsetId;
	}
	
	public void setMeronymSynsetId (String meronymSynsetId) {
		this.meronymSynsetId = meronymSynsetId;
	}
}