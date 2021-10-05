package org.sunbird.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.platform.domain.BaseTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;


public class ContentFlaggingTests extends BaseTest{

	int rn = generateRandomInt(0, 9999999);

	String jsonCreateValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_" + rn+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_NFT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";	String jsonContentClean = "{\"request\": {\"searchProperty\": \"identifier\",\"searchOperator\": \"startsWith\",\"searchString\": \"org.ektep.test.plugin.ft\"}}";
	String uploadedFile = "https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/org.ektep.test.plugin.ft/artifact/archive_1508928130106.zip";
	
	static ClassLoader classLoader = ChannelWorkflowTests.class.getClassLoader();
	static File path = new File(classLoader.getResource("UploadFiles/").getFile());
	static URL url = classLoader.getResource("DownloadedFiles");
	static File downloadPath;

	@BeforeClass
	public static void setup() throws URISyntaxException {
		downloadPath = new File(url.toURI().getPath());
	}

	@AfterClass
	public static void end() throws IOException {
		FileUtils.cleanDirectory(downloadPath);
	}

	public String createContent(){
		setURI();
		Response R = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");
 		return nodeId;
	}

	// Flagging Draft content
	@Test
	public void flagDraftContentExpect4xx(){
		String nodeId = createContent();
		
		// Get Versionkey
		setURI();
		try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);}
		Response R4 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId).
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();
		
		JsonPath jP4 = R4.jsonPath();
		String version_key = jP4.get("result.content.versionKey");

		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"flagReasons\":[\"Copyright Violation\"],\"flaggedBy\":\"Vignesh\",\"versionKey\":"+version_key+"}}").
		with().
		contentType(JSON).
		when().
		post("/content/v3/flag/"+nodeId).
		then().
		//log().all().
		spec(get400ResponseSpec());		
	}
	
	// Flagging content in review state
	@Test
	public void flagReviewedContentExpect4xx(){
		String nodeId = createContent();
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/uploadContent.zip")).
		when().
		post("/content/v3/upload/" + nodeId);
		//then().
		//log().all().
		//spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {

			// Setting status to review
			setURI();
			Response R = 
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body("{\"request\":{\"content\":{}}}").
			when().
			post("/content/v3/review/" + nodeId).
			then().
			//log().all().
			//spec(get200ResponseSpec()).
			extract().response();
			

			JsonPath jP = R.jsonPath();
			String version_key = jP.get("result.versionKey");
		
			// Flag the content
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body("{\"request\":{\"flagReasons\":[\"Copyright Violation\"],\"flaggedBy\":\"Vignesh\",\"versionKey\":"+version_key+"}}").
			with().
			contentType(JSON).
			when().
			post("/content/v3/flag/"+nodeId).
			then().
			//log().all().
			spec(get400ResponseSpec());					
		}
	}
	
	// Flag live content
	@Ignore
	@Test
	public void flagLiveContentExpectSuccess200() {
		String nodeId = createContent();
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/uploadContent.zip")).
		when().
		post("/content/v3/upload/" + nodeId);
		//then().
		//log().all().
		//spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {

			// Publish created content
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/" + nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());
			
			// Get Versionkey
			setURI();
			try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);}
			Response R4 = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/" + nodeId).
					then().
					//log().all().
					//spec(get200ResponseSpec()).
					extract().response();
			
			JsonPath jP4 = R4.jsonPath();
			String version_key = jP4.get("result.content.versionKey");
		
		// Flag the content
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"flagReasons\":[\"Copyright Violation\"],\"flaggedBy\":\"Vignesh\",\"versionKey\":"+version_key+"}}").
		with().
		contentType(JSON).
		when().
		post("/content/v3/flag/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec());				
		}
		
		// Get content and validate
		setURI();
		Response R5 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId).
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP5 = R5.jsonPath();
		String statusActual = jP5.get("result.content.status");
		Assert.assertTrue(statusActual.equals("Flagged"));
	}
	
	// Accept flag with valid flagged content
	@Ignore
	@Test
	public void acceptFlagValidContentExpectSuccess200(){
		String nodeId = createContent();
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/uploadContent.zip")).
		when().
		post("/content/v3/upload/" + nodeId);
		//then().
		//log().all().
		//spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {

			// Publish created content
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/" + nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());
			
			// Get Versionkey
			setURI();
			try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);}
			Response R4 = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/" + nodeId).
					then().
					//log().all().
					//spec(get200ResponseSpec()).
					extract().response();
			
			JsonPath jP4 = R4.jsonPath();
			String version_key = jP4.get("result.content.versionKey");
		
		// Flag the content
		setURI();
		Response R5 =
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"flagReasons\":[\"Copyright Violation\"],\"flaggedBy\":\"Vignesh\",\"versionKey\":"+version_key+"}}").
		with().
		contentType(JSON).
		when().
		post("/content/v3/flag/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec()).				
		extract().response();
		
		JsonPath jP5 = R5.jsonPath();
		String version_key2 = jP5.get("result.versionKey");
		
		// Accept Flag
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"versionKey\":"+version_key2+"}}").
		with().
		contentType(JSON).
		when().
		post("/content/v3/flag/accept/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec());
				
		// Get content and validate the image content
		setURI();
		Response R6 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" +nodeId+"?mode=edit").
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP6 = R6.jsonPath();
		String statusActual = jP6.get("result.content.status");
		Assert.assertTrue(statusActual.equals("FlagDraft"));
		
		// Get content and validate the flagged content
		setURI();
		Response R7 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" +nodeId).
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP7 = R7.jsonPath();
		String status = jP7.get("result.content.status");
		Assert.assertTrue(status.equals("Flagged"));
		}	
	}
	
	// Accept Flag invalid content
	@Test
	public void acceptFlagInvalidContentExpect4xx(){
		String nodeId = createContent();
		
		// Get Versionkey
		setURI();
		try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);}
		Response R4 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId).
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();
		
		JsonPath jP4 = R4.jsonPath();
		String version_key = jP4.get("result.content.versionKey");
		
		// Accept flag
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"versionKey\":"+version_key+"}}").
		with().
		contentType(JSON).
		when().
		post("/content/v3/flag/accept/"+nodeId).
		then().
		//log().all().
		spec(get400ResponseSpec());
	}
	
	// Reject Flag valid content
	@Ignore
	@Test
	public void rejectFlagValidContentExpectSuccess200(){
		String nodeId = createContent();
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/uploadContent.zip")).
		when().
		post("/content/v3/upload/" + nodeId);
		//then().
		//log().all().
		//spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {

			// Publish created content
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/" + nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());
			
			// Get Versionkey
			setURI();
			try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);}
			Response R4 = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/" + nodeId).
					then().
					//log().all().
					//spec(get200ResponseSpec()).
					extract().response();
			
			JsonPath jP4 = R4.jsonPath();
			String version_key = jP4.get("result.content.versionKey");
		
		// Flag the content
		setURI();
		Response R5 =
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"flagReasons\":[\"Copyright Violation\"],\"flaggedBy\":\"Vignesh\",\"versionKey\":"+version_key+"}}").
		with().
		contentType(JSON).
		when().
		post("/content/v3/flag/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec()).				
		extract().response();
		
		JsonPath jP5 = R5.jsonPath();
		String version_key2 = jP5.get("result.versionKey");
		
		// Reject Flag
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"versionKey\":"+version_key2+"}}").
		with().
		contentType(JSON).
		when().
		post("/content/v3/flag/reject/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec());
				
		// Get content and validate the image content
		setURI();
		Response R6 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" +nodeId+"?mode=edit").
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP6 = R6.jsonPath();
		String statusActual = jP6.get("result.content.status");
		Assert.assertTrue(statusActual.equals("Live"));
		
		// Get content and validate the flagged content
		setURI();
		Response R7 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" +nodeId).
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP7 = R7.jsonPath();
		String status = jP7.get("result.content.status");
		Assert.assertTrue(status.equals("Live"));
		}	
	}
	
	// Reject flag invalid content
	@Test
	public void rejectFlagInvalidContenExpect4xx(){
		String nodeId = createContent();
		
		// Get Versionkey
		setURI();
		try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);}
		Response R4 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId).
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();
		
		JsonPath jP4 = R4.jsonPath();
		String version_key = jP4.get("result.content.versionKey");
		
		// Reject flag
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"versionKey\":"+version_key+"}}").
		with().
		contentType(JSON).
		when().
		post("/content/v3/flag/reject/"+nodeId).
		then().
		//log().all().
		spec(get400ResponseSpec());
	}
	
	// Reject accepted flag content
	@Test
	public void rejectFlaggedContentExpect4xx(){
		String nodeId = createContent();
		
		// Upload zip file
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/uploadContent.zip")).
		when().
		post("/content/v3/upload/" + nodeId);
		//then().
		//log().all().
		//spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {

			// Publish created content
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/" + nodeId).
			then().
			//log().all().
			//spec(get200ResponseSpec()).
			extract().response();
			
			// Get Versionkey
			setURI();
			try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);}
			Response R4 = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/" + nodeId).
					then().
					//log().all().
					//spec(get200ResponseSpec()).
					extract().response();
			
			JsonPath jP4 = R4.jsonPath();
			String version_key = jP4.get("result.content.versionKey");
		
		// Flag the content
		setURI();
		Response R5 =
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"flagReasons\":[\"Copyright Violation\"],\"flaggedBy\":\"Vignesh\",\"versionKey\":"+version_key+"}}").
		with().
		contentType(JSON).
		when().
		post("/content/v3/flag/"+nodeId).
		then().
		//log().all().
		//spec(get200ResponseSpec()).				
		extract().response();
		
		JsonPath jP5 = R5.jsonPath();
		String version_key2 = jP5.get("result.versionKey");
		
		// Accept Flag
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"versionKey\":"+version_key2+"}}").
		with().
		contentType(JSON).
		when().
		post("/content/v3/flag/accept/"+nodeId).
		then().
		//log().all().
		//spec(get200ResponseSpec()).
		extract().response();
		
		// Reject flag
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{}}").
		with().
		contentType(JSON).
		when().
		post("/content/v3/flag/reject/"+nodeId).
		then().
		//log().all().
		spec(get400ResponseSpec());
		}
	}
	
	// Review flagDraft content and check for flagReview state
	@Ignore
	@Test
	public void reviewFlagDraftContentExpectSuccess200(){
		String nodeId = createContent();
		
		// Upload zip file
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/uploadContent.zip")).
		when().
		post("/content/v3/upload/" + nodeId);
		//then().
		//log().all().
		//spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {

			// Publish created content
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/" + nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());
			
			// Get Versionkey
			setURI();
			try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);}
			Response R4 = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/" + nodeId).
					then().
					//log().all().
					//spec(get200ResponseSpec()).
					extract().response();
			
			JsonPath jP4 = R4.jsonPath();
			String version_key = jP4.get("result.content.versionKey");
		
		// Flag the content
		setURI();
		Response R5 =
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"flagReasons\":[\"Copyright Violation\"],\"flaggedBy\":\"Vignesh\",\"versionKey\":"+version_key+"}}").
		with().
		contentType(JSON).
		when().
		post("/content/v3/flag/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec()).				
		extract().response();
		
		JsonPath jP5 = R5.jsonPath();
		String version_key2 = jP5.get("result.versionKey");
		
		// Accept Flag
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"versionKey\":"+version_key2+"}}").
		with().
		contentType(JSON).
		when().
		post("/content/v3/flag/accept/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec());
				
		// Get content and validate the image content
		setURI();
		Response R6 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" +nodeId+"?mode=edit").
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP6 = R6.jsonPath();
		String statusActual = jP6.get("result.content.status");
		Assert.assertTrue(statusActual.equals("FlagDraft"));
		
		// Review the flagDraftcontent
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{}}}").
		when().
		post("/content/v3/review/" + nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		// Get content and validate the flagged content
		setURI();
		Response R7 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" +nodeId).
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP7 = R7.jsonPath();
		String status = jP7.get("result.content.status");
		Assert.assertTrue(status.equals("Flagged"));
		
		// Get content and validate the reviewed content
		setURI();
		Response R8 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" +nodeId+"?mode=edit").
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP8 = R8.jsonPath();
		String statusNew = jP8.get("result.content.status");
		Assert.assertTrue(statusNew.equals("FlagReview"));
		}	
	}

	// Publish flagReview Content and validate the status
	@Ignore
	@Test
	public void publishFlagReviewContent(){
		String nodeId = createContent();
		
		// Upload zip file
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/uploadContent.zip")).
		when().
		post("/content/v3/upload/" + nodeId);
		//then().
		//log().all().
		//spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {

			// Publish created content
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/" + nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());
			
			// Get Versionkey
			setURI();
			try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);}
			Response R4 = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/" + nodeId).
					then().
					//log().all().
					//spec(get200ResponseSpec()).
					extract().response();
			
			JsonPath jP4 = R4.jsonPath();
			String version_key = jP4.get("result.content.versionKey");
		
		// Flag the content
		setURI();
		Response R5 =
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"flagReasons\":[\"Copyright Violation\"],\"flaggedBy\":\"Vignesh\",\"versionKey\":"+version_key+"}}").
		with().
		contentType(JSON).
		when().
		post("/content/v3/flag/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec()).				
		extract().response();
		
		JsonPath jP5 = R5.jsonPath();
		String version_key2 = jP5.get("result.versionKey");
		
		// Accept Flag
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"versionKey\":"+version_key2+"}}").
		with().
		contentType(JSON).
		when().
		post("/content/v3/flag/accept/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec());
				
		// Get content and validate the image content
		setURI();
		Response R6 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" +nodeId+"?mode=edit").
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP6 = R6.jsonPath();
		String statusActual = jP6.get("result.content.status");
		Assert.assertTrue(statusActual.equals("FlagDraft"));
		
		// Review the flagDraftcontent
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{}}}").
		when().
		post("/content/v3/review/" + nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		// Get content and validate the reviewed content
		setURI();
		Response R8 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" +nodeId+"?mode=edit").
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP8 = R8.jsonPath();
		String statusNew = jP8.get("result.content.status");
		Assert.assertTrue(statusNew.equals("FlagReview"));

		// Publish created content
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
		when().
		post("/content/v3/publish/" + nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		// Get content and validate the flagged content
		setURI();
		try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);}		
		Response R7 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" +nodeId).
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP7 = R7.jsonPath();
		String status = jP7.get("result.content.status");
		Assert.assertTrue(status.equals("Live"));
		}
	}
	
	// Private Members
	private boolean isValidXML(String body) {
		boolean isValid = true;
		if (!StringUtils.isBlank(body)) {
			try {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				dBuilder.parse(new InputSource(new StringReader(body)));
			} catch (ParserConfigurationException | SAXException | IOException e) {
				isValid = false;
			}
		}
		return isValid;
	}

	private boolean isValidJSON(String body) {
		boolean isValid = true;
		if (!StringUtils.isBlank(body)) {
			try {
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
				objectMapper.readTree(body);
			} catch (IOException e) {
				isValid = false;
			}
		}
		return isValid;
	}
	
}
