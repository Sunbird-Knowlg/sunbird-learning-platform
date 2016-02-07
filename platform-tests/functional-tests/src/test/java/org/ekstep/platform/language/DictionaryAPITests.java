package org.ekstep.platform.language;


import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;

import org.junit.Test;
import org.ekstep.platform.domain.BaseTest;
import static com.jayway.restassured.http.ContentType.JSON;

public class DictionaryAPITests extends BaseTest 
{
	
	//String langAPIVersion = "v1";
	
	String jsonCreateExistingEnglishWord = "{\"request\":{\"words\":[{\"lemma\":\"en:W:Test QA2\",\"code\":\"en:W:Test QA2\",\"identifier\":\"en:W:Test QA2\",\"tags\": \"[]\",\"Synonyms\":[{\"identifier\":\"TestQA1\"}]}]}}";
	String jsonCreateNewEnglishWord = "{\"request\":{\"words\":[{\"lemma\":\"en:W:Test QA4\",\"code\":\"en:W:Test QA4\",\"identifier\":\"en:W:Test QA4\",\"tags\": \"[]\",\"Synonyms\":[{\"identifier\":\"TestQA3\"}]}]}}";
	String jsonCreateMultipleEnglishWords = "{\"request\":{  \"words\":[  {  \"lemma\":\"TestDuplWord1\",\"code\":\"qa:W:TestDuplWord1\",\"tags\":[\"QA\"],\"Synonyms\":{\"identifier\":\"qa:W:TestDuplWord1\"}]},{  \"lemma\":\"TestDuplWord2\",\"code\":\"qa:W:TestDuplWord2\",\"tags\":[\"QA\"],\"Synonyms\":[{\"identifier\": \"qa:W:TestDuplWord2\"}]}]}}";
	String jsonCreateMultipleDuplicateEnglishWords = "{\"request\":{  \"words\":[  {  \"lemma\":\"TestDuplFailWord\",\"code\":\"qa:W:TestDupl1Word1\",\"tags\":[\"QA\"],\"Synonyms\":{\"identifier\":\"qa:W:TestDuplWord1\"}]},{  \"lemma\":\"TestDuplFailWord\",\"code\":\"qa:W:TestDuplFailWord2\",\"tags\":[\"QA\"],\"Synonyms\":[{\"identifier\": \"qa:W:TestFailDuplWord2\"}]}]}}";
	String jsonSearchValidWord = "{\"request\":{\"lemma\":\"new test word 3\"}}";
	String jsonSearchInvalidWord = "{\"request\":{\"lemma\":\"new123\"}}";
		
	
	//------------ Language API start --------------//
	//Get Languages - List of languages
	@Test
	public void getLanguageExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v1/language").
		then().
			//log().all().
			spec(get200ResponseSpec()).
			body("result.languages.name", hasItems("English","Hindi","Kannada"));
	}
	
	@Test
	public void getLanguageWithInvalidURLExpect500()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v1/languages").
		then().
			//log().all().
			spec(get500ResponseSpec());
	}
	
	
	//------------ Language API end --------------//
	
	
	
	
	//------------ Words API start --------------//
	//Get synonyms
	@Test
	public void getSynonymsExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v1/language/dictionary/word/hi/synonym/hi:W:000209946").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	@Test
	public void getSynonymsOfNonExistingWordExpect4xx()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v1/language/dictionary/word/hi/synonym/xyz").
		then().
			//log().all().
			spec(get404ResponseSpec());
	}
	
	@Test
	public void getSynonymsWithInvalidLanguageIdExpect4xx()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v1/language/dictionary/word/hin/synonym/hi:W:000209946").
		then().
			//log().all().
			spec(get400ResponseSpec());
	}
	
	//Get Related words
	@Test
	public void getRelatedWordsExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v1/language/dictionary/word/en/relation/63736?relations=antonyms").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	@Test
	public void getRelatedWordsWithInvalidLanguageIdExpect4xx()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v1/language/dictionary/word/eng/relation/63736?relations=antonyms").
		then().
			//log().all().
			spec(get400ResponseSpec());
	}
	
	@Test
	public void getRelatedWordsWithMisspelledRelationExpect4xx()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v1/language/dictionary/word/en/relatin/63736?relations=antonyms").
		then().
			//log().all().
			spec(get400ResponseSpec());
	}
	
	
	//Create Word
	@Test
	public void  createNewWordExpectSuccess200() {
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateNewEnglishWord).
		with().
			contentType(JSON).
		when().
			post("v1/language/dictionary/word/en/").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	@Test
	public void  createExistingWordExpect400() {
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(jsonCreateExistingEnglishWord).
		with().
			contentType(JSON).
		when().
			post("v1/language/dictionary/word/en/").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	//CreateMultipleWords in Single Request
	public void  createMultipleWordsInSingleRequestExpect200() {
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(jsonCreateMultipleEnglishWords).
		with().
			contentType(JSON).
		when().
			post("v1/language/dictionary/word/en/").
		then().
			log().all().
			spec(get200ResponseSpec()).
			body("result.node_id.size()", is(2));
	}
	
	
	//CreateDuplicateMultipleWords in Single Request
		public void  createDuplicateMultipleWordsInSingleRequestExpect200() {
			setURI();
			given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateMultipleDuplicateEnglishWords).
			with().
				contentType(JSON).
			when().
				post("v1/language/dictionary/word/en/").
			then().
				log().all().
				spec(get400ResponseSpec()).
				body("result.node_id.size()", is(1));
		}
	
	//Get Words
	@Test
	public void getExistingWordsExpectSuccess200() {
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
			get("v1/language/dictionary/word/en").
	then().
		log().all().
		spec(get200ResponseSpec());
	}
	
	@Test
	public void getNonExistingWordsExpect400() {
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
			patch("v1/language/dictionary/word/abc").
		then().
			log().all().
			spec(get400ResponseSpec());
		
	}
		
	//Get Word
	@Test
	public void getExistingWordExpectSuccess200() {
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
			get("v1/language/dictionary/word/en/en:W:Test QA1").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	@Test
	public void getNonExistingWordExpect400() {
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
			patch("v1/language/dictionary/word/en/en:W:TestXYZ").
		then().
			log().all().
			spec(get400ResponseSpec());
		
	}

	//Search Word
	@Test
	public void searchValidWordExpectSuccess200() {
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(jsonSearchValidWord).
		with().
			contentType("application/json").
		when().
			post("v1/language/dictionary/search/en").
		then().
			log().all().
			spec(get200ResponseSpec());
		
	}
	
	@Test
	public void searchInvalidWordExpect400() {
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(jsonSearchInvalidWord).
		with().
			contentType("application/json").
		when().
			post("v1/language/dictionary/search/en").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
		
	@Test
	public void addNewRelationExpectSuccess200() {
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
			post("v1/language/dictionary/word/en/en:W:Test QA2/synonym/en:S:Test QA45").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
		
	//Delete Relation
	@Test
	public void deleteValidRelationExpectSuccess200() {
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
			post("v1/language/dictionary/word/en/en:W:Test QA4/synonym/en:W:Test QA45").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
		
	@Test
	public void deleteInvalidRelationExpect400() {
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
			post("v1/language/dictionary/word/en/en:W:Test QA28/synonym/en:W:Test QA44").
		then().
			log().all().
			spec(get400ResponseSpec());
	}

}

	

