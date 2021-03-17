package org.sunbird.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.sunbird.platform.domain.BaseTest;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

import net.lingala.zip4j.core.ZipFile;
@Ignore
public class MimeTypeMgrTests extends BaseTest {

	int rn = generateRandomInt(0, 9999999);

	String jsonCreateValidContent = "{ \"request\": { \"content\": { \"mediaType\": \"content\",\"identifier\": \"LP_FT_"+rn+"\", \"visibility\": \"Default\", \"name\": \"test\", \"language\": [ \"English\" ], \"contentType\": \"Resource\", \"code\": \"test\", \"osId\": \"org.sunbird.quiz.app\", \"pkgVersion\": 1, \"mimeType\":\"video/youtube\", \"artifactUrl\":\"https://www.youtube.com/watch?v=s10ARdfQUOY\" } } } ";
	String jsonCreateContentWithInvalidMimeType = "{ \"request\": { \"content\": { \"identifier\": \"LP_FT_"+rn+"\", \"mediaType\": \"content\", \"visibility\": \"Default\", \"name\": \"test\", \"language\": [ \"English\" ], \"contentType\": \"Resource\", \"code\": \"test\", \"osId\": \"org.sunbird.quiz.app\", \"pkgVersion\": 1, \"mimeType\":\"videos/youtubes\", \"artifactUrl\":\"https://www.youtube.com/watch?v=s10ARdfQUOY\" } } } ";
	String jsonContentWithPublisherId = "{\"request\": {\"content\": {\"lastPublishedBy\": \"Ekstep\"}}}";
	String jsonContentClean = "{\"request\": {\"searchProperty\": \"name\",\"searchOperator\": \"startsWith\",\"searchString\": \"LP_FT_\"}}";
	String jsonCreateInvalidUrlContent = "{ \"request\": { \"content\": { \"identifier\": \"LP_FT_"+rn+"\", \"mediaType\": \"content\", \"visibility\": \"Default\", \"name\": \"test\", \"language\": [ \"English\" ], \"contentType\": \"Resource\", \"code\": \"test\", \"osId\": \"org.sunbird.quiz.app\", \"pkgVersion\": 1, \"mimeType\":\"video/youtube\", \"artifactUrl\":\"https://www.videos.com/watch?v=s10ARdfQUOY\" } } } ";
	String jsonCreateValidPdfContent = "{ \"request\": { \"content\": {\"identifier\": \"LP_FT_"+rn+"\", \"mediaType\": \"content\", \"visibility\": \"Default\", \"name\": \"test\", \"language\": [ \"English\" ], \"appIcon\":\"http://media.idownloadblog.com/wp-content/uploads/2014/08/YouTube-2.9-for-iOS-app-icon-small.png\", \"contentType\": \"Resource\", \"code\": \"test\", \"osId\": \"org.sunbird.quiz.app\", \"pkgVersion\": 1, \"mimeType\":\"application/pdf\" } } }";
	String jsonCreateValidPdfContentWithInvalidMimeType = "{ \"request\": { \"identifier\": \"LP_FT_"+rn+"\", \"content\": { \"mediaType\": \"content\", \"visibility\": \"Default\", \"name\": \"test\", \"language\": [ \"English\" ], \"appIcon\":\"http://media.idownloadblog.com/wp-content/uploads/2014/08/YouTube-2.9-for-iOS-app-icon-small.png\", \"contentType\": \"Resource\", \"code\": \"test\", \"osId\": \"org.sunbird.quiz.app\", \"pkgVersion\": 1, \"mimeType\":\"application/pdsaf\" } } }";
	String jsonCreateValidPdfContentWithUrl = "{ \"request\": { \"content\": { \"identifier\": \"LP_FT_"+rn+"\", \"mediaType\": \"content\", \"visibility\": \"Default\", \"name\": \"test\", \"language\": [ \"English\" ], \"appIcon\":\"http://media.idownloadblog.com/wp-content/uploads/2014/08/YouTube-2.9-for-iOS-app-icon-small.png\", \"contentType\": \"Resource\", \"code\": \"test\", \"osId\": \"org.sunbird.quiz.app\", \"pkgVersion\": 1, \"mimeType\":\"application/pdf\",\"artifactUrl\":\"http://www.pdf995.com/samples/pdf.pdf\" } } }";
	String jsonCreateValidDocContent = "{ \"request\": { \"content\": { \"identifier\": \"LP_FT_"+rn+"\", \"mediaType\": \"content\", \"visibility\": \"Default\", \"name\": \"test\", \"language\": [ \"English\" ], \"appIcon\":\"http://media.idownloadblog.com/wp-content/uploads/2014/08/YouTube-2.9-for-iOS-app-icon-small.png\", \"contentType\": \"Resource\", \"code\": \"test\", \"osId\": \"org.sunbird.quiz.app\", \"pkgVersion\": 1, \"mimeType\":\"application/msword\" } } }";
	String jsonCreateValidEpubContent = "{\"request\":{\"content\":{\"osId\":\"org.sunbird.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Test Epub content\",\"gradeLevel\":[\"Grade 2\"],\"name\":\"Epub\",\"language\":[\"English\"],\"contentType\":\"Story\",\"code\":\"test epub content\",\"mimeType\":\"application/epub\"}}}";
	String jsonCreateEpubContentWithInvalidZip = "{\"request\":{\"content\":{\"osId\":\"org.sunbird.quiz.app\",\"mediaType\":\"content\",\"visibility\":\"Default\",\"description\":\"Test Epub content\",\"gradeLevel\":[\"Grade 2\"],\"name\":\"Epub\",\"language\":[\"English\"],\"contentType\":\"Story\",\"code\":\"test epub content\",\"mimeType\":\"application/epub\"}}}";
	private String PROCESSING = "Processing";
	private String PENDING = "Pending";
	
