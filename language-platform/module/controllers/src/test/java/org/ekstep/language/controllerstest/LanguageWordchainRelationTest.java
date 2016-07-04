package org.ekstep.language.controllerstest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.language.mgr.impl.DictionaryManagerImpl;
import org.ekstep.language.router.LanguageRequestRouterPool;
import org.ekstep.language.test.util.RequestResponseTestHelper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.util.Neo4jGraphFactory;
import com.ilimi.taxonomy.mgr.impl.TaxonomyManagerImpl;


public class LanguageWordchainRelationTest {

	private static DictionaryManagerImpl dictionaryManager = new DictionaryManagerImpl();
	private static TaxonomyManagerImpl taxonomyManager = new TaxonomyManagerImpl();
	private static ObjectMapper mapper = new ObjectMapper();
	private static String TEST_LANGUAGE1 = "enTest";
	private static String TEST_LANGUAGE2 = "kaTest";
	
	private List<String> wordIds =  Arrays.asList(new String[]{ "word_test1", "word_test2", "word_test3"});

	static {
		LanguageRequestRouterPool.init();
	}

	@BeforeClass
	public static void init() throws Exception {
		createGraph();
		createDefinition();
		createWord();
		Thread.sleep(5000);
	}

	@AfterClass
	public static void close() throws IOException, InterruptedException {
		deleteGraph();
	}

	@Test
	public void getEnglishWordBoundaryRelationTest() throws Exception {
		Node node = dictionaryManager.getDataNode(TEST_LANGUAGE1, wordIds.get(0) , "Word");
		List<Relation> outRelation = node.getOutRelations();
		Iterator<Relation> rItr =outRelation.iterator();
		while(rItr.hasNext()){
			Relation rel = rItr.next();
			Assert.assertTrue(rel.getRelationType().equalsIgnoreCase(RelationTypes.STARTS_WITH_AKSHARA.relationName()) ||
					rel.getRelationType().equalsIgnoreCase(RelationTypes.ENDS_WITH_AKSHARA.relationName()) ||
					rel.getRelationType().equalsIgnoreCase(RelationTypes.RYMING_SOUNDS.relationName()));
		}
	}

	@Test
	public void getKannadaWordBoundaryRelationTest() throws Exception {
		Node node = dictionaryManager.getDataNode(TEST_LANGUAGE2, wordIds.get(1) , "Word");
		List<Relation> outRelation = node.getOutRelations();
		Iterator<Relation> rItr =outRelation.iterator();
		String naTextId = "";
		while(rItr.hasNext()){
			Relation rel = rItr.next();
			Assert.assertTrue(rel.getRelationType().equalsIgnoreCase(RelationTypes.STARTS_WITH_AKSHARA.relationName()) ||
					rel.getRelationType().equalsIgnoreCase(RelationTypes.ENDS_WITH_AKSHARA.relationName()) ||
					rel.getRelationType().equalsIgnoreCase(RelationTypes.RYMING_SOUNDS.relationName()));
			if(rel.getRelationType().equalsIgnoreCase(RelationTypes.STARTS_WITH_AKSHARA.relationName())){
				naTextId = rel.getEndNodeId();
			}
		}
		
		node = dictionaryManager.getDataNode(TEST_LANGUAGE2, wordIds.get(2) , "Word");
		outRelation = node.getOutRelations();
		rItr =outRelation.iterator();
		while(rItr.hasNext()){
			Relation rel = rItr.next();
			Assert.assertTrue(rel.getRelationType().equalsIgnoreCase(RelationTypes.STARTS_WITH_AKSHARA.relationName()) ||
					rel.getRelationType().equalsIgnoreCase(RelationTypes.ENDS_WITH_AKSHARA.relationName()) ||
					rel.getRelationType().equalsIgnoreCase(RelationTypes.RYMING_SOUNDS.relationName()));
		}
		
		if(StringUtils.isNotEmpty(naTextId)){
			node = dictionaryManager.getDataNode(TEST_LANGUAGE2, naTextId , "Phonetic_Boundary");
			outRelation = node.getOutRelations();
			rItr =outRelation.iterator();
			while(rItr.hasNext()){
				Relation rel = rItr.next();
				if(rel.getRelationType().equalsIgnoreCase(RelationTypes.STARTS_WITH_AKSHARA.relationName())){
					Assert.assertEquals(rel.getStartNodeId(), wordIds.get(1));
				}
				if(rel.getRelationType().equalsIgnoreCase(RelationTypes.ENDS_WITH_AKSHARA.relationName())){
					Assert.assertEquals(rel.getStartNodeId(), wordIds.get(2));
				}
			}
		}
		
	}
	
