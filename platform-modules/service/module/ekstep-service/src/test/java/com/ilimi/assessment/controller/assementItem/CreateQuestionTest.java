package com.ilimi.assessment.controller.assementItem;

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
public class CreateQuestionTest extends BaseCucumberTest {

    private String taxonomyId;
    private String questionDetails;
    private ResultActions actions;

    private void basicAssertion(Response resp) {
        Assert.assertEquals("ekstep.lp.assessment_item.create", resp.getId());
        Assert.assertEquals("1.0", resp.getVer());
    }

    public void getquestion(String questionId) {
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String> header = new HashMap<String, String>();
        String path = "/assessmentitem/" + questionId;
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

    @When("^Create Question Taxonomy is (.*) with (.*)$")
    public void createquestion(String taxonomyId, String questionDetails) throws Exception {
        this.taxonomyId = taxonomyId;
        this.questionDetails = questionDetails;
        String contentString = "{ \"request\": { \"assessment_item\": { \"identifier\":\"Q2\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Select a char of vowels - 1.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"question_type\": \"mcq\", \"description\": \"GeometryTest\", \"options\": [ { \"content_type\": \"text/html\", \"content\": \"A\", \"is_answer\": true }, { \"content_type\": \"text/html\", \"content\": \"B\", \"is_answer\": false }, { \"content_type\": \"text/html\", \"content\": \"C\", \"is_answer\": false } ], \"code\": \"Q1\", \"difficulty_level\": \"easy\", \"num_answers\": 3, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String> header = new HashMap<String, String>();
        String path = "/assessmentitem";
        header.put("user-id", "ilimi");
        if (this.taxonomyId.equals("empty"))
            params.put("taxonomyId", "");
        else if (!this.taxonomyId.equals("absent"))
            params.put("taxonomyId", this.taxonomyId);
        
        if (questionDetails.equals("proper question data for mcq")) {
            contentString = "{ \"request\": { \"assessment_item\": { \"identifier\":\"Q2\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Select a char of vowels - 1.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"question_type\": \"mcq\", \"description\": \"GeometryTest\", \"options\": [ { \"content_type\": \"text/html\", \"content\": \"A\", \"is_answer\": true }, { \"content_type\": \"text/html\", \"content\": \"B\", \"is_answer\": false }, { \"content_type\": \"text/html\", \"content\": \"C\", \"is_answer\": false } ], \"code\": \"Q1\", \"difficulty_level\": \"easy\", \"num_answers\": 3, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        } else if (questionDetails.equals("proper question data for mmcq")) {
            contentString = "{ \"request\": { \"assessment_item\": { \"identifier\":\"Q2\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Select a char of vowels - 1.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"question_type\": \"mmcq\", \"description\": \"GeometryTest\", \"options\": [ { \"content_type\": \"text/html\", \"content\": \"A\", \"is_answer\": true }, { \"content_type\": \"text/html\", \"content\": \"B\", \"is_answer\": true }, { \"content_type\": \"text/html\", \"content\": \"C\", \"is_answer\": false } ], \"code\": \"Q1\", \"difficulty_level\": \"easy\", \"num_answers\": 3, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        } else if (questionDetails.equals("proper question data for ftb")) {
            contentString = "{ \"request\": { \"assessment_item\": { \"identifier\":\"Q2\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Select a char of vowels - 1.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"question_type\": \"ftb\", \"description\": \"GeometryTest\", \"answer\": [ { \"Answer\": \"correct\" } ], \"code\": \"Q1\", \"difficulty_level\": \"easy\", \"num_answers\": 3, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        } else if (questionDetails.equals("proper question data for speech_question")) {
            contentString = "{ \"request\": { \"assessment_item\": { \"identifier\":\"Q2\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Select a char of vowels - 1.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"question_type\": \"speech_question\", \"description\": \"GeometryTest\", \"answer\": [ { \"Answer\": \"correct\" } ], \"code\": \"Q1\", \"difficulty_level\": \"easy\", \"num_answers\": 3, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        } else if (questionDetails.equals("proper question data for canvas_question")) {
            contentString = "{ \"request\": { \"assessment_item\": { \"identifier\":\"Q2\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Select a char of vowels - 1.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"question_type\": \"canvas_question\", \"description\": \"GeometryTest\", \"answer\": [ { \"Answer\": \"correct\" } ], \"code\": \"Q1\", \"difficulty_level\": \"easy\", \"num_answers\": 3, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        } else if (questionDetails.equals("proper question data for mtf")) {
            contentString = "{ \"request\": { \"assessment_item\": { \"identifier\":\"Q3\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Select a char of vowels - 1.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"question_type\": \"mtf\", \"description\": \"GeometryTest\", \"code\": \"Q3\", \"lhs_options\":[{\"content_type\": \"text/html\", \"content\": \"A\", \"index\" : true }], \"rhs_options\":[{\"content_type\": \"text/html\", \"content\": \"A\", \"index\" : true }], \"difficulty_level\": \"easy\", \"num_answers\": 3, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        } else if (questionDetails.equals("invalid question options for mcq")) {
            contentString = "{ \"request\": { \"assessment_item\": { \"identifier\":\"Q2\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Select a char of vowels - 1.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"question_type\": \"mcq\", \"description\": \"GeometryTest\", \"options\": [ { \"content_type\": \"text/html\", \"content\": \"A\", \"is_answer\": true }, { \"content_type\": \"text/html\", \"content\": \"B\", \"is_answer\": false }, { \"content_type\": \"text/html\", \"content\": \"C\" } ], \"code\": \"Q1\", \"difficulty_level\": \"easy\", \"num_answers\": 3, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        } else if (questionDetails.equals("multiple answer for mcq")) {
            contentString = "{ \"request\": { \"assessment_item\": { \"identifier\":\"Q2\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Select a char of vowels - 1.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"question_type\": \"mcq\", \"description\": \"GeometryTest\", \"options\": [ { \"content_type\": \"text/html\", \"content\": \"A\", \"is_answer\": true }, { \"content_type\": \"text/html\", \"content\": \"B\", \"is_answer\": false }, { \"content_type\": \"text/html\", \"content\": \"C\", \"is_answer\": true } ], \"code\": \"Q2\", \"difficulty_level\": \"easy\", \"num_answers\": 3, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        } else if (questionDetails.equals("no options for mcq")) {
            contentString = "{ \"request\": { \"assessment_item\": { \"identifier\":\"Q2\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Select a char of vowels - 1.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"question_type\": \"mcq\", \"description\": \"GeometryTest\", \"options\": [ { \"content_type\": \"text/html\", \"content\": \"A\", \"is_answer\": false }, { \"content_type\": \"text/html\", \"content\": \"B\", \"is_answer\": false }, { \"content_type\": \"text/html\", \"content\": \"C\", \"is_answer\": false} ], \"code\": \"Q2\", \"difficulty_level\": \"easy\", \"num_answers\": 3, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        } else if (questionDetails.equals("no multiple answer for mmcq")) {
            contentString = "{ \"request\": { \"assessment_item\": { \"identifier\":\"Q2\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Select a char of vowels - 1.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"question_type\": \"mmcq\", \"description\": \"GeometryTest\", \"options\": [ { \"content_type\": \"text/html\", \"content\": \"A\", \"is_answer\": true }, { \"content_type\": \"text/html\", \"content\": \"B\", \"is_answer\": false }, { \"content_type\": \"text/html\", \"content\": \"C\", \"is_answer\": false } ], \"code\": \"Q2\", \"difficulty_level\": \"easy\", \"num_answers\": 3, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
            System.out.println("\nEnter MMCQ........\n");
        }
        
        
        
        
        
        else if (questionDetails.equals("question as blank")) {
            contentString = "{\"request\": {}}";
        } else if (questionDetails.equals("empty object type")) {
            contentString = "{ \"request\": { \"assessment_item\": { \"identifier\":\"Q2\", \"objectType\": \"\", \"metadata\": { \"title\": \"Select a char of vowels - 1.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"question_type\": \"mcq\", \"description\": \"GeometryTest\", \"options\": [ { \"content_type\": \"text/html\", \"content\": \"A\", \"is_answer\": true }, { \"content_type\": \"text/html\", \"content\": \"B\", \"is_answer\": false }, { \"content_type\": \"text/html\", \"content\": \"C\", \"is_answer\": false } ], \"code\": \"Q1\", \"difficulty_level\": \"easy\", \"num_answers\": 3, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        } else if (questionDetails.equals("wrong definition node")) {
            contentString = "{ \"request\": { \"assessment_item\": { \"identifier\":\"Q2\", \"objectType\": \"ilimi\", \"metadata\": { \"title\": \"Select a char of vowels - 1.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"question_type\": \"mcq\", \"description\": \"GeometryTest\", \"options\": [ { \"content_type\": \"text/html\", \"content\": \"A\", \"is_answer\": true }, { \"content_type\": \"text/html\", \"content\": \"B\", \"is_answer\": false }, { \"content_type\": \"text/html\", \"content\": \"C\", \"is_answer\": false } ], \"code\": \"Q1\", \"difficulty_level\": \"easy\", \"num_answers\": 3, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        } else if (questionDetails.equals("require metadata")) {
            contentString = "{ \"request\": { \"assessment_item\": { \"identifier\":\"Q2\", \"objectType\": \"AssessmentItem\",\"metadata\" : {}, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        } else if(questionDetails.equals("invalid data type for select")) {
            contentString = "{ \"request\": { \"assessment_item\": { \"identifier\":\"Q2\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Select a char of vowels - 1.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"question_type\": \"mcq\", \"description\": \"GeometryTest\", \"options\": [ { \"content_type\": \"text/html\", \"content\": \"A\", \"is_answer\": true }, { \"content_type\": \"text/html\", \"content\": \"B\", \"is_answer\": false }, { \"content_type\": \"text/html\", \"content\": \"C\", \"is_answer\": false } ], \"code\": \"Q1\", \"difficulty_level\": \"easy\", \"num_answers\": 3, \"owner\": \"Ilimi\",\"status\": [\"ilimi\"], \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        } else if(questionDetails.equals("invalid data type")) {
            contentString = "{ \"request\": { \"assessment_item\": { \"identifier\":\"Q2\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Select a char of vowels - 1.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"question_type\": \"mcq\", \"description\": \"GeometryTest\", \"options\": [ { \"content_type\": \"text/html\", \"content\": \"A\", \"is_answer\": true }, { \"content_type\": \"text/html\", \"content\": \"B\", \"is_answer\": false }, { \"content_type\": \"text/html\", \"content\": \"C\", \"is_answer\": false } ], \"code\": \"Q1\", \"difficulty_level\": \"easy\", \"num_answers\": 3, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": \"ilimi\", \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        } else if(questionDetails.equals("unsupported relation")) {
        	contentString = "{ \"request\": { \"assessment_item\": { \"identifier\":\"Q2\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Select a char of vowels - 1.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"question_type\": \"mcq\", \"description\": \"GeometryTest\", \"options\": [ { \"content_type\": \"text/html\", \"content\": \"A\", \"is_answer\": true }, { \"content_type\": \"text/html\", \"content\": \"B\", \"is_answer\": false }, { \"content_type\": \"text/html\", \"content\": \"C\", \"is_answer\": false } ], \"code\": \"Q1\", \"difficulty_level\": \"easy\", \"num_answers\": 3, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"ilimi\" } ] } } }";
        }

        actions = resultActionPost(contentString, path, params, MediaType.APPLICATION_JSON, header, mockMvc);
    }

    @Then("^return status of create Question is (.*) and response code is (\\d+)$")
    public void assertResultAction(String status, int code) {
        assertStatus(actions, code);
        Response resp = jasonToObject(actions);
        if (resp != null) {
            basicAssertion(resp);
            Assert.assertEquals(status, resp.getParams().getStatus());
            if (StringUtils.isNotBlank(taxonomyId) && !taxonomyId.equals("absent") && !taxonomyId.equals("empty")
                    && questionDetails.trim().toLowerCase().equals("proper question data")) {
                Map<String, Object> result = resp.getResult();
                getquestion((String) result.get("node_id"));
            }
        }
    }

    @And("^get error message as (.*)$")
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
            if (this.questionDetails.equals("question as blank")) {
                Assert.assertEquals(message.toLowerCase(), resp.getParams().getErrmsg().toLowerCase());
                Assert.assertEquals("ERR_ASSESSMENT_BLANK_ITEM", resp.getParams().getErr());
            } else if (this.questionDetails.equals("empty object type") || this.questionDetails.equals("wrong definition node")
                    || this.questionDetails.equals("unsupported relation") || this.questionDetails.equals("invalid data type for select") || this.questionDetails.equals("invalid question options for mcq") || this.questionDetails.equals("multiple answer for mcq") || this.questionDetails.equals("no multiple answer for mmcq") || this.questionDetails.equals("no options answer for mcq")) {
                Map<String, Object> result = resp.getResult();
                @SuppressWarnings("unchecked")
                ArrayList<String> msg = (ArrayList<String>) result.get("messages");
                Assert.assertEquals(message.toLowerCase(), msg.get(0).toLowerCase());
            } else if(this.questionDetails.equals("invalid relation node")) {
                Map<String, Object> result = resp.getResult();
                @SuppressWarnings("unchecked")
                ArrayList<String> msg = (ArrayList<String>) result.get("messages");
                Assert.assertTrue(msg.get(0).toLowerCase().contains(message.toLowerCase()));
            }
        }
    }

}
