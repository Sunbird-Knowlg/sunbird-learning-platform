package com.ilimi.taxonomy.controller.concept;

import java.io.IOException;









//import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class SampleCucumber {

    @Autowired 
    private WebApplicationContext context;
    
    private MockMvc mockMvc;
    
    private ResultActions actions;
    
    @Before
    public void setup() throws IOException {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }
    
    @When("^I give Taxonomy (.*) and Concept ID (.*)$")
    public void getConcept(String graphId, String conceptId) throws Exception {
        actions = mockMvc.perform(get("/concept/"+conceptId).param("taxonomyId", graphId).param("cfields", "name").header("Content-Type", "application/json").header("user-id", "jeetu"));
    }
    
    @Then("^I should get the concept with name (.*)$")
    public void assertResult(String name) throws Exception {
        actions.andExpect(status().is(200));
        // TODO: do the assertion on name.
    }
    
}
