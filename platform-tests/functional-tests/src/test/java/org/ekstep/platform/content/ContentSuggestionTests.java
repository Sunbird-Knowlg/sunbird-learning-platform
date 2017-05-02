package org.ekstep.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.platform.domain.BaseTest;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

import net.lingala.zip4j.core.ZipFile;

public class ContentSuggestionTests extends BaseTest {

	int rn = generateRandomInt(0, 9999999);
	
	String jsonCreateContentRequest = "{ \"request\": { \"content\": {\"identifier\": \"LP_FT_"+rn+"\", \"mediaType\": \"content\", \"visibility\": \"Default\", \"name\": \"testContent\", \"language\": [ \"Hindi\"],\"contentType\": \"Story\", \"code\": \"org.ekstep.plugin\", \"osId\": \"org.ekstep.quiz.app\", \"pkgVersion\": 1 } } } ";
	String jsonContentClean = "{\"request\": {\"searchProperty\": \"name\",\"searchOperator\": \"startsWith\",\"searchString\": \"LP_FT\"}}";
	
	String jsonCreateSuggestionRequest = "{ \"request\": {\"content\":{\"objectId\":\""+nodeId+"\", \"objectType\":\"content\", \"suggestedBy\":\"rashmi\", \"command\":\"update\", \"params\":{\"gradeLevel\":[\"kindergarten\"], \"code\":\"org.ekstep.test\", \"concepts\": [ { \"identifier\": \"LO46\", \"name\": \"Comprehension Of Stories\", \"objectType\": \"Concept\", \"relation\": \"associatedTo\", \"description\": \"Comprehension Of Stories\", \"index\": null } ] } } } } ";
	String jsonCreateSuggestionWithoutCommand = "{ \"request\": {\"content\":{\"objectId\":\""+nodeId+"\", \"objectType\":\"content\", \"suggestedBy\":\"rashmi\", \"params\":{\"gradeLevel\":[\"kindergarten\"], \"code\":\"org.ekstep.test\", \"concepts\": [ { \"identifier\": \"LO46\", \"name\": \"Comprehension Of Stories\", \"objectType\": \"Concept\", \"relation\": \"associatedTo\", \"description\": \"Comprehension Of Stories\", \"index\": null } ] } } } } ";
	String jsonCreateSuggestionWithoutSuggestedBy = "{ \"request\": {\"content\":{\"objectId\":\""+nodeId+"\", \"objectType\":\"content\", \"command\":\"update\", \"params\":{\"gradeLevel\":[\"kindergarten\"], \"code\":\"org.ekstep.test\", \"concepts\": [ { \"identifier\": \"LO46\", \"name\": \"Comprehension Of Stories\", \"objectType\": \"Concept\", \"relation\": \"associatedTo\", \"description\": \"Comprehension Of Stories\", \"index\": null } ] } } } } ";
	String jsonCreateSuggestionWithoutObjectId = "{ \"request\": {\"content\":{\"objectType\":\"content\", \"suggestedBy\":\"rashmi\", \"command\":\"update\", \"params\":{\"gradeLevel\":[\"kindergarten\"], \"code\":\"org.ekstep.test\", \"concepts\": [ { \"identifier\": \"LO46\", \"name\": \"Comprehension Of Stories\", \"objectType\": \"Concept\", \"relation\": \"associatedTo\", \"description\": \"Comprehension Of Stories\", \"index\": null } ] } } } } ";
	String jsonCreateSuggestionWithoutObjectType = "{ \"request\": {\"content\":{\"objectId\":\""+nodeId+"\", \"suggestedBy\":\"rashmi\", \"command\":\"update\", \"params\":{\"gradeLevel\":[\"kindergarten\"], \"code\":\"org.ekstep.test\", \"concepts\": [ { \"identifier\": \"LO46\", \"name\": \"Comprehension Of Stories\", \"objectType\": \"Concept\", \"relation\": \"associatedTo\", \"description\": \"Comprehension Of Stories\", \"index\": null } ] } } } } ";
	String jsonCreateSuggestionWithoutParams = "{ \"request\": {\"content\":{\"objectId\":\""+nodeId+"\", \"objectType\":\"content\", \"suggestedBy\":\"rashmi\", \"command\":\"update\"} } } } ";
	String jsonCreateSuggestionWithNonExistingObjectId = "{ \"request\": { \"content\": {\"identifier\": \"LP_FT_Test\", \"mediaType\": \"content\", \"visibility\": \"Default\", \"name\": \"testContent\", \"language\": [ \"Hindi\"],\"contentType\": \"Story\", \"code\": \"org.ekstep.plugin\", \"osId\": \"org.ekstep.quiz.app\", \"pkgVersion\": 1 } } } ";
	
