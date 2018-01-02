package org.ekstep.dialcode.controller.test;

import java.util.Collection;
import java.util.Map;

import org.ekstep.common.dto.Response;
import org.ekstep.dialcode.mgr.IDialCodeManager;
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

	private static String cassandraScript_1="CREATE KEYSPACE IF NOT EXISTS dialcode_store_test WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};";
	private static String cassandraScript_2="CREATE TABLE IF NOT EXISTS dialcode_store_test.dial_code_test (identifier text,dialcode_index double,publisher text,channel text,batchCode text,metadata text,count int,status text,generated_on text,published_on text, primary key(identifier));";
	
	
	private static  String generateDialCodeReq_1 = "{\"request\": {\"dialcodes\": {\"count\":2,\"publisher\": \"testPublisher\",\"batchCode\":\"ka_math_std1\"}}}";
	private static  String generateDialCodeReq_2 = "{\"request\": {\"dialcodes\": {\"count\":200,\"publisher\": \"testPub11\",\"batchCode\":\"ap_match_std2\"}}}";
	
	private static  String generateDialCodeReqInvalidPublisher = "{\"request\": {\"dialcodes\": {\"count\":2,\"publisher\": \"\",\"batchCode\":\"ap_match_std2\"}}}";
	private static  String generateDialCodeReqInvalidCount = "{\"request\": {\"dialcodes\": {\"count\":\"\",\"publisher\": \"testPub1isherInvalidCount\",\"batchCode\":\"ka_math_std2\"}}}";

	
	@BeforeClass
	public static void setup() throws Exception {
		//loadDefinition("definitions/content_definition.json");
		setupEmbeddedCassandra(cassandraScript_1,cassandraScript_2);
		generateDIALCode();
	}
	
	@AfterClass
	public static void finish(){
		
	}

	@Before
	public void init() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}
	
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
}