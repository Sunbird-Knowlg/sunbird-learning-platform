package com.ilimi.taxonomy.learningobjectcontroller.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

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
public class CreateGameTest {
	@Autowired 
    private WebApplicationContext context;
    
    private MockMvc mockMvc;
    
    @Before
    public void setup() throws IOException {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }
    
    
    @org.junit.Test
    public void create() throws Exception {
    	String contentString = "{\"request\": {\"LEARNING_OBJECT\": {\"objectType\": \"Game\",\"graphId\": \"NUMERACY\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\",\"developer\" : \"Play Store\",\"owner\"     : \"Google & its Developers\"},\"inRelations\" : [{\"startNodeId\": \"G1\",\"relationType\": \"associatedTo\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
        ResultActions actions = mockMvc.perform(post("/learning-object/").param("taxonomyId", "NUMERACY").contentType(MediaType.APPLICATION_JSON).content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().isOk());
    }
    
    @org.junit.Test
    public void emptyTaxonomyId() throws Exception {
    	String contentString = "{\"request\": {\"LEARNING_OBJECT\": {\"objectType\": \"Game\",\"graphId\": \"NUMERACY\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\",\"developer\" : \"Play Store\",\"owner\"     : \"Google & its Developers\"},\"inRelations\" : [{\"startNodeId\": \"G1\",\"relationType\": \"associatedTo\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
        ResultActions actions = mockMvc.perform(post("/learning-object/").param("taxonomyId", "").contentType(MediaType.APPLICATION_JSON).content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().is(400));
        String content = (String) actions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Response resp = objectMapper.readValue(content, Response.class);
        Assert.assertEquals("Taxonomy Id is blank", resp.getParams().get("errmsg"));
    }
    
    @org.junit.Test
    public void withoutTaxonomyId() throws Exception {
    	String contentString = "{\"request\": {\"LEARNING_OBJECT\": {\"objectType\": \"Game\",\"graphId\": \"NUMERACY\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\",\"developer\" : \"Play Store\",\"owner\"     : \"Google & its Developers\"},\"inRelations\" : [{\"startNodeId\": \"G1\",\"relationType\": \"associatedTo\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
        ResultActions actions = mockMvc.perform(post("/learning-object/").contentType(MediaType.APPLICATION_JSON).content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().is(400));
        Assert.assertEquals("Required String parameter 'taxonomyId' is not present", actions.andReturn().getResponse().getErrorMessage());        
    }
    
    @org.junit.Test
    public void blankGameObject() throws Exception {
    	String contentString = "{\"request\": {\"LEARNING_OBJECT\": {}}}";
        ResultActions actions = mockMvc.perform(post("/learning-object/").param("taxonomyId", "NUMERACY").contentType(MediaType.APPLICATION_JSON).content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().is(400));
        String content = (String) actions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Response resp = objectMapper.readValue(content, Response.class);
        Assert.assertEquals("Validation Errors", resp.getParams().get("errmsg"));
    }
    
    @org.junit.Test
    public void emptyObjectType() throws Exception {
    	String contentString = "{\"request\": {\"LEARNING_OBJECT\": {\"objectType\": \"\",\"graphId\": \"NUMERACY\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\",\"developer\" : \"Play Store\",\"owner\"     : \"Google & its Developers\"},\"inRelations\" : [{\"startNodeId\": \"G1\",\"relationType\": \"associatedTo\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
        ResultActions actions = mockMvc.perform(post("/learning-object/").param("taxonomyId", "NUMERACY").contentType(MediaType.APPLICATION_JSON).content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().is(400));
        String content = (String) actions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Response resp = objectMapper.readValue(content, Response.class);
        Assert.assertEquals("Validation Errors", resp.getParams().get("errmsg"));
    }
    
    
    @org.junit.Test
    public void definationNodeNotFound() throws Exception {
    	String contentString = "{\"request\": {\"LEARNING_OBJECT\": {\"objectType\": \"Game\",\"graphId\": \"NUMERACY\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\",\"developer\" : \"Play Store\",\"owner\"     : \"Google & its Developers\"},\"inRelations\" : [{\"startNodeId\": \"G1\",\"relationType\": \"associatedTo\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
        ResultActions actions = mockMvc.perform(post("/learning-object/").param("taxonomyId", "NUMERACY").contentType(MediaType.APPLICATION_JSON).content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().is(400));
        String content = (String) actions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Response resp = objectMapper.readValue(content, Response.class);
        Assert.assertEquals("Validation Errors", resp.getParams().get("errmsg"));
    }
        
