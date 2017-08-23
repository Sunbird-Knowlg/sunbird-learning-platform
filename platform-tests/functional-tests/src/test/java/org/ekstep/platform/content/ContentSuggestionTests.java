package org.ekstep.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.ekstep.platform.domain.BaseTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

public class ContentSuggestionTests extends BaseTest {
	
	public RequestSpecification getRequestSpecification(String content_type,String user_id, String APIToken)
	{
		RequestSpecBuilder builderreq = new RequestSpecBuilder();
		builderreq.addHeader("Content-Type", content_type);
		builderreq.addHeader("user-id", user_id);
		builderreq.addHeader("Authorization", APIToken);
		RequestSpecification requestSpec = builderreq.build();
		return requestSpec;
	}


	int rn = generateRandomInt(0, 9999999);
	String nodeId;
	
	String jsonCreateContentRequest = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_"+rn+"\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_NFT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
	String jsonContentClean = "{\"request\": {\"searchProperty\": \"name\",\"searchOperator\": \"startsWith\",\"searchString\": \"LP_FT\"}}";
	
	String jsonCreateSuggestionRequest = "{\"request\":{\"content\":{\"objectId\":\"nodeId\",\"objectType\":\"content\",\"suggestedBy\":\"340\",\"command\":\"update\",\"params\":{\"gradeLevel\":[\"Grade 6\",\"Grade 8\"],\"ageGroup\":[\"5-6\"],\"language\":[\"Bengali\",\"English\",\"Hindi\",\"Tamil\",\"Telugu\"]}}}}";
	String jsonCreateSuggestionWithoutCommand = "{ \"request\": {\"content\":{\"objectId\":\"nodeId\", \"objectType\":\"content\", \"suggestedBy\":\"rashmi\", \"params\":{\"gradeLevel\":[\"kindergarten\"], \"code\":\"org.ekstep.test\", \"concepts\": [ { \"identifier\": \"LO46\", \"name\": \"Comprehension Of Stories\", \"objectType\": \"Concept\", \"relation\": \"associatedTo\", \"description\": \"Comprehension Of Stories\", \"index\": null } ] } } } } ";
	String jsonCreateSuggestionWithoutSuggestedBy = "{ \"request\": {\"content\":{\"objectId\":\"nodeId\", \"objectType\":\"content\", \"command\":\"update\", \"params\":{\"gradeLevel\":[\"kindergarten\"], \"code\":\"org.ekstep.test\", \"concepts\": [ { \"identifier\": \"LO46\", \"name\": \"Comprehension Of Stories\", \"objectType\": \"Concept\", \"relation\": \"associatedTo\", \"description\": \"Comprehension Of Stories\", \"index\": null } ] } } } } ";
	String jsonCreateSuggestionWithoutObjectId = "{ \"request\": {\"content\":{\"objectType\":\"content\", \"suggestedBy\":\"rashmi\", \"command\":\"update\", \"params\":{\"gradeLevel\":[\"kindergarten\"], \"code\":\"org.ekstep.test\", \"concepts\": [ { \"identifier\": \"LO46\", \"name\": \"Comprehension Of Stories\", \"objectType\": \"Concept\", \"relation\": \"associatedTo\", \"description\": \"Comprehension Of Stories\", \"index\": null } ] } } } } ";
	String jsonCreateSuggestionWithoutObjectType = "{ \"request\": {\"content\":{\"objectId\":\"nodeId\", \"suggestedBy\":\"rashmi\", \"command\":\"update\", \"params\":{\"gradeLevel\":[\"kindergarten\"], \"code\":\"org.ekstep.test\", \"concepts\": [ { \"identifier\": \"LO46\", \"name\": \"Comprehension Of Stories\", \"objectType\": \"Concept\", \"relation\": \"associatedTo\", \"description\": \"Comprehension Of Stories\", \"index\": null } ] } } } } ";
	String jsonCreateSuggestionWithoutParams = "{ \"request\": {\"content\":{\"objectId\":\"nodeId\", \"objectType\":\"content\", \"suggestedBy\":\"rashmi\", \"command\":\"update\"} } } } ";
	String jsonCreateSuggestionWithNonExistingObjectId = "{ \"request\": { \"content\": {\"identifier\": \"LP_FT_Test\", \"mediaType\": \"content\", \"visibility\": \"Default\", \"name\": \"testContent\", \"language\": [ \"Hindi\"],\"contentType\": \"Story\", \"code\": \"org.ekstep.plugin\", \"osId\": \"org.ekstep.quiz.app\", \"pkgVersion\": 1 } } } ";
	
