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
public class UpdateGameTest extends BaseIlimiTest{

    
    @Test
    public void updateGame() {
    	String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"Game\",\"graphId\": \"NUMERACY\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\"}}}}";
        Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object/G12";
    	params.put("taxonomyId", "NUMERACY");
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPatch(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().isAccepted());
		} catch (Exception e) {
			e.printStackTrace();
		}    
    }
    
    @Test
    public void emptytaxonomyId() {
    	String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"Game\",\"graphId\": \"NUMERACY\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\"}}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object/G1";
    	params.put("taxonomyId", "");
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPatch(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			e.printStackTrace();
		}
        Response resp = jasonToObject(actions);
        Assert.assertEquals("Taxonomy Id is blank", resp.getParams().getErrmsg());
        Assert.assertEquals("ERR_TAXONOMY_BLANK_TAXONOMY_ID", resp.getParams().getErr());
    }
    
    @Test
    public void withoutTaxonomyId() {
    	String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"Game\",\"graphId\": \"NUMERACY\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\"}}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object/G1";
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
    public void blankGameObject() {
    	String contentString = "{\"request\": {}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object/G1";
    	params.put("taxonomyId", "NUMERACY");
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPatch(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			e.printStackTrace();
		}
        Response resp = jasonToObject(actions);
        Assert.assertEquals("Learning Object is blank", resp.getParams().getErrmsg());
        Assert.assertEquals("ERR_LOB_BLANK_LEARNING_OBJECT", resp.getParams().getErr());
    }
    
    
    @Test
    public void gameNotFound() {
    	String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"Game\",\"graphId\": \"NUMERACY\",\"identifier\": \"G98\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\"}}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object/G123";
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
    }
    
    @Test
    public void emptyObjectType() {
    	String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"\",\"graphId\": \"NUMERACY\",\"identifier\": \"G1\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\"}}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object/G123";
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
        Assert.assertEquals("Object type not set for node: G1", msg.get(0)); 
        Assert.assertEquals("Node Metadata validation failed", resp.getParams().getErrmsg());
    }
    
    @Test
    public void requireMetaData() {
    	String contentString = "{\"request\": {\"learning_object\": {\"objectType\": \"Game\",\"graphId\": \"NUMERACY\",\"identifier\": \"G1\",\"nodeType\": \"DATA_NODE\",\"metadata\": {}}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object/G1";
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
        Assert.assertEquals("Required Metadata name not set", msg.get(0)); 
        Assert.assertEquals("Node Metadata validation failed", resp.getParams().getErrmsg());
        Assert.assertEquals("ERR_GRAPH_UPDATE_NODE_VALIDATION_FAILED", resp.getParams().getErr());
    }
    

}
