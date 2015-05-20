package com.ilimi.taxonomy.controller.concept;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.ilimi.common.dto.Response;
import com.ilimi.taxonomy.base.test.BaseIlimiTest;

@WebAppConfiguration
@RunWith(value=SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class UpadateConceptTest extends BaseIlimiTest{
    
	private void basicAssertion(Response resp){
		Assert.assertEquals("ekstep.lp.concept.update", resp.getId());
        Assert.assertEquals("1.0", resp.getVer());
	}
	
    @Test
    public void updateConcept() {
    	String contentString = "{\"request\": {\"concept\": {\"identifier\": \"Num:C1\",\"nodeType\": \"DATA_NODE\",\"objectType\": \"Concept\",\"metadata\": {\"identifier\": \"Num:C1\",\"code\": \"Num:C1\",\"learningObjective\": [\"New Learning Objective 222\"],\"arrayProp\": [\"value1\", \"value3\", \"valu5\"]},\"tags\": [\"Subconcept\", \"tag 9\", \"tag 10\"]},\"COMMENT\" : \"\"}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/Num:C1";
    	params.put("taxonomyId", "NUMERACY");
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPatch(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().isAccepted());
		} catch (Exception e) {
			e.printStackTrace();
		} 
        Response resp = jasonToObject(actions);
        basicAssertion(resp);
        Assert.assertEquals("SUCCESS", resp.getParams().getStatus());
    }
    
    @Test
    public void withoutTaxonomyId()  {
    	String contentString = "{\"request\": {\"concept\": {\"identifier\": \"Num:C1\",\"nodeType\": \"DATA_NODE\",\"objectType\": \"Concept\",\"metadata\": {\"identifier\": \"Num:C1\",\"code\": \"Num:C1\",\"learningObjective\": [\"New Learning Objective 222\"],\"arrayProp\": [\"value1\", \"value3\", \"valu5\"]},\"tags\": [\"Subconcept\", \"tag 9\", \"tag 10\"]},\"COMMENT\" : \"\"}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/Num:C1";
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPatch(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			e.printStackTrace();
		}      
        Assert.assertEquals("Required String parameter 'taxonomyId' is not present", actions.andReturn().getResponse().getErrorMessage());
    }
    
    @Test
    public void emptyTaxonomyId()  {
    	String contentString = "{\"request\": {\"concept\": {\"identifier\": \"Num:C1\",\"nodeType\": \"DATA_NODE\",\"objectType\": \"Concept\",\"metadata\": {\"identifier\": \"Num:C1\",\"code\": \"Num:C1\",\"learningObjective\": [\"New Learning Objective 222\"],\"arrayProp\": [\"value1\", \"value3\", \"valu5\"]},\"tags\": [\"Subconcept\", \"tag 9\", \"tag 10\"]},\"COMMENT\" : \"\"}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/Num:C1";
    	params.put("taxonomyId", "");
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPatch(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			e.printStackTrace();
		}
        Response resp = jasonToObject(actions);
        basicAssertion(resp);
        Assert.assertEquals("Taxonomy Id is blank", resp.getParams().getErrmsg());
        Assert.assertEquals("ERR_TAXONOMY_BLANK_TAXONOMY_ID", resp.getParams().getErr());
    }
    
    @Test
    public void blankConceptObject()  {
    	String contentString = "{\"request\": {}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/Num:C1";
    	params.put("taxonomyId", "NUMERACY");
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPatch(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			e.printStackTrace();
		}
        Response resp = jasonToObject(actions);
        basicAssertion(resp);
        Assert.assertEquals("Concept Object is blank", resp.getParams().getErrmsg());
        Assert.assertEquals("ERR_TAXONOMY_BLANK_CONCEPT", resp.getParams().getErr());
    }
    
   
    @Test
    public void conceptObjectNotFound()  {
    	String contentString = "{\"request\": {\"concept\": {\"identifier\": \"ilimi\",\"nodeType\": \"DATA_NODE\",\"objectType\": \"sadf\",\"metadata\": {\"identifier\": \"Num:C1\",\"code\": \"Num:C1\",\"learningObjective\": [\"New Learning Objective 222\"],\"arrayProp\": [\"value1\", \"value3\", \"valu5\"]},\"tags\": [\"Subconcept\", \"tag 9\", \"tag 10\"]},\"COMMENT\" : \"\"}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/Num:C1";
    	params.put("taxonomyId", "NUMERACY");
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPatch(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(404));
		} catch (Exception e) {
			e.printStackTrace();
		}
        Response resp = jasonToObject(actions);
        Assert.assertEquals("Node Not Found", resp.getParams().getErrmsg());
        Assert.assertEquals("ERR_GRAPH_UPDATE_NODE_NOT_FOUND", resp.getParams().getErr());
        Map<String, Object> result = resp.getResult();
        @SuppressWarnings("unchecked")
		ArrayList<String>   msg = (ArrayList<String>) result.get("messages");
        Assert.assertEquals("Node not found", msg.get(0));

    }
    
    @Test
    public void emptyObjectType()  {
    	String contentString = "{\"request\": {\"concept\": {\"identifier\": \"Num:C1\",\"nodeType\": \"DATA_NODE\",\"objectType\": \"\",\"metadata\": {\"identifier\": \"Num:C1\",\"code\": \"Num:C1\",\"learningObjective\": [\"New Learning Objective 222\"],\"arrayProp\": [\"value1\", \"value3\", \"valu5\"]},\"tags\": [\"Subconcept\", \"tag 9\", \"tag 10\"]},\"COMMENT\" : \"\"}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/Num:C1";
    	params.put("taxonomyId", "NUMERACY");
    	header.put("user-id", "jeetu");
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
        Assert.assertEquals("Object type not set for node: Num:C1", msg.get(0)); 
        Assert.assertEquals("Node Metadata validation failed", resp.getParams().getErrmsg());
    } 
    
    @Test
    public void nodeNotFoundForObjectType()  {
    	String contentString = "{\"request\": {\"concept\": {\"identifier\": \"jeetu\",\"nodeType\": \"DATA_NODE\",\"objectType\": \"Concept\",\"metadata\": {\"identifier\": \"Num:C1\",\"code\": \"Num:C1\",\"learningObjective\": [\"New Learning Objective 222\"],\"arrayProp\": [\"value1\", \"value3\", \"valu5\"]},\"tags\": [\"Subconcept\", \"tag 9\", \"tag 10\"]},\"COMMENT\" : \"\"}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/Num:C1";
    	params.put("taxonomyId", "NUMERACY");
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPatch(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(404));
		} catch (Exception e) {
			e.printStackTrace();
		}
        Response resp = jasonToObject(actions);
        Map<String, Object> result = resp.getResult();
        @SuppressWarnings("unchecked")
		ArrayList<String>   msg = (ArrayList<String>) result.get("messages");
        Assert.assertEquals("Node Not Found", msg.get(0)); 
        Assert.assertEquals("Node Not Found", resp.getParams().getErrmsg());
        Assert.assertEquals("ERR_GRAPH_UPDATE_NODE_NOT_FOUND", resp.getParams().getErr());
    }
    
    @Test
    public void requireMetaData()  {
    	String contentString = "{\"request\": {\"concept\": {\"identifier\": \"Num:C1\",\"nodeType\": \"DATA_NODE\",\"objectType\": \"Concept\",\"metadata\": {\"identifier\": \"Num:C1\",\"code\": \"\",\"learningObjective\": [\"New Learning Objective 222\"],\"arrayProp\": [\"value1\", \"value3\", \"valu5\"]},\"tags\": [\"Subconcept\", \"tag 9\", \"tag 10\"]},\"COMMENT\" : \"\"}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/Num:C1";
    	params.put("taxonomyId", "NUMERACY");
    	header.put("user-id", "jeetu");
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
        Assert.assertEquals("Required Metadata code not set", msg.get(0)); 
        Assert.assertEquals("Node Metadata validation failed", resp.getParams().getErrmsg());
        Assert.assertEquals("ERR_GRAPH_UPDATE_NODE_VALIDATION_FAILED", resp.getParams().getErr());
    }
    
    @Test
    public void invalidDataType() {
    	String contentString = "{\"request\": {\"concept\": {\"identifier\": \"Num:C1\",\"nodeType\": \"DATA_NODE\",\"objectType\": \"Concept\",\"metadata\": {\"identifier\": \"Num:C1\",\"code\": \"Num:C1\", \"status\": 123, \"learningObjective\": [\"New Learning Objective 222\"],\"arrayProp\": [\"value1\", \"value3\", \"valu5\"]},\"tags\": [\"Subconcept\", \"tag 9\", \"tag 10\"]},\"COMMENT\" : \"\"}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/Num:C1";
    	params.put("taxonomyId", "NUMERACY");
    	header.put("user-id", "jeetu");
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
        Assert.assertEquals("Metadata status should be one of: [Draft, Active, Retired]", msg.get(0)); 
        Assert.assertEquals("Node Metadata validation failed", resp.getParams().getErrmsg());
        Assert.assertEquals("ERR_GRAPH_UPDATE_NODE_VALIDATION_FAILED", resp.getParams().getErr());
    }
    
 
}
