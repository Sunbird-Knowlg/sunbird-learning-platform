package com.ilimi.taxonomy.controller.concept.cucumber;

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
public class CreateConceptTest extends BaseCucumberTest {
	
	private String taxonomyId;
	
	private void basicAssertion(Response resp){
		Assert.assertEquals("ekstep.lp.concept.create", resp.getId());
        Assert.assertEquals("1.0", resp.getVer());
	}
	
	private void ErrorMsgAssertion(Response resp){
		Assert.assertEquals("ERR_GRAPH_ADD_NODE_VALIDATION_FAILED", resp.getParams().getErr());
        Assert.assertEquals("Validation Errors", resp.getParams().getErrmsg());        
	}
	
	public void getConcept(String conceptId) {	
        Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/" + conceptId;
    	params.put("taxonomyId", "NUMERACY");
    	params.put("cfields", "name");
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().isAccepted());
		} catch (Exception e) {
			e.printStackTrace();
		}   
        Response resp = jasonToObject(actions);
        //new FindConceptTest().basicAssertion(resp);
        Assert.assertEquals("SUCCESS", resp.getParams().getStatus());
    }
	
	@Before
    public void setup() throws IOException {
        initMockMVC();
    }
		
	@When("^I give Taxonomy ID (.*)$")
	public void getInputData(String taxonomyId){
		if(taxonomyId.equals("absent"))
			this.taxonomyId = "absent";
		if(taxonomyId.equals("empty"))
			this.taxonomyId = "";
		else
			this.taxonomyId = taxonomyId;		
	}
	
	@Then("^Create a Concept and get the status (.*)$")
    public void createConcept(String status) throws Exception {
		String contentString = "{\"request\": {\"concept\": {\"objectType\": \"Concept\",\"metadata\": {\"name\": \"GeometryTest\",\"description\": \"GeometryTest\",\"code\": \"Num:C234\",\"learningObjective\": [\"\"]},\"inRelations\" : [{\"startNodeId\": \"Num:C1:SC1\",\"relationType\": \"isParentOf\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept";
    	params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(202));
		} catch (Exception e) {
			e.printStackTrace();
		}

        Response resp = jasonToObject(actions);
        basicAssertion(resp);
        Assert.assertEquals(status, resp.getParams().getStatus());
        Map<String, Object> result = resp.getResult();
		getConcept((String)result.get("node_id"));							
	}
	
	@Then("^I should get Error Message Taxonomy Id is (.*) and status is (\\d+)$")
    public void getConceptWithoutTaxonomy(String errmsg, int status) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "{\"request\": {\"concept\": {\"objectType\": \"Concept\",\"metadata\": {\"name\": \"GeometryTest\",\"description\": \"GeometryTest\",\"code\": \"Num:C234\",\"learningObjective\": [\"\"]},\"inRelations\" : [{\"startNodeId\": \"Num:C1:SC1\",\"relationType\": \"isParentOf\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept";
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
        	basicAssertion(resp);
        	Assert.assertEquals(resp.getParams().getErrmsg(), "Taxonomy Id is " + errmsg);
        	Assert.assertEquals("ERR_TAXONOMY_BLANK_TAXONOMY_ID", resp.getParams().getErr());
        }               
    }
	
	@Then("^I should get errMsg is Validation (.*)$")
	public void conceptObjectBlank(String error) throws Throwable {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "\"{\"request\": {\"concept\": {}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept";
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
	
	@Then("^I should get errMsg is Concept Object is (.*)$")
	public void i_should_get_errMsg_is_Concept_Object_is_blank(String conceptObject) throws Throwable {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "{\"request\": {\"CONCEPT\": {\"objectType\": \"\",\"metadata\": {\"name\": \"GeometryTest\",\"description\": \"GeometryTest\",\"code\": \"Num:C234\",\"learningObjective\": [\"\"]},\"inRelations\" : [{\"startNodeId\": \"Num:C1:SC1\",\"relationType\": \"isParentOf\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept";
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
    	basicAssertion(resp);
        Assert.assertEquals("Concept Object is " + conceptObject, resp.getParams().getErrmsg());         
	}
	
	@Then("^I should get errMsg is node (.*) found")
	public void nodeNotFound(String error){
		String contentString = "{\"request\": {\"concept\": {\"objectType\": \"Concept\",\"metadata\": {\"name\": \"GeometryTest\",\"description\": \"GeometryTest\",\"code\": \"Num:C234\",\"learningObjective\": [\"\"]},\"inRelations\" : [{\"startNodeId\": \"tempNode\",\"relationType\": \"isParentOf\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept";
    	if(taxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "ilimi");
    	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);
        try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			e.printStackTrace();
		}
        Response resp = jasonToObject(actions);      
        Map<String, Object> result = resp.getResult();
        @SuppressWarnings("unchecked")
		ArrayList<String>   msg = (ArrayList<String>) result.get("messages");
        Assert.assertEquals("Node "+error+" found: tempNode", msg.get(0));
        Assert.assertEquals("Failed to update relations and tags", resp.getParams().getErrmsg());
        Assert.assertEquals("ERR_GRAPH_UPDATE_NODE_VALIDATION_FAILED", resp.getParams().getErr());
	}
	
	@Then("^I should get errMsg is metadata (.*) is not set$")
	public void i_should_get_errMsg_is_metadata_name_is_not_set(String name) throws Throwable {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "{\"request\": {\"concept\": {\"objectType\": \"Concept\",\"inRelations\" : [{\"startNodeId\": \"Num:C1:SC1\",\"relationType\": \"isParentOf\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept";
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
    	ErrorMsgAssertion(resp);
    	Map<String, Object> result = resp.getResult();
        @SuppressWarnings("unchecked")
		ArrayList<String>   msg = (ArrayList<String>) result.get("messages");
        Assert.assertEquals("Required Metadata "+name+" not set", msg.get(0));
	}
	
	@Then("^I should get errMsg is datatype is invalid")
	public void invalidDataType(){
		String contentString = "{\"request\": {\"concept\": {\"objectType\": \"Concept\",\"metadata\": {\"name\": \"GeometryTest\",\"status\": \"wrongstatus\",\"des\": \"GeometryTest\",\"code\": \"Num:C234\",\"learningObjective\": [\"\"]},\"inRelations\" : [{\"startNodeId\": \"Num:C1:SC1\",\"relationType\": \"isParentOf\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept";
    	if(taxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "ilimi");
    	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);
        try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			e.printStackTrace();
		} 
        Response resp = jasonToObject(actions);
        basicAssertion(resp);
        ErrorMsgAssertion(resp);
        Map<String, Object> result = resp.getResult();
        @SuppressWarnings("unchecked")
		ArrayList<String>   msg = (ArrayList<String>) result.get("messages");
        Assert.assertEquals("Metadata status should be one of: [Draft, Active, Retired]", msg.get(0));
	}
	
	@Then("^I should get errMsg is Relation is (.*) supported$")
	public void i_should_get_errMsg_is_Relation_is_not_supported(String check) throws Throwable {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "{\"request\": {\"concept\": {\"objectType\": \"Concept\",\"metadata\": {\"name\": \"GeometryTest\",\"description\": \"GeometryTest\",\"code\": \"Num:C234\",\"learningObjective\": [\"\"]},\"inRelations\" : [{\"startNodeId\": \"Num:C1:SC1\",\"relationType\": \"isParen\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept";
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
    	basicAssertion(resp);
    	ErrorMsgAssertion(resp);
    	Map<String, Object> result = resp.getResult();
        @SuppressWarnings("unchecked")
		ArrayList<String>   msg = (ArrayList<String>) result.get("messages");
        Assert.assertEquals("Relation isParen is "+check+" supported", msg.get(0));
	}
	
	@Then("^Required incoming relations are (.*)$")
	public void required_incoming_relations_are_missing(String check) throws Throwable {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "{\"request\": {\"concept\": {\"objectType\": \"Concept\",\"metadata\": {\"name\": \"GeometryTest\",\"description\": \"GeometryTest\",\"code\": \"Num:C234\",\"learningObjective\": [\"\"]},\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept";
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
    	basicAssertion(resp);
    	ErrorMsgAssertion(resp);
        Map<String, Object> result = resp.getResult();
        @SuppressWarnings("unchecked")
		ArrayList<String>   msg = (ArrayList<String>) result.get("messages");
        Assert.assertEquals("Required incoming relations are "+ check, msg.get(0));       
	}
	

}
