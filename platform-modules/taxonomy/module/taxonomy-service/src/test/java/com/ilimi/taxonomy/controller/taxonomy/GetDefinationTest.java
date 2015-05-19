package com.ilimi.taxonomy.controller.taxonomy;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import com.ilimi.common.dto.Response;
import com.ilimi.taxonomy.test.util.BaseIlimiTest;

@WebAppConfiguration
@RunWith(value=SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class GetDefinationTest extends BaseIlimiTest{

    
   @Test
    public void getDefination() {
        Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/taxonomy/NUMERACY/definition/Game";
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().isAccepted());
		} catch (Exception e) {
			e.printStackTrace();
		}     
   }
   
   @Test
   public void taxonomyIdNotFound() {
		Map<String, String> params = new HashMap<String, String>();
		Map<String, String> header = new HashMap<String, String>();
		String path = "/taxonomy/NUM/definition/Game";
		header.put("user-id", "jeetu");
		ResultActions actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
		try {
			actions.andExpect(status().is(404));
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jasonToObject(actions);
		Assert.assertEquals("Failed to get definition node", resp.getParams().getErrmsg());
		Assert.assertEquals("ERR_GRAPH_SEARCH_NODE_NOT_FOUND", resp.getParams().getErr());
   }
   
   @Test
   public void objectTypeNotFound() {
		Map<String, String> params = new HashMap<String, String>();
		Map<String, String> header = new HashMap<String, String>();
		String path = "/taxonomy/NUMERACY/definition/Gam";
		header.put("user-id", "jeetu");
		ResultActions actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
		try {
			actions.andExpect(status().is(404));
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jasonToObject(actions);
		Assert.assertEquals("Failed to get definition node", resp.getParams().getErrmsg());
		Assert.assertEquals("ERR_GRAPH_SEARCH_NODE_NOT_FOUND", resp.getParams().getErr());
   }
}