	private static Set<String> allowed_file_extensions = new HashSet<String>();

	static {
		allowed_file_extensions.add("doc");
		allowed_file_extensions.add("docx");
		allowed_file_extensions.add("ppt");
		allowed_file_extensions.add("pptx");
		allowed_file_extensions.add("key");
		allowed_file_extensions.add("odp");
		allowed_file_extensions.add("pps");
		allowed_file_extensions.add("odt");
		allowed_file_extensions.add("wpd");
		allowed_file_extensions.add("wps");
		allowed_file_extensions.add("wks");
	}
	
	static ClassLoader classLoader = MimeTypeMgrTests.class.getClassLoader();
	static File path = new File(classLoader.getResource("UploadFiles/").getFile());
	static URL url = classLoader.getResource("DownloadedFiles");
	static File downloadPath;
	
	 @BeforeClass
	 public static void setup() throws URISyntaxException{
	    downloadPath = new File(url.toURI().getPath());
	 }

	 @AfterClass
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
	public void createValidYoutubeContentExpectSuccess200() {
		contentCleanUp();
		setURI();
		Response R = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/content").
				then().
		//		log().all().
				spec(get200ResponseSpec())
				.extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String node = jp.get("result.node_id");

		// Publish
		setURI();
			given().
			spec(getRequestSpec(contentType, validuserId)).
			when().
			get("/learning/v2/content/publish/" + node).
			then().
		//  log().all().
			spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R4 =
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/" + node).
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Validate the response
		JsonPath jp4 = R4.jsonPath();
		String status = jp4.get("result.content.status");
		asyncPublishValidationContents(node, status);
		accessURL(node);
	}

	@Test
	public void createYoutubeContentWithInvalidYoutubeUrlExpect400() {
		contentCleanUp();
		setURI();
		Response R = given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateInvalidUrlContent).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/content").
				then().
			//	log().all().
			//	spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String node = jp.get("result.node_id");

