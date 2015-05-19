package com.ilimi.taxonomy.controller.concept;

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
import com.ilimi.taxonomy.test.util.BaseIlimiTest;


@WebAppConfiguration
@RunWith(value=SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class CreateConceptTest extends BaseIlimiTest{

    @Test
    public void create(){
    	String contentString = "{\"request\": {\"concept\": {\"objectType\": \"Concept\",\"metadata\": {\"name\": \"GeometryTest\",\"description\": \"GeometryTest\",\"code\": \"Num:C234\",\"learningObjective\": [\"\"]},\"inRelations\" : [{\"startNodeId\": \"Num:C1:SC1\",\"relationType\": \"isParentOf\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept";
    	params.put("taxonomyId", "NUMERACY");
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().isAccepted());
		} catch (Exception e) {
			e.printStackTrace();
		}        
    }    
   
    @Test
    public void WithoutTaxonomyId() {
    	String contentString = "{\"request\": {\"concept\": {\"objectType\": \"Concept\",\"metadata\": {\"name\": \"GeometryTest\",\"description\": \"GeometryTest\",\"code\": \"Num:C234\","
        		+"\"learningObjective\": [\"\"]\"},\"inRelations\" : [{\"startNodeId\": \"Num:C1:SC1\",\"relationType\": \"isParentOf\"}],\"tags\": [\"tag 1\", \"tag 33\"]\"}}"
        		+"}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept";
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
    public void emptyTaxonomyId() {
    	String contentString = "{\"request\": {\"concept\": {\"objectType\": \"Concept\",\"metadata\": {\"name\": \"GeometryTest\",\"description\": \"GeometryTest\",\"code\": \"Num:C234\",\"learningObjective\": [\"\"]},\"inRelations\" : [{\"startNodeId\": \"Num:C1:SC1\",\"relationType\": \"isParentOf\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept";
    	params.put("taxonomyId", "");
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);
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
    public void blankConcept() {
    	String contentString = "\"{\"request\": {\"concept\": {}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept";
    	params.put("taxonomyId", "NUMERACY");
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);
        try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			e.printStackTrace();
		}
        try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Response resp = jasonToObject(actions);
        Assert.assertEquals("ERR_GRAPH_ADD_NODE_VALIDATION_FAILED", resp.getParams().getErr());
        Assert.assertEquals("Validation Errors", resp.getParams().getErrmsg()); 
   
        
    }
    
    @Test
    public void emptyObjectType() {
    	String contentString = "{\"request\": {\"CONCEPT\": {\"objectType\": \"\",\"metadata\": {\"name\": \"GeometryTest\",\"description\": \"GeometryTest\",\"code\": \"Num:C234\",\"learningObjective\": [\"\"]},\"inRelations\" : [{\"startNodeId\": \"Num:C1:SC1\",\"relationType\": \"isParentOf\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept";
    	params.put("taxonomyId", "NUMERACY");
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);
        try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			e.printStackTrace();
		}
        try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Response resp = jasonToObject(actions);
        Assert.assertEquals("Concept Object is blank", resp.getParams().getErrmsg());
       
        
    }
    
    @Test
    public void nodeNotFoundForObjectType()  {
    	String contentString = "{\"request\": {\"concept\": {\"objectType\": \"Concept\",\"metadata\": {\"name\": \"GeometryTest\",\"description\": \"GeometryTest\",\"code\": \"Num:C234\",\"learningObjective\": [\"\"]},\"inRelations\" : [{\"startNodeId\": \"tempNode\",\"relationType\": \"isParentOf\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept";
    	params.put("taxonomyId", "NUMERACY");
    	header.put("user-id", "jeetu");
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
        Assert.assertEquals("Node not found: tempNode", msg.get(0));
        Assert.assertEquals("Failed to update relations and tags", resp.getParams().getErrmsg());
            
    }
    
    @Test
    public void requireMetaData()  {
    	String contentString = "{\"request\": {\"concept\": {\"objectType\": \"Concept\",\"inRelations\" : [{\"startNodeId\": \"Num:C1:SC1\",\"relationType\": \"isParentOf\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept";
    	params.put("taxonomyId", "NUMERACY");
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);
        try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			e.printStackTrace();
		}
        Response resp = jasonToObject(actions);
        Assert.assertEquals("Validation Errors", resp.getParams().getErrmsg());
        Map<String, Object> result = resp.getResult();
        @SuppressWarnings("unchecked")
		ArrayList<String>   msg = (ArrayList<String>) result.get("messages");
        Assert.assertEquals("Required Metadata name not set", msg.get(0));
        Assert.assertEquals("Required Metadata code not set", msg.get(1));
        
    }
    
    @Test
    public void invalidDataType()  {
    	String contentString = "{\"request\": {\"concept\": {\"objectType\": \"Concept\",\"metadata\": {\"name\": \"GeometryTest\",\"des\": \"GeometryTest\",\"code\": \"Num:C234\",\"learningObjective\": [\"\"]},\"inRelations\" : [{\"startNodeId\": \"Num:C1:SC1\",\"relationType\": \"isParentOf\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept";
    	params.put("taxonomyId", "NUMERACY");
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);
        try {
			actions.andExpect(status().is(400));
		} catch (Exception e) {
			e.printStackTrace();
		} 
        
    }
    
    @Test
    public void unsupportedRelation()  {
    	String contentString = "{\"request\": {\"concept\": {\"objectType\": \"Concept\",\"metadata\": {\"name\": \"GeometryTest\",\"description\": \"GeometryTest\",\"code\": \"Num:C234\",\"learningObjective\": [\"\"]},\"inRelations\" : [{\"startNodeId\": \"Num:C1:SC1\",\"relationType\": \"isParen\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept";
    	params.put("taxonomyId", "NUMERACY");
    	header.put("user-id", "jeetu");
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
        Assert.assertEquals("Relation isParen is not supported", msg.get(0));
    }
    
    @Test
    public void incomingOutgoingRelation()  {
    	String contentString = "{\"request\": {\"concept\": {\"objectType\": \"Concept\",\"metadata\": {\"name\": \"GeometryTest\",\"description\": \"GeometryTest\",\"code\": \"Num:C234\",\"learningObjective\": [\"\"]},\"tags\": [\"tag 1\", \"tag 33\"]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept";
    	params.put("taxonomyId", "NUMERACY");
    	header.put("user-id", "jeetu");
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
        Assert.assertEquals("Required incoming relations are missing", msg.get(0));       
    }  
    
}
