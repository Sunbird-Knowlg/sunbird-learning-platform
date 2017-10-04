package org.ekstep.platform.content;

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
import org.ekstep.platform.domain.BaseTest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
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

public class UnlistedPublishTestCases extends BaseTest{

	int rn = generateRandomInt(0, 9999999);
	String jsonCreateValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_" + rn+ "\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_NFT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
	String jsonCreateContentCollection = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_Collection_" + rn+ "\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"description\": \"Test_QA\",\"name\": \"LP_NFT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Collection\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"owner\": \"EkStep\", \"children\": [{ \"identifier\": \"id1\"}, { \"identifier\": \"id2\"}]}}}";
	String jsonContentClean = "{\"request\": {\"searchProperty\": \"identifier\",\"searchOperator\": \"startsWith\",\"searchString\": \"LP_NFT\"}}";
	String jsonUpdateATContentBody = "{\"request\": {\"content\": {\"versionKey\": \"version_Key\", \"body\": \"{\"theme\":{\"id\":\"theme\",\"version\":\"1.0\",\"startStage\":\"5bc9ae69-8e3c-4ea2-91a8-9d1c8aeabc08\",\"stage\":[{\"x\":0,\"y\":0,\"w\":100,\"h\":100,\"id\":\"5bc9ae69-8e3c-4ea2-91a8-9d1c8aeabc08\",\"rotate\":null,\"config\":{\"__cdata\":\"{\"opacity\":100,\"strokeWidth\":1,\"stroke\":\"rgba(255, 255, 255, 0)\",\"autoplay\":false,\"visible\":true,\"color\":\"#FFFFFF\",\"genieControls\":false,\"instructions\":\"\"}\"},\"manifest\":{\"media\":[]},\"org.ekstep.text\":[{\"x\":35.97,\"y\":34.81,\"minWidth\":20,\"w\":35,\"maxWidth\":500,\"fill\":\"#000000\",\"fontStyle\":\"normal\",\"fontWeight\":\"normal\",\"stroke\":\"rgba(255, 255, 255, 0)\",\"strokeWidth\":1,\"opacity\":1,\"editable\":false,\"lineHeight\":1.3,\"h\":5.02,\"rotate\":0,\"textType\":\"text\",\"z-index\":0,\"font\":\"NotoSans\",\"fontsize\":48,\"weight\":\"\",\"id\":\"dc623358-a349-4515-9a19-dd75ac0e5b96\",\"config\":{\"__cdata\":\"{\"opacity\":100,\"strokeWidth\":1,\"stroke\":\"rgba(255, 255, 255, 0)\",\"autoplay\":false,\"visible\":true,\"text\":\"Vignesh \",\"color\":\"#000000\",\"fontfamily\":\"NotoSans\",\"fontsize\":18,\"fontweight\":false,\"fontstyle\":false,\"align\":\"left\"}\"}}]}],\"manifest\":{\"media\":[{\"id\":\"org.ekstep.text\",\"plugin\":\"org.ekstep.text\",\"ver\":\"1.0\",\"src\":\"/content-plugins/org.ekstep.text-1.0/renderer/supertextplugin.js\",\"type\":\"plugin\"},{\"id\":\"org.ekstep.text_manifest\",\"plugin\":\"org.ekstep.text\",\"ver\":\"1.0\",\"src\":\"/content-plugins/org.ekstep.text-1.0/manifest.json\",\"type\":\"json\"}]},\"plugin-manifest\":{\"plugin\":[{\"id\":\"org.ekstep.text\",\"ver\":\"1.0\",\"type\":\"plugin\",\"depends\":\"\"}]},\"compatibilityVersion\":2}}\"}}";
	
	static ClassLoader classLoader = ContentPublishWorkflowTests.class.getClassLoader();
	static URL url = classLoader.getResource("DownloadedFiles");
	static File downloadPath;
	static File path = new File(classLoader.getResource("UploadFiles/").getFile());

	@BeforeClass
	public static void setup() throws URISyntaxException {
		downloadPath = new File(url.toURI().getPath());
	}


	@Test
	public void unlistUploadedDraftContentExpectSuccess200(){
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
		//log().all().
		spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {
			Assert.assertTrue(unlistedPublishValidation(nodeId));			
		}
	}
	
	@Test
	public void unlistBodyUpdatedDraftContentExpectSuccess200(){
		setURI();
		Response R = given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				//log().all().
				// spec(get200ResponseSpec()).
				extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");
		String versionKey = jp.get("result.versionKey");

		// Update content body
		try {Thread.sleep(5000);} catch (InterruptedException e){}
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
		//log().all().
		spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP2 = R2.jsonPath();
		String body = jP2.get("result.content.body");
		Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
		if (isValidXML(body) || isValidJSON(body)) {
			Assert.assertTrue(unlistedPublishValidation(nodeId));			
		}
	}
	
