package org.ekstep.dialcode.mgr.impl.test;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.dialcode.mgr.impl.DialCodeManagerImpl;
import org.ekstep.dialcode.test.common.TestSetupUtil;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
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
@Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class DialCodeManagerImplTest extends TestSetupUtil {

	@Autowired
	private DialCodeManagerImpl dialCodeMgr;

	private static String dialCode = "";
	private static String publisherId = "";
	private static ObjectMapper mapper = new ObjectMapper();

	private static String cassandraScript_1 = "CREATE KEYSPACE IF NOT EXISTS dialcode_store_test WITH replication = {'class': 'SimpleStrategy','replication_factor': '1'};";
	private static String cassandraScript_2 = "CREATE TABLE IF NOT EXISTS dialcode_store_test.system_config_test (prop_key text,prop_value text,primary key(prop_key));";
	private static String cassandraScript_3 = "CREATE TABLE IF NOT EXISTS dialcode_store_test.dial_code_test (identifier text,dialcode_index double,publisher text,channel text,batchCode text,metadata text,status text,generated_on text,published_on text, primary key(identifier));";
	private static String cassandraScript_4 = "CREATE TABLE IF NOT EXISTS dialcode_store_test.publisher (identifier text,name text,channel text,created_on text,updated_on text,primary key(identifier));";

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void setup() throws Exception {
		executeScript(cassandraScript_1, cassandraScript_2, cassandraScript_3, cassandraScript_4);
		System.out.println("Cassandra is Ready.");
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
}
