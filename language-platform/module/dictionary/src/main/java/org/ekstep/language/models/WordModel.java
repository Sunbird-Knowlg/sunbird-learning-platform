package org.ekstep.language.models;

import java.util.Map;

public class WordModel {
	String identifier = null;
	String languageId = null;
	String wordLemma = null;
	String sourceType = null;
	String idInSource = null;
	String usage = null;
	String meaning = null;
	String antonymOfWordId = null;
	String memberOfSynsetId = null;
	String hyponymOfWordId = null;
	String hypernymOfWordId = null;
	String homonymOfWordId = null;
	String meronymOfWordId = null;
	Integer totalCitation = null;
	Map<String, Integer> citationBySourceType = null;
	Map<String, Integer> citationBySource = null;
	Map<String, Integer> citationByPOS = null;
	Map<String, Integer> citationByGrad = null;
	
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
	
	public String getWordLemma () {
		return wordLemma;
	}
	
	public void setWordLemma (String wordLemma) {
		this.wordLemma = wordLemma;
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
	
	public void setIdInSource (String idInSource) {
		this.idInSource = idInSource;
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
	
	public String getAntonymOfWordId () {
		return antonymOfWordId;
	}
	
	public void setAntonymOfWordId (String antonymOfWordId) {
		this.antonymOfWordId = antonymOfWordId;
	}
	
	public String getMemberOfSynsetId () {
		return memberOfSynsetId;
	}
	
	public void setMemberOfSynsetId (String memberOfSynsetId) {
		this.memberOfSynsetId = memberOfSynsetId;
	}
	
	public String getHyponymOfWordId () {
		return hyponymOfWordId;
	}
	
	public void setHyponymOfWordId (String hyponymOfWordId) {
		this.hyponymOfWordId = hyponymOfWordId;
	}
	
	public String getHypernymOfWordId () {
		return hypernymOfWordId;
	}
	
	public void setHypernymOfWordId (String hypernymOfWordId) {
		this.hypernymOfWordId = hypernymOfWordId;
	}
	
	public String getHomonymOfWordId () {
		return homonymOfWordId;
	}
	
	public void setHomonymOfWordId (String homonymOfWordId) {
		this.homonymOfWordId = homonymOfWordId;
	}
	
	public String getMeronymOfWordId () {
		return meronymOfWordId;
	}
	
	public void setMeronymOfWordId (String meronymOfWordId) {
		this.meronymOfWordId = meronymOfWordId;
	}
	
	public Map<String, Integer> getCitationBySourceType() {
		return citationBySourceType;
	}

	public void setCitationBySourceType(Map<String, Integer> citationBySourceType) {
		this.citationBySourceType = citationBySourceType;
	}

	public Map<String, Integer> getCitationBySource() {
		return citationBySource;
	}

	public void setCitationBySource(Map<String, Integer> citationBySource) {
		this.citationBySource = citationBySource;
	}

	public Map<String, Integer> getCitationByPOS() {
		return citationByPOS;
	}

	public void setCitationByPOS(Map<String, Integer> citationByPOS) {
		this.citationByPOS = citationByPOS;
	}

	public Map<String, Integer> getCitationByGrad() {
		return citationByGrad;
	}

	public void setCitationByGrad(Map<String, Integer> citationByGrad) {
		this.citationByGrad = citationByGrad;
	}
	
	public Integer getTotalCitation() {
		return totalCitation;
	}

	public void setTotalCitation(Integer totalCitation) {
		this.totalCitation = totalCitation;
	}
	
	@Override
	public int hashCode(){
		return this.wordLemma.hashCode();
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null)
			return false;
		if(!(obj instanceof WordModel))
			return false;
		
		WordModel other = (WordModel) obj;
	    return this.wordLemma == other.wordLemma;
			
	}
}