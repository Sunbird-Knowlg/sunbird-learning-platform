package org.ekstep.dialcode.model;

import java.io.Serializable;
import java.util.Map;

/**
 * Model Class for DIAL Code
 * 
 * @author gauraw
 *
 */
public class DialCode implements Serializable {

	private static final long serialVersionUID = 6038730711148121947L;

	private String identifier;
	private String channel;
	private String publisher;
	private String batchCode;
	private String status;
	private String generatedOn;
	private String publishedOn;
	private Map<String, Object> metadata;

	public DialCode() {

	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getBatchCode() {
		return batchCode;
	}

	public void setBatchCode(String batchCode) {
		this.batchCode = batchCode;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getGeneratedOn() {
		return generatedOn;
	}

	public void setGeneratedOn(String generatedOn) {
		this.generatedOn = generatedOn;
	}

	public String getPublishedOn() {
		return publishedOn;
	}

	public void setPublishedOn(String publishedOn) {
		this.publishedOn = publishedOn;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

}
