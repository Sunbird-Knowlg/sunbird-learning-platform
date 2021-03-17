package org.sunbird.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.platform.domain.BaseTest;
import org.json.JSONException;
//import org.hamcrest.CoreMatchers;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

import net.lingala.zip4j.core.ZipFile;
@Ignore
public class ContentPublishV3TestCases extends BaseTest {

	int rn = generateRandomInt(0, 9999999);
	String jsonCreateValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_" + rn+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_NFT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
	String jsonCreateValidContentUpdated = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_" + rn+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_NFT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"gradeLevel\":[\"Class 1\"], \"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
	String jsonCreateValidContentWithConcept = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_" + rn+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\"\"concepts\":[{\"identifier\":\"LO1\",\"name\":\"Word Meaning\",\"objectType\":\"Concept\",\"relation\":\"associatedTo\",\"description\":\"Understanding meaning of words\",\"index\":null,\"status\":null,\"depth\":null,\"mimeType\":null,\"visibility\":null}],,\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_NFT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
	String jsonCreateContentCollection = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_Collection_" + rn+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"description\": \"Test_QA\",\"name\": \"LP_NFT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Collection\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"owner\": \"EkStep\", \"children\": [{ \"identifier\": \"id1\"}, { \"identifier\": \"id2\"}]}}}";
	String jsonCreateTextbookUnit = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_Collection_" + rn+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_NFT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"TextBookUnit\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"owner\": \"EkStep\", \"children\": [{\"identifier\": \"id1\"},{ \"identifier\": \"id2\"},{\"identifier\": \"id3\"},{\"identifier\": \"id4\"}]}}}";
	String jsonUpdateContentValid = "{\"request\": {\"content\": {\"versionKey\": \"version_Key\", \"status\": \"Live\"}}}";
	String jsonGetContentList = "{\"request\": { \"search\": {\"tags\":[\"LP_functionalTest\"],\"limit\": 5000}}}";
	String jsonGetContentListEmptySearch = "{\"request\": { \"search\": {}}}";
	String jsonCreateNestedCollection = "{\"request\": {\"content\": {\"identifier\": \"Test_QANested_" + rn+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_NFT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Collection\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"owner\": \"EkStep\", \"children\": [{ \"identifier\": \"id1\"}]}}}";
	String jsonCreateInvalidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_" + rn+ "\",\"osId\": \"org.sunbird.app\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_NFT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.archive\",\"pkgVersion\": 3,\"tags\":[\"LP_functionalTest\"]}}}";
	String jsonUpdateATContentBody = "{\"request\": {\"content\": {\"versionKey\": \"version_Key\", \"body\": \"{\\\"theme\\\":{\\\"id\\\":\\\"theme\\\",\\\"version\\\":\\\"1.0\\\",\\\"startStage\\\":\\\"cd168631-889c-4414-909d-a85a83ca3a68\\\",\\\"stage\\\":[{\\\"x\\\":0,\\\"y\\\":0,\\\"w\\\":100,\\\"h\\\":100,\\\"id\\\":\\\"cd168631-889c-4414-909d-a85a83ca3a68\\\",\\\"rotate\\\":null,\\\"config\\\":{\\\"__cdata\\\":\\\"{\\\"opacity\\\":100,\\\"strokeWidth\\\":1,\\\"stroke\\\":\\\"rgba(255, 255, 255, 0)\\\",\\\"autoplay\\\":false,\\\"visible\\\":true,\\\"color\\\":\\\"#FFFFFF\\\",\\\"genieControls\\\":false,\\\"instructions\\\":\\\"\\\"}\\\"},\\\"param\\\":[{\\\"name\\\":\\\"next\\\",\\\"value\\\":\\\"b4a01a33-a6e4-4c63-b37a-11c783c950b5\\\"}],\\\"manifest\\\":{\\\"media\\\":[{\\\"assetId\\\":\\\"do_11233272325713920013\\\"}]},\\\"image\\\":[{\\\"asset\\\":\\\"do_11233272325713920013\\\",\\\"x\\\":20,\\\"y\\\":20,\\\"w\\\":49.81,\\\"h\\\":88.56,\\\"rotate\\\":0,\\\"z-index\\\":0,\\\"id\\\":\\\"d434956b-672c-4204-bdd1-864dbae40c0c\\\",\\\"config\\\":{\\\"__cdata\\\":\\\"{\\\"opacity\\\":100,\\\"strokeWidth\\\":1,\\\"stroke\\\":\\\"rgba(255, 255, 255, 0)\\\",\\\"autoplay\\\":false,\\\"visible\\\":true}\\\"}}]},{\\\"x\\\":0,\\\"y\\\":0,\\\"w\\\":100,\\\"h\\\":100,\\\"id\\\":\\\"b4a01a33-a6e4-4c63-b37a-11c783c950b5\\\",\\\"rotate\\\":null,\\\"config\\\":{\\\"__cdata\\\":\\\"{\\\"opacity\\\":100,\\\"strokeWidth\\\":1,\\\"stroke\\\":\\\"rgba(255, 255, 255, 0)\\\",\\\"autoplay\\\":false,\\\"visible\\\":true,\\\"color\\\":\\\"#FFFFFF\\\",\\\"genieControls\\\":false,\\\"instructions\\\":\\\"\\\"}\\\"},\\\"param\\\":[{\\\"name\\\":\\\"previous\\\",\\\"value\\\":\\\"cd168631-889c-4414-909d-a85a83ca3a68\\\"}],\\\"manifest\\\":{\\\"media\\\":[{\\\"assetId\\\":\\\"do_11233272325713920013\\\"},{\\\"assetId\\\":\\\"do_10095813\\\"}]},\\\"image\\\":[{\\\"asset\\\":\\\"do_11233272325713920013\\\",\\\"x\\\":20,\\\"y\\\":20,\\\"w\\\":49.81,\\\"h\\\":88.56,\\\"rotate\\\":0,\\\"z-index\\\":0,\\\"id\\\":\\\"cc35e88c-1630-414b-9d50-a343c522e316\\\",\\\"config\\\":{\\\"__cdata\\\":\\\"{\\\"opacity\\\":100,\\\"strokeWidth\\\":1,\\\"stroke\\\":\\\"rgba(255, 255, 255, 0)\\\",\\\"autoplay\\\":false,\\\"visible\\\":true}\\\"}},{\\\"asset\\\":\\\"do_10095813\\\",\\\"x\\\":20,\\\"y\\\":20,\\\"w\\\":49.49,\\\"h\\\":87.98,\\\"rotate\\\":0,\\\"z-index\\\":1,\\\"id\\\":\\\"7849c5a6-0013-44a6-97ae-c5872974d500\\\",\\\"config\\\":{\\\"__cdata\\\":\\\"{\\\"opacity\\\":100,\\\"strokeWidth\\\":1,\\\"stroke\\\":\\\"rgba(255, 255, 255, 0)\\\",\\\"autoplay\\\":false,\\\"visible\\\":true}\\\"}}]}],\\\"manifest\\\":{\\\"media\\\":[{\\\"id\\\":\\\"do_11233272325713920013\\\",\\\"src\\\":\\\"/assets/public/content/do_11233272325713920013/artifact/5c568572a97acec4f01f596694396418_1505459382119.jpeg\\\",\\\"type\\\":\\\"image\\\"},{\\\"id\\\":\\\"do_10095813\\\",\\\"src\\\":\\\"/assets/public/content/c7a7d301f288f1afe24117ad59083b2a_1475430290462.jpeg\\\",\\\"type\\\":\\\"image\\\"}]},\\\"plugin-manifest\\\":{\\\"plugin\\\":[]},\\\"compatibilityVersion\\\":2}}\"}}}";
	String jsonUpdateChildren = "{\"request\":{\"content\":{\"children\":[],\"versionKey\":\"version_Key\"}}}";
	String jsonPublishContent = "{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}";
	String jsonUpdateMetadata = "{\"request\":{\"content\":{\"versionKey\":\"version_key\",\"language\":[\"Tamil\",\"Telugu\"]}}}";
	String invalidContentId = "LP_NFT" + rn + "";
	String malformedXMLBody = "xml version=\"1.0\" ";
	String malformedJSONBody = "{\"theme\":{\"manifes77\",\"scribble\":[],\"htext\":[],\"g\":[]}";
	String jsonContentClean = "{\"request\": {\"searchProperty\": \"identifier\",\"searchOperator\": \"startsWith\",\"searchString\": \"LP_NFT\"}}";

	String jsonSimpleSearchQuery = "{\"request\": {\"filters\":{},\"query\": \"add\",\"limit\": 10}}";
	String jsonFilteredSearch = "{\"request\": {\"filters\": {\"objectType\": [\"content\", \"concept\"], \"identifier\":[\"identifierNew\"]},\"limit\": 10}}"; 
	String jsonSearchWithFilter = "{\"request\":{\"filters\":{\"objectType\":[\"Concept\",\"Word\",\"Domain\",\"Dimension\",\"AssessmentItem\",\"Content\",\"Method\"]}}}";
	String jsonSearchQueryAndFilter = "{\"request\":{\"query\":\"lion\",\"filters\":{\"objectType\":[\"Concept\",\"Word\",\"Domain\",\"Dimension\",\"AssessmentItem\",\"Content\",\"Method\"]}}}";
	String jsonSearchLogicalReq = "{\"request\":{\"filters\":{\"objectType\":[\"Word\"],\"graph_id\":[\"hi\"],\"orthographic_complexity\":{\"<=\":2,\">\":0.5},\"syllableCount\":{\">\":2,\"<=\":5}}}}";
	String jsonSearchWithStartsEnds = "{\"request\":{\"filters\":{\"objectType\":[\"Word\"],\"graph_id\":[\"en\"],\"lemma\":{\"startsWith\":\"e\",\"endsWith\":\"t\"}}}}";
	String jsonSearchWithEquals = "{\"request\":{\"filters\":{\"lemma\":\"eat\"}}}";
	String jsonSearchWithFacets = "{\"request\":{\"filters\":{\"objectType\":[\"Concept\",\"Word\",\"Domain\",\"Dimension\",\"AssessmentItem\",\"Content\",\"Method\"]},\"facets\":[\"contentType\",\"domain\",\"ageGroup\",\"language\",\"gradeLevel\"]}}";
	String jsonSearchWithSortByAsc = "{\"request\":{\"filters\":{\"objectType\":[\"Concept\",\"Word\",\"Domain\",\"Dimension\",\"AssessmentItem\",\"Content\",\"Method\"]},\"facets\":[\"contentType\",\"domain\",\"ageGroup\",\"language\",\"gradeLevel\"],\"sort_by\":{\"name\":\"asc\"}}}";
	String jsonSearchWithSortByDsc = "{\"request\":{\"filters\":{\"objectType\":[\"Concept\",\"Word\",\"Domain\",\"Dimension\",\"AssessmentItem\",\"Content\",\"Method\"]},\"facets\":[\"contentType\",\"domain\",\"ageGroup\",\"language\",\"gradeLevel\"],\"sort_by\":{\"name\":\"desc\"}}}";
	private String PROCESSING = "Processing";
	private String PENDING = "Pending";
	
	
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
	@Before
	public void delay(){
		try{Thread.sleep(3000);}catch(InterruptedException e){ System.out.println(e);} 
	}
	@After
	public void CleanUp() {
	}

	// Create and get ECML Content
	@Test
	public void createValidEcmlContentExpectSuccess200() {
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
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Get content and validate
		setURI();
		Response R1 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when()
				.get("/content/v3/read/" + nodeId).
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.content.identifier");
		String versionKey = jP1.get("result.content.versionKey");
		Assert.assertTrue(versionKey != null);
		Assert.assertEquals(nodeId, identifier);
		contentCleanUp(nodeId);
	}

