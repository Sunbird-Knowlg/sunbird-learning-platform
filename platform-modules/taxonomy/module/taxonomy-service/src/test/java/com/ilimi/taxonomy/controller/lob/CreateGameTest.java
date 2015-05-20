package com.ilimi.taxonomy.controller.lob;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import com.ilimi.common.dto.Response;
import com.ilimi.taxonomy.base.test.BaseIlimiTest;

@WebAppConfiguration
@RunWith(value=SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class CreateGameTest extends BaseIlimiTest{
    
	private void basicAssertion(Response resp){
		Assert.assertEquals("ekstep.lp.learning-object.create", resp.getId());
        Assert.assertEquals("1.0", resp.getVer());
	}
	
    @Test
    public void create() {
    	String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"Game\",\"graphId\": \"NUMERACY\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\",\"developer\" : \"Play Store\",\"owner\"     : \"Google & its Developers\"},\"inRelations\" : [{\"startNodeId\": \"G1\",\"relationType\": \"associatedTo\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
        Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object";
    	params.put("taxonomyId", "NUMERACY");
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
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
    public void emptyTaxonomyId() {
    	String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"Game\",\"graphId\": \"NUMERACY\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\",\"developer\" : \"Play Store\",\"owner\"     : \"Google & its Developers\"},\"inRelations\" : [{\"startNodeId\": \"G1\",\"relationType\": \"associatedTo\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object";
    	params.put("taxonomyId", "");
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
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
    public void withoutTaxonomyId() {
    	String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"Game\",\"graphId\": \"NUMERACY\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\",\"developer\" : \"Play Store\",\"owner\"     : \"Google & its Developers\"},\"inRelations\" : [{\"startNodeId\": \"G1\",\"relationType\": \"associatedTo\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object";
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			e.printStackTrace();
		}      
        Assert.assertEquals("Required String parameter 'taxonomyId' is not present", actions.andReturn().getResponse().getErrorMessage());        
    }
    
    @Test
    public void blankGameObject() {
    	String contentString = "{\"request\": {}}";
    	Map<String, String> params = new HashMap<String, String>();
     	Map<String, String> header = new HashMap<String, String>();
     	String path = "/learning-object";
     	params.put("taxonomyId", "NUMERACY");
     	header.put("user-id", "jeetu");
     	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
         try {
 			actions.andExpect(status().is(400));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}  
         Response resp = jasonToObject(actions);
         basicAssertion(resp);
         Assert.assertEquals("Learning Object is blank", resp.getParams().getErrmsg());
         Assert.assertEquals("ERR_LOB_BLANK_LEARNING_OBJECT", resp.getParams().getErr());
    }
    
    @Test
    public void emptyObjectType() {
    	String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"\",\"graphId\": \"NUMERACY\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\",\"developer\" : \"Play Store\",\"owner\"     : \"Google & its Developers\"},\"inRelations\" : [{\"startNodeId\": \"G1\",\"relationType\": \"associatedTo\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
     	Map<String, String> header = new HashMap<String, String>();
     	String path = "/learning-object";
     	params.put("taxonomyId", "NUMERACY");
     	header.put("user-id", "jeetu");
     	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
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
        Assert.assertEquals("Object type not set for node: G99", msg.get(0));
        Assert.assertEquals("ERR_GRAPH_ADD_NODE_VALIDATION_FAILED", resp.getParams().getErr());         
        Assert.assertEquals("Validation Errors", resp.getParams().getErrmsg());
    }
    
    
    @Test
    public void definationNodeNotFound() {
    	String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"Game\",\"graphId\": \"NUMERACY\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\",\"developer\" : \"Play Store\",\"owner\" : \"Google & its Developers\"},\"inRelations\" : [{\"startNodeId\": \"G1\",\"relationType\": \"associatedTo\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
     	Map<String, String> header = new HashMap<String, String>();
     	String path = "/learning-object";
     	params.put("taxonomyId", "NUMERACY");
     	header.put("user-id", "jeetu");
     	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
         try {
 			actions.andExpect(status().is(400));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}  
        Response resp = jasonToObject(actions);
        basicAssertion(resp);
    }
        
    @Test
    public void requireMetaData() {
    	String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"Game\",\"graphId\": \"NUMERACY\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"inRelations\" : [{\"startNodeId\": \"G1\",\"relationType\": \"associatedTo\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
     	Map<String, String> header = new HashMap<String, String>();
     	String path = "/learning-object";
     	params.put("taxonomyId", "NUMERACY");
     	header.put("user-id", "jeetu");
     	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
         try {
 			actions.andExpect(status().is(400));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}          
        Response resp = jasonToObject(actions);
        basicAssertion(resp);
        Assert.assertEquals("Validation Errors", resp.getParams().getErrmsg());
        Map<String, Object> result = resp.getResult();
        @SuppressWarnings("unchecked")
		ArrayList<String>   msg = (ArrayList<String>) result.get("messages");
        Assert.assertEquals("Required Metadata name not set", msg.get(0));
        Assert.assertEquals("Required Metadata code not set", msg.get(1));
        Assert.assertEquals("Required Metadata developer not set", msg.get(2));
        Assert.assertEquals("Required Metadata owner not set", msg.get(3));
    }
    
    @Test
    public void invalidDataType() {
    	String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"Game\",\"graphId\": \"NUMERACY\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"status\": 123,\"code\": \"ek.lit.an\",\"developer\" : \"Play Store\",\"owner\"     : \"Google & its Developers\", \"interactivityLevel\" : \"s\"},\"inRelations\" : [{\"startNodeId\": \"G1\",\"relationType\": \"associatedTo\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
     	Map<String, String> header = new HashMap<String, String>();
     	String path = "/learning-object";
     	params.put("taxonomyId", "NUMERACY");
     	header.put("user-id", "jeetu");
     	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
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
        Assert.assertEquals("Metadata status should be one of: [Draft, Review, Active, Retired, Mock]", msg.get(0)); 
        Assert.assertEquals("Validation Errors", resp.getParams().getErrmsg());
        Assert.assertEquals("ERR_GRAPH_ADD_NODE_VALIDATION_FAILED", resp.getParams().getErr());
    }
    
    @Test
    public void unsupportedRelation() {
    	String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"Game\",\"graphId\": \"NUMERACY\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\",\"developer\" : \"Play Store\",\"owner\": \"Google & its Developers\"},\"inRelations\" : [{\"startNodeId\": \"G1\",\"relationType\": \"isParent\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
     	Map<String, String> header = new HashMap<String, String>();
     	String path = "/learning-object";
     	params.put("taxonomyId", "NUMERACY");
     	header.put("user-id", "jeetu");
     	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
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
        Assert.assertEquals("Relation isParent is not supported", msg.get(0));
    }
}
