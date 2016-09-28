package org.ekstep.platform.language;

import org.ekstep.platform.domain.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;


public class LessonComplexityTestCases extends BaseTest{
	
	String jsonUpdateSingleTextbook = "";
	String jsonUpdateMultipleTextbook = "";
	String jsonUpdateInvalidComplexityLanguage = "";
	String jsonUpdateInvalidComplexityGrade = "";
	String jsonUpdateNonIndianLanguage = "";
	String jsonTextAnalysisValid = "";
	
	// Get Grade level complexity with valid language
	
	@Test
	public void getGradeLevelComplexityValidExpectSuccess200(){
		
		// Update hindi language with single textbook
		
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body("").
		with().
			contentType(JSON).
		when().
			patch("/v1/language/tools/gradeLevelComplexity/hi/").
		then().
			spec(get200ResponseSpec());

		// Get complexity for same language
		
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().	
			get("/v1/language/tools/gradeLevelComplexity/hi").
		then().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extract the response
		JsonPath jPath = R.jsonPath();
		
	}
	
	// Get Grade level complexity with invalid language
	
	@Test
	public void getGradeLevelComplexityInvalidExpect400(){
		
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/v1/language/tools/gradeLevelComplexity/hda").
		then().
			spec(get400ResponseSpec());
	}
	
	// Update grade level complexity with single textbook
	
	@Test
	public void updateComplexitySingleTextbookExpectSuccess200(){
		
		setURI();
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body("").
		with().
			contentType(JSON).
		when().
			patch("/v1/language/tools/gradeLevelComplexity/hi/").
		then().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extract the response
		JsonPath jPath = R.jsonPath();
		Float avgComplexity = jPath.get("result.text_complexity.averageTotalComplexity");
		System.out.println(avgComplexity);		
	}
	
	// Update grade level complexity with multiple textbook

	@Test 
	public void updateComplexityMultipleTextbooksExpectSuccess200(){
		
		// Update the complexity value with single book
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body("").
		with().
			contentType(JSON).
		when().
			post("/v1/language/tools/gradeLevelComplexity/hi/").
		then().
			spec(get200ResponseSpec());
		
		//Get the complexity value
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().	
			get("/v1/language/tools/gradeLevelComplexity/hi").
		then().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extract the response
		JsonPath jPath = R.jsonPath();
		Float avgComplexity = jPath.get("result.text_complexity.averageTotalComplexity");
		System.out.println(avgComplexity);
		
		// Update text of multiple textbooks
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body("").
		with().
			contentType(JSON).
		when().
			post("/v1/language/tools/gradeLevelComplexity/hi/").
		then().
			spec(get200ResponseSpec());
		
		//Get the complexity value
		setURI();
		Response R2 = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().	
			get("/v1/language/tools/gradeLevelComplexity/hi").
		then().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extract the response
		JsonPath jPath2 = R2.jsonPath();
		Float avgComplexity2 = jPath2.get("result.text_complexity.averageTotalComplexity");
		Assert.assertFalse(avgComplexity.equals(avgComplexity2));
		System.out.println(avgComplexity2);
	}
	
	// Update grade level complexity with value of second language higher than first language
	
	@Test
	public void updateInvalidComplexityValuesWithLanguage(){

		// Update the complexity value for first language	
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body("").
		with().
			contentType(JSON).
		when().
			post("/v1/language/tools/gradeLevelComplexity/hi/").
		then().
			spec(get200ResponseSpec());
		
		//Get the complexity value
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().	
			get("/v1/language/tools/gradeLevelComplexity/hi").
		then().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extract the response
		JsonPath jPath = R.jsonPath();
		Float avgComplexity = jPath.get("result.text_complexity.averageTotalComplexity");
		System.out.println(avgComplexity);
		
		// Update text the complexity value for second language(higher than first language)
		
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body("").
		with().
			contentType(JSON).
		when().
			post("/v1/language/tools/gradeLevelComplexity/hi/").
		then().
			spec(get400ResponseSpec());
		
	}

	// Update complexity with value of grade-1 first language higher than grade-2 first language
	
	@Test
	public void updateInvalidComplexitValuesWithGrade(){
		
		// Update the complexity value for grade-2 first language	
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body("").
		with().
			contentType(JSON).
		when().
			post("/v1/language/tools/gradeLevelComplexity/hi/").
		then().
			spec(get200ResponseSpec());
		
		//Get the complexity value
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().	
			get("/v1/language/tools/gradeLevelComplexity/hi").
		then().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extract the response
		JsonPath jPath = R.jsonPath();
		Float avgComplexity = jPath.get("result.text_complexity.averageTotalComplexity");
		System.out.println(avgComplexity);
		
		// Update text the complexity value for grade-1 first language(higher than grade-2)
		
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body("").
		with().
			contentType(JSON).
		when().
			post("/v1/language/tools/gradeLevelComplexity/hi/").
		then().
			spec(get400ResponseSpec());		
	}
	
	// Update complexity with non-indian language
	
	@Test
	public void updateComplexityWithNonIndianLanguageExpect4xx(){
		
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body("").
		with().
			contentType(JSON).
		when().
			post("/v1/language/tools/gradeLevelComplexity/hi/").
		then().
			spec(get400ResponseSpec());		
	}
	
	// Text analysis with text lies within the complexity value's range
	
	@Test
	public void textAnalysisWithValidTextExpectSuccess200(){
		
		setURI();
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body("").
		with().
			contentType(JSON).
		when().
			post("/v1/language/tools/textAnalysis/hi/text/").
		then().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jPath = R.jsonPath();
		String avgComplexity = jPath.get("result.text_complexity.averageTotalComplexity");
	}
	
	// Text analysis with text lies within the complexity value's range of more than one grade

	@Test
	public void textAnalysisValidTextMultipleGradesExpectSuccess200() {
		setURI();
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body("").
		with().
			contentType(JSON).
		when().
			post("/v1/language/tools/textAnalysis/hi/text/").
		then().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jPath = R.jsonPath();
		String avgComplexity = jPath.get("result.text_complexity.averageTotalComplexity");
	}
	
	// Text analysis with text which doesn't have complexity values
	@Test
	public void textAnalysisForTextWithNoComplexityExpectSuccess200() {
		
		setURI();
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body("").
		with().
			contentType(JSON).
		when().
			post("/v1/language/tools/textAnalysis/ka/text/").
		then().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jPath = R.jsonPath();
		String avgComplexity = jPath.get("result.text_complexity.averageTotalComplexity");
	}
	
	// Text analysis with text which doesn't support complexity calculation
	
	@Test
	public void textAnlaysisForUnsupportedLanguageExpect4xx(){
		
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body("").
		with().
			contentType(JSON).
		when().
			post("/v1/language/tools/textAnalysis/en/text/").
		then().
			spec(get400ResponseSpec());
	}	
}
