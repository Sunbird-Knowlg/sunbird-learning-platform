package org.ekstep.lp.domain;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

public class synSetsApi extends BaseTest {
	int rn = generateRandomInt(0, 500);

	String jsonCreateSynset = "{ \"request\": {\"synset\":{ \"identifier\": \"ka_s_716\", \"lemma\": \"ಕೋತಿ\",\"difficultyLevel\": \"Easy\"}}}";
	String jsonCreateSynsetWithRelation = "{ \"request\": {\"synset\":{ \"identifier\": \"ka_s_716\", \"lemma\": \"ಕೋತಿ\",\"difficultyLevel\": \"Easy\", \"Antonyms\":[{\"identifier\": \"ka_s_713\"}]}}}";
	String jsonSearchSynset = "{ \"request\": {\"search\": { \"gloss\": [\"Hi\"],\"identifier\": [\"ka_s_716\"],\"fields\": [\"gloss\"],\"limit\": 100}}}";
	String jsonUpdateSynset = "{\"request\": {\"synset\":{\"difficultyLevel\": \"Easy\",\"Antonyms\":[{\"identifier\":\"ka_s_713\"}]}}}";
	
	JSONObject jsCreate = new JSONObject(jsonCreateSynset);
	JSONObject jsSearch = new JSONObject(jsonSearchSynset);
	JSONObject jsUpdate = new JSONObject(jsonUpdateSynset);
	// Create Synset with valid language
	@Test
	public void createValidLanguageSynsetExpectSuccess200() {
		
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA"+rn+"").put("identifier", "TestQA_id_"+rn+"");
		String jsonCreateValidSynset = jsCreate.toString();
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidSynset).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	// Create Synset with invalid language
	@Test
	public void createInvalidLanguageSynsetExpect400() {
		
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA"+rn+"").put("identifier", "TestQA_id_"+rn+"");
		String jsonCreateValidSynset = jsCreate.toString();
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidSynset).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/123abc").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	// Create Synset with case sensitive language
	
	@Test
	public void createInvalidCaseLanguageSynsetExpect400() {
		
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA"+rn+"").put("identifier", "TestQA_id_"+rn+"");
		String jsonCreateValidSynset = jsCreate.toString();
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidSynset).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/EN").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	// Create Synset with Blank language
	
