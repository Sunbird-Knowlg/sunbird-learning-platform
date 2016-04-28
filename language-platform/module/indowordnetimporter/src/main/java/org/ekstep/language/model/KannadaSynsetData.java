package org.ekstep.language.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.ekstep.language.common.enums.LanguageParams;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.WhereJoinTable;

import javax.persistence.JoinColumn;

@Entity
@Table(name = "tbl_all_kannada_synset_data")
public class KannadaSynsetData implements LanguageSynsetData {

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
	protected List<KannadaSynsetDataLite> hypernyms = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_noun_hyponymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = {
			@JoinColumn(name = "hyponymy_id") })
	protected List<KannadaSynsetDataLite> hyponyms = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_meronymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = {
			@JoinColumn(name = "meronym_id") })
	protected List<KannadaSynsetDataLite> meronyms = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_holonymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = {
			@JoinColumn(name = "holonym_id") })
	protected List<KannadaSynsetDataLite> holonyms = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_antonymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = {
			@JoinColumn(name = "antonym_id") })
	protected List<KannadaSynsetDataLite> antonyms = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_action_object", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = {
			@JoinColumn(name = "object_id") })
	protected List<KannadaSynsetDataLite> actions = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_action_object", joinColumns = { @JoinColumn(name = "object_id") }, inverseJoinColumns = {
			@JoinColumn(name = "synset_id") })
	protected List<KannadaSynsetDataLite> objects = new ArrayList<>();

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "synset_id", referencedColumnName = "synset_id", nullable = true)
	@Cascade(CascadeType.MERGE)
	protected TamilSynsetDataLite tamilTranslation;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "synset_id", referencedColumnName = "synset_id", nullable = true)
	@Cascade(CascadeType.MERGE)
	protected AssameseSynsetDataLite assameseTranslation;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "synset_id", referencedColumnName = "synset_id", nullable = true)
	@Cascade(CascadeType.MERGE)
	protected BengaliSynsetDataLite bengaliTranslation;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "synset_id", referencedColumnName = "synset_id", nullable = true)
	@Cascade(CascadeType.MERGE)
	protected BodoSynsetDataLite bodoTranslation;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "synset_id", referencedColumnName = "synset_id", nullable = true)
	@Cascade(CascadeType.MERGE)
	protected GujaratiSynsetDataLite gujaratiTranslation;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name="english_hindi_id_mapping", joinColumns = @JoinColumn(name="hindi_id"),	inverseJoinColumns = @JoinColumn(name="english_id"))
	@WhereJoinTable(clause = "type_link='Direct'")
	@Cascade(CascadeType.MERGE)
	private List<EnglishSynsetDataLite> englishTranslations;

	public KannadaSynsetData() {
		super();
	}

	public TamilSynsetDataLite getTamilTranslation() {
		return tamilTranslation;
	}

	public void setTamilTranslation(TamilSynsetDataLite tamilTranslation) {
		this.tamilTranslation = tamilTranslation;
	}

	public AssameseSynsetDataLite getAssameseTranslation() {
		return assameseTranslation;
	}

	public void setAssameseTranslation(AssameseSynsetDataLite assameseTranslation) {
		this.assameseTranslation = assameseTranslation;
	}

	public BengaliSynsetDataLite getBengaliTranslation() {
		return bengaliTranslation;
	}

	public void setBengaliTranslation(BengaliSynsetDataLite bengaliTranslation) {
		this.bengaliTranslation = bengaliTranslation;
	}

	public BodoSynsetDataLite getBodoTranslation() {
		return bodoTranslation;
	}

	public void setBodoTranslation(BodoSynsetDataLite bodoTranslation) {
		this.bodoTranslation = bodoTranslation;
	}

	public GujaratiSynsetDataLite getGujaratiTranslation() {
		return gujaratiTranslation;
	}

	public void setGujaratiTranslation(GujaratiSynsetDataLite gujaratiTranslation) {
		this.gujaratiTranslation = gujaratiTranslation;
	}

	public List<EnglishSynsetDataLite> getEnglishTranslations() {
		return englishTranslations;
	}

	public void setEnglishTranslations(List<EnglishSynsetDataLite> englishTranslations) {
		this.englishTranslations = englishTranslations;
	}

	public List<KannadaSynsetDataLite> getMeronyms() {
		return meronyms;
	}

	public void setMeronyms(List<KannadaSynsetDataLite> meronyms) {
		this.meronyms = meronyms;
	}

	public List<KannadaSynsetDataLite> getHolonyms() {
		return holonyms;
	}

	public void setHolonyms(List<KannadaSynsetDataLite> holonyms) {
		this.holonyms = holonyms;
	}

	public List<KannadaSynsetDataLite> getAntonyms() {
		return antonyms;
	}

	public void setAntonyms(List<KannadaSynsetDataLite> antonyms) {
		this.antonyms = antonyms;
	}

	public List<KannadaSynsetDataLite> getHyponyms() {
		return hyponyms;
	}

	public void setHyponyms(List<KannadaSynsetDataLite> hyponyms) {
		this.hyponyms = hyponyms;
	}

	public List<KannadaSynsetDataLite> getHypernyms() {
		return hypernyms;
	}

	public void setHypernyms(List<KannadaSynsetDataLite> hypernyms) {
		this.hypernyms = hypernyms;
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

	public List<KannadaSynsetDataLite> getActions() {
		return actions;
	}

	public void setActions(List<KannadaSynsetDataLite> actions) {
		this.actions = actions;
	}

	public List<KannadaSynsetDataLite> getObjects() {
		return objects;
	}

	public void setObjects(List<KannadaSynsetDataLite> objects) {
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

		// translations
		Map<String, List<SynsetDataLite>> translationsMap = new HashMap<String, List<SynsetDataLite>>();
		if (getAssameseTranslation() != null)
			translationsMap.put("Assamese", Arrays.asList(getAssameseTranslation().getSynsetDataLite()));
		if (getBengaliTranslation() != null)
			translationsMap.put("Bengali", Arrays.asList(getBengaliTranslation().getSynsetDataLite()));
		if (getBodoTranslation() != null)
			translationsMap.put("Bodo", Arrays.asList(getBodoTranslation().getSynsetDataLite()));
		if (getGujaratiTranslation() != null)
			translationsMap.put("Gujarati", Arrays.asList(getGujaratiTranslation().getSynsetDataLite()));
		if (getTamilTranslation() != null)
			translationsMap.put("Kannada", Arrays.asList(getTamilTranslation().getSynsetDataLite()));
		if (getEnglishTranslations() != null)
			translationsMap.put("English", getEnglishSynsetDataLiteList(getEnglishTranslations()));
		synsetData.setTranslations(translationsMap);

		return synsetData;
	}

	private List<SynsetDataLite> getSynsetDataLiteList(List<KannadaSynsetDataLite> languageSynsetLiteList) {
		List<SynsetDataLite> synsetDataLiteList = new ArrayList<SynsetDataLite>();
		for (KannadaSynsetDataLite langSynsetDataLite : languageSynsetLiteList) {
			synsetDataLiteList.add(langSynsetDataLite.getSynsetDataLite());
		}
		return synsetDataLiteList;
	}
	
	private List<SynsetDataLite> getEnglishSynsetDataLiteList(List<EnglishSynsetDataLite> englishSynsetDataLiteList){
		List<SynsetDataLite> synsetDataLiteList = new ArrayList<SynsetDataLite>();
		for(EnglishSynsetDataLite englishSynsetDataLite: englishSynsetDataLiteList){
			synsetDataLiteList.add(englishSynsetDataLite.getSynsetDataLite());
		}
		return synsetDataLiteList;
	}
}
