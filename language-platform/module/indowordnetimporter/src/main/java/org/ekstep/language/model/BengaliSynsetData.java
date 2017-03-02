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
@Table(name = "tbl_all_bengali_synset_data")
public class BengaliSynsetData implements LanguageSynsetData {

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
	protected List<BengaliSynsetDataLite> hypernyms = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_noun_hyponymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = {
			@JoinColumn(name = "hyponymy_id") })
	protected List<BengaliSynsetDataLite> hyponyms = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_meronymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = {
			@JoinColumn(name = "meronym_id") })
	protected List<BengaliSynsetDataLite> meronyms = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_holonymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = {
			@JoinColumn(name = "holonym_id") })
	protected List<BengaliSynsetDataLite> holonyms = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_antonymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = {
			@JoinColumn(name = "antonym_id") })
	protected List<BengaliSynsetDataLite> antonyms = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_action_object", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = {
			@JoinColumn(name = "object_id") })
	protected List<BengaliSynsetDataLite> actions = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_action_object", joinColumns = { @JoinColumn(name = "object_id") }, inverseJoinColumns = {
			@JoinColumn(name = "synset_id") })
	protected List<BengaliSynsetDataLite> objects = new ArrayList<>();

	public BengaliSynsetData() {
		super();
	}

	public List<BengaliSynsetDataLite> getHypernyms() {
		return hypernyms;
	}

	public void setHypernyms(List<BengaliSynsetDataLite> hypernyms) {
		this.hypernyms = hypernyms;
	}

	public List<BengaliSynsetDataLite> getHyponyms() {
		return hyponyms;
	}

	public void setHyponyms(List<BengaliSynsetDataLite> hyponyms) {
		this.hyponyms = hyponyms;
	}

	public List<BengaliSynsetDataLite> getMeronyms() {
		return meronyms;
	}

	public void setMeronyms(List<BengaliSynsetDataLite> meronyms) {
		this.meronyms = meronyms;
	}

	public List<BengaliSynsetDataLite> getHolonyms() {
		return holonyms;
	}

	public void setHolonyms(List<BengaliSynsetDataLite> holonyms) {
		this.holonyms = holonyms;
	}

	public List<BengaliSynsetDataLite> getAntonyms() {
		return antonyms;
	}

	public void setAntonyms(List<BengaliSynsetDataLite> antonyms) {
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

	public List<BengaliSynsetDataLite> getActions() {
		return actions;
	}

	public void setActions(List<BengaliSynsetDataLite> actions) {
		this.actions = actions;
	}

	public List<BengaliSynsetDataLite> getObjects() {
		return objects;
	}

	public void setObjects(List<BengaliSynsetDataLite> objects) {
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

	private List<SynsetDataLite> getSynsetDataLiteList(List<BengaliSynsetDataLite> bengaliSynsetLiteList) {
		List<SynsetDataLite> synsetDataLiteList = new ArrayList<SynsetDataLite>();
		for (BengaliSynsetDataLite bengaliSynsetDataLite : bengaliSynsetLiteList) {
			SynsetDataLite liteSynsetData = bengaliSynsetDataLite.getSynsetDataLite();
			if (!synsetDataLiteList.contains(liteSynsetData)) {
				synsetDataLiteList.add(liteSynsetData);
			}
		}
		return synsetDataLiteList;
	}
}
