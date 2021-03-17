package org.sunbird.assessmentservice.load.test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.sunbird.common.dto.Response;
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
public class AssessmentItemSetLoadTest /* extends AbstractTestNGSpringContextTests  */{
	
	@Autowired
	protected WebApplicationContext context;
	private ResultActions actions; 
	public static final int IC = 300;
	public static final int PS = 300;
	long [] sum = {0,0,0};
	List<String> questionIds =  Collections.synchronizedList(new ArrayList<String>());
	String setId ;
	
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
    
//    @AfterTest
    public void calculateAVG(){
		System.out.println();
    	System.out.println("Avg time taken by create AssessmentItem Set API for " +IC+ " Threads : " + sum[0]/(float)(IC) + " ms");
    	System.out.println("Avg time taken by get AssessmentItem Set API for " +IC+ " Threads    : " + sum[1]/(float)(IC) + " ms");
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
    
    // @Test(threadPoolSize = PS, invocationCount = IC, priority = 1)
    public void createQuestionSet() {
    	MockMvc mockMvc;
    	mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    	String questionSetContentString = "{ \"request\": { \"assessment_item_set\": { \"objectType\": \"ItemSet\", \"metadata\": { \"title\": \"Testing ItemSet - MCQQ_{{$randomInt}}.\", \"type\": \"materialised\", \"description\": \"Testing of ItemSet Using AssessmentItems\", \"code\": \"ItemSet_{{$randomInt}}\", \"difficulty_level\": \"low\", \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"memberIds\": [ \""+questionIds.get(0) +"\", \""+questionIds.get(1) +"\",\""+questionIds.get(2) +"\", \""+questionIds.get(3) +"\", \""+questionIds.get(4) +"\", \""+questionIds.get(5) +"\", \""+questionIds.get(6) +"\",\""+questionIds.get(8) +"\",\""+questionIds.get(9) +"\",\""+questionIds.get(10) +"\",\""+questionIds.get(11) +"\",\""+questionIds.get(12) +"\" ] } } } }";
    	String path = "/assessmentitemset";
    	try {
    		long t1 = System.currentTimeMillis();
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path).param("taxonomyId", "numeracy").contentType(MediaType.APPLICATION_JSON).content(questionSetContentString.getBytes()).header("user-id", "ilimi"));
	    	long t2 = System.currentTimeMillis();
	        sum[0] = sum[0] + (t2 - t1);
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
    	Response resp = jasonToObject(actions);
        Assert.assertEquals("successful", resp.getParams().getStatus());
		Map<String, Object> result = resp.getResult();
		setId = (String) result.get("set_id");
    }
    
    // @Test(threadPoolSize = PS, invocationCount = IC, priority = 1)
    public void getQuestionSet() {
    	MockMvc mockMvc;
    	mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    	String path = "/assessmentitemset/" + this.setId;
    	try {
    		long t1 = System.currentTimeMillis();
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).param("taxonomyId", "numeracy").contentType(MediaType.APPLICATION_JSON).header("user-id", "ilimi"));
	    	long t2 = System.currentTimeMillis();
	        sum[1] = sum[1] + (t2 - t1);
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
