package org.ekstep.assessment.controller.assementItem;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.ekstep.common.dto.Response;
import org.ekstep.taxonomy.base.test.BaseCucumberTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@Ignore
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class DeleteAssessmentItemTest extends BaseCucumberTest{
	
	private String taxonomyId;
	private String questionId;
	ResultActions actions;
	
	private void basicAssertion(Response resp){
		Assert.assertEquals("ekstep.lp.assessment_item.delete", resp.getId());
        Assert.assertEquals("1.0", resp.getVer());
	}
	
	 public String createQuestion(){
		String contentString = "{\"request\":{\"assessment_item\":{\"identifier\":\"tempQ\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"title\":\"Select a char of vowels - 1.\",\"body\":{\"content_type\":\"text/html\",\"content\":\"Select a char of vowels.\"},\"type\":\"mcq\",\"description\":\"GeometryTest\",\"options\":[{\"value\":{\"type\":\"text\",\"content\":\"A\",\"asset\":\"a\"},\"score\":1,\"answer\":true},{\"value\":{\"type\":\"text\",\"content\":\"B\",\"asset\":\"a\"}},{\"value\":{\"type\":\"text\",\"content\":\"C\",\"asset\":\"a\"}}],\"code\":\"Q1\",\"difficulty_level\":\"low\",\"num_answers\":1,\"owner\":\"Ilimi\",\"used_for\":\"assessment\",\"score\":3,\"qlevel\":\"EASY\",\"max_score\":5,\"max_time\":120,\"rendering_metadata\":[{\"interactivity\":[\"drag-drop\",\"zoom\"],\"keywords\":[\"compare\",\"multi-options\"],\"rendering_hints\":{\"styles\":\"css styles that will override the theme level styles for this one item\",\"view-mode\":\"landscape\"}}]},\"outRelations\":[{\"endNodeId\":\"Num:C1:SC1\",\"relationType\":\"associatedTo\"}]}}}";
    	Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/v1/assessmentitem";
    	params.put("taxonomyId", "numeracy");
    	header.put("user-id", "ilimi");
    	ResultActions actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().isOk());
		} catch (Exception e) {
			e.printStackTrace();
		}         
        Response resp = jasonToObject(actions);
        Assert.assertEquals("successful", resp.getParams().getStatus());
		Map<String, Object> result = resp.getResult();
		return (String) result.get("node_id");
	 }
	 
	 public void nodeNotFound(String questionId)  {
        Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/v1/assessmentitem/" + questionId;
    	params.put("taxonomyId", "numeracy");
    	params.put("cfields", "name");
    	header.put("user-id", "ilimi");
    	ResultActions actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
        try {
			actions.andExpect(status().is(404));
		} catch (Exception e) {
			e.printStackTrace();
		}
        Response resp = jasonToObject(actions);
        //new FindquestionTest().basicAssertion(resp);;
        Assert.assertEquals("ERR_GRAPH_SEARCH_UNKNOWN_ERROR", resp.getParams().getErr());
        Assert.assertEquals("Node not found: "+questionId, resp.getParams().getErrmsg());
		}
	
	@Before
    public void setup() throws IOException {
        initMockMVC();
    }
	
	@When("^Deleting a Question Taxonomy id is (.*) and question id is (.*)$")
	public void getInputData(String taxonomyId, String questionId){
		this.questionId = questionId;
		if(questionId.equals("ilimi"))
			this.questionId = createQuestion();
		else
			questionId = createQuestion();
		if(taxonomyId.equals("absent"))
			this.taxonomyId = "absent";
		if(taxonomyId.equals("empty"))
			this.taxonomyId = "";
		else
			this.taxonomyId = taxonomyId;
		this.questionId = questionId;
		Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/v1/assessmentitem/" + questionId;
    	if ("empty".equals(this.taxonomyId))
            params.put("taxonomyId", "");
        else if (!"absent".equals(this.taxonomyId))
            params.put("taxonomyId", this.taxonomyId);  
    	header.put("user-id", "ilimi");
    	actions = resultActionDelete(path, params, MediaType.APPLICATION_JSON, header, mockMvc);     
	}
	
	@Then("^return status of delete question is (.*) and response code is (\\d+)$")
	public void assertResultAction(String status, int code) throws Exception {
		assertStatus(actions, code);
		Response resp = jasonToObject(actions);
		if (resp != null) {
			basicAssertion(resp);
			Assert.assertEquals(status, resp.getParams().getStatus());
		}
	}
	
	@And("^return error message by delete question API is (.*)$")
	public void assertErrorMessage(String message) {
		if (taxonomyId.equals("absent")) {
			Assert.assertEquals(actions.andReturn().getResponse().getErrorMessage(), message);
		} else if (taxonomyId.equals("empty")) {
			Response resp = jasonToObject(actions);
			Assert.assertEquals(resp.getParams().getErrmsg(), message);
			Assert.assertEquals("ERR_ASSESSMENT_BLANK_TAXONOMY_ID", resp.getParams().getErr());
		} else {
			Response resp = jasonToObject(actions);
			if (message.equals("Node not found")) {
				Assert.assertEquals(message.toLowerCase() + questionId, resp.getParams().getErrmsg().toLowerCase());
				Assert.assertEquals("ERR_GRAPH_NODE_NOT_FOUND", resp.getParams().getErr());
			} 
		}
	}
}
