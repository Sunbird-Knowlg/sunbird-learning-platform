package org.sunbird.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.sunbird.platform.domain.BaseTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

public class ContentAPITests extends BaseTest {

	int rn = generateRandomInt(0, 999999);

	String jsonCreateValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn
			+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"
			+ rn
			+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";

	String jsonGetContentList = "{\"request\": { \"search\": {\"contentType\": \"Resource\", \"limit\":10}}}";
	String jsonGetContentListEmptySearch = "{\"request\": { \"search\": {}}}";

	String jsonCreateContentValid = "{\"request\": {\"content\": {\"body\": \"<theme></theme>\",\"status\": \"Draft\",\"description\": \"QA\",\"subject\": \"literacy\",\"name\": \"QA_"
			+ rn
			+ "\",\"owner\": \"EkStep\",\"s3Key\": \"content/build_a_sentence_1446706423188.zip\",\"downloadUrl\": \"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/build_a_sentence_1446706423188.zip\",\"osId\":\"Test_Id\",\"code\": \"org.sunbird.num.build.sentence\",\"contentType\": \"Resource\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"pkgVersion\": 2, \"tags\": [\"English\"],\"children\": [{ \"identifier\": \"org.sunbird.story.en.vayu\", \"index\": 0},{\"identifier\": \"org.sunbird.story.en.tree\", \"index\": 1} ],\"concepts\": [{ \"identifier\": \"LO52\"}, { \"identifier\": \"LO51\"}]}}}";
	String jsonCreateContentWithRelations = "{\"request\": {\"content\": {\"identifier\": \"Test QA_" + rn
			+ "\",\"contentType\": \"Resource\",\"body\": \"<theme></theme>\",\"status\": \"Live\",\"description\": \"English Stories Collection\", \"subject\": \"literacy\",\"name\": \"English Stories Collection\", \"owner\": \"EkStep\",\"code\": \"content_collection_7\", \"tags\": \"[English]\", \"mimeType\": \"application/vnd.ekstep.content-collection\",\"osId\":\"Test_Id\",\"tags\": [\"English\"],\"children\": [{ \"identifier\": \"org.sunbird.story.en.vayu\", \"index\": 0},{\"identifier\": \"org.sunbird.story.en.tree\", \"index\": 1} ],\"concepts\": [{ \"identifier\": \"LO52\"}, { \"identifier\": \"LO51\"}]}}}";

	String jsoncreateNonExistingParentContent = "{\"request\": {\"content\": {\"identifier\": \"content_collection_8\",\"contentType\": \"Resource\",\"body\": \"<theme></theme>\",\"status\": \"Live\",\"description\": \"English Stories Collection\", \"subject\": \"literacy\",\"name\": \"English Stories Collection\", \"owner\": \"EkStep\",\"code\": \"content_collection_7\", \"language\": \"English\", \"mimeType\": \"application/vnd.ekstep.content-collection\",\"tags\": [\"English\"],\"parent\": [{ \"identifier\": \"org.sunbird.story.en.vayu\", \"index\": 0},{\"identifier\": \"org.sunbird.story.en.tree\", \"index\": 1} ],\"concepts\": [{ \"identifier\": \"LO52\"}, { \"identifier\": \"LO51\"}]}}}";
	String jsonCreateInvalidContent = "{\"request\": {\"content\": {\"identifier\": \"content_collection_8\",\"contentType\": \"Resource\",\"body\": \"<theme></theme>\",\"status\": \"Live\",\"description\": \"English Stories Collection\", \"subject\": \"literacy\",\"name\": \"English Stories Collection\", \"owner\": \"EkStep\",\"code\": \"content_collection_7\", \"language\": \"English\", \"mimeType\": \"application/vnd.ekstep.content-collection\",\"tags\": [\"English\"],\"children\": [{ \"identifier\": \"org.sunbird.story.en.vayu\", \"index\": 0},{\"identifier\": \"org.sunbird.story.en.tree\", \"index\": 1} ],\"concepts\": [{ \"identifier\": \"L102\"}, { \"identifier\": \"LO51\"}]}}}";
	String jsonCreateInvalidConceptsContent = "{\"request\": {\"content\": {\"identifier\": \"content_collection_8\",\"contentType\": \"Resource\",\"body\": \"<theme></theme>\",\"status\": \"Live\",\"description\": \"English Stories Collection\", \"subject\": \"literacy\",\"name\": \"English Stories Collection\", \"owner\": \"EkStep\",\"code\": \"content_collection_7\", \"language\": \"English\", \"mimeType\": \"application/vnd.ekstep.content-collection\",\"tags\": [\"English\"],\"children\": [{ \"identifier\": \"org.sunbird.story.en.vayu\", \"index\": 0},{\"identifier\": \"org.sunbird.story.en.tree\", \"index\": 1} ],\"concepts\": [{ \"identifier\": \"LO52\"}, { \"identifier\": \"LO51\"}, {\"identifier\": \"Num:C3:SC1:MC12\"}]}}}";

