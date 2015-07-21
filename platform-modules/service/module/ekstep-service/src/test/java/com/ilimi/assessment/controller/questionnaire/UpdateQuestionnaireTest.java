package com.ilimi.assessment.controller.questionnaire;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.ArrayList;
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
public class UpdateQuestionnaireTest extends BaseCucumberTest{
	private String taxonomyId;
	@SuppressWarnings("unused")
	private String questionnaireId;
	private String questionnaireDetails;
	private ResultActions actions;
	
	private void basicAssertion(Response resp){
		Assert.assertEquals("ekstep.lp.questionnaire.update", resp.getId());
        Assert.assertEquals("1.0", resp.getVer());
	}
	
	public String createQuestionnaire() {
		MockMvc mockMvc;		
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "{ \"request\": { \"questionnaire\": { \"objectType\": \"Questionnaire\", \"metadata\": { \"code\": \"QR1\", \"language\": \"English\", \"title\": \"Demo Questionnaire for Ekstep Platform\", \"description\": \"Description of Demo Questionnaire - Ekstep Platform\", \"instructions\": \"Instructions of Demo Questionnaire - Ekstep Platform\", \"used_for\": \"assessment\", \"type\": \"materialised\", \"duration\": 20, \"total_items\": 6, \"strict_sequencing\": false, \"allow_skip\": true, \"max_score\": 20, \"status\": \"Draft\", \"owner\": \"Ilimi\", \"copyright\": \"Ilimi\", \"license\": \"Ilimi\", \"items\": [ \"Q1\", \"Q4\",\"Q10\", \"Q5\", \"Q6\", \"Q7\", \"Q8\",\"Q9\" ] }, \"outRelations\": [ { \"relationType\": \"associatedTo\", \"endNodeId\": \"Num:C1:SC1\" } ] } } }";
        Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/questionnaire";
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
	
	@Before
    public void setup() {
        try {
			initMockMVC();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	@When("^Update questionnaire when Taxonomy id is (.*) and questionnaire id is (.*) with (.*)$")
	public void getInputData(String taxonomyId, String questionnaireId , String questionnaireDetail){
		this.questionnaireDetails = questionnaireDetail.toLowerCase();
		this.taxonomyId = taxonomyId;
		if(questionnaireId.equals("ilimi"))
			this.questionnaireId = questionnaireId;
		else
			this.questionnaireId = createQuestionnaire();
		String contentString = "{ \"request\": { \"questionnaire\": { \"objectType\": \"Questionnaire\", \"metadata\": { \"code\": \"QR1\", \"language\": \"English\", \"title\": \"Demo Questionnaire for Ekstep Platform\", \"description\": \"Description of Demo Questionnaire - Ekstep Platform\", \"instructions\": \"Instructions of Demo Questionnaire - Ekstep Platform\", \"used_for\": \"assessment\", \"type\": \"materialised\", \"duration\": 20, \"total_items\": 10, \"strict_sequencing\": false, \"allow_skip\": true, \"max_score\": 20, \"status\": \"Draft\", \"owner\": \"Ilimi\", \"copyright\": \"Ilimi\", \"license\": \"Ilimi\", \"items\": [ \"Q1\", \"Q2\", \"Q3\", \"Q4\", \"Q5\", \"Q6\", \"Q7\", \"Q8\", \"Q9\", \"Q10\" ] }, \"outRelations\": [ { \"relationType\": \"associatedTo\", \"endNodeId\": \"Num:C1:SC1\" } ] } } }";
		Map<String, String> params = new HashMap<String, String>();
    	Map<String, String> header = new HashMap<String, String>();
    	String path = "/questionnaire/" + this.questionnaireId;
    	header.put("user-id", "ilimi");
		 if (this.taxonomyId.equals("empty"))
	            params.put("taxonomyId", "");
	        else if (!this.taxonomyId.equals("absent"))
	            params.put("taxonomyId", this.taxonomyId);
    	if (questionnaireDetails.equals("questionnaire is blank")) {
	            contentString = "{\"request\": {}}";
	        }  else if (questionnaireDetails.equals("empty object type")) {
	        	contentString = "{ \"request\": { \"questionnaire\": { \"objectType\": \"\", \"metadata\": { \"code\": \"QR1\", \"language\": \"English\", \"title\": \"Demo Questionnaire for Ekstep Platform\", \"description\": \"Description of Demo Questionnaire - Ekstep Platform\", \"instructions\": \"Instructions of Demo Questionnaire - Ekstep Platform\", \"used_for\": \"assessment\", \"type\": \"materialised\", \"duration\": 20, \"total_items\": 10, \"strict_sequencing\": false, \"allow_skip\": true, \"max_score\": 20, \"status\": \"Draft\", \"owner\": \"Ilimi\", \"copyright\": \"Ilimi\", \"license\": \"Ilimi\", \"items\": [ \"Q1\", \"Q2\", \"Q3\", \"Q4\", \"Q5\", \"Q6\", \"Q7\", \"Q8\", \"Q9\", \"Q10\" ] }, \"outRelations\": [ { \"relationType\": \"associatedTo\", \"endNodeId\": \"Num:C1:SC1\" } ] } } }";
	        } else if(questionnaireDetails.equals("wrong questionnaire id")) {
	        	contentString = "{ \"request\": { \"questionnaire\": { \"objectType\": \"Questionnaire\", \"metadata\": { \"code\": \"QR1\", \"language\": \"English\", \"title\": \"Demo Questionnaire for Ekstep Platform\", \"description\": \"Description of Demo Questionnaire - Ekstep Platform\", \"instructions\": \"Instructions of Demo Questionnaire - Ekstep Platform\", \"used_for\": \"assessment\", \"type\": \"materialised\", \"duration\": 20, \"total_items\": 10, \"strict_sequencing\": false, \"allow_skip\": true, \"max_score\": 20, \"status\": \"Draft\", \"owner\": \"Ilimi\", \"copyright\": \"Ilimi\", \"license\": \"Ilimi\", \"items\": [ \"Q1\", \"Q2\", \"Q3\", \"Q4\", \"Q5\", \"Q6\", \"Q7\", \"Q8\", \"Q9\", \"Q10\" ] }, \"outRelations\": [ { \"relationType\": \"associatedTo\", \"endNodeId\": \"Num:C1:SC1\" } ] } } }";
	        } else if(questionnaireDetails.equals("wrong member id")){
	        	contentString = "{ \"request\": { \"questionnaire\": { \"objectType\": \"Questionnaire\", \"metadata\": { \"code\": \"QR1\", \"language\": \"English\", \"title\": \"Demo Questionnaire for Ekstep Platform\", \"description\": \"Description of Demo Questionnaire - Ekstep Platform\", \"instructions\": \"Instructions of Demo Questionnaire - Ekstep Platform\", \"used_for\": \"assessment\", \"type\": \"materialised\", \"duration\": 20, \"total_items\": 10, \"strict_sequencing\": false, \"allow_skip\": true, \"max_score\": 20, \"status\": \"Draft\", \"owner\": \"Ilimi\", \"copyright\": \"Ilimi\", \"license\": \"Ilimi\", \"items\": [ \"Q1\", \"Q2\", \"Q3\", \"Q4\", \"Q5\", \"Q6\", \"Q7\", \"Q8\", \"Q9\", \"Q109\" ] }, \"outRelations\": [ { \"relationType\": \"associatedTo\", \"endNodeId\": \"Num:C1:SC1\" } ] } } }";
	        } else if (questionnaireDetails.equals("wrong definition node")) {
	            contentString = "{ \"request\": { \"questionnaire\": { \"objectType\": \"ilimi\", \"metadata\": { \"code\": \"QR1\", \"language\": \"English\", \"title\": \"Demo Questionnaire for Ekstep Platform\", \"description\": \"Description of Demo Questionnaire - Ekstep Platform\", \"instructions\": \"Instructions of Demo Questionnaire - Ekstep Platform\", \"used_for\": \"assessment\", \"type\": \"materialised\", \"duration\": 20, \"total_items\": 6, \"strict_sequencing\": false, \"allow_skip\": true, \"max_score\": 20, \"status\": \"Draft\", \"owner\": \"Ilimi\", \"copyright\": \"Ilimi\", \"license\": \"Ilimi\", \"items\": [ \"Q1\", \"Q2\", \"Q3\", \"Q4\", \"Q5\", \"Q6\", \"Q7\", \"Q8\" ] }, \"outRelations\": [ { \"relationType\": \"associatedTo\", \"endNodeId\": \"Num:C1:SC1\" } ] } } }";
	        } else if (questionnaireDetails.equals("require metadata")) {
	            contentString = "{ \"request\": { \"questionnaire\": { \"objectType\": \"Questionnaire\", \"metadata\": {  }, \"outRelations\": [ { \"relationType\": \"associatedTo\", \"endNodeId\": \"Num:C1:SC1\" } ] } } }";
	        } else if(questionnaireDetails.equals("unsupported relation")) {
	            contentString = "{ \"request\": { \"questionnaire\": { \"objectType\": \"Questionnaire\", \"metadata\": { \"code\": \"QR1\", \"language\": \"English\", \"title\": \"Demo Questionnaire for Ekstep Platform\", \"description\": \"Description of Demo Questionnaire - Ekstep Platform\", \"instructions\": \"Instructions of Demo Questionnaire - Ekstep Platform\", \"used_for\": \"assessment\", \"type\": \"materialised\", \"duration\": 20, \"total_items\": 6, \"strict_sequencing\": false, \"allow_skip\": true, \"max_score\": 20, \"status\": \"Draft\", \"owner\": \"Ilimi\", \"copyright\": \"Ilimi\", \"license\": \"Ilimi\", \"items\": [ \"Q1\", \"Q2\", \"Q3\", \"Q4\", \"Q5\", \"Q6\", \"Q7\", \"Q8\" ] }, \"outRelations\": [ { \"relationType\": \"ilimi\", \"endNodeId\": \"Num:C1:SC1\" } ] } } }";
	        } else if(questionnaireDetails.equals("insufficient assessment items")) {
	        	contentString = "{ \"request\": { \"questionnaire\": { \"objectType\": \"Questionnaire\", \"metadata\": { \"code\": \"QR1\", \"language\": \"English\", \"title\": \"Demo Questionnaire for Ekstep Platform\", \"description\": \"Description of Demo Questionnaire - Ekstep Platform\", \"instructions\": \"Instructions of Demo Questionnaire - Ekstep Platform\", \"used_for\": \"assessment\", \"type\": \"materialised\", \"duration\": 20, \"total_items\": 16, \"strict_sequencing\": false, \"allow_skip\": true, \"max_score\": 20, \"status\": \"Draft\", \"owner\": \"Ilimi\", \"copyright\": \"Ilimi\", \"license\": \"Ilimi\", \"items\": [ \"Q1\", \"Q2\", \"Q3\", \"Q4\", \"Q5\", \"Q6\", \"Q7\", \"Q8\" ] }, \"outRelations\": [ { \"relationType\": \"associatedTo\", \"endNodeId\": \"Num:C1:SC1\" } ] } } }";
	        } 
	        actions = resultActionPatch(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);
	}
	
	@Then("^return status of update questionnaire is (.*) and response code is (\\d+)$")
    public void assertResultAction(String status, int code) {
        assertStatus(actions, code);
        Response resp = jasonToObject(actions);
        if (resp != null) {
            basicAssertion(resp);
            Assert.assertEquals(status, resp.getParams().getStatus());
        }
    }
	
	@And("^get error message of update questionnaire is (.*)$")
    public void assertErrorMessage(String message) {
        System.out.println("Msg:" + message);
        if (taxonomyId.equals("absent")) {
            Assert.assertEquals(actions.andReturn().getResponse().getErrorMessage().toLowerCase(), message.toLowerCase());
        } else if (taxonomyId.equals("empty")) {
            Response resp = jasonToObject(actions);
            Assert.assertEquals(resp.getParams().getErrmsg().toLowerCase(), message.toLowerCase());
            Assert.assertEquals("ERR_ASSESSMENT_BLANK_TAXONOMY_ID", resp.getParams().getErr());
        } else {
            Response resp = jasonToObject(actions);
            if (this.questionnaireDetails.equals("questionnaire is blank")) {
                Assert.assertEquals(message.toLowerCase(), resp.getParams().getErrmsg().toLowerCase());
                Assert.assertEquals("ERR_ASSESSMENT_BLANK_QUESTIONNAIRE", resp.getParams().getErr());
            } else if(this.questionnaireDetails.equals("wrong questionnaire id")){
            	Assert.assertEquals(message.toLowerCase(), resp.getParams().getErrmsg().toLowerCase());
                Assert.assertEquals("ERR_GRAPH_SEARCH_UNKNOWN_ERROR", resp.getParams().getErr());
            } else if (this.questionnaireDetails.equals("empty object type") || this.questionnaireDetails.equals("wrong definition node") || this.questionnaireDetails.equals("insufficient assessment items")
                    || this.questionnaireDetails.equals("unsupported relation") || this.questionnaireDetails.equals("require metadata") || this.questionnaireDetails.equals("invalid data type for select")) {
                Map<String, Object> result = resp.getResult();
                @SuppressWarnings("unchecked")
                ArrayList<String> msg = (ArrayList<String>) result.get("messages");
                Assert.assertEquals(message.toLowerCase(), msg.get(0).toLowerCase());
            } else if(this.questionnaireDetails.equals("require metadata")){
            	Map<String, Object> result = resp.getResult();
            	@SuppressWarnings("unchecked")
				ArrayList<String>   msg = (ArrayList<String>) result.get("messages");
                Assert.assertEquals(message.toLowerCase(), msg.get(0).toLowerCase()); 
                Assert.assertEquals("Node Metadata validation failed", resp.getParams().getErrmsg());
                Assert.assertEquals("ERR_GRAPH_UPDATE_NODE_VALIDATION_FAILED", resp.getParams().getErr());
            } else if(this.questionnaireDetails.equals("wrong member id")) {
            	Assert.assertEquals(resp.getParams().getErrmsg(), message);
                Assert.assertEquals("ERR_GRAPH_ADD_SET_MEMBER_INVALID_REQ_PARAMS", resp.getParams().getErr());
            }          
        }
	}    
}

