package org.ekstep.platform.framework;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import org.ekstep.platform.domain.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class FrameworkAPITest extends BaseTest{

	int rn = generateRandomInt(0, 999999);
	public String channelId = "in.ekstep";
	public String invalidChannelId = "in.ekstep.invalid";
	String jsonCreateValidFramework = "{\"request\":{\"framework\":{\"name\":\"LP_FR_"+ rn +"\",\"description\":\"LP_FR\",\"code\":\"LP_FR_"+ rn +"\"}}}";
	//String jsonCreateValidFramework = "{\"request\":{\"framework\":{\"name\":\"LP_FR_"+ rn +"\",\"description\":\"LP_FR\",\"code\":\"LP_FR_"+ rn +"\",\"channels\":[{\"identifier\":\"Test\"}]}}}";
	
	// Get valid frameworkId
	@Test
	public void getFrameworkExpectSuccess200(){
		setURI();
		Response R =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				when().
				get("/framework/v3/read/NCF").
				then().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP1 = R.jsonPath();
		String identifier = jP1.get("result.framework.identifier");
		String versionKey = jP1.get("result.framework.versionKey");
		Assert.assertTrue(versionKey!=null);
		Assert.assertEquals("NCF", identifier);
		System.out.println(identifier);
	}
	
	// Get blank frameworkId
	@Test
	public void getFrameworkExpectFail500(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken)).
		when().
		get("/framework/v3/read/").
		then().
		spec(get500ResponseSpec());
	}
	
	// Get invalid frameworkId
	@Test
	public void getInvalidContentExpect404(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken)).
		when().
		get("/framework/v3/read/F;NDSAF").
		then().
		spec(get404ResponseSpec());
	}
	
	// Create and get Framework
	@Test
	public void createValidFrameworkExpectSuccess200(){
		setURI();
		Response R =
				given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(jsonCreateValidFramework).
				with().
				contentType(JSON).
				when().
				post("/framework/v3/create").
				then().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String frameworkNode = jp.get("result.node_id");

		// Get Framework and validate
		setURI();
		Response R1 =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				when().
				get("/framework/v3/read/"+frameworkNode).
				then().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.framework.identifier");
		String versionKey = jP1.get("result.framework.versionKey");
		Assert.assertTrue(versionKey!=null);
		Assert.assertEquals(frameworkNode, identifier);
	}
		
	// Create Framework without channelId
	@Test
	public void createValidFrameworkWithNoChannelIdExpectFail400(){
		setURI();
		given().spec(getRequestSpecification(contentType, validuserId, APIToken)).
		body(jsonCreateValidFramework).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/create").
		then().
		spec(get400ResponseSpec()).
		extract().
		response();
	}
	
	// Create Framework with invalid channelId
	@Test
	public void createValidFrameworkWithInvalidChannelIdExpectFail400(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken, invalidChannelId, appId)).
		body(jsonCreateValidFramework).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/create").
		then().
		spec(get400ResponseSpec()).
		extract().
		response();
	}
	
	// Create Framework with no APIToken
	/*@Test
	public void createValidFrameworkWithNoAPITokenExpectFail401(){
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(jsonCreateValidFramework).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/create").
		then().
		spec(get401ResponseSpec()).
		extract().
		response();
	}*/

	// Create Framework with no APIToken
	/*@Test
	public void createValidFrameworkWithInvalidAPITokenExpectFail401(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, channelId, APIToken+1)).
		body(jsonCreateValidFramework).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/create").
		then().
		spec(get401ResponseSpec()).
		extract().
		response();
	}*/
	
	// Create and get Framework
	@Test
	public void createExistingFrameworkExpectSuccess400(){
		setURI();
		Response R =
				given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(jsonCreateValidFramework).
				with().
				contentType(JSON).
				when().
				post("/framework/v3/create").
				then().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String frameworkNode = jp.get("result.node_id");
		System.out.println(frameworkNode);
		String jsonCreateExistingValidFramework = "{\"request\":{\"framework\":{\"name\":\""+ frameworkNode +"\",\"description\":\"LP_FR\",\"code\":\""+ frameworkNode +"\"}}}";
		
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
		body(jsonCreateExistingValidFramework).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/create").
		then().
		spec(get400ResponseSpec()).
		extract().
		response();
	}

}