	String jsonCreatenewConceptContent = "{\"request\": {\"content\": {\"body\": \"<theme></theme>\",\"status\": \"Draft\",\"description\": \"QA\",\"subject\": \"literacy\",\"name\": \"QA\",\"owner\": \"EkStep\",\"s3Key\": \"content/build_a_sentence_1446706423188.zip\",\"downloadUrl\": \"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/build_a_sentence_1446706423188.zip\",\"code\": \"org.sunbird.num.build.sentence\",\"contentType\": \"Resource\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"pkgVersion\": 2, \"tags\": [\"English\"],\"children\": [{ \"identifier\": \"org.sunbird.story.en.vayu\", \"index\": 0},{\"identifier\": \"org.sunbird.story.en.tree\", \"index\": 1} ],\"concepts\": [{ \"identifier\": \"LO52\"}, { \"identifier\": \"LO51\"}]}}}";
	String jsonGetUpdatedContentList = "{\"request\": { \"search\": {\"name\": \"contentname\"}}}";
	String jsonUpdateContentValid = "{\"request\": {\"content\": {\"body\": \"<theme></theme>\",\"status\": \"Draft\",\"description\": \"QA_Updated test\",\"subject\": \"literacy\",\"name\": \"QA_Updated Test\",\"owner\": \"EkStep\",\"s3Key\": \"content/build_a_sentence_1446706423188.zip\",\"downloadUrl\": \"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/build_a_sentence_1446706423188.zip\",\"osId\":\"Test_Id\",\"code\": \"org.sunbird.num.build.sentence\",\"contentType\": \"Resource\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"pkgVersion\": 2, \"tags\": [\"English\"],\"children\": [{ \"identifier\": \"org.sunbird.story.en.vayu\", \"index\": 0},{\"identifier\": \"org.sunbird.story.en.tree\", \"index\": 1} ],\"concepts\": [{ \"identifier\": \"LO52\"}, { \"identifier\": \"LO51\"}]}}}";
	String jsonCreateAndPublishContentValid = "{\"request\": {\"content\": {\"body\": \"<theme></theme>\",\"status\": \"Live\",\"description\": \"QA\",\"subject\": \"literacy\",\"name\": \"QA2\",\"owner\": \"EkStep\",\"s3Key\": \"content/build_a_sentence_1446706423188.zip\",\"downloadUrl\": \"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/build_a_sentence_1446706423188.zip\",\"code\": \"org.qa2.publish.content\",\"identifier\":\"org.qa2.publish.content\",\"contentType\": \"Resource\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"pkgVersion\": 2}}}";

