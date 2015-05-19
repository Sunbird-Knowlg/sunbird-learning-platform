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
public class DeleteRelationConceptTest extends CucumberBaseTestIlimi{
	
	private String conceptId1;
	private String conceptId2;
	private String TaxonomyId;
	private String relationType;
	
	@When("^I give Concept id (.*) Relation (.*) with Concept ID (.*) and taxonomy Id (.*)$")
	public void getInputData(String conceptId1, String relationType, String conceptId2, String taxonomyId){
		if(taxonomyId.equals("absent"))
			this.TaxonomyId = "absent";
		if(taxonomyId.equals("empty"))
			this.TaxonomyId = "";
		else
			this.TaxonomyId = taxonomyId;
		this.conceptId1 = conceptId1;
		this.conceptId2 = conceptId2;
		this.relationType = relationType;
		
	}
	
	@Then("^Delete the relation and get the status (.*)$")
    public void getConcept(String concept) throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/" + conceptId1 + "/" + relationType + "/" + conceptId2;
    	params.put("taxonomyId", TaxonomyId);
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionDelete(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(202));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Then("^I should get ErrMsg Taxonomy Id is (.*) and status is (\\d+)$")
    public void getConceptWithoutTaxonomy(String errmsg, int status) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/" + conceptId1 + "/" + relationType + "/" + conceptId2;
    	if(TaxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", TaxonomyId);
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionDelete(path, params, MediaType.APPLICATION_JSON, header, mockMvc); 
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
	@Then("^I should get ErrMsg Node not found and status is (\\d+)$")
    public void nodeNotFound(int status) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/" + conceptId1 + "/" + relationType + "/" + conceptId2;
    	if(TaxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", TaxonomyId);
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionDelete(path, params, MediaType.APPLICATION_JSON, header, mockMvc); 
    	try {
			actions.andExpect(status().is(status));
		} catch (Exception e) {
			System.out.println();
			e.printStackTrace();
		} 
    	Response resp = jasonToObject(actions);
    	Assert.assertEquals("Node not found: " + conceptId1 , resp.getParams().getErrmsg());               
    }
	
	@Then("^I should get unsupported relation and status is (\\d+)$")
    public void unspportedRelation(int status) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/concept/" + conceptId1 + "/" + relationType + "/" + conceptId2;
    	if(TaxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", TaxonomyId);
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionDelete(path, params, MediaType.APPLICATION_JSON, header, mockMvc); 
    	try {
			actions.andExpect(status().is(status));
		} catch (Exception e) {
			System.out.println();
			e.printStackTrace();
		} 
    	//Assert.assertEquals(actions.andReturn().getResponse().getErrorMessage(), "Node not found: " + conceptId1);               
    }
	
}
