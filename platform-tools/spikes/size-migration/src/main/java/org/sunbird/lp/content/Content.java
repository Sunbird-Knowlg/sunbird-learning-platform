package org.sunbird.lp.content;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Content {

	private String contentId;
	private String arifactUrl;
	private String downloadUrl;
	private double contentSize;

	public Content(String contentId, String artifactUrl) {
		this.contentId = contentId;
		this.arifactUrl = artifactUrl;
	}

	@Override
	public String toString() {		
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public JsonNode asJson() {
		return  new ObjectMapper().convertValue(this, JsonNode.class);
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public String getArifactUrl() {
		return arifactUrl;
	}

	public void setArifactUrl(String arifactUrl) {
		this.arifactUrl = arifactUrl;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public double getContentSize() {
		return contentSize;
	}

	public void setContentSize(double contentSize) {
		this.contentSize = contentSize;
	}

}
