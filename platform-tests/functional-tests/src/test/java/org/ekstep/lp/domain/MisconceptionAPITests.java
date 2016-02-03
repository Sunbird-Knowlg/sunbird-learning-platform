
	
package org.ekstep.lp.domain;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.equalTo;
import org.junit.Test;


public class MisconceptionAPITests extends BaseTest {
	
	String JsonMisconceptionSearchWithTagAndCode = "{ \"request\": {\"search\": {\"tags\": [\"QA\"],\"code\": \"LD4\" }}}";
	String JsonMisconceptionSearchWithTag = "{ \"request\": {\"search\": {\"tags\": [\"QA\"] }}}";
	
	
	String JsonSaveMisconceptionValid ="{\"request\":{ \"object\":{ \"description\":\"Doesn't understand carry-over QA\",\"name\":\"Carry Over QA\",\"code\":\"Num:MC1:QA\",\"identifier\":\"Num:MC1:QA\",\"missing_concept\": \"Num:C1\"}}}";
			
	//TO-DO: once definition of method is confirmed, form these bodies.
	String JsonSaveMisconceptionWithEmptyParent = ""; 
	String JsonSaveMisconceptionWithInvalidParent = ""; 	
	String JsonUpdateMisconceptionValid = "{\"request\":{\"object\":{ \"description\":\"Dimension_Valid_TEST Updated\",\"name\":\"LD1_TEST_U\",\"code\":\"Lit:Dim:1TestU\",\"identifier\":\"STATISTICS_TEST\",\"tags\":[\"Class QA\"],\"parent\": [{\"identifier\": \"literacy\"}]}}}";
	
	
	@Test
	public void getMisconceptionsExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("domains/literacy/misconceptions").
		then().
			spec(get200ResponseSpec());
	        //body("result.dimensions.status", hasItems(liveStatus));
	}
	
	@Test
	public void getMisconceptionExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("domains/literacy/misconception/Num:MC1:QA").
		then().
			spec(get200ResponseSpec());
	}
	
	
	@Test
	public void getMisconceptionsInvalidDomainExpect404()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("domains/abc/misconceptions").
		then().
			spec(get404ResponseSpec());
	}

	@Test
	public void getMisconceptionsInvalidIdExpect404()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("domains/literacy/misconceptions/xyz").
		then().
			spec(get404ResponseSpec());
	}
	
	//Create Dimension Valid
	@Test
	public void createMisconceptionExpectSuccess()
	{
		//saveDimension API call 
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonSaveMisconceptionValid).
			with().
				contentType(JSON).
		when().
			post("domains/literacy/misconceptions").
		then().
			log().all().
			spec(get200ResponseSpec());
			//body("id", equalTo(""))		
		
		//getDimension API call to verify if the above dimension has been created. 
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("domains/literacy/misconceptions/Num:MC1:QA").
		then().
			spec(get200ResponseSpec());		
	}
	
	
	@Test
	public void createMisconceptionWithEmptyParentExpect4xx()
	{
		//saveDimension API call 
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonSaveMisconceptionWithEmptyParent).
			with().
				contentType(JSON).
		when().
			post("domains/literacy/misconceptions").
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
			get("domains/literacy/misconceptions/NUM:C1:QA2").
		then().
			log().all().
			spec(get400ResponseSpec());			
	}
	
	@Test
	public void createMisconceptionWithNonExistingParentExpect4xx()
	{
		//saveDimension API call 
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonSaveMisconceptionWithInvalidParent).
			with().
				contentType(JSON).
		when().
			post("domains/literacy/misconceptions").
		then().
			log().all().
			spec(get400ResponseSpec()).
			spec(verify400DetailedResponseSpec("Failed to update relations and tags", 
					"CLIENT_ERROR",""));
				
		// verify that the dimension is not saved in DB. 
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("domains/literacy/misconceptions/NUM:C1:QA3").
		then().
			spec(get400ResponseSpec());		

	}
	
	//Search Method
	
	@Test
	public void searchMisconceptionExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonMisconceptionSearchWithTagAndCode).
			with().
				contentType("application/json").
			when().
				post("domains/literacy/methods/search").
			then().
			log().all().
			spec(get200ResponseSpec());
			//body("id", equalTo("orchestrator.searchDomainObjects"));
	}
		
		//Update Dimension
		@Test
		public void updateMisconceptionValidInputsExpectSuccess200()
		{
			setURI();
			given().
				spec(getRequestSpec(contentType, validuserId)).
				body(JsonUpdateMisconceptionValid).
				with().
					contentType("application/json").
				when().
					patch("domains/literacy/methods/LD01").
				then().
					log().all().
					spec(get200ResponseSpec());
				//body("id", equalTo("orchestrator.searchDomainObjects"));
		}
		
		//Delete Method
		@Test
		public void deleteMisconceptionValidSuccess200()
		{
			setURI();
			given().
				spec(getRequestSpec(contentType, validuserId)).
			when().
				delete("domains/literacy/misconception/NUM:C1:M").
			then().
				log().all().
				spec(get200ResponseSpec());
				//body("id", equalTo("orchestrator.searchDomainObjects"));
		}
		
		//TO-DO: Get related misconceptions
		
}



