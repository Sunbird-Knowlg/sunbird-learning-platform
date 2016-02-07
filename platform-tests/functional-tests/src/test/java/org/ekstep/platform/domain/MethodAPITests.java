package org.ekstep.platform.domain;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.equalTo;
import org.junit.Test;


public class MethodAPITests extends BaseTest {
	
	String JsonMethodSearchWithTagAndCode = "{ \"request\": {\"search\": {\"tags\": [\"QA\"],\"code\": \"LD4\" }}}";
	String JsonMethodSearchWithTag = "{ \"request\": {\"search\": {\"tags\": [\"QA\"] }}}";
	
	
	String JsonSaveMethodValid ="{\"request\":{ \"object\":{ \"description\":\"See pictures and arrange in collections QA\",\"name\":\"Sort Collections QA\",\"code\":\"Num:C1 QA\",\"identifier\":\"Num:C1:QA\",\"skills\": [\"QA1\", \"QA2\"],\"material\": [\"m1\", \"m2\"],\"complexity\": [\"c1\", \"c2\"],\"cognitiveProcessing\": [\"co1\", \"co2\"],\"interactivity\": [\"i1\", \"i2\"],\"parent\": [{\"identifier\": \"\"}]}}}";
			
	//TO-DO: once definition of method is confirmed, form these bodies.
	String JsonSaveMethodWithEmptyParent = ""; //"{\"request\":{\"object\":{ \"description\":\"Dimension_With_Empty Parent_TEST\",\"name\":\"LD_TEST1\",\"code\":\"Lit:Dim:1Test\",\"identifier\":\"LD_TEST_EMPTY_PARENT10\",\"tags\":[\"Class QA\"],\"parent\": [{\"identifier\": \"\"}]}}}";
	String JsonSaveMethodWithInvalidParent = ""; //"{\"request\":{\"object\":{ \"description\":\"Dimension With No Parent TEST\",\"name\":\"LD1_TEST2\",\"code\":\"Lit:Dim:2Test\",\"identifier\":\"LD_TEST_NON_PARENT\",\"tags\":[\"Class QA\"],\"parent\": [{\"identifier\": \"num\"}]}}}";
	
	String JsonUpdateMethodValid = "{\"request\":{\"object\":{ \"description\":\"Dimension_Valid_TEST Updated\",\"name\":\"LD1_TEST_U\",\"code\":\"Lit:Dim:1TestU\",\"identifier\":\"STATISTICS_TEST\",\"tags\":[\"Class QA\"],\"parent\": [{\"identifier\": \"literacy\"}]}}}";
	
	
	@Test
	public void getMethodsExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains/literacy/methods").
		then().
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
			get("v2/domains/literacy/methods/Method1").
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
			get("v2/domains/abc/methods").
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
			get("v2/domains/literacy/methods/xyz").
		then().
			spec(get404ResponseSpec());
	}
	
	//Create Dimension Valid
	@Test
	public void createMethodExpectSuccess()
	{
		//saveDimension API call 
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonSaveMethodValid).
			with().
				contentType(JSON).
		when().
			post("v2/domains/literacy/methods").
		then().
			log().all().
			spec(get200ResponseSpec());
			//body("id", equalTo(""))		
		
		//getDimension API call to verify if the above dimension has been created. 
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains/literacy/methods/Num:C1:QA").
		then().
			spec(get200ResponseSpec());		
	}
	
	
	@Test
	public void createMethodWithEmptyParentExpect4xx()
	{
		//saveDimension API call 
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonSaveMethodWithEmptyParent).
			with().
				contentType(JSON).
		when().
			post("v2/domains/literacy/methods").
		then().
			log().all().
			spec(get400ResponseSpec()).
			spec(verify400DetailedResponseSpec("Failed to update relations and tags", 
								"CLIENT_ERROR",""));
		
		
		// verify that the dimension is not saved in DB. 
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains/literacy/methods/NUM:C1:QA2").
		then().
			log().all().
			spec(get400ResponseSpec());			
	}
	
	@Test
	public void createMethodWithNonExistingParentExpect4xx()
	{
		//saveDimension API call 
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonSaveMethodWithInvalidParent).
			with().
				contentType(JSON).
		when().
			post("v2/domains/literacy/methods").
		then().
			log().all().
			spec(get400ResponseSpec()).
			spec(verify400DetailedResponseSpec("Failed to update relations and tags", 
					"CLIENT_ERROR",""));
				
		// verify that the dimension is not saved in DB. 
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains/literacy/methods/NUM:C1:QA3").
		then().
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
				post("v2/domains/literacy/methods/search").
			then().
			log().all().
			spec(get200ResponseSpec());
			//body("id", equalTo("orchestrator.searchDomainObjects"));
	}
		
		//Update Dimension
		@Test
		public void updateMethodValidInputsExpectSuccess200()
		{
			setURI();
			given().
				spec(getRequestSpec(contentType, validuserId)).
				body(JsonUpdateMethodValid).
				with().
					contentType("application/json").
				when().
					patch("v2/domains/literacy/methods/LD01").
				then().
					log().all().
					spec(get200ResponseSpec());
				//body("id", equalTo("orchestrator.searchDomainObjects"));
		}
		
		//Delete Method
		@Test
		public void deleteMethodValidSuccess200()
		{
			setURI();
			given().
				spec(getRequestSpec(contentType, validuserId)).
			when().
				delete("v2/domains/literacy/methods/NUM:C1:M").
			then().
				log().all().
				spec(get200ResponseSpec());
				//body("id", equalTo("orchestrator.searchDomainObjects"));
		}
	

}
