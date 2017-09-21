/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.shell;

import com.typesafe.config.Config;
import org.ekstep.tools.loader.service.ExecutionContext;

/**
 *
 * @author feroz
 */
public class ShellContext {
    private Config currentConfig;
    private String currentUser;
            
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
    
    public ExecutionContext getContext() {
        return new ExecutionContext(currentConfig, currentUser);
    }
}
