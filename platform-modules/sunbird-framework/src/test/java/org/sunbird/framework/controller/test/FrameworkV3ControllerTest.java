package org.sunbird.framework.controller.test;

import java.util.List;
import java.util.Map;

import org.sunbird.common.dto.Response;
import org.sunbird.common.util.FrameworkCache;
import org.sunbird.framework.mgr.impl.CategoryInstanceManagerImpl;
import org.sunbird.framework.mgr.impl.CategoryManagerImpl;
import org.sunbird.framework.mgr.impl.ChannelManagerImpl;
import org.sunbird.framework.mgr.impl.FrameworkManagerImpl;
import org.sunbird.graph.cache.util.RedisStoreUtil;
import org.sunbird.learning.router.LearningRequestRouterPool;
import org.sunbird.framework.test.common.CommonTestSetup;
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
 * Test Cases for Framework API
 * 
 * @author gauraw
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class FrameworkV3ControllerTest extends CommonTestSetup {

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

	private static final String createFrameworkReq = "{\"name\": \"NCERT01\",\"description\": \"NCERT framework of Karnatka\",\"code\": \"ka_ncert\"}";

	private static final String createChannelReq = "{\"name\":\"Karnatka\",\"description\":\"Channel for Karnatka\",\"code\":\"channelKA\"}";

	private static final String createCategoryReq = "{ \"name\":\"Class\", \"description\":\"\", \"code\":\"class\" }";

	private static final String createFrameworkValidJson = "{\"id\":\"ekstep.framework.create\",\"ver\": \"3.0\",\"ts\": \"YYYY-MM-DDThh:mm:ssZ+/-nn.nn\",\"params\": {\"did\": \"1234\",\"key\": \"1234\",\"msgid\": \"test1234\"},\"request\": {\"framework\": {\"name\": \"NCERT01\",\"description\": \"NCERT framework of Karnatka\",\"code\": \"org.sunbird.framework.create\"}}}";

	private static final String createFrameworkInvalidJson = "{\"id\":\"ekstep.framework.create\",\"ver\": \"3.0\",\"ts\": \"YYYY-MM-DDThh:mm:ssZ+/-nn.nn\",\"params\": {\"did\": \"1234\",\"key\": \"1234\",\"msgid\": \"test1234\"},\"request\": {\"frameworks\": {\"name\": \"NCERT01\",\"description\": \"NCERT framework of Karnatka\",\"code\": \"org.sunbird.framework.create\"}}}";

	private static String updateFrameworkValidJson = "{\"id\": \"ekstep.framework.update\",\"ver\": \"3.0\",\"ts\": \"YYYY-MM-DDThh:mm:ssZ+/-nn.nn\",\"params\": {\"did\": \"1234\",\"key\": \"1234\",\"msgid\": \"test1234\"},\"request\": {\"framework\": {\"versionKey\": \"1511787372693\",\"description\": \" framework description\",\"categories\": [{\"identifier\": \"do_11238579307347148811\",\"name\": \"cat3\"}]}}}";

	private static final String updateFrameworkInvalidCIJson = "{\"id\": \"ekstep.framework.update\",\"ver\": \"3.0\",\"ts\": \"YYYY-MM-DDThh:mm:ssZ+/-nn.nn\",\"params\": {\"did\": \"1234\",\"key\": \"1234\",\"msgid\": \"test1234\"},\"request\": {\"framework\": {\"versionKey\": \"1511787372693\",\"description\": \" framework description\",\"categories\": [{\"identifier\": \"do_112385793073471488\",\"name\": \"cat3\"}]}}}";

	private static final String listFrameworkValidJson = "{\"id\": \"ekstep.framework.list\",\"ver\": \"3.0\",\"ts\": \"YYYY-MM-DDThh:mm:ssZ+/-nn.nn\",\"params\": {\"did\": \"1234\",\"key\": \"1234\",\"msgid\": \"test1234\"},\"request\": { \"search\": {\"status\":\"\"}}}";

	private static final String listFrameworkInvalidJson = "{\"id\": \"ekstep.framework.list\",\"ver\": \"3.0\",\"ts\": \"YYYY-MM-DDThh:mm:ssZ+/-nn.nn\",\"params\": {\"did\": \"1234\",\"key\": \"1234\",\"msgid\": \"test1234\"},\"request\": {}}";


	private static final String SCRIPT_2 = "CREATE TABLE IF NOT EXISTS hierarchy_store_test.framework_hierarchy_test (identifier text,hierarchy text,PRIMARY KEY (identifier));";
	private static final String SCRIPT_1 = "CREATE KEYSPACE IF NOT EXISTS hierarchy_store_test WITH replication = {'class': 'SimpleStrategy','replication_factor': '1'};";


	@BeforeClass
	public static void setUp() throws Exception {
		loadDefinition("definitions/channel_definition.json", "definitions/framework_definition.json",
				"definitions/category_definition.json", "definitions/categoryInstance_definition.json");
		executeScript(SCRIPT_1, SCRIPT_2);
		LearningRequestRouterPool.init();
		createChannel();
		createFramework();
	}

	@Before
	public void init() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
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
	
	/*
	 * Scenario 1 : Create Framework with valid url and valid request body.
	 * 
	 * Given: Valid url and valid request body.
	 * When: Framework create API hits.
	 * Then: 200 - OK, 1 framework with no relationship got created.
	 * 
	 */
	@Test
	public void mockTestFrameworkCreate() throws Exception {
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
	public void mockTestFrameworkCreateWithInvalidURL() throws Exception {
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
	public void mockTestFrameworkCreateWithInvalidJson() throws Exception {
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
		delay(30000);
		//Read Framework
		path = BASE_PATH + "/read/" + "test_fr";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
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
	public void mockTestFrameworkReadWithInvalidUrl() throws Exception {
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
	public void mockTestFrameworkReadWithInvalidFrameworkId() throws Exception {
		String id = "ttttt1234"; // Invalid Framework Id
		String path = BASE_PATH + "/read/" + id;
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
	public void mockTestFrameworkUpdateExpect404() throws Exception {
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
	public void mockTestFrameworkUpdateWithInvalidUrl() throws Exception {
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
	public void mockTestFrameworkUpdate() throws Exception {
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
	public void mockTestFrameworkUpdateExpect400() throws Exception {
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
	public void mockTestFrameworkExpect404() throws Exception {
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
	public void mockTestFrameworkUpdateWithInvalidJson() throws Exception {
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
	public void mockTestFrameworkList() throws Exception {
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
	public void mockTestFrameworkListWithInvalidUrl() throws Exception {
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
	public void mockTestFrameworkListWithInvalidJson() throws Exception {
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
	public void mockTestFrameworkRetire() throws Exception {
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
	public void mockTestFrameworkRetireExpect404() throws Exception {
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
	public void mockTestFrameworkRetireExpectNotFound() throws Exception {
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
	public void mockTestFrameworkCreateExpectRes400() throws Exception {
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
	public void mockTestFrameworkCreateExpect400() throws Exception{
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
	public void mockTestFrameworkCopy() throws Exception {
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
	public void mockTestFrameworkCopyWithInvalidUrl() throws Exception {
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
	public void mockTestFrameworkCopyExpectResponse400() throws Exception {
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
	public void mockTestFrameworkCopyExpectResp400() throws Exception {
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
	public void mockTestFrameworkCopyWith404() throws Exception {
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
	public void mockTestFrameworkCopyExpectRes400() throws Exception {
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
	public void mockTestFrameworkCopyExpectResponseCode400() throws Exception {
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
	public void mockTestFrameworkExpect400() throws Exception {
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
	public void mockTestFrameworkCopyExpect400() throws Exception {
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
	
	@SuppressWarnings("unchecked")
	@Test
	public void createFrameworkWithEmptyTranslationsExpect200() throws Exception {
		//Create Framework
		String createFrameworkReq="{\"request\": {\"framework\": {\"name\": \"Test Framework\",\"description\": \"test framework\",\"code\": \"test.fr.1\",\"owner\": \"in.ekstep\",\"type\": \"K-12\",\"translations\":{}}}}";
		String path = BASE_PATH + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelKA")
				.content(createFrameworkReq));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		//Publish Framework
		path = BASE_PATH + "/publish/" + "test.fr.1";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("X-Channel-Id", "channelKA").contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		delay(30000);
		//Read Framework
		path = BASE_PATH + "/read/" + "test.fr.1";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON));
		Response resp=getResponse(actions);
		Map<String,Object> framework=(Map<String, Object>) resp.getResult().get("framework");
		String name=(String) framework.get("name");
		String code=(String) framework.get("code");
		String desc=(String) framework.get("description");
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Assert.assertEquals("Test Framework", name);
		Assert.assertEquals("test.fr.1", code);
		Assert.assertEquals("test framework", desc);
		Assert.assertNull(framework.get("translations"));
	}
	/*
	 * Create Framework with Translation having invalid language code.
	 * Expected: 400 - CLIENT_ERROR
	 * */
	@Test
	public void createFrameworkWithTranslationsExpect400() throws Exception {
		String createFrameworkReq="{\"request\": {\"framework\": {\"name\": \"Test Framework\",\"description\": \"test framework\",\"code\": \"test.fr.2\",\"owner\": \"in.ekstep\",\"type\": \"K-12\",\"translations\":{\"pq\":\"टेस्ट फ़्रेम्वर्क\",\"ka\":\"ೂಾೇೂ ಿೀೋಸಾೈದೀಕ\"}}}}";
		String path = BASE_PATH + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelKA")
				.content(createFrameworkReq));
		Response resp=getResponse(actions);
		Assert.assertEquals(400, resp.getResponseCode().code());
		Assert.assertEquals("ERR_INVALID_LANGUAGE_CODE", resp.getParams().getErr());
	}


	@Test
	@SuppressWarnings("unchecked")
	public void readFrameworkWithValidIdAndCategoriesExpect200() throws Exception {
		String frameworkHierarchy = "{\"identifier\":\"tests\",\"code\":\"NCF\",\"name\":\"State (Uttar Pradesh)\"," +
				"\"description\":\"NCF \",\"categories\":[{\"identifier\":\"ncf_board\",\"code\":\"board\"," +
				"\"terms\":[{\"identifier\":\"ncf_board_cbse\",\"code\":\"cbse\",\"translations\":null,\"name\":\"CBSE\"," +
				"\"description\":\"\",\"index\":1,\"category\":\"board\",\"status\":\"Live\"},{\"identifier\":\"ncf_board_icse\"," +
				"\"code\":\"icse\",\"translations\":null,\"name\":\"ICSE\",\"description\":\"\",\"index\":2,\"category\":\"board\"," +
				"\"status\":\"Live\"},{\"identifier\":\"ncf_board_upboard\",\"code\":\"upboard\",\"translations\":null," +
				"\"name\":\"State (Uttar Pradesh)\",\"description\":\"State (Uttar Pradesh)\",\"index\":3,\"category\":\"board\"," +
				"\"status\":\"Live\"},{\"identifier\":\"ncf_board_apboard\",\"code\":\"apboard\",\"translations\":null," +
				"\"name\":\"State (Andhra Pradesh)\",\"description\":\"State (Andhra Pradesh)\",\"index\":4,\"category\":\"board\"," +
				"\"status\":\"Live\"},{\"identifier\":\"ncf_board_tnboard\",\"code\":\"tnboard\",\"translations\":null,\"name\":\"State (Tamil Nadu)\",\"description\":\"State (Tamil Nadu)\",\"index\":5,\"category\":\"board\",\"status\":\"Live\"},{\"identifier\":\"ncf_board_mscert\",\"code\":\"mscert\",\"translations\":null,\"name\":\"State (Maharashtra)\",\"description\":\"State (Maharashtra)\",\"index\":6,\"category\":\"board\",\"status\":\"Live\"},{\"identifier\":\"ncf_board_bser\",\"code\":\"bser\",\"translations\":null,\"name\":\"State (Rajasthan)\",\"description\":\"State (Rajasthan)\",\"index\":7,\"category\":\"board\",\"status\":\"Live\"},{\"identifier\":\"ncf_board_others\",\"code\":\"others\",\"translations\":null,\"name\":\"Other\",\"description\":\"Other\",\"index\":8,\"category\":\"board\",\"status\":\"Live\"}],\"translations\":null,\"name\":\"Curriculum\",\"description\":\"\",\"index\":1,\"status\":\"Live\"},{\"identifier\":\"ncf_gradelevel\",\"code\":\"gradeLevel\",\"terms\":[{\"identifier\":\"ncf_gradelevel_kindergarten\",\"code\":\"kindergarten\",\"translations\":null,\"name\":\"KG\",\"description\":\"KG\",\"index\":1,\"category\":\"gradelevel\",\"status\":\"Live\"},{\"identifier\":\"ncf_gradelevel_grade1\",\"code\":\"grade1\",\"translations\":null,\"name\":\"Class 1\",\"description\":\"Class 1\",\"index\":2,\"category\":\"gradelevel\",\"status\":\"Live\"},{\"identifier\":\"ncf_gradelevel_grade2\",\"code\":\"grade2\",\"translations\":null,\"name\":\"Class 2\",\"description\":\"Class 2\",\"index\":3,\"category\":\"gradeLevel\",\"status\":\"Live\"},{\"identifier\":\"ncf_gradelevel_grade3\",\"code\":\"grade3\",\"translations\":null,\"name\":\"Class 3\",\"description\":\"Class 3\",\"index\":4,\"category\":\"gradelevel\",\"status\":\"Live\"},{\"identifier\":\"ncf_gradelevel_grade4\",\"code\":\"grade4\",\"translations\":null,\"name\":\"Class 4\",\"description\":\"Class 4\",\"index\":5,\"category\":\"gradelevel\",\"status\":\"Live\"},{\"identifier\":\"ncf_gradelevel_grade5\",\"code\":\"grade5\",\"translations\":null,\"name\":\"Class 5\",\"description\":\"Class 5\",\"index\":6,\"category\":\"gradelevel\",\"status\":\"Live\"},{\"identifier\":\"ncf_gradelevel_grade6\",\"code\":\"grade6\",\"translations\":null,\"name\":\"Class 6\",\"description\":\"Class 6\",\"index\":7,\"category\":\"gradelevel\",\"status\":\"Live\"},{\"identifier\":\"ncf_gradelevel_grade7\",\"code\":\"grade7\",\"translations\":null,\"name\":\"Class 7\",\"description\":\"Class 7\",\"index\":8,\"category\":\"gradelevel\",\"status\":\"Live\"},{\"identifier\":\"ncf_gradelevel_grade8\",\"code\":\"grade8\",\"translations\":null,\"name\":\"Class 8\",\"description\":\"Class 8\",\"index\":9,\"category\":\"gradelevel\",\"status\":\"Live\"},{\"identifier\":\"ncf_gradelevel_grade9\",\"code\":\"grade9\",\"translations\":null,\"name\":\"Class 9\",\"description\":\"Class 9\",\"index\":10,\"category\":\"gradelevel\",\"status\":\"Live\"},{\"identifier\":\"ncf_gradelevel_grade10\",\"code\":\"grade10\",\"translations\":null,\"name\":\"Class 10\",\"description\":\"Class 10\",\"index\":11,\"category\":\"gradelevel\",\"status\":\"Live\"},{\"identifier\":\"ncf_gradelevel_grade11\",\"code\":\"grade11\",\"translations\":null,\"name\":\"Class 11\",\"description\":\"Class 11\",\"index\":12,\"category\":\"gradelevel\",\"status\":\"Live\"},{\"identifier\":\"ncf_gradelevel_grade12\",\"code\":\"grade12\",\"translations\":null,\"name\":\"Class 12\",\"description\":\"Class 12\",\"index\":13,\"category\":\"gradelevel\",\"status\":\"Live\"},{\"identifier\":\"ncf_gradelevel_others\",\"code\":\"others\",\"translations\":null,\"name\":\"Other\",\"description\":\"\",\"index\":14,\"category\":\"gradeLevel\",\"status\":\"Live\"}],\"translations\":null,\"name\":\"Class\",\"description\":\"\",\"index\":2,\"status\":\"Live\"},{\"identifier\":\"ncf_subject\",\"code\":\"subject\",\"terms\":[{\"identifier\":\"ncf_subject_mathematics\",\"code\":\"mathematics\",\"translations\":null,\"name\":\"Mathematics\",\"description\":\"\",\"index\":1,\"category\":\"subject\",\"status\":\"Live\"},{\"identifier\":\"ncf_subject_english\",\"code\":\"english\",\"translations\":null,\"name\":\"English\",\"description\":\"\",\"index\":2,\"category\":\"subject\",\"status\":\"Live\"},{\"identifier\":\"ncf_subject_tamil\",\"code\":\"tamil\",\"translations\":null,\"name\":\"Tamil\",\"description\":\"\",\"index\":3,\"category\":\"subject\",\"status\":\"Live\"},{\"identifier\":\"ncf_subject_telugu\",\"code\":\"telugu\",\"translations\":null,\"name\":\"Telugu\",\"description\":\"\",\"index\":4,\"category\":\"subject\",\"status\":\"Live\"},{\"identifier\":\"ncf_subject_geography\",\"code\":\"geography\",\"translations\":null,\"name\":\"Geography\",\"description\":\"\",\"index\":5,\"category\":\"subject\",\"status\":\"Live\"},{\"identifier\":\"ncf_subject_urdu\",\"code\":\"urdu\",\"translations\":null,\"name\":\"Urdu\",\"description\":\"\",\"index\":6,\"category\":\"subject\",\"status\":\"Live\"},{\"identifier\":\"ncf_subject_kannada\",\"code\":\"kannada\",\"translations\":null,\"name\":\"Kannada\",\"description\":\"\",\"index\":7,\"category\":\"subject\",\"status\":\"Live\"},{\"identifier\":\"ncf_subject_assamese\",\"code\":\"assamese\",\"translations\":null,\"name\":\"Assamese\",\"description\":\"\",\"index\":8,\"category\":\"subject\",\"status\":\"Live\"},{\"identifier\":\"ncf_subject_physics\",\"code\":\"physics\",\"translations\":null,\"name\":\"Physics\",\"description\":\"\",\"index\":9,\"category\":\"subject\",\"status\":\"Live\"},{\"identifier\":\"ncf_subject_chemistry\",\"code\":\"chemistry\",\"translations\":null,\"name\":\"Chemistry\",\"description\":\"\",\"index\":10,\"category\":\"subject\",\"status\":\"Live\"},{\"identifier\":\"ncf_subject_hindi\",\"code\":\"hindi\",\"translations\":null,\"name\":\"Hindi\",\"description\":\"\",\"index\":11,\"category\":\"subject\",\"status\":\"Live\"},{\"identifier\":\"ncf_subject_marathi\",\"code\":\"marathi\",\"translations\":null,\"name\":\"Marathi\",\"description\":\"\",\"index\":12,\"category\":\"subject\",\"status\":\"Live\"},{\"identifier\":\"ncf_subject_environmentalstudies\",\"code\":\"environmentalstudies\",\"translations\":null,\"name\":\"EvS\",\"description\":\"EvS\",\"index\":13,\"category\":\"subject\",\"status\":\"Live\"},{\"identifier\":\"ncf_subject_politicalscience\",\"code\":\"politicalscience\",\"translations\":null,\"name\":\"Political Science\",\"description\":\"\",\"index\":14,\"category\":\"subject\",\"status\":\"Live\"},{\"identifier\":\"ncf_subject_bengali\",\"code\":\"bengali\",\"translations\":null,\"name\":\"Bengali\",\"description\":\"\",\"index\":15,\"category\":\"subject\",\"status\":\"Live\"},{\"identifier\":\"ncf_subject_history\",\"code\":\"history\",\"translations\":null,\"name\":\"History\",\"description\":\"\",\"index\":16,\"category\":\"subject\",\"status\":\"Live\"},{\"identifier\":\"ncf_subject_gujarati\",\"code\":\"gujarati\",\"translations\":null,\"name\":\"Gujarati\",\"description\":\"\",\"index\":17,\"category\":\"subject\",\"status\":\"Live\"},{\"identifier\":\"ncf_subject_biology\",\"code\":\"biology\",\"translations\":null,\"name\":\"Biology\",\"description\":\"\",\"index\":18,\"category\":\"subject\",\"status\":\"Live\"},{\"identifier\":\"ncf_subject_oriya\",\"code\":\"oriya\",\"translations\":null,\"name\":\"Odia\",\"description\":\"Odia\",\"index\":19,\"category\":\"subject\",\"status\":\"Live\"},{\"identifier\":\"ncf_subject_punjabi\",\"code\":\"punjabi\",\"translations\":null,\"name\":\"Punjabi\",\"description\":\"\",\"index\":20,\"category\":\"subject\",\"status\":\"Live\"},{\"identifier\":\"ncf_subject_nepali\",\"code\":\"nepali\",\"translations\":null,\"name\":\"Nepali\",\"description\":\"\",\"index\":21,\"category\":\"subject\",\"status\":\"Live\"},{\"identifier\":\"ncf_subject_malayalam\",\"code\":\"malayalam\",\"translations\":null,\"name\":\"Malayalam\",\"description\":\"\",\"index\":22,\"category\":\"subject\",\"status\":\"Live\"},{\"identifier\":\"ncf_subject_socialstudies\",\"code\":\"socialstudies\",\"translations\":null,\"name\":\"Social Studies\",\"description\":\"Social Studies\",\"index\":23,\"category\":\"subject\",\"status\":\"Live\"},{\"identifier\":\"ncf_subject_science\",\"code\":\"science\",\"translations\":null,\"name\":\"Science\",\"description\":\"Science\",\"index\":24,\"category\":\"subject\",\"status\":\"Live\"},{\"identifier\":\"ncf_subject_sanskrit\",\"code\":\"sanskrit\",\"translations\":null,\"name\":\"Sanskrit\",\"description\":\"Sanskrit\",\"index\":25,\"category\":\"subject\",\"status\":\"Live\"},{\"identifier\":\"ncf_subject_healthandphysicaleducation\",\"code\":\"healthandphysicaleducation\",\"translations\":null,\"name\":\"Health and Physical Education\",\"description\":\"Health and Physical Education\",\"index\":26,\"category\":\"subject\",\"status\":\"Live\"},{\"identifier\":\"ncf_subject_economics\",\"code\":\"economics\",\"translations\":null,\"name\":\"Economics\",\"description\":\"Economics\",\"index\":27,\"category\":\"subject\",\"status\":\"Live\"}],\"translations\":null,\"name\":\"Resource\",\"description\":\"Resource\",\"index\":3,\"status\":\"Live\"},{\"identifier\":\"ncf_medium\",\"code\":\"medium\",\"terms\":[{\"identifier\":\"ncf_medium_english\",\"code\":\"english\",\"translations\":null,\"name\":\"English\",\"description\":\"\",\"index\":1,\"category\":\"medium\",\"status\":\"Live\"},{\"identifier\":\"ncf_medium_hindi\",\"code\":\"hindi\",\"translations\":null,\"name\":\"Hindi\",\"description\":\"\",\"index\":2,\"category\":\"medium\",\"status\":\"Live\"},{\"identifier\":\"ncf_medium_oriya\",\"code\":\"oriya\",\"translations\":null,\"name\":\"Odia\",\"description\":\"Odia\",\"index\":3,\"category\":\"medium\",\"status\":\"Live\"},{\"identifier\":\"ncf_medium_telugu\",\"code\":\"telugu\",\"translations\":null,\"name\":\"Telugu\",\"description\":\"\",\"index\":4,\"category\":\"medium\",\"status\":\"Live\"},{\"identifier\":\"ncf_medium_kannada\",\"code\":\"kannada\",\"translations\":null,\"name\":\"Kannada\",\"description\":\"\",\"index\":5,\"category\":\"medium\",\"status\":\"Live\"},{\"identifier\":\"ncf_medium_marathi\",\"code\":\"marathi\",\"translations\":null,\"name\":\"Marathi\",\"description\":\"\",\"index\":6,\"category\":\"medium\",\"status\":\"Live\"},{\"identifier\":\"ncf_medium_assamese\",\"code\":\"assamese\",\"translations\":null,\"name\":\"Assamese\",\"description\":\"\",\"index\":7,\"category\":\"medium\",\"status\":\"Live\"},{\"identifier\":\"ncf_medium_bengali\",\"code\":\"bengali\",\"translations\":null,\"name\":\"Bengali\",\"description\":\"\",\"index\":8,\"category\":\"medium\",\"status\":\"Live\"},{\"identifier\":\"ncf_medium_gujarati\",\"code\":\"gujarati\",\"translations\":null,\"name\":\"Gujarati\",\"description\":\"\",\"index\":9,\"category\":\"medium\",\"status\":\"Live\"},{\"identifier\":\"ncf_medium_urdu\",\"code\":\"urdu\",\"translations\":null,\"name\":\"Urdu\",\"description\":\"\",\"index\":10,\"category\":\"medium\",\"status\":\"Live\"},{\"identifier\":\"ncf_medium_other\",\"code\":\"other\",\"translations\":null,\"name\":\"Other\",\"description\":\"\",\"index\":11,\"category\":\"medium\",\"status\":\"Live\"}],\"translations\":null,\"name\":\"Medium\",\"description\":\"\",\"index\":4,\"status\":\"Live\"},{\"identifier\":\"ncf_topic\",\"code\":\"topic\",\"terms\":[{\"identifier\":\"ncf_topic_leadership_management\",\"code\":\"leadership_management\",\"translations\":null,\"name\":\"Leadership Management\",\"description\":\"Leadership Management\",\"index\":1,\"category\":\"topic\",\"status\":\"Live\"},{\"identifier\":\"ncf_topic_health_education\",\"code\":\"health_education\",\"translations\":null,\"name\":\"Health Education\",\"description\":\"Health Education\",\"index\":2,\"category\":\"topic\",\"status\":\"Live\"},{\"identifier\":\"ncf_topic_personal_development\",\"code\":\"personal_development\",\"translations\":null,\"name\":\"Personal Development\",\"description\":\"Personal Development\",\"index\":3,\"category\":\"topic\",\"status\":\"Live\"}],\"translations\":null,\"name\":\"Topic\",\"description\":\"Topic\",\"index\":5,\"status\":\"Live\"}],\"type\":\"K-12\",\"objectType\":\"Framework\"}";
		String query = "INSERT into hierarchy_store_test.framework_hierarchy_test(identifier, hierarchy) values('tests', '" + frameworkHierarchy + "');";
		executeScript(query);
		//Read Framework
		String path = BASE_PATH + "/read/tests/?categories=subject,board";

		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp=getResponse(actions);
		Map<String,Object> framework=(Map<String, Object>) resp.getResult().get("framework");
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Assert.assertNotNull(framework);
		Assert.assertNotNull(RedisStoreUtil.get("fw_tests_board_subject"));
		FrameworkCache.delete("tests");
	}

	public void readFrameworkWithValidIdentifierAndAssociationsExpect200() throws Exception {
		String frameworkHierarchy = "{\"identifier\":\"tn_k-12_5\",\"code\":\"tn_k-12_5\",\"name\":\"State (Tamil Nadu)\",\"description\":\"tn_k-12_5\",\"categories\":[{\"identifier\":\"tn_k-12_5_subject\",\"code\":\"subject\",\"terms\":[{\"associations\":[{\"identifier\":\"tn_k-12_5_topic_english_l1con_118\",\"code\":\"english_l1Con_118\",\"translations\":null,\"name\":\"The Cat And The Pain Killer\",\"description\":\"The Cat And The Pain Killer\",\"category\":\"topic\",\"status\":\"Live\"},{\"identifier\":\"tn_k-12_5_topic_english_l1con_116\",\"code\":\"english_l1Con_116\",\"translations\":null,\"name\":\"Seventeen Oranges\",\"description\":\"Seventeen Oranges\",\"category\":\"topic\",\"status\":\"Live\"},{\"identifier\":\"tn_k-12_5_topic_english_l1con_120\",\"code\":\"english_l1Con_120\",\"translations\":null,\"name\":\"On Killing A Tree\",\"description\":\"On Killing A Tree\",\"category\":\"topic\",\"status\":\"Live\"},{\"identifier\":\"tn_k-12_5_topic_english_l1con_117\",\"code\":\"english_l1Con_117\",\"translations\":null,\"name\":\"The Spider And The Fly\",\"description\":\"The Spider And The Fly\",\"category\":\"topic\",\"status\":\"Live\"},{\"identifier\":\"tn_k-12_5_topic_english_l1con_119\",\"code\":\"english_l1Con_119\",\"translations\":null,\"name\":\"Water–the Elixir Of Life\",\"description\":\"Water–the Elixir Of Life\",\"category\":\"topic\",\"status\":\"Live\"}],\"identifier\":\"tn_k-12_5_subject_english\",\"code\":\"english\",\"translations\":null,\"name\":\"English\",\"description\":\"English\",\"index\":24,\"category\":\"subject\",\"status\":\"Live\"}],\"translations\":null,\"name\":\"Subject\",\"description\":\"Subject\",\"index\":4,\"status\":\"Live\"}],\"type\":\"K-12\",\"objectType\":\"Framework\"}";
		String query = "INSERT into hierarchy_store_test.framework_hierarchy_test(identifier, hierarchy) values('tn_k-12_5', '" + frameworkHierarchy + "');";
		executeScript(query);
		//Read Framework
		String path = BASE_PATH + "/read/" + "tn_k-12_5";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp=getResponse(actions);
		Map<String,Object> framework=(Map<String, Object>) resp.getResult().get("framework");
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		List<Map<String, Object>> associations = (List<Map<String, Object>>) ((List<Map<String, Object>>) ((List<Map<String, Object>>)framework.get("categories")).get(0).get("terms")).get(0).get("associations");
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Assert.assertNotNull(associations);
	}
}