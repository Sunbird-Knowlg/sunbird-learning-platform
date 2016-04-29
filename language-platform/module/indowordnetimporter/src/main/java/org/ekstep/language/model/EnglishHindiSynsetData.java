package org.ekstep.language.model;

import java.util.ArrayList;
import java.util.List;

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
@Table(name = "tbl_all_english_synset_data")
public class EnglishHindiSynsetData {

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
	@JoinTable (name = "tbl_noun_hypernymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = {
			@JoinColumn(name = "hypernymy_id") })
	protected List<EnglishSynsetDataLite> hypernyms = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_noun_hyponymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = {
			@JoinColumn(name = "hyponymy_id") })
	protected List<EnglishSynsetDataLite> hyponyms = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_meronymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = {
			@JoinColumn(name = "meronym_id") })
	protected List<EnglishSynsetDataLite> meronyms = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_holonymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = {
			@JoinColumn(name = "holonym_id") })
	protected List<EnglishSynsetDataLite> holonyms = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_antonymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = {
			@JoinColumn(name = "antonym_id") })
	protected List<EnglishSynsetDataLite> antonyms = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_action_object", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = {
			@JoinColumn(name = "object_id") })
	protected List<EnglishSynsetDataLite> actions = new ArrayList<>();

	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_action_object", joinColumns = { @JoinColumn(name = "object_id") }, inverseJoinColumns = {
			@JoinColumn(name = "synset_id") })
	protected List<EnglishSynsetDataLite> objects = new ArrayList<>();

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
	protected HindiSynsetDataLite hindiTranslation;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "synset_id", referencedColumnName = "synset_id", nullable = true)
	@Cascade(CascadeType.MERGE)
	protected KannadaSynsetDataLite kannadaTranslation;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "synset_id", referencedColumnName = "synset_id", nullable = true)
	@Cascade(CascadeType.MERGE)
	protected KonkaniSynsetDataLite konkaniTranslation;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "synset_id", referencedColumnName = "synset_id", nullable = true)
	@Cascade(CascadeType.MERGE)
	protected MalayalamSynsetDataLite malayalamTranslation;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "synset_id", referencedColumnName = "synset_id", nullable = true)
	@Cascade(CascadeType.MERGE)
	protected MarathiSynsetDataLite marathiTranslation;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "synset_id", referencedColumnName = "synset_id", nullable = true)
	@Cascade(CascadeType.MERGE)
	protected NepaliSynsetDataLite nepaliTranslation;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "synset_id", referencedColumnName = "synset_id", nullable = true)
	@Cascade(CascadeType.MERGE)
	protected OriyaSynsetDataLite oriyaTranslation;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "synset_id", referencedColumnName = "synset_id", nullable = true)
	@Cascade(CascadeType.MERGE)
	protected PunjabiSynsetDataLite punjabiTranslation;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "synset_id", referencedColumnName = "synset_id", nullable = true)
	@Cascade(CascadeType.MERGE)
	protected SanskritSynsetDataLite sanskritTranslation;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "synset_id", referencedColumnName = "synset_id", nullable = true)
	@Cascade(CascadeType.MERGE)
	protected TeluguSynsetDataLite teluguTranslation;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "english_hindi_id_mapping", joinColumns = @JoinColumn(name = "hindi_id") , inverseJoinColumns = @JoinColumn(name = "english_id") )
	@WhereJoinTable(clause = "type_link='Direct'")
	@Cascade(CascadeType.MERGE)
	private List<EnglishSynsetDataLite> englishTranslations;

	public EnglishHindiSynsetData() {
		super();
	}

	public MalayalamSynsetDataLite getMalayalamTranslation() {
		return malayalamTranslation;
	}

	public void setMalayalamTranslation(MalayalamSynsetDataLite malayalamTranslation) {
		this.malayalamTranslation = malayalamTranslation;
	}

	public MarathiSynsetDataLite getMarathiTranslation() {
		return marathiTranslation;
	}

	public void setMarathiTranslation(MarathiSynsetDataLite marathiTranslation) {
		this.marathiTranslation = marathiTranslation;
	}

	public NepaliSynsetDataLite getNepaliTranslation() {
		return nepaliTranslation;
	}

	public void setNepaliTranslation(NepaliSynsetDataLite nepaliTranslation) {
		this.nepaliTranslation = nepaliTranslation;
	}

	public OriyaSynsetDataLite getOriyaTranslation() {
		return oriyaTranslation;
	}

	public void setOriyaTranslation(OriyaSynsetDataLite oriyaTranslation) {
		this.oriyaTranslation = oriyaTranslation;
	}

	public PunjabiSynsetDataLite getPunjabiTranslation() {
		return punjabiTranslation;
	}

	public void setPunjabiTranslation(PunjabiSynsetDataLite punjabiTranslation) {
		this.punjabiTranslation = punjabiTranslation;
	}

	public SanskritSynsetDataLite getSanskritTranslation() {
		return sanskritTranslation;
	}

	public void setSanskritTranslation(SanskritSynsetDataLite sanskritTranslation) {
		this.sanskritTranslation = sanskritTranslation;
	}

	public TeluguSynsetDataLite getTeluguTranslation() {
		return teluguTranslation;
	}

	public void setTeluguTranslation(TeluguSynsetDataLite teluguTranslation) {
		this.teluguTranslation = teluguTranslation;
	}

	public KonkaniSynsetDataLite getKonkaniTranslation() {
		return konkaniTranslation;
	}

	public void setKonkaniTranslation(KonkaniSynsetDataLite konkaniTranslation) {
		this.konkaniTranslation = konkaniTranslation;
	}

	public HindiSynsetDataLite getHindiTranslation() {
		return hindiTranslation;
	}

	public void setHindiTranslation(HindiSynsetDataLite hindiTranslation) {
		this.hindiTranslation = hindiTranslation;
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

	public List<EnglishSynsetDataLite> getHypernyms() {
		return hypernyms;
	}

	public void setHypernyms(List<EnglishSynsetDataLite> hypernyms) {
		this.hypernyms = hypernyms;
	}

	public List<EnglishSynsetDataLite> getHyponyms() {
		return hyponyms;
	}

	public void setHyponyms(List<EnglishSynsetDataLite> hyponyms) {
		this.hyponyms = hyponyms;
	}

	public List<EnglishSynsetDataLite> getMeronyms() {
		return meronyms;
	}

	public void setMeronyms(List<EnglishSynsetDataLite> meronyms) {
		this.meronyms = meronyms;
	}

	public List<EnglishSynsetDataLite> getHolonyms() {
		return holonyms;
	}

	public void setHolonyms(List<EnglishSynsetDataLite> holonyms) {
		this.holonyms = holonyms;
	}

	public List<EnglishSynsetDataLite> getAntonyms() {
		return antonyms;
	}

	public void setAntonyms(List<EnglishSynsetDataLite> antonyms) {
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

	public List<EnglishSynsetDataLite> getActions() {
		return actions;
	}

	public void setActions(List<EnglishSynsetDataLite> actions) {
		this.actions = actions;
	}

	public List<EnglishSynsetDataLite> getObjects() {
		return objects;
	}

	public void setObjects(List<EnglishSynsetDataLite> objects) {
		this.objects = objects;
	}
}
