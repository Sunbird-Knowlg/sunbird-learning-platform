package org.ekstep.tools.loader.service;

import java.net.URL;
import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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
public class ConceptServiceTest {
    
    private JsonObject concept = null;
    private Config config = null;
	private String user = null, authToken = null, clientId = null;
    private ExecutionContext context = null;
    
    public ConceptServiceTest() {
	}
    
    @Before
    public void loadConcept() throws Exception {
        config = ConfigFactory.parseResources("loader.conf");
        config = config.resolve();
        user = "bulk-loader-test";
		context = new ExecutionContext(config, user, authToken, clientId);
        
        URL resource = Resources.getResource("concept.json");
        String conceptData = Resources.toString(resource, Charset.defaultCharset());
        JsonParser parser = new JsonParser();
        concept = parser.parse(conceptData).getAsJsonObject();
    }

    @Test
    public void testCreate() throws Exception {
        
		ConceptServiceImpl service = new ConceptServiceImpl(context);
        service.create(concept, context);

        if (concept.get("identifier") != null) {
            JsonArray conceptIds = new JsonArray();
            conceptIds.add(concept.get("identifier"));
			service.retire(concept.get("identifier").getAsString(), context, concept.get("framework").getAsString());
        }
    }

	@Test
	public void testUpdate() throws Exception {

		ConceptServiceImpl service = new ConceptServiceImpl(context);
		service.create(concept, context);

		if (concept.get("identifier") != null) {
			String conceptId = concept.get("identifier").getAsString();
			String updateData = "{\"identifier\":\"" + conceptId + "\", \"framework\":\""
					+ concept.get("framework").getAsString() + "\", \"name\":\"ConceptUpdateTest\"}";
			JsonParser parser = new JsonParser();
			JsonObject updateConcept = parser.parse(updateData).getAsJsonObject();
			service.update(updateConcept, context);

			service.retire(conceptId, context, concept.get("framework").getAsString());

		}
	}
}
