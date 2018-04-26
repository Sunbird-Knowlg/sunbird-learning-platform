package org.ekstep.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.io.File;
import java.util.List;

import org.ekstep.platform.domain.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

/**
 * @author gauraw
 *
 */
public class ContentPublishV3Test extends BaseTest{

	int rn = generateRandomInt(0, 999999);
	static ClassLoader classLoader = ContentPublishV3Test.class.getClassLoader();
	static File path = new File(classLoader.getResource("UploadFiles/").getFile());
	
	public void delay(){
		try {
			Thread.sleep(2000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	//Publish Content with Checklist & Comment
	@Test
	public void publishContentExpect200_01(){
		//Create Content
		int rn = generateRandomInt(0, 999999);
		String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn+ "\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/pdf\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
		setURI();
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
		
		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/pdf.pdf")).
		when().
		post("/content/v3/upload/" + identifier)
		.then().
		//log().all().
		spec(get200ResponseSpec());
				
		//publish content
		String publishContentReq = "{\"request\": {\"content\": {\"publisher\": \"EkStep\",\"lastPublishedBy\": \"Ekstep\",\"publishChecklist\":[\"GoodQuality\",\"CorrectConcept\"],\"publishComment\":\"OK\"}}}";
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
		body(publishContentReq).
		with().
		contentType(JSON).
		when().
		post("content/v3/publish/"+identifier).
		then().//log().all().
		spec(get200ResponseSpec());

		delay();
		
		// Get Content and Validate
		setURI();
		Response resp = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + identifier).
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().response();
		
		// Validate the response
		JsonPath jp2 = resp.jsonPath();
		String status = jp2.get("result.content.status");
		List<String> publishChecklist = jp2.get("result.content.publishChecklist");
		String publishComment = jp2.get("result.content.publishComment");
		Assert.assertTrue("Live".equals(status));
		Assert.assertTrue(publishChecklist.contains("GoodQuality") && publishChecklist.contains("CorrectConcept"));
		Assert.assertTrue("OK".equals(publishComment));
	}
	
	//Publish Content with empty check list
	@Test
	public void publishContentExpect400_01(){
		//Create Content
		int rn = generateRandomInt(0, 999999);
		String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn+ "\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/pdf\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
		setURI();
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
		
		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/pdf.pdf")).
		when().
		post("/content/v3/upload/" + identifier)
		.then();
		//log().all().
		//.spec(get200ResponseSpec());
				
		//publish content
		String publishContentReq = "{\"request\": {\"content\": {\"publisher\": \"EkStep\",\"lastPublishedBy\": \"Ekstep\",\"publishChecklist\":[],\"publishComment\":\"OK\"}}}";
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
		body(publishContentReq).
		with().
		contentType(JSON).
		when().
		post("content/v3/publish/"+identifier).
		then().log().all().
		spec(get400ResponseSpec());

	}
	
	//Publish Content with empty check list
	@Test
	public void publishContentExpect400_02(){
		//Create Content
		int rn = generateRandomInt(0, 999999);
		String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn+ "\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/pdf\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
		setURI();
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
		
		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/pdf.pdf")).
		when().
		post("/content/v3/upload/" + identifier)
		.then();
		//log().all().
		//.spec(get200ResponseSpec());
				
		//publish content
		String publishContentReq = "{\"request\": {\"content\": {\"publisher\": \"EkStep\",\"lastPublishedBy\": \"Ekstep\",\"publishChecklist\":\"\",\"publishComment\":\"OK\"}}}";
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
		body(publishContentReq).
		with().
		contentType(JSON).
		when().
		post("content/v3/publish/"+identifier).
		then().//log().all().
		spec(get400ResponseSpec());

	}
	
	//Publish Content without check list
	@Test
	public void publishContentExpect400_03(){
		//Create Content
		int rn = generateRandomInt(0, 999999);
		String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn+ "\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/pdf\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
		setURI();
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
		
		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/pdf.pdf")).
		when().
		post("/content/v3/upload/" + identifier)
		.then();
		//log().all().
		//.spec(get200ResponseSpec());
				
		//publish content
		String publishContentReq = "{\"request\": {\"content\": {\"publisher\": \"EkStep\",\"lastPublishedBy\": \"Ekstep\",\"publishComment\":\"OK\"}}}";
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
		body(publishContentReq).
		with().
		contentType(JSON).
		when().
		post("content/v3/publish/"+identifier).
		then().//log().all().
		spec(get400ResponseSpec());

	}
	
}
