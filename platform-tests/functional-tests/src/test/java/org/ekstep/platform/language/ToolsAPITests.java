package org.ekstep.platform.language;


import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import org.ekstep.platform.domain.BaseTest;
import org.junit.Test;

public class ToolsAPITests extends BaseTest {

	String jsonForLexicalMeasuresSentencesValid = "{ \"request\": {\"language_id\" : \"hi\",\"texts\": [\"आप से मिल के ख़ुशी हुई, आप से मिल के अच्छा लगा।\"]}}";
	String jsonForLexicalMeasuresWordsValid = "{ \"request\": {\"language_id\" : \"hi\",\"words\": [\"आप\", \"मिल\", \"ख़ुशी\", \"अच्छा\"]}}";
	String jsonForLexicalMeasuresInvalidLanguage = "{ \"request\": {\"language_id\" : \"it\",\"words\": [\"आप\", \"मिल\", \"ख़ुशी\", \"अच्छा\"]}}";
	
	@Test
	public void  getLexileMeasuresSentenceExpectSuccess200() {
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
			body(jsonForLexicalMeasuresSentencesValid).
		with().
			contentType(JSON).
		when().
			post("v1/language/tools/lexileMeasures").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	
	@Test
	public void  getLexileMeasuresWordsExpectSuccess200() {
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
			body(jsonForLexicalMeasuresWordsValid).
		with().
			contentType(JSON).
		when().
			post("v1/language/tools/lexileMeasures").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	@Test
	public void  getLexileMeasuresInvalidLanguageExpect4xx() {
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
			body(jsonForLexicalMeasuresInvalidLanguage).
		with().
			contentType(JSON).
		when().
			post("v1/language/tools/lexileMeasures").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	
	
}
