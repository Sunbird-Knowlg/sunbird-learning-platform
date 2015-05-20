package com.ilimi.taxonomy.controller.iob.cucumber.test;

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
public class DeleteGameTest extends BaseCucumberTest{
	
	private String taxonomyId;
	private String gameId;
	
	@Before
    public void setup() throws IOException {
        initMockMVC();
    }
	
	private void basicAssertion(Response resp){
		Assert.assertEquals("ekstep.lp.learning-object.delete", resp.getId());
        Assert.assertEquals("1.0", resp.getVer());
	}
	
	@When("^I Give input taxonomy ID (.*) and Game ID (.*)$")
	public void getInputData(String taxonomyId, String gameId){
		if(taxonomyId.equals("absent"))
			this.taxonomyId = "absent";
		if(taxonomyId.equals("empty"))
			this.taxonomyId = "";
		else
			this.taxonomyId = taxonomyId;
		this.gameId = gameId;		
	}
	
	@Then("^Delete the Game Id (.*)$")
    public void deleteGame(String game) throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object/" + gameId;
    	params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionDelete(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(202));
		} catch (Exception e) {
			e.printStackTrace();
		}
        Response resp = jasonToObject(actions);
		basicAssertion(resp);
		Assert.assertEquals("SUCCESS", resp.getParams().getStatus());
	}
	
	@Then("^I should get Error Taxonomy Id is (.*) and status is (\\d+)$")
    public void getConceptWithoutTaxonomy(String errmsg, int status) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object/" + gameId;
    	if(taxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionDelete(path, params, MediaType.APPLICATION_JSON, header, mockMvc); 
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
        	Assert.assertEquals(resp.getParams().getErrmsg(), "Taxonomy Id is " + errmsg);
        	Assert.assertEquals("ERR_TAXONOMY_BLANK_TAXONOMY_ID", resp.getParams().getErr());
        }               
    }
	
	@Then("^I should get Error Node not found and status is (\\d+)$")
    public void nodeNotFound(int status) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object/" + gameId;
    	if(taxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionDelete(path, params, MediaType.APPLICATION_JSON, header, mockMvc); 
    	try {
			actions.andExpect(status().is(status));
		} catch (Exception e) {
			System.out.println();
			e.printStackTrace();
		} 
    	Response resp = jasonToObject(actions);
    	basicAssertion(resp);
    	Assert.assertEquals("Node not found: " + gameId , resp.getParams().getErrmsg());
    	Assert.assertEquals("ERR_GRAPH_NODE_NOT_FOUND", resp.getParams().getErr());
    }
	
}
