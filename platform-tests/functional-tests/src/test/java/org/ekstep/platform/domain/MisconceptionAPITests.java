	
package org.ekstep.platform.domain;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import org.junit.Ignore;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class MisconceptionAPITests extends BaseTest 
{
	
	String JsonMisconceptionSearchWithTagAndCode = "{ \"request\": {\"search\": {\"tags\": [\"QA\"],\"code\": \"LD4\" }}}";
	String JsonMisconceptionSearchWithTag = "{ \"request\": {\"search\": {\"tags\": [\"QA\"] }}}";
	String JsonMisconceptionSearchWithObjectType = "{ \"request\": {\"search\":{\"objectType\": \"Content\"}}}";
	String JsonSaveMisconceptionValid ="{\"request\":{ \"object\":{ \"description\":\"Doesn't understand carry-over QA\",\"name\":\"Carry Over QA\",\"code\":\"MC"+generateRandomInt(0, 500)+"\",\"identifier\":\"MC"+generateRandomInt(0, 500)+"\",\"missing_concept\": \"Num:C1\"}}}";		
	//TO-DO: once definition of misconception is confirmed, form these bodies.
	String JsonSaveMisconceptionWithEmptyParent = ""; 
	String JsonSaveMisconceptionWithInvalidParent = ""; 
	String JsonSaveMisconceptionWithoutMandatoryFields = "{\"request\":{ \"object\":{ \"description\":\"Doesn't understand carry-over QA\",\"name\":\"Carry Over QA\",\"identifier\":\"MC"+generateRandomInt(0, 500)+"\",\"missing_concept\": \"Num:C1\"}}}";
	String JsonUpdateMisconceptionValid = "{\"request\":{\"object\":{ \"description\":\"Dimension_Valid_TEST Updated\",\"name\":\"LD1_TEST_U\",\"code\":\"Lit:Dim:1TestU\",\"tags\":[\"Class QA\"],\"parent\": [{\"identifier\": \"literacy\"}]}}}";
	
	@Test
	public void getMisconceptionsExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains/literacy/misconceptions").
		then().
			spec(get200ResponseSpec());
	        
	}
	
	@Test
	public void getMisconceptionExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains/literacy/misconception/MC").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	
	@Test
	public void getMisconceptionsInvalidDomainExpect404()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains/abc/misconceptions").
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
			get("v2/domains/literacy/misconceptions/xyz").
		then().
			spec(get404ResponseSpec());
	}
	
	//Create Misconception Valid
	@Test
	public void createMisconceptionExpectSuccess()
	{
		//saveMisconception API call 
		setURI();
		Response response1 = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonSaveMisconceptionValid).
			with().
				contentType(JSON).
		when().
			post("v2/domains/literacy/misconceptions").
		then().
			log().all().
			spec(get200ResponseSpec()).		
		extract().
	    	response(); 
		
		//getting the identifier of created misconception
		JsonPath jp1 = response1.jsonPath();
		String mcId = jp1.get("result.node_id");
				
		//getDimension API call to verify if the above misconception has been created. 
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains/literacy/misconceptions/"+mcId).
		then().
			spec(get200ResponseSpec());		
	}
	
	
	@Test
	public void createMisconceptionWithEmptyParentExpect4xx()
	{
		//createMisconception API call 
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonSaveMisconceptionWithEmptyParent).
			with().
				contentType(JSON).
		when().
			post("v2/domains/literacy/misconceptions").
		then().
			log().all().
			spec(get400ResponseSpec()).
			spec(verify400DetailedResponseSpec("Failed to update relations and tags", "CLIENT_ERROR",""));
		
		// verify that the misconception is not saved in DB. 
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains/literacy/misconceptions/NUM:C1:QA2").
		then().
			log().all().
			spec(get400ResponseSpec());			
	}
	
	@Test
	public void createMisconceptionWithNonExistingParentExpect4xx()
	{
		//saveMisconception API call 
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonSaveMisconceptionWithInvalidParent).
			with().
				contentType(JSON).
		when().
			post("v2/domains/literacy/misconceptions").
		then().
			log().all().
			spec(get400ResponseSpec()).
			spec(verify400DetailedResponseSpec("Failed to update relations and tags","CLIENT_ERROR",""));
				
		// verify that the misconception is not saved in DB. 
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains/literacy/misconceptions/NUM:C1:QA3").
		then().
			spec(get400ResponseSpec());		

	}
	
	@Test
	public void createMisconceptionWithoutMandatoryFieldsExpect4xx()
	{
		//save Misconception API call 
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonSaveMisconceptionWithoutMandatoryFields).
		with().
			contentType(JSON).
		when().
			post("v2/domains/literacy/misconceptions").
		then().
			log().all().
			spec(get400ResponseSpec());
			//spec(verify400DetailedResponseSpec("Failed to update relations and tags", "CLIENT_ERROR",""));
		
		//getting the identifier of misconception from Json
		int startIndexOfIdentifier = JsonSaveMisconceptionWithoutMandatoryFields.indexOf("identifier");
		int startIndexOfTags = JsonSaveMisconceptionWithoutMandatoryFields.indexOf("missing_concept");
		String misconceptionId = JsonSaveMisconceptionWithoutMandatoryFields.substring(startIndexOfIdentifier+13,startIndexOfTags-3);
		
		// verify that the misconception is not saved in DB. 
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains/literacy/misconceptions/"+misconceptionId).
		then().
			log().all().
			spec(get404ResponseSpec());			
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
				post("v2/domains/literacy/methods/search").
			then().
			log().all().
			spec(get200ResponseSpec());
			//body("id", equalTo("orchestrator.searchDomainObjects"));
	}
	
	@Test
	public void searchMisconceptionsWithObjectTypeExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonMisconceptionSearchWithObjectType).
		with().
			contentType("application/json").
		when().
			post("domains/literacy/misconceptions/search").
		then().
			log().all().
			spec(get200ResponseSpec());
			//body("result.misconceptions.size()", equalTo(0));
	}
		
	//Update Misconception
	@Test
	public void updateMisconceptionValidInputsExpectSuccess200()
	{
		//saveMisconception API call 
		setURI();
		Response response1 = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonSaveMisconceptionValid).
		with().
			contentType(JSON).
		when().
			post("v2/domains/literacy/misconceptions").
		then().
			log().all().
			spec(get200ResponseSpec()).		
		extract().
			response(); 
				
		//getting the identifier of created misconception
		JsonPath jp1 = response1.jsonPath();
		String mcId = jp1.get("result.node_id");
		
		//updating the above created misconception
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonUpdateMisconceptionValid).
		with().
			contentType("application/json").
		when().
			patch("v2/domains/literacy/misconception/"+mcId).
		then().
			log().all().
			spec(get200ResponseSpec());	
	}
	
	@Test
	public void UpdateNonExistingMisconceptionExpect4xx()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonUpdateMisconceptionValid).
		with().
			contentType("application/json").
		when().
			patch("domains/literacy/misconceptions/xyz").
		then().
			log().all().
			spec(get404ResponseSpec());
	}
	
	//Delete Method
	@Test
	public void deleteMisconceptionValidSuccess200()
	{
		//saveMisconception API call 
		setURI();
		Response response1 = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonSaveMisconceptionValid).
		with().
			contentType(JSON).
		when().
			post("v2/domains/literacy/misconceptions").
		then().
			log().all().
			spec(get200ResponseSpec()).		
		extract().
			response(); 
						
		//getting the identifier of created misconception
		JsonPath jp1 = response1.jsonPath();
		String mcId = jp1.get("result.node_id");
				
		//deleting the above created misconception
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			delete("v2/domains/literacy/misconceptions/"+mcId).
		then().
			log().all().
			spec(get200ResponseSpec());
			//body("id", equalTo("orchestrator.searchDomainObjects"));
	}
	
	@Test
	public void deleteNonExistingMisconceptionExpect4xx()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			delete("v2/domains/literacy/misconceptions/xyz").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	
	//Get related misconceptions
	@Ignore
	@Test
	public void getRelatedMisconceptionsExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("domains/literacy/misconceptions/NUM:MC1:QA1/related").
		then().
			spec(get200ResponseSpec());
	}	
}