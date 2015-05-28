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

import com.ilimi.common.dto.Response;
import com.ilimi.taxonomy.base.test.BaseCucumberTest;

import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class UpdateConceptTest extends BaseCucumberTest{
	
	private String taxonomyId;
	private String conceptId;
	
	private void basicAssertion(Response resp){
		Assert.assertEquals("ekstep.lp.concept.update", resp.getId());
        Assert.assertEquals("1.0", resp.getVer());
	}
	
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
    public void createConcept(String status) {
		String contentString = "{\"request\": {\"concept\": {\"identifier\": \"Num:C1\",\"nodeType\": \"DATA_NODE\",\"objectType\": \"Concept\",\"metadata\": {\"identifier\": \"Num:C1\",\"code\": \"Num:C1\",\"learningObjective\": [\"New Learning Objective 222\"],\"arrayProp\": [\"value1\", \"value3\", \"valu5\"]},\"tags\": [\"Subconcept\", \"tag 9\", \"tag 10\"]},\"COMMENT\" : \"\"}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/" + conceptId;
    	params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "ilimi");
    	ResultActions actions = resultActionPatch(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(200));
		} catch (Exception e) {
			e.printStackTrace();
		}
        Response resp = jasonToObject(actions);
        basicAssertion(resp);
        Assert.assertEquals(status, resp.getParams().getStatus());
	}
	
	@Then("^i should get Error Message Taxonomy Id is (.*) and status is (\\d+)$")
    public void getConceptWithoutOrEmptyTaxonomy(String errmsg, int status) {
		String contentString = "{\"request\": {\"concept\": {\"identifier\": \"Num:C1\",\"nodeType\": \"DATA_NODE\",\"objectType\": \"Concept\",\"metadata\": {\"identifier\": \"Num:C1\",\"code\": \"Num:C1\",\"learningObjective\": [\"New Learning Objective 222\"],\"arrayProp\": [\"value1\", \"value3\", \"valu5\"]},\"tags\": [\"Subconcept\", \"tag 9\", \"tag 10\"]},\"COMMENT\" : \"\"}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/" + conceptId;
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
        	Assert.assertEquals("ERR_TAXONOMY_BLANK_TAXONOMY_ID", resp.getParams().getErr());
        	Assert.assertEquals(resp.getParams().getErrmsg(), "Taxonomy Id is " + errmsg);
        }               
    }
	
	@Then("^i should get errMsg is concept object is (.*)$")
	public void i_should_get_errMsg_is_Validation_Error(String error) {
		String contentString =  "{\"request\": {}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/" + conceptId;
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
        Assert.assertEquals("Concept Object is "+error, resp.getParams().getErrmsg());
        Assert.assertEquals("ERR_TAXONOMY_BLANK_CONCEPT", resp.getParams().getErr());         
	}
	
	@Then("i should get errMsg is node (.*) found")
	public void nodeNotFound(String error){
		String contentString = "{\"request\": {\"concept\": {\"identifier\": \"ilimi\",\"nodeType\": \"DATA_NODE\",\"objectType\": \"sadf\",\"metadata\": {\"identifier\": \"Num:C1\",\"code\": \"Num:C1\",\"learningObjective\": [\"New Learning Objective 222\"],\"arrayProp\": [\"value1\", \"value3\", \"valu5\"]},\"tags\": [\"Subconcept\", \"tag 9\", \"tag 10\"]},\"COMMENT\" : \"\"}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/" + conceptId;
    	if(taxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", taxonomyId);
    	header.put("user-id", "ilimi");
    	ResultActions actions = resultActionPatch(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(404));
		} catch (Exception e) {
			e.printStackTrace();
		}
        Response resp = jasonToObject(actions);
        Assert.assertEquals("Node "+error+" Found", resp.getParams().getErrmsg());
        Assert.assertEquals("ERR_GRAPH_UPDATE_NODE_NOT_FOUND", resp.getParams().getErr());
        Map<String, Object> result = resp.getResult();
	}
	
	@Then("^i should get errMsg is concept object type is (.*)$")
	public void i_should_get_errMsg_is_Concept_Object_is_blank(String conceptObject) {
		String contentString = "{\"request\": {\"concept\": {\"identifier\": \"Num:C1\",\"nodeType\": \"DATA_NODE\",\"objectType\": \"\",\"metadata\": {\"identifier\": \"Num:C1\",\"code\": \"Num:C1\",\"learningObjective\": [\"New Learning Objective 222\"],\"arrayProp\": [\"value1\", \"value3\", \"valu5\"]},\"tags\": [\"Subconcept\", \"tag 9\", \"tag 10\"]},\"COMMENT\" : \"\"}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/" + conceptId;
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
        Map<String, Object> result = resp.getResult();
        @SuppressWarnings("unchecked")
		ArrayList<String>   msg = (ArrayList<String>) result.get("messages");
        Assert.assertEquals("Object type not set for node: Num:C1", msg.get(0)); 
        Assert.assertEquals("Node Metadata validation failed", resp.getParams().getErrmsg());
	}
	
	@Then("^i should get errMsg is metadata (.*) is not set$")
	public void i_should_get_errMsg_is_metadata_name_is_not_set(String name) {
		String contentString = "{\"request\": {\"concept\": {\"identifier\": \"Num:C1\",\"nodeType\": \"DATA_NODE\",\"objectType\": \"Concept\",\"metadata\": {\"identifier\": \"Num:C1\",\"code\": \"\",\"learningObjective\": [\"New Learning Objective 222\"],\"arrayProp\": [\"value1\", \"value3\", \"valu5\"]},\"tags\": [\"Subconcept\", \"tag 9\", \"tag 10\"]},\"COMMENT\" : \"\"}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/" + conceptId;
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
    	Map<String, Object> result = resp.getResult();
        @SuppressWarnings("unchecked")
		ArrayList<String>   msg = (ArrayList<String>) result.get("messages");
        Assert.assertEquals("Required Metadata "+name+" not set", msg.get(0)); 
        Assert.assertEquals("Node Metadata validation failed", resp.getParams().getErrmsg());
        Assert.assertEquals("ERR_GRAPH_UPDATE_NODE_VALIDATION_FAILED", resp.getParams().getErr());
	}
	
	@Then("^i should get errMsg is invalid node$")
	public void i_should_get_errMsg_is_Relation_is_not_supported() {
		String contentString = "{\"request\": {\"concept\": {\"identifier\": \"Num:C1\",\"nodeType\": \"DATA_NODE\",\"objectType\": \"Concept\",\"metadata\": {\"identifier\": \"Num:C1\",\"code\": \"Num:C1\", \"status\": 123, \"learningObjective\": [\"New Learning Objective 222\"],\"arrayProp\": [\"value1\", \"value3\", \"valu5\"]},\"tags\": [\"Subconcept\", \"tag 9\", \"tag 10\"]},\"COMMENT\" : \"\"}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/" + conceptId;
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
        Map<String, Object> result = resp.getResult();
        @SuppressWarnings("unchecked")
		ArrayList<String>   msg = (ArrayList<String>) result.get("messages");
        Assert.assertEquals("Metadata status should be one of: [Draft, Active, Retired]", msg.get(0)); 
        Assert.assertEquals("Node Metadata validation failed", resp.getParams().getErrmsg());
        Assert.assertEquals("ERR_GRAPH_UPDATE_NODE_VALIDATION_FAILED", resp.getParams().getErr());
	}
}
