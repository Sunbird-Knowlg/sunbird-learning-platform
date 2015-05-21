package com.ilimi.taxonomy.controller.concept.cucumber;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import com.ilimi.common.dto.Response;
import com.ilimi.taxonomy.base.test.BaseCucumberTest;

import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class GetAllConceptTest extends BaseCucumberTest{
	
	private String TaxonomyId;
	
	@Before
    public void setup() throws IOException {
        initMockMVC();
    }
	
	private void basicAssertion(Response resp){
		Assert.assertEquals("ekstep.lp.concept.list", resp.getId());
        Assert.assertEquals("1.0", resp.getVer());
	}
	
	@When("^Taxonomy Id is (.*)$")
	public void getTaxonomyId(String taxonomyId){
		if(taxonomyId.equals("absent"))
			this.TaxonomyId = "absent";
		if(taxonomyId.equals("blank"))
			this.TaxonomyId = "";
		else
			this.TaxonomyId = taxonomyId;		
	}
	
	@Then("^I should get all (.*) Concepts and status is (.*)$")
    public void getConcept(String concept, String status) throws Exception {
		
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
        Response resp = jasonToObject(actions);
        basicAssertion(resp);
        Assert.assertEquals(status, resp.getParams().getStatus());
       
    }
	
	@Then("^I should get ErrorMessage Taxonomy Id is (.*) and status is (\\d+)$")
    public void getConceptWithoutAndEmptyTaxonomy(String errmsg, int status) {
		
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
            basicAssertion(resp);
            Assert.assertEquals("ERR_TAXONOMY_BLANK_TAXONOMY_ID", resp.getParams().getErr());
        	Assert.assertEquals(resp.getParams().getErrmsg(), "Taxonomy Id is " + errmsg);
        }               
    }
}
