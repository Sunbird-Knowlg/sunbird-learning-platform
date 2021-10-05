package org.sunbird.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import org.sunbird.platform.domain.BaseTest;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
//@Ignore
public class CopyContentV3APITests extends BaseTest{


	int rn = generateRandomInt(0, 9999999);
	String jsonCreateValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_Content_" + rn+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_NFT_Content_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
	String jsonUpdateHierarchyTwoChild = "{\"request\":{\"data\":{\"nodesModified\":{\"unitId1\":{\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\", \"name\":\"LP_FT_CourseUnit1_+rn+\",\"contentType\":\"TextBookUnit\",\"code\":\"Test_QA\"}},\"unitId2\":{\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"name\":\"LP_FT_CourseUnit2_+rn+\",\"contentType\":\"TextBookUnit\",\"code\":\"Test_QA\"}}},"
			+ "\"hierarchy\":{\"TextbookId\":{\"name\":\"LP_NFT_Collection_"+rn+"\",\"contentType\":\"TextBook\",\"children\":[\"unitId1\",\"unitId2\"],\"root\":true},\"unitId1\":{\"name\":\"LP_FT_CourseUnit1_"+rn+"\",\"contentType\":\"TextBookUnit\",\"children\":[\"contentId1\"],\"root\":false},\"unitId2\":{\"name\":\"LP_FT_CourseUnit2_"+rn+"\",\"contentType\":\"TextBookUnit\",\"children\":[\"contentId2\"],\"root\":false},\"contentId1\":{\"name\":\"LP_FT_Content1_"+rn+"\",\"root\":false},"
			+ "\"contentId2\":{\"name\":\"LP_FT_Content2_"+rn+"\",\"root\":false}}}}}";
	String jsonUpdateHierarchyOneChild = "{\"request\":{\"data\":{\"nodesModified\":{\"unitId1\":{\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"name\":\"LP_FT_CourseUnit1_"+rn+"\",\"contentType\":\"TextBookUnit\",\"code\":\"Test_QA\"}}},\"hierarchy\":{\"TextbookId\":{\"name\":\"LP_NFT_Collection_"+rn+"\",\"contentType\":\"TextBook\",\"children\":[\"unitId1\"],\"root\":true},\"unitId1\":{\"name\":\"LP_FT_CourseUnit1_"+rn+"\","
			+ "\"contentType\":\"TextBookUnit\",\"children\":[\"contentId1\"],\"root\":false},\"contentId1\":{\"name\":\"LP_FT_Content1_"+rn+"\",\"root\":false}}}}}";
	String jsonCreateValidTextBookUnit = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_Unit" + rn+ "\", \"mediaType\": \"content\",\"visibility\": \"Parent\",\"name\": \"LP_NFT_Unit_"+ rn+ "\",\"contentType\": \"TextBookUnit\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"tags\":[\"LP_functionalTest\"]}}}";
	String jsonCreateValidTextBook = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_TBook" + rn+ "\", \"mediaType\": \"content\",\"name\": \"LP_NFT_TBook_"+ rn+ "\",\"contentType\": \"TextBook\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"tags\":[\"LP_functionalTest\"]}}}";
	String jsonUpdateMetadata = "{\"request\":{\"content\":{\"versionKey\":\"version_key\",\"name\":\"updatedContentName\"}}}";
	String jsonCopyContent = "{\"request\":{\"content\":{\"createdBy\":\"Test\",\"createdFor\":[\"Test\"],\"organisation\":[\"Test\"]}}}";
	
	static ClassLoader classLoader = ContentPublishWorkflowTests.class.getClassLoader();
	static URL url = classLoader.getResource("DownloadedFiles");
	static File downloadPath;
	static File path = new File(classLoader.getResource("UploadFiles/").getFile());

	private static String contentId1 = null;
	private static String contentId2 = null;
	private static String contentId3 = null;
	private static String unitId1 = null;
	private static String unitId2 = null;
	private static String artifactURL1 = null;
	private static String artifactURL2 = null;
	private static String versionKey1 = null;
	private static String versionKey2 = null;
	private static String name1 = null;
	private static String name2 = null;
	private String PROCESSING = "Processing";
	private String PENDING = "Pending";
	
