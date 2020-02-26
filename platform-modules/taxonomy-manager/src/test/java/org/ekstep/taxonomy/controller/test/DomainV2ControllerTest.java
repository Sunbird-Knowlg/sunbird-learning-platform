package org.ekstep.taxonomy.controller.test;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.dto.Response;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class DomainV2ControllerTest {
		
		@Autowired
		private WebApplicationContext context;
		private ResultActions actions;
		
		@Test
		@Ignore
		public void findDomainTest(){
			
			MockMvc mockMvc;
			mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
			String path = "/v2/domains/graph/literacy?depth=40";
			try {
				actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header(
						"user-id", "ilimi"));
				Assert.assertEquals(200, actions.andReturn().getResponse()
						.getStatus());
				Response response = jsonToObject(actions);
				assertEquals("successful", response.getParams().getStatus());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@Test
		@Ignore
		public void findDomainTestWithInvalidUrl(){
			MockMvc mockMvc;
			mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
			String path = "/v2/domains/graph/liteacy?depth=40";
			try {
				actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header(
						"user-id", "ilimi"));
				Assert.assertEquals(404, actions.andReturn().getResponse()
						.getStatus());
				Response response = jsonToObject(actions);
				assertEquals("failed", response.getParams().getStatus());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@Test
		public void findDomainTestWithoutGraphId(){
			MockMvc mockMvc;
			mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
			String path = "/v2/literacy";
			try {
				actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header(
						"user-id", "ilimi"));
				Assert.assertEquals(404, actions.andReturn().getResponse()
						.getStatus());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
			
			public static Response jsonToObject(ResultActions actions) {
				String content = null;
				Response resp = null;
				try {
					content = actions.andReturn().getResponse().getContentAsString();
					ObjectMapper objectMapper = new ObjectMapper();
					if (StringUtils.isNotBlank(content))
						resp = objectMapper.readValue(content, Response.class);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return resp;
			}
}
		
		
