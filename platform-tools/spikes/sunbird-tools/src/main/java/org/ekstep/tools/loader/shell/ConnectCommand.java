/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.shell;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.File;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

/**
 *
 * @author feroz
 */
@Component
public class ConnectCommand implements CommandMarker {
    
    @CliCommand(value = "login", help = "Connect to the target environment")
    public String login(
            @CliOption(key = { "user" }, mandatory = true, help = "User name to login") final String user,
            @CliOption(key = { "password" }, mandatory = true, help = "Password to login") final String password,
            @CliOption(key = { "conf" }, mandatory = true, help = "Config file for environment") final File confFile
    ) {
        
        Config conf = ConfigFactory.parseFile(confFile);
        
        ShellContext context = ShellContext.getInstance();
        context.setCurrentConfig(conf);
        context.setCurrentUser(user);
        
        return "Connected to " + conf.getString("env") + " as " + user;
    }
    
    @CliCommand(value = "logout", help = "Disconnect from the target environment")
    public String logout() {
        
        
        ShellContext context = ShellContext.getInstance();
        Config conf = context.getCurrentConfig();
        
        if (conf == null) return "Not logged in.";
        String env = conf.getString("env");
        
        context.setCurrentConfig(null);
        context.setCurrentUser(null);
        return "Disconnected from " + env;
    }
}
