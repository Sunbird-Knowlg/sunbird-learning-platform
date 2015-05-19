package com.ilimi.taxonomy.controller.iob.cucumber.test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.ilimi.taxonomy.cucumber.concept.controller.test.CucumberBaseTestIlimi;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class CreateGameTest extends CucumberBaseTestIlimi{
	
	private String taxonomyId;
	
	@Given("^Game object is blank$")
	public void concept_object_is_blank() {
	   
	}
	
	@Given("^Game Object type is blank$")
	public void object_type_is_blank() {
	   
	}
	
	@Given("^Game Missing metadata$")
	public void missing_metadata() throws Throwable {
	    
	}
	
	@Given("^Game Unspported Relation$")
	public void unspported_Relation() throws Throwable {
	  
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
    public void createConcept(String concept) throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"Game\",\"graphId\": \"NUMERACY\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\",\"developer\" : \"Play Store\",\"owner\"     : \"Google & its Developers\"},\"inRelations\" : [{\"startNodeId\": \"G1\",\"relationType\": \"associatedTo\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object";
    	params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(202));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Then("^i will get Error Message Taxonomy Id is (.*) and status is (\\d+)$")
    public void getConceptWithoutTaxonomy(String errmsg, int status) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"Game\",\"graphId\": \"NUMERACY\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\",\"developer\" : \"Play Store\",\"owner\"     : \"Google & its Developers\"},\"inRelations\" : [{\"startNodeId\": \"G1\",\"relationType\": \"associatedTo\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object";
    	if(taxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "jeetu");
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
        	Assert.assertEquals(resp.getParams().getErrmsg(), "Taxonomy Id is " + errmsg);
        }               
    }
	
	@Then("^i will get errMsg is Validation (.*)$")
	public void i_should_get_errMsg_is_Validation_Error(String error) throws Throwable {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "{\"request\": {\"learning_object\": {}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object";
    	if(taxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc); 
    	try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			System.out.println();
			e.printStackTrace();
		} 
         
	}
	
	@Then("^i will get errMsg is Game Object is (.*)$")
	public void i_should_get_errMsg_is_Concept_Object_is_blank(String error) throws Throwable {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"\",\"graphId\": \"NUMERACY\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\",\"developer\" : \"Play Store\",\"owner\"     : \"Google & its Developers\"},\"inRelations\" : [{\"startNodeId\": \"G1\",\"relationType\": \"associatedTo\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object";
    	if(taxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc); 
    	try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			System.out.println();
			e.printStackTrace();
		} 
    	Response resp = jasonToObject(actions);
        Assert.assertEquals("Concept Object is " + error, resp.getParams().getErrmsg());         
	}
	
	@Then("^i will get errMsg is metadata (.*) is not set$")
	public void i_should_get_errMsg_is_metadata_name_is_not_set(String name) throws Throwable {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"Game\",\"graphId\": \"NUMERACY\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"inRelations\" : [{\"startNodeId\": \"G1\",\"relationType\": \"associatedTo\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object";
    	if(taxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "jeetu");
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
        Assert.assertEquals("Required Metadata "+name+" not set", msg.get(0));
	}
	
	@Then("^i will get errMsg is Relation is (.*) supported$")
	public void i_should_get_errMsg_is_Relation_is_not_supported(String check) throws Throwable {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "{\"request\": {\"concept\": {\"objectType\": \"Concept\",\"metadata\": {\"name\": \"GeometryTest\",\"description\": \"GeometryTest\",\"code\": \"Num:C234\",\"learningObjective\": [\"\"]},\"inRelations\" : [{\"startNodeId\": \"Num:C1:SC1\",\"relationType\": \"isParen\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object";
    	params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "jeetu");
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
        Assert.assertEquals("Relation isParen is "+check+" supported", msg.get(0));
	}
	
	@Given("^Game incoming outgoing relation$")
	public void incoming_outgoing_relation() throws Throwable {
	   
	}	
}
