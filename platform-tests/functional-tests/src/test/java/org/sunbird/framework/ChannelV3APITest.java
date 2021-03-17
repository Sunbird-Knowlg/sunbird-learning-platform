package org.sunbird.framework;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.platform.domain.BaseTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

/**
 * @author gauraw
 *
 */
public class ChannelV3APITest extends BaseTest {
	int rn = generateRandomInt(0, 999999);

	private static String channelId = "";
	private static String frameworkId = "";
	private static String versionKey = "";
	private static String frameworkName = "Functional Test Framework";

	@Before
	public void init() {
		if (StringUtils.isBlank(channelId))
			createChannel();
		if (StringUtils.isBlank(frameworkId))
			createFramework();
	}

	private void createChannel() {
		setURI();
		String createChannelReq = "{\"request\": {\"channel\":{\"name\":\"LFT_Test\",\"description\":\"Channel for Functional Test\",\"code\":\"LFT_"
				+ rn + "\"}}}";
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(createChannelReq).with().contentType(JSON).when().post("channel/v3/create").then().extract()
				.response();
		JsonPath jp1 = res.jsonPath();
		channelId = jp1.get("result.node_id");
		versionKey = jp1.get("result.versionKey");
	}

	private void createFramework() {
		setURI();
		String createFrameworkReq = "{\"request\": {\"framework\": {\"name\": \"Functional Test Framework\",\"description\": \"Functional Test Framework\",\"code\": \"org.sunbird.framework.lft_"
				+ rn + "\"}}}";
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(createFrameworkReq).with().contentType(JSON).when().post("framework/v3/create").then().extract()
				.response();
		JsonPath jp1 = res.jsonPath();
		frameworkId = jp1.get("result.node_id");
	}

	@Test
	public void createChannelExpect200() {
		setURI();
		String createChannelReq = "{\"request\": {\"channel\":{\"name\":\"LFT_Test\",\"description\":\"Channel for Functional Test\",\"code\":\"LFT_"
				+ rn + "\"}}}";
		Response res1 = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(createChannelReq).with().contentType(JSON).when().post("channel/v3/create").then()
				.spec(get200ResponseSpec()).extract().response();

		// Extracting the JSON path
		JsonPath jp1 = res1.jsonPath();
		String nodeId = jp1.get("result.node_id");

		// Get Channel and validate
		setURI();
		Response res2 = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.when().get("channel/v3/read/" + nodeId).then().spec(get200ResponseSpec()).extract().response();

		JsonPath jp2 = res2.jsonPath();
		String identifier = jp2.get("result.channel.identifier");
		assertEquals(nodeId, identifier);
	}

	@Test
	public void createChannelExpect400() {
		setURI();
		String createChannelReq = "{\"request\": {\"channel\":{\"name\":\"LFT_Test\",\"description\":\"Channel for Functional Test\",\"code\":\"\"}}}";
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(createChannelReq).with().contentType(JSON).when().post("channel/v3/create").then()
				.spec(get400ResponseSpec());
	}

	@Test
	public void readChannelExpect200() {
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.with().contentType(JSON).when().get("channel/v3/read/" + channelId).then().spec(get200ResponseSpec())
				.extract().response();
		JsonPath jp = res.jsonPath();
		String identifier = jp.get("result.channel.identifier");
		assertEquals(channelId, identifier);
	}

	@Test
	public void readChannelExpect404() {
		setURI();
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).with()
				.contentType(JSON).when().get("channel/v3/read/" + "ABCTTT").then().spec(get404ResponseSpec());
	}

	@Test
	public void listChannelExpect200() {
		setURI();
		String listChannelReq = "{\"request\": {}}";
		Response res1 = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.body(listChannelReq).with().contentType(JSON).when().post("channel/v3/list").then()
				.spec(get200ResponseSpec()).extract().response();

		JsonPath jp1 = res1.jsonPath();
		int count = (int) jp1.get("result.count");
		Assert.assertNotEquals(0, count);
	}

	@Test
	public void updateChannelExpect200() {
		String updateChannelReq = "{\"request\": {\"channel\":{\"frameworks\": [{\"identifier\": \"" + frameworkId
				+ "\",\"name\": \"" + frameworkName + "\"}]}}}";
		setURI();
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).with()
				.body(updateChannelReq).contentType(JSON).when().patch("channel/v3/update/" + channelId).then()
				.spec(get200ResponseSpec());
		setURI();
		Response res = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId))
				.with().contentType(JSON).when().get("channel/v3/read/" + channelId).then().spec(get200ResponseSpec())
				.extract().response();
		JsonPath jp = res.jsonPath();
		List<Map<String, Object>> frameworks = jp.get("result.channel.frameworks");
		Map<String, Object> framework = frameworks.get(0);
		String identifier = (String) framework.get("identifier");
		assertEquals(frameworkId, identifier);

	}

	@Test
	public void updateChannelExpect400() {
		String updateChannelReq = "{\"request\": {\"channel\":{\"frameworks\": [{\"identifier\": \"" + "ABC11111"
				+ "\",\"name\": \"" + frameworkName + "\"}]}}}";
		setURI();
		given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).with()
				.body(updateChannelReq).contentType(JSON).when().patch("channel/v3/update/" + channelId).then()
				.spec(get400ResponseSpec());
	}

}
