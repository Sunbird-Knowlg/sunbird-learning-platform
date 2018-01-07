/**
 * 
 */
package org.ekstep.dialcode.model;

import java.io.Serializable;
import java.util.Map;

/**
 * @author amitpriyadarshi
 *
 */
public class Publisher implements Serializable{

	private static final long serialVersionUID = 6038730711148121947L;

	private String identifier;
	private String name;
	private String channel;
	private String createdOn;
	private String updatedOn;
	
	public Publisher() {
		super();
	}

	
	/**
	 * @param identifier
	 * @param name
	 * @param channel
	 * @param createdOn
	 * @param updatedOn
	 */
	public Publisher(String identifier, String name, String channel, String createdOn, String updatedOn) {
		super();
		this.identifier = identifier;
		this.name = name;
		this.channel = channel;
		this.createdOn = createdOn;
		this.updatedOn = updatedOn;
	}


	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	public String getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(String updatedOn) {
		this.updatedOn = updatedOn;
	}
}
