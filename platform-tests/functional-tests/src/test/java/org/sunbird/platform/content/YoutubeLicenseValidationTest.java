package org.sunbird.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import org.sunbird.platform.domain.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

/**
 * @author gauraw
 *
 */
public class YoutubeLicenseValidationTest extends BaseTest{
	
	int rn = generateRandomInt(0, 999999);

	/*
	 * Given Valid Video URL.
	 * Expected: 200 - OK
	 * 
	 * */
	@Test
	public void testYoutubeContentWithValidVideoUrl_01(){
		//Create Content
		setURI();
		String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn
				+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"
				+ rn
				+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"video/x-youtube\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
		Response res = 
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(createValidContent).
				with().
				contentType(JSON).
				when().
				//post("v3/content/create").
				post("content/v3/create").
				then().//log().all().
				extract().
				response();
		JsonPath jp = res.jsonPath();
		String identifier = jp.get("result.node_id");
		
		//Upload Youtube Video to the Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart("fileUrl","http://youtu.be/-wtIMTCHWuI").
		when().
		//post("v3/content/upload/" + identifier).
		post("/content/v3/upload/" + identifier).
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		// Get content and validate
		setURI();
		Response R1 = given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + identifier).
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP1 = R1.jsonPath();
		String status = jP1.get("result.content.status");
		String license=jP1.get("result.content.license");
		Assert.assertEquals(status, "Draft");
		Assert.assertEquals(license, "Standard YouTube License");
		
	}
	
	/*
	 * Given Valid Video URL.
	 * Expected: 200 - OK
	 * 
	 * */
	@Test
	public void testYoutubeContentWithValidVideoUrl_02(){
		//Create Content
		setURI();
		String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn
				+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"
				+ rn
				+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"video/x-youtube\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
		Response res = 
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(createValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().//log().all().
				extract().
				response();
		JsonPath jp = res.jsonPath();
		String identifier = jp.get("result.node_id");
		
		//Upload Youtube Video to the Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart("fileUrl","https://www.youtube.com/watch?v=owr198WQpM8").
		when().
		post("/content/v3/upload/" + identifier).
		then()
		//.log().all()
		.spec(get200ResponseSpec());
		
		// Get content and validate
		setURI();
		Response R1 = given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + identifier).
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP1 = R1.jsonPath();
		String status = jP1.get("result.content.status");
		String license=jP1.get("result.content.license");
		Assert.assertEquals(status, "Draft");
		Assert.assertEquals(license, "Creative Commons Attribution (CC BY)");
		
	}
	
	/*
	 * Given Invalid Video URL.
	 * Expected: 400 - CLIENT_ERROR
	 * 
	 * */
	@Test
	public void testYoutubeContentWithInValidVideoUrl_03(){
		//Create Content
		setURI();
		String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn
				+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"
				+ rn
				+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"video/x-youtube\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
		Response res = 
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(createValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().//log().all().
				extract().
				response();
		JsonPath jp = res.jsonPath();
		String identifier = jp.get("result.node_id");
		
		//Upload Youtube Video to the Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart("fileUrl","http://www.youtube.com/attribution_link?a=JdfC0C9V6ZI&u=%2Fwatch%3Fv%3DEhxJLojIE_o%26feature%3Dshare").
		when().
		post("/content/v3/upload/" + identifier).
		then()
		//.log().all()
		.spec(get400ResponseSpec());
		
	}
	
	
}