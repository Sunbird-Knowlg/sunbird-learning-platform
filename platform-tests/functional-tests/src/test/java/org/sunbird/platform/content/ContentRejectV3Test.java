package org.sunbird.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.io.File;
import java.util.List;

import org.sunbird.platform.domain.BaseTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

/**
 * @author gauraw
 *
 */
public class ContentRejectV3Test extends BaseTest{

	int rn = generateRandomInt(0, 999999);
	static ClassLoader classLoader = ContentRejectV3Test.class.getClassLoader();
	static File path = new File(classLoader.getResource("UploadFiles/").getFile());
	
	public void delay(){
		try {
			Thread.sleep(2000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Test 1 : Reject Review State Content before Publish (Live). 
	
	@Test
	public void rejectContentExpect200_01(){
		
		//Create Content
		int rn = generateRandomInt(0, 999999);
		String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/pdf\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
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
				
		//Send for Review
		String reviewContent="{\"request\": {\"content\": {}}}";
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
		body(reviewContent).
		with().
		contentType(JSON).
		when().
		post("content/v3/review/"+identifier).
		then().//log().all().
		spec(get200ResponseSpec());
				
		// Get Content and Validate Status
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
		Assert.assertTrue(status.equals("Review"));
		
		//Reject Content
		String rejectContentReq="{\"request\": {\"content\":{\"rejectReasons\":[\"WrongConcept\", \"PoorQuality\"],\"rejectComment\":\"Overall not a good quality content\"}}}";
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
		body(rejectContentReq).
		with().
		contentType(JSON).
		when().
		post("content/v3/reject/"+identifier).
		then().//log().all().
		spec(get200ResponseSpec());
		
		//Get Content and Validate
		setURI();
		Response response = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + identifier).
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().response();
		
		// Validate the response
		JsonPath jpath = response.jsonPath();
		String rejectStatus = jpath.get("result.content.status");
		List<String> rejectReasons= jpath.get("result.content.rejectReasons");
		String rejectComment=jpath.get("result.content.rejectComment");
		Assert.assertTrue("Draft".equals(rejectStatus));
		Assert.assertTrue(rejectReasons.contains("WrongConcept") && rejectReasons.contains("PoorQuality"));
		Assert.assertTrue("Overall not a good quality content".equals(rejectComment));
	}
	
	// Test 2 : Reject Review State Content After Publish (Live). 
	
		@Test
		public void rejectContentExpect200_02(){
			
			//Create Content
			int rn = generateRandomInt(0, 999999);
			String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/pdf\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
			setURI();
			Response resp1 = 
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
			
			JsonPath jp1 = resp1.jsonPath();
			String identifier = jp1.get("result.node_id");
			String versionKey = jp1.get("result.versionKey");
			
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
			Response resp2 = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/" + identifier).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().response();
			
			// Validate the response
			JsonPath jp2 = resp2.jsonPath();
			versionKey = jp2.get("result.content.versionKey");
			String status = jp2.get("result.content.status");
			List<String> publishChecklist = jp2.get("result.content.publishChecklist");
			String publishComment = jp2.get("result.content.publishComment");
			Assert.assertTrue("Live".equals(status));
			Assert.assertTrue(publishChecklist.contains("GoodQuality") && publishChecklist.contains("CorrectConcept"));
			Assert.assertTrue("OK".equals(publishComment));
			
			// Update content to create Image Node
			String updateReq="{\"request\": {\"content\": {\"versionKey\": \""+versionKey+"\",\"name\": \"Test Content - 000001\"}}}";
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body(updateReq).
			with().
			contentType("application/json").
			when().patch("/content/v3/update/" + identifier).
			then().//log().all().
			spec(get200ResponseSpec());
			
			//Send for Review
			String reviewContent="{\"request\": {\"content\": {}}}";
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
			body(reviewContent).
			with().
			contentType(JSON).
			when().
			post("content/v3/review/"+identifier).
			then().//log().all().
			spec(get200ResponseSpec());
					
			// Get Content and Validate Status
			setURI();
			Response resp3 = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/" + identifier+"?mode=edit").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().response();
			
			// Validate the response
			JsonPath jp3 = resp3.jsonPath();
			String reviewStatus = jp3.get("result.content.status");
			Assert.assertTrue(reviewStatus.equals("Review"));
			
			//Reject Content
			String rejectContentReq="{\"request\": {\"content\":{\"rejectReasons\":[\"WrongConcept\", \"PoorQuality\"],\"rejectComment\":\"Overall not a good quality content\"}}}";
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
			body(rejectContentReq).
			with().
			contentType(JSON).
			when().
			post("content/v3/reject/"+identifier).
			then().//log().all().
			spec(get200ResponseSpec());
			
			//Get Content and Validate
			setURI();
			Response response = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/" + identifier+"?mode=edit").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().response();
			
			// Validate the response
			JsonPath jpath = response.jsonPath();
			String rejectStatus = jpath.get("result.content.status");
			List<String> rejectReasons= jpath.get("result.content.rejectReasons");
			String rejectComment=jpath.get("result.content.rejectComment");
			publishChecklist = jpath.get("result.content.publishChecklist");
			publishComment = jpath.get("result.content.publishComment");
			Assert.assertTrue(rejectStatus.equals("Draft"));
			Assert.assertTrue(rejectReasons.contains("WrongConcept") && rejectReasons.contains("PoorQuality"));
			Assert.assertTrue("Overall not a good quality content".equals(rejectComment));
			Assert.assertNull(publishChecklist);
			Assert.assertNull(publishComment);
			
		}
		
	// Test 3 : Reject Review State Flagged Content. 
	
		@Test
		public void rejectContentExpect200_03(){
			
			//Create Content
			int rn = generateRandomInt(0, 999999);
			String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/pdf\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
			setURI();
			Response resp1 = 
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
			
			JsonPath jp1 = resp1.jsonPath();
			String identifier = jp1.get("result.node_id");
			String versionKey = jp1.get("result.versionKey");
			
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
			Response resp2 = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/" + identifier).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().response();
			
			// Validate the response
			JsonPath jp2 = resp2.jsonPath();
			versionKey = jp2.get("result.content.versionKey");
			String status = jp2.get("result.content.status");
			List<String> publishChecklist = jp2.get("result.content.publishChecklist");
			String publishComment = jp2.get("result.content.publishComment");
			Assert.assertTrue("Live".equals(status));
			Assert.assertTrue(publishChecklist.contains("GoodQuality") && publishChecklist.contains("CorrectConcept"));
			Assert.assertTrue("OK".equals(publishComment));
			
			//Flag Content
			String contentFlagReq="{\"request\": {\"flagReasons\":[\"Copyright Violation\"],\"flaggedBy\":\"gauraw\",\"versionKey\": \""+versionKey+"\"}}";
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
			body(contentFlagReq).
			with().
			contentType(JSON).
			when().
			post("content/v3/flag/"+identifier).
			then().//log().all().
			spec(get200ResponseSpec());
			
			// Get Content and Validate
			setURI();
			Response resp3 = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/" + identifier).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().response();
			
			// Validate the response
			JsonPath jp3 = resp3.jsonPath();
			versionKey = jp3.get("result.content.versionKey");
			String flagStatus = jp3.get("result.content.status");
			Assert.assertTrue("Flagged".equals(flagStatus));
			
			// Accept Flagged Content to create Image Node.
			String acceptFlag="{\"request\": {\"versionKey\": \""+versionKey+"\"}}";
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body(acceptFlag).
			with().
			contentType("application/json").
			when().post("/content/v3/flag/accept/" + identifier).
			then().//log().all().
			spec(get200ResponseSpec());
			
			// Get Content and Validate
			setURI();
			Response resp4 = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/" + identifier+"?mode=edit").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().response();
			
			// Validate the response
			JsonPath jp4 = resp4.jsonPath();
			versionKey = jp4.get("result.content.versionKey");
			String flagDraftStatus = jp4.get("result.content.status");
			Assert.assertTrue("FlagDraft".equals(flagDraftStatus));
			
			//Send for Review
			String reviewContent="{\"request\": {\"content\": {}}}";
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
			body(reviewContent).
			with().
			contentType(JSON).
			when().
			post("content/v3/review/"+identifier).
			then().//log().all().
			spec(get200ResponseSpec());
					
			// Get Content and Validate Status
			setURI();
			Response resp5 = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/" + identifier+"?mode=edit").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().response();
			
			// Validate the response
			JsonPath jp5 = resp5.jsonPath();
			String reviewStatus = jp5.get("result.content.status");
			Assert.assertTrue(reviewStatus.equals("FlagReview"));
			
			//Reject Content
			String rejectContentReq="{\"request\": {\"content\":{\"rejectReasons\":[\"WrongConcept\", \"PoorQuality\"],\"rejectComment\":\"Overall not a good quality content\"}}}";
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
			body(rejectContentReq).
			with().
			contentType(JSON).
			when().
			post("content/v3/reject/"+identifier).
			then().//log().all().
			spec(get200ResponseSpec());
			
			//Get Content and Validate
			setURI();
			Response response = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/" + identifier+"?mode=edit").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().response();
			
			// Validate the response
			JsonPath jpath = response.jsonPath();
			String rejectStatus = jpath.get("result.content.status");
			List<String> rejectReasons= jpath.get("result.content.rejectReasons");
			String rejectComment=jpath.get("result.content.rejectComment");
			publishChecklist = jpath.get("result.content.publishChecklist");
			publishComment = jpath.get("result.content.publishComment");
			Assert.assertTrue(rejectStatus.equals("FlagDraft"));
			Assert.assertTrue(rejectReasons.contains("WrongConcept") && rejectReasons.contains("PoorQuality"));
			Assert.assertTrue("Overall not a good quality content".equals(rejectComment));
			Assert.assertNull(publishChecklist);
			Assert.assertNull(publishComment);
			
		}
		
		// Test 4 : Reject Review State Content before Publish (Live) without Reject Reason. 
		
		@Test
		public void rejectContentExpect200_04(){
			
			//Create Content
			int rn = generateRandomInt(0, 999999);
			String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/pdf\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
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
			//.log().all().
			//spec(get200ResponseSpec());
					
			//Send for Review
			String reviewContent="{\"request\": {\"content\": {}}}";
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
			body(reviewContent).
			with().
			contentType(JSON).
			when().
			post("content/v3/review/"+identifier).
			then();
			//.log().all().
			//spec(get200ResponseSpec());
					
			// Get Content and Validate Status
			setURI();
			Response resp = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/" + identifier).
					then().
					//log().all().
					//spec(get200ResponseSpec()).
					extract().response();
			
			// Validate the response
			JsonPath jp2 = resp.jsonPath();
			String status = jp2.get("result.content.status");
			Assert.assertTrue(status.equals("Review"));
			
			//Reject Content
			String rejectContentReq="{\"request\": {\"content\":{\"rejectComment\":\"Overall not a good quality content\"}}}";
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
			body(rejectContentReq).
			with().
			contentType(JSON).
			when().
			post("content/v3/reject/"+identifier).
			then().//log().all().
			spec(get200ResponseSpec());
		}
		
		// Test 5 : Reject Review State Content before Publish (Live) with blank Reject Reason. 
		
		@Test
		public void rejectContentExpect200_05(){
			
			//Create Content
			setURI();
			int rn = generateRandomInt(0, 999999);
			String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/pdf\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
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
			//.log().all().
			//spec(get200ResponseSpec());
					
			//Send for Review
			String reviewContent="{\"request\": {\"content\": {}}}";
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
			body(reviewContent).
			with().
			contentType(JSON).
			when().
			post("content/v3/review/"+identifier).
			then();
			//.log().all().
			//spec(get200ResponseSpec());
					
			// Get Content and Validate Status
			setURI();
			Response resp = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/" + identifier).
					then()
					//.log().all().
					//spec(get200ResponseSpec()).
					.extract().response();
			
			// Validate the response
			JsonPath jp2 = resp.jsonPath();
			String status = jp2.get("result.content.status");
			Assert.assertTrue(status.equals("Review"));
			
			//Reject Content
			setURI();
			String rejectContentReq="{\"request\": {\"content\":{\"rejectReasons\":[],\"rejectComment\":\"Overall not a good quality content\"}}}";
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
			body(rejectContentReq).
			with().
			contentType(JSON).
			when().
			post("content/v3/reject/"+identifier).
			then().log().all().
			spec(get200ResponseSpec());
		}
		
		// Test 6 : Reject Review State Content before Publish (Live) with blank Reject Reason.
		@Ignore
		@Test
		public void rejectContentExpect200_06(){
			
			//Create Content
			int rn = generateRandomInt(0, 999999);
			String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/pdf\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
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
			//.log().all().
			//spec(get200ResponseSpec());
					
			//Send for Review
			String reviewContent="{\"request\": {\"content\": {}}}";
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
			body(reviewContent).
			with().
			contentType(JSON).
			when().
			post("content/v3/review/"+identifier).
			then();
			//.log().all().
			//spec(get200ResponseSpec());
					
			// Get Content and Validate Status
			setURI();
			Response resp = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/" + identifier).
					then()
					//.log().all().
					//spec(get200ResponseSpec()).
					.extract().response();
			
			// Validate the response
			JsonPath jp2 = resp.jsonPath();
			String status = jp2.get("result.content.status");
			Assert.assertTrue(status.equals("Review"));
			
			//Reject Content
			String rejectContentReq="{\"request\": {\"content\":{\"rejectReasons\":\"\",\"rejectComment\":\"Overall not a good quality content\"}}}";
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
			body(rejectContentReq).
			with().
			contentType(JSON).
			when().
			post("content/v3/reject/"+identifier).
			then().log().all().
			spec(get200ResponseSpec());
		}

	@Test
	public void rejectContentExpect200_07(){

		//Create Content
		int rn = generateRandomInt(0, 999999);
		String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/pdf\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
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
		//.log().all().
		//spec(get200ResponseSpec());

		//Send for Review
		String reviewContent="{\"request\": {\"content\": {}}}";
		setURI();
		given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(reviewContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/review/"+identifier).
				then();
		//.log().all().
		//spec(get200ResponseSpec());

		// Get Content and Validate Status
		setURI();
		Response resp =
				given().
						spec(getRequestSpecification(contentType, userId, APIToken)).
						when().
						get("/content/v3/read/" + identifier).
						then()
						//.log().all().
						//spec(get200ResponseSpec()).
						.extract().response();

		// Validate the response
		JsonPath jp2 = resp.jsonPath();
		String status = jp2.get("result.content.status");
		Assert.assertTrue(status.equals("Review"));

		//Reject Content
		String rejectContentReq="{\"request\": {\"content\":{\"rejectReasons\":[\"\"],\"rejectComment\":\"Overall not a good quality content\"}}}";
		setURI();
		given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(rejectContentReq).
				with().
				contentType(JSON).
				when().
				post("content/v3/reject/"+identifier).
				then().log().all().
				spec(get200ResponseSpec());
	}
	// Test 3 : Reject Review State Flagged Content.
	//TODO: Unignore when flag functionality is added

	@Test
	public void rejectContentExpect400_07(){

		//Create Content
		int rn = generateRandomInt(0, 999999);
		String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/pdf\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
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
		//.log().all().
		//spec(get200ResponseSpec());

		//Send for Review
		String reviewContent="{\"request\": {\"content\": {}}}";
		setURI();
		given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(reviewContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/review/"+identifier).
				then();
		//.log().all().
		//spec(get200ResponseSpec());

		// Get Content and Validate Status
		setURI();
		Response resp =
				given().
						spec(getRequestSpecification(contentType, userId, APIToken)).
						when().
						get("/content/v3/read/" + identifier).
						then()
						//.log().all().
						//spec(get200ResponseSpec()).
						.extract().response();

		// Validate the response
		JsonPath jp2 = resp.jsonPath();
		String status = jp2.get("result.content.status");
		Assert.assertTrue(status.equals("Review"));

		//Reject Content
		String rejectContentReq="{\"request\": {\"content\":{\"rejectReasons\":\"\",\"rejectComment\":\"Overall not a good quality content\"}}}";
		setURI();
		given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(rejectContentReq).
				with().
				contentType(JSON).
				when().
				post("content/v3/reject/"+identifier).
				then().log().all().
				spec(get400ResponseSpec());
	}
	@Ignore
	@Test
	public void rejectContentExpect400_08(){

		//Create Content
		int rn = generateRandomInt(0, 999999);
		String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/pdf\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
		setURI();
		Response resp1 =
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

		JsonPath jp1 = resp1.jsonPath();
		String identifier = jp1.get("result.node_id");
		String versionKey = jp1.get("result.versionKey");

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
		Response resp2 =
				given().
						spec(getRequestSpecification(contentType, userId, APIToken)).
						when().
						get("/content/v3/read/" + identifier).
						then().
						//log().all().
								spec(get200ResponseSpec()).
						extract().response();

		// Validate the response
		JsonPath jp2 = resp2.jsonPath();
		versionKey = jp2.get("result.content.versionKey");
		String status = jp2.get("result.content.status");
		List<String> publishChecklist = jp2.get("result.content.publishChecklist");
		String publishComment = jp2.get("result.content.publishComment");
		Assert.assertTrue("Live".equals(status));
		Assert.assertTrue(publishChecklist.contains("GoodQuality") && publishChecklist.contains("CorrectConcept"));
		Assert.assertTrue("OK".equals(publishComment));

		//Flag Content
		String contentFlagReq="{\"request\": {\"flagReasons\":[\"Copyright Violation\"],\"flaggedBy\":\"gauraw\",\"versionKey\": \""+versionKey+"\"}}";
		setURI();
		given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(contentFlagReq).
				with().
				contentType(JSON).
				when().
				post("content/v3/flag/"+identifier).
				then().//log().all().
				spec(get200ResponseSpec());

		// Get Content and Validate
		setURI();
		Response resp3 =
				given().
						spec(getRequestSpecification(contentType, userId, APIToken)).
						when().
						get("/content/v3/read/" + identifier).
						then().
						//log().all().
								spec(get200ResponseSpec()).
						extract().response();

		// Validate the response
		JsonPath jp3 = resp3.jsonPath();
		versionKey = jp3.get("result.content.versionKey");
		String flagStatus = jp3.get("result.content.status");
		Assert.assertTrue("Flagged".equals(flagStatus));

		// Accept Flagged Content to create Image Node.
		String acceptFlag="{\"request\": {\"versionKey\": \""+versionKey+"\"}}";
		setURI();
		given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(acceptFlag).
				with().
				contentType("application/json").
				when().post("/content/v3/flag/accept/" + identifier).
				then().//log().all().
				spec(get200ResponseSpec());

		// Get Content and Validate
		setURI();
		Response resp4 =
				given().
						spec(getRequestSpecification(contentType, userId, APIToken)).
						when().
						get("/content/v3/read/" + identifier+"?mode=edit").
						then().
						//log().all().
								spec(get200ResponseSpec()).
						extract().response();

		// Validate the response
		JsonPath jp4 = resp4.jsonPath();
		versionKey = jp4.get("result.content.versionKey");
		String flagDraftStatus = jp4.get("result.content.status");
		Assert.assertTrue("FlagDraft".equals(flagDraftStatus));

		//Send for Review
		String reviewContent="{\"request\": {\"content\": {}}}";
		setURI();
		given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(reviewContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/review/"+identifier).
				then().//log().all().
				spec(get200ResponseSpec());

		// Get Content and Validate Status
		setURI();
		Response resp5 =
				given().
						spec(getRequestSpecification(contentType, userId, APIToken)).
						when().
						get("/content/v3/read/" + identifier+"?mode=edit").
						then().
						//log().all().
								spec(get200ResponseSpec()).
						extract().response();

		// Validate the response
		JsonPath jp5 = resp5.jsonPath();
		String reviewStatus = jp5.get("result.content.status");
		Assert.assertTrue(reviewStatus.equals("FlagReview"));

		//Reject Content
		String rejectContentReq="{\"request\": {\"content\":{\"rejectReasons\":[\"WrongConcept\", \"PoorQuality\"],\"rejectComment\":\"Overall not a good quality content\"}}}";
		setURI();
		given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(rejectContentReq).
				with().
				contentType(JSON).
				when().
				post("content/v3/reject/"+identifier).
				then().//log().all().
				spec(get200ResponseSpec());

		//Get Content and Validate
		setURI();
		Response response =
				given().
						spec(getRequestSpecification(contentType, userId, APIToken)).
						when().
						get("/content/v3/read/" + identifier+"?mode=edit").
						then().
						//log().all().
								spec(get200ResponseSpec()).
						extract().response();

		// Validate the response
		JsonPath jpath = response.jsonPath();
		String rejectStatus = jpath.get("result.content.status");
		List<String> rejectReasons= jpath.get("result.content.rejectReasons");
		String rejectComment=jpath.get("result.content.rejectComment");
		publishChecklist = jpath.get("result.content.publishChecklist");
		publishComment = jpath.get("result.content.publishComment");
		Assert.assertTrue(rejectStatus.equals("FlagDraft"));
		Assert.assertTrue(rejectReasons.contains("WrongConcept") && rejectReasons.contains("PoorQuality"));
		Assert.assertTrue("Overall not a good quality content".equals(rejectComment));
		Assert.assertNull(publishChecklist);
		Assert.assertNull(publishComment);

	}
}
