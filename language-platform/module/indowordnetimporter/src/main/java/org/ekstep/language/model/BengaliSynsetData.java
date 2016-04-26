package org.ekstep.language.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import javax.persistence.JoinColumn;

@Entity
@Table(name="tbl_all_bengali_synset_data")
public class BengaliSynsetData {
	
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
	protected List<BengaliSynsetDataLite> hypernyms = new ArrayList<>();
	
	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_noun_hyponymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = { @JoinColumn(name = "hyponymy_id") })
	protected List<BengaliSynsetDataLite> hyponyms = new ArrayList<>();
	
	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_meronymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = { @JoinColumn(name = "meronym_id") })
	protected List<BengaliSynsetDataLite> meronyms = new ArrayList<>();
	
	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_holonymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = { @JoinColumn(name = "holonym_id") })
	protected List<BengaliSynsetDataLite> holonyms = new ArrayList<>();
	
	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_antonymy", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = { @JoinColumn(name = "antonym_id") })
	protected List<BengaliSynsetDataLite> antonyms = new ArrayList<>();
	
	
	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(CascadeType.MERGE)
	@Fetch(FetchMode.SUBSELECT)
	@JoinTable(name = "tbl_action_object", joinColumns = { @JoinColumn(name = "synset_id") }, inverseJoinColumns = { @JoinColumn(name = "object_id") })
	protected List<BengaliSynsetDataLite> actionObjects = new ArrayList<>();
	
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "synset_id", referencedColumnName = "synset_id", nullable = true)
	@Cascade(CascadeType.MERGE)
	protected TamilSynsetDataLite tamilTranslation;
	
	public BengaliSynsetData() {
		super();
	}
	
	public BengaliSynsetData(int synset_id, byte[] synset, byte[] gloss, String category,
			List<BengaliSynsetDataLite> hypernyms, List<BengaliSynsetDataLite> hyponyms, List<BengaliSynsetDataLite> meronyms,
			List<BengaliSynsetDataLite> holonyms, List<BengaliSynsetDataLite> antonyms,
			List<BengaliSynsetDataLite> actionObjects) {
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
		this.actionObjects = actionObjects;
	}
	
	public List<BengaliSynsetDataLite> getActionObjects() {
		return actionObjects;
	}

	public void setActionObjects(List<BengaliSynsetDataLite> actionObjects) {
		this.actionObjects = actionObjects;
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

	public List<BengaliSynsetDataLite> getHyponyms() {
		return hyponyms;
	}

	public void setHyponyms(List<BengaliSynsetDataLite> hyponyms) {
		this.hyponyms = hyponyms;
	}

	public List<BengaliSynsetDataLite> getHypernyms() {
		return hypernyms;
	}

	public void setHypernyms(List<BengaliSynsetDataLite> hypernyms) {
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
}
