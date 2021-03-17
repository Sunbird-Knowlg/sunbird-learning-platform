package org.sunbird.taxonomy.controller.taxonomy.cucumber;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sunbird.common.dto.Response;
import org.sunbird.taxonomy.base.test.BaseCucumberTest;
import org.junit.Assert;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class GetAllDefinitionTest extends BaseCucumberTest{
	
	private String taxonomyId;
		
	@Before
    public void setup() throws IOException {
        initMockMVC();
    }
	
	@When("taxonomy ID is (.*)$")
	public void getInputData(String taxonomyId) {
		this.taxonomyId = taxonomyId;
	}
	
	@Then("^I should get all definition and status is (.*)$")
	public void getDefination(String status){
		Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/taxonomy/"+taxonomyId+"/definition";
    	header.put("user-id", "ilimi");
    	ResultActions actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().isOk());
		} catch (Exception e) {
			e.printStackTrace();
		} 
        Response resp = jasonToObject(actions);
        Assert.assertEquals("ekstep.lp.definition.list", resp.getId());
        Assert.assertEquals("1.0", resp.getVer());
        Assert.assertEquals(status, resp.getParams().getStatus());
        
        Map<String, Object> result = resp.getResult();
        @SuppressWarnings("unchecked")
        List<Object>  definition_node =  (ArrayList<Object>) result.get("definition_nodes");
        @SuppressWarnings("unchecked")
        Map<String, Object> nodeDef1 = (Map<String, Object>) definition_node.get(0);
        @SuppressWarnings("unchecked")
        Map<String, Object> nodeDef2 = (Map<String, Object>) definition_node.get(1);
        
        Assert.assertEquals("Concept", nodeDef1.get("objectType"));
        Assert.assertEquals("Taxonomy", nodeDef2.get("objectType"));        
	}
	
	@Then("I should get (.*) to get definition node and status should be (\\d+)")
	public void wrongTaxonomyId(String error, int status){
		Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/taxonomy/"+taxonomyId+"/definition";
    	header.put("user-id", "ilimi");
    	ResultActions actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(status));
		} catch (Exception e) {
			e.printStackTrace();
		} 
        
        Response resp = jasonToObject(actions);
		Assert.assertEquals(error+" to get definition node", resp.getParams().getErrmsg());
		Assert.assertEquals("ERR_GRAPH_SEARCH_NODE_NOT_FOUND", resp.getParams().getErr());
	}
	
	
}
