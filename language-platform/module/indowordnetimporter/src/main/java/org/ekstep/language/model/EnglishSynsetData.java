package org.ekstep.language.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.ekstep.language.common.enums.LanguageParams;

@Entity
@Table(name = "tbl_all_english_synset_data")
public class EnglishSynsetData implements LanguageSynsetData{

	@Id
	private int synset_id;

	@Column(name = "synset", unique = false, nullable = false, length = 900000)
	private byte[] synset;

	@Column(name = "gloss", unique = false, nullable = false, length = 900000)
	private byte[] gloss;

	private String category;
	
	private List<EnglishHindiSynsetData> englishHindiMappings;
	
	protected List<EnglishSynsetDataLite> hypernyms = new ArrayList<>();

	protected List<EnglishSynsetDataLite> hyponyms = new ArrayList<>();

	protected List<EnglishSynsetDataLite> meronyms = new ArrayList<>();

	protected List<EnglishSynsetDataLite> holonyms = new ArrayList<>();

	protected List<EnglishSynsetDataLite> antonyms = new ArrayList<>();

	protected List<EnglishSynsetDataLite> actions = new ArrayList<>();

	protected List<EnglishSynsetDataLite> objects = new ArrayList<>();

	protected List<SynsetDataLite> assameseTranslation;

	protected List<SynsetDataLite> bengaliTranslation;

	protected List<SynsetDataLite> bodoTranslation;

	protected List<SynsetDataLite> gujaratiTranslation;

	protected List<SynsetDataLite> hindiTranslation;

	protected List<SynsetDataLite> kannadaTranslation;

	protected List<SynsetDataLite> konkaniTranslation;

	protected List<SynsetDataLite> malayalamTranslation;

	protected List<SynsetDataLite> marathiTranslation;

	protected List<SynsetDataLite> nepaliTranslation;

	protected List<SynsetDataLite> oriyaTranslation;

	protected List<SynsetDataLite> punjabiTranslation;

	protected List<SynsetDataLite> sanskritTranslation;

	protected List<SynsetDataLite> teluguTranslation;
	
	protected List<SynsetDataLite> tamilTranslation;