	String jsonRejectSuggestionWithValidRequest = "{ \"request\":{ \"content\":{ \"status\":\"reject\", \"comments\":[\"suggestion not applicable\"] } } } ";
	String jsonRejectSuggestionWithoutStatus = "{ \"request\":{ \"content\":{\"comments\":[\"suggestion not applicable\"] } } } ";
	String jsonRejectSuggestionWithInvalidStatus = "{ \"request\":{ \"content\":{ \"status\":\"rejects\", \"comments\":[\"suggestion not applicable\"] } } }";
	
	String jsonApproveSuggestionWithValidRequest = "{ \"request\":{ \"content\":{ \"status\":\"approve\", \"comments\":[\"suggestion applicable\"] } } } ";
	String jsonApproveSuggestionWithoutStatus = "{ \"request\":{ \"content\":{\"comments\":[\"suggestion applicable\"] } } } ";
	String jsonApproveSuggestionWithInvalidStatus = "{ \"request\":{ \"content\":{ \"status\":\"approves\", \"comments\":[\"suggestion applicable\"] } } } ";
	
	String jsonListSuggestionWithValidRequest = "{ \"request\": { \"content\": { \"suggestedBy\": \"rashmi\", \"suggestion_id\": \"sg_11223636344706662417\" } } } ";
	
	
	static ClassLoader classLoader = ContentSuggestionTests.class.getClassLoader();
	static File path = new File(classLoader.getResource("UploadFiles/").getFile());
	static String nodeId;
	static String suggestionId; 
	static URL url = classLoader.getResource("DownloadedFiles");
	static File downloadPath;
	char ch = '"';
	
//	@BeforeClass
//	public static void setup() throws URISyntaxException{
//		downloadPath = new File(url.toURI().getPath());		
//	}	
//	
//	@AfterClass
//	public static void end() throws IOException{
//		FileUtils.cleanDirectory(downloadPath);	
//	}
//	
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
		
