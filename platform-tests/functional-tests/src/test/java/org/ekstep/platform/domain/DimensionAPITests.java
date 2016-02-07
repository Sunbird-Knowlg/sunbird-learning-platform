package org.ekstep.platform.domain;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.equalTo;
import org.junit.Test;


public class DimensionAPITests extends BaseTest {

	String JsonInPutForDimensionSearchWithTagAndCode = "{ \"request\": {\"search\": {\"tags\": [\"Dimension\"],\"code\": \"LD4\" }}}";
	String JsonInPutForDimensionSearchWithTag = "{ \"request\": {\"search\": {\"tags\": [\"Dimension\"] }}}";
	
	
	String JsonSaveDimensionValid = "{\"request\":{\"object\":{ \"description\":\"Dimension_Valid_TEST\",\"name\":\"LD1_TEST\",\"code\":\"Lit:Dim:1Test\",\"identifier\":\"STATISTICS_TEST\",\"tags\":[\"Class QA\"],\"parent\": [{\"identifier\": \"literacy\"}]}}}";
	String JsonSaveDimensionWithEmptyParent = "{\"request\":{\"object\":{ \"description\":\"Dimension_With_Empty Parent_TEST\",\"name\":\"LD_TEST1\",\"code\":\"Lit:Dim:1Test\",\"identifier\":\"LD_TEST_EMPTY_PARENT10\",\"tags\":[\"Class QA\"],\"parent\": [{\"identifier\": \"\"}]}}}";
	String JsonSaveDimensionWithInvalidParent = "{\"request\":{\"object\":{ \"description\":\"Dimension With No Parent TEST\",\"name\":\"LD1_TEST2\",\"code\":\"Lit:Dim:2Test\",\"identifier\":\"LD_TEST_NON_PARENT\",\"tags\":[\"Class QA\"],\"parent\": [{\"identifier\": \"num\"}]}}}";
	
	String JsonUpdateDimensionValid = "{\"request\":{\"object\":{ \"description\":\"Dimension_Valid_TEST Updated\",\"name\":\"LD1_TEST_U\",\"code\":\"Lit:Dim:1TestU\",\"identifier\":\"STATISTICS_TEST\",\"tags\":[\"Class QA\"],\"parent\": [{\"identifier\": \"literacy\"}]}}}";
	
	/***
	 * The following are the positive tests on getDimensions, searchDimensions and getDimension API calls. 
	 */
	@Test
	public void getDimensionsExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains/numeracy/dimensions").
		then().
			spec(get200ResponseSpec()).
	        body("result.dimensions.status", hasItems(liveStatus));
	}
	
	@Test
	public void getDimensionExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains/literacy/dimensions/LD6").
		then().
			spec(get200ResponseSpec());
	}
	
	
	
	/***
	 * The following are the negative tests on getDimensions and getDimension API calls. 
	 */
	@Test
	public void getDimensionsInvalidDomainExpect404()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains/abc/dimensions").
		then().
			spec(get404ResponseSpec());
	}

	@Test
	public void getDimensionInvalidIdExpect404()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains/literacy/dimensions/xyz").
		then().
			spec(get404ResponseSpec());
	}
	
	//Create Dimension Valid
	@Test
	public void createDimensionExpectSuccess()
	{
		//saveDimension API call 
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonSaveDimensionValid).
			with().
				contentType(JSON).
		when().
			post("v2/domains/literacy/dimensions").
		then().
			log().all().
			spec(get200ResponseSpec());
			//body("id", equalTo(""))		
		
		//getDimension API call to verify if the above dimension has been created. 
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains/literacy/dimensions/LD1_TEST").
		then().
			spec(get200ResponseSpec());		
	}
	
	
	@Test
	public void createDimensionWithEmptyParentExpect4xx()
	{
		//saveDimension API call 
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonSaveDimensionWithEmptyParent).
			with().
				contentType(JSON).
		when().
			post("v2/domains/literacy/dimensions").
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
			get("v2/domains/literacy/dimensions/LD_TEST_EMPTY_PARENT10").
		then().
			log().all().
			spec(get400ResponseSpec());			
	}
	
	@Test
	public void createDimensionWithNonExistingParentExpect4xx()
	{
		//saveDimension API call 
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonSaveDimensionWithInvalidParent).
			with().
				contentType(JSON).
		when().
			post("v2/domains/literacy/dimensions").
		then().
			log().all().
			spec(get400ResponseSpec()).
			spec(verify400DetailedResponseSpec("Failed to update relations and tags", 
					"CLIENT_ERROR",""));
				
		// verify that the dimension is not saved in DB. 
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains/literacy/dimensions/LD_TEST_NON_PARENT").
		then().
			spec(get400ResponseSpec());		

	}
	
	//Search Dimension
	
	@Test
	public void searchDimensionsExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonInPutForDimensionSearchWithTagAndCode).
			with().
				contentType("application/json").
			when().
				post("v2/domains/literacy/dimensions/search").
			then().
			log().all().
			spec(get200ResponseSpec());
			//body("id", equalTo("orchestrator.searchDomainObjects"));
	}
	
	//Update Dimension
	@Test
	public void updateDimensionValidInputsExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonUpdateDimensionValid).
			with().
				contentType("application/json").
			when().
				patch("v2/domains/literacy/dimensions/LD01").
			then().
				log().all().
				spec(get200ResponseSpec());
			//body("id", equalTo("orchestrator.searchDomainObjects"));
	}
	
	
	
	//Merge Dimension - TO-DO: Later
	
	
	
}