    @org.junit.Test
    public void requireMetaData() throws Exception {
    	String contentString = "{\"request\": {\"LEARNING_OBJECT\": {\"objectType\": \"Game\",\"graphId\": \"NUMERACY\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"inRelations\" : [{\"startNodeId\": \"G1\",\"relationType\": \"associatedTo\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
        ResultActions actions = mockMvc.perform(post("/learning-object/").param("taxonomyId", "NUMERACY").contentType(MediaType.APPLICATION_JSON).content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().is(400));
        String content = (String) actions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Response resp = objectMapper.readValue(content, Response.class);
        Assert.assertEquals("Validation Errors", resp.getParams().get("errmsg"));
    }
    
    @org.junit.Test
    public void invalidDataType() throws Exception {
    	String contentString = "{\"request\": {\"LEARNING_OBJECT\": {\"objectType\": \"Game\",\"graphId\": \"NUMERACY\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\",\"developer\" : \"Play Store\",\"owner\"     : \"Google & its Developers\", \"interactivityLevel\" : \"s\"},\"inRelations\" : [{\"startNodeId\": \"G1\",\"relationType\": \"associatedTo\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
        ResultActions actions = mockMvc.perform(post("/learning-object/").param("taxonomyId", "NUMERACY").contentType(MediaType.APPLICATION_JSON).content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().is(400));
        String content = (String) actions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Response resp = objectMapper.readValue(content, Response.class);
        Assert.assertEquals("Validation Errors", resp.getParams().get("errmsg"));
    }
    
    @org.junit.Test
    public void unsupportedRelation() throws Exception {
    	String contentString = "{\"request\": {\"LEARNING_OBJECT\": {\"objectType\": \"Game\",\"graphId\": \"NUMERACY\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\",\"developer\" : \"Play Store\",\"owner\": \"Google & its Developers\"},\"inRelations\" : [{\"startNodeId\": \"G1\",\"relationType\": \"\"}],\"tags\": [\"tag 1\", \"tag 33\"]}}}";
        ResultActions actions = mockMvc.perform(post("/learning-object/").param("taxonomyId", "NUMERACY").contentType(MediaType.APPLICATION_JSON).content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().is(400));
        String content = (String) actions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Response resp = objectMapper.readValue(content, Response.class);
        Assert.assertEquals("Validation Errors", resp.getParams().get("errmsg"));        
    }
    
    @org.junit.Test
    public void requireInOutRelation() throws Exception {
    	String contentString = "{\"request\": {\"LEARNING_OBJECT\": {\"objectType\": \"Game\",\"graphId\": \"NUMERACY\",\"identifier\": \"G99\",\"nodeType\": \"DATA_NODE\",\"metadata\": {\"name\": \"Animals Puzzle For Kids\",\"code\": \"ek.lit.an\",\"developer\" : \"Play Store\",\"owner\" : \"Google & its Developers\"},\"tags\": [\"tag 1\", \"tag 33\"]}}}";
        ResultActions actions = mockMvc.perform(post("/learning-object/").param("taxonomyId", "NUMERACY").contentType(MediaType.APPLICATION_JSON).content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().is(400));
        String content = (String) actions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Response resp = objectMapper.readValue(content, Response.class);
        Assert.assertEquals("Validation Errors", resp.getParams().get("errmsg"));
    }
}
