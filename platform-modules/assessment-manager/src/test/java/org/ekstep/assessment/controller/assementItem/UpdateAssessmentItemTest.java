package org.ekstep.assessment.controller.assementItem;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.ArrayList;
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
public class UpdateAssessmentItemTest extends BaseCucumberTest{
	
	private String taxonomyId;
	private String questionId;
	private String questionDetails;
	private ResultActions actions;
	
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
	
	private void basicAssertion(Response resp){
		Assert.assertEquals("ekstep.lp.assessment_item.update", resp.getId());
        Assert.assertEquals("1.0", resp.getVer());
	}
	
	@Before
    public void setup() throws IOException {
        initMockMVC();
    }
	
	@When("^Updating a question Taxonomy id is (.*) and question id is (.*) with (.*)$")
	public void getInputData(String taxonomyId, String questionId , String questionDetail){
		this.questionDetails = questionDetail.toLowerCase();
		this.taxonomyId = taxonomyId;
		this.questionId = questionId;
		if(questionId.equals("ilimi"))
			this.questionId = questionId;
		else
			this.questionId = createQuestion();
		String contentString = "{\"request\":{\"assessment_item\":{\"identifier\":\"tempQ\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"title\":\"Select a char of vowels - 1.\",\"body\":{\"content_type\":\"text/html\",\"content\":\"Select a char of vowels.\"},\"type\":\"mcq\",\"description\":\"GeometryTest\",\"options\":[{\"value\":{\"type\":\"text\",\"content\":\"A\",\"asset\":\"a\"},\"score\":1,\"answer\":true},{\"value\":{\"type\":\"text\",\"content\":\"B\",\"asset\":\"a\"}},{\"value\":{\"type\":\"text\",\"content\":\"C\",\"asset\":\"a\"}}],\"code\":\"Q1\",\"difficulty_level\":\"low\",\"num_answers\":1,\"owner\":\"Ilimi\",\"used_for\":\"assessment\",\"score\":3,\"qlevel\":\"EASY\",\"max_score\":5,\"max_time\":120,\"rendering_metadata\":[{\"interactivity\":[\"drag-drop\",\"zoom\"],\"keywords\":[\"compare\",\"multi-options\"],\"rendering_hints\":{\"styles\":\"css styles that will override the theme level styles for this one item\",\"view-mode\":\"landscape\"}}]},\"outRelations\":[{\"endNodeId\":\"Num:C1:SC1\",\"relationType\":\"associatedTo\"}]}}}";
		Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/v1/assessmentitem/" + this.questionId;
    	header.put("user-id", "ilimi");
    	if ("empty".equals(this.taxonomyId))
            params.put("taxonomyId", "");
        else if (!"absent".equals(this.taxonomyId))
            params.put("taxonomyId", this.taxonomyId);  
    	if (questionDetails.equals("question as blank")) {
	            contentString = "{\"request\": {}}";
	        } else if (questionDetails.equals("proper question data and relation changes")) {
	        	contentString = "{\"request\":{\"assessment_item\":{\"identifier\":\"tempQ\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"title\":\"Select a char of vowels - 1.\",\"body\":{\"content_type\":\"text/html\",\"content\":\"Select a char of vowels.\"},\"type\":\"mcq\",\"description\":\"GeometryTest\",\"options\":[{\"value\":{\"type\":\"text\",\"content\":\"A\",\"asset\":\"a\"},\"score\":1,\"answer\":true},{\"value\":{\"type\":\"text\",\"content\":\"B\",\"asset\":\"a\"}},{\"value\":{\"type\":\"text\",\"content\":\"C\",\"asset\":\"a\"}}],\"code\":\"Q1\",\"difficulty_level\":\"low\",\"num_answers\":1,\"owner\":\"Ilimi\",\"used_for\":\"assessment\",\"score\":3,\"qlevel\":\"EASY\",\"max_score\":5,\"max_time\":120,\"rendering_metadata\":[{\"interactivity\":[\"drag-drop\",\"zoom\"],\"keywords\":[\"compare\",\"multi-options\"],\"rendering_hints\":{\"styles\":\"css styles that will override the theme level styles for this one item\",\"view-mode\":\"landscape\"}}]},\"outRelations\":[{\"endNodeId\":\"Num:C1:SC1\",\"relationType\":\"associatedTo\"}]}}}";
	        } else if (questionDetails.equals("wrong question id")) {
	        	contentString = "{\"request\":{\"assessment_item\":{\"identifier\":\"ilimi\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"title\":\"Select a char of vowels - 1.\",\"body\":{\"content_type\":\"text/html\",\"content\":\"Select a char of vowels.\"},\"type\":\"mcq\",\"description\":\"GeometryTest\",\"options\":[{\"value\":{\"type\":\"text\",\"content\":\"A\",\"asset\":\"aaa\"},\"score\":1,\"answer\":true},{\"value\":{\"type\":\"text\",\"content\":\"B\",\"asset\":\"bbb\"}},{\"value\":{\"type\":\"text\",\"content\":\"C\",\"asset\":\"ccc\"}}],\"code\":\"Q1\",\"difficulty_level\":\"low\",\"num_answers\":1,\"owner\":\"Ilimi\",\"used_for\":\"assessment\",\"score\":3,\"qlevel\":\"EASY\",\"max_score\":5,\"max_time\":120,\"rendering_metadata\":[{\"interactivity\":[\"drag-drop\",\"zoom\"],\"keywords\":[\"compare\",\"multi-options\"],\"rendering_hints\":{\"styles\":\"css styles that will override the theme level styles for this one item\",\"view-mode\":\"landscape\"}}]},\"outRelations\":[{\"endNodeId\":\"Num:C1:SC1\",\"relationType\":\"associatedTo\"}]}}}";
	        } else if (questionDetails.equals("object type not set")) {
	        	contentString = "{\"request\":{\"assessment_item\":{\"identifier\":\"tempQ\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"title\":\"Select a char of vowels - 1.\",\"body\":{\"content_type\":\"text/html\",\"content\":\"Select a char of vowels.\"},\"type\":\"mcq\",\"description\":\"GeometryTest\",\"options\":[{\"value\":{\"type\":\"text\",\"content\":\"A\",\"asset\":\"a\"},\"score\":1,\"answer\":true},{\"value\":{\"type\":\"text\",\"content\":\"B\",\"asset\":\"a\"}},{\"value\":{\"type\":\"text\",\"content\":\"C\",\"asset\":\"a\"}}],\"code\":\"Num:Q234\",\"difficulty_level\":\"low\",\"num_answers\":1,\"owner\":\"Ilimi\",\"used_for\":\"assessment\",\"score\":3,\"max_time\":120,\"rendering_metadata\":[{\"interactivity\":[\"drag-drop\",\"zoom\"],\"keywords\":[\"compare\",\"multi-options\"],\"rendering_hints\":{\"styles\":\"css styles that will override the theme level styles for this one item\",\"view-mode\":\"landscape\"}}]}}}}";
	        } else if (questionDetails.equals("require metadata")) {
	        	contentString = "{ \"request\": { \"assessment_item\": { \"identifier\":\"tempQ\", \"objectType\": \"AssessmentItem\",\"metadata\" : {}, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
	        } else if(questionDetails.equals("invalid data type for select")) {
	        	contentString = "{\"request\":{\"assessment_item\":{\"identifier\":\"tempQ\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"title\":\"Select a char of vowels - 1.\",\"body\":{\"content_type\":\"text/html\",\"content\":\"Select a char of vowels.\"},\"type\":\"mcq\",\"description\":\"GeometryTest\",\"options\":[{\"value\":{\"type\":\"text\",\"content\":\"A\",\"asset\":\"aaa\"},\"score\":1,\"answer\":true},{\"value\":{\"type\":\"text\",\"content\":\"B\",\"asset\":\"bbb\"}},{\"value\":{\"type\":\"text\",\"content\":\"C\",\"asset\":\"ccc\"}}],\"code\":\"Q1\",\"difficulty_level\":\"easy\",\"num_answers\":3,\"owner\":\"Ilimi\",\"status\":[\"ilimi\"],\"used_for\":\"assessment\",\"score\":3,\"qlevel\":\"EASY\",\"max_score\":5,\"max_time\":120,\"rendering_metadata\":[{\"interactivity\":[\"drag-drop\",\"zoom\"],\"keywords\":[\"compare\",\"multi-options\"],\"rendering_hints\":{\"styles\":\"css styles that will override the theme level styles for this one item\",\"view-mode\":\"landscape\"}}]}}}}";
	        } else if (questionDetails.equals("wrong definition node")) {
	            contentString = "{\"request\":{\"assessment_item\":{\"identifier\":\"tempQ\",\"objectType\":\"ilimi\",\"metadata\":{\"title\":\"Select a char of vowels - 1.\",\"body\":{\"content_type\":\"text/html\",\"content\":\"Select a char of vowels.\"},\"type\":\"mcq\",\"description\":\"GeometryTest\",\"options\":[{\"value\":{\"type\":\"text\",\"content\":\"A\",\"asset\":\"aaa\"},\"score\":1,\"answer\":true},{\"value\":{\"type\":\"text\",\"content\":\"B\",\"asset\":\"bbb\"}},{\"value\":{\"type\":\"text\",\"content\":\"C\",\"asset\":\"ccc\"}}],\"code\":\"Q1\",\"difficulty_level\":\"easy\",\"num_answers\":3,\"owner\":\"Ilimi\",\"used_for\":\"assessment\",\"score\":3,\"qlevel\":\"EASY\",\"max_score\":5,\"max_time\":120,\"rendering_metadata\":[{\"interactivity\":[\"drag-drop\",\"zoom\"],\"keywords\":[\"compare\",\"multi-options\"],\"rendering_hints\":{\"styles\":\"css styles that will override the theme level styles for this one item\",\"view-mode\":\"landscape\"}}]}}}}";
	        } 
	        actions = resultActionPatch(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);
	}
	
