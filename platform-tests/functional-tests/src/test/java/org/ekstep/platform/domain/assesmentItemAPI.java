package org.ekstep.platform.domain;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import org.junit.Test;


public class assesmentItemAPI extends BaseTest {
	
	String jsonCreateAssessmentItemMCQ = "{\"request\":{\"assessment_item\": {\"identifier\": \"ek.n.q935 - QA6\",\"objectType\": \"/learning/v1/assessmentitem\",\"metadata\": {\"code\": \"ek.n.q935 - QA6\",\"name\": \"MCQ Question 2\",\"type\": \"mcq\",\"num_answers\": 1,\"template\": \"mcq_template_2\",\"qlevel\": \"MEDIUM\",\"owner\": \"username_1\",\"title\": \"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ.  ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\",\"question\":\"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ.             ಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\",\"model\":{\"img\":{\"type\":\"image\",\"asset\":\"perimeter\"},\"img2\":{\"type\":\"image\",\"asset\":\"smallSquare\"},\"subtext\":\"(= 1  ಚದರ ಸೆಂ.ಮೀ)\"},\"options\": [{\"value\": {\"type\": \"text\", \"asset\": \"12&10\", \"font\": \"Verdana\", \"color\": \"white\",  \"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\", \"asset\": \"14&7\", \"font\": \"Verdana\", \"color\": \"white\",  \"fontsize\": \"240\"}},	{\"value\": {\"type\": \"text\", \"asset\": \"16&8\", \"font\": \"Verdana\", \"color\": \"white\",  \"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\", \"asset\": \"12&7\", \"font\": \"Verdana\", \"color\": \"white\",  \"fontsize\": \"240\"},\"score\":1}],\"max_score\": 1,\"partial_scoring\": false,\"feedback\": \"\"},\"outRelations\": [{\"endNodeId\": \"Num:C1:SC1\",\"relationType\": \"associatedTo\"}]}}}";
	String jsonCreateInvalidAssessmentItemMCQ = "{\"request\":{\"assessment_item\": {\"identifier\": \"ek.n.q935 - QA5\",\"objectType\": \"/learning/v1/assessmentitem\",\"metadata\": {\"code\": \"ek.n.q935 - QA5\",\"name\": \"MCQ Question 2\",\"type\": \"mcq\",\"num_answers\": 1,\"template\": \"mcq_template_2\",\"qlevel\": \"\",\"owner\": \"username_1\",\"title\": \"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ.  ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\",\"question\":\"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ.             ಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\",\"model\":{\"img\":{\"type\":\"image\",\"asset\":\"perimeter\"},\"img2\":{\"type\":\"image\",\"asset\":\"smallSquare\"},\"subtext\":\"(= 1  ಚದರ ಸೆಂ.ಮೀ)\"},\"options\": [{\"value\": {\"type\": \"text\", \"asset\": \"12&10\", \"font\": \"Verdana\", \"color\": \"white\",  \"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\", \"asset\": \"14&7\", \"font\": \"Verdana\", \"color\": \"white\",  \"fontsize\": \"240\"}},	{\"value\": {\"type\": \"text\", \"asset\": \"16&8\", \"font\": \"Verdana\", \"color\": \"white\",  \"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\", \"asset\": \"12&7\", \"font\": \"Verdana\", \"color\": \"white\",  \"fontsize\": \"240\"},\"score\":1}],\"max_score\": 1,\"partial_scoring\": false,\"feedback\": \"\"},\"outRelations\": [{\"endNodeId\": \"Num:C1:SC1\",\"relationType\": \"associatedTo\"}]}}}";
	
	String jsonCreateAssessmentItemFTB = "{\"request\": {\"assessment_item\": {\"identifier\": \"ek.n.q932 - QA06\",\"objectType\": \"/learning/v1/assessmentitem\",\"metadata\": {\"identifier\": \"ek.n.q932 - QA06\",\"name\": \"FTB Question 1 - QA06\",\"template\": \"ftb_template_1\",\"type\": \"ftb\",\"num_answers\": 1,\"model\": {},\"answer\": {\"ans1\" : {\"value\" : 5, \"score\": 1}},\"code\": \"aq1_set_1_2\",\"qlevel\": \"MEDIUM\",\"owner\": \"EkStep\",\"used_for\": \"worksheet\",\"max_score\": 1,\"partial_scoring\": true},\"outRelations\": [{\"endNodeId\": \"Num:C1:SC1\",\"relationType\": \"associatedTo\"}]}}}";
	String jsonCreateInvalidAssessmentItemFTB = "{\"request\": {\"assessment_item\": {\"identifier\": \"ek.n.q932 - QA21\",\"objectType\": \"/learning/v1/assessmentitem\",\"metadata\": {\"identifier\": \"ek.n.q932 - QA21\",\"name\": \"FTB Question 1 - QA21\",\"template\": \"ftb_template_1\",\"type\": \"ftb\",\"num_answers\": 1,\"model\": {},\"answer\": {\"ans1\" : {\"value\" : 5, \"score\": 1}},\"code\": \"aq1_set_1_2\",\"qlevel\": \"MEDIUM\",\"owner\": \"EkStep\",\"used_for\": \"worksheet\",\"max_score\": 1,\"partial_scoring\": true},\"outRelations\": [{\"endNodeId\": \"Num:C1:SC1\",\"relationType\": \"associatedTo\"}]}}}";
	
