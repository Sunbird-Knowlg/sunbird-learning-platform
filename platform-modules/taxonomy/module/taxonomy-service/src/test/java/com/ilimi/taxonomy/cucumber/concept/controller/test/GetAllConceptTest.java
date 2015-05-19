package com.ilimi.taxonomy.cucumber.concept.controller.test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ilimi.common.dto.Response;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class GetAllConceptTest extends CucumberBaseTestIlimi{
	
	private String TaxonomyId;
	
	@When("^Taxonomy Id is (.*)$")
	public void getTaxonomyId(String taxonomyId){
		if(taxonomyId.equals("absent"))
			this.TaxonomyId = "absent";
		if(taxonomyId.equals("blank"))
			this.TaxonomyId = "";
		else
			this.TaxonomyId = taxonomyId;		
	}
	
	@Then("^I should get ErrorMessage Taxonomy Id is (.*) and status is (\\d+)$")
    public void getConceptWithoutTaxonomy(String errmsg, int status) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept";
    	if(TaxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", TaxonomyId);
    	params.put("games", "true");
    	params.put("cfields", "name");
    	params.put("gfields", "name");
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc); 
    	try {
			actions.andExpect(status().is(status));
		} catch (Exception e) {
			System.out.println();
			e.printStackTrace();
		} 
        if(TaxonomyId.equals("absent")) {
        	Assert.assertEquals(actions.andReturn().getResponse().getErrorMessage(), "Required String parameter 'taxonomyId' is not present");
        } else{
        	Response resp = jasonToObject(actions);
        	Assert.assertEquals(resp.getParams().getErrmsg(), "Taxonomy Id is " + errmsg);
        }               
    }

	@Then("^I should get all (.*) Concepts$")
    public void getConcept(String concept) throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept";
    	params.put("taxonomyId", TaxonomyId);
    	params.put("games", "true");
    	params.put("cfields", "name");
    	params.put("gfields", "name");
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(202));
		} catch (Exception e) {
			e.printStackTrace();
		} 
   
       
    }
}