	private static void createWord() throws JsonParseException,
			JsonMappingException, IOException {
		String contentString = "{  \"request\": {    \"words\": [      {        \"identifier\": \"word_test1\",        \"lemma\": \"cat\",        \"status\": \"Live\"      }   ]  }}";
		Map<String, Object> map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
		Request request = RequestResponseTestHelper.getRequest(map);
		Response response = dictionaryManager.create(TEST_LANGUAGE1, "Word",
				request);
		Assert.assertEquals("successful", response.getParams().getStatus());		
		
		contentString = "{  \"request\": {    \"words\": [      {        \"identifier\": \"word_test2\",        \"lemma\": \"ನೀರು\",        \"status\": \"Live\"      }   ]  }}";
		map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
		request = RequestResponseTestHelper.getRequest(map);
		response = dictionaryManager.create(TEST_LANGUAGE2, "Word",
				request);
		Assert.assertEquals("successful", response.getParams().getStatus());

		contentString = "{  \"request\": {    \"words\": [      {        \"identifier\": \"word_test3\",        \"lemma\": \"ಮನೆ\",        \"status\": \"Live\"      }   ]  }}";
		map = mapper.readValue(contentString,
				new TypeReference<Map<String, Object>>() {
				});
		request = RequestResponseTestHelper.getRequest(map);
		response = dictionaryManager.create(TEST_LANGUAGE2, "Word",
				request);
		Assert.assertEquals("successful", response.getParams().getStatus());
	}


	public static void createDefinition(){
		createDefinitionsStatic(TEST_LANGUAGE1);
		createDefinitionsStatic(TEST_LANGUAGE2);
	}
	
