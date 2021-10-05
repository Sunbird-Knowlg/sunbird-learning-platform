package org.sunbird.framework.manager.test;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.framework.mgr.ICategoryManager;
import org.sunbird.graph.engine.common.GraphEngineTestSetup;
import org.junit.Assert;
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
public class CategoryManagerTest extends GraphEngineTestSetup {
	
	@Autowired
	private ICategoryManager mgr;
	private static ObjectMapper mapper = new ObjectMapper();

	private static int rn = generateRandomNumber(0, 9999);

	private String createCategoryValidRequest = "{\"category\":{\"name\":\"category\",\"description\":\"sample description of category\",\"code\":\"medium_1"+ rn + "\"}}}";
	private String createCategoryWithoutCode = "{\"category\":{\"name\":\"category\",\"description\":\"sample description of category\"}}";

	@Rule
	public final ExpectedException exception = ExpectedException.none();

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
		exception.expect(ClientException.class);
		Map<String, Object> requestMap = mapper.readValue(createCategoryWithoutCode, new TypeReference<Map<String, Object>>() {});
		Map<String,Object> categoryMap = (Map)requestMap.get("category");
		Response response = mgr.createCategory(categoryMap);
		String responseCode = response.getResponseCode().toString();
		assertTrue("CLIENT_ERROR".equals(responseCode));
	}
	
	@Test
	public void createCategoryWithInvalidRequest() throws Exception {
		exception.expect(ClientException.class);
		Map<String,Object> categoryMap = new HashMap<String,Object>();
		Response response = mgr.createCategory(categoryMap);
		String responseCode = response.getResponseCode().toString();
		assertTrue("CLIENT_ERROR".equals(responseCode));
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
		String responseCode = resp.getResponseCode().toString();
		assertTrue("CLIENT_ERROR".equals(responseCode));
	}
	
	@Test
	public void updateCategoryWithoutNodeId() throws Exception {
		Map<String,Object> updateRequest = new HashMap<String,Object>();
		updateRequest.put("description", "testDescription");
		Response resp = mgr.updateCategory(null, updateRequest);
		String responseCode = resp.getResponseCode().toString();
		assertTrue("CLIENT_ERROR".equals(responseCode));
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
		exception.expect(ClientException.class);
		Response res = mgr.searchCategory(null);
		String responseCode = res.getResponseCode().toString();
		assertTrue("CLIENT_ERROR".equals(responseCode));
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
		assertTrue("CLIENT_ERROR".equals(responseCode));
	}
	
	@Test
	public void retireCategoryWithoutNodeId() throws Exception {
		Response resp = mgr.retireCategory("do_12456");
		String responseCode=(String) resp.getResponseCode().toString();
		assertTrue("CLIENT_ERROR".equals(responseCode));
	}
	
	private static int generateRandomNumber(int min, int max) {
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
}