		// Publish
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		contentType(JSON).
		when().
		get("/learning/v2/content/publish/" + node).
		then().
	//	log().all().
		spec(get400ResponseSpec());
	}

	@Test
	public void createYoutubeContentWithoutUrlExpect400(){
		contentCleanUp();
		setURI();
		Response R =
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateInvalidUrlContent).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/content").
				then().
			//	log().all().
			//	spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String node = jp.get("result.node_id");
		
				// Publish
				setURI();
				given()
				.spec(getRequestSpec(contentType, validuserId)).
				contentType(JSON).
				when().
				get("/learning/v2/content/publish/" + node).
				then().
			//	log().all().
				spec(get400ResponseSpec());
	}

	@Test
	public void createYoutubeContentWithUploadPipeLineExpect400(){
		contentCleanUp();
		setURI();
		Response R =
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateInvalidUrlContent).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/content").
				then().
			//	log().all().
			//	spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String node = jp.get("result.node_id");
		
		// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path + "/uploadContent.zip")).
				when().
				post("/learning/v2/content/upload/" + node).
				then().
			//	 log().all().
				spec(get400ResponseSpec());
	}
	
	@Test
	public void createYoutubeContentWithInvalidMimeTypeExpect400(){
		contentCleanUp();
		setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateContentWithInvalidMimeType).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/content").
				then().
				//log().all().
				spec(get400ResponseSpec());
	}
	
	@Test
	public void createValidPdfContentExpect200(){
		   contentCleanUp();
			setURI();
			Response R =
					given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateValidPdfContent).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
			//		log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String node = jp.get("result.node_id");
			
		// Upload Content
				setURI();
					given().
					spec(getRequestSpec(uploadContentType, validuserId)).
					multiPart(new File(path + "/pdf.pdf")).
					when().
					post("/learning/v2/content/upload/" + node).
					then().
					// log().all().
					spec(get200ResponseSpec());
				

	// Publish Content
				setURI();
					given()
					.spec(getRequestSpec(contentType, validuserId)).
					contentType(JSON).
					when().
					get("/learning/v2/content/publish/" + node).
					then().
				//	log().all().
					spec(get200ResponseSpec());
				
	// Get content and validate
				setURI();
				Response R4 =
						given().
						spec(getRequestSpec(contentType, validuserId)).
						when().
						get("/learning/v2/content/" + node).
						then().
						// log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				// Validate the response
				JsonPath jp4 = R4.jsonPath();
				String status = jp4.get("result.content.status");
				asyncPublishValidationContents(node, status);
				accessURL(node);
}
	
	@Test
	public void createPdfContentWithInvalidMimeTypeExpect400(){
		contentCleanUp();
		setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateValidPdfContentWithInvalidMimeType).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/content").
				then().
		//		log().all().
				spec(get400ResponseSpec());	
	}
	
	@Test
	public void createPdfContentWithUrlExpect200(){
		contentCleanUp();
		setURI();
		Response R1 = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateValidPdfContentWithUrl).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/content").
				then().
	  			log().all().
				spec(get200ResponseSpec()).
				extract().
				response();
		
		// Extracting the JSON path
			JsonPath jp = R1.jsonPath();
			String node = jp.get("result.node_id");
		
		// Publish Content
		setURI();
				given()
				.spec(getRequestSpec(contentType, validuserId)).
				contentType(JSON).
				when().
				get("/learning/v2/content/publish/" + node).
				then().
				log().all().
				spec(get200ResponseSpec());
		
		// Get content and validate
		setURI();
		Response R4 =
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/" + node).
				then().
				// log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Validate the response
		JsonPath jp4 = R4.jsonPath();
		String status = jp4.get("result.content.status");
		asyncPublishValidationContents(node, status);
		accessURL(node);
	}
	
	@Test
	public void createPdfWithInvalidFileUploadedExpect400(){
		contentCleanUp();
		setURI();
		Response R =
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateValidPdfContent).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/content").
				then().
		//		log().all().
//				spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String node = jp.get("result.node_id");
		
	// Upload Content
				setURI();
					given().
					spec(getRequestSpec(uploadContentType, validuserId)).
					multiPart(new File(path + "/carpenter.png")).
					when().
					post("/learning/v2/content/upload/" + node).
					then().
					// log().all().
					spec(get400ResponseSpec());
	}
	
	@Test
	public void createValidDocContentExpect200(){
		contentCleanUp();
		setURI();
		Response R1 = 
			given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateValidDocContent).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/content").
				then().
	  		//	log().all().
				spec(get200ResponseSpec()).
				extract().
				response();
			
			// Extracting the JSON path
				JsonPath jp = R1.jsonPath();
				String node = jp.get("result.node_id");
			
		// Upload Content
				setURI();
					given().
					spec(getRequestSpec(uploadContentType, validuserId)).
					multiPart(new File(path + "/sample.pptx")).
					when().
					post("/learning/v2/content/upload/" + node).
					then().
					// log().all().
					spec(get200ResponseSpec());
					
				// Publish Content
				setURI();
					given()
					.spec(getRequestSpec(contentType, validuserId)).
					contentType(JSON).
					when().
					get("/learning/v2/content/publish/" + node).
					then().
				//	log().all().
					spec(get200ResponseSpec());
				
				// Get content and validate
				setURI();
				Response R4 =
					given().
					spec(getRequestSpec(contentType, validuserId)).
					when().
					get("/learning/v2/content/" + node).
					then().
					// log().all().
					spec(get200ResponseSpec()).
					extract().
					response();
		
				// Validate the response
				JsonPath jp4 = R4.jsonPath();
				String status = jp4.get("result.content.status");
				asyncPublishValidationContents(node, status);
				accessURL(node);
	}
	
	@Test
	public void createValidEpubContentExpect200(){
		   contentCleanUp();
			setURI();
			Response R =
					given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateValidEpubContent).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
			//		log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String node = jp.get("result.node_id");
			
		// Upload Content
				setURI();
					given().
					spec(getRequestSpec(uploadContentType, validuserId)).
					multiPart(new File(path + "/sample4.epub")).
					when().
					post("/learning/v2/content/upload/" + node).
					then().
				//	 log().all().
					spec(get200ResponseSpec());
				

	// Publish Content
				setURI();
					given()
					.spec(getRequestSpec(contentType, validuserId)).
					contentType(JSON).
					when().
					get("/learning/v2/content/publish/" + node).
					then().
				//	log().all().
					spec(get200ResponseSpec());
				
	// Get content and validate
				setURI();
				Response R4 =
						given().
						spec(getRequestSpec(contentType, validuserId)).
						when().
						get("/learning/v2/content/" + node).
						then().
						// log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				// Validate the response
				JsonPath jp4 = R4.jsonPath();
				String status = jp4.get("result.content.status");
				asyncPublishValidationContents(node, status);
				accessURL(node);
	}

	@Test
	public void createValidEpubContentWithInvalidZipExpect400(){
		   contentCleanUp();
			setURI();
			Response R =
					given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateValidEpubContent).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
