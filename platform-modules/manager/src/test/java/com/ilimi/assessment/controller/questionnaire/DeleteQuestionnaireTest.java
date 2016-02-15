package com.ilimi.assessment.controller.questionnaire;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.ilimi.common.dto.Response;
import com.ilimi.taxonomy.base.test.BaseCucumberTest;

import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;


@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class DeleteQuestionnaireTest extends BaseCucumberTest{
	
	private String taxonomyId;
	private String questionnaireId;
	ResultActions actions;
	
	@Before
    public void setup() throws IOException {
        initMockMVC();
    }
	
	public String createQuestionnaire() {
		MockMvc mockMvc;		
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "{\"request\":{\"questionnaire\":{\"objectType\":\"Questionnaire\",\"metadata\":{\"code\":\"akshara.grade4.qpaper\",\"language\":\"English\",\"title\":\"Akshara Grade4 Questionnaire\",\"description\":\"Akshara Grade4 Questionnaire\",\"type\":\"materialised\",\"subject\":\"LIT\",\"duration\":30,\"domain\":\"numeracy\",\"total_items\":4,\"strict_sequencing\":false,\"allow_skip\":true,\"max_score\":15,\"status\":\"Live\",\"items\":[\"ques_1\",\"ques_2\",\"ques_3\",\"ques_4\"]}}}}";
        Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/v1/questionnaire";
    	params.put("taxonomyId", "numeracy");
    	header.put("user-id", "ilimi");
    	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().isOk());
		} catch (Exception e) {
			e.printStackTrace();
		}  
        Response resp = jasonToObject(actions);
        //basicAssertion(resp);
        Assert.assertEquals("successful", resp.getParams().getStatus());
        Map<String, Object> result = resp.getResult();
		return (String) result.get("node_id");
    }
	
	private void basicAssertion(Response resp){
		Assert.assertEquals("ekstep.lp.questionnaire.delete", resp.getId());
        Assert.assertEquals("1.0", resp.getVer());
	}
	
	@When("^Deleting a questionnaire Taxonomy id is (.*) and questionnaire id is (.*)$")
	public void getInputData(String taxonomyId, String questionnaireId){
		if(questionnaireId.equals("ilimi"))
			this.questionnaireId = questionnaireId;
		else
			questionnaireId = createQuestionnaire();
		if(taxonomyId.equals("absent"))
			this.taxonomyId = "absent";
		if(taxonomyId.equals("empty"))
			this.taxonomyId = "";
		else
			this.taxonomyId = taxonomyId;
		this.questionnaireId = questionnaireId;
		Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/v1/questionnaire/" + this.questionnaireId;
    	if ("empty".equals(this.taxonomyId))
            params.put("taxonomyId", "");
        else if (!"absent".equals(this.taxonomyId))
            params.put("taxonomyId", this.taxonomyId);  
    	header.put("user-id", "ilimi");
    	actions = resultActionDelete(path, params, MediaType.APPLICATION_JSON, header, mockMvc);     
	}
	
	@Then("^return status of delete questionnaire is (.*) and response code is (\\d+)$")
	public void assertResultAction(String status, int code) throws Exception {
		assertStatus(actions, code);
		Response resp = jasonToObject(actions);
		if (resp != null) {
			basicAssertion(resp);
			Assert.assertEquals(status, resp.getParams().getStatus());
		}
	}
	
	@And("^return error message by delete questionnaire API is (.*)$")
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
