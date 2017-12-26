package org.ekstep.orchestrator.controller;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
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

@Ignore
@FixMethodOrder(MethodSorters.DEFAULT)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })	
public class ExecutionControllerTest {

	@Autowired
	private WebApplicationContext context;
	MockMvc mockMvc;
	private ResultActions actions;
	
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}
	
	@Ignore
	@Test
	public void testExecution() throws Exception {
		
		String path = "/v3/channel/create";
		String json = "{\"request\":{\"channel\":{\"name\":\"content\",\"description\":\"\",\"code\":\"org.ekstep.test\",\"contentFilter\":{\"filters\":{\"gradeLevel\":[\"Grade 3\"]}}}}}";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).content(json));
		System.out.println("Response:" + actions.andReturn().getResponse().getContentAsString());
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}
}
