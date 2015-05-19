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
import com.ilimi.taxonomy.test.util.BaseIlimiTest;

@WebAppConfiguration
@RunWith(value=SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class DeleteConceptTest extends BaseIlimiTest {

    @Test
    public void deleteConcept() {
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/Num:C1";
    	params.put("taxonomyId", "NUMERACY");
    	header.put("user-id", "jeetu");
    	BaseIlimiTest tutil = new BaseIlimiTest();
    	ResultActions actions = tutil.resultActionDelete(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().isAccepted());
		} catch (Exception e) {
			e.printStackTrace();
		}        
    }
    
    @Test
    public void withoutTaxonomyId() {
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/Num:C1";
    	header.put("user-id", "jeetu");
    	BaseIlimiTest tutil = new BaseIlimiTest();
    	ResultActions actions = tutil.resultActionDelete(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().isAccepted());
		} catch (Exception e) {
			e.printStackTrace();
		}   
        Assert.assertEquals("Required String parameter 'taxonomyId' is not present", actions.andReturn().getResponse().getErrorMessage());
    }
    
    @Test
    public void emptyTaxonomyId() {
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/Num:C1";
    	params.put("taxonomyId", "");
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionDelete(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
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
    public void conceptIdNotFound() {
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/jeetu";
    	params.put("taxonomyId", "NUMERACY");
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionDelete(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(404));
		} catch (Exception e) {
			e.printStackTrace();
		}   
        Response resp = jasonToObject(actions);
        Assert.assertEquals("Node not found: jeetu", resp.getParams().getErrmsg());
        Assert.assertEquals("ERR_GRAPH_NODE_NOT_FOUND", resp.getParams().getErr());
    }

}