	@BeforeClass
	public static void setup() throws URISyntaxException {
		downloadPath = new File(url.toURI().getPath());
	}

	@AfterClass
	public static void end() throws IOException {
		// FileUtils.cleanDirectory(downloadPath);
	}
	
//	@Before
//	public void init() {
//		if ((StringUtils.isBlank(contentId1)) || (StringUtils.isBlank(contentId2)))
//			createContent();
//		if ((StringUtils.isBlank(unitId1)) || (StringUtils.isBlank(unitId2)))
//			createTextBookUnit();
//		try {Thread.sleep(3000);} catch (InterruptedException e) {e.printStackTrace();}
//	}	
	
	private void createContent(){
		for (int i=1; i<=2; i++){
			if(i==1)
				jsonCreateValidContent = jsonCreateValidContent.replace("LP_NFT_Content", "LP_NFT_Content1_");
			if(i==2)
				jsonCreateValidContent = jsonCreateValidContent.replace("LP_NFT_Content", "LP_NFT_Content2_");
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
					//log().all().
					//spec(get200ResponseSpec()).
					extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			
			// Upload Content
			setURI();
			given().
			spec(getRequestSpecification(uploadContentType, userId, APIToken)).
			multiPart(new File(path + "/ExternalJsonItemDataCdata.zip")).
			when().
			post("/content/v3/upload/" + nodeId).
			then().
			//log().all().
			//spec(get200ResponseSpec()).
			extract().response();

			// Publish the content
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/"+nodeId).
			then().
			//log().all().
			//spec(get200ResponseSpec()).
			extract().response();
			
			// Get Content and validate
			setURI();
			Response Res2 = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId).
					then().
					//log().all().
					//spec(get200ResponseSpec()).
					extract().
					response();
			
			JsonPath jPath1 = Res2.jsonPath();
			String identifier = jPath1.get("result.content.identifier");
			String versionKey = jPath1.get("result.content.versionKey");
			String artifactUrl = jPath1.get("result.content.artifactUrl");
			String status = jPath1.get("result.content.status");
			String name = jPath1.get("result.content.name");
			if(i==1)
				contentId1 = nodeId;
				artifactURL1 = artifactUrl;
				name1 = name;
				versionKey1 = versionKey;
			if(i==2)
				contentId2 = nodeId;
				artifactURL2 = artifactUrl;
				name2 = name;
				versionKey2 = versionKey;
				
			Assert.assertTrue(versionKey != null);
			Assert.assertEquals(nodeId, identifier);

		}
	}
	
	private void createParentVisibilityContent(){
		// Create content with visibility as parent
		String jsonCreateParentVisContent = jsonCreateValidContent.replace("LP_NFT_Content", "LP_NFT_Content3_").replace("Default", "Parent");
		setURI();
		Response R = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateParentVisContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");
		
		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/ExternalJsonItemDataCdata.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		//log().all().
		//spec(get200ResponseSpec()).
		extract().response();

		// Publish the content
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
		when().
		post("/content/v3/publish/"+nodeId).
		then().
		//log().all().
		//spec(get200ResponseSpec()).
		extract().response();
		
		// Get Content and validate
		setURI();
		Response Res2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/"+nodeId).
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().
				response();
		
		JsonPath jPath1 = Res2.jsonPath();
		//String identifier = jPath1.get("result.content.identifier");
		String versionKey = jPath1.get("result.content.versionKey");
		String artifactUrl = jPath1.get("result.content.artifactUrl");			
		contentId3 = nodeId;			
		Assert.assertTrue(versionKey != null);
		//Assert.assertEquals(nodeId, identifier);
	}
	
	private void createTextBookUnit(){
	// Create TextBookUnint
		for (int i=1; i<=2; i++){
			if(i==1)
				jsonCreateValidTextBookUnit = jsonCreateValidTextBookUnit.replace("LP_NFT_Unit", "LP_NFT_Unit1_");
			if(i==2)
				jsonCreateValidTextBookUnit = jsonCreateValidTextBookUnit.replace("LP_NFT_Unit", "LP_NFT_Unit2_");
		setURI();
		Response Res2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidTextBookUnit).
				when().
				post("/content/v3/create").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().response();
		
		JsonPath jPath2 = Res2.jsonPath();
		String unitId = jPath2.get("result.node_id");
		
		if (i==1)
			unitId1 = unitId;
		if (i==2)
			unitId2 = unitId;
		}
	}

	// Copy live content
	@Test
	public void copyLiveContentExpectSuccess200(){
		createContent();
		setURI();
		Response Res1 =
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCopyContent).
				with().
				contentType(JSON).
				when().
				post("/content/v3/copy/" +contentId1).
				then().
				spec(get200ResponseSpec()).
				extract().response();
		
		JsonPath jPath = Res1.jsonPath();
		String copyId = jPath.get("result.node_id."+contentId1+"");
		
		// Get Content and validate
		setURI();
		Response Res2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/"+copyId).
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();
		
		JsonPath jPath1 = Res2.jsonPath();
		String identifier = jPath1.get("result.content.identifier");
	//	String artifactUrl = jPath1.get("result.content.artifactUrl");
		String status = jPath1.get("result.content.status");
		Assert.assertTrue(identifier!=contentId1);
		Assert.assertTrue(status.equals("Draft"));		
	}
	
	// Copy content and validate snapshot
	@Test
	public void copyContentWithSnapshotExpectSuccess200(){
		createContent();
		setURI();
		Response Res1 =
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCopyContent).
				with().
				contentType(JSON).
				when().
				post("/content/v3/copy/" +contentId1).
				then().
				spec(get200ResponseSpec()).
				extract().response();
		
		JsonPath jPath = Res1.jsonPath();
		String copyId = jPath.get("result.node_id."+contentId1+"");
		
		// Get Content and validate
		setURI();
		Response Res2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/"+copyId).
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();
		
		JsonPath jPath1 = Res2.jsonPath();
		String identifier = jPath1.get("result.content.identifier");
	//	String artifactUrl = jPath1.get("result.content.artifactUrl");
		String status = jPath1.get("result.content.status");
		Assert.assertTrue(identifier!=contentId1);
		Assert.assertTrue(status.equals("Draft"));
		
		// Publish copied content
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
		when().
		post("/content/v3/publish/"+copyId).
		then().
		//log().all().
		//spec(get200ResponseSpec()).
		extract().response();
		
		// Get Content and validate
		try {Thread.sleep(5000);} catch (InterruptedException e) {e.printStackTrace();}
		setURI();
		Response Res3 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/"+copyId).
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().
				response();
		
		JsonPath jPath2 = Res3.jsonPath();
		String previewUrl = jPath2.get("result.content.previewUrl");
		String statusUpdated = jPath2.get("result.content.status");
		Assert.assertTrue((previewUrl.endsWith("latest")) && statusUpdated.equals("Live"));		 
	}
	
	// Copy draft content
	@Test
	public void copyDraftContentExpect4xx(){
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
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");
		
		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/ExternalJsonItemDataCdata.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		//log().all().
		//spec(get200ResponseSpec()).
		extract().response();
		
		// Copy draft content
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonCopyContent).
		with().
		contentType(JSON).
		when().
		post("/content/v3/copy/" +nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec());
	}
	
	// Copy content which image node
	@Test
	public void copyContentWithImageNodeExpectSuccess200(){
		createContent();
		// Update content metadata
		jsonUpdateMetadata = jsonUpdateMetadata.replace("version_key", versionKey1);
		//System.out.println(jsonUpdateMetadata);
		try{Thread.sleep(5000);}catch(Exception e){e.printStackTrace();};
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonUpdateMetadata).
		with().
		contentType("application/json").
		when().
		patch("/content/v3/update/" + contentId1).
		then().
		//log().all().
		spec(get200ResponseSpec()).
		extract().response();

		// Check for image node
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		get("/content/v3/read/"+contentId1+".img").
		then().
		//log().all().
		spec(get200ResponseSpec()).
		extract().
		response();
	
		// Copy content
		setURI();
		Response Res =
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCopyContent).
				with().
				contentType(JSON).
				when().
				post("/content/v3/copy/" +contentId1).
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().response();
		
		JsonPath jPath = Res.jsonPath();
		String copyId = jPath.get("result.node_id."+contentId1+"");
		System.out.println(copyId);		
		
		// Get content and validate
		setURI();
		Response Res2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/"+copyId).
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();
		
		JsonPath jPath1 = Res2.jsonPath();
		String identifier = jPath1.get("result.content.identifier");
		String name = jPath1.get("result.content.name");
	//	String artifactUrl = jPath1.get("result.content.artifactUrl");
		String status = jPath1.get("result.content.status");
		Assert.assertTrue(name!="updatedContentName");
		Assert.assertTrue(identifier!=contentId1);
		Assert.assertTrue(status.equals("Draft"));
	}
	
	// Copy flagged content
	@Test
	public void copyFlaggedContentExpect4xx(){
		createContent();
		// Get Version Key
		setURI();
		Response Res2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/"+contentId1).
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().
				response();
		
		JsonPath jPath1 = Res2.jsonPath();
		String versionKey = jPath1.get("result.content.versionKey");
		
		// Flag content
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"flagReasons\":[\"Copyright Violation\"],\"flaggedBy\":\"Vignesh\",\"versionKey\":"+versionKey+"}}").
		with().
		contentType(JSON).
		when().
		post("/content/v3/flag/"+contentId1).
		then().
		//log().all().
		//spec(get200ResponseSpec()).
		extract().response();
		
		// Copy flagged content
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonCopyContent).
		with().
		contentType(JSON).
		when().
		post("/content/v3/copy/" +contentId1).
		then().
		spec(get400ResponseSpec());
		
	}
	
	// Copy asset
	@Test
	public void copyAssetExpect4xx(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("contentType", "Asset").put("mimeType", "image/jpeg");
		String jsonCreateImageAssetValid = js.toString();
		Response R = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateImageAssetValid).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/jpegImage.jpeg")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		//log().all().
		//spec(get200ResponseSpecUpload()).
		extract().response();
		
		// Copy Asset
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonCopyContent).
		with().
		contentType(JSON).
		when().
		post("/content/v3/copy/" +nodeId).
		then().
		spec(get400ResponseSpec());		
	}
	
	// Copy invalid identifier
	@Test
	public void copyInvalidIdentifierExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonCopyContent).
		with().
		contentType(JSON).
		when().
		post("/content/v3/copy/ahdsuanav").
		then().
		spec(get404ResponseSpec());	
	}
	
	// Copy with identifier of other object types
	@Test
	public void copyDiffObjTypesExpect4xx(){
		
	}
	
	// Copy with blank identifier
	@Test
	public void copyBlankIdentifierExpect5xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonCopyContent).
		with().
		contentType(JSON).
		when().
		post("/content/v3/copy/").
		then().
		spec(get500ResponseSpec());
	}
	
	// Copy retired content
	@Test
	public void copyRetiredContentExpect4xx(){
		createContent();
		// Retire content
		setURI();
		try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);} 	
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		delete("/content/v3/retire/" + contentId1).
		then().
		//spec(get200ResponseSpec()).
		extract().response();

		// Get content and validate
		setURI();
		Response R1 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + contentId1).
				then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP1 = R1.jsonPath();
		String status = jP1.get("result.content.status");
		//System.out.println(status);
		Assert.assertEquals(status, "Retired");
		
		// Copy content
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonCopyContent).
		with().
		contentType(JSON).
		when().
		post("/content/v3/copy/" +contentId1).
		then().
		spec(get400ResponseSpec());	
		
	}
	
	// Copy textbook with no children
	@Test
	public void copyTextBookWithNoChildrenExpectSuccess200(){
		createContent();
		createTextBookUnit();
		// Create TextBook 
		setURI();
		Response Res3 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidTextBook).
				when().
				post("/content/v3/create").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().response();
		
		JsonPath jPath3 = Res3.jsonPath();
		String textBookId = jPath3.get("result.node_id");
		
		// Publish textbook
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
		when().
		post("/content/v3/publish/"+textBookId).
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		// Copy textbook
		setURI();
		Response Res1 =
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCopyContent).
				with().
				contentType(JSON).
				when().
				post("/content/v3/copy/" +textBookId).
				then().
				spec(get200ResponseSpec()).
				extract().response();
		
		JsonPath jPath = Res1.jsonPath();
		String copyId = jPath.get("result.node_id."+textBookId+"");
		
		// Get Content and validate
		setURI();
		Response Res2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/"+copyId).
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();
		
		JsonPath jPath1 = Res2.jsonPath();
		String identifier = jPath1.get("result.content.identifier");
	//	String artifactUrl = jPath1.get("result.content.artifactUrl");
		String status = jPath1.get("result.content.status");
		Assert.assertTrue(identifier!=textBookId);
		Assert.assertTrue(status.equals("Draft"));
	}
	
	// Copy textbook with one unit and one children
	@Test
	public void copyTextBookWithUnitExpectSuccess200(){
		createContent();
		createTextBookUnit();
		// Create TextBook 
		setURI();
		Response Res3 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidTextBook).
				when().
				post("/content/v3/create").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().response();
		
		JsonPath jPath3 = Res3.jsonPath();
		String textBookId = jPath3.get("result.node_id");
		
		// Update Hierarchy
		setURI();
		jsonUpdateHierarchyOneChild = jsonUpdateHierarchyOneChild.replaceAll("TextbookId", textBookId).replaceAll("unitId1", unitId1).replaceAll("contentId1", contentId1);
		//System.out.println(jsonUpdateHierarchyOneChild);
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonUpdateHierarchyOneChild).
		with().
		contentType(JSON).
		when().
		patch("/content/v3/hierarchy/update/").
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		// Publish textbook
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
		when().
		post("/content/v3/publish/"+textBookId).
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		// Copy textbook
		try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);} 	
		setURI();
		Response Res1 =
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCopyContent).
				with().
				contentType(JSON).
				when().
				post("/content/v3/copy/" +textBookId).
				then().
				spec(get200ResponseSpec()).
				extract().response();
		
		JsonPath jPath = Res1.jsonPath();
		String copyId = jPath.get("result.node_id."+textBookId+"");
		
		// Get Content and validate
		setURI();
		Response Res2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/"+copyId).
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();
		
		JsonPath jPath1 = Res2.jsonPath();
		String identifier = jPath1.get("result.content.identifier");
	//	String artifactUrl = jPath1.get("result.content.artifactUrl");
		ArrayList<String> childNodes = jPath1.get("result.content.children");
		int nodesCount = childNodes.size();
		//System.out.println(nodesCount);
		String status = jPath1.get("result.content.status");
		Assert.assertTrue(nodesCount==1);
		Assert.assertTrue(identifier!=textBookId);
		Assert.assertTrue(status.equals("Draft"));
	}
	
	// Copy textbook unit with two children
	@Test
	public void copyTextBookWithTwoChildrenExpectSuccess200(){
		createContent();
		createTextBookUnit();
		// Create TextBook 
		setURI();
		Response Res3 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidTextBook).
				when().
				post("/content/v3/create").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().response();
		
		JsonPath jPath3 = Res3.jsonPath();
		String textBookId = jPath3.get("result.node_id");
		
		// Update Hierarchy
		setURI();
		jsonUpdateHierarchyTwoChild = jsonUpdateHierarchyTwoChild.replaceAll("TextbookId", textBookId).replaceAll("unitId1", unitId1).replaceAll("unitId2", unitId2).replaceAll("contentId1", contentId1).replaceAll("contentId2", contentId2);
		//System.out.println(jsonUpdateHierarchyTwoChild);
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonUpdateHierarchyTwoChild).
		with().
		contentType(JSON).
		when().
		patch("/content/v3/hierarchy/update/").
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		// Publish textbook
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
		when().
		post("/content/v3/publish/"+textBookId).
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		// Copy textbook
		try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);} 	
		setURI();
		Response Res1 =
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCopyContent).
				with().
				contentType(JSON).
				when().
				post("/content/v3/copy/" +textBookId).
				then().
				spec(get200ResponseSpec()).
				extract().response();
		
		JsonPath jPath = Res1.jsonPath();
		String copyId = jPath.get("result.node_id."+textBookId+"");
		
		// Get Content and validate
		setURI();
		Response Res2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/"+copyId).
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();
		
		JsonPath jPath1 = Res2.jsonPath();
		String identifier = jPath1.get("result.content.identifier");
	//	String artifactUrl = jPath1.get("result.content.artifactUrl");
		ArrayList<String> childNodes = jPath1.get("result.content.children");
		int nodesCount = childNodes.size();
		//System.out.println(nodesCount);
		String status = jPath1.get("result.content.status");
		Assert.assertTrue(nodesCount==2);
		Assert.assertTrue(identifier!=textBookId);
		Assert.assertTrue(status.equals("Draft"));
	}
	
	// Copy textbook with resource visibility as parent
	@Test
	public void copyTextBookWithResourceVisibilityParentExpectSuccess200(){
		createContent();
		createParentVisibilityContent();
		createTextBookUnit();
		// Create TextBook 
		setURI();
		Response Res3 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidTextBook).
				when().
				post("/content/v3/create").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().response();
		
		JsonPath jPath3 = Res3.jsonPath();
		String textBookId = jPath3.get("result.node_id");
		
		// Update Hierarchy
		setURI();
		jsonUpdateHierarchyTwoChild = jsonUpdateHierarchyTwoChild.replaceAll("TextbookId", textBookId).replaceAll("unitId1", unitId1).replaceAll("unitId2", unitId2).replaceAll("contentId1", contentId1).replaceAll("contentId2", contentId3);
		//System.out.println(jsonUpdateHierarchyTwoChild);
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonUpdateHierarchyTwoChild).
		with().
		contentType(JSON).
		when().
		patch("/content/v3/hierarchy/update/").
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		// Publish textbook
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
		when().
		post("/content/v3/publish/"+textBookId).
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		// Copy textbook
		try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);} 	
		setURI();
		Response Res1 =
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCopyContent).
				with().
				contentType(JSON).
				when().
				post("/content/v3/copy/" +textBookId).
				then().
				spec(get200ResponseSpec()).
				extract().response();
		
		JsonPath jPath = Res1.jsonPath();
		String copyId = jPath.get("result.node_id."+textBookId+"");
		
		// Get Content and validate
		setURI();
		Response Res2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/"+copyId).
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().
				response();
		
		JsonPath jPath1 = Res2.jsonPath();
		String identifier = jPath1.get("result.content.identifier");
	//	String artifactUrl = jPath1.get("result.content.artifactUrl");
		ArrayList<String> childNodes = jPath1.get("result.content.children");
		int nodesCount = childNodes.size();
		//System.out.println(nodesCount);
		String status = jPath1.get("result.content.status");
		Assert.assertTrue(nodesCount==2);
		Assert.assertTrue(identifier!=textBookId);
		Assert.assertTrue(status.equals("Draft"));
	}
	
	// Copy content with invalid request
	@Test
	public void copyContentInvalidRequestExpect4xx(){
		createContent();
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{\"createdBy\":\"Test\",\"createdFor\":[\"Test\"]}}}").
		with().
		contentType(JSON).
		when().
		post("/content/v3/copy/" +contentId1).
		then().
		spec(get400ResponseSpec());
	}
}