	String jsonCreateAssessmentItemMTF = "{ \"request\": {\"assessment_item\": {  \"identifier\": \"ek.n.q933 - QA02\",  \"objectType\": \"/learning/v1/assessmentitem\", \"metadata\": {\"code\": \"ek.n.q933 - QA02\",\"name\": \"MTF Question 1 - QA02\",\"type\": \"mtf\",\"template\": \"mtf_template_3\",\"qlevel\": \"MEDIUM\",\"title\": \"ಕೊಟ್ಟಿರುವ ಸಂಖ್ಯೆಗಳನ್ನು ಇಳಿಕೆ ಕ್ರಮದಲ್ಲಿ ಜೋಡಿಸಿರಿ.\",\"question\":\"2080\",\"model\":{\"data0\":\"23450\",\"data1\":\"23540\"},\"lhs_options\": [{\"value\": {\"type\": \"image\", \"asset\": \"grey\"},\"index\": 0}],\"rhs_options\": [{\"value\": {\"type\": \"text\", \"asset\": \">\", \"font\": \"Verdana\", \"color\": \"white\",  \"fontsize\": \"1000\"}},{\"value\": {\"type\": \"text\", \"asset\": \"=\", \"font\": \"Verdana\", \"color\": \"white\",  \"fontsize\": \"1000\"}},{\"value\": {\"type\": \"text\", \"asset\": \"<\", \"font\": \"Verdana\", \"color\": \"white\",  \"fontsize\": \"1000\"},\"answer\": 0}],\"max_score\": 6,\"partial_scoring\": true,\"feedback\": \"\"}}}}";
	String jsonCreateInvalidAssessmentItemMTF = "{ \"request\": {\"assessment_item\": {  \"identifier\": \"ek.n.q933 - QA1\",  \"objectType\": \"/learning/v1/assessmentitem\", \"metadata\": {\"code\": \"ek.n.q933 - QA1\",\"name\": \"MTF Question 1 - QA1\",\"type\": \"mtf\",\"template\": \"mtf_template_3\",\"qlevel\": \"\",\"title\": \"ಕೊಟ್ಟಿರುವ ಸಂಖ್ಯೆಗಳನ್ನು ಇಳಿಕೆ ಕ್ರಮದಲ್ಲಿ ಜೋಡಿಸಿರಿ.\",\"question\":\"2080\",\"model\":{\"data0\":\"23450\",\"data1\":\"23540\"},\"lhs_options\": [{\"value\": {\"type\": \"image\", \"asset\": \"grey\"},\"index\": 0}],\"rhs_options\": [{\"value\": {\"type\": \"text\", \"asset\": \">\", \"font\": \"Verdana\", \"color\": \"white\",  \"fontsize\": \"1000\"}},{\"value\": {\"type\": \"text\", \"asset\": \"=\", \"font\": \"Verdana\", \"color\": \"white\",  \"fontsize\": \"1000\"}},{\"value\": {\"type\": \"text\", \"asset\": \"<\", \"font\": \"Verdana\", \"color\": \"white\",  \"fontsize\": \"1000\"},\"answer\": 0}],\"max_score\": 6,\"partial_scoring\": true,\"feedback\": \"\"}}}}";

