package org.ekstep.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.io.File;
import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;

import org.ekstep.platform.domain.BaseTest;
//import org.hamcrest.CoreMatchers;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;


public class contentPublishWorkflow extends BaseTest{

	int rn = generateRandomInt(2000, 2500);
	
	String jsonCreateValidContent = "{\"request\": {\"content\": {\"identifier\": \"Test_QA_"+rn+"\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"Test_QA_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"pkgVersion\": 3,\"owner\": \"EkStep\"}}}";
	String jsonCreateContentCollection = "{\"request\": {\"content\": {\"identifier\": \"Test_QA_"+rn+"\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"Test_QA_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Collection\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"pkgVersion\": 3,\"owner\": \"EkStep\", \"children\": [{ \"identifier\": \"id1\"}, { \"identifier\": \"id2\"}]}}}";
	String jsonUpdateContentValid = "{\"request\": {\"content\": {\"status\": \"Live\"}}}";
	String jsonGetContentList = "{\"request\": { \"search\": {\"sort\": \"contentType\",\"order\": \"asc\"}}}";
	String jsonCreateNestedCollection = "{\"request\": {\"content\": {\"identifier\": \"Test_QANested_"+rn+"\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"Test_QA_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Collection\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"pkgVersion\": 3,\"owner\": \"EkStep\", \"children\": [{ \"identifier\": \"id1\"}]}}}";

	String invalidContentId = "TestQa_"+rn+"";
	
	// Create and get ECML Content
	@Test
	public void createValidEcmlContentExpectSuccess200(){
		setURI();
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidContent).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String ecmlNode = jp.get("result.node_id");
		
