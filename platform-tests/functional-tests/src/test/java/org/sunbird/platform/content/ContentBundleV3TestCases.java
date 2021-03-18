package org.sunbird.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.platform.domain.BaseTest;
//import org.hamcrest.CoreMatchers;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

import net.lingala.zip4j.core.ZipFile;

public class ContentBundleV3TestCases extends BaseTest {

	public RequestSpecification getRequestSpecification(String content_type, String user_id, String APIToken) {
		RequestSpecBuilder builderreq = new RequestSpecBuilder();
		builderreq.addHeader("Content-Type", content_type);
		builderreq.addHeader("user-id", user_id);
		builderreq.addHeader("Authorization", APIToken);
		RequestSpecification requestSpec = builderreq.build();
		return requestSpec;
	}

	int rn = generateRandomInt(0, 9999999);

	String jsonCreateValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_" + rn
			+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"
			+ rn
			+ "\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"pkgVersion\": 3,\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
	String jsonCreateContentCollection = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_Collection" + rn
			+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"
			+ rn
			+ "\",\"language\":[\"English\"],\"contentType\": \"Collection\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"pkgVersion\": 3,\"owner\": \"EkStep\", \"children\": [{ \"identifier\": \"id1\"}, { \"identifier\": \"id2\"}]}}}";
	String jsonCreateThreeContentCollection = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_Collection" + rn
			+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"
			+ rn
			+ "\",\"language\":[\"English\"],\"contentType\": \"Collection\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"pkgVersion\": 3,\"owner\": \"EkStep\", \"children\": [{ \"identifier\": \"id1\"}, { \"identifier\": \"id2\"}, { \"identifier\": \"id3\"}]}}}";
	String jsonUpdateContentValid = "{\"request\": {\"content\": {\"versionKey\": \"null\", \"status\": \"Live\"}}}";
	String jsonGetContentList = "{\"request\": { \"search\": {\"tags\":[\"LP_functionalTest\"], \"sort\": \"contentType\",\"order\": \"asc\"}}}";
	String jsonCreateNestedCollection = "{\"request\": {\"content\": {\"identifier\": \"Test_QANested_"+rn+"\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Collection\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"pkgVersion\": 3,\"owner\": \"EkStep\", \"children\": [{ \"identifier\": \"id1\"}]}}}";
	String jsonCreateInvalidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_"+rn+"\",\"osId\": \"org.sunbird.app\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.archive\",\"pkgVersion\": 3,\"tags\":[\"LP_functionalTest\"]}}}";
	String jsonUpdateATContentBody = "{\"request\": {\"content\": {\"versionKey\": \"null\", \"body\": \"{\\\"theme\\\":{\\\"id\\\":\\\"theme\\\",\\\"version\\\":\\\"1.0\\\",\\\"startStage\\\":\\\"cd168631-889c-4414-909d-a85a83ca3a68\\\",\\\"stage\\\":[{\\\"x\\\":0,\\\"y\\\":0,\\\"w\\\":100,\\\"h\\\":100,\\\"id\\\":\\\"cd168631-889c-4414-909d-a85a83ca3a68\\\",\\\"rotate\\\":null,\\\"config\\\":{\\\"__cdata\\\":\\\"{\\\"opacity\\\":100,\\\"strokeWidth\\\":1,\\\"stroke\\\":\\\"rgba(255, 255, 255, 0)\\\",\\\"autoplay\\\":false,\\\"visible\\\":true,\\\"color\\\":\\\"#FFFFFF\\\",\\\"genieControls\\\":false,\\\"instructions\\\":\\\"\\\"}\\\"},\\\"param\\\":[{\\\"name\\\":\\\"next\\\",\\\"value\\\":\\\"b4a01a33-a6e4-4c63-b37a-11c783c950b5\\\"}],\\\"manifest\\\":{\\\"media\\\":[{\\\"assetId\\\":\\\"do_11233272325713920013\\\"}]},\\\"image\\\":[{\\\"asset\\\":\\\"do_11233272325713920013\\\",\\\"x\\\":20,\\\"y\\\":20,\\\"w\\\":49.81,\\\"h\\\":88.56,\\\"rotate\\\":0,\\\"z-index\\\":0,\\\"id\\\":\\\"d434956b-672c-4204-bdd1-864dbae40c0c\\\",\\\"config\\\":{\\\"__cdata\\\":\\\"{\\\"opacity\\\":100,\\\"strokeWidth\\\":1,\\\"stroke\\\":\\\"rgba(255, 255, 255, 0)\\\",\\\"autoplay\\\":false,\\\"visible\\\":true}\\\"}}]},{\\\"x\\\":0,\\\"y\\\":0,\\\"w\\\":100,\\\"h\\\":100,\\\"id\\\":\\\"b4a01a33-a6e4-4c63-b37a-11c783c950b5\\\",\\\"rotate\\\":null,\\\"config\\\":{\\\"__cdata\\\":\\\"{\\\"opacity\\\":100,\\\"strokeWidth\\\":1,\\\"stroke\\\":\\\"rgba(255, 255, 255, 0)\\\",\\\"autoplay\\\":false,\\\"visible\\\":true,\\\"color\\\":\\\"#FFFFFF\\\",\\\"genieControls\\\":false,\\\"instructions\\\":\\\"\\\"}\\\"},\\\"param\\\":[{\\\"name\\\":\\\"previous\\\",\\\"value\\\":\\\"cd168631-889c-4414-909d-a85a83ca3a68\\\"}],\\\"manifest\\\":{\\\"media\\\":[{\\\"assetId\\\":\\\"do_11233272325713920013\\\"},{\\\"assetId\\\":\\\"do_10095813\\\"}]},\\\"image\\\":[{\\\"asset\\\":\\\"do_11233272325713920013\\\",\\\"x\\\":20,\\\"y\\\":20,\\\"w\\\":49.81,\\\"h\\\":88.56,\\\"rotate\\\":0,\\\"z-index\\\":0,\\\"id\\\":\\\"cc35e88c-1630-414b-9d50-a343c522e316\\\",\\\"config\\\":{\\\"__cdata\\\":\\\"{\\\"opacity\\\":100,\\\"strokeWidth\\\":1,\\\"stroke\\\":\\\"rgba(255, 255, 255, 0)\\\",\\\"autoplay\\\":false,\\\"visible\\\":true}\\\"}},{\\\"asset\\\":\\\"do_10095813\\\",\\\"x\\\":20,\\\"y\\\":20,\\\"w\\\":49.49,\\\"h\\\":87.98,\\\"rotate\\\":0,\\\"z-index\\\":1,\\\"id\\\":\\\"7849c5a6-0013-44a6-97ae-c5872974d500\\\",\\\"config\\\":{\\\"__cdata\\\":\\\"{\\\"opacity\\\":100,\\\"strokeWidth\\\":1,\\\"stroke\\\":\\\"rgba(255, 255, 255, 0)\\\",\\\"autoplay\\\":false,\\\"visible\\\":true}\\\"}}]}],\\\"manifest\\\":{\\\"media\\\":[{\\\"id\\\":\\\"do_11233272325713920013\\\",\\\"src\\\":\\\"/assets/public/content/do_11233272325713920013/artifact/5c568572a97acec4f01f596694396418_1505459382119.jpeg\\\",\\\"type\\\":\\\"image\\\"},{\\\"id\\\":\\\"do_10095813\\\",\\\"src\\\":\\\"/assets/public/content/c7a7d301f288f1afe24117ad59083b2a_1475430290462.jpeg\\\",\\\"type\\\":\\\"image\\\"}]},\\\"plugin-manifest\\\":{\\\"plugin\\\":[]},\\\"compatibilityVersion\\\":2}}\"}}}";