	String jsonCreateValidExtractContent = "{\"request\": {\"content\": {\"identifier\": \"Test_QA_" + rn
			+ "\",\"status\": \"Live\", \"mediaType\": \"content\", \"visibility\": \"Default\", \"name\": \"Test_QA_"
			+ rn
			+ "\", \"downloadUrl\": \"https://s3-ap-southeast-1.amazonaws.com/ekstep-public/content/1452241166851_PrathamStories_Day_2_JAN_8_2016.zip\", \"language\": [\"English\"], \"contentType\": \"Resource\", \"code\": \"Test_QA_"
			+ rn
			+ "\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\", \"osId\": \"org.sunbird.quiz.app\", \"pkgVersion\": 1}}}";
	String jsonCreateValidExtractContentWithRelation = "{\"request\": {\"content\": {\"identifier\": \"Test_QA_" + rn
			+ "\",\"status\": \"Live\", \"mediaType\": \"content\", \"visibility\": \"Default\", \"name\": \"Test_QA_"
			+ rn
			+ "\", \"downloadUrl\": \"https://s3-ap-southeast-1.amazonaws.com/ekstep-public/content/1452241166851_PrathamStories_Day_2_JAN_8_2016.zip\", \"language\": [\"English\"], \"contentType\": \"Resource\", \"code\": \"Test_QA_"
			+ rn
			+ "\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\", \"children\": [{\"identifier\": \"org.sunbird.story.en.vayu\", \"index\": 0},{\"identifier\":\"org.sunbird.story.en.tree\", \"index\": 1}],\"osId\": \"org.sunbird.quiz.app\", \"pkgVersion\": 1}}}";

	String jsonUpdateATContentBody = "{\"request\": {\"content\": {\"versionKey\": \"version_Key\", \"body\": \"<theme><theme>\"}}}";

	String jsonCreateValidContentEcml = "{\"request\": {\"content\": {\"identifier\": \"Test_QA_" + rn
			+ "\",\"osId\": \"org.sunbird.quiz.app\",\"status\": \"Live\",\"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"Test_QA_"
			+ rn
			+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"pkgVersion\": 3,\"owner\": \"EkStep\",\"body\":{\"theme\":{\"manifest\":{\"media\":[{\"id\":\"barber_img\",\"src\":\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/barber_1454918396799.png\",\"type\":\"image\"},{\"id\":\"tailor_img\",\"src\":\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/tailor_1454918475261.png\",\"type\":\"image\"},{\"id\":\"carpenter_img\",\"src\":\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/carpenter_1454918523295.png\",\"type\":\"image\"}]}}}}}}";
	String invalidContentId = "TestQa_" + rn + "";
	
	private String jsonCcreateWithOwnershipType = "{\"request\":{\"content\":{\"identifier\":\"LP_FT_" + rn + "\",\"name\":\"LP_FT_" + rn + "\",\"code\":\"LP_FT_" + rn + "\",\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"ownershipType\":[\"createdFor\"]}}}";
	
	private String jsonCcreateWithWrongOwnershipType = "{\"request\":{\"content\":{\"identifier\":\"LP_FT_" + rn + "\",\"name\":\"LP_FT_" + rn + "\",\"code\":\"LP_FT_" + rn + "\",\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"ownershipType\":[\"created\"]}}}";

	static ClassLoader classLoader = ContentAPITests.class.getClassLoader();
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
	
	public RequestSpecification getRequestSpecification(String content_type, String user_id, String APIToken) {
		RequestSpecBuilder builderreq = new RequestSpecBuilder();
		builderreq.addHeader("Content-Type", content_type);
		builderreq.addHeader("user-id", user_id);
		builderreq.addHeader("Authorization", APIToken);
		RequestSpecification requestSpec = builderreq.build();
		return requestSpec;
	}
	
	public void delay(){
		try {
			Thread.sleep(3000);
		} catch (Exception e) {
			
		}
	}

	// Create and get Content
	@Test
	public void getValidContentExpectSuccess200() {
		setURI();
		Response R = given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body(jsonCreateValidContent).with().contentType(JSON).when().post("content/v3/create").then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String ecmlNode = jp.get("result.node_id");

		// Get content and validate
		setURI();
		Response R1 = given().spec(getRequestSpecification(contentType, validuserId, APIToken)).when()
				.get("/content/v3/read/" + ecmlNode).then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.content.identifier");
		String versionKey = jP1.get("result.content.versionKey");
		Assert.assertTrue(versionKey != null);
		Assert.assertEquals(ecmlNode, identifier);
	}

