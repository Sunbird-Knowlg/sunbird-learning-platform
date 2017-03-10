package org.ekstep.language.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.WhereJoinTable;

@Entity
@Table(name = "tbl_all_hindi_synset_data")
public class EnglishHindiSynsetDataLite implements LanguageSynsetDataLite {

	@Id
	private int synset_id;

	@Column(name = "gloss", unique = false, nullable = false, length = 100000)
	private byte[] gloss;

	private String category;
	
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "english_hindi_id_mapping", joinColumns = @JoinColumn(name = "hindi_id") , inverseJoinColumns = @JoinColumn(name = "english_id") )
	@WhereJoinTable(clause = "type_link='Direct'")
	@Cascade(CascadeType.MERGE)
	private List<EnglishSynsetDataLite> englishMappings;

	public EnglishHindiSynsetDataLite() {
		super();
	}

	public EnglishHindiSynsetDataLite(int synset_id, byte[] gloss, String category) {
		super();
		this.synset_id = synset_id;
		this.gloss = gloss;
		this.category =category;
	}


	public int getSynset_id() {
		return synset_id;
	}

	public void setSynset_id(int synset_id) {
		this.synset_id = synset_id;
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

	public SynsetDataLite getSynsetDataLite() {
		SynsetDataLite synsetDataLite = new SynsetDataLite();
		synsetDataLite.setSynset_id(this.synset_id);
		synsetDataLite.setGloss(this.gloss);
		synsetDataLite.setCategory(this.category);
		return synsetDataLite;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof EnglishHindiSynsetDataLite))
			return false;

		EnglishHindiSynsetDataLite other = (EnglishHindiSynsetDataLite) o;
		if (this.synset_id != other.synset_id)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return this.synset_id * 37;
	}

	public List<EnglishSynsetDataLite> getEnglishMappings() {
		return englishMappings;
	}

	public void setEnglishMappings(List<EnglishSynsetDataLite> englishMappings) {
		this.englishMappings = englishMappings;
	}
}
