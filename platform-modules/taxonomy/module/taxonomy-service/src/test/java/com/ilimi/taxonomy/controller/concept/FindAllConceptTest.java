package com.ilimi.taxonomy.controller.concept;

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
import com.ilimi.taxonomy.base.test.BaseIlimiTest;

@WebAppConfiguration
@RunWith(value=SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class FindAllConceptTest extends BaseIlimiTest{
     
	private void basicAssertion(Response resp){
		Assert.assertEquals("ekstep.lp.concept.list", resp.getId());
        Assert.assertEquals("1.0", resp.getVer());
	}
	
    @Test
    public void findAllConcepts() {
        Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept";
    	params.put("taxonomyId", "NUMERACY");
    	params.put("games", "true");
    	params.put("cfields", "name");
    	params.put("gfields", "name");
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().isAccepted());
		} catch (Exception e) {
			e.printStackTrace();
		} 
        Response resp = jasonToObject(actions);
        basicAssertion(resp);
        Assert.assertEquals("SUCCESS", resp.getParams().getStatus());
    }
    
    @Test
    public void emptyTaxonomyId()  {
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept";
    	params.put("taxonomyId", "");
    	params.put("games", "true");
    	params.put("cfields", "name");
    	params.put("gfields", "name");
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			e.printStackTrace();
		}  
        Response resp = jasonToObject(actions);
        basicAssertion(resp);
        Assert.assertEquals("Taxonomy Id is blank", resp.getParams().getErrmsg());
        Assert.assertEquals("ERR_TAXONOMY_BLANK_TAXONOMY_ID", resp.getParams().getErr());
   }
    
    @Test
    public void withoutTaxonomyId()  {
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept";
    	params.put("games", "true");
    	params.put("cfields", "name");
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
    
    @Test
    public void getConceptWithGamesFalse() {
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept";
    	params.put("taxonomyId", "NUMERACY");
    	params.put("games", "false");
    	params.put("cfields", "name");
    	params.put("gfields", "name");
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().isAccepted());
		} catch (Exception e) {
			e.printStackTrace();
		}  
        Response resp = jasonToObject(actions);
        basicAssertion(resp);
    }
}
