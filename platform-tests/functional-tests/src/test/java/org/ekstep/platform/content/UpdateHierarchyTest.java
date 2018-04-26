package org.ekstep.platform.content;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.platform.domain.BaseTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;


public class UpdateHierarchyTest extends BaseTest{

	int rn = generateRandomInt(0, 9999999);
	String jsonCreateValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_" + rn+ "\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_NFT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
	String jsonUpdateHierarchyTwoChild = "{\"request\":{\"data\":{\"nodesModified\":{\"unitId1\":{\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\", \"name\":\"LP_FT_CourseUnit1_+rn+\",\"contentType\":\"TextBookUnit\",\"code\":\"Test_QA\"}},\"unitId2\":{\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"name\":\"LP_FT_CourseUnit2_+rn+\",\"contentType\":\"TextBookUnit\",\"code\":\"Test_QA\"}}},"
			+ "\"hierarchy\":{\"TextbookId\":{\"name\":\"LP_NFT_Collection_"+rn+"\",\"contentType\":\"TextBook\",\"children\":[\"unitId1\",\"unitId2\"],\"root\":true},\"unitId1\":{\"name\":\"LP_FT_CourseUnit1_"+rn+"\",\"contentType\":\"TextBookUnit\",\"children\":[\"contentId1\"],\"root\":false},\"unitId2\":{\"name\":\"LP_FT_CourseUnit2_"+rn+"\",\"contentType\":\"TextBookUnit\",\"children\":[\"contentId2\"],\"root\":false},\"contentId1\":{\"name\":\"LP_FT_Content1_"+rn+"\",\"root\":false},"
			+ "\"contentId2\":{\"name\":\"LP_FT_Content2_"+rn+"\",\"root\":false}}}}}";
	String jsonCreateValidTextBookUnit = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_Unit" + rn+ "\", \"mediaType\": \"content\",\"visibility\": \"Parent\",\"name\": \"LP_NFT_Unit_"+ rn+ "\",\"contentType\": \"TextBookUnit\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"tags\":[\"LP_functionalTest\"]}}}";
	String jsonCreateValidTextBook = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_TBook" + rn+ "\", \"mediaType\": \"content\",\"visibility\": \"Parent\",\"name\": \"LP_NFT_TBook_"+ rn+ "\",\"contentType\": \"TextBook\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"tags\":[\"LP_functionalTest\"]}}}";
	
	static ClassLoader classLoader = ContentPublishWorkflowTests.class.getClassLoader();
	static URL url = classLoader.getResource("DownloadedFiles");
	static File downloadPath;
	static File path = new File(classLoader.getResource("UploadFiles/").getFile());

	private static String contentId1 = null;
	private static String contentId2 = null;
	private static String unitId1 = null;
	private static String unitId2 = null;
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
	
	@Before
	public void init() {
		if ((StringUtils.isBlank(contentId1)) || (StringUtils.isBlank(contentId2)))
			createContent();
		if ((StringUtils.isBlank(unitId1)) || (StringUtils.isBlank(unitId2)))
			createTextBookUnit();
		try {Thread.sleep(3000);} catch (InterruptedException e) {e.printStackTrace();}
	}	
	
