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
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.WhereJoinTable;

@Entity
@Table(name="tbl_all_tamil_synset_data")
public class TamilSynsetData implements LanguageSynsetData{
	
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
	@JoinTable(name = "tbl_noun_hypernymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = { @JoinColumn(name = "hypernymy_id") })
	protected List<TamilSynsetDataLite> hypernyms = new ArrayList<>();
	
	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_noun_hyponymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = { @JoinColumn(name = "hyponymy_id") })
	protected List<TamilSynsetDataLite> hyponyms = new ArrayList<>();
	
	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_meronymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = { @JoinColumn(name = "meronym_id") })
	protected List<TamilSynsetDataLite> meronyms = new ArrayList<>();
	
	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_holonymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = { @JoinColumn(name = "holonym_id") })
	protected List<TamilSynsetDataLite> holonyms = new ArrayList<>();
	
	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_antonymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = { @JoinColumn(name = "antonym_id") })
	protected List<TamilSynsetDataLite> antonyms = new ArrayList<>();
	
	
	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_action_object", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = { @JoinColumn(name = "object_id") })
	protected List<TamilSynsetDataLite> actions = new ArrayList<>();
	
	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_action_object", joinColumns = { @JoinColumn(name = "object_id") }, inverseJoinColumns = { @JoinColumn(name = "synset_id") })
	protected List<TamilSynsetDataLite> objects = new ArrayList<>();
	
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
	
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "synset_id", referencedColumnName = "synset_id", nullable = true)
	@Cascade(CascadeType.MERGE)
	protected KannadaSynsetDataLite kannadaTranslation;
	
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name="english_hindi_id_mapping", joinColumns = @JoinColumn(name="hindi_id"),	inverseJoinColumns = @JoinColumn(name="english_id"))
	@WhereJoinTable(clause = "type_link='Direct'")
	@Cascade(CascadeType.MERGE)
	private List<EnglishSynsetDataLite> englishTranslations;
	
	public TamilSynsetData() {
		super();
	}
	
	public TamilSynsetData(int synset_id, byte[] synset, byte[] gloss, String category,
			List<TamilSynsetDataLite> hypernyms, List<TamilSynsetDataLite> hyponyms, List<TamilSynsetDataLite> meronyms,
			List<TamilSynsetDataLite> holonyms, List<TamilSynsetDataLite> antonyms,
			List<TamilSynsetDataLite> actionObjects) {
		super();
		this.synset_id = synset_id;
		this.synset = synset;
		this.gloss = gloss;
		this.category = category;
		this.hypernyms = hypernyms;
		this.hyponyms = hyponyms;
		this.meronyms = meronyms;
		this.holonyms = holonyms;
		this.antonyms = antonyms;
		this.actions = actionObjects;
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

	public KannadaSynsetDataLite getKannadaTranslation() {
		return kannadaTranslation;
	}

	public void setKannadaTranslation(KannadaSynsetDataLite kannadaTranslation) {
		this.kannadaTranslation = kannadaTranslation;
	}

	public List<TamilSynsetDataLite> getHypernyms() {
		return hypernyms;
	}

	public void setHypernyms(List<TamilSynsetDataLite> hypernyms) {
		this.hypernyms = hypernyms;
	}

	public List<TamilSynsetDataLite> getHyponyms() {
		return hyponyms;
	}

	public void setHyponyms(List<TamilSynsetDataLite> hyponyms) {
		this.hyponyms = hyponyms;
	}

	public List<TamilSynsetDataLite> getMeronyms() {
		return meronyms;
	}

	public void setMeronyms(List<TamilSynsetDataLite> meronyms) {
		this.meronyms = meronyms;
	}

	public List<TamilSynsetDataLite> getHolonyms() {
		return holonyms;
	}

	public void setHolonyms(List<TamilSynsetDataLite> holonyms) {
		this.holonyms = holonyms;
	}

	public List<TamilSynsetDataLite> getAntonyms() {
		return antonyms;
	}

	public void setAntonyms(List<TamilSynsetDataLite> antonyms) {
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
	
	public List<EnglishSynsetDataLite> getEnglishTranslations() {
		return englishTranslations;
	}

	public void setEnglishTranslations(List<EnglishSynsetDataLite> englishTranslations) {
		this.englishTranslations = englishTranslations;
	}
	
	public List<TamilSynsetDataLite> getActions() {
		return actions;
	}

	public void setActions(List<TamilSynsetDataLite> actions) {
		this.actions = actions;
	}

	public List<TamilSynsetDataLite> getObjects() {
		return objects;
	}

	public void setObjects(List<TamilSynsetDataLite> objects) {
		this.objects = objects;
	}

	public SynsetData getSynsetData(){
		SynsetData synsetData = new SynsetData();
		synsetData.setSynset_id(this.synset_id);
		synsetData.setSynset(this.synset);
		synsetData.setGloss(this.gloss);
		synsetData.setCategory(this.category);
		
		//relations
		synsetData.setAntonyms(getSynsetDataLiteList(getAntonyms()));
		synsetData.setHolonyms(getSynsetDataLiteList(getHolonyms()));
		synsetData.setHypernyms(getSynsetDataLiteList(getHypernyms()));
		synsetData.setHyponyms(getSynsetDataLiteList(getHyponyms()));
		synsetData.setMeronyms(getSynsetDataLiteList(getMeronyms()));
		synsetData.setActionObjects(getSynsetDataLiteList(getObjects()));
		synsetData.setActions(getSynsetDataLiteList(getActions()));
		
		//translations
		Map<String, SynsetDataLite> translationsMap = new HashMap<String, SynsetDataLite>();
		if(getAssameseTranslation() != null)
		translationsMap.put("Assamese",getAssameseTranslation().getSynsetDataLite());
		if(getBengaliTranslation() != null)
		translationsMap.put("Bengali",getBengaliTranslation().getSynsetDataLite());
		if(getBodoTranslation() != null)
		translationsMap.put("Bodo",getBodoTranslation().getSynsetDataLite());
		if(getGujaratiTranslation() != null)
		translationsMap.put("Gujarati",getGujaratiTranslation().getSynsetDataLite());
		if(getKannadaTranslation() != null)
		translationsMap.put("Kannada",getKannadaTranslation().getSynsetDataLite());
		if(getEnglishTranslations() != null && !getEnglishTranslations().isEmpty())
		translationsMap.put("English",getEnglishSynsetDataLiteList(getEnglishTranslations()).get(0));
		synsetData.setTranslations(translationsMap);
		
		return synsetData;
	}
	
	private List<SynsetDataLite> getSynsetDataLiteList(List<TamilSynsetDataLite> tamilSynsetLiteList){
		List<SynsetDataLite> synsetDataLiteList = new ArrayList<SynsetDataLite>();
		for(TamilSynsetDataLite tamilSynsetDataLite: tamilSynsetLiteList){
			synsetDataLiteList.add(tamilSynsetDataLite.getSynsetDataLite());
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
