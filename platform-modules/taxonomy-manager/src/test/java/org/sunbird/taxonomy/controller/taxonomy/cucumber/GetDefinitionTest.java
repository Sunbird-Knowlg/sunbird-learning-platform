package org.sunbird.taxonomy.controller.taxonomy.cucumber;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.sunbird.common.dto.Response;
import org.sunbird.taxonomy.base.test.BaseCucumberTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@Ignore
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class GetDefinitionTest extends BaseCucumberTest{
	
	private String taxonomyId;
	private String objectType;
	
	@Before
    public void setup() throws IOException {
        initMockMVC();
    }
	
	@When("taxonomy Id is (.*) and objectType is (.*)$")
	public void getInputData(String taxonomyId, String objectType) {
		this.taxonomyId = taxonomyId;
		this.objectType = objectType;
	}
	
	@Then("^I should get the numeracy and status is (.*)$")
	public void getDefination(String status){
		Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/taxonomy/"+taxonomyId+"/definition/" + objectType;
    	header.put("user-id", "ilimi");
    	ResultActions actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().isOk());
		} catch (Exception e) {
			e.printStackTrace();
		} 
        Response resp = jasonToObject(actions);
        Assert.assertEquals("ekstep.lp.definition.find", resp.getId());
        Assert.assertEquals("1.0", resp.getVer());
        Assert.assertEquals(status, resp.getParams().getStatus());
        
    	Map<String, Object> result = resp.getResult();
        @SuppressWarnings("unchecked")
        Map<String, Object>  defination_node =  (Map<String, Object>) result.get("definition_node");
        
        Assert.assertEquals("Game", defination_node.get("objectType"));
		// @SuppressWarnings("unchecked")
		// List<String> array = (ArrayList<String>) defination_node.get("outRelations");
        //LinkedHashMap<String, String> hashMap = array.get(0); 
	}
	
	@Then("^I should get status is (\\d+) and (.*) to get definition node$")
	public void getDefinationWithWrongTaxonomy(int status, String error){
		Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/taxonomy/"+taxonomyId+"/definition/" + objectType;
    	header.put("user-id", "ilimi");
    	ResultActions actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(status));
		} catch (Exception e) {
			e.printStackTrace();
		} 
        
        Response resp = jasonToObject(actions);
		Assert.assertEquals("Failed to get definition node", resp.getParams().getErrmsg());
		Assert.assertEquals("ERR_GRAPH_SEARCH_NODE_NOT_FOUND", resp.getParams().getErr());
        
	}
	
}