	String jsonRejectSuggestionWithValidRequest = "{ \"request\":{ \"content\":{ \"status\":\"reject\", \"comments\":[\"suggestion not applicable\"] } } } ";
	String jsonRejectSuggestionWithoutStatus = "{ \"request\":{ \"content\":{\"comments\":[\"suggestion not applicable\"] } } } ";
	String jsonRejectSuggestionWithInvalidStatus = "{ \"request\":{ \"content\":{ \"status\":\"rejects\", \"comments\":[\"suggestion not applicable\"] } } }";
	
	String jsonApproveSuggestionWithValidRequest = "{ \"request\":{ \"content\":{ \"status\":\"approve\", \"comments\":[\"suggestion applicable\"] } } } ";
	String jsonApproveSuggestionWithoutStatus = "{ \"request\":{ \"content\":{\"comments\":[\"suggestion applicable\"] } } } ";
	String jsonApproveSuggestionWithInvalidStatus = "{ \"request\":{ \"content\":{ \"status\":\"approves\", \"comments\":[\"suggestion applicable\"] } } } ";
	
	String jsonListSuggestionWithValidRequest = "{ \"request\": { \"content\": { } } } ";
	
	
	static ClassLoader classLoader = ContentSuggestionTests.class.getClassLoader();
	static File path = new File(classLoader.getResource("UploadFiles/").getFile());
	static String suggestionId; 
	static URL url = classLoader.getResource("DownloadedFiles");
	static File downloadPath;
	char ch = '"';
	
	@BeforeClass
	public static void setup() throws URISyntaxException{
		downloadPath = new File(url.toURI().getPath());		
	}	
	
	@AfterClass
	public static void end() throws IOException{
		FileUtils.cleanDirectory(downloadPath);
	}
	
	// Content clean up	
		public void contentCleanUp(){
			setURI();
			given().
			body(jsonContentClean).
			with().
			contentType(JSON).
			when().
			post("learning/v1/exec/content_qe_deleteContentBySearchStringInField");
		}
		
