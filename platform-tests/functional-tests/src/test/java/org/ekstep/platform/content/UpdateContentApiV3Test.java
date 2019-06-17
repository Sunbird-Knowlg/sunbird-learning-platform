package org.ekstep.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import org.apache.http.HttpStatus;
import org.ekstep.platform.domain.BaseTest;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

/**
 * Intended to test functionality of update content and update hierarchy for
 * image creations and failing updates
 *
 */
public class UpdateContentApiV3Test extends BaseTest {

	/**
	 * To use a framework id value which has term with subject code 'english'
	 */
	private final static String FRAMEWORK_ID_NCFCOPY = "NCFCOPY";
	private final static String englishValidValue = "English";
	private final static String englishInValidValue = "Eng";

	private final static String IMAGE = ".img";
	private final static String RESPONSE_CODE_OK = "OK";
	private final static String RESPONSE_CODE_CLIENT_ERROR = "CLIENT_ERROR";
	private final static String NODE_ID_PATH = "result.node_id";
	private final static String VERSION_KEY_PATH = "result.versionKey";
	private final static String CONTENT_VERSION_KEY_PATH = "result.content.versionKey";


	private final String resourceRequestWithCorrectSubject = "{\"request\": {\"content\": {\"code\": \"test - TB1\",\"name\": \"test TB1\", \"mimeType\": \"application/pdf\", \"contentType\": \"Resource\", \"framework\":\""
			+ FRAMEWORK_ID_NCFCOPY + "\",\"subject\":[\"English\"]}}}";
	private final String textbookRequestWithCorrectSubject = "{\"request\": {\"content\": {\"code\": \"textBookCode\",\"name\": \"TextBKName\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"contentType\": \"TextBook\",\"framework\":\""
			+ FRAMEWORK_ID_NCFCOPY + "\",\"subject\":[\"English\"]}}}";

	@After
	public void updateNCFCOPY() {
		updateNCFCOPYWith(englishValidValue);
	}

	@Test
	public void testResourceForImgCreationWith400() throws InterruptedException {
		String resourceId = buildResource();
		String versionKey = getFrom(getContent(resourceId), CONTENT_VERSION_KEY_PATH);

		updateNCFCOPYWith(englishInValidValue);
		String requestWithVersionKey = "{\"request\": {\"content\": {\"versionKey\": \"" + versionKey + "\"}}}";
		Response updateResponse = updateContentWith(resourceId, requestWithVersionKey);
		String nodeId = getFrom(updateResponse, NODE_ID_PATH);

		assertEquals(updateResponse.getStatusCode(), HttpStatus.SC_BAD_REQUEST);
		assertEquals(resourceId + IMAGE, nodeId);
		assertEquals(getResponseCodeFrom(updateResponse), RESPONSE_CODE_CLIENT_ERROR);

	}

	@Test
	public void testResourceUpdateFor200() throws InterruptedException {

		String resourceId = buildResource();
		String versionKey = getFrom(getContent(resourceId), CONTENT_VERSION_KEY_PATH);

		updateNCFCOPYWith(englishValidValue);

		String requestWithVersionKey = "{\"request\": {\"content\": {\"versionKey\": \"" + versionKey + "\"}}}";
		Response updateResponse = updateContentWith(resourceId, requestWithVersionKey);

		assertEquals(updateResponse.getStatusCode(), HttpStatus.SC_OK);
		assertEquals(getResponseCodeFrom(updateResponse), RESPONSE_CODE_OK);

		String versionKeyImg = getFrom(updateResponse, VERSION_KEY_PATH);
		String requestWithCorrectSubject = "{\"request\": {\"content\": {\"versionKey\": \"" + versionKeyImg
				+ "\",\"subject\":[\""+englishValidValue+"\"]}}}";
		Response updateWithCorrectSubject = updateContentWith(resourceId, requestWithCorrectSubject);
		assertEquals(updateWithCorrectSubject.getStatusCode(), HttpStatus.SC_OK);
		assertEquals(getResponseCodeFrom(updateWithCorrectSubject), RESPONSE_CODE_OK);

	}

