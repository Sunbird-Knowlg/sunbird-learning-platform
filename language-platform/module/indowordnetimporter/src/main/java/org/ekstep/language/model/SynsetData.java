package org.ekstep.language.model;

import java.util.List;

public interface SynsetData {
	

	public List<SynsetDataLite> getActionObjects();
	
	public List<SynsetDataLite> getMeronyms();

	public List<SynsetDataLite> getHolonyms();

	public List<SynsetDataLite> getAntonyms();

	public List<SynsetDataLite> getHyponyms();

	public List<SynsetDataLite> getHypernyms();

	public int getSynsetId();
	
	public byte[] getSynset();
	
	public byte[] getGloss();
	
	public String getCategory();
}
