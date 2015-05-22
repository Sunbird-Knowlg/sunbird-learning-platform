package com.ilimi.taxonomy.controller.lob.cucumber;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ilimi.common.dto.Response;
import com.ilimi.taxonomy.base.test.BaseCucumberTest;

import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class GetGameTest extends BaseCucumberTest{
	    
    private String taxonomyId;
    private String gameId;
    
    private void basicAssertion(Response resp){
		Assert.assertEquals("ekstep.lp.learning-object.find", resp.getId());
        Assert.assertEquals("1.0", resp.getVer());
	}
    
    @Before
    public void setup() throws IOException {
        initMockMVC();
    }
    
    @When("^taxonomy id is (.*) and Game Id is (.*)$")
	public void getInputData(String taxonomyId, String gameId){
		if(taxonomyId.equals("absent"))
			this.taxonomyId = "absent";
		if(taxonomyId.equals("empty"))
			this.taxonomyId = "";
		else
			this.taxonomyId = taxonomyId;
		this.gameId = gameId;
	}
    
    @Then("^i should get the Game with status (.*)$")
    public void createConcept(String status) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object/" + gameId;
    	params.put("taxonomyId", taxonomyId);
    	params.put("gfields", "name");
    	header.put("user-id", "ilimi");
    	ResultActions actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(200));
		} catch (Exception e) {
			e.printStackTrace();
		}
        Response resp = jasonToObject(actions);
        basicAssertion(resp);
        Assert.assertEquals(status, resp.getParams().getStatus());
	}
    
    @Then("^i should get Error message Taxonomy Id is (.*) and status is (\\d+)$")
    public void getConceptWithoutTaxonomy(String errmsg, int status) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object/" + gameId;
    	if(taxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "ilimi");
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
        	Assert.assertEquals("ERR_TAXONOMY_BLANK_TAXONOMY_ID", resp.getParams().getErr());
        	Assert.assertEquals(resp.getParams().getErrmsg(), "Taxonomy Id is " + errmsg);
        }               
    }
    
    @Then("^i should get Error message Node not found and status is (\\d+)$")
    public void nodeNotFound(int status) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object/" + gameId;
    	if(taxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "ilimi");
    	ResultActions actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc); 
    	try {
			actions.andExpect(status().is(status));
		} catch (Exception e) {
			System.out.println();
			e.printStackTrace();
		} 
    	
    	Response resp = jasonToObject(actions);
        basicAssertion(resp);
        Assert.assertEquals("Node not found: " + gameId, resp.getParams().getErrmsg());
        Assert.assertEquals("ERR_GRAPH_SEARCH_UNKNOWN_ERROR", resp.getParams().getErr());
    }
	
}