		// Get content and validate
		setURI();
		Response R1 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/"+ecmlNode).
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.content.identifier");
		Assert.assertEquals(ecmlNode, identifier);
	}

	// Create and get valid HTML
	@Test
	public void createValidHTMLContentExpectSuccess200(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("mimeType", "application/vnd.ekstep.html-archive");
		String jsonCreateValidContentHtml = js.toString();
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidContentHtml).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String htmlNode = jp.get("result.node_id");
		
		// Get content and check
		setURI();
		Response R1 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/"+htmlNode).
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP1 = R1.jsonPath();
		String status = jP1.get("result.content.status");
		Assert.assertEquals(status, "Draft");
	}

	// Create and get valid APK
		@Test
		public void createValidAPKContentExpectSuccess200(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("mimeType", "application/vnd.android.package-archive");
		String jsonCreateValidContentAPK = js.toString();
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidContentAPK).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String apkNode = jp.get("result.node_id");
		
		// Get content and check
		setURI();
		Response R1 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/"+apkNode).
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.content.identifier");
		Assert.assertEquals(apkNode, identifier);
	}
		
	// Create and get new collection
	@Test
	public void createValidCollectionExpectSuccess200(){
		String node1 = null;
		String node2 = null;
		int count = 1;
		while(count<=2){
		setURI();
		int rn = generateRandomInt(500, 999);
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("identifier", "Test_QAT_"+rn+"").put("name", "Test_QAT-"+rn+"");
		String jsonCreateValidChild = js.toString();
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidChild).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");
		if(count==1){
			node1 = nodeId;
		}
		if(count==2){
			node2 = nodeId;
		}
		count++;
	}
		// Create collection
		setURI();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
		Response R1 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateContentCollection).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Get collection and validate
		JsonPath jp1 = R1.jsonPath();
		String collectionNode = jp1.get("result.node_id");
		
		// Get collection
		setURI();
		Response R2 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/"+collectionNode).
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP2 = R2.jsonPath();
		ArrayList<String> identifiers = jP2.get("result.content.children.identifier");
		if(identifiers.contains(node1)&&identifiers.contains(node2)){
			System.out.println("Collection creation successful");
		}
		else{
			System.out.println("Content collection fails");
		}		
	}
	
	// Create collection with invalid content
	
	// Create content
	@Test
	public void createInvalidCollectionExpect400(){
		setURI();
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidContent).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String ecmlNode = jp.get("result.node_id");
		
		// Create collection
		setURI();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", ecmlNode).replace("id2", invalidContentId);
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateContentCollection).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get400ResponseSpec());
	}		
	// Update and get list
	@Test
	public void updateValidContentExpectSuccess200(){
		setURI();
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidContent).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");
		
		// Update content status to live
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonUpdateContentValid).
			with().
				contentType("application/json").
			when().
				patch("content/"+nodeId).
			then().
				log().all().
				spec(get200ResponseSpec());
		
		// Get content list and check for content
		setURI();
		Response R1 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonGetContentList).
			with().
				contentType("application/json").
			when().
				post("content/list").
			then().
				log().all().
				spec(get200ResponseSpec()).
			extract().
				response();
			
		// Validate the response
		JsonPath jp1 = R1.jsonPath();
		ArrayList<String> identifier = jp1.get("result.content.identifier");
		if(identifier.contains(nodeId)){
			System.out.println("Update Success");
		}
		else{
			System.out.println("Update fail");
		}
		
		// Update status as Retired
		setURI();
		jsonUpdateContentValid = jsonUpdateContentValid.replace("Live", "Retired");
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonUpdateContentValid).
			with().
				contentType("application/json").
			when().
				patch("content/"+nodeId).
			then().
				log().all().
				spec(get200ResponseSpec());
		
		// Get content list and check for content
		setURI();
		Response R2 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonGetContentList).
			with().
				contentType("application/json").
			when().
				post("content/list").
			then().
				log().all().
				spec(get200ResponseSpec()).
			extract().
				response();
			
		// Validate the response
		JsonPath jp2 = R2.jsonPath();
		ArrayList<String> identifier2 = jp2.get("result.content.identifier");
		if(identifier2.contains(nodeId)){
			System.out.println("Update fail");
		}
		else{
			System.out.println("Retired content is not available - Update success");
		}
		
		// Update content with Review status
		setURI();
		jsonUpdateContentValid = jsonUpdateContentValid.replace("Live", "Review");
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonUpdateContentValid).
			with().
				contentType("application/json").
			when().
				patch("content/"+nodeId).
			then().
				log().all().
				spec(get200ResponseSpec());
		
		// Get content list and check for content
		setURI();
		Response R3 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonGetContentList).
			with().
				contentType("application/json").
			when().
				post("content/list").
			then().
				log().all().
				spec(get200ResponseSpec()).
			extract().
				response();
			
		// Validate the response
		JsonPath jp3 = R3.jsonPath();
		ArrayList<String> identifier3 = jp3.get("result.content.identifier");
		if(identifier3.contains(nodeId)){
			System.out.println("Update fail");
		}
		else{
			System.out.println("Review content is not available - Update Success");
		}
	}
	
	// Upload valid content expect success
	
	//Create content
	@Test
	public void uploadContentExpectSuccess200(){
		setURI();
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidContent).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String ecmlNode = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File("/Users/purnima/Documents/Upload Files/haircut_story.zip")).
		when().
			post("content/upload/"+ecmlNode).
		then().
			log().all().
			spec(get200ResponseSpec());
		
		// Get content and validate
		setURI();
		Response R1 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/"+ecmlNode).
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP1 = R1.jsonPath();
		String artifactUrl = jP1.get("result.content.artifactUrl");
		if (artifactUrl.endsWith(".zip")&&artifactUrl.contains("haircut")){
			System.out.println("Upload Success");
		}
		else{
			System.out.println("Upload Fails");
		}
	}
	
	// Upload file without index
	
	//Create content
	@Test
	public void uploadContentWithoutIndexExpect400(){
		setURI();
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidContent).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String ecmlNode = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File("/Users/purnima/Documents/Upload Files/haircut_withoutindex.zip")).
		when().
			post("content/upload/"+ecmlNode).
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	// Upload file with invalid ecml
	
	//Create content
	@Test
	public void uploadContentWithInvalidEcmlExpect400(){
		setURI();
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidContent).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String ecmlNode = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File("/Users/purnima/Documents/Upload Files/haircut_invalidEcml.zip")).
		when().
			post("content/upload/"+ecmlNode).
		then().
			log().all().
			spec(get400ResponseSpec());
	}

	// Upload file with Empty zip

	//Create content
	@Test
	public void uploadContentWithEmptyZipExpect400(){
		setURI();
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidContent).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String ecmlNode = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File("/Users/purnima/Documents/Upload Files/haircut_empty.zip")).
		when().
			post("content/upload/"+ecmlNode).
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	//Upload file more than 20 MB
	
	//Create content
	@Test
	public void uploadContentAboveLimitExpect400(){
		setURI();
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidContent).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String ecmlNode = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File("/Users/purnima/Documents/Upload Files/the_moon_and_the_cap.zip")).
		when().
			post("content/upload/"+ecmlNode).
		then().
			log().all().
			spec(get400ResponseSpec());
	}	

	
	// Upload File without assets
	//Create content
	@Test
	public void uploadContentWithoutAssetsExpectSuccess200(){
		setURI();
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidContent).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String ecmlNode = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File("/Users/purnima/Documents/Upload Files/haircut_withoutAssets.zip")).
		when().
			post("content/upload/"+ecmlNode).
		then().
			log().all().
			spec(get200ResponseSpec());
		
		// Get content and validate
		setURI();
		Response R1 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/"+ecmlNode).
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP1 = R1.jsonPath();
		String artifactUrl = jP1.get("result.content.artifactUrl");
		if (artifactUrl.endsWith(".zip")&&artifactUrl.contains("haircut")){
			System.out.println("Upload Success");
		}
		else{
			System.out.println("Upload Fails");
		}
	}
	
	// Upload invalid file
	
	//Create content
	@Test
	public void uploadContentInvalidFileExpect400(){
		setURI();
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidContent).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String ecmlNode = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File("/Users/purnima/Documents/Upload Files/carpenter.png")).
		when().
			post("content/upload/"+ecmlNode).
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	// Upload multiple files
	
	//Create content
	@Test
	public void uploadContentMultipleExpectSuccess200(){
		setURI();
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidContent).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String ecmlNode = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File("/Users/purnima/Documents/Upload Files/haircut_story.zip")).
		when().
			post("content/upload/"+ecmlNode).
		then().
			log().all().
			spec(get200ResponseSpec());
		
		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File("/Users/purnima/Documents/Upload Files/the_moon_and_the_cap.zip")).
		when().
			post("content/upload/"+ecmlNode).
		then().
			log().all().
			spec(get200ResponseSpec());
		
		
		// Get content and validate
		setURI();
		Response R1 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/"+ecmlNode).
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP1 = R1.jsonPath();
		String artifactUrl = jP1.get("result.content.artifactUrl");
		if (artifactUrl.endsWith(".zip")&&artifactUrl.contains("the_moon_and_the_cap")){
			System.out.println("Upload Success");
		}
		else{
			System.out.println("Upload Fails");
		}
	}
	
	// Create, upload, publish and validate ECML content
	
	//Create content
	@Test
	public void publishContentExpectSuccess200(){
		setURI();
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidContent).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String ecmlNode = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File("/Users/purnima/Documents/Upload Files/haircut_story.zip")).
		when().
			post("content/upload/"+ecmlNode).
		then().
			log().all().
			spec(get200ResponseSpec());
		
		// Publish created content
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/publish/"+ecmlNode).
		then().
			log().all().
			spec(get200ResponseSpec());
		
		// Get content and validate
		setURI();
		Response R1 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/"+ecmlNode).
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP1 = R1.jsonPath();
		String artifactUrl = jP1.get("result.content.artifactUrl");
		String downloadUrl = jP1.get("result.content.downloadUrl");
		int size = jP1.get("result.content.size");
		String status = jP1.get("result.content.status");
		if (artifactUrl.endsWith(".zip")&&downloadUrl.endsWith(".ecar")&&downloadUrl.contains(ecmlNode)&&status.equals("Live")&&size<=20000000){
			System.out.println("Publish Success");
		}
		else{
			System.out.println("Publish Fails");
		}
	}	
	
	// Create, upload and publish worksheet
	
	//Create content
	@Test
	public void publishWorksheetExpectSuccess200(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("contentType", "Worksheet");
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidContent).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String ecmlNode = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File("/Users/purnima/Documents/Upload Files/Akshara_worksheet.zip")).
		when().
			post("content/upload/"+ecmlNode).
		then().
			log().all().
			spec(get200ResponseSpec());
		
		// Publish created content
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/publish/"+ecmlNode).
		then().
			log().all().
			spec(get200ResponseSpec());
		
		// Get content and validate
		setURI();
		Response R1 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/"+ecmlNode).
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP1 = R1.jsonPath();
		String artifactUrl = jP1.get("result.content.artifactUrl");
		String downloadUrl = jP1.get("result.content.downloadUrl");
		String status = jP1.get("result.content.status");
		int size = jP1.get("result.content.size");
		if (artifactUrl.endsWith(".zip")&&downloadUrl.endsWith(".ecar")&&downloadUrl.contains(ecmlNode)&&status.equals("Live")&&size<=20000000){
			System.out.println("Publish Success");
		}
		else{
			System.out.println("Publish Fails");
		}
	}	

	// Create, upload, publish and validate HTML Content
	
	//Create content
	@Test
	public void publishHTMLContentExpectSuccess200(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("mimeType", "application/vnd.ekstep.html-archive");
		String jsonCreateValidContentHtml = js.toString();
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidContentHtml).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String htmlNode = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File("/Users/purnima/Documents/Upload Files/Build-a-sentence.zip")).
		when().
			post("content/upload/"+htmlNode).
		then().
			log().all().
			spec(get200ResponseSpec());
		
		// Publish created content
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/publish/"+htmlNode).
		then().
			log().all().
			spec(get200ResponseSpec());
		
		// Get content and validate
		setURI();
		Response R1 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/"+htmlNode).
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP1 = R1.jsonPath();
		String artifactUrl = jP1.get("result.content.artifactUrl");
		String downloadUrl = jP1.get("result.content.downloadUrl");
		String status = jP1.get("result.content.status");
		int size = jP1.get("result.content.size");
		if (artifactUrl.endsWith(".zip")&&downloadUrl.endsWith(".ecar")&&downloadUrl.contains(htmlNode)&&status.equals("Live")&&size<=20000000){
			System.out.println("Publish Success");
		}
		else{
			System.out.println("Publish Fails");
		}
	}	
	
	// Create, upload, publish and validate APK Content
	
	//Create content
	@Test
	public void publishAPKContentExpectSuccess200(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("mimeType", "application/vnd.android.package-archive");
		String jsonCreateValidContentAPK = js.toString();
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidContentAPK).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String apkNode = jp.get("result.node_id");
	
		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File("/Users/purnima/Documents/Upload Files/haircut_story.zip")).
		when().
			post("content/upload/"+apkNode).
		then().
			log().all().
			spec(get200ResponseSpec());
		
		// Publish created content
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/publish/"+apkNode).
		then().
			log().all().
			spec(get200ResponseSpec());
		
		// Get content and validate
		setURI();
		Response R1 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/"+apkNode).
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP1 = R1.jsonPath();
		String artifactUrl = jP1.get("result.content.artifactUrl");
		String downloadUrl = jP1.get("result.content.downloadUrl");
		String status = jP1.get("result.content.status");
		int size = jP1.get("result.content.size");
		if (artifactUrl.endsWith(".zip")&&downloadUrl.endsWith(".ecar")&&downloadUrl.contains(apkNode)&&status.equals("Live")&&size<=20000000){
			System.out.println("Publish Success");
		}
		else{
			System.out.println("Publish Fails");
		}
	}
	
	// Create, upload, publish and validate valid collection
	@Test
	public void publishValidCollectionExpectSuccess200(){
		String node1 = null;
		String node2 = null;
		int count = 1;
		while(count<=2){
		setURI();
		int rn = generateRandomInt(500, 999);
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("identifier", "Test_QAT_"+rn+"").put("name", "Test_QAT-"+rn+"");
		String jsonCreateValidChild = js.toString();
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidChild).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");
		if(count==1){
			node1 = nodeId;
			
			// Upload Content
			setURI();
			given().
			spec(getRequestSpec(uploadContentType, validuserId)).
			multiPart(new File("/Users/purnima/Documents/Upload Files/haircut_story.zip")).
			when().
				post("content/upload/"+node1).
			then().
				log().all().
				spec(get200ResponseSpec());
			
			// Publish created content
			setURI();
			given().
				spec(getRequestSpec(contentType, validuserId)).
			when().
				get("content/publish/"+node1).
			then().
				log().all().
				spec(get200ResponseSpec());
			
		}
		if(count==2){
			node2 = nodeId;
			
			// Upload Content
			setURI();
			given().
			spec(getRequestSpec(uploadContentType, validuserId)).
			multiPart(new File("/Users/purnima/Documents/Upload Files/the_moon_and_the_cap.zip")).
			when().
				post("content/upload/"+node2).
			then().
				log().all().
				spec(get200ResponseSpec());
			
			// Publish created content
			setURI();
			given().
				spec(getRequestSpec(contentType, validuserId)).
			when().
				get("content/publish/"+node2).
			then().
				log().all().
				spec(get200ResponseSpec());
		}
		count++;
	}
		// Create collection
		setURI();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
		Response R1 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateContentCollection).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");
		
		// Publish collection
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/publish/"+nodeId).
		then().
			log().all().
			spec(get200ResponseSpec());
		
		// Get content and validate
		setURI();
		Response R2 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/"+nodeId).
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Validate the response
		JsonPath jp2 = R2.jsonPath();
		String status = jp2.get("result.content.status");
		String c_identifier = jp2.get("result.content.identifier");
		String downloadUrl = jp2.get("result.content.downloadUrl");
		ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
		if (downloadUrl.contains(nodeId)&&downloadUrl.endsWith(".ecar")&&status.equals("Live")&&c_identifier.equals(nodeId)&&identifier1.contains(node1)&&identifier1.contains(node2)){
			System.out.println("Publish collection success");
		}
		else{
			System.out.println("Publish collection fails");
		}
	}
	
	// Create, upload and publish nested collection
	@Test
	public void publishNestedCollectionExpectSuccess200(){
		String node1 = null;
		String node2 = null;
		int count = 1;
		while(count<=2){
		setURI();
		int rn = generateRandomInt(900, 1999);
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("identifier", "Test_QAT_"+rn+"").put("name", "Test_QAT-"+rn+"");
		String jsonCreateValidChild = js.toString();
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidChild).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");
		if(count==1){
			node1 = nodeId;
			
			// Upload Content
			setURI();
			given().
			spec(getRequestSpec(uploadContentType, validuserId)).
			multiPart(new File("/Users/purnima/Documents/Upload Files/haircut_story.zip")).
			when().
				post("content/upload/"+node1).
			then().
				log().all().
				spec(get200ResponseSpec());
			
			// Publish created content
			setURI();
			given().
				spec(getRequestSpec(contentType, validuserId)).
			when().
				get("content/publish/"+node1).
			then().
				log().all().
				spec(get200ResponseSpec());
			
		}
		if(count==2){
			node2 = nodeId;
			
			// Upload Content
			setURI();
			given().
			spec(getRequestSpec(uploadContentType, validuserId)).
			multiPart(new File("/Users/purnima/Documents/Upload Files/the_moon_and_the_cap.zip")).
			when().
				post("content/upload/"+node2).
			then().
				log().all().
				spec(get200ResponseSpec());
			
			// Publish created content
			setURI();
			given().
				spec(getRequestSpec(contentType, validuserId)).
			when().
				get("content/publish/"+node2).
			then().
				log().all().
				spec(get200ResponseSpec());
		}
		count++;
	}
		// Create collection
		setURI();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
		Response R1 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateContentCollection).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP1 = R1.jsonPath();
		String nodeId1 = jP1.get("result.node_id");
		
		// Publish collection
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/publish/"+nodeId1).
		then().
			log().all().
			spec(get200ResponseSpec());
		
		// Create nested collection
		setURI();
		jsonCreateNestedCollection = jsonCreateNestedCollection.replace("id1", nodeId1);
		Response R2 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateNestedCollection).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP2 = R2.jsonPath();
		String collectionId = jP2.get("result.node_id");
		
		// Publish collection
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/publish/"+collectionId).
		then().
			log().all().
			spec(get200ResponseSpec());
		
		// Get content and validate
		setURI();
		Response R3 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/"+collectionId).
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Validate the response
		JsonPath jp3 = R3.jsonPath();
		String status = jp3.get("result.content.status");
		String c_identifier = jp3.get("result.content.identifier");
		String downloadUrl = jp3.get("result.content.downloadUrl");
		ArrayList<String> identifier1 = jp3.get("result.content.children.identifier");
		if (downloadUrl.contains(collectionId)&&downloadUrl.endsWith(".ecar")&&status.equals("Live")&&c_identifier.equals(collectionId)&&identifier1.contains(nodeId1)){
			System.out.println("Publish nested collection success");
		}
		else{
			System.out.println("Publish nested collection fails");
		}
	}
}