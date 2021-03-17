package org.sunbird.platform.domain;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.hasItems;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;


public class DimensionAPIV3Tests extends BaseTest {

	String JsonInPutForDimensionSearchWithTagAndCode = "{ \"request\": {\"search\": {\"tags\": [\"Dimension\"],\"code\": \"LD4\" }}}";
	String JsonInPutForDimensionSearchWithTag = "{ \"request\": {\"search\": {\"tags\": [\"Dimension\"] }}}";
	String JsonCreateLiteracyDimension = "{\"request\":{\"object\":{ \"description\":\"Dimension_Valid_TEST\",\"name\":\"LD1_TEST\",\"code\":\"Lit:Dim:1Test\",\"identifier\":\"LD_TEST_"+generateRandomInt(0, 50000)+"\",\"tags\":[\"Test_QA\"],\"parent\": [{\"identifier\": \"literacy\"}]}}}";
	String JsonCreateNumeracyDimension = "{\"request\":{\"object\":{ \"description\":\"Dimension_Valid_TEST\",\"name\":\"LD1_TEST\",\"code\":\"Lit:Dim:1Test\",\"identifier\":\"LD_TEST_"+generateRandomInt(0, 50000)+"\",\"tags\":[\"Test_QA\"],\"parent\": [{\"identifier\": \"numeracy\"}]}}}";
	String JsonCreateScienceDimension = "{\"request\":{\"object\":{ \"description\":\"Dimension_Valid_TEST\",\"name\":\"LD1_TEST\",\"code\":\"Lit:Dim:1Test\",\"identifier\":\"LD_TEST_"+generateRandomInt(0, 50000)+"\",\"tags\":[\"Test_QA\"],\"parent\": [{\"identifier\": \"science\"}]}}}";
	String JsonSaveDimensionWithEmptyParent = "{\"request\":{\"object\":{ \"description\":\"Dimension_With_Empty Parent_TEST\",\"name\":\"LD_TEST1\",\"code\":\"Lit:Dim:1Test\",\"identifier\":\"LD_TEST_EMPTY_PARENT_"+generateRandomInt(0, 500)+"\",\"tags\":[\"Class QA\"],\"parent\": [{\"identifier\": \"\"}]}}}";
	String JsonSaveDimensionWithInvalidParent = "{\"request\":{\"object\":{ \"description\":\"Dimension With No Parent TEST\",\"name\":\"LD1_TEST2\",\"code\":\"Lit:Dim:2Test\",\"identifier\":\"LD_TEST_NON_PARENT_"+generateRandomInt(0, 500)+"\",\"tags\":[\"Class QA\"],\"parent\": [{\"identifier\": \"nudcam\"}]}}}";
	String JsonUpdateDimensionValid = "{\"request\":{\"object\":{ \"description\":\"Dimension_Valid_TEST Updated\",\"name\":\"LD1_TEST_U\",\"tags\":[\"Class QA\"],\"parent\": [{\"identifier\": \"literacy\"}]}}}";
	
	/***
	 * The following are the positive tests on getDimensions and getDimension API calls. 
	 */
	
	@After
	public void delay(){
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	// Get dimensions list - Numeracy
	@Test
	public void getDimensionListNumeracyExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
			get("/domain/v3/numeracy/dimensions/list").
		then().
			//log().all().
			spec(get200ResponseSpec()).
			//body("results.dimensions.subject", hasItems("numeracy")).
	        body("result.dimensions.status", hasItems("Live"));
	}
	
	// Get dimensions list - Literacy
	
	@Test
	public void getDimensionListLiteracyExpectSuccess200(){
			setURI();
			given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
			when().
				get("/domain/v3/literacy/dimensions/list").
			then().
				//log().all().
				spec(get200ResponseSpec()).
				//body("results.dimensions.subject", hasItems("literacy")).
				body("result.dimensions.status", hasItems("Live"));
	}
	
	// Get Dimension list - Science
	
	@Test
	public void getDimensionListScienceExpectSuccess200(){
		setURI();
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
			get("/domain/v3/science/dimensions/list").
		then().
			//log().all().
			spec(get200ResponseSpec()).
			//body("results.dimensions.subject", hasItems("science")).
			body("result.dimensions.status", hasItems("Live"));
	}
	
	// Get Dimension list - Invalid
	
	@Ignore
	public void getDimensionListInvalidExpect4xx(){
		setURI();
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
			get("/domain/v3/afsf/dimensions/list").
		then().
			//log().all().
			spec(get404ResponseSpec());
			//body("results.dimensions.subject", hasItems("science")).
			//body("result.dimensions.status", hasItems("Live"));
	}
	
