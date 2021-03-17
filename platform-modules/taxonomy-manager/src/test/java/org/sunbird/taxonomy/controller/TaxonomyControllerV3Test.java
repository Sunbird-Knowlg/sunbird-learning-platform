/**
 * 
 */
package org.sunbird.taxonomy.controller;

import java.io.IOException;

import org.sunbird.graph.engine.common.GraphEngineTestSetup;
import org.sunbird.taxonomy.mgr.ITaxonomyManager;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author gauraw
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class TaxonomyControllerV3Test extends GraphEngineTestSetup {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private ITaxonomyManager taxonomyManager;

	private ResultActions actions;

	MockMvc mockMvc;

	private static ObjectMapper mapper = new ObjectMapper();

	private static String GRAPH_ID = "domain";
	private static Boolean isDefSaved = false;

	@BeforeClass
	public static void setup() throws Exception {
		loadDefinition("definitions/content_definition.json", "definitions/concept_definition.json",
				"definitions/dimension_definition.json", "definitions/domain_definition.json",
				"definitions/framework_definition.json");
	}

	@AfterClass
	public static void clean() throws IOException {

	}

	@Before
	public void init() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
		if (!isDefSaved)
			saveDefinition();
	}

	private void saveDefinition() throws Exception {
		String path = "/taxonomy/" + GRAPH_ID + "/definition";
		String createReq = "{\"definitionNodes\": [{\"objectType\": \"Framework\",\"properties\": [{\"propertyName\": \"name\",\"title\": \"Name\",\"description\": \"Name of the framework\",\"category\": \"General\",\"dataType\": \"Text\",\"required\": true,\"displayProperty\": \"Editable\",\"defaultValue\": \"\",\"renderingHints\": \"{'inputType': 'text', 'order': 1}\",\"indexed\": true},{\"propertyName\": \"description\",\"title\": \"Description\",\"description\": \"Description of the framework\",\"category\": \"General\",\"dataType\": \"Text\",\"required\": false,\"displayProperty\": \"Editable\",\"defaultValue\": \"\",\"renderingHints\": \"{'inputType': 'text', 'order': 2}\",\"indexed\": false},{\"propertyName\": \"code\",\"title\": \"Code\",\"description\": \"Unique code for the framework\",\"category\": \"General\",\"dataType\": \"Text\",\"required\": true,\"displayProperty\": \"Editable\",\"defaultValue\": \"\",\"renderingHints\": \"{'inputType': 'text', 'order': 3}\",\"indexed\": false},{\"propertyName\": \"type\",\"title\": \"Type\",\"description\": \"Type for the framework\",\"category\": \"General\",\"dataType\": \"Select\",\"range\": [\"K-12\",\"TPD\"],\"required\": true,\"displayProperty\": \"Editable\",\"defaultValue\": \"K-12\",\"renderingHints\": \"{'inputType': 'text', 'order': 3}\",\"indexed\": false},{\"propertyName\": \"status\",\"title\": \"Status\",\"description\": \"Status of the framework\",\"category\": \"Lifecycle\",\"dataType\": \"Select\",\"range\":[\"Live\",\"Retired\"],\"required\": false,\"indexed\": true,\"displayProperty\": \"Editable\",\"defaultValue\": \"Live\",\"renderingHints\": \"{'inputType': 'select', 'order': 5}\"},{\"propertyName\": \"createdBy\",\"title\": \"Created By\",\"description\": \"\",\"category\": \"audit\",\"dataType\": \"Text\",\"required\": false,\"indexed\": false,\"displayProperty\": \"Readonly\",\"defaultValue\": \"\",\"renderingHints\": \"{ 'inputType': 'text',  'order': 6}\"},{\"propertyName\": \"createdOn\",\"title\": \"Created On\",\"description\": \"\",\"category\": \"audit\",\"dataType\": \"Date\",\"range\": [],\"required\": false,\"indexed\": false,\"displayProperty\": \"Readonly\",\"defaultValue\": \"\",\"renderingHints\": \"{ 'order': 7}\"},  {\"propertyName\": \"lastUpdatedBy\",\"title\": \"Last Updated By\",\"description\": \"\",\"category\": \"audit\",\"dataType\": \"Text\",\"range\": [],\"required\": false,\"indexed\": false,\"displayProperty\": \"Editable\",\"defaultValue\": \"\",\"renderingHints\": \"{ 'inputType': 'text',  'order': 8}\"},{\"propertyName\": \"lastUpdatedOn\",\"title\": \"Last Updated On\",\"description\": \"\",\"category\": \"audit\",\"dataType\": \"Date\",\"range\": [],\"required\": false,\"indexed\": false,\"displayProperty\": \"Readonly\",\"defaultValue\": \"\",\"renderingHints\": \"{ 'order': 13 }\"}],\"inRelations\":[{\"relationName\": \"hasSequenceMember\",\"objectTypes\": [\"Channel\"],\"title\": \"channels\",\"description\": \"Channels of the framework\",\"required\": false}],\"outRelations\":[{\"relationName\": \"hasSequenceMember\",\"objectTypes\": [\"CategoryInstance\"],\"title\": \"categories\",\"description\": \"Categories of framework\",\"required\": false	}],\"systemTags\": [],\"metadata\": {\"ttl\": 24,\"limit\": 50}}]}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(createReq));
		isDefSaved = true;
	}

	/*
	 * Read Definition with Valid URI & Valid Grpah ID. Expect : 200 - OK
	 */
	@Test
	public void testSuggestions_01() throws Exception {
		String path = "/v3/definitions/list?graph_id=domain";
		actions = mockMvc.perform(
				MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON).header("user-id", "ilimi"));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Update Definition with Valid URI & Valid Grpah ID. Expect : 200 - OK
	 */
	@Test
	public void testSuggestions_02() throws Exception {
		String path = "/v3/definitions/update/DEFINITION_NODE_Framework";
		String updateReq = "{\"definitionNodes\": [{\"objectType\": \"Framework\",\"properties\": [{\"propertyName\": \"name\",\"title\": \"Name\",\"description\": \"Name of the framework\",\"category\": \"General\",\"dataType\": \"Text\",\"required\": true,\"displayProperty\": \"Editable\",\"defaultValue\": \"\",\"renderingHints\": \"{'inputType': 'text', 'order': 1}\",\"indexed\": true},{\"propertyName\": \"description\",\"title\": \"Description\",\"description\": \"Description of the framework\",\"category\": \"General\",\"dataType\": \"Text\",\"required\": false,\"displayProperty\": \"Editable\",\"defaultValue\": \"\",\"renderingHints\": \"{'inputType': 'text', 'order': 2}\",\"indexed\": false},{\"propertyName\": \"code\",\"title\": \"Code\",\"description\": \"Unique code for the framework\",\"category\": \"General\",\"dataType\": \"Text\",\"required\": true,\"displayProperty\": \"Editable\",\"defaultValue\": \"\",\"renderingHints\": \"{'inputType': 'text', 'order': 3}\",\"indexed\": false},{\"propertyName\": \"type\",\"title\": \"Type\",\"description\": \"Type for the framework\",\"category\": \"General\",\"dataType\": \"Select\",\"range\": [\"K-12\",\"TPD\"],\"required\": true,\"displayProperty\": \"Editable\",\"defaultValue\": \"K-12\",\"renderingHints\": \"{'inputType': 'text', 'order': 3}\",\"indexed\": false},{\"propertyName\": \"status\",\"title\": \"Status\",\"description\": \"Status of the framework\",\"category\": \"Lifecycle\",\"dataType\": \"Select\",\"range\":[\"Live\",\"Retired\"],\"required\": false,\"indexed\": true,\"displayProperty\": \"Editable\",\"defaultValue\": \"Live\",\"renderingHints\": \"{'inputType': 'select', 'order': 5}\"},{\"propertyName\": \"createdBy\",\"title\": \"Created By\",\"description\": \"\",\"category\": \"audit\",\"dataType\": \"Text\",\"required\": false,\"indexed\": false,\"displayProperty\": \"Readonly\",\"defaultValue\": \"\",\"renderingHints\": \"{ 'inputType': 'text',  'order': 6}\"},{\"propertyName\": \"createdOn\",\"title\": \"Created On\",\"description\": \"\",\"category\": \"audit\",\"dataType\": \"Date\",\"range\": [],\"required\": false,\"indexed\": false,\"displayProperty\": \"Readonly\",\"defaultValue\": \"\",\"renderingHints\": \"{ 'order': 7}\"},  {\"propertyName\": \"lastUpdatedBy\",\"title\": \"Last Updated By\",\"description\": \"\",\"category\": \"audit\",\"dataType\": \"Text\",\"range\": [],\"required\": false,\"indexed\": false,\"displayProperty\": \"Editable\",\"defaultValue\": \"\",\"renderingHints\": \"{ 'inputType': 'text',  'order': 8}\"},{\"propertyName\": \"lastUpdatedOn\",\"title\": \"Last Updated On\",\"description\": \"\",\"category\": \"audit\",\"dataType\": \"Date\",\"range\": [],\"required\": false,\"indexed\": false,\"displayProperty\": \"Readonly\",\"defaultValue\": \"\",\"renderingHints\": \"{ 'order': 13 }\"}],\"inRelations\":[{\"relationName\": \"hasSequenceMember\",\"objectTypes\": [\"Channel\"],\"title\": \"channels\",\"description\": \"Channels of the framework\",\"required\": false}],\"outRelations\":[{\"relationName\": \"hasSequenceMember\",\"objectTypes\": [\"CategoryInstance\"],\"title\": \"categories\",\"description\": \"Categories of framework\",\"required\": false	}],\"systemTags\": [],\"metadata\": {\"ttl\": 24,\"limit\": 50}}]}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("user-id", "ilimi").content(updateReq));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

}