	// Get blank content
	@Test
	public void getblankContentExpect500() {
		setURI();
		given().spec(getRequestSpecification(contentType, validuserId, APIToken)).when().get("/content/v3/read/").then()
				.
				//log().all().
				spec(get500ResponseSpec());
	}

	// Get invalid content
	@Test
	public void getInvalidContentExpect404() {
		setURI();
		given().spec(getRequestSpecification(contentType, validuserId, APIToken)).when().get("/content/v3/read/F;NDSAF")
				.then().
				//log().all().
				spec(get404ResponseSpec());
	}

	// Create and get image content for content in draft status

	@Test
	public void getinvalidImageContentExpect400() {
		setURI();
		Response R = given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body(jsonCreateValidContent).with().contentType(JSON).when().post("content/v3/create").then().
				//log().all().
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String ecmlNode = jp.get("result.node_id");

		// Get content and validate
		setURI();
		given().spec(getRequestSpecification(contentType, validuserId, APIToken)).when()
				.get("/content/v3/read/" + ecmlNode + ".img").then().log().all().spec(get404ResponseSpec());
	}

	// Create and get image content for valid content
	@Test
	public void getImageContentVaidExpectSuccess200() {
		setURI();
		Response R1 = given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body(jsonCreateValidContent).with().contentType(JSON).when().post("content/v3/create").then().log().all().
				extract().response();

		// Extracting the JSON path
		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");

		// Upload Content
		setURI();
				given().spec(getRequestSpecification(uploadContentType, validuserId, APIToken))
				.multiPart(new File(path + "/uploadContent.zip")).then().post("/content/v3/upload/" + nodeId);

		// Publish created content
		setURI();
		given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + nodeId);
		
		delay();
		
