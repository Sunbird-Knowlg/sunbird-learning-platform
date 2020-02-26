package org.ekstep.taxonomy.controller.taxonomy.cucumber;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.ekstep.common.dto.Response;
import org.ekstep.taxonomy.base.test.BaseCucumberTest;
import org.junit.Assert;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class GetTaxonomyTest extends BaseCucumberTest{
	
	private String taxonomyId;
	
	@Before
    public void setup() throws IOException {
        initMockMVC();
    }
	
	@When("The taxonomy Id is (.*)$")
	public void getAllTaxonomy(String taxonomyId){
		this.taxonomyId = taxonomyId;
	}
	
	@Then("I should get all the numeracy data")
	public void getAllNumeracy(){
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/taxonomy/" + taxonomyId;
    	params.put("subgraph", "true");
    	params.put("cfields", "name");
    	params.put("tfields", "name");
    	header.put("user-id", "ilimi");
    	ResultActions actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().isOk());
		} catch (Exception e) {
			e.printStackTrace();
		}   
	}
	
	@Then("I should get Error message node (.*) found is blank and status is (\\d+)")
	public void wrongTaxonomyId(String error, int status){
		this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		Map<String, String> params = new HashMap<String, String>();
		Map<String, String> header = new HashMap<String, String>();
		String path = "/taxonomy/" + taxonomyId;
		params.put("subgraph", "true");
		params.put("cfields", "name");
		params.put("tfields", "name");
		header.put("user-id", "ilimi");
		ResultActions actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
		try {
			actions.andExpect(status().is(status));
		} catch (Exception e) {
			e.printStackTrace();
		}  
		Response resp = jasonToObject(actions);
		Assert.assertEquals("org.ekstep.common.exception.ResourceNotFoundException: Node "+error+" found: " + taxonomyId, resp.getParams().getErrmsg());
		Assert.assertEquals("ERR_GRAPH_SEARCH_UNKNOWN_ERROR", resp.getParams().getErr());
	}
}
