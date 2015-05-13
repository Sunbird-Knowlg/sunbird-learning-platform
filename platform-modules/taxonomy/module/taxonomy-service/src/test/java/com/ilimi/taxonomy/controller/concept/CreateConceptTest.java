package com.ilimi.taxonomy.controller.concept;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.ilimi.graph.common.Response;

@WebAppConfiguration
@RunWith(value=SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class CreateConceptTest {
	@Autowired 
    private WebApplicationContext context;
    
    private MockMvc mockMvc;
    
    @Before
    public void setup() throws IOException {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }
    
    @org.junit.Test
    public void create() throws Exception {
    	String contentString = "{\"request\": {\"CONCEPT\": {\"objectType\": \"Concept\",\"metadata\": {\"name\": \"GeometryTest\",\"description\": \"GeometryTest\",\"code\": \"Num:C234\",\"learningObjective\": [\"\"]},\"inRelations\" : [{\"startNodeId\": \"Num:C1:SC1\",\"relationType\": \"isParentOf\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
        ResultActions actions = mockMvc.perform(post("/concept").param("taxonomyId", "NUMERACY").contentType(MediaType.APPLICATION_JSON)
        		.content(contentString.getBytes()).header("user-id", "jeetu"));
        
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().isOk());
    }
    
    @org.junit.Test
    public void WithoutTaxonomyId() throws Exception {
    	String contentString = "{\"request\": {\"CONCEPT\": {\"objectType\": \"Concept\",\"metadata\": {\"name\": \"GeometryTest\",\"description\": \"GeometryTest\",\"code\": \"Num:C234\","
        		+"\"learningObjective\": [\"\"]\"},\"inRelations\" : [{\"startNodeId\": \"Num:C1:SC1\",\"relationType\": \"isParentOf\"}],\"tags\": [\"tag 1\", \"tag 33\"]\"}}"
        		+"}";
        ResultActions actions = mockMvc.perform(post("/concept").header("Content-Type", "application/json").contentType(MediaType.APPLICATION_JSON)
                .content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().is(400));
        Assert.assertEquals("Required String parameter 'taxonomyId' is not present", actions.andReturn().getResponse().getErrorMessage());
    }
    
    
    @org.junit.Test
    public void emptyTaxonomyId() throws Exception {
    	String contentString = "{\"request\": {\"CONCEPT\": {\"objectType\": \"Concept\",\"metadata\": {\"name\": \"GeometryTest\",\"description\": \"GeometryTest\",\"code\": \"Num:C234\",\"learningObjective\": [\"\"]},\"inRelations\" : [{\"startNodeId\": \"Num:C1:SC1\",\"relationType\": \"isParentOf\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
        ResultActions actions = mockMvc.perform(post("/concept").param("taxonomyId", "").header("Content-Type", "application/json").contentType(MediaType.APPLICATION_JSON)
                .content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().is(400));        
        String content = (String) actions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Response resp = objectMapper.readValue(content, Response.class);
//        Assert.assertEquals("Taxonomy Id is blank", resp.getParams().get("errmsg"));
    }
    
    
    @org.junit.Test
    public void blankConcept() throws Exception {
        ResultActions actions = mockMvc.perform(post("/concept").param("taxonomyId", "NUMERACY").header("Content-Type", "application/json").contentType(MediaType.APPLICATION_JSON)
                .content("{\"request\": {\"CONCEPT\": {}}}".getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().is(400));
        String content = (String) actions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Response resp = objectMapper.readValue(content, Response.class);
//        Assert.assertEquals("Validation Errors", resp.getParams().get("errmsg"));
   
        
    }
    
    @org.junit.Test
    public void emptyObjectType() throws Exception {
    	String contentString = "{\"request\": {\"CONCEPT\": {\"objectType\": \"\",\"metadata\": {\"name\": \"GeometryTest\",\"description\": \"GeometryTest\",\"code\": \"Num:C234\",\"learningObjective\": [\"\"]},\"inRelations\" : [{\"startNodeId\": \"Num:C1:SC1\",\"relationType\": \"isParentOf\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
        ResultActions actions = mockMvc.perform(post("/concept").param("taxonomyId", "NUMERACY").header("Content-Type", "application/json").contentType(MediaType.APPLICATION_JSON)
                .content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().is(400));
        String content = (String) actions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Response resp = objectMapper.readValue(content, Response.class);
//        Assert.assertEquals("Validation Errors", resp.getParams().get("errmsg"));
       
        
    }
    
    @org.junit.Test
    public void nodeNotFoundForObjectType() throws Exception {
    	String contentString = "{\"request\": {\"CONCEPT\": {\"objectType\": \"Concept\",\"metadata\": {\"name\": \"GeometryTest\",\"description\": \"GeometryTest\",\"code\": \"Num:C234\",\"learningObjective\": [\"\"]},\"inRelations\" : [{\"startNodeId\": \"tempNode\",\"relationType\": \"isParentOf\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
        ResultActions actions = mockMvc.perform(post("/concept").param("taxonomyId", "NUMERACY").header("Content-Type", "application/json").contentType(MediaType.APPLICATION_JSON)
                .content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().is(400));
        String content = (String) actions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Response resp = objectMapper.readValue(content, Response.class);
//        Assert.assertEquals("Failed to update relations and tags", resp.getParams().get("errmsg"));
               
        
    }
    
    @org.junit.Test
    public void requireMetaData() throws Exception {
    	String contentString = "{\"request\": {\"CONCEPT\": {\"objectType\": \"Concept\",\"inRelations\" : [{\"startNodeId\": \"Num:C1:SC1\",\"relationType\": \"isParentOf\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
        ResultActions actions = mockMvc.perform(post("/concept").param("taxonomyId", "NUMERACY").header("Content-Type", "application/json").contentType(MediaType.APPLICATION_JSON)
                .content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().is(400));      
        String content = (String) actions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Response resp = objectMapper.readValue(content, Response.class);
//        Assert.assertEquals("Validation Errors", resp.getParams().get("errmsg"));
        
    }
    
    // Validation Errors TEST CASES
    @org.junit.Test
    public void invalidDataType() throws Exception {
    	String contentString = "{\"request\": {\"CONCEPT\": {\"objectType\": \"asdf\",\"metadata\": {\"name\": \"GeometryTest\",\"description\": \"GeometryTest\",\"code\": \"Num:C234\",\"learningObjective\": [\"\"]},\"inRelations\" : [{\"startNodeId\": \"Num:C1:SC1\",\"relationType\": \"isParentOf\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
        ResultActions actions = mockMvc.perform(post("/concept").param("taxonomyId", "NUMERACY").header("Content-Type", "application/json").contentType(MediaType.APPLICATION_JSON)
                .content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().is(400));        
        
    }
    
    @org.junit.Test
    public void unsupportedRelation() throws Exception {
    	String contentString = "{\"request\": {\"CONCEPT\": {\"objectType\": \"Concept\",\"metadata\": {\"name\": \"GeometryTest\",\"description\": \"GeometryTest\",\"code\": \"Num:C234\",\"learningObjective\": [\"\"]},\"inRelations\" : [{\"startNodeId\": \"Num:C1:SC1\",\"relationType\": \"isParen\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
        ResultActions actions = mockMvc.perform(post("/concept").param("taxonomyId", "NUMERACY").header("Content-Type", "application/json").contentType(MediaType.APPLICATION_JSON)
                .content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().is(400));        
        
    }
    
    @org.junit.Test
    public void incomingOutgoingRelation() throws Exception {
    	String contentString = "{\"request\": {\"CONCEPT\": {\"objectType\": \"Concept\",\"metadata\": {\"name\": \"GeometryTest\",\"description\": \"GeometryTest\",\"code\": \"Num:C234\",\"learningObjective\": [\"\"]},\"tags\": [\"tag 1\", \"tag 33\"]}}}";
        ResultActions actions = mockMvc.perform(post("/concept").param("taxonomyId", "NUMERACY").header("Content-Type", "application/json").contentType(MediaType.APPLICATION_JSON)
                .content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().is(400)); 
        String content = (String) actions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Response resp = objectMapper.readValue(content, Response.class);
//        Assert.assertEquals("ERR_GRAPH_ADD_NODE_ERROR", resp.getParams().get("err"));
//        @SuppressWarnings("unchecked")
//		HashMap<String, String> map = (HashMap<String, String>)resp.getParams().get("MESSAGES");
//        Object message = map.get("valueObjectList");
        
        
    }
    
    
    
    
    
    
    
    
    
    
}
