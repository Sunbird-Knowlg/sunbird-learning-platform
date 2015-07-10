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
public class CreateGameTest extends BaseCucumberTest{
	
	private String taxonomyId;
	
	private void basicAssertion(Response resp){
		Assert.assertEquals("ekstep.lp.learning-object.create", resp.getId());
        Assert.assertEquals("1.0", resp.getVer());
	}
	
	@Before
    public void setup() throws IOException {
        initMockMVC();
    }
		
	@When("^i give taxonomy ID (.*)$")
	public void getInputData(String taxonomyId){
		if(taxonomyId.equals("absent"))
			this.taxonomyId = "absent";
		if(taxonomyId.equals("empty"))
			this.taxonomyId = "";
		else
			this.taxonomyId = taxonomyId;		
	}
	
	@Then("^Create a Game and get the status (.*)$")
    public void createGame(String status) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"Game\",\"graphId\": \"numeracy\",\"identifier\": \"G1\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\",\"developer\" : \"Play Store\",\"owner\"     : \"Google & its Developers\"},\"inRelations\" : [{\"startNodeId\": \"G1\",\"relationType\": \"associatedTo\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object";
    	params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "ilimi");
    	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(200));
		} catch (Exception e) {
			e.printStackTrace();
		}
        Response resp = jasonToObject(actions);
        basicAssertion(resp);
        Assert.assertEquals(status, resp.getParams().getStatus());
	}
	
	@Then("^i will get Error Message Taxonomy Id is (.*) and status is (\\d+)$")
    public void getConceptWithoutTaxonomy(String errmsg, int status) {
		String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"Game\",\"graphId\": \"numeracy\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\",\"developer\" : \"Play Store\",\"owner\"     : \"Google & its Developers\"},\"inRelations\" : [{\"startNodeId\": \"G1\",\"relationType\": \"associatedTo\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object";
    	if(taxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "ilimi");
    	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc); 
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
	
	@Then("^i will get errMsg is (.*) learning object$")
	public void i_should_get_errMsg_is_Validation_Error(String error) {
		String contentString = "{\"request\": {}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object";
    	if(taxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "ilimi");
    	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc); 
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
	
	@Then("^i will get errMsg is Game Object is (.*)$")
	public void i_should_get_errMsg_is_Concept_Object_is_blank(String error) throws Throwable {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"\",\"graphId\": \"numeracy\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\",\"developer\" : \"Play Store\",\"owner\"     : \"Google & its Developers\"},\"inRelations\" : [{\"startNodeId\": \"G1\",\"relationType\": \"associatedTo\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object";
    	if(taxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "ilimi");
    	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc); 
    	try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			System.out.println();
			e.printStackTrace();
		} 
    	Response resp = jasonToObject(actions);
        basicAssertion(resp);
        Map<String, Object> result = resp.getResult();
        @SuppressWarnings("unchecked")
		ArrayList<String>   msg = (ArrayList<String>) result.get("messages");
        Assert.assertEquals("Object type not set for node: G99", msg.get(0));
        Assert.assertEquals("ERR_GRAPH_ADD_NODE_VALIDATION_FAILED", resp.getParams().getErr());         
        Assert.assertEquals("Validation Errors", resp.getParams().getErrmsg());         
	}
	
	@Then("^i will get errMsg is metadata (.*) is not set$")
	public void i_should_get_errMsg_is_metadata_name_is_not_set(String name) throws Throwable {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"Game\",\"graphId\": \"numeracy\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"inRelations\" : [{\"startNodeId\": \"G1\",\"relationType\": \"associatedTo\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object";
    	if(taxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "ilimi");
    	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc); 
    	try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			System.out.println();
			e.printStackTrace();
		} 
    	Response resp = jasonToObject(actions);
        basicAssertion(resp);
        Assert.assertEquals("Validation Errors", resp.getParams().getErrmsg());
        Map<String, Object> result = resp.getResult();
        @SuppressWarnings("unchecked")
		ArrayList<String>   msg = (ArrayList<String>) result.get("messages");
        Assert.assertEquals("Required Metadata code not set", msg.get(1));
        Assert.assertEquals("Required Metadata developer not set", msg.get(2));
        Assert.assertEquals("Required Metadata owner not set", msg.get(3));
        Assert.assertEquals("Required Metadata "+name+" not set", msg.get(0));
	}
	
	@Then("^i will get errMsg is validation error and status (\\d+)$")
	public void invalidDataType(int status){
		String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"Game\",\"graphId\": \"numeracy\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"status\": 123,\"code\": \"ek.lit.an\",\"developer\" : \"Play Store\",\"owner\"     : \"Google & its Developers\", \"interactivityLevel\" : \"s\"},\"inRelations\" : [{\"startNodeId\": \"G1\",\"relationType\": \"associatedTo\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
     	Map<String, String> header = new HashMap<String, String>();
     	String path = "/learning-object";
     	params.put("taxonomyId", taxonomyId);
     	header.put("user-id", "ilimi");
     	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
         try {
 			actions.andExpect(status().is(status));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}       
        Response resp = jasonToObject(actions);
        basicAssertion(resp);
        Map<String, Object> result = resp.getResult();
        @SuppressWarnings("unchecked")
		ArrayList<String>   msg = (ArrayList<String>) result.get("messages");
        //Assert.assertEquals("Metadata status should be one of: [Draft, Review, Live, Retired, Mock]", msg.get(0)); 
        Assert.assertEquals("Validation Errors", resp.getParams().getErrmsg());
        Assert.assertEquals("ERR_GRAPH_ADD_NODE_VALIDATION_FAILED", resp.getParams().getErr());
	}
	
	@Then("^i will get errMsg is Relation is (.*) supported$")
	public void i_should_get_errMsg_is_Relation_is_not_supported(String check) throws Throwable {
		String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"Game\",\"graphId\": \"numeracy\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\",\"developer\" : \"Play Store\",\"owner\": \"Google & its Developers\"},\"inRelations\" : [{\"startNodeId\": \"G1\",\"relationType\": \"isParent\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object";
    	params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "ilimi");
    	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc); 
    	try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			System.out.println();
			e.printStackTrace();
		} 
    	Response resp = jasonToObject(actions);
    	Map<String, Object> result = resp.getResult();
        @SuppressWarnings("unchecked")
		ArrayList<String>   msg = (ArrayList<String>) result.get("messages");
        Assert.assertEquals("Relation isParent is "+check+" supported", msg.get(0));
	}	
}
