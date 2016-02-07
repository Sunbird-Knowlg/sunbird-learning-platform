package org.ekstep.platform.language;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import org.ekstep.platform.domain.BaseTest;
import org.junit.Test;

public class ParserAPITests extends BaseTest {
	
	String jsonParseHindiSentence = "{ \"request\": {\"language_id\" : \"hi\",\"wordSuggestions\": true,\"relatedWords\": true,\"translations\": true,\"equivalentWords\": true,\"content\": \"आप से मिल के ख़ुशी हुई, आप से मिल के अच्छा लगा।\"}}";
	String jsonParseInvalidLanguageSentence = "{ \"request\": {\"language_id\" : \"it\",\"wordSuggestions\": true,\"relatedWords\": true,\"translations\": true,\"equivalentWords\": true,\"content\": \"आप से मिल के ख़ुशी हुई, आप से मिल के अच्छा लगा।\"}}";
	
	//Parse Hindi Sentence
		@Test
		public void  praseSentenceExpectSuccess200() {
			setURI();
			given().
			spec(getRequestSpec(contentType, validuserId)).
				body(jsonParseHindiSentence).
			with().
				contentType(JSON).
			when().
				post("v1/language/parser").
			then().
				log().all().
				spec(get200ResponseSpec());
		}
		
		@Test
		public void  praseSentenceInvalidLanguageExpect400() {
			setURI();
			given().
			spec(getRequestSpec(contentType, validuserId)).
				body(jsonParseInvalidLanguageSentence).
			with().
				contentType(JSON).
			when().
				post("v1/language/parser").
			then().
				log().all().
				spec(get400ResponseSpec());
		}
		


	
}
