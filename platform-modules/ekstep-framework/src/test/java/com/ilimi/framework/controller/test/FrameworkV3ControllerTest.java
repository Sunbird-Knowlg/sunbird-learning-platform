package com.ilimi.framework.controller.test;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
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
import com.ilimi.common.dto.Response;
import com.ilimi.framework.mgr.impl.CategoryInstanceManagerImpl;
import com.ilimi.framework.mgr.impl.ChannelManagerImpl;
import com.ilimi.framework.mgr.impl.FrameworkManagerImpl;
import com.ilimi.framework.test.common.TestSetup;

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
public class FrameworkV3ControllerTest extends TestSetup {

	@Autowired
	private WebApplicationContext context;

	MockMvc mockMvc;

	private ResultActions actions;
	private static final String basePath = "/v3/framework";
	private static ObjectMapper mapper = new ObjectMapper();
	private static String frameworkId;
	private static String categoryInstanceId;
	private static String channelId;
	private static CategoryInstanceManagerImpl categoryInstManager = new CategoryInstanceManagerImpl();
	private static ChannelManagerImpl channelManager = new ChannelManagerImpl();
	private static FrameworkManagerImpl frameworkManager = new FrameworkManagerImpl();

	private static final String createFrameworkReq = "{\"name\": \"NCERT01\",\"description\": \"NCERT framework of Karnatka\",\"code\": \"org.ekstep.framework.create\",\"owner\": \"channelKA\"}";

	private static final String createCategoryInstanceReq = "{\"name\":\"category\",\"description\":\"\",\"code\":\"medium\"}";
	
	private static final String createChannelReq = "{\"name\":\"channel\",\"description\":\"\",\"code\":\"channelKA\",\"identifier\":\"channelKA\"}";

	private static final String createFrameworkValidJson = "{\"id\":\"ekstep.framework.create\",\"ver\": \"3.0\",\"ts\": \"YYYY-MM-DDThh:mm:ssZ+/-nn.nn\",\"params\": {\"did\": \"1234\",\"key\": \"1234\",\"msgid\": \"test1234\"},\"request\": {\"framework\": {\"name\": \"NCERT01\",\"description\": \"NCERT framework of Karnatka\",\"code\": \"org.ekstep.framework.create\",\"owner\": \"channelKA\"}}}";

	// frameworks is used instead of framework
	private static final String createFrameworkInvalidJson = "{\"id\":\"ekstep.framework.create\",\"ver\": \"3.0\",\"ts\": \"YYYY-MM-DDThh:mm:ssZ+/-nn.nn\",\"params\": {\"did\": \"1234\",\"key\": \"1234\",\"msgid\": \"test1234\"},\"request\": {\"frameworks\": {\"name\": \"NCERT01\",\"description\": \"NCERT framework of Karnatka\",\"code\": \"org.ekstep.framework.create\",\"owner\": \"channelKA\"}}}";

	private static String updateFrameworkValidJson = "{\"id\": \"ekstep.framework.update\",\"ver\": \"3.0\",\"ts\": \"YYYY-MM-DDThh:mm:ssZ+/-nn.nn\",\"params\": {\"did\": \"1234\",\"key\": \"1234\",\"msgid\": \"test1234\"},\"request\": {\"framework\": {\"versionKey\": \"1511787372693\",\"description\": \" framework description\",\"categories\": [{\"identifier\": \"do_11238579307347148811\",\"name\": \"cat3\"}]}}}";

	private static final String updateFrameworkInvalidCIJson = "{\"id\": \"ekstep.framework.update\",\"ver\": \"3.0\",\"ts\": \"YYYY-MM-DDThh:mm:ssZ+/-nn.nn\",\"params\": {\"did\": \"1234\",\"key\": \"1234\",\"msgid\": \"test1234\"},\"request\": {\"framework\": {\"versionKey\": \"1511787372693\",\"description\": \" framework description\",\"categories\": [{\"identifier\": \"do_112385793073471488\",\"name\": \"cat3\"}]}}}";

	private static final String listFrameworkValidJson = "{\"id\": \"ekstep.framework.list\",\"ver\": \"3.0\",\"ts\": \"YYYY-MM-DDThh:mm:ssZ+/-nn.nn\",\"params\": {\"did\": \"1234\",\"key\": \"1234\",\"msgid\": \"test1234\"},\"request\": { \"search\": {\"status\":\"\"}}}";

	private static final String listFrameworkInvalidJson = "{\"id\": \"ekstep.framework.list\",\"ver\": \"3.0\",\"ts\": \"YYYY-MM-DDThh:mm:ssZ+/-nn.nn\",\"params\": {\"did\": \"1234\",\"key\": \"1234\",\"msgid\": \"test1234\"},\"request\": {}}";

	@BeforeClass
	public static void setup() throws Exception {
		loadDefinition("definitions/channel_definition.json", "definitions/framework_definition.json", "definitions/categoryInstance_definition.json");
		createChannel();
		createFramework();
		createCategoryInstance();
	}