	@Test
	public void testTextBookForImgCreationWith400() throws InterruptedException {
		String textBookId = buildTextBook();
		String versionKey = getFrom(getContent(textBookId), CONTENT_VERSION_KEY_PATH);

		updateNCFCOPYWith(englishInValidValue);
		String requestWithVersionKey = "{ \"request\": { \"data\": { \"nodesModified\": { \"" + textBookId
				+ "\": { \"isNew\": false, \"root\": true, \"metadata\": { \"mimeType\": \"application/vnd.ekstep.content-collection\", \"keywords\": [], \"contentType\": \"TextBook\", \"versionKey\": \""
				+ versionKey + "\"} } }, \"hierarchy\": {} } } }";
		Response updateResponse = updateHierarchy(requestWithVersionKey);
		String nodeId = getFrom(updateResponse, NODE_ID_PATH);

		assertEquals(updateResponse.getStatusCode(), HttpStatus.SC_BAD_REQUEST);
		assertEquals(textBookId + IMAGE, nodeId);
		assertEquals(getResponseCodeFrom(updateResponse), RESPONSE_CODE_CLIENT_ERROR);
	}

	@Test 
	public void testTextBookUpdateFor200() throws InterruptedException {
		String textBookId = buildTextBook();		
		String versionKey = getFrom(getContent(textBookId), CONTENT_VERSION_KEY_PATH);

		updateNCFCOPYWith(englishValidValue);

		String requestWithVersionKey = "{ \"request\": { \"data\": { \"nodesModified\": { \"" + textBookId
				+ "\": { \"isNew\": false, \"root\": true, \"metadata\": { \"mimeType\": \"application/vnd.ekstep.content-collection\", \"keywords\": [], \"contentType\": \"TextBook\", \"versionKey\": \""
				+ versionKey + "\"} } }, \"hierarchy\": {} } } }";
		Response updateResponse = updateHierarchy(requestWithVersionKey);

		assertEquals(updateResponse.getStatusCode(), HttpStatus.SC_OK);
		assertEquals(getResponseCodeFrom(updateResponse), RESPONSE_CODE_OK);

		String versionKeyImg = getFrom(getContent(textBookId+IMAGE), CONTENT_VERSION_KEY_PATH);
		String requestWithCorrectSubject = "{ \"request\": { \"data\": { \"nodesModified\": { \"" + textBookId
				+ "\": { \"isNew\": false, \"root\": true, \"metadata\": { \"mimeType\": \"application/vnd.ekstep.content-collection\", \"keywords\": [], \"contentType\": \"TextBook\",\"subject\":[\""
				+ englishValidValue + "\"], \"versionKey\": \"" + versionKeyImg + "\"} } }, \"hierarchy\": {} } } }";
		Response updateWithCorrectSubject = updateHierarchy(requestWithCorrectSubject);
		assertEquals(updateWithCorrectSubject.getStatusCode(), HttpStatus.SC_OK);
		assertEquals(getResponseCodeFrom(updateWithCorrectSubject), RESPONSE_CODE_OK);

	}
	
	private String buildResource() throws InterruptedException {
		Response response = createContent(resourceRequestWithCorrectSubject);
		String resourceId = getFrom(response, NODE_ID_PATH);
		if (uploadToContent(resourceId).getStatusCode() == HttpStatus.SC_OK) {
			publish("/content/v3/publish/" + resourceId);
			Thread.sleep(10000);

		}
		return resourceId;
	}

	
	private String buildTextBook() throws InterruptedException {
		Response response = createContent(textbookRequestWithCorrectSubject);
		String textBookId = getFrom(response, NODE_ID_PATH);
		addUnitsToHierarchy(textBookId); // add units to TextBook

		publish("/content/v3/publish/" + textBookId);
		Thread.sleep(10000);
		return textBookId;
	}

	/**
	 * To get the value from response result for given key
	 * 
	 * @param response
	 * @param key
	 * @return
	 */
	private String getFrom(Response response, String resultKeyPath) {
		JsonPath jsonResponse = response.jsonPath();
		System.out.println("jsonResponse.get ==" +jsonResponse.get("result"));
		return jsonResponse.get(resultKeyPath);
	}

	/**
	 * return response code from response
	 */
	private String getResponseCodeFrom(Response response) {
		JsonPath jsonResponse = response.jsonPath();
		return jsonResponse.get("responseCode");
	}
	
