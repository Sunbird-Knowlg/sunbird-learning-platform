package com.ilimi.assessment.controller.questionnaire;

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
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class GetQuestionnaireTest extends BaseCucumberTest{
	    
    private String taxonomyId;
    private String questionnaireId;
    ResultActions actions;
    
    private void basicAssertion(Response resp){
		Assert.assertEquals("ekstep.lp.questionnaire.find", resp.getId());
        Assert.assertEquals("1.0", resp.getVer());
	}
    
    @Before
    public void setup() throws IOException {
        initMockMVC();
    }
    
    @When("^Get questionnaire when Taxonomy id is (.*) and questionnaire id is (.*)$")
    public void getInput(String taxonomyId, String questionnaireId) {
    	this.questionnaireId = questionnaireId;
    	if(taxonomyId.equals("absent"))
			this.taxonomyId = "absent";
		if(taxonomyId.equals("empty"))
			this.taxonomyId = "";
		else
			this.taxonomyId = taxonomyId;
        Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/learning-object/" + this.questionnaireId;
    	if(taxonomyId.equals("absent")){}
    	else
    		params.put("taxonomyId", this.taxonomyId);
    	params.put("cfields", "name");
    	header.put("user-id", "ilimi");;
    	actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc);
    }
    
	@Then("^return status of get questionnaire is (.*) and response code is (\\d+)$")
	public void assertResultAction(String status, int code) throws Exception {
		assertStatus(actions, code);
		Response resp = jasonToObject(actions);
		if (resp != null) {
			basicAssertion(resp);
			Assert.assertEquals(status, resp.getParams().getStatus());
		}
	}
	
	@And("^get error message of get questionnaire is (.*)$")
	public void assertErrorMessage(String message) {
		if (taxonomyId.equals("absent")) {
			Assert.assertEquals(actions.andReturn().getResponse().getErrorMessage(), message);
		} else if (taxonomyId.equals("empty")) {
			Response resp = jasonToObject(actions);
			Assert.assertEquals(resp.getParams().getErrmsg(), message);
			Assert.assertEquals("ERR_TAXONOMY_BLANK_TAXONOMY_ID", resp.getParams().getErr());
		} else {
			Response resp = jasonToObject(actions);
			if (message.equals("Node not found")) {
				Assert.assertEquals(message.toLowerCase() + questionnaireId, resp.getParams().getErrmsg().toLowerCase());
				Assert.assertEquals("ERR_GRAPH_NODE_NOT_FOUND", resp.getParams().getErr());
			} 
		}
	}
}
