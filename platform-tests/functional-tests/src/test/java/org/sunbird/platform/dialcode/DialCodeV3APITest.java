package org.sunbird.platform.dialcode;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.platform.domain.BaseTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

/**
 * @author gauraw
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DialCodeV3APITest extends BaseTest {

	int rn = generateRandomInt(0, 999999);
	static ClassLoader classLoader = DialCodeV3APITest.class.getClassLoader();
	static File path = new File(classLoader.getResource("UploadFiles/").getFile());
	String jsonUpdateMetadata = "{\"request\":{\"content\":{\"versionKey\":\"version_key\",\"language\":[\"Tamil\",\"Telugu\"]}}}";
	private String createPublisherReq = "{\"request\": {\"publisher\": {\"identifier\":\"LFT_PUB_" + rn
			+ "\",\"name\": \"Functional Test Publisher\"}}}";

	private static String publisherId = "";
	private static String dialCodeId = "";
	private static String dialCodeId_2 = "";
	private static String contentId_1 = "";
	private static String contentId_2 = "";

	@Before
	public void init() {
		if (StringUtils.isBlank(publisherId))
			createPublisher();
		if (StringUtils.isBlank(dialCodeId))
			generateDialCode();
		if (StringUtils.isBlank(contentId_1))
			createContent();
	}

	private void createPublisher() {
		String createPublisherReq = "{\"request\": {\"publisher\": {\"identifier\":\"LFT_PUB_" + rn
				+ "\",\"name\": \"Functional Test Publisher\"}}}";
		setURI();
		Response R = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(createPublisherReq).with().contentType(JSON).when().post("dialcode/v3/publisher/create").then()
				.extract().response();
		JsonPath jp = R.jsonPath();
		publisherId = jp.get("result.identifier");
	}

	private void generateDialCode() {
		String dialCodeGenReq = "{\"request\": {\"dialcodes\": {\"count\":2,\"publisher\": \"" + publisherId + "\"}}}";
		setURI();
		Response R = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeGenReq).with().contentType(JSON).when().post("dialcode/v3/generate").then().extract()
				.response();
		JsonPath jp = R.jsonPath();
		List<String> dialcodes = jp.get("result.dialcodes");
		dialCodeId = dialcodes.get(0);
		dialCodeId_2 = dialcodes.get(1);

	}

	private void createContent() {
		for (int i = 1; i <= 2; i++) {
			int rn = generateRandomInt(0, 999999);
			String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn
					+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"
					+ rn
					+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/pdf\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
			setURI();
			Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
					.body(createValidContent).with().contentType(JSON).when().post("content/v3/create").then().extract()
					.response();
			JsonPath jp = res.jsonPath();
			String identifier = jp.get("result.node_id");
			if (i == 1)
				contentId_1 = identifier;
			if (i == 2)
				contentId_2 = identifier;
		}
	}

	@Test
	public void linkDialCodeWithLiveAndImageContentExpectSuccess200() throws InterruptedException {
		//Create Content
		int rn = generateRandomInt(0, 999999);
		String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn
				+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"
				+ rn
				+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/pdf\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(createValidContent).with().contentType(JSON).when().post("content/v3/create").then().//log().all().
				extract().response();

		JsonPath jp = res.jsonPath();
		String identifier = jp.get("result.node_id");

		Thread.sleep(3000);

		// Link 1st DIAL Code to Content
		setURI();
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + identifier
				+ "\"],\"dialcode\": [\"" + dialCodeId + "\"]}}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then().//log().all().
				spec(get200ResponseSpec());

		// Upload Content
		setURI();
		given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
				.multiPart(new File(path + "/pdf.pdf")).when().post("/content/v3/upload/" + identifier).then().
				//log().all().
				spec(get200ResponseSpec());

		// Publish Created Content
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + identifier).then().
				//log().all().
				spec(get200ResponseSpec());

		Thread.sleep(5000);

		// Get Published Content and Validate
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + identifier).then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp2 = R2.jsonPath();
		String status = jp2.get("result.content.status");
		ArrayList<String> dialcode_1 = jp2.get("result.content.dialcodes");
		String versionKey = jp2.get("result.content.versionKey");
		Assert.assertTrue(status.equals("Live") && dialcode_1.contains(dialCodeId));

		// Update content metadata to create Image Node
		jsonUpdateMetadata = jsonUpdateMetadata.replace("version_key", versionKey);
		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		;
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonUpdateMetadata).with()
				.contentType("application/json").when().patch("/content/v3/update/" + identifier).then().//log().all().
				spec(get200ResponseSpec());

		// Link 2nd DIAL Code to Content.
		// Expected : 2nd DIAL Code Should be linked with both image and Live Content
		setURI();
		String dialCodeLinkReqUpdated = "{\"request\": {\"content\": {\"identifier\": [\"" + identifier
				+ "\"],\"dialcode\": [\"" + dialCodeId_2 + "\"]}}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeLinkReqUpdated).with().contentType(JSON).when().post("content/v3/dialcode/link").then().//log().all().
				spec(get200ResponseSpec());

		// Get Image Content and validate
		setURI();
		Response R3 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + identifier + "?mode=edit").then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp3 = R3.jsonPath();
		String statusImage = jp3.get("result.content.status");
		ArrayList<String> dialcode_Image = jp3.get("result.content.dialcodes");
		Assert.assertTrue(statusImage.equals("Draft") && dialcode_Image.contains(dialCodeId_2));

		// Publish the content image and validate
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + identifier).then().
				//log().all().
				spec(get200ResponseSpec());
		Thread.sleep(5000);

		// Get Content and validate
		setURI();
		Response R4 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + identifier).then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp4 = R4.jsonPath();
		String statusNew = jp4.get("result.content.status");
		ArrayList<String> dialcodeNew = jp4.get("result.content.dialcodes");
		Assert.assertTrue(statusNew.equals("Live") && dialcodeNew.contains(dialCodeId_2));
	}
	@Ignore
	@Test
	public void createPublisherExpect200() {
		setURI();
		Response res1 = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(createPublisherReq).with().contentType(JSON).when().post("dialcode/v3/publisher/create").then()
				.spec(get200ResponseSpec()).extract().response();

		// Extracting the JSON path
		JsonPath jp1 = res1.jsonPath();
		String nodeId = jp1.get("result.identifier");

		// Get Publisher and validate
		setURI();
		Response res2 = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.when().get("dialcode/v3/publisher/read/" + nodeId).then().spec(get200ResponseSpec()).extract()
				.response();

		JsonPath jP2 = res2.jsonPath();
		String identifier = jP2.get("result.publisher.identifier");
		assertEquals(nodeId, identifier);
	}

	@Test
	public void createPublisherExpect400() {
		String createPublisherReq = "{\"request\": {\"publisher\": {\"identifier\":\"" + publisherId
				+ "\",\"name\": \"Functional Test Publisher\"}}}";
		setURI();
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(createPublisherReq).with().contentType(JSON).when().post("dialcode/v3/publisher/create").then()
				.spec(get400ResponseSpec());
	}

	/*
	 * Given: Valid Publisher Identifier. When: Publisher Read API Hits. Then:
	 * 200 - OK : Publisher Object will be returned.
	 */
	@Ignore
	@Test
	public void readPublisherWithValidIdentifierExpect200() {
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.when().get("/dialcode/v3/publisher/read/" + publisherId).then().spec(get200ResponseSpec()).extract()
				.response();
		JsonPath jp = res.jsonPath();
		String identifier = jp.get("result.publisher.identifier");
		String channel = jp.get("result.publisher.channel");
		assertEquals(publisherId, identifier);
		assertEquals(channelId, channel);
	}

	/*
	 * Given: Invalid Publisher Identifier. When: Publisher Read API Hits. Then:
	 * 404 - RESOURCE_NOT_FOUND : Publisher with Identifier: <Publisher
	 * Identifier given in Request> does not exists.
	 */
	@Ignore
	@Test
	public void readPublisherWithInvalidIdentifierExpect404() {
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.when().get("/dialcode/v3/publisher/read/" + "pub_abcd").then().spec(get404ResponseSpec()).extract()
				.response();
		JsonPath jp = res.jsonPath();
		String errCode = jp.get("params.err");
		assertEquals(errCode, "ERR_INVALID_PUBLISHER_READ_REQUEST");
	}

	/*
	 * Given: Valid Publisher Identifier. When: Publisher Update API Hits. Then:
	 * 200 - OK : <Publisher Identifier given in Request> will be returned in
	 * result.
	 */
	@Ignore
	@Test
	public void updatePublisherExpect200() {
		String updatePublisherReq = "{\"request\": {\"publisher\": {\"name\": \"Functional Test Publisher - Updated\"}}}";
		setURI();
		Response resp = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(updatePublisherReq).with().contentType(JSON).when()
				.patch("dialcode/v3/publisher/update/" + publisherId).then().spec(get200ResponseSpec()).extract()
				.response();
		JsonPath jp = resp.jsonPath();
		String identifier = jp.get("result.identifier");
		assertEquals(identifier, publisherId);

	}

	/*
	 * Given: Invalid Publisher Identifier. When: Publisher Update API Hits.
	 * Then: 404 - RESOURCE_NOT_FOUND : Publisher with Identifier: <Publisher
	 * Identifier given in Request> does not exists.
	 */
	@Ignore
	@Test
	public void updatePublisherWithInvalidIdentifierExpect404() {
		String updatePublisherReq = "{\"request\": {\"publisher\": {\"name\": \"Functional Test Publisher - Updated\"}}}";
		setURI();
		Response resp = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(updatePublisherReq).with().contentType(JSON).when()
				.patch("dialcode/v3/publisher/update/" + "PUBAAAB").then().spec(get404ResponseSpec()).extract()
				.response();
		JsonPath jp = resp.jsonPath();
		String errCode = jp.get("params.err");
		assertEquals("ERR_INVALID_PUBLISHER_UPDATE_REQUEST", errCode);

	}

	@Test
	public void generateDIALCodeExpect200() {
		String dialCodeGenReq = "{\"request\": {\"dialcodes\": {\"count\":2,\"publisher\": \"" + publisherId + "\"}}}";
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeGenReq).with().contentType(JSON).when().post("dialcode/v3/generate").then()
				.spec(get200ResponseSpec()).extract().response();
		JsonPath jp = res.jsonPath();
		List<String> dialcodes = jp.get("result.dialcodes");
		String publisher = jp.get("result.publisher");
		int count = jp.get("result.count");
		assertEquals(2, count);
		assertEquals(publisherId, publisher);
		assertEquals(2, dialcodes.size());
	}

	/*
	 * Given: DialCode, Valid Count (more than configured), Valid Publisher.
	 * When: DialCode Generate API Hits. Then: 200 - OK : DialCodes will be
	 * generated with max count configured in the platform (i.e: 1000)
	 * 
	 */
	@Test
	public void generateDIALCodeWithMoreThanAllowedCountExpect200() {
		String dialCodeGenReq = "{\"request\": {\"dialcodes\": {\"count\":1005,\"publisher\": \"" + publisherId
				+ "\"}}}";
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeGenReq).with().contentType(JSON).when().post("dialcode/v3/generate").then()
				.spec(get207ResponseSpec()).extract().response();
		JsonPath jp = res.jsonPath();
		List<String> dialcodes = jp.get("result.dialcodes");
		String publisher = jp.get("result.publisher");
		int count = jp.get("result.count");
		assertEquals(1000, count);
		assertEquals(publisherId, publisher);
		assertEquals(1000, dialcodes.size());
	}

	/*
	 * Given: DialCode, Valid Count,Valid Publisher,BatchCode. When: DialCode
	 * Generate API Hits. Then: 200 - OK : DialCodes will be generated with
	 * given batch code.
	 * 
	 */
	@Test
	public void generateDIALCodeWithBatchCodeExpect200() {
		String dialCodeGenReq = "{\"request\": {\"dialcodes\": {\"count\":1,\"publisher\": \"" + publisherId
				+ "\",\"batchCode\":\"TEST_BATCH_01\"}}}";
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeGenReq).with().contentType(JSON).when().post("dialcode/v3/generate").then()
				.spec(get200ResponseSpec()).extract().response();
		JsonPath jp = res.jsonPath();
		List<String> dialcodes = jp.get("result.dialcodes");
		String publisher = jp.get("result.publisher");
		String batchcode = jp.get("result.batchcode");
		int count = jp.get("result.count");
		assertEquals(1, count);
		assertEquals(publisherId, publisher);
		assertEquals("TEST_BATCH_01", batchcode);
		assertEquals(1, dialcodes.size());
	}

	/*
	 * Given: DialCode, Invalid Count, Valid Publisher. When: DialCode Generate
	 * API Hits. Then: 400 - CLIENT_ERROR : Please give valid count to generate.
	 */
	@Test
	public void generateDIALCodeWithInvalidCountExpect400() {
		String dialCodeGenReq = "{\"request\": {\"dialcodes\": {\"count\":-2,\"publisher\": \"" + publisherId + "\"}}}";
		setURI();
		Response resp = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeGenReq).with().contentType(JSON).when().post("dialcode/v3/generate").then()
				.spec(get400ResponseSpec()).extract().response();
		JsonPath jPath = resp.jsonPath();
		String responseCode = jPath.getString("responseCode");
		String errorCode = jPath.getString("params.err");
		String errorMessage = jPath.getString("params.errmsg");
		assertEquals("CLIENT_ERROR", responseCode);
		assertEquals("ERR_COUNT_VALIDATION_FAILED", errorCode);
		assertEquals("Please give valid count to generate.", errorMessage);

	}

	@Test
	public void generateDIALCodeExpect400() {
		String dialCodeGenReq = "{\"request\": {\"dialcodes\": {\"count\":2,\"publisher\": \"\"}}}";
		setURI();
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).body(dialCodeGenReq)
				.with().contentType(JSON).when().post("dialcode/v3/generate").then().spec(get400ResponseSpec());
	}

	@Test
	public void readDIALCodeExpect200() {
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.when().get("/dialcode/v3/read/" + dialCodeId).then().spec(get200ResponseSpec()).extract().response();
		JsonPath jp = res.jsonPath();
		String identifier = jp.get("result.dialcode.identifier");
		String channel = jp.get("result.dialcode.channel");
		String publisher = jp.get("result.dialcode.publisher");
		assertEquals(dialCodeId, identifier);
		assertEquals(channelId, channel);
		assertEquals(publisherId, publisher);

	}

	@Test
	public void readDIALCodeExpect404() {
		setURI();
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).when()
				.get("/dialcode/v3/read/" + "AA").then().spec(get404ResponseSpec());
	}

	@Test
	public void updateDIALCodeExpect200() {
		String dialCodeUpdateReq = "{\"request\": {\"dialcode\": {\"metadata\": {\"class\":\"Test Class\",\"subject\":\"Test Subject\",\"board\":\"Test Board\"}}}}";
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeUpdateReq).with().contentType(JSON).when().patch("/dialcode/v3/update/" + dialCodeId)
				.then().spec(get200ResponseSpec()).extract().response();
		JsonPath jp = res.jsonPath();
		String identifier = jp.get("result.identifier");
		assertEquals(identifier, dialCodeId);
	}

	/*
	 * Given: Invalid DialCode Identifier. When: DialCode Update API Hits. Then:
	 * 404 - RESOURCE_NOT_FOUND : Dial Code Not Found With Id: <dialcode
	 * identifier given in URI>
	 * 
	 */
	@Test
	public void updateDIALCodeWithInvalidIdentifierExpect404() {
		String dialCodeUpdateReq = "{\"request\": {\"dialcode\": {\"metadata\": {\"class\":\"Test Class\",\"subject\":\"Test Subject\",\"board\":\"Test Board\"}}}}";
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeUpdateReq).with().contentType(JSON).when().patch("/dialcode/v3/update/" + "ABC123").then()
				.spec(get404ResponseSpec()).extract().response();
		JsonPath jp = res.jsonPath();
		String errorCode = jp.get("params.err");
		assertEquals("ERR_DIALCODE_INFO", errorCode);
	}

	/*
	 * Given: DialCode with Live Status, metadata to be updated. When: DialCode
	 * Update API Hits. Then: 400 - CLIENT_ERROR : Dial Code with Live status
	 * can't be updated.
	 * 
	 */
	@Test
	public void updatePublishedDIALCodeExpect400() {
		//generate DialCode
		String dialCodeGenReq = "{\"request\": {\"dialcodes\": {\"count\":1,\"publisher\": \"" + publisherId + "\"}}}";
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeGenReq).with().contentType(JSON).when().post("dialcode/v3/generate").then().extract()
				.response();
		JsonPath jp = res.jsonPath();
		List<String> dialcodes = jp.get("result.dialcodes");
		String dialCode = dialcodes.get(0);

		//Publish DialCode
		String dialCodePublishReq = "{}";
		setURI();
		Response resp = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodePublishReq).with().contentType(JSON).when().post("/dialcode/v3/publish/" + dialCode)
				.then().extract().response();
		JsonPath jp1 = resp.jsonPath();
		String identifier = jp1.get("result.identifier");
		assertEquals(identifier, dialCode);

		//Update DialCode
		String dialCodeUpdateReq = "{\"request\": {\"dialcode\": {\"metadata\": {\"class\":\"Test Class\",\"subject\":\"Test Subject\",\"board\":\"Test Board\"}}}}";
		setURI();
		Response response = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeUpdateReq).with().contentType(JSON).when().patch("/dialcode/v3/update/" + dialCode).then()
				.spec(get400ResponseSpec()).extract().response();
		JsonPath jPath = response.jsonPath();
		String errorCode = jPath.get("params.err");
		String errorMessage = jPath.get("params.errmsg");
		assertEquals("ERR_DIALCODE_UPDATE", errorCode);
		assertEquals("Dial Code with Live status can't be updated.", errorMessage);
	}

	@Test
	public void publishDIALCodeExpect200() {
		String dialCodePublishReq = "{}";
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodePublishReq).with().contentType(JSON).when().post("/dialcode/v3/publish/" + dialCodeId)
				.then().spec(get200ResponseSpec()).extract().response();
		JsonPath jp = res.jsonPath();
		String identifier = jp.get("result.identifier");
		assertEquals(identifier, dialCodeId);

		setURI();
		Response res2 = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.when().get("/dialcode/v3/read/" + dialCodeId).then().spec(get200ResponseSpec()).extract().response();
		JsonPath jp2 = res2.jsonPath();
		String id = jp2.get("result.dialcode.identifier");
		String status = jp2.get("result.dialcode.status");
		assertEquals(dialCodeId, id);
		assertEquals("Live", status);
	}

	/*
	 * Given: Invalid DialCode Identifier. When: DialCode Publish API Hits.
	 * Then: 404 - RESOURCE_NOT_FOUND : Dial Code Not Found With Id: <dialcode
	 * identifier given in URI>
	 */
	@Test
	public void publishDIALCodeWithInvalidIdentifierExpect404() {
		String dialCodePublishReq = "{}";
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodePublishReq).with().contentType(JSON).when().post("/dialcode/v3/publish/" + "ABCDEF")
				.then().spec(get404ResponseSpec()).extract().response();
		JsonPath jp = res.jsonPath();
		String err = jp.get("params.err");
		assertEquals("ERR_DIALCODE_INFO", err);

	}

	/*
	 * Given: Valid DialCode Identifier, Invalid Channel ID (Different Channel
	 * Id which is not used while generating DialCode). When: DialCode Publish
	 * API Hits. Then: 400 - CLIENT_ERROR : Invalid Channel Id.
	 */
	@Test
	public void publishDIALCodeWithInvalidChannelExpect400() {
		String dialCodePublishReq = "{}";
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, "channelABC", appId))
				.body(dialCodePublishReq).with().contentType(JSON).when().post("/dialcode/v3/publish/" + dialCodeId)
				.then().spec(get400ResponseSpec()).extract().response();
		JsonPath jp = res.jsonPath();
		String responseCode = jp.get("responseCode");
		String errCode = jp.getString("params.err");
		assertEquals("ERR_INVALID_CHANNEL_ID", errCode);
		assertEquals("CLIENT_ERROR", responseCode);

	}

	@Test
	public void listDialCodeExpect200() {
		String listDialCodeReq = "{\"request\": {\"search\": {\"publisher\":\"" + publisherId + "\"}}}";
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(listDialCodeReq).with().contentType(JSON).when().post("dialcode/v3/list").then()
				.spec(get200ResponseSpec()).extract().response();
		JsonPath jp = res.jsonPath();
		int count = jp.get("result.count");
		assertEquals(1002, count);
	}

	/*
	 * Given: Valid Publisher Identifier, Invalid Channel ID (Different Channel
	 * Id which is not used while generating DialCode). When: DialCode List API
	 * Hits. Then: 200 - OK : No DialCode Object will be returned.
	 * 
	 */
	@Test
	public void listDialCodeWithInvalidChannelExpect200() {
		String listDialCodeReq = "{\"request\": {\"search\": {\"publisher\":\"" + publisherId + "\"}}}";
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, "testABC", appId))
				.body(listDialCodeReq).with().contentType(JSON).when().post("dialcode/v3/list").then()
				.spec(get200ResponseSpec()).extract().response();
		JsonPath jp = res.jsonPath();
		int count = jp.get("result.count");
		assertEquals(0, count);
	}

	/*
	 * Given: Valid Channel Id. When: DialCode List API Hits. Then: 400 -
	 * CLIENT_ERROR : Publisher is mandatory to list DailCodes
	 */
	@Test
	public void listDialCodeWithoutPublisherExpect400() {
		String listDialCodeReq = "{\"request\": {\"search\": {}}}";
		setURI();
		Response resp = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(listDialCodeReq).with().contentType(JSON).when().post("dialcode/v3/list").then()
				.spec(get400ResponseSpec()).extract().response();
		JsonPath jp = resp.jsonPath();
		String resCode = jp.get("responseCode");
		String errCode = jp.get("params.err");
		String errMsg = jp.get("params.errmsg");
		assertEquals("CLIENT_ERROR", resCode);
		assertEquals("ERR_INVALID_SEARCH_REQUEST", errCode);
		assertEquals("Publisher is mandatory to list DailCodes", errMsg);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void searchDialCodeExpect200() throws InterruptedException {
		Thread.sleep(2000);
		String searchReq = "{\"request\": {\"search\": {\"identifier\": \"" + dialCodeId + "\"}}}";
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(searchReq).with().contentType(JSON).when().post("dialcode/v3/search").then()
				.spec(get200ResponseSpec()).extract().response();
		JsonPath jp = res.jsonPath();
		int count = jp.get("result.count");
		List<Object> dialcodes = jp.get("result.dialcodes");
		Map<String, Object> map = null;
		if (1 == count)
			map = (Map<String, Object>) dialcodes.get(0);
		String identifier = (String) map.get("identifier");
		assertEquals(dialCodeId, identifier);
	}

	/*
	 * Given: Valid DialCode Identifier, Invalid Channel ID (Different Channel
	 * Id which is not used while generating DialCode). When: DialCode Search
	 * API Hits. Then: 200 - OK : No DialCode Object will be returned.
	 */
	@Test
	public void searchDialCodeWithInvalidChannelExpect200() throws InterruptedException {
		Thread.sleep(2000);
		String searchReq = "{\"request\": {\"search\": {\"identifier\": \"" + dialCodeId + "\"}}}";
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, "testABC", appId))
				.body(searchReq).with().contentType(JSON).when().post("dialcode/v3/search").then()
				.spec(get200ResponseSpec()).extract().response();
		JsonPath jp = res.jsonPath();
		int count = jp.get("result.count");
		assertEquals(0, count);
	}

	/*
	 * Given: Valid Publisher, Batch Code, Status & Valid Channel ID. When:
	 * DialCode Search API Hits. Then: 200 - OK : All DialCode Objects will be
	 * returned which match with criteria given in request
	 */
	@Test
	public void searchDialCodeWithValidCriteriaExpect200() throws InterruptedException {
		Thread.sleep(5000);
		String searchReq = "{\"request\": {\"search\": {\"identifier\": \"" + dialCodeId + "\"}}}";
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(searchReq).with().contentType(JSON).when().post("dialcode/v3/search").then()
				.spec(get200ResponseSpec()).extract().response();
		JsonPath jp = res.jsonPath();
		int count = jp.get("result.count");
		List<Object> dialcodes = jp.get("result.dialcodes");
		Map<String, Object> map = null;
		if (1 == count)
			map = (Map<String, Object>) dialcodes.get(0);
		String identifier = (String) map.get("identifier");
		String status = (String) map.get("status");
		assertEquals(dialCodeId, identifier);
	}

	@Test
	public void linkDialCodeToContentExpect200() {
		setURI();
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + contentId_1
				+ "\"],\"dialcode\": [\"" + dialCodeId + "\"]}}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then()
				//.log().all()
				.spec(get200ResponseSpec());
	}

	@Test
	public void linkDialCodeToContentsExpect200() throws Exception {
		Thread.sleep(3000);
		setURI();
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + contentId_1 + "\",\""
				+ contentId_2 + "\"],\"dialcode\": [\"" + dialCodeId + "\"]}}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then()
				.spec(get200ResponseSpec());
	}

	@Test
	public void linkDialCodesToContentExpect200() {
		setURI();
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + contentId_1
				+ "\"],\"dialcode\": [\"" + dialCodeId + "\",\"" + dialCodeId_2 + "\"]}}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then()
				.spec(get200ResponseSpec());
	}

	@Test
	public void linkDialCodesToContentsExpect200() {
		setURI();
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + contentId_1 + "\",\""
				+ contentId_2 + "\"],\"dialcode\": [\"" + dialCodeId + "\",\"" + dialCodeId_2 + "\"]}}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then()
				.spec(get200ResponseSpec());
	}

	@Test
	public void linkDialCodeToContentExpect404() {
		setURI();
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + "do_ABC123"
				+ "\"],\"dialcode\": [\"" + dialCodeId + "\"]}}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then()
				.spec(get404ResponseSpec());
	}

	@Test
	public void linkDialCodeToContent_Bulk_Expect200() {
		setURI();
		String dialCodeLinkReq = "{\"request\":{\"content\":[{\"dialcode\":\"" + dialCodeId + "\",\"identifier\":\""
				+ contentId_1 + "\"},{\"dialcode\":\"" + dialCodeId_2 + "\",\"identifier\":\"" + contentId_1 + "\"}]}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then()
				.spec(get200ResponseSpec());
	}
	@Ignore
	@Test
	public void linkDialCodeToContent_Bulk_Expect207() {
		setURI();
		String dialCodeLinkReq = "{\"request\":{\"content\":[{\"dialcode\":\"" + dialCodeId + "\",\"identifier\":\""
				+ contentId_1 + "\"},{\"dialcode\":\"" + dialCodeId_2 + "\",\"identifier\":\"" + "do_abc123" + "\"}]}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then()
				.spec(get207ResponseSpec());
	}

	@Test
	public void linkDialCodeToContent_Bulk_Expect404() {
		setURI();
		String dialCodeLinkReq = "{\"request\":{\"content\":[{\"dialcode\":\"" + "ABCAAA" + "\",\"identifier\":\""
				+ contentId_1 + "\"},{\"dialcode\":\"" + dialCodeId_2 + "\",\"identifier\":\"" + "do_abc123" + "\"}]}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then()
				.spec(get404ResponseSpec());
	}

	@Test
	public void linkDialCodeToContent_Bulk_1_Expect404() {
		setURI();
		String dialCodeLinkReq = "{\"request\":{\"content\":[{\"dialcode\":\"" + dialCodeId + "\",\"identifier\":\""
				+ "do_def123" + "\"},{\"dialcode\":\"" + dialCodeId_2 + "\",\"identifier\":\"" + "do_abc123" + "\"}]}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then()
				.spec(get404ResponseSpec());
	}

	//Un Link Dial Code. Provided Blank DialCode (""). Expected : 200 - OK.
	@Test
	public void unLinkDialCodeToContent_1_Expect200() throws Exception {
		//Create Content
		int rn = generateRandomInt(0, 999999);
		String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn
				+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"
				+ rn
				+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/pdf\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(createValidContent).with().contentType(JSON).when().post("content/v3/create").then().//log().all().
				extract().response();

		JsonPath jp = res.jsonPath();
		String identifier = jp.get("result.node_id");

		Thread.sleep(3000);

		// Link DIAL Code to Content
		setURI();
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + identifier
				+ "\"],\"dialcode\": [\"" + dialCodeId + "\"]}}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then().//log().all().
				spec(get200ResponseSpec());

		//UnLink DIAL Code From Content

		setURI();
		String dialCodeUnLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + identifier
				+ "\"],\"dialcode\":\"\"}}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeUnLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then().//log().all().
				spec(get200ResponseSpec());

		// Get Content and Validate
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + identifier).then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp2 = R2.jsonPath();
		ArrayList<String> dialcode_1 = jp2.get("result.content.dialcodes");
		Assert.assertEquals(null, dialcode_1);
	}

	//Un Link Dial Code. Provided Blank DialCode ([]). Expected : 200 - OK.
	@Test
	public void unLinkDialCodeToContent_2_Expect200() throws Exception {
		//Create Content
		int rn = generateRandomInt(0, 999999);
		String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn
				+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"
				+ rn
				+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/pdf\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(createValidContent).with().contentType(JSON).when().post("content/v3/create").then()//.log().all()
				.extract().response();

		JsonPath jp = res.jsonPath();
		String identifier = jp.get("result.node_id");

		Thread.sleep(3000);

		// Link DIAL Code to Content
		setURI();
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + identifier
				+ "\"],\"dialcode\": [\"" + dialCodeId + "\"]}}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then().//log().all().
				spec(get200ResponseSpec());

		//UnLink DIAL Code From Content

		setURI();
		String dialCodeUnLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + identifier
				+ "\"],\"dialcode\":[]}}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeUnLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then().//log().all().
				spec(get200ResponseSpec());

		// Get Content and Validate
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + identifier).then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp2 = R2.jsonPath();
		ArrayList<String> dialcode_1 = jp2.get("result.content.dialcodes");
		Assert.assertEquals(null, dialcode_1);
	}

	//Un Link Dial Code. Provided Blank DialCode ([""]). Expected : 200 - OK.
	@Test
	public void unLinkDialCodeToContent_3_Expect200() throws Exception {
		//Create Content
		int rn = generateRandomInt(0, 999999);
		String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn
				+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"
				+ rn
				+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/pdf\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(createValidContent).with().contentType(JSON).when().post("content/v3/create").then().//log().all().
				extract().response();

		JsonPath jp = res.jsonPath();
		String identifier = jp.get("result.node_id");

		Thread.sleep(3000);

		// Link DIAL Code to Content
		setURI();
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + identifier
				+ "\"],\"dialcode\": [\"" + dialCodeId + "\"]}}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then().//log().all().
				spec(get200ResponseSpec());

		//UnLink DIAL Code From Content

		setURI();
		String dialCodeUnLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + identifier
				+ "\"],\"dialcode\":[\"\"]}}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeUnLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then().//log().all().
				spec(get200ResponseSpec());

		// Get Content and Validate
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + identifier).then()
				//.log().all()
				.spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp2 = R2.jsonPath();
		ArrayList<String> dialcode_1 = jp2.get("result.content.dialcodes");
		Assert.assertEquals(null, dialcode_1);
	}

	/*
	 * Given: Invalid Content Identifier(s). When: Content Link API Hits. Then:
	 * 404 - RESOURCE_NOT_FOUND : Content not found with id(s):<Invalid Content
	 * identifier given in request>
	 * 
	 */
	@Test
	public void unlinkDialCodeWithInvalidContentIdentifier() {
		setURI();
		String dialCodeUnLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + "do_ABC11111"
				+ "\"],\"dialcode\":[\"\"]}}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeUnLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then().//log().all().
				spec(get404ResponseSpec());
	}

	/*
	 * Given: DialCode Identifier(s) which need not be removed/unlinked, Valid
	 * Content Id(s). When: Content Link API Hits. Then: 200 - OK : null
	 * 
	 */
	@Test
	public void unLinkPartialDialCodeToContentExpect200() throws Exception {
		//Create Content
		int rn = generateRandomInt(0, 999999);
		String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn
				+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"
				+ rn
				+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/pdf\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(createValidContent).with().contentType(JSON).when().post("content/v3/create").then()//.log().all()
				.extract().response();

		JsonPath jp = res.jsonPath();
		String identifier = jp.get("result.node_id");

		Thread.sleep(3000);

		// Link DIAL Code to Content
		setURI();
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + identifier
				+ "\"],\"dialcode\": [\"" + dialCodeId + "\",\"" + dialCodeId_2 + "\"]}}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then().//log().all().
				spec(get200ResponseSpec());

		//UnLink DIAL Code From Content

		setURI();
		String dialCodeUnLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + identifier
				+ "\"],\"dialcode\":[\"" + dialCodeId_2 + "\"]}}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeUnLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then().//log().all().
				spec(get200ResponseSpec());

		// Get Content and Validate
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + identifier).then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp2 = R2.jsonPath();
		ArrayList<String> dialcode_1 = jp2.get("result.content.dialcodes");
		Assert.assertEquals(dialCodeId_2, dialcode_1.get(0));
	}

	//Link Dial Code to Content (Provided Blank Identifier ("")). Expected: 400
	@Test
	public void linkDialCodeToInvalidContent_1_Expect400() {
		setURI();
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": \"\",\"dialcode\": [\"" + dialCodeId
				+ "\"]}}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then()
				//.log().all()
				.spec(get400ResponseSpec());
	}

	//Link Dial Code to Content (Provided Blank Identifier ([""])). Expected: 400
	@Test
	public void linkDialCodeToInvalidContent_2_Expect400() {
		setURI();
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"\"],\"dialcode\": [\"" + dialCodeId
				+ "\"]}}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then()//.log().all()
				.spec(get400ResponseSpec());
	}

	//Link Dial Code to Content (Provided Blank Identifier ([""])). Expected: 400
	@Test
	public void linkDialCodeToInvalidContent_3_Expect400() {
		setURI();
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [],\"dialcode\": [\"" + dialCodeId
				+ "\"]}}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then()//.log().all()
				.spec(get400ResponseSpec());
	}

	//Link Dial Code to Content (Provided Blank Identifier (" ")). Expected: 400
	//Actual : 404
	@Ignore
	@Test
	public void linkDialCodeToInvalidContent_4_Expect400() {
		setURI();
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": \" \",\"dialcode\": [\"" + dialCodeId
				+ "\"]}}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then()//.log().all()
				.spec(get400ResponseSpec());
	}

}