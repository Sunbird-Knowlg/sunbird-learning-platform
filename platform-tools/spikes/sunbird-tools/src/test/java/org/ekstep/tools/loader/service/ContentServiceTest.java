/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.service;

import com.google.common.io.Resources;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.net.URL;
import java.nio.charset.Charset;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author feroz
 */
public class ContentServiceTest {
    
    private JsonObject content = null;
    private Config config = null;
    private String user = null;
    private ExecutionContext context = null;
    
    public ContentServiceTest() {
    }
    
    @Before
    public void loadContent() throws Exception {
        config = ConfigFactory.parseResources("loader.conf");
        config = config.resolve();
        user = "bulk-loader-test";
        context = new ExecutionContext(config, user);
        
        URL resource = Resources.getResource("content.json");
        String contentData = Resources.toString(resource, Charset.defaultCharset());
        JsonParser parser = new JsonParser();
        content = parser.parse(contentData).getAsJsonObject();
    }

    @Test
    public void testCreate() throws Exception {
        
        ContentServiceImpl service = new ContentServiceImpl();
        service.init(context);
        service.create(content, context);

        if (content.get("content_id") != null) {
            JsonArray contentIds = new JsonArray();
            contentIds.add(content.get("content_id"));
            service.retire(contentIds, context);
        }
    }
}
