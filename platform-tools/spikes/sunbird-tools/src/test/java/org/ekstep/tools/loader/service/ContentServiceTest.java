/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.service;

import java.net.URL;
import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Resources;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 *
 * @author feroz
 */
public class ContentServiceTest {
    
    private JsonObject content = null;
    private Config config = null;
	private String user = null, authToken = null, clientId = null;
    private ExecutionContext context = null;
    
    public ContentServiceTest() {
    }
    
    @Before
    public void loadContent() throws Exception {
        config = ConfigFactory.parseResources("loader.conf");
        config = config.resolve();
		user = "pradTest";
		authToken = "363e69d1-81be-3a64-8f35-e3c9b7f441c6";
		clientId = "0123709530078822407";
		context = new ExecutionContext(config, user, authToken, clientId);
        
        URL resource = Resources.getResource("content.json");
        String contentData = Resources.toString(resource, Charset.defaultCharset());
        JsonParser parser = new JsonParser();
        content = parser.parse(contentData).getAsJsonObject();
    }

    @Test
    public void testCreate() throws Exception {
        
		ContentService service = new ContentServiceImpl(context);
        service.create(content, context);

        if (content.get("content_id") != null) {
            JsonArray contentIds = new JsonArray();
            contentIds.add(content.get("content_id"));
            service.retire(contentIds, context);
        }
    }
}
