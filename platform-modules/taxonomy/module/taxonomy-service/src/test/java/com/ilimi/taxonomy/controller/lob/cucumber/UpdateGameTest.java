package com.ilimi.taxonomy.controller.lob.cucumber;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
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

@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class UpdateGameTest extends BaseCucumberTest{
	private String taxonomyId;
	private String gameId;
	
	private void basicAssertion(Response resp){
		Assert.assertEquals("ekstep.lp.learning-object.update", resp.getId());
        Assert.assertEquals("1.0", resp.getVer());
	}
	
	@Before
    public void setup() {
        try {
			initMockMVC();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	@When("^i give input taxonomy ID is (.*) and concept ID is (.*)$")
	public void getInputData(String taxonomyId, String gameId){
		if(taxonomyId.equals("absent"))
			this.taxonomyId = "absent";
		if(taxonomyId.equals("empty"))
			this.taxonomyId = "";
		else
			this.taxonomyId = taxonomyId;
		this.gameId = gameId;
	}
	
	@Then("^Update the Game and get the status (.*)")
	public void updateGame(String status){
		String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"Game\",\"graphId\": \"numeracy\",\"identifier\": \"G1\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \" Puzzle For Kids\",\"code\": \"ek.lit.an\"}}}}";
        Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object/G1";
    	params.put("taxonomyId", "numeracy");
    	header.put("user-id", "ilimi");
    	ResultActions actions = resultActionPatch(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().isOk());
		} catch (Exception e) {
			e.printStackTrace();
		} 
        Response resp = jasonToObject(actions);
        basicAssertion(resp);
        Assert.assertEquals(status,resp.getParams().getStatus());
	}
	
	@Then("^i should get Error Message taxonomy Id is (.*) and status is (\\d+)$")
    public void getConceptWithoutOrEmptyTaxonomy(String errmsg, int status) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "{\"request\": {\"concept\": {\"identifier\": \"Num:C1\",\"nodeType\": \"DATA_NODE\",\"objectType\": \"Concept\",\"metadata\": {\"identifier\": \"Num:C1\",\"code\": \"Num:C1\",\"learningObjective\": [\"New Learning Objective 222\"],\"arrayProp\": [\"value1\", \"value3\", \"valu5\"]},\"tags\": [\"Subconcept\", \"tag 9\", \"tag 10\"]},\"COMMENT\" : \"\"}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object/" + gameId;
    	if(taxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "ilimi");
    	ResultActions actions = resultActionPatch(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc); 
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
	
	@Then("^i should get error message is Learning Object is (.*)$")
	public void gameObjectBlank(String error) {
		String contentString = "{\"request\": {}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object/" + gameId;
    	if(taxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "ilimi");
    	ResultActions actions = resultActionPatch(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc); 
    	try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			System.out.println();
			e.printStackTrace();
		}
    	Response resp = jasonToObject(actions);
    	basicAssertion(resp);
        Assert.assertEquals("Learning Object is blank", resp.getParams().getErrmsg());
        Assert.assertEquals("ERR_LOB_BLANK_LEARNING_OBJECT", resp.getParams().getErr());
	}
	
	@Then("^i should get error message game (.*) found")
	public void gameNotFound(String error){
		String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"Game\",\"graphId\": \"numeracy\",\"identifier\": \"G98\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\"}}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object/"+ gameId;
    	params.put("taxonomyId", "numeracy");
    	header.put("user-id", "ilimi");
    	ResultActions actions = resultActionPatch(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(404));
		} catch (Exception e) {
			e.printStackTrace();
		}
        Response resp = jasonToObject(actions);
        basicAssertion(resp);
        Assert.assertEquals("Node "+error+" Found", resp.getParams().getErrmsg());
        Assert.assertEquals("ERR_GRAPH_UPDATE_NODE_NOT_FOUND", resp.getParams().getErr());
	}
	
	@Then("i should get error message object type not set for node (.*)$")
	public void emptyObjectType(String node){
		String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"\",\"graphId\": \"numeracy\",\"identifier\": \"G1\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\"}}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object/" + gameId;
    	params.put("taxonomyId", "numeracy");
    	header.put("user-id", "ilimi");
    	ResultActions actions = resultActionPatch(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			e.printStackTrace();
		}
        Response resp = jasonToObject(actions);
        basicAssertion(resp);
        Map<String, Object> result = resp.getResult();
        @SuppressWarnings("unchecked")
		ArrayList<String>   msg = (ArrayList<String>) result.get("messages");
        Assert.assertEquals("Object type not set for node: "+node, msg.get(0)); 
        Assert.assertEquals("Node Metadata validation failed", resp.getParams().getErrmsg());
	}
	
	@Then("^i should get error message node metadata validation (.*)$")
	public void requireMetadata(String error){
		String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"Game\",\"graphId\": \"numeracy\",\"identifier\": \"G1\",\"nodeType\": \"DATA_NODE\",\"metadata\": {}}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object/" + gameId;
    	params.put("taxonomyId", "numeracy");
    	header.put("user-id", "ilimi");
    	ResultActions actions = resultActionPatch(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			e.printStackTrace();
		}
        Response resp = jasonToObject(actions);
        
        Map<String, Object> result = resp.getResult();
        @SuppressWarnings("unchecked")
		ArrayList<String>   msg = (ArrayList<String>) result.get("messages");
        Assert.assertEquals("Required Metadata name not set", msg.get(0)); 
        Assert.assertEquals("Node Metadata validation failed", resp.getParams().getErrmsg());
        Assert.assertEquals("ERR_GRAPH_UPDATE_NODE_VALIDATION_FAILED", resp.getParams().getErr());
    }
    
}