	private void createContent(){
		for (int i=1; i<=2; i++){
			if(i==1)
				jsonCreateValidContent = jsonCreateValidContent.replace("LP_NFT_", "LP_NFT_Content1_");
			if(i==2)
				jsonCreateValidContent = jsonCreateValidContent.replace("LP_NFT_", "LP_NFT_Content2_");
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
					spec(get200ResponseSpec()).
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
			spec(get200ResponseSpec());

			// Publish the content
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/"+nodeId).
			then().
			log().all().
			spec(get200ResponseSpec());
						
			if(i==1)
				contentId1 = nodeId;
			if(i==2)
				contentId2 = nodeId;
			
			// Get Content and validate
			setURI();
			Response Res2 = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();
			
			JsonPath jPath1 = Res2.jsonPath();
			String identifier = jPath1.get("result.content.identifier");
			String versionKey = jPath1.get("result.content.versionKey");
			Assert.assertTrue(versionKey != null);
			Assert.assertEquals(nodeId, identifier);
			System.out.println(nodeId);

		}
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
				log().all().
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
	
	@Test
	public void createAndUpdateHierarchyExpectSuccess200(){		
	// Create TextBook 
	setURI();
	Response Res3 = 
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body(jsonCreateValidTextBook).
			when().
			post("/content/v3/create").
			then().
			log().all().
			spec(get200ResponseSpec()).
			extract().response();
	
	JsonPath jPath3 = Res3.jsonPath();
	String textBookId = jPath3.get("result.node_id");
	
	// Update Hierarchy
	setURI();
	System.out.println(contentId1 +contentId2+ unitId1 +unitId2);
	jsonUpdateHierarchyTwoChild = jsonUpdateHierarchyTwoChild.replaceAll("TextbookId", textBookId).replaceAll("unitId1", unitId1).replaceAll("unitId2", unitId2).replaceAll("contentId1", contentId1).replaceAll("contentId2", contentId2);
	System.out.println(jsonUpdateHierarchyTwoChild);
	given().
	spec(getRequestSpecification(contentType, userId, APIToken)).
	body(jsonUpdateHierarchyTwoChild).
	with().
	contentType(JSON).
	when().
	patch("/content/v3/hierarchy/update/").
	then().
	log().all().
	spec(get200ResponseSpec());
	
	//Publish the textbook
	setURI();
	given().
	spec(getRequestSpecification(contentType, userId, APIToken)).
	body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
	when().
	post("/content/v3/publish/"+textBookId).
	then().
	log().all().
	spec(get200ResponseSpec());
	validateEcar(textBookId);
	
	}
	
	// Validate ECAR
	private void validateEcar(String textBookId) {
	// Get Content and validate
	setURI();
	Response Res2 = 
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			when().
			get("/content/v3/read/"+textBookId).
			then().
			//log().all().
			spec(get200ResponseSpec()).
			extract().
			response();
	
	JsonPath jPath1 = Res2.jsonPath();
	String statusActual = jPath1.get("result.content.status");
	
	// Validating the status
	if (statusActual.equals(PROCESSING) || statusActual.equals(PENDING)) {
		for (int i = 1000; i <= 30000; i = i + 1000) {
			try {Thread.sleep(i);} catch (InterruptedException e) {
				//System.out.println(e);
			}
			setURI();
			Response R3 = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/"+textBookId).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Validate the response
			JsonPath jp3 = R3.jsonPath();
			String statusUpdated = jp3.get("result.content.status");
			System.out.println(statusUpdated);
			if (statusUpdated.equals(PROCESSING) || statusUpdated.equals(PENDING)) {
				i = i + 1000;
			}
			if (statusUpdated.equals("Live")) {
				break;
			}
		}
	}
	String downloadUrl = jPath1.get("result.content.downloadUrl");
	
	String ecarName = "ecar_" + rn + "";
	try{
	FileUtils.copyURLToFile(new URL(downloadUrl), new File(downloadPath + "/" + ecarName + ".zip"));
	String source = downloadPath + "/" + ecarName + ".zip";
	File Destination = new File(downloadPath + "/" + ecarName + "");
	String Dest = Destination.getPath();
	ZipFile zip = new ZipFile(source);
	zip.extractAll(Dest);
	File manifest = new File(Dest + "/manifest.json");
	if(manifest.exists())
		System.out.println("Manifest Exists");
	JsonParser parser = new JsonParser();
	
	JsonElement jsonElement = parser.parse(new FileReader(manifest));
	JsonObject obj = jsonElement.getAsJsonObject();

	JsonObject arc = obj.getAsJsonObject("archive");
	JsonElement cnt = arc.get("count"); 
	int count = cnt.getAsInt();
	System.out.println(count);
	JsonArray items = arc.getAsJsonArray("items");
	int totalItems = items.size();
	System.out.println(totalItems);
	Iterator i = items.iterator();
	System.out.println(i);
	while (i.hasNext()) {
		JsonObject item = (JsonObject) i.next();
		System.out.println(item.toString()); 
	}
	}
	
	catch(Exception e){}
	
	}
	private String getStringValue(JsonObject obj, String attr) {
		if (obj.has(attr)) {
			JsonElement element = obj.get(attr);
			return element.getAsString();
		}
		return null;
	}
	
}
