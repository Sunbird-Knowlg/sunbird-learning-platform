package org.sunbird.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import org.sunbird.platform.domain.BaseTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
@Ignore
public class ObjectChannelIdAppIdConsumerIdTest extends BaseTest {
	
	public RequestSpecification getRequestSpecWithHeaderExceptChannelId(String content_type,String user_id)
	{
		RequestSpecBuilder builderreq = new RequestSpecBuilder();
		builderreq.addHeader("Content-Type", content_type);
		builderreq.addHeader("user-id", user_id);
		builderreq.addHeader("consumerId", "FT_DEMO_CONSUMER_ID");
		builderreq.addHeader("channelId", "FT_DEMO_CHANNEL_ID");
		builderreq.addHeader("appId", "FT_DEMO_APP_ID");
		RequestSpecification requestSpec = builderreq.build();
		return requestSpec;
	}
	
	int rn = generateRandomInt(0, 9999999);
	
	String DEF_CHANNEL_ID = "in.ekstep";

	String jsonCreateValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn
			+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"
			+ rn
			+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"pkgVersion\": 3,\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
	String jsonContentClean = "{\"request\": {\"searchProperty\": \"name\",\"searchOperator\": \"startsWith\",\"searchString\": \"LP_FT_\"}}";

	// Content clean up
	public void contentCleanUp() {
		setURI();
		given().body(jsonContentClean).with().contentType(JSON).when()
				.post("learning/v1/exec/content_qe_deleteContentBySearchStringInField");
	}

	@Test
	public void createValidContentWithHeaderExpect200() {
		contentCleanUp();
		setURI();
		Response R = given().spec(getRequestSpecWithHeaderExceptChannelId(contentType, validuserId)).body(jsonCreateValidContent)
				.with().contentType(JSON).when().post("/learning/v2/content/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String node = jp.get("result.node_id");

		// Get content and validate
		setURI();
		Response R1 = given().spec(getRequestSpec(contentType, validuserId)).when()
				.get("/content/v3/read/" + node + "?fields=consumerId,appId,channel").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String consumerId = jP1.get("result.content.consumerId");
		String channelId = jP1.get("result.content.channelId");
		String appId = jP1.get("result.content.appId");
		Assert.assertTrue(consumerId!=null);
		Assert.assertTrue(channelId!=null);
		Assert.assertTrue(appId!=null);
		Assert.assertEquals("FT_DEMO_CONSUMER_ID", consumerId);
		Assert.assertEquals("FT_DEMO_CHANNEL_ID", channelId);
		Assert.assertEquals("FT_DEMO_APP_ID", appId);
	}
	
	@Test
	public void createValidContentWithoutHeaderExpect200() {
		contentCleanUp();
		setURI();
		Response R = given().spec(getRequestSpec(contentType, validuserId)).body(jsonCreateValidContent)
				.with().contentType(JSON).when().post("/content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String node = jp.get("result.node_id");

		// Get content and validate
		setURI();
		Response R1 = given().spec(getRequestSpec(contentType, validuserId)).when()
				.get("/learning/v3/public/content/read/" + node + "?fields=consumerId,appId,channel").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String consumerId = jP1.get("result.content.consumerId");
		String channelId = jP1.get("result.content.channelId");
		String appId = jP1.get("result.content.appId");
		Assert.assertTrue(consumerId==null);
		Assert.assertTrue(channelId==null);
		Assert.assertTrue(appId==null);
	}
	
	@Test
	public void createValidContentWithoutChannelIdExpect200() {
		contentCleanUp();
		setURI();
		Response R = given().spec(getRequestSpecWithHeaderExceptChannelId(contentType, validuserId)).body(jsonCreateValidContent)
				.with().contentType(JSON).when().post("/content/v2/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String node = jp.get("result.node_id");

		// Get content and validate
		setURI();
		Response R1 = given().spec(getRequestSpec(contentType, validuserId)).when()
				.get("/learning/v3/public/content/read/" + node + "?fields=consumerId,appId,channel").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String consumerId = jP1.get("result.content.consumerId");
		String channelId = jP1.get("result.content.channelId");
		String appId = jP1.get("result.content.appId");
		Assert.assertTrue(consumerId!=null);
		Assert.assertTrue(channelId!=null);
		Assert.assertTrue(appId!=null);
		Assert.assertEquals("FT_DEMO_CONSUMER_ID", consumerId);
		Assert.assertEquals("FT_DEMO_CHANNEL_ID", DEF_CHANNEL_ID);
		Assert.assertEquals("FT_DEMO_APP_ID", appId);
	}

}
