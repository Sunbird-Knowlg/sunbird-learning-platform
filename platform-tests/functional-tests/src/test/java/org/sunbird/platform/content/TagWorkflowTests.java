package org.sunbird.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.io.File;
import java.net.URL;

import org.sunbird.platform.domain.BaseTest;
import org.junit.Ignore;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
@Ignore
public class TagWorkflowTests extends BaseTest{

	int rn = generateRandomInt(0, 9999999);
	
	String jsonCreateValidContent = "{\"request\":{\"content\":{\"mediaType\":\"content\",\"identifier\":\"LP_FT_"+rn+"\",\"visibility\":\"Default\",\"name\":\"test\",\"language\":[\"English\"],\"tags\":[\"akshara\"],\"contentType\":\"Resource\",\"code\":\"test\",\"osId\":\"org.sunbird.quiz.app\",\"pkgVersion\":1,\"mimeType\":\"video/youtube\",\"artifactUrl\":\"https://www.youtube.com/watch?v=s10ARdfQUOY\"}}}";
	String jsonContentClean = "{\"request\": {\"searchProperty\": \"name\",\"searchOperator\": \"startsWith\",\"searchString\": \"LP_FT_\"}}";
	
	static ClassLoader classLoader = MimeTypeMgrTests.class.getClassLoader();
	static File path = new File(classLoader.getResource("UploadFiles/").getFile());
	static URL url = classLoader.getResource("DownloadedFiles");
	static File downloadPath;
	
//	 @BeforeClass
//	 public static void setup() throws URISyntaxException{
//	    downloadPath = new File(url.toURI().getPath());
//	 }
//
//	 @AfterClass
//	 public static void end() throws IOException{
//		FileUtils.cleanDirectory(downloadPath);		
//	 }
	
	@Test
	public void createValidYoutubeContentExpectSuccess200() {
//		contentCleanUp();
		setURI();
		Response R = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("/learning-service/v2/content").
				then().
		//		log().all().
				spec(get200ResponseSpec())
				.extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String node_id = jp.get("result.node_id");
		
		// Get content and validate
				setURI();
				Response R4 =
						given().
						spec(getRequestSpec(contentType, validuserId)).
						when().
						get("/learning/v2/content/" + node_id).
						then().
						// log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				// Validate the response
				JsonPath jp4 = R4.jsonPath();
				String status = jp4.get("result.content.status");
				System.out.println(jp4.getString("result.content.tags"));
	}
	
//	// Content clean up	
//		public void contentCleanUp(){
//			setURI();
//			given().
//			body(jsonContentClean).
//			with().
//			contentType(JSON).
//			when().
//			post("learning/v1/exec/content_qe_deleteContentBySearchStringInField");
//		}

}
