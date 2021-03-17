package org.sunbird.framework.manager.test;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.exception.ServerException;
import org.sunbird.framework.mgr.ICategoryInstanceManager;
import org.sunbird.framework.mgr.ICategoryManager;
import org.sunbird.framework.mgr.IChannelManager;
import org.sunbird.framework.mgr.IFrameworkManager;
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

/**
 * 
 * @author Rashmi
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class CategoryInstanceManagerTest extends BaseCategoryInstanceMgrTest {
	
	@Autowired
	private ICategoryInstanceManager categoryInstanceMgr;
	
	@Autowired
	private ICategoryManager categoryMgr;
	
	@Autowired
	private IChannelManager channelMgr;
	
	@Autowired
	private IFrameworkManager frmwrkMgr;
	
	private static ObjectMapper mapper = new ObjectMapper();
	
	private String createCategoryValidRequest = "{\"name\":\"category\",\"description\":\"\",\"code\":\"class_1" + "\"}";
	private String createCategoryInvalidRequest = "{}";
	
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
		Map<String,Object> categoryMap = (Map) resultMap.get("category");
		List<Map> frameworkMap = (List) categoryMap.get("frameworks");
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
	public void createCategoryInstanceWithInvalidRequest() throws Exception{
		Map<String, Object> requestMap = mapper.readValue(createCategoryInvalidRequest, new TypeReference<Map<String, Object>>() {});
		String categoryId = createCategory(categoryMgr);
		requestMap.put("code", categoryId);
		String frameworkId = createFramework(channelMgr, frmwrkMgr);
		Response response = categoryInstanceMgr.createCategoryInstance(frameworkId, requestMap);
		String responseCode=(String) response.getResponseCode().toString();
		assertTrue("CLIENT_ERROR".equals(responseCode));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void readCategoryInstanceWithValidId() throws Exception {
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
		Map<String,Object> categoryMap = (Map) resultMap.get("category");
		List<Map> frameworkMap = (List) categoryMap.get("frameworks");
		Assert.assertEquals(1, frameworkMap.size());
	}
	
	@Test (expected = ClientException.class)
	public void readCategoryWithInvalidFramework() throws Exception {
		String categoryId = createCategory(categoryMgr);
		Response resp = categoryInstanceMgr.readCategoryInstance("do_11234", categoryId);
		String responseCode=(String) resp.getResponseCode().toString();
		assertTrue("ERR_CHANNEL_NOT_FOUND/ERR_FRAMEWORK_NOT_FOUND".equals(responseCode));
	}
	
	@Test (expected = ClientException.class)
	public void readCategoryWithInvalidcategoryId() throws Exception {
		String frameworkId = createFramework(channelMgr, frmwrkMgr);
		Response resp = categoryInstanceMgr.readCategoryInstance(frameworkId, "do_123456787654");
		String responseCode=(String) resp.getResponseCode().toString();
		assertTrue("ERR_CHANNEL_NOT_FOUND/ERR_FRAMEWORK_NOT_FOUND".equals(responseCode));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void updateCategoryInstanceWithValidId() throws Exception {
		Map<String, Object> requestMap = mapper.readValue(createCategoryValidRequest, new TypeReference<Map<String, Object>>() {});
		String categoryId = createCategory(categoryMgr);
		requestMap.put("code", categoryId);
		String frameworkId = createFramework(channelMgr, frmwrkMgr);
		Response response = categoryInstanceMgr.createCategoryInstance(frameworkId, requestMap);
		Assert.assertEquals(ResponseCode.OK, response.getResponseCode());
		Map<String,Object> result = response.getResult();
		String node_id = (String) result.get("node_id");
		String versionKey = (String) result.get("versionKey");
		Assert.assertEquals(true, StringUtils.isNotBlank(node_id));
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("description", "update category instance");
		map.put("versionkey", versionKey);
		System.out.println("framework" + frameworkId + "categoryId" + categoryId);
		categoryInstanceMgr.updateCategoryInstance(frameworkId, categoryId, map);
		Response response1 = categoryInstanceMgr.readCategoryInstance(frameworkId, categoryId);
		Map<String,Object> resultMap = response1.getResult();
		Map<String,Object> categoryMap = (Map) resultMap.get("category");
		String description = (String) categoryMap.get("description");
		Assert.assertEquals(description, "update category instance");
	}
	
	@Test (expected = ClientException.class)
	public void updateCategoryWithInvalidFrameworkId() throws Exception {
		Map<String, Object> requestMap = mapper.readValue(createCategoryValidRequest, new TypeReference<Map<String, Object>>() {});
		String categoryId = createCategory(categoryMgr);
		requestMap.put("code", categoryId);
		String frameworkId = createFramework(channelMgr, frmwrkMgr);
		Response response = categoryInstanceMgr.createCategoryInstance(frameworkId, requestMap);
		Assert.assertEquals(ResponseCode.OK, response.getResponseCode());
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("description", "update category instance");
		Response resp = categoryInstanceMgr.updateCategoryInstance("do_11234", categoryId, map);
		String responseCode=(String) resp.getResponseCode().toString();
		assertTrue("ERR_CHANNEL_NOT_FOUND/ERR_FRAMEWORK_NOT_FOUND".equals(responseCode));
	}
	
	@Test (expected = ClientException.class)
	public void updateCategoryWithInvalidCategoryId() throws Exception {
		String frameworkId = createFramework(channelMgr, frmwrkMgr);
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("description", "update category instance");
		Response resp = categoryInstanceMgr.updateCategoryInstance(frameworkId, "do_123456787654", map);
		String responseCode=(String) resp.getResponseCode().toString();
		assertTrue("ERR_CHANNEL_NOT_FOUND/ERR_FRAMEWORK_NOT_FOUND".equals(responseCode));
	}
	
	@Test
	public void updateCategoryWithEmptyRequest() throws Exception {
		String frameworkId = createFramework(channelMgr, frmwrkMgr);
		Response resp = categoryInstanceMgr.updateCategoryInstance(frameworkId, "do_123456787654", null);
		String responseCode=(String) resp.getResponseCode().toString();
		assertTrue("CLIENT_ERROR".equals(responseCode));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void searchCategoryInstance() throws Exception {
		Map<String, Object> requestMap = mapper.readValue(createCategoryValidRequest, new TypeReference<Map<String, Object>>() {});
		String categoryId = createCategory(categoryMgr);
		requestMap.put("code", categoryId);
		String frameworkId = createFramework(channelMgr, frmwrkMgr);
		Response response = categoryInstanceMgr.createCategoryInstance(frameworkId, requestMap);
		Assert.assertEquals(ResponseCode.OK, response.getResponseCode());
		Map<String,Object> searchMap = new HashMap<String,Object>();
		Response res = categoryInstanceMgr.searchCategoryInstance(frameworkId, searchMap);
		Map<String,Object> resultMap = res.getResult();
		List<Map<String,Object>> categoryMap = (List) resultMap.get("categories");
		Assert.assertEquals(1, categoryMap.size());
	}
	
	@Test
	public void searchCategoryInstanceWithoutRequest() throws Exception {
		String frameworkId = createFramework(channelMgr, frmwrkMgr);
		Response resp = categoryInstanceMgr.searchCategoryInstance(frameworkId, null);
		String responseCode=(String) resp.getResponseCode().toString();
		assertTrue("CLIENT_ERROR".equals(responseCode));
	}
	
	@Test
	public void searchCategoryInstanceWithInvalidFramework() throws Exception {
		Map<String,Object> searchMap = new HashMap<String,Object>();
		Response resp = categoryInstanceMgr.searchCategoryInstance("do_123456776543", searchMap);
		String responseCode=(String) resp.getResponseCode().toString();
		assertTrue("SERVER_ERROR".equals(responseCode));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void retireCategoryInstance() throws Exception {
		Map<String, Object> requestMap = mapper.readValue(createCategoryValidRequest, new TypeReference<Map<String, Object>>() {});
		String categoryId = createCategory(categoryMgr);
		requestMap.put("code", categoryId);
		String frameworkId = createFramework(channelMgr, frmwrkMgr);
		Response response = categoryInstanceMgr.createCategoryInstance(frameworkId, requestMap);
		Assert.assertEquals(ResponseCode.OK, response.getResponseCode());
		Response resp = categoryInstanceMgr.retireCategoryInstance(frameworkId, categoryId);
		System.out.println((String)resp.getResponseCode().toString());
		Response response1 = categoryInstanceMgr.readCategoryInstance(frameworkId, categoryId);
		Map<String,Object> resultMap = response1.getResult();
		Map<String,Object> categoryMap = (Map) resultMap.get("category");
		Assert.assertEquals("Retired", (String)categoryMap.get("status"));
	}
	
	@Test(expected = ClientException.class)
	public void retireCategoryInstanceWithInvalidCategoryId() throws Exception {
		String frameworkId = createFramework(channelMgr, frmwrkMgr);
		Response resp = categoryInstanceMgr.retireCategoryInstance(frameworkId, "do_123456787654");
		String responseCode=(String) resp.getResponseCode().toString();
		assertTrue("ERR_CHANNEL_NOT_FOUND/ERR_FRAMEWORK_NOT_FOUND".equals(responseCode));
	}
	
	@Test (expected = ClientException.class)
	public void retireCategoryInstanceWithInvalidFrameworkId() throws Exception {
		String categoryId = createCategory(categoryMgr);
		Response resp = categoryInstanceMgr.retireCategoryInstance("do_12567890", categoryId);
		String responseCode=(String) resp.getResponseCode().toString();
		assertTrue("ERR_CHANNEL_NOT_FOUND/ERR_FRAMEWORK_NOT_FOUND".equals(responseCode));
	}
	
	@Test
	public void testValidateCategoryId() {
		Boolean value = categoryInstanceMgr.validateScopeId(null);
		Assert.assertEquals(false, value);
	}
}
