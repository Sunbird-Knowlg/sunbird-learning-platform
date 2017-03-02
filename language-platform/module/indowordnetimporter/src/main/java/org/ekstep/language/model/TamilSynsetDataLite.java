package org.ekstep.language.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_all_tamil_synset_data")
public class TamilSynsetDataLite implements LanguageSynsetDataLite {

	@Id
	private int synset_id;

	@Column(name = "gloss", unique = false, nullable = false, length = 100000)
	private byte[] gloss;

	private String category;
	
	public TamilSynsetDataLite() {
		super();
	}

	public TamilSynsetDataLite(int synset_id, byte[] gloss, String category) {
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
		if (!(o instanceof TamilSynsetDataLite))
			return false;

		TamilSynsetDataLite other = (TamilSynsetDataLite) o;
		if (this.synset_id != other.synset_id)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return this.synset_id * 37;
	}
}
