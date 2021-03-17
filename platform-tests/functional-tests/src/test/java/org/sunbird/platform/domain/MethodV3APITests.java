package org.sunbird.platform.domain;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;


@Ignore
public class MethodV3APITests extends BaseTest {

	String JsonMethodSearchWithTagAndCode = "{ \"request\": {\"search\": {\"tags\": [\"QA\"],\"code\": \"LD4\" }}}";
	String JsonMethodSearchWithTag = "{ \"request\": {\"search\": {\"tags\": [\"QA\"] }}}";
	String JsonSaveMethodValid ="{\"request\":{ \"object\":{ \"description\":\"See pictures and arrange in collections QA\",\"name\":\"Sort Collections QA\",\"code\":\"LP_FT_"+generateRandomInt(99, 99999)+"\",\"identifier\":\"LP_FT_"+generateRandomInt(99, 99999)+"\",\"skills\": [\"QA1\", \"QA2\"],\"material\": [\"m1\", \"m2\"],\"complexity\": [\"c1\", \"c2\"],\"cognitiveProcessing\": [\"co1\", \"co2\"],\"interactivity\": [\"i1\", \"i2\"],\"parent\": [{\"identifier\": \"LM1\"}]}}}";
	String JsonSaveMethodWithEmptyParent = "{\"request\":{ \"object\":{ \"description\":\"See pictures and arrange in collections QA\",\"name\":\"Sort Collections QA\",\"code\":\"LP_FT_"+generateRandomInt(99, 99999)+"\",\"identifier\":\"LP_FT_"+generateRandomInt(99, 99999)+"\",\"skills\": [\"QA1\", \"QA2\"],\"material\": [\"m1\", \"m2\"],\"complexity\": [\"c1\", \"c2\"],\"cognitiveProcessing\": [\"co1\", \"co2\"],\"interactivity\": [\"i1\", \"i2\"],\"parent\": [{\"identifier\": \"\"}]}}}";
	String JsonSaveMethodWithInvalidParent = "{\"request\":{ \"object\":{ \"description\":\"See pictures and arrange in collections QA\",\"name\":\"Sort Collections QA\",\"code\":\"LP_FT_"+generateRandomInt(99, 99999)+"\",\"identifier\":\"LP_FT_"+generateRandomInt(99, 99999)+"\",\"skills\": [\"QA1\", \"QA2\"],\"material\": [\"m1\", \"m2\"],\"complexity\": [\"c1\", \"c2\"],\"cognitiveProcessing\": [\"co1\", \"co2\"],\"interactivity\": [\"i1\", \"i2\"],\"parent\": [{\"identifier\": \"num\"}]}}}"; 
	String JsonSaveMethodWithConcepts = "{\"request\":{ \"object\":{ \"description\":\"See pictures and arrange in collections QA\",\"name\":\"Sort Collections QA\",\"code\":\"LP_FT_"+generateRandomInt(99, 99999)+"\",\"identifier\":\"LP_FT_"+generateRandomInt(99, 99999)+"\",\"skills\": [\"QA1\", \"QA2\"],\"material\": [\"m1\", \"m2\"],\"complexity\": [\"c1\", \"c2\"],\"cognitiveProcessing\": [\"co1\", \"co2\"],\"interactivity\": [\"i1\", \"i2\"],\"parent\": [{\"identifier\": \"LM1\"}],\"concepts\": [{\"identifier\": \"Num:C3:SC1\"}]}}}";
	String JsonSaveMethodWithDomainAsConceptAndWithNonExistingConcept ="{\"request\":{ \"object\":{ \"description\":\"See pictures and arrange in collections QA\",\"name\":\"Sort Collections QA\",\"code\":\"LP_FT_"+generateRandomInt(99, 99999)+"\",\"identifier\":\"LP_FT_"+generateRandomInt(99, 99999)+"\",\"skills\": [\"QA1\", \"QA2\"],\"material\": [\"m1\", \"m2\"],\"complexity\": [\"c1\", \"c2\"],\"cognitiveProcessing\": [\"co1\", \"co2\"],\"interactivity\": [\"i1\", \"i2\"],\"parent\": [{\"identifier\": \"Num:C1:QA_test\"}],\"concepts\": [{\"identifier\": \"literacy\"},{\"identifier\": \"concept_act\"}]}}}";
	String JsonUpdateMethodValid = "{\"request\":{ \"object\":{ \"description\":\"Updated QA\",\"name\":\"Sort Collections QA updated\",\"skills\": [\"QA1\", \"QA2\"],\"material\": [\"m1\", \"m2\"],\"complexity\": [\"c1\", \"c2\"],\"cognitiveProcessing\": [\"cp1\", \"cp2\"],\"interactivity\": [\"in1\", \"in2\"],\"parent\": [{\"identifier\": \"Num:C1:QA\"}]}}}";

