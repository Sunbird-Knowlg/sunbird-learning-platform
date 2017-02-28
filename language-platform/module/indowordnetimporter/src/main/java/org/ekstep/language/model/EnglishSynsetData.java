package org.ekstep.language.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.ekstep.language.common.enums.LanguageParams;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.WhereJoinTable;

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
	
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "english_hindi_id_mapping", joinColumns = @JoinColumn(name = "english_id") , inverseJoinColumns = @JoinColumn(name = "hindi_id") )
	@WhereJoinTable(clause = "type_link='Direct'")
	@Cascade(CascadeType.MERGE)
	private List<EnglishHindiSynsetData> englishHindiMappings;
	
	@Transient
	protected List<EnglishSynsetDataLite> hypernyms = new ArrayList<EnglishSynsetDataLite>();

	@Transient
	protected List<EnglishSynsetDataLite> hyponyms = new ArrayList<EnglishSynsetDataLite>();

	@Transient
	protected List<EnglishSynsetDataLite> meronyms = new ArrayList<EnglishSynsetDataLite>();

	@Transient
	protected List<EnglishSynsetDataLite> holonyms = new ArrayList<EnglishSynsetDataLite>();

	@Transient
	protected List<EnglishSynsetDataLite> antonyms = new ArrayList<EnglishSynsetDataLite>();

	@Transient
	protected List<EnglishSynsetDataLite> actions = new ArrayList<EnglishSynsetDataLite>();

	@Transient
	protected List<EnglishSynsetDataLite> objects = new ArrayList<EnglishSynsetDataLite>();

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

	@Override
	public SynsetData getSynsetData() {
		SynsetData synsetData = new SynsetData();
		if(this.englishHindiMappings!=null && this.englishHindiMappings.size()>0 
				&& this.englishHindiMappings.get(0).getSynset_id()!=0)
		{
			synsetData.setSynset_id(this.englishHindiMappings.get(0).getSynset_id());
		}else{
			synsetData.setSynset_id(this.synset_id);
		}
		synsetData.setEnglish_synset_id(this.synset_id);
		synsetData.setSynset(this.synset);
		synsetData.setGloss(this.gloss);
		synsetData.setCategory(this.category);

		// relations
		Map<String, List<SynsetDataLite>> relationsMap = new HashMap<String, List<SynsetDataLite>>();
		
		for(EnglishHindiSynsetData englishHindiSynsetData: getEnglishHindiMappings()){
			
			antonyms.addAll(englishHindiSynsetData.getFinalAntonyms());
			holonyms.addAll(englishHindiSynsetData.getFinalHolonyms());
			hypernyms.addAll(englishHindiSynsetData.getFinalHypernyms());
			hyponyms.addAll(englishHindiSynsetData.getFinalHyponyms());
			meronyms.addAll(englishHindiSynsetData.getFinalMeronyms());
			objects.addAll(englishHindiSynsetData.getFinalObjects());
			actions.addAll(englishHindiSynsetData.getFinalActions());
			
		}
		
		relationsMap.put(LanguageParams.antonyms.name(), getSynsetDataLiteList(getAntonyms()));
		relationsMap.put(LanguageParams.holonyms.name(), getSynsetDataLiteList(getHolonyms()));
		relationsMap.put(LanguageParams.hypernyms.name(), getSynsetDataLiteList(getHypernyms()));
		relationsMap.put(LanguageParams.hyponyms.name(), getSynsetDataLiteList(getHyponyms()));
		relationsMap.put(LanguageParams.meronyms.name(), getSynsetDataLiteList(getMeronyms()));
		relationsMap.put(LanguageParams.objects.name(), getSynsetDataLiteList(getObjects()));
		relationsMap.put(LanguageParams.actions.name(), getSynsetDataLiteList(getActions()));
		synsetData.setRelations(relationsMap);

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
