package org.ekstep.language.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_all_kannada_synset_data")
public class KannadaSynsetDataLite implements LanguageSynsetDataLite {

	@Id
	private int synset_id;

	@Column(name = "synset", unique = false, nullable = false, length = 100000)
	private byte[] synset;

	public KannadaSynsetDataLite() {
		super();
	}

	public KannadaSynsetDataLite(int synset_id, byte[] synset) {
		super();
		this.synset_id = synset_id;
		this.synset = synset;
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

	public SynsetDataLite getSynsetDataLite() {
		SynsetDataLite synsetDataLite = new SynsetDataLite();
		synsetDataLite.setSynset_id(this.synset_id);
		synsetDataLite.setSynset(this.synset);
		return synsetDataLite;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof KannadaSynsetDataLite))
			return false;

		KannadaSynsetDataLite other = (KannadaSynsetDataLite) o;
		if (this.synset_id != other.synset_id)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return this.synset_id * 37;
	}
}
