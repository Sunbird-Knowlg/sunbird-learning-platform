package com.ilimi.taxonomy.controller.concept.cucumber;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ilimi.common.dto.Response;
import com.ilimi.taxonomy.base.test.BaseCucumberTest;

import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.api.junit.Cucumber;

@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class GetConceptTest extends BaseCucumberTest{
   
    private ResultActions actions;    
    private String taxonomyId;
    private String conceptId;
    private static String NUMERACY = "NUMERACY";
    
    private void basicAssertion(Response resp){
		Assert.assertEquals("ekstep.lp.concept.find", resp.getId());
        Assert.assertEquals("1.0", resp.getVer());
	}
    
    @Before
    public void setup() throws IOException {
        initMockMVC();
    }
    
    @When("^I give Taxonomy (.*) and Concept ID (.*)$")
    public void getInput(String taxonomyId, String conceptId) {
    	if(taxonomyId.equals("absent"))
			this.taxonomyId = "absent";
		if(taxonomyId.equals("empty"))
			this.taxonomyId = "";
		else
			this.taxonomyId = taxonomyId;
        this.conceptId = conceptId;
    }
    
    @Then("^I should get the status as (\\d+) and taxonomy id is (.*)$")
    public void getConceptWithoutTaxonomy(int status, String error) throws Exception {       
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/" + conceptId;
    	if(taxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", taxonomyId);
    	params.put("cfields", "name");
    	header.put("user-id", "jeetu");;
    	ResultActions actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc); 
    	try {
			actions.andExpect(status().is(status));
		} catch (Exception e) {
			System.out.println();
			e.printStackTrace();
		} 
        if(taxonomyId.equals("absent")) {
        	Assert.assertEquals(actions.andReturn().getResponse().getErrorMessage(), "Required String parameter 'taxonomyId' is not present");
        } else{
        	Response resp = jasonToObject(actions);
        	basicAssertion(resp);
        	Assert.assertEquals(resp.getParams().getErrmsg(), "Taxonomy Id is " + error);
        }           
            
    }
    
    @Then("^I should get the status as (\\d+) and node (.*) found")
    public void nodeNotFound(int status, String error){
    	Map<String, String> params = new HashMap<String, String>();
     	Map<String, String> header = new HashMap<String, String>();
     	String path = "/concept/" + conceptId;
     	params.put("taxonomyId", taxonomyId);
     	params.put("cfields", "name");
     	header.put("user-id", "jeetu");
     	ResultActions actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
         try {
 			actions.andExpect(status().is(404));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
         Response resp = jasonToObject(actions);
         basicAssertion(resp);
         Assert.assertEquals("ERR_GRAPH_SEARCH_UNKNOWN_ERROR", resp.getParams().getErr());
         Assert.assertEquals("Node "+error+" found: jeetu", resp.getParams().getErrmsg());
    }
    
    @Then("^I should get the concept with name (.*)$")
    public void getConcept(String name) throws Exception {
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/" + conceptId;
    	params.put("taxonomyId", taxonomyId);
    	params.put("cfields", "name");
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
    

}
