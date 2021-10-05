package org.sunbird.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.sunbird.platform.domain.BaseTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class CreateContentWithNewCategoryTest extends BaseTest {

	String jsonCreateContentWithCategory = "{ \"request\": { \"content\": { \"mediaType\": \"content\",\"mimeType\":\"application/pdf\", \"visibility\": \"Default\", \"name\": \"testContent\", \"category\":[ \"library\" ], \"language\": [ \"Hindi\" ], \"contentType\": \"Resource\", \"code\": \"org.sunbird.plugin\", \"osId\": \"org.sunbird.quiz.app\", \"pkgVersion\": 1 } } } ";
	String jsonCreateContentWithNewGrade = "{ \"request\": { \"content\": { \"mediaType\": \"content\",\"mimeType\":\"application/pdf\", \"visibility\": \"Default\", \"name\": \"testContent\", \"gradeLevel\":[ \"Class 2\" ], \"language\": [ \"Hindi\" ], \"contentType\": \"Resource\", \"code\": \"org.sunbird.plugin\", \"osId\": \"org.sunbird.quiz.app\", \"pkgVersion\": 1 } } } ";
	String nodeId = null;
	String jsonContentClean = "{\"request\": {\"searchProperty\": \"name\",\"searchOperator\": \"startsWith\",\"searchString\": \"LP_FT\"}}";

	static ClassLoader classLoader = CreateContentWithNewCategoryTest.class.getClassLoader();
	static URL url = classLoader.getResource("DownloadedFiles");
	static File downloadPath;
	char ch = '"';

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
		given().
		body(jsonContentClean).
		with().
		contentType(JSON).
		when().
		post("learning/v1/exec/content_qe_deleteContentBySearchStringInField");
	}

	/**
	 * Test method to check content creation with new category "library",
	 * 
	 */
	@Test
	public void createContentWithNewCategory() {
		if (nodeId == null) {
			setURI();
			Response R = given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateContentWithCategory).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			nodeId = jp.get("result.node_id");
			System.out.println("nodeId=" + nodeId);
		}
	}
	
	/**
	 * Test method to check content creation with new category "library",
	 * 
	 */
	@Test
	public void createContentWith12Grade() {
		if (nodeId == null) {
			setURI();
			Response R = given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateContentWithNewGrade).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			nodeId = jp.get("result.node_id");
			System.out.println("nodeId=" + nodeId);
		}
	}
}