		public void createContent() {
			if (nodeId == null) {
				setURI();
				Response R = given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body(jsonCreateContentRequest).
						with().
						contentType(JSON).
						when().
						post("/content/v3/create").
						then().
						// log().all().
						//spec(get200ResponseSpec()).
						extract().
						response();

				// Extracting the JSON path
				JsonPath jp = R.jsonPath();
				nodeId = jp.get("result.node_id");
				System.out.println("nodeId=" + nodeId);


				// Upload Content
				setURI();
				given().
				spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
				multiPart(new File(path + "/uploadContent.zip")).
				then().
				post("/content/v3/upload/" + nodeId);
				// log().all().
				//spec(get200ResponseSpec());

				// Publish
				setURI();
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Ekstep\"}}}").
				then().
				post("/content/v3/publish/" + nodeId);
				// log().all().
				//spec(get200ResponseSpec());
			}
		}
	
	@Test
	public void createSuggestions(){
		createContent();
		jsonCreateSuggestionRequest = jsonCreateSuggestionRequest.replace("nodeId", nodeId);
		setURI();
		Response R = given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonCreateSuggestionRequest).
				with().
				contentType(JSON).
				when().
				post("/content/v3/suggestions/create").
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		suggestionId = jp.get("result.suggestion_id");
		System.out.println("suggestion_id=" + suggestionId);
		
		// Read the suggestions
		setURI();
		Response R1 = given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				with().
				contentType(JSON).
				when().
				get("/content/v3/suggestions/read/"+ nodeId).
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().
				response();
		
		// Extracting the JSON path
		JsonPath jp1 = R1.jsonPath();
		List<Object> suggestions = jp1.get("result.suggestions");
		Assert.assertEquals(suggestions.isEmpty(), false);
	}
	
	@Test
	public void createSuggestionWithoutCommand(){
		createContent();
		setURI();
		jsonCreateSuggestionWithoutCommand = jsonCreateSuggestionWithoutCommand.replace("nodeId", nodeId);
		Response R = given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonCreateSuggestionWithoutCommand).
				with().
				contentType(JSON).
				when().
				post("/content/v3/suggestions/create").
				then().
				log().all().
				spec(get400ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String message = jp.get("params.errmsg");
		String err = jp.get("params.err");
		String expectedErr = "MISSING_COMMAND";
		String expectedErrMsg = "Invalid Request | Missing Command parameter";
		Assert.assertEquals(message, expectedErrMsg);
		Assert.assertEquals(err, expectedErr);
	}
	
	@Test
	public void createSuggestionWithoutObjectId(){
		setURI();
		Response R = given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonCreateSuggestionWithoutObjectId).
				with().
				contentType(JSON).
				when().
				post("/content/v3/suggestions/create").
				then().
				log().all().
				spec(get400ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String message = jp.get("params.errmsg");
		String err = jp.get("params.err");
		String expectedErr = "MISSING_OBJECT_ID";
		String expectedErrMsg = "Invalid Request | Missing ObjectId parameter";
		Assert.assertEquals(message, expectedErrMsg);
		Assert.assertEquals(err, expectedErr);
	}
	
	@Test
	public void createSuggestionWithoutSuggestedBy(){
		createContent();
		setURI();
		jsonCreateSuggestionWithoutSuggestedBy = jsonCreateSuggestionWithoutSuggestedBy.replace("nodeId", nodeId);
		Response R = given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonCreateSuggestionWithoutSuggestedBy).
				with().
				contentType(JSON).
				when().
				post("/content/v3/suggestions/create").
				then().
				log().all().
				spec(get400ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String message = jp.get("params.errmsg");
		String err = jp.get("params.err");
		String expectedErr = "MISSING_SUGGESTED_BY";
		String expectedErrMsg = "Invalid Request | Missing SuggestedBy parameter";
		Assert.assertEquals(message, expectedErrMsg);
		Assert.assertEquals(err, expectedErr);
	}
	
	@Test
	public void createSuggestionWithoutObjectType(){
		createContent();
		setURI();
		jsonCreateSuggestionWithoutObjectType = jsonCreateSuggestionWithoutObjectType.replace("nodeId", nodeId);
		Response R = given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonCreateSuggestionWithoutObjectType).
				with().
				contentType(JSON).
				when().
				post("/content/v3/suggestions/create").
				then().
				log().all().
				spec(get400ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String message = jp.get("params.errmsg");
		String err = jp.get("params.err");
		String expectedErr = "MISSING_OBJECT_TYPE";
		String expectedErrMsg = "Invalid Request | Missing ObjectType parameter";
		Assert.assertEquals(message, expectedErrMsg);
		Assert.assertEquals(err, expectedErr);
	}
	
	@Test
	public void createSuggestionWithoutParams(){
		createContent();
		setURI();
		jsonCreateSuggestionWithoutParams = jsonCreateSuggestionWithoutParams.replace("nodeId", nodeId);
		Response R = given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonCreateSuggestionWithoutParams).
				with().
				contentType(JSON).
				when().
				post("/content/v3/suggestions/create").
				then().
				log().all().
				spec(get400ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String message = jp.get("params.errmsg");
		String err = jp.get("params.err");
		String expectedErr = "MISSING_PARAMS";
		String expectedErrMsg = "Invalid Request | Missing params parameter";
		Assert.assertEquals(message, expectedErrMsg);
		Assert.assertEquals(err, expectedErr);
	}
	
	@Test
	public void createSuggestionsWithInvalidUrl(){
		setURI();
	    given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonCreateSuggestionRequest).
				with().
				contentType(JSON).
				when().
				post("/content/v3/suggestions/create").
				then().
				log().all().
				spec(get400ResponseSpec()).
				extract().
				response();
	}
	@Test
	public void createSuggestionsWithNonExistingContentId(){
		setURI();
		Response R = given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonCreateSuggestionWithNonExistingObjectId).
				with().
				contentType(JSON).
				when().
				post("/content/v3/suggestions/create").
				then().
				log().all().
				spec(get400ResponseSpec()).
				extract().
				response();
		
		// Extracting the JSON path
				JsonPath jp = R.jsonPath();
				String message = jp.get("params.errmsg");
				String err = jp.get("params.err");
				String expectedErr = "MISSING_OBJECT_ID";
				String expectedErrMsg = "Invalid Request | Missing ObjectId parameter";
				Assert.assertEquals(message, expectedErrMsg);
				Assert.assertEquals(err, expectedErr);
	}
	
	@Test
	public void readSuggestionByContentId(){
		createSuggestions();
		setURI();
		Response R = given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				with().
				contentType(JSON).
				when().
				get("/content/v3/suggestions/read/"+ nodeId).
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().
				response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		List<Object> suggestions = jp.get("result.suggestions");
		Assert.assertEquals(suggestions.isEmpty(), false);
	}
	
	@Test
	public void readSuggestionByNonExistingContentId(){
		setURI();
		Response R = given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				with().
				contentType(JSON).
				when().
				get("/content/v3/suggestions/read/LP_12+TEST").
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().
				response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		List<Object> suggestions = jp.get("result.suggestions");
		Assert.assertEquals(suggestions.isEmpty(), true);
	}
	
	@Test
	public void readSuggestionsWithInvalidUrl(){
		setURI();
		given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				with().
				contentType(JSON).
				when().
				get("/content/v3/suggestions/read/LP_12+TEST").
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().
				response();
	}
	
	@Test
	public void rejectSuggestionWithValidRequest(){
		createSuggestions();
		setURI();
		Response R = given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonRejectSuggestionWithValidRequest).
				with().
				contentType(JSON).
				when().
				post("/content/v3/suggestions/reject/"+ suggestionId).
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().
				response();
		
				// Extracting the JSON path
				JsonPath jp = R.jsonPath();
				String message = jp.get("result.message");
				String expectedMsg = "suggestion rejected successfully";
				Assert.assertEquals(message, expectedMsg);
	}
	
	@Ignore
	public void rejectSuggestionWithoutStatus(){
		createSuggestions();
		setURI();
		Response R = given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonRejectSuggestionWithoutStatus).
				with().
				contentType(JSON).
				when().
				post("/content/v3/suggestions/reject/"+ suggestionId).
				then().
				log().all().
				spec(get400ResponseSpec()).
				extract().
				response();
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String message = jp.get("params.err");
		String expectedMsg = "MISSING_STATUS";
		String ErrMessage = jp.get("params.errmsg");
		String expectedErrMessage = "Error! Invalid status";
		Assert.assertEquals(message, expectedMsg);
		Assert.assertEquals(ErrMessage, expectedErrMessage);
	}
	
	@Test
	public void rejectSuggestionWithBlankSuggestionId(){
		setURI();
		Response R = given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonRejectSuggestionWithInvalidStatus).
				with().
				contentType(JSON).
				when().
				post("/content/v3/suggestions/reject/ ").
				then().
				log().all().
				spec(get500ResponseSpec()).
				extract().
				response();
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String message = jp.get("params.err");
		String expectedMsg = "ERR_SCRIPT_NOT_FOUND";
		String ErrMessage = jp.get("params.errmsg");
		String expectedErrMessage = "Invalid request path: /v3/suggestions/reject/";
		Assert.assertEquals(message, expectedMsg);
		Assert.assertEquals(ErrMessage, expectedErrMessage);
	}
	
	@Test
	public void rejectSuggestionWithInvalidSuggestionId(){
		setURI();
		given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonRejectSuggestionWithValidRequest).
				with().
				contentType(JSON).
				when().
				post("/content/v3/suggestions/reject/afdakW").
				then().
				log().all().
				spec(get500ResponseSpec()).
				extract().
				response();
	}
	
	@Test
	public void approveSuggestionWithValidRequest(){
		createSuggestions();
		setURI();
		Response R = given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonApproveSuggestionWithValidRequest).
				with().
				contentType(JSON).
				when().
				post("/content/v3/suggestions/approve/"+ suggestionId).
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().
				response();
		
				// Extracting the JSON path
				JsonPath jp = R.jsonPath();
				String message = jp.get("result.message");
				String expectedMsg = "suggestion accepted successfully! Content Update started successfully";
				Assert.assertEquals(message, expectedMsg);
	}
	
	@Ignore
	public void approveSuggestionWithoutStatus(){
		setURI();
		Response R = given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonApproveSuggestionWithoutStatus).
				with().
				contentType(JSON).
				when().
				post("/content/v3/suggestions/approve/"+ suggestionId).
				then().
				log().all().
				spec(get400ResponseSpec()).
				extract().
				response();
		
		// Extracting the JSON path
				JsonPath jp = R.jsonPath();
				String message = jp.get("params.err");
				String expectedMsg = "MISSING_STATUS";
				String ErrMessage = jp.get("params.errmsg");
				String expectedErrMessage = "Error! Invalid status";
				Assert.assertEquals(message, expectedMsg);
				Assert.assertEquals(ErrMessage, expectedErrMessage);
	}
	
	@Test
	public void approveSuggestionWithBlankSuggestionId(){
		setURI();
		Response R = given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonApproveSuggestionWithoutStatus).
				with().
				contentType(JSON).
				when().
				post("/content/v3/suggestions/approve/").
				then().
				log().all().
				spec(get500ResponseSpec()).
				extract().
				response();
		
			// Extracting the JSON path
				JsonPath jp = R.jsonPath();
				String message = jp.get("params.err");
				String expectedMsg = "ERR_SCRIPT_NOT_FOUND";
				String ErrMessage = jp.get("params.errmsg");
				String expectedErrMessage = "Invalid request path: /v3/suggestions/approve/";
				Assert.assertEquals(message, expectedMsg);
				Assert.assertEquals(ErrMessage, expectedErrMessage);
	}
	
	@Test
	public void approveSuggestionWithInvalidSuggestionId(){
		setURI();
		given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonApproveSuggestionWithValidRequest).
				with().
				contentType(JSON).
				when().
				post("/content/v3/suggestions/approve/asdjfhdjksa").
				then().
				log().all().
				spec(get500ResponseSpec()).
				extract().
				response();
	}
	
	@Test
	public void listSuggestionWithValidRequest(){
		setURI();
		Response R = given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonListSuggestionWithValidRequest).
				with().
				contentType(JSON).
				when().
				post("/content/v3/suggestions/list").
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().
				response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		List<Object> list = jp.get("result.suggestions");
		Assert.assertEquals(list.isEmpty(), false);
	}
}
