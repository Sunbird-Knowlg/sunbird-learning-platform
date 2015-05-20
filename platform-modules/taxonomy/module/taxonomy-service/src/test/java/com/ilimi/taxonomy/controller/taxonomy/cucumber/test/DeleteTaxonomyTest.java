package com.ilimi.taxonomy.controller.taxonomy.cucumber.test;

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
public class DeleteTaxonomyTest extends BaseCucumberTest{
	
	private String taxonomyId;
	
	@Before
    public void setup() throws IOException {
        initMockMVC();
    }
	
	@When("I Give input Taxonomy ID (.*)$")
	public void getInputData(String taxonomyId){
		this.taxonomyId = taxonomyId;
	}
	
	@Then("Delete the (.*) graph and get the status (.*)$")
	public void deleteTaxonomy(String graphId, String status){
		Map<String, String> params = new HashMap<String, String>();
		Map<String, String> header = new HashMap<String, String>();
		String path = "/taxonomy/" + taxonomyId;
		header.put("user-id", "jeetu");
		ResultActions actions = resultActionDelete(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
		try {
			actions.andExpect(status().isAccepted());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jasonToObject(actions);
        Assert.assertEquals("ekstep.lp.definition.delete", resp.getId());
        Assert.assertEquals("1.0", resp.getVer());
        Assert.assertEquals(status, resp.getParams().getStatus());
        Assert.assertEquals(graphId, resp.getResult().get("graph_id"));
	}
	
	@Then("delete the (.*) graph and get the status (\\d+)$")
	public void deleteTaxonomy(String graphId, int status){
		Map<String, String> params = new HashMap<String, String>();
		Map<String, String> header = new HashMap<String, String>();
		String path = "/taxonomy/" + taxonomyId;
		header.put("user-id", "jeetu");
		ResultActions actions = resultActionDelete(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
		try {
			actions.andExpect(status().isAccepted());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