//					log().all().
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String node = jp.get("result.node_id");
			
		// Upload Content
				setURI();
					given().
					spec(getRequestSpec(uploadContentType, validuserId)).
					multiPart(new File(path + "/ecml_with_json.zip")).
					when().
					post("/learning/v2/content/upload/" + node).
					then().
				//	 log().all().
					spec(get400ResponseSpec());
	}
	
	@Test
	public void createValidEpubContentWithInvalidContentId(){
		   contentCleanUp();
			setURI();
			Response R =
					given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateValidEpubContent).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
//					log().all().
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String node = jp.get("result.node_id");
			
		// Upload Content
				setURI();
					given().
					spec(getRequestSpec(uploadContentType, validuserId)).
					multiPart(new File(path + "/ecml_with_json.zip")).
					when().
					post("/learning/v2/content/upload/LP_FT").
					then().
				//	 log().all().
					spec(get400ResponseSpec());
	}
	
	@Test
	public void createValidEpubContentWithInvalidUrl(){
		   contentCleanUp();
			setURI();
			Response R =
					given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateValidEpubContent).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/conwsxstent").
					then().
//					log().all().
					spec(get500ResponseSpec()).
					extract().
					response();
	}
	
	@SuppressWarnings("unused")
	private boolean accessURL(String nodeId) throws ClassCastException {
		boolean accessURL = true;
		// Get content and validate
		setURI();
		Response R1 = given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/" + nodeId).
				then().
		//		log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP1 = R1.jsonPath();

		// Fetching metadatas from API response

		String artifactUrl = jP1.get("result.content.artifactUrl");
		String downloadUrl = jP1.get("result.content.downloadUrl");
		String statusActual = jP1.get("result.content.status");
		String mimeTypeActual = jP1.get("result.content.mimeType");
		String codeActual = jP1.get("result.content.code");
		String osIdActual = jP1.get("result.content.osId");
		String contentTypeActual = jP1.get("result.content.contentType");
		String mediaTypeActual = jP1.get("result.content.mediaType");
		String descriptionActual = jP1.get("result.content.description");
		Float pkgVersionActual = jP1.get("result.content.pkgVersion");
		Float size = jP1.get("result.content.size");

		// Downloading the zip file from artifact url and ecar from download url
		// and saving with different name
		try {
			String downloadPath = nodeId;
			String ecarName = "ecar_" + rn + "";
			String uploadFile = "upload_" + rn + "";

			FileUtils.copyURLToFile(new URL(downloadUrl), new File(downloadPath + "/" + ecarName + ".zip"));
			String source = downloadPath + "/" + ecarName + ".zip";

			File Destination = new File(downloadPath + "/" + ecarName + "");
			String Dest = Destination.getPath();
			System.out.println(Dest);
			try {

				// Extracting the uploaded file using artifact url
				ZipFile zipUploaded = new ZipFile(source);
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
						System.out.println(fName);
						String extension = FilenameUtils.getExtension(fName);
						if(mimeTypeActual.equalsIgnoreCase("application/pdf") || mimeTypeActual.equalsIgnoreCase("application/msword")){
							if(fName.endsWith(".pdf")){
								Assert.assertEquals(extension, "pdf");
							}
							if(allowed_file_extensions.contains(extension)){
								Assert.assertEquals(allowed_file_extensions.contains(extension), true);
							}
						}
						if (fName.endsWith(".zip") || fName.endsWith(".rar")) {
							ZipFile ecarZip = new ZipFile(fPath);
							ecarZip.extractAll(dirName);

							// Fetching the assets
							File assetsPath = new File(dirName + "/assets");
							File[] extractedAssets = assetsPath.listFiles();
							if (assetsPath.exists()) {

								int assetCount = assetsPath.listFiles().length;
								// System.out.println(assetCount);

								int uploadAssetsCount = uploadAssetsPath.listFiles().length;
								// System.out.println(uploadAssetsCount);

								// Asserting the assets count in uploaded zip
								// file and ecar file
								Assert.assertEquals(assetCount, uploadAssetsCount);

								// Compare the files in both of the folders are
								// same
								compareFiles(uploadListFiles, extractedAssets);
							}
						} else {
							System.out.println("No zip file found");
						}
					} else {
						System.out.println("No zip file exists");
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
						Assert.assertEquals(statusActual, status);
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
						Assert.assertNotSame(pkgVersionActual, pkgVersion);
						Assert.assertEquals(statusActual.equals("Live"), true);
						if(mimeType.equals("video/youtube")){
							String url = getStringValue(item, "downloadUrl");
							Assert.assertEquals(url, artifactUrl);
							String compatibilityLevel = getStringValue(item, "compatibilityLevel");
							Assert.assertEquals("4", compatibilityLevel);
							String artiUrl = getStringValue(item, "artifactUrl");
							Assert.assertEquals(artiUrl, artifactUrl);
						}
						if(mimeType.equals("application/msword") || mimeType.equals("application/pdf")){
							String compatibilityLevel = getStringValue(item, "compatibilityLevel");
							Assert.assertEquals("4", compatibilityLevel);
							Assert.assertTrue(downloadUrl.endsWith(".ecar")&&statusActual.equals("Live"));
						}
						if(mimeType.equals("application/epub")){
							String compatibilityLevel = getStringValue(item, "compatibilityLevel");
							Assert.assertEquals("4", compatibilityLevel);
							Assert.assertTrue(downloadUrl.endsWith(".ecar")&&statusActual.equals("Live"));
							Assert.assertEquals(true, artifactUrl.endsWith("index.epub"));
						}
					} catch (JSONException jse) {
						accessURL = false;
						// jse.printStackTrace();
					}
				}
			} catch (Exception x) {
				accessURL = false;
				// x.printStackTrace();
			}
		} catch (Exception e) {
			accessURL = false;
			// e.printStackTrace();
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
					// System.out.println("Common files are: "+filesincommon);
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
			// System.out.println("Files are same");
			return "success";
		} else {
			System.out.println(filesnotpresent);
			return filesnotpresent;
		}
	}

	// Async publish validations - Other contents
	public void asyncPublishValidationContents(String nodeId, String statusActual) {
		for (int i = 1000; i <= 5000; i = i + 1000) {
			try {
				Thread.sleep(i);
			} catch (InterruptedException e) {
				System.out.println(e);
			}
			setURI();
			Response R3 = given().spec(getRequestSpec(contentType, validuserId)).when()
					.get("/learning/v2/content/" + nodeId).then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Validate the response
			JsonPath jp3 = R3.jsonPath();
			String statusUpdated = jp3.get("result.content.status");
			// System.out.println(statusUpdated);
			if (statusUpdated.equals(PROCESSING) || statusUpdated.equals(PENDING)) {
				i = i + 1000;
			}
			if (statusUpdated.equals("Live")) {
				break;
			}
		}
	}

	// Async Publish validations - Collection
	public void asyncPublishValidations(ArrayList<String> identifier1, String status, String nodeId,
			String c_identifier, String node1, String node2) {
		if (status.equals(PROCESSING) || status.equals(PENDING)) {
			for (int i = 1000; i <= 5000; i = i + 1000) {
				try {
					Thread.sleep(i);
				} catch (InterruptedException e) {
					System.out.println(e);
				}
				setURI();
				Response R3 = given().spec(getRequestSpec(contentType, validuserId)).when()
						.get("/learning/v2/content/" + nodeId).then().
						// log().all().
						spec(get200ResponseSpec()).extract().response();

				// Validate the response
				JsonPath jp3 = R3.jsonPath();
				String statusUpdated = jp3.get("result.content.status");
				if (statusUpdated.equals(PROCESSING) || statusUpdated.equals(PENDING)) {
					i++;
				}
				if (statusUpdated.equals("Live")) {
					Assert.assertTrue(statusUpdated.equals("Live") && c_identifier.equals(nodeId)
							&& identifier1.contains(node1) && identifier1.contains(node2));
				}
			}
		}
		if (status.equals("Live")) {
			Assert.assertTrue(status.equals("Live") || status.equals(PROCESSING) || status.equals(PENDING) && c_identifier.equals(nodeId)
					&& identifier1.contains(node1) && identifier1.contains(node2));
		}
	}
}