	private Response getContent(String contentId) {
		return given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).when()
				.get("/content/v3/read/" + contentId).then().extract().response();
	}

	private Response createContent(String request) {
		setURI();
		return given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).body(request)
				.with().contentType(JSON).when().post("/content/v3/create").then().extract().response();

	}

	private Response uploadToContent(String contentId) {
		setURI();
		return given().spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).multiPart("fileUrl",
				"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/do_11244050082933145615/artifact/content-term-relationship_1518615834729.pdf")
				.then().post("/content/v3/upload/" + contentId).then().extract().response();
	}

	private Response updateContentWith(String contentId, String requestBody) {
		setURI();
		return given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(requestBody).with().contentType(JSON).when().patch("/content/v3/update/" + contentId).andReturn();

	}

	private Response addUnitsToHierarchy(String contentId) {
		String request = "{ \"request\": { \"data\": { \"nodesModified\": { \"" + contentId
				+ "\": { \"isNew\": false, \"root\": true, \"metadata\": { \"mimeType\": \"application/vnd.ekstep.content-collection\", \"keywords\": [], \"contentType\": \"TextBook\" } }, \"TestBookUnit-01\": { \"isNew\": true, \"root\": false, \"metadata\": { \"mimeType\": \"application/vnd.ekstep.content-collection\", \"keywords\": [], \"name\": \"Test_Collection_TextBookUnit_01\", \"description\": \"Test_Collection_TextBookUnit_01\", \"contentType\": \"TextBookUnit\", \"code\": \"30b0cc0c-18dc-4462-9b2b-8390b90dd3aca\" } }, \"TestBookUnit-01-01\": { \"isNew\": true, \"root\": false, \"metadata\": { \"mimeType\": \"application/vnd.ekstep.content-collection\", \"keywords\": [], \"name\": \"Test_Collection_TextBookUnit_01_01\", \"description\": \"TTest_Collection_TextBookUnit_01_01\", \"contentType\": \"TextBookUnit\", \"code\": \"30b0cc0c-18dc-4462-9b2b-8390b90dd3aca\" } }, \"TestBookUnit-02\": { \"isNew\": true, \"root\": false, \"metadata\": { \"mimeType\": \"application/vnd.ekstep.content-collection\", \"keywords\": [], \"name\": \"Test_Collection_TextBookUnit_02\", \"description\": \"TTest_Collection_TextBookUnit_02\", \"contentType\": \"TextBookUnit\", \"code\": \"30b0cc0c-18dc-4462-9b2b-8390b90dd3aca\" } }, \"TestBookUnit-02-01\": { \"isNew\": true, \"root\": false, \"metadata\": { \"mimeType\": \"application/vnd.ekstep.content-collection\", \"keywords\": [], \"name\": \"Test_Collection_TextBookUnit_02_01\", \"description\": \"TTest_Collection_TextBookUnit_02_01\", \"contentType\": \"TextBookUnit\", \"code\": \"30b0cc0c-18dc-4462-9b2b-8390b90dd3aca\" } } }, \n"
				+ "\"hierarchy\": { \"" + contentId
				+ "\": { \"name\": \"Test-G-Dev-01\", \"contentType\": \"TextBook\", \"children\": [ \"TestBookUnit-01\", \"TestBookUnit-02\" ], \"root\": true }, \"TestBookUnit-01\": { \"name\": \"Test_Collection_TextBookUnit_01\", \"contentType\": \"TextBookUnit\", \"children\": [ \"TestBookUnit-01-01\" ], \"root\": false }, \"TestBookUnit-02\": { \"name\": \"Test_Collection_TextBookUnit_02\", \"contentType\": \"TextBookUnit\", \"children\": [ \"TestBookUnit-02-01\" ], \"root\": false } }, \"lastUpdatedBy\": \"ecff2373-6c52-4956-b103-a9741eae16f0\" } } } ";

		return updateHierarchy(request);
	}

	private Response updateHierarchy(String request) {
		setURI();
		return given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).body(request)
				.with().contentType(JSON).when().patch("/content/v3/hierarchy/update").andReturn();
	}

	private void updateNCFCOPYWith(String value) {
		String request = "{\"request\": {\"term\": { \"name\":\"" + value + "\"}}}";
		setURI();
		Response response = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(request).with().contentType(JSON).when()
				.patch("/framework/v3/term/update/english?framework=" + FRAMEWORK_ID_NCFCOPY + "&category=subject")
				.then().body("responseCode", equalTo("OK")).extract().response();

		if (response.getStatusCode() == HttpStatus.SC_OK)
			publish("/framework/v3/publish/" + FRAMEWORK_ID_NCFCOPY);

	}

	private void publish(String uri) {
		String publishRequest = "{\"request\": {\"content\": {\"publisher\": \"in.ekStep\",\"lastPublishedBy\": \"in.ekstep\"}}}";
		setURI();
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).body(publishRequest)
				.with().contentType(JSON).when().post(uri);
	}

}