	@Test
	public void createContent() {
		if (nodeId == null) {
			setURI();
			Response R = given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateContentRequest).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
					// log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			nodeId = jp.get("result.node_id");
			System.out.println("nodeId=" + nodeId);

			
			// Upload Content
			setURI();
			given().spec(getRequestSpec(uploadContentType, validuserId))
					.multiPart(new File(path + "/uploadContent.zip")).when()
					.post("/learning/v2/content/upload/" + nodeId).then().
					// log().all().
					spec(get200ResponseSpec());

			// Get body and validate
			setURI();
			Response R3 = given().spec(getRequestSpec(contentType, validuserId)).when()
					.get("/learning/v2/content/" + nodeId + "?fields=body").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			JsonPath jp3 = R3.jsonPath();
			String body = jp3.get("result.content.body");
			Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
			if (isValidXML(body) || isValidJSON(body)) {
				Assert.assertTrue(accessURL(nodeId));
			}

			// Publish
			setURI();
			given().spec(getRequestSpec(contentType, validuserId)).when().get("/learning/v2/content/publish/" + nodeId)
					.then().
					// log().all().
					spec(get200ResponseSpec());

			// Get content and validate
			setURI();
			Response R4 = given().spec(getRequestSpec(contentType, validuserId)).when()
					.get("/learning/v2/content/" + nodeId).then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Validate the response
			JsonPath jp4 = R4.jsonPath();
			String status = jp4.get("result.content.status");
			asyncPublishValidationContents(nodeId, status);
		}
	}
	
	@Test
	public void createSuggestions(){
		setURI();
		Response R = given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateSuggestionRequest).
				with().
				contentType(JSON).
				when().
				post("/learning/v3/suggestions/create").
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		suggestionId = jp.get("result.suggestion_id");
		System.out.println("suggestion_id=" + suggestionId);
	}
	
	@Test
	public void createSuggestionWithoutCommand(){
		setURI();
		Response R = given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateSuggestionWithoutCommand).
				with().
				contentType(JSON).
				when().
				post("/learning/v3/suggestions/create").
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
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateSuggestionWithoutObjectId).
				with().
				contentType(JSON).
				when().
				post("/learning/v3/suggestions/create").
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
		setURI();
		Response R = given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateSuggestionWithoutSuggestedBy).
				with().
				contentType(JSON).
				when().
				post("/learning/v3/suggestions/create").
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
		setURI();
		Response R = given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateSuggestionWithoutObjectType).
				with().
				contentType(JSON).
				when().
				post("/learning/v3/suggestions/create").
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
		setURI();
		Response R = given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateSuggestionWithoutParams).
				with().
				contentType(JSON).
				when().
				post("/learning/v3/suggestions/create").
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
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateSuggestionRequest).
				with().
				contentType(JSON).
				when().
				post("/learning/v3/suggestions/creaate").
				then().
				log().all().
				spec(get500ResponseSpec()).
				extract().
				response();
	}
	@Test
	public void createSuggestionsWithNonExistingContentId(){
		setURI();
		Response R = given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateSuggestionWithNonExistingObjectId).
				with().
				contentType(JSON).
				when().
				post("/learning/v3/suggestions/create").
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
		setURI();
		Response R = given().
				spec(getRequestSpec(contentType, validuserId)).
				with().
				contentType(JSON).
				when().
				get("/learning/v3/suggestions/read/"+ nodeId).
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
				spec(getRequestSpec(contentType, validuserId)).
				with().
				contentType(JSON).
				when().
				get("/learning/v3/suggestions/read/LP_12+TEST").
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
				spec(getRequestSpec(contentType, validuserId)).
				with().
				contentType(JSON).
				when().
				get("/learning/v3/suggestions/reads/LP_12+TEST").
				then().
				log().all().
				spec(get500ResponseSpec()).
				extract().
				response();
	}
	
	@Test
	public void rejectSuggestionWithValidRequest(){
		setURI();
		Response R = given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonRejectSuggestionWithValidRequest).
				with().
				contentType(JSON).
				when().
				post("/learning/v3/suggestions/reject/"+ suggestionId).
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
	
	@Test
	public void rejectSuggestionWithoutStatus(){
		setURI();
		Response R = given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonRejectSuggestionWithoutStatus).
				with().
				contentType(JSON).
				when().
				post("/learning/v3/suggestions/reject/"+ suggestionId).
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
	public void rejectSuggestionWithInvalidStatus(){
		setURI();
		Response R = given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonRejectSuggestionWithInvalidStatus).
				with().
				contentType(JSON).
				when().
				post("/learning/v3/suggestions/reject/"+ suggestionId).
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
	public void rejectSuggestionWithInvalidUrl(){
		setURI();
		given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonRejectSuggestionWithValidRequest).
				with().
				contentType(JSON).
				when().
				post("/learning/v3/suggestions/rejects/"+ suggestionId).
				then().
				log().all().
				spec(get500ResponseSpec()).
				extract().
				response();
	}
	
	@Test
	public void approveSuggestionWithValidRequest(){
		setURI();
		Response R = given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonApproveSuggestionWithValidRequest).
				with().
				contentType(JSON).
				when().
				post("/learning/v3/suggestions/approve/"+ suggestionId).
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
	
	@Test
	public void approveSuggestionWithoutStatus(){
		setURI();
		Response R = given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonApproveSuggestionWithoutStatus).
				with().
				contentType(JSON).
				when().
				post("/learning/v3/suggestions/approve/"+ suggestionId).
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
	public void approveSuggestionWithInvalidStatus(){
		setURI();
		Response R = given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonApproveSuggestionWithoutStatus).
				with().
				contentType(JSON).
				when().
				post("/learning/v3/suggestions/approve/"+ suggestionId).
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
	public void approveSuggestionWithInvalidUrl(){
		setURI();
		given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonRejectSuggestionWithValidRequest).
				with().
				contentType(JSON).
				when().
				post("/learning/v3/suggestions/approvess/"+ suggestionId).
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
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonListSuggestionWithValidRequest).
				with().
				contentType(JSON).
				when().
				post("/learning/v3/suggestions/list").
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
	
	@Test
	public void listSuggestionWithInvalidUrl(){
		setURI();
		Response R = given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonListSuggestionWithValidRequest).
				with().
				contentType(JSON).
				when().
				post("/learning/v3/list/suggestions/lists").
				then().
				log().all().
				spec(get500ResponseSpec()).
				extract().
				response();
	}
	
	// Async Publish validations - Collection
	public void asyncPublishValidations(ArrayList<String> identifier1, String status, String nodeId,
			String c_identifier, String node1, String node2) {
		if (status.equals("Processing")) {
			for (int i = 1000; i <= 5000; i = i + 1000) {
				try {
					Thread.sleep(i);
				} catch (InterruptedException e) {
					System.out.println(e);
				}
				setURI();
				Response R3 = given().spec(getRequestSpec(contentType, validuserId)).when()
						.get("/learning/v2/content/" + nodeId).then().
						// log().all().
						spec(get200ResponseSpec()).extract().response();

				// Validate the response
				JsonPath jp3 = R3.jsonPath();
				String statusUpdated = jp3.get("result.content.status");
				if (statusUpdated.equals("Processing")) {
					i++;
				}
				if (statusUpdated.equals("Live")) {
					Assert.assertTrue(statusUpdated.equals("Live") && c_identifier.equals(nodeId)
							&& identifier1.contains(node1) && identifier1.contains(node2));
				}
			}
		}
		if (status.equals("Live")) {
			Assert.assertTrue(status.equals("Live") || status.equals("Processing") && c_identifier.equals(nodeId)
					&& identifier1.contains(node1) && identifier1.contains(node2));
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

	@SuppressWarnings("unused")
	private boolean accessURL(String nodeId) throws ClassCastException {
		boolean accessURL = false;

		// Publish created content
		setURI();
		given().spec(getRequestSpec(contentType, validuserId)).when().get("/learning/v2/content/publish/" + nodeId)
				.then().log().all().spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R1 = given().spec(getRequestSpec(contentType, validuserId)).when()
				.get("/learning/v2/content/" + nodeId).then().log().all().spec(get200ResponseSpec()).extract()
				.response();

		JsonPath jP1 = R1.jsonPath();

		// Fetching metadatas from API response

		String artifactUrl = jP1.get("result.content.artifactUrl");
		String downloadUrl = jP1.get("result.content.downloadUrl");
		String statusActual = jP1.get("result.content.status");
		String mimeTypeActual = jP1.get("result.content.mimeType");
		String codeActual = jP1.get("result.content.code");
		String osIdActual = jP1.get("result.content.osId");
		String contentTypeActual = jP1.get("result.content.contentType");
		String mediaTypeActual = jP1.get("result.content.mediaType");
		String descriptionActual = jP1.get("result.content.description");
		// Float pkgVersionActual = jP1.get("result.content.pkgVersion");
		// System.out.println(pkgVersionActual);
		Float size = jP1.get("result.content.size");
		try {
			// Validating the status
			System.out.println(statusActual);
			if (statusActual.equals("Processing")) {
				asyncPublishValidationContents(nodeId, statusActual);
			}
			// Downloading the zip file from artifact url and ecar from download
			// url and saving with different name

			String ecarName = "ecar_" + rn + "";
			String uploadFile = "upload_" + rn + "";

			FileUtils.copyURLToFile(new URL(artifactUrl), new File(downloadPath + "/" + uploadFile + ".zip"));
			String uploadSource = downloadPath + "/" + uploadFile + ".zip";

			FileUtils.copyURLToFile(new URL(downloadUrl), new File(downloadPath + "/" + ecarName + ".zip"));
			String source = downloadPath + "/" + ecarName + ".zip";

			File Destination = new File(downloadPath + "/" + ecarName + "");
			String Dest = Destination.getPath();

			try {

				// Extracting the uploaded file using artifact url
				ZipFile zipUploaded = new ZipFile(uploadSource);
				zipUploaded.extractAll(Dest);

				// Downloaded from artifact url
				File uploadAssetsPath = new File(Dest + "/assets");
				File[] uploadListFiles = uploadAssetsPath.listFiles();

				// Extracting the ecar file
				ZipFile zip = new ZipFile(source);
				zip.extractAll(Dest);

				String folderName = nodeId;
				String dirName = Dest + "/" + folderName;

				File fileName = new File(dirName);
				File[] listofFiles = fileName.listFiles();

				for (File file : listofFiles) {

					// Validating the ecar file

					if (file.isFile()) {
						String fPath = file.getAbsolutePath();
						String fName = file.getName();
						System.out.println(fName);

						if (fName.endsWith(".zip") || fName.endsWith(".rar")) {
							ZipFile ecarZip = new ZipFile(fPath);
							ecarZip.extractAll(dirName);

							// Fetching the assets
							File assetsPath = new File(dirName + "/assets");
							File[] extractedAssets = assetsPath.listFiles();
							if (assetsPath.exists()) {

								int assetCount = assetsPath.listFiles().length;
								System.out.println(assetCount);

								int uploadAssetsCount = uploadAssetsPath.listFiles().length;
								System.out.println(uploadAssetsCount);

								// Asserting the assets count in uploaded zip
								// file and ecar file
								Assert.assertEquals(assetCount, uploadAssetsCount);

								// Compare the files in both of the folders are
								// same
								compareFiles(uploadListFiles, extractedAssets);
							}
						} else {
							System.out.println("No zip file found");
						}
					} else {
						System.out.println("No zip file exists");
					}
				}

				// Validating the manifest
				File manifest = new File(Dest + "/manifest.json");

				Gson gson = new Gson();
				JsonParser parser = new JsonParser();

				JsonElement jsonElement = parser.parse(new FileReader(manifest));
				JsonObject obj = jsonElement.getAsJsonObject();

				JsonObject arc = obj.getAsJsonObject("archive");
				JsonArray items = arc.getAsJsonArray("items");

				@SuppressWarnings("rawtypes")

				// Extracting the metadata from manifest and assert with api
				// response

				Iterator i = items.iterator();
				while (i.hasNext()) {
					try {
						JsonObject item = (JsonObject) i.next();
						String name = getStringValue(item, "name");
						String mimeType = getStringValue(item, "mimeType");
						Assert.assertEquals(mimeTypeActual, mimeType);
						String status = getStringValue(item, "status");
						Assert.assertEquals(statusActual, status);
						String code = getStringValue(item, "code");
						Assert.assertEquals(codeActual, code);
						String osID = getStringValue(item, "osId");
						Assert.assertEquals(osIdActual, osID);
						String contentType = getStringValue(item, "contentType");
						Assert.assertEquals(contentTypeActual, contentType);
						String mediaType = getStringValue(item, "mediaType");
						Assert.assertEquals(mediaTypeActual, mediaType);
						String description = getStringValue(item, "description");
						Assert.assertEquals(descriptionActual, description);
						String pkgVersion = getStringValue(item, "pkgVersion");
						// Assert.assertNotSame(pkgVersionActual, pkgVersion);
						Assert.assertTrue(artifactUrl.endsWith(".zip") && downloadUrl.endsWith(".ecar")
								&& statusActual.equals("Live"));
					} catch (JSONException jse) {
						accessURL = false;
						// jse.printStackTrace();
					}
				}
			} catch (Exception x) {
				accessURL = false;
				// x.printStackTrace();
			}
		} catch (Exception e) {
			accessURL = false;
			// e.printStackTrace();
		}
		return true;
	}

	private String getStringValue(JsonObject obj, String attr) {
		if (obj.has(attr)) {
			JsonElement element = obj.get(attr);
			return element.getAsString();
		}
		return null;
	}

	// Compare the files extracted from artifact URL and ECAR

	public String compareFiles(File[] uploadListFiles, File[] extractedAssets) {
		String filesincommon = "";
		String filesnotpresent = "";
		boolean final_status = true;
		for (int i = 0; i < uploadListFiles.length; i++) {
			boolean status = false;
			for (int k = 0; k < extractedAssets.length; k++) {
				if (uploadListFiles[i].getName().equalsIgnoreCase(extractedAssets[k].getName())) {
					filesincommon = uploadListFiles[i].getName() + "," + filesincommon;
					// System.out.println("Common files are: "+filesincommon);
					status = true;
					break;
				}
			}
			if (!status) {
				final_status = false;
				filesnotpresent = uploadListFiles[i].getName() + "," + filesnotpresent;
			}
		}
		// Assert.assertTrue(final_status);
		if (final_status) {
			// System.out.println("Files are same");
			return "success";
		} else {
			System.out.println(filesnotpresent);
			return filesnotpresent;
		}
	}

	// Async publish validations - Other contents
	public void asyncPublishValidationContents(String nodeId, String statusActual) {
		for (int i = 1000; i <= 5000; i = i + 1000) {
			try {
				Thread.sleep(i);
			} catch (InterruptedException e) {
				System.out.println(e);
			}
			setURI();
			Response R3 = given().spec(getRequestSpec(contentType, validuserId)).when()
					.get("/learning/v2/content/" + nodeId).then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Validate the response
			JsonPath jp3 = R3.jsonPath();
			String statusUpdated = jp3.get("result.content.status");
			// System.out.println(statusUpdated);
			if (statusUpdated.equals("Processing")) {
				i = i + 1000;
			}
			if (statusUpdated.equals("Live")) {
				break;
			}
		}
	}
}
