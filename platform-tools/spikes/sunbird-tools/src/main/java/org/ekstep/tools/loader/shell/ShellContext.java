/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.shell;

import org.ekstep.tools.loader.service.ExecutionContext;

import com.typesafe.config.Config;

/**
 *
 * @author feroz
 */
public class ShellContext {
    private Config currentConfig;
    private String currentUser;
	private String authToken;
	private String clientId;
	private String password;
            
    private static ShellContext context;
    
    public static ShellContext getInstance() {
        if (context == null) context = new ShellContext();
        return context;
    }

    /**
     * @return the currentConfig
     */
    public Config getCurrentConfig() {
        return currentConfig;
    }

    /**
     * @param currentConfig the currentConfig to set
     */
    public void setCurrentConfig(Config currentConfig) {
        this.currentConfig = currentConfig;
    }

    /**
     * @return the currentUser
     */
    public String getCurrentUser() {
        return currentUser;
    }

    /**
     * @param currentUser the currentUser to set
     */
    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }
    
	/**
	 * @return the authToken
	 */
	public String getAuthToken() {
		return authToken;
	}

	/**
	 * @param authToken
	 *            the authToken to set
	 */
	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	/**
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * @param clientId
	 *            the clientId to set
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	public ExecutionContext getContext() {
		return new ExecutionContext(currentConfig, currentUser, authToken, clientId, password);
    }
}
