package org.ekstep.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.platform.domain.BaseTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class H5PContentWorkflowTest extends BaseTest {

	String jsonCreateValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_H5PContentWorkflowTest\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_H5PContentWorkflowTest\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.h5p-archive\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
	String jsonContentClean = "{\"request\": {\"searchProperty\": \"identifier\",\"searchOperator\": \"startsWith\",\"searchString\": \"LP_FT_H5PContentWorkflowTest\"}}";
	String publishMessage = "Publish Operation for Content Id 'LP_FT_H5PContentWorkflowTest' Started Successfully!";

	static ClassLoader classLoader = ContentPublishWorkflowTests.class.getClassLoader();
	static URL url = classLoader.getResource("DownloadedFiles");
	static File downloadPath;
	static File path = new File(classLoader.getResource("UploadFiles/").getFile());

	@BeforeClass
	public static void setup() throws URISyntaxException {
		downloadPath = new File(url.toURI().getPath());
	}

	@AfterClass
	public static void end() throws IOException {
		FileUtils.cleanDirectory(downloadPath);

	}

	// Content clean up
	public void contentCleanUp() {
		setURI();
		given().body(jsonContentClean).with().contentType(JSON).when()
				.post("learning/v1/exec/content_qe_deleteContentBySearchStringInField");
	}

	@Test
	public void createUploadAndPublishH5PContentExpectSuccess200() {
		// Pre-Created Content Cleanup
		contentCleanUp();

		// Content Create
		setURI();
		Response r1 = given().spec(getRequestSpec(contentType, validuserId)).body(jsonCreateValidContent).with()
				.contentType(JSON).when().post("/content/v3/create").then().spec(get200ResponseSpec()).extract()
				.response();

		// Extracting the JSON path
		JsonPath jp = r1.jsonPath();
		String nodeId = jp.get("result.node_id");
		String versionKey = jp.get("result.versionKey");
		Assert.assertTrue(StringUtils.isNotBlank(nodeId));
		Assert.assertTrue(StringUtils.isNotBlank(versionKey));

		// Content Upload
		setURI();
		Response r2 = given().spec(getRequestSpec(uploadContentType, validuserId))
				.multiPart(new File(path + "/valid_h5p_content.h5p")).when().post("/content/v3/upload/" + nodeId).then()
				.spec(get200ResponseSpec()).extract().response();
		JsonPath jp2 = r2.jsonPath();
		String id = jp2.get("result.node_id");
		String contentUrl = jp2.get("result.content_url");
		versionKey = jp2.get("result.versionKey");
		Assert.assertTrue(StringUtils.isNotBlank(id));
		Assert.assertTrue(StringUtils.isNotBlank(contentUrl));
		Assert.assertTrue(StringUtils.isNotBlank(versionKey));
		Assert.assertTrue(StringUtils.equalsIgnoreCase(id, nodeId));
		Assert.assertTrue(StringUtils.endsWithIgnoreCase(contentUrl, ".zip"));

		// Content Review
		setURI();
		Response r3 = given().spec(getRequestSpec(contentType, validuserId)).body("{\"request\":{\"content\":{}}}")
				.when().post("/content/v3/review/" + nodeId).then().log().all().spec(get200ResponseSpec()).extract()
				.response();
		JsonPath jP3 = r3.jsonPath();
		id = jP3.get("result.node_id");
		versionKey = jP3.get("result.versionKey");
		Assert.assertTrue(StringUtils.isNotBlank(id));
		Assert.assertTrue(StringUtils.isNotBlank(versionKey));
		Assert.assertTrue(StringUtils.equalsIgnoreCase(id, nodeId));

		// Content Get and Check for Review Status
		setURI();
		Response r4 = given().spec(getRequestSpec(contentType, validuserId)).when()
				.get("/content/v3/read/" + nodeId + "?fields=body").then().spec(get200ResponseSpec()).extract()
				.response();

		JsonPath jP4 = r4.jsonPath();
		String artifactUrl = jP4.get("result.content.artifactUrl");
		String status = jP4.get("result.content.status");
		Assert.assertTrue(StringUtils.endsWithIgnoreCase(artifactUrl, ".zip"));
		Assert.assertTrue(StringUtils.equalsIgnoreCase(status, "Review"));

		// Content Publish
		setURI();
		Response r5 = given().spec(getRequestSpec(contentType, validuserId)).when()
				.get("/learning/v2/content/publish/" + nodeId).then().spec(get200ResponseSpec()).extract().response();
		JsonPath jP5 = r5.jsonPath();
		id = jP5.get("result.node_id");
		versionKey = jP5.get("result.versionKey");
		String publishStatus = jP5.get("result.publishStatus");
		Assert.assertTrue(StringUtils.isNotBlank(id));
		Assert.assertTrue(StringUtils.isNotBlank(versionKey));
		Assert.assertTrue(StringUtils.equalsIgnoreCase(id, nodeId));
		Assert.assertTrue(StringUtils.equalsIgnoreCase(publishStatus, publishMessage));
	}

	@Test
	public void createUploadInvalidH5PContentExpectSuccess400() {
		// Pre-Created Content Cleanup
		contentCleanUp();

		// Content Create
		setURI();
		Response r1 = given().spec(getRequestSpec(contentType, validuserId)).body(jsonCreateValidContent).with()
				.contentType(JSON).when().post("/content/v3/create").then().spec(get200ResponseSpec()).extract()
				.response();

		// Extracting the JSON path
		JsonPath jp = r1.jsonPath();
		String nodeId = jp.get("result.node_id");
		String versionKey = jp.get("result.versionKey");
		Assert.assertTrue(StringUtils.isNotBlank(nodeId));
		Assert.assertTrue(StringUtils.isNotBlank(versionKey));

		// Content Upload
		setURI();
		given().spec(getRequestSpec(uploadContentType, validuserId))
				.multiPart(new File(path + "/invalid_h5p_content.h5p")).when().post("/content/v3/upload/" + nodeId)
				.then().spec(get400ResponseSpec());
	}

}
