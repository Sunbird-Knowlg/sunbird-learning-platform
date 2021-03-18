package org.sunbird.assessmentItem.controller;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.sunbird.common.dto.Response;
import org.sunbird.test.common.CommonTestSetup;
import org.junit.Assert;
import org.junit.Before;
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

/**
 * The AssessmentItemTest class contains tests to validate creation of
 * AssessmentItems, fetching the AssessmentItems, updating given asessmentItem,
 * search for assessmentItems based on given criteria, delete the given
 * assessmentitem Positive and negative test senarios have been specified for
 * each of the operation
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AssessmentitemV3Test extends CommonTestSetup {

	@Autowired
	private WebApplicationContext context;
	private MockMvc mockMvc;
	private ResultActions actions;
	private final String base_path = "/assessment/v3/items";

	private static String cassandraScript_1 = "CREATE KEYSPACE IF NOT EXISTS content_store_test WITH replication = {'class': 'SimpleStrategy','replication_factor': '1'};";
	private static String cassandraScript_2 = "CREATE TABLE IF NOT EXISTS content_store_test.question_data_test (question_id text,last_updated_on timestamp,body blob, question blob, solutions blob, editorState blob,PRIMARY KEY (question_id));";

	@BeforeClass
	public static void beforeClass() throws Exception {
		loadDefinition("definitions/concept_definition.json", "definitions/content_definition.json",
				"definitions/dimension_definition.json", "definitions/item_definition.json",
				"definitions/itemset_definition.json");
		executeScript(cassandraScript_1, cassandraScript_2);
	}

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
	}

	@Test
	@Ignore
	public void createAssessmentItem() {
		String request = "{\"request\":{\"assessment_item\":{\"identifier\":\"LP_UT_test_01\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"createdBy\":\"874ed8a5-782e-4f6c-8f36-e0288455901e\",\"creator\":\"Creation\",\"organisation\":[\"DLF Public School (Rajinder Nagar)\"],\"code\":\"124bd88e-0b38-0ba2-3719-f5c99e6ba38d\",\"type\":\"reference\",\"category\":\"VSA\",\"itemType\":\"UNIT\",\"version\":3,\"name\":\"vsa_NCFCOPY\",\"body\":\"<p>Write down in your words the conversation between the bird in the rishi’s ashram and the king.</p>\",\"editorState\":{\"solutions\":[\"<p>When the bird.</p>\"]},\"question\":\"<p>Write down in your words the conversation between the bird in the rishi’s ashram and the king.</p>\",\"solutions\":[\"<p>When the bird.</p>\"],\"learningOutcome\":[],\"bloomsLevel\":[\"Knowledge (Remembering)\"],\"templateId\":\"NA\",\"programId\":\"2bf17140-8124-11e9-bafa-676cba786201\",\"program\":\"CBSE\",\"channel\":\"b00bc992ef25f1a9a8d63291e20efc8d\",\"framework\":\"NCFCOPY\",\"board\":\"CBSE\",\"medium\":\"English\",\"gradeLevel\":[\"Kindergarten\"],\"subject\":\"English\",\"topic\":[\"Topic 1\"],\"status\":\"Review\",\"media\":[],\"qumlVersion\":0.5,\"textBookUnitIdentifier\":\"do_112771678991564800152\",\"authorNames\":\"Creation\"}}}}";
		try {
			String path = base_path + "/create";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.getCause();
		}
	}

	static int rn = generateRandomNumber(0, 9999);

	// create an assessmentItem without "code" in request body
	// expect 400 ok response
	@Test
	public void createAssessmentItemWithoutCode() {
		String request = "{ \"request\": { \"assessment_item\": { \"identifier\": \"LP_UT_" + rn
				+ "\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"name\": \"MTF Question 1\", \"type\": \"mtf\", \"template\": \"mtf_template_3\", \"qlevel\": \"MEDIUM\", \"title\": \"ಕೊಟ್ಟಿರುವ ಸಂಖ್ಯೆಗಳನ್ನು ಇಳಿಕೆ ಕ್ರಮದಲ್ಲಿ ಜೋಡಿಸಿರಿ.\", \"question\":\"2080\", \"model\":{ \"data0\":\"23450\", \"data1\":\"23540\" }, \"lhs_options\": [ { \"value\": {\"type\": \"image\", \"asset\": \"grey\"}, \"index\": 0 } ], \"rhs_options\": [ { \"value\": {\"type\": \"text\", \"asset\": \">\"} }, { \"value\": {\"type\": \"text\", \"asset\": \"=\"} }, { \"value\": {\"type\": \"mixed\", \"text\": \"<\", \"image\": \"image1\", \"audio\": \"audio1\"}, \"answer\": 0 } ], \"max_score\": 6, \"partial_scoring\": true, \"feedback\": \"\" } } } }";
		try {
			String path = base_path + "/create";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("failed", resp.getParams().getStatus());
	}

	// create assessmentItem without "name" in the request body
	// expect 400 response
	@Test
	public void createAssessmentItemWithoutName() {
		String request = "{ \"request\": { \"assessment_item\": { \"identifier\": \"LP_UT_" + rn
				+ "\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"code\": \"test.mcq_mixed_115\", \"type\": \"mtf\", \"template\": \"mtf_template_3\", \"qlevel\": \"MEDIUM\", \"title\": \"ಕೊಟ್ಟಿರುವ ಸಂಖ್ಯೆಗಳನ್ನು ಇಳಿಕೆ ಕ್ರಮದಲ್ಲಿ ಜೋಡಿಸಿರಿ.\", \"question\":\"2080\", \"model\":{ \"data0\":\"23450\", \"data1\":\"23540\" }, \"lhs_options\": [ { \"value\": {\"type\": \"image\", \"asset\": \"grey\"}, \"index\": 0 } ], \"rhs_options\": [ { \"value\": {\"type\": \"text\", \"asset\": \">\"} }, { \"value\": {\"type\": \"text\", \"asset\": \"=\"} }, { \"value\": {\"type\": \"mixed\", \"text\": \"<\", \"image\": \"image1\", \"audio\": \"audio1\"}, \"answer\": 0 } ], \"max_score\": 6, \"partial_scoring\": true, \"feedback\": \"\" } } } }";
		try {
			String path = base_path + "/create";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("failed", resp.getParams().getStatus());
	}

	// create assessmentItem without "type" in the request body
	// expect 400 response
	@Test
	public void createAssessmentItemWithoutType() {
		String request = "{ \"request\": { \"assessment_item\": { \"identifier\": \"LP_UT_" + rn
				+ "\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"code\": \"test.mcq_mixed_115\", \"name\": \"MTF Question 1\", \"template\": \"mtf_template_3\", \"qlevel\": \"MEDIUM\", \"title\": \"ಕೊಟ್ಟಿರುವ ಸಂಖ್ಯೆಗಳನ್ನು ಇಳಿಕೆ ಕ್ರಮದಲ್ಲಿ ಜೋಡಿಸಿರಿ.\", \"question\":\"2080\", \"model\":{ \"data0\":\"23450\", \"data1\":\"23540\" }, \"lhs_options\": [ { \"value\": {\"type\": \"image\", \"asset\": \"grey\"}, \"index\": 0 } ], \"rhs_options\": [ { \"value\": {\"type\": \"text\", \"asset\": \">\"} }, { \"value\": {\"type\": \"text\", \"asset\": \"=\"} }, { \"value\": {\"type\": \"mixed\", \"text\": \"<\", \"image\": \"image1\", \"audio\": \"audio1\"}, \"answer\": 0 } ], \"max_score\": 6, \"partial_scoring\": true, \"feedback\": \"\" } } } }";
		try {
			String path = base_path + "/create";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("failed", resp.getParams().getStatus());
	}

	// create assessmentItem without "template"
	// expect 400 response
	@Test
	public void createAssessmentItemWithoutTemplate() {
		String request = "{ \"request\": { \"assessment_item\": { \"identifier\": \"LP_UT_" + rn
				+ "\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"name\": \"MTF Question 1\", \"type\": \"mtf\", \"qlevel\": \"MEDIUM\", \"title\": \"ಕೊಟ್ಟಿರುವ ಸಂಖ್ಯೆಗಳನ್ನು ಇಳಿಕೆ ಕ್ರಮದಲ್ಲಿ ಜೋಡಿಸಿರಿ.\", \"question\":\"2080\", \"model\":{ \"data0\":\"23450\", \"data1\":\"23540\" }, \"lhs_options\": [ { \"value\": {\"type\": \"image\", \"asset\": \"grey\"}, \"index\": 0 } ], \"rhs_options\": [ { \"value\": {\"type\": \"text\", \"asset\": \">\"} }, { \"value\": {\"type\": \"text\", \"asset\": \"=\"} }, { \"value\": {\"type\": \"mixed\", \"text\": \"<\", \"image\": \"image1\", \"audio\": \"audio1\"}, \"answer\": 0 } ], \"max_score\": 6, \"partial_scoring\": true, \"feedback\": \"\" } } } }";
		try {
			String path = base_path + "/create";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("failed", resp.getParams().getStatus());
	}

	// create an already existing assessmentItem
	// expect 400 response
	@Test
	public void createNonexistingAssessmentItem() {
		String request = "{ \"request\": { \"assessment_item\": { \"identifier\": \"LP_UT_test_01\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"code\": \"test.mcq_mixed_115\", \"name\": \"MCQ Question 2\", \"type\": \"mcq\", \"num_answers\": 1, \"template\": \"mcq_template_2\", \"qlevel\": \"MEDIUM\", \"owner\": \"username_1\", \"title\": \"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ. ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\", \"question\": \"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ. \nಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\", \"model\": { \"img\": { \"type\": \"image\", \"asset\": \"perimeter\" }, \"img2\": { \"type\": \"image\", \"asset\": \"smallSquare\" }, \"subtext\": \"( = 1 ಚದರ ಸೆಂ.ಮೀ)\" }, \"options\": [ { \"value\": { \"type\": \"text\", \"asset\": \"12&10\" } }, { \"value\": { \"type\": \"text\", \"asset\": \"14&7\" } }, { \"value\": { \"type\": \"mixed\", \"text\": \"16&8\", \"image\": \"image1\", \"audio\": \"audio1\" } }, { \"value\": { \"type\": \"mixed\", \"image\": \"image2\", \"audio\": \"audio2\" }, \"answer\": true } ], \"max_score\": 1, \"partial_scoring\": false, \"feedback\": \"\", \"responses\": [ { \"values\": {\"12&10\": true}, \"score\": 1 } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
		try {
			String path = base_path + "/create";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// create an assessmentitem with invalid url
	// expect 404 response
	@Test
	public void createAssessmentItemWithInvalidUrl() {
		String request = "{ \"request\": { \"assessment_item\": { \"identifier\": \"LP_UT_test_01\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"code\": \"test.mcq_mixed_115\", \"name\": \"MCQ Question 2\", \"type\": \"mcq\", \"num_answers\": 1, \"template\": \"mcq_template_2\", \"qlevel\": \"MEDIUM\", \"owner\": \"username_1\", \"title\": \"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ. ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\", \"question\": \"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ. \nಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\", \"model\": { \"img\": { \"type\": \"image\", \"asset\": \"perimeter\" }, \"img2\": { \"type\": \"image\", \"asset\": \"smallSquare\" }, \"subtext\": \"( = 1 ಚದರ ಸೆಂ.ಮೀ)\" }, \"options\": [ { \"value\": { \"type\": \"text\", \"asset\": \"12&10\" } }, { \"value\": { \"type\": \"text\", \"asset\": \"14&7\" } }, { \"value\": { \"type\": \"mixed\", \"text\": \"16&8\", \"image\": \"image1\", \"audio\": \"audio1\" } }, { \"value\": { \"type\": \"mixed\", \"image\": \"image2\", \"audio\": \"audio2\" }, \"answer\": true } ], \"max_score\": 1, \"partial_scoring\": false, \"feedback\": \"\", \"responses\": [ { \"values\": {\"12&10\": true}, \"score\": 1 } ] }, \"outRelations\": [ { \"endNodeId\": \"Num:C1:SC1\", \"relationType\": \"associatedTo\" } ] } } }";
		try {
			String path = base_path + "/create";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// create assessmentItem without lhs or rhs options for an mtf type
	// assessementitem
	// expect 400 ok response
	@Test
	public void createAssessmentItemWithoutOptions() {
		String request = "{ \"request\": { \"assessment_item\": { \"identifier\": \"LP_UT_" + rn
				+ "\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"code\": \"test.mtf_mixed_1\", \"name\": \"MTF Question 1\", \"type\": \"mtf\", \"template\": \"mtf_template_3\", \"qlevel\": \"MEDIUM\", \"title\": \"ಕೊಟ್ಟಿರುವ ಸಂಖ್ಯೆಗಳನ್ನು ಇಳಿಕೆ ಕ್ರಮದಲ್ಲಿ ಜೋಡಿಸಿರಿ.\", \"question\":\"2080\", \"model\":{ \"data0\":\"23450\", \"data1\":\"23540\" }, \"rhs_options\": [ { \"value\": {\"type\": \"text\", \"asset\": \">\"} }, { \"value\": {\"type\": \"text\", \"asset\": \"=\"} }, { \"value\": {\"type\": \"mixed\", \"text\": \"<\", \"image\": \"image1\", \"audio\": \"audio1\"}, \"answer\": 0 } ], \"max_score\": 6, \"partial_scoring\": true, \"feedback\": \"\" } } } }";
		try {
			String path = base_path + "/create";
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("failed", resp.getParams().getStatus());
	}

	// Gets an existing assessmentItem with valid assessmentitem Id
	// expect 200 ok response
	@Test
	@Ignore
	public void createAssessmentItem_get() {
		String path = base_path + "/read/LP_UT_test_01";
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

	// Get an non-existing assessmentItem
	// expect 404 response
	@Test
	public void getNonexistingAssessmentItem() {
		String path = base_path + "/read/wings.D.0";
		try {
			actions = this.mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
			Response response = jsonToObject(actions);
			assertEquals("failed", response.getParams().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Get an valid assessmentItem with invalid url
	// expect 404 response
	@Test
	public void getAssessmentItemTestWithInvalidUrl() {
		String path = base_path + "/red/wings.D.01";
		try {
			actions = this.mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	@Test
	public void getAssessmentList() {
		List<String> nodes = createAssessmentItem(10);
		String path = base_path + "/list?offset=0&limit=10";
		String contentString = "{ \"request\": { \"metadata\": { \"filters\": [] }}}";
		try {
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).content(contentString));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
			Response response = jsonToObject(actions);
			assertEquals("successful", response.getParams().getStatus());
			assertEquals(false, response.getResult().isEmpty());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Update assessment with valid assessmentitem id
	// expect 200 ok response
	@Test
	@Ignore
	public void updateAssessmentItem() {
		String node_id = "LP_UT_test_01";
		String contentString = "{\"request\":{\"assessment_item\":{\"objectType\":\"AssessmentItem\",\"metadata\":{\"createdBy\":\"874ed8a5-782e-4f6c-8f36-e0288455901e\",\"creator\":\"Creation\",\"organisation\":[\"DLF Public School (Rajinder Nagar)\"],\"code\":\"124bd88e-0b38-0ba2-3719-f5c99e6ba38d\",\"type\":\"reference\",\"category\":\"VSA\",\"itemType\":\"UNIT\",\"version\":3,\"name\":\"vsa_NCFCOPY\",\"body\":\"<p>Write down in your words the conversation between the bird in the rishi’s ashram and the king.</p>\",\"editorState\":{\"solutions\":[\"<p>When the bird.</p>\"]},\"question\":\"<p>Write down in your words the conversation between the bird in the rishi’s ashram and the king.</p>\",\"solutions\":[\"<p>When the bird.</p>\"],\"learningOutcome\":[],\"bloomsLevel\":[\"Knowledge (Remembering)\"],\"templateId\":\"NA\",\"programId\":\"2bf17140-8124-11e9-bafa-676cba786201\",\"program\":\"CBSE\",\"channel\":\"b00bc992ef25f1a9a8d63291e20efc8d\",\"framework\":\"NCFCOPY\",\"board\":\"CBSE\",\"medium\":\"English\",\"gradeLevel\":[\"Kindergarten\"],\"subject\":\"English\",\"topic\":[\"Topic 1\"],\"status\":\"Review\",\"media\":[],\"qumlVersion\":0.5,\"textBookUnitIdentifier\":\"do_112771678991564800152\",\"authorNames\":\"Creation\"}}}}";
		String path = base_path + "/update/" + node_id;
		try {
			actions = this.mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).content(contentString));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
	}

	// Update an non-existing assessmentItem
	@Test
	public void updateNonExistingAssessmentItem() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "{ \"request\": { \"assessment_item\": { \"objectType\": \"AssessmentItem\", \"metadata\": { \"template\": \"mtf_template_3\", \"model\": { \"data0\": \"23450\", \"data1\": \"23540\" }, \"subject\": \"domain\", \"qid\": \"wings.d.0.1\", \"qtype\": \"mtf\", \"code\": \"G5Q1\", \"lastUpdatedOn\": \"2016-04-14T07:54:58.073+0000\", \"type\": \"mtf\", \"concepts\": [ { \"identifier\": \"Num:C2:SC2:MC5\", \"name\": \"Ascending & descending order,skip counting, additive reasoning upto 100\", \"objectType\": \"Concept\", \"relation\": \"associatedTo\", \"description\": null, \"index\": null } ], \"feedback\": \"\", \"createdOn\": \"2016-02-08T07:34:43.614+0000\", \"qlevel\": \"MEDIUM\", \"title\": \"ಕೊಟ್ಟಿರುವ ಎರಡು ಸಂಖ್ಯೆಗಳನ್ನು ಹೋಲಿಸಿ, ಸೂಕ್ತ ಚಿಹ್ನೆಯನ್ನು ಆರಿಸಿರಿ \", \"partial_scoring\": true, \"name\": \"G5Q1\", \"usedIn\": \"numeracy_377\", \"max_score\": 6, \"lhs_options\": [ { \"value\": { \"type\": \"image\", \"asset\": \"grey\" }, \"index\": 0 } ], \"gradeLevel\": [ \"Grade 1\" ], \"question\": \"2080\", \"language\": [ \"English\" ], \"identifier\": \"G5Q1\", \"rhs_options\": [ { \"value\": { \"type\": \"text\", \"asset\": \">\", \"font\": \"Verdana\", \"color\": \"black\", \"fontsize\": \"1000\" } }, { \"value\": { \"type\": \"text\", \"asset\": \"=\", \"font\": \"Verdana\", \"color\": \"black\", \"fontsize\": \"1000\" } }, { \"value\": { \"type\": \"text\", \"asset\": \"<\", \"font\": \"Verdana\", \"color\": \"black\", \"fontsize\": \"1000\" }, \"answer\": 0 } ] } } } }";
		String path = base_path + "/update/test123";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).content(contentString));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Response resp = jsonToObject(actions);
		// Assert.assertEquals("successful", resp.getParams().getStatus());
	}

	// Update an existing assessmentItem with invalid url
	// expect 404 respone
	@Test
	public void updateAssessmentItemWithInvalidUrl() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "{ \"request\": { \"assessment_item\": { \"objectType\": \"AssessmentItem\", \"metadata\": { \"template\": \"mtf_template_3\", \"model\": { \"data0\": \"23450\", \"data1\": \"23540\" }, \"subject\": \"domain\", \"qid\": \"wings.d.0.1\", \"qtype\": \"mtf\", \"code\": \"G5Q1\", \"lastUpdatedOn\": \"2016-04-14T07:54:58.073+0000\", \"type\": \"mtf\", \"concepts\": [ { \"identifier\": \"Num:C2:SC2:MC5\", \"name\": \"Ascending & descending order,skip counting, additive reasoning upto 100\", \"objectType\": \"Concept\", \"relation\": \"associatedTo\", \"description\": null, \"index\": null } ], \"feedback\": \"\", \"createdOn\": \"2016-02-08T07:34:43.614+0000\", \"qlevel\": \"MEDIUM\", \"title\": \"ಕೊಟ್ಟಿರುವ ಎರಡು ಸಂಖ್ಯೆಗಳನ್ನು ಹೋಲಿಸಿ, ಸೂಕ್ತ ಚಿಹ್ನೆಯನ್ನು ಆರಿಸಿರಿ \", \"partial_scoring\": true, \"name\": \"G5Q1\", \"usedIn\": \"numeracy_377\", \"max_score\": 6, \"lhs_options\": [ { \"value\": { \"type\": \"image\", \"asset\": \"grey\" }, \"index\": 0 } ], \"gradeLevel\": [ \"Grade 1\" ], \"question\": \"2080\", \"language\": [ \"English\" ], \"identifier\": \"G5Q1\", \"rhs_options\": [ { \"value\": { \"type\": \"text\", \"asset\": \">\", \"font\": \"Verdana\", \"color\": \"black\", \"fontsize\": \"1000\" } }, { \"value\": { \"type\": \"text\", \"asset\": \"=\", \"font\": \"Verdana\", \"color\": \"black\", \"fontsize\": \"1000\" } }, { \"value\": { \"type\": \"text\", \"asset\": \"<\", \"font\": \"Verdana\", \"color\": \"black\", \"fontsize\": \"1000\" }, \"answer\": 0 } ] } } } }";
		String path = base_path + "/aupdate/wings.d.0.1";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).content(contentString));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Update an existing assessmentItem with invalid request body
	// expect 400 response
	@Test
	public void updateInvalidAssessmentItem() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String contentString = "{ \"request\": { \"assessment_item\": { \"objectType\": \"AssessmentItem\", \"metadata\": { \"template\": \"mtf_template_3\", \"model\": { \"data0\": \"23450\", \"data1\": \"23540\" } } } } }";
		String path = base_path + "/update/wings.d.0.1";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).content(contentString));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Search assessmentItem with appropriate filters
	// expect 200 ok response
	@Test
	public void searchAssessmentItem() {
		String contentString = "{ \"request\": { \"metadata\": { \"filters\": [ {\"property\" : \"template_id\", \"operator\": \"=\", \"value\": \"domain_3490\"} ] }, \"sortOrder\": [ {\"sortField\": \"code\", \"sortOrder\": \"DESC\"} ], \"startPosition\": 0, \"resultSize\": 10 } }";
		String path = base_path + "/search";
		try {
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).content(contentString));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("successful", resp.getParams().getStatus());
	}

	// Search for an assessmentItem with invalid url
	// expect 404 response
	@Test
	public void searchAssessmentItemWithInvalidUrl() {
		String contentString = "{ \"request\": { \"metadata\": { \"filters\": [ {\"property\" : \"template_id\", \"operator\": \"=\", \"value\": \"domain_3490\"} ] }, \"sortOrder\": [ {\"sortField\": \"code\", \"sortOrder\": \"DESC\"} ], \"startPosition\": 0, \"resultSize\": 10 } }";
		String path = base_path + "/sdearch";
		try {
			actions = this.mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).content(contentString));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Delete an assessmentItem with valid assesssmentitem id
	// expect 200 ok response
	// @Test
	public void deleteAssessmentItem() {
		String node_id = "LP_UT_test_01";
		String path = base_path + "/retire/" + node_id;
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

	// Delete an non-existiing assessmentItem
	// expect 404 response
	// @Test
	public void deleteNonexistingAssessmentItem() {
		String path = base_path + "/retire/q_1_s_urdu_01";
		try {
			actions = this.mockMvc.perform(MockMvcRequestBuilders.delete(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		Assert.assertEquals("failed", resp.getParams().getStatus());
	}

	// Delete assessmentItem with invalid url
	// expect 404 response
	@Test
	public void deleteAssessmentItemWithInvalidUrl() {
		String path = base_path + "/retdire/q_1_s_urdu";
		try {
			actions = this.mockMvc.perform(MockMvcRequestBuilders.delete(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// method to generate random numbers for a given range of input
	private static int generateRandomNumber(int min, int max) {
		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}

	// method to create assessmentItems
	public List<String> createAssessmentItem(int range) {
		MockMvc mockMvc;
		List<String> nodes = new ArrayList<String>();
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		for (int i = 0; i < range; i++) {
			String request = "{\"request\": {\"assessment_item\": {\"identifier\": \"LP_UT_test_" + i
					+ "\",\"objectType\": \"AssessmentItem\",\"metadata\": {\"code\": \"test.qtt02\",\"body\":\"Test Data for body........save it in cassandra\", \"itemType\":\"UNIT\",\"category\":\"MCQ\",\"version\":2,\"name\": \"MCQ Question 2\",\"type\": \"mcq\",\"num_answers\": 1,\"template\": \"mcq_template_2\",\"template_id\":\"mcq_template_2\",\"qlevel\": \"MEDIUM\",\"owner\": \"username_1\",\"title\": \"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ.  ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\",\"question\": \"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ.ಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\",\"model\": {\"img\": {\"type\": \"image\",\"asset\": \"perimeter\"},\"img2\": {\"type\": \"image\",\"asset\": \"smallSquare\"},\"subtext\": \"( = 1  ಚದರ ಸೆಂ.ಮೀ)\"},\"options\": [{\"value\": {\"type\": \"text\",\"asset\": \"12&10\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"14&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"16&8\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"12&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"},\"score\": 1}],\"max_score\": 1,\"partial_scoring\": false,\"feedback\": \"\"}}}}";
			try {
				String path = base_path + "/create";
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
