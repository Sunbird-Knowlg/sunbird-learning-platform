package org.ekstep.platform.language;

import org.ekstep.platform.domain.BaseTest;
import org.json.JSONObject;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import org.junit.Assert;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;


public class LinkWordsTestCases extends BaseTest{

	String jsonAddTranslation = "{\"translations\": {\"synset1\": {\"hi\": [\"hi_s1\"], \"en\": [\"en_s1\"]}}}";
	// 
	
	@Test
	public void searchWordsSingleLanguageExpectSuccess200(){
		
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body("").
		with().
			contentType(JSON).
		when().
			post("language/v1/language/search").
		then().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jPath = R.jsonPath();
		String lemma = jPath.get("result.lemma");				
	}
	
	@Test
	public void searchWordsMultipleLanguageExpectSuccess200(){
		
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body("").
		with().
			contentType(JSON).
		when().
			post("language/v1/language/search").
		then().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jPath = R.jsonPath();
		String lemma = jPath.get("result.lemma");	
	}
	
	@Test
	public void searchNonExistingWordsMultipleLanguages(){
		
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body("").
		with().
			contentType(JSON).
		when().
			post("language/v1/language/search").
		then().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jPath = R.jsonPath();
		String lemma = jPath.get("result.lemma");
		Assert.assertTrue(lemma.isEmpty());
	}
	
	@Test
	public void searchExistingNonExistingWordsMulitpleLanguagesExpectSuccess200(){
		
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body("").
		with().
			contentType(JSON).
		when().
			post("language/v1/language/search").
		then().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jPath = R.jsonPath();
		String lemma = jPath.get("result.lemma");
	}
	
	// Search Words in languages which doesn't loaded
	
	@Test
	public void searchNonExistingLanguageWords(){
		
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body("").
		with().
			contentType(JSON).
		when().
			post("language/v1/language/search").
		then().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jPath = R.jsonPath();
		String lemma = jPath.get("result.lemma");
		Assert.assertTrue(lemma.isEmpty());
	}
	
	// Get Translations of word with single meaning in all languages
	
	@Test
	public void getTranslationSingleMeaningWordExpectSuccess200(){
		
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/v2/language/translations/hi/hi_01").
		then().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jPath = R.jsonPath();	
	}
	
	// Get Translations of word with multiple meaning in all languages
	
	@Test
	public void getTranslationMultipleMeaningWordExpectSuccess200(){
		
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/v2/language/translations/hi/hi_02").
		then().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jPath = R.jsonPath();	
	}
	
	// Get Translations of word with single meaning with given list of languages
	
	@Test
	public void getTranslationSingleMeaningListedLanguageExpectSuccess200(){
		
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/v2/language/translations/hi/hi_01?languages=hi,en,ka").
		then().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jPath = R.jsonPath();	
	}
	
	// Get Translations of word with multiple meaning with given list of languages
	
	@Test
	public void getTranslationWithMultipleMeaningListedLanguageExpectSuccess200(){
		
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/v2/language/translations/hi/hi_02?languages=hi,en,ka").
		then().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jPath = R.jsonPath();
	}
	
	// Get translations for word with no translations
	
	@Test
	public void getTranslationsUnavailableLanguages(){
		
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/v2/language/translations/hi/hi_01?languages=bn,en").
		then().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jPath = R.jsonPath();
	}
	
	// Get Translations for word with existing and non-existing languages
	
	@Test
	public void getTranslationsExistingNonExistingLanguages(){
		
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/v2/language/translations/hi/hi_01?languages=hi,ta,bn").
		then().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jPath = R.jsonPath();
	}
	
	// Get Translations of word not loaded in wordnet
	
	@Test
	public void getTranslationsNotLoadedLanguage(){
		
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/v2/language/translations/hi/hi_01?languages=ur").
		then().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jPath = R.jsonPath();
	}
	
	// Add translation between two meanings
	
	@Test
	public void addTranslationValidMeaningsExpectSuccess200(){
		
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonAddTranslation).
		with().
			contentType(JSON).
		when().
			post("/v2/language/translations/hi/hi_2").
		then().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jPath = R.jsonPath();
		
		setURI();
		Response R1 = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/v2/language/translations/hi/hi_01").
		then().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jPath1 = R1.jsonPath();
	}
	
	// Add Translations between valid and invalid meanings
	
	@Test
	public void addTranslationsvalidAndInvalidMeaningExpect4xx(){
		
		setURI();
		JSONObject js =new JSONObject(jsonAddTranslation);
		js.getJSONObject("request").getJSONObject("sysnet1").put("en", "asfa");
		String jsonAddTranslationValidInvalid = js.toString();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonAddTranslationValidInvalid).
		with().
			contentType(JSON).	
		when().	
			post("/v2/language/translations/hi/hi_2").
		then().
			spec(get400ResponseSpec());
	}
	
	// Add Translation between a valid and a retired meaning
	
	@Test
	public void addTranslationsValidAndRetiredMeaningExpect4xx(){
		
		setURI();
		JSONObject js =new JSONObject(jsonAddTranslation);
		js.getJSONObject("request").getJSONObject("sysnet1").put("en", "en_012");
		String jsonAddTranslationValidInvalid = js.toString();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonAddTranslationValidInvalid).
		with().
			contentType(JSON).	
		when().	
			post("/v2/language/translations/hi/hi_2").
		then().
			spec(get400ResponseSpec());
	}
	
	// Remove Translation between valid meanings
	
	@Test
	public void removeTranslationsValidExpectSuccess200(){
		
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonAddTranslation).
		with().
			contentType(JSON).
		when().
			delete("/v2/language/translations/hi/hi_2").
		then().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jPath = R.jsonPath();
		
		setURI();
		Response R1 = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/v2/language/translations/hi/hi_01").
		then().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jPath1 = R1.jsonPath();
	}
	
	// Remove translation between valid and non-existing meaning
	
	@Test
	public void removeTranslationValidInvalidMeaningExpect4xx(){
		
		setURI();
		JSONObject js =new JSONObject(jsonAddTranslation);
		js.getJSONObject("request").getJSONObject("sysnet1").put("en", "en_012");
		String jsonRemoveTranslationValidInvalid = js.toString();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonRemoveTranslationValidInvalid).
		with().
			contentType(JSON).	
		when().	
			delete("/v2/language/translations/hi/hi_2").
		then().
			spec(get400ResponseSpec());

	}
	
}
