package org.ekstep.assessmentItemset.controller;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.dto.Response;
import org.ekstep.graph.engine.common.GraphEngineTestSetup;
import org.ekstep.test.common.CommonTestSetup;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
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
@Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AssessmentItemSetTest extends CommonTestSetup {

	@Autowired
	private WebApplicationContext context;
	MockMvc mockMvc;
	private ResultActions actions;

	List<String> nodes = new ArrayList<String>();
	String set_id = null;
	String set_id1 = null;
	List<Integer> items = new ArrayList<Integer>();

	@BeforeClass
	public static void beforeClass() throws Exception {
		loadDefinition("definitions/concept_definition.json", "definitions/content_definition.json",
				"definitions/dimension_definition.json", "definitions/item_definition.json",
				"definitions/itemset_definition.json");
	}

	// Create assessmentItemset
	// expect 200 response
	@Ignore
	@Test
	public void createItemsetWithValidRequest() throws InterruptedException {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		nodes = createAssessmentItem(2);
		Thread.sleep(2000);
		String str = "\"" + StringUtils.join(nodes.toArray(new String[0]), "\",\"") + "\"";
		String request = "{ \"request\": { \"assessment_item_set\": { \"objectType\": \"ItemSet\", \"metadata\": { \"title\": \"Akshara Worksheet Grade 5 Item Set\", \"type\": \"materialised\", \"max_score\": 2, \"total_items\": 2, \"description\": \"Akshara Worksheet Grade 5 Item Set\", \"code\": \"akshara.grade5.ws1.test\", \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"memberIds\": ["
				+ str + "  ] } } } }";
		try {
			String path = "/v1/assessmentitemset";
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
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

	// Create assessmentItemset without "maxscore" in request body
	// expect 400 response
	@Test
	public void createItemSetWithoutMaxscore() throws Exception {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String str = "\"" + StringUtils.join(nodes.toArray(new String[0]), "\",\"") + "\"";
		String request = "{ \"request\": { \"assessment_item_set\": { \"objectType\": \"ItemSet\", \"metadata\": { \"title\": \"Akshara Worksheet Grade 5 Item Set\", \"type\": \"materialised\", \"total_items\": 3, \"description\": \"Akshara Worksheet Grade 5 Item Set\", \"code\": \"akshara.grade5.ws1.test\", \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"memberIds\": [ "
				+ str
				+ " ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
		try {
			String path = "/v1/assessmentitemset";
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
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
	public void createItemSetWithoutCode() throws Exception {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String str = "\"" + StringUtils.join(nodes.toArray(new String[0]), "\",\"") + "\"";
		String request = "{ \"request\": { \"assessment_item_set\": { \"objectType\": \"ItemSet\", \"metadata\": { \"title\": \"Akshara Worksheet Grade 5 Item Set\", \"type\": \"materialised\", \"max_score\": 15, \"total_items\": 3, \"description\": \"Akshara Worksheet Grade 5 Item Set\",\"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"memberIds\": [ "
				+ str
				+ " ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
		try {
			String path = "/v1/assessmentitemset";
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
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
	public void createItemSetWithoutObjectype() throws Exception {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String str = "\"" + StringUtils.join(nodes.toArray(new String[0]), "\",\"") + "\"";
		String request = "{ \"request\": { \"assessment_item_set\": {  \"metadata\": { \"title\": \"Akshara Worksheet Grade 5 Item Set\", \"type\": \"materialised\", \"max_score\": 15, \"total_items\": 3, \"description\": \"Akshara Worksheet Grade 5 Item Set\", \"code\": \"akshara.grade5.ws1.test\", \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"memberIds\": [ "
				+ str
				+ " ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
		try {
			String path = "/v1/assessmentitemset";
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
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
	public void createItemSetWithoutMetadata() throws Exception {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String request = "{ \"request\": { \"assessment_item_set\": { } } }";
		try {
			String path = "/v1/assessmentitemset";
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
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
	public void createItemSetWithoutMembersAndRelations() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String request = "{ \"request\": { \"assessment_item_set\": {\"objectType\": \"ItemSet\", \"metadata\": { \"title\": \"AksharaWorksheet Grade 5 Item Set\", \"type\": \"materialised\", \"max_score\":17, \"total_items\": 17, \"description\": \"Akshara Worksheet Grade 5Item Set\", \"code\": \"akshara.grade5.ws1.test\", \"owner\": \"Ilimi\",\"used_for\": \"assessment\" } } } }";
		String path = "/v1/assessmentitemset";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
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
	public void createItemSetWithInvalidUrl() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String request = "{ \"request\": { \"assessment_item_set\": {\"objectType\": \"ItemSet\", \"metadata\": { \"title\": \"Akshara Worksheet Grade 5 Item Set\", \"type\": \"materialised\", \"max_score\":17, \"total_items\": 17, \"description\": \"Akshara Worksheet Grade 5Item Set\", \"code\": \"akshara.grade5.ws1.test\", \"owner\": \"Ilimi\", \"used_for\": \"assessment\" } } } }";
		String path = "/v1/assssmentitemset";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).content(request));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Gets AssessmentItemSet with valid SetId
	// expect 200 response
	// @Test
	public void getValidItemset() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/assessmentitemset/do_100001121";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi")
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
	public void getNonExistingItemset() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String inValidItemSetId = "LP_UT_test_02";
		String path = "/v1/assessmentitemset/" + inValidItemSetId;
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Gets assessmentItemSet with invalid url
	// expects 404 response
	@Test
	public void getItemsetWithInvalidUrl() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String validItemSetId = "do_10000112";
		String path = "/v2/assessmentitemset/" + validItemSetId;
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Update assessmentItemset with valid ItemSetId and valid request
	// expect 200 response
	// @Test
	public void updateItemSetWithValidRequest() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String request = "{ \"request\": { \"assessment_item_set\": { \"objectType\": \"ItemSet\", \"metadata\": { \"title\": \"Testing ItemSet - MCQQ_650.\", \"type\": \"materialised\", \"description\": \"Testing of ItemSet Using AssessmentItems\", \"code\": \"ItemSet_650\", \"difficulty_level\": \"high\", \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"max_score\": 3, \"memberIds\": [ \"test.q901\", \"test.q902\", \"test.q903\" ] } } } }";
		String path = "/v1/assessmentitemset/do_100001121";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi")
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
	public void updateItemsetWithoutMembers() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String validItemsetId = "do_10000107";
		String request = "{ \"request\": { \"assessment_item_set\": { \"objectType\": \"ItemSet\", \"metadata\": { \"title\": \"Testing ItemSet- MCQQ_650.\", \"type\": \"materialised\", \"description\": \"Testing ofItemSet Using AssessmentItems\", \"code\": \"ItemSet_650\",\"difficulty_level\": \"high\", \"owner\": \"Ilimi\", \"used_for\":\"assessment\", \"max_score\": 3 } } } }";
		String path = "/v1/assessmentitemset/" + validItemsetId;
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi")
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
	@Ignore
	@Test
	public void updateNonexistingItemSet() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String inValidItemsetId = "LP_UT_test_14";
		String request = "{ \"request\": { \"assessment_item_set\": {\"objectType\": \"ItemSet\", \"metadata\": { \"title\": \"Testing ItemSet- MCQQ_650.\", \"type\": \"materialised\", \"description\": \"Testing ofItemSet Using AssessmentItems\", \"code\": \"ItemSet_650\",\"difficulty_level\": \"high\", \"owner\": \"Ilimi\", \"used_for\":\"assessment\", \"max_score\": 3, \"memberIds\": [ \"test.q901\",\"test.q902\", \"test.q903\" ] } } } }";
		String path = "/v1/assessmentitemset/" + inValidItemsetId;
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi")
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
	public void updateItemSetWithInvalidUrl() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String request = "{ \"request\": { \"assessment_item_set\": {\"objectType\": \"ItemSet\", \"metadata\": { \"title\": \"Testing ItemSet- MCQQ_650.\", \"type\": \"materialised\", \"description\": \"Testing ofItemSet Using AssessmentItems\", \"code\": \"ItemSet_650\", \"difficulty_level\": \"high\", \"owner\": \"Ilimi\", \"used_for\":\"assessment\", \"max_score\": 3, \"memberIds\": [ \"test.q901\",\"test.q902\", \"test.q903\" ] } } } }";
		String path = "/v1/assessmntitemst/";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).content(request));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Search assessmentItemset with required search criteria
	// expect 200 response
	// @Test
	public void searchItemSetWithValidRequest() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String request = "{ \"request\": { \"metadata\": { \"filters\": [ {\"property\" : \"used_for\", \"value\": \"assessment\"} ] } } }";
		String path = "/v1/assessmentitemset/search";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi")
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
	public void searchItemSetWithoutFilters() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String request = "{ \"request\": { \"metadata\": } } }";
		String path = "/v1/assessmentitemset/search";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).content(request));
			Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Search assessmentItemset with invalid url
	// expect 404 response
	@Test
	public void searchItemSetWithInvalidUrl() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String request = "{ \"request\": { \"metadata\": { \"filters\": [ {\"property\" : \"total_items\", \"operator\": \">=\", \"value\": 17} ], \"op\": \"AND\" } } }";
		String path = "/v1/assessmentitset/sarch";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).content(request));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Delete an assessmentItemset with valid itemset id
	// expect 200 ok response
	@Test
	public void deleteValidItemset() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String set_id = "do_10000108";
		String path = "/v1/assessmentitemset/" + set_id;
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.delete(path).header("user-id", "ilimi")
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
	public void deleteNonexistingItemset() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/assessmentitemset/q_1_s_urdu_01";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.delete(path).header("user-id", "ilimi")
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
	public void deleteItemsetWithInvalidUrl() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/essmentitemset//q_1_s_urdu";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.delete(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// method to create assessmentItems
	public List<String> createAssessmentItem(int range) {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		for (int i = 0; i < range; i++) {
			String request = "{\"request\": {\"assessment_item\": {\"identifier\": \"LP_UT_test_01\",\"objectType\": \"AssessmentItem\",\"metadata\": {\"code\": \"test.qtt02\",\"itemType\":\"UNIT\",\"category\":\"MCQ\",\"version\":2,\"name\": \"MCQ Question 2\",\"type\": \"mcq\",\"num_answers\": 1,\"template\": \"mcq_template_2\",\"template_id\":\"mcq_template_2\",\"qlevel\": \"MEDIUM\",\"owner\": \"username_1\",\"title\": \"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ.  ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\",\"question\": \"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ.ಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\",\"model\": {\"img\": {\"type\": \"image\",\"asset\": \"perimeter\"},\"img2\": {\"type\": \"image\",\"asset\": \"smallSquare\"},\"subtext\": \"( = 1  ಚದರ ಸೆಂ.ಮೀ)\"},\"options\": [{\"value\": {\"type\": \"text\",\"asset\": \"12&10\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"14&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"16&8\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"12&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"},\"score\": 1}],\"max_score\": 1,\"partial_scoring\": false,\"feedback\": \"\"}}}}";
			try {
				String path = "/v1/assessmentitem";
				actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
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
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		List<Integer> rn = generateRandomNumber(10, 99);
		for (int num : rn) {
			String request = "{ \"request\": { \"assessment_item\": { \"identifier\": \"LP_UT_test_" + num
					+ "\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"code\": \"test.mtf_mixed_1\", \"name\": \"MTF Question 1\", \"type\": \"mtf\", \"template\": \"mtf_template_3\", \"qlevel\": \"MEDIUM\", \"title\": \"ಕೊಟ್ಟಿರುವ ಸಂಖ್ಯೆಗಳನ್ನು ಇಳಿಕೆ ಕ್ರಮದಲ್ಲಿ ಜೋಡಿಸಿರಿ.\", \"question\":\"2080\", \"model\":{ \"data0\":\"23450\", \"data1\":\"23540\" }, \"lhs_options\": [ { \"value\": {\"type\": \"image\", \"asset\": \"grey\"}, \"index\": 0 } ], \"rhs_options\": [ { \"value\": {\"type\": \"text\", \"asset\": \">\"} }, { \"value\": {\"type\": \"text\", \"asset\": \"=\"} }, { \"value\": {\"type\": \"mixed\", \"text\": \"<\", \"image\": \"image1\", \"audio\": \"audio1\"}, \"answer\": 0 } ], \"max_score\": 6, \"partial_scoring\": true, \"feedback\": \"\" } } } }";
			try {
				String path = "/v1/assessmentitem";
				actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
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

	// method to generate random numbers for a given range of input
	public List<Integer> generateRandomNumber(int min, int max) {
		for (int i = 10; i < 20; i++) {
			if (min >= max) {
				throw new IllegalArgumentException("max must be greater than min");
			}
			Random r = new Random();
			items.add(r.nextInt((max - min) + 1) + min + i);
		}
		return items;
	}

	// method to convert resultActions to required response
	public Response jsonToObject(ResultActions actions) {
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