	String jsonUpdateValidAssessment = "{\"request\":{\"assessment_item\": {\"identifier\": \"ek.n.q935 - QA6\",\"objectType\": \"/learning/v1/assessmentitem\",\"metadata\": {\"code\": \"ek.n.q935 - QA6\",\"name\": \"MCQ Question 2\",\"type\": \"mcq\",\"num_answers\": 1,\"template\": \"mcq_template_2\",\"qlevel\": \"MEDIUM\",\"owner\": \"username_1\",\"title\": \"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ.  ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\",\"question\":\"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ.             ಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\",\"model\":{\"img\":{\"type\":\"image\",\"asset\":\"perimeter\"},\"img2\":{\"type\":\"image\",\"asset\":\"smallSquare\"},\"subtext\":\"(= 1  ಚದರ ಸೆಂ.ಮೀ)\"},\"options\": [{\"value\": {\"type\": \"text\", \"asset\": \"12&10\", \"font\": \"Verdana\", \"color\": \"white\",  \"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\", \"asset\": \"14&7\", \"font\": \"Verdana\", \"color\": \"white\",  \"fontsize\": \"240\"}},	{\"value\": {\"type\": \"text\", \"asset\": \"16&8\", \"font\": \"Verdana\", \"color\": \"white\",  \"fontsize\": \"240\"}},{\"value\": {\"type\": \"text\", \"asset\": \"12&7\", \"font\": \"Verdana\", \"color\": \"white\",  \"fontsize\": \"240\"},\"score\":1}],\"max_score\": 1,\"partial_scoring\": false,\"feedback\": \"\"},\"outRelations\": [{\"endNodeId\": \"Num:C1:SC1\",\"relationType\": \"associatedTo\"}]}}}";
	
	String jsonSearchValidAssessmentItem = "{ \"request\": { \"metadata\": {\"filters\":[{\"property\" : \"type\", \"operator\": \"=\", \"value\": \"mcq\"},{\"property\" : \"owner\", \"operator\": \"=\", \"value\": \"username_1\"}]}}}";
	String jsonSearchInvalidAssessmentItem = "{ \"request\": { \"metadata\": {\"filters\":[{\"property\" : \"type\", \"operator\": \"!=\", \"value\": \"mcr\"},{\"property\" : \"owner\", \"operator\": \"=\", \"value\": \"ajsdghj\"}]}}}";
	
	
	
	//Create AssessmentItem MCQ
	@Test
	public void createAssessmentItemMCQExpectSuccess200() {
		setURI();
		given(). 
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateAssessmentItemMCQ).
		with().
			contentType(JSON).
		when().
			post("/learning/v1/assessmentitem").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	@Test
	public void createInvalidAssessmentItemMCQExpect400() {
		setURI();
		given(). 
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateInvalidAssessmentItemMCQ).
		with().
			contentType(JSON).
		when().
			post("/learning/v1/assessmentitem").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	//Create Assessment FTB
	@Test
	public void createAssessmentItemFTBExpectSuccess200() {
		setURI();
		given(). 
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateAssessmentItemFTB).
		with().
			contentType(JSON).
		when().
			post("/learning/v1/assessmentitem").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	@Test
	public void createInvalidAssessmentItemFTBExpect400() {
		setURI();
		given(). 
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateInvalidAssessmentItemFTB).
		with().
			contentType(JSON).
		when().
			post("/learning/v1/assessmentitem").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	//Create Assessment MTF
	@Test
	public void createAssessmentItemMTFExpectSuccess200() {
		setURI();
		given(). 
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateAssessmentItemMTF).
		with().
			contentType(JSON).
		when().
			post("/learning/v1/assessmentitem").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	@Test
	public void createInvalidAssessmentItemMTFExpectSuccess200() {
		setURI();
		given(). 
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateInvalidAssessmentItemMTF).
		with().
			contentType(JSON).
		when().
			post("/learning/v1/assessmentitem").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	//Update Assessment
	@Test
	public void updateValidAssessmentExpectSuccess200() {
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonUpdateValidAssessment).
		with().
			contentType("application/json").
		when().
			patch("/learning/v1/assessmentitem/ek.n.q932 - QA07").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	@Test
	public void updateInvalidAssessmentExpect400() {
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonUpdateValidAssessment).
		with().
			contentType("application/json").
		when().
			patch("/learning/v1/assessmentitem/ek.n.q932 - QA7678789").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	//Get Assessment
	@Test
	public void getValidAssessmentItemExpectSuccess200() {
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("learning/v1/assessmentitem/ek.n.q932 - QA21").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	@Test
	public void getInvalidAssessmentItemExpect400() {
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("/learning/v1/assessmentitem/ek.n.q932 - QA234").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	//Search Assessment
	@Test
	public void searchValidAssessmentItemExpectSuccess200() {
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonSearchValidAssessmentItem).
		with().
			contentType(JSON).
		when().
			post("learning/v1/assessmentitem/search").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	@Test
	public void searchInvalidValidAssessmentItemExpect400() {
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonSearchInvalidAssessmentItem).
		with().
			contentType(JSON).
		when().
			post("/learning/v1/assessmentitem/search").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	//Delete Assessment
	@Test
	public void deleteValidAssessmentExpectSuccess200() {
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			delete("/learning/v1/assessmentitem/ek.n.q932 - QA2").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	@Test
	public void deleteInvalidAssessmentExpect400() {
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			delete("/learning/v1/assessmentitem/ek.n.q932 - QA2").
		then().
			log().all().
			spec(get400ResponseSpec());
	}

}
