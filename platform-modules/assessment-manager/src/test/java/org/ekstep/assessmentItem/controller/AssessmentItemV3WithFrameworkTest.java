/**
 * 
 */
package org.ekstep.assessmentItem.controller;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.graph.model.cache.CategoryCache;
import org.ekstep.test.common.CommonTestSetup;
import org.junit.AfterClass;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author gauraw
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AssessmentItemV3WithFrameworkTest extends CommonTestSetup {

	@Autowired
	private WebApplicationContext context;
	private MockMvc mockMvc;
	private ResultActions actions;
	private final String basePath = "/assessment/v3/items";
	private static String assessmentId = "";
	private static ObjectMapper mapper = new ObjectMapper();

	private static String cassandraScript_1 = "CREATE KEYSPACE IF NOT EXISTS content_store_test WITH replication = {'class': 'SimpleStrategy','replication_factor': '1'};";
	private static String cassandraScript_2 = "CREATE TABLE IF NOT EXISTS content_store_test.question_data_test (question_id text,last_updated_on timestamp,body blob, question blob, solutions blob, editorState blob,PRIMARY KEY (question_id));";

	@BeforeClass
	public static void setup() throws Exception {
		loadDefinition("definitions/concept_definition.json", "definitions/dimension_definition.json",
				"definitions/item_definition.json", "definitions/itemset_definition.json");
		executeScript(cassandraScript_1, cassandraScript_2);
		createFramework();
	}

	@AfterClass
	public static void clean() {
	}

	@Before
	public void init() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
		if (StringUtils.isBlank(assessmentId))
			createAssessmentItem();
	}

	/*
	 * Framework Name: NCFTEST
	 * 
	 * Framework Category: medium, subject, board, gradeLevel, topic
	 * 
	 * Framework Term: english, math, cbse, class-1, addition
	 *
	 */
	public static void createFramework() throws Exception {
		String fwHierarchy = "{\"categories\":[{\"identifier\":\"ncftest_medium\",\"code\":\"medium\",\"terms\":[{\"identifier\":\"ncftest_medium_english\",\"code\":\"english\",\"name\":\"english\",\"description\":\"English Medium\",\"index\":1,\"category\":\"medium\",\"status\":\"Live\"}],\"name\":\"medium\",\"description\":\"Medium for NCFTEST\",\"index\":1,\"status\":\"Live\"},{\"identifier\":\"ncftest_subject\",\"code\":\"subject\",\"terms\":[{\"identifier\":\"ncftest_subject_math\",\"code\":\"math\",\"name\":\"math\",\"description\":\"Mathematics\",\"index\":1,\"category\":\"subject\",\"status\":\"Live\"}],\"name\":\"subject\",\"description\":\"Subject for NCFTEST\",\"index\":2,\"status\":\"Live\"},{\"identifier\":\"ncftest_board\",\"code\":\"board\",\"terms\":[{\"identifier\":\"ncftest_board_cbse\",\"code\":\"cbse\",\"name\":\"cbse\",\"description\":\"CBSE Board\",\"index\":1,\"category\":\"board\",\"status\":\"Live\"}],\"name\":\"board\",\"description\":\"Board for NCFTEST\",\"index\":3,\"status\":\"Live\"},{\"identifier\":\"ncftest_topic\",\"code\":\"topic\",\"terms\":[{\"identifier\":\"ncftest_topic_addition\",\"code\":\"addition\",\"name\":\"addition\",\"description\":\"Addition\",\"index\":1,\"category\":\"topic\",\"status\":\"Live\"}],\"name\":\"topic\",\"description\":\"Topics for NCFTEST\",\"index\":4,\"status\":\"Live\"},{\"identifier\":\"ncftest_gradelevel\",\"code\":\"gradeLevel\",\"terms\":[{\"identifier\":\"ncftest_gradelevel_class-1\",\"code\":\"class 1\",\"name\":\"class 1\",\"description\":\"Class 1\",\"index\":1,\"category\":\"gradeLevel\",\"status\":\"Live\"}],\"name\":\"gradeLevel\",\"description\":\"Grade Level for NCFTEST\",\"index\":5,\"status\":\"Live\"}]}";
		Map<String, Object> frameworkHierarchy = mapper.readValue(fwHierarchy,
				new TypeReference<Map<String, Object>>() {
				});
		CategoryCache.setFramework("NCFTEST", frameworkHierarchy);
	}

	private void createAssessmentItem() throws Exception {
		String request = "{\"request\":{\"assessment_item\":{\"objectType\":\"AssessmentItem\",\"identifier\":\"LP_UTEST_01\",\"metadata\":{\"createdBy\":\"874ed8a5-782e-4f6c-8f36-e0288455901e\",\"creator\":\"Creation\",\"organisation\":[\"DLF Public School (Rajinder Nagar)\"],\"code\":\"124bd88e-0b38-0ba2-3719-f5c99e6ba38d\",\"type\":\"reference\",\"category\":\"VSA\",\"itemType\":\"UNIT\",\"version\":3,\"name\":\"vsa_NCFCOPY\",\"body\":\"<p>Write down in your words the conversation between the bird in the rishi’s ashram and the king.</p>\",\"editorState\":{\"solutions\":[\"<p>When the bird.</p>\"]},\"question\":\"<p>Write down in your words the conversation between the bird in the rishi’s ashram and the king.</p>\",\"solutions\":[\"<p>When the bird.</p>\"],\"learningOutcome\":[],\"bloomsLevel\":[\"Knowledge (Remembering)\"],\"templateId\":\"NA\",\"programId\":\"2bf17140-8124-11e9-bafa-676cba786201\",\"program\":\"CBSE\",\"channel\":\"b00bc992ef25f1a9a8d63291e20efc8d\",\"framework\":\"NCFCOPY\",\"board\":\"CBSE\",\"medium\":\"English\",\"gradeLevel\":[\"Kindergarten\"],\"subject\":\"English\",\"topic\":[\"Topic 1\"],\"status\":\"Review\",\"media\":[],\"qumlVersion\":0.5,\"textBookUnitIdentifier\":\"do_112771678991564800152\",\"authorNames\":\"Creation\"}}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		if (200 == actions.andReturn().getResponse().getStatus())
			assessmentId = "LP_UTEST_01";
	}

	/*
	 * Create Assessment Item without Framework Expected: Item should be created
	 * with NCF Framework
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testItem_01() throws Exception {
		String request = "{\"request\": {\"assessment_item\": {\"identifier\": \"LP_UTEST_02\",\"objectType\": \"AssessmentItem\",\"metadata\": {\"code\": \"test.qtt02\",\"body\":\"Test Data for body........save it in cassandra\", \"itemType\":\"UNIT\",\"category\":\"MCQ\",\"version\":2,\"name\": \"MCQ Question 2\",\"type\": \"mcq\",\"num_answers\": 1,\"template\": \"mcq_template_2\",\"template_id\":\"mcq_template_2\",\"qlevel\": \"MEDIUM\",\"owner\": \"username_1\",\"title\": \"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ.  ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\",\"question\": \"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ.ಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\",\"model\": {\"img\": {\"type\": \"image\",\"asset\": \"perimeter\"},\"img2\": {\"type\": \"image\",\"asset\": \"smallSquare\"},\"subtext\": \"( = 1  ಚದರ ಸೆಂ.ಮೀ)\"},\"options\": [{\"value\": {\"type\": \"text\",\"asset\": \"12&10\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"14&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"16&8\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"12&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"},\"score\": 1}],\"max_score\": 1,\"partial_scoring\": false,\"feedback\": \"\"}}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		path = basePath + "/read/LP_UTEST_02";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		String frId = (String) ((Map<String, Object>) resp.getResult().get("assessment_item")).get("framework");
		assertEquals("NCF", frId);
	}

	/*
	 * Create Assessment Item with Framework (NCFTEST). Expected: Item should be
	 * created with given Framework
	 */
	@Test
	public void testItem_02() throws Exception {
		String request = "{\"request\": {\"assessment_item\": {\"identifier\": \"LP_UTEST_03\",\"objectType\": \"AssessmentItem\",\"metadata\": {\"code\": \"test.qtt02\",\"framework\":\"NCFTEST\",\"body\":\"Test Data for body........save it in cassandra\", \"itemType\":\"UNIT\",\"category\":\"MCQ\",\"version\":2,\"name\": \"MCQ Question 2\",\"type\": \"mcq\",\"num_answers\": 1,\"template\": \"mcq_template_2\",\"template_id\":\"mcq_template_2\",\"qlevel\": \"MEDIUM\",\"owner\": \"username_1\",\"title\": \"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ.  ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\",\"question\": \"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ.ಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\",\"model\": {\"img\": {\"type\": \"image\",\"asset\": \"perimeter\"},\"img2\": {\"type\": \"image\",\"asset\": \"smallSquare\"},\"subtext\": \"( = 1  ಚದರ ಸೆಂ.ಮೀ)\"},\"options\": [{\"value\": {\"type\": \"text\",\"asset\": \"12&10\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"14&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"16&8\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"12&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"},\"score\": 1}],\"max_score\": 1,\"partial_scoring\": false,\"feedback\": \"\"}}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		path = basePath + "/read/LP_UTEST_03";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		@SuppressWarnings("unchecked")
		String frId = (String) ((Map<String, Object>) resp.getResult().get("assessment_item")).get("framework");
		assertEquals("NCFTEST", frId);
	}

	/*
	 * Create AssessmentItem with valid data for medium (medium=english).
	 * Expected: 200 - OK. Record Should be created with given data.
	 */
	@Test
	public void testItem_03() throws Exception {
		String request = "{\"request\": {\"assessment_item\": {\"identifier\": \"LP_UTEST_04\",\"objectType\": \"AssessmentItem\",\"metadata\": {\"code\": \"test.qtt02\",\"framework\":\"NCFTEST\",\"medium\":\"english\",\"body\":\"Test Data for body........save it in cassandra\", \"itemType\":\"UNIT\",\"category\":\"MCQ\",\"version\":2,\"name\": \"MCQ Question 2\",\"type\": \"mcq\",\"num_answers\": 1,\"template\": \"mcq_template_2\",\"template_id\":\"mcq_template_2\",\"qlevel\": \"MEDIUM\",\"owner\": \"username_1\",\"title\": \"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ.  ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\",\"question\": \"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ.ಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\",\"model\": {\"img\": {\"type\": \"image\",\"asset\": \"perimeter\"},\"img2\": {\"type\": \"image\",\"asset\": \"smallSquare\"},\"subtext\": \"( = 1  ಚದರ ಸೆಂ.ಮೀ)\"},\"options\": [{\"value\": {\"type\": \"text\",\"asset\": \"12&10\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"14&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"16&8\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"12&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"},\"score\": 1}],\"max_score\": 1,\"partial_scoring\": false,\"feedback\": \"\"}}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		path = basePath + "/read/LP_UTEST_04";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		@SuppressWarnings("unchecked")
		String medium = (String) ((Map<String, Object>) resp.getResult().get("assessment_item")).get("medium");
		assertEquals("english", medium);
	}

	/*
	 * Create AssessmentItem with Invalid data for medium (medium=hindi).
	 * Expected: 400 - CLIENT_ERROR. Record Should not be created with given
	 * data.
	 */
	@Test
	public void testItem_04() throws Exception {
		String request = "{\"request\": {\"assessment_item\": {\"identifier\": \"LP_UTEST_05\",\"objectType\": \"AssessmentItem\",\"metadata\": {\"code\": \"test.qtt02\",\"framework\":\"NCFTEST\",\"medium\":\"hindi\",\"body\":\"Test Data for body........save it in cassandra\", \"itemType\":\"UNIT\",\"category\":\"MCQ\",\"version\":2,\"name\": \"MCQ Question 2\",\"type\": \"mcq\",\"num_answers\": 1,\"template\": \"mcq_template_2\",\"template_id\":\"mcq_template_2\",\"qlevel\": \"MEDIUM\",\"owner\": \"username_1\",\"title\": \"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ.  ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\",\"question\": \"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ.ಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\",\"model\": {\"img\": {\"type\": \"image\",\"asset\": \"perimeter\"},\"img2\": {\"type\": \"image\",\"asset\": \"smallSquare\"},\"subtext\": \"( = 1  ಚದರ ಸೆಂ.ಮೀ)\"},\"options\": [{\"value\": {\"type\": \"text\",\"asset\": \"12&10\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"14&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"16&8\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"12&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"},\"score\": 1}],\"max_score\": 1,\"partial_scoring\": false,\"feedback\": \"\"}}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Update AssessmentItem with valid data for medium (medium=english).
	 * Expected: 200 - OK. Record Should be updated with given data.
	 */
	@Test
	@Ignore
	public void testItem_05() throws Exception {
		String request = "{\"request\":{\"assessment_item\":{\"identifier\":\"" + assessmentId + "+\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"createdBy\":\"874ed8a5-782e-4f6c-8f36-e0288455901e\",\"creator\":\"Creation\",\"organisation\":[\"DLF Public School (Rajinder Nagar)\"],\"code\":\"124bd88e-0b38-0ba2-3719-f5c99e6ba38d\",\"type\":\"reference\",\"category\":\"VSA\",\"itemType\":\"UNIT\",\"version\":3,\"name\":\"vsa_NCFCOPY\",\"body\":\"<p>Write down in your words the conversation between the bird in the rishi’s ashram and the king.</p>\",\"editorState\":{\"solutions\":[\"<p>When the bird.</p>\"]},\"question\":\"<p>Write down in your words the conversation between the bird in the rishi’s ashram and the king.</p>\",\"solutions\":[\"<p>When the bird.</p>\"],\"learningOutcome\":[],\"bloomsLevel\":[\"Knowledge (Remembering)\"],\"templateId\":\"NA\",\"programId\":\"2bf17140-8124-11e9-bafa-676cba786201\",\"program\":\"CBSE\",\"channel\":\"b00bc992ef25f1a9a8d63291e20efc8d\",\"framework\":\"NCFCOPY\",\"board\":\"CBSE\",\"medium\":\"English\",\"gradeLevel\":[\"Kindergarten\"],\"subject\":\"English\",\"topic\":[\"Topic 1\"],\"status\":\"Review\",\"media\":[],\"qumlVersion\":0.5,\"textBookUnitIdentifier\":\"do_112771678991564800152\",\"authorNames\":\"Creation\"}}}}";
		String path = basePath + "/update/" + assessmentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		path = basePath + "/read/" + assessmentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		@SuppressWarnings("unchecked")
		String medium = (String) ((Map<String, Object>) resp.getResult().get("assessment_item")).get("medium");
		assertEquals("English", medium);
	}

	/*
	 * Update AssessmentItem with Invalid data for medium (medium=hindi).
	 * Expected: 400 - CLIENT_ERROR. Record Should not be updated with given
	 * data.
	 */
	@Test
	@Ignore
	public void testItem_06() throws Exception {
		String request = "{\"request\": {\"assessment_item\": {\"identifier\": \"" + assessmentId
				+ "\",\"objectType\": \"AssessmentItem\",\"metadata\": {\"code\": \"test.qtt02\",\"framework\":\"NCFTEST\",\"medium\":\"hindi\",\"body\":\"Test Data for body........save it in cassandra\", \"itemType\":\"UNIT\",\"category\":\"MCQ\",\"version\":2,\"name\": \"MCQ Question 2\",\"type\": \"mcq\",\"num_answers\": 1,\"template\": \"mcq_template_2\",\"template_id\":\"mcq_template_2\",\"qlevel\": \"MEDIUM\",\"owner\": \"username_1\",\"title\": \"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ.  ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\",\"question\": \"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ.ಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\",\"model\": {\"img\": {\"type\": \"image\",\"asset\": \"perimeter\"},\"img2\": {\"type\": \"image\",\"asset\": \"smallSquare\"},\"subtext\": \"( = 1  ಚದರ ಸೆಂ.ಮೀ)\"},\"options\": [{\"value\": {\"type\": \"text\",\"asset\": \"12&10\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"14&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"16&8\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"12&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"},\"score\": 1}],\"max_score\": 1,\"partial_scoring\": false,\"feedback\": \"\"}}}}";
		String path = basePath + "/update/" + assessmentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Create AssessmentItem with valid data for subject (subject=math).
	 * Expected: 200 - OK. Record Should be created with given data.
	 */
	@Test
	public void testItem_07() throws Exception {
		String request = "{\"request\": {\"assessment_item\": {\"identifier\": \"LP_UTEST_06\",\"objectType\": \"AssessmentItem\",\"metadata\": {\"code\": \"test.qtt02\",\"framework\":\"NCFTEST\",\"subject\":\"math\",\"body\":\"Test Data for body........save it in cassandra\", \"itemType\":\"UNIT\",\"category\":\"MCQ\",\"version\":2,\"name\": \"MCQ Question 2\",\"type\": \"mcq\",\"num_answers\": 1,\"template\": \"mcq_template_2\",\"template_id\":\"mcq_template_2\",\"qlevel\": \"MEDIUM\",\"owner\": \"username_1\",\"title\": \"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ.  ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\",\"question\": \"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ.ಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\",\"model\": {\"img\": {\"type\": \"image\",\"asset\": \"perimeter\"},\"img2\": {\"type\": \"image\",\"asset\": \"smallSquare\"},\"subtext\": \"( = 1  ಚದರ ಸೆಂ.ಮೀ)\"},\"options\": [{\"value\": {\"type\": \"text\",\"asset\": \"12&10\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"14&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"16&8\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"12&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"},\"score\": 1}],\"max_score\": 1,\"partial_scoring\": false,\"feedback\": \"\"}}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		path = basePath + "/read/LP_UTEST_06";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		@SuppressWarnings("unchecked")
		String subject = (String) ((Map<String, Object>) resp.getResult().get("assessment_item")).get("subject");
		assertEquals("math", subject);
	}

	/*
	 * Create AssessmentItem with Invalid data for subject (subject=science).
	 * Expected: 400 - CLIENT_ERROR. Record Should not be created with given
	 * data.
	 */
	@Test
	public void testItem_08() throws Exception {
		String request = "{\"request\": {\"assessment_item\": {\"identifier\": \"LP_UTEST_07\",\"objectType\": \"AssessmentItem\",\"metadata\": {\"code\": \"test.qtt02\",\"framework\":\"NCFTEST\",\"subject\":\"science\",\"body\":\"Test Data for body........save it in cassandra\", \"itemType\":\"UNIT\",\"category\":\"MCQ\",\"version\":2,\"name\": \"MCQ Question 2\",\"type\": \"mcq\",\"num_answers\": 1,\"template\": \"mcq_template_2\",\"template_id\":\"mcq_template_2\",\"qlevel\": \"MEDIUM\",\"owner\": \"username_1\",\"title\": \"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ.  ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\",\"question\": \"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ.ಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\",\"model\": {\"img\": {\"type\": \"image\",\"asset\": \"perimeter\"},\"img2\": {\"type\": \"image\",\"asset\": \"smallSquare\"},\"subtext\": \"( = 1  ಚದರ ಸೆಂ.ಮೀ)\"},\"options\": [{\"value\": {\"type\": \"text\",\"asset\": \"12&10\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"14&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"16&8\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"12&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"},\"score\": 1}],\"max_score\": 1,\"partial_scoring\": false,\"feedback\": \"\"}}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Update AssessmentItem with valid data for subject (medium=math).
	 * Expected: 200 - OK. Record Should be updated with given data.
	 */
	@Test
	@Ignore
	public void testItem_09() throws Exception {
		String request = "{\"request\":{\"assessment_item\":{\"identifier\":\"" + assessmentId + "+\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"createdBy\":\"874ed8a5-782e-4f6c-8f36-e0288455901e\",\"creator\":\"Creation\",\"organisation\":[\"DLF Public School (Rajinder Nagar)\"],\"code\":\"124bd88e-0b38-0ba2-3719-f5c99e6ba38d\",\"type\":\"reference\",\"category\":\"VSA\",\"itemType\":\"UNIT\",\"version\":3,\"name\":\"vsa_NCFCOPY\",\"body\":\"<p>Write down in your words the conversation between the bird in the rishi’s ashram and the king.</p>\",\"editorState\":{\"solutions\":[\"<p>When the bird.</p>\"]},\"question\":\"<p>Write down in your words the conversation between the bird in the rishi’s ashram and the king.</p>\",\"solutions\":[\"<p>When the bird.</p>\"],\"learningOutcome\":[],\"bloomsLevel\":[\"Knowledge (Remembering)\"],\"templateId\":\"NA\",\"programId\":\"2bf17140-8124-11e9-bafa-676cba786201\",\"program\":\"CBSE\",\"channel\":\"b00bc992ef25f1a9a8d63291e20efc8d\",\"framework\":\"NCFCOPY\",\"board\":\"CBSE\",\"medium\":\"English\",\"gradeLevel\":[\"Kindergarten\"],\"subject\":\"English\",\"topic\":[\"Topic 1\"],\"status\":\"Review\",\"media\":[],\"qumlVersion\":0.5,\"textBookUnitIdentifier\":\"do_112771678991564800152\",\"authorNames\":\"Creation\"}}}}";
		String path = basePath + "/update/" + assessmentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		path = basePath + "/read/" + assessmentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		String subject = (String) ((Map<String, Object>) resp.getResult().get("assessment_item")).get("subject");
		assertEquals("English", subject);
	}

	/*
	 * Update AssessmentItem with Invalid data for subject (subject=science).
	 * Expected: 400 - CLIENT_ERROR. Record Should not be updated with given
	 * data.
	 */
	@Test
	@Ignore
	public void testItem_10() throws Exception {
		String request = "{\"request\": {\"assessment_item\": {\"identifier\": \"" + assessmentId
				+ "\",\"objectType\": \"AssessmentItem\",\"metadata\": {\"code\": \"test.qtt02\",\"framework\":\"NCFTEST\",\"subject\":\"science\",\"body\":\"Test Data for body........save it in cassandra\", \"itemType\":\"UNIT\",\"category\":\"MCQ\",\"version\":2,\"name\": \"MCQ Question 2\",\"type\": \"mcq\",\"num_answers\": 1,\"template\": \"mcq_template_2\",\"template_id\":\"mcq_template_2\",\"qlevel\": \"MEDIUM\",\"owner\": \"username_1\",\"title\": \"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ.  ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\",\"question\": \"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ.ಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\",\"model\": {\"img\": {\"type\": \"image\",\"asset\": \"perimeter\"},\"img2\": {\"type\": \"image\",\"asset\": \"smallSquare\"},\"subtext\": \"( = 1  ಚದರ ಸೆಂ.ಮೀ)\"},\"options\": [{\"value\": {\"type\": \"text\",\"asset\": \"12&10\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"14&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"16&8\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"12&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"},\"score\": 1}],\"max_score\": 1,\"partial_scoring\": false,\"feedback\": \"\"}}}}";
		String path = basePath + "/update/" + assessmentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Create AssessmentItem with valid data for board (board=cbse). Expected:
	 * 200 - OK. Record Should be created with given data.
	 */
	@Test
	public void testItem_11() throws Exception {
		String request = "{\"request\": {\"assessment_item\": {\"identifier\": \"LP_UTEST_08\",\"objectType\": \"AssessmentItem\",\"metadata\": {\"code\": \"test.qtt02\",\"framework\":\"NCFTEST\",\"board\":\"cbse\",\"body\":\"Test Data for body........save it in cassandra\", \"itemType\":\"UNIT\",\"category\":\"MCQ\",\"version\":2,\"name\": \"MCQ Question 2\",\"type\": \"mcq\",\"num_answers\": 1,\"template\": \"mcq_template_2\",\"template_id\":\"mcq_template_2\",\"qlevel\": \"MEDIUM\",\"owner\": \"username_1\",\"title\": \"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ.  ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\",\"question\": \"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ.ಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\",\"model\": {\"img\": {\"type\": \"image\",\"asset\": \"perimeter\"},\"img2\": {\"type\": \"image\",\"asset\": \"smallSquare\"},\"subtext\": \"( = 1  ಚದರ ಸೆಂ.ಮೀ)\"},\"options\": [{\"value\": {\"type\": \"text\",\"asset\": \"12&10\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"14&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"16&8\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"12&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"},\"score\": 1}],\"max_score\": 1,\"partial_scoring\": false,\"feedback\": \"\"}}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		path = basePath + "/read/LP_UTEST_08";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		String board = (String) ((Map<String, Object>) resp.getResult().get("assessment_item")).get("board");
		assertEquals("cbse", board);
	}

	/*
	 * Create AssessmentItem with Invalid data for board (board=icse). Expected:
	 * 400 - CLIENT_ERROR. Record Should not be created with given data.
	 */
	@Test
	public void testItem_12() throws Exception {
		String request = "{\"request\": {\"assessment_item\": {\"identifier\": \"LP_UTEST_09\",\"objectType\": \"AssessmentItem\",\"metadata\": {\"code\": \"test.qtt02\",\"framework\":\"NCFTEST\",\"board\":\"icse\",\"body\":\"Test Data for body........save it in cassandra\", \"itemType\":\"UNIT\",\"category\":\"MCQ\",\"version\":2,\"name\": \"MCQ Question 2\",\"type\": \"mcq\",\"num_answers\": 1,\"template\": \"mcq_template_2\",\"template_id\":\"mcq_template_2\",\"qlevel\": \"MEDIUM\",\"owner\": \"username_1\",\"title\": \"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ.  ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\",\"question\": \"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ.ಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\",\"model\": {\"img\": {\"type\": \"image\",\"asset\": \"perimeter\"},\"img2\": {\"type\": \"image\",\"asset\": \"smallSquare\"},\"subtext\": \"( = 1  ಚದರ ಸೆಂ.ಮೀ)\"},\"options\": [{\"value\": {\"type\": \"text\",\"asset\": \"12&10\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"14&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"16&8\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"12&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"},\"score\": 1}],\"max_score\": 1,\"partial_scoring\": false,\"feedback\": \"\"}}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Update AssessmentItem with valid data for board (board=cbse). Expected:
	 * 200 - OK. Record Should be updated with given data.
	 */
	@Test
	@Ignore
	public void testItem_13() throws Exception {
		String request = "{\"request\":{\"assessment_item\":{\"identifier\":\"" + assessmentId + "+\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"createdBy\":\"874ed8a5-782e-4f6c-8f36-e0288455901e\",\"creator\":\"Creation\",\"organisation\":[\"DLF Public School (Rajinder Nagar)\"],\"code\":\"124bd88e-0b38-0ba2-3719-f5c99e6ba38d\",\"type\":\"reference\",\"category\":\"VSA\",\"itemType\":\"UNIT\",\"version\":3,\"name\":\"vsa_NCFCOPY\",\"body\":\"<p>Write down in your words the conversation between the bird in the rishi’s ashram and the king.</p>\",\"editorState\":{\"solutions\":[\"<p>When the bird.</p>\"]},\"question\":\"<p>Write down in your words the conversation between the bird in the rishi’s ashram and the king.</p>\",\"solutions\":[\"<p>When the bird.</p>\"],\"learningOutcome\":[],\"bloomsLevel\":[\"Knowledge (Remembering)\"],\"templateId\":\"NA\",\"programId\":\"2bf17140-8124-11e9-bafa-676cba786201\",\"program\":\"CBSE\",\"channel\":\"b00bc992ef25f1a9a8d63291e20efc8d\",\"framework\":\"NCFCOPY\",\"board\":\"CBSE\",\"medium\":\"English\",\"gradeLevel\":[\"Kindergarten\"],\"subject\":\"English\",\"topic\":[\"Topic 1\"],\"status\":\"Review\",\"media\":[],\"qumlVersion\":0.5,\"textBookUnitIdentifier\":\"do_112771678991564800152\",\"authorNames\":\"Creation\"}}}}";
		String path = basePath + "/update/" + assessmentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		path = basePath + "/read/" + assessmentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		String board = (String) ((Map<String, Object>) resp.getResult().get("assessment_item")).get("board");
		assertEquals("CBSE", board);
	}

	/*
	 * Update AssessmentItem with Invalid data for board (board=icse). Expected:
	 * 400 - CLIENT_ERROR. Record Should not be updated with given data.
	 */
	@Test
	@Ignore
	public void testItem_14() throws Exception {
		String request = "{\"request\": {\"assessment_item\": {\"identifier\": \"" + assessmentId
				+ "\",\"objectType\": \"AssessmentItem\",\"metadata\": {\"code\": \"test.qtt02\",\"framework\":\"NCFTEST\",\"subject\":\"board\",\"icse\":\"Test Data for body........save it in cassandra\", \"itemType\":\"UNIT\",\"category\":\"MCQ\",\"version\":2,\"name\": \"MCQ Question 2\",\"type\": \"mcq\",\"num_answers\": 1,\"template\": \"mcq_template_2\",\"template_id\":\"mcq_template_2\",\"qlevel\": \"MEDIUM\",\"owner\": \"username_1\",\"title\": \"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ.  ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\",\"question\": \"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ.ಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\",\"model\": {\"img\": {\"type\": \"image\",\"asset\": \"perimeter\"},\"img2\": {\"type\": \"image\",\"asset\": \"smallSquare\"},\"subtext\": \"( = 1  ಚದರ ಸೆಂ.ಮೀ)\"},\"options\": [{\"value\": {\"type\": \"text\",\"asset\": \"12&10\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"14&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"16&8\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"12&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"},\"score\": 1}],\"max_score\": 1,\"partial_scoring\": false,\"feedback\": \"\"}}}}";
		String path = basePath + "/update/" + assessmentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Create AssessmentItem with valid data for gradeLevel (gradeLevel=class
	 * 1). Expected: 200 - OK. Record Should be created with given data.
	 */
	@Test
	public void testItem_15() throws Exception {
		String request = "{\"request\": {\"assessment_item\": {\"identifier\": \"LP_UTEST_10\",\"objectType\": \"AssessmentItem\",\"metadata\": {\"code\": \"test.qtt02\",\"framework\":\"NCFTEST\",\"gradeLevel\":[\"class 1\"],\"body\":\"Test Data for body........save it in cassandra\", \"itemType\":\"UNIT\",\"category\":\"MCQ\",\"version\":2,\"name\": \"MCQ Question 2\",\"type\": \"mcq\",\"num_answers\": 1,\"template\": \"mcq_template_2\",\"template_id\":\"mcq_template_2\",\"qlevel\": \"MEDIUM\",\"owner\": \"username_1\",\"title\": \"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ.  ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\",\"question\": \"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ.ಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\",\"model\": {\"img\": {\"type\": \"image\",\"asset\": \"perimeter\"},\"img2\": {\"type\": \"image\",\"asset\": \"smallSquare\"},\"subtext\": \"( = 1  ಚದರ ಸೆಂ.ಮೀ)\"},\"options\": [{\"value\": {\"type\": \"text\",\"asset\": \"12&10\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"14&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"16&8\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"12&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"},\"score\": 1}],\"max_score\": 1,\"partial_scoring\": false,\"feedback\": \"\"}}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		path = basePath + "/read/LP_UTEST_10";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		List<String> gradeLevel = (List<String>) ((Map<String, Object>) resp.getResult().get("assessment_item"))
				.get("gradeLevel");
		assertEquals("class 1", (String) gradeLevel.get(0));
	}

	/*
	 * Create AssessmentItem with Invalid data for gradeLevel
	 * (gradeLevel=grade1). Expected: 400 - CLIENT_ERROR. Record Should not be
	 * created with given data.
	 */
	@Test
	public void testItem_16() throws Exception {
		String request = "{\"request\": {\"assessment_item\": {\"identifier\": \"LP_UTEST_11\",\"objectType\": \"AssessmentItem\",\"metadata\": {\"code\": \"test.qtt02\",\"framework\":\"NCFTEST\",\"gradeLevel\":[\"grade1\"],\"body\":\"Test Data for body........save it in cassandra\", \"itemType\":\"UNIT\",\"category\":\"MCQ\",\"version\":2,\"name\": \"MCQ Question 2\",\"type\": \"mcq\",\"num_answers\": 1,\"template\": \"mcq_template_2\",\"template_id\":\"mcq_template_2\",\"qlevel\": \"MEDIUM\",\"owner\": \"username_1\",\"title\": \"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ.  ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\",\"question\": \"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ.ಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\",\"model\": {\"img\": {\"type\": \"image\",\"asset\": \"perimeter\"},\"img2\": {\"type\": \"image\",\"asset\": \"smallSquare\"},\"subtext\": \"( = 1  ಚದರ ಸೆಂ.ಮೀ)\"},\"options\": [{\"value\": {\"type\": \"text\",\"asset\": \"12&10\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"14&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"16&8\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"12&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"},\"score\": 1}],\"max_score\": 1,\"partial_scoring\": false,\"feedback\": \"\"}}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Update AssessmentItem with valid data for gradeLevel (gradeLevel=class
	 * 1). Expected: 200 - OK. Record Should be updated with given data.
	 */
	@SuppressWarnings("unchecked")
	@Test
	@Ignore
	public void testItem_17() throws Exception {
		String request = "{\"request\":{\"assessment_item\":{\"identifier\":\"" + assessmentId + "+\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"createdBy\":\"874ed8a5-782e-4f6c-8f36-e0288455901e\",\"creator\":\"Creation\",\"organisation\":[\"DLF Public School (Rajinder Nagar)\"],\"code\":\"124bd88e-0b38-0ba2-3719-f5c99e6ba38d\",\"type\":\"reference\",\"category\":\"VSA\",\"itemType\":\"UNIT\",\"version\":3,\"name\":\"vsa_NCFCOPY\",\"body\":\"<p>Write down in your words the conversation between the bird in the rishi’s ashram and the king.</p>\",\"editorState\":{\"solutions\":[\"<p>When the bird.</p>\"]},\"question\":\"<p>Write down in your words the conversation between the bird in the rishi’s ashram and the king.</p>\",\"solutions\":[\"<p>When the bird.</p>\"],\"learningOutcome\":[],\"bloomsLevel\":[\"Knowledge (Remembering)\"],\"templateId\":\"NA\",\"programId\":\"2bf17140-8124-11e9-bafa-676cba786201\",\"program\":\"CBSE\",\"channel\":\"b00bc992ef25f1a9a8d63291e20efc8d\",\"framework\":\"NCFCOPY\",\"board\":\"CBSE\",\"medium\":\"English\",\"gradeLevel\":[\"Kindergarten\"],\"subject\":\"English\",\"topic\":[\"Topic 1\"],\"status\":\"Review\",\"media\":[],\"qumlVersion\":0.5,\"textBookUnitIdentifier\":\"do_112771678991564800152\",\"authorNames\":\"Creation\"}}}}";
		String path = basePath + "/update/" + assessmentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		path = basePath + "/read/" + assessmentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		List<String> gradeLevel = (List<String>) ((Map<String, Object>) resp.getResult().get("assessment_item"))
				.get("gradeLevel");
		assertEquals("Kindergarten", (String) gradeLevel.get(0));
	}

	/*
	 * Update AssessmentItem with Invalid data for gradeLevel
	 * (gradeLevel=grade-1). Expected: 400 - CLIENT_ERROR. Record Should not be
	 * updated with given data.
	 */
	@Test
	@Ignore
	public void testItem_18() throws Exception {
		String request = "{\"request\": {\"assessment_item\": {\"identifier\": \"" + assessmentId
				+ "\",\"objectType\": \"AssessmentItem\",\"metadata\": {\"code\": \"test.qtt02\",\"framework\":\"NCFTEST\"\"gradeLevel\":[\"grade-1\"],\"icse\":\"Test Data for body........save it in cassandra\", \"itemType\":\"UNIT\",\"category\":\"MCQ\",\"version\":2,\"name\": \"MCQ Question 2\",\"type\": \"mcq\",\"num_answers\": 1,\"template\": \"mcq_template_2\",\"template_id\":\"mcq_template_2\",\"qlevel\": \"MEDIUM\",\"owner\": \"username_1\",\"title\": \"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ.  ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\",\"question\": \"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ.ಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\",\"model\": {\"img\": {\"type\": \"image\",\"asset\": \"perimeter\"},\"img2\": {\"type\": \"image\",\"asset\": \"smallSquare\"},\"subtext\": \"( = 1  ಚದರ ಸೆಂ.ಮೀ)\"},\"options\": [{\"value\": {\"type\": \"text\",\"asset\": \"12&10\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"14&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"16&8\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"12&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"},\"score\": 1}],\"max_score\": 1,\"partial_scoring\": false,\"feedback\": \"\"}}}}";
		String path = basePath + "/update/" + assessmentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Create AssessmentItem with valid data for topic (topic=addition).
	 * Expected: 200 - OK. Record Should be created with given data.
	 */
	@Test
	public void testItem_19() throws Exception {
		String request = "{\"request\": {\"assessment_item\": {\"identifier\": \"LP_UTEST_12\",\"objectType\": \"AssessmentItem\",\"metadata\": {\"code\": \"test.qtt02\",\"framework\":\"NCFTEST\",\"topic\":[\"addition\"],\"body\":\"Test Data for body........save it in cassandra\", \"itemType\":\"UNIT\",\"category\":\"MCQ\",\"version\":2,\"name\": \"MCQ Question 2\",\"type\": \"mcq\",\"num_answers\": 1,\"template\": \"mcq_template_2\",\"template_id\":\"mcq_template_2\",\"qlevel\": \"MEDIUM\",\"owner\": \"username_1\",\"title\": \"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ.  ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\",\"question\": \"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ.ಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\",\"model\": {\"img\": {\"type\": \"image\",\"asset\": \"perimeter\"},\"img2\": {\"type\": \"image\",\"asset\": \"smallSquare\"},\"subtext\": \"( = 1  ಚದರ ಸೆಂ.ಮೀ)\"},\"options\": [{\"value\": {\"type\": \"text\",\"asset\": \"12&10\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"14&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"16&8\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"12&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"},\"score\": 1}],\"max_score\": 1,\"partial_scoring\": false,\"feedback\": \"\"}}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		path = basePath + "/read/LP_UTEST_12";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		List<String> topic = (List<String>) ((Map<String, Object>) resp.getResult().get("assessment_item"))
				.get("topic");
		assertEquals("addition", (String) topic.get(0));
	}

	/*
	 * Create AssessmentItem with Invalid data for topic (topic=subtraction).
	 * Expected: 400 - CLIENT_ERROR. Record Should not be created with given
	 * data.
	 */
	@Test
	public void testItem_20() throws Exception {
		String request = "{\"request\": {\"assessment_item\": {\"identifier\": \"LP_UTEST_13\",\"objectType\": \"AssessmentItem\",\"metadata\": {\"code\": \"test.qtt02\",\"framework\":\"NCFTEST\",\"topic\":[\"subtraction\"],\"body\":\"Test Data for body........save it in cassandra\", \"itemType\":\"UNIT\",\"category\":\"MCQ\",\"version\":2,\"name\": \"MCQ Question 2\",\"type\": \"mcq\",\"num_answers\": 1,\"template\": \"mcq_template_2\",\"template_id\":\"mcq_template_2\",\"qlevel\": \"MEDIUM\",\"owner\": \"username_1\",\"title\": \"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ.  ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\",\"question\": \"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ.ಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\",\"model\": {\"img\": {\"type\": \"image\",\"asset\": \"perimeter\"},\"img2\": {\"type\": \"image\",\"asset\": \"smallSquare\"},\"subtext\": \"( = 1  ಚದರ ಸೆಂ.ಮೀ)\"},\"options\": [{\"value\": {\"type\": \"text\",\"asset\": \"12&10\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"14&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"16&8\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"12&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"},\"score\": 1}],\"max_score\": 1,\"partial_scoring\": false,\"feedback\": \"\"}}}}";
		String path = basePath + "/create";
		actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

	/*
	 * Update AssessmentItem with valid data for topic (topic=addition).
	 * Expected: 200 - OK. Record Should be updated with given data.
	 */
	@Test
	@Ignore
	public void testItem_21() throws Exception {
		String request = "{\"request\":{\"assessment_item\":{\"identifier\":\"" + assessmentId + "+\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"createdBy\":\"874ed8a5-782e-4f6c-8f36-e0288455901e\",\"creator\":\"Creation\",\"organisation\":[\"DLF Public School (Rajinder Nagar)\"],\"code\":\"124bd88e-0b38-0ba2-3719-f5c99e6ba38d\",\"type\":\"reference\",\"category\":\"VSA\",\"itemType\":\"UNIT\",\"version\":3,\"name\":\"vsa_NCFCOPY\",\"body\":\"<p>Write down in your words the conversation between the bird in the rishi’s ashram and the king.</p>\",\"editorState\":{\"solutions\":[\"<p>When the bird.</p>\"]},\"question\":\"<p>Write down in your words the conversation between the bird in the rishi’s ashram and the king.</p>\",\"solutions\":[\"<p>When the bird.</p>\"],\"learningOutcome\":[],\"bloomsLevel\":[\"Knowledge (Remembering)\"],\"templateId\":\"NA\",\"programId\":\"2bf17140-8124-11e9-bafa-676cba786201\",\"program\":\"CBSE\",\"channel\":\"b00bc992ef25f1a9a8d63291e20efc8d\",\"framework\":\"NCFCOPY\",\"board\":\"CBSE\",\"medium\":\"English\",\"gradeLevel\":[\"Kindergarten\"],\"subject\":\"English\",\"topic\":[\"Topic 1\"],\"status\":\"Review\",\"media\":[],\"qumlVersion\":0.5,\"textBookUnitIdentifier\":\"do_112771678991564800152\",\"authorNames\":\"Creation\"}}}}";
		String path = basePath + "/update/" + assessmentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		path = basePath + "/read/" + assessmentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep"));
		assertEquals(200, actions.andReturn().getResponse().getStatus());
		Response resp = getResponse(actions);
		List<String> topic = (List<String>) ((Map<String, Object>) resp.getResult().get("assessment_item"))
				.get("topic");
		assertEquals("Topic 1", (String) topic.get(0));
	}

	/*
	 * Update AssessmentItem with Invalid data for topic (topic=subtraction).
	 * Expected: 400 - CLIENT_ERROR. Record Should not be updated with given
	 * data.
	 */
	@Ignore
	@Test
	public void testItem_22() throws Exception {
		String request = "{\"request\": {\"assessment_item\": {\"identifier\": \"" + assessmentId
				+ "\",\"objectType\": \"AssessmentItem\",\"metadata\": {\"code\": \"test.qtt02\",\"framework\":\"NCFTEST\"\"topic\":[\"subtraction\"],\"icse\":\"Test Data for body........save it in cassandra\", \"itemType\":\"UNIT\",\"category\":\"MCQ\",\"version\":2,\"name\": \"MCQ Question 2\",\"type\": \"mcq\",\"num_answers\": 1,\"template\": \"mcq_template_2\",\"template_id\":\"mcq_template_2\",\"qlevel\": \"MEDIUM\",\"owner\": \"username_1\",\"title\": \"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ.  ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\",\"question\": \"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ.ಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\",\"model\": {\"img\": {\"type\": \"image\",\"asset\": \"perimeter\"},\"img2\": {\"type\": \"image\",\"asset\": \"smallSquare\"},\"subtext\": \"( = 1  ಚದರ ಸೆಂ.ಮೀ)\"},\"options\": [{\"value\": {\"type\": \"text\",\"asset\": \"12&10\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"14&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"16&8\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\",\"asset\": \"12&7\",\"font\": \"Verdana\",\"color\": \"white\",\"fontsize\": \"240\"},\"score\": 1}],\"max_score\": 1,\"partial_scoring\": false,\"feedback\": \"\"}}}}";
		String path = basePath + "/update/" + assessmentId;
		actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).contentType(MediaType.APPLICATION_JSON)
				.header("X-Channel-Id", "in.ekstep").content(request));
		Assert.assertEquals(400, actions.andReturn().getResponse().getStatus());
	}

}