	String jsonContentWithPublisherId = "{\"request\":{\"content\":{\"lastPublishedBy\":\"Ekstep\"}}}";

	String invalidContentId = "LP_FT" + rn + "";
	String jsonContentClean = "{\"request\": {\"searchProperty\": \"name\",\"searchOperator\": \"startsWith\",\"searchString\": \"LP_FT_\"}}";

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

	// Create and bundle content

	// Create content
	@Test
	public void createAndBundleECMLContentExpectSuccess200() {
		setURI();
		Response R = given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body(jsonCreateValidContent).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Get node_id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");

		// Upload content
		setURI();
		given().spec(getRequestSpecification(uploadContentType, validuserId, APIToken))
		.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + nodeId).then().
		// log().all().
		spec(get200ResponseSpec());

		// Bundle created content
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, validuserId, APIToken)).body(
				"{\"request\": {\"content_identifiers\": [\"" + nodeId + "\"],\"file_name\": \"Testqa_bundle_ECML\"}}")
				.when().post("content/v3/bundle").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");
		Assert.assertTrue(bundleValidation(ecarUrl));
	}

	// Bundle content without upload file
	@Test
	public void createAndBundleWithoutUploadExpect4xx() {
		setURI();
		Response R = given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body(jsonCreateValidContent).with().contentType(JSON).when().post("content/v3/create").then().extract()
				.response();

		// Get node_id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");

		// Bundle created content
		setURI();
		given().spec(getRequestSpecification(contentType, validuserId, APIToken)).body(
				"{\"request\": {\"content_identifiers\": [\"" + nodeId + "\"],\"file_name\": \"Testqa_bundle_ECML\"}}")
		.when().post("content/v3/bundle").then().
		// log().all().
		spec(get400ResponseSpec());
	}

	// Create and Bundle APK
	@Test
	public void createAndBundleAPKContentExpectSuccess200() {
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("mimeType", "application/vnd.android.package-archive");
		String jsonCreateValidContentAPK = js.toString();
		Response R = given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body(jsonCreateValidContentAPK).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload content
		setURI();
		given().spec(getRequestSpecification(uploadContentType, validuserId, APIToken))
		.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + nodeId).then().
		// log().all().
		spec(get200ResponseSpec());

		// Bundle created content
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, validuserId, APIToken)).body(
				"{\"request\": {\"content_identifiers\": [\"" + nodeId + "\"],\"file_name\": \"Testqa_bundle_APK\"}}")
				.when().post("content/v3/bundle").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");
		Assert.assertTrue(bundleValidation(ecarUrl));
		// contentCleanUp(nodeId);
	}

	// Create and Bundle collection
	@Test
	public void createAndBundleCollectionExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		int count = 1;
		while (count <= 2) {
			setURI();
			int rn = generateRandomInt(500, 999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_FT_T_" + rn + "").put("name",
					"LP_FT_T-" + rn + "");
			String jsonCreateValidChild = js.toString();
			Response R = given().spec(getRequestSpecification(contentType, validuserId, APIToken))
					.body(jsonCreateValidChild).with().contentType(JSON).when().post("content/v3/create").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, validuserId, APIToken))
				.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node1)
				.then().
				// log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body(jsonContentWithPublisherId).with().contentType(JSON).when()
				.post("content/v3/publish/" + node1).then().
				// log().all().
				spec(get200ResponseSpec());

			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, validuserId, APIToken))
				.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node2)
				.then().
				// log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body(jsonContentWithPublisherId).with().contentType(JSON).when()
				.post("content/v3/publish/" + node2).then().
				// log().all().
				spec(get200ResponseSpec());
			}
			count++;
		}
		// Create collection
		setURI();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
		Response R1 = given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body(jsonCreateContentCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		// Get collection and validate
		JsonPath jp1 = R1.jsonPath();
		String nodeId = jp1.get("result.node_id");

		// Bundle created content
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body("{\"request\": {\"content_identifiers\": [\"" + nodeId
						+ "\"],\"file_name\": \"Testqa_bundle_Collection\"}}")
				.when().post("content/v3/bundle").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");
		Assert.assertTrue(bundleValidation(ecarUrl));
		//// contentCleanUp(nodeId);
	}

	// Bundle AT content
	@Ignore
	public void bundleAuthoringToolContentExpectSuccess200() {
		setURI();
		Response R = given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body(jsonCreateValidContent).with().contentType(JSON).when().post("content/v3/create").then().log()
				.all().extract().response();

		// Get node_id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		String versionKey = jP.get("result.versionKey");

		// Update content body
		setURI();
		jsonUpdateATContentBody = jsonUpdateATContentBody.replace("null", versionKey);
		given().spec(getRequestSpecification(contentType, validuserId, APIToken)).body(jsonUpdateATContentBody).with()
		.contentType("application/json").then().patch("/content/v3/update/" + nodeId);

		// Bundle created content
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body("{\"request\": {\"content_identifiers\": [\"" + nodeId
						+ "\"],\"file_name\": \"Testqa_bundle_ECML\"}}")
				.when().post("content/v3/bundle").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");
		Assert.assertTrue(bundleValidation(ecarUrl));
	}

	// Bundle content with updated body post upload
	@Ignore
	public void bundleBodyUpdateContentExpectSuccess200(){
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
				//log().all().
				extract().
				response();

		// Get node_id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");

		// Upload Zip File
		setURI();
		Response R1 =
				given().
				spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
				multiPart(new File(path+"/uploadContent.zip")).
				when().
				post("/content/v3/upload/"+nodeId).
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP1 = R1.jsonPath();
		String versionKey = jP1.get("result.versionKey");
		String artifactUrl = jP1.get("result.content_url");
		Assert.assertFalse(artifactUrl.equals(null));

		// Update content body
		setURI();
		jsonUpdateATContentBody = jsonUpdateATContentBody.replace("null", versionKey);
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken)).
		body(jsonUpdateATContentBody).
		with().
		contentType("application/json").
		when().
		patch("/content/v3/update/"+nodeId).
		//		then().
		//		log().all().
		then().		
		spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				when().
				get("/content/v3/read/"+nodeId).
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String artifactUrlUpdated = jP2.get("result.content.artifactUrl");
		//ArrayList<String> language = jP2.get("result.content.language");
		//Assert.assertTrue(language.contains("Hindi") && language.contains("Tamil") && language.contains("Telugu"));
		Assert.assertEquals(artifactUrlUpdated, null);

		// Bundle created content
		setURI();
		Response R3 = 
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body("{\"request\": {\"content_identifiers\": [\""+nodeId+"\"],\"file_name\": \"Testqa_bundle_ECML\"}}").
				when().
				post("content/v3/bundle").
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP3 = R3.jsonPath();
		String ecarUrl = jP3.get("result.ECAR_URL");
		Assert.assertTrue(bundleValidation(ecarUrl, "EKSTEP-ECO"));
	}

	// Bundle AT content and Uploaded content
	@Ignore
	public void bundleATAndUploadedContentExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		int count = 1;
		while (count <= 2) {
			setURI();
			int rn = generateRandomInt(1500, 999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_FT_T_" + rn + "").put("name","LP_FT_T-" + rn + "");
			String jsonCreateValidChild = js.toString();
			Response R = 
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
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
			String versionKey;
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, validuserId, APIToken))
				.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node1)
				.then().
				// log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonContentWithPublisherId).
				with().
				contentType(JSON).
				when().
				post("content/v3/publish/" + node1).
				then().
				log().all().
				spec(get200ResponseSpec());
			}
			if (count == 2) {
				node2 = nodeId;
				versionKey = jp.get("result.versionKey");
				// Update content body
				try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);} 
				setURI();
				jsonUpdateATContentBody = jsonUpdateATContentBody.replace("null", versionKey);
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonUpdateATContentBody).
				with().
				contentType("application/json").
				when().
				patch("/content/v3/update/" + nodeId).
				then().
				log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonContentWithPublisherId).
				with().
				contentType(JSON).
				when().
				post("content/v3/publish/" + node2).
				then().
				log().all().
				spec(get200ResponseSpec());
			}
			count++;
		}

		// Bundle both the contents
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body("{\"request\": {\"content_identifiers\": [\"" + node1 + "\",\"" + node2 + "\"],\"file_name\": \"Testqa_bundle_ECML&APK\"}}").
				when().
				post("content/v3/bundle").
				then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");
		Assert.assertTrue(bundleValidation(ecarUrl));
		// contentCleanUp(node1);

	}

	// Bundle existing and non-existing content
	@Test
	public void bundleExistingAndNonExistingContentExpect4xx() {
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
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
		multiPart(new File(path + "/uploadContent.zip")).
		then().
		post("/content/v3/upload/" + nodeId);

		// Bundle created content
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken)).
		body("{\"request\": {\"content_identifiers\": [\"" + nodeId + "\",\"" + invalidContentId+ "\"],\"file_name\": \"Testqa_bundle_invalid\"}}").
		when().
		post("content/v3/bundle").
		then().
		// log().all().
		spec(get404ResponseSpec());
	}

	// Bundle ECML and APK Contents
	@Test
	public void bundleECMLAndAPKContentsExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		int count = 1;
		while (count <= 2) {
			setURI();
			int rn = generateRandomInt(900, 999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			if (count == 1) {
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_FT_T_" + rn + "").put("name","LP_FT_T-" + rn + "");
			}
			if (count == 2) {
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_FT_T_" + rn + "").put("name", "LP_FT_T-" + rn + "").put("mimeType", "application/vnd.android.package-archive");
			}
			String jsonCreateValidChild = js.toString();
			Response R = 
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
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
				given().spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
				multiPart(new File(path + "/uploadContent.zip")).
				when().
				post("/content/v3/upload/" + node1).
				then().
				// log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonContentWithPublisherId).
				with().
				contentType(JSON).
				when().
				post("content/v3/publish/" + node1).
				then().
				// log().all().
				spec(get200ResponseSpec());
			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
				multiPart(new File(path + "/uploadContent.zip")).
				when().
				post("/content/v3/upload/" + node2).
				then().
				// log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonContentWithPublisherId).
				with().
				contentType(JSON).
				when().
				post("content/v3/publish/" + node2).
				then().
				// log().all().
				spec(get200ResponseSpec());
			}
			count++;
		}
		// Bundle both the contents
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body("{\"request\": {\"content_identifiers\": [\"" + node1 + "\",\"" + node2
						+ "\"],\"file_name\": \"Testqa_bundle_ECML&APK\"}}")
				.when().post("content/v3/bundle").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");
		Assert.assertTrue(bundleValidation(ecarUrl));
		// contentCleanUp(node1);
	}

	// Bundle Live and Draft contents
	@Test
	public void bundleLiveAndDraftContentsExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		int count = 1;
		while (count <= 2) {
			setURI();
			int rn = generateRandomInt(900, 999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_FT_T_" + rn + "").put("name",
					"LP_FT_T-" + rn + "");
			String jsonCreateValidChild = js.toString();
			Response R = given().spec(getRequestSpecification(contentType, validuserId, APIToken))
					.body(jsonCreateValidChild).with().contentType(JSON).when().post("content/v3/create").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, validuserId, APIToken))
				.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node1)
				.then().
				// log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body(jsonContentWithPublisherId).with().contentType(JSON).when()
				.post("content/v3/publish/" + node1).then().
				// log().all().
				spec(get200ResponseSpec());

			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, validuserId, APIToken))
				.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node2)
				.then().
				// log().all().
				spec(get200ResponseSpec());
			}
			count++;
		}
		// Bundle both the contents
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body("{\"request\": {\"content_identifiers\": [\"" + node1 + "\",\"" + node2
						+ "\"],\"file_name\": \"Testqa_bundle_ECML&APK\"}}")
				.when().post("content/v3/bundle").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");
		Assert.assertTrue(bundleValidation(ecarUrl));
		// contentCleanUp(node1);
	}

	// Bundle Live and Retired Content
	@Test
	public void bundleLiveAndRetiredContentExpect4xx() {
		String node1 = null;
		String node2 = null;
		int count = 1;
		while (count <= 2) {
			setURI();
			int rn = generateRandomInt(900, 999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_FT_T_" + rn + "").put("name",
					"LP_FT_T-" + rn + "");
			String jsonCreateValidChild = js.toString();
			Response R = given().spec(getRequestSpecification(contentType, validuserId, APIToken))
					.body(jsonCreateValidChild).with().contentType(JSON).when().post("content/v3/create").then().
					// log().all().
					// spec(get200ResponseSpec()).
					extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, validuserId, APIToken))
				.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node1);
				// then().
				// log().all().
				// spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body(jsonContentWithPublisherId).with().contentType(JSON).when()
				.post("content/v3/publish/" + node1);
				// then().
				// log().all().
				// spec(get200ResponseSpec());

			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, validuserId, APIToken))
				.multiPart(new File(path + "/tweenAndaudioSprite.zip")).then()
				.post("/content/v3/upload/" + node2);
				// then().
				// log().all().
				// spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonContentWithPublisherId).
				with().
				contentType(JSON).
				when().
				post("content/v3/publish/" + node2).
				then().
				// log().all().
				// spec(get200ResponseSpec()).
				extract().response();

				// Update status as Retired
				setURI();
				try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);} 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				delete("/content/v3/retire/"+node2);	
				//then().
				//log().all();
				//spec(get200ResponseSpec());
			}	
			count++;
		}

		// Bundle both the contents
		setURI();
		given().spec(getRequestSpecification(contentType, validuserId, APIToken)).
		body("{\"request\": {\"content_identifiers\": [\"" + node1 + "\",\"" + node2 + "\"],\"file_name\": \"Testqa_bundle_LiveAndRetired\"}}").
		when().
		post("content/v3/bundle").
		then().
		//log().all().
		spec(get400ResponseSpec());
		//// contentCleanUp(node1);
	}

	// Bundle collection with Live, Draft and Review contents
	@Test
	public void bundleCollectionWithLDRContentsExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		String node3 = null;
		int count = 1;
		while (count <= 3) {
			setURI();
			int rn = generateRandomInt(900, 999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_FT_T_" + rn + "").put("name","LP_FT_T-" + rn + "");
			String jsonCreateValidChild = js.toString();
			Response R = 
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
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
				spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
				multiPart(new File(path + "/uploadContent.zip")).
				when().
				post("/content/v3/upload/" + node1)
				.then().
				// log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonContentWithPublisherId).
				with().contentType(JSON).
				when().
				post("content/v3/publish/" + node1).
				then().
				// log().all().
				spec(get200ResponseSpec());
			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
				multiPart(new File(path + "/tweenAndaudioSprite.zip")).
				when().
				post("/content/v3/upload/" + node2).
				then().
				// log().all().
				spec(get200ResponseSpec());

			}
			if (count == 3) {
				node3 = nodeId;
				String versionKey = jp.get("result.versionKey");

				// Upload Content
				setURI();
				given().
				spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
				multiPart(new File(path + "/uploadContent.zip")).
				when().
				post("/content/v3/upload/" + node3).
				then().
				// log().all().
				spec(get200ResponseSpec());

				// Update status as Review
				setURI();
				jsonUpdateContentValid = jsonUpdateContentValid.replace("Live", "Review").replace("null", versionKey);
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonUpdateContentValid).
				with().
				contentType("application/json").
				then().
				patch("/content/v3/update/" + node3);
			}
			count++;
		}

		// Create collection
		setURI();
		jsonCreateThreeContentCollection = jsonCreateThreeContentCollection.replace("id1", node1).replace("id2", node2).replace("id3", node3);
		Response R1 = 
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonCreateThreeContentCollection).
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

		// Bundle created content
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body("{\"request\": {\"content_identifiers\": [\"" + nodeId+ "\"],\"file_name\": \"Testqa_bundle_LDRContentCollection\"}}").
				when().
				post("content/v3/bundle").
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");
		Assert.assertTrue(bundleValidation(ecarUrl));
		// contentCleanUp(nodeId);
	}

	// Bundle collection with live and retired contents
	@Ignore
	public void bundleCollectionWithLiveAndRetiredContentsExpect400() {
		String node1 = null;
		String node2 = null;
		int count = 1;
		while (count <= 2) {
			setURI();
			int rn = generateRandomInt(900, 999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_FT_T_" + rn + "").put("name","LP_FT_T-" + rn + "");
			String jsonCreateValidChild = js.toString();
			Response R = 
					given().spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonCreateValidChild).
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
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
				multiPart(new File(path + "/uploadContent.zip")).
				when().
				post("/content/v3/upload/" + node1).
				then().
				// log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body(jsonContentWithPublisherId).with().contentType(JSON).when()
				.post("content/v3/publish/" + node1);
				// then().
				// log().all().
				// spec(get200ResponseSpec());

			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
				multiPart(new File(path + "/uploadContent.zip")).
				when().
				post("/content/v3/upload/" + node2);
				// then().
				// log().all().
				// spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonContentWithPublisherId).
				with().
				contentType(JSON).
				when().
				post("content/v3/publish/" + node2).
				then().
				// log().all().
				// spec(get200ResponseSpec()).
				extract().response();

				// Update status as Retired
				setURI();
				try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);} 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				delete("/content/v3/retire/"+node2);	
				//then().
				//log().all();
				//spec(get200ResponseSpec());
			}
			count++;
		}
		// Create collection
		setURI();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
		Response R1 = given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body(jsonCreateContentCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				// spec(get200ResponseSpec()).
				extract().response();

		// Get collection and validate
		JsonPath jp1 = R1.jsonPath();
		String nodeId = jp1.get("result.node_id");

		// Bundle created content
		setURI();
		given().spec(getRequestSpecification(contentType, validuserId, APIToken))
		.body("{\"request\": {\"content_identifiers\": [\"" + nodeId
				+ "\"],\"file_name\": \"Testqa_bundle_CollectionLiveAndRetired\"}}")
		.when().post("content/v3/bundle").then().
		// log().all().
		spec(get400ResponseSpec());
		// contentCleanUp(nodeId);

	}

	// Bundle nested collection
	@Test
	public void bundleNestedCollectionExpectSuccess200() {
		String node1 = null;
		String node2 = null;
		int count = 1;
		while (count <= 2) {
			setURI();
			int rn = generateRandomInt(900, 199999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "Test_QAT_" + rn + "").put("name",
					"Test_QAT-" + rn + "");
			String jsonCreateValidChild = js.toString();
			Response R = given().spec(getRequestSpecification(contentType, validuserId, APIToken))
					.body(jsonCreateValidChild).with().contentType(JSON).when().post("content/v3/create").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, validuserId, APIToken))
				.multiPart(new File(path + "/uploadContent.zip")).when().post("/content/v3/upload/" + node1)
				.then().
				// log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body(jsonContentWithPublisherId).with().contentType(JSON).when()
				.post("content/v3/publish/" + node1).then().
				// log().all().
				spec(get200ResponseSpec());

			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().spec(getRequestSpecification(uploadContentType, validuserId, APIToken))
				.multiPart(new File(path + "/tweenAndaudioSprite.zip")).when()
				.post("/content/v3/upload/" + node2).then().
				// log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body(jsonContentWithPublisherId).with().contentType(JSON).when()
				.post("content/v3/publish/" + node2).then().
				// log().all().
				spec(get200ResponseSpec());
			}
			count++;
		}

		// Create collection
		setURI();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
		Response R1 = given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body(jsonCreateContentCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId1 = jP1.get("result.node_id");

		// Publish created content
		setURI();
		given().spec(getRequestSpecification(contentType, validuserId, APIToken)).body(jsonContentWithPublisherId)
		.with().contentType(JSON).when().post("content/v3/publish/" + nodeId1).then().
		// log().all().
		spec(get200ResponseSpec());

		// Create nested collection
		setURI();
		jsonCreateNestedCollection = jsonCreateNestedCollection.replace("id1", nodeId1);
		Response R3 = given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body(jsonCreateNestedCollection).with().contentType(JSON).when().post("content/v3/create").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP3 = R3.jsonPath();
		String nodeId = jP3.get("result.node_id");

		// Publish created content
		setURI();
		given().spec(getRequestSpecification(contentType, validuserId, APIToken)).body(jsonContentWithPublisherId)
		.with().contentType(JSON).when().post("content/v3/publish/" + nodeId).then().
		// log().all().
		spec(get200ResponseSpec());

		// Bundle created content
		setURI();
		Response R2 = given().spec(getRequestSpecification(contentType, validuserId, APIToken))
				.body("{\"request\": {\"content_identifiers\": [\"" + nodeId
						+ "\"],\"file_name\": \"Testqa_bundle_nestedCollection\"}}")
				.when().post("content/v3/bundle").then().
				// log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");
		Assert.assertTrue(bundleValidation(ecarUrl));
		// contentCleanUp(nodeId);
	}

	// Content clean up
	public void contentCleanUp(String nodeId) {
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		delete("/content/v3/retire/"+nodeId).
		then().
		spec(get200ResponseSpec());
	}

	@SuppressWarnings({ "unused" })
	private boolean bundleValidation(String ecarUrl) throws ClassCastException {
		// double manifestVesionActual = 1.1;
		boolean bundleValidation = true;
		try {
			String bundleName = "bundle_" + rn + "";

			// Downloading the Ecar from ecar url
			FileUtils.copyURLToFile(new URL(ecarUrl), new File(downloadPath + "/" + bundleName + ".zip"));
			String bundlePath = downloadPath + "/" + bundleName + ".zip";

			// Setting up extract path
			File bundleExtract = new File(downloadPath + "/" + bundleName);
			String bundleExtractPath = bundleExtract.getPath();

			try {
				// Unzip the file
				ZipFile bundleZip = new ZipFile(bundlePath);
				bundleZip.extractAll(bundleExtractPath);

				File fileName = new File(bundleExtractPath);
				File[] listofFiles = fileName.listFiles();

				// Validating the folders having zip file or not
				for (File file : listofFiles) {
					if (file.isFile() && file.getName().endsWith("json")) {
						// Reading the manifest
						File manifest = new File(file.getPath());
						// Gson gson = new Gson();
						JsonParser parser = new JsonParser();
						JsonElement jsonElement = parser.parse(new FileReader(manifest));
						JsonObject obj = jsonElement.getAsJsonObject();
						JsonElement manifestVersionElement = obj.get("ver");
						Double manifestVersion = manifestVersionElement.getAsDouble();
						// Assert.assertTrue(manifestVersion.equals(manifestVesionActual));

						// Validating expiry and items
						JsonObject arc = obj.getAsJsonObject("archive");
						if (arc.has("expires") && arc.has("items")) {
							JsonArray items = arc.getAsJsonArray("items");

							@SuppressWarnings("rawtypes")
							Iterator i = items.iterator();
							while (i.hasNext()) {
								try {

									// Validating download url, status and package version
									JsonObject item = (JsonObject) i.next();
									JsonElement downloadUrlElement = item.get("downloadUrl");
									String contentTypeElement = getStringValue(item, "contentType");
									if (contentTypeElement.equals("Collection")) {
										downloadUrlElement = downloadUrlElement.getAsJsonNull();
										Assert.assertTrue(downloadUrlElement.isJsonNull());
									} else {
										Assert.assertTrue(downloadUrlElement != null);
										String downloadUrl = downloadUrlElement.getAsString();
									}
									JsonElement statusElement = item.get("status");
									String status = statusElement.getAsString();
									JsonElement pkgVersionElement = item.get("pkgVersion");
									Float pkgVersion = pkgVersionElement.getAsFloat();
									if (status.equals("draft") || status.equals("review")) {
										Assert.assertTrue(pkgVersion.equals("0"));
									}
								} catch (Exception classCastException) {
									classCastException.printStackTrace();
									return false;
								}
							}
						}
					} else if (file.isDirectory()) {
						File[] listofsubFiles = file.listFiles();
						for (File newfile : listofsubFiles) {
							String fName = newfile.getName();
							if (fName.endsWith(".zip") || fName.endsWith(".rar")) {
								// System.out.println(fName);
							}
						}
					}
				}
			} catch (Exception zipExtract) {
				zipExtract.printStackTrace();
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	@SuppressWarnings({ "unused" })
	private boolean bundleValidation(String ecarUrl, String searchString) throws ClassCastException {
		// double manifestVesionActual = 1.1;
		boolean bundleValidation = true;
		try {
			String bundleName = "bundle_" + rn + "";

			// Downloading the Ecar from ecar url
			FileUtils.copyURLToFile(new URL(ecarUrl), new File(downloadPath + "/" + bundleName + ".zip"));
			String bundlePath = downloadPath + "/" + bundleName + ".zip";

			// Setting up extract path
			File bundleExtract = new File(downloadPath + "/" + bundleName);
			String bundleExtractPath = bundleExtract.getPath();

			try {
				// Unzip the file
				ZipFile bundleZip = new ZipFile(bundlePath);
				bundleZip.extractAll(bundleExtractPath);

				File fileName = new File(bundleExtractPath);
				File[] listofFiles = fileName.listFiles();

				// Validating the folders having zip file or not
				for (File file : listofFiles) {
					// System.out.println(file.getName());
					if (file.isFile() && file.getName().endsWith("json")) {
						// Reading the manifest
						File manifest = new File(file.getPath());
						// Gson gson = new Gson();
						JsonParser parser = new JsonParser();
						JsonElement jsonElement = parser.parse(new FileReader(manifest));
						JsonObject obj = jsonElement.getAsJsonObject();
						JsonElement manifestVersionElement = obj.get("ver");
						Double manifestVersion = manifestVersionElement.getAsDouble();
						// System.out.println(manifestVersion);
						// Assert.assertTrue(manifestVersion.equals(manifestVesionActual));

						// Validating expiry and items
						JsonObject arc = obj.getAsJsonObject("archive");
						if (arc.has("expires") && arc.has("items")) {
							JsonArray items = arc.getAsJsonArray("items");

							@SuppressWarnings("rawtypes")
							Iterator i = items.iterator();
							while (i.hasNext()) {
								try {

									// Validating download url, status and package version
									JsonObject item = (JsonObject) i.next();
									JsonElement downloadUrlElement = item.get("downloadUrl");
									String contentTypeElement = getStringValue(item, "contentType");
									if (contentTypeElement.equals("Collection")) {
										downloadUrlElement = downloadUrlElement.getAsJsonNull();
										Assert.assertTrue(downloadUrlElement.isJsonNull());
									} else {
										Assert.assertTrue(downloadUrlElement != null);
										String downloadUrl = downloadUrlElement.getAsString();
									}
									JsonElement statusElement = item.get("status");
									String status = statusElement.getAsString();
									JsonElement pkgVersionElement = item.get("pkgVersion");
									Float pkgVersion = pkgVersionElement.getAsFloat();
									if (status.equals("draft") || status.equals("review")) {
										Assert.assertTrue(pkgVersion.equals("0"));
									}
								} catch (Exception classCastException) {
									classCastException.printStackTrace();
									return false;
								}
							}
						}
					} else if (file.isFile() && file.getName().endsWith("index.ecml")) {
						File ecmlFile = new File(file.getPath());
						String ecml = getFileString(ecmlFile);
						assertTrue(StringUtils.contains(ecml, searchString));
					} else if (file.isDirectory()) {
						File[] listofsubFiles = file.listFiles();
						for (File newfile : listofsubFiles) {
							String fName = newfile.getName();
							if (fName.endsWith(".zip") || fName.endsWith(".rar")) {
								// System.out.println(fName);
							}
						}
					}
				}
			} catch (Exception zipExtract) {
				zipExtract.printStackTrace();
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	private String getStringValue(JsonObject obj, String attr) {
		if (obj.has(attr)) {
			JsonElement element = obj.get(attr);
			return element.getAsString();
		}
		return null;
	}

	private String getFileString(File file) {
		String fileString = "";
		try {
			fileString = FileUtils.readFileToString(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileString;
	}

}