	@Test
	public void unlistReviewedContentExpectSuccess200(){
		setURI();
		Response R = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				//log().all().
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
		//log().all().
		spec(get200ResponseSpec());

		// Get body and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId + "?fields=body").
				then().
				//log().all().
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
			//log().all().
			spec(get200ResponseSpec());

			// Get content and validate
			setURI();
			Response R1 = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/" + nodeId).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().response();

			JsonPath jP1 = R1.jsonPath();
			String status = jP1.get("result.content.status");
			//System.out.println(status);
			Assert.assertEquals(status, "Review");
			Assert.assertTrue(unlistedPublishValidation(nodeId));
		}
	}
	
	@Test
	public void unlistPublishedContentExpectSuccess200(){
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
		//log().all().
		spec(get200ResponseSpec());

		// Publish the content 
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
		when().
		post("/content/v3/publish/" + nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec());
		Assert.assertTrue(unlistedPublishValidation(nodeId));		
	}
	
	@Test
	public void unlistRetiredContentExpectSuccess200(){
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
		//log().all().
		spec(get200ResponseSpec());

		// Publish the content 
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
		when().
		post("/content/v3/publish/" + nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		//Retire the content
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		delete("/content/v3/retire/" + nodeId).
		then().
		spec(get200ResponseSpec());
		Assert.assertTrue(unlistedPublishValidation(nodeId));		
	}
	
	@Test
	public void unlistSecondVersionContentExpectSuccess200(){
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
		//log().all().
		spec(get200ResponseSpec());

		// Publish the content 
		setURI();
		Response R2 =
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
		when().
		post("/content/v3/publish/" + nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec()).
		extract().response();
		
		JsonPath jp2 = R2.jsonPath();
		String versionKey = jp2.get("result.versionKey");

		// Update content body
		try {Thread.sleep(5000);} catch (InterruptedException e){}
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
		//log().all().
		spec(get200ResponseSpec());
		Assert.assertTrue(unlistedPublishValidation(nodeId));
	}
	
	@Test
	public void unlistValidCollectionExpectSuccess200(){
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
					//log().all().
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
				//log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
				when().
				post("/content/v3/publish/" + node1).
				then().
				//log().all().
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
				//log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
				when().
				post("/content/v3/publish/" + node2).
				then().
				//log().all().
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
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");

		// Unlisted Publish created Collection
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
		when().
		post("/content/v3/publish/unlisted/" + nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId).
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().response();

		// Validate the response
		JsonPath jp2 = R2.jsonPath();
		String status = jp2.get("result.content.status");
		String c_identifier = jp2.get("result.content.identifier");
		// String downloadUrl = jp2.get("result.content.downloadUrl");
		ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
		asyncPublishValidations(identifier1, status, nodeId, c_identifier, node1, node2);

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
	private boolean unlistedPublishValidation(String nodeId) throws ClassCastException {
		boolean unlistedPublishValidation = true;

		// Unlisted Publish created content
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
		when().
		post("/content/v3/publish/unlisted/" + nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R5 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				when().
				get("/content/v3/read/" + nodeId).
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP5 = R5.jsonPath();
		String statusActual = jP5.get("result.content.status");

		try {
			// Validating the status
			if (statusActual.equals("Processing")) {
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
					//System.out.println(statusUpdated);
					if (statusUpdated.equals("Processing")) {
						i = i + 1000;
					}
					if (statusUpdated.equals("Unlisted")) {
						break;
					}
				}
			}

			// Get content and validate
			setURI();
			Response R1 = given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					when().
					get("/content/v3/read/" + nodeId).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().response();

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
			// Float pkgVersionActual = jP1.get("result.content.pkgVersion");
			//System.out.println(pkgVersionActual);
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
						//System.out.println(fName);

						if (fName.endsWith(".zip") || fName.endsWith(".rar") || fName.endsWith(".apk")) {
							ZipFile ecarZip = new ZipFile(fPath);
							ecarZip.extractAll(dirName);

							// Fetching the assets
							File assetsPath = new File(dirName + "/assets");
							File[] extractedAssets = assetsPath.listFiles();
							if (assetsPath.exists()) {

								int assetCount = assetsPath.listFiles().length;
								//System.out.println(assetCount);

								int uploadAssetsCount = uploadAssetsPath.listFiles().length;
								//System.out.println(uploadAssetsCount);

								// Asserting the assets count in uploaded zip
								// file and ecar file
								Assert.assertEquals(assetCount, uploadAssetsCount);

								// Compare the files in both of the folders are
								// same
								compareFiles(uploadListFiles, extractedAssets);
								}
							} 
							else {
								//System.out.println("No zip file found");
								}
							} 
							else {
							//System.out.println("No zip file exists");
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
								String pkgVersion = getStringValue(item, "pkgVersion");
								// Assert.assertNotSame(pkgVersionActual, pkgVersion);
								Assert.assertTrue(artifactUrl.endsWith(".zip") || artifactUrl.endsWith(".apk")
										&& downloadUrl.endsWith(".ecar") && statusUpdated.equals("Unlisted"));
								//System.out.println(description +mediaType +code);
							} catch (JSONException jse) {
								return false;
								// jse.printStackTrace();
								}
							}
						} 
						catch (Exception x) {
						return false;
						// x.printStackTrace();
					}
				} 
				catch (Exception e) {
				return false;
			// e.printStackTrace();
		}
		return unlistedPublishValidation;
	}

	private String getStringValue(JsonObject obj, String attr) {
		if (obj.has(attr)) {
			JsonElement element = obj.get(attr);
			return element.getAsString();
		}
		return null;
	}

	// Async Publish validations - Collection
	public void asyncPublishValidations(ArrayList<String> identifier1, String status, String nodeId,
			String c_identifier, String node1, String node2) {
		if (status.equals("Processing")) {
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
				if (statusUpdated.equals("Processing")) {
					//System.out.println(statusUpdated);
					i++;
				}
				if (statusUpdated.equals("Unlisted")) {
					Assert.assertTrue(
							c_identifier.equals(nodeId) && identifier1.contains(node1) && identifier1.contains(node2));
					break;
				}
			}
		} else if (status.equals("Unlisted")) {
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
					//System.out.println("Common files are: "+filesincommon);
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
			//System.out.println("Files are same");
			return "success";
		} else {
			//System.out.println(filesnotpresent);
			return filesnotpresent;
		}
	}

}