	@Ignore
	public void createContentWithValidConceptExpectSuccess200() {
		setURI();
		Response R = given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidContentWithConcept).
				with().contentType(JSON).
				when().
				post("content/v3/create").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Get content and validate
		setURI();
		Response R1 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId).
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.content.identifier");
		String versionKey = jP1.get("result.content.versionKey");
		Assert.assertTrue(versionKey != null);
		Assert.assertEquals(nodeId, identifier);
		contentCleanUp(nodeId);
	}

	// Create and get valid HTML
	@Test
	public void createValidHTMLContentExpectSuccess200() {
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("mimeType", "application/vnd.ekstep.html-archive");
		String jsonCreateValidContentHtml = js.toString();
		Response R = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidContentHtml).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Get content and check
		setURI();
		Response R1 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId).
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP1 = R1.jsonPath();
		String status = jP1.get("result.content.status");
		String versionKey = jP1.get("result.content.versionKey");
		Assert.assertTrue(versionKey != null);
		Assert.assertEquals(status, "Draft");
		contentCleanUp(nodeId);
	}
	
	// Create content with grade level, without framework and check for the default framework
	@Test 
	public void createContentWithValidGradeLevelExpectSuccess200(){
		setURI();
		Response R = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidContentUpdated).
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

		// Get content and check
		setURI();
		Response R1 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId).
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.content.identifier");
		String versionKey = jP1.get("result.content.versionKey");
		ArrayList<String> gradeLevel = jP1.get("result.content.gradeLevel");
		Assert.assertTrue(gradeLevel.contains("Class 1"));
		Assert.assertTrue(versionKey != null);
		Assert.assertEquals(nodeId, identifier);
		contentCleanUp(nodeId);
	}

	// Create and get valid APK
	@Test
	public void createValidAPKContentExpectSuccess200() {
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("mimeType", "application/vnd.android.package-archive");
		String jsonCreateValidContentAPK = js.toString();
		Response R = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidContentAPK).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Get content and check
		setURI();
		Response R1 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId).
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.content.identifier");
		String versionKey = jP1.get("result.content.versionKey");
		Assert.assertTrue(versionKey != null);
		Assert.assertEquals(nodeId, identifier);
		contentCleanUp(nodeId);
	}

	// Create and get new collection
	@Test
	public void createValidCollectionExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		int count = 1;
		while (count <= 2) {
			setURI();
			int rn = generateRandomInt(2000, 29999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFTT_" + rn + "").put("name","LP_NFTT-" + rn + "");
			String jsonCreateValidChild = js.toString();
			Response R = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					body(jsonCreateValidChild).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					// log().all().
					spec(get200ResponseSpec()).
					extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;
			}
			if (count == 2) {
				node2 = nodeId;
			}
			count++;
		}
		// Create collection
		setURI();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
		Response R1 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateContentCollection).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		// Get collection and validate
		JsonPath jp1 = R1.jsonPath();
		String nodeId = jp1.get("result.node_id");

		// Get collection
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId).
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		ArrayList<String> identifiers = jP2.get("result.content.children.identifier");
		String versionKey = jP2.get("result.content.versionKey");
		Assert.assertTrue(versionKey != null);
		Assert.assertTrue(identifiers.contains(node1) && identifiers.contains(node2));
		contentCleanUp(nodeId);
	}

	// Create content without contentType
	@Test
	public void createContentWithoutContentTypeExpect4xx(){
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").remove("contentType");
		jsonCreateValidContent = js.toString();
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonCreateValidContent).
		with().
		contentType(JSON).
		when().
		post("content/v3/create").
		then().
		// log().all().
		spec(get400ResponseSpec());
		
	}
	
	// Create content with content type and resource type
	@Test
	public void createContentwithContentTypeResourceTypeExpectSuccess200(){
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("resourceType","Story");
		jsonCreateValidContent = js.toString();
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
		// log().all().
		spec(get200ResponseSpec()).
		extract().response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Get content and validate
		setURI();
		Response R1 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when()
				.get("/content/v3/read/" + nodeId).
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.content.identifier");
		String versionKey = jP1.get("result.content.versionKey");
		String resourceType = jP1.get("result.content.resourceType");
		Assert.assertTrue(versionKey != null);
		Assert.assertTrue(resourceType.equals("Story"));
		Assert.assertEquals(nodeId, identifier);
		contentCleanUp(nodeId);
	}
	
	// Create content with resource type as list
	@Test
	public void createContentWithResourceTypeAsListExpect4xxx(){
			JSONObject js = new JSONObject(jsonCreateValidContent);
			ArrayList<String> resourceType = new ArrayList<String>();
			resourceType.add("Story");
			js.getJSONObject("request").getJSONObject("content").put("resourceType",resourceType);
			jsonCreateValidContent = js.toString();
			// System.out.println(jsonCreateValidContent);
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body(jsonCreateValidContent).
			with().
			contentType(JSON).
			when().
			post("content/v3/create").
			then().
			// log().all().
			spec(get400ResponseSpec()).
			extract().response();
	}
	
	// Create Invalid content
	@Test
	public void createInvalidContentExpects400() {
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonCreateInvalidContent).
		with().
		contentType(JSON).
		when().
		post("content/v3/create").
		then().
		spec(get400ResponseSpec());
	}

	// Create content with invalid mimeType
	@Test
	public void createInValidmimeTypeContentExpectSuccess200() {
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("mimeType", "application/vnd.ekstep.-archive");
		String jsonCreateValidContentHtml = js.toString();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonCreateValidContentHtml).
		with().
		contentType(JSON).
		when().
		post("content/v3/create").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}

	// Create content with invalid contentType
	@Test
	public void createInValidcontentTypeContentExpectSuccess200() {
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("contentType", "TestContentType01");
		String jsonCreateValidContentHtml = js.toString();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonCreateValidContentHtml).
		with().
		contentType(JSON).
		when().
		post("content/v3/create").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}

	// Create Existing content
	@Test
	public void createExistingContentExpect400() {
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
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String ecmlNode = jp.get("result.node_id");

		// Create content with the same identifier
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("identifier", ecmlNode);
		String jsonCreateExistingContent = js.toString();
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonCreateExistingContent).
		with().
		contentType(JSON).
		when().
		post("content/v3/create").
		then().
		// log().all().
		spec(get400ResponseSpec());
		contentCleanUp(ecmlNode);

	}

	// Create collection with invalid content

	// Create content
	@Test
	public void createInvalidCollectionExpect400() {
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
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String ecmlNode = jp.get("result.node_id");

		// Create collection
		setURI();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", ecmlNode).replace("id2",invalidContentId);
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonCreateContentCollection).
		with().
		contentType(JSON).
		when().
		post("content/v3/create").
		then().
		// log().all().
		spec(get400ResponseSpec());
		contentCleanUp(ecmlNode);
	}

	// Update and get list
	@Test
	public void updateValidContentExpectSuccess200() {
		setURI();
		Response R = given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidContent).
				with().contentType(JSON).
				when().
				post("content/v3/create").then().
				// log().all().
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");
		String versionKey = jp.get("result.versionKey");

		// Update content status to live
		setURI();
		jsonUpdateContentValid = jsonUpdateContentValid.replace("version_Key", versionKey);
		// System.out.println(jsonUpdateContentValid);
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonUpdateContentValid).
		with().
		contentType("application/json").when().patch("/content/v3/update/" + nodeId).then().
		// log().all().
		spec(get400ResponseSpec());
		contentCleanUp(nodeId);
	}
	
	// Create and get Content
	@Test
	public void getValidContentExpectSuccess200(){
		setURI();
		Response R =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String ecmlNode = jp.get("result.node_id");

		// Get content and validate
		setURI();
		Response R1 =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				when().
				get("/content/v3/read/"+ecmlNode).
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.content.identifier");
		String versionKey = jP1.get("result.content.versionKey");
		Assert.assertTrue(versionKey!=null);
		Assert.assertEquals(ecmlNode, identifier);
		contentCleanUp(ecmlNode);
	}


	// Get blank content
	@Test
	public void getblankContentExpect500(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken)).
		when().
		get("/content/v3/read/").
		then().
		// log().all().
		spec(get500ResponseSpec());
	}

	// Get invalid content
	@Test
	public void getInvalidContentExpect404(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken)).
		when().
		get("/content/v3/read/F;NDSAF").
		then().
		// log().all().
		spec(get404ResponseSpec());
	}

	// Create and get image content for content in draft status

	@Test
	public void getinvalidImageContentExpect400(){
		setURI();
		Response R =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				// log().all().
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String ecmlNode = jp.get("result.node_id");

		// Get content and validate
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken)).
		when().
		get("/content/v3/read/"+ecmlNode+".img").
		then().
		// log().all().
		spec(get404ResponseSpec());
	}
	
	// Create and get image content for valid content
	@Test
	public void getImageContentVaidExpectSuccess200(){
		setURI();
		Response R =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				// log().all().
				//spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
		multiPart(new File(path+"/uploadContent.zip")).
		then().
		post("/content/v3/upload/"+nodeId);
		//then().
		// log().all().
		//spec(get200ResponseSpec());

		// Publish created content
		setURI();
		Response Rp2=
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
				when().
				post("/content/v3/publish/"+nodeId).
				then().
				// log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		JsonPath j2 = Rp2.jsonPath();
		String versionKey = j2.get("result.versionKey");

		// Update Content
		setURI();
		jsonUpdateATContentBody = jsonUpdateATContentBody.replace("version_Key", versionKey);
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken)).
		body(jsonUpdateATContentBody).
		with().
		contentType("application/json").
		then().
		patch("/content/v3/update/"+nodeId);
		//then().
		// log().all().
		//spec(get200ResponseSpec());

		// Get and validate
		setURI();
		Response R2 =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				when().
				get("/content/v3/read/"+nodeId+"?mode=edit").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String identifier = jP2.get("result.content.identifier");
		Assert.assertFalse(identifier.contains(".img"));
		contentCleanUp(nodeId);
	}

	//Get content with fields

	@Test
	public void getContentWithFieldsExpectSuccess200(){
		setURI();
		Response R =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				// log().all().
				//spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
		multiPart(new File(path+"/uploadContent.zip")).
		then().
		post("/content/v3/upload/"+nodeId);
		//then().
		// log().all().
		//spec(get200ResponseSpec());

		// Publish created content
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken)).
		body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
		then().
		post("/content/v3/publish/"+nodeId);

		// Get and validate
		setURI();
		Response R2 =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				when().
				get("/content/v3/read/"+nodeId+"?fields=body,artifactUrl,downloadUrl").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String identifier = jP2.get("result.content.identifier");
		String body = jP2.get("result.content.body");
		String artifactUrl = jP2.get("result.content.artifactUrl");
		Assert.assertFalse(identifier.contains(".img"));
		Assert.assertTrue(body!=null && artifactUrl.endsWith(".zip"));
		contentCleanUp(nodeId);
	}

	//Get Content List
	@Test
	public void getContentListExpectSuccess200()
	{
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonGetContentList).
		with().
		contentType("application/json").
		when().
		post("/content/v3/list").
		then().
		// log().all().
		spec(get200ResponseSpec());

	}

	//Get Content List
	@Test
	public void getContentListEmptySearchExpect200()
	{
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonGetContentListEmptySearch).
		with().
		contentType("application/json").
		when().
		post("/content/v3/list").
		then().
		// log().all().
		spec(get200ResponseSpec());
	}

	//Search Content List
	@Test
	public void searchContentListExpectSuccess200()
	{
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonGetContentList).
		with().
		contentType("application/json").
		when().
		post("content/v3/search").
		then().
		// log().all().
		spec(get200ResponseSpec());
	}

	@Test
	public void getContentExpectSuccess200()
	{
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
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Get content and validate
		setURI();
		Response R1 = given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId).
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.content.identifier");
		String versionKey = jP1.get("result.content.versionKey");
		Assert.assertTrue(versionKey != null);
		Assert.assertEquals(nodeId, identifier);
	}

	// Update content with metadata changes
	@Ignore
	public void updateMetaDataExpectSuccess200() {
		setURI();
		Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("/content/v3/create").
				then().spec(get200ResponseSpec()).
				// log().all().
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/uploadContent.zip")).
		when().post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Publish created content
		setURI();
		Response R1 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
				when().
				post("/content/v3/publish/" + nodeId).
				then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String versionKey = jP1.get("result.versionKey");

		// Update content metadata
		jsonUpdateMetadata = jsonUpdateMetadata.replace("version_key", versionKey);
		//// System.out.println(jsonUpdateMetadata);
		try{Thread.sleep(5000);}catch(Exception e){e.printStackTrace();};
		setURI();
		Response nR = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonUpdateMetadata).
				with().
				contentType("application/json").
				when().patch("/content/v3/update/" + nodeId).
				then().// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Extracting the JSON path
		JsonPath njP = nR.jsonPath();
		String versionKey1 = njP.get("result.versionKey");
		Assert.assertFalse(versionKey.equals(versionKey1));

		// Publish the content
		try{Thread.sleep(5000);}catch(Exception e){e.printStackTrace();};
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
		when().
		post("/content/v3/publish/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get and validate the change
		setURI();
		Response R2 = given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("content/v3/read/" + nodeId).
				then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP2 = R2.jsonPath();
		ArrayList<String> language = jP2.get("result.content.language");
		Assert.assertTrue(language.contains("Tamil") && language.contains("Telugu"));
		contentCleanUp(nodeId);
	}
	

	// Upload file without index
	// Create content
	@Test
	public void uploadContentWithoutIndexExpect400() {
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
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/UploadWithoutIndex.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get400ResponseSpec());
		contentCleanUp(nodeId);
	}

	// Upload file with invalid ecml
	// Create content
	@Test
	public void uploadContentWithInvalidEcmlExpect400() {
		setURI();
		Response R = given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/uploadInvalidEcml.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get400ResponseSpec());
		contentCleanUp(nodeId);
	}

	// Upload html content without index.html
	// Create content
	@Test
	public void uploadHTMLContentWithoutIndexExpect400() {
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("mimeType", "application/vnd.ekstep.html-archive");
		String jsonCreateValidContentHtml = js.toString();
		Response R = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidContentHtml).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				// // log().all().
				// spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/Build-a-sentence.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get400ResponseSpec());
		contentCleanUp(nodeId);
	}

	// Upload file with invalid mimeType

	// Create content
	@Test
	public void uploadContentWithInvalidmimeTypeExpect400() {
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
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/uploadAPK.apk")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get400ResponseSpec());
		contentCleanUp(nodeId);
	}

	// Upload file with Empty zip

	// Create content
	@Test
	public void uploadContentWithEmptyZipExpect400() {
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
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/haircut_empty.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get400ResponseSpec());
		contentCleanUp(nodeId);
	}

	// Upload with invalid json

	// Create content
	@Test
	public void uploadContentWithInvalidJSONExpect400() {
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
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/ecmlCorruptedJSON.zip")).
		when().
		post("/content/v3/upload/" + nodeId).then().
		// log().all().
		spec(get400ResponseSpec());
		contentCleanUp(nodeId);
	}

	// Upload file more than 50 MB and assets above 20 MB

	// Create content
	@Ignore
	public void uploadContentAboveLimitExpect400() {
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
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/contentAbove50MB.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get400ResponseSpec());
		contentCleanUp(nodeId);
	}

	// Upload File with missing assets
	// Create content
	@Test
	public void uploadContentWithMissingAssetsExpect400() {
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
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/haircut_withoutAssets.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get400ResponseSpec());
		contentCleanUp(nodeId);
}
	
	//Upload content with single asset above 50 mb
	@Test
	public void uploadContentWithAssetAbove50MbExect4xx(){
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
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/upload60MbAssetContent.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get400ResponseSpec());
		contentCleanUp(nodeId);
	}

	// Upload invalid file

	// Create content
	@Test
	public void uploadContentInvalidFileExpect400() {
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
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/carpenter.png")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get400ResponseSpec());
		contentCleanUp(nodeId);
	}

	// Create and upload image asset valid

	/*
	 * // Create and upload Invalid audio asset
	 * 
	 * //Create content
	 * 
	 * @Test public void uploadandPublishInvalidAudioAssetExpect4xx(){ setURI();
	 * JSONObject js = new JSONObject(jsonCreateValidContent);
	 * js.getJSONObject("request").getJSONObject("content").put("contentType",
	 * "Asset").put("mimeType", "audio/mp3"); String jsonCreateImageAssetInvalid
	 * = js.toString(); Response R = given().
	 * spec(getRequestSpecification(contentType, userId, APIToken)).
	 * body(jsonCreateImageAssetInvalid). with(). contentType(JSON). when().
	 * post("content/v3/create"). then(). //// log().all().
	 * spec(get200ResponseSpec()). extract(). response();
	 * 
	 * // Extracting the JSON path JsonPath jp = R.jsonPath(); String nodeId =
	 * jp.get("result.node_id");
	 * 
	 * // Upload Content setURI(); given().
	 * spec(getRequestSpecification(uploadContentType, userId, APIToken)).
	 * multiPart(new File(path+"/pngImage.png")). when().
	 * post("/content/v3/upload/"+nodeId). then(). //// log().all().
	 * spec(get400ResponseSpec()); }
	 * 
	 * // Create and upload invalid mimeType audio asset
	 * 
	 * //Create content
	 * 
	 * @Test public void uploadandPublishInvalidmimeTypeAudioAssetExpect4xx(){
	 * setURI(); JSONObject js = new JSONObject(jsonCreateValidContent);
	 * js.getJSONObject("request").getJSONObject("content").put("contentType",
	 * "Asset").put("mimeType", "audio/mp3"); String jsonCreateImageAssetInvalid
	 * = js.toString(); Response R = given().
	 * spec(getRequestSpecification(contentType, userId, APIToken)).
	 * body(jsonCreateImageAssetInvalid). with(). contentType(JSON). when().
	 * post("content/v3/create"). then(). //// log().all().
	 * spec(get200ResponseSpec()). extract(). response();
	 * 
	 * // Extracting the JSON path JsonPath jp = R.jsonPath(); String nodeId =
	 * jp.get("result.node_id");
	 * 
	 * // Upload Content setURI(); given().
	 * spec(getRequestSpecification(uploadContentType, userId, APIToken)).
	 * multiPart(new File(path+"/Oggaudio.ogg")). when().
	 * post("/content/v3/upload/"+nodeId). then(). //// log().all().
	 * spec(get400ResponseSpec()); }
	 */

	// Upload valid content expect success

	// Create content
	@Test
	public void uploadandPublishContentExpectSuccess200() {
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
				// log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		//// System.out.println(path);
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/uploadContent.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get body and validate

		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {
			Assert.assertTrue(accessURL(nodeId));
		}
		contentCleanUp(nodeId);
	}

	// Create, upload and review with valid content

	// Create content
	@Test
	public void reviewContentExpectSuccess200() {
		setURI();
		Response R = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/uploadContent.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {

			// Setting status to review
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body("{\"request\":{\"content\":{}}}").
			when().
			post("/content/v3/review/" + nodeId).
			then().
			// log().all().
			spec(get200ResponseSpec());

			// Get content and validate
			setURI();
			Response R1 = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/" + nodeId).
					then().
					// log().all().
					spec(get200ResponseSpec()).
					extract().response();

			JsonPath jP1 = R1.jsonPath();
			String status = jP1.get("result.content.status");
			//// System.out.println(status);
			Assert.assertEquals(status, "Review");
			contentCleanUp(nodeId);
		}
	}

	// Review invalid content
	@Test
	public void reviewInvalidContentExpect4xx() {
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{}}}").
		when().
		post("content/v3/review/asfdkfa").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}

	// Review content without body
	@Test
	public void reviewContentWithoutBodyExpect4xx() {
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
				// log().all().
				// spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Review the content
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{}}}").
		when().
		post("/content/v3/review/" + nodeId).
		then().
		// log().all().
		spec(get400ResponseSpec());
		contentCleanUp(nodeId);
	}

	// Review authoring tool created content
	@Ignore
	public void reivewATContentExpectSuccess200() {
		setURI();
		Response R = given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				// log().all().
				// spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");
		String versionKey = jp.get("result.versionKey");

		// Update content body
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			//// System.out.println(e);
		}
		setURI();
		jsonUpdateATContentBody = jsonUpdateATContentBody.replace("version_Key", versionKey);
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonUpdateATContentBody).
		with().
		contentType("application/json").
		when().
		patch("/content/v3/update/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		//// System.out.println(body);

		// Setting status to review
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{}}}").
		when().
		post("/content/v3/review/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R1 = given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId).
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP1 = R1.jsonPath();
		String status = jP1.get("result.content.status");
		//// System.out.println(status);
		Assert.assertEquals(status, "Review");
		contentCleanUp(nodeId);
	}
	
	// Reject valid content
	@Test
	public void rejectValidContentExpectSuccess200(){
		setURI();
		Response R = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/uploadContent.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {

			// Setting status to review
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body("{\"request\":{\"content\":{}}}").
			when().
			post("/content/v3/review/" + nodeId).
			then().
			// log().all().
			spec(get200ResponseSpec());
			
			// Get content and validate
			setURI();
			Response R1 = given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/" + nodeId).
					then().
					// log().all().
					spec(get200ResponseSpec()).
					extract().response();

			JsonPath jP1 = R1.jsonPath();
			String status = jP1.get("result.content.status");
			//// System.out.println(status);
			Assert.assertEquals(status, "Review");
			
			// Reject the content
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body("{\"request\":{}}").
			when().
			post("/content/v3/reject/"+nodeId).
			then().
			// log().all(). 
			spec(get200ResponseSpec());
			
			// Get content and validate
			setURI();
			Response R3 = given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/" + nodeId).
					then().
					// log().all().
					spec(get200ResponseSpec()).
					extract().response();

			JsonPath jP3 = R3.jsonPath();
			String statusNew = jP3.get("result.content.status");
			//// System.out.println(status);
			Assert.assertEquals(statusNew, "Draft");
		}
		contentCleanUp(nodeId);
	}

	// Reject content with invalid content id
	@Test
	public void rejectContentWithInvalidContentIdExpect4xx(){

		// Reject the content
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{}}").
		when().
		post("/content/v3/reject/ajksdhvkja").
		then().
		// log().all(). 
		spec(get404ResponseSpec());
	}
	
	// Reject content with Draft status
	@Test
	public void rejectDraftContentExpect400(){
		setURI();
		Response R = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				// log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");
		
		// Reject Content
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{}}").
		when().
		post("/content/v3/reject/"+nodeId).
		then().
		// log().all(). 
		spec(get400ResponseSpec());
		contentCleanUp(nodeId);
	}
	
	// Reject content with flagReview Status
	@Test
	public void rejectFlagReviewContentExpectSuccess200(){
		setURI();
		Response R = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				// log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");
		
		// Upload zip file
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/uploadContent.zip")).
		when().
		post("/content/v3/upload/" + nodeId);
		//then().
		// log().all().
		//spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				// log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {

			// Publish created content
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/" + nodeId).
			then().
			// log().all().
			spec(get200ResponseSpec());
			
			// Get Versionkey
			setURI();
			try{Thread.sleep(5000);}catch(InterruptedException e){ System.out.println(e);}
			Response R4 = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/" + nodeId).
					then().
					// log().all().
					//spec(get200ResponseSpec()).
					extract().response();
			
			JsonPath jP4 = R4.jsonPath();
			String version_key = jP4.get("result.content.versionKey");
		
		// Flag the content
		setURI();
		Response R5 =
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"flagReasons\":[\"Copyright Violation\"],\"flaggedBy\":\"Vignesh\",\"versionKey\":"+version_key+"}}").
		with().
		contentType(JSON).
		when().
		post("/content/v3/flag/"+nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec()).				
		extract().response();
		
		JsonPath jP5 = R5.jsonPath();
		String version_key2 = jP5.get("result.versionKey");
		
		// Accept Flag
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"versionKey\":"+version_key2+"}}").
		with().
		contentType(JSON).
		when().
		post("/content/v3/flag/accept/"+nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());
		
		// Review the flagDraftcontent
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{}}}").
		when().
		post("/content/v3/review/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());
		
		// Get content and validate the reviewed content
		setURI();
		Response R8 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" +nodeId+"?mode=edit").
				then().
				// log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP8 = R8.jsonPath();
		String statusNew = jP8.get("result.content.status");
		Assert.assertTrue(statusNew.equals("FlagReview"));
		
		// Reject flag review content
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{}}").
		when().
		post("/content/v3/reject/"+nodeId).
		then().
		// log().all(). 
		spec(get200ResponseSpec());
		
		// Get content and validate
		setURI();
		Response R3 = given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId+".img").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP3 = R3.jsonPath();
		String statusUpdated = jP3.get("result.content.status");
		// System.out.println(status);
		Assert.assertEquals(statusUpdated, "FlagDraft");
		}
		contentCleanUp(nodeId);
	}
	
	// Retire draft content
	@Test
	public void retireDraftContentExpectSuccess200(){
	
	// Create content	
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
			// log().all().
			spec(get200ResponseSpec()).extract().response();

	// Extracting the JSON path
	JsonPath jp = R.jsonPath();
	String nodeId = jp.get("result.node_id");
	
	// Retire content
	setURI();
	given().
	spec(getRequestSpecification(contentType, userId, APIToken)).
	when().
	delete("/content/v3/retire/" + nodeId).
	then().
	spec(get200ResponseSpec());
	
	// Get content and validate
	setURI();
	Response R1 = 
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			when().
			get("/content/v3/read/" + nodeId).
			then().
			// log().all().
			spec(get200ResponseSpec()).
			extract().response();

	JsonPath jP1 = R1.jsonPath();
	String status = jP1.get("result.content.status");
	//// System.out.println(status);
	Assert.assertEquals(status, "Retired");
	contentCleanUp(nodeId);
	}
	
	// Retire review content
	public void retireReviewContentExpectSuccess200(){
		setURI();
		Response R = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/uploadContent.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {

			// Setting status to review
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body("{\"request\":{\"content\":{}}}").
			when().
			post("/content/v3/review/" + nodeId).
			then().
			// log().all().
			spec(get200ResponseSpec());
			
			// Retire content
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			when().
			delete("/content/v3/retire/" + nodeId).
			then().
			spec(get200ResponseSpec());

			// Get content and validate
			setURI();
			Response R1 = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/" + nodeId).
					then().
					// log().all().
					spec(get200ResponseSpec()).
					extract().response();

			JsonPath jP1 = R1.jsonPath();
			String status = jP1.get("result.content.status");
			//// System.out.println(status);
			Assert.assertEquals(status, "Retired");
		}
	}
	
	// Retire Live Content
	@Test
	public void retireLiveContentExpectSuccess200(){
		setURI();
		Response R = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/uploadContent.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {

			// Publish Content
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/" + nodeId).
			then().
			// log().all().
			spec(get200ResponseSpec());

			
			// Retire content
			setURI();
			try{Thread.sleep(5000);}catch(InterruptedException e){ System.out.println(e);} 	
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			when().
			delete("/content/v3/retire/" + nodeId).
			then().
			spec(get200ResponseSpec());

			// Get content and validate
			setURI();
			Response R1 = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/" + nodeId).
					then().
					// log().all().
					spec(get200ResponseSpec()).
					extract().response();

			JsonPath jP1 = R1.jsonPath();
			String status = jP1.get("result.content.status");
			// System.out.println(status);
			Assert.assertEquals(status, "Retired");
		}
	}
	
	// Retire content with invalid content id
	@Test
	public void retireInvalidContentExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		delete("/content/v3/retire/ncajskgiuf").
		then().
		spec(get404ResponseSpec());
	}
	
	// Create, upload, publish and validate ECML content
	
	// Create content
	@Test
	public void publishContentExpectSuccess200() {
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
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/uploadContent.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {
			Assert.assertTrue(accessURL(nodeId));
		}
		contentCleanUp(nodeId);
	}
	
	//Publish content with asset more than 20 mb
	@Test
	public void publishContentAbove20MbAssetExpectSuccess200(){
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
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/upload32mbAssetContent.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {
			Assert.assertTrue(accessURL(nodeId));
		}
		contentCleanUp(nodeId);
	}
	
	// Publish content with assessment item and validate the count
	@Ignore
	@Test
	public void publishContentWithAssessmentItemExpectSuccess200(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("totalScore", 10).put("totalQuestions", 5);
		jsonCreateValidContent = js.toString();
		// System.out.println(jsonCreateValidContent);
		Response R = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/uploadContent.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {
			Assert.assertTrue(accessURL(nodeId));
		}
		contentCleanUp(nodeId);
	}

	// Create content
	@Test
	public void publishValidImageAssetExpectSuccess200() {
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
				// log().all().
				spec(get200ResponseSpec()).
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
		// log().all().
		spec(get200ResponseSpecUpload());
		contentCleanUp(nodeId);
	}

	/*
	 * // Create and upload image asset Invalid
	 * 
	 * //Create content
	 * 
	 * @Test public void uploadandPublishInValidImageAssetExpectSuccess200(){
	 * setURI(); JSONObject js = new JSONObject(jsonCreateValidContent);
	 * js.getJSONObject("request").getJSONObject("content").put("contentType",
	 * "Asset").put("mimeType", "image/jpeg"); String
	 * jsonCreateImageAssetInvalid = js.toString(); Response R = given().
	 * spec(getRequestSpecification(contentType, userId, APIToken)).
	 * body(jsonCreateImageAssetInvalid). with(). contentType(JSON). when().
	 * post("content/v3/create"). then(). //// log().all().
	 * spec(get200ResponseSpec()). extract(). response();
	 * 
	 * // Extracting the JSON path JsonPath jp = R.jsonPath(); String nodeId =
	 * jp.get("result.node_id");
	 * 
	 * // Upload Content setURI(); given().
	 * spec(getRequestSpecification(uploadContentType, userId, APIToken)).
	 * multiPart(new File(path+"/Verbs_test.zip")). when().
	 * post("/content/v3/upload/"+nodeId). then(). //// log().all().
	 * spec(get400ResponseSpec()); }
	 * 
	 * // Create and upload image asset Invalid
	 * 
	 * //Create content
	 * 
	 * @Test public void uploadandPublishPngInvalidImageAssetExpect4xx(){
	 * setURI(); JSONObject js = new JSONObject(jsonCreateValidContent);
	 * js.getJSONObject("request").getJSONObject("content").put("contentType",
	 * "Asset").put("mimeType", "image/jpeg"); String
	 * jsonCreateImageAssetInvalid = js.toString(); Response R = given().
	 * spec(getRequestSpecification(contentType, userId, APIToken)).
	 * body(jsonCreateImageAssetInvalid). with(). contentType(JSON). when().
	 * post("content/v3/create"). then(). //// log().all().
	 * spec(get200ResponseSpec()). extract(). response();
	 * 
	 * // Extracting the JSON path JsonPath jp = R.jsonPath(); String nodeId =
	 * jp.get("result.node_id");
	 * 
	 * // Upload Content setURI(); given().
	 * spec(getRequestSpecification(uploadContentType, userId, APIToken)).
	 * multiPart(new File(path+"/pngImage.png")). when().
	 * post("/content/v3/upload/"+nodeId). then(). //// log().all().
	 * spec(get400ResponseSpec()); }
	 */

	// Create and upload audio asset Valid

	// Create content
	@Test
	public void publishValidAudioAssetExpectSuccess200() {
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("contentType", "Asset").put("mimeType", "audio/mp3");
		String jsonCreateImageAssetInvalid = js.toString();
		Response R = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateImageAssetInvalid).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/sample.mp3")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Publish the created asset
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
		when().
		post("/content/v3/publish/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());
		contentCleanUp(nodeId);
	}

	/*
	 * // Create and upload Invalid audio asset
	 * 
	 * //Create content
	 * 
	 * @Test public void uploadandPublishInvalidAudioAssetExpect4xx(){ setURI();
	 * JSONObject js = new JSONObject(jsonCreateValidContent);
	 * js.getJSONObject("request").getJSONObject("content").put("contentType",
	 * "Asset").put("mimeType", "audio/mp3"); String jsonCreateImageAssetInvalid
	 * = js.toString(); Response R = given().
	 * spec(getRequestSpecification(contentType, userId, APIToken)).
	 * body(jsonCreateImageAssetInvalid). with(). contentType(JSON). when().
	 * post("content/v3/create"). then(). //// log().all().
	 * spec(get200ResponseSpec()). extract(). response();
	 * 
	 * // Extracting the JSON path JsonPath jp = R.jsonPath(); String nodeId =
	 * jp.get("result.node_id");
	 * 
	 * // Upload Content setURI(); given().
	 * spec(getRequestSpecification(uploadContentType, userId, APIToken)).
	 * multiPart(new File(path+"/pngImage.png")). when().
	 * post("/content/v3/upload/"+nodeId). then(). //// log().all().
	 * spec(get400ResponseSpec()); }
	 * 
	 * // Create and upload invalid mimeType audio asset
	 * 
	 * //Create content
	 * 
	 * @Test public void uploadandPublishInvalidmimeTypeAudioAssetExpect4xx(){
	 * setURI(); JSONObject js = new JSONObject(jsonCreateValidContent);
	 * js.getJSONObject("request").getJSONObject("content").put("contentType",
	 * "Asset").put("mimeType", "audio/mp3"); String jsonCreateImageAssetInvalid
	 * = js.toString(); Response R = given().
	 * spec(getRequestSpecification(contentType, userId, APIToken)).
	 * body(jsonCreateImageAssetInvalid). with(). contentType(JSON). when().
	 * post("content/v3/create"). then(). //// log().all().
	 * spec(get200ResponseSpec()). extract(). response();
	 * 
	 * // Extracting the JSON path JsonPath jp = R.jsonPath(); String nodeId =
	 * jp.get("result.node_id");
	 * 
	 * // Upload Content setURI(); given().
	 * spec(getRequestSpecification(uploadContentType, userId, APIToken)).
	 * multiPart(new File(path+"/Oggaudio.ogg")). when().
	 * post("/content/v3/upload/"+nodeId). then(). //// log().all().
	 * spec(get400ResponseSpec()); }
	 */

	// Upload valid content expect success

	// Upload valid content with special characters expect success

	// Create content
	@Test
	public void publishContentWithSpecialCharactersExpectSuccess200() {
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("name", ".TestContent!23.");
		String jsonCreateValidContentSpclChar = js.toString();
		Response R = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidContentSpclChar).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/uploadContent.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get body and validate

		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {
			Assert.assertTrue(accessURL(nodeId));
		}
		contentCleanUp(nodeId);
	}
	
	// Upload content with valid Ecml (With external JSON for item data, another controller with __cdata item data )

	// Create content
	@Test
	public void publishContentWithExternaJSONItemDataCDataExpectSuccess200() {
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
				// log().all().
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
		// log().all().
		spec(get200ResponseSpec());

		// Get body and validate

		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {
			Assert.assertTrue(accessURL(nodeId));
		}
		contentCleanUp(nodeId);
	}

	// Upload content with valid Ecml containing JSON item data

	// Create content
	@Test
	public void publishContentWithJSONItemDataExpectSuccess200() {
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
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/Item_json.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {
			Assert.assertTrue(accessURL(nodeId));
		}
		contentCleanUp(nodeId);
	}

	// Upload content with valid ECML containing data JSONs

	// Create content
	@Ignore
	public void publishContentWithDataJSONExpectSuccess200() {
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
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/Data_json_ecml.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			//// System.out.println(e);
		}
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {
			Assert.assertTrue(accessURL(nodeId));
		}
		contentCleanUp(nodeId);
	}

	// Upload Content with valid ECML containing custom plugin

	// Create content
	@Ignore
	public void publishContentWithCustomPluginExpectSuccess200() {
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
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/Custom_Plugin.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get body and validate
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			//// System.out.println(e);
		}
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {
			Assert.assertTrue(accessURL(nodeId));
		}
		contentCleanUp(nodeId);
	}

	// Upload Files with Tween animation, audio sprites and Image sprite

	// Create content
	@Test
	public void publishContentWithAudioImageSpriteTweenAnimationExpectSuccess200() {
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
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/tweenAndaudioSprite.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {
			Assert.assertTrue(accessURL(nodeId));
		}
		contentCleanUp(nodeId);
	}

	// Upload File with valid JSON ecml
	// Create content
	@Test
	public void publishContentWithJSONEcmlExpectSuccess200() {
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
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/ecml_with_json.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {
			Assert.assertTrue(accessURL(nodeId));
		}
		contentCleanUp(nodeId);
	}

	// Upload File with valid JSON ecml
	// Create content
	@Test
	public void publishContentWithoutAssetsExpectSuccess200() {
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
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/Ecml_without_asset.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {
			Assert.assertTrue(accessURL(nodeId));
		}
		contentCleanUp(nodeId);
	}

	// Publish content and upload zip and validate

	@Test
	public void publishContentNewZipExpectSuccess200() {
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
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/uploadContent.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {
			
			// Publish created content
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/" + nodeId).
			then().
			// log().all().
			spec(get200ResponseSpec());		
		}

		// Upload Content
		setURI();
		try{Thread.sleep(5000);}catch(InterruptedException e){ System.out.println(e);} 
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/tweenAndaudioSprite.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());
		
		// Publish created content
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
		when().
		post("/content/v3/publish/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		try{Thread.sleep(5000);}catch(InterruptedException e){ System.out.println(e);} 
		Response R3 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP3 = R3.jsonPath();
		String bodyNew = jP3.get("result.content.body");
		if (isValidXML(bodyNew) || isValidJSON(bodyNew)) {
			Assert.assertTrue(body != bodyNew);
		}
		contentCleanUp(nodeId);
	}

	// Upload multiple files

	// Create content
	@Test
	public void publishContentMultipleExpectSuccess200() {
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
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/uploadContent.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/uploadContent.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {
			Assert.assertTrue(accessURL(nodeId));
		}
		contentCleanUp(nodeId);
	}

	// Create, update and publish content

	// Create content
	@Ignore
	public void publishATContentExpectSuccess200() {
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
				// log().all().
				// spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");
		String versionKey = jp.get("result.versionKey");

		// Update content body
		setURI();
		jsonUpdateATContentBody = jsonUpdateATContentBody.replace("version_Key", versionKey);
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonUpdateATContentBody).
		with().
		contentType(JSON).
		then().
		patch("/content/v3/update/" + nodeId);
		// then().
		//// log().all().
		// spec(get200ResponseSpec());

		// Publish created content
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
		when().
		post("/content/v3/publish/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get content and validate
		for (int i = 1000; i <= 5000; i = i + 1000) {
			try {
				Thread.sleep(i);
			} catch (InterruptedException e) {
				//// System.out.println(e);
			}
			setURI();
			Response R3 = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/" + nodeId).
					then().
					// log().all().
					spec(get200ResponseSpec()).
					extract().response();

			// Validate the response
			JsonPath jp3 = R3.jsonPath();
			String statusUpdated = jp3.get("result.content.status");
			//// System.out.println(statusUpdated);
			if (statusUpdated.equals(PROCESSING) || statusUpdated.equals(PENDING)) {
				i = i + 1000;
			}
			if (statusUpdated.equals("Live")) {
				break;
			}
		}
		contentCleanUp(nodeId);
	}

	// Create, upload and publish worksheet

	// Create content
	@Test
	public void publishWorksheetExpectSuccess200() {
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("contentType", "Worksheet");
		String jsonCreateValidWorksheet = js.toString();
		Response R = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidWorksheet).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/Akshara_worksheet.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {
			Assert.assertTrue(accessURL(nodeId));
		}
		contentCleanUp(nodeId);
	}

	// Create, upload, publish and validate HTML Content

	// Create content
	@Test
	public void publishHTMLContentExpectSuccess200() {
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("mimeType", "application/vnd.ekstep.html-archive");
		String jsonCreateValidContentHtml = js.toString();
		Response R =
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidContentHtml).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/uploadHtml.zip")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {
			Assert.assertTrue(accessURL(nodeId));
		}
		contentCleanUp(nodeId);
	}

	// Create, upload, publish and validate APK Content

	// Create content
	@Test
	public void publishAPKContentExpectSuccess200() {
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("osId", "org.sunbird.aser").put("mimeType","application/vnd.android.package-archive");
		String jsonCreateValidContentAPK = js.toString();
		Response R = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidContentAPK).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path + "/uploadAPK.apk")).
		when().
		post("/content/v3/upload/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {
			Assert.assertTrue(accessURL(nodeId));
		}
		contentCleanUp(nodeId);
	}

	// Create, upload, publish and validate valid collection
	@Test
	public void publishValidCollectionExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		int count = 1;
		while (count <= 2) {
			setURI();
			int rn = generateRandomInt(9999, 1999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFTT_" + rn + "").put("name","LP_NFTT-" + rn + "");
			String jsonCreateValidChild = js.toString();
			Response R = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					body(jsonCreateValidChild).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					// log().all().
					spec(get200ResponseSpec()).
					extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpecification(uploadContentType, userId, APIToken)).
				multiPart(new File(path + "/uploadContent.zip")).
				when().
				post("/content/v3/upload/" + node1).
				then().
				// log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
				when().
				post("/content/v3/publish/" + node1).
				then().
				// log().all().
				spec(get200ResponseSpec());

			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpecification(uploadContentType, userId, APIToken)).
				multiPart(new File(path + "/tweenAndaudioSprite.zip")).
				when().
				post("/content/v3/upload/" + node2).
				then().
				// log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
				when().
				post("/content/v3/publish/" + node2).
				then().
				// log().all().
				spec(get200ResponseSpec());
			}
			count++;
		}
		// Create collection
		setURI();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
		Response R1 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateContentCollection).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");

		// Publish collection
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
		when().
		post("/content/v3/publish/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId).
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		// Validate the response
		JsonPath jp2 = R2.jsonPath();
		String status = jp2.get("result.content.status");
		String c_identifier = jp2.get("result.content.identifier");
		// String downloadUrl = jp2.get("result.content.downloadUrl");
		ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
		asyncPublishValidations(identifier1, status, nodeId, c_identifier, node1, node2);
		contentCleanUp(nodeId);
	}

	// Create, upload, publish and validate valid collection
	@Test
	public void publishCollectionWithRetiredContentExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		int count = 1;
		while (count <= 2) {
			setURI();
			int rn = generateRandomInt(9999, 1999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFTT_" + rn + "").put("name","LP_NFTT-" + rn + "");
			String jsonCreateValidChild = js.toString();
			Response R = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					body(jsonCreateValidChild).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpecification(uploadContentType, userId, APIToken)).
				multiPart(new File(path + "/uploadContent.zip")).
				when().
				post("/content/v3/upload/" + node1).
				then().
				// log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
				when().
				post("/content/v3/publish/" + node1).
				then().
				// log().all().
				spec(get200ResponseSpec());
			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpecification(uploadContentType, userId, APIToken)).
				multiPart(new File(path + "/tweenAndaudioSprite.zip")).
				when().
				post("/content/v3/upload/" + node2).
				then().
				// log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node2).then().extract().response();

				JsonPath jp1 = R1.jsonPath();
				String versionKey = jp1.get("result.versionKey");

				// Update status as Retired
				setURI();
				jsonUpdateContentValid = jsonUpdateContentValid.replace("Live", "Retired").replace("version_Key",
						versionKey);
				//// System.out.println(jsonUpdateContentValid);
				given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonUpdateContentValid).with()
						.contentType("application/json").then().
						// log().all().
						patch("/content/v3/update/" + node2);

			}
			count++;
		}
		// Create collection
		setURI();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
		Response R1 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateContentCollection).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");

		// Publish collection
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
		when().
		post("/content/v3/publish/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId).
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		// Validate the response
		JsonPath jp2 = R2.jsonPath();
		String status = jp2.get("result.content.status");
		String c_identifier = jp2.get("result.content.identifier");
		// String downloadUrl = jp2.get("result.content.downloadUrl");
		ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
		asyncPublishValidations(identifier1, status, nodeId, c_identifier, node1, node2);
		contentCleanUp(nodeId);
	}

	// Create, upload and publish collection of different mimeTypes

	@Test
	public void publishCollectionDiffMimeTypesExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		int count = 1;
		while (count <= 2) {
			setURI();
			int rn = generateRandomInt(9999, 1999999);
			if (count == 1) {
				JSONObject js = new JSONObject(jsonCreateValidContent);
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFTT_" + rn + "")
						.put("name", "LP_NFTT-" + rn + "").put("mimeType", "application/vnd.ekstep.html-archive");
				String jsonCreateValidChild = js.toString();
				Response R = given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body(jsonCreateValidChild).with().contentType(JSON).when().post("content/v3/create").then().
						// log().all().
						spec(get200ResponseSpec()).extract().response();

				// Extracting the JSON path
				JsonPath jp = R.jsonPath();
				String nodeId = jp.get("result.node_id");
				node1 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/uploadHtml.zip")).when().post("/content/v3/upload/" + node1).then()
						.
						// log().all().
						spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node1).then().
						// log().all().
						spec(get200ResponseSpec());

			}
			if (count == 2) {
				JSONObject js = new JSONObject(jsonCreateValidContent);
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFTT_" + rn + "")
						.put("name", "LP_NFTT-" + rn + "").put("osId", "org.sunbird.aser")
						.put("mimeType", "application/vnd.android.package-archive");
				String jsonCreateValidChild = js.toString();
				Response R = given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body(jsonCreateValidChild).with().contentType(JSON).when().post("content/v3/create").then().
						// log().all().
						spec(get200ResponseSpec()).extract().response();

				// Extracting the JSON path
				JsonPath jp = R.jsonPath();
				String nodeId = jp.get("result.node_id");
				node2 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/uploadAPK.apk")).when().post("/content/v3/upload/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			count++;
		}
		// Create collection
		setURI();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateContentCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");

		// Publish collection
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp2 = R2.jsonPath();
		String status = jp2.get("result.content.status");
		String c_identifier = jp2.get("result.content.identifier");
		// String downloadUrl = jp2.get("result.content.downloadUrl");
		ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
		asyncPublishValidations(identifier1, status, nodeId, c_identifier, node1, node2);
		contentCleanUp(nodeId);
	}

	// Create, upload, publish and validate valid collection with contents
	// created from authoring tool
	@Ignore
	public void publishValidCollectionWithATContentsExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		int count = 1;
		while (count <= 2) {
			setURI();
			int rn = generateRandomInt(1999, 999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFTT_" + rn + "").put("name",
					"LP_NFTT-" + rn + "");
			String jsonCreateValidChild = js.toString();
			Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidChild)
					.with().contentType(JSON).when().post("content/v3/create").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node1)
						.then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node1).then().
						// log().all().
						spec(get200ResponseSpec());

			}
			if (count == 2) {
				node2 = nodeId;

				// Get node_id
				JsonPath jP = R.jsonPath();
				String versionKey = jP.get("result.versionKey");

				// Update content body
				setURI();
				jsonUpdateATContentBody = jsonUpdateATContentBody.replace("version_Key", versionKey);
				given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonUpdateATContentBody)
						.with().contentType("application/json").when().patch("/content/v3/update/" + nodeId).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			count++;
		}
		// Create collection
		setURI();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateContentCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");

		// Publish collection
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp2 = R2.jsonPath();
		String status = jp2.get("result.content.status");
		//// System.out.println(status);
		String c_identifier = jp2.get("result.content.identifier");
		ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
		asyncPublishValidations(identifier1, status, nodeId, c_identifier, node1, node2);
		contentCleanUp(nodeId);
	}

	// Publish collection with live and retired content
	@Test
	public void publishCollectionWithLiveandRetiredContentExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		int count = 1;
		while (count <= 2) {
			setURI();
			int rn = generateRandomInt(500, 99999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_" + rn + "").put("name",
					"LP_NFT_T-" + rn + "");
			String jsonCreateValidChild = js.toString();
			Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidChild)
					.with().contentType(JSON).when().post("content/v3/create").then().
					// log().all().
					extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/uploadContent.zip")).then().post("/content/v3/upload/" + node1);

				// Publish created content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").then()
						.post("/content/v3/publish/" + node1);

			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/ExternalJsonItemDataCdata.zip")).then()
						.post("/content/v3/upload/" + node2);

				// Publish created content
				setURI();
				Response R1 = given().
						spec(getRequestSpecification(contentType, userId, APIToken)).
						body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
						when().
						post("/content/v3/publish/" + node2).
						then().
						// log().all().
						extract().response();

				JsonPath jp1 = R1.jsonPath();
				//String versionKey = jp1.get("result.versionKey");

				// Update status as Retired
				setURI();
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				delete("/content/v3/retire/" + node2).
				then().
				// log().all().
				spec(get200ResponseSpec());
			}
			count++;
		}
		// Create collection
		setURI();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateContentCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				extract().response();

		// Get collection and validate
		JsonPath jp1 = R1.jsonPath();
		String collectionNode = jp1.get("result.node_id");

		// Publish created content
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + collectionNode).
				then().
				// log().all().
				spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + collectionNode).then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp2 = R2.jsonPath();
		String status = jp2.get("result.content.status");
		String c_identifier = jp2.get("result.content.identifier");
		// String downloadUrl = jp2.get("result.content.downloadUrl");
		ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
		asyncPublishValidations(identifier1, status, collectionNode, c_identifier, node1, node2);
		contentCleanUp(collectionNode);
	}

	// Create upload and publish collection and remove children
	@Ignore
	@Test
	public void publishandRemoveChildrenCollectionExpect4xx() {
		String node1 = null;
		String node2 = null;
		int count = 1;
		while (count <= 2) {
			setURI();
			int rn = generateRandomInt(1999, 1999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFTT_" + rn + "").put("name",
					"LP_NFTT-" + rn + "");
			String jsonCreateValidChild = js.toString();
			Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidChild)
					.with().contentType(JSON).when().post("content/v3/create").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId1 = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId1;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node1)
						.then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node1).then().
						// log().all().
						spec(get200ResponseSpec());

			}
			if (count == 2) {
				node2 = nodeId1;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			count++;
		}
		// Create collection
		setURI();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateContentCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");

		// Publish collection
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP2 = R2.jsonPath();
		String versionKey = jP2.get("result.versionKey");

		// Update the children content
		setURI();
		jsonUpdateChildren = jsonUpdateChildren.replace("[]", "[{\"identifier\":\"" + node2 + "\"}]")
				.replace("version_Key", versionKey);
		given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonUpdateChildren).when()
				.patch("/content/v3/update/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec());
			contentCleanUp(nodeId);
	}

	// Create, upload and publish nested collection
	@Test
	public void publishNestedCollectionExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		int count = 1;
		while (count <= 2) {
			setURI();
			int rn = generateRandomInt(900, 19999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFTT_" + rn + "").put("name",
					"LP_NFTT-" + rn + "");
			String jsonCreateValidChild = js.toString();
			Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidChild)
					.with().contentType(JSON).when().post("content/v3/create").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId1 = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId1;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node1)
						.then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node1).then().
						// log().all().
						spec(get200ResponseSpec());

			}
			if (count == 2) {
				node2 = nodeId1;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			count++;
		}
		// Create collection
		setURI();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateContentCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");

		// Publish collection
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp2 = R2.jsonPath();
		String status = jp2.get("result.content.status");
		String c_identifier = jp2.get("result.content.identifier");
		// String downloadUrl = jp2.get("result.content.downloadUrl");
		ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
		asyncPublishValidations(identifier1, status, nodeId, c_identifier, node1, node2);

		// Create nested collection
		setURI();
		jsonCreateNestedCollection = jsonCreateNestedCollection.replace("id1", nodeId);
		Response R3 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateNestedCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP3 = R3.jsonPath();
		String collectionId = jP3.get("result.node_id");

		// Publish collection
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + collectionId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R4 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + collectionId).then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp4 = R4.jsonPath();
		String n_status = jp4.get("result.content.status");
		String n_identifier = jp4.get("result.content.identifier");
		ArrayList<String> n_identifier1 = jp4.get("result.content.children.identifier");
		Assert.assertTrue(n_status.equals("Live") || n_status.equals(PROCESSING) || n_status.equals(PENDING) && n_identifier.equals(collectionId)
				&& n_identifier1.contains(nodeId));
		contentCleanUp(collectionId);
		}

	// Publish Text book with draft children with visibility - Default
	@Test
	public void publishTextBookDraftChildrenExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		int count = 1;
		while (count <= 2) {
			setURI();
			int rn = generateRandomInt(9999, 1999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFTT_" + rn + "").put("name",
					"LP_NFTT-" + rn + "");
			String jsonCreateValidChild = js.toString();
			Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidChild)
					.with().contentType(JSON).when().post("content/v3/create").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node1)
						.then().
						// log().all().
						spec(get200ResponseSpec());

			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			count++;
		}
		// Create Textbook
		setURI();
		JSONObject js = new JSONObject(jsonCreateContentCollection);
		js.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
		String jsonCreateContentCollection = js.toString();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateContentCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");

		// Publish textbook
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp2 = R2.jsonPath();
		String status = jp2.get("result.content.status");
		String c_identifier = jp2.get("result.content.identifier");
		// String downloadUrl = jp2.get("result.content.downloadUrl");
		ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
		asyncPublishValidations(identifier1, status, nodeId, c_identifier, node1, node2);
		contentCleanUp(nodeId);
	}

	// Publish Text book with draft children with visibility - Parent
	@Test
	public void publishParentTextBookDraftChildrenExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		int count = 1;
		while (count <= 2) {
			setURI();
			int rn = generateRandomInt(9999, 1999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent")
					.put("identifier", "LP_NFTT_" + rn + "").put("name", "LP_NFTT-" + rn + "");
			String jsonCreateValidChild = js.toString();
			Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidChild)
					.with().contentType(JSON).when().post("content/v3/create").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node1)
						.then().
						// log().all().
						spec(get200ResponseSpec());
			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			count++;
		}
		// Create Textbook
		setURI();
		JSONObject js = new JSONObject(jsonCreateContentCollection);
		js.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
		String jsonCreateContentCollection = js.toString();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateContentCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");

		// Publish textbook
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp2 = R2.jsonPath();
		String status = jp2.get("result.content.status");
		String c_identifier = jp2.get("result.content.identifier");
		// String downloadUrl = jp2.get("result.content.downloadUrl");
		ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
		asyncPublishValidations(identifier1, status, nodeId, c_identifier, node1, node2);
		contentCleanUp(nodeId);
	}

	// Publish Text book with live and draft children with visibility - Parent
	@Test
	public void publishParentTextBookLiveDraftChildrenExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		int count = 1;
		while (count <= 2) {
			setURI();
			int rn = generateRandomInt(9999, 1999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent")
					.put("identifier", "LP_NFTT_" + rn + "").put("name", "LP_NFTT-" + rn + "");
			String jsonCreateValidChild = js.toString();
			Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidChild)
					.with().contentType(JSON).when().post("content/v3/create").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node1)
						.then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node1).then().
						// log().all().
						spec(get200ResponseSpec());

			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			count++;
		}
		// Create Textbook
		setURI();
		JSONObject js = new JSONObject(jsonCreateContentCollection);
		js.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
		String jsonCreateContentCollection = js.toString();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateContentCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");

		// Publish textbook
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp2 = R2.jsonPath();
		String status = jp2.get("result.content.status");
		String c_identifier = jp2.get("result.content.identifier");
		// String downloadUrl = jp2.get("result.content.downloadUrl");
		ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
		asyncPublishValidations(identifier1, status, nodeId, c_identifier, node1, node2);
		contentCleanUp(nodeId);
	}

	// Publish Text book with Live children with visibility - Default
	@Test
	public void publishTextBookLiveChildrenExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		int count = 1;
		while (count <= 2) {
			setURI();
			int rn = generateRandomInt(9999, 1999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFTT_" + rn + "").put("name",
					"LP_NFTT-" + rn + "");
			String jsonCreateValidChild = js.toString();
			Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidChild)
					.with().contentType(JSON).when().post("content/v3/create").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node1)
						.then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node1).then().
						// log().all().
						spec(get200ResponseSpec());

			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());

			}
			count++;
		}
		// Create Textbook
		setURI();
		JSONObject js = new JSONObject(jsonCreateContentCollection);
		js.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
		String jsonCreateContentCollection = js.toString();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateContentCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");

		// Publish textbook
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp2 = R2.jsonPath();
		String status = jp2.get("result.content.status");
		String c_identifier = jp2.get("result.content.identifier");
		// String downloadUrl = jp2.get("result.content.downloadUrl");
		ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
		asyncPublishValidations(identifier1, status, nodeId, c_identifier, node1, node2);
		contentCleanUp(nodeId);
	}

	// Publish Text book with Live and Draft children with visibility - Default
	@Test
	public void publishTextBookLiveDraftChildrenExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		int count = 1;
		while (count <= 2) {
			setURI();
			int rn = generateRandomInt(9999, 1999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFTT_" + rn + "").put("name",
					"LP_NFTT-" + rn + "");
			String jsonCreateValidChild = js.toString();
			Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidChild)
					.with().contentType(JSON).when().post("content/v3/create").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node1)
						.then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node1).then().
						// log().all().
						spec(get200ResponseSpec());

			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			count++;
		}

		// Create Textbook
		setURI();
		JSONObject js = new JSONObject(jsonCreateContentCollection);
		js.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
		String jsonCreateContentCollection = js.toString();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateContentCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");

		// Publish textbook
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp2 = R2.jsonPath();
		String status = jp2.get("result.content.status");
		String c_identifier = jp2.get("result.content.identifier");
		// String downloadUrl = jp2.get("result.content.downloadUrl");
		ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
		asyncPublishValidations(identifier1, status, nodeId, c_identifier, node1, node2);
		contentCleanUp(nodeId);
	}

	// Publish Text book with Live children with visibility - Parent
	@Test
	public void publishParentTextBookLiveChildrenExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		int count = 1;
		while (count <= 2) {
			setURI();
			int rn = generateRandomInt(9999, 1999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent")
					.put("identifier", "LP_NFTT_" + rn + "").put("name", "LP_NFTT-" + rn + "");
			String jsonCreateValidChild = js.toString();
			Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidChild)
					.with().contentType(JSON).when().post("content/v3/create").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node1)
						.then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node1).then().
						// log().all().
						spec(get200ResponseSpec());

			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());

			}
			count++;
		}
		// Create Textbook
		setURI();
		JSONObject js = new JSONObject(jsonCreateContentCollection);
		js.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
		String jsonCreateContentCollection = js.toString();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateContentCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");

		// Publish textbook
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp2 = R2.jsonPath();
		String status = jp2.get("result.content.status");
		String c_identifier = jp2.get("result.content.identifier");
		// String downloadUrl = jp2.get("result.content.downloadUrl");
		ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
		asyncPublishValidations(identifier1, status, nodeId, c_identifier, node1, node2);
		contentCleanUp(nodeId);
	}

	// Publish Text book with Live and Retired children with visibility - Parent
	@Test
	public void publishTextBookLiveandRetiredChildrenExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		int count = 1;
		while (count <= 2) {
			setURI();
			int rn = generateRandomInt(9999, 1999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent")
					.put("identifier", "LP_NFTT_" + rn + "").put("name", "LP_NFTT-" + rn + "");
			String jsonCreateValidChild = js.toString();
			Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidChild)
					.with().contentType(JSON).when().post("content/v3/create").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node1)
						.then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node1).then().
						// log().all().
						spec(get200ResponseSpec());

			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish created content
				setURI();
				Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node2).then().extract().response();

				JsonPath jp1 = R1.jsonPath();
				//String versionKey = jp1.get("result.versionKey");

				// Update status as Retired
				setURI();
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				delete("/content/v3/retire/" + node2).
				then().
				spec(get200ResponseSpec());
			}
			count++;
		}
		// Create Textbook
		setURI();
		JSONObject js = new JSONObject(jsonCreateContentCollection);
		js.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
		String jsonCreateContentCollection = js.toString();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateContentCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");

		// Publish textbook
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp2 = R2.jsonPath();
		String status = jp2.get("result.content.status");
		String c_identifier = jp2.get("result.content.identifier");
		// String downloadUrl = jp2.get("result.content.downloadUrl");
		ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
		asyncPublishValidations(identifier1, status, nodeId, c_identifier, node1, node2);
		contentCleanUp(nodeId);
	}

	// Create, upload and publish nested textbook with collection as child
	@Test
	public void publishNestedTextBookWithCollectionExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		String node3 = null;
		String node4 = null;
		int count = 1;
		while (count <= 4) {
			setURI();
			int rn = generateRandomInt(9999, 1999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			if (count == 1) {
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_" + rn + "")
						.put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 2) {
				js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent")
						.put("identifier", "LP_NFT_T_" + rn + "").put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 3) {
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_" + rn + "")
						.put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 4) {
				js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent")
						.put("identifier", "LP_NFT_T_" + rn + "").put("name", "LP_NFT_T-" + rn + "");
			}
			String jsonCreateValidChild = js.toString();
			Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidChild)
					.with().contentType(JSON).when().post("content/v3/create").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node1)
						.then().
						// log().all().
						spec(get200ResponseSpec());

			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			if (count == 3) {
				node3 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node3).then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node3).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			if (count == 4) {
				node4 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node4).then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node4).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			count++;
		}
		// Create TextbookUnit
		setURI();
		JSONObject js2 = new JSONObject(jsonCreateTextbookUnit);
		js2.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("contentType",
				"collection");
		String jsonCreateTextbookUnit = js2.toString();
		jsonCreateTextbookUnit = jsonCreateTextbookUnit.replace("id1", node1).replace("id2", node2)
				.replace("id3", node3).replace("id4", node4);
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateTextbookUnit)
				.with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");

		// Create Textbook
		setURI();
		JSONObject js1 = new JSONObject(jsonCreateNestedCollection);
		js1.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
		String jsonCreateNestedCollection = js1.toString();
		jsonCreateNestedCollection = jsonCreateNestedCollection.replace("id1", nodeId);
		Response R3 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateNestedCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP3 = R3.jsonPath();
		String textBookId = jP3.get("result.node_id");

		// Publish textbook
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + textBookId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R4 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + textBookId).then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp4 = R4.jsonPath();
		String n_status = jp4.get("result.content.status");
		String n_identifier = jp4.get("result.content.identifier");
		ArrayList<String> n_identifier1 = jp4.get("result.content.children.identifier");
		Assert.assertTrue(n_status.equals("Live")
				|| n_status.equals(PROCESSING) || n_status.equals(PENDING) && n_identifier.equals(textBookId) && n_identifier1.contains(nodeId));
		contentCleanUp(textBookId);
	}

	// Create, upload and publish nested textbook with draft textbook unit and visibility-Parent
	@Test
	public void publishNestedTextBookDraftTBUnitParentExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		String node3 = null;
		String node4 = null;
		int count = 1;
		while (count <= 4) {
			setURI();
			int rn = generateRandomInt(9999, 1999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			if (count == 1) {
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_" + rn + "")
						.put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 2) {
				js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent")
						.put("identifier", "LP_NFT_T_" + rn + "").put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 3) {
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_" + rn + "")
						.put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 4) {
				js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent")
						.put("identifier", "LP_NFT_T_" + rn + "").put("name", "LP_NFT_T-" + rn + "");
			}
			String jsonCreateValidChild = js.toString();
			Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidChild)
					.with().contentType(JSON).when().post("content/v3/create").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node1)
						.then().
						// log().all().
						spec(get200ResponseSpec());

			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node2)
						.then().
						// log().all().
						spec(get200ResponseSpec());
			}
			if (count == 3) {
				node3 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node3)
						.then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node3).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			if (count == 4) {
				node4 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node4).then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node4).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			count++;
		}
		// Create TextbookUnit
		setURI();
		JSONObject js2 = new JSONObject(jsonCreateTextbookUnit);
		js2.getJSONObject("request").getJSONObject("content").put("visibility", "Parent");
		String jsonCreateTextbookUnit = js2.toString();
		jsonCreateTextbookUnit = jsonCreateTextbookUnit.replace("id1", node1).replace("id2", node2)
				.replace("id3", node3).replace("id4", node4);
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateTextbookUnit)
				.with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");

		// Create Textbook
		setURI();
		JSONObject js1 = new JSONObject(jsonCreateNestedCollection);
		js1.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
		String jsonCreateNestedCollection = js1.toString();
		jsonCreateNestedCollection = jsonCreateNestedCollection.replace("id1", nodeId);
		Response R3 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateNestedCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP3 = R3.jsonPath();
		String textBookId = jP3.get("result.node_id");

		// Publish textbook
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + textBookId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R4 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + textBookId).then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp4 = R4.jsonPath();
		String n_status = jp4.get("result.content.status");
		String n_identifier = jp4.get("result.content.identifier");
		ArrayList<String> n_identifier1 = jp4.get("result.content.children.identifier");
		Assert.assertTrue(n_status.equals("Live")
				|| n_status.equals(PROCESSING) || n_status.equals(PENDING) && n_identifier.equals(textBookId) && n_identifier1.contains(nodeId));
		contentCleanUp(textBookId);
	}

	// Create, upload and publish nested textbook with draft textbook unit and visibility-Default
	@Test
	public void publishNestedTextBookDraftTBUnitDefaultExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		String node3 = null;
		String node4 = null;
		int count = 1;
		while (count <= 4) {
			setURI();
			int rn = generateRandomInt(9999, 1999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			if (count == 1) {
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_" + rn + "")
						.put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 2) {
				js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent")
						.put("identifier", "LP_NFT_T_" + rn + "").put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 3) {
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_" + rn + "")
						.put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 4) {
				js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent")
						.put("identifier", "LP_NFT_T_" + rn + "").put("name", "LP_NFT_T-" + rn + "");
			}
			String jsonCreateValidChild = js.toString();
			Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidChild)
					.with().contentType(JSON).when().post("content/v3/create").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node1)
						.then().
						// log().all().
						spec(get200ResponseSpec());

			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			if (count == 3) {
				node3 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node3).then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node3).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			if (count == 4) {
				node4 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node4).then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node4).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			count++;
		}
		// Create TextbookUnit
		setURI();
		jsonCreateTextbookUnit = jsonCreateTextbookUnit.replace("id1", node1).replace("id2", node2)
				.replace("id3", node3).replace("id4", node4);
		//// System.out.println(jsonCreateTextbookUnit);
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateTextbookUnit)
				.with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");

		// Create Textbook
		setURI();
		JSONObject js1 = new JSONObject(jsonCreateNestedCollection);
		js1.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
		String jsonCreateNestedCollection = js1.toString();
		jsonCreateNestedCollection = jsonCreateNestedCollection.replace("id1", nodeId);
		//// System.out.println(jsonCreateNestedCollection);
		Response R3 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateNestedCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP3 = R3.jsonPath();
		String textBookId = jP3.get("result.node_id");

		// Publish textbook
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + textBookId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R4 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + textBookId).then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp4 = R4.jsonPath();
		String n_status = jp4.get("result.content.status");
		String n_identifier = jp4.get("result.content.identifier");
		ArrayList<String> n_identifier1 = jp4.get("result.content.children.identifier");
		Assert.assertTrue(n_status.equals("Live")
				|| n_status.equals(PROCESSING) || n_status.equals(PENDING) && n_identifier.equals(textBookId) && n_identifier1.contains(nodeId));
		contentCleanUp(textBookId);
	}

	// Create, upload and publish nested textbook with Live textbook unit and visibility-Parent
	@Test
	public void publishNestedTextBookLiveTBUnitParentExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		String node3 = null;
		String node4 = null;
		int count = 1;
		while (count <= 4) {
			setURI();
			int rn = generateRandomInt(9999, 1999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			if (count == 1) {
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_" + rn + "")
						.put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 2) {
				js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent")
						.put("identifier", "LP_NFT_T_" + rn + "").put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 3) {
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_" + rn + "")
						.put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 4) {
				js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent")
						.put("identifier", "LP_NFT_T_" + rn + "").put("name", "LP_NFT_T-" + rn + "");
			}
			String jsonCreateValidChild = js.toString();
			Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidChild)
					.with().contentType(JSON).when().post("content/v3/create").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node1)
						.then().
						// log().all().
						spec(get200ResponseSpec());

			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			if (count == 3) {
				node3 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node3).then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node3).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			if (count == 4) {
				node4 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node4).then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish content 4
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node4).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			count++;
		}
		// Create TextbookUnit
		setURI();
		JSONObject js2 = new JSONObject(jsonCreateTextbookUnit);
		js2.getJSONObject("request").getJSONObject("content").put("visibility", "Parent");
		String jsonCreateTextbookUnit = js2.toString();
		jsonCreateTextbookUnit = jsonCreateTextbookUnit.replace("id1", node1).replace("id2", node2)
				.replace("id3", node3).replace("id4", node4);
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateTextbookUnit)
				.with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");

		// Publish Textbook unit
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Create Textbook
		setURI();
		JSONObject js1 = new JSONObject(jsonCreateNestedCollection);
		js1.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
		String jsonCreateNestedCollection = js1.toString();
		jsonCreateNestedCollection = jsonCreateNestedCollection.replace("id1", nodeId);
		Response R3 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateNestedCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP3 = R3.jsonPath();
		String textBookId = jP3.get("result.node_id");

		// Publish textbook
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + textBookId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R4 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + textBookId).then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp4 = R4.jsonPath();
		String n_status = jp4.get("result.content.status");
		String n_identifier = jp4.get("result.content.identifier");
		ArrayList<String> n_identifier1 = jp4.get("result.content.children.identifier");
		Assert.assertTrue(n_status.equals("Live")
				|| n_status.equals(PROCESSING) || n_status.equals(PENDING) && n_identifier.equals(textBookId) && n_identifier1.contains(nodeId));
		contentCleanUp(textBookId);
	}

	// Create, upload and publish nested textbook with Live textbook unit and visibility-Parent
	@Test
	public void publishNestedTextBookLiveTBUnitDefaultExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		String node3 = null;
		String node4 = null;
		int count = 1;
		while (count <= 4) {
			setURI();
			int rn = generateRandomInt(9999, 1999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			if (count == 1) {
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_" + rn + "")
						.put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 2) {
				js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent")
						.put("identifier", "LP_NFT_T_" + rn + "").put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 3) {
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_" + rn + "")
						.put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 4) {
				js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent")
						.put("identifier", "LP_NFT_T_" + rn + "").put("name", "LP_NFT_T-" + rn + "");
			}
			String jsonCreateValidChild = js.toString();
			Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidChild)
					.with().contentType(JSON).when().post("content/v3/create").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node1)
						.then().
						// log().all().
						spec(get200ResponseSpec());

			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			if (count == 3) {
				node3 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node3).then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node3).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			if (count == 4) {
				node4 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node4).then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node4).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			count++;
		}
		// Create TextbookUnit
		setURI();
		jsonCreateTextbookUnit = jsonCreateTextbookUnit.replace("id1", node1).replace("id2", node2)
				.replace("id3", node3).replace("id4", node4);
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateTextbookUnit)
				.with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");

		// Publish Textbook unit
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Create Textbook
		setURI();
		JSONObject js1 = new JSONObject(jsonCreateNestedCollection);
		js1.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
		String jsonCreateNestedCollection = js1.toString();
		jsonCreateNestedCollection = jsonCreateNestedCollection.replace("id1", nodeId);
		Response R3 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateNestedCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP3 = R3.jsonPath();
		String textBookId = jP3.get("result.node_id");

		// Publish textbook
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + textBookId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R4 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + textBookId).then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp4 = R4.jsonPath();
		String n_status = jp4.get("result.content.status");
		String n_identifier = jp4.get("result.content.identifier");
		ArrayList<String> n_identifier1 = jp4.get("result.content.children.identifier");
		Assert.assertTrue(n_status.equals("Live")
				|| n_status.equals(PROCESSING) || n_status.equals(PENDING) && n_identifier.equals(textBookId) && n_identifier1.contains(nodeId));
		contentCleanUp(textBookId);
	}

	// Create, upload and publish nested textbook with Draft textbook unit and visibility-Default with draft children
	@Test
	public void publishNestedTextBookDraftTBUnitDraftChildrenExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		String node3 = null;
		String node4 = null;
		int count = 1;
		while (count <= 4) {
			setURI();
			int rn = generateRandomInt(9999, 1999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			if (count == 1) {
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_" + rn + "")
						.put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 2) {
				js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent")
						.put("identifier", "LP_NFT_T_" + rn + "").put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 3) {
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_" + rn + "")
						.put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 4) {
				js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent")
						.put("identifier", "LP_NFT_T_" + rn + "").put("name", "LP_NFT_T-" + rn + "");
			}
			String jsonCreateValidChild = js.toString();
			Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidChild)
					.with().contentType(JSON).when().post("content/v3/create").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node1)
						.then().
						// log().all().
						spec(get200ResponseSpec());

			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			if (count == 3) {
				node3 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node3).then().
						// log().all().
						spec(get200ResponseSpec());

			}
			if (count == 4) {
				node4 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node4).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			count++;
		}
		// Create TextbookUnit
		setURI();
		jsonCreateTextbookUnit = jsonCreateTextbookUnit.replace("id1", node1).replace("id2", node2)
				.replace("id3", node3).replace("id4", node4);
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateTextbookUnit)
				.with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");

		// Create Textbook
		setURI();
		JSONObject js1 = new JSONObject(jsonCreateNestedCollection);
		js1.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
		String jsonCreateNestedCollection = js1.toString();
		jsonCreateNestedCollection = jsonCreateNestedCollection.replace("id1", nodeId);
		Response R3 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateNestedCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP3 = R3.jsonPath();
		String textBookId = jP3.get("result.node_id");

		// Publish textbook
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + textBookId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R4 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + textBookId).then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp4 = R4.jsonPath();
		String n_status = jp4.get("result.content.status");
		String n_identifier = jp4.get("result.content.identifier");
		ArrayList<String> n_identifier1 = jp4.get("result.content.children.identifier");
		Assert.assertTrue(n_status.equals("Live")
				|| n_status.equals(PROCESSING) || n_status.equals(PENDING) && n_identifier.equals(textBookId) && n_identifier1.contains(nodeId));
		contentCleanUp(textBookId);
	}

	// Create, upload and publish nested textbook with Draft textbook unit and visibility-Parent with draft children
	@Test
	public void publishNestedTextBookDraftTBUnitParentDraftChildrenExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		String node3 = null;
		String node4 = null;
		int count = 1;
		while (count <= 4) {
			setURI();
			int rn = generateRandomInt(9999, 1999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			if (count == 1) {
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_" + rn + "")
						.put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 2) {
				js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent")
						.put("identifier", "LP_NFT_T_" + rn + "").put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 3) {
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_" + rn + "")
						.put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 4) {
				js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent")
						.put("identifier", "LP_NFT_T_" + rn + "").put("name", "LP_NFT_T-" + rn + "");
			}
			String jsonCreateValidChild = js.toString();
			Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidChild)
					.with().contentType(JSON).when().post("content/v3/create").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node1)
						.then().
						// log().all().
						spec(get200ResponseSpec());

			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			if (count == 3) {
				node3 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node3).then().
						// log().all().
						spec(get200ResponseSpec());

			}
			if (count == 4) {
				node4 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node4).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			count++;
		}
		// Create TextbookUnit
		setURI();
		JSONObject js2 = new JSONObject(jsonCreateTextbookUnit);
		js2.getJSONObject("request").getJSONObject("content").put("visibility", "Parent");
		String jsonCreateTextbookUnit = js2.toString();
		jsonCreateTextbookUnit = jsonCreateTextbookUnit.replace("id1", node1).replace("id2", node2)
				.replace("id3", node3).replace("id4", node4);
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateTextbookUnit)
				.with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");

		// Create Textbook
		setURI();
		JSONObject js1 = new JSONObject(jsonCreateNestedCollection);
		js1.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
		String jsonCreateNestedCollection = js1.toString();
		jsonCreateNestedCollection = jsonCreateNestedCollection.replace("id1", nodeId);
		Response R3 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateNestedCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP3 = R3.jsonPath();
		String textBookId = jP3.get("result.node_id");

		// Publish textbook
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + textBookId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R4 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + textBookId).then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp4 = R4.jsonPath();
		String n_status = jp4.get("result.content.status");
		String n_identifier = jp4.get("result.content.identifier");
		ArrayList<String> n_identifier1 = jp4.get("result.content.children.identifier");
		Assert.assertTrue(n_status.equals("Live")
				|| n_status.equals(PROCESSING) || n_status.equals(PENDING) && n_identifier.equals(textBookId) && n_identifier1.contains(nodeId));
		contentCleanUp(textBookId);
	}

	// Create, upload and publish nested textbook with Live textbook unit and visibility-Default and live children
	@Ignore
	public void publishNestedTextBookLiveTBUnitDefaultLiveChildrenExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		String node3 = null;
		String node4 = null;
		int count = 1;
		while (count <= 4) {
			setURI();
			int rn = generateRandomInt(9999, 1999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			if (count == 1) {
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_" + rn + "")
						.put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 2) {
				js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent")
						.put("identifier", "LP_NFT_T_" + rn + "").put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 3) {
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_" + rn + "")
						.put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 4) {
				js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent")
						.put("identifier", "LP_NFT_T_" + rn + "").put("name", "LP_NFT_T-" + rn + "");
			}
			String jsonCreateValidChild = js.toString();
			Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidChild)
					.with().contentType(JSON).when().post("content/v3/create").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node1)
						.then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node1).then().
						// log().all().
						spec(get200ResponseSpec());

			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			if (count == 3) {
				node3 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node3).then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node3).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			if (count == 4) {
				node4 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node4).then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node4).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			count++;
		}
		// Create TextbookUnit
		setURI();
		jsonCreateTextbookUnit = jsonCreateTextbookUnit.replace("id1", node1).replace("id2", node2)
				.replace("id3", node3).replace("id4", node4);
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateTextbookUnit)
				.with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");

		// Publish Textbook unit
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			//// System.out.println(e);
		}
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + node3).then().
				// log().all().
				spec(get200ResponseSpec());

		// Create Textbook
		setURI();
		JSONObject js1 = new JSONObject(jsonCreateNestedCollection);
		js1.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
		String jsonCreateNestedCollection = js1.toString();
		jsonCreateNestedCollection = jsonCreateNestedCollection.replace("id1", nodeId);
		Response R3 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateNestedCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP3 = R3.jsonPath();
		String textBookId = jP3.get("result.node_id");

		// Publish textbook
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			//// System.out.println(e);
		}
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + textBookId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R4 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + textBookId).then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp4 = R4.jsonPath();
		String n_status = jp4.get("result.content.status");
		String n_identifier = jp4.get("result.content.identifier");
		ArrayList<String> n_identifier1 = jp4.get("result.content.children.identifier");
		Assert.assertTrue(n_status.equals("Live")
				|| n_status.equals(PROCESSING) || n_status.equals(PENDING) && n_identifier.equals(textBookId) && n_identifier1.contains(nodeId));
		contentCleanUp(textBookId);
	}

	// Create, upload and publish nested textbook with Live textbook unit and visibility-Parent and live children
	@Test
	public void publishNestedTextBookLiveTBUnitParentLiveChildrenExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		String node3 = null;
		String node4 = null;
		int count = 1;
		while (count <= 4) {
			setURI();
			int rn = generateRandomInt(9999, 1999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			if (count == 1) {
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_" + rn + "")
						.put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 2) {
				js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent")
						.put("identifier", "LP_NFT_T_" + rn + "").put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 3) {
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_" + rn + "")
						.put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 4) {
				js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent")
						.put("identifier", "LP_NFT_T_" + rn + "").put("name", "LP_NFT_T-" + rn + "");
			}
			String jsonCreateValidChild = js.toString();
			Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidChild)
					.with().contentType(JSON).when().post("content/v3/create").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node1)
						.then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node1).then().
						// log().all().
						spec(get200ResponseSpec());

			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			if (count == 3) {
				node3 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node3).then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node3).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			if (count == 4) {
				node4 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node4).then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node4).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			count++;
		}
		// Create TextbookUnit
		setURI();
		JSONObject js2 = new JSONObject(jsonCreateTextbookUnit);
		js2.getJSONObject("request").getJSONObject("content").put("visibility", "Parent");
		String jsonCreateTextbookUnit = js2.toString();
		jsonCreateTextbookUnit = jsonCreateTextbookUnit.replace("id1", node1).replace("id2", node2)
				.replace("id3", node3).replace("id4", node4);
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateTextbookUnit)
				.with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");

		// Publish Textbook unit
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Create Textbook
		setURI();
		JSONObject js1 = new JSONObject(jsonCreateNestedCollection);
		js1.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
		String jsonCreateNestedCollection = js1.toString();
		jsonCreateNestedCollection = jsonCreateNestedCollection.replace("id1", nodeId);
		Response R3 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateNestedCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP3 = R3.jsonPath();
		String textBookId = jP3.get("result.node_id");

		// Publish textbook
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + textBookId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R4 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + textBookId).then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp4 = R4.jsonPath();
		String n_status = jp4.get("result.content.status");
		String n_identifier = jp4.get("result.content.identifier");
		ArrayList<String> n_identifier1 = jp4.get("result.content.children.identifier");
		Assert.assertTrue(n_status.equals("Live")
				|| n_status.equals(PROCESSING) || n_status.equals(PENDING) && n_identifier.equals(textBookId) && n_identifier1.contains(nodeId));
		contentCleanUp(textBookId);
	}

	// Create, upload and publish nested textbook with Live textbook unit and visibility-Parent and live children
	@Test
	public void publishNestedTextBookWithMultipleTBUnitParentExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		String node3 = null;
		String node4 = null;
		int count = 1;
		while (count <= 4) {
			setURI();
			int rn = generateRandomInt(9999, 1999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			if (count == 1) {
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_" + rn + "")
						.put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 2) {
				js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent")
						.put("identifier", "LP_NFT_T_" + rn + "").put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 3) {
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_" + rn + "")
						.put("name", "LP_NFT_T-" + rn + "");
			}
			if (count == 4) {
				js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent")
						.put("identifier", "LP_NFT_T_" + rn + "").put("name", "LP_NFT_T-" + rn + "");
			}
			String jsonCreateValidChild = js.toString();
			Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidChild)
					.with().contentType(JSON).when().post("content/v3/create").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node1)
						.then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node1).then().
						// log().all().
						spec(get200ResponseSpec());

			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node2).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			if (count == 3) {
				node3 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node3).then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node3).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			if (count == 4) {
				node4 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
						.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
						.post("/content/v3/upload/" + node4).then().
						// log().all().
						spec(get200ResponseSpec());

				// Publish content
				setURI();
				given().spec(getRequestSpecification(contentType, userId, APIToken))
						.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
						.post("/content/v3/publish/" + node4).then().
						// log().all().
						spec(get200ResponseSpec());
			}
			count++;
		}
		// Create TextbookUnit1
		setURI();
		JSONObject js2 = new JSONObject(jsonCreateTextbookUnit);
		js2.getJSONObject("request").getJSONObject("content").put("visibility", "Parent");
		String jsonCreateTextbookUnit = js2.toString();
		jsonCreateTextbookUnit = jsonCreateTextbookUnit.replace("id1", node1).replace("id2", node2)
				.replace("id3", node3).replace("id4", node4);
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateTextbookUnit)
				.with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");

		// Publish Textbook unit 1
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Create TextbookUnit 2
		setURI();
		JSONObject js3 = new JSONObject(jsonCreateNestedCollection);
		js3.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
		String jsonCreateNestedCollection2 = js3.toString();
		jsonCreateNestedCollection2 = jsonCreateNestedCollection2.replace("id1", nodeId).replace("Test_QANested_",
				"Test_Textbook2_");
		Response R2 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateNestedCollection2).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP2 = R2.jsonPath();
		String nodeId2 = jP2.get("result.node_id");

		// Publish Textbook unit
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + nodeId2).then().
				// log().all().
				spec(get200ResponseSpec());

		// Create Textbook
		setURI();
		JSONObject js1 = new JSONObject(jsonCreateNestedCollection);
		js1.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
		String jsonCreateNestedCollection = js1.toString();
		jsonCreateNestedCollection = jsonCreateNestedCollection.replace("id1", nodeId2);
		Response R3 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateNestedCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP3 = R3.jsonPath();
		String textBookId = jP3.get("result.node_id");

		// Publish textbook
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + textBookId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R4 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + textBookId).then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Validate the response
		JsonPath jp4 = R4.jsonPath();
		String n_status = jp4.get("result.content.status");
		String n_identifier = jp4.get("result.content.identifier");
		ArrayList<String> n_identifier1 = jp4.get("result.content.children.identifier");
		Assert.assertTrue(n_status.equals("Live")
				|| n_status.equals(PROCESSING) || n_status.equals(PENDING) && n_identifier.equals(textBookId) && n_identifier1.contains(nodeId2));
		contentCleanUp(textBookId);
	}

	// Publish content with malformed XML body

	// Create content
	@Test
	public void publishMalformedJSONContentExpect4xx() {
		setURI();
		Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidContent)
				.with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		Response R1 = given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
				.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + nodeId).then()
				.spec(get200ResponseSpec()).
				// log().all().
				extract().response();

		// Extracting the JSON path
		JsonPath jp1 = R1.jsonPath();
		String versionKey1 = jp1.get("result.versionKey");

		// Update the body with malformed XML
		setURI();
		JSONObject js = new JSONObject(jsonUpdateContentValid);
		js.getJSONObject("request").getJSONObject("content").put("versionKey", versionKey1)
				.put("body", malformedJSONBody).remove("status");
		jsonUpdateContentValid = js.toString();
		given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonUpdateContentValid).with()
				.contentType("application/json").when().patch("/content/v3/update/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + nodeId + "?fields=body").then().extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertFalse((isValidJSON(body) || isValidXML(body)));
		contentCleanUp(nodeId);
	}

	// Publish content with malformed XML body

	// Create content
	@Test
	public void publishMalformedXMLContentExpect4xx() {
		setURI();
		Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidContent)
				.with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		Response R1 = given().spec(getRequestSpecification(uploadContentType, userId, APIToken))
				.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + nodeId).then()
				.spec(get200ResponseSpec()).
				// log().all().
				extract().response();

		// Extracting the JSON path
		JsonPath jp1 = R1.jsonPath();
		String versionKey1 = jp1.get("result.versionKey");

		// Update the body with malformed XML
		setURI();
		JSONObject js = new JSONObject(jsonUpdateContentValid);
		js.getJSONObject("request").getJSONObject("content").put("versionKey", versionKey1)
				.put("body", malformedXMLBody).remove("status");
		jsonUpdateContentValid = js.toString();
		given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonUpdateContentValid).with()
				.contentType("application/json").when().patch("/content/v3/update/" + nodeId).then().
				// log().all().
				spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + nodeId + "?fields=body").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertFalse((isValidJSON(body) || isValidXML(body)));
		contentCleanUp(nodeId);
	}
	
	// Create and search content
		@Test
		public void createAndSearchExpectSuccess200() throws InterruptedException{
			//contentCleanUp();
			setURI();
			Response R =
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					body(jsonCreateValidContent).
					with().
					contentType(JSON).
					when().
					post("/content/v3/create").
					then().
					//// log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String ecmlNode = jp.get("result.node_id");	
			
			// Upload Content
			setURI();
			given().
			spec(getRequestSpecification(uploadContentType, userId, APIToken)).
			multiPart(new File(path+"/uploadContent.zip")).
			when().
			post("/content/v3/upload/"+ecmlNode).
			then().
			// log().all().
			spec(get200ResponseSpec());

			
			// Publish created content
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/"+ecmlNode).
			then().
			// log().all().
			spec(get200ResponseSpec());
			
			// Searching with query
			try{Thread.sleep(10000);}catch(InterruptedException e){System.out.println(e);} 
			setURI();
			String jsonSimpleQuery = jsonFilteredSearch.replace("identifierNew", ecmlNode);
			Response R2 =
			given().
		 		spec(getRequestSpecification(contentType, userId, APIToken)).
		 		body(jsonSimpleQuery).
			with().
			 	contentType(JSON).
			when().
			 	post("/content/v3/adv/search").
			then().
			 	// log().all().
			 	spec(get200ResponseSpec()).
			 extract().
			 	response();
			
			// Extracting the JSON path and validate the result
			JsonPath jp2 = R2.jsonPath();
			ArrayList<String> name = jp2.get("result.content.name");
			Assert.assertTrue(name.contains(ecmlNode));
			contentCleanUp(ecmlNode);
			}
			
		@Test
		public void searchInvalidContentExpect200(){
			setURI();
			JSONObject js = new JSONObject(jsonSimpleSearchQuery);
			js.getJSONObject("request").put("query", invalidContentId);
			String jsonSimpleQuery = js.toString();
			given(). 
		 		spec(getRequestSpecification(contentType, userId, APIToken)).
		 		body(jsonSimpleQuery).
			with().
			 	contentType(JSON).
			when().
			 	post("/content/v3/adv/search").
			then().
			 	// log().all().
			 	spec(get200ResponseSpec());		
		}
		
		// Blank composite search query
		@Test
		public void blankCompositeSearchExpect200() {
			setURI();
			JSONObject js = new JSONObject(jsonSimpleSearchQuery);
			js.getJSONObject("request").put("query", "");
			String jsonBlankSearch = js.toString();
			given().
		 		spec(getRequestSpecification(contentType, userId, APIToken)).
		 		body(jsonBlankSearch).
			with().
			 	contentType(JSON).
			when().
			 	post("/content/v3/adv/search").
			then().
			 	// log().all().
			 	spec(get200ResponseSpec());
			
			}
		
		// Blank space in query
		@Test
		public void blankSpaceSearchExpect200() {
			setURI();
			JSONObject js = new JSONObject(jsonSimpleSearchQuery);
			js.getJSONObject("request").put("query", "");
			String jsonBlankSearch = js.toString();
			//// System.out.println(jsonBlankSearch);
			given().
		 		spec(getRequestSpecification(contentType, userId, APIToken)).
		 		body(jsonBlankSearch).
			with().
			 	contentType(JSON).
			when().
			 	post("/content/v3/adv/search"). 
			then().
			 	// log().all().
			 	spec(get200ResponseSpec());
			}
			
		// Blank space before and after query
			
		@Test
		public void createAndSearchWithSpaceExpectSuccess200(){
			setURI();
			Response R =
			given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidContent).
			with().
				contentType(JSON).
			when().
				post("/content/v3/create").
			then().
				// log().all().
				spec(get200ResponseSpec()).
			extract().
				response();
			
			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String ecmlNode = jp.get("result.node_id");
			
			// Space before query
			setURI();
			JSONObject js = new JSONObject(jsonFilteredSearch);
			js.getJSONObject("request").put("query", "  "+ecmlNode);
			String jsonSimpleQuery = js.toString();
			given().
		 		spec(getRequestSpecification(contentType, userId, APIToken)).
		 		body(jsonSimpleQuery).
			with().
			 	contentType(JSON).
			when().
			 	post("/content/v3/adv/search").
			then().
			 	// log().all().
			 	spec(get200ResponseSpec());
			
			// Space after query
			setURI();
			JSONObject js1 = new JSONObject(jsonFilteredSearch);
			js.getJSONObject("request").put("query", ecmlNode+"  ");
			String jsonSpaceAfterQuery = js1.toString();
			given().
		 		spec(getRequestSpecification(contentType, userId, APIToken)).
		 		body(jsonSpaceAfterQuery).
			with().
			 	contentType(JSON).
			when().
			 	post("/content/v3/adv/search").
			then().
			 	// log().all().
			 	spec(get200ResponseSpec());		
	}
		
		// Simple search request
		
		@Test
		public void simpleSearchExpectSuccess200() {
			setURI();
			given().
		 		spec(getRequestSpecification(contentType, userId, APIToken)).
		 		body(jsonSimpleSearchQuery).
			with().
			 	contentType(JSON).
			when().
			 	post("/content/v3/adv/search").
			then().
			 	// log().all().
			 	spec(get200ResponseSpec());
			}
		
		// Search with filters

		@Test
		public void searchWithFiltersExpectSuccess200(){
			setURI();
			given().
		 		spec(getRequestSpecification(contentType, userId, APIToken)).
		 		body(jsonSearchWithFilter).
			with().
			 	contentType(JSON).
			when().
			 	post("/content/v3/adv/search").
			then().
			 	// log().all().
			 	spec(get200ResponseSpec());
		}
		
		// Search with query and filters
		
		@Test
		public void searchWithQueryAndFiltersExpectSuccess200(){
			setURI();
			given().
		 		spec(getRequestSpecification(contentType, userId, APIToken)).
	 		body(jsonSearchQueryAndFilter).
			with().
			 	contentType(JSON).
			when().
			 	post("/content/v3/adv/search").
			then().
			 	// log().all().
			 	spec(get200ResponseSpec());
		}
		
		// Search with Logical Request
		
		@Test
		public void searchWithLogicalRequestExpectSuccess200(){
		setURI();
		given().
	 		spec(getRequestSpecification(contentType, userId, APIToken)).
	 		body(jsonSearchLogicalReq).
		with().
		 	contentType(JSON).
		when().
		 	post("/content/v3/adv/search").
		then().
		 	// log().all().
		 	spec(get200ResponseSpec());
		}
		
		// Search with starts and ends with
		
		@Test
		public void searchWithStartsAndEndsExpectSuccess200(){
			setURI();
			given().
		 		spec(getRequestSpecification(contentType, userId, APIToken)).
		 		body(jsonSearchWithStartsEnds).
			with().
			 	contentType(JSON).
			when().
			 	post("/content/v3/adv/search").
			then().
			 	// log().all().
			 	spec(get200ResponseSpec());
		}
		
		// Search with Equals query
		@Test
		public void searchWithEqualsExpectSuccess200(){
			setURI();
			given().
		 		spec(getRequestSpecification(contentType, userId, APIToken)).
		 		body(jsonSearchWithEquals).
			with().
			 	contentType(JSON).
			when().
			 	post("/content/v3/adv/search").
			then().
			 	// log().all().
			 	spec(get200ResponseSpec());
		}
		
		// Search with Facets
		
		@Test
		public void searchWithFacetsExpectSuccess200(){
			setURI();
			given().
		 		spec(getRequestSpecification(contentType, userId, APIToken)).
		 		body(jsonSearchWithFacets).
			with().
			 	contentType(JSON).
			when().
			 	post("/content/v3/adv/search").
			then().
			 	// log().all().
			 	spec(get200ResponseSpec());
		}
		
		// Search with Sortby Ascending
		
		@Test
		public void searchWithSortAscExpectSuccess200(){
			setURI();
			given().
		 		spec(getRequestSpecification(contentType, userId, APIToken)).
		 		body(jsonSearchWithSortByAsc).
			with().
			 	contentType(JSON).
			when().
			 	post("/content/v3/adv/search").
			then().
			 	// log().all().
			 	spec(get200ResponseSpec());
		}
		
		// Search with sort by descending
		
		@Test
		public void searchWithSortDescExpectSuccess200(){
			setURI();
			given().
		 		spec(getRequestSpecification(contentType, userId, APIToken)).
		 		body(jsonSearchWithSortByDsc).
			with().
			 	contentType(JSON).
			when().
			 	post("/content/v3/adv/search").
			then().
			 	// log().all().
			 	spec(get200ResponseSpec());
		}
		
		//  Search request with filters		
		@Test
		public void filteredSearchExpectSuccess200() {
			setURI();
			given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonFilteredSearch).
			with().	
				contentType(JSON).
			when().
				post("/content/v3/adv/search").
			then().
				// log().all().
				spec(get200ResponseSpec());
		}
		
	// Audit history for a valid content
		@Test
		public void auditHistoryValidContentExpectSuccess200(){
			setURI();
			Response R =
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					body(jsonCreateValidContent).
					with().
					contentType(JSON).
					when().
					post("/content/v3/create").
					then().
					// log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String ecmlNode = jp.get("result.node_id");

			// Audit history for the created content
			setURI();
			try{Thread.sleep(5000);}catch(InterruptedException e){ System.out.println(e);} 
			Response R1 = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/history/"+ecmlNode+"?graphId=domain").
					then().
					// log().all().
					spec(get200ResponseSpec()).
					extract().response();
					
			JsonPath jp1 = R1.jsonPath();
			ArrayList<String> record = jp1.get("result.audit_history_record");
			Assert.assertFalse(record.isEmpty());
			contentCleanUp(ecmlNode);
		}
		
		// Audit History for invalid content
		@Test
		public void auditHistoryForInvalidContentExpect200(){
			setURI();
			Response R1 = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/history/ahdjklvn?graphId=domain").
					then().
					// log().all().
					spec(get200ResponseSpec()).
					extract().response();
			
			JsonPath jP = R1.jsonPath();
			ArrayList<String> record = jP.get("result.audit_history_record");
			Assert.assertTrue(record.isEmpty());
		}
		
	// Content clean up
	public void contentCleanUp(String nodeId) {
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		delete("/content/v3/retire/" + nodeId).
		then().
		spec(get200ResponseSpec());
	}

	// Private Members
	private boolean isValidXML(String body) {
		boolean isValid = true;
		if (!StringUtils.isBlank(body)) {
			try {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				dBuilder.parse(new InputSource(new StringReader(body)));
			} catch (ParserConfigurationException | SAXException | IOException e) {
				isValid = false;
			}
		}
		return isValid;
	}

	private boolean isValidJSON(String body) {
		boolean isValid = true;
		if (!StringUtils.isBlank(body)) {
			try {
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
				objectMapper.readTree(body);
			} catch (IOException e) {
				isValid = false;
			}
		}
		return isValid;
	}

	@SuppressWarnings("unused")
	private boolean accessURL(String nodeId) throws ClassCastException {
		boolean accessURL = true;

		// Publish created content
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
		when().
		post("/content/v3/publish/" + nodeId).
		then().
		// log().all().
		spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId).
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String statusActual = jP2.get("result.content.status");

		try {
			// Validating the status
			if (statusActual.equals(PROCESSING) || statusActual.equals(PENDING)) {
				for (int i = 1000; i <= 30000; i = i + 1000) {
					try {
						Thread.sleep(i);
					} catch (InterruptedException e) {
						//// System.out.println(e);
					}
					setURI();
					Response R3 = given().spec(getRequestSpec(contentType, userId)).when()
							.get("/learning/v2/content/" + nodeId).then().
							// log().all().
							spec(get200ResponseSpec()).extract().response();

					// Validate the response
					JsonPath jp3 = R3.jsonPath();
					String statusUpdated = jp3.get("result.content.status");
					//// System.out.println(statusUpdated);
					if (statusUpdated.equals(PROCESSING) || statusUpdated.equals(PENDING)) {
						i = i + 1000;
					}
					if (statusUpdated.equals("Live")) {
						break;
					}
				}
			}

			// Get content and validate
			setURI();
			Response R1 = given().spec(getRequestSpec(contentType, userId)).when().get("/learning/v2/content/" + nodeId)
					.then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			JsonPath jP1 = R1.jsonPath();
			String statusUpdated = jP1.get("result.content.status");

			// Fetching metadatas from API response

			String artifactUrl = jP1.get("result.content.artifactUrl");
			String downloadUrl = jP1.get("result.content.downloadUrl");
			String mimeTypeActual = jP1.get("result.content.mimeType");
			String codeActual = jP1.get("result.content.code");
			String osIdActual = jP1.get("result.content.osId");
			String contentTypeActual = jP1.get("result.content.contentType");
			String mediaTypeActual = jP1.get("result.content.mediaType");
			String descriptionActual = jP1.get("result.content.description");
			String totalQuestions = jP1.get("result.content.totalQuestions");
			// Float pkgVersionActual = jP1.get("result.content.pkgVersion");
			//// System.out.println(pkgVersionActual);
			Float size = jP1.get("result.content.size");

			// Downloading the zip file from artifact url and ecar from download
			// url and saving with different name

			String ecarName = "ecar_" + rn + "";
			String uploadFile = "upload_" + rn + "";

			FileUtils.copyURLToFile(new URL(artifactUrl), new File(downloadPath + "/" + uploadFile + ".zip"));
			String uploadSource = downloadPath + "/" + uploadFile + ".zip";

			FileUtils.copyURLToFile(new URL(downloadUrl), new File(downloadPath + "/" + ecarName + ".zip"));
			String source = downloadPath + "/" + ecarName + ".zip";

			File Destination = new File(downloadPath + "/" + ecarName + "");
			String Dest = Destination.getPath();

			try {

				// Extracting the uploaded file using artifact url
				ZipFile zipUploaded = new ZipFile(uploadSource);
				zipUploaded.extractAll(Dest);

				// Downloaded from artifact url
				File uploadAssetsPath = new File(Dest + "/assets");
				File[] uploadListFiles = uploadAssetsPath.listFiles();

				// Extracting the ecar file
				ZipFile zip = new ZipFile(source);
				zip.extractAll(Dest);

				String folderName = nodeId;
				String dirName = Dest + "/" + folderName;

				File fileName = new File(dirName);
				File[] listofFiles = fileName.listFiles();

				for (File file : listofFiles) {

					// Validating the ecar file

					if (file.isFile()) {
						String fPath = file.getAbsolutePath();
						String fName = file.getName();
						//// System.out.println(fName);

						if (fName.endsWith(".zip") || fName.endsWith(".rar") || fName.endsWith(".apk")) {
							ZipFile ecarZip = new ZipFile(fPath);
							ecarZip.extractAll(dirName);

							// Fetching the assets
							File assetsPath = new File(dirName + "/assets");
							File[] extractedAssets = assetsPath.listFiles();
							if (assetsPath.exists()) {

								int assetCount = assetsPath.listFiles().length;
								//// System.out.println(assetCount);

								int uploadAssetsCount = uploadAssetsPath.listFiles().length;
								//// System.out.println(uploadAssetsCount);

								// Asserting the assets count in uploaded zip
								// file and ecar file
								Assert.assertEquals(assetCount, uploadAssetsCount);

								// Compare the files in both of the folders are
								// same
								compareFiles(uploadListFiles, extractedAssets);
							}
						} else {
							//// System.out.println("No zip file found");
						}
					} else {
						//// System.out.println("No zip file exists");
					}
				}

				// Validating the manifest
				File manifest = new File(Dest + "/manifest.json");

				Gson gson = new Gson();
				JsonParser parser = new JsonParser();

				JsonElement jsonElement = parser.parse(new FileReader(manifest));
				JsonObject obj = jsonElement.getAsJsonObject();

				JsonObject arc = obj.getAsJsonObject("archive");
				JsonArray items = arc.getAsJsonArray("items");

				@SuppressWarnings("rawtypes")

				// Extracting the metadata from manifest and assert with api
				// response

				Iterator i = items.iterator();
				while (i.hasNext()) {
					try {
						JsonObject item = (JsonObject) i.next();
						String name = getStringValue(item, "name");
						String mimeType = getStringValue(item, "mimeType");
						Assert.assertEquals(mimeTypeActual, mimeType);
						String status = getStringValue(item, "status");
						Assert.assertEquals(statusUpdated, status);
						String code = getStringValue(item, "code");
						Assert.assertEquals(codeActual, code);
						String osID = getStringValue(item, "osId");
						Assert.assertEquals(osIdActual, osID);
						String contentType = getStringValue(item, "contentType");
						Assert.assertEquals(contentTypeActual, contentType);
						String mediaType = getStringValue(item, "mediaType");
						Assert.assertEquals(mediaTypeActual, mediaType);
						String description = getStringValue(item, "description");
						Assert.assertEquals(descriptionActual, description);
						//String totalQues = getStringValue(item,"totalQuestions").toString();
						//// System.out.println(totalQuestions +totalQues);
//						if(getStringValue(item,"totalQuestions")!=null){
//							Assert.assertEquals(totalQuestions, totalQues);
//						}
						String pkgVersion = getStringValue(item, "pkgVersion");
						// Assert.assertNotSame(pkgVersionActual, pkgVersion);
						Assert.assertTrue(artifactUrl.endsWith(".zip") || artifactUrl.endsWith(".apk")
								&& downloadUrl.endsWith(".ecar") && statusUpdated.equals("Live"));
						//// System.out.println(description +mediaType +code);
					} catch (JSONException jse) {
						return false;
						// jse.printStackTrace();
					}
				}
			} catch (Exception x) {
				return false;
				// x.printStackTrace();
			}
			
		}
		catch (Exception e) {
			return false;
			// e.printStackTrace();
		}
		return accessURL;
	}

	private String getStringValue(JsonObject obj, String attr) {
		if (obj.has(attr)) {
			JsonElement element = obj.get(attr);
			return element.getAsString();
		}
		return null;
	}

	/*
	 * / Async publish validations - Other contents public void
	 * asyncPublishValidationContents(String nodeId, String statusActual){ for
	 * (int i=1000; i<=5000; i=i+1000){
	 * try{Thread.sleep(i);}catch(InterruptedException
	 * e){//// System.out.println(e);} setURI(); Response R3 = given().
	 * spec(getRequestSpec(contentType, userId)). when().
	 * get("/learning/v2/content/"+nodeId). then(). //// log().all().
	 * spec(get200ResponseSpec()). extract(). response();
	 * 
	 * // Validate the response JsonPath jp3 = R3.jsonPath(); String
	 * statusUpdated = jp3.get("result.content.status");
	 * //// System.out.println(statusUpdated); if
	 * (statusUpdated.equals("Processing")){ i=i+1000; } if
	 * (statusUpdated.equals("Live")){ //// System.out.println(statusUpdated); } } }
	 */

	// Async Publish validations - Collection
	public void asyncPublishValidations(ArrayList<String> identifier1, String status, String nodeId,
			String c_identifier, String node1, String node2) {
		if (status.equals(PROCESSING) || status.equals(PENDING)) {
			for (int i = 1000; i <= 30000; i = i + 1000) {
				try {
					Thread.sleep(i);
				} catch (InterruptedException e) {
					//// System.out.println(e);
				}
				setURI();
				Response R3 = given().spec(getRequestSpec(contentType, userId)).when()
						.get("/learning/v2/content/" + nodeId).then().
						// log().all().
						spec(get200ResponseSpec()).extract().response();

				// Validate the response
				JsonPath jp3 = R3.jsonPath();
				String statusUpdated = jp3.get("result.content.status");
				if (statusUpdated.equals(PROCESSING) || statusUpdated.equals(PENDING)) {
					//// System.out.println(statusUpdated);
					i++;
				}
				if (statusUpdated.equals("Live")) {
					Assert.assertTrue(
							c_identifier.equals(nodeId) && identifier1.contains(node1) && identifier1.contains(node2));
					break;
				}
			}
		} else if (status.equals("Live")) {
			Assert.assertTrue(
					c_identifier.equals(nodeId) && identifier1.contains(node1) && identifier1.contains(node2));
		}
	}

	// Compare the files extracted from artifact URL and ECAR

	public String compareFiles(File[] uploadListFiles, File[] extractedAssets) {
		String filesincommon = "";
		String filesnotpresent = "";
		boolean final_status = true;
		for (int i = 0; i < uploadListFiles.length; i++) {
			boolean status = false;
			for (int k = 0; k < extractedAssets.length; k++) {
				if (uploadListFiles[i].getName().equalsIgnoreCase(extractedAssets[k].getName())) {
					filesincommon = uploadListFiles[i].getName() + "," + filesincommon;
					//// System.out.println("Common files are: "+filesincommon);
					status = true;
					break;
				}
			}
			if (!status) {
				final_status = false;
				filesnotpresent = uploadListFiles[i].getName() + "," + filesnotpresent;
			}
		}
		// Assert.assertTrue(final_status);
		if (final_status) {
			//// System.out.println("Files are same");
			return "success";
		} else {
			//// System.out.println(filesnotpresent);
			return filesnotpresent;
		}
	}
}
