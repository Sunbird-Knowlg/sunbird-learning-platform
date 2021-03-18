package org.sunbird.platform.domain;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import org.junit.Ignore;
import org.junit.Test;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

@Ignore
public class MethodAPITests extends BaseTest {
	
	String JsonMethodSearchWithTagAndCode = "{ \"request\": {\"search\": {\"tags\": [\"QA\"],\"code\": \"LD4\" }}}";
	String JsonMethodSearchWithTag = "{ \"request\": {\"search\": {\"tags\": [\"QA\"] }}}";
	String JsonSaveMethodValid ="{\"request\":{ \"object\":{ \"description\":\"See pictures and arrange in collections QA\",\"name\":\"Sort Collections QA\",\"code\":\"LP_FT_"+generateRandomInt(0, 500)+"\",\"identifier\":\"LP_FT_"+generateRandomInt(0, 500)+"\",\"skills\": [\"QA1\", \"QA2\"],\"material\": [\"m1\", \"m2\"],\"complexity\": [\"c1\", \"c2\"],\"cognitiveProcessing\": [\"co1\", \"co2\"],\"interactivity\": [\"i1\", \"i2\"],\"parent\": [{\"identifier\": \"LM1\"}]}}}";
	String JsonSaveMethodWithEmptyParent = "{\"request\":{ \"object\":{ \"description\":\"See pictures and arrange in collections QA\",\"name\":\"Sort Collections QA\",\"code\":\"LP_FT_"+generateRandomInt(0, 500)+"\",\"identifier\":\"LP_FT_"+generateRandomInt(0, 500)+"\",\"skills\": [\"QA1\", \"QA2\"],\"material\": [\"m1\", \"m2\"],\"complexity\": [\"c1\", \"c2\"],\"cognitiveProcessing\": [\"co1\", \"co2\"],\"interactivity\": [\"i1\", \"i2\"],\"parent\": [{\"identifier\": \"\"}]}}}";
	String JsonSaveMethodWithInvalidParent = "{\"request\":{ \"object\":{ \"description\":\"See pictures and arrange in collections QA\",\"name\":\"Sort Collections QA\",\"code\":\"LP_FT_"+generateRandomInt(0, 500)+"\",\"identifier\":\"LP_FT_"+generateRandomInt(0, 500)+"\",\"skills\": [\"QA1\", \"QA2\"],\"material\": [\"m1\", \"m2\"],\"complexity\": [\"c1\", \"c2\"],\"cognitiveProcessing\": [\"co1\", \"co2\"],\"interactivity\": [\"i1\", \"i2\"],\"parent\": [{\"identifier\": \"num\"}]}}}"; 
	String JsonSaveMethodWithConcepts = "{\"request\":{ \"object\":{ \"description\":\"See pictures and arrange in collections QA\",\"name\":\"Sort Collections QA\",\"code\":\"LP_FT_"+generateRandomInt(0, 500)+"\",\"identifier\":\"LP_FT_"+generateRandomInt(0, 500)+"\",\"skills\": [\"QA1\", \"QA2\"],\"material\": [\"m1\", \"m2\"],\"complexity\": [\"c1\", \"c2\"],\"cognitiveProcessing\": [\"co1\", \"co2\"],\"interactivity\": [\"i1\", \"i2\"],\"parent\": [{\"identifier\": \"LM1\"}],\"concepts\": [{\"identifier\": \"Num:C3:SC1\"}]}}}";
	String JsonSaveMethodWithDomainAsConceptAndWithNonExistingConcept ="{\"request\":{ \"object\":{ \"description\":\"See pictures and arrange in collections QA\",\"name\":\"Sort Collections QA\",\"code\":\"LP_FT_"+generateRandomInt(0, 500)+"\",\"identifier\":\"LP_FT_"+generateRandomInt(0, 500)+"\",\"skills\": [\"QA1\", \"QA2\"],\"material\": [\"m1\", \"m2\"],\"complexity\": [\"c1\", \"c2\"],\"cognitiveProcessing\": [\"co1\", \"co2\"],\"interactivity\": [\"i1\", \"i2\"],\"parent\": [{\"identifier\": \"Num:C1:QA_test\"}],\"concepts\": [{\"identifier\": \"literacy\"},{\"identifier\": \"concept_act\"}]}}}";
	String JsonUpdateMethodValid = "{\"request\":{ \"object\":{ \"description\":\"Updated QA\",\"name\":\"Sort Collections QA updated\",\"skills\": [\"QA1\", \"QA2\"],\"material\": [\"m1\", \"m2\"],\"complexity\": [\"c1\", \"c2\"],\"cognitiveProcessing\": [\"cp1\", \"cp2\"],\"interactivity\": [\"in1\", \"in2\"],\"parent\": [{\"identifier\": \"Num:C1:QA\"}]}}}";
	
