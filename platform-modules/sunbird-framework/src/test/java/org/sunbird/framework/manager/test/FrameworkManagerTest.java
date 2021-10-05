package org.sunbird.framework.manager.test;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.framework.mgr.impl.ChannelManagerImpl;
import org.sunbird.framework.mgr.impl.FrameworkManagerImpl;
import org.sunbird.framework.test.common.TestParams;
import org.sunbird.graph.engine.common.GraphEngineTestSetup;
import org.sunbird.learning.router.LearningRequestRouterPool;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit Test Cases for Framework API.
 * 
 * @author gauraw
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FrameworkManagerTest extends GraphEngineTestSetup {

	private static String frameworkId;
	private static String channelId;

	private static ChannelManagerImpl channelManager = new ChannelManagerImpl();
	private static FrameworkManagerImpl frameworkManager = new FrameworkManagerImpl();

	private static ObjectMapper mapper = new ObjectMapper();

	private static final String createFrameworkReq = "{\"name\": \"NCERT01\",\"description\": \"NCERT framework of Karnatka\",\"code\": \"ka_ncert01\"}";
	private static final String createChannelReq = "{\"name\":\"channelKA\",\"description\":\"\",\"code\":\"channelKA\"}";
	private static final String createFrameworkReqJson = "{\"name\": \"KASB01\",\"description\": \"State Board framework of Karnatka\",\"code\": \"ka_sb01\"}";
	private static final String updateFrameworkJson = "{\"versionKey\": \"1511787372693\",\"description\": \" framework description\",\"categories\": [{\"identifier\": \"do_11238579307347148811\",\"name\": \"cat3\"}]}";

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void initTest() throws Exception {
		loadDefinition("definitions/channel_definition.json", "definitions/framework_definition.json",
				"definitions/categoryInstance_definition.json");
		LearningRequestRouterPool.init();
		createChannel();
		createFramework();
	}


	/**
	 * Create Framework with Valid request body, Valid Channel
	 * 
	 */
	@Test
	public void testFrameworkCreate() {

		try {
			Map<String, Object> requestMap = mapper.readValue(createFrameworkReqJson,
					new TypeReference<Map<String, Object>>() {
					});
			Response response = frameworkManager.createFramework(requestMap, channelId);
			String frameworkId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			assertTrue(StringUtils.isNotBlank(frameworkId));
			assertTrue(StringUtils.isNotBlank(versionKey));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create Framework with valid request body, invalid channel id (channel id
	 * doesn't exist)
	 * 
	 */
	@Test
	public void testFrameworkCreateWithInvalidChannel() {

		try {
			Map<String, Object> requestMap = mapper.readValue(createFrameworkReqJson,
					new TypeReference<Map<String, Object>>() {
					});
			String channelId = "test1234";
			Response response = frameworkManager.createFramework(requestMap, channelId);
			String responseCode = (String) response.getResponseCode().toString();
			int resCode = response.getResponseCode().code();
			assertTrue("CLIENT_ERROR".equals(responseCode));
			assertTrue(resCode == 400);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create Framework with invalid request body, valid channel id
	 * 
	 */
	@Test
	public void testFrameworkCreateWithInvalidReqBody() {

		try {
			Map<String, Object> requestMap = null;
			Response response = frameworkManager.createFramework(requestMap, channelId);
			String responseCode = (String) response.getResponseCode().toString();
			int resCode = response.getResponseCode().code();
			assertTrue("CLIENT_ERROR".equals(responseCode));
			assertTrue(resCode == 400);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Read Framework with Invalid framework id
	 * 
	 * @throws Exception
	 * 
	 */
	@Test
	public void testFrameworkReadWithInvalidFrameworkId() throws Exception {
		exception.expect(ResourceNotFoundException.class);
		String frameworkId = "test1234"; // Invalid framework id
		Response response = frameworkManager.readFramework(frameworkId, Arrays.asList());
		String responseCode = response.getResponseCode().toString();
		assertTrue("ERR_DATA_NOT_FOUND".equals(responseCode));
	}

	/**
	 * Update Framework with Invalid Owner
	 * 
	 * 
	 */
	@Test
	public void testFrameworkUpdateWithInvalidOwner() throws Exception {

		Map<String, Object> requestMap = mapper.readValue(updateFrameworkJson,
				new TypeReference<Map<String, Object>>() {
				});
		String channelId = "test1234";
		Response response = frameworkManager.updateFramework(frameworkId, channelId, requestMap);
		String responseCode = response.getResponseCode().toString();
		int resCode = response.getResponseCode().code();
		assertTrue("CLIENT_ERROR".equals(responseCode));
		assertTrue(resCode == 400);
	}

	/**
	 * Retire Framework with Invalid Owner
	 * 
	 * 
	 */
	@Test
	public void testFrameworkRetireWithInvalidOwner() throws Exception {

		String channelId = "test1234"; // Invalid Owner
		Response response = frameworkManager.retireFramework(frameworkId, channelId);
		String responseCode = response.getResponseCode().toString();
		int resCode = response.getResponseCode().code();
		assertTrue("CLIENT_ERROR".equals(responseCode));
		assertTrue(resCode == 400);
	}

	private static void createFramework() {

		try {
			Map<String, Object> requestMap = mapper.readValue(createFrameworkReq,
					new TypeReference<Map<String, Object>>() {
					});
			Response resp = frameworkManager.createFramework(requestMap, channelId);
			frameworkId = (String) resp.getResult().get("node_id");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void createChannel() {
		try {
			Map<String, Object> requestMap = mapper.readValue(createChannelReq,
					new TypeReference<Map<String, Object>>() {
					});

			Response resp = channelManager.createChannel(requestMap);
			channelId = (String) resp.getResult().get(TestParams.node_id.name());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}