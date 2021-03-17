package org.sunbird.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import org.sunbird.platform.domain.BaseTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
@Ignore
public class ContentUploadTest extends BaseTest{

	private String PROCESSING = "Processing";
	private String PENDING = "Pending";
	int rn = generateRandomInt(0, 999999);
	String jsonCreateValidContent = "{\"request\":{\"content\":{\"osId\":\"org.sunbird.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Test\",\"name\":\"Test_3\",\"language\":[\"English\"],\"contentType\":\"Plugin\",\"code\":\"LP_NFT_PLUGIN_TEST_"+rn+"\",\"tags\":[\"Content\"],\"mimeType\":\"application/vnd.ekstep.plugin-archive\"}}}";
	String jsonContentClean = "{\"request\": {\"searchProperty\": \"identifier\",\"searchOperator\": \"startsWith\",\"searchString\": \"org.ektep.test.plugin.ft\"}}";
	String uploadedFile = "https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/org.ektep.test.plugin.ft/artifact/archive_1508928130106.zip";
	
	static ClassLoader classLoader = ContentUploadTest.class.getClassLoader();
	static File path = new File(classLoader.getResource("UploadFiles/").getFile());
	static URL url = classLoader.getResource("DownloadedFiles");
	static File downloadPath;

	@BeforeClass
	public static void setup() throws URISyntaxException {
		downloadPath = new File(url.toURI().getPath());
	}

	@AfterClass
	public static void end() throws IOException {
		//FileUtils.cleanDirectory(downloadPath);
	}

	public String createContent(){
		setURI();
		Response R = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				log().all().
				spec(get200ResponseSpec()).extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");
		return nodeId;
	}
	
	// Create Content
	@Test
	public void createContentAndUploadTestExpect200() throws InterruptedException {

		String nodeId = createContent();
		// Get content and validate
		setURI();
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when()
				.get("/content/v3/read/" + nodeId).
				then().
				log().all().
//				spec(get200ResponseSpec()).
				extract().response();
		
		// Upload Conent
		setURI();
		Response R2 =
			given().
			spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
			multiPart(new File(path+"/Archive.zip")).
			then().
			post("/content/v3/upload/"+nodeId).
			then().
			log().all().
//			spec(get200ResponseSpec()).
			extract().response();
		JsonPath jP2 = R2.jsonPath();
		String content_url = jP2.get("result.content_url");
		
		// Publish content
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken)).
		body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
		when().
		post("/content/v3/publish/"+ nodeId).
		then().
		log().all().
//		spec(get200ResponseSpec()).
		extract().response();

		// Content read
		setURI();
		Thread.sleep(5000);
		Response R3 = given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId).
				then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp2 = R3.jsonPath();
		String statusValue = jp2.get("result.content.status");
		String c_identifier = jp2.get("result.content.identifier");
		// String downloadUrl = jp2.get("result.content.downloadUrl");
		asyncPublishValidations(null, statusValue, nodeId, c_identifier, null, null);
		
		setURI();
		Thread.sleep(5000);
		Response R4 =
			given().
			spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
			multiPart("fileUrl", new File(content_url), "text/plain").
			then().
			post("/content/v3/upload/"+nodeId).
			then().
			log().all().
//			spec(get200ResponseSpec()).
			extract().response();
		JsonPath jP3 = R4.jsonPath();
		String content_url_2 = jP3.get("result.content_url");
		Assert.assertEquals(false, content_url_2.contains(".img"));
	}

	public void asyncPublishValidations(ArrayList<String> identifier1, String status, String nodeId,
			String c_identifier, String node1, String node2) {
		if (status.equals(PROCESSING) || status.equals(PENDING)) {
			for (int i = 1000; i <= 30000; i = i + 1000) {
				try {
					Thread.sleep(i);
				} catch (InterruptedException e) {
					//System.out.println(e);
				}
				setURI();
				Response R3 = given().spec(getRequestSpec(contentType, userId)).when()
						.get("/learning/v2/content/" + nodeId).then().
						//log().all().
						spec(get200ResponseSpec()).extract().response();

				// Validate the response
				JsonPath jp3 = R3.jsonPath();
				String statusUpdated = jp3.get("result.content.status");
				if (statusUpdated.equals(PROCESSING) || statusUpdated.equals(PENDING)) {
					//System.out.println(statusUpdated);
					i++;
				}
				if (statusUpdated.equals("Live")) {
					Assert.assertTrue(
							c_identifier.equals(nodeId) && identifier1.contains(node1) && identifier1.contains(node2));
					break;
				}
			}
		}
	}
}