	@Before
	public void delay(){
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	// Get literacy methods list 
	@Test
	public void getLiteracyMethodsExpectSuccess200()
	{ 
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		get("/domain/v3/literacy/methods/list").
		then().
		//log().all().
		spec(get200ResponseSpec());
		//body("result.dimensions.status", hasItems(liveStatus));
	}

	// Get numeracy methods list
	@Ignore
	public void getMethodsNumeracyExpectSuccess200()
	{
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		get("/domain/v3/numeracy/methods/list").
		then().
		//log().all().
		spec(get200ResponseSpec());
		//body("result.dimensions.status", hasItems(liveStatus));
	}

	// Get science methods list
	@Test
	public void getMethodScienceExpectSuccess200(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		get("/domain/v3/literacy/methods/list").
		then().
		//log().all().
		spec(get200ResponseSpec());
		//body("result.dimensions.status", hasItems(liveStatus));
	}

	// Read valid method
	@Test
	public void getMethodExpectSuccess200()
	{
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		get("/domain/v3/literacy/methods/read/LM1").
		then().
		spec(get200ResponseSpec());
	}

	// Read methods with invalid domain
	@Test
	public void getMethodsInvalidDomainExpect404()
	{
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		get("/learning/v2/domains/abc/methods").
		then().
		spec(get404ResponseSpec());
	}

	// Read invalid method
	@Test
	public void getMethodsInvalidIdExpect404()
	{
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		get("/domain/v3/literacy/methods/read/xyz").
		then().
		spec(get404ResponseSpec());
	}

	//Create Literacy Method Valid
	@Test
	public void createLiteracyMethodExpectSuccess200()
	{
		//saveMethod API call 
		setURI();
		Response response1 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(JsonSaveMethodValid).
				with().
				contentType(JSON).
				when().
				post("/domain/v3/literacy/methods/create").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response(); 

		//getting the identifier of created method
		JsonPath jp1 = response1.jsonPath();
		String methodId = jp1.get("result.node_id");


		//getMethod API call to verify if the above method has been created. 
		setURI();
		try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);} 
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		get("/domain/v3/literacy/methods/read/"+methodId).
		then().
		//log().all().
		spec(get200ResponseSpec());		
	}
	
	// Create and read numeracy method
	@Test
	public void createNumeracyMethodExpectSuccess200(){
		//saveMethod API call 
		setURI();
		Response response1 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(JsonSaveMethodValid).
				with().
				contentType(JSON).
				when().
				post("/domain/v3/numeracy/methods/create").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response(); 

		//getting the identifier of created method
		JsonPath jp1 = response1.jsonPath();
		String methodId = jp1.get("result.node_id");


		//getMethod API call to verify if the above method has been created. 
		setURI();
		try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);} 
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		get("/domain/v3/numeracy/methods/read/"+methodId).
		then().
		//log().all().
		spec(get200ResponseSpec());		
	}
	
	// Create and read science method
	@Test
	public void createScienceMethodExpectSuccess200(){
		setURI();
		Response response1 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(JsonSaveMethodValid).
				with().
				contentType(JSON).
				when().
				post("/domain/v3/science/methods/create").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response(); 

		//getting the identifier of created method
		JsonPath jp1 = response1.jsonPath();
		String methodId = jp1.get("result.node_id");


		//getMethod API call to verify if the above method has been created. 
		setURI();
		try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);} 
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		get("/domain/v3/science/methods/read/"+methodId).
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
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(JsonSaveMethodWithConcepts).
				with().
				contentType(JSON).
				when().
				post("/domain/v3/literacy/methods/create").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response(); 

		//getting the identifier of created method
		JsonPath jp1 = response1.jsonPath();
		String methodId = jp1.get("result.node_id");


		//getMethod API call to verify if the above method has been created. 
		setURI();
		try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);} 
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		get("/domain/v3/literacy/methods/read/"+methodId).
		then().
		//log().all().
		spec(get200ResponseSpec());		
	}

	// Create method with invalid concept
	@Test
	public void createMethodWithDomainAsConceptAndWithInvalidConceptExpect4xx()
	{
		//saveMethod API call 
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(JsonSaveMethodWithDomainAsConceptAndWithNonExistingConcept).
		with().
		contentType(JSON).
		when().
		post("/domain/v3/literacy/methods/create").
		then().
		//log().all().
		spec(get400ResponseSpec());			
	}

	// Create method with empty parent
	@Test
	public void createMethodWithEmptyParentExpect4xx()
	{
		//saveMethod API call 
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(JsonSaveMethodWithEmptyParent).
		with().
		contentType(JSON).
		when().
		post("/domain/v3/literacy/methods/create").
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
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(JsonSaveMethodWithInvalidParent).
		with().
		contentType(JSON).
		when().
		post("/domain/v3/literacy/methods/create").
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
		spec(getRequestSpecification(contentType, userId, APIToken)).
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