	public EnglishSynsetData() {
		super();
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

	public List<EnglishHindiSynsetData> getEnglishHindiMappings() {
		return englishHindiMappings;
	}

	public void setEnglishHindiMappings(List<EnglishHindiSynsetData> englishHindiMappings) {
		this.englishHindiMappings = englishHindiMappings;
	}

	public List<EnglishSynsetDataLite> getHypernyms() {
		return hypernyms;
	}

	public List<EnglishSynsetDataLite> getHyponyms() {
		return hyponyms;
	}

	public List<EnglishSynsetDataLite> getMeronyms() {
		return meronyms;
	}

	public List<EnglishSynsetDataLite> getHolonyms() {
		return holonyms;
	}

	public List<EnglishSynsetDataLite> getAntonyms() {
		return antonyms;
	}

	public List<EnglishSynsetDataLite> getActions() {
		return actions;
	}

	public List<EnglishSynsetDataLite> getObjects() {
		return objects;
	}

	public List<SynsetDataLite> getAssameseTranslation() {
		return assameseTranslation;
	}

	public List<SynsetDataLite> getBengaliTranslation() {
		return bengaliTranslation;
	}

	public List<SynsetDataLite> getBodoTranslation() {
		return bodoTranslation;
	}

	public List<SynsetDataLite> getGujaratiTranslation() {
		return gujaratiTranslation;
	}

	public List<SynsetDataLite> getHindiTranslation() {
		return hindiTranslation;
	}

	public List<SynsetDataLite> getKannadaTranslation() {
		return kannadaTranslation;
	}

	public List<SynsetDataLite> getKonkaniTranslation() {
		return konkaniTranslation;
	}

	public List<SynsetDataLite> getMalayalamTranslation() {
		return malayalamTranslation;
	}

	public List<SynsetDataLite> getMarathiTranslation() {
		return marathiTranslation;
	}

	public List<SynsetDataLite> getNepaliTranslation() {
		return nepaliTranslation;
	}

	public List<SynsetDataLite> getOriyaTranslation() {
		return oriyaTranslation;
	}

	public List<SynsetDataLite> getPunjabiTranslation() {
		return punjabiTranslation;
	}

	public List<SynsetDataLite> getSanskritTranslation() {
		return sanskritTranslation;
	}

	public List<SynsetDataLite> getTeluguTranslation() {
		return teluguTranslation;
	}

	public List<SynsetDataLite> getTamilTranslation() {
		return tamilTranslation;
	}

	@Override
	public SynsetData getSynsetData() {
		SynsetData synsetData = new SynsetData();
		synsetData.setSynset_id(this.synset_id);
		synsetData.setSynset(this.synset);
		synsetData.setGloss(this.gloss);
		synsetData.setCategory(this.category);

		// relations
		Map<String, List<SynsetDataLite>> relationsMap = new HashMap<String, List<SynsetDataLite>>();
		
		for(EnglishHindiSynsetData englishHindiSynsetData: getEnglishHindiMappings()){
			antonyms.addAll(englishHindiSynsetData.getAntonyms());
			holonyms.addAll(englishHindiSynsetData.getHolonyms());
			hypernyms.addAll(englishHindiSynsetData.getHypernyms());
			hyponyms.addAll(englishHindiSynsetData.getHyponyms());
			meronyms.addAll(englishHindiSynsetData.getMeronyms());
			objects.addAll(englishHindiSynsetData.getObjects());
			actions.addAll(englishHindiSynsetData.getActions());
			
			assameseTranslation.add(englishHindiSynsetData.getAssameseTranslation().getSynsetDataLite());
			bengaliTranslation.add(englishHindiSynsetData.getBengaliTranslation().getSynsetDataLite());
			bodoTranslation.add(englishHindiSynsetData.getBodoTranslation().getSynsetDataLite());
			gujaratiTranslation.add(englishHindiSynsetData.getGujaratiTranslation().getSynsetDataLite());
			hindiTranslation.add(englishHindiSynsetData.getHindiTranslation().getSynsetDataLite());
			kannadaTranslation.add(englishHindiSynsetData.getKannadaTranslation().getSynsetDataLite());
			konkaniTranslation.add(englishHindiSynsetData.getKonkaniTranslation().getSynsetDataLite());
			malayalamTranslation.add(englishHindiSynsetData.getMalayalamTranslation().getSynsetDataLite());
			marathiTranslation.add(englishHindiSynsetData.getMarathiTranslation().getSynsetDataLite());
			nepaliTranslation.add(englishHindiSynsetData.getNepaliTranslation().getSynsetDataLite());
			oriyaTranslation.add(englishHindiSynsetData.getOriyaTranslation().getSynsetDataLite());
			punjabiTranslation.add(englishHindiSynsetData.getPunjabiTranslation().getSynsetDataLite());
			sanskritTranslation.add(englishHindiSynsetData.getSanskritTranslation().getSynsetDataLite());
			teluguTranslation.add(englishHindiSynsetData.getTeluguTranslation().getSynsetDataLite());
		}
		
		relationsMap.put(LanguageParams.antonyms.name(), getSynsetDataLiteList(getAntonyms()));
		relationsMap.put(LanguageParams.holonyms.name(), getSynsetDataLiteList(getHolonyms()));
		relationsMap.put(LanguageParams.hypernyms.name(), getSynsetDataLiteList(getHypernyms()));
		relationsMap.put(LanguageParams.hyponyms.name(), getSynsetDataLiteList(getHyponyms()));
		relationsMap.put(LanguageParams.meronyms.name(), getSynsetDataLiteList(getMeronyms()));
		relationsMap.put(LanguageParams.objects.name(), getSynsetDataLiteList(getObjects()));
		relationsMap.put(LanguageParams.actions.name(), getSynsetDataLiteList(getActions()));
		synsetData.setRelations(relationsMap);

		// translations
		Map<String, List<SynsetDataLite>> translationsMap = new HashMap<String, List<SynsetDataLite>>();
		if (getAssameseTranslation() != null)
			translationsMap.put("Assamese", getAssameseTranslation());
		if (getBengaliTranslation() != null)
			translationsMap.put("Bengali", getBengaliTranslation());
		if (getBodoTranslation() != null)
			translationsMap.put("Bodo", getBodoTranslation());
		if (getGujaratiTranslation() != null)
			translationsMap.put("Gujarati", getGujaratiTranslation());
		if (getHindiTranslation() != null)
			translationsMap.put("Hindi", getHindiTranslation());
		if (getKannadaTranslation() != null)
			translationsMap.put("Konkani", getKannadaTranslation());
		if (getKonkaniTranslation() != null)
			translationsMap.put("Konkani", getKonkaniTranslation());
		if (getMalayalamTranslation() != null)
			translationsMap.put("Malayalam", getMalayalamTranslation());
		if (getMarathiTranslation() != null)
			translationsMap.put("Konkani", getMarathiTranslation());
		if (getNepaliTranslation() != null)
			translationsMap.put("Nepali", getNepaliTranslation());
		if (getOriyaTranslation() != null)
			translationsMap.put("Oriya", getOriyaTranslation());
		if (getPunjabiTranslation() != null)
			translationsMap.put("Punjabi", getPunjabiTranslation());
		if (getSanskritTranslation() != null)
			translationsMap.put("Sanskrit", getSanskritTranslation());
		if (getTeluguTranslation() != null)
			translationsMap.put("Telugu", getTeluguTranslation());
		if (getTamilTranslation() != null)
			translationsMap.put("Tamil", getTamilTranslation());
		synsetData.setTranslations(translationsMap);

		return synsetData;

	}
	
	private List<SynsetDataLite> getSynsetDataLiteList(List<EnglishSynsetDataLite> englishSynsetLiteList) {
		List<SynsetDataLite> synsetDataLiteList = new ArrayList<SynsetDataLite>();
		for (EnglishSynsetDataLite englishSynsetDataLite : englishSynsetLiteList) {
			SynsetDataLite liteSynsetData = englishSynsetDataLite.getSynsetDataLite();
			if (!synsetDataLiteList.contains(liteSynsetData)) {
				synsetDataLiteList.add(liteSynsetData);
			}
		}
		return synsetDataLiteList;
	}
}
