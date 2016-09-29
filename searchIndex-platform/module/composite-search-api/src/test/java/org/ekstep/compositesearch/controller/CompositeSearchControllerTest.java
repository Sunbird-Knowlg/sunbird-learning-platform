package org.ekstep.compositesearch.controller;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.ilimi.common.dto.Response;

/**
 * 
 * @author rayulu
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class CompositeSearchControllerTest extends BaseSearchServiceTest {

	@Autowired
	private WebApplicationContext context;
	private ResultActions actions;
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchByQuery() {
		String contentString = "{\"request\":{\"query\": \"हिन्दी\", \"filters\": {\"objectType\": [\"Content\"], \"status\": []}}}";
		Response response = getResponse(contentString);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("content");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() > 0);
		boolean found = false;
		for (Object obj : list) {
			Map<String, Object> content = (Map<String, Object>) obj;
			String desc = (String) content.get("description");
			if (null != desc && desc.contains("हिन्दी"))
				found = true;
		}
		Assert.assertTrue(found);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchFilters() {
		String contentString = "{\"request\":{\"filters\": {\"objectType\": [\"Content\"], \"status\": []}}}";
		Response response = getResponse(contentString);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("content");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() == 32);
		for (Object obj : list) {
			Map<String, Object> content = (Map<String, Object>) obj;
			String objectType = (String) content.get("objectType");
			Assert.assertEquals("Content", objectType);
		}
	}
	
	@Test
	public void testSearchArrayFilter() {
	}
	
	@Test
	public void testSearchStringValueFilter() {
	}
	
	@Test
	public void testSearchStartsWithFilter() {
	}
	
	@Test
	public void testSearchEndsWithFilter() {
	}
	
	@Test
	public void testSearchMinFilter() {
	}
	
	@Test
	public void testSearchMaxFilter() {
	}
	
	@Test
	public void testSearchLTFilter() {
	}
	
	@Test
	public void testSearchGTFilter() {
	}
	
	@Test
	public void testSearchValueFilter() {
	}
	
	@Test
	public void testSearchExistsCondition() {
	}
	
	@Test
	public void testSearchNotExistsCondition() {
	}
	
	@Test
	public void testSearchFacets() {
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchLimit() {
		String contentString = "{\"request\":{\"filters\": {\"objectType\": [\"Content\"], \"status\": []}, \"limit\": 10}}";
		Response response = getResponse(contentString);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("content");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() == 10);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchEmptyResult() {
		String contentString = "{\"request\":{\"filters\": {\"objectType\": [\"InvalidObjectType\"], \"status\": []}, \"limit\": 10}}";
		Response response = getResponse(contentString);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("content");
		Assert.assertNull(list);
		Integer count = (Integer) result.get("count");
		Assert.assertTrue(count == 0);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchSortAsc() {
		String contentString = "{\"request\":{\"filters\": {\"objectType\": [\"Content\"], \"status\": []}, \"limit\": 5, \"sort_by\": {\"identifier\": \"asc\"}}}";
		Response response = getResponse(contentString);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("content");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() == 5);
		Map<String, Object> content1 = (Map<String, Object>) list.get(0);
		String id1 = (String) content1.get("identifier");
		Assert.assertEquals("do_10000001", id1);
		
		Map<String, Object> content5 = (Map<String, Object>) list.get(4);
		String id5 = (String) content5.get("identifier");
		Assert.assertEquals("do_10000005", id5);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSearchSortDesc() {
		String contentString = "{\"request\":{\"filters\": {\"objectType\": [\"Content\"], \"status\": []}, \"limit\": 2, \"sort_by\": {\"identifier\": \"desc\"}}}";
		Response response = getResponse(contentString);
		Map<String, Object> result = response.getResult();
		List<Object> list = (List<Object>) result.get("content");
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() == 2);
		Map<String, Object> content1 = (Map<String, Object>) list.get(0);
		String id1 = (String) content1.get("identifier");
		Assert.assertEquals("do_10000032", id1);
		
		Map<String, Object> content2 = (Map<String, Object>) list.get(1);
		String id2 = (String) content2.get("identifier");
		Assert.assertEquals("do_10000031", id2);
	}
	
	@Test
	public void testCount() {
	}
	
	private Response getResponse(String contentString) {
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v2/search";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path)
					.contentType(MediaType.APPLICATION_JSON)
					.content(contentString.getBytes())
					.header("user-id", "ilimi"));
			System.out.println("Response code: " + actions.andReturn().getResponse().getStatus());
			Assert.assertEquals(200, actions.andReturn().getResponse()
					.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response response = jsonToObject(actions);
		Assert.assertEquals("successful", response.getParams().getStatus());
		return response;
	}
}
