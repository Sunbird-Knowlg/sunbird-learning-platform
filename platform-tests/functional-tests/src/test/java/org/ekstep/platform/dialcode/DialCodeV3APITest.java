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
					+ "\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"
					+ rn
					+ "\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/pdf\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
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

	@Test
	public void listDialCodeExpect200() {
		String listDialCodeReq = "{\"request\": {\"search\": {\"publisher\":\"" + publisherId + "\"}}}";
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(listDialCodeReq).with().contentType(JSON).when().post("dialcode/v3/list").then()
				.spec(get200ResponseSpec()).extract().response();
		JsonPath jp = res.jsonPath();
		int count = jp.get("result.count");
		assertEquals(2, count);
	}

	@Test
	public void listDialCodeExpect400() {
		String listDialCodeReq = "{\"request\": {\"search\": {}}}";
		setURI();
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(listDialCodeReq).with().contentType(JSON).when().post("dialcode/v3/list").then()
				.spec(get400ResponseSpec());
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

	@Test
	public void linkDialCodeToContentExpect200() {
		setURI();
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + contentId_1
				+ "\"],\"dialcode\": [\"" + dialCodeId + "\"]}}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then()
				.spec(get200ResponseSpec());
	}

	@Test
	public void linkDialCodeToContentsExpect200() {
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
	public void linkDialCodesToContentsExpect400() {
		setURI();
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + contentId_1 + "\",\""
				+ contentId_2 + "\"],\"dialcode\": [\"" + dialCodeId + "\",\"" + "ABC123" + "\"]}}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then()
				.spec(get400ResponseSpec());
	}

	@Test
	public void linkDialCodeToContentExpect400() {
		setURI();
		String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + "do_ABC123"
				+ "\"],\"dialcode\": [\"" + dialCodeId + "\"]}}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then()
				.spec(get400ResponseSpec());
	}
	
	@Test
	public void linkDialCodeToContent_Bulk_Expect200() {
		setURI();
		String dialCodeLinkReq="{\"request\":{\"content\":[{\"dialcode\":\""+dialCodeId+"\",\"identifier\":\""+contentId_1+"\"},{\"dialcode\":\""+dialCodeId_2+"\",\"identifier\":\""+contentId_1+"\"}]}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then()
				.spec(get200ResponseSpec());
	}
	
	@Test
	public void linkDialCodeToContent_Bulk_Expect207() {
		setURI();
		String dialCodeLinkReq="{\"request\":{\"content\":[{\"dialcode\":\""+dialCodeId+"\",\"identifier\":\""+contentId_1+"\"},{\"dialcode\":\""+dialCodeId_2+"\",\"identifier\":\""+"do_abc123"+"\"}]}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then()
				.spec(get207ResponseSpec());
	}
	
	@Test
	public void linkDialCodeToContent_Bulk_Expect400() {
		setURI();
		String dialCodeLinkReq="{\"request\":{\"content\":[{\"dialcode\":\""+"ABCAAA"+"\",\"identifier\":\""+contentId_1+"\"},{\"dialcode\":\""+dialCodeId_2+"\",\"identifier\":\""+"do_abc123"+"\"}]}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then()
				.spec(get400ResponseSpec());
	}
	
	@Test
	public void linkDialCodeToContent_Bulk_1_Expect400() {
		setURI();
		String dialCodeLinkReq="{\"request\":{\"content\":[{\"dialcode\":\""+dialCodeId+"\",\"identifier\":\""+"do_def123"+"\"},{\"dialcode\":\""+dialCodeId_2+"\",\"identifier\":\""+"do_abc123"+"\"}]}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(dialCodeLinkReq).with().contentType(JSON).when().post("content/v3/dialcode/link").then()
				.spec(get400ResponseSpec());
	}

}