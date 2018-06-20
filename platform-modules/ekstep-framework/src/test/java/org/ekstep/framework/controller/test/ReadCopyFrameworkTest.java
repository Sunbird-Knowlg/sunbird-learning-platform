/**
 * 
 */
package org.ekstep.framework.controller.test;

import java.util.HashMap;
import java.util.Map;

import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.framework.mgr.impl.ChannelManagerImpl;
import org.ekstep.framework.mgr.impl.FrameworkManagerImpl;
import org.ekstep.graph.engine.common.GraphEngineTestSetup;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.junit.After;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author pradyumna
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class ReadCopyFrameworkTest extends GraphEngineTestSetup {

	@Autowired
	private WebApplicationContext context;

	MockMvc mockMvc;

	private ResultActions actions;
	private static ObjectMapper mapper = new ObjectMapper();
	private static final String COMPOSITE_SEARCH_INDEX = "testfrcompositesearch";
	private static final String COMPOSITE_SEARCH_INDEX_TYPE = "cs";
	private static final String basePath = "/v3/framework";
	private static String frameworkId;
	private static String channelId;
	private static ChannelManagerImpl channelManager = new ChannelManagerImpl();
	private static FrameworkManagerImpl frameworkManager = new FrameworkManagerImpl();

	private static final String createFrameworkReq = "{\"name\": \"NCERT01\",\"description\": \"NCERT framework of Karnatka\",\"code\": \"test_term\"}";
	private static final String createChannelReq = "{\"name\":\"Karnatka\",\"description\":\"Channel for Karnatka\",\"code\":\"channelKA\"}";

	@BeforeClass
	public static void beforeTest() throws Exception {
		ElasticSearchUtil.initialiseESClient(COMPOSITE_SEARCH_INDEX,
				Platform.config.getString("search.es_conn_info"));
		loadDefinition("definitions/channel_definition.json", "definitions/framework_definition.json",
				"definitions/categoryInstance_definition.json");

	}

	@After
	public void after() throws Exception {
		System.out.println("deleting index: " + COMPOSITE_SEARCH_INDEX);
		ElasticSearchUtil.deleteIndex(COMPOSITE_SEARCH_INDEX);
	}

	@Before
	public void init() throws Exception {
		createTestIndex();
		Thread.sleep(300);
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}

	private static void createTestIndex() throws Exception {
		CompositeSearchConstants.COMPOSITE_SEARCH_INDEX = COMPOSITE_SEARCH_INDEX;
		System.out.println("creating index: " + COMPOSITE_SEARCH_INDEX);
		String settings = "{\"analysis\":{\"analyzer\":{\"cs_index_analyzer\":{\"type\":\"custom\",\"tokenizer\":\"standard\",\"filter\":[\"lowercase\",\"mynGram\"]},\"cs_search_analyzer\":{\"type\":\"custom\",\"tokenizer\":\"standard\",\"filter\":[\"standard\",\"lowercase\"]},\"keylower\":{\"tokenizer\":\"keyword\",\"filter\":\"lowercase\"}},\"filter\":{\"mynGram\":{\"type\":\"edge_ngram\",\"min_gram\":1,\"max_gram\":20,\"token_chars\":[\"letter\",\"digit\",\"whitespace\",\"punctuation\",\"symbol\"]}}}}";
		String mappings = "{\"dynamic_templates\":[{\"longs\":{\"match_mapping_type\":\"long\",\"mapping\":{\"type\":\"long\",\"fields\":{\"raw\":{\"type\":\"long\"}}}}},{\"booleans\":{\"match_mapping_type\":\"boolean\",\"mapping\":{\"type\":\"boolean\",\"fields\":{\"raw\":{\"type\":\"boolean\"}}}}},{\"doubles\":{\"match_mapping_type\":\"double\",\"mapping\":{\"type\":\"double\",\"fields\":{\"raw\":{\"type\":\"double\"}}}}},{\"dates\":{\"match_mapping_type\":\"date\",\"mapping\":{\"type\":\"date\",\"fields\":{\"raw\":{\"type\":\"date\"}}}}},{\"strings\":{\"match_mapping_type\":\"string\",\"mapping\":{\"type\":\"text\",\"copy_to\":\"all_fields\",\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\"}}}}}],\"properties\":{\"all_fields\":{\"type\":\"text\",\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\"}}}}}";
		ElasticSearchUtil.addIndex(COMPOSITE_SEARCH_INDEX, COMPOSITE_SEARCH_INDEX_TYPE, settings, mappings);
		populateTestDoc();
	}

	/**
	 * @throws Exception @throws
	 * 
	 */
	private static void populateTestDoc() throws Exception {
		createChannel();
		Map<String, Object> requestMap = mapper.readValue(createFrameworkReq, new TypeReference<Map<String, Object>>() {
		});
		requestMap.put("channel", channelId);
		Response resp = frameworkManager.createFramework(requestMap, channelId);
		frameworkId = (String) resp.getResult().get("node_id");
		Map<String, Object> indexDoc = new HashMap<String, Object>();
		indexDoc.put("fw_hierarchy",
				"{\"categories\":[{\"identifier\":\"test_term_medium\",\"code\":\"medium\",\"terms\":[{\"identifier\":\"test_term_medium_kannada\",\"code\":\"kannada\",\"name\":\"Kannada\",\"description\":\"Term for Kannada\",\"index\":1,\"category\":\"test_term_medium\",\"status\":\"Live\"},{\"identifier\":\"test_term_medium_english\",\"code\":\"English\",\"name\":\"English\",\"description\":\"Term for English\",\"index\":2,\"category\":\"test_term_medium\",\"status\":\"Live\"},{\"identifier\":\"test_term_medium_hindi\",\"code\":\"Hindi\",\"name\":\"Hindi\",\"description\":\"Term for Hindi\",\"index\":3,\"category\":\"test_term_medium\",\"status\":\"Live\"},{\"identifier\":\"test_term_medium_urdu\",\"code\":\"Urdu\",\"name\":\"Urdu\",\"description\":\"Term for Urdu\",\"index\":4,\"category\":\"test_term_medium\",\"status\":\"Live\"},{\"identifier\":\"test_term_medium_french\",\"code\":\"French\",\"name\":\"French\",\"description\":\"Term for French\",\"index\":5,\"category\":\"test_term_medium\",\"status\":\"Live\"}],\"name\":\"Medium\",\"description\":\"Description of Medium.\",\"index\":1,\"status\":\"Live\"}],\"status\":\"Live\"}");
		indexDoc.put("graph_id", "domain");
		indexDoc.put("node_id", 12345);
		indexDoc.put("identifier", "test_term");
		indexDoc.put("objectType", "Framework");
		indexDoc.put("nodeType", "DATA_NODE");
		ElasticSearchUtil.addDocumentWithId(COMPOSITE_SEARCH_INDEX, COMPOSITE_SEARCH_INDEX_TYPE, "test_term",
				mapper.writeValueAsString(indexDoc));
	}

	private static void createChannel() {
		try {
			Map<String, Object> requestMap = mapper.readValue(createChannelReq,
					new TypeReference<Map<String, Object>>() {
					});

			Response resp = channelManager.createChannel(requestMap);
			channelId = (String) resp.getResult().get("node_id");
		} catch (Exception e) {
			System.out.println("Exception Occured while creating Channel :" + e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public void mockTestFramework_01() throws Exception {
		String path = basePath + "/read/" + frameworkId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Assert.assertTrue(actions.andReturn().getResponse().getContentAsString().contains("test_term_medium_english"));
	}

}
