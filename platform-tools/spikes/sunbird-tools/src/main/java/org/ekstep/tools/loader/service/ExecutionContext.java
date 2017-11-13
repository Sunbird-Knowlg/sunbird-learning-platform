/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.service;

import com.typesafe.config.Config;

/**
 *
 * @author feroz
 */
public class ExecutionContext {
	private final Config currentConfig;
	private final String currentUser;
	private final String authToken;
	private final String clientId;
	private final String acessToken;

	public ExecutionContext(Config conf, String user, String authToken, String clientId) {
		this.currentConfig = conf;
		this.currentUser = user;
		this.authToken = authToken;
		this.clientId = clientId;
		this.acessToken = null;
	}

	/**
	 * @param currentConfig2
	 * @param currentUser2
	 * @param authToken2
	 * @param clientId2
	 * @param password
	 */
	public ExecutionContext(Config currentConfig, String currentUser, String authToken, String clientId,
			String password) {
		this.currentConfig = currentConfig;
		this.currentUser = currentUser;
		this.authToken = authToken;
		this.clientId = clientId;
		this.acessToken = password;
	}

	/**
	 * @return the currentConfig
	 */
	public Config getCurrentConfig() {
		return currentConfig;
	}

	/**
	 * @return the currentUser
	 */
	public String getCurrentUser() {
		return currentUser;
	}

	/**
	 * @return the authToken
	 */
	public String getAuthToken() {
		return authToken;
	}

	/**
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * @return the acessToken
	 */
	public String getAcessToken() {
		return acessToken;
	}

	/**
	 * Utility wrapper to get a string config from the context
	 */
	public String getString(String key) {
		return this.currentConfig.getString(key);
	}
}
