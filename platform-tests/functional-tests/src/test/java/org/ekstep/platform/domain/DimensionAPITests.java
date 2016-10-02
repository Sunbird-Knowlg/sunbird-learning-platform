package org.ekstep.platform.domain;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.hasItems;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;


public class DimensionAPITests extends BaseTest {

	String JsonInPutForDimensionSearchWithTagAndCode = "{ \"request\": {\"search\": {\"tags\": [\"Dimension\"],\"code\": \"LD4\" }}}";
	String JsonInPutForDimensionSearchWithTag = "{ \"request\": {\"search\": {\"tags\": [\"Dimension\"] }}}";
	String JsonSaveDimensionValid = "{\"request\":{\"object\":{ \"description\":\"Dimension_Valid_TEST\",\"name\":\"LD1_TEST\",\"code\":\"Lit:Dim:1Test\",\"identifier\":\"LD_TEST_"+generateRandomInt(0, 500)+"\",\"tags\":[\"Class QA\"],\"parent\": [{\"identifier\": \"literacy\"}]}}}";
	String JsonSaveDimensionWithEmptyParent = "{\"request\":{\"object\":{ \"description\":\"Dimension_With_Empty Parent_TEST\",\"name\":\"LD_TEST1\",\"code\":\"Lit:Dim:1Test\",\"identifier\":\"LD_TEST_EMPTY_PARENT_"+generateRandomInt(0, 500)+"\",\"tags\":[\"Class QA\"],\"parent\": [{\"identifier\": \"\"}]}}}";
	String JsonSaveDimensionWithInvalidParent = "{\"request\":{\"object\":{ \"description\":\"Dimension With No Parent TEST\",\"name\":\"LD1_TEST2\",\"code\":\"Lit:Dim:2Test\",\"identifier\":\"LD_TEST_NON_PARENT_"+generateRandomInt(0, 500)+"\",\"tags\":[\"Class QA\"],\"parent\": [{\"identifier\": \"nudcam\"}]}}}";
	String JsonUpdateDimensionValid = "{\"request\":{\"object\":{ \"description\":\"Dimension_Valid_TEST Updated\",\"name\":\"LD1_TEST_U\",\"code\":\"Lit:Dim:1TestU\",\"tags\":[\"Class QA\"],\"parent\": [{\"identifier\": \"literacy\"}]}}}";
	
	/***
	 * The following are the positive tests on getDimensions and getDimension API calls. 
	 */
	@Test
	public void getDimensionsExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("/learning/v2/domains/numeracy/dimensions").
		then().
			spec(get200ResponseSpec()).
	        body("result.dimensions.status", hasItems("Live"));
	}
	
	@Test
	public void getDimensionExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("/learning/v2/domains/numeracy/dimensions/Num:C3").
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
			get("/learning/v2/domains/abc/dimensions").
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
			get("/learning/v2/domains/literacy/dimensions/xyz").
		then().
			spec(get404ResponseSpec());
	}
	
	//Create Dimension 
	@Test
	public void createDimensionExpectSuccess200()
	{
		//saveDimension API call 
		setURI();
		Response response1 = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonSaveDimensionValid).
			with().
				contentType(JSON).
		when().
			post("/learning/v2/domains/literacy/dimensions").
		then().
			//log().all().
			spec(get200ResponseSpec()).
		extract().
	    	response(); 
		
		//getting the identifier of created Dimension
		JsonPath jp1 = response1.jsonPath();
		String dimensionId = jp1.get("result.node_id");
		
		//getDimension API call to verify if the above dimension has been created.
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("/learning/v2/domains/literacy/dimensions/"+dimensionId).
		then().
			//log().all().
			spec(get200ResponseSpec());		
	}
	
	
	@Test
	public void createDimensionWithEmptyParentExpect400()
	{
		//saveDimension API call 
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonSaveDimensionWithEmptyParent).
			with().
				contentType(JSON).
		when().
			post("/learning/v2/domains/literacy/dimensions").
		then().
			//log().all()
			spec(get400ResponseSpec());
			/*spec(verify400DetailedResponseSpec("Failed to update relations and tags", "CLIENT_ERROR",""));
		
		//getting the identifier of Dimension from Json
		int startIndexOfIdentifier = JsonSaveDimensionWithEmptyParent.indexOf("identifier");
		int startIndexOfTags = JsonSaveDimensionWithEmptyParent.indexOf("tags");
		String dimensionId = JsonSaveDimensionWithEmptyParent.substring(startIndexOfIdentifier+13,startIndexOfTags-3);
	
		// verify that the dimension is not saved in DB. 
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("/learning/v2/domains/literacy/dimensions/"+dimensionId).
		then().
			//log().all().
			spec(get404ResponseSpec());	*/		
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
			post("/learning/v2/domains/literacy/dimensions").
		then().
			//log().all().
			spec(get400ResponseSpec());
			/*spec(verify400DetailedResponseSpec("Failed to update relations and tags","CLIENT_ERROR",""));
		
		//getting the identifier of Dimension from Json
		int startIndexOfIdentifier = JsonSaveDimensionWithInvalidParent.indexOf("identifier");
		int startIndexOfTags = JsonSaveDimensionWithInvalidParent.indexOf("tags");
		String dimensionId = JsonSaveDimensionWithInvalidParent.substring(startIndexOfIdentifier+13,startIndexOfTags-3);
		
		// verify that the dimension is not saved in DB. 
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("/learning/v2/domains/literacy/dimensions/"+dimensionId).
		then().
			spec(get404ResponseSpec());*/

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
			post("/learning/v2/domains/literacy/dimensions/search").
		then().
			//log().all().
			spec(get200ResponseSpec());
			//body("id", equalTo("orchestrator.searchDomainObjects"));
	}
	
	//Update Dimension
	@Test
	public void updateDimensionValidInputsExpectSuccess200()
	{
		//saveDimension API call 
		setURI();
		Response response1 = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonSaveDimensionValid).
		with().
			contentType(JSON).
		when().
			post("/learning/v2/domains/literacy/dimensions").
		then().
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response(); 
				
		//getting the identifier of created Dimension
		JsonPath jp1 = response1.jsonPath();
		String dimensionId = jp1.get("result.node_id");
		
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonUpdateDimensionValid).
		with().
			contentType("application/json").
		when().
			patch("/learning/v2/domains/literacy/dimensions/"+dimensionId).
		then().
			//log().all().
			spec(get200ResponseSpec());
			//body("id", equalTo("orchestrator.searchDomainObjects"));
	}
	
	//Merge Dimension - TO-DO: Later	
}