package com.ilimi.taxonomy.conceptcontroller.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
public class UpadateConceptTest {
	@Autowired 
    private WebApplicationContext context;
    
    private MockMvc mockMvc;
    
    @Before
    public void setup() throws IOException {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }
    
    @org.junit.Test
    public void updateConcept() throws Exception {
    	String contentString = "{\"request\": {\"CONCEPT\": {\"identifier\": \"Num:C1\",\"nodeType\": \"DATA_NODE\",\"objectType\": \"Concept\",\"metadata\": {\"identifier\": \"Num:C1\",\"code\": \"Num:C1\",\"learningObjective\": [\"New Learning Objective 222\"],\"arrayProp\": [\"value1\", \"value3\", \"valu5\"]},\"tags\": [\"Subconcept\", \"tag 9\", \"tag 10\"]},\"COMMENT\" : \"\"}}";
        ResultActions actions = mockMvc.perform(patch("/concept/Num:C1").param("taxonomyId", "NUMERACY").contentType(MediaType.APPLICATION_JSON)
        		.content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().isOk());
    }
    
    @org.junit.Test
    public void withoutTaxonomyId() throws Exception {
    	String contentString = "{\"request\": {\"CONCEPT\": {\"identifier\": \"Num:C1\",\"nodeType\": \"DATA_NODE\",\"objectType\": \"Concept\",\"metadata\": {\"identifier\": \"Num:C1\",\"code\": \"Num:C1\",\"learningObjective\": [\"New Learning Objective 222\"],\"arrayProp\": [\"value1\", \"value3\", \"valu5\"]},\"tags\": [\"Subconcept\", \"tag 9\", \"tag 10\"]},\"COMMENT\" : \"\"}}";
        ResultActions actions = mockMvc.perform(patch("/concept/Num:C1").contentType(MediaType.APPLICATION_JSON)
        		.content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().is(400));
        Assert.assertEquals("Required String parameter 'taxonomyId' is not present", actions.andReturn().getResponse().getErrorMessage());
    }
    
    @org.junit.Test
    public void emptyTaxonomyId() throws Exception {
    	String contentString = "{\"request\": {\"CONCEPT\": {\"identifier\": \"Num:C1\",\"nodeType\": \"DATA_NODE\",\"objectType\": \"Concept\",\"metadata\": {\"identifier\": \"Num:C1\",\"code\": \"Num:C1\",\"learningObjective\": [\"New Learning Objective 222\"],\"arrayProp\": [\"value1\", \"value3\", \"valu5\"]},\"tags\": [\"Subconcept\", \"tag 9\", \"tag 10\"]},\"COMMENT\" : \"\"}}";
        ResultActions actions = mockMvc.perform(patch("/concept/Num:C1").param("taxonomyId", "").contentType(MediaType.APPLICATION_JSON)
        		.content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().is(400));        
        String content = (String) actions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Response resp = objectMapper.readValue(content, Response.class);
        Assert.assertEquals("Taxonomy Id is blank", resp.getParams().get("errmsg"));
    }
    
    @org.junit.Test
    public void blankConceptObject() throws Exception {
    	String contentString = "{\"request\": {\"CONCEPT\": {}}}";
        ResultActions actions = mockMvc.perform(patch("/concept/Num:C1").param("taxonomyId", "NUMERACY").contentType(MediaType.APPLICATION_JSON)
        		.content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().is(400));        
        String content = (String) actions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Response resp = objectMapper.readValue(content, Response.class);
        Assert.assertEquals("Concept Object is blank", resp.getParams().get("errmsg"));
    }
    
   
    @org.junit.Test
    public void conceptObjectNotFound() throws Exception {
    	String contentString = "{\"request\": {\"CONCEPT\": {\"identifier\": \"Jeetu\",\"nodeType\": \"DATA_NODE\",\"objectType\": \"sadf\",\"metadata\": {\"identifier\": \"Num:C1\",\"code\": \"Num:C1\",\"learningObjective\": [\"New Learning Objective 222\"],\"arrayProp\": [\"value1\", \"value3\", \"valu5\"]},\"tags\": [\"Subconcept\", \"tag 9\", \"tag 10\"]},\"COMMENT\" : \"\"}}";
        ResultActions actions = mockMvc.perform(patch("/concept/Num:C1").param("taxonomyId", "NUMERACY").contentType(MediaType.APPLICATION_JSON)
        		.content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().is(404));        
        String content = (String) actions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Response resp = objectMapper.readValue(content, Response.class);
        Assert.assertEquals("Node Not Found", resp.getParams().get("errmsg"));
    }
    
