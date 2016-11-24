package org.ekstep.platform.language;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ekstep.platform.domain.BaseTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class WordPatchUpdateTest extends BaseTest {

	
	static int randomNo;
	String jsonCreateNewWord = "{\"request\":{\"words\":[{\"identifier\":\"word_"+randomNo+"\",\"lemma\":\"newtestword"+randomNo+"\",\"primaryMeaning\":{\"identifier\":\"pm_"+randomNo+"\",\"gloss\":\"ss1\",\"category\":\"Person\",\"exampleSentences\":[\"es11\",\"es12\"],\"synonyms\":[{\"identifier\":\"synonym_"+randomNo+"_1\",\"lemma\":\"testSynonym1\"},{\"lemma\":\"synonym_"+randomNo+"_2\",\"lemma\":\"testSynonym2\"}]},\"status\":\"Draft\",\"otherMeanings\":[{ \"identifier\":\"om_"+randomNo+"_1\",\"gloss\":\"new_other_ss1\",\"category\":\"Thing\"},{  \"identifier\":\"om_"+randomNo+"_2\", \"gloss\":\"new_other_ss2\",\"category\":\"Person\"}]}]}}";
	
	
	static{
		Random random = new Random();
		randomNo = random.nextInt(9999999);
	}
	
	static String nodeId;
	
	@Before
	public void createWord(){
		if(nodeId==null){
			setURI();
			Response R = given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateNewWord).
			with().
			contentType(JSON).
			when().
			post("language/v2/language/dictionary/word/en").
			then().
			//spec(get200ResponseSpec()).
			extract().
			response();
		
		
			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			List<String> nodeIds = (List<String>)jp.get("result.node_ids");
			nodeId = nodeIds.get(0);
			 System.out.println("nodeId="+nodeId);
		}

	}
	
	@Test
	public void  patchUpdateWordMetadata() {

		String pathUpdate = "{ \"request\": { \"word\": { \"isLoanWord\": true } }}";
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(pathUpdate).
		with().
		contentType(JSON).
		when().
		patch("language/v2/language/dictionary/word/en/"+nodeId).
		then().
		spec(get200ResponseSpec());
		
		Response R1 = 
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
		get("language/v2/language/dictionary/word/en/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec()).
		extract().
		response();
		
		JsonPath jp1 = R1.jsonPath();
		Assert.assertNotNull(jp1.getBoolean("result.Word.isPhrase"));

	}
	
	@Test
	public void  patchUpdateWithNewPrimaryMeaning() {

		String pathUpdate = "{ \"request\":{ \"word\":{ \"primaryMeaning\":{ \"gloss\":\"new_ss1\",\"category\":\"Place\"} } } }";
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(pathUpdate).
		with().
		contentType(JSON).
		when().
		patch("language/v2/language/dictionary/word/en/"+nodeId).
		then().
		spec(get200ResponseSpec());
	
		Response R1 = 
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
		get("language/v2/language/dictionary/word/en/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec()).
		extract().
		response();
		
		JsonPath jp1 = R1.jsonPath();
		String primaryMeaningGloss = jp1.getString("result.Word.primaryMeaning.gloss");
		String primaryMeaningCategory = jp1.getString("result.Word.primaryMeaning.category");
		List<Map<String,Object>> synonyms = jp1.getList("result.Word.primaryMeaning.synonyms");
		List<String> synonymIds = new ArrayList<String>();
		for(Map<String,Object> s:synonyms)
			synonymIds.add((String) s.get("identifier"));
		Assert.assertNotNull(synonymIds);
		Assert.assertEquals("primary meaning is not changed, primaryMeaningGloss is not reflected changes ", "new_ss1", primaryMeaningGloss);
		Assert.assertEquals("primary meaning is not changed, primaryMeaningCategory is not reflected changes ", "Place", primaryMeaningCategory);;
	}
	
	@Test
	public void  patchUpdateWithDifferentPrimaryMeaning() {

		Response R0 = 
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
		get("language/v2/language/dictionary/word/en/"+nodeId).
		then().
		extract().
		response();
		
		JsonPath jp1 = R0.jsonPath();
		String oldPrimaryMeaningId = jp1.getString("result.Word.primaryMeaningId");

		
		String pathUpdate = "{ \"request\":{ \"word\":{ \"primaryMeaning\":{ \"gloss\":\"new_different_ss1\",\"category\":\"Place\"} } } }";
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(pathUpdate).
		with().
		contentType(JSON).
		then().
		patch("language/v2/language/dictionary/word/en/"+nodeId);
		
		Response R1 = 
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
		get("language/v2/language/dictionary/word/en/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec()).
		extract().
		response();
		
		jp1 = R1.jsonPath();
		String primaryMeaningGloss = jp1.getString("result.Word.primaryMeaning.gloss");
		String primaryMeaningCategory = jp1.getString("result.Word.primaryMeaning.category");
		String newPrimaryMeaningId = jp1.getString("result.Word.primaryMeaningId");
		Assert.assertNotEquals(newPrimaryMeaningId, oldPrimaryMeaningId);
		System.out.println(jp1.getString("result.Word.primaryMeaning"));
		Assert.assertEquals("primary meaning is not changed, primaryMeaningGloss is not reflected changes ", "new_different_ss1", primaryMeaningGloss);
		Assert.assertEquals("primary meaning is not changed, primaryMeaningCategory is not reflected changes ", "Place", primaryMeaningCategory);;
		
		pathUpdate = "{ \"request\":{ \"word\":{ \"primaryMeaning\":{ \"identifier\":\""+oldPrimaryMeaningId+"\"} } } }";
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(pathUpdate).
		with().
		contentType(JSON).
		when().
		patch("language/v2/language/dictionary/word/en/"+nodeId).
		then().
		spec(get200ResponseSpec());
		
		R1 = 
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
		get("language/v2/language/dictionary/word/en/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec()).
		extract().
		response();
		
		jp1 = R1.jsonPath();
		String primaryMeaningId = jp1.getString("result.Word.primaryMeaningId");
		Assert.assertEquals(primaryMeaningId, oldPrimaryMeaningId);
	}
	
	@Test
	public void  patchUpdateWithOtherMeaning() {

		String pathUpdate = "{ \"request\":{ \"word\":{ \"otherMeanings\":[{ \"identifier\":\"om_"+randomNo+"_1\",\"gloss\":\"new_other_ss1_modified\",\"category\":\"Quality\"},{ \"gloss\":\"new_other_ss3\",\"category\":\"Person\"}] }  } }";
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(pathUpdate).
		with().
		contentType(JSON).
		when().
		patch("language/v2/language/dictionary/word/en/"+nodeId).
		then().
		spec(get200ResponseSpec());
	
		Response R1 = 
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
		get("language/v2/language/dictionary/word/en/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec()).
		extract().
		response();
		
		JsonPath jp1 = R1.jsonPath();
		List<Map<String,Object>> otherMeanings = jp1.getList("result.Word.otherMeanings");
		List<String> otherMeaningValues = new ArrayList<String>();
		for(Map<String,Object> om:otherMeanings)
			otherMeaningValues.add((String) om.get("gloss"));
		Assert.assertNotNull(otherMeaningValues);
		Assert.assertEquals(otherMeaningValues.size(), 2);
		Assert.assertTrue(otherMeaningValues.contains("new_other_ss1_modified"));
		Assert.assertTrue(otherMeaningValues.contains("new_other_ss3"));
	}
	
	@Test
	public void  patchUpdateWithInvalidOtherMeaning() {

		String pathUpdate = "{ \"request\":{ \"word\":{ \"otherMeanings\":[{ \"identifier\":\"om_new_"+randomNo+"_1\",\"gloss\":\"new_other_ss1_total\",\"category\":\"Quality\"}] }  } }";
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(pathUpdate).
		with().
		contentType(JSON).
		when().
		patch("language/v2/language/dictionary/word/en/"+nodeId).
		then().
			spec(get500ResponseSpec());
	}
	
	@Test
	public void  patchUpdateNewSynonymTest() {

		String pathUpdate = "{ \"request\":{ \"word\": { \"primaryMeaning\":{ \"identifier\":\"pm_"+randomNo+"\", \"synonyms\":[ { \"identifier\":\"synonym_"+randomNo+"_1\", \"lemma\":\"testSynonym_"+randomNo+"_1\" }, { \"identifier\":\"synonym_"+randomNo+"_2\", \"lemma\":\"testSynonym_"+randomNo+"_2\" } , { \"identifier\":\"synonym_"+randomNo+"_3\", \"lemma\":\"testSynonym_"+randomNo+"_3\" } ] } } } }";
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(pathUpdate).
		with().
		contentType(JSON).
		when().
		patch("language/v2/language/dictionary/word/en/"+nodeId).
		then().
		spec(get200ResponseSpec());
	
		Response R1 = 
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
		get("language/v2/language/dictionary/word/en/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec()).
		extract().
		response();
		
		JsonPath jp1 = R1.jsonPath();
		List<Map<String,Object>> synonyms = jp1.getList("result.Word.primaryMeaning.synonyms");
		List<String> synonymIds = new ArrayList<String>();
		for(Map<String,Object> s:synonyms)
			synonymIds.add((String) s.get("identifier"));
		Assert.assertTrue(synonymIds.contains("synonym_"+randomNo+"_3"));
	}
	
	@Test
	public void  patchUpdateWithRemovalOfExistingOneSynonymTest() {

		String pathUpdate = "{ \"request\":{ \"word\":{ \"primaryMeaning\":{ \"identifier\":\"pm_"+randomNo+"\", \"synonyms\":[ { \"identifier\":\"synonym_"+randomNo+"_1\", \"lemma\":\"testSynonym_"+randomNo+"_1\" },{ \"identifier\":\"synonym_"+randomNo+"_3\", \"lemma\":\"testSynonym_"+randomNo+"_3\" } ] } } } }";
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(pathUpdate).
		with().
		contentType(JSON).
		when().
		patch("language/v2/language/dictionary/word/en/"+nodeId).
		then().
		spec(get200ResponseSpec());
	
		Response R1 = 
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
		get("language/v2/language/dictionary/word/en/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec()).
		extract().
		response();
		
		JsonPath jp1 = R1.jsonPath();
		List<Map<String,Object>> synonyms = jp1.getList("result.Word.primaryMeaning.synonyms");
		List<String> synonymIds = new ArrayList<String>();
		for(Map<String,Object> s:synonyms)
			synonymIds.add((String) s.get("identifier"));
		System.out.println(synonymIds);
		Assert.assertFalse(synonymIds.contains("synonym_"+randomNo+"_2"));
	}
	
	@Test
	public void  patchUpdateWithDifferentSynonymSet() {

		String pathUpdate = "{ \"request\":{ \"word\":{ \"primaryMeaning\":{ \"identifier\":\"pm_"+randomNo+"\", \"synonyms\":[ { \"identifier\":\"synonym_new_"+randomNo+"_1\", \"lemma\":\"testNewSynonym_"+randomNo+"_1\" }, { \"identifier\":\"synonym_new_"+randomNo+"_2\", \"lemma\":\"testNewSynonym_"+randomNo+"_2\" }  ] } } } }";
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(pathUpdate).
		with().
		contentType(JSON).
		when().
		patch("language/v2/language/dictionary/word/en/"+nodeId).
		then().
		spec(get200ResponseSpec());
	
		Response R1 = 
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
		get("language/v2/language/dictionary/word/en/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec()).
		extract().
		response();
		
		JsonPath jp1 = R1.jsonPath();
		List<Map<String,Object>> synonyms = jp1.getList("result.Word.primaryMeaning.synonyms");
		List<String> synonymIds = new ArrayList<String>();
		for(Map<String,Object> s:synonyms)
			synonymIds.add((String) s.get("identifier"));
		System.out.println(synonymIds);
		Assert.assertTrue(synonymIds.contains("synonym_new_"+randomNo+"_1"));
		Assert.assertTrue(synonymIds.contains("synonym_new_"+randomNo+"_2"));
		Assert.assertFalse(synonymIds.contains("synonym_"+randomNo+"_1"));
		Assert.assertFalse(synonymIds.contains("synonym_"+randomNo+"_2"));
		Assert.assertFalse(synonymIds.contains("synonym_"+randomNo+"_3"));
	}
	
	
	@Test
	public void  patchUpdateWithDifferentAntonymSet() {

		String pathUpdate = "{ \"request\":{ \"word\": { \"primaryMeaning\":{ \"identifier\":\"pm_"+randomNo+"\", \"antonyms\":[ { \"identifier\":\"antonym_new_"+randomNo+"_1\", \"lemma\":\"testNewAntonym_"+randomNo+"_1\" }, { \"identifier\":\"antonym_new_"+randomNo+"_2\", \"lemma\":\"testNewAntonym_"+randomNo+"_2\" }  ] } }  } }";
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(pathUpdate).
		with().
		contentType(JSON).
		when().
		patch("language/v2/language/dictionary/word/en/"+nodeId).
		then().
		spec(get200ResponseSpec());
	
		Response R1 = 
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
		get("language/v2/language/dictionary/word/en/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec()).
		extract().
		response();
		
		JsonPath jp1 = R1.jsonPath();
		List<Map<String,Object>> synonyms = jp1.getList("result.Word.primaryMeaning.antonyms");
		List<String> synonymIds = new ArrayList<String>();
		for(Map<String,Object> s:synonyms)
			synonymIds.add((String) s.get("identifier"));
		Assert.assertTrue(synonymIds.contains("antonym_new_"+randomNo+"_1"));
		Assert.assertTrue(synonymIds.contains("antonym_new_"+randomNo+"_2"));
		Assert.assertFalse(synonymIds.contains("antonym_"+randomNo+"_1"));
		Assert.assertFalse(synonymIds.contains("antonym_"+randomNo+"_2"));
	}
	
	@Test
	public void  patchUpdateWithDifferentHypernymsSet() {

		String pathUpdate = "{ \"request\":{ \"word\": { \"primaryMeaning\":{ \"identifier\":\"pm_"+randomNo+"\", \"hypernyms\":[ { \"identifier\":\"hypernym_new_"+randomNo+"_1\", \"lemma\":\"testNewHypernym_"+randomNo+"_1\" }, { \"identifier\":\"hypernym_new_"+randomNo+"_2\", \"lemma\":\"testNewHypernym_"+randomNo+"_2\" }  ] } }  } }";
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(pathUpdate).
		with().
		contentType(JSON).
		when().
		patch("language/v2/language/dictionary/word/en/"+nodeId).
		then().
		spec(get200ResponseSpec());
	
		Response R1 = 
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
		get("language/v2/language/dictionary/word/en/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec()).
		extract().
		response();
		
		JsonPath jp1 = R1.jsonPath();
		List<Map<String,Object>> synonyms = jp1.getList("result.Word.primaryMeaning.hypernyms");
		List<String> synonymIds = new ArrayList<String>();
		for(Map<String,Object> s:synonyms)
			synonymIds.add((String) s.get("identifier"));
		Assert.assertTrue(synonymIds.contains("hypernym_new_"+randomNo+"_1"));
		Assert.assertTrue(synonymIds.contains("hypernym_new_"+randomNo+"_2"));
		Assert.assertFalse(synonymIds.contains("hypernym_"+randomNo+"_1"));
		Assert.assertFalse(synonymIds.contains("hypernym_"+randomNo+"_2"));
	}
	
	@Test
	public void  patchUpdateWithDifferentHolonymsSet() {

		String pathUpdate = "{ \"request\":{ \"word\": { \"primaryMeaning\":{ \"identifier\":\"pm_"+randomNo+"\", \"holonyms\":[ { \"identifier\":\"holonym_new_"+randomNo+"_1\", \"lemma\":\"testNewHolonym_"+randomNo+"_1\" }, { \"identifier\":\"holonym_new_"+randomNo+"_2\", \"lemma\":\"testNewHolonym_"+randomNo+"_2\" }  ] } } } }";
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(pathUpdate).
		with().
		contentType(JSON).
		when().
		patch("language/v2/language/dictionary/word/en/"+nodeId).
		then().
		spec(get200ResponseSpec());
	
		Response R1 = 
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
		get("language/v2/language/dictionary/word/en/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec()).
		extract().
		response();
		
		JsonPath jp1 = R1.jsonPath();
		List<Map<String,Object>> synonyms = jp1.getList("result.Word.primaryMeaning.holonyms");
		List<String> synonymIds = new ArrayList<String>();
		for(Map<String,Object> s:synonyms)
			synonymIds.add((String) s.get("identifier"));
		Assert.assertTrue(synonymIds.contains("holonym_new_"+randomNo+"_1"));
		Assert.assertTrue(synonymIds.contains("holonym_new_"+randomNo+"_2"));
		Assert.assertFalse(synonymIds.contains("holonym_"+randomNo+"_1"));
		Assert.assertFalse(synonymIds.contains("holonym_"+randomNo+"_2"));
	}
	
	@Test
	public void  patchUpdateWithDifferentHyponymsSet() {

		String pathUpdate = "{ \"request\":{ \"word\": { \"primaryMeaning\":{ \"identifier\":\"pm_"+randomNo+"\", \"hyponyms\":[ { \"identifier\":\"hyponym_new_"+randomNo+"_1\", \"lemma\":\"testNewHyponym_"+randomNo+"_1\" }, { \"identifier\":\"hyponym_new_"+randomNo+"_2\", \"lemma\":\"testNewHyponym_"+randomNo+"_2\" }  ] } } } }";
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(pathUpdate).
		with().
		contentType(JSON).
		when().
		patch("language/v2/language/dictionary/word/en/"+nodeId).
		then().
		spec(get200ResponseSpec());
	
		Response R1 = 
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
		get("language/v2/language/dictionary/word/en/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec()).
		extract().
		response();
		
		JsonPath jp1 = R1.jsonPath();
		List<Map<String,Object>> synonyms = jp1.getList("result.Word.primaryMeaning.hyponyms");
		List<String> synonymIds = new ArrayList<String>();
		for(Map<String,Object> s:synonyms)
			synonymIds.add((String) s.get("identifier"));
		Assert.assertTrue(synonymIds.contains("hyponym_new_"+randomNo+"_1"));
		Assert.assertTrue(synonymIds.contains("hyponym_new_"+randomNo+"_2"));
		Assert.assertFalse(synonymIds.contains("hyponym_"+randomNo+"_1"));
		Assert.assertFalse(synonymIds.contains("hyponym_"+randomNo+"_2"));
	}
	
	@Test
	public void  patchUpdateWithDifferentMeronymsSet() {

		String pathUpdate = "{ \"request\":{ \"word\": { \"primaryMeaning\":{ \"identifier\":\"pm_"+randomNo+"\", \"meronyms\":[ { \"identifier\":\"meronym_new_"+randomNo+"_1\", \"lemma\":\"testNewMeronym_"+randomNo+"_1\" }, { \"identifier\":\"meronym_new_"+randomNo+"_2\", \"lemma\":\"testNewMeronym_"+randomNo+"_2\" }  ] } } } }";
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(pathUpdate).
		with().
		contentType(JSON).
		when().
		patch("language/v2/language/dictionary/word/en/"+nodeId).
		then().
		spec(get200ResponseSpec());
	
		Response R1 = 
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
		get("language/v2/language/dictionary/word/en/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec()).
		extract().
		response();
		
		JsonPath jp1 = R1.jsonPath();
		List<Map<String,Object>> synonyms = jp1.getList("result.Word.primaryMeaning.meronyms");
		List<String> synonymIds = new ArrayList<String>();
		for(Map<String,Object> s:synonyms)
			synonymIds.add((String) s.get("identifier"));
		Assert.assertTrue(synonymIds.contains("meronym_new_"+randomNo+"_1"));
		Assert.assertTrue(synonymIds.contains("meronym_new_"+randomNo+"_2"));
		Assert.assertFalse(synonymIds.contains("meronym_"+randomNo+"_1"));
		Assert.assertFalse(synonymIds.contains("meronym_"+randomNo+"_2"));
	}

}