	@Then("^return status of update question is (.*) and response code is (\\d+)$")
    public void assertResultAction(String status, int code) {
        assertStatus(actions, code);
        Response resp = jasonToObject(actions);
        if (resp != null) {
            basicAssertion(resp);
            Assert.assertEquals(status, resp.getParams().getStatus());
        }
    }
	
	@And("^return error message by update question API is (.*)$")
    public void assertErrorMessage(String message) {
        if (taxonomyId.equals("absent")) {
            Assert.assertEquals(actions.andReturn().getResponse().getErrorMessage().toLowerCase(), message.toLowerCase());
        } else if (taxonomyId.equals("empty")) {
            Response resp = jasonToObject(actions);
            Assert.assertEquals(resp.getParams().getErrmsg().toLowerCase(), message.toLowerCase());
            Assert.assertEquals("ERR_ASSESSMENT_BLANK_TAXONOMY_ID", resp.getParams().getErr());
        } else {
            Response resp = jasonToObject(actions);
            if (this.questionDetails.equals("question is blank")) {
                Assert.assertEquals(message.toLowerCase(), resp.getParams().getErrmsg().toLowerCase());
                Assert.assertEquals("ERR_ASSESSMENT_BLANK_ITEM", resp.getParams().getErr());
            } else if(this.questionDetails.equals("wrong question id")){
            	Assert.assertEquals(message.toLowerCase(), resp.getParams().getErrmsg().toLowerCase());
                Assert.assertEquals("ERR_GRAPH_UPDATE_NODE_NOT_FOUND", resp.getParams().getErr());
            } else if (this.questionDetails.equals("invalid data type for select") || this.questionDetails.equals("wrong definition node")
                    || this.questionDetails.equals("object type not set") || this.questionDetails.equals("require metadata")) {
                Map<String, Object> result = resp.getResult();
                @SuppressWarnings("unchecked")
                ArrayList<String> msg = (ArrayList<String>) result.get("messages");
                Assert.assertEquals(message.toLowerCase(), msg.get(0).toLowerCase());
            }            
            else if(this.questionDetails.equals("invalid node")) {
                Map<String, Object> result = resp.getResult();
                @SuppressWarnings("unchecked")
                ArrayList<String> msg = (ArrayList<String>) result.get("messages");
                Assert.assertEquals(message.toLowerCase(), msg.get(0).toLowerCase()); 
                Assert.assertEquals("Node Metadata validation failed", resp.getParams().getErrmsg());
                Assert.assertEquals("ERR_GRAPH_UPDATE_NODE_VALIDATION_FAILED", resp.getParams().getErr());
            }
        }
    }

}
