package com.ilimi.taxonomy;

import java.io.IOException;






//import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.beans.factory.annotation.Autowired;
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
public class Test {

    @Autowired 
    private WebApplicationContext context;
    
    private MockMvc mockMvc;
    
    private ResultActions actions;
    
    @Before
    public void setup() throws IOException {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }
    
    @When("^I get all taxonomy data$")
    public void fetchAllTaxonomy() throws Exception {
        actions = mockMvc.perform(get("/taxonomy").param("tfields", "name").header("Content-Type", "application/json").header("user-id", "mahesh"));
    }
    
    @Then("^I will get the resopnse status value (\\d+)$")
    public void assertResult(int status) throws Exception {
        actions.andExpect(status().is(status));
    }
    
}
