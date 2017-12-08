package com.ilimi.framework.manager.test;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.framework.mgr.ICategoryInstanceManager;
import com.ilimi.framework.mgr.ICategoryManager;
import com.ilimi.framework.mgr.IChannelManager;
import com.ilimi.framework.mgr.IFrameworkManager;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class CategoryInstanceManagerTest extends BaseCategoryInstanceMgrTest {
	
	@Autowired
	ICategoryInstanceManager categoryInstanceMgr;
	
	@Autowired
	ICategoryManager categoryMgr;
	
	@Autowired
	IChannelManager channelMgr;
	
	@Autowired
	IFrameworkManager frmwrkMgr;
	
	static ObjectMapper mapper = new ObjectMapper();

	static int rn = generateRandomNumber(0, 9999);
	
	String createCategoryValidRequest = "{\"name\":\"category\",\"description\":\"\",\"code\":\"class_1" + "\"}";
	String createCategoryInvalidRequest = "{}";
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void createCategoryInstanceForFramework() throws Exception {
		Map<String, Object> requestMap = mapper.readValue(createCategoryValidRequest, new TypeReference<Map<String, Object>>() {});
		String categoryId = createCategory(categoryMgr);
		requestMap.put("code", categoryId);
		String frameworkId = createFramework(channelMgr, frmwrkMgr);
		Response response = categoryInstanceMgr.createCategoryInstance(frameworkId, requestMap);
		Assert.assertEquals(ResponseCode.OK, response.getResponseCode());
		Map<String,Object> result = response.getResult();
		String node_id = (String) result.get("node_id");
		Assert.assertEquals(true, StringUtils.isNotBlank(node_id));
		Response resp = categoryInstanceMgr.readCategoryInstance(frameworkId, categoryId);
		Map<String,Object> resultMap = resp.getResult();
		Map<String,Object> categoryMap = (Map) resultMap.get("categoryInstance");
		List<Map> frameworkMap = (List) categoryMap.get("framework");
		Assert.assertEquals(1, frameworkMap.size());
	}
	
	@Test(expected = ClientException.class)
	public void createCategoryInstanceWithInvalidCategory() throws Exception{
		Map<String, Object> requestMap = mapper.readValue(createCategoryValidRequest, new TypeReference<Map<String, Object>>() {});
		requestMap.put("code", "do_898089654");
		categoryInstanceMgr.createCategoryInstance("do_12345678", requestMap);
	}
	
	@Test(expected = ServerException.class)
	public void createCategoryInstanceWithInvalidFramework() throws Exception {
		Map<String, Object> requestMap = mapper.readValue(createCategoryValidRequest, new TypeReference<Map<String, Object>>() {});
		String categoryId = createCategory(categoryMgr);
		requestMap.put("code", categoryId);
		categoryInstanceMgr.createCategoryInstance("do_90897878", requestMap);
	}
	
	@Test
	public void createCategoryInstanceWithInValidRequest() throws Exception{
		Map<String, Object> requestMap = mapper.readValue(createCategoryInvalidRequest, new TypeReference<Map<String, Object>>() {});
		String categoryId = createCategory(categoryMgr);
		requestMap.put("code", categoryId);
		String frameworkId = createFramework(channelMgr, frmwrkMgr);
		Response response = categoryInstanceMgr.createCategoryInstance(frameworkId, requestMap);
		String responseCode=(String) response.getResponseCode().toString();
		assertTrue(responseCode.equals("CLIENT_ERROR"));
	}

	
	private static int generateRandomNumber(int min, int max) {
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
}
