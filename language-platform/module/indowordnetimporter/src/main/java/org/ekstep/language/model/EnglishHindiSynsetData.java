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

import org.ekstep.language.common.enums.LanguageParams;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "tbl_all_hindi_synset_data")
public class EnglishHindiSynsetData implements LanguageSynsetData {

	@Id
	private int synset_id;

	@Column(name = "synset", unique = false, nullable = false, length = 900000)
	private byte[] synset;

	@Column(name = "gloss", unique = false, nullable = false, length = 900000)
	private byte[] gloss;

	private String category;

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_noun_hypernymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = {
			@JoinColumn(name = "hypernymy_id") })
	protected List<EnglishHindiSynsetDataLite> hypernyms = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_noun_hyponymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = {
			@JoinColumn(name = "hyponymy_id") })
	protected List<EnglishHindiSynsetDataLite> hyponyms = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_meronymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = {
			@JoinColumn(name = "meronym_id") })
	protected List<EnglishHindiSynsetDataLite> meronyms = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_holonymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = {
			@JoinColumn(name = "holonym_id") })
	protected List<EnglishHindiSynsetDataLite> holonyms = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_antonymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = {
			@JoinColumn(name = "antonym_id") })
	protected List<EnglishHindiSynsetDataLite> antonyms = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_action_object", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = {
			@JoinColumn(name = "object_id") })
	protected List<EnglishHindiSynsetDataLite> actions = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_action_object", joinColumns = { @JoinColumn(name = "object_id") }, inverseJoinColumns = {
			@JoinColumn(name = "synset_id") })
	protected List<EnglishHindiSynsetDataLite> objects = new ArrayList<>();

	public EnglishHindiSynsetData() {
		super();
	}

	public List<EnglishHindiSynsetDataLite> getHypernyms() {
		return hypernyms;
	}

	public void setHypernyms(List<EnglishHindiSynsetDataLite> hypernyms) {
		this.hypernyms = hypernyms;
	}

	public List<EnglishHindiSynsetDataLite> getHyponyms() {
		return hyponyms;
	}

	public void setHyponyms(List<EnglishHindiSynsetDataLite> hyponyms) {
		this.hyponyms = hyponyms;
	}

	public List<EnglishHindiSynsetDataLite> getMeronyms() {
		return meronyms;
	}

	public void setMeronyms(List<EnglishHindiSynsetDataLite> meronyms) {
		this.meronyms = meronyms;
	}

	public List<EnglishHindiSynsetDataLite> getHolonyms() {
		return holonyms;
	}

	public void setHolonyms(List<EnglishHindiSynsetDataLite> holonyms) {
		this.holonyms = holonyms;
	}

	public List<EnglishHindiSynsetDataLite> getAntonyms() {
		return antonyms;
	}

	public List<EnglishSynsetDataLite> getFinalAntonyms() {
		return getFinalRelations(antonyms);
	}
	
	public List<EnglishSynsetDataLite> getFinalHypernyms() {
		return getFinalRelations(hypernyms);
	}
	public List<EnglishSynsetDataLite> getFinalHyponyms() {
		return getFinalRelations(hyponyms);
	}
	public List<EnglishSynsetDataLite> getFinalHolonyms() {
		return getFinalRelations(holonyms);
	}
	public List<EnglishSynsetDataLite> getFinalMeronyms() {
		return getFinalRelations(meronyms);
	}
	public List<EnglishSynsetDataLite> getFinalActions() {
		return getFinalRelations(actions);
	}
	public List<EnglishSynsetDataLite> getFinalObjects() {
		return getFinalRelations(objects);
	}
	
	public List<EnglishSynsetDataLite> getFinalRelations(List<EnglishHindiSynsetDataLite> relations) {
		List<EnglishSynsetDataLite> finalRelations = new ArrayList<EnglishSynsetDataLite>();
		for(EnglishHindiSynsetDataLite relation: relations){
			finalRelations.addAll(relation.getEnglishMappings());
		}
		return finalRelations;
	}
	public void setAntonyms(List<EnglishHindiSynsetDataLite> antonyms) {
		this.antonyms = antonyms;
	}

	public int getSynset_id() {
		return synset_id;
	}

	public int getSynsetId() {
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

	public List<EnglishHindiSynsetDataLite> getActions() {
		return actions;
	}

	public void setActions(List<EnglishHindiSynsetDataLite> actions) {
		this.actions = actions;
	}

	public List<EnglishHindiSynsetDataLite> getObjects() {
		return objects;
	}

	public void setObjects(List<EnglishHindiSynsetDataLite> objects) {
		this.objects = objects;
	}

	public SynsetData getSynsetData() {
		SynsetData synsetData = new SynsetData();
		synsetData.setSynset_id(this.synset_id);
		synsetData.setSynset(this.synset);
		synsetData.setGloss(this.gloss);
		synsetData.setCategory(this.category);

		// relations
		Map<String, List<SynsetDataLite>> relationsMap = new HashMap<String, List<SynsetDataLite>>();
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

	private List<SynsetDataLite> getSynsetDataLiteList(List<EnglishHindiSynsetDataLite> hindiSynsetLiteList) {
		List<SynsetDataLite> synsetDataLiteList = new ArrayList<SynsetDataLite>();
		for (EnglishHindiSynsetDataLite hindiSynsetDataLite : hindiSynsetLiteList) {
			SynsetDataLite liteSynsetData = hindiSynsetDataLite.getSynsetDataLite();
			if (!synsetDataLiteList.contains(liteSynsetData)) {
				synsetDataLiteList.add(liteSynsetData);
			}
		}
		return synsetDataLiteList;
	}

	public HindiSynsetDataLite getHindiTranslation() {
		HindiSynsetDataLite temp = new HindiSynsetDataLite();
		temp.setSynset_id(this.synset_id);
		temp.setGloss(this.gloss);
		temp.setCategory(this.category);
		return temp;
	}
}
