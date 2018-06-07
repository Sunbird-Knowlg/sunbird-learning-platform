package org.ekstep.dialcode.controller.test;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.cassandra.CassandraTestSetup;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.dialcode.mgr.impl.DialCodeManagerImpl;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Test Cases for DIAL Code.
 * 
 * @author gauraw
 *
 */
@Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class DialCodeV3ControllerTest extends CassandraTestSetup {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private static DialCodeManagerImpl dialCodeMgr;

	MockMvc mockMvc;
	private ResultActions actions;
	private static String dialCode = "";
	private static String publisherId = "";
	private static String DIALCODE_INDEX = "testdialcode";
	private static String DIALCODE_INDEX_TYPE = "dc";
	private static final String basePath = "/v3/dialcode";
	private static ObjectMapper mapper = new ObjectMapper();
	static ElasticSearchUtil elasticSearchUtil = new ElasticSearchUtil();

	private static String cassandraScript_1 = "CREATE KEYSPACE IF NOT EXISTS dialcode_store_test WITH replication = {'class': 'SimpleStrategy','replication_factor': '1'};";
	private static String cassandraScript_2 = "CREATE TABLE IF NOT EXISTS dialcode_store_test.system_config_test (prop_key text,prop_value text,primary key(prop_key));";
	private static String cassandraScript_3 = "CREATE TABLE IF NOT EXISTS dialcode_store_test.dial_code_test (identifier text,dialcode_index double,publisher text,channel text,batchCode text,metadata text,status text,generated_on text,published_on text, primary key(identifier));";
	private static String cassandraScript_4 = "CREATE TABLE IF NOT EXISTS dialcode_store_test.publisher (identifier text,name text,channel text,created_on text,updated_on text,primary key(identifier));";
	private static String cassandraScript_5 = "INSERT INTO dialcode_store_test.system_config_test(prop_key,prop_value) values('dialcode_max_index','1');";

	private static String generateDialCodeReq_1 = "{\"request\": {\"dialcodes\": {\"count\":2,\"publisher\": \"mock_pub01\",\"batchCode\":\"ka_math_std1\"}}}";

	private static String generateDialCodeReqInvalidPublisher = "{\"request\": {\"dialcodes\": {\"count\":2,\"publisher\": \"\",\"batchCode\":\"ap_match_std2\"}}}";
	private static String generateDialCodeReqInvalidCount = "{\"request\": {\"dialcodes\": {\"count\":\"\",\"publisher\": \"testPub1isherInvalidCount\",\"batchCode\":\"ka_math_std2\"}}}";

	@BeforeClass
	public static void setup() throws Exception {
		executeScript(cassandraScript_1, cassandraScript_2, cassandraScript_3, cassandraScript_4, cassandraScript_5);
		if (StringUtils.isBlank(publisherId))
			createPublisher();
		if (StringUtils.isBlank(dialCode))
			generateDIALCode();
		createDialCodeIndex();

	}

	@AfterClass
	public static void finish() throws IOException, InterruptedException, ExecutionException {
		ElasticSearchUtil.deleteIndex(DIALCODE_INDEX, "default");
	}

	@Before
	public void init() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
		if (StringUtils.isBlank(publisherId))
			createPublisher();
		if (StringUtils.isBlank(dialCode))
			generateDIALCode();
	}

	private static void generateDIALCode() throws Exception {
		String dialCodeGenReq = "{\"count\":1,\"publisher\": \"mock_pub01\",\"batchCode\":\"test_math_std1\"}";
		String channelId = "channelTest";
		Map<String, Object> requestMap = mapper.readValue(dialCodeGenReq, new TypeReference<Map<String, Object>>() {
		});
		Response resp = dialCodeMgr.generateDialCode(requestMap, channelId);
		@SuppressWarnings("unchecked")
		Collection<String> obj = (Collection) resp.getResult().get("dialcodes");
		for (String s : obj) {
			dialCode = s;
		}
	}

	private static void createPublisher() throws Exception {
		String createPublisherReq = "{\"identifier\":\"mock_pub01\",\"name\": \"Mock Publisher 1\"}";
		String channelId = "channelTest";
		Map<String, Object> requestMap = mapper.readValue(createPublisherReq, new TypeReference<Map<String, Object>>() {
		});
		Response resp = dialCodeMgr.createPublisher(requestMap, channelId);
		publisherId = (String) resp.get("identifier");
	}

	/*
	 * Scenario 1 : Generate Dial Code with valid url and valid request body.
	 * 
	 * Given: Valid url and valid request body. When: Generate DIAL Code API
	 * hits. Then: 200 - OK, DIAL Code will be generated and returned as
	 * Response.
	 * 
	 */
	@Test
	public void testDialCode_01() throws Exception {
		String path = basePath + "/generate";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(generateDialCodeReq_1));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 2 : Generate Dial Code with Invalid url and valid request body.
	 * 
	 * Given: Invalid url and valid request body. When: Generate DIAL Code API
	 * hits. Then: 404 - Invalid request path
	 * 
	 */
	@Test
	public void testDialCode_02() throws Exception {
		String path = basePath + "/generat";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(generateDialCodeReq_1));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 3 : Generate Dial Code with valid url, valid request body,
	 * Invalid Publisher (No Publisher).
	 * 
	 * Given: Valid url,valid request body and Invalid Publisher (No Publisher).
	 * When: Generate DIAL Code API hits. Then: 400 - Client Error . Error
	 * Message : "Publisher is Manadatory"
	 * 
	 */
	@Test
	public void testDialCode_03() throws Exception {
		String path = basePath + "/generate";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(generateDialCodeReqInvalidPublisher));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 4 : Generate Dial Code with valid url,valid request body and
	 * Invalid Count.
	 * 
	 * Given: Valid url and valid request body. When: Generate DIAL Code API
	 * hits. Then: 400 - Client Error. Invalid Count
	 * 
	 */
	@Test
	public void testDialCode_04() throws Exception {
		String path = basePath + "/generate";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(generateDialCodeReqInvalidCount));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 5 : Generate Dial Code with valid url,Invalid request body.
	 * 
	 * Given: Valid url and Invalid request body. When: Generate DIAL Code API
	 * hits. Then: 400 - Client Error. Invalid Request
	 * 
	 */
	@Test
	public void testDialCode_05() throws Exception {
		String path = basePath + "/generate";
		String req = "{\"request\": {}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 6 : Read Dial Code with valid url and valid identifier.
	 * 
	 * Given: Valid url and valid identifier. When: Read DIAL Code API hits.
	 * Then: 200 - OK . DIAL Code details will be returned as Response.
	 * 
	 */
	@Test
	public void testDialCode_06() throws Exception {
		String path = basePath + "/read/" + dialCode;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 7 : Read Dial Code with valid url and Invalid identifier.
	 * 
	 * Given: Valid url and Invalid identifier. When: Read DIAL Code API hits.
	 * Then: 404 - ResurceNotFoundException . Client Error
	 * 
	 */
	@Test
	public void testDialCode_07() throws Exception {
		String path = basePath + "/read/" + "ABC11111111";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 8 : Read Dial Code with Invalid url and valid identifier.
	 * 
	 * Given: Invalid url and valid identifier. When: Read DIAL Code API hits.
	 * Then: 404 - Invalid Request
	 * 
	 */
	@Test
	public void testDialCode_08() throws Exception {
		String path = basePath + "/rea/" + dialCode;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 09 : Update Dial Code with Valid url, valid request body and
	 * valid identifier.
	 * 
	 * Given: valid url,valid request body and valid identifier. When: Update
	 * DIAL Code API hits. Then: 200 - OK
	 * 
	 */

	@Test
	public void testDialCode_09() throws Exception {
		String path = basePath + "/update/" + dialCode;
		String req = "{\"request\": {\"dialcode\": {\"publisher\": \"testPublisheUpdated\",\"metadata\": {\"class\":\"std2\",\"subject\":\"Math\",\"board\":\"AP CBSE\"}}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 10 : Update Dial Code with Invalid url, valid request body and
	 * valid identifier.
	 * 
	 * Given: Invalid url,valid request body and valid identifier. When: Update
	 * DIAL Code API hits. Then: 404 - Invalid Request
	 * 
	 */

	@Test
	public void testDialCode_10() throws Exception {
		String path = basePath + "/updat/" + dialCode;
		String req = "{\"request\": {\"dialcode\": {\"publisher\": \"testPublisheUpdated\",\"metadata\": {\"class\":\"std2\",\"subject\":\"Math\",\"board\":\"AP CBSE\"}}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 11 : Update Dial Code with Valid url, valid request body and
	 * Invalid identifier.
	 * 
	 * Given: Valid url,valid request body and Invalid identifier. When: Update
	 * DIAL Code API hits. Then: 404 - Resource Not Found
	 * 
	 */

	@Test
	public void testDialCode_11() throws Exception {
		String path = basePath + "/update/" + "ABCTEST";
		String req = "{\"request\": {\"dialcode\": {\"publisher\": \"testPublisheUpdated\",\"metadata\": {\"class\":\"std2\",\"subject\":\"Math\",\"board\":\"AP CBSE\"}}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 12 : Update Dial Code with Valid url, Invalid request body and
	 * valid identifier.
	 * 
	 * Given: Valid url,Invalid request body and valid identifier. When: Update
	 * DIAL Code API hits. Then: 400 - Client Error. Invalid Request
	 * 
	 */

	@Test
	public void testDialCode_12() throws Exception {
		String path = basePath + "/update/" + dialCode;
		String req = "{\"request\": {\"dialcodess\": {\"publisher\": \"testPublisheUpdated\",\"metadata\": {\"class\":\"std2\",\"subject\":\"Math\",\"board\":\"AP CBSE\"}}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 13 : Publish Dial Code with valid url and valid identifier.
	 * 
	 * Given: valid url and valid identifier. When: Publish DIAL Code API hits.
	 * Then: 200 - OK
	 * 
	 */
	@Test
	public void testDialCode_13() throws Exception {
		String path = basePath + "/publish/" + dialCode;
		String req = "{}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").header("user-id", "test").content(req));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 14: Publish Dial Code with Invalid url and valid identifier.
	 * 
	 * Given: Invalid url and valid identifier. When: Publish DIAL Code API
	 * hits. Then: 404 - Invalid Request
	 * 
	 */
	@Test
	public void testDialCode_14() throws Exception {
		String path = basePath + "/publis/" + dialCode;
		String req = "{}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").header("user-id", "test").content(req));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 15 : Publish Dial Code with Valid url and Invalid identifier.
	 * 
	 * Given: valid url and Invalid identifier. When: Publish DIAL Code API
	 * hits. Then: 404 - Resource Not Found
	 * 
	 */
	@Test
	public void testDialCode_15() throws Exception {
		String path = basePath + "/publish/" + "TEST1111";
		String req = "{}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").header("user-id", "test").content(req));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 16 : List Dial Code with Valid url and Valid Request Body
	 * 
	 * Given: valid url and valid request body When: List DIAL Code API hits.
	 * Then: 200 - OK
	 * 
	 */
	// @Ignore
	@Test
	public void testDialCode_16() throws Exception {
		String path = basePath + "/list/";
		String req = "{\"request\": {\"search\": {\"publisher\":\"mock_pub01\",\"status\":\"Draft\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 17 : List Dial Code with Invalid url and Valid Request Body
	 * 
	 * Given: Invalid url and valid request body When: List DIAL Code API hits.
	 * Then: 404 - Invalid Request
	 * 
	 */
	@Test
	public void testDialCode_17() throws Exception {
		String path = basePath + "/lis/";
		String req = "{\"request\": {\"search\": {\"status\":\"Draft\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 18 : List Dial Code with valid url and Invalid Request Body
	 * 
	 * Given: Valid url and Invalid request body When: List DIAL Code API hits.
	 * Then: 400 - Invalid Request
	 * 
	 */
	@Test
	public void testDialCode_18() throws Exception {
		String path = basePath + "/list/";
		String req = "{\"request\": {\"searchs\": {\"status\":\"Draft\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 19 : Create Publisher with valid url and Valid Request Body
	 * 
	 * Given: Valid url and Valid request body When: Create Publisher API hits.
	 * Then: 200 - OK (If Publisher doesn't exist in the system)
	 * 
	 */
	@Test
	public void testDialCode_19() throws Exception {
		String path = basePath + "/publisher/create";
		String req = "{\"request\":{\"publisher\": {\"identifier\":\"publisher2\",\"name\": \"PUBLISHER2\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 20 : Create Publisher with valid url and Invalid Request Body
	 * but the publisher who already exist in the system
	 * 
	 * Given: Valid url and Valid request body When: Create Publisher API hits.
	 * Then: 400 - Client Error
	 * 
	 */
	@Test
	public void testDialCode_20() throws Exception {
		String path = basePath + "/publisher/create";
		String req = "{\"request\":{\"publisher\": {\"identifier\":\"mock_pub01\",\"name\": \"PUBLISHER2\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 21 : Read Publisher with valid url and valid identifier.
	 * 
	 * Given: Valid url and Valid identifier. When: Read Publisher API hits.
	 * Then: 200 - OK
	 * 
	 */
	@Test
	public void testDialCode_21() throws Exception {
		String path = basePath + "/publisher/read/" + publisherId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 22 : Read Publisher with valid url and Invalid identifier.
	 * 
	 * Given: Valid url and Invalid identifier. When: Read Publisher API hits.
	 * Then: 400 - CLIENT ERROR
	 * 
	 */
	@Test
	public void testDialCode_22() throws Exception {
		String path = basePath + "/publisher/read/" + "abc123";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 23 : Update Pubisher with valid url and valid request body.
	 * 
	 * Given: Valid url and Valid Request Body. When: Update Publisher API hits.
	 * Then: 200 - OK
	 * 
	 */
	@Test
	public void testDialCode_23() throws Exception {
		String path = basePath + "/publisher/update/" + publisherId;
		String req = "{\"request\":{\"publisher\": {\"name\": \"PUBLISHER001\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 24 : Update Pubisher with valid url and valid request body and
	 * Invalid Identifier.
	 * 
	 * Given: Valid url and Valid Request Body and Invalid Identifier. When:
	 * Update Publisher API hits. Then: 400 - CLIENT_ERROR
	 * 
	 */
	@Test
	public void testDialCode_24() throws Exception {
		String path = basePath + "/publisher/update/" + "test123";
		String req = "{\"request\":{\"publisher\": {\"name\": \"PUBLISHER001\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 25 : Update Pubisher with valid url and valid request body and
	 * valid Identifier, different channel Id.
	 * 
	 * Given: Valid url and Valid Request Body and valid Identifier, Invalid
	 * Channel Id. When: Update Publisher API hits. Then: 400 - CLIENT_ERROR
	 * 
	 */
	// TODO: Invalid Test Case. There is no check for Channel Id while update.
	@Ignore
	@Test
	public void testDialCode_25() throws Exception {
		String path = basePath + "/publisher/update/" + publisherId;
		String req = "{\"request\":{\"publisher\": {\"name\": \"PUBLISHER001\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channel").content(req));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 26 : Create Publisher with Invalid url and Valid Request Body
	 * 
	 * Given: Invalid url and Valid request body When: Create Publisher API
	 * hits. Then: 404 - Invalid request path
	 * 
	 */
	@Test
	public void testDialCode_26() throws Exception {
		String path = basePath + "/publisher/creat";
		String req = "{\"request\":{\"publisher\": {\"identifier\":\"publisher2\",\"name\": \"PUBLISHER2\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 27 : Create Publisher with valid url and Invalid Request Body
	 * (Identifier field not present)
	 * 
	 * Given: Valid url and Valid request body When: Create Publisher API hits.
	 * Then: 400 - Client Error
	 * 
	 */
	@Test
	public void testDialCode_27() throws Exception {
		String path = basePath + "/publisher/create";
		String req = "{\"request\":{\"publisher\": {\"identifier\":\"\",\"name\": \"PUBLISHER2\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 28 : Create Publisher with valid url and Invalid Request Body
	 * 
	 * Given: Valid url and Valid request body When: Create Publisher API hits.
	 * Then: 400 - Client Error
	 * 
	 */
	@Test
	public void testDialCode_28() throws Exception {
		String path = basePath + "/publisher/create";
		String req = "{\"request\":{}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 29 : Read Publisher with Invalid url and valid identifier.
	 * 
	 * Given: Invalid url and valid identifier. When: Read Publisher API hits.
	 * Then: 404 - Invalid request path
	 * 
	 */
	@Test
	public void testDialCode_29() throws Exception {
		String path = basePath + "/publisher/rea/" + "abc123";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * //TODO: Write one new test case Scenario 30 : Read Publisher with Invalid
	 * url and valid identifier.
	 * 
	 * Given: Invalid url and valid identifier. When: Read Publisher API hits.
	 * Then: 404 - Invalid request path
	 * 
	 */
	@Ignore
	@Test
	public void testDialCode_30() throws Exception {
		String path = basePath + "/publisher/rea/" + "abc123";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 31 : Generate DIAL Code with valid URI and invalid request.
	 * 
	 * Given: valid URI and Invalid Request Body. When: Generate DIAL Code API
	 * hits. Then: 400 - CLIENT_ERROR
	 * 
	 */
	@Test
	public void testDialCode_31() throws Exception {
		String path = basePath + "/generate";
		String req = "{\"request\":{}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 32 : Generate DIAL Code with valid URI and Valid Request (DIAL
	 * Code count is more than max allowed count).
	 * 
	 * Given: valid URI and valid Request Body. When: Generate DIAL Code API
	 * hits. Then: 207 - PARTIAL_SUCCESS
	 * 
	 */
	@Test
	public void testDialCode_32() throws Exception {
		String path = basePath + "/generate";
		String req = "{\"request\": {\"dialcodes\": {\"count\":2000,\"publisher\": \"mock_pub01\",\"batchCode\":\"ka_math_std1\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		Assert.assertEquals(207, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 33 : Search DIAL Code with valid uri and valid request body
	 * 
	 * Given: valid URI and valid Request Body. When: Search DIAL Code API hits.
	 * Then: 200 - OK
	 * 
	 */
	@Test
	public void testDialCode_33() throws Exception {
		createDialCodeIndex();
		Thread.sleep(2000);
		String path = basePath + "/search";
		String req = "{\"request\": {\"search\": {\"identifier\": \"" + dialCode + "\",\"limit\":10,\"offset\":0}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		ObjectMapper objectMapper = new ObjectMapper();
		Response response = objectMapper.readValue(actions.andReturn().getResponse().getContentAsString(), Response.class);
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		Assert.assertEquals(1, (int) response.getResult().get("count"));
	}

	/*
	 * Scenario 34 : Sync DIAL Code with valid uri and valid request body
	 * 
	 * Given: valid URI and valid Request Body. When: Sync DIAL Code API hits.
	 * Then: 200 - OK
	 * 
	 */
	@Test
	public void testDialCode_34() throws Exception {
		String path = basePath + "/sync" + "?identifiers=" + dialCode;
		String req = "{\"request\":{\"sync\":{\"publisher\":\"mock_pub01\",\"batchCode\":\"ka_math_std1\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 35 : Sync DIAL Code with valid uri and Invalid request body
	 * 
	 * Given: valid URI and Invalid Request Body. When: Sync DIAL Code API hits.
	 * Then: 400 - CLIENT_ERROR
	 * 
	 */
	@Test
	public void testDialCode_35() throws Exception {

		String path = basePath + "/sync";
		String req = "{\"request\":{\"sync\":{\"channel\":\"test\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Scenario 36 : Search DIAL Code with valid uri and Invalid request body
	 * 
	 * Given: valid URI and Invalid Request Body. When: Search DIAL Code API
	 * hits. Then: 400 - CLIENT_ERROR
	 * 
	 */
	@Test
	public void testDialCode_36() throws Exception {
		String path = basePath + "/search";
		String req = "{\"request\": {\"search\": {\"identifier\": \"" + dialCode + "\",\"limit\":\"ABC\"}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "channelTest").content(req));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	private static void createDialCodeIndex() throws IOException {
		CompositeSearchConstants.DIAL_CODE_INDEX = DIALCODE_INDEX;
		ElasticSearchUtil.registerESClient("default", Platform.config.getString("dialcode.es_conn_info"));
		String settings = "{\"analysis\": {       \"analyzer\": {         \"dc_index_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"lowercase\",             \"mynGram\"           ]         },         \"dc_search_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"standard\",             \"lowercase\"           ]         },         \"keylower\": {           \"tokenizer\": \"keyword\",           \"filter\": \"lowercase\"         }       },       \"filter\": {         \"mynGram\": {           \"type\": \"nGram\",           \"min_gram\": 1,           \"max_gram\": 20,           \"token_chars\": [             \"letter\",             \"digit\",             \"whitespace\",             \"punctuation\",             \"symbol\"           ]         }       }     }   }";
		String mappings = "{\"dynamic_templates\":[{\"longs\":{\"match_mapping_type\":\"long\",\"mapping\":{\"type\":\"long\",\"fields\":{\"raw\":{\"type\":\"long\"}}}}},{\"booleans\":{\"match_mapping_type\":\"boolean\",\"mapping\":{\"type\":\"boolean\",\"fields\":{\"raw\":{\"type\":\"boolean\"}}}}},{\"doubles\":{\"match_mapping_type\":\"double\",\"mapping\":{\"type\":\"double\",\"fields\":{\"raw\":{\"type\":\"double\"}}}}},{\"dates\":{\"match_mapping_type\":\"date\",\"mapping\":{\"type\":\"date\",\"fields\":{\"raw\":{\"type\":\"date\"}}}}},{\"strings\":{\"match_mapping_type\":\"string\",\"mapping\":{\"type\":\"text\",\"copy_to\":\"all_fields\",\"analyzer\":\"dc_index_analyzer\",\"search_analyzer\":\"dc_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\"}}}}}],\"properties\":{\"all_fields\":{\"type\":\"text\",\"analyzer\":\"dc_index_analyzer\",\"search_analyzer\":\"dc_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\"}}}}}";
		ElasticSearchUtil.addIndex(CompositeSearchConstants.DIAL_CODE_INDEX,
				CompositeSearchConstants.DIAL_CODE_INDEX_TYPE, settings, mappings, "default");

		populateData();
	}

	private static void populateData() throws JsonProcessingException, IOException {
		Map<String, Object> indexDocument = new HashMap<String, Object>();
		indexDocument.put("dialcode_index", 1);
		indexDocument.put("identifier", dialCode);
		indexDocument.put("channel", "channelTest");
		indexDocument.put("batchcode", "test_math_std1");
		indexDocument.put("publisher", "mock_pub01");
		indexDocument.put("status", "Draft");
		indexDocument.put("generated_on", "2018-01-30T16:50:40.562");
		indexDocument.put("index", "true");
		indexDocument.put("operationType", "CREATE");
		indexDocument.put("nodeType", "EXTERNAL");
		indexDocument.put("userId", "ANONYMOUS");
		indexDocument.put("createdOn", "2018-01-30T16:50:40.593+0530");
		indexDocument.put("userId", "ANONYMOUS");
		indexDocument.put("objectType", "DialCode");

		ElasticSearchUtil.addDocumentWithId(DIALCODE_INDEX, CompositeSearchConstants.DIAL_CODE_INDEX_TYPE, dialCode,
				mapper.writeValueAsString(indexDocument), "default");
	}

}