	@Test
	public void createBlankLanguageSynsetExpect500() {
		
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA"+rn+"").put("identifier", "TestQA_id_"+rn+"");
		String jsonCreateValidSynset = jsCreate.toString();
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidSynset).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/").
		then().
			log().all().
			spec(get500ResponseSpec());
	}
	// Create synset with blank identifier
	@Test
	public void createBlankIdentifierSynsetExpect400() {
		
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA"+rn+"").put("identifier", "");
		String jsonCreateBlankIdentifierSynset = jsCreate.toString();
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateBlankIdentifierSynset).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	// Create synset with Existing identifier
	@Test
	public void createExistingIdentifierSynsetExpect400(){
		
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA"+rn+"").put("identifier", "ka_s_716");
		String jsonCreateExistingIdentifierSynset = jsCreate.toString();
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateExistingIdentifierSynset).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	// Create synset with valid identifier
	@Test
	public void createValidIdentifierSynsetExpectSuccess200(){
		
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA"+rn+"").put("identifier", "TestQA_id_"+rn+"");
		String jsonCreateValidIdentifierSynset = jsCreate.toString();
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidIdentifierSynset).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
		
	// Create synset with Existing Lemma
	@Test
	public void createExistingLemmaSynsetExpect400(){
		
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", "Test").put("identifier", "TestQA_id_"+rn+"");
		String jsonCreateExistingLemmaSynset = jsCreate.toString();
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateExistingLemmaSynset).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get400ResponseSpec());
		}
	
	// Create synset with Invalid Lemma(Lemma of other language)
	@Test
	public void createInvalidLemmaSynsetExpect400(){
		
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", "ಕೋತಿ").put("identifier", "TestQA_id_"+rn+"");
		String jsonCreateOtherLanguageLemmaSynset = jsCreate.toString();
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateOtherLanguageLemmaSynset).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get400ResponseSpec());
		}
	
	// Create synset with Created Lemma
	@Test
	public void createExistingSynsetLemmaExpect400(){
		
		//Create valid synset
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"");
		String jsonCreateValidLemmaSynset = jsCreate.toString();
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidLemmaSynset).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		//Fetching the node id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		
		// Get the created synset
		setURI();
		Response R2 = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("language/dictionary/Synset/en/" +nodeId).
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		//Fetching the gloss
		JsonPath jP2 = R2.jsonPath();
		String gloss = jP2.get("result.node.gloss");
		
		// Create synset with newly created synset
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", gloss).put("identifier", "TestQA_id_"+rn+"");
		String jsonCreateExistingLemmaSynset = jsCreate.toString();
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateExistingLemmaSynset).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	// Synset with valid word as synonym(relation)
	@Test
	public void createSynsetWithValidRelationExpectSuccess200() {
		jsonCreateSynsetWithRelation = jsonCreateSynsetWithRelation.replace("Antonyms", "synonyms");
		JSONObject jsCreateSynset = new JSONObject(jsonCreateSynsetWithRelation);
		jsCreateSynset.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"").getJSONObject("synonyms").put("identifier", "en_w_707");
		String jsonCreateSynsetWithRelation = jsCreate.toString();
		System.out.println(jsonCreateSynsetWithRelation);
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateSynsetWithRelation).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	// Synset with Invalid word as synonym(relation)
	@Test
	public void createSynsetWithInvalidRelationExpect400() {
		jsonCreateSynsetWithRelation = jsonCreateSynsetWithRelation.replace("Antonyms", "synonyms");
		JSONObject jsCreateSynset = new JSONObject(jsonCreateSynsetWithRelation);
		jsCreateSynset.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"").getJSONObject("synonyms").put("identifier", "Test_123");
		String jsonCreateSynsetWithRelation = jsCreate.toString();
		System.out.println(jsonCreateSynsetWithRelation);
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateSynsetWithRelation).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	// Synset with synset as synonym(relation)
	@Test
	public void createSynsetWithSynsetRelationExpect400() {
		
		//Create valid synset
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"");
		String jsonCreateValidLemmaSynset = jsCreate.toString();
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidLemmaSynset).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		//Fetching the node id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		
		//Create Synset with synset as relation
		jsonCreateSynsetWithRelation = jsonCreateSynsetWithRelation.replace("Antonyms", "synonyms");
		JSONObject jsCreateSynset = new JSONObject(jsonCreateSynsetWithRelation);
		jsCreateSynset.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"").getJSONObject("synonyms").put("identifier", nodeId);
		String jsonCreateSynsetWithRelation = jsCreate.toString();
		System.out.println(jsonCreateSynsetWithRelation);
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateSynsetWithRelation).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	// Synset with word as Antonym(relation)
	@Test
	public void createSynsetWithWordAsAntonymExpect400() {
		//jsonCreateSynsetWithRelation = jsonCreateSynsetWithRelation.replace("Antonyms", "synonyms");
		JSONObject jsCreateSynset = new JSONObject(jsonCreateSynsetWithRelation);
		jsCreateSynset.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"").getJSONObject("antonyms").put("identifier", "en_w_707");
		String jsonCreateSynsetWithRelation = jsCreate.toString();
		System.out.println(jsonCreateSynsetWithRelation);
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateSynsetWithRelation).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	// Synset with synset as antonym(relation)
	@Test
	public void createSynsetWithSynsetAsAntonymExpectSuccess200() {
		
		//Create valid synset
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"");
		String jsonCreateValidLemmaSynset = jsCreate.toString();
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidLemmaSynset).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		//Fetching the node id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		
		//Create Synset with synset as Antonym
		//jsonCreateSynsetWithRelation = jsonCreateSynsetWithRelation.replace("Antonyms", "synonyms");
		JSONObject jsCreateSynset = new JSONObject(jsonCreateSynsetWithRelation);
		jsCreateSynset.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"").getJSONObject("antonyms").put("identifier", nodeId);
		String jsonCreateSynsetWithRelation = jsCreate.toString();
		System.out.println(jsonCreateSynsetWithRelation);
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateSynsetWithRelation).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get200ResponseSpec());
		}
		
	// Synset with word as hypernyms(relation)
	@Test
	public void createSynsetWithWordAsHypernymsExpect400() {
		jsonCreateSynsetWithRelation = jsonCreateSynsetWithRelation.replace("Antonyms", "hypernyms");
		JSONObject jsCreateSynset = new JSONObject(jsonCreateSynsetWithRelation);
		jsCreateSynset.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"").getJSONObject("hypernyms").put("identifier", "en_w_707");
		String jsonCreateSynsetWithRelation = jsCreate.toString();
		System.out.println(jsonCreateSynsetWithRelation);
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateSynsetWithRelation).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	// Synset with synset as hypernyms(relation)
	@Test
	public void createSynsetWithSynsetAsHypernymExpectSuccess200() {
		
		//Create valid synset
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"");
		String jsonCreateValidLemmaSynset = jsCreate.toString();
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidLemmaSynset).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		//Fetching the node id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		
		//Create Synset with synset as hypernyms
		jsonCreateSynsetWithRelation = jsonCreateSynsetWithRelation.replace("Antonyms", "hypernyms");
		JSONObject jsCreateSynset = new JSONObject(jsonCreateSynsetWithRelation);
		jsCreateSynset.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"").getJSONObject("hypernyms").put("identifier", nodeId);
		String jsonCreateSynsetWithRelation = jsCreate.toString();
		System.out.println(jsonCreateSynsetWithRelation);
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateSynsetWithRelation).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	// Synset with word as holonyms(relation)
	@Test
	public void createSynsetWithWordAsHolonymsExpect400() {
		jsonCreateSynsetWithRelation = jsonCreateSynsetWithRelation.replace("Antonyms", "holonyms");
		JSONObject jsCreateSynset = new JSONObject(jsonCreateSynsetWithRelation);
		jsCreateSynset.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"").getJSONObject("holonyms").put("identifier", "en_w_707");
		String jsonCreateSynsetWithRelation = jsCreate.toString();
		System.out.println(jsonCreateSynsetWithRelation);
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateSynsetWithRelation).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	// Synset with synset as holonyms(relation)
	@Test
	public void createSynsetWithSynsetAsHolonymsExpectSuccess200() {
		
		//Create valid synset
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"");
		String jsonCreateValidLemmaSynset = jsCreate.toString();
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidLemmaSynset).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		//Fetching the node id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		
		//Create Synset with synset as hypernyms
		jsonCreateSynsetWithRelation = jsonCreateSynsetWithRelation.replace("Antonyms", "holonyms");
		JSONObject jsCreateSynset = new JSONObject(jsonCreateSynsetWithRelation);
		jsCreateSynset.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"").getJSONObject("holonyms").put("identifier", nodeId);
		String jsonCreateSynsetWithRelation = jsCreate.toString();
		System.out.println(jsonCreateSynsetWithRelation);
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateSynsetWithRelation).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	// Synset with word as hyponyms(relation)
	@Test
	public void createSynsetWithWordAsHyponymsExpect400() {
		jsonCreateSynsetWithRelation = jsonCreateSynsetWithRelation.replace("Antonyms", "hyponyms");
		JSONObject jsCreateSynset = new JSONObject(jsonCreateSynsetWithRelation);
		jsCreateSynset.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"").getJSONObject("hyponyms").put("identifier", "en_w_707");
		String jsonCreateSynsetWithRelation = jsCreate.toString();
		System.out.println(jsonCreateSynsetWithRelation);
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateSynsetWithRelation).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	// Synset with synset as hyponyms(relation)
	@Test
	public void createSynsetWithSynsetAsHyponymsExpectSuccess200() {
		
		//Create valid synset
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"");
		String jsonCreateValidLemmaSynset = jsCreate.toString();
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidLemmaSynset).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		//Fetching the node id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		
		//Create Synset with synset as hypernyms
		jsonCreateSynsetWithRelation = jsonCreateSynsetWithRelation.replace("Antonyms", "hyponyms");
		JSONObject jsCreateSynset = new JSONObject(jsonCreateSynsetWithRelation);
		jsCreateSynset.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"").getJSONObject("hyponyms").put("identifier", nodeId);
		String jsonCreateSynsetWithRelation = jsCreate.toString();
		System.out.println(jsonCreateSynsetWithRelation);
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateSynsetWithRelation).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	// Synset with word as meronyms(relation)
	@Test
	public void createSynsetWithWordAsMeronymsExpect400() {
		jsonCreateSynsetWithRelation = jsonCreateSynsetWithRelation.replace("Antonyms", "meronyms");
		JSONObject jsCreateSynset = new JSONObject(jsonCreateSynsetWithRelation);
		jsCreateSynset.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"").getJSONObject("meronyms").put("identifier", "en_w_707");
		String jsonCreateSynsetWithRelation = jsCreate.toString();
		System.out.println(jsonCreateSynsetWithRelation);
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateSynsetWithRelation).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	// Synset with synset as meronyms(relation)
	@Test
	public void createSynsetWithSynsetAsMeronymsExpectSuccess200() {
		
		//Create valid synset
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"");
		String jsonCreateValidLemmaSynset = jsCreate.toString();
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidLemmaSynset).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		//Fetching the node id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		
		//Create Synset with synset as meronyms
		jsonCreateSynsetWithRelation = jsonCreateSynsetWithRelation.replace("Antonyms", "meronyms");
		JSONObject jsCreateSynset = new JSONObject(jsonCreateSynsetWithRelation);
		jsCreateSynset.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"").getJSONObject("meronyms").put("identifier", nodeId);
		String jsonCreateSynsetWithRelation = jsCreate.toString();
		System.out.println(jsonCreateSynsetWithRelation);
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateSynsetWithRelation).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	// Create synset with valid URL
	@Test
	public void createValidURLSynsetExpectSuccess200() {
		
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA"+rn+"").put("identifier", "TestQA_id_"+rn+"");
		String jsonCreateValidSynset = jsCreate.toString();
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidSynset).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	// Create synset with valid URL
	@Test
	public void createInvalidURLSynsetExpect400() {
		
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA"+rn+"").put("identifier", "TestQA_id_"+rn+"");
		String jsonCreateValidSynset = jsCreate.toString();
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidSynset).
		with().
			contentType(JSON).
		when().
			post("domain/dictionary/synset/en").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	// Get valid synset with valid language and url
	@Test
	public void getValidURLSynsetExpectSuccess200() {
		
		//Create valid synset
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"");
		String jsonCreateValidLemmaSynset = jsCreate.toString();
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidLemmaSynset).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		//Fetching the node id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		
		// Get the created synset
		setURI();
		Response R2 = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("language/dictionary/Synset/en/" +nodeId).
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Fetching the identifier and check for the condition
		JsonPath jP1 = R2.jsonPath();
		String identifier = jP1.get("result.node_id");
		Assert.assertEquals(nodeId, identifier);
	}
	
	// Get Invalid synset with valid language and url
	@Test
	public void getInvalidSynsetValidUrlExpect400() {
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("language/dictionary/Synset/en/xfafn123").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	// Get Valid Synset with valid url and invalid language
	@Test
	public void getInalidLanguageSynsetExpect400() {
		
		//Create valid synset
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"");
		String jsonCreateValidLemmaSynset = jsCreate.toString();
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidLemmaSynset).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		//Fetching the node id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		
		// Get the created synset
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("language/dictionary/Synset/r343/" +nodeId).
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	// Get Invalid synset with Invalid language and Valid url
	@Test
	public void getInvalidSynsetInvalidLanguageExpect400() {
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("language/dictionary/Synset/r2134/xfafn123").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
		
	// Get Valid synset with valid language and invalid url
	@Test
	public void getInvalidURLSynsetExpect400() {
		
		//Create valid synset
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"");
		String jsonCreateValidLemmaSynset = jsCreate.toString();
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidLemmaSynset).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		//Fetching the node id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		
		// Get the synset
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("domain/dictionary/synset/en" +nodeId).
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	// Search valid synset using valid url
	@Test
	public void searchValidSynsetExpectSuccess200() {
		
		//Create valid synset
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"");
		String jsonCreateValidLemmaSynset = jsCreate.toString();
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidLemmaSynset).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		//Fetching the node id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		
		//Searching the created synset
		setURI();
		jsSearch.getJSONObject("request").getJSONObject("synset").put("identifier", nodeId);
		String jsonSearchValidSynset = jsSearch.toString();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonSearchValidSynset).
		when().
			post("synset/search/en").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	//Search invalid synset with valid url(Other language synset)
	@Test
	public void searchOtherLanguageSynsetExpect400() {
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonSearchSynset).
		when().
			post("synset/search/en").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	//Search valid synset with invalid url(Other language synset)
	@Test
	public void searchInvalidURLSynsetExpect500() {
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonSearchSynset).
		when().
			post("domain/search/en").
		then().
			log().all().
			spec(get500ResponseSpec());
	}
	
	//Update synset with valid url and content
	@Test
	public void updateValidSynsetExpectSuccess200() {
		//Create valid synset
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"");
		String jsonCreateValidLemmaSynset = jsCreate.toString();
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidLemmaSynset).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		//Fetching the node id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		
		//Updating the created synset
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonUpdateSynset).
		when().
			post("language/dictionary/Synset/en/" +nodeId).
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	//Update synset with invalid language and valid content
	@Test
	public void updateInvalidLanguageSynsetExpect400() {
		//Create valid synset
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"");
		String jsonCreateValidLemmaSynset = jsCreate.toString();
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidLemmaSynset).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		//Fetching the node id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		
		//Updating the created synset
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonUpdateSynset).
		when().
			post("language/dictionary/Synset/ka/" +nodeId).
		then().
			log().all().
			spec(get500ResponseSpec());
	}
	
	//Update synset with valid url and invalid content
	@Test
	public void updateInvalidContentSynsetExpect400() {
		//Create valid synset
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"");
		String jsonCreateValidLemmaSynset = jsCreate.toString();
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidLemmaSynset).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		//Fetching the node id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		
		//Updating the created synset
		setURI();
		jsUpdate.getJSONObject("request").getJSONObject("synset").put("difficultyLevel", "Hard");
		String jsonUpdateInvalidContent = jsUpdate.toString();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonUpdateInvalidContent).
		when().
			post("language/dictionary/Synset/en/" +nodeId).
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	// Update Synset with invalid URL
	@Test
	public void updateSynsetWithInvalidURLExpect500(){

		//Create valid synset
		jsCreate.getJSONObject("request").getJSONObject("synset").put("lemma", "Test_QA_"+rn+"").put("identifier", "TestQA_id_"+rn+"");
		String jsonCreateValidLemmaSynset = jsCreate.toString();
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidLemmaSynset).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/Synset/en").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		//Fetching the node id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		
		//Updating the created synset
		setURI();
		jsUpdate.getJSONObject("request").getJSONObject("synset").put("difficultyLevel", "Hard");
		String jsonUpdateInvalidContent = jsUpdate.toString();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonUpdateInvalidContent).
		when().
			post("domain/dictionary/Synset/en/" +nodeId).
		then().
			log().all().
			spec(get500ResponseSpec());
	}
}
