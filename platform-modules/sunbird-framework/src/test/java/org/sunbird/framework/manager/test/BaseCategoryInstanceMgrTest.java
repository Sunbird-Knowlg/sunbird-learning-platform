package org.sunbird.framework.manager.test;

import java.util.Map;
import java.util.Random;

import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.framework.mgr.ICategoryManager;
import org.sunbird.framework.mgr.IChannelManager;
import org.sunbird.framework.mgr.IFrameworkManager;
import org.sunbird.graph.engine.common.GraphEngineTestSetup;
import org.junit.Assert;
import org.junit.BeforeClass;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author Rashmi
 *
 */
public class BaseCategoryInstanceMgrTest extends GraphEngineTestSetup {
	
	private static ObjectMapper mapper = new ObjectMapper();
	private static int rn = generateRandomNumber(0, 9999);

	private String createCategoryValidRequest = "{\"name\":\"category\",\"description\":\"sample description of category\",\"code\":\"grade"+ rn + System.currentTimeMillis() +  "\"}}}";
	private String createFrameworkReq = "{\"name\": \"NCERT01\",\"description\": \"NCERT framework of Karnatka\",\"code\": \"ncert" + rn + System.currentTimeMillis() + "\"}";
	private String createChannelReq = "{\"name\":\"state_MH\",\"description\":\"test\",\"code\":\"MAHARASTRA" + rn + System.currentTimeMillis()+ "\"}";

	@BeforeClass()
	public static void beforeClass() throws Exception {
		loadDefinition("definitions/category_definition.json", "definitions/framework_definition.json", "definitions/channel_definition.json", "definitions/categoryInstance_definition.json");
	}

	public String createCategory(ICategoryManager mgr) throws Exception {
		Map<String, Object> requestMap = mapper.readValue(createCategoryValidRequest, new TypeReference<Map<String, Object>>() {});
		Response response = mgr.createCategory(requestMap);
		Assert.assertEquals(ResponseCode.OK, response.getResponseCode());
		String node_id = (String)response.get("node_id");
		return node_id;
	}
	
	@SuppressWarnings({"unchecked","rawtypes"})
	public String createChannel(IChannelManager mgr)  throws Exception {
		Map<String, Object> requestMap = mapper.readValue(createChannelReq, new TypeReference<Map<String, Object>>() {});
		Response response = mgr.createChannel(requestMap);
		Map<String,Object> result = (Map)response.getResult();
		String node_id = (String)result.get("node_id");
		return node_id;	
	}
	
	@SuppressWarnings({"unchecked","rawtypes"})
	public String createFramework(IChannelManager cMgr, IFrameworkManager fMgr) throws Exception {
		String channelId = createChannel(cMgr);
		Map<String, Object> requestMap = mapper.readValue(createFrameworkReq, new TypeReference<Map<String, Object>>() {});
		Response response = fMgr.createFramework(requestMap, channelId);
		Map<String,Object> result = (Map)response.getResult();
		String node_id = (String)result.get("node_id");
		return node_id;	
	}
	
	private static int generateRandomNumber(int min, int max) {
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
}