    @org.junit.Test
    public void emptyObjectType() throws Exception {
    	String contentString = "{\"request\": {\"CONCEPT\": {\"identifier\": \"Num:C1\",\"nodeType\": \"DATA_NODE\",\"objectType\": \"\",\"metadata\": {\"identifier\": \"Num:C1\",\"code\": \"Num:C1\",\"learningObjective\": [\"New Learning Objective 222\"],\"arrayProp\": [\"value1\", \"value3\", \"valu5\"]},\"tags\": [\"Subconcept\", \"tag 9\", \"tag 10\"]},\"COMMENT\" : \"\"}}";
        ResultActions actions = mockMvc.perform(patch("/concept/Num:C1").param("taxonomyId", "NUMERACY").contentType(MediaType.APPLICATION_JSON)
        		.content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().is(400));        
        String content = (String) actions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Response resp = objectMapper.readValue(content, Response.class);
        Assert.assertEquals("Node Metadata validation failed", resp.getParams().get("errmsg"));
    } 
    
    @org.junit.Test
    public void nodeNotFoundForObjectType() throws Exception {
    	String contentString = "{\"request\": {\"CONCEPT\": {\"identifier\": \"jeetu\",\"nodeType\": \"DATA_NODE\",\"objectType\": \"Concept\",\"metadata\": {\"identifier\": \"Num:C1\",\"code\": \"Num:C1\",\"learningObjective\": [\"New Learning Objective 222\"],\"arrayProp\": [\"value1\", \"value3\", \"valu5\"]},\"tags\": [\"Subconcept\", \"tag 9\", \"tag 10\"]},\"COMMENT\" : \"\"}}";
        ResultActions actions = mockMvc.perform(patch("/concept/Num:C1").param("taxonomyId", "NUMERACY").contentType(MediaType.APPLICATION_JSON)
        		.content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().is(404));        
        String content = (String) actions.andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Response resp = objectMapper.readValue(content, Response.class);
        Assert.assertEquals("Node Not Found", resp.getParams().get("errmsg"));
    }
    
    //error
    @org.junit.Test
    public void requireMetaData() throws Exception {
    	String contentString = "{\"request\": {\"CONCEPT\": {\"identifier\": \"Num:C1\",\"nodeType\": \"DATA_NODE\",\"objectType\": \"Concept\",\"tags\": [\"Subconcept\", \"tag 9\", \"tag 10\"]},\"COMMENT\" : \"\"}}";
        ResultActions actions = mockMvc.perform(patch("/concept/Num:C1").param("taxonomyId", "NUMERACY").contentType(MediaType.APPLICATION_JSON)
        		.content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().isOk());
        //TO DO assertion and correct method
    }
    
    //error
    @org.junit.Test
    public void invalidDataType() throws Exception {
    	String contentString = "{\"request\": {\"CONCEPT\": {\"identifier\": \"Num:C1\",\"nodeType\": \"DATA_NODE\",\"objectType\": \"Concept\",\"metadata\": {\"identifier\": \"Num:C1\",\"code\": \"Num:C1\",\"lear\": [\"New Learning Objective 222\"],\"arrayProp\": [\"value1\", \"value3\", \"valu5\"]},\"tags\": [\"Subconcept\", \"tag 9\", \"tag 10\"]},\"COMMENT\" : \"\"}}";
        ResultActions actions = mockMvc.perform(patch("/concept/Num:C1").param("taxonomyId", "NUMERACY").contentType(MediaType.APPLICATION_JSON)
        		.content(contentString.getBytes()).header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().isOk());
        //TO DO assertion and correct method
    }
    
 
}
