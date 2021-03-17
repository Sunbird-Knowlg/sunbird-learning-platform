package org.sunbird.assessment.util;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

public class QuestionTemplateHandler {

    private static void initVelocityEngine(String templateName) {
        Properties p = new Properties();
        if(!templateName.startsWith("http") && !templateName.startsWith("/")){
            p.setProperty("resource.loader", "class");
            p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        }
        Velocity.init(p);
    }

    public static String handleHtmlTemplate(String templateName, Map<String, Object> context) {
        initVelocityEngine(templateName);
        VelocityContext veContext = new VelocityContext();
        context.entrySet().stream().forEach(entry -> veContext.put(entry.getKey(), entry.getValue()));
        StringWriter w = new StringWriter();
        Velocity.mergeTemplate(templateName, "UTF-8", veContext, w);
        return w.toString();
    }
}