	public static void createDefinitionsStatic(String language) {
		String contentString = "{ \"definitionNodes\": [ { \"objectType\": \"Word\", \"properties\": [ { \"propertyName\": \"lemma\", \"title\": \"Word\", \"description\": \"\", \"category\": \"general\", \"dataType\": \"Text\", \"range\": [], \"required\": true, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'order': 1 }\" }, { \"propertyName\": \"ekstepWordnet\", \"title\": \"EkStep Wordnet\", \"description\": \"\", \"category\": \"general\", \"dataType\": \"Boolean\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"false\", \"renderingHints\": \"{ 'order': 2 }\" }, { \"propertyName\": \"isPhrase\", \"title\": \"Is Phrase\", \"description\": \"\", \"category\": \"general\", \"dataType\": \"Boolean\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"false\", \"renderingHints\": \"{ 'order': 3 }\" }, { \"propertyName\": \"hasConnotative\", \"title\": \"Has Connotative (non-literal) Meaning\", \"description\": \"\", \"category\": \"general\", \"dataType\": \"Boolean\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'order': 4 }\" }, { \"propertyName\": \"isLoanWord\", \"title\": \"Is Loan Word\", \"description\": \"\", \"category\": \"general\", \"dataType\": \"Boolean\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'order': 5 }\" }, { \"propertyName\": \"orthographic_complexity\", \"title\": \"Orthographic Complexity\", \"description\": \"\", \"category\": \"lexile\", \"dataType\": \"Number\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Readonly\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': 'textarea', 'order': 6 }\" }, { \"propertyName\": \"phonologic_complexity\", \"title\": \"Phonological Complexity\", \"description\": \"\", \"category\": \"lexile\", \"dataType\": \"Number\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Readonly\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': 'textarea', 'order': 7 }\" }, { \"propertyName\": \"sources\", \"title\": \"Sources\", \"description\": \"\", \"category\": \"general\", \"dataType\": \"List\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': 'text', 'order': 8 }\" }, { \"propertyName\": \"grade\", \"title\": \"Grade\", \"description\": \"\", \"category\": \"grammar\", \"dataType\": \"Multi-Select\", \"range\": [ \"1\", \"2\", \"3\", \"4\", \"5\", \"6\", \"7\", \"8\", \"9\", \"10\" ], \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': 'textarea', 'order': 9 }\" }, { \"propertyName\": \"sourceTypes\", \"title\": \"Source Types\", \"description\": \"\", \"category\": \"general\", \"dataType\": \"List\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': 'text', 'order': 10 }\" }, { \"propertyName\": \"primaryMeaningId\", \"title\": \"Primary Meaning\", \"description\": \"\", \"category\": \"general\", \"dataType\": \"Text\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Hidden\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': 'text', 'order': 11 }\" }, { \"propertyName\": \"syllableCount\", \"title\": \"Syllable Count\", \"description\": \"\", \"category\": \"lexile\", \"dataType\": \"Number\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Readonly\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': 'text', 'order': 12 }\" }, { \"propertyName\": \"syllableNotation\", \"title\": \"Syllable Notation\", \"description\": \"\", \"category\": \"lexile\", \"dataType\": \"Text\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Readonly\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': 'textarea', 'order': 13 }\" }, { \"propertyName\": \"unicodeNotation\", \"title\": \"Unicode Notation\", \"description\": \"\", \"category\": \"lexile\", \"dataType\": \"Text\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Readonly\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': 'textarea', 'order': 14 }\" }, { \"propertyName\": \"pos\", \"title\": \"POS (Parts of Speech)\", \"description\": \"\", \"category\": \"grammar\", \"dataType\": \"List\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': 'textarea', 'order': 15 }\" }, { \"propertyName\": \"namedEntityType\", \"title\": \"Named Entity Type\", \"description\": \"\", \"category\": \"grammar\", \"dataType\": \"List\", \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': 'select', 'order': 16 }\" }, { \"propertyName\": \"ageBand\", \"title\": \"Age Band\", \"description\": \"\", \"category\": \"pedagogy\", \"dataType\": \"Select\", \"range\": [ \"1-5\", \"6-10\", \"11-15\", \"16-20\" ], \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': 'select', 'order': 17 }\" }, { \"propertyName\": \"difficultyLevel\", \"title\": \"Difficulty Level\", \"description\": \"\", \"category\": \"pedagogy\", \"dataType\": \"Select\", \"range\": [ \"Easy\", \"Medium\", \"Difficult\" ], \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': 'select', 'order': 18 }\" }, { \"propertyName\": \"occurrenceCount\", \"title\": \"Occurrence Count\", \"description\": \"\", \"category\": \"frequency\", \"dataType\": \"Number\", \"required\": false, \"indexed\": true, \"displayProperty\": \"Readonly\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': 'textarea', 'order': 19 }\" }, { \"propertyName\": \"sampleUsages\", \"title\": \"Sample Usages\", \"description\": \"\", \"category\": \"sampleData\", \"dataType\": \"List\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': '', 'order': 20 }\" }, { \"propertyName\": \"audio\", \"title\": \"Audio\", \"description\": \"\", \"category\": \"sampleData\", \"dataType\": \"List\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': '', 'order': 21 }\" }, { \"propertyName\": \"pictures\", \"title\": \"Pictures\", \"description\": \"\", \"category\": \"sampleData\", \"dataType\": \"List\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': '', 'order': 22 }\" }, { \"propertyName\": \"pronunciations\", \"title\": \"Pronunciations\", \"description\": \"\", \"category\": \"sampleData\", \"dataType\": \"List\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': '', 'order': 23 }\" }, { \"propertyName\": \"reviewers\", \"title\": \"Reviewers\", \"description\": \"\", \"category\": \"audit\", \"dataType\": \"List\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': 'text', 'order': 24 }\" }, { \"propertyName\": \"lastUpdatedBy\", \"title\": \"Last Updated By\", \"description\": \"\", \"category\": \"audit\", \"dataType\": \"List\", \"range\": [], \"required\": false, \"indexed\": false, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': 'text', 'order': 25 }\" }, { \"propertyName\": \"lastUpdatedOn\", \"title\": \"Last Updated On\", \"description\": \"\", \"category\": \"audit\", \"dataType\": \"Date\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Readonly\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'order': 26 }\" }, { \"propertyName\": \"status\", \"title\": \"Status\", \"description\": \"Status of the domain\", \"category\": \"audit\", \"dataType\": \"Select\", \"range\": [ \"Draft\", \"Live\", \"Review\", \"Retired\" ], \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"Draft\", \"renderingHints\": \"{'inputType': 'select', 'order': 27}\" }, { \"propertyName\": \"relevancy\", \"title\": \"Relevancy\", \"description\": \"\", \"category\": \"analytics\", \"dataType\": \"Text\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Readonly\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'order': 28 }\" }, { \"propertyName\": \"complexity\", \"title\": \"Complexity\", \"description\": \"\", \"category\": \"analytics\", \"dataType\": \"Text\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Readonly\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'order': 29 }\" }, { \"propertyName\": \"allowedSuffixes\", \"title\": \"Allowed Suffixes\", \"description\": \"\", \"category\": \"supportability\", \"dataType\": \"List\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'order': 30 }\" }, { \"propertyName\": \"allowedPrefixes\", \"title\": \"Allowed Prefixes\", \"description\": \"\", \"category\": \"supportability\", \"dataType\": \"List\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'order': 31 }\" }, { \"propertyName\": \"allowedInfixes\", \"title\": \"Allowed Infixes\", \"description\": \"\", \"category\": \"supportability\", \"dataType\": \"List\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'order': 32 }\" }, { \"propertyName\": \"tenseForms\", \"title\": \"Tense Forms\", \"description\": \"\", \"category\": \"supportability\", \"dataType\": \"List\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'order': 33 }\" }, { \"propertyName\": \"plurality\", \"title\": \"Plural Forms\", \"description\": \"\", \"category\": \"supportability\", \"dataType\": \"List\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'order': 34 }\" }, { \"propertyName\": \"singularForms\", \"title\": \"Singular Forms\", \"description\": \"\", \"category\": \"supportability\", \"dataType\": \"List\", \"range\": [], \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'order': 35 }\" }, { \"propertyName\": \"variants\", \"title\": \"Morphological Variants\", \"description\": \"\", \"category\": \"supportability\", \"dataType\": \"List\", \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': 'select', 'order': 36 }\" }, { \"propertyName\": \"pos_categories\", \"title\": \"POS Categories\", \"description\": \"\", \"category\": \"supportability\", \"dataType\": \"List\", \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': 'select', 'order': 37 }\" }, { \"propertyName\": \"genders\", \"title\": \"Genders\", \"description\": \"\", \"category\": \"supportability\", \"dataType\": \"List\", \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': 'select', 'order': 38 }\" }, { \"propertyName\": \"person\", \"title\": \"Person\", \"description\": \"\", \"category\": \"supportability\", \"dataType\": \"List\", \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': 'select', 'order': 39 }\" }, { \"propertyName\": \"cases\", \"title\": \"Grammatical Cases\", \"description\": \"\", \"category\": \"supportability\", \"dataType\": \"List\", \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': 'select', 'order': 40 }\" }, { \"propertyName\": \"theme\", \"title\": \"Theme\", \"description\": \"\", \"category\": \"general\", \"dataType\": \"Text\", \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': 'select', 'order': 41 }\" } ], \"inRelations\": [ { \"relationName\": \"synonym\", \"title\": \"synonyms\", \"description\": \"\", \"required\": false, \"objectTypes\": [ \"Synset\" ], \"renderingHints\": \"{ 'order': 26 }\" }, { \"relationName\": \"hasRhymingSound\", \"title\": \"hasRhymingSound\", \"description\": \"\", \"required\": false, \"objectTypes\": [ \"Phonetic_Boundary\" ], \"renderingHints\": \"\" } ], \"outRelations\": [ { \"relationName\": \"hasAntonym\", \"title\": \"antonyms\", \"description\": \"\", \"required\": false, \"objectTypes\": [ \"Word\" ], \"renderingHints\": \"{ 'order': 26 }\" }, { \"relationName\": \"hasHypernym\", \"title\": \"hypernyms\", \"description\": \"\", \"required\": false, \"objectTypes\": [ \"Word\" ], \"renderingHints\": \"{ 'order': 26 }\" }, { \"relationName\": \"hasHolonym\", \"title\": \"holonyms\", \"description\": \"\", \"required\": false, \"objectTypes\": [ \"Word\" ], \"renderingHints\": \"{ 'order': 26 }\" }, { \"relationName\": \"hasHyponym\", \"title\": \"hyponyms\", \"description\": \"\", \"required\": false, \"objectTypes\": [ \"Word\" ], \"renderingHints\": \"{ 'order': 26 }\" }, { \"relationName\": \"hasMeronym\", \"title\": \"meronyms\", \"description\": \"\", \"required\": false, \"objectTypes\": [ \"Word\" ], \"renderingHints\": \"{ 'order': 26 }\" }, { \"relationName\": \"startsWithAkshara\", \"title\": \"startsWithAkshara\", \"description\": \"\", \"required\": false, \"objectTypes\": [ \"Phonetic_Boundary\" ], \"renderingHints\": \"\" }, { \"relationName\": \"endsWithAkshara\", \"title\": \"endsWithAkshara\", \"description\": \"\", \"required\": false, \"objectTypes\": [ \"Phonetic_Boundary\" ], \"renderingHints\": \"\" }, { \"relationName\": \"hasRhymingSound\", \"title\": \"hasRhymingSound\", \"description\": \"\", \"required\": false, \"objectTypes\": [ \"Phonetic_Boundary\" ], \"renderingHints\": \"\" } ], \"systemTags\": [ { \"name\": \"Review Tags\", \"description\": \"Need to Review this Word.\" }, { \"name\": \"Missing Information\", \"description\": \"Some the information is missing.\" }, { \"name\": \"Incorrect Data\", \"description\": \"Wrong information about this word.\" }, { \"name\": \"Spelling Mistakes\", \"description\": \"Incorrect Spellings\" } ], \"metadata\": { \"ttl\": 24, \"limit\": 50 } } ] }";
		taxonomyManager.updateDefinition(language, contentString);
		contentString = "{  \"definitionNodes\": [    {      \"objectType\": \"Synset\",      \"properties\": [        {          \"propertyName\": \"gloss\",          \"title\": \"Gloss\",          \"description\": \"\",          \"category\": \"general\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 3 }\"        },        {          \"propertyName\": \"glossInEnglish\",          \"title\": \"Gloss in English\",          \"description\": \"\",          \"category\": \"general\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text', 'order': 5 }\"        },        {          \"propertyName\": \"exampleSentences\",          \"title\": \"Example Sentences\",          \"description\": \"\",          \"category\": \"general\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text', 'order': 1 }\"        },        {          \"propertyName\": \"frames\",          \"title\": \"Sentence Frames\",          \"description\": \"\",          \"category\": \"general\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text', 'order': 1 }\"        },        {          \"propertyName\": \"pos\",          \"title\": \"POS\",          \"description\": \"\",          \"category\": \"grammar\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'textarea', 'order': 19 }\"        },        {          \"propertyName\": \"namedEntityType\",          \"title\": \"Named Entity Type\",          \"description\": \"\",          \"category\": \"grammar\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 4 }\"        },        {          \"propertyName\": \"pictures\",          \"title\": \"Pictures\",          \"description\": \"URL\",          \"category\": \"sampleData\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"\"        },        {          \"propertyName\": \"Audio\",          \"title\": \"audio\",          \"description\": \"URL\",          \"category\": \"sampleData\",          \"dataType\": \"List\",          \"range\": [],          \"required\": false,          \"indexed\": true,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'teaxtarea',  'order': 9 }\"        },        {          \"propertyName\": \"reviewers\",          \"title\": \"Reviewers\",          \"description\": \"\",          \"category\": \"audit\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text',  'order': 14 }\"        },        {          \"propertyName\": \"lastUpdatedBy\",          \"title\": \"Last Updated By\",          \"description\": \"\",          \"category\": \"audit\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Editable\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'inputType': 'text',  'order': 14 }\"        },        {          \"propertyName\": \"lastUpdatedOn\",          \"title\": \"Last Updated On\",          \"description\": \"\",          \"category\": \"audit\",          \"dataType\": \"Date\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 22 }\"        },        {            \"propertyName\": \"status\",            \"title\": \"Status\",            \"description\": \"Status of the domain\",            \"category\": \"audit\",            \"dataType\": \"Select\",            \"range\":            [                \"Draft\",                \"Live\",                \"Retired\"            ],            \"required\": false,			\"indexed\": true,            \"displayProperty\": \"Editable\",            \"defaultValue\": \"Draft\",            \"renderingHints\": \"{'inputType': 'select', 'order': 23}\"        },        {          \"propertyName\": \"source\",          \"title\": \"Source\",          \"description\": \"\",          \"category\": \"conflicts\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        },        {          \"propertyName\": \"conflictStatus\",          \"title\": \"Conflict Status\",          \"description\": \"\",          \"category\": \"conflicts\",          \"dataType\": \"Text\",          \"range\": [],          \"required\": false,          \"indexed\": false,          \"displayProperty\": \"Readonly\",          \"defaultValue\": \"\",          \"renderingHints\": \"{ 'order': 25 }\"        }      ],      \"inRelations\": [],      \"outRelations\": [        {          \"relationName\": \"synonym\",          \"title\": \"Synonyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Word\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        },        {          \"relationName\": \"hasAntonym\",          \"title\": \"Antonyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Synset\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        },        {          \"relationName\": \"hasHypernym\",          \"title\": \"Hypernyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Synset\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        },        {          \"relationName\": \"hasHolonym\",          \"title\": \"Holonyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Synset\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        },        {          \"relationName\": \"hasHyponym\",          \"title\": \"Hyponyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Synset\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        },        {          \"relationName\": \"hasMeronym\",          \"title\": \"Meronyms\",          \"description\": \"\",          \"required\": false,          \"objectTypes\": [            \"Synset\"          ],          \"renderingHints\": \"{ 'order': 26 }\"        }      ],      \"systemTags\": [        {          \"name\": \"Review Tags\",          \"description\": \"Need to Review this Synset.\"        },        {          \"name\": \"Missing Information\",          \"description\": \"Some the information is missing.\"        },        {          \"name\": \"Incorrect Data\",          \"description\": \"Wrong information about this Synset.\"        }      ],      \"metadata\": {        \"ttl\": 24,        \"limit\": 50      }    }  ]}";
		taxonomyManager.updateDefinition(language, contentString);
		contentString = "{ \"definitionNodes\": [ { \"objectType\": \"Phonetic_Boundary\", \"properties\": [ { \"propertyName\": \"text\", \"title\": \"Text\", \"description\": \"\", \"category\": \"general\", \"dataType\": \"Text\", \"range\": [], \"required\": true, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'order': 1 }\" }, { \"propertyName\": \"type\", \"title\": \"Type\", \"description\": \"\", \"category\": \"general\", \"dataType\": \"Select\", \"range\": [ \"AksharaBoundary\",\"RhymingSound\" ], \"required\": false, \"indexed\": true, \"displayProperty\": \"Editable\", \"defaultValue\": \"\", \"renderingHints\": \"{ 'inputType': 'textarea', 'order': 2 }\" } ], \"inRelations\": [ { \"relationName\": \"startsWithAkshara\", \"title\": \"startsWithAkshara\", \"description\": \"\", \"required\": false, \"objectTypes\": [ \"Word\" ], \"renderingHints\": \"\" }, { \"relationName\": \"endsWithAkshara\", \"title\": \"endsWithAkshara\", \"description\": \"\", \"required\": false, \"objectTypes\": [ \"Word\" ], \"renderingHints\": \"\" }, { \"relationName\": \"hasRhymingSound\", \"title\": \"RhymingSound\", \"description\": \"\", \"required\": false, \"objectTypes\": [ \"Word\" ], \"renderingHints\": \"\" } ], \"outRelations\": [ { \"relationName\": \"hasRhymingSound\", \"title\": \"RhymingSound\", \"description\": \"\", \"required\": false, \"objectTypes\": [ \"Word\" ], \"renderingHints\": \"\" } ], \"systemTags\": [], \"metadata\": { \"ttl\": 24, \"limit\": 50 } } ] }";
		taxonomyManager.updateDefinition(language, contentString);
	}

	public static void deleteDefinitionStatic(String language) {
		taxonomyManager.delete(language);
	}

	public static void createGraph(){
		if (!Neo4jGraphFactory.graphExists(TEST_LANGUAGE1)) 
			Neo4jGraphFactory.createGraph(TEST_LANGUAGE1);
		if (!Neo4jGraphFactory.graphExists(TEST_LANGUAGE2)) 
			Neo4jGraphFactory.createGraph(TEST_LANGUAGE2);	
	}

	public static void deleteGraph(){
        GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(TEST_LANGUAGE1);
        if (null != graphDb) {
            Neo4jGraphFactory.shutdownGraph(TEST_LANGUAGE1);
        }
        Neo4jGraphFactory.deleteGraph(TEST_LANGUAGE1);
        graphDb = Neo4jGraphFactory.getGraphDb(TEST_LANGUAGE2);
        if (null != graphDb) {
            Neo4jGraphFactory.shutdownGraph(TEST_LANGUAGE2);
        }
        Neo4jGraphFactory.deleteGraph(TEST_LANGUAGE2);
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
