package com.ilimi.taxonomy.controller.lob;

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
public class GetAllGamesTest extends BaseIlimiTest{
	    
   @Test
    public void findAllGames() {
        Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object/";
    	params.put("taxonomyId", "NUMERACY");
    	params.put("objectType", "Games");
    	params.put("offset", "0");
    	params.put("limit", "10");
    	params.put("gfields", "name");
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().isAccepted());
		} catch (Exception e) {
			e.printStackTrace();
		}       
   }
   
   @Test
   public void emptyTaxonomyId() {
	   	Map<String, String> params = new HashMap<String, String>();
		Map<String, String> header = new HashMap<String, String>();
		String path = "/learning-object/";
		params.put("taxonomyId", "");
		params.put("objectType", "Games");
		params.put("offset", "0");
		params.put("limit", "10");
		params.put("gfields", "name");
		header.put("user-id", "jeetu");
		ResultActions actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
		try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			e.printStackTrace();
		}       
		Response resp = jasonToObject(actions);
		Assert.assertEquals("Taxonomy Id is blank", resp.getParams().getErrmsg());
		Assert.assertEquals("ERR_TAXONOMY_BLANK_TAXONOMY_ID", resp.getParams().getErr());
  }
   
   @Test
   public void withoutTaxonomyId() {
	   Map<String, String> params = new HashMap<String, String>();
	   	Map<String, String> header = new HashMap<String, String>();
	   	String path = "/learning-object/";
	   	params.put("taxonomyId", "NUMERACY");
	   	params.put("objectType", "Games");
	   	params.put("offset", "0");
	   	params.put("limit", "10");
	   	params.put("gfields", "name");
	   	header.put("user-id", "jeetu");
	   	ResultActions actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
	   	try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			e.printStackTrace();
		}       
	   	Assert.assertEquals("Required String parameter 'taxonomyId' is not present", actions.andReturn().getResponse().getErrorMessage());
  }
   
   
}
