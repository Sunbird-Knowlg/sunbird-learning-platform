package org.ekstep.framework.controller.test;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.framework.mgr.impl.CategoryInstanceManagerImpl;
import org.ekstep.framework.mgr.impl.CategoryManagerImpl;
import org.ekstep.framework.mgr.impl.ChannelManagerImpl;
import org.ekstep.framework.mgr.impl.FrameworkManagerImpl;
import org.ekstep.graph.engine.common.GraphEngineTestSetup;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Mock Test Cases for Framework API
 * 
 * @author gauraw
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class FrameworkV3ControllerTest extends GraphEngineTestSetup {

	@Autowired
	private WebApplicationContext context;

	MockMvc mockMvc;

	private ResultActions actions;
	private static final String BASE_PATH = "/framework/v3";
	private static ObjectMapper mapper = new ObjectMapper();
	private static String frameworkId;
	private static String frameworkIdforCopyAPI;
	private static String channelId;
	private static ChannelManagerImpl channelManager = new ChannelManagerImpl();
	private static FrameworkManagerImpl frameworkManager = new FrameworkManagerImpl();
	private static CategoryInstanceManagerImpl categoryInstanceManager = new CategoryInstanceManagerImpl();
	private static CategoryManagerImpl categoryManager = new CategoryManagerImpl();
	private static final String TEST_INDEX_NAME="testcompositesearch";

	private static final String createFrameworkReq = "{\"name\": \"NCERT01\",\"description\": \"NCERT framework of Karnatka\",\"code\": \"ka_ncert\"}";

	private static final String createChannelReq = "{\"name\":\"Karnatka\",\"description\":\"Channel for Karnatka\",\"code\":\"channelKA\"}";

	private static final String createCategoryReq = "{ \"name\":\"Class\", \"description\":\"\", \"code\":\"class\" }";

	private static final String createFrameworkValidJson = "{\"id\":\"ekstep.framework.create\",\"ver\": \"3.0\",\"ts\": \"YYYY-MM-DDThh:mm:ssZ+/-nn.nn\",\"params\": {\"did\": \"1234\",\"key\": \"1234\",\"msgid\": \"test1234\"},\"request\": {\"framework\": {\"name\": \"NCERT01\",\"description\": \"NCERT framework of Karnatka\",\"code\": \"org.ekstep.framework.create\"}}}";

	private static final String createFrameworkInvalidJson = "{\"id\":\"ekstep.framework.create\",\"ver\": \"3.0\",\"ts\": \"YYYY-MM-DDThh:mm:ssZ+/-nn.nn\",\"params\": {\"did\": \"1234\",\"key\": \"1234\",\"msgid\": \"test1234\"},\"request\": {\"frameworks\": {\"name\": \"NCERT01\",\"description\": \"NCERT framework of Karnatka\",\"code\": \"org.ekstep.framework.create\"}}}";

	private static String updateFrameworkValidJson = "{\"id\": \"ekstep.framework.update\",\"ver\": \"3.0\",\"ts\": \"YYYY-MM-DDThh:mm:ssZ+/-nn.nn\",\"params\": {\"did\": \"1234\",\"key\": \"1234\",\"msgid\": \"test1234\"},\"request\": {\"framework\": {\"versionKey\": \"1511787372693\",\"description\": \" framework description\",\"categories\": [{\"identifier\": \"do_11238579307347148811\",\"name\": \"cat3\"}]}}}";

	private static final String updateFrameworkInvalidCIJson = "{\"id\": \"ekstep.framework.update\",\"ver\": \"3.0\",\"ts\": \"YYYY-MM-DDThh:mm:ssZ+/-nn.nn\",\"params\": {\"did\": \"1234\",\"key\": \"1234\",\"msgid\": \"test1234\"},\"request\": {\"framework\": {\"versionKey\": \"1511787372693\",\"description\": \" framework description\",\"categories\": [{\"identifier\": \"do_112385793073471488\",\"name\": \"cat3\"}]}}}";

	private static final String listFrameworkValidJson = "{\"id\": \"ekstep.framework.list\",\"ver\": \"3.0\",\"ts\": \"YYYY-MM-DDThh:mm:ssZ+/-nn.nn\",\"params\": {\"did\": \"1234\",\"key\": \"1234\",\"msgid\": \"test1234\"},\"request\": { \"search\": {\"status\":\"\"}}}";

	private static final String listFrameworkInvalidJson = "{\"id\": \"ekstep.framework.list\",\"ver\": \"3.0\",\"ts\": \"YYYY-MM-DDThh:mm:ssZ+/-nn.nn\",\"params\": {\"did\": \"1234\",\"key\": \"1234\",\"msgid\": \"test1234\"},\"request\": {}}";

	@BeforeClass
	public static void setup() throws Exception {
		loadDefinition("definitions/channel_definition.json", "definitions/framework_definition.json",
				"definitions/category_definition.json", "definitions/categoryInstance_definition.json");
		LearningRequestRouterPool.init();
		createCompositeSearchIndex();
		createChannel();
		createFramework();
	}

	@Before
	public void init() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}
	
	@AfterClass
	public static void finish() throws Exception {
		ElasticSearchUtil.deleteIndex(TEST_INDEX_NAME);
	}
	
	private void delay(long time) {
		try {
			Thread.sleep(time);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void createFramework() {
		try {
			for (int i = 1; i <= 2; i++) {

				if (i == 1) {
					Map<String, Object> requestMap = mapper.readValue(createFrameworkReq,
							new TypeReference<Map<String, Object>>() {
							});
					requestMap.put("channel", channelId);
					Response resp = frameworkManager.createFramework(requestMap, channelId);
					frameworkId = (String) resp.getResult().get("node_id");
				} else if (i == 2) {
					String createFrameworkReq = "{\"name\": \"NCERT\",\"description\": \"NCERT framework of Bihar\",\"code\": \"br_ncert\"}";
					Map<String, Object> requestMap = mapper.readValue(createFrameworkReq,
							new TypeReference<Map<String, Object>>() {
							});
					requestMap.put("channel", channelId);
					Response resp = frameworkManager.createFramework(requestMap, channelId);
					frameworkIdforCopyAPI = (String) resp.getResult().get("node_id");
					Map<String, Object> categoryInstanceRequestMap = mapper.readValue(createCategoryReq,
							new TypeReference<Map<String, Object>>() {
							});
					categoryManager.createCategory(categoryInstanceRequestMap);
					categoryInstanceManager.createCategoryInstance(frameworkIdforCopyAPI, categoryInstanceRequestMap);
				}
			}

		} catch (Exception e) {
			System.out.println("Exception Occured while creating Framework :" + e.getMessage());
			e.printStackTrace();
		}

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
	
	private static void createCompositeSearchIndex() throws Exception {
		CompositeSearchConstants.COMPOSITE_SEARCH_INDEX=TEST_INDEX_NAME;
		ElasticSearchUtil.initialiseESClient(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, Platform.config.getString("search.es_conn_info"));
		String settings = "{\"analysis\":{\"analyzer\":{\"cs_index_analyzer\":{\"type\":\"custom\",\"tokenizer\":\"standard\",\"filter\":[\"lowercase\",\"mynGram\"]},\"cs_search_analyzer\":{\"type\":\"custom\",\"tokenizer\":\"standard\",\"filter\":[\"standard\",\"lowercase\"]},\"keylower\":{\"tokenizer\":\"keyword\",\"filter\":\"lowercase\"}},\"filter\":{\"mynGram\":{\"type\":\"nGram\",\"min_gram\":1,\"max_gram\":20,\"token_chars\":[\"letter\",\"digit\",\"whitespace\",\"punctuation\",\"symbol\"]}}}}";
		String mappings = "{\"dynamic_templates\":[{\"nested\":{\"match_mapping_type\":\"object\",\"mapping\":{\"type\":\"nested\",\"fields\":{\"type\":\"nested\"}}}},{\"longs\":{\"match_mapping_type\":\"long\",\"mapping\":{\"type\":\"long\",\"fields\":{\"raw\":{\"type\":\"long\"}}}}},{\"booleans\":{\"match_mapping_type\":\"boolean\",\"mapping\":{\"type\":\"boolean\",\"fields\":{\"raw\":{\"type\":\"boolean\"}}}}},{\"doubles\":{\"match_mapping_type\":\"double\",\"mapping\":{\"type\":\"double\",\"fields\":{\"raw\":{\"type\":\"double\"}}}}},{\"dates\":{\"match_mapping_type\":\"date\",\"mapping\":{\"type\":\"date\",\"fields\":{\"raw\":{\"type\":\"date\"}}}}},{\"strings\":{\"match_mapping_type\":\"string\",\"mapping\":{\"type\":\"text\",\"copy_to\":\"all_fields\",\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}}}}}],\"properties\":{\"all_fields\":{\"type\":\"text\",\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\"}}}}}";
		ElasticSearchUtil.addIndex(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, settings, mappings);
	}
	
	/*
	 * Scenario 1 : Create Framework with valid url and valid request body.
	 * 
	 * Given: Valid url and valid request body.
	 * When: Framework create API hits.
	 * Then: 200 - OK, 1 framework with no relationship got created.
	 * 
	 */
	@Test
	public void mockTestFramework_01() throws Exception {
		String path = BASE_PATH + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelKA")
				.content(createFrameworkValidJson));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 2 : create Framework with invalid url and valid request body.
	 * 
	 * Given: Invalid url and valid request body.
	 * When: Framework create API
	 * hits. Then: 404 - Invalid request path
	 * 
	 */
	@Test
	public void mockTestFramework_02() throws Exception {
		String path = BASE_PATH + "/creat"; // Invalid url
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.content(createFrameworkValidJson));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 3 : Create Framework with valid url and invalid request body.
	 * 
	 * Given: Valid url and invalid request body 
	 * When: Framework create API
	 * hits. Then: 400 - Bad Request with error Message: Invalid Request
	 * 
	 */
	@Test
	public void mockTestFramework_03() throws Exception {
		String path = BASE_PATH + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.content(createFrameworkInvalidJson));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 4 : Read Framework with valid url and valid framework
	 * identifier.
	 * 
	 * Given: Valid url and valid framework identifier 
	 * When: Framework read API hits. 
	 * Then: 200 - OK, Framework details with given identifier returns.
	 * 
	 */
	
	@Test
	@SuppressWarnings("unchecked")
	public void readFrameworkWithValidIdentifierExpect200() throws Exception {
		//Create Framework
		String createFrameworkReq = "{\"request\":{\"framework\":{\"name\": \"TESTFR01\",\"description\": \"Test Framework\",\"code\": \"test_fr\"}}}";
		String path = BASE_PATH + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelKA")
				.content(createFrameworkReq));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		//Publish Framework
		path = BASE_PATH + "/publish/" + "test_fr";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("X-Channel-Id", "channelKA").contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		delay(10000);
		//Read Framework
		path = BASE_PATH + "/read/" + "test_fr";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON));
		Response resp=getResponse(actions);
		Map<String,Object> framework=(Map<String, Object>) resp.getResult().get("framework");
		String name=(String) framework.get("name");
		String code=(String) framework.get("code");
		String desc=(String) framework.get("description");
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Assert.assertEquals("TESTFR01", name);
		Assert.assertEquals("test_fr", code);
		Assert.assertEquals("Test Framework", desc);
	}

	/*
	 * Scenario 5 : Read Framework with invalid url and valid framework identifier.
	 * 
	 * Given: Invalid url and valid framework identifier 
	 * When: Framework Read API hits. 
	 * Then: 404- Invalid request path
	 * 
	 */
	@Test
	public void mockTestFramework_05() throws Exception {
		String path = BASE_PATH + "/rea/" + frameworkId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 6 : Read Framework with valid url and invalid framework identifier.
	 * 
	 * Given: Valid url and invalid framework identifier 
	 * When: Framework read API hits. 
	 * Then: 404 - Resource Not Found with error Message: Framework not found with id: framework Id
	 * 
	 */
	@Test
	public void mockTestFramework_06() throws Exception {
		String frameworkId = "ttttt1234"; // Invalid Framework Id
		String path = BASE_PATH + "/read/" + frameworkId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 7 : update Framework with valid url, valid request body and
	 * valid framework identifier.
	 * 
	 * Given: Valid url, Valid request body and Valid framework identifier 
	 * When: Framework update API hits. 
	 * Then: 200 - OK, Framework partial update done.
	 * 
	 */
	
	@Test
	public void mockTestFramework_07() throws Exception {
		String path = BASE_PATH + "/update/" + frameworkId;
		String updateFrameworkValidJson = "{\"id\": \"ekstep.framework.update\",\"ver\": \"3.0\",\"ts\": \"YYYY-MM-DDThh:mm:ssZ+/-nn.nn\",\"params\": {\"did\": \"1234\",\"key\": \"1234\",\"msgid\": \"test1234\"},\"request\": {\"framework\": {\"versionKey\": \"1511787372693\",\"description\": \" Karnatka NCERT Framework for Std 1 to 10\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelKA").content(updateFrameworkValidJson));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 8 : update Framework with Invalid url, valid request body and
	 * valid framework identifier.
	 * 
	 * Given: Valid url, Valid request body and Valid framework identifier 
	 * When:Framework update API hits. 
	 * Then: 404 , Invalid Request.
	 * 
	 */
	@Test
	public void mockTestFramework_08() throws Exception {
		String path = BASE_PATH + "/updat/" + frameworkId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelKA").content(updateFrameworkValidJson));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 *
	 * Scenario 9 : update Framework with valid url, valid request body and
	 * valid framework identifier but invalid header( Channel Id Not Present in
	 * Header).
	 * 
	 * Given: Valid url, Valid request body and Valid framework identifier, Invalid Header (Channel Id Not Present) 
	 * When: Framework update API hits.
	 * Then: 400 , Bad Request with error Message: Invalid Request
	 * 
	 */
	@Test
	public void mockTestFramework_09() throws Exception {
		String path = BASE_PATH + "/update/" + frameworkId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.content(updateFrameworkValidJson));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 *
	 * Scenario 10 : update Framework with valid url, valid request body and
	 * valid framework identifier but invalid owner( Channel Id in Header will
	 * not match with owner channel Id).
	 * 
	 * Given: Valid url, Valid request body and Valid framework identifier,
	 * Invalid Header (Channel Id in Header will not match with owner channel id) 
	 * When: Framework update API hits. 
	 * Then: 400 , Invalid Request. Channel Id Not Matched. - CLIENT_ERROR
	 * 
	 */
	@Test
	public void mockTestFramework_10() throws Exception {
		String path = BASE_PATH + "/update/" + frameworkId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelBR").content(updateFrameworkValidJson));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 *
	 * Scenario 11 : update Framework with valid url, valid request body and
	 * Invalid framework identifier.
	 * 
	 * Given: Valid url, Valid request body and Invalid framework identifier
	 * When: Framework update API hits. 
	 * Then: 404 - Resource Not Found with error Message: Framework not found with id: framework Id
	 * 
	 */
	@Test
	public void mockTestFramework_11() throws Exception {
		String path = BASE_PATH + "/update/" + "test11111";
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelKA").content(updateFrameworkValidJson));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 *	// check - if custom message and different http error code is required. 
	 * Scenario 12 : update Framework with valid url, Invalid request body
	 * (wrong category instance id) and valid framework identifier.
	 * 
	 * Given: Valid url, Invalid request body (wrong category instance id) and valid framework identifier.
	 * When: Framework update API hits. 
	 * Then: 400 , Invalid node: could not find node: category instance id - CLIENT_ERROR
	 * 
	 */
	@Test
	public void mockTestFramework_12() throws Exception {
		String path = BASE_PATH + "/update/" + frameworkId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelKA").content(updateFrameworkInvalidCIJson));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 *
	 * Scenario 13 : list Framework with valid url, valid request body
	 * 
	 * Given: Valid url and Valid request body 
	 * When: Framework list API hits.
	 * Then: 200 ,OK - List of All Frameworks will be returned (By default
	 * "Live" status will be considered, if status is not supplied in criteria )
	 * 
	 */
	@Test
	public void mockTestFramework_13() throws Exception {
		String path = BASE_PATH + "/list";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.content(listFrameworkValidJson));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 *
	 * Scenario 14 : list Framework with Invalid url, valid request body
	 * 
	 * Given: Invalid url and Valid request body 
	 * When: Framework list API hits.
	 * Then: 404 , Invalid Request
	 * 
	 */
	@Test
	public void mockTestFramework_14() throws Exception {
		String path = BASE_PATH + "/lis";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.content(listFrameworkValidJson));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 *
	 * Scenario 15 : list Framework with Valid url, Invalid request body
	 * 
	 * Given: Valid url and Invalid request body 
	 * When: Framework list API hits.
	 * Then: 400 , Invalid Search Request - CLIENT_ERROR
	 * 
	 */
	@Test
	public void mockTestFramework_15() throws Exception {
		String path = BASE_PATH + "/list";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.content(listFrameworkInvalidJson));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 *
	 * Scenario 16 : Retire Framework with Valid url, Valid Framework Id
	 * 
	 * Given: Valid url and valid framework Id 
	 * When: Framework Retire API hits.
	 * Then: 200 - OK , Framework Status will be changed to "Retire" from "Live"
	 * 
	 */
	
	@Test
	public void mockTestFramework_16() throws Exception {
		String path = BASE_PATH + "/retire/" + frameworkId;
		actions = mockMvc.perform(
				MockMvcRequestBuilders.delete(path).header("user-id", "gauraw").header("X-Channel-Id", "channelKA"));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 *
	 * Scenario 17 : Retire Framework with Valid url, Invalid Framework Id
	 * 
	 * Given: Valid url and valid framework Id 
	 * When: Framework Retire API hits.
	 * Then: 404, - Resource Not Found with error Message: Framework not found with id: framework Id
	 * 
	 */
	@Test
	public void mockTestFramework_17() throws Exception {
		String path = BASE_PATH + "/retire/" + "test1234";
		actions = mockMvc.perform(
				MockMvcRequestBuilders.delete(path).header("user-id", "gauraw").header("X-Channel-Id", "channelKA"));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 *
	 * Scenario 18 : Retire Framework with Invalid url, Valid Framework Id
	 * 
	 * Given: Invalid url and valid framework Id 
	 * When: Framework Retire API hits. 
	 * Then: 404, Invalid Request
	 * 
	 */
	@Test
	public void mockTestFramework_18() throws Exception {
		String path = BASE_PATH + "/retire" + frameworkId;
		actions = mockMvc.perform(
				MockMvcRequestBuilders.delete(path).header("user-id", "gauraw").header("X-Channel-Id", "channelKA"));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}
	
	/*
	 * Scenario 19 : Create Framework with Valid Url, Valid Request Body & Invalid Channel ID.
	 * 
	 * Given: Valid url and valid request body and invalid channel id
	 * When: Framework Create API hits. 
	 * Then: 400, Invalid Channel Id. Channel doesn't exist.
	 * 
	 */
	@Test
	public void mockTestFramework_19() throws Exception {
		String path = BASE_PATH + "/create";
		actions = mockMvc.perform(
				MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "test").content(createFrameworkValidJson));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}
	
	/*
	 * Scenario 20 : Create Framework with Valid Url, Valid Request Body & Invalid Code.
	 * 
	 * Given: Valid url and valid request body and Invalid Code
	 * When: Framework Create API hits. 
	 * Then: 400, Unique code is mandatory for framework
	 * 
	 */
	@Test
	public void mockTestFramework_20() throws Exception{
		String createFrameworkValidJson = "{\"id\":\"ekstep.framework.create\",\"ver\": \"3.0\",\"ts\": \"YYYY-MM-DDThh:mm:ssZ+/-nn.nn\",\"params\": {\"did\": \"1234\",\"key\": \"1234\",\"msgid\": \"test1234\"},\"request\": {\"framework\": {\"name\": \"NCERT01\",\"description\": \"NCERT framework of Karnatka\",\"owner\": \"channelKA\"}}}";
		String path = BASE_PATH + "/create";
		actions = mockMvc.perform(
				MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "test").content(createFrameworkValidJson));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 21 : copy Framework with valid url, valid request body and
	 * valid framework identifier.
	 * 
	 * Given: Valid url, Valid request body and Valid framework identifier 
	 * When: Framework copy API hits. 
	 * Then: 200 - OK, Framework with full hierarchy got coppied.
	 * 
	 */
	
	@Test
	public void mockTestFramework_21() throws Exception {
		String path = BASE_PATH + "/copy/" + frameworkIdforCopyAPI;
		String copyFrameworkValidJson = "{\"request\":{\"framework\":{\"code\":\"NCERT COPY 21\",\"name\":\"NCERT COPY 21\",\"description\":\"NCERT COPY 21 Description\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelKA").content(copyFrameworkValidJson));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 22 : Copy Framework with Invalid url, valid request body and
	 * valid framework identifier.
	 * 
	 * Given: Valid url, Valid request body and Valid framework identifier 
	 * When:Framework copy API hits. 
	 * Then: 404 , Invalid Request.
	 * 
	 */
	@Test
	public void mockTestFramework_22() throws Exception {
		String path = BASE_PATH + "/cop/" + frameworkIdforCopyAPI;
		String copyFrameworkValidJson = "{\"request\":{\"framework\":{\"code\":\"NCERT COPY 22\",\"name\":\"NCERT COPY 22\",\"description\":\"NCERT COPY 22 Description\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelKA").content(copyFrameworkValidJson));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 *
	 * Scenario 23 : Copy Framework with valid url, valid request body and
	 * valid framework identifier but invalid header( Channel Id Not Present in
	 * Header).
	 * 
	 * Given: Valid url, Valid request body and Valid framework identifier, Invalid Header (Channel Id Not Present) 
	 * When: Framework copy API hits.
	 * Then: 400 , Bad Request with error Message: Invalid Request
	 * 
	 */
	@Test
	public void mockTestFramework_23() throws Exception {
		String path = BASE_PATH + "/copy/" + frameworkIdforCopyAPI;
		String copyFrameworkValidJson = "{\"request\":{\"framework\":{\"code\":\"NCERT COPY 23\",\"name\":\"NCERT COPY 23\",\"description\":\"NCERT COPY 23 Description\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.content(copyFrameworkValidJson));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 *
	 * Scenario 24 : Copy Framework with valid url, valid request body and
	 * valid framework identifier but invalid owner( Channel Id in Header will
	 * not match with owner channel Id).
	 * 
	 * Given: Valid url, Valid request body and Valid framework identifier,
	 * Invalid Header (Channel Id in Header will not match with owner channel id) 
	 * When: Framework copy API hits. 
	 * Then: 400 , Invalid Request. Channel Id Not Matched. - CLIENT_ERROR
	 * 
	 */
	@Test
	public void mockTestFramework_24() throws Exception {
		String path = BASE_PATH + "/copy/" + frameworkIdforCopyAPI;
		String copyFrameworkValidJson = "{\"request\":{\"framework\":{\"code\":\"NCERT COPY 24\",\"name\":\"NCERT COPY 24\",\"description\":\"NCERT COPY 24 Description\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelBR").content(copyFrameworkValidJson));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 *
	 * Scenario 25 : Copy Framework with valid url, valid request body and
	 * Invalid framework identifier.
	 * 
	 * Given: Valid url, Valid request body and Invalid framework identifier
	 * When: Framework copy API hits. 
	 * Then: 404 - Resource Not Found with error Message: Framework not found with id: framework Id
	 * 
	 */
	@Test
	public void mockTestFramework_25() throws Exception {
		String path = BASE_PATH + "/copy/" + "test11111";
		String copyFrameworkValidJson = "{\"request\":{\"framework\":{\"code\":\"NCERT COPY 25\",\"name\":\"NCERT COPY 25\",\"description\":\"NCERT COPY 25 Description\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelKA").content(copyFrameworkValidJson));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 26 : Copy Framework with valid url, Invalid request body
	 * (original frameworkId and duplicate framework code is same) and valid framework identifier.
	 * 
	 * Given: Valid url, Invalid request body (original frameworkId and copied framework code is same) 
	 * and valid framework identifier.
	 * When: Framework copy API hits. 
	 * Then: 400 , Unique code is mandatory for framework - CLIENT_ERROR
	 * 
	 */
	@Test
	public void mockTestFramework_26() throws Exception {
		String path = BASE_PATH + "/copy/" + frameworkIdforCopyAPI;
		String copyFrameworkValidJson = "{\"request\":{\"framework\":{\"code\":\""+ frameworkIdforCopyAPI + "\",\"name\":\"NCERT COPY 26\",\"description\":\"NCERT COPY 26 Description\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelKA").content(copyFrameworkValidJson));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}
	
	/*
	 * Scenario 27 : Copy Framework with valid url, Invalid request body
	 * (does not have code) and valid framework identifier.
	 * 
	 * Given: Valid url, Invalid request body (does not have code) 
	 * and valid framework identifier.
	 * When: Framework copy API hits. 
	 * Then: 400 , Unique code is mandatory for framework - CLIENT_ERROR
	 * 
	 */
	@Test
	public void mockTestFramework_27() throws Exception {
		String path = BASE_PATH + "/copy/" + frameworkIdforCopyAPI;
		String copyFrameworkValidJson = "{\"request\":{\"framework\":{\"name\":\"NCERT COPY 27\",\"description\":\"NCERT COPY 27 Description\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelKA").content(copyFrameworkValidJson));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}
	
	/*
	 * Scenario 28 : Copy Framework with valid url, Invalid request body
	 * (try to create framework with certain code where framework already exists) and valid framework identifier.
	 * 
	 * Given: Valid url, Invalid request body (try to create framework with certain code where framework already exists) 
	 * and valid framework identifier.
	 * When: Framework copy API hits. 
	 * Then: 400 , Unique code is mandatory for framework - CLIENT_ERROR
	 * 
	 */
	@Test
	public void mockTestFramework_28() throws Exception {
		String existingFrameworkId = "";
		try {
			String createFrameworkReq = "{\"name\": \"NCERT28\",\"description\": \"NCERT28 framework\",\"code\": \"NCERT28\"}";
			Map<String, Object> requestMap = mapper.readValue(createFrameworkReq,
					new TypeReference<Map<String, Object>>() {
					});
			requestMap.put("channel", channelId);
			Response resp = frameworkManager.createFramework(requestMap, channelId);
			existingFrameworkId = (String) resp.getResult().get("node_id");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String path = BASE_PATH + "/copy/" + frameworkIdforCopyAPI;
		String copyFrameworkValidJson = "{\"request\":{\"framework\":{\"code\": \"" + existingFrameworkId + "\", \"name\":\"NCERT COPY 28\",\"description\":\"NCERT COPY 28 Description\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelKA").content(copyFrameworkValidJson));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void mockTestFramework_29() throws Exception {
		String path = BASE_PATH + "/copy/" + frameworkIdforCopyAPI;
		String copyFrameworkValidJson = "{\"request\":{}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelKA").content(copyFrameworkValidJson));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void createFrameworkWithTranslationsExpect200() throws Exception {
		//Create Framework
		String createFrameworkReq="{\"request\": {\"framework\": {\"name\": \"Test Framework\",\"description\": \"test framework\",\"code\": \"test.fr\",\"owner\": \"in.ekstep\",\"type\": \"K-12\",\"translations\":{\"hi\":\"टेस्ट फ़्रेम्वर्क\",\"ka\":\"ೂಾೇೂ ಿೀೋಸಾೈದೀಕ\"}}}}";
		String path = BASE_PATH + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelKA")
				.content(createFrameworkReq));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		//Publish Framework
		path = BASE_PATH + "/publish/" + "test.fr";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("X-Channel-Id", "channelKA").contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		delay(10000);
		//Read Framework
		path = BASE_PATH + "/read/" + "test.fr";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON));
		Response resp=getResponse(actions);
		Map<String,Object> framework=(Map<String, Object>) resp.getResult().get("framework");
		String name=(String) framework.get("name");
		String code=(String) framework.get("code");
		String desc=(String) framework.get("description");
		Map<String,Object> translations=mapper.readValue((String)framework.get("translations"), Map.class);
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Assert.assertEquals("Test Framework", name);
		Assert.assertEquals("test.fr", code);
		Assert.assertEquals("test framework", desc);
		Assert.assertEquals("टेस्ट फ़्रेम्वर्क", (String)translations.get("hi"));
		Assert.assertEquals("ೂಾೇೂ ಿೀೋಸಾೈದೀಕ", (String)translations.get("ka"));
	}
	
	/**
	 * @param actions
	 * @return
	 */
	private static Response getResponse(ResultActions actions) {
		String content = null;
		Response resp = null;
		try {
			content = actions.andReturn().getResponse().getContentAsString();
			if (StringUtils.isNotBlank(content))
				resp = mapper.readValue(content, Response.class);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}
		
}