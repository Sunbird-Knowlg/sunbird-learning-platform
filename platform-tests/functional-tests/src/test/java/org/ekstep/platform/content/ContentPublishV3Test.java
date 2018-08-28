package org.ekstep.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.ekstep.platform.domain.BaseTest;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

import net.lingala.zip4j.core.ZipFile;

/**
 * @author gauraw
 *
 */
public class ContentPublishV3Test extends BaseTest{

	private static ClassLoader classLoader = ContentPublishV3Test.class.getClassLoader();
	private static File path = new File(classLoader.getResource("UploadFiles/").getFile());
	private static ObjectMapper mapper=new ObjectMapper();
	
	public void delay(long time){
		try {
			Thread.sleep(time);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	//Publish Content with Checklist & Comment
	@Test
	public void publishContentWithCheckListExpect200(){
		//Create Content
		int rn = generateRandomInt(0, 999999);
		String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn+ "\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/pdf\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
		setURI();
		Response response = 
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(createValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().//log().all().
				extract().
				response();
		
		JsonPath jsonResponse = response.jsonPath();
		String identifier = jsonResponse.get("result.node_id");
		
		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/pdf.pdf")).
		when().
		post("/content/v3/upload/" + identifier)
		.then().
		//log().all().
		spec(get200ResponseSpec());
				
		//publish content
		String publishContentReq = "{\"request\": {\"content\": {\"publisher\": \"EkStep\",\"lastPublishedBy\": \"Ekstep\",\"publishChecklist\":[\"GoodQuality\",\"CorrectConcept\"],\"publishComment\":\"OK\"}}}";
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
		body(publishContentReq).
		with().
		contentType(JSON).
		when().
		post("content/v3/publish/"+identifier).
		then().//log().all().
		spec(get200ResponseSpec());

		delay(15000);
		
		// Get Content and Validate
		setURI();
		response = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + identifier).
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().response();
		
		// Validate the response
		jsonResponse = response.jsonPath();
		String status = jsonResponse.get("result.content.status");
		List<String> publishChecklist = jsonResponse.get("result.content.publishChecklist");
		String publishComment = jsonResponse.get("result.content.publishComment");
		assertTrue("Live".equals(status));
		assertTrue(publishChecklist.contains("GoodQuality") && publishChecklist.contains("CorrectConcept"));
		assertTrue("OK".equals(publishComment));
	}
	
	//Publish Content with empty check list
	@Test
	public void publishContentWithEmptyCheckListExpect400(){
		//Create Content
		int rn = generateRandomInt(0, 999999);
		String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn+ "\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/pdf\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
		setURI();
		Response response = 
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(createValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().//log().all().
				extract().
				response();
		
		JsonPath jsonResponse = response.jsonPath();
		String identifier = jsonResponse.get("result.node_id");
		
		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/pdf.pdf")).
		when().
		post("/content/v3/upload/" + identifier)
		.then();
		//log().all().
				
		//publish content
		String publishContentReq = "{\"request\": {\"content\": {\"publisher\": \"EkStep\",\"lastPublishedBy\": \"Ekstep\",\"publishChecklist\":[],\"publishComment\":\"OK\"}}}";
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
		body(publishContentReq).
		with().
		contentType(JSON).
		when().
		post("content/v3/publish/"+identifier).
		then().//log().all().
		spec(get400ResponseSpec());

	}
	
	//Publish Content with empty check list
	@Test
	public void publishContentWithEmptyCheckListExpectClientError(){
		//Create Content
		int rn = generateRandomInt(0, 999999);
		String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn+ "\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/pdf\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
		setURI();
		Response response = 
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(createValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().//log().all().
				extract().
				response();
		
		JsonPath jsonResponse = response.jsonPath();
		String identifier = jsonResponse.get("result.node_id");
		
		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/pdf.pdf")).
		when().
		post("/content/v3/upload/" + identifier)
		.then();
		//log().all().
				
		//publish content
		String publishContentReq = "{\"request\": {\"content\": {\"publisher\": \"EkStep\",\"lastPublishedBy\": \"Ekstep\",\"publishChecklist\":\"\",\"publishComment\":\"OK\"}}}";
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
		body(publishContentReq).
		with().
		contentType(JSON).
		when().
		post("content/v3/publish/"+identifier).
		then().//log().all().
		spec(get400ResponseSpec());

	}
	
	//Publish Content without check list
	@Test
	public void publishContentWithoutCheckListExpect400(){
		//Create Content
		int rn = generateRandomInt(0, 999999);
		String createValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn+ "\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/pdf\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
		setURI();
		Response response = 
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(createValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().//log().all().
				extract().
				response();
		
		JsonPath jsonResponse = response.jsonPath();
		String identifier = jsonResponse.get("result.node_id");
		
		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/pdf.pdf")).
		when().
		post("/content/v3/upload/" + identifier)
		.then();
		//log().all().
				
		//publish content
		String publishContentReq = "{\"request\": {\"content\": {\"publisher\": \"EkStep\",\"lastPublishedBy\": \"Ekstep\",\"publishComment\":\"OK\"}}}";
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
		body(publishContentReq).
		with().
		contentType(JSON).
		when().
		post("content/v3/publish/"+identifier).
		then().//log().all().
		spec(get400ResponseSpec());

	}
	
	// ECAR and Spine ECAR Should Not Have "posterImage" metadata.
	@Test
	public void testEcarAndSpineEcarForResourceContent() {
		//Create Asset Content
		int rn = generateRandomInt(0, 999999);
		String createAssetContentReq="{\"request\":{\"content\":{\"name\":\"Test Asset\",\"code\":\"test.asset.1\",\"mimeType\":\"image/jpeg\",\"contentType\":\"Asset\",\"mediaType\":\"image\"}}}";
		setURI();
		Response response = 
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(createAssetContentReq).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				extract().
				response();
		JsonPath jsonResponse = response.jsonPath();
		String assetId = jsonResponse.get("result.node_id");
		
		//Upload Asset
		setURI();
		response = 
				given().
				spec(getRequestSpecification(uploadContentType, userId, APIToken)).
				multiPart(new File(path + "/edu-success.jpeg")).
				when().
				post("/content/v3/upload/" + assetId)
				.then().
				extract().
				response();
		
		jsonResponse = response.jsonPath();
		String assetUrl = jsonResponse.get("result.content_url");
		
		delay(15000);
		
		//Create Content
		String createResourceContentReq="{\"request\":{\"content\":{\"name\":\"LP_FT_"+rn+"\",\"code\":\"test.res.1\",\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"appIcon\":\""+assetUrl+"\"}}}";
		setURI();
		response = 
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(createResourceContentReq).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				extract().
				response();
		
		jsonResponse = response.jsonPath();
		String identifier = jsonResponse.get("result.node_id");
				
		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/pdf.pdf")).
		when().
		post("/content/v3/upload/" + identifier)
		.then().
		spec(get200ResponseSpec());
						
		//publish content
		String publishContentReq = "{\"request\": {\"content\": {\"publisher\": \"EkStep\",\"lastPublishedBy\": \"Ekstep\",\"publishChecklist\":[\"GoodQuality\",\"CorrectConcept\"],\"publishComment\":\"OK\"}}}";
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
		body(publishContentReq).
		with().
		contentType(JSON).
		when().
		post("content/v3/publish/"+identifier).
		then().
		spec(get200ResponseSpec());

		delay(15000);
				
		// Get Content and Validate
		setURI();
		response = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + identifier).
				then().
				spec(get200ResponseSpec()).
				extract().response();
		
		// Validate the response
		jsonResponse = response.jsonPath();
		String status = jsonResponse.get("result.content.status");
		assertTrue("Live".equals(status));
		assertTrue(validateEcar(jsonResponse.get("result.content.downloadUrl")));
		assertTrue(validateEcar(jsonResponse.get("result.content.variants.spine.ecarUrl")));
	
	}


	@SuppressWarnings("unchecked")
	private boolean validateEcar(String url) {
		boolean isValidEcar = false;
		String ecarName = "test_bundle";
		String downloadPath = "/data/testBundle/";
		String ecarPath = downloadPath + ecarName + ".zip";
		try {
			// Download Ecar
			FileUtils.copyURLToFile(new URL(url), new File(downloadPath + ecarName + ".zip"));

			// Extract Ecar
			File bundleExtract = new File(downloadPath + ecarName);
			String bundleExtractPath = bundleExtract.getPath();

			ZipFile bundleZip = new ZipFile(ecarPath);
			bundleZip.extractAll(bundleExtractPath);

			File fileName = new File(bundleExtractPath);
			File[] files = fileName.listFiles();

			for (File file : files) {
				if (file.isFile() && file.getName().endsWith("json")) {
					Map<String, Object> manifestMap = mapper.readValue(file, new TypeReference<Map<String, Object>>() {
					});
					Map<String, Object> archive = (Map<String, Object>) manifestMap.get("archive");
					List<Map<String, Object>> items = (List<Map<String, Object>>) archive.get("items");
					Map<String, Object> props = items.get(0);
					String appIcon = (String) props.get("appIcon");
					String posterImage = (String) props.get("posterImage");

					if (null != appIcon && null == posterImage) {
						if (new File(bundleExtractPath + "/" + appIcon).exists())
							isValidEcar = true;
					}
				}
			}
			FileUtils.deleteDirectory(new File(downloadPath));
		} catch (Exception e) {
			e.printStackTrace();
			try {
				FileUtils.deleteDirectory(new File(downloadPath));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return isValidEcar;
	}
	
}
