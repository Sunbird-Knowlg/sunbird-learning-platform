package org.ekstep.framework.manager.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.framework.mgr.IChannelManager;
import org.ekstep.framework.mgr.IFrameworkManager;
import org.ekstep.framework.test.common.TestParams;
import org.ekstep.graph.cache.util.RedisStoreUtil;
import org.ekstep.graph.engine.common.GraphEngineTestSetup;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author Rashmi
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class ChannelManagerTest extends GraphEngineTestSetup {

	@Autowired
	private IChannelManager channelMgr;

	@Autowired
	private IFrameworkManager frameworkMgr;

	private static ObjectMapper mapper = new ObjectMapper();

	private static int rn = generateRandomNumber(0, 9999);

	private static String frameworkId = "";
	private static String channelId = "";

	private String createChannelValidRequest = "{\"name\":\"channel\",\"description\":\"sample description of channel\",\"code\":\"karnataka"
			+ rn + "\"}";
	private String createChannelWithoutCode = "{\"name\":\"channel\",\"description\":\"sample description of channel\"}";

	private static String createLicenseQuery = "create(n:domain {lastStatusChangedOn:\"2019-11-21T15:04:26.363+0530\",IL_SYS_NODE_TYPE:\"DATA_NODE\",IL_FUNC_OBJECT_TYPE:\"License\",name:\"cc-by-0001\",lastUpdatedOn:\"2019-11-21T15:04:26.363+0530\",createdOn:\"2019-11-21T15:04:26.363+0530\",IL_UNIQUE_ID:\"cc-by-0001\",url:\"kp-test-url\",versionKey:\"1574328866363\",status:\"Live\"});";

	private static final String LICENSE_REDIS_KEY = "edge_license";

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@BeforeClass()
	public static void beforeClass() throws Exception {
		loadDefinition("definitions/channel_definition.json", "definitions/framework_definition.json");
		delay(2000);
	}

	private static void createLicense(String... querys) {
		for(String query:querys){
			try{
				graphDb.execute(query);
			}catch(Exception e){

			}
		}
	}

	@Before
	public void init() {
		if (StringUtils.isBlank(channelId))
			createChannel();
		if (StringUtils.isBlank(frameworkId))
			createFramework();
	}

	private void createFramework() {
		String createFrameworkReq = "{\"name\": \"NCERT01\",\"description\": \"NCERT framework of Karnatka\",\"code\": \"org.ekstep.framework.create\",\"channels\":[{\"identifier\": \""
				+ channelId + "\"}]}";
		try {
			Map<String, Object> requestMap = mapper.readValue(createFrameworkReq,
					new TypeReference<Map<String, Object>>() {
					});
			Response resp = frameworkMgr.createFramework(requestMap, channelId);
			frameworkId = (String) resp.getResult().get("node_id");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void createChannel() {
		String createChannelReq = "{\"name\":\"channelTest\",\"description\":\"\",\"code\":\"channelTest\"}";
		try {
			Map<String, Object> requestMap = mapper.readValue(createChannelReq,
					new TypeReference<Map<String, Object>>() {
					});
			Response resp = channelMgr.createChannel(requestMap);
			channelId = (String) resp.getResult().get(TestParams.node_id.name());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void createChannelWithValidRequest() throws Exception {
		Map<String, Object> requestMap = mapper.readValue(createChannelValidRequest,
				new TypeReference<Map<String, Object>>() {
				});
		Response response = channelMgr.createChannel(requestMap);
		Assert.assertEquals(ResponseCode.OK, response.getResponseCode());
		Map<String, Object> result = response.getResult();
		Assert.assertEquals(true, StringUtils.isNoneBlank((String) result.get("node_id")));
	}

	@Test
	public void createChannelWithoutCode() throws Exception {
		Map<String, Object> requestMap = mapper.readValue(createChannelWithoutCode,
				new TypeReference<Map<String, Object>>() {
				});
		Response response = channelMgr.createChannel(requestMap);
		String responseCode = (String) response.getResponseCode().toString();
		assertTrue("CLIENT_ERROR".equals(responseCode));
	}

	@Test
	public void createChannelWithInvalidRequest() throws Exception {
		Map<String, Object> channelMap = new HashMap<String, Object>();
		Response response = channelMgr.createChannel(channelMap);
		String responseCode = (String) response.getResponseCode().toString();
		assertTrue("CLIENT_ERROR".equals(responseCode));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void readChannelWithValidNodeId() throws Exception {
		Response resp = channelMgr.readChannel(channelId);
		Assert.assertEquals(ResponseCode.OK, resp.getResponseCode());
		Map<String, Object> resultMap = resp.getResult();
		Map<String, Object> channelResult = (Map) resultMap.get("channel");
		Assert.assertEquals(channelId, channelResult.get("identifier"));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void updateChannelWithNodeId() throws Exception {
		Map<String, Object> updateRequest = new HashMap<String, Object>();
		updateRequest.put("description", "testDescription");
		updateRequest.put("identifier", channelId);
		Response resp = channelMgr.updateChannel(channelId, updateRequest);
		Assert.assertEquals(ResponseCode.OK, resp.getResponseCode());
		Response readResp = channelMgr.readChannel(channelId);
		Map<String, Object> map = readResp.getResult();
		Map<String, Object> channelResult = (Map) map.get("channel");
		Assert.assertEquals("testDescription", channelResult.get("description"));
	}

	@Test
	public void updateChannelWithInValidNodeId() throws Exception {
		Map<String, Object> updateRequest = new HashMap<String, Object>();
		updateRequest.put("description", "testDescription");
		Response resp = channelMgr.updateChannel("do_13234567", updateRequest);
		String responseCode = (String) resp.getResponseCode().toString();
		assertTrue("CLIENT_ERROR".equals(responseCode));
	}

	@Test
	public void updatechannelWithoutNodeId() throws Exception {
		Map<String, Object> updateRequest = new HashMap<String, Object>();
		updateRequest.put("description", "testDescription");
		Response resp = channelMgr.updateChannel(null, updateRequest);
		String responseCode = (String) resp.getResponseCode().toString();
		assertTrue("CLIENT_ERROR".equals(responseCode));
	}

	@Test
	public void searchChannelWithValidRequest() throws Exception {
		Map<String, Object> requestMap = mapper.readValue(createChannelValidRequest,
				new TypeReference<Map<String, Object>>() {
				});
		Map<String, Object> searchRequestMap = new HashMap<String, Object>();
		Response response = channelMgr.createChannel(requestMap);
		Map<String, Object> result = response.getResult();
		String node_id = (String) result.get("node_id");
		searchRequestMap.put("identifier", node_id);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("request", searchRequestMap);
		Response res = channelMgr.listChannel(searchRequestMap);
		Assert.assertEquals(ResponseCode.OK, res.getResponseCode());
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void retireChannel() throws Exception {
		channelMgr.retireChannel(channelId);
		Response res = channelMgr.readChannel(channelId);
		Map<String, Object> map = res.getResult();
		Map<String, Object> channel = (Map) map.get("channel");
		Assert.assertEquals("Retired", (String) channel.get("status"));
	}

	@Test
	public void retireChannelWithInvalidId() throws Exception {
		Response resp = channelMgr.retireChannel(null);
		String responseCode = (String) resp.getResponseCode().toString();
		assertTrue("CLIENT_ERROR".equals(responseCode));
	}

	@Test
	public void retireChannelWithoutNodeId() throws Exception {
		Response resp = channelMgr.retireChannel("do_12456");
		String responseCode = (String) resp.getResponseCode().toString();
		assertTrue("CLIENT_ERROR".equals(responseCode));
	}

	@Test
	public void updateChannelWithValidDefaultLicense() throws Exception {
		String defaultLicense = "cc-by-0001";
		Map<String, Object> updateRequest = new HashMap<String, Object>();
		updateRequest.put("description", "testDescription");
		updateRequest.put("identifier", channelId);
		updateRequest.put("defaultLicense", defaultLicense);
		RedisStoreUtil.saveList(LICENSE_REDIS_KEY, new ArrayList<Object>(){{add(defaultLicense);}});
		Response responseCode = channelMgr.updateChannel(channelId, updateRequest);
		Assert.assertEquals(ResponseCode.OK, responseCode.getResponseCode());
		RedisStoreUtil.delete(LICENSE_REDIS_KEY);
		Response readResp = channelMgr.readChannel(channelId);
		Map<String, Object> map = readResp.getResult();
		Map<String, Object> channelResult = (Map) map.get("channel");
		Assert.assertEquals("testDescription", channelResult.get("description"));
		Assert.assertEquals(defaultLicense, channelResult.get("defaultLicense"));
	}

	@Test
	public void updateChannelWithInvalidDefaultLicense() throws Exception {
		exception.expect(ClientException.class);
		String defaultLicense = "cc-by-0001";
		Map<String, Object> updateRequest = new HashMap<String, Object>();
		updateRequest.put("description", "testDescription");
		updateRequest.put("identifier", channelId);
		updateRequest.put("defaultLicense", defaultLicense);
		RedisStoreUtil.saveList(LICENSE_REDIS_KEY, new ArrayList<Object>(){{add("creative-common-0001");}});
		channelMgr.updateChannel(channelId, updateRequest);
		RedisStoreUtil.delete(LICENSE_REDIS_KEY);
	}

	@Test
	public void createChannelWithValidDefaultLicense() throws Exception {
		String defaultLicense = "cc-by-0001";
		Map<String, Object> requestMap = mapper.readValue(createChannelValidRequest,
				new TypeReference<Map<String, Object>>() {
				});
		requestMap.put("defaultLicense", defaultLicense);
		RedisStoreUtil.saveList(LICENSE_REDIS_KEY, new ArrayList<Object>(){{add(defaultLicense);}});
		Response response = channelMgr.createChannel(requestMap);
		Assert.assertEquals(ResponseCode.OK, response.getResponseCode());
		RedisStoreUtil.delete(LICENSE_REDIS_KEY);
		Map<String, Object> result = response.getResult();
		Assert.assertEquals(true, StringUtils.isNoneBlank((String) result.get("node_id")));
	}

	@Test
	public void createChannelWithInvalidDefaultLicense() throws Exception {
		exception.expect(ClientException.class);
		String defaultLicense = "cc-by-0001";
		Map<String, Object> requestMap = mapper.readValue(createChannelValidRequest,
				new TypeReference<Map<String, Object>>() {
				});
		requestMap.put("defaultLicense", defaultLicense);
		RedisStoreUtil.saveList(LICENSE_REDIS_KEY, new ArrayList<Object>(){{add("creative-common-0001");}});
		channelMgr.createChannel(requestMap);
		RedisStoreUtil.delete(LICENSE_REDIS_KEY);
	}

	@Test
	public void createChannelWithValidNeo4jDefaultLicense() throws Exception {
		createLicense(createLicenseQuery);
		String defaultLicense = "cc-by-0001";
		Map<String, Object> requestMap = mapper.readValue(createChannelValidRequest,
				new TypeReference<Map<String, Object>>() {
				});
		requestMap.put("defaultLicense", defaultLicense);
		Response response = channelMgr.createChannel(requestMap);
		Assert.assertEquals(ResponseCode.OK, response.getResponseCode());
		Map<String, Object> result = response.getResult();
		Assert.assertEquals(true, StringUtils.isNoneBlank((String) result.get("node_id")));
		RedisStoreUtil.delete(LICENSE_REDIS_KEY);
	}

	@Test
	public void createChannelWithInvalidNeo4jDefaultLicense() throws Exception {
		exception.expect(ServerException.class);
		RedisStoreUtil.delete(LICENSE_REDIS_KEY);
		String defaultLicense = "cc-by-0001";
		Map<String, Object> requestMap = mapper.readValue(createChannelValidRequest,
				new TypeReference<Map<String, Object>>() {
				});
		requestMap.put("defaultLicense", defaultLicense);
		channelMgr.createChannel(requestMap);
	}

	private static int generateRandomNumber(int min, int max) {
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
}
