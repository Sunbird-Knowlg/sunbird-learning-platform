package org.ekstep.dialcode.mgr.impl.test;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.cassandra.CassandraTestSetup;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.dialcode.mgr.impl.DialCodeManagerImpl;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author gauraw
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class DialCodeManagerImplTest extends CassandraTestSetup {

	@Autowired
	private DialCodeManagerImpl dialCodeMgr;

	private static String dialCode = "";
	private static String publisherId = "";
	private static ObjectMapper mapper = new ObjectMapper();

	private static String cassandraScript_1 = "CREATE KEYSPACE IF NOT EXISTS dialcode_store_test WITH replication = {'class': 'SimpleStrategy','replication_factor': '1'};";
	private static String cassandraScript_2 = "CREATE TABLE IF NOT EXISTS dialcode_store_test.system_config_test (prop_key text,prop_value text,primary key(prop_key));";
	private static String cassandraScript_3 = "CREATE TABLE IF NOT EXISTS dialcode_store_test.dial_code_test (identifier text,dialcode_index double,publisher text,channel text,batchCode text,metadata text,status text,generated_on text,published_on text, primary key(identifier));";
	private static String cassandraScript_4 = "CREATE TABLE IF NOT EXISTS dialcode_store_test.publisher (identifier text,name text,channel text,created_on text,updated_on text,primary key(identifier));";
	private static String cassandraScript_5 = "INSERT INTO dialcode_store_test.system_config_test(prop_key,prop_value) values('dialcode_max_index','1');";

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void setup() throws Exception {
		executeScript(cassandraScript_1, cassandraScript_2, cassandraScript_3, cassandraScript_4, cassandraScript_5);
	}

	@AfterClass
	public static void finish() {

	}

	@Before
	public void init() throws Exception {

		if (StringUtils.isBlank(publisherId))
			createPublisher();
		if (StringUtils.isBlank(dialCode))
			generateDIALCode();

	}

	private void generateDIALCode() throws Exception {
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

	private void createPublisher() throws Exception {
		String createPublisherReq = "{\"identifier\":\"mock_pub01\",\"name\": \"Mock Publisher 1\"}";
		String channelId = "channelTest";
		Map<String, Object> requestMap = mapper.readValue(createPublisherReq, new TypeReference<Map<String, Object>>() {
		});
		Response resp = dialCodeMgr.createPublisher(requestMap, channelId);
		publisherId = (String) resp.get("identifier");
	}

	@Test
	public void dialCodeTest_01() throws Exception {
		String dialCodeGenReq = "{\"count\":1,\"publisher\": \"mock_pub01\"}";
		String channelId = "channelTest";
		Map<String, Object> requestMap = mapper.readValue(dialCodeGenReq, new TypeReference<Map<String, Object>>() {
		});
		Response response = dialCodeMgr.generateDialCode(requestMap, channelId);
		assertTrue(response.getResponseCode().toString().equals("OK"));
		assertTrue(response.getResponseCode().code() == 200);
	}

	@Test
	public void dialCodeTest_02() throws Exception {
		exception.expect(ClientException.class);
		String dialCodeGenReq = "{\"count\":1,\"publisher\": \"mock_pub\"}";
		String channelId = "channelTest";
		Map<String, Object> requestMap = mapper.readValue(dialCodeGenReq, new TypeReference<Map<String, Object>>() {
		});
		Response response = dialCodeMgr.generateDialCode(requestMap, channelId);
	}

	@Test
	public void dialCodeTest_03() throws Exception {
		exception.expect(ClientException.class);
		String dialCodeGenReq = "{\"count\":1,\"publisher\": \"mock_pub\"}";
		String channelId = "channelTest";
		Map<String, Object> requestMap = mapper.readValue(dialCodeGenReq, new TypeReference<Map<String, Object>>() {
		});
		Response response = dialCodeMgr.generateDialCode(requestMap, channelId);
	}

	@Test
	public void dialCodeTest_04() throws Exception {
		String dialCodeId = null;
		Response response = dialCodeMgr.readDialCode(dialCodeId);
		Assert.assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
	}

	// Publish Dial Code with Different Channel Id - CLIENT_ERROR
	@Test
	public void dialCodeTest_05() throws Exception {
		String channelId = "channelABC";
		Response response = dialCodeMgr.publishDialCode(dialCode, channelId);
		Assert.assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
	}

	// Publish Dial Code with Same Channel Id - 200 - OK
	@Test
	public void dialCodeTest_06() throws Exception {
		String channelId = "channelTest";
		Response response = dialCodeMgr.publishDialCode(dialCode, channelId);
		Assert.assertEquals("OK", response.getResponseCode().toString());
	}

	// Update Dial Code with Different Channel Id - CLIENT_ERROR
	@Test
	public void dialCodeTest_07() throws Exception {
		String dialCodeUpdateReq = "{\"dialcode\": {\"publisher\": \"testPublisheUpdated\",\"metadata\": {\"class\":\"std2\",\"subject\":\"Math\",\"board\":\"AP CBSE\"}}}";
		String channelId = "channelABC";
		Map<String, Object> requestMap = mapper.readValue(dialCodeUpdateReq, new TypeReference<Map<String, Object>>() {
		});
		Response response = dialCodeMgr.updateDialCode(dialCode, channelId, requestMap);
		Assert.assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
	}

	// Update Dial Code having Live Status - CLIENT_ERROR
	@Test
	public void dialCodeTest_08() throws Exception {
		String dialCodeUpdateReq = "{\"dialcode\": {\"publisher\": \"testPublisheUpdated\",\"metadata\": {\"class\":\"std2\",\"subject\":\"Math\",\"board\":\"AP CBSE\"}}}";
		String channelId = "channelTest";
		Map<String, Object> requestMap = mapper.readValue(dialCodeUpdateReq, new TypeReference<Map<String, Object>>() {
		});
		Response response = dialCodeMgr.updateDialCode(dialCode, channelId, requestMap);
		Assert.assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
	}

	// List Dial Code without Publisher - CLIENT_ERROR
	@Test
	public void dialCodeTest_09() throws Exception {
		String listReq = "{\"status\":\"Live\"}";
		String channelId = "channelTest";
		Map<String, Object> requestMap = mapper.readValue(listReq, new TypeReference<Map<String, Object>>() {
		});
		Response response = dialCodeMgr.listDialCode(channelId, requestMap);
		Assert.assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
	}

	// Search Dial Code with null map - CLIENT_ERROR
	@Test
	public void dialCodeTest_10() throws Exception {
		String channelId = "channelTest";
		Response response = dialCodeMgr.searchDialCode(channelId, null);
		Assert.assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
	}

	// Search Dial Code with Invalid limit - CLIENT_ERROR
	@Test
	public void dialCodeTest_11() throws Exception {
		exception.expect(ClientException.class);
		String searchReq = "{\"status\":\"Live\",\"limit\":\"abc\"}";
		String channelId = "channelTest";
		Map<String, Object> requestMap = mapper.readValue(searchReq, new TypeReference<Map<String, Object>>() {
		});
		Response response = dialCodeMgr.searchDialCode(channelId, requestMap);
	}

	// Sync Dial Code with null Request - CLIENT_ERROR
	@Test
	public void dialCodeTest_12() throws Exception {
		String channelId = "channelTest";
		Response response = dialCodeMgr.syncDialCode(channelId, null, null);
		Assert.assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
	}

	// Create Publisher without Name - CLIENT_ERROR
	@Test
	public void dialCodeTest_13() throws Exception {
		String createPubReq = "{\"identifier\":\"mock_pub01\"}";
		String channelId = "channelTest";
		Map<String, Object> requestMap = mapper.readValue(createPubReq, new TypeReference<Map<String, Object>>() {
		});
		Response response = dialCodeMgr.createPublisher(requestMap, channelId);
		Assert.assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
	}

	// Create Publisher with Invalid Name - CLIENT_ERROR
	@Test
	public void dialCodeTest_14() throws Exception {
		String createPubReq = "{\"identifier\":\"mock_pub01\",\"name\":\"\"}";
		String channelId = "channelTest";
		Map<String, Object> requestMap = mapper.readValue(createPubReq, new TypeReference<Map<String, Object>>() {
		});
		Response response = dialCodeMgr.createPublisher(requestMap, channelId);
		Assert.assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
	}

	// Read Publisher with Blank Id - CLIENT_ERROR
	@Test
	public void dialCodeTest_15() throws Exception {
		Response response = dialCodeMgr.readPublisher("");
		Assert.assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
	}

	// Update Publisher with null metadata - CLIENT_ERROR
	@Test
	public void dialCodeTest_16() throws Exception {
		Response response = dialCodeMgr.updatePublisher("ABC", "ABC", null);
		Assert.assertEquals("CLIENT_ERROR", response.getResponseCode().toString());
	}

	// Generate DIAL Code with Invalid Count - Client Exception
	@Test
	public void dialCodeTest_17() throws Exception {
		exception.expect(ClientException.class);
		String dialCodeGenReq = "{\"count\":\"ABC\",\"publisher\": \"mock_pub01\",\"batchCode\":\"test_math_std1\"}";
		String channelId = "channelTest";
		Map<String, Object> requestMap = mapper.readValue(dialCodeGenReq, new TypeReference<Map<String, Object>>() {
		});
		Response response = dialCodeMgr.generateDialCode(requestMap, channelId);
	}

	// Generate DIAL Code with Invalid Count (Integer but -ve number) -
	// CLIENT_ERROR
	@Test
	public void dialCodeTest_18() throws Exception {
		exception.expect(ClientException.class);
		String dialCodeGenReq = "{\"count\":-2,\"publisher\": \"mock_pub01\",\"batchCode\":\"test_math_std1\"}";
		String channelId = "channelTest";
		Map<String, Object> requestMap = mapper.readValue(dialCodeGenReq, new TypeReference<Map<String, Object>>() {
		});
		Response response = dialCodeMgr.generateDialCode(requestMap, channelId);
	}

}