	@Test
	public void getMethodsExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("/learning/v2/domains/literacy/methods").
		then().
			//log().all().
			spec(get200ResponseSpec());
	        //body("result.dimensions.status", hasItems(liveStatus));
	}
	
	@Test
	public void getMethodExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("/learning/v2/domains/literacy/methods/LM1").
		then().
			spec(get200ResponseSpec());
	}
	
	@Test
	public void getMethodsInvalidDomainExpect404()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("/learning/v2/domains/abc/methods").
		then().
			spec(get404ResponseSpec());
	}

	@Test
	public void getMethodsInvalidIdExpect404()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("/learning/v2/domains/literacy/methods/xyz").
		then().
			spec(get404ResponseSpec());
	}
	
	//Create Method Valid
	@Test
	public void createMethodExpectSuccess200()
	{
		//saveMethod API call 
		setURI();
		Response response1 = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonSaveMethodValid).
			with().
				contentType(JSON).
		when().
			post("/learning/v2/domains/literacy/methods").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
	    	response(); 
		
		//getting the identifier of created method
		JsonPath jp1 = response1.jsonPath();
		String methodId = jp1.get("result.node_id");
				
		
		//getMethod API call to verify if the above method has been created. 
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("/learning/v2/domains/literacy/methods/"+methodId).
		then().
			//log().all().
			spec(get200ResponseSpec());		
	}
	
	@Test
	public void createMethodWithConceptExpectSuccess200()
	{
		//saveMethod API call 
		setURI();
		Response response1 = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonSaveMethodWithConcepts).
			with().
				contentType(JSON).
		when().
			post("/learning/v2/domains/literacy/methods").
		then().
			//log().all().
			spec(get200ResponseSpec()).
		extract().
	    	response(); 
		
		//getting the identifier of created method
		JsonPath jp1 = response1.jsonPath();
		String methodId = jp1.get("result.node_id");
				
		
		//getMethod API call to verify if the above method has been created. 
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("/learning/v2/domains/literacy/methods/"+methodId).
		then().
			//log().all().
			spec(get200ResponseSpec());		
	}
	
	@Test
	public void createMethodWithDomainAsConceptAndWithNonExistingConceptExpect4xx()
	{
		//saveMethod API call 
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonSaveMethodWithDomainAsConceptAndWithNonExistingConcept).
			with().
				contentType(JSON).
		when().
			post("/learning/v2/domains/literacy/methods").
		then().
			//log().all().
			spec(get400ResponseSpec());			
	}
	
	@Test
	public void createMethodWithEmptyParentExpect4xx()
	{
		//saveMethod API call 
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonSaveMethodWithEmptyParent).
			with().
				contentType(JSON).
		when().
			post("/learning/v2/domains/literacy/methods").
		then().
			//log().all().
			spec(get400ResponseSpec());
	}
	
	@Test
	public void createMethodWithNonExistingParentExpect4xx()
	{
		//saveMethod API call 
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonSaveMethodWithInvalidParent).
			with().
				contentType(JSON).
		when().
			post("/learning/v2/domains/literacy/methods").
		then().
			//log().all().
			spec(get400ResponseSpec());
	}
	
	//Search Method
	
	@Test
	public void searchMethodsExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonMethodSearchWithTagAndCode).
			with().
				contentType("application/json").
			when().
				post("/learning/v2/domains/literacy/methods/search").
			then().
			//log().all().
			spec(get200ResponseSpec());
			//body("id", equalTo("orchestrator.searchDomainObjects"));
	}
}