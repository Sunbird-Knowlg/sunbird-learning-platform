package com.ilimi.assessment.controller.questionnaire;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
public class CreateQuestionnaireTest extends BaseCucumberTest {

    private String taxonomyId;
    private String questionnaireDetails;
    private ResultActions actions;

    private void basicAssertion(Response resp) {
        Assert.assertEquals("ekstep.lp.questionnaire.create", resp.getId());
        Assert.assertEquals("1.0", resp.getVer());
    }

    public void getquestionnaire(String questionnaireId) {
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String> header = new HashMap<String, String>();
        String path = "/questionnaire/" + questionnaireId;
        params.put("taxonomyId", "numeracy");
        params.put("cfields", "name");
        header.put("user-id", "ilimi");
        ResultActions actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc);
        try {
            actions.andExpect(status().isOk());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Response resp = jasonToObject(actions);
        Assert.assertEquals("successful", resp.getParams().getStatus());
    }

    @Before
    public void setup() throws IOException {
        initMockMVC();
    }

    @When("^Create Questionnaire Taxonomy is (.*) with (.*)$")
    public void createquestionnaire(String taxonomyId, String questionnaireDetails) throws Exception {
        this.taxonomyId = taxonomyId;
        this.questionnaireDetails = questionnaireDetails;

        String contentString = "{ \"request\": { \"questionnaire\": { \"objectType\": \"Questionnaire\", \"metadata\": { \"code\": \"QR1\", \"language\": \"English\", \"title\": \"Demo Questionnaire for Ekstep Platform\", \"description\": \"Description of Demo Questionnaire - Ekstep Platform\", \"instructions\": \"Instructions of Demo Questionnaire - Ekstep Platform\", \"used_for\": \"assessment\", \"type\": \"materialised\", \"duration\": 20, \"total_items\": 6, \"strict_sequencing\": false, \"allow_skip\": true, \"max_score\": 20, \"status\": \"Draft\", \"owner\": \"Ilimi\", \"copyright\": \"Ilimi\", \"license\": \"Ilimi\", \"items\": [ \"Q1\", \"Q4\",\"Q10\", \"Q5\", \"Q6\", \"Q7\", \"Q8\",\"Q9\" ] }, \"outRelations\": [ { \"relationType\": \"associatedTo\", \"endNodeId\": \"Num:C1:SC1\" } ] } } }";
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String> header = new HashMap<String, String>();
        String path = "/questionnaire";
        header.put("user-id", "ilimi");
        if (this.taxonomyId.equals("empty"))
            params.put("taxonomyId", "");
        else if (!this.taxonomyId.equals("absent"))
            params.put("taxonomyId", this.taxonomyId);

        if (questionnaireDetails.equals("questionnaire as blank")) {
            contentString = "{\"request\": {}}";
        } else if (questionnaireDetails.equals("empty object type")) {
            contentString = "{ \"request\": { \"questionnaire\": { \"objectType\": \"\", \"metadata\": { \"code\": \"QR1\", \"language\": \"English\", \"title\": \"Demo Questionnaire for Ekstep Platform\", \"description\": \"Description of Demo Questionnaire - Ekstep Platform\", \"instructions\": \"Instructions of Demo Questionnaire - Ekstep Platform\", \"used_for\": \"assessment\", \"type\": \"materialised\", \"duration\": 20, \"total_items\": 6, \"strict_sequencing\": false, \"allow_skip\": true, \"max_score\": 20, \"status\": \"Draft\", \"owner\": \"Ilimi\", \"copyright\": \"Ilimi\", \"license\": \"Ilimi\", \"items\": [ \"Q1\", \"Q2\", \"Q3\", \"Q4\", \"Q5\", \"Q6\", \"Q7\", \"Q8\" ] }, \"outRelations\": [ { \"relationType\": \"associatedTo\", \"endNodeId\": \"Num:C1:SC1\" } ] } } }";
        } else if (questionnaireDetails.equals("wrong definition node")) {
            contentString = "{ \"request\": { \"questionnaire\": { \"objectType\": \"ilimi\", \"metadata\": { \"code\": \"QR1\", \"language\": \"English\", \"title\": \"Demo Questionnaire for Ekstep Platform\", \"description\": \"Description of Demo Questionnaire - Ekstep Platform\", \"instructions\": \"Instructions of Demo Questionnaire - Ekstep Platform\", \"used_for\": \"assessment\", \"type\": \"materialised\", \"duration\": 20, \"total_items\": 6, \"strict_sequencing\": false, \"allow_skip\": true, \"max_score\": 20, \"status\": \"Draft\", \"owner\": \"Ilimi\", \"copyright\": \"Ilimi\", \"license\": \"Ilimi\", \"items\": [ \"Q1\", \"Q2\", \"Q3\", \"Q4\", \"Q5\", \"Q6\", \"Q7\", \"Q8\" ] }, \"outRelations\": [ { \"relationType\": \"associatedTo\", \"endNodeId\": \"Num:C1:SC1\" } ] } } }";
        } else if (questionnaireDetails.equals("require metadata")) {
            contentString = "{ \"request\": { \"questionnaire\": { \"objectType\": \"Questionnaire\", \"metadata\": {  }, \"outRelations\": [ { \"relationType\": \"associatedTo\", \"endNodeId\": \"Num:C1:SC1\" } ] } } }";
        } else if(questionnaireDetails.equals("unsupported relation")) {
            contentString = "{ \"request\": { \"questionnaire\": { \"objectType\": \"Questionnaire\", \"metadata\": { \"code\": \"QR1\", \"language\": \"English\", \"title\": \"Demo Questionnaire for Ekstep Platform\", \"description\": \"Description of Demo Questionnaire - Ekstep Platform\", \"instructions\": \"Instructions of Demo Questionnaire - Ekstep Platform\", \"used_for\": \"assessment\", \"type\": \"materialised\", \"duration\": 20, \"total_items\": 6, \"strict_sequencing\": false, \"allow_skip\": true, \"max_score\": 20, \"status\": \"Draft\", \"owner\": \"Ilimi\", \"copyright\": \"Ilimi\", \"license\": \"Ilimi\", \"items\": [ \"Q1\", \"Q2\", \"Q3\", \"Q4\", \"Q5\", \"Q6\", \"Q7\", \"Q8\" ] }, \"outRelations\": [ { \"relationType\": \"ilimi\", \"endNodeId\": \"Num:C1:SC1\" } ] } } }";
        } else if(questionnaireDetails.equals("invalid data type for select")) {
            contentString = "{ \"request\": { \"questionnaire\": { \"objectType\": \"Questionnaire\", \"metadata\": { \"code\": \"QR1\", \"used_for\":\"ilimi\", \"language\": \"English\", \"title\": \"Demo Questionnaire for Ekstep Platform\", \"description\": \"Description of Demo Questionnaire - Ekstep Platform\", \"instructions\": \"Instructions of Demo Questionnaire - Ekstep Platform\", \"used_for\": \"assessment\", \"type\": \"materialised\", \"duration\": 20, \"total_items\": 6, \"strict_sequencing\": false, \"allow_skip\": true, \"max_score\": 20, \"status\": \"Draft\", \"owner\": \"Ilimi\", \"copyright\": \"Ilimi\", \"license\": \"Ilimi\", \"items\": [ \"Q1\", \"Q2\", \"Q3\", \"Q4\", \"Q5\", \"Q6\", \"Q7\", \"Q8\" ] }, \"outRelations\": [ { \"relationType\": \"associatedTo\", \"endNodeId\": \"Num:C1:SC1\" } ] } } }";
        } else if(questionnaireDetails.equals("insufficient assessment items")) {
        	contentString = "{ \"request\": { \"questionnaire\": { \"objectType\": \"Questionnaire\", \"metadata\": { \"code\": \"QR1\", \"language\": \"English\", \"title\": \"Demo Questionnaire for Ekstep Platform\", \"description\": \"Description of Demo Questionnaire - Ekstep Platform\", \"instructions\": \"Instructions of Demo Questionnaire - Ekstep Platform\", \"used_for\": \"assessment\", \"type\": \"materialised\", \"duration\": 20, \"total_items\": 16, \"strict_sequencing\": false, \"allow_skip\": true, \"max_score\": 20, \"status\": \"Draft\", \"owner\": \"Ilimi\", \"copyright\": \"Ilimi\", \"license\": \"Ilimi\", \"items\": [ \"Q1\", \"Q2\", \"Q3\", \"Q4\", \"Q5\", \"Q6\", \"Q7\", \"Q8\" ] }, \"outRelations\": [ { \"relationType\": \"associatedTo\", \"endNodeId\": \"Num:C1:SC1\" } ] } } }";
        } else if(questionnaireDetails.equals("wrong member id")) {
        	contentString = "{ \"request\": { \"questionnaire\": { \"objectType\": \"Questionnaire\", \"metadata\": { \"code\": \"QR1\", \"language\": \"English\", \"title\": \"Demo Questionnaire for Ekstep Platform\", \"description\": \"Description of Demo Questionnaire - Ekstep Platform\", \"instructions\": \"Instructions of Demo Questionnaire - Ekstep Platform\", \"used_for\": \"assessment\", \"type\": \"materialised\", \"duration\": 20, \"total_items\": 5, \"strict_sequencing\": false, \"allow_skip\": true, \"max_score\": 20, \"status\": \"Draft\", \"owner\": \"Ilimi\", \"copyright\": \"Ilimi\", \"license\": \"Ilimi\", \"items\": [ \"Q1\", \"Q2\", \"Q3\", \"Q4\", \"Q5\", \"Q6\", \"Q17\", \"Q21\" ] }, \"outRelations\": [ { \"relationType\": \"associatedTo\", \"endNodeId\": \"Num:C1:SC1\" } ] } } }";
        }

        actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);
    }

    @Then("^return status of create Questionnaire is (.*) and response code is (\\d+)$")
    public void assertResultAction(String status, int code) {
        assertStatus(actions, code);
        Response resp = jasonToObject(actions);
        if (resp != null) {
            basicAssertion(resp);
            Assert.assertEquals(status, resp.getParams().getStatus());
            if (StringUtils.isNotBlank(taxonomyId) && !taxonomyId.equals("absent") && !taxonomyId.equals("empty")
                    && questionnaireDetails.trim().toLowerCase().equals("proper questionnaire data")) {
                Map<String, Object> result = resp.getResult();
                getquestionnaire((String) result.get("node_id"));
            }
        }
    }

    @And("^get error message of create Questionnaire is (.*)$")
    public void assertErrorMessage(String message) {
        System.out.println("Msg:" + message);
        if (taxonomyId.equals("absent")) {
            Assert.assertEquals(actions.andReturn().getResponse().getErrorMessage(), message);
        } else if (taxonomyId.equals("empty")) {
            Response resp = jasonToObject(actions);
            Assert.assertEquals(resp.getParams().getErrmsg(), message);
            Assert.assertEquals("ERR_ASSESSMENT_BLANK_TAXONOMY_ID", resp.getParams().getErr());
        } else {
            Response resp = jasonToObject(actions);
            if (this.questionnaireDetails.equals("questionnaire as blank")) {
                Assert.assertEquals(message.toLowerCase(), resp.getParams().getErrmsg().toLowerCase());
                Assert.assertEquals("ERR_ASSESSMENT_BLANK_QUESTIONNAIRE", resp.getParams().getErr());
            } else if (this.questionnaireDetails.equals("empty object type") || this.questionnaireDetails.equals("wrong definition node") || this.questionnaireDetails.equals("insufficient assessment items")
                    || this.questionnaireDetails.equals("unsupported relation") || this.questionnaireDetails.equals("require metadata") || this.questionnaireDetails.equals("invalid data type for select")) {
                Map<String, Object> result = resp.getResult();
                @SuppressWarnings("unchecked")
                ArrayList<String> msg = (ArrayList<String>) result.get("messages");
                Assert.assertEquals(message.toLowerCase(), msg.get(0).toLowerCase());
            } else if(this.questionnaireDetails.equals("wrong member id")) {
            	Assert.assertEquals(resp.getParams().getErrmsg(), message);
                Assert.assertEquals("ERR_GRAPH_CREATE_SET_INVALID_MEMBER_IDS", resp.getParams().getErr());
            }
        }
    }

}
