package com.ilimi.assessmentservice.load.test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import com.ilimi.common.dto.Response;

@Test
@ContextConfiguration({ "classpath:servlet-context.xml" })						
@WebAppConfiguration
public class AssessmentItemLoadTest extends AbstractTestNGSpringContextTests{
   
	private ResultActions actions;
	@Autowired
	protected WebApplicationContext context;	
	long [] sum = {0,0,0,0,0,0,0,0,0,0,0,0};
	public static final int IC = 1;
	public static final int PS = 1;
	List<String> questionIds =  Collections.synchronizedList(new ArrayList<String>());
	static int i = 0;
	AtomicInteger aiG = new AtomicInteger();
	AtomicInteger aiU = new AtomicInteger();
	AtomicInteger aiD = new AtomicInteger();
    
	public Response jsonToObject(ResultActions actions) {
    	String content = null;
		try {
			content = actions.andReturn().getResponse().getContentAsString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        ObjectMapper objectMapper = new ObjectMapper();
        Response resp = null;
        try {
			if(StringUtils.isNotBlank(content))
			    resp = objectMapper.readValue(content, Response.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
        return resp;
    }
	
	public static synchronized int generateNumber(){
		return i++;
	}

	@AfterTest
    public void calculateAVG(){
		System.out.println();
    	System.out.println("Avg time taken by create question API(MCQ) for " +IC+ " Threads :" + sum[0]/(float)(1000*IC) + " seconds");
    	System.out.println("Avg time taken by create question API(MMCQ) for " +IC+ " Threads :" + sum[4]/(float)(1000*IC) + " seconds");
    	System.out.println("Avg time taken by create question API(FTB) for " +IC+ " Threads :" + sum[5]/(float)(1000*IC) + " seconds");
    	System.out.println("Avg time taken by create question API(MTF) for " +IC+ " Threads :" + sum[6]/(float)(1000*IC) + " seconds");
    	System.out.println("Avg time taken by create question API(Speech) for " +IC+ " Threads :" + sum[7]/(float)(1000*IC) + " seconds");
    	System.out.println("Avg time taken by create question API(Canvas) for " +IC+ " Threads :" + sum[8]/(float)(1000*IC) + " seconds");
    	System.out.println("Avg time taken by update question API for " +IC+ " Threads :" + sum[1]/(float)(1000*IC) + " seconds");
    	System.out.println("Avg time taken by get question API for " +IC+ " Threads    :" + sum[2]/(float)(1000*IC) + " seconds");
    	System.out.println("Avg time taken by delete question API for " +IC+ " Threads :" + sum[3]/(float)(1000*IC) + " seconds");
    	System.out.println("QuestionIds Size : " + questionIds.size());
    }
    
//    @Test(threadPoolSize = PS, invocationCount = IC )
//    public void createQuestionMCQ() {
//    	MockMvc mockMvc;
//    	mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
//        String contentString = "{ \"request\": { \"assessment_item\": {\"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Select a char of vowels - 1.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"question_type\": \"mcq\", \"description\": \"GeometryTest\", \"options\": [ { \"content_type\": \"text/html\", \"content\": \"A\", \"is_answer\": true }, { \"content_type\": \"text/html\", \"content\": \"B\", \"is_answer\": false }, { \"content_type\": \"text/html\", \"content\": \"C\", \"is_answer\": false } ], \"code\": \"Q1\", \"difficulty_level\": \"low\", \"num_answers\": 1, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
//        String path = "/assessmentitem";
//        try {
//        	long t1 = System.currentTimeMillis();
//			actions = mockMvc.perform(MockMvcRequestBuilders.post(path).param("taxonomyId", "numeracy").contentType(MediaType.APPLICATION_JSON).content(contentString.getBytes()).header("user-id", "ilimi"));
//	        long t2 = System.currentTimeMillis();
//	        sum[0] = sum[0] + (t2 - t1);
//			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//        Response resp = jsonToObject(actions);
//        Assert.assertEquals("successful", resp.getParams().getStatus());
//		Map<String, Object> result = resp.getResult();
//		String nodeId = (String) result.get("node_id");
//		questionIds.add(nodeId);      
//    } 
//    
//    @Test(threadPoolSize = PS, invocationCount = IC )
//    public void createQuestionMMCQ() {
//    	long t1 = System.currentTimeMillis();
//    	MockMvc mockMvc;
//    	mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
//        String contentString = "{ \"request\": { \"assessment_item\": { \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Select vowels letters.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select vowels letters.\" }, \"question_type\": \"mmcq\", \"description\": \"GeometryTest\", \"options\": [ { \"content_type\": \"text/html\", \"content\": \"A\", \"is_answer\": true }, { \"content_type\": \"text/html\", \"content\": \"B\", \"is_answer\": false }, { \"content_type\": \"text/html\", \"content\": \"C\", \"is_answer\": true }, { \"content_type\": \"text/html\", \"content\": \"E\", \"is_answer\": true } ], \"code\": \"MMCQ1\", \"difficulty_level\": \"low\", \"num_answers\": 3, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
//        String path = "/assessmentitem";
//        try {
//			actions = mockMvc.perform(MockMvcRequestBuilders.post(path).param("taxonomyId", "numeracy").contentType(MediaType.APPLICATION_JSON).content(contentString.getBytes()).header("user-id", "ilimi"));
//			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//        long t2 = System.currentTimeMillis();
//        sum[4] = sum[4] + (t2 - t1);
//    } 
//    
//    @Test(threadPoolSize = PS, invocationCount = IC )
//    public void createQuestionFTB() {
//    	long t1 = System.currentTimeMillis();
//    	MockMvc mockMvc;
//    	mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
//        String contentString = "{ \"request\": { \"assessment_item\": { \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Write first letter of vowels in english.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Write first letter of vowels in english.\" }, \"question_type\": \"ftb\", \"description\": \"Literacy Test\", \"answer\": [ \"a\", \"A\" ], \"code\": \"FTB_1\", \"difficulty_level\": \"low\", \"num_answers\": 2, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
//        String path = "/assessmentitem";
//        try {
//			actions = mockMvc.perform(MockMvcRequestBuilders.post(path).param("taxonomyId", "numeracy").contentType(MediaType.APPLICATION_JSON).content(contentString.getBytes()).header("user-id", "ilimi"));
//			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//        long t2 = System.currentTimeMillis();
//        sum[5] = sum[5] + (t2 - t1);
//    } 
//    
//    @Test(threadPoolSize = PS, invocationCount = IC )
//    public void createQuestionMTF() {
//    	long t1 = System.currentTimeMillis();
//    	MockMvc mockMvc;
//    	mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
//        String contentString = "{ \"request\": { \"assessment_item\": { \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Match the capital letters with small letters\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"question_type\": \"mtf\", \"description\": \"GeometryTest\", \"code\": \"MTFQ_1\", \"lhs_options\": [ { \"content_type\": \"text/html\", \"content\": \"K\", \"index\": 4 }, { \"content_type\": \"text/html\", \"content\": \"C\", \"index\": 1 }, { \"content_type\": \"text/html\", \"content\": \"B\", \"index\": 2 }, { \"content_type\": \"text/html\", \"content\": \"D\", \"index\": 3 } ], \"rhs_options\": [ { \"content_type\": \"text/html\", \"content\": \"c\", \"index\": 1 }, { \"content_type\": \"text/html\", \"content\": \"b\", \"index\": 2 }, { \"content_type\": \"text/html\", \"content\": \"d\", \"index\": 3 }, { \"content_type\": \"text/html\", \"content\": \"k\", \"index\": 4 } ], \"difficulty_level\": \"low\", \"num_answers\": 3, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
//        String path = "/assessmentitem";
//        try {
//			actions = mockMvc.perform(MockMvcRequestBuilders.post(path).param("taxonomyId", "numeracy").contentType(MediaType.APPLICATION_JSON).content(contentString.getBytes()).header("user-id", "ilimi"));
//			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//        long t2 = System.currentTimeMillis();
//        sum[6] = sum[6] + (t2 - t1);
//    } 
//    
//    @Test(threadPoolSize = PS, invocationCount = IC )
//    public void createQuestionSpeech() {
//    	long t1 = System.currentTimeMillis();
//    	MockMvc mockMvc;
//    	mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
//        String contentString = "{ \"request\": { \"assessment_item\": { \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Spell the letter 'a'.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Spell the letter 'a'.\" }, \"question_type\": \"speech_question\", \"description\": \"Literacy Test\", \"answer\": [ \"http://platform.ekstep.in/sounds/letter_a.mp3\" ], \"code\": \"SPQ_1\", \"difficulty_level\": \"low\", \"num_answers\": 1, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
//        String path = "/assessmentitem";
//        try {
//			actions = mockMvc.perform(MockMvcRequestBuilders.post(path).param("taxonomyId", "numeracy").contentType(MediaType.APPLICATION_JSON).content(contentString.getBytes()).header("user-id", "ilimi"));
//			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//        long t2 = System.currentTimeMillis();
//        sum[7] = sum[7] + (t2 - t1);
//    } 
    
    @Test(threadPoolSize = PS, invocationCount = IC )
    public void createQuestionCanvas() {
    	MockMvc mockMvc;
    	mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        String contentString = "{ \"request\": { \"assessment_item\": { \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Write the name of the animal.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Write the name of the animal showed below <img src='images/monkey.png' />\" }, \"question_type\": \"canvas_question\", \"description\": \"Literacy Test\", \"answer\": [ \"http://platform.ekstep.in/sounds/word_monkey.jpeg\" ], \"code\": \"CQ_1\", \"difficulty_level\": \"low\", \"num_answers\": 1, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
        String path = "/assessmentitem";
        try {
        	long t1 = System.currentTimeMillis();
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path).param("taxonomyId", "numeracy").contentType(MediaType.APPLICATION_JSON).content(contentString.getBytes()).header("user-id", "ilimi"));
			long t2 = System.currentTimeMillis();
	        sum[8] = sum[8] + (t2 - t1);
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
    } 
    
    @Test(threadPoolSize = PS, invocationCount = IC, priority = 2)
    public void updateQuestion() {
    	long t1 = System.currentTimeMillis();
    	MockMvc mockMvc;
    	mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    	String contentString = "{ \"request\": { \"assessment_item\": { \"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Select a char of vowels - 1.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"question_type\": \"mcq\", \"description\": \"GeometryTest\", \"options\": [ { \"content_type\": \"text/html\", \"content\": \"A\", \"is_answer\": true }, { \"content_type\": \"text/html\", \"content\": \"B\", \"is_answer\": false }, { \"content_type\": \"text/html\", \"content\": \"C\", \"is_answer\": false } ], \"code\": \"Q1\", \"difficulty_level\": \"low\", \"num_answers\": 1, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";    	
        String path = "/assessmentitem/" + questionIds.get(aiU.getAndIncrement());
        try {
			actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).param("taxonomyId", "numeracy").contentType(MediaType.APPLICATION_JSON).content(contentString.getBytes()).header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
        long t2 = System.currentTimeMillis();
        sum[1] = sum[1] + (t2 - t1);
    } 
    
    @Test(threadPoolSize = PS, invocationCount = IC, priority = 3)
    public void getQuestion(){
    	long t1 = System.currentTimeMillis();   
    	MockMvc mockMvc;
    	mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    	String path = "/assessmentitem/" + questionIds.get(aiG.getAndIncrement());
        try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).param("taxonomyId", "numeracy").contentType(MediaType.APPLICATION_JSON).header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
        long t2 = System.currentTimeMillis();
        sum[2] = sum[2] + (t2 - t1);
	}
    
    @Test(threadPoolSize = PS, invocationCount = IC, priority = 4)
    public void deleteQuestion(){
    	long t1 = System.currentTimeMillis();
    	MockMvc mockMvc;
    	mockMvc = MockMvcBuilders.webAppContextSetup(context).build();  
    	String path = "/assessmentitem/" + questionIds.get(aiD.getAndIncrement());
        try {
			actions = mockMvc.perform(MockMvcRequestBuilders.delete(path).param("taxonomyId", "numeracy").contentType(MediaType.APPLICATION_JSON).header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
        long t2 = System.currentTimeMillis();
        sum[3] = sum[3] + (t2 - t1);
	}    


}
