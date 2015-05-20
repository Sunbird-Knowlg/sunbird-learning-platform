package com.ilimi.taxonomy.controller.iob.cucumber.test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ilimi.common.dto.Response;
import com.ilimi.taxonomy.base.test.BaseIlimiTest;
import com.ilimi.taxonomy.base.test.BaseCucumberTest;

import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class DeleteGameRelationTest extends BaseCucumberTest{
	
	private String gameId1;
	private String gameId2;
	private String TaxonomyId;
	private String relationType;
	
	private void basicAssertion(Response resp){
		Assert.assertEquals("ekstep.lp.learning-object.delete", resp.getId());
        Assert.assertEquals("1.0", resp.getVer());
	}
	
	@Before
    public void setup() {
        try {
			initMockMVC();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	@When("^I give Game Id (.*) Relation (.*) with Game Id (.*) and taxonomy Id (.*)$")
	public void getInputData(String gameId1, String relationType, String gameId2, String taxonomyId){
		if(taxonomyId.equals("absent"))
			this.TaxonomyId = "absent";
		if(taxonomyId.equals("empty"))
			this.TaxonomyId = "";
		else
			this.TaxonomyId = taxonomyId;
		this.gameId1 = gameId1;
		this.gameId2 = gameId2;
		this.relationType = relationType;
		
	}
	
	@Then("^Delete the Relation and get the status (.*)$")
    public void getConcept(String concept) {
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object/" + gameId1 + "/" + relationType + "/" + gameId2;
    	params.put("taxonomyId", TaxonomyId);
    	header.put("user-id", "jeetu");
    	ResultActions actions = resultActionDelete(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(202));
		} catch (Exception e) {
			e.printStackTrace();
		}
        Response resp = jasonToObject(actions);
        basicAssertion(resp);
        Assert.assertEquals("SUCCESS", resp.getParams().getStatus());
	}
	
	@Then("^I will get ErrMsg Taxonomy Id is (.*) and status is (\\d+)$")
    public void getConceptWithoutTaxonomy(String errmsg, int status) {
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object/" + gameId1 + "/" + relationType + "/" + gameId2;
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
        	basicAssertion(resp);
        	Assert.assertEquals(resp.getParams().getErrmsg(), "Taxonomy Id is " + errmsg);
            Assert.assertEquals("ERR_TAXONOMY_BLANK_TAXONOMY_ID", resp.getParams().getErr());
        }               
    }
	@Then("^I will get ErrMsg Node not found and status is (\\d+)$")
    public void nodeNotFound(int status) {
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object/" + gameId1 + "/" + relationType + "/" + gameId2;
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
    	Assert.assertEquals("Node not found: " + gameId1 , resp.getParams().getErrmsg());               
    }
	
	@Then("^I will get unsupported relation and status is (\\d+)$")
    public void unspportedRelation(int status) {
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object/" + gameId1 + "/" + relationType + "/" + gameId2;
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
    	
        Assert.assertEquals("UnSupported Relation: associated", resp.getParams().getErrmsg());
        Assert.assertEquals("ERR_RELATION_CREATE", resp.getParams().getErr());               
    }
	
}
