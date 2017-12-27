/**
 * 
 */
package org.platform.telemetry.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author mahesh
 *
 */

@JsonInclude(Include.NON_NULL)
public class Context {

	public Context(String channel, String env) {
		super();
		this.channel = channel;
		this.env = env;
	}
	
	String channel;
	Producer pdata;
	String env;
	/**
	 * @return the channel
	 */
	public String getChannel() {
		return channel;
	}
	/**
	 * @param channel the channel to set
	 */
	public void setChannel(String channel) {
		this.channel = channel;
	}
	/**
	 * @return the pdata
	 */
	public Producer getPdata() {
		return pdata;
	}
	/**
	 * @param pdata the pdata to set
	 */
	public void setPdata(Producer pdata) {
		this.pdata = pdata;
	}
	/**
	 * @return the env
	 */
	public String getEnv() {
		return env;
	}
	/**
	 * @param env the env to set
	 */
	public void setEnv(String env) {
		this.env = env;
	}
	
}
