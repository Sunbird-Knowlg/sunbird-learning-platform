package org.ekstep.framework.manager.test;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.framework.mgr.IChannelManager;
import org.ekstep.framework.mgr.IFrameworkManager;
import org.ekstep.framework.test.common.TestParams;
import org.ekstep.graph.engine.common.GraphEngineTestSetup;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
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
	IChannelManager channelMgr;

	@Autowired
	IFrameworkManager frameworkMgr;

	static ObjectMapper mapper = new ObjectMapper();

	static int rn = generateRandomNumber(0, 9999);

	static String frameworkId = "";
	static String channelId = "";

	String createChannelValidRequest = "{\"name\":\"channel\",\"description\":\"sample description of channel\",\"code\":\"karnataka"
			+ rn + "\"}";
	String createChannelWithoutCode = "{\"name\":\"channel\",\"description\":\"sample description of channel\"}";
	String createChannelWithoutInvalidRequest = "{\"channn3el\":{\"name\":\"channel\",\"description\":\"sample description of channel\"}}";

	@BeforeClass()
	public static void beforeClass() throws Exception {
		loadDefinition("definitions/channel_definition.json", "definitions/framework_definition.json");
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
		assertTrue(responseCode.equals("CLIENT_ERROR"));
	}

	@Test
	public void createChannelWithInvalidRequest() throws Exception {
		Map<String, Object> channelMap = new HashMap<String, Object>();
		Response response = channelMgr.createChannel(channelMap);
		String responseCode = (String) response.getResponseCode().toString();
		assertTrue(responseCode.equals("CLIENT_ERROR"));
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
		assertTrue(responseCode.equals("CLIENT_ERROR"));
	}

	@Test
	public void updatechannelWithoutNodeId() throws Exception {
		Map<String, Object> updateRequest = new HashMap<String, Object>();
		updateRequest.put("description", "testDescription");
		Response resp = channelMgr.updateChannel(null, updateRequest);
		String responseCode = (String) resp.getResponseCode().toString();
		assertTrue(responseCode.equals("CLIENT_ERROR"));
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
		assertTrue(responseCode.equals("CLIENT_ERROR"));
	}

	@Test
	public void retireChannelWithoutNodeId() throws Exception {
		Response resp = channelMgr.retireChannel("do_12456");
		String responseCode = (String) resp.getResponseCode().toString();
		assertTrue(responseCode.equals("CLIENT_ERROR"));
	}

	private static int generateRandomNumber(int min, int max) {
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
}
