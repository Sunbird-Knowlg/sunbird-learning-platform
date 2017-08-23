package org.ekstep.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.io.File;
import java.net.URL;

import org.ekstep.platform.domain.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class ArtifactMimeTypeTest extends BaseTest {

	private static String VALID_ECML_MIMETYPE = "application/vnd.ekstep.ecml-archive";
	private static String VALID_ECML_ARTIFACT_MIMETYPE = "application/vnd.ekstep.ecml-archive+zip";
	private static String VALID_HTML_MIMETYPE = "application/vnd.ekstep.html-archive";
	private static String VALID_HTML_ARTIFACT_MIMETYPE = "application/vnd.ekstep.html-archive+zip";
	private static String nodeId = "FT_EXPLICIT_CONTENT_IDENTIFIER";

	String jsonCreateContentWithValidMimeType = "{ \"request\": { \"content\": { \"mediaType\": \"content\", \"identifier\": " + nodeId + ", \"visibility\": \"Default\", \"name\": \"testContent\", \"category\":[ \"library\" ], \"language\": [ \"Hindi\" ], \"contentType\": \"Story\", \"code\": \"org.ekstep.plugin\", \"osId\": \"org.ekstep.quiz.app\", \"pkgVersion\": 1, \"mimeType\": "
			+ VALID_ECML_MIMETYPE + " } } } ";
	
	String jsonCreateContentWithoutMimeType = "{ \"request\": { \"content\": { \"mediaType\": \"content\", \"identifier\": " + nodeId + ", \"visibility\": \"Default\", \"name\": \"testContent\", \"category\":[ \"library\" ], \"language\": [ \"Hindi\" ], \"contentType\": \"Story\", \"code\": \"org.ekstep.plugin\", \"osId\": \"org.ekstep.quiz.app\", \"pkgVersion\": 1 } } } ";
	
	String jsonCreateContentWithValidMimeTypeAndValidArtifactMimeType = "{ \"request\": { \"content\": { \"mediaType\": \"content\", \"identifier\": " + nodeId + ", \"visibility\": \"Default\", \"name\": \"testContent\", \"category\":[ \"library\" ], \"language\": [ \"Hindi\" ], \"contentType\": \"Story\", \"code\": \"org.ekstep.plugin\", \"osId\": \"org.ekstep.quiz.app\", \"pkgVersion\": 1, \"mimeType\": "
			+ VALID_ECML_MIMETYPE + ", \"artifactMimeType\": " + VALID_ECML_ARTIFACT_MIMETYPE + " } } } ";
	
	String jsonCreateContentWithValidMimeTypeAndInValidArtifactMimeType = "{ \"request\": { \"content\": { \"mediaType\": \"content\", \"identifier\": " + nodeId + ", \"visibility\": \"Default\", \"name\": \"testContent\", \"category\":[ \"library\" ], \"language\": [ \"Hindi\" ], \"contentType\": \"Story\", \"code\": \"org.ekstep.plugin\", \"osId\": \"org.ekstep.quiz.app\", \"pkgVersion\": 1, \"mimeType\": "
			+ VALID_ECML_MIMETYPE + ", \"artifactMimeType\": " + VALID_HTML_ARTIFACT_MIMETYPE + " } } } ";
	
	String jsonUpdateValidMimeTypeInContent = "{\"request\": {\"content\": {\"versionKey\": \"version_Key\", \"mimeType\": " + VALID_ECML_MIMETYPE + "}}}";
	
	String jsonUpdateValidMimeTypeAndValidArtifactMimeTypeInContent = "{\"request\": {\"content\": {\"versionKey\": \"version_Key\", \"mimeType\": " + VALID_ECML_MIMETYPE + ", \"artifactMimeType\": " + VALID_ECML_ARTIFACT_MIMETYPE + "}}}";
	
	String jsonUpdateValidMimeTypeAndInValidArtifactMimeTypeInContent = "{\"request\": {\"content\": {\"versionKey\": \"version_Key\", \"mimeType\": " + VALID_ECML_MIMETYPE + ", \"artifactMimeType\": " + VALID_HTML_ARTIFACT_MIMETYPE + "}}}";
	
	String jsonUpdateWithoutMimeTypeAndInContent = "{\"request\": {\"content\": {\"versionKey\": \"version_Key\", \"appIcon\": \"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/games/1452249885847_literacy_512.png\"}}}";

	String jsonContentCleanUp = "{\"request\": {\"searchProperty\": \"name\",\"searchOperator\": \"startsWith\",\"searchString\": \"LP_FT\"}}";

	String jsonCleanSingleContent = "{\"request\": {\"searchProperty\": \"identifier\",\"searchOperator\": \"startsWith\",\"searchString\": " + nodeId + "}}";

	static ClassLoader classLoader = CreateContentWithNewCategoryTest.class.getClassLoader();
	static URL url = classLoader.getResource("DownloadedFiles");
	static File downloadPath;
	char ch = '"';

	// Content clean up
	public void contentCleanUp() {
		setURI();
		given().body(jsonContentCleanUp).with().contentType(JSON).when()
				.post("learning/v1/exec/content_qe_deleteContentBySearchStringInField");
	}

	// Content clean up
	public void cleanSingleContent() {
		setURI();
		given().body(jsonCleanSingleContent).with().contentType(JSON).when()
				.post("learning/v1/exec/content_qe_deleteContentBySearchStringInField");
	}

	@Test
	public void createValidContentWithValidMimeTypeExpect200() {
		contentCleanUp();
		setURI();
		Response R = given().spec(getRequestSpecWithHeaderExceptChannelId(contentType, validuserId))
				.body(jsonCreateContentWithValidMimeType).with().contentType(JSON).when()
				.post("/learning/v2/content/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String node = jp.get("result.node_id");

		// Get content and validate
		setURI();
		Response R1 = given().spec(getRequestSpec(contentType, validuserId)).when().get("/content/v3/read/" + node)
				.then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String artifactMimeType = jP1.get("result.content.artifactMimeType");
		String mimeType = jP1.get("result.content.mimeType");
		Assert.assertEquals(VALID_ECML_ARTIFACT_MIMETYPE, artifactMimeType);
		Assert.assertEquals(VALID_ECML_MIMETYPE, mimeType);
		cleanSingleContent();
	}
	
	@Test
	public void createValidContentWithoutMimeTypeExpect200() {
		contentCleanUp();
		setURI();
		Response R = given().spec(getRequestSpecWithHeaderExceptChannelId(contentType, validuserId))
				.body(jsonCreateContentWithoutMimeType).with().contentType(JSON).when()
				.post("/learning/v2/content/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String node = jp.get("result.node_id");

		// Get content and validate
		setURI();
		Response R1 = given().spec(getRequestSpec(contentType, validuserId)).when().get("/content/v3/read/" + node)
				.then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String artifactMimeType = jP1.get("result.content.artifactMimeType");
		String mimeType = jP1.get("result.content.mimeType");
		Assert.assertEquals(null, artifactMimeType);
		Assert.assertEquals(VALID_ECML_MIMETYPE, mimeType);
		cleanSingleContent();
	}
	
	@Test
	public void createValidContentWithValidMimeTypeAndValidArtifactMimeTypeExpect200() {
		contentCleanUp();
		setURI();
		Response R = given().spec(getRequestSpecWithHeaderExceptChannelId(contentType, validuserId))
				.body(jsonCreateContentWithValidMimeTypeAndValidArtifactMimeType).with().contentType(JSON).when()
				.post("/learning/v2/content/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String node = jp.get("result.node_id");

		// Get content and validate
		setURI();
		Response R1 = given().spec(getRequestSpec(contentType, validuserId)).when().get("/content/v3/read/" + node)
				.then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String artifactMimeType = jP1.get("result.content.artifactMimeType");
		String mimeType = jP1.get("result.content.mimeType");
		Assert.assertEquals(VALID_ECML_ARTIFACT_MIMETYPE, artifactMimeType);
		Assert.assertEquals(VALID_ECML_MIMETYPE, mimeType);
		cleanSingleContent();
	}
	
	@Test
	public void createValidContentWithValidMimeTypeAndInValidArtifactMimeTypeExpect200() {
		contentCleanUp();
		setURI();
		Response R = given().spec(getRequestSpecWithHeaderExceptChannelId(contentType, validuserId))
				.body(jsonCreateContentWithValidMimeTypeAndInValidArtifactMimeType).with().contentType(JSON).when()
				.post("/learning/v2/content/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String node = jp.get("result.node_id");

		// Get content and validate
		setURI();
		Response R1 = given().spec(getRequestSpec(contentType, validuserId)).when().get("/content/v3/read/" + node)
				.then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String artifactMimeType = jP1.get("result.content.artifactMimeType");
		String mimeType = jP1.get("result.content.mimeType");
		Assert.assertEquals(VALID_ECML_ARTIFACT_MIMETYPE, artifactMimeType);
		Assert.assertEquals(VALID_ECML_MIMETYPE, mimeType);
		cleanSingleContent();
	}

}
