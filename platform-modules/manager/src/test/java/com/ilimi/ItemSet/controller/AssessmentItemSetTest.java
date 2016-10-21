package com.ilimi.ItemSet.controller;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.ilimi.common.dto.Response;
import com.ilimi.taxonomy.content.common.BaseTest;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })

/**
 * The AssessmentItemSetTest class contains junit tests to validate creation of
 * Itemsets from given assessmentitems, fetching the AssessmentItemsets,
 * updating given asessmentItemsets, search for assessmentItemsets based on
 * given criteria, delete the given assessmentitemsets Positive and negative
 * test senarios have been specified for each of the operation
 */
public class AssessmentItemSetTest extends BaseTest {

	@Autowired
	private WebApplicationContext context;
	private MockMvc mockMvc;
	private ResultActions actions;
	List<String> nodes = new ArrayList<String>();
	String set_id = null;
	String set_id1 = null;
	static List<Integer> items = new ArrayList<Integer>();
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}

	public void BeforeClass() {
		nodes = createAssessmentItem(6);
		String str = "\"" + StringUtils.join(nodes.toArray(new String[0]), "\",\"") + "\"";
		String request = "{ \"request\": { \"assessment_item_set\": { \"objectType\": \"ItemSet\", \"metadata\": { \"title\": \"Akshara Worksheet Grade 5 Item Set\", \"type\": \"materialised\", \"max_score\": 15, \"total_items\": 3, \"description\": \"Akshara Worksheet Grade 5 Item Set\", \"code\": \"akshara.grade5.ws1.test\", \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"memberIds\": ["
				+ str
				+ "  ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
		try {
			String path = "/v1/assessmentitemset";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		System.out.println("result:" + resp.getResult().get("set_id"));
		set_id = (String) resp.getResult().get("set_id");
		Assert.assertEquals("successful", resp.getParams().getStatus());
	}

	// Create assessmentItemset without "maxscore" in request body
	// expect 400 response
	@Test
	public void createItemSet_01() throws Exception {
		String str = "\"" + StringUtils.join(nodes.toArray(new String[0]), "\",\"") + "\"";
		String request = "{ \"request\": { \"assessment_item_set\": { \"objectType\": \"ItemSet\", \"metadata\": { \"title\": \"Akshara Worksheet Grade 5 Item Set\", \"type\": \"materialised\", \"total_items\": 3, \"description\": \"Akshara Worksheet Grade 5 Item Set\", \"code\": \"akshara.grade5.ws1.test\", \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"memberIds\": [ "
				+ str
				+ " ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
		try {
			String path = "/v1/assessmentitemset";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("failed", resp.getParams().getStatus());
	}

	// Create assessmentItemset without "code" in request body
	// expect 400 response
	@Test
	public void createItemSet_02() throws Exception {
		String str = "\"" + StringUtils.join(nodes.toArray(new String[0]), "\",\"") + "\"";
		String request = "{ \"request\": { \"assessment_item_set\": { \"objectType\": \"ItemSet\", \"metadata\": { \"title\": \"Akshara Worksheet Grade 5 Item Set\", \"type\": \"materialised\", \"max_score\": 15, \"total_items\": 3, \"description\": \"Akshara Worksheet Grade 5 Item Set\",\"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"memberIds\": [ "
				+ str
				+ " ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
		try {
			String path = "/v1/assessmentitemset";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("failed", resp.getParams().getStatus());
	}

	// Create assessmentItemset without "objectType" in request body
	// expect 400 response
	@Test
	public void createItemSet_03() throws Exception {
		String str = "\"" + StringUtils.join(nodes.toArray(new String[0]), "\",\"") + "\"";
		String request = "{ \"request\": { \"assessment_item_set\": {  \"metadata\": { \"title\": \"Akshara Worksheet Grade 5 Item Set\", \"type\": \"materialised\", \"max_score\": 15, \"total_items\": 3, \"description\": \"Akshara Worksheet Grade 5 Item Set\", \"code\": \"akshara.grade5.ws1.test\", \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"memberIds\": [ "
				+ str
				+ " ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
		try {
			String path = "/v1/assessmentitemset";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("failed", resp.getParams().getStatus());
	}

	// Create assessmentItemset without "metadata" in request body
	// expect 400 response
	@Test
	public void createItemSet_04() throws Exception {
		String request = "{ \"request\": { \"assessment_item_set\": { } } }";
		try {
			String path = "/v1/assessmentitemset";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("failed", resp.getParams().getStatus());
	}

	// Create assessmentItemset without "members and relations" in request body
	// expect 400 response
	@Test
	public void createItemSet_05() {
		String request = "{ \"request\": { \"assessment_item_set\": {\"objectType\": \"ItemSet\", \"metadata\": { \"title\": \"AksharaWorksheet Grade 5 Item Set\", \"type\": \"materialised\", \"max_score\":17, \"total_items\": 17, \"description\": \"Akshara Worksheet Grade 5Item Set\", \"code\": \"akshara.grade5.ws1.test\", \"owner\": \"Ilimi\",\"used_for\": \"assessment\" } } } }";
		String path = "/v1/assessmentitemset";
		try {
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).content(request));
			Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("failed", resp.getParams().getStatus());
	}

	// Create assessmentItemset with invalid url
	// expect 404 response
	@Test
	public void createItemSet_06() {
		String request = "{ \"request\": { \"assessment_item_set\": {\"objectType\": \"ItemSet\", \"metadata\": { \"title\": \"Akshara Worksheet Grade 5 Item Set\", \"type\": \"materialised\", \"max_score\":17, \"total_items\": 17, \"description\": \"Akshara Worksheet Grade 5Item Set\", \"code\": \"akshara.grade5.ws1.test\", \"owner\": \"Ilimi\", \"used_for\": \"assessment\" } } } }";
		String path = "/v1/assssmentitemset";
		try {
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).content(request));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// Create assessmentItemset 
	// expect 200 response
	@Test
	public void createItemset_07() {
		nodes = createAssessmentItemSet();
		String str = "\"" + StringUtils.join(nodes.toArray(new String[0]), "\",\"") + "\"";
		String request = "{ \"request\": { \"assessment_item_set\": { \"objectType\": \"ItemSet\", \"metadata\": { \"title\": \"Akshara Worksheet Grade 5 Item Set\", \"type\": \"materialised\", \"max_score\": 15, \"total_items\": 3, \"description\": \"Akshara Worksheet Grade 5 Item Set\", \"code\": \"akshara.grade5.ws1.test\", \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"memberIds\": ["
				+ str
				+ "  ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
		try {
			String path = "/v1/assessmentitemset";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		System.out.println("result:" + resp.getResult().get("set_id"));
		set_id1 = (String) resp.getResult().get("set_id");
		Assert.assertEquals("successful", resp.getParams().getStatus());
	}

	// Gets AssessmentItemSet with valid SetId
	// expect 200 response
	@Test
	public void getAssessmentItem_01() {
		BeforeClass();
		String path = "/v1/assessmentitemset/do_10000108";
		try {
			actions = this.mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
	}

	// Gets assessmentItemSet with invalid SetId
	// expects 404 response
	@Test
	public void getAssessmentItem_02() {
		String inValidItemSetId = "LP_UT_test_02";
		String path = "/v1/assessmentitemset/" + inValidItemSetId;
		try {
			actions = this.mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Gets assessmentItemSet with invalid url
	// expects 404 response
	@Test
	public void getAssessmentItem_03() {
		String validItemSetId = "do_10000108";
		String path = "/v2/assessmentitemset/" + validItemSetId;
		try {
			actions = this.mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Update assessmentItemset with valid ItemSetId and valid request
	// expect 200 response
//	@Test
	public void updateItemSet_01() {
		nodes = createAssessmentItemSet();
		String str = "\"" + StringUtils.join(nodes.toArray(new String[0]), "\",\"") + "\"";
		String request = "{ \"request\": { \"assessment_item_set\": { \"objectType\": \"ItemSet\", \"metadata\": { \"title\": \"Akshara Worksheet Grade 5 Item Set\", \"type\": \"materialised\", \"total_items\": 3, \"description\": \"Akshara Worksheet Grade 5 Item Set\", \"code\": \"akshara.grade5.ws1.test\", \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"memberIds\": [ "
				+ str
				+ " ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
		String path = "/v1/assessmentitemset/do_10000108";
		try {
			actions = this.mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).content(request));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
	}

	// Update assessmentItemset with valid ItemSetId & request without members
	// expect 400 response
	@Test
	public void updateItemSet_02() {
		String validItemsetId = "do_10000107";
		String request = "{ \"request\": { \"assessment_item_set\": { \"objectType\": \"ItemSet\", \"metadata\": { \"title\": \"Testing ItemSet- MCQQ_650.\", \"type\": \"materialised\", \"description\": \"Testing ofItemSet Using AssessmentItems\", \"code\": \"ItemSet_650\",\"difficulty_level\": \"high\", \"owner\": \"Ilimi\", \"used_for\":\"assessment\", \"max_score\": 3 } } } }";
		String path = "/v1/assessmentitemset/" + validItemsetId;
		try {
			actions = this.mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).content(request));
			Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("failed", resp.getParams().getStatus());
	}

	// Update assessmentItemset with invalid ItemSetId
	// expect 404 response
	@Test
	public void updateItemSet_03() {
		String inValidItemsetId = "LP_UT_test_14";
		String request = "{ \"request\": { \"assessment_item_set\": {\"objectType\": \"ItemSet\", \"metadata\": { \"title\": \"Testing ItemSet- MCQQ_650.\", \"type\": \"materialised\", \"description\": \"Testing ofItemSet Using AssessmentItems\", \"code\": \"ItemSet_650\",\"difficulty_level\": \"high\", \"owner\": \"Ilimi\", \"used_for\":\"assessment\", \"max_score\": 3, \"memberIds\": [ \"test.q901\",\"test.q902\", \"test.q903\" ] } } } }";
		String path = "/v1/assessmentitemset/" + inValidItemsetId;
		try {
			actions = this.mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).content(request));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("failed", resp.getParams().getStatus());
	}

	// Update assessmentItemset with invalid url
	// expect 404 response
	@Test
	public void updateItemSet_04() {
		String request = "{ \"request\": { \"assessment_item_set\": {\"objectType\": \"ItemSet\", \"metadata\": { \"title\": \"Testing ItemSet- MCQQ_650.\", \"type\": \"materialised\", \"description\": \"Testing ofItemSet Using AssessmentItems\", \"code\": \"ItemSet_650\", \"difficulty_level\": \"high\", \"owner\": \"Ilimi\", \"used_for\":\"assessment\", \"max_score\": 3, \"memberIds\": [ \"test.q901\",\"test.q902\", \"test.q903\" ] } } } }";
		String path = "/v1/assessmntitemst/";
		try {
			actions = this.mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).content(request));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Search assessmentItemset with required search criteria
	// expect 200 response
	// @Test
	public void searchItemSet_01() {
		BeforeClass();
		String request = "{ \"request\": { \"metadata\": { \"filters\": [ {\"property\" : \"total_items\", \"operator\": \"=\", \"value\": 3} ] } } }";
		String path = "/v1/assessmentitemset/search";
		try {
			actions = this.mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).content(request));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
	}

	// Search assessmentItemset without filters
	// expect 404 response
	@Test
	public void searchItemSet_02() {
		String request = "{ \"request\": { \"metadata\": } } }";
		String path = "/v1/assessmentitemset/search";
		try {
			actions = this.mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).content(request));
			Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Search assessmentItemset with invalid url
	// expect 404 response
	@Test
	public void searchItemSet_03() {
		String request = "{ \"request\": { \"metadata\": { \"filters\": [ {\"property\" : \"total_items\", \"operator\": \">=\", \"value\": 17} ], \"op\": \"AND\" } } }";
		String path = "/v1/assessmentitset/sarch";
		try {
			actions = this.mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).content(request));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Delete an assessmentItemset with valid itemset id
	// expect 200 ok response
	@Test
	public void deleteAssessmentItem_01() {
		String set_id = "do_10000108";
		String path = "/v1/assessmentitemset/" + set_id;
		try {
			actions = this.mockMvc.perform(MockMvcRequestBuilders.delete(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
	}

	// Delete an non-existiing assessmentItemset
	// expect 404 response(check) but returning 200 response
	@Test
	public void deleteAssessmentItem_02() {
		String path = "/v1/assessmentitemset/q_1_s_urdu_01";
		try {
			actions = this.mockMvc.perform(MockMvcRequestBuilders.delete(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
	}

	// Delete assessmentItemSet with invalid url
	// expect 404 response
	@Test
	public void deleteAssessmentItem_03() {
		String path = "/v1/essmentitemset//q_1_s_urdu";
		try {
			actions = this.mockMvc.perform(MockMvcRequestBuilders.delete(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// method to create assessmentItems
	public List<String> createAssessmentItem(int range) {
		for (int i = 0; i < range; i++) {
			String request = "{ \"request\": { \"assessment_item\": { \"identifier\": \"LP_UT_test_" + i
					+ "\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"code\": \"test.mtf_mixed_1\", \"name\": \"MTF Question 1\", \"type\": \"mtf\", \"template\": \"mtf_template_3\", \"qlevel\": \"MEDIUM\", \"title\": \"ಕೊಟ್ಟಿರುವ ಸಂಖ್ಯೆಗಳನ್ನು ಇಳಿಕೆ ಕ್ರಮದಲ್ಲಿ ಜೋಡಿಸಿರಿ.\", \"question\":\"2080\", \"model\":{ \"data0\":\"23450\", \"data1\":\"23540\" }, \"lhs_options\": [ { \"value\": {\"type\": \"image\", \"asset\": \"grey\"}, \"index\": 0 } ], \"rhs_options\": [ { \"value\": {\"type\": \"text\", \"asset\": \">\"} }, { \"value\": {\"type\": \"text\", \"asset\": \"=\"} }, { \"value\": {\"type\": \"mixed\", \"text\": \"<\", \"image\": \"image1\", \"audio\": \"audio1\"}, \"answer\": 0 } ], \"max_score\": 6, \"partial_scoring\": true, \"feedback\": \"\" } } } }";
			try {
				String path = "/v1/assessmentitem";
				actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
						.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8)
						.content(request));
				Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
			} catch (Exception e) {
				e.printStackTrace();
			}
			Response resp = jsonToObject(actions);
			String node_id = (String) resp.getResult().get("node_id");
			nodes.add(node_id);
			Assert.assertEquals("successful", resp.getParams().getStatus());
		}
		return nodes;
	}

	// method to create assessmentItems
	public List<String> createAssessmentItemSet() {
		List<Integer> rn = generateRandomNumber(10, 99);
		for(int num : rn){
		String request = "{ \"request\": { \"assessment_item\": { \"identifier\": \"LP_UT_test_" + num
				+ "\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"code\": \"test.mtf_mixed_1\", \"name\": \"MTF Question 1\", \"type\": \"mtf\", \"template\": \"mtf_template_3\", \"qlevel\": \"MEDIUM\", \"title\": \"ಕೊಟ್ಟಿರುವ ಸಂಖ್ಯೆಗಳನ್ನು ಇಳಿಕೆ ಕ್ರಮದಲ್ಲಿ ಜೋಡಿಸಿರಿ.\", \"question\":\"2080\", \"model\":{ \"data0\":\"23450\", \"data1\":\"23540\" }, \"lhs_options\": [ { \"value\": {\"type\": \"image\", \"asset\": \"grey\"}, \"index\": 0 } ], \"rhs_options\": [ { \"value\": {\"type\": \"text\", \"asset\": \">\"} }, { \"value\": {\"type\": \"text\", \"asset\": \"=\"} }, { \"value\": {\"type\": \"mixed\", \"text\": \"<\", \"image\": \"image1\", \"audio\": \"audio1\"}, \"answer\": 0 } ], \"max_score\": 6, \"partial_scoring\": true, \"feedback\": \"\" } } } }";
		try {
			String path = "/v1/assessmentitem";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		String node_id = (String) resp.getResult().get("node_id");
		nodes.add(node_id);
		Assert.assertEquals("successful", resp.getParams().getStatus());
		}
		return nodes;
	}

	// method to generate random numbers for a given range of input
	public static List<Integer> generateRandomNumber(int min, int max) {
		for(int i =10;i<20; i++){
		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}
		Random r = new Random();
		  items.add(r.nextInt((max - min) + 1) + min + i);
		}
		return items;
	}

	// method to convert resultActions to required response
	public static Response jsonToObject(ResultActions actions) {
		String content = null;
		Response resp = null;
		try {
			content = actions.andReturn().getResponse().getContentAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			if (StringUtils.isNotBlank(content))
				resp = objectMapper.readValue(content, Response.class);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}
}