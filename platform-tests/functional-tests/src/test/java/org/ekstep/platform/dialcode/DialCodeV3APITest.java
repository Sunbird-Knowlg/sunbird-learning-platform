package org.ekstep.platform.dialcode;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.platform.domain.BaseTest;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

/**
 * @author gauraw
 *
 */
public class DialCodeV3APITest extends BaseTest {

	int rn = generateRandomInt(0, 999999);

	private String createPublisherReq = "{\"request\": {\"publisher\": {\"identifier\":\"LFT_PUB_" + rn
			+ "\",\"name\": \"Functional Test Publisher\"}}}";

	private static String publisherId = "";
	private static String dialCodeId = "";

	@Before
	public void init() {
		if (StringUtils.isBlank(publisherId))
			createPublisher();
		if (StringUtils.isBlank(dialCodeId))
			generateDialCode();
	}

	private void createPublisher() {
		String createPublisherReq = "{\"request\": {\"publisher\": {\"identifier\":\"LFT_PUB_" + rn
				+ "\",\"name\": \"Functional Test Publisher\"}}}";
		setURI();
		Response R = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(createPublisherReq).with().contentType(JSON).when().post("dialcode/v3/publisher/create").then()
				.spec(get200ResponseSpec()).extract().response();
		JsonPath jp = R.jsonPath();
		publisherId = jp.get("result.identifier");
	}

	private void generateDialCode() {
		String dialCodeGenReq = "{\"request\": {\"dialcodes\": {\"count\":1,\"publisher\": \"" + publisherId + "\"}}}";
		setURI();
		Response R = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeGenReq).with().contentType(JSON).when().post("dialcode/v3/generate").then()
				.spec(get200ResponseSpec()).extract().response();
		JsonPath jp = R.jsonPath();
		List<String> dialcodes = jp.get("result.dialcodes");
		dialCodeId = dialcodes.get(0);
	}

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
				.when()
				.get("dialcode/v3/publisher/read/" + nodeId).then().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP2 = res2.jsonPath();
		String identifier = jP2.get("result.publisher.identifier");
		assertEquals(nodeId, identifier);
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
	public void updateDIALCodeExpect200() {
		String dialCodeUpdateReq = "{\"request\": {\"dialcode\": {\"metadata\": {\"class\":\"Test Class\",\"subject\":\"Test Subject\",\"board\":\"Test Board\"}}}}";
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeUpdateReq).with().contentType(JSON).when().patch("/dialcode/v3/update/" + dialCodeId)
				.then()
				.spec(get200ResponseSpec()).extract().response();
		JsonPath jp = res.jsonPath();
		String identifier = jp.get("result.identifier");
		assertEquals(identifier, dialCodeId);
	}

	@Test
	public void publishDIALCodeExpect200() {
		String dialCodePublishReq = "{}";
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodePublishReq).with().contentType(JSON).when().post("/dialcode/v3/publish/" + dialCodeId)
				.then()
				.spec(get200ResponseSpec()).extract().response();
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

	@Test
	public void listDialCodeExpect200() {
		String listDialCodeReq = "{\"request\": {\"search\": {\"publisher\":\"" + publisherId + "\"}}}";
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(listDialCodeReq).with().contentType(JSON).when().post("dialcode/v3/list").then()
				.spec(get200ResponseSpec()).extract().response();
		JsonPath jp = res.jsonPath();
		int count = jp.get("result.count");
		assertEquals(1, count);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void searchDialCodeExpect200() {
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

}