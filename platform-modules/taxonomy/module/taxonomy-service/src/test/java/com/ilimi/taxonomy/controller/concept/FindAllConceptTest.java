package com.ilimi.taxonomy.controller.concept;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
public class FindAllConceptTest {
	@Autowired 
    private WebApplicationContext context;
    
    private MockMvc mockMvc;
    
    @Before
    public void setup() throws IOException {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }
     
    @org.junit.Test
    public void findAllConcepts() throws Exception {
        ResultActions actions = mockMvc.perform(get("/concept").param("taxonomyId", "NUMERACY").param("games", "true").param("cfields", "name").param("gfields", "name").header("Content-Type", "application/json").header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().isOk());
        System.out.println(actions.andReturn().getResponse().getContentAsString());
        System.out.println("Status:"+actions.andReturn().getResponse().getStatus());
    }
    
    @org.junit.Test
    public void emptyTaxonomyId() throws Exception {
        ResultActions actions = mockMvc.perform(get("/concept").param("taxonomyId", "").param("games", "true").param("cfields", "name").param("gfields", "name").header("Content-Type", "application/json").header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().is(400));        
        String content = (String) actions.andReturn().getResponse().getContentAsString();
        System.out.println(content);
        ObjectMapper objectMapper = new ObjectMapper();
        Response resp = objectMapper.readValue(content, Response.class);
//        Assert.assertEquals("Taxonomy Id is blank", resp.getParams().get("errmsg"));
   }
    
    @org.junit.Test
    public void withoutTaxonomyId() throws Exception {
        ResultActions actions = mockMvc.perform(get("/concept").param("games", "true").param("cfields", "name").param("gfields", "name").header("Content-Type", "application/json").header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().is(400));
        Assert.assertEquals("Required String parameter 'taxonomyId' is not present", actions.andReturn().getResponse().getErrorMessage());
    }
    
    @org.junit.Test
    public void getConceptWithGamesFalse() throws Exception {
        ResultActions actions = mockMvc.perform(get("/concept").param("taxonomyId", "NUMERACY").param("games", "false").param("cfields", "name").param("gfields", "name").header("Content-Type", "application/json").header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
      //TODO Assertion for Game Data
        actions.andExpect(status().isOk());
    }
    
    @org.junit.Test
    public void getConceptWithGamesTrue() throws Exception {
        ResultActions actions = mockMvc.perform(get("/concept").param("taxonomyId", "NUMERACY").param("games", "true").param("cfields", "name").param("gfields", "name").header("Content-Type", "application/json").header("user-id", "jeetu"));
        actions.andDo(MockMvcResultHandlers.print());
        actions.andExpect(status().isOk());
    }
}
