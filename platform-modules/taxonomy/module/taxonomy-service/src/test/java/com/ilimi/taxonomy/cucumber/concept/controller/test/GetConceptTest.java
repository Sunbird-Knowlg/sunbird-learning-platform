package com.ilimi.taxonomy.cucumber.concept.controller.test;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.api.junit.Cucumber;

@WebAppConfiguration
@RunWith(Cucumber.class)
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class GetConceptTest extends CucumberBaseTestIlimi{
   
    private ResultActions actions;    
    private String graphId;
    private String conceptId;
    private static String NUMERACY = "NUMERACY";
    
    @When("^I give Taxonomy (.*) and Concept ID (.*)$")
    public void getInput(String graphId, String conceptId) {
        this.graphId = graphId;
        this.conceptId = conceptId;
        System.out.println(graphId + "::" + conceptId);
    }
    @When("^I give Concept ID (.*)$")
    public void getData(String conceptId) throws Exception {
        this.conceptId = conceptId;
    }
    
    @Then("^I should get the status as (\\d+)$")
    public void getConceptWithoutTaxonomy(int status) throws Exception {
    	this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/"+conceptId;
    	if((NUMERACY.equals(graphId) || "".equals(graphId)))
    		params.put("taxonomyId", graphId);
    	params.put("cfields", "name");
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(status));
		} catch (Exception e) {
			e.printStackTrace();
		}         
            
    }
    
    @Then("^I should get the concept with name (.*)$")
    public void getConcept(String name) throws Exception {
    	String str = "/concept/"+ conceptId;
    	this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    	actions = mockMvc.perform(get(str).param("taxonomyId", graphId).param("cfields", "name").header("Content-Type", "application/json").header("user-id", "jeetu"));
    	actions.andExpect(status().is(202));
        // TODO: do the assertion on name.
    }
    

}
