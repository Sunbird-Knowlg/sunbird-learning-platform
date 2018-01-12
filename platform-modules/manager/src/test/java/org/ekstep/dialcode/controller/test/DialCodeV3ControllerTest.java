package org.ekstep.dialcode.controller.test;

import java.util.Collection;
import java.util.Map;

import org.ekstep.common.dto.Response;
import org.ekstep.dialcode.mgr.impl.DialCodeManagerImpl;
import org.ekstep.dialcode.test.common.TestSetupUtil;
import org.junit.AfterClass;
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

/**
 * 
 * Mock Test Cases for DIAL Code.
 * 
 * @author gauraw
 *
 */
@Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class DialCodeV3ControllerTest extends TestSetupUtil{

	@Autowired
	private WebApplicationContext context;
	
	MockMvc mockMvc;

	private ResultActions actions;
	private static final String basePath = "/v3/dialcode";
	private static ObjectMapper mapper = new ObjectMapper();
	private static DialCodeManagerImpl dialCodeMgr = new DialCodeManagerImpl();
	
	private static String dialCode;

	private static  String generateDialCodeReq_1 = "{\"request\": {\"dialcodes\": {\"count\":2,\"publisher\": \"testPublisher\",\"batchCode\":\"ka_math_std1\"}}}";
	private static  String generateDialCodeReq_2 = "{\"request\": {\"dialcodes\": {\"count\":200,\"publisher\": \"testPub11\",\"batchCode\":\"ap_match_std2\"}}}";
	
	private static  String generateDialCodeReqInvalidPublisher = "{\"request\": {\"dialcodes\": {\"count\":2,\"publisher\": \"\",\"batchCode\":\"ap_match_std2\"}}}";
	private static  String generateDialCodeReqInvalidCount = "{\"request\": {\"dialcodes\": {\"count\":\"\",\"publisher\": \"testPub1isherInvalidCount\",\"batchCode\":\"ka_math_std2\"}}}";

	
	@BeforeClass
	public static void setup() throws Exception {
		generateDIALCode();
	}
	
	@AfterClass
	public static void finish(){
		
	}

	@Before
	public void init() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void generateDIALCode() throws Exception{
		String dialCodeGenReq="{\"count\":1,\"publisher\": \"testPublisher\",\"batchCode\":\"test_math_std1\"}";
		String channelId="channelTest";
		Map<String, Object> requestMap = mapper.readValue(dialCodeGenReq,
				new TypeReference<Map<String, Object>>() {});
		Response resp=dialCodeMgr.generateDialCode(requestMap, channelId);
		 Collection<String> obj= (Collection)resp.getResult().get("dialcodes");
		 for (String s:obj) {
			 dialCode=s;
		}
	}
	
	/*
	 * Scenario 1 : Generate Dial Code with valid url and valid request body.
	 * 
	 * Given: Valid url and valid request body.
	 * When: Generate DIAL Code API hits.
	 * Then: 200 - OK, DIAL Code will be generated and returned as Response.
	 * 
	 */
	@Test
	public void testDialCode_01() throws Exception {
		String path = basePath + "/generate";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelTest")
				.content(generateDialCodeReq_1));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}
	
	/*
	 * Scenario 2 : Generate Dial Code with Invalid url and valid request body.
	 * 
	 * Given: Invalid url and valid request body.
	 * When: Generate DIAL Code API hits.
	 * Then: 404 - Invalid request path
	 * 
	 */
	@Test
	public void testDialCode_02() throws Exception {
		String path = basePath + "/generat";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelTest")
				.content(generateDialCodeReq_1));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}
	
	/*
	 * Scenario 3 : Generate Dial Code with valid url, valid request body, Invalid Publisher (No Publisher).
	 * 
	 * Given: Valid url,valid request body and Invalid Publisher (No Publisher).
	 * When: Generate DIAL Code API hits.
	 * Then:  400 - Client Error . Error Message : "Publisher is Manadatory"
	 * 
	 */
	@Test
	public void testDialCode_03() throws Exception {
		String path = basePath + "/generate";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelTest")
				.content(generateDialCodeReqInvalidPublisher));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}
	
	/*
	 * Scenario 4 : Generate Dial Code with valid url,valid request body and Invalid Count.
	 * 
	 * Given: Valid url and valid request body.
	 * When: Generate DIAL Code API hits.
	 * Then:  400 - Client Error. Invalid Count
	 * 
	 */
	@Test
	public void testDialCode_04() throws Exception {
		String path = basePath + "/generate";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelTest")
				.content(generateDialCodeReqInvalidCount));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}
	
	/*
	 * Scenario 5 : Generate Dial Code with valid url,Invalid request body.
	 * 
	 * Given: Valid url and Invalid request body.
	 * When: Generate DIAL Code API hits.
	 * Then:  400 - Client Error. Invalid Request
	 * 
	 */
	@Test
	public void testDialCode_05() throws Exception {
		String path = basePath + "/generate";
		String req="{\"request\": {}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelTest")
				.content(req));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}
	
	/*
	 * Scenario 6 : Read Dial Code with valid url and valid identifier.
	 * 
	 * Given: Valid url and valid identifier.
	 * When: Read DIAL Code API hits.
	 * Then:  200 - OK . DIAL Code details will be returned as Response.
	 * 
	 */
	@Test
	public void testDialCode_06() throws Exception {
		String path = basePath + "/read/"+dialCode;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path));
		System.out.println("Response:::::::::"+actions.andReturn().getResponse().getContentAsString());
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}
	
	/*
	 * Scenario 7 : Read Dial Code with valid url and Invalid identifier.
	 * 
	 * Given: Valid url and Invalid identifier.
	 * When: Read DIAL Code API hits.
	 * Then:  404 - ResurceNotFoundException . Client Error
	 * 
	 */
	@Test
	public void testDialCode_07() throws Exception {
		String path = basePath + "/read/"+"ABC11111111";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}
	
	/*
	 * Scenario 8 : Read Dial Code with Invalid url and valid identifier.
	 * 
	 * Given: Invalid url and valid identifier.
	 * When: Read DIAL Code API hits.
	 * Then:  404 - Invalid Request
	 * 
	 */
	@Test
	public void testDialCode_08() throws Exception {
		String path = basePath + "/rea/"+dialCode;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}
	
	/*
	 * Scenario 09 : Update Dial Code with Valid url, valid request body and valid identifier.
	 * 
	 * Given: valid url,valid request body and valid identifier.
	 * When: Update DIAL Code API hits.
	 * Then:  200 - OK
	 * 
	 */
	
	@Test
	public void testDialCode_09() throws Exception {
		String path = basePath + "/update/"+dialCode;
		String req="{\"request\": {\"dialcode\": {\"publisher\": \"testPublisheUpdated\",\"metadata\": {\"class\":\"std2\",\"subject\":\"Math\",\"board\":\"AP CBSE\"}}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelTest")
				.content(req));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}
	
	/*
	 * Scenario 10 : Update Dial Code with Invalid url, valid request body and valid identifier.
	 * 
	 * Given: Invalid url,valid request body and valid identifier.
	 * When: Update DIAL Code API hits.
	 * Then:  404 - Invalid Request
	 * 
	 */
	
	@Test
	public void testDialCode_10() throws Exception {
		String path = basePath + "/updat/"+dialCode;
		String req="{\"request\": {\"dialcode\": {\"publisher\": \"testPublisheUpdated\",\"metadata\": {\"class\":\"std2\",\"subject\":\"Math\",\"board\":\"AP CBSE\"}}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelTest")
				.content(req));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}
	
	/*
	 * Scenario 11 : Update Dial Code with Valid url, valid request body and Invalid identifier.
	 * 
	 * Given: Valid url,valid request body and Invalid identifier.
	 * When: Update DIAL Code API hits.
	 * Then:  404 - Resource Not Found
	 * 
	 */
	
	@Test
	public void testDialCode_11() throws Exception {
		String path = basePath + "/update/"+"ABCTEST";
		String req="{\"request\": {\"dialcode\": {\"publisher\": \"testPublisheUpdated\",\"metadata\": {\"class\":\"std2\",\"subject\":\"Math\",\"board\":\"AP CBSE\"}}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelTest")
				.content(req));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}
	
	/*
	 * Scenario 12 : Update Dial Code with Valid url, Invalid request body and valid identifier.
	 * 
	 * Given: Valid url,Invalid request body and valid identifier.
	 * When: Update DIAL Code API hits.
	 * Then:  400 - Client Error. Invalid Request
	 * 
	 */
	
	@Test
	public void testDialCode_12() throws Exception {
		String path = basePath + "/update/"+dialCode;
		String req="{\"request\": {\"dialcodess\": {\"publisher\": \"testPublisheUpdated\",\"metadata\": {\"class\":\"std2\",\"subject\":\"Math\",\"board\":\"AP CBSE\"}}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelTest")
				.content(req));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}
	
	
	
	
	/*
	 * Scenario 13 : Publish Dial Code with valid url and valid identifier.
	 * 
	 * Given: valid url and valid identifier.
	 * When: Publish DIAL Code API hits.
	 * Then:  200 - OK
	 * 
	 */
	@Test
	public void testDialCode_13() throws Exception {
		String path = basePath + "/publish/"+dialCode;
		String req="{}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelTest").header("user-id", "test")
				.content(req));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}
	
	/*
	 * Scenario 14: Publish Dial Code with Invalid url and valid identifier.
	 * 
	 * Given: Invalid url and valid identifier.
	 * When: Publish DIAL Code API hits.
	 * Then:  404 - Invalid Request
	 * 
	 */
	@Test
	public void testDialCode_14() throws Exception {
		String path = basePath + "/publis/"+dialCode;
		String req="{}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelTest").header("user-id", "test")
				.content(req));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}
	
	/*
	 * Scenario 15 : Publish Dial Code with Valid url and Invalid identifier.
	 * 
	 * Given: valid url and Invalid identifier.
	 * When: Publish DIAL Code API hits.
	 * Then:  404 - Resource Not Found
	 * 
	 */
	@Test
	public void testDialCode_15() throws Exception {
		String path = basePath + "/publish/"+"TEST1111";
		String req="{}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelTest").header("user-id", "test")
				.content(req));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}
	
	/*
	 * Scenario 16 : List Dial Code with Valid url and Valid Request Body
	 * 
	 * Given: valid url and valid request body
	 * When: List DIAL Code API hits.
	 * Then:  200 - OK
	 * 
	 */
	@Test
	public void testDialCode_16() throws Exception {
		String path = basePath + "/list/";
		String req="{\"request\": {\"search\": {\"status\":\"Draft\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelTest")
				.content(req));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}
	
	/*
	 * Scenario 17 : List Dial Code with Invalid url and Valid Request Body
	 * 
	 * Given: Invalid url and valid request body
	 * When: List DIAL Code API hits.
	 * Then:  404 - Invalid Request
	 * 
	 */
	@Test
	public void testDialCode_17() throws Exception {
		String path = basePath + "/lis/";
		String req="{\"request\": {\"search\": {\"status\":\"Draft\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelTest")
				.content(req));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}
	
	/*
	 * Scenario 18 : List Dial Code with valid url and Invalid Request Body
	 * 
	 * Given: Valid url and Invalid request body
	 * When: List DIAL Code API hits.
	 * Then:  400 - Invalid Request
	 * 
	 */
	@Ignore
	@Test
	public void testDialCode_18() throws Exception {
		String path = basePath + "/list/";
		String req="{\"request\": {\"searchs\": {\"status\":\"Draft\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelTest")
				.content(req));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}
	
	/*
	 * Scenario 19 : List Dial Code with valid url and Invalid Request Body (Invalid Criteria)
	 * 
	 * Given: Valid url and Invalid request body
	 * When: List DIAL Code API hits.
	 * Then:  200 - OK . Empty Result
	 * 
	 */
	@Ignore
	@Test
	public void testDialCode_19() throws Exception {
		String path = basePath + "/list/";
		String req="{\"request\": {\"search\": {\"status\":\"Test\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelTest")
				.content(req));
		System.out.println("Response:::::::::"+actions.andReturn().getResponse().getContentAsString());
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}
	
	
}