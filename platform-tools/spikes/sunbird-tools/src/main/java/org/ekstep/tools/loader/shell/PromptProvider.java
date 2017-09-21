package org.ekstep.tools.loader.shell;


import com.typesafe.config.Config;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.support.DefaultPromptProvider;
import org.springframework.stereotype.Component;

/**
 *
 *
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PromptProvider extends DefaultPromptProvider {

    @Override
    public String getPrompt() {
        String prompt = "loader > ";
                
        ShellContext context = ShellContext.getInstance();
        if (context.getCurrentConfig() != null) {
            Config conf = context.getCurrentConfig();
            String env = conf.getString("env");
            prompt = "loader@" + env + " > ";
        }
        
        return prompt;
    }

    @Override
    public String getProviderName() {
        return "Sunbird Tools";
    }

}
