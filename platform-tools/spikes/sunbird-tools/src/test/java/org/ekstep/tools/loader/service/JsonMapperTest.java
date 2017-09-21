/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.service;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.jtwig.environment.EnvironmentConfiguration;
import org.jtwig.environment.EnvironmentConfigurationBuilder;
import org.jtwig.functions.FunctionRequest;
import org.jtwig.functions.SimpleJtwigFunction;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author feroz
 */
public class JsonMapperTest {
    
    public JsonMapperTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
//    
//    @Test
//    public void testTwigTemplate() {
//        JtwigTemplate template = JtwigTemplate.classpathTemplate("content.twig");
//        JtwigModel model = JtwigModel.newModel();
//        
//        model.with("name", "Haathi Bhaalu");
//        model.with("type", "ecml");
//        
//        template.render(model, System.out);
//    }
    
    @Ignore
    @Test
    public void testTwigFunction() {
        
        JtwigModel model = JtwigModel.newModel();
        
        model.with("name", "Haathi Bhaalu");
        model.with("type", "ecml");
        model.with("lang", "hi");
        
        LoadFunction loader = new LoadFunction();
        loader.setModel(model);
        
        EnvironmentConfiguration configuration = EnvironmentConfigurationBuilder.configuration().functions().add(loader).and().build();        
        JtwigTemplate template = JtwigTemplate.classpathTemplate("content_fn.twig", configuration);
        template.render(model, System.out);
    }
//    
//    @Test
//    public void testFreemarkerTemplate() throws Exception {
//        Configuration cfg = new Configuration(new Version("2.3.23"));
//
//        cfg.setClassForTemplateLoading(JsonMapperTest.class, "/");
//        cfg.setDefaultEncoding("UTF-8");
//
//        Template template = cfg.getTemplate("content.ftl");
//
//        Map<String, Object> templateData = new HashMap<>();
//        templateData.put("name", "Haathi Bhaalu");
//        templateData.put("type", "ecml");
//
//        try (StringWriter out = new StringWriter()) {
//            
//            template.process(templateData, out);
//            System.out.println(out.getBuffer().toString());
//
//            out.flush();
//        }
//    }
    
    // @Ignore
    @Test
    public void testConfig() {
        JtwigModel model = JtwigModel.newModel();
        
        model.with("name", "Haathi Bhaalu");
        model.with("type", "ecml");
        model.with("lang", "hi");
        model.with("grades", "class 1; class 2; class 3");
        model.with("child", "{something: else}");
        model.with("concept1", "placevalue");
        model.with("concept2", "counting");
        
        LoadFunction loader = new LoadFunction();
        loader.setModel(model);
        
        EnvironmentConfiguration configuration = EnvironmentConfigurationBuilder.configuration().functions().add(loader).and().build();        
        JtwigTemplate template = JtwigTemplate.classpathTemplate("content_fn.twig", configuration);
        String configStr = template.render(model);
        
        ConfigRenderOptions options = ConfigRenderOptions.concise().setFormatted(true).setJson(true);
//        options.setComments(false);
//        options.setJson(true);
//        options.setFormatted(true);
//        options.setOriginComments(false);
                
        Config conf = ConfigFactory.parseString(configStr);
        System.out.println("**--------");
        System.out.println(conf.root().render(options));
    }
    
    @Ignore
    @Test
    public void testConfigExt() {
        Properties data = new Properties();
        data.setProperty("csv.name", "haathi");
        data.setProperty("csv.type", "ecml");
        data.setProperty("csv.lang", "hi");
        
        ConfigRenderOptions options = ConfigRenderOptions.concise().setFormatted(true).setJson(true);
        Config baseConfig = ConfigFactory.parseProperties(data);
        Config childConfig = ConfigFactory.parseResources("content");
        Config merged = childConfig.withFallback(baseConfig);
        System.out.println(merged.root().render(options));
        
    }
    
    private static class LoadFunction extends SimpleJtwigFunction {
        
            private JtwigModel model = null;
            
            public void setModel(JtwigModel oModel) {
                model = oModel;
            }
            
            @Override
            public String name() {
                return "load";
            }

            @Override
            public Object execute(FunctionRequest functionRequest) {
                String key = (String) functionRequest.get(0);
                String file = (String) functionRequest.get(1);
                String type = (String) functionRequest.get(2);
                
                Map<String, String> data = new HashMap<>();
                data.put("en", "1english");
                data.put("hi", "1hindi");
                
                model.with(key, data);
                
                return "";
            }
            
            // Load Mapping - load a properties file into data model
            // Load CSV - load CSV data into the model
            // Load JSON - load JSON data into the model 
            // Sanitize - given a string, remove leading, trailing spaces, internal repeated spaces, internal repeated chars
            // Sluggify
            // URL
            // Generate ID? 
            // Split a given string field into array of strings? 
            // JSON escape
            // JSON unescape
        
    }
}
