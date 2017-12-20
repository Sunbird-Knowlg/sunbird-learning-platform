package com.ilimi.framework.manager.test;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
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
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.framework.mgr.ICategoryManager;
import com.ilimi.framework.mgr.IChannelManager;
import com.ilimi.framework.mgr.IFrameworkManager;
import com.ilimi.framework.test.common.TestSetup;

/**
 * 
 * @author Rashmi
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class CategoryManagerTest extends TestSetup {
	
	@Autowired
	ICategoryManager mgr;
	ICategoryManager categoryMgr;
	IChannelManager channelMgr;
	IFrameworkManager frmwrkMgr;
	static ObjectMapper mapper = new ObjectMapper();

	static int rn = generateRandomNumber(0, 9999);
	
	String createCategoryValidRequest = "{\"category\":{\"name\":\"category\",\"description\":\"sample description of category\",\"code\":\"medium_1"+ rn + "\"}}}";
	String createCategoryWithoutCode = "{\"category\":{\"name\":\"category\",\"description\":\"sample description of category\"}}";
	String createCategoryWithoutInvalidRequest = "{\"catesafgory\":{\"name\":\"category\",\"description\":\"sample description of category\"}}";
	
	@BeforeClass()
	public static void beforeClass() throws Exception {
		loadDefinition("definitions/category_definition.json");
	}
	
	@SuppressWarnings({"unchecked","rawtypes"})
	@Test
	public void createCategory() throws Exception {
		Map<String, Object> requestMap = mapper.readValue(createCategoryValidRequest, new TypeReference<Map<String, Object>>() {});
		Map<String,Object> categoryMap = (Map)requestMap.get("category");
		Response response = mgr.createCategory(categoryMap);
		Assert.assertEquals(ResponseCode.OK, response.getResponseCode());
		Map<String,Object> result = response.getResult();
		Assert.assertEquals(true, StringUtils.isNoneBlank((String)result.get("node_id")));
	}
	
	@SuppressWarnings({"unchecked","rawtypes"})
	@Test
	public void createCategoryWithoutCode() throws Exception {
		Map<String, Object> requestMap = mapper.readValue(createCategoryWithoutCode, new TypeReference<Map<String, Object>>() {});
		Map<String,Object> categoryMap = (Map)requestMap.get("category");
		Response response = mgr.createCategory(categoryMap);
		String responseCode=(String) response.getResponseCode().toString();
		assertTrue(responseCode.equals("CLIENT_ERROR"));
	}
	
	@Test
	public void createCategoryWithInvalidRequest() throws Exception {
		Map<String,Object> categoryMap = new HashMap<String,Object>();
		Response response = mgr.createCategory(categoryMap);
		String responseCode=(String) response.getResponseCode().toString();
		assertTrue(responseCode.equals("CLIENT_ERROR"));
	}
	
	@SuppressWarnings({"unchecked","rawtypes"})
	@Test
	public void readCategoryWithValidNodeId() throws Exception {
		Map<String, Object> requestMap = mapper.readValue(createCategoryValidRequest, new TypeReference<Map<String, Object>>() {});
		Map<String,Object> categoryMap = (Map)requestMap.get("category");
		String identifier = (String)categoryMap.get("code");
		categoryMap.put("code", identifier + rn);
		Response response = mgr.createCategory(categoryMap);
		Map<String,Object> result = response.getResult();
		String node_id = (String)result.get("node_id");
		Response resp = mgr.readCategory(node_id);
		Assert.assertEquals(ResponseCode.OK, resp.getResponseCode());
		Map<String,Object> resultMap = resp.getResult();
		Map<String,Object> categoryResult = (Map)resultMap.get("category");
		Assert.assertEquals(node_id, categoryResult.get("identifier"));
		Assert.assertEquals("sample description of category", categoryResult.get("description"));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void updateCategoryWithNodeId() throws Exception {
		Map<String, Object> requestMap = mapper.readValue(createCategoryValidRequest, new TypeReference<Map<String, Object>>() {});
		Map<String,Object> categoryMap = (Map)requestMap.get("category");
		String code = (String)categoryMap.get("code");
		categoryMap.put("code", code+rn+rn+System.currentTimeMillis());
		Response response = mgr.createCategory(categoryMap);
		Map<String,Object> result = response.getResult();
		String node_id = (String)result.get("node_id");
		Map<String,Object> updateRequest = new HashMap<String,Object>();
		updateRequest.put("description", "testDescription");
		updateRequest.put("identifier", node_id);
		updateRequest.put("versionKey", result.get("versionKey"));
		Response resp = mgr.updateCategory(node_id, updateRequest);
		Assert.assertEquals(ResponseCode.OK, resp.getResponseCode());
		Response readResp = mgr.readCategory(node_id);
		Map<String,Object> map = readResp.getResult();
		Map<String,Object> categoryResult = (Map)map.get("category");
		Assert.assertEquals("testDescription", categoryResult.get("description"));
	}
	
	@Test
	public void updateCategoryWithInValidNodeId() throws Exception {
		Map<String,Object> updateRequest = new HashMap<String,Object>();
		updateRequest.put("description", "testDescription");
		Response resp = mgr.updateCategory("do_13234567", updateRequest);
		String responseCode=(String) resp.getResponseCode().toString();
		assertTrue(responseCode.equals("CLIENT_ERROR"));	
	}
	
	@Test
	public void updateCategoryWithoutNodeId() throws Exception {
		Map<String,Object> updateRequest = new HashMap<String,Object>();
		updateRequest.put("description", "testDescription");
		Response resp = mgr.updateCategory(null, updateRequest);
		String responseCode=(String) resp.getResponseCode().toString();
		assertTrue(responseCode.equals("CLIENT_ERROR"));
	}
	
	@SuppressWarnings({"unchecked","rawtypes"})
	@Test
	public void searchCategoryWithValidRequest() throws Exception {
		Map<String, Object> requestMap = mapper.readValue(createCategoryValidRequest, new TypeReference<Map<String, Object>>() {});
		Map<String,Object> searchRequestMap = new HashMap<String,Object>();
		Map<String,Object> categoryMap = (Map)requestMap.get("category");
		Response response = mgr.createCategory(categoryMap);
		Map<String,Object> result = response.getResult();
		String node_id = (String)result.get("node_id");
		searchRequestMap.put("identifier", node_id);
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("request", searchRequestMap);
		Response res = mgr.searchCategory(searchRequestMap);
		Assert.assertEquals(ResponseCode.OK, res.getResponseCode());
	}
	
	@Test
	public void searchCategoryWithoutRequest() throws Exception {
		Response res = mgr.searchCategory(null);
		String responseCode=(String) res.getResponseCode().toString();
		assertTrue(responseCode.equals("CLIENT_ERROR"));
	}

	@SuppressWarnings({"unchecked","rawtypes"})
	@Test
	public void retireCategory() throws Exception {
		Map<String, Object> requestMap = mapper.readValue(createCategoryValidRequest, new TypeReference<Map<String, Object>>() {});
		Map<String,Object> categoryMap = (Map)requestMap.get("category");
		String code = (String)categoryMap.get("code");
		categoryMap.put("code", code+rn+rn);
		Response response = mgr.createCategory(categoryMap);
		Map<String,Object> result = response.getResult();
		String node_id = (String)result.get("node_id");
		mgr.retireCategory(node_id);
		Response res = mgr.readCategory(node_id);
		Map<String,Object> map = res.getResult();
		Map<String,Object> category = (Map)map.get("category");
		Assert.assertEquals("Retired", (String)category.get("status"));
	}
	
	@Test
	public void retireCategoryWithInvalidId() throws Exception {
		Response resp = mgr.retireCategory(null);
		String responseCode=(String) resp.getResponseCode().toString();
		assertTrue(responseCode.equals("CLIENT_ERROR"));
	}
	
	@Test
	public void retireCategoryWithoutNodeId() throws Exception {
		Response resp = mgr.retireCategory("do_12456");
		String responseCode=(String) resp.getResponseCode().toString();
		assertTrue(responseCode.equals("CLIENT_ERROR"));
	}
	
	private static int generateRandomNumber(int min, int max) {
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
}
