package com.ilimi.taxonomy.controller.concept.cucumber.test;

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

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class UpdateConceptTest extends CucumberBaseTestIlimi{
	
	private String taxonomyId;
	private String conceptId;
	
	@Before
    public void setup() throws IOException {
        initMockMVC();
    }
	
	@When("^i give Taxonomy ID (.*) and concept is (.*)$")
	public void getInputData(String taxonomyId, String conceptId){
		if(taxonomyId.equals("absent"))
			this.taxonomyId = "absent";
		if(taxonomyId.equals("empty"))
			this.taxonomyId = "";
		else
			this.taxonomyId = taxonomyId;
		this.conceptId = conceptId;
	}
	
	@Then("^update a Concept and get the status (.*)$")
    public void createConcept(String status) throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "{\"request\": {\"concept\": {\"identifier\": \"Num:C1\",\"nodeType\": \"DATA_NODE\",\"objectType\": \"Concept\",\"metadata\": {\"identifier\": \"Num:C1\",\"code\": \"Num:C1\",\"learningObjective\": [\"New Learning Objective 222\"],\"arrayProp\": [\"value1\", \"value3\", \"valu5\"]},\"tags\": [\"Subconcept\", \"tag 9\", \"tag 10\"]},\"COMMENT\" : \"\"}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/" + conceptId;
    	params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPatch(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(202));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Then("^i should get Error Message Taxonomy Id is (.*) and status is (\\d+)$")
    public void getConceptWithoutOrEmptyTaxonomy(String errmsg, int status) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "{\"request\": {\"concept\": {\"identifier\": \"Num:C1\",\"nodeType\": \"DATA_NODE\",\"objectType\": \"Concept\",\"metadata\": {\"identifier\": \"Num:C1\",\"code\": \"Num:C1\",\"learningObjective\": [\"New Learning Objective 222\"],\"arrayProp\": [\"value1\", \"value3\", \"valu5\"]},\"tags\": [\"Subconcept\", \"tag 9\", \"tag 10\"]},\"COMMENT\" : \"\"}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/" + conceptId;
    	if(taxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "jeetu");
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
        	Assert.assertEquals(resp.getParams().getErrmsg(), "Taxonomy Id is " + errmsg);
        }               
    }
	
	@Then("^i should get errMsg is Validation (.*)$")
	public void i_should_get_errMsg_is_Validation_Error(String error) throws Throwable {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "\"{\"request\": {\"concept\": {}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/" + conceptId;
    	if(taxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPatch(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc); 
    	try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			System.out.println();
			e.printStackTrace();
		} 
         
	}
	
	@Then("^i should get errMsg is Concept Object is (.*)$")
	public void i_should_get_errMsg_is_Concept_Object_is_blank(String conceptObject) throws Throwable {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "{\"request\": {\"CONCEPT\": {\"objectType\": \"\",\"metadata\": {\"name\": \"GeometryTest\",\"description\": \"GeometryTest\",\"code\": \"Num:C234\",\"learningObjective\": [\"\"]},\"inRelations\" : [{\"startNodeId\": \"Num:C1:SC1\",\"relationType\": \"isParentOf\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/" + conceptId;
    	if(taxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPatch(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc); 
    	try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			System.out.println();
			e.printStackTrace();
		} 
    	Response resp = jasonToObject(actions);
        Assert.assertEquals("Concept Object is " + conceptObject, resp.getParams().getErrmsg());         
	}
	
	@Then("^i should get errMsg is metadata (.*) is not set$")
	public void i_should_get_errMsg_is_metadata_name_is_not_set(String name) throws Throwable {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "{\"request\": {\"concept\": {\"objectType\": \"Concept\",\"inRelations\" : [{\"startNodeId\": \"Num:C1:SC1\",\"relationType\": \"isParentOf\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/" + conceptId;
    	if(taxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPatch(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc); 
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
	
	@Then("^i should get errMsg is Relation is (.*) supported$")
	public void i_should_get_errMsg_is_Relation_is_not_supported(String check) throws Throwable {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "{\"request\": {\"concept\": {\"objectType\": \"Concept\",\"metadata\": {\"name\": \"GeometryTest\",\"description\": \"GeometryTest\",\"code\": \"Num:C234\",\"learningObjective\": [\"\"]},\"inRelations\" : [{\"startNodeId\": \"Num:C1:SC1\",\"relationType\": \"isParen\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/" + conceptId;
    	if(taxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPatch(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc); 
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
}
