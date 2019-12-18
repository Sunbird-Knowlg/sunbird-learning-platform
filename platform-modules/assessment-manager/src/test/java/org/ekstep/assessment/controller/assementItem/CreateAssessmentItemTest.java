package org.ekstep.assessment.controller.assementItem;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
public class CreateAssessmentItemTest extends BaseCucumberTest {

    private String taxonomyId;
    private String questionDetails;
    private ResultActions actions;

    private void basicAssertion(Response resp) {
        Assert.assertEquals("ekstep.lp.assessment_item.create", resp.getId());
        Assert.assertEquals("1.0", resp.getVer());
    }
 
    public String createQuestion(String temp){
		String QuestionId = temp;
		String contentString = "{ \"request\": { \"assessment_item\": { \"identifier\":\""+QuestionId +"\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Select a char of vowels - 1.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"type\": \"mcq\", \"description\": \"GeometryTest\", \"options\": [{\"value\":{\"type\":\"text\",\"content\":\"A\",\"asset\":\"a\"},\"score\":1,\"answer\":true},{\"value\":{\"type\":\"text\",\"content\":\"B\",\"asset\":\"a\"}},{\"value\":{\"type\":\"text\",\"content\":\"C\",\"asset\":\"a\"}}], \"code\": \"Q1\", \"difficulty_level\": \"low\", \"num_answers\": 1, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3,\"qlevel\":\"EASY\",\"max_score\":5, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
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
    
    public void getquestion(String questionId) {
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String> header = new HashMap<String, String>();
        String path = "/v1/assessmentitem/" + questionId;
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
    
    @When("^create question data for Assessment-items$")
    public void create(){
    	ArrayList<String> arr = new ArrayList<String>();
    	arr.add("Q1");arr.add("Q2");arr.add("Q3");arr.add("Q4");arr.add("Q5");arr.add("Q6");arr.add("Q7");arr.add("Q8");arr.add("Q9");arr.add("Q10");
        for(int i = 0; i < 10; i++){
        	createQuestion(arr.get(i));
        }
        
    }
    
    @When("^Creating a Question Taxonomy id is (.*) with (.*)$")
    public void createquestion(String taxonomyId, String questionDetails) throws Exception {
        this.taxonomyId = taxonomyId;
        this.questionDetails = questionDetails;
        String contentString = "{ \"request\": { \"assessment_item\": { \"identifier\":\"tQ1\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Select a char of vowels - 1.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"type\": \"mcq\", \"description\": \"GeometryTest\", \"options\": [{\"value\":{\"type\":\"text\",\"content\":\"A\",\"asset\":\"a\"},\"score\":1,\"answer\":true},{\"value\":{\"type\":\"text\",\"content\":\"B\",\"asset\":\"a\"}},{\"value\":{\"type\":\"text\",\"content\":\"C\",\"asset\":\"a\"}}], \"code\": \"Q1\", \"difficulty_level\": \"low\", \"num_answers\": 1, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3,\"qlevel\":\"EASY\",\"max_score\":5, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String> header = new HashMap<String, String>();
        String path = "/v1/assessmentitem";
        header.put("user-id", "ilimi");
        if ("empty".equals(this.taxonomyId))
            params.put("taxonomyId", "");
        else if (!"absent".equals(this.taxonomyId))
            params.put("taxonomyId", this.taxonomyId);        
        if (questionDetails.equals("proper question data for mcq")) {
            contentString = "{ \"request\": { \"assessment_item\": { \"identifier\":\"tQ21\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Select a char of vowels - 21.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"type\": \"mcq\", \"description\": \"GeometryTest\", \"options\": [{\"value\":{\"type\":\"text\",\"content\":\"A\",\"asset\":\"a\"},\"score\":1,\"answer\":true},{\"value\":{\"type\":\"text\",\"content\":\"B\",\"asset\":\"a\"}},{\"value\":{\"type\":\"text\",\"content\":\"C\",\"asset\":\"a\"}}], \"code\": \"Q24\", \"difficulty_level\": \"low\", \"num_answers\": 3, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3,\"qlevel\":\"EASY\",\"max_score\":5, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        } else if (questionDetails.equals("proper question data for mmcq")) {
            contentString = "{\"request\":{\"assessment_item\":{\"identifier\":\"tQ2\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"title\":\"Select vowels letters.\",\"body\":{\"content_type\":\"text/html\",\"content\":\"Select vowels letters.\"},\"type\":\"mmcq\",\"description\":\"GeometryTest\",\"options\":[{\"value\":{\"type\":\"text\",\"content\":\"A\",\"asset\":\"aaa\"},\"score\":1},{\"value\":{\"type\":\"text\",\"content\":\"B\",\"asset\":\"bbb\"},\"score\":1},{\"value\":{\"type\":\"text\",\"content\":\"C\",\"asset\":\"ccc\"}}],\"code\":\"MMCQ1\",\"difficulty_level\":\"low\",\"num_answers\":2,\"owner\":\"Ilimi\",\"used_for\":\"assessment\",\"score\":3,\"qlevel\":\"EASY\",\"max_score\":5,\"max_time\":120,\"rendering_metadata\":[{\"interactivity\":[\"drag-drop\",\"zoom\"],\"keywords\":[\"compare\",\"multi-options\"],\"rendering_hints\":{\"styles\":\"css styles that will override the theme level styles for this one item\",\"view-mode\":\"landscape\"}}]},\"outRelations\":[{\"endNodeId\":\"Num:C1:SC1\",\"relationType\":\"associatedTo\"}]}}}";
        } else if (questionDetails.equals("proper question data for ftb")) {
            contentString = "{\"request\":{\"assessment_item\":{\"identifier\":\"aq1_set_1_2\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"identifier\":\"aq1_set_1_2\",\"template\":\"ftb_template_1\",\"type\":\"ftb\",\"num_answers\":1,\"model\":{},\"answer\":{\"ans1\":{\"value\":5,\"score\":1}},\"code\":\"aq1_set_1_2\",\"qlevel\":\"MEDIUM\",\"owner\":\"EkStep\",\"used_for\":\"worksheet\",\"max_score\":1,\"partial_scoring\":true},\"outRelations\":[{\"endNodeId\":\"Num:C1\",\"relationType\":\"associatedTo\"}]}}}";
        } else if (questionDetails.equals("proper question data for speech_question")) {
            contentString = "{\"request\":{\"assessment_item\":{\"identifier\":\"SQ1\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"identifier\":\"SQ1\",\"template\":\"speech_question_template_1\",\"type\":\"speech_question\",\"num_answers\":1,\"model\":{},\"answer\":{\"ans1\":{\"value\":5,\"score\":1}},\"code\":\"SQ1\",\"qlevel\":\"MEDIUM\",\"owner\":\"EkStep\",\"used_for\":\"worksheet\",\"max_score\":1,\"partial_scoring\":true},\"outRelations\":[{\"endNodeId\":\"Num:C1\",\"relationType\":\"associatedTo\"}]}}}";
        } else if (questionDetails.equals("proper question data for canvas_question")) {
            contentString = "{\"request\":{\"assessment_item\":{\"identifier\":\"CQ1\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"identifier\":\"CQ1\",\"template\":\"canvas_question_template_1\",\"type\":\"canvas_question\",\"num_answers\":1,\"model\":{},\"answer\":{\"ans1\":{\"value\":5,\"score\":1}},\"code\":\"CQ1\",\"qlevel\":\"MEDIUM\",\"owner\":\"EkStep\",\"used_for\":\"worksheet\",\"max_score\":1,\"partial_scoring\":true},\"outRelations\":[{\"endNodeId\":\"Num:C1\",\"relationType\":\"associatedTo\"}]}}}";
        } else if (questionDetails.equals("proper question data for mtf")) {
            contentString = "{\"request\":{\"assessment_item\":{\"identifier\":\"ek.n.q916\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"code\":\"ek.n.q916\",\"type\":\"mtf\",\"template\":\"mtf_template_3\",\"qlevel\":\"MEDIUM\",\"title\":\"????????? ???????????? ????? ????????? ????????.\",\"question\":\"2080\",\"model\":{\"data0\":\"23450\",\"data1\":\"23540\"},\"lhs_options\":[{\"value\":{\"type\":\"image\",\"asset\":\"grey\"},\"index\":0}],\"rhs_options\":[{\"value\":{\"type\":\"text\",\"asset\":\">\",\"font\":\"Verdana\",\"color\":\"white\",\"fontsize\":\"1000\"}},{\"value\":{\"type\":\"text\",\"asset\":\"=\",\"font\":\"Verdana\",\"color\":\"white\",\"fontsize\":\"1000\"}},{\"value\":{\"type\":\"text\",\"asset\":\"<\",\"font\":\"Verdana\",\"color\":\"white\",\"fontsize\":\"1000\"},\"answer\":0}],\"max_score\":6,\"partial_scoring\":true,\"feedback\":\"\"}}}}";
        } else if (questionDetails.equals("invalid question options for mcq")) {
            contentString = "{\"request\":{\"assessment_item\":{\"identifier\":\"tQ2\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"title\":\"Select a char of vowels - 1.\",\"body\":{\"content_type\":\"text/html\",\"content\":\"Select a char of vowels.\"},\"type\":\"mcq\",\"description\":\"GeometryTest\",\"options\":[{\"value\":{\"type\":\"text\",\"content\":\"A\",\"asset\":\"aaa\"}},{\"value\":{\"type\":\"text\",\"content\":\"B\",\"asset\":\"bbb\"}},{\"value\":{\"type\":\"text\",\"content\":\"C\",\"asset\":\"ccc\"}}],\"code\":\"Q1\",\"difficulty_level\":\"low\",\"num_answers\":3,\"owner\":\"Ilimi\",\"used_for\":\"assessment\",\"score\":3,\"qlevel\":\"EASY\",\"max_score\":5,\"max_time\":120,\"rendering_metadata\":[{\"interactivity\":[\"drag-drop\",\"zoom\"],\"keywords\":[\"compare\",\"multi-options\"],\"rendering_hints\":{\"styles\":\"css styles that will override the theme level styles for this one item\",\"view-mode\":\"landscape\"}}]},\"outRelations\":[{\"endNodeId\":\"Num:C1:SC1\",\"relationType\":\"associatedTo\"}]}}}";
        } else if (questionDetails.equals("invalid question options for ftb")) {
            contentString = "{\"request\":{\"assessment_item\":{\"identifier\":\"aq1_set_1_2\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"identifier\":\"aq1_set_1_2\",\"template\":\"ftb_template_1\",\"type\":\"ftb\",\"num_answers\":1,\"model\":{},\"code\":\"aq1_set_1_2\",\"qlevel\":\"MEDIUM\",\"owner\":\"EkStep\",\"used_for\":\"worksheet\",\"max_score\":1,\"partial_scoring\":true},\"outRelations\":[{\"endNodeId\":\"Num:C1\",\"relationType\":\"associatedTo\"}]}}}";
        } else if (questionDetails.equals("invalid question options for mtf")) {
            contentString = "{\"request\":{\"assessment_item\":{\"identifier\":\"ek.n.q916\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"code\":\"ek.n.q916\",\"type\":\"mtf\",\"template\":\"mtf_template_3\",\"qlevel\":\"MEDIUM\",\"title\":\"????????? ???????????? ????? ????????? ????????.\",\"question\":\"2080\",\"model\":{\"data0\":\"23450\",\"data1\":\"23540\"},\"lhs_options\":[{\"value\":{\"type\":\"image\",\"asset\":\"grey\"}}],\"rhs_options\":[{\"value\":{\"type\":\"text\",\"asset\":\">\",\"font\":\"Verdana\",\"color\":\"white\",\"fontsize\":\"1000\"}},{\"value\":{\"type\":\"text\",\"asset\":\"=\",\"font\":\"Verdana\",\"color\":\"white\",\"fontsize\":\"1000\"}},{\"value\":{\"type\":\"text\",\"asset\":\"<\",\"font\":\"Verdana\",\"color\":\"white\",\"fontsize\":\"1000\"},\"answer\":0}],\"max_score\":6,\"partial_scoring\":true,\"feedback\":\"\"}}}}";
        } else if (questionDetails.equals("wrong answer for ftb")) {
            contentString = "{ \"request\": { \"assessment_item\": { \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Write first letter of vowels in english.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Write first letter of vowels in english.\" }, \"type\": \"ftb\", \"description\": \"Literacy Test\", \"code\": \"FTB_1\", \"difficulty_level\": \"low\", \"num_answers\": 3, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"answer\":\"wrongAns\" \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        } else if (questionDetails.equals("multiple answer for mcq")) {
            contentString = "{\"request\":{\"assessment_item\":{\"identifier\":\"tQ21\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"title\":\"Select a char of vowels - 21.\",\"body\":{\"content_type\":\"text/html\",\"content\":\"Select a char of vowels.\"},\"type\":\"mcq\",\"description\":\"GeometryTest\",\"options\":[{\"value\":{\"type\":\"text\",\"content\":\"A\",\"asset\":\"a\"}},{\"value\":{\"type\":\"text\",\"content\":\"B\",\"asset\":\"a\"}},{\"value\":{\"type\":\"text\",\"content\":\"C\",\"asset\":\"a\"}}],\"code\":\"Q24\",\"difficulty_level\":\"low\",\"num_answers\":3,\"owner\":\"Ilimi\",\"used_for\":\"assessment\",\"score\":3,\"qlevel\":\"EASY\",\"max_score\":5,\"max_time\":120,\"rendering_metadata\":[{\"interactivity\":[\"drag-drop\",\"zoom\"],\"keywords\":[\"compare\",\"multi-options\"],\"rendering_hints\":{\"styles\":\"css styles that will override the theme level styles for this one item\",\"view-mode\":\"landscape\"}}]},\"outRelations\":[{\"endNodeId\":\"Num:C1:SC1\",\"relationType\":\"associatedTo\"}]}}}";
        } else if (questionDetails.equals("no options for mcq")) {
            contentString = "{ \"request\": { \"assessment_item\": { \"identifier\":\"tQ2\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Select a char of vowels - 1.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"type\": \"mcq\", \"description\": \"GeometryTest\", \"options\": [ { \"content_type\": \"text/html\", \"content\": \"A\", \"is_answer\": false }, { \"content_type\": \"text/html\", \"content\": \"B\", \"is_answer\": false }, { \"content_type\": \"text/html\", \"content\": \"C\", \"is_answer\": false} ], \"code\": \"Q2\", \"difficulty_level\": \"low\", \"num_answers\": 3, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        } else if (questionDetails.equals("no multiple answer for mmcq")) {
            contentString = "{\"request\":{\"assessment_item\":{\"identifier\":\"tQ2\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"title\":\"Select vowels letters.\",\"body\":{\"content_type\":\"text/html\",\"content\":\"Select vowels letters.\"},\"type\":\"mmcq\",\"description\":\"GeometryTest\",\"options\":[{\"value\":{\"type\":\"text\",\"content\":\"A\",\"asset\":\"aaa\"},\"score\":1},{\"value\":{\"type\":\"text\",\"content\":\"B\",\"asset\":\"bbb\"}},{\"value\":{\"type\":\"text\",\"content\":\"C\",\"asset\":\"ccc\"}}],\"code\":\"MMCQ1\",\"difficulty_level\":\"low\",\"num_answers\":1,\"owner\":\"Ilimi\",\"used_for\":\"assessment\",\"score\":3,\"qlevel\":\"EASY\",\"max_score\":5,\"max_time\":120,\"rendering_metadata\":[{\"interactivity\":[\"drag-drop\",\"zoom\"],\"keywords\":[\"compare\",\"multi-options\"],\"rendering_hints\":{\"styles\":\"css styles that will override the theme level styles for this one item\",\"view-mode\":\"landscape\"}}]},\"outRelations\":[{\"endNodeId\":\"Num:C1:SC1\",\"relationType\":\"associatedTo\"}]}}}";
        } else if (questionDetails.equals("invalid no. of options for mmcq")) {
        	contentString = "{ \"request\": { \"assessment_item\": { \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Select vowels letters.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select vowels letters.\" }, \"type\": \"mmcq\", \"description\": \"GeometryTest\", \"options\": [ { \"content_type\": \"text/html\", \"content\": \"A\", \"is_answer\": true }, { \"content_type\": \"text/html\", \"content\": \"B\", \"is_answer\": false }, { \"content_type\": \"text/html\", \"content\": \"C\", \"is_answer\": true }, { \"content_type\": \"text/html\", \"content\": \"E\", \"is_answer\": true } ], \"code\": \"MMCQ1\", \"difficulty_level\": \"low\", \"num_answers\": 1, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        } else if (questionDetails.equals("invalid no. of answer for ftb")) {
        	contentString = "{\"request\":{\"assessment_item\":{\"identifier\":\"aq1_set_1_2\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"identifier\":\"aq1_set_1_2\",\"template\":\"ftb_template_1\",\"type\":\"ftb\",\"num_answers\":2,\"model\":{},\"answer\":{},\"code\":\"aq1_set_1_2\",\"qlevel\":\"MEDIUM\",\"owner\":\"EkStep\",\"used_for\":\"worksheet\",\"max_score\":1,\"partial_scoring\":true},\"outRelations\":[{\"endNodeId\":\"Num:C1\",\"relationType\":\"associatedTo\"}]}}}";
        } else if (questionDetails.equals("invalid no. of answer for mtf")) {
        	contentString = "{ \"request\": { \"assessment_item\": { \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Match the capital letters with small letters\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"type\": \"mtf\", \"description\": \"GeometryTest\", \"code\": \"MTFQ_1\", \"lhs_options\": [ { \"content_type\": \"text/html\", \"content\": \"K\", \"index\": 4 }, { \"content_type\": \"text/html\", \"content\": \"C\", \"index\": 1 }, { \"content_type\": \"text/html\", \"content\": \"B\", \"index\": 2 }, { \"content_type\": \"text/html\", \"content\": \"D\", \"index\": 3 } ], \"rhs_options\": [ { \"content_type\": \"text/html\", \"content\": \"c\", \"index\": 1 }, { \"content_type\": \"text/html\", \"content\": \"b\", \"index\": 2 }, { \"content_type\": \"text/html\", \"content\": \"d\", \"index\": 3 }, { \"content_type\": \"text/html\", \"content\": \"k\", \"index\": 4 } ], \"difficulty_level\": \"low\", \"num_answers\": 3, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        } else if (questionDetails.equals("invalid no. of answer for speech")) {
        	contentString = "{ \"request\": { \"assessment_item\": { \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Spell the letter 'a'.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Spell the letter 'a'.\" }, \"type\": \"speech_question\", \"description\": \"Literacy Test\", \"answer\": [ \"http://platform.ekstep.in/sounds/letter_a.mp3\" ], \"code\": \"SPQ_1\", \"difficulty_level\": \"low\", \"num_answers\": 3, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        } else if (questionDetails.equals("invalid no. of answer for canvas")) {
        	contentString = "{ \"request\": { \"assessment_item\": { \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Write the name of the animal.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Write the name of the animal showed below <img src='images/monkey.png' />\" }, \"type\": \"canvas_question\", \"description\": \"Literacy Test\", \"answer\": [ \"http://platform.ekstep.in/sounds/word_monkey.jpeg\" ], \"code\": \"CQ_1\", \"difficulty_level\": \"low\", \"num_answers\": 3, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        } else if (questionDetails.equals("question as blank")) {
            contentString = "{\"request\": {}}";
        } else if (questionDetails.equals("empty object type")) {
            contentString = "{ \"request\": { \"assessment_item\": { \"identifier\":\"tQ2\", \"objectType\": \"\", \"metadata\": { \"title\": \"Select a char of vowels - 1.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"type\": \"mcq\", \"description\": \"GeometryTest\", \"options\": [ { \"content_type\": \"text/html\", \"content\": \"A\", \"is_answer\": true }, { \"content_type\": \"text/html\", \"content\": \"B\", \"is_answer\": false }, { \"content_type\": \"text/html\", \"content\": \"C\", \"is_answer\": false } ], \"code\": \"Q1\", \"difficulty_level\": \"low\", \"num_answers\": 3, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        } else if (questionDetails.equals("wrong definition node")) {
            contentString = "{ \"request\": { \"assessment_item\": { \"identifier\":\"tQ2\", \"objectType\": \"ilimi\", \"metadata\": { \"title\": \"Select a char of vowels - 1.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"type\": \"mcq\", \"description\": \"GeometryTest\", \"options\": [ { \"content_type\": \"text/html\", \"content\": \"A\", \"is_answer\": true }, { \"content_type\": \"text/html\", \"content\": \"B\", \"is_answer\": false }, { \"content_type\": \"text/html\", \"content\": \"C\", \"is_answer\": false } ], \"code\": \"Q1\", \"difficulty_level\": \"low\", \"num_answers\": 3, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        } else if (questionDetails.equals("require metadata")) {
            contentString = "{ \"request\": { \"assessment_item\": { \"identifier\":\"tQ2\", \"objectType\": \"AssessmentItem\",\"metadata\" : {}, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        } else if(questionDetails.equals("invalid data type for select")) {
            contentString = "{\"request\":{\"assessment_item\":{\"identifier\":\"tQ2\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"title\":\"Select a char of vowels - 1.\",\"body\":{\"content_type\":\"text/html\",\"content\":\"Select a char of vowels.\"},\"type\":\"mcq\",\"description\":\"GeometryTest\",\"options\":[{\"value\":{\"type\":\"text\",\"content\":\"A\",\"asset\":\"aaa\"},\"score\":1,\"answer\":true},{\"value\":{\"type\":\"text\",\"content\":\"B\",\"asset\":\"bbb\"}},{\"value\":{\"type\":\"text\",\"content\":\"C\",\"asset\":\"ccc\"}}],\"code\":\"Q1\",\"difficulty_level\":\"low\",\"num_answers\":3,\"owner\":\"Ilimi\",\"status\":\"ilimi\",\"used_for\":\"assessment\",\"score\":3,\"qlevel\":\"EASY\",\"max_score\":5,\"max_time\":120,\"rendering_metadata\":[{\"interactivity\":[\"drag-drop\",\"zoom\"],\"keywords\":[\"compare\",\"multi-options\"],\"rendering_hints\":{\"styles\":\"css styles that will override the theme level styles for this one item\",\"view-mode\":\"landscape\"}}]},\"outRelations\":[{\"endNodeId\":\"Num:C1:SC1\",\"relationType\":\"associatedTo\"}]}}}";
        } else if(questionDetails.equals("invalid data type")) {
            contentString = "{ \"request\": { \"assessment_item\": { \"identifier\":\"tQ2\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Select a char of vowels - 1.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"type\": \"mcq\", \"description\": \"GeometryTest\", \"options\": [ { \"content_type\": \"text/html\", \"content\": \"A\", \"is_answer\": true }, { \"content_type\": \"text/html\", \"content\": \"B\", \"is_answer\": false }, { \"content_type\": \"text/html\", \"content\": \"C\", \"is_answer\": false } ], \"code\": \"Q1\", \"difficulty_level\": \"low\", \"num_answers\": 3, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": \"ilimi\", \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        } else if(questionDetails.equals("unsupported relation")) {
        	contentString = "{\"request\":{\"assessment_item\":{\"identifier\":\"tQ2\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"title\":\"Select a char of vowels - 1.\",\"body\":{\"content_type\":\"text/html\",\"content\":\"Select a char of vowels.\"},\"type\":\"mcq\",\"description\":\"GeometryTest\",\"options\":[{\"value\":{\"type\":\"text\",\"content\":\"A\",\"asset\":\"aaa\"},\"score\":1,\"answer\":true},{\"value\":{\"type\":\"text\",\"content\":\"B\",\"asset\":\"bbb\"}},{\"value\":{\"type\":\"text\",\"content\":\"C\",\"asset\":\"ccc\"}}],\"code\":\"Q1\",\"difficulty_level\":\"low\",\"num_answers\":3,\"owner\":\"Ilimi\",\"status\":\"Draft\",\"used_for\":\"assessment\",\"score\":3,\"qlevel\":\"EASY\",\"max_score\":5,\"max_time\":120,\"rendering_metadata\":[{\"interactivity\":[\"drag-drop\",\"zoom\"],\"keywords\":[\"compare\",\"multi-options\"],\"rendering_hints\":{\"styles\":\"css styles that will override the theme level styles for this one item\",\"view-mode\":\"landscape\"}}]},\"outRelations\":[{\"endNodeId\":\"Num:C1:SC1\",\"relationType\":\"ilimi\"}]}}}";
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

    @And("^return error message as (.*)$")
    public void assertErrorMessage(String message) {
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
            } else if (this.questionDetails.equals("empty object type") || this.questionDetails.equals("wrong definition node") || this.questionDetails.equals("invalid question options for ftb") || this.questionDetails.equals("wrong answer for  ftb") || this.questionDetails.equals("invalid question options for mtf")
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
