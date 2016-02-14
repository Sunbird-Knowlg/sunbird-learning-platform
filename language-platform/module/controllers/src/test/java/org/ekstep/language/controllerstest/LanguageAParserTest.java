package org.ekstep.language.controllerstest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.language.router.LanguageRequestRouterPool;
import org.ekstep.language.util.ElasticSearchUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
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

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.taxonomy.mgr.impl.TaxonomyManagerImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class LanguageAParserTest extends BaseManager {

	@Autowired
	private WebApplicationContext context;
	private static TaxonomyManagerImpl taxonomyManager = new TaxonomyManagerImpl();
	private ResultActions actions;
	static ElasticSearchUtil util;
	private static String TEST_LANGUAGE_NEW = "testparser";
	private static Logger LOGGER = LogManager.getLogger(LanguageAParserTest.class.getName());

	static {
		LanguageRequestRouterPool.init();
	}

	@BeforeClass
	public static void init() throws Exception {
		// Definitions
		deleteDefinitionStatic(TEST_LANGUAGE_NEW);
		createDefinitionsStatic(TEST_LANGUAGE_NEW);
		// createData();
	}

	@AfterClass
	public static void close() throws IOException, InterruptedException {
		deleteDefinitionStatic(TEST_LANGUAGE_NEW);
	}

	
	private void createData() throws JsonParseException, JsonMappingException, IOException {
		Map<String, Object> metaData = new HashMap<String, Object>();
		Node word = new Node("w_1", SystemNodeTypes.DATA_NODE.name(), "Word");
		metaData.put("lemma", "wordOne");
		word.setMetadata(metaData);
		Response res = null;
		Request req = getRequest(TEST_LANGUAGE_NEW, GraphEngineManagers.NODE_MANAGER, "createDataNode");
		req.put(GraphDACParams.node.name(), word);
		res = getResponse(req, LOGGER);
		if (!res.getParams().getStatus().equalsIgnoreCase("successful")) {
			LOGGER.error(res.getParams().getErr() + res.getParams().getErrmsg());
			System.out.println(res.getParams().getErr() + res.getParams().getErrmsg());
		}
		Assert.assertEquals("successful", res.getParams().getStatus());

		word = new Node("w_2", SystemNodeTypes.DATA_NODE.name(), "Word");
		metaData = new HashMap<String, Object>();
		metaData.put("lemma", "wordTwo");
		word.setMetadata(metaData);
		res = null;
		req = getRequest(TEST_LANGUAGE_NEW, GraphEngineManagers.NODE_MANAGER, "createDataNode");
		req.put(GraphDACParams.node.name(), word);
		res = getResponse(req, LOGGER);
		Assert.assertEquals("successful", res.getParams().getStatus());

		word = new Node("w_3", SystemNodeTypes.DATA_NODE.name(), "Word");
		metaData = new HashMap<String, Object>();
		metaData.put("lemma", "wordThree");
		word.setMetadata(metaData);
		res = null;
		req = getRequest(TEST_LANGUAGE_NEW, GraphEngineManagers.NODE_MANAGER, "createDataNode");
		req.put(GraphDACParams.node.name(), word);
		res = getResponse(req, LOGGER);
		Assert.assertEquals("successful", res.getParams().getStatus());

		Node synset = new Node("s_3", SystemNodeTypes.DATA_NODE.name(), "Synset");
		res = null;
		req = getRequest(TEST_LANGUAGE_NEW, GraphEngineManagers.NODE_MANAGER, "createDataNode");
		req.put(GraphDACParams.node.name(), synset);
		res = getResponse(req, LOGGER);
		Assert.assertEquals("successful", res.getParams().getStatus());

		synset = new Node("s_1", SystemNodeTypes.DATA_NODE.name(), "Synset");
		List<Relation> outRels = new ArrayList<Relation>();
		Relation outRel = new Relation(null, RelationTypes.SYNONYM.relationName(), "w_1");
		outRels.add(outRel);
		outRel = new Relation(null, RelationTypes.SYNONYM.relationName(), "w_2");
		outRels.add(outRel);
		outRel = new Relation(null, RelationTypes.HYPERNYM.relationName(), "s_3");
		outRels.add(outRel);
		synset.setOutRelations(outRels);
		res = null;
		req = getRequest(TEST_LANGUAGE_NEW, GraphEngineManagers.NODE_MANAGER, "createDataNode");
		req.put(GraphDACParams.node.name(), synset);
		res = getResponse(req, LOGGER);
		Assert.assertEquals("successful", res.getParams().getStatus());

		synset = new Node("s_2", SystemNodeTypes.DATA_NODE.name(), "Synset");
		outRels = new ArrayList<Relation>();
		outRel = new Relation(null, RelationTypes.SYNONYM.relationName(), "w_3");
		outRels.add(outRel);
		outRel = new Relation(null, RelationTypes.HYPERNYM.relationName(), "s_3");
		outRels.add(outRel);
		synset.setOutRelations(outRels);
		res = null;
		req = getRequest(TEST_LANGUAGE_NEW, GraphEngineManagers.NODE_MANAGER, "createDataNode");
		req.put(GraphDACParams.node.name(), synset);
		res = getResponse(req, LOGGER);
		Assert.assertEquals("successful", res.getParams().getStatus());
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void parserTest() throws JsonParseException, JsonMappingException, IOException {
		createData();
		String contentString = "{\"request\":{\"language_id\":\"testparser\",\"wordSuggestions\":true,\"relatedWords\":true,\"translations\":true,\"equivalentWords\":true,\"limit\":10,\"content\":\"wordOne\"}}";
		String word = "wordOne";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/parser";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
					.content(contentString.getBytes()).header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Response response = jsonToObject(actions);
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		Map<String, Object> wordMap = (Map<String, Object>) result.get(word);
		List<String> equivalentWords = (List<String>) wordMap.get("equivalentWords");
		Assert.assertTrue(equivalentWords.contains("wordTwo"));
		List<String> relatedWords = (List<String>) wordMap.get("relatedWords");
		Assert.assertTrue(relatedWords.contains("wordTwo"));
		Assert.assertTrue(relatedWords.contains("wordThree"));
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void parserTeluguTest() throws JsonParseException, JsonMappingException, IOException {
		String contentString = "{\"request\":{\"language_id\":\"te\",\"wordSuggestions\":true,\"relatedWords\":true,\"translations\":true,\"equivalentWords\":true,\"limit\":10,\"content\":\"wordOne\"}}";
		String word = "wordOne";
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/language/parser";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON)
					.content(contentString.getBytes()).header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response response = jsonToObject(actions);
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		Map<String, Object> wordMap = (Map<String, Object>) result.get(word);
		List<String> equivalentWords = (List<String>) wordMap.get("equivalentWords");
		Assert.assertTrue(equivalentWords.contains("తండ్రి"));
		Assert.assertTrue(equivalentWords.contains("ఇనుము"));
		Assert.assertTrue(equivalentWords.contains("సంఘర్షణ"));
		List<String> relatedWords = (List<String>) wordMap.get("relatedWords");
		Assert.assertTrue(relatedWords.contains("తండ్రి"));
		Assert.assertTrue(relatedWords.contains("ఇనుము"));
		Assert.assertTrue(relatedWords.contains("సంఘర్షణ"));
		List<String> suggestions = (List<String>) wordMap.get("suggestions");
		Assert.assertTrue(suggestions.contains("తండ్రి"));
		Assert.assertTrue(suggestions.contains("ఇనుము"));
		Assert.assertTrue(suggestions.contains("సంఘర్షణ"));

	}

	public static void createDefinitionsStatic(String language) {
		String contentString = "{  \"definitionNodes\": [    {      \"objectType\": \"Word\",      \"properties\": [        {          \"propertyName\": \"lemma\",          \"title\": \"Lemma\",          \"description\": \"\",          \"category\": \"general\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": true,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 3 }\"        },        {          \"propertyName\": \"sources\",          \"title\": \"Sources\",          \"description\": \"\",          \"category\": \"general\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text', 'order': 2 }\"        },        {          \"propertyName\": \"sourceTypes\",          \"title\": \"Source Types\",          \"description\": \"\",          \"category\": \"general\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text', 'order': 2 }\"        },        {          \"propertyName\": \"commisionedBy\",          \"title\": \"Commisioned By\",          \"description\": \"\",          \"category\": \"general\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text', 'order': 5 }\"        },        {          \"propertyName\": \"defaultSynset\",          \"title\": \"Default Synset\",          \"description\": \"\",          \"category\": \"general\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text', 'order': 1 }\"        },        {          \"propertyName\": \"syllableCount\",          \"title\": \"Syllable Count\",          \"description\": \"\",          \"category\": \"lexile\",          \"dataType\": \"Number\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text', 'order': 4 }\"        },        {          \"propertyName\": \"syllableNotation\",          \"title\": \"Syllable Notation\",          \"description\": \"\",          \"category\": \"lexile\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'textarea', 'order': 4 }\"        },        {          \"propertyName\": \"unicodeNotation\",          \"title\": \"Unicode Notation\",          \"description\": \"\",          \"category\": \"lexile\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'textarea', 'order': 4 }\"        },        {          \"propertyName\": \"rtsNotation\",          \"title\": \"RTS Notation\",          \"description\": \"\",          \"category\": \"lexile\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'textarea', 'order': 4 }\"        },        {          \"propertyName\": \"vectorsRepresentation\",          \"title\": \"Vectors representation\",          \"description\": \"\",          \"category\": \"lexile\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'select',  'order': 9 }\"        },        {          \"propertyName\": \"orthographicFeatures\",          \"title\": \"Orthographic Features\",          \"description\": \"\",          \"category\": \"lexile\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"\"        },        {          \"propertyName\": \"orthographic_complexity\",          \"title\": \"Orthographic Complexity\",          \"description\": \"\",          \"category\": \"lexile\",          \"dataType\": \"Number\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'textarea',  'order': 4 }\"        },        {          \"propertyName\": \"phonologic_complexity\",          \"title\": \"Phonological Complexity\",          \"description\": \"\",          \"category\": \"lexile\",          \"dataType\": \"Number\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'textarea',  'order': 4 }\"        },        {          \"propertyName\": \"pos\",          \"title\": \"POS (Parts of Speech)\",          \"description\": \"\",          \"category\": \"grammar\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'textarea', 'order': 19 }\"        },        {          \"propertyName\": \"grade\",          \"title\": \"Grade\",          \"description\": \"\",          \"category\": \"grammar\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'textarea', 'order': 19 }\"        },        {          \"propertyName\": \"morphology\",          \"title\": \"Morphology\",          \"description\": \"\",          \"category\": \"grammar\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 4 }\"        },        {          \"propertyName\": \"parts\",          \"title\": \"Parts\",          \"description\": \"\",          \"category\": \"grammar\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 4 }\"        },        {          \"propertyName\": \"affixes\",          \"title\": \"Affixes\",          \"description\": \"\",          \"category\": \"grammar\",          \"dataType\": \"List\",          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'textarea', 'order': 14 }\"        },        {          \"propertyName\": \"namedEntityType\",          \"title\": \"Named Entity Type\",          \"description\": \"\",          \"category\": \"grammar\",          \"dataType\": \"List\",          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'select', 'order': 14 }\"        },        {          \"propertyName\": \"loanWordSourceLanguage\",          \"title\": \"Loan Word Source Language\",          \"description\": \"\",          \"category\": \"grammar\",          \"dataType\": \"Select\",          \"range\": [            \"hi\",            \"en\",            \"ka\",            \"te\"          ],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'select',  'order': 14 }\"        },        {          \"propertyName\": \"ageBand\",          \"title\": \"Age Band\",          \"description\": \"\",          \"category\": \"pedagogy\",          \"dataType\": \"Select\",          \"range\": [            \"1-5\",            \"6-10\",            \"11-15\",            \"16-20\"          ],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'select', 'order': 14 }\"        },        {          \"propertyName\": \"microConcepts\",          \"title\": \"Micro Concepts\",          \"description\": \"\",          \"category\": \"pedagogy\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'teaxtarea', 'order': 14 }\"        },        {          \"propertyName\": \"difficultyLevel\",          \"title\": \"Difficulty Level\",          \"description\": \"\",          \"category\": \"pedagogy\",          \"dataType\": \"Select\",          \"range\": [            \"Easy\",            \"Medium\",            \"Difficult\"          ],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'select', 'order': 4 }\"        },        {          \"propertyName\": \"occurrenceCount\",          \"title\": \"Occurrence Count\",          \"description\": \"\",          \"category\": \"frequency\",          \"dataType\": \"Number\",          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'textarea', 'order': 4 }\"        },        {          \"propertyName\": \"senseSetCount\",          \"title\": \"Sense Set Count\",          \"description\": \"\",          \"category\": \"frequency\",          \"dataType\": \"Number\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'Number', 'order': 4 }\"        },        {          \"propertyName\": \"posCount\",          \"title\": \"Parts of the Speech Count\",          \"description\": \"\",          \"category\": \"frequency\",          \"dataType\": \"Number\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'number', 'order': 4 }\"        },        {          \"propertyName\": \"userSets\",          \"title\": \"User Sets\",          \"description\": \"\",          \"category\": \"frequency\",          \"dataType\": \"Number\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'number', 'order': 4 }\"        },        {          \"propertyName\": \"sampleUsages\",          \"title\": \"Sample Usages\",          \"description\": \"\",          \"category\": \"sampleData\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"\"        },        {          \"propertyName\": \"audio\",          \"title\": \"Audio\",          \"description\": \"\",          \"category\": \"sampleData\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': '',  'order': 9 }\"        },        {          \"propertyName\": \"pictures\",          \"title\": \"Pictures\",          \"description\": \"\",          \"category\": \"sampleData\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': '',  'order': 14 }\"        },        {          \"propertyName\": \"pronunciations\",          \"title\": \"Pronunciations\",          \"description\": \"\",          \"category\": \"sampleData\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': '',  'order': 14 }\"        },        {          \"propertyName\": \"reviewers\",          \"title\": \"Reviewers\",          \"description\": \"\",          \"category\": \"audit\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text',  'order': 14 }\"        },        {          \"propertyName\": \"lastUpdatedBy\",          \"title\": \"Last Updated By\",          \"description\": \"\",          \"category\": \"audit\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text',  'order': 14 }\"        },        {          \"propertyName\": \"lastUpdatedOn\",          \"title\": \"Last Updated On\",          \"description\": \"\",          \"category\": \"audit\",          \"dataType\": \"Date\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 22 }\"        },        {            \"propertyName\": \"status\",            \"title\": \"Status\",            \"description\": \"Status of the domain\",            \"category\": \"audit\",            \"dataType\": \"Select\",            \"range\":            [                \"Draft\",                \"Live\",                \"Review\",                \"Retired\"            ],            \"required\": false,			\"indexed\": true,            \"displayProperty\": \"Editable\",            \"defaultValue\": \"Live\",            \"renderingHints\": \"{'inputType': 'select', 'order': 23}\"        },        {          \"propertyName\": \"source\",          \"title\": \"Source\",          \"description\": \"\",          \"category\": \"conflicts\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        },        {          \"propertyName\": \"conflictStatus\",          \"title\": \"Conflict Status\",          \"description\": \"\",          \"category\": \"conflicts\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        },        {          \"propertyName\": \"relevancy\",          \"title\": \"Relevancy\",          \"description\": \"\",          \"category\": \"analytics\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        },        {          \"propertyName\": \"complexity\",          \"title\": \"Complexity\",          \"description\": \"\",          \"category\": \"analytics\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        },        {          \"propertyName\": \"possibleSpellings\",          \"title\": \"Possible Spellings\",          \"description\": \"\",          \"category\": \"supportability\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        },        {          \"propertyName\": \"allowedSuffixes\",          \"title\": \"Allowed Suffixes\",          \"description\": \"\",          \"category\": \"supportability\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        },        {          \"propertyName\": \"allowedPrefixes\",          \"title\": \"Allowed Prefixes\",          \"description\": \"\",          \"category\": \"supportability\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        },        {          \"propertyName\": \"allowedInfixes\",          \"title\": \"Allowed Infixes\",          \"description\": \"\",          \"category\": \"supportability\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        },        {          \"propertyName\": \"tenseForms\",          \"title\": \"Tense Forms\",          \"description\": \"\",          \"category\": \"supportability\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        },        {          \"propertyName\": \"pluralForms\",          \"title\": \"Plural Forms\",          \"description\": \"\",          \"category\": \"supportability\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        },        {          \"propertyName\": \"singularForms\",          \"title\": \"Singular Forms\",          \"description\": \"\",          \"category\": \"supportability\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        },        {          \"propertyName\": \"genders\",          \"title\": \"Genders\",          \"description\": \"\",          \"category\": \"supportability\",          \"dataType\": \"List\",          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'select', 'order': 25 }\"        },        {          \"propertyName\": \"pronouns\",          \"title\": \"Pronouns\",          \"description\": \"\",          \"category\": \"supportability\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        }      ],      \"inRelations\": [        {          \"relationName\": \"synonym\",          \"title\": \"synonyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Synset\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        }      ],      \"outRelations\": [        {          \"relationName\": \"hasAntonym\",          \"title\": \"antonyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Word\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        },        {          \"relationName\": \"hasHypernym\",          \"title\": \"hypernyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Word\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        },        {          \"relationName\": \"hasHolonym\",          \"title\": \"holonyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Word\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        },        {          \"relationName\": \"hasHyponym\",          \"title\": \"hyponyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Word\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        },        {          \"relationName\": \"hasMeronym\",          \"title\": \"meronyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Word\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        }      ],      \"systemTags\": [        {          \"name\": \"Review Tags\",          \"description\": \"Need to Review this Word.\"        },        {          \"name\": \"Missing Information\",          \"description\": \"Some the information is missing.\"        },        {          \"name\": \"Incorrect Data\",          \"description\": \"Wrong information about this word.\"        },        {          \"name\": \"Spelling Mistakes\",          \"description\": \"Incorrect Spellings\"        }      ],      \"metadata\": {        \"ttl\": 24,        \"limit\": 50      }    }  ]}";
		Response resp = taxonomyManager.updateDefinition(language, contentString);
		if (!resp.getParams().getStatus().equalsIgnoreCase("successful")) {
			LOGGER.error(resp.getParams().getErr() + resp.getParams().getErrmsg());
			System.out.println(resp.getParams().getErr() + resp.getParams().getErrmsg());
		}
		Assert.assertEquals("successful", resp.getParams().getStatus());
		contentString = "{  \"definitionNodes\": [    {      \"objectType\": \"Synset\",      \"properties\": [        {          \"propertyName\": \"gloss\",          \"title\": \"Gloss\",          \"description\": \"\",          \"category\": \"general\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 3 }\"        },        {          \"propertyName\": \"glossInEnglish\",          \"title\": \"Gloss in English\",          \"description\": \"\",          \"category\": \"general\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text', 'order': 5 }\"        },        {          \"propertyName\": \"exampleSentences\",          \"title\": \"Example Sentences\",          \"description\": \"\",          \"category\": \"general\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text', 'order': 1 }\"        },        {          \"propertyName\": \"frames\",          \"title\": \"Sentence Frames\",          \"description\": \"\",          \"category\": \"general\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text', 'order': 1 }\"        },        {          \"propertyName\": \"pos\",          \"title\": \"POS\",          \"description\": \"\",          \"category\": \"grammar\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'textarea', 'order': 19 }\"        },        {          \"propertyName\": \"namedEntityType\",          \"title\": \"Named Entity Type\",          \"description\": \"\",          \"category\": \"grammar\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 4 }\"        },        {          \"propertyName\": \"pictures\",          \"title\": \"Pictures\",          \"description\": \"URL\",          \"category\": \"sampleData\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"\"        },        {          \"propertyName\": \"Audio\",          \"title\": \"audio\",          \"description\": \"URL\",          \"category\": \"sampleData\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'teaxtarea',  'order': 9 }\"        },        {          \"propertyName\": \"reviewers\",          \"title\": \"Reviewers\",          \"description\": \"\",          \"category\": \"audit\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text',  'order': 14 }\"        },        {          \"propertyName\": \"lastUpdatedBy\",          \"title\": \"Last Updated By\",          \"description\": \"\",          \"category\": \"audit\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text',  'order': 14 }\"        },        {          \"propertyName\": \"lastUpdatedOn\",          \"title\": \"Last Updated On\",          \"description\": \"\",          \"category\": \"audit\",          \"dataType\": \"Date\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 22 }\"        },        {            \"propertyName\": \"status\",            \"title\": \"Status\",            \"description\": \"Status of the domain\",            \"category\": \"audit\",            \"dataType\": \"Select\",            \"range\":            [                \"Draft\",                \"Live\",                \"Retired\"            ],            \"required\": false,			\"indexed\": true,            \"displayProperty\": \"Editable\",            \"defaultValue\": \"Draft\",            \"renderingHints\": \"{'inputType': 'select', 'order': 23}\"        },        {          \"propertyName\": \"source\",          \"title\": \"Source\",          \"description\": \"\",          \"category\": \"conflicts\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        },        {          \"propertyName\": \"conflictStatus\",          \"title\": \"Conflict Status\",          \"description\": \"\",          \"category\": \"conflicts\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        }      ],      \"inRelations\": [],      \"outRelations\": [        {          \"relationName\": \"synonym\",          \"title\": \"Synonyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Word\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        },        {          \"relationName\": \"hasAntonym\",          \"title\": \"Antonyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Synset\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        },        {          \"relationName\": \"hasHypernym\",          \"title\": \"Hypernyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Synset\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        },        {          \"relationName\": \"hasHolonym\",          \"title\": \"Holonyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Synset\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        },        {          \"relationName\": \"hasHyponym\",          \"title\": \"Hyponyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Synset\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        },        {          \"relationName\": \"hasMeronym\",          \"title\": \"Meronyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Synset\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        }      ],      \"systemTags\": [        {          \"name\": \"Review Tags\",          \"description\": \"Need to Review this Synset.\"        },        {          \"name\": \"Missing Information\",          \"description\": \"Some the information is missing.\"        },        {          \"name\": \"Incorrect Data\",          \"description\": \"Wrong information about this Synset.\"        }      ],      \"metadata\": {        \"ttl\": 24,        \"limit\": 50      }    }  ]}";
		resp = taxonomyManager.updateDefinition(language, contentString);
		if (!resp.getParams().getStatus().equalsIgnoreCase("successful")) {
			LOGGER.error(resp.getParams().getErr() + resp.getParams().getErrmsg());
			System.out.println(resp.getParams().getErr() + resp.getParams().getErrmsg());
		}
		Assert.assertEquals("successful", resp.getParams().getStatus());
	}

	public static void deleteDefinitionStatic(String language) {
		taxonomyManager.delete(language);
	}

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
