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
	public void testSearchSuccess() throws Exception {
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
	
	@Test
	public void testCount() throws Exception {
		String contentString = "{\"request\":{\"filters\": {\"objectType\": [\"Content\"], \"status\": []}}}";
		Response response = getCountResponse(contentString);
		Map<String, Object> result = response.getResult();
		System.out.println(result);
		Double count = (Double) result.get("count");
		Assert.assertNotNull(count);
	}
	
	@Test
	public void testMetrics() throws Exception {
		String contentString = "{\"request\":{\"filters\": {\"objectType\": [\"Content\"], \"status\": []}}}";
		Response response = getMetricsResponse(contentString);
		Map<String, Object> result = response.getResult();
		Integer count = (Integer) result.get("count");
		Assert.assertNotNull(count);
		Assert.assertTrue(count > 0);
	}
	
	private Response getCountResponse(String contentString) throws Exception {
		String path = "/v2/search/count";
		actions = getActions(path, contentString);
		return checkSuccessResponse();
	}
	
	private Response getResponse(String contentString) throws Exception {
		String path = "/v2/search";
		actions = getActions(path, contentString);
		return checkSuccessResponse();
	}
	
	private Response getMetricsResponse(String contentString) throws Exception {
		String path = "/v2/metrics";
		actions = getActions(path, contentString);
		return checkSuccessResponse();
	}
	
	private ResultActions getActions(String path, String contentString) throws Exception {
		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path)
				.contentType(MediaType.APPLICATION_JSON)
				.content(contentString.getBytes())
				.header("user-id", "ilimi"));
		return actions;
	}
	
	private Response checkSuccessResponse() {
		Assert.assertEquals(200, actions.andReturn().getResponse()
				.getStatus());
		Response response = jsonToObject(actions);
		Assert.assertEquals("successful", response.getParams().getStatus());
		return response;
	}
}
