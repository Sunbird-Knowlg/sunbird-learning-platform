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

    public ExecutionContext(Config conf, String user) {
        this.currentConfig = conf;
        this.currentUser = user;
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
     * Utility wrapper to get a string config from the context
     */
    public String getString(String key) {
        return this.currentConfig.getString(key);
    }
}