	@Before
	public void init() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}

	private static void createFramework() {

		try {
			Map<String, Object> requestMap = mapper.readValue(createFrameworkReq,
					new TypeReference<Map<String, Object>>() {
					});
			requestMap.put("owner", channelId);
			Response resp = frameworkManager.createFramework(requestMap);
			frameworkId = (String) resp.getResult().get("node_id");
		} catch (Exception e) {
			System.out.println("Exception Occured while creating Framework :" + e.getMessage());
			e.printStackTrace();
		}

	}

	private static void createCategoryInstance() {
		try {
			Map<String, Object> requestMap = mapper.readValue(createCategoryInstanceReq,
					new TypeReference<Map<String, Object>>() {
					});

			Response resp = categoryInstManager.createCategoryInstance(frameworkId, requestMap);
			categoryInstanceId = (String) resp.getResult().get("node_id");
			System.out.println("Category Instance: " + categoryInstanceId);
		} catch (Exception e) {
			System.out.println("Exception Occured while creating Category Instance :" + e.getMessage());
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
			System.out.println("Channel Id: " + channelId);
		} catch (Exception e) {
			System.out.println("Exception Occured while creating Channel :" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	//Framework Create API -- Start
	
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
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelKA")
				.content(createFrameworkValidJson));
		Response resp = mapper.readValue(actions.andReturn().getResponse().getContentAsString(),
				new TypeReference<Response>() {
				});
		frameworkId = (String) resp.getResult().get("node_id");
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
		String path = basePath + "/creat"; // Invalid url
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
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.content(createFrameworkInvalidJson));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}
	
	//Framework Create API -- End
	

	// Framework Read API -- Start

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
	public void mockTestFramework_04() throws Exception {
		String path = basePath + "/read/" + frameworkId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
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
		String path = basePath + "/rea/" + frameworkId;
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
		String path = basePath + "/read/" + frameworkId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	// Framework Read API -- End
	

	// Update Framework API -- Start

	/*
	 * Scenario 7 : update Framework with valid url, valid request body and
	 * valid framework identifier.
	 * 
	 * Given: Valid url, Valid request body and Valid framework identifier When:
	 * Framework update API hits. Then: 200 - OK, Framework partial update done.
	 * 
	 */
	
	@Test
	public void mockTestFramework_07() throws Exception {
		String path = basePath + "/update/" + frameworkId;
		String updateFrameworkValidJson = "{\"id\": \"ekstep.framework.update\",\"ver\": \"3.0\",\"ts\": \"YYYY-MM-DDThh:mm:ssZ+/-nn.nn\",\"params\": {\"did\": \"1234\",\"key\": \"1234\",\"msgid\": \"test1234\"},\"request\": {\"framework\": {\"versionKey\": \"1511787372693\",\"description\": \" framework description\",\"categories\": [{\"identifier\": \""
				+ categoryInstanceId + "\",\"name\": \"cat3\"}]}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelKA").content(updateFrameworkValidJson));
		System.out.println("Response 07: " + actions.andReturn().getResponse().getContentAsString());
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 8 : update Framework with Invalid url, valid request body and
	 * valid framework identifier.
	 * 
	 * Given: Valid url, Valid request body and Valid framework identifier When:
	 * Framework update API hits. Then: 404 , Invalid Request.
	 * 
	 */
	@Test
	public void mockTestFramework_08() throws Exception {
		String path = basePath + "/updat/" + frameworkId;
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
		String path = basePath + "/update/" + frameworkId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.content(updateFrameworkValidJson));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 *
	 * Scenario 10 : update Framework with valid url, valid request body and
	 * valid framework identifier but invalid owner( Channel Id in Header will
	 * not match with owner).
	 * 
	 * Given: Valid url, Valid request body and Valid framework identifier,
	 * Invalid Header (Channel Id in Header will not match with owner) 
	 * When: Framework update API hits. 
	 * Then: 400 , Invalid Request. Owner Information Not Matched. - CLIENT_ERROR
	 * 
	 */
	@Test
	public void mockTestFramework_10() throws Exception {
		String path = basePath + "/update/" + frameworkId;
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
		String path = basePath + "/update/" + "test11111";
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
		String path = basePath + "/update/" + frameworkId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelKA").content(updateFrameworkInvalidCIJson));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	// Update Framework API -- End
	

	// Framework List API -- Start

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
		String path = basePath + "/list";
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
		String path = basePath + "/lis";
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
		String path = basePath + "/list";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.content(listFrameworkInvalidJson));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	// Framework List API -- End

	// Framework Retire API -- Start

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
		String path = basePath + "/retire/" + frameworkId;
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
		String path = basePath + "/retire/" + "test1234";
		actions = mockMvc.perform(
				MockMvcRequestBuilders.delete(path).header("user-id", "gauraw").header("X-Channel-Id", "channelKA"));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * //check
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
		String path = basePath + "/retire" + frameworkId;
		actions = mockMvc.perform(
				MockMvcRequestBuilders.delete(path).header("user-id", "gauraw").header("X-Channel-Id", "channelKA"));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	// Framework Retire API -- End
	
	
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
		String path = basePath + "/create";
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
		String path = basePath + "/create";
		actions = mockMvc.perform(
				MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "test").content(createFrameworkValidJson));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}
}