package org.ekstep.assessmentservice.load.test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.dto.Response;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ContextConfiguration({ "classpath:servlet-context.xml" })						
@WebAppConfiguration
public class QuestionnaireLoadTest /* extends AbstractTestNGSpringContextTests */{

	private ResultActions actions;
	@Autowired
	protected WebApplicationContext context;	
	long [] sum = {0,0,0,0,0,0,0};
	public static final int IC = 80;
	public static final int PS = 80;
	List<String> questionnaireIds =  Collections.synchronizedList(new ArrayList<String>());
	List<String> questionIds =  Collections.synchronizedList(new ArrayList<String>());
	AtomicInteger aiG = new AtomicInteger(0);
	AtomicInteger aiU = new AtomicInteger(0);
	AtomicInteger aiD = new AtomicInteger(0);
	AtomicInteger aiDL = new AtomicInteger(0);
    
	public Response jasonToObject(ResultActions actions) {
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

//	@AfterTest
    public void calculateAVG(){
		System.out.println("Question Ids :" + questionIds.size() + " ::: " + "Questionnaire Ids :" + questionnaireIds.size());
    	System.out.println("Avg time taken by create Questionnaire API for " +IC+ " Threads  : " + sum[0]/IC + " ms");
    	System.out.println("Avg time taken by update Questionnaire API for " +IC+ " Threads  : " + sum[1]/IC + " ms");
    	System.out.println("Avg time taken by get Questionnaire API for " +IC+ " Threads     : " + sum[2]/IC + " ms");
    	System.out.println("Avg time taken by delete Questionnaire API for " +IC+ " Threads  : " + sum[3]/IC + " ms");
    	System.out.println("Avg time taken by deliver Questionnaire API for " +IC+ " Threads : " + sum[4]/IC + " ms");
    	System.out.println();
    }
	
	// @Test
	public void createQuestions(){
		for(int i = 0; i < 13; i ++){
			MockMvc mockMvc;
	    	mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	        String contentString = "{ \"request\": { \"assessment_item\": {\"objectType\": \"AssessmentItem\", \"metadata\": { \"title\": \"Select a char of vowels - 1.\", \"body\": { \"content_type\": \"text/html\", \"content\": \"Select a char of vowels.\" }, \"type\": \"mcq\", \"description\": \"GeometryTest\", \"options\": [ { \"content_type\": \"text/html\", \"content\": \"A\", \"is_answer\": true }, { \"content_type\": \"text/html\", \"content\": \"B\", \"is_answer\": false }, { \"content_type\": \"text/html\", \"content\": \"C\", \"is_answer\": false } ], \"code\": \"Q1\", \"difficulty_level\": \"low\", \"num_answers\": 1, \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"score\": 3, \"max_time\": 120, \"rendering_metadata\": [ { \"interactivity\": [ \"drag-drop\", \"zoom\" ], \"keywords\": [ \"compare\", \"multi-options\" ], \"rendering_hints\": { \"styles\": \"css styles that will override the theme level styles for this one item\", \"view-mode\": \"landscape\" } } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
	        String path = "/assessmentitem";
	        try {
				actions = mockMvc.perform(MockMvcRequestBuilders.post(path).param("taxonomyId", "numeracy").contentType(MediaType.APPLICATION_JSON).content(contentString.getBytes()).header("user-id", "ilimi"));
				Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
			} catch (Exception e) {
				e.printStackTrace();
			}
	        Response resp = jasonToObject(actions);
	        Assert.assertEquals("successful", resp.getParams().getStatus());
			Map<String, Object> result = resp.getResult();
			String nodeId = (String) result.get("node_id");
			questionIds.add(nodeId);
		}
	}
	
	// @Test( dependsOnMethods = "createQuestions")
    public void assistQuestionnaire() {
    	MockMvc mockMvc;
    	mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    	String contentString = "{ \"request\": { \"questionnaire\": { \"objectType\": \"Questionnaire\", \"metadata\": { \"code\": \"QR1\", \"language\": \"English\", \"title\": \"Demo Questionnaire for Ekstep Platform\", \"description\": \"Description of Demo Questionnaire - Ekstep Platform\", \"instructions\": \"Instructions of Demo Questionnaire - Ekstep Platform\", \"used_for\": \"assessment\", \"type\": \"materialised\", \"duration\": 20, \"total_items\": 6, \"strict_sequencing\": false, \"allow_skip\": true, \"max_score\": 20, \"status\": \"Draft\", \"owner\": \"Ilimi\", \"copyright\": \"Ilimi\", \"license\": \"Ilimi\", \"items\": [ \""+questionIds.get(0) +"\", \""+questionIds.get(1) +"\",\""+questionIds.get(2) +"\", \""+questionIds.get(3) +"\", \""+questionIds.get(4) +"\", \""+questionIds.get(5) +"\", \""+questionIds.get(6) +"\",\""+questionIds.get(8) +"\",\""+questionIds.get(9) +"\",\""+questionIds.get(10) +"\",\""+questionIds.get(11) +"\",\""+questionIds.get(12) +"\" ] }, \"outRelations\": [ { \"relationType\": \"associatedTo\", \"endNodeId\": \"Num:C1:SC1\" } ] } } }";
        String path = "/questionnaire";
        try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path).param("taxonomyId", "numeracy").contentType(MediaType.APPLICATION_JSON).content(contentString.getBytes()).header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}

    } 
    
    // @Test(threadPoolSize = PS, invocationCount = IC , dependsOnMethods = "assistQuestionnaire")
    public void createQuestionnaire() {
    	MockMvc mockMvc;
    	mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    	String contentString = "{ \"request\": { \"questionnaire\": { \"objectType\": \"Questionnaire\", \"metadata\": { \"code\": \"QR1\", \"language\": \"English\", \"title\": \"Demo Questionnaire for Ekstep Platform\", \"description\": \"Description of Demo Questionnaire - Ekstep Platform\", \"instructions\": \"Instructions of Demo Questionnaire - Ekstep Platform\", \"used_for\": \"assessment\", \"type\": \"materialised\", \"duration\": 20, \"total_items\": 6, \"strict_sequencing\": false, \"allow_skip\": true, \"max_score\": 20, \"status\": \"Draft\", \"owner\": \"Ilimi\", \"copyright\": \"Ilimi\", \"license\": \"Ilimi\", \"items\": [ \""+questionIds.get(0) +"\", \""+questionIds.get(1) +"\",\""+questionIds.get(2) +"\", \""+questionIds.get(3) +"\", \""+questionIds.get(4) +"\", \""+questionIds.get(5) +"\", \""+questionIds.get(6) +"\",\""+questionIds.get(8) +"\",\""+questionIds.get(9) +"\",\""+questionIds.get(10) +"\",\""+questionIds.get(11) +"\",\""+questionIds.get(12) +"\" ] }, \"outRelations\": [ { \"relationType\": \"associatedTo\", \"endNodeId\": \"Num:C1:SC1\" } ] } } }";
        String path = "/questionnaire";
        try {
        	long t1 = System.currentTimeMillis();
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path).param("taxonomyId", "numeracy").contentType(MediaType.APPLICATION_JSON).content(contentString.getBytes()).header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
			long t2 = System.currentTimeMillis();
	        sum[0] = sum[0] + (t2 - t1);
		} catch (Exception e) {
			e.printStackTrace();
		}
        Response resp = jasonToObject(actions);
        Assert.assertEquals("successful", resp.getParams().getStatus());
		Map<String, Object> result = resp.getResult();
		String nodeId = (String) result.get("node_id");
		questionnaireIds.add(nodeId);
    } 
    
    // @Test(threadPoolSize = PS, invocationCount = IC, dependsOnMethods = "createQuestionnaire")
    public void updateQuestionnaire() {
    	MockMvc mockMvc;
    	mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    	String contentString = "{ \"request\": { \"questionnaire\": { \"objectType\": \"Questionnaire\", \"metadata\": { \"code\": \"QR1\", \"language\": \"English\", \"title\": \"Demo Questionnaire for Ekstep Platform\", \"description\": \"Description of Demo Questionnaire - Ekstep Platform\", \"instructions\": \"Instructions of Demo Questionnaire - Ekstep Platform\", \"used_for\": \"assessment\", \"type\": \"materialised\", \"duration\": 20, \"total_items\": 5, \"strict_sequencing\": false, \"allow_skip\": true, \"max_score\": 20, \"status\": \"Draft\", \"owner\": \"Ilimi\", \"copyright\": \"Ilimi\", \"license\": \"Ilimi\", \"items\": [ \""+questionIds.get(0) +"\", \""+questionIds.get(1) +"\",\""+questionIds.get(2) +"\", \""+questionIds.get(3) +"\", \""+questionIds.get(4) +"\", \""+questionIds.get(5) +"\", \""+questionIds.get(6) +"\",\""+questionIds.get(8) +"\",\""+questionIds.get(9) +"\",\""+questionIds.get(10) +"\",\""+questionIds.get(11) +"\",\""+questionIds.get(12) +"\" ] }, \"outRelations\": [ { \"relationType\": \"associatedTo\", \"endNodeId\": \"Num:C1:SC1\" } ] } } }";    	
        String path = "/questionnaire/" + questionnaireIds.get(aiU.getAndIncrement());
        try {
        	long t1 = System.currentTimeMillis();
			actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).param("taxonomyId", "numeracy").contentType(MediaType.APPLICATION_JSON).content(contentString.getBytes()).header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
			long t2 = System.currentTimeMillis();
	        sum[1] = sum[1] + (t2 - t1);
		} catch (Exception e) {
			e.printStackTrace();
		}
        
    } 
    
    // @Test(threadPoolSize = PS, invocationCount = IC, dependsOnMethods = "updateQuestionnaire")
    public void getQuestionnaire(){
    	MockMvc mockMvc;
    	mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    	String path = "/questionnaire/" + questionnaireIds.get(aiG.getAndIncrement());
        try {
        	long t1 = System.currentTimeMillis();
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).param("taxonomyId", "numeracy").contentType(MediaType.APPLICATION_JSON).header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
			long t2 = System.currentTimeMillis();
	        sum[2] = sum[2] + (t2 - t1);
		} catch (Exception e) {
			e.printStackTrace();
		}
        
	}
    
    // @Test(threadPoolSize = PS, invocationCount = IC, dependsOnMethods = "getQuestionnaire")
    public void deliverQuestionnaire(){
    	MockMvc mockMvc;
    	mockMvc = MockMvcBuilders.webAppContextSetup(context).build();  
    	String path = "/questionnaire/deliver/" + questionnaireIds.get(aiDL.getAndIncrement());
        try {
        	long t1 = System.currentTimeMillis();        	
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).param("taxonomyId", "numeracy").contentType(MediaType.APPLICATION_JSON).header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	        long t2 = System.currentTimeMillis();
	        sum[4] = sum[4] + (t2 - t1);
		} catch (Exception e) {
			e.printStackTrace();
		};
	}
    
    // @Test(threadPoolSize = PS, invocationCount = IC, dependsOnMethods = "deliverQuestionnaire")
    public void deleteQuestionnaire(){
    	MockMvc mockMvc;
    	mockMvc = MockMvcBuilders.webAppContextSetup(context).build();  
    	String path = "/questionnaire/" + questionnaireIds.get(aiD.getAndIncrement());
        try {
        	long t1 = System.currentTimeMillis();
			actions = mockMvc.perform(MockMvcRequestBuilders.delete(path).param("taxonomyId", "numeracy").contentType(MediaType.APPLICATION_JSON).header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	        long t2 = System.currentTimeMillis();
	        sum[3] = sum[3] + (t2 - t1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}    
    
    

}