		// Get Content
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, validuserId, APIToken)).when()
				.get("/content/v3/read/" + nodeId).then().
				log().all().
				extract().response();
		JsonPath jP2 = R2.jsonPath();
		String versionKey = jP2.get("result.content.versionKey");

		// Update Content
		setURI();
		jsonUpdateATContentBody = jsonUpdateATContentBody.replace("version_Key", versionKey);
		given().spec(getRequestSpecification(contentType, validuserId, APIToken)).body(jsonUpdateATContentBody).with()
				.contentType("application/json").then().patch("/content/v3/update/" + nodeId);

		// Get and validate
		setURI();
		Response R3 = given().spec(getRequestSpecification(contentType, validuserId, APIToken)).when()
				.get("/content/v3/read/" + nodeId + "?mode=edit").then().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP3 = R3.jsonPath();
		String identifier = jP3.get("result.content.identifier");
		String status=jP3.get("result.content.status");
		Assert.assertEquals(nodeId, identifier);
		Assert.assertEquals("Draft", status);
	}

	// Get image content with fields 
	@Ignore
	@Test
	public void getFieldsImageContentExpectSuccess200() {
		setURI();
		Response R = given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body(jsonCreateValidContent).with().contentType(JSON).when().post("content/v3/create").then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().spec(getRequestSpecification(uploadContentType, validuserId, APIToken))
				.multiPart(new File(path + "/uploadContent.zip")).then().post("/content/v3/upload/" + nodeId);
		//then().
		//log().all().
		//spec(get200ResponseSpec());

		// Publish created content
		setURI();
		Response Rp2 = given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").when()
				.post("/content/v3/publish/" + nodeId).then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		JsonPath j2 = Rp2.jsonPath();
		String versionKey = j2.get("result.versionKey");

		// Update Content
		setURI();
		jsonUpdateATContentBody = jsonUpdateATContentBody.replace("version_Key", versionKey);
		given().spec(getRequestSpecification(contentType, validuserId, APIToken)).body(jsonUpdateATContentBody).with()
				.contentType("application/json").then().patch("/content/v3/update/" + nodeId);
		//then().
		//log().all().
		//spec(get200ResponseSpec());

		// Get and validate
		setURI();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			System.out.println(e);
		}
		Response R2 = given().spec(getRequestSpecification(contentType, validuserId, APIToken)).when()
				.get("/content/v3/read/" + nodeId + "?mode=edit,fields=body,artifactUrl,downloadUrl,status").then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP2 = R2.jsonPath();
		String identifier = jP2.get("result.content.identifier");
		String body = jP2.get("result.content.body");
		String status = jP2.get("result.content.status");
		String artifactUrl = jP2.get("result.content.artifactUrl");
		String downloadUrl = jP2.get("result.content.downloadUrl");
		Assert.assertFalse(identifier.contains(".img"));
		//Assert.assertTrue(body!=null && artifactUrl.endsWith(".zip") && status.equals("Draft"));
	}

	//Get content with fields

	@Test
	public void getContentWithFieldsExpectSuccess200() {
		setURI();
		Response R = given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body(jsonCreateValidContent).with().contentType(JSON).when().post("content/v3/create").then().
				//log().all().
				//spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().spec(getRequestSpecification(uploadContentType, validuserId, APIToken))
				.multiPart(new File(path + "/uploadContent.zip")).then().post("/content/v3/upload/" + nodeId);
		//then().
		//log().all().
		//spec(get200ResponseSpec());

		// Publish created content
		setURI();
		given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").then()
				.post("/content/v3/publish/" + nodeId);

		// Get and validate
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, validuserId, APIToken)).when()
				.get("/content/v3/read/" + nodeId + "?fields=body,artifactUrl,downloadUrl").then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP2 = R2.jsonPath();
		String identifier = jP2.get("result.content.identifier");
		String body = jP2.get("result.content.body");
		String artifactUrl = jP2.get("result.content.artifactUrl");
		Assert.assertFalse(identifier.contains(".img"));
		Assert.assertTrue(body != null && artifactUrl.endsWith(".zip"));

	}

	//Get Content List
	@Test
	public void getContentListExpectSuccess200() {
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonGetContentList).with()
				.contentType("application/json").when().post("/content/v3/list").then().log().all()
				.spec(get200ResponseSpec());

	}

	//Get Content List
	@Test
	public void getContentListEmptySearchExpect200() {
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonGetContentListEmptySearch).with()
				.contentType("application/json").when().post("/content/v3/list").then().log().all()
				.spec(get200ResponseSpec());

	}

	//Search Content List
	@Test
	public void searchContentListExpectSuccess200() {
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonGetContentList).with()
				.contentType("application/json").when().post("content/v3/search").then().log().all()
				.spec(get200ResponseSpec());

	}
	@Ignore
	@Test
	public void searchContentListEmptySearchExpect200() {
		setURI();
		given().spec(getRequestSpecification(contentType, validuserId, APIToken)).body(jsonGetContentListEmptySearch).with()
				.contentType("application/json").when().post("content/v3/search").then().log().all()
				.spec(get200ResponseSpec());

	}

	@Test
	public void getContentExpectSuccess200() {
		setURI();
		Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidContent)
				.with().contentType(JSON).when().post("content/v3/create").then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Get content and validate
		setURI();
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + nodeId).then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.content.identifier");
		String versionKey = jP1.get("result.content.versionKey");
		Assert.assertTrue(versionKey != null);
		Assert.assertEquals(nodeId, identifier);

	}
	
	@Test
	public void createContentWithoutOwnershipTypeExpectSuccess200() {
		setURI();
		Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidContent)
				.with().contentType(JSON).when().post("content/v3/create").then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Get content and validate
		setURI();
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + nodeId).then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.content.identifier");
		String versionKey = jP1.get("result.content.versionKey");
		List<String> ownershipType = (List<String>)jP1.get("result.content.ownershipType");
		Assert.assertTrue(versionKey != null);
		Assert.assertEquals(nodeId, identifier);
		String[] expected = {"createdBy"};
		Assert.assertArrayEquals(expected, ownershipType.toArray(new String[ownershipType.size()]));

	}
	
	@Test
	public void createContentWithOwnershipTypeExpectSuccess200() {
		setURI();
		Response R = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCcreateWithOwnershipType)
				.with().contentType(JSON).when().post("content/v3/create").then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Get content and validate
		setURI();
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/content/v3/read/" + nodeId).then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.content.identifier");
		String versionKey = jP1.get("result.content.versionKey");
		List<String> ownershipType = (List<String>)jP1.get("result.content.ownershipType");
		Assert.assertTrue(versionKey != null);
		Assert.assertEquals(nodeId, identifier);
		String[] expected = {"createdFor"};
		Assert.assertArrayEquals(expected, ownershipType.toArray(new String[ownershipType.size()]));

	}
	
	@Test
	public void createContentWithWrongOwnershipTypeExpectSuccess200() {
		setURI();
		Response response = given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCcreateWithWrongOwnershipType)
				.with().contentType(JSON).when().post("content/v3/create").then().
				//log().all().
				spec(get400ResponseSpec()).extract().response();
		JsonPath jp = response.jsonPath();
		String responseCode = jp.get("responseCode");
		String errorCode = jp.get("params.err");
		Assert.assertEquals("CLIENT_ERROR", responseCode);
		Assert.assertEquals("ERR_GRAPH_ADD_NODE_VALIDATION_FAILED", errorCode);
	}

	//	//Create Content
	//	@Test
	//	public void createContentValidExpectSuccess200(){
	//
	//		setURI();
	//		Response R = 
	//				given().
	//				spec(getRequestSpec(contentType, validuserId)).
	//				body(jsonCreateContentValid).
	//				with().
	//				contentType(JSON).
	//				when().
	//				post("content").
	//				then().
	//				log().all().
	//				spec(get200ResponseSpec()).
	//				extract().
	//				response();
	//
	//		//Getting the node id
	//		JsonPath jP = R.jsonPath();
	//		String nodeId = jP.get("result.node_id");
	//		System.out.println(nodeId);
	//
	//		//Getting the created content
	//		setURI();
	//		given().
	//		spec(getRequestSpec(contentType, validuserId)).
	//		when().
	//		get("content/"+nodeId).
	//		then().
	//		log().all().
	//		spec(get200ResponseSpec());
	//	}
	//
	//	@Test
	//	public void createContentWithRelationsToConceptsExpect200(){		
	//		setURI();
	//		given().
	//		spec(getRequestSpec(contentType, validuserId)).
	//		body(jsonCreateContentWithRelations).
	//		with().
	//		contentType(JSON).
	//		when().
	//		post("content").
	//		then().
	//		log().all().
	//		spec(get200ResponseSpec());		
	//	}
	//
	//	//Update Content 
	//	@Test
	//	public void updateContentValidInputsExpectSuccess200()
	//	{
	//		//Create new content
	//		setURI();
	//		Response R = 
	//				given().
	//				spec(getRequestSpec(contentType, validuserId)).
	//				body(jsonCreateContentValid).
	//				with().
	//				contentType(JSON).
	//				when().
	//				post("content").
	//				then().
	//				log().all().
	//				spec(get200ResponseSpec()).
	//				extract().
	//				response();
	//
	//		//Getting the nodeID
	//		JsonPath jP = R.jsonPath();
	//		String nodeId = jP.get("result.node_id");
	//		System.out.println(nodeId);
	//
	//		// Update Content
	//		setURI();
	//		given().
	//		spec(getRequestSpec(contentType, validuserId)).
	//		body(jsonUpdateContentValid).
	//		with().
	//		contentType("application/json").
	//		when().
	//		patch("content/"+nodeId).
	//		then().
	//		log().all().
	//		spec(get200ResponseSpec());
	//	}
	//
	//	@Test
	//	public void updateContentNotExistingExpect400()
	//	{
	//		setURI();
	//		given().
	//		spec(getRequestSpec(contentType, validuserId)).
	//		body(jsonUpdateContentValid).
	//		with().
	//		contentType("application/json").
	//		when().
	//		patch("content/Test_Qa_not existing1").
	//		then().
	//		log().all().
	//		spec(get400ResponseSpec());
	//	}
	//
	//	@Test
	//	public void createNonExistingParentContentExpects400()
	//	{
	//		setURI();
	//		given().
	//		spec(getRequestSpec(contentType, validuserId)).
	//		body(jsoncreateNonExistingParentContent).		
	//		with().
	//		contentType(JSON).
	//		when().
	//		post("content").	
	//		then().
	//		log().all().
	//		spec(get400ResponseSpec());		
	//	}
	//
	//	@Test
	//	public void createInvalidContentExpects400()
	//	{
	//		setURI();
	//		given().
	//		spec(getRequestSpec(contentType, validuserId)).
	//		body(jsonCreateInvalidContent).
	//		with().
	//		contentType(JSON).
	//		when().
	//		post("content").
	//		then().
	//		log().all().
	//		spec(get400ResponseSpec());
	//	}
	//
	//	// Create content with invalid concepts
	//	@Test
	//	public void createInvalidConceptsContentExpects400()
	//	{
	//		setURI();
	//		given().
	//		spec(getRequestSpec(contentType, validuserId)).
	//		body(jsonCreateInvalidConceptsContent).
	//		with().
	//		content(JSON).
	//		when().
	//		post("content").
	//		then().
	//		log().all().
	//		spec(get400ResponseSpec());
	//	}
	//
	//	// Create content with new concept
	//
	//	@Test
	//	public void createNewConceptContentExpectSuccess200()
	//	{
	//		setURI();
	//		given().
	//		spec(getRequestSpec(contentType, validuserId)).
	//		body(jsonCreateValidContentEcml).
	//		with().
	//		content(JSON).
	//		when().
	//		post("content").
	//		then().	
	//		log().all().
	//		spec(get200ResponseSpec());
	//	}
	//
	//	// Create and publish valid content 
	//
	//	//Create Content
	//	@Test
	//	public void createAndPublishValidContentExpectSuccess200() 
	//	{
	//		setURI();
	//		JSONObject js = new JSONObject(jsonCreateValidContentEcml);
	//		js.getJSONObject("request").getJSONObject("content").put("mimeType", "");
	//		String jsonCreateValidContentAssets = js.toString();
	//		Response R = 
	//				given().
	//				spec(getRequestSpec(contentType, validuserId)).
	//				body(jsonCreateValidContentAssets).
	//				with().
	//				contentType(JSON).
	//				when().
	//				post("content").
	//				then().
	//				log().all().
	//				spec(get200ResponseSpec()).
	//				extract().
	//				response();
	//
	//		// Get node_id
	//		JsonPath jP = R.jsonPath();
	//		String nodeId = jP.get("result.node_id");
	//		System.out.println(nodeId);
	//
	//		// Publish created content
	//		setURI();
	//		Response R1 = 
	//				given().
	//				spec(getRequestSpec(contentType, validuserId)).
	//				when().
	//				get("content/publish/"+nodeId).
	//				then().
	//				log().all().
	//				spec(get200ResponseSpec()).
	//				extract().
	//				response();
	//
	//		JsonPath jP1 = R1.jsonPath();
	//		String contentURL = jP1.get("result.content_url");
	//
	//		// Get Content
	//		setURI();
	//		Response R2 =
	//				given().
	//				spec(getRequestSpec(contentType, validuserId)).
	//				when().
	//				get("content/"+nodeId).
	//				then().
	//				log().all().
	//				spec(get200ResponseSpec()).
	//				extract().
	//				response();
	//
	//		JsonPath jP2 = R2.jsonPath();
	//		String downloadURL = jP2.get("result.content.downloadUrl");
	//		Assert.assertEquals(contentURL, downloadURL);
	//	}
	//
	//	// Publish invalid content
	//
	//	@Test
	//	public void publishInvalidContentIdExpect404() 
	//	{
	//		setURI();
	//		given().
	//		spec(getRequestSpec(contentType, validuserId)).
	//		when().
	//		get("content/publish/"+invalidContentId).
	//		then().
	//		log().all().
	//		spec(get404ResponseSpec());
	//	}	
	//
	//	// Publish Invalid Path
	//
	//	@Test
	//	public void publishInvalidPathExpect500() 
	//	{
	//		setURI();
	//		given().
	//		spec(getRequestSpec(contentType, validuserId)).
	//		when().
	//		get("domain/publish/testqa18").
	//		then().
	//		log().all().
	//		spec(get500ResponseSpec());
	//	}

}
