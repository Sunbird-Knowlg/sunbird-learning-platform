package org.ekstep.language.model;

import javax.persistence.Column;
import javax.persistence.Id;

public class SynsetDataLite{
	
	@Id
	private int synset_id;
	
	@Column(name = "synset", unique = false, nullable = false, length = 100000)
	private byte[] synset;
	
	public SynsetDataLite() {
		super();
	}
	
	public SynsetDataLite(int synset_id, byte[] synset) {
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
}
