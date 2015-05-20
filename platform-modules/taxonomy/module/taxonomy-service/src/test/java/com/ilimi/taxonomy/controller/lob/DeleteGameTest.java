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
import com.ilimi.taxonomy.base.test.BaseIlimiTest;

@WebAppConfiguration
@RunWith(value=SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class DeleteGameTest extends BaseIlimiTest{
	
	private void basicAssertion(Response resp){
		Assert.assertEquals("ekstep.lp.learning-object.delete", resp.getId());
        Assert.assertEquals("1.0", resp.getVer());
	}
	
    @Test
    public void deleteConcept() {        
        Map<String, String> params = new HashMap<String, String>();
		Map<String, String> header = new HashMap<String, String>();
		String path = "/learning-object/G1";
		params.put("taxonomyId", "NUMERACY");
		header.put("user-id", "ilimi");		
		ResultActions actions = resultActionDelete(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
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
    public void emptyTaxonomyId() {
    	Map<String, String> params = new HashMap<String, String>();
 		Map<String, String> header = new HashMap<String, String>();
 		String path = "/learning-object/G1";
 		params.put("taxonomyId", "");
 		header.put("user-id", "ilimi");		
 		ResultActions actions = resultActionDelete(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
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
    public void withoutTaxonomyId() {
    	Map<String, String> params = new HashMap<String, String>();
 		Map<String, String> header = new HashMap<String, String>();
 		String path = "/learning-object/G1";
 		header.put("user-id", "ilimi");		
 		ResultActions actions = resultActionDelete(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
 		try {
 			actions.andExpect(status().is(400));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
        Assert.assertEquals("Required String parameter 'taxonomyId' is not present", actions.andReturn().getResponse().getErrorMessage());
    }
    
    @Test
    public void gameNotFound() {
    	Map<String, String> params = new HashMap<String, String>();
 		Map<String, String> header = new HashMap<String, String>();
 		String path = "/learning-object/ilimi";
 		params.put("taxonomyId", "NUMERACY");
 		header.put("user-id", "ilimi");		
 		ResultActions actions = resultActionDelete(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
 		try {
 			actions.andExpect(status().is(404));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		 Response resp = jasonToObject(actions);
 		 basicAssertion(resp);
         Assert.assertEquals("Node not found: ilimi", resp.getParams().getErrmsg());
         Assert.assertEquals("ERR_GRAPH_NODE_NOT_FOUND", resp.getParams().getErr());
    }

}