	@Test
	public void getDimensionExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
			get("/domain/v3/numeracy/dimensions/read/Num:C3").
		then().
			//log().all().
			spec(get200ResponseSpec());
	}
	
	/***
	 * The following are the negative tests on getDimensions and getDimension API calls. 
	 */
	@Ignore
	public void getDimensionsInvalidDomainExpect404()
	{
		setURI();
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
			get("/domain/v3/abc/dimensions/list").
		then().
			spec(get404ResponseSpec());
	}
	
	// 

	@Test
	public void getDimensionInvalidIdExpect404()
	{
		setURI();
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
			get("/domain/v3/literacy/dimensions/read/xyz").
		then().
			spec(get404ResponseSpec());
	}
	
	//Create literacy Dimension 
	
	@Test
	public void createLiteracyDimensionExpectSuccess200()
	{
		//saveDimension API call 
		setURI();
		Response response1 = 
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body(JsonCreateLiteracyDimension).
			with().
				contentType(JSON).
		when().
			post("/domain/v3/literacy/dimensions/create").
		then().
			////log().all().
			spec(get200ResponseSpec()).
		extract().
	    	response(); 
		
		//getting the identifier of created Dimension
		JsonPath jp1 = response1.jsonPath();
		String dimensionId = jp1.get("result.node_id");
		
		//getDimension API call to verify if the above dimension has been created.
		setURI();
		try{Thread.sleep(10000);}catch(InterruptedException e){System.out.println(e);} 				
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
			get("/domain/v3/literacy/dimensions/read/"+dimensionId).
		then().
			////log().all().
			spec(get200ResponseSpec());		
	}
	
	// Create dimension with existing dimension as parent
	
	@Test
	public void createDimensionWithDimensionAsParentExpect4xx(){
		
		setURI();
		Response response1 = 
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body(JsonCreateLiteracyDimension).
			with().
				contentType(JSON).
		when().
			post("/domain/v3/literacy/dimensions/create").
		then().
			////log().all().
		extract().
	    	response(); 
	
		//getting the identifier of created Dimension
		JsonPath jp1 = response1.jsonPath();
		String dimensionId = jp1.get("result.node_id");

	// Using created dimension as parent
		setURI();
		JsonCreateLiteracyDimension = JsonCreateLiteracyDimension.replace("literacy", dimensionId);
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(JsonCreateLiteracyDimension).
		with().
			contentType(JSON).
		when().
			post("/domain/v3/literacy/dimensions/create").
		then().
			////log().all().
		spec(get400ResponseSpec());
	}
	
	// Create numeracy dimension 
	
	@Test
	public void createNumeracyDimesionExpectSuccess200(){
		setURI();
		Response response1 = 
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body(JsonCreateNumeracyDimension).
			with().
				contentType(JSON).
		when().
			post("/domain/v3/numeracy/dimensions/create").
		then().
			////log().all().
			spec(get200ResponseSpec()).
		extract().
	    	response(); 
		
		//getting the identifier of created Dimension
		JsonPath jp1 = response1.jsonPath();
		String dimensionId = jp1.get("result.node_id");
		
		//getDimension API call to verify if the above dimension has been created.
		setURI();
		try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);} 				
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
			get("/domain/v3/numeracy/dimensions/read/"+dimensionId).
		then().
			////log().all().
			spec(get200ResponseSpec());		
	}
	
	// Create Science dimension
	
	@Test
	public void createScienceDimesionExpectSuccess200(){
		setURI();
		Response response1 = 
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body(JsonCreateScienceDimension).
			with().
				contentType(JSON).
		when().
			post("/domain/v3/science/dimensions/create").
		then().
			////log().all().
			spec(get200ResponseSpec()).
		extract().
	    	response(); 
		
		//getting the identifier of created Dimension
		JsonPath jp1 = response1.jsonPath();
		String dimensionId = jp1.get("result.node_id");
		
		//getDimension API call to verify if the above dimension has been created.
		try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);} 
		setURI();
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
			get("/domain/v3/science/dimensions/read/"+dimensionId).
		then().
			////log().all().
			spec(get200ResponseSpec());		
	}
	
	// Dimension with empty parent
	
	@Test
	public void createDimensionWithEmptyParentExpect400()
	{
		//saveDimension API call 
		setURI();
		Response R =
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body(JsonSaveDimensionWithEmptyParent).
			with().
				contentType(JSON).
		when().
			post("/domain/v3/literacy/dimensions/create").
		then().
			//log().all().
			spec(get400ResponseSpec()).
		//	spec(verify400DetailedResponseSpec("Failed to update relations and tags", "CLIENT_ERROR","")).
		extract().
			response();
		
		//getting the identifier of created Dimension
		JsonPath jp1 = R.jsonPath();
		String dimensionId = jp1.get("result.node_id");
		
		// verify that the dimension is not saved in DB. 
		try{Thread.sleep(10000);}catch(InterruptedException e){System.out.println(e);} 		
		setURI();
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
			get("/domain/v3/literacy/dimensions/read/"+dimensionId).
		then().
			//log().all().
			spec(get404ResponseSpec());		
	}
	
	// Dimension with non existing parent
	
	@Test
	public void createDimensionWithNonExistingParentExpect4xx()
	{
		//saveDimension API call 
		setURI();
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body(JsonSaveDimensionWithInvalidParent).
			with().
				contentType(JSON).
		when().
			post("/domain/v3/literacy/dimensions/create").
		then().
			//log().all().
			spec(get400ResponseSpec());
	}	
	
	//Update Dimension
	
	@Test
	public void updateDimensionValidInputsExpectSuccess200()
	{
		//saveDimension API call 
		setURI();
		Response response1 = 
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body(JsonCreateLiteracyDimension).
		with().
			contentType(JSON).
		when().
			post("/domain/v3/literacy/dimensions/create").
		then().
			////log().all().
			spec(get200ResponseSpec()).
		extract().
			response(); 
				
		//getting the identifier of created Dimension
		JsonPath jp1 = response1.jsonPath();
		String dimensionId = jp1.get("result.node_id");
		
		setURI();
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body(JsonUpdateDimensionValid).
		with().
			contentType("application/json").
		when().
			patch("/domain/v3/literacy/dimensions/update/"+dimensionId).
		then().
			////log().all().
			spec(get200ResponseSpec());
	}
	
	//Update Dimension with invalid path
	
	@Ignore
	public void updateDimensionInvalidPathExpect4xx()
	{
		//saveDimension API call 
		setURI();
		Response response1 = 
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body(JsonCreateLiteracyDimension).
		with().
			contentType(JSON).
		when().
			post("/domain/v3/literacy/dimensions/create").
		then().
			//log().all().
		extract().
			response(); 
				
		//getting the identifier of created Dimension
		JsonPath jp1 = response1.jsonPath();
		String dimensionId = jp1.get("result.node_id");
		
		setURI();
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body(JsonUpdateDimensionValid).
		with().
			contentType("application/json").
		when().
			patch("/domain/v3/literacy/update/"+dimensionId).
		then().
			//log().all().
			spec(get400ResponseSpec());
	}
	
	//Update Dimension with invalid content id
	
	@Ignore
	public void updateDimensionInvalidContentIdExpect4xx()
	{
		//saveDimension API call 
		setURI();
		Response response1 = 
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body(JsonCreateLiteracyDimension).
		with().
			contentType(JSON).
		when().
			post("/domain/v3/literacy/dimensions/create").
		then().
			////log().all().
		extract().
			response(); 
				
		//getting the identifier of created Dimension
		JsonPath jp1 = response1.jsonPath();
		String dimensionId = jp1.get("result.node_id");
		
		setURI();
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body(JsonUpdateDimensionValid).
		with().
			contentType("application/json").
		when().
			patch("/domain/v3/literacy/dimensions/update/"+dimensionId+"dfad").
		then().
			////log().all().
			spec(get400ResponseSpec());
	}
	
	//Update Dimension with invalid request
	
	@Test
	public void updateDimensionInvalidPathExpectSuccess200()
	{
		//saveDimension API call 
		setURI();
		Response response1 = 
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body(JsonCreateLiteracyDimension).
		with().
			contentType(JSON).
		when().
			post("/domain/v3/literacy/dimensions/create").
		then().
			////log().all().
		extract().
			response(); 
				
		//getting the identifier of created Dimension
		JsonPath jp1 = response1.jsonPath();
		String dimensionId = jp1.get("result.node_id");
		
		setURI();
		try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);} 		
		JsonUpdateDimensionValid = JsonUpdateDimensionValid.replace("literacy", "Test_1234");
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body(JsonUpdateDimensionValid).
		with().
			contentType("application/json").
		when().
			patch("/domain/v3/literacy/dimensions/update/"+dimensionId).
		then().
			////log().all().
			spec(get400ResponseSpec());
	}
}