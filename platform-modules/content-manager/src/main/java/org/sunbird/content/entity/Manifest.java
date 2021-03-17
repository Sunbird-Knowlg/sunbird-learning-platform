package org.sunbird.content.entity;

import java.util.List;

public class Manifest extends ECRFObject {
	
	private List<Media> medias = null;

	/**
     * @return the medias
     */
	public List<Media> getMedias() {
		return medias;
	}

	/** 
     * @param medias the medias to set
     */
	public void setMedias(List<Media> medias) {
		this.medias = medias;
	}

}
