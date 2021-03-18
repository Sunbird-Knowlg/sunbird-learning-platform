package org.sunbird.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.sunbird.platform.domain.BaseTest;
import org.junit.Ignore;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class ChannelWorkflowTests extends BaseTest {

	String jsonCreateValidChannel = "{\"request\":{\"channel\":{\"name\":\"content\",\"description\":\"\",\"code\":\"org.sunbird.test\",\"contentFilter\":{\"filters\":{\"gradeLevel\":[\"Grade 3\"]}}}}}";
	String jsonContentClean = "{\"request\": {\"searchProperty\": \"name\",\"searchOperator\": \"startsWith\",\"searchString\": \"LP_FT_\"}}";
	String jsonCreateInValidChannel = "{\"request\":{\"Channel\":{\"name\":\"content\",\"description\":\"\",\"code\":\"org.sunbird.test\",\"contentFilter\":{\"filters\":{\"gradeLevel\":[\"Grade 3\"]}}}}}";	
	String jsonUpdateValidChannel = "{\"request\":{\"channel\":{\"description\":\"LP Channel API\"}}}";
	String jsonListValidChannnel = "{\"request\":{}}";
	
	static ClassLoader classLoader = ChannelWorkflowTests.class.getClassLoader();
	static File path = new File(classLoader.getResource("UploadFiles/").getFile());
	static URL url = classLoader.getResource("DownloadedFiles");
	static File downloadPath;
	
//	 @BeforeClass
	 public static void setup() throws URISyntaxException{
	    downloadPath = new File(url.toURI().getPath());
	 }

//	 @AfterClass
	 public static void end() throws IOException{
		FileUtils.cleanDirectory(downloadPath);		
	 }

	// Content clean up	
	public void contentCleanUp(){
		setURI();
		given().
		body(jsonContentClean).
		with().
		contentType(JSON).
		when().
		post("learning/v1/exec/content_qe_deleteContentBySearchStringInField");
	}

	 
	// Create Content
	@Test
	public void createValidChannelExpect200() {
		contentCleanUp();
		setURI();
		Response R = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateValidChannel).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/channel/create").
				then().
		//		log().all().
				spec(get200ResponseSpec())
				.extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String node = jp.get("result.node_id");
	}
	
	@Test
	public void createChannelWithInvalidRequest(){
		contentCleanUp();
		setURI();
		Response R = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateInValidChannel).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/channel/create").
				then().
		//		log().all().
				spec(get400ResponseSpec())
				.extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String node = jp.get("result.node_id");
	}
	
	@Test
	public void getChannelWithValidChannel(){
		contentCleanUp();
		setURI();
		Response R = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateValidChannel).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/channel/create").
				then().
		//		log().all().
				spec(get200ResponseSpec())
				.extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String node = jp.get("result.node_id");

				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/channel/read/" + node).
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().
				response();
	}
	@Ignore
	@Test
	public void getChannelWithInValidChannel(){
		contentCleanUp();
		setURI(); 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/channel/read/do_90786755").
				then().
				// log().all().
				spec(get404ResponseSpec()).
				extract().
				response();
	}
	@Ignore
	@Test
	public void updateChannelWithValid(){
		contentCleanUp();
		setURI();
		Response R = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateValidChannel).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/channel/create").
				then().
		//		log().all().
				spec(get200ResponseSpec())
				.extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String node = jp.get("result.node_id");
		Response R1 = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonUpdateValidChannel).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/channel/update/"+node).
				then().
		//		log().all().
				spec(get200ResponseSpec())
				.extract().
				response();

		// Extracting the JSON path
		JsonPath jp1 = R1.jsonPath();
		String node1 = jp1.get("result.node_id");
		String description = jp.getString("result.node.metadata.description");
		System.out.println(description);
	}

	@Ignore
	@Test
	public void updateChannelWithInvalidChannelId(){
		contentCleanUp();
		setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonUpdateValidChannel).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/channel/update/do_7897").
				then().
		//		log().all().
				spec(get404ResponseSpec())
				.extract().
				response();
	}
	
	@Test
	public void updateChannelWithInvalidUrl(){
		contentCleanUp();
		setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonUpdateValidChannel).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/cannel/update/do_7897").
				then().
		//		log().all().
				spec(get500ResponseSpec())
				.extract().
				response();
	}
	@Ignore
	@Test
	public void listChannelWithValidRequest(){
		contentCleanUp();
		setURI();
		Response R1 = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonUpdateValidChannel).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/channel/list").
				then().
				log().all().
				spec(get200ResponseSpec())
				.extract().
				response();
			
		// Extracting the JSON path
				JsonPath jp1 = R1.jsonPath();
				String node1 = jp1.get("result.node_id");
				String description = jp1.getString("result.node.metadata.description");
				System.out.println(description);
	}
	@Ignore
	@Test
	public void listChannelWithInvalidRequest(){
		contentCleanUp();
		setURI();
		Response R1 = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonListValidChannnel).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/channel/list").
				then().
		//		log().all().
				spec(get200ResponseSpec())
				.extract().
				response();
		
		// Extracting the JSON path
				JsonPath jp1 = R1.jsonPath();
	}
}
