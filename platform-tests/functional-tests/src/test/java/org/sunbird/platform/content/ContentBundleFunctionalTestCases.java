package org.sunbird.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
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
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

import net.lingala.zip4j.core.ZipFile;

	
public class ContentBundleFunctionalTestCases extends BaseTest{

	int rn = generateRandomInt(0, 9999999);
	
	String jsonCreateValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_"+rn+"\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"pkgVersion\": 3,\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
	String jsonCreateContentCollection = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_Collection"+rn+"\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Collection\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"pkgVersion\": 3,\"owner\": \"EkStep\", \"children\": [{ \"identifier\": \"id1\"}, { \"identifier\": \"id2\"}]}}}";
	String jsonCreateThreeContentCollection = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_Collection"+rn+"\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Collection\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"pkgVersion\": 3,\"owner\": \"EkStep\", \"children\": [{ \"identifier\": \"id1\"}, { \"identifier\": \"id2\"}, { \"identifier\": \"id3\"}]}}}";
	String jsonUpdateContentValid = "{\"request\": {\"content\": {\"versionKey\": \"version_Key\", \"status\": \"Live\"}}}";
	String jsonGetContentList = "{\"request\": { \"search\": {\"tags\":[\"LP_functionalTest\"], \"sort\": \"contentType\",\"order\": \"asc\"}}}";
	String jsonCreateNestedCollection = "{\"request\": {\"content\": {\"identifier\": \"Test_QANested_"+rn+"\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Collection\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"pkgVersion\": 3,\"owner\": \"EkStep\", \"children\": [{ \"identifier\": \"id1\"}]}}}";
	String jsonCreateInvalidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_"+rn+"\",\"osId\": \"org.sunbird.app\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.archive\",\"pkgVersion\": 3,\"tags\":[\"LP_functionalTest\"]}}}";
	String jsonUpdateATContentBody = "{\"request\": {\"content\": {\"versionKey\": \"version_Key\", \"body\": \"{\\\"theme\\\":{\\\"manifest\\\":{\\\"media\\\":[{\\\"id\\\":\\\"tick\\\",\\\"type\\\":\\\"image\\\",\\\"src\\\":\\\"https://qa.ekstep.in/assets/public/content/1455104185970tick.png\\\",\\\"assetId\\\":\\\"tick\\\"},{\\\"id\\\":\\\"domain_2890\\\",\\\"type\\\":\\\"audio\\\",\\\"src\\\":\\\"https://qa.ekstep.in/assets/public/content/%C3%A0%C2%B2%C2%9A_1463054756900.mp3\\\",\\\"assetId\\\":\\\"domain_2890\\\"},{\\\"id\\\":\\\"validate\\\",\\\"src\\\":\\\"https://qa.ekstep.in/assets/public/content/btn_ok_highlights_1460705843676.png\\\",\\\"type\\\":\\\"image\\\",\\\"assetId\\\":\\\"domain_38852\\\"},{\\\"id\\\":\\\"popupTint\\\",\\\"src\\\":\\\"https://qa.ekstep.in/assets/public/content/PopupTint_1460636175572.png\\\",\\\"type\\\":\\\"image\\\",\\\"assetId\\\":\\\"domain_38606\\\"},{\\\"id\\\":\\\"goodjobBg\\\",\\\"src\\\":\\\"https://qa.ekstep.in/assets/public/content/goodjobBg_1460727428389.png\\\",\\\"type\\\":\\\"image\\\",\\\"assetId\\\":\\\"domain_38939\\\"},{\\\"id\\\":\\\"retryBg\\\",\\\"src\\\":\\\"https://qa.ekstep.in/assets/public/content/retryBg_1460727370746.png\\\",\\\"type\\\":\\\"image\\\",\\\"assetId\\\":\\\"domain_38938\\\"},{\\\"id\\\":\\\"retry_audio\\\",\\\"src\\\":\\\"https://qa.ekstep.in/assets/public/content/retry_1460636610607.mp3\\\",\\\"type\\\":\\\"sound\\\",\\\"assetId\\\":\\\"domain_38624\\\"},{\\\"id\\\":\\\"goodjob_audio\\\",\\\"src\\\":\\\"https://qa.ekstep.in/assets/public/content/goodJob_1460636677521.mp3\\\",\\\"type\\\":\\\"sound\\\",\\\"assetId\\\":\\\"domain_38625\\\"},{\\\"id\\\":\\\"next\\\",\\\"src\\\":\\\"https://qa.ekstep.in/assets/public/content/btn_next_1461401649059.png\\\",\\\"type\\\":\\\"image\\\",\\\"assetId\\\":\\\"domain_40358\\\"},{\\\"id\\\":\\\"previous\\\",\\\"src\\\":\\\"https://qa.ekstep.in/assets/public/content/btn_back_1461401700215.png\\\",\\\"type\\\":\\\"image\\\",\\\"assetId\\\":\\\"domain_40359\\\"},{\\\"id\\\":\\\"submit\\\",\\\"src\\\":\\\"https://qa.ekstep.in/assets/public/content/icon_submit_1459243202199.png\\\",\\\"type\\\":\\\"image\\\",\\\"assetId\\\":\\\"domain_14524\\\"},{\\\"id\\\":\\\"home\\\",\\\"src\\\":\\\"https://qa.ekstep.in/assets/public/content/icon_home_1459242981364.png\\\",\\\"type\\\":\\\"image\\\",\\\"assetId\\\":\\\"domain_14519\\\"},{\\\"id\\\":\\\"reload\\\",\\\"src\\\":\\\"https://qa.ekstep.in/assets/public/content/icon_reload_1459243110661.png\\\",\\\"type\\\":\\\"image\\\",\\\"assetId\\\":\\\"domain_14522\\\"},{\\\"id\\\":\\\"icon_hint\\\",\\\"src\\\":\\\"https://qa.ekstep.in/assets/public/content/icon_hint_1454918891133.png\\\",\\\"type\\\":\\\"image\\\",\\\"assetId\\\":\\\"domain_799\\\"},{\\\"id\\\":\\\"bg\\\",\\\"src\\\":\\\"https://qa.ekstep.in/assets/public/content/background_1458729298020.png\\\",\\\"type\\\":\\\"image\\\"}]},\\\"id\\\":\\\"theme\\\",\\\"ver\\\":0.2,\\\"startStage\\\":\\\"Stage\\\",\\\"controller\\\":[{\\\"name\\\":\\\"dictionary\\\",\\\"type\\\":\\\"data\\\",\\\"id\\\":\\\"dictionary\\\",\\\"__cdata\\\":{}}],\\\"template\\\":[{\\\"image\\\":[{\\\"event\\\":{\\\"action\\\":{\\\"type\\\":\\\"command\\\",\\\"command\\\":\\\"show\\\",\\\"asset\\\":\\\"retryDialog\\\"},\\\"type\\\":\\\"click\\\"},\\\"asset\\\":\\\"popupTint\\\",\\\"x\\\":-100,\\\"y\\\":-150,\\\"w\\\":550,\\\"h\\\":600,\\\"visible\\\":true,\\\"id\\\":\\\"popup-Tint\\\"},{\\\"asset\\\":\\\"retryBg\\\",\\\"x\\\":0,\\\"y\\\":0,\\\"w\\\":150,\\\"h\\\":150,\\\"visible\\\":true,\\\"id\\\":\\\"right\\\"}],\\\"shape\\\":[{\\\"event\\\":{\\\"action\\\":[{\\\"type\\\":\\\"command\\\",\\\"command\\\":\\\"hide\\\",\\\"asset\\\":\\\"retryDialog\\\"},{\\\"type\\\":\\\"command\\\",\\\"command\\\":\\\"SHOWHTMLELEMENTS\\\",\\\"asset\\\":\\\"retry\\\"}],\\\"type\\\":\\\"click\\\"},\\\"type\\\":\\\"roundrect\\\",\\\"x\\\":72,\\\"y\\\":25,\\\"w\\\":50,\\\"h\\\":65,\\\"visible\\\":true,\\\"id\\\":\\\"retry\\\",\\\"hitArea\\\":true},{\\\"event\\\":{\\\"action\\\":{\\\"type\\\":\\\"command\\\",\\\"command\\\":\\\"transitionTo\\\",\\\"asset\\\":\\\"theme\\\",\\\"param\\\":\\\"next\\\",\\\"effect\\\":\\\"fadein\\\",\\\"direction\\\":\\\"left\\\",\\\"ease\\\":\\\"linear\\\",\\\"duration\\\":100},\\\"type\\\":\\\"click\\\"},\\\"type\\\":\\\"roundrect\\\",\\\"x\\\":110,\\\"y\\\":100,\\\"w\\\":25,\\\"h\\\":35,\\\"visible\\\":true,\\\"id\\\":\\\"continue\\\",\\\"hitArea\\\":true}],\\\"id\\\":\\\"retry\\\"},{\\\"g\\\":{\\\"image\\\":[{\\\"asset\\\":\\\"popupTint\\\",\\\"x\\\":0,\\\"y\\\":0,\\\"w\\\":100,\\\"h\\\":100,\\\"visible\\\":true,\\\"id\\\":\\\"popup-Tint\\\"}],\\\"text\\\":[{\\\"x\\\":25,\\\"y\\\":25,\\\"w\\\":50,\\\"h\\\":9,\\\"visible\\\":true,\\\"editable\\\":true,\\\"model\\\":\\\"word.lemma\\\",\\\"weight\\\":\\\"normal\\\",\\\"font\\\":\\\"helvetica\\\",\\\"color\\\":\\\"rgb(0,0,0)\\\",\\\"fontstyle\\\":\\\"\\\",\\\"fontsize\\\":75,\\\"align\\\":\\\"left\\\",\\\"z-index\\\":1,\\\"id\\\":\\\"lemma\\\"},{\\\"x\\\":25,\\\"y\\\":35,\\\"w\\\":50,\\\"h\\\":40,\\\"visible\\\":true,\\\"editable\\\":true,\\\"model\\\":\\\"word.gloss\\\",\\\"weight\\\":\\\"normal\\\",\\\"font\\\":\\\"helvetica\\\",\\\"color\\\":\\\"rgb(0,0,0)\\\",\\\"fontstyle\\\":\\\"\\\",\\\"fontsize\\\":43,\\\"align\\\":\\\"left\\\",\\\"z-index\\\":2,\\\"id\\\":\\\"gloss\\\"}],\\\"shape\\\":[{\\\"x\\\":20,\\\"y\\\":20,\\\"w\\\":60,\\\"h\\\":60,\\\"visible\\\":true,\\\"editable\\\":true,\\\"type\\\":\\\"roundrect\\\",\\\"radius\\\":10,\\\"opacity\\\":1,\\\"fill\\\":\\\"#45b3a5\\\",\\\"stroke-width\\\":1,\\\"z-index\\\":0,\\\"id\\\":\\\"textBg\\\"}],\\\"x\\\":0,\\\"y\\\":0,\\\"w\\\":100,\\\"h\\\":100,\\\"event\\\":{\\\"action\\\":[{\\\"type\\\":\\\"command\\\",\\\"command\\\":\\\"SHOWHTMLELEMENTS\\\",\\\"asset\\\":\\\"textBg\\\"},{\\\"type\\\":\\\"command\\\",\\\"command\\\":\\\"hide\\\",\\\"parent\\\":true}],\\\"type\\\":\\\"click\\\"}},\\\"id\\\":\\\"infoTemplate\\\"},{\\\"image\\\":[{\\\"event\\\":{\\\"action\\\":{\\\"type\\\":\\\"command\\\",\\\"command\\\":\\\"show\\\",\\\"asset\\\":\\\"\\\"},\\\"type\\\":\\\"click\\\"},\\\"asset\\\":\\\"popupTint\\\",\\\"x\\\":-100,\\\"y\\\":-150,\\\"w\\\":550,\\\"h\\\":600,\\\"visible\\\":true,\\\"id\\\":\\\"popup-Tint\\\"},{\\\"event\\\":{\\\"action\\\":[{\\\"type\\\":\\\"command\\\",\\\"command\\\":\\\"transitionTo\\\",\\\"asset\\\":\\\"theme\\\",\\\"param\\\":\\\"next\\\",\\\"effect\\\":\\\"fadein\\\",\\\"direction\\\":\\\"left\\\",\\\"ease\\\":\\\"linear\\\",\\\"duration\\\":500}],\\\"type\\\":\\\"click\\\"},\\\"asset\\\":\\\"goodjobBg\\\",\\\"x\\\":0,\\\"y\\\":0,\\\"w\\\":150,\\\"h\\\":150,\\\"visible\\\":true,\\\"id\\\":\\\"continue\\\"}],\\\"id\\\":\\\"goodjob\\\"}],\\\"stage\\\":[{\\\"id\\\":\\\"Stage\\\",\\\"x\\\":0,\\\"y\\\":0,\\\"w\\\":100,\\\"h\\\":100,\\\"param\\\":[{\\\"name\\\":\\\"next\\\",\\\"value\\\":\\\"scene3e8f3e6d-72db-45db-aca6-e88d95cb87c8\\\"}],\\\"events\\\":{\\\"event\\\":[]},\\\"image\\\":[{\\\"x\\\":0,\\\"y\\\":0,\\\"w\\\":71.11111111111111,\\\"h\\\":77.77777777777779,\\\"visible\\\":true,\\\"editable\\\":true,\\\"asset\\\":\\\"tick\\\",\\\"z-index\\\":0}],\\\"text\\\":[],\\\"shape\\\":[],\\\"hotspot\\\":[],\\\"embed\\\":[],\\\"div\\\":[],\\\"audio\\\":[],\\\"scribble\\\":[],\\\"htext\\\":[],\\\"g\\\":[],\\\"preload\\\":true},{\\\"id\\\":\\\"scene3e8f3e6d-72db-45db-aca6-e88d95cb87c8\\\",\\\"x\\\":0,\\\"y\\\":0,\\\"w\\\":100,\\\"h\\\":100,\\\"param\\\":[{\\\"name\\\":\\\"previous\\\",\\\"value\\\":\\\"Stage\\\"}],\\\"events\\\":{\\\"event\\\":[{\\\"action\\\":{\\\"type\\\":\\\"command\\\",\\\"command\\\":\\\"play\\\",\\\"asset\\\":\\\"domain_2890\\\",\\\"loop\\\":1},\\\"type\\\":\\\"enter\\\"},{\\\"action\\\":{\\\"type\\\":\\\"command\\\",\\\"command\\\":\\\"stop\\\",\\\"asset\\\":\\\"domain_2890\\\",\\\"loop\\\":1},\\\"type\\\":\\\"exit\\\"}]},\\\"image\\\":[],\\\"text\\\":[],\\\"shape\\\":[{\\\"x\\\":24.583333333333332,\\\"y\\\":17.555555555555554,\\\"w\\\":13.88888888888889,\\\"h\\\":22.22222222222222,\\\"visible\\\":true,\\\"editable\\\":true,\\\"type\\\":\\\"roundrect\\\",\\\"radius\\\":1,\\\"opacity\\\":1,\\\"fill\\\":\\\"rgb(255, 255, 0)\\\",\\\"stroke-width\\\":1,\\\"z-index\\\":0},{\\\"x\\\":39.72222222222222,\\\"y\\\":12.222222222222221,\\\"w\\\":27.77777777777778,\\\"h\\\":44.44444444444444,\\\"visible\\\":true,\\\"editable\\\":true,\\\"type\\\":\\\"ellipse\\\",\\\"opacity\\\":1,\\\"fill\\\":\\\"rgb(0,255,0)\\\",\\\"stroke-width\\\":1,\\\"z-index\\\":1},{\\\"x\\\":48.333333333333336,\\\"y\\\":58.22222222222222,\\\"w\\\":13.88888888888889,\\\"h\\\":22.22222222222222,\\\"visible\\\":true,\\\"editable\\\":true,\\\"type\\\":\\\"roundrect\\\",\\\"radius\\\":10,\\\"opacity\\\":1,\\\"fill\\\":\\\"red\\\",\\\"stroke-width\\\":1,\\\"z-index\\\":2}],\\\"hotspot\\\":[{\\\"x\\\":27.63888888888889,\\\"y\\\":47.77777777777778,\\\"w\\\":13.88888888888889,\\\"h\\\":22.22222222222222,\\\"visible\\\":true,\\\"editable\\\":true,\\\"type\\\":\\\"roundrect\\\",\\\"radius\\\":1,\\\"fill\\\":\\\"red\\\",\\\"stroke-width\\\":1,\\\"keyword\\\":\\\"\\\",\\\"hitArea\\\":true,\\\"z-index\\\":3}],\\\"embed\\\":[],\\\"div\\\":[],\\\"audio\\\":[{\\\"asset\\\":\\\"domain_2890\\\"}],\\\"scribble\\\":[],\\\"htext\\\":[],\\\"g\\\":[]}]}}\"}}}}";

	
	String invalidContentId = "LP_FT"+rn+"";
	String jsonContentClean = "{\"request\": {\"searchProperty\": \"name\",\"searchOperator\": \"startsWith\",\"searchString\": \"LP_FT_\"}}";
	
	static ClassLoader classLoader = ContentPublishWorkflowTests.class.getClassLoader();
	static URL url = classLoader.getResource("DownloadedFiles");
	static File downloadPath;
	static File path = new File(classLoader.getResource("UploadFiles/").getFile());
	
	@BeforeClass
	public static void setup() throws URISyntaxException{
		downloadPath = new File(url.toURI().getPath());		
	}	
	
	@AfterClass
	public static void end() throws IOException{
		FileUtils.cleanDirectory(downloadPath);		
	}

	// Create and bundle content

	// Create content
	@Test
	public void createAndBundleECMLContentExpectSuccess200(){
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
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Get node_id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");

		// Upload content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/uploadContent.zip")).
		when().
		post("/learning/v2/content/upload/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec());

		// Bundle created content
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body("{\"request\": {\"content_identifiers\": [\""+nodeId+"\"],\"file_name\": \"Testqa_bundle_ECML\"}}").
				when().
				post("learning/v2/content/bundle").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");	
		//System.out.println(ecarUrl);
		Assert.assertTrue(bundleValidation(ecarUrl));
	}
	
	// Bundle content without upload file
	@Test
	public void createAndBundleWithoutUploadExpect4xx(){
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
				extract().response();

		// Get node_id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");

		// Bundle created content
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body("{\"request\": {\"content_identifiers\": [\""+nodeId+"\"],\"file_name\": \"Testqa_bundle_ECML\"}}").
		when().
		post("learning/v2/content/bundle").
		then().
		log().all().
		spec(get400ResponseSpec());
	}

	// Create and Bundle APK
	@Test
	public void createAndBundleAPKContentExpectSuccess200() {
		contentCleanUp();
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidContent);
		js.getJSONObject("request").getJSONObject("content").put("osId", "org.sunbird.aser").put("mimeType", "application/vnd.android.package-archive");
		String jsonCreateValidContentAPK = js.toString();
		Response R =
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateValidContentAPK).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/content").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/uploadAPK.apk")).
		when().
		post("/learning/v2/content/upload/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec());

		// Bundle created content
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body("{\"request\": {\"content_identifiers\": [\""+nodeId+"\"],\"file_name\": \"Testqa_bundle_APK\"}}").
				when().
				post("learning/v2/content/bundle").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");
		//System.out.println(ecarUrl);
		Assert.assertTrue(bundleValidation(ecarUrl));
	}

	// Create and Bundle collection
	@Test
	public void createAndBundleCollectionExpectSuccess200(){
		contentCleanUp();
		String node1 = null;
		String node2 = null;
		int count = 1;
		while(count<=2){
			setURI();
			int rn = generateRandomInt(500, 999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_FT_T_"+rn+"").put("name", "LP_FT_T-"+rn+"");
			String jsonCreateValidChild = js.toString();
			Response R =
					given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateValidChild).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if(count==1){
				node1 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/uploadContent.zip")).
				when().
				post("/learning/v2/content/upload/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/publish/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

			}
			if(count==2){
				node2 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/ExternalJsonItemDataCdata.zip")).
				when().
				post("/learning/v2/content/upload/"+node2).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/publish/"+node2).
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
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateContentCollection).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/content").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Get collection and validate
		JsonPath jp1 = R1.jsonPath();
		String collectionNode = jp1.get("result.node_id");

		// Bundle created content
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body("{\"request\": {\"content_identifiers\": [\""+collectionNode+"\"],\"file_name\": \"Testqa_bundle_Collection\"}}").
				when().
				post("learning/v2/content/bundle").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");
		//System.out.println(ecarUrl);
		Assert.assertTrue(bundleValidation(ecarUrl));
	}
	
	// Bundle AT content
	@Test
	public void bundleAuthoringToolContentExpectSuccess200(){
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
				//log().all().
				extract().
				response();

		// Get node_id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		String versionKey = jP.get("result.versionKey");

		// Update content body
		setURI();
		jsonUpdateATContentBody = jsonUpdateATContentBody.replace("version_Key", versionKey);
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(jsonUpdateATContentBody).
		with().
		contentType("application/json").
		when().
		patch("/learning/v2/content/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		// Bundle created content
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body("{\"request\": {\"content_identifiers\": [\""+nodeId+"\"],\"file_name\": \"Testqa_bundle_ECML\"}}").
				when().
				post("learning/v2/content/bundle").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");
		Assert.assertTrue(bundleValidation(ecarUrl));
	}

	// Bundle AT content and Uploaded content
	@Test
	public void bundleATAndUploadedContentExpectSuccess200(){
		contentCleanUp();
		String node1 = null;
		String node2 = null;
		int count = 1;
		while(count<=2){
			setURI();
			int rn = generateRandomInt(500, 999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_FT_T_"+rn+"").put("name", "LP_FT_T-"+rn+"");
			String jsonCreateValidChild = js.toString();
			Response R =
					given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateValidChild).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			String versionKey;
			if(count==1){
				node1 = nodeId;
				//System.out.println(nodeId);
				
				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/uploadContent.zip")).
				when().
				post("/learning/v2/content/upload/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/publish/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

			}
			if(count==2){
				node2 = nodeId;
				versionKey = jp.get("result.versionKey");

				// Update content body
				setURI();
				jsonUpdateATContentBody = jsonUpdateATContentBody.replace("version_Key", versionKey);
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonUpdateATContentBody).
				with().
				contentType("application/json").
				when().
				patch("/learning/v2/content/"+nodeId).
				then().
				//log().all().
				spec(get200ResponseSpec());
				
				// Publish created content
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/publish/"+node2).
				then().
				//log().all().
				spec(get200ResponseSpec());
			}
			count++;
		}
		
		//Bundle both the contents
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body("{\"request\": {\"content_identifiers\": [\""+node1+"\",\""+node2+"\"],\"file_name\": \"Testqa_bundle_ECML&APK\"}}").
				when().
				post("learning/v2/content/bundle").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");
		//System.out.println(ecarUrl);		
		Assert.assertTrue(bundleValidation(ecarUrl));
	}

	// Bundle existing and non-existing content
	@Test
	public void bundleExistingAndNonExistingContentExpect4xx(){
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
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/uploadContent.zip")).
		then().
		post("/learning/v2/content/upload/"+nodeId);

		// Bundle created content
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body("{\"request\": {\"content_identifiers\": [\""+nodeId+"\",\""+invalidContentId+"\"],\"file_name\": \"Testqa_bundle_invalid\"}}").
		when().
		post("learning/v2/content/bundle").
		then().
		//log().all().
		spec(get404ResponseSpec());
	}

	// Bundle ECML and APK Contents
	@Test
	public void bundleECMLAndAPKContentsExpectSuccess200(){
		contentCleanUp();
		String node1 = null;
		String node2 = null;
		int count = 1;
		while(count<=2){
			setURI();
			int rn = generateRandomInt(500, 999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			if(count==1){
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_FT_T_"+rn+"").put("name", "LP_FT_T-"+rn+"");
			}
			if(count==2){
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_FT_T_"+rn+"").put("name", "LP_FT_T-"+rn+"").put("osId", "org.sunbird.aser").put("mimeType", "application/vnd.android.package-archive");
			}
			String jsonCreateValidChild = js.toString();
			Response R =
					given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateValidChild).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if(count==1){
				node1 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/uploadContent.zip")).
				when().
				post("/learning/v2/content/upload/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/publish/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

			}
			if(count==2){
				node2 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/uploadAPK.apk")).
				when().
				post("/learning/v2/content/upload/"+node2).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/publish/"+node2).
				then().
				//log().all().
				spec(get200ResponseSpec());
			}
			count++;
		}
		//Bundle both the contents
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body("{\"request\": {\"content_identifiers\": [\""+node1+"\",\""+node2+"\"],\"file_name\": \"Testqa_bundle_ECML&APK\"}}").
				when().
				post("learning/v2/content/bundle").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");
		Assert.assertTrue(bundleValidation(ecarUrl));
	}

	// Bundle Live and Draft contents
	@Test
	public void bundleLiveAndDraftContentsExpectSuccess200(){
		contentCleanUp();
		String node1 = null;
		String node2 = null;
		int count = 1;
		while(count<=2){
			setURI();
			int rn = generateRandomInt(500, 999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_FT_T_"+rn+"").put("name", "LP_FT_T-"+rn+"");
			String jsonCreateValidChild = js.toString();
			Response R =
					given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateValidChild).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if(count==1){
				node1 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/uploadContent.zip")).
				when().
				post("/learning/v2/content/upload/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/publish/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

			}
			if(count==2){
				node2 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/ExternalJsonItemDataCdata.zip")).
				when().
				post("/learning/v2/content/upload/"+node2).
				then().
				//log().all().
				spec(get200ResponseSpec());
			}	
			count++;
		}
		//Bundle both the contents
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body("{\"request\": {\"content_identifiers\": [\""+node1+"\",\""+node2+"\"],\"file_name\": \"Testqa_bundle_ECML&APK\"}}").
				when().
				post("learning/v2/content/bundle").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");
		//System.out.println(ecarUrl);		
		Assert.assertTrue(bundleValidation(ecarUrl));
	}

	// Bundle Live and Retired Content
	@Ignore
	@Test
	public void bundleLiveAndRetiredContentExpect4xx(){
		contentCleanUp();
		String node1 = null;
		String node2 = null;
		int count = 1;
		while(count<=2){
			setURI();
			int rn = generateRandomInt(500, 999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_FT_T_"+rn+"").put("name", "LP_FT_T-"+rn+"");
			String jsonCreateValidChild = js.toString();
			Response R =
					given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateValidChild).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if(count==1){
				node1 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/uploadContent.zip")).
				then().
				post("/learning/v2/content/upload/"+node1);

				// Publish created content
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				then().
				get("/learning/v2/content/publish/"+node1);

			}
			if(count==2){
				node2 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/ExternalJsonItemDataCdata.zip")).
				then().
				post("/learning/v2/content/upload/"+node2);
				
				// Publish created content
				setURI();
				Response R1 = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/publish/"+node2).
				then().
				extract().response();
				
				JsonPath jp1 = R1.jsonPath();
				String versionKey = jp1.get("result.versionKey");
				
				// Update status as Retired
				setURI();
				jsonUpdateContentValid = jsonUpdateContentValid.replace("Live", "Retired").replace("version_Key", versionKey);
				//System.out.println(jsonUpdateContentValid);
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonUpdateContentValid).
				with().
				contentType("application/json").
				then().
				//log().all().
				patch("/learning/v2/content/"+node2);

			}	
			count++;
		}

		//Bundle both the contents
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body("{\"request\": {\"content_identifiers\": [\""+node1+"\",\""+node2+"\"],\"file_name\": \"Testqa_bundle_Live&Retired\"}}").
		when().
		post("learning/v2/content/bundle").
		then().
		//log().all().
		spec(get400ResponseSpec());
	}

	// Bundle collection with Live, Draft and Review contents
	@Ignore
	@Test
	public void bundleCollectionWithLDRContentsExpectSuccess200(){
		contentCleanUp();
		String node1 = null;
		String node2 = null;
		String node3 = null;
		int count = 1;
		while(count<=3){
			setURI();
			int rn = generateRandomInt(500, 999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_FT_T_"+rn+"").put("name", "LP_FT_T-"+rn+"");
			String jsonCreateValidChild = js.toString();
			Response R =
					given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateValidChild).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if(count==1){
				node1 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/uploadContent.zip")).
				when().
				post("/learning/v2/content/upload/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/publish/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

			}
			if(count==2){
				node2 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/tweenAndaudioSprite.zip")).
				when().
				post("/learning/v2/content/upload/"+node2).
				then().
				//log().all().
				spec(get200ResponseSpec());

			}
			if(count==3){
				node3 = nodeId;
				String versionKey = jp.get("result.versionKey");

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/ExternalJsonItemDataCdata.zip")).
				when().
				post("/learning/v2/content/upload/"+node3).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Update status as Review
				setURI();
				jsonUpdateContentValid = jsonUpdateContentValid.replace("Live", "Review").replace("version_Key", versionKey);
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonUpdateContentValid).
				with().
				contentType("application/json").
				then().
				patch("/learning/v2/content/"+node3);
			}
			count++;
		}

		// Create collection
		setURI();
		jsonCreateThreeContentCollection = jsonCreateThreeContentCollection.replace("id1", node1).replace("id2", node2).replace("id3", node3);
		Response R1 =
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateThreeContentCollection).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/content").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Get collection and validate
		JsonPath jp1 = R1.jsonPath();
		String collectionNode = jp1.get("result.node_id");

		// Bundle created content
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body("{\"request\": {\"content_identifiers\": [\""+collectionNode+"\"],\"file_name\": \"Testqa_bundle_LDRContentCollection\"}}").
				when().
				post("learning/v2/content/bundle").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");
		//System.out.println(ecarUrl);
		Assert.assertTrue(bundleValidation(ecarUrl));
	}
	
	// Bundle collection with live and retired contents
	@Ignore
	@Test
	public void bundleCollectionWithLiveAndRetiredContentsExpect400(){
		contentCleanUp();
		String node1 = null;
		String node2 = null;
		int count = 1;
		while(count<=2){
			setURI();
			int rn = generateRandomInt(500, 999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_FT_T_"+rn+"").put("name", "LP_FT_T-"+rn+"");
			String jsonCreateValidChild = js.toString();
			Response R =
					given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateValidChild).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
					//log().all().
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if(count==1){
				node1 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/uploadContent.zip")).
				then().
				post("/learning/v2/content/upload/"+node1);

				// Publish created content
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				then().
				get("/learning/v2/content/publish/"+node1);

			}
			if(count==2){
				node2 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/ExternalJsonItemDataCdata.zip")).
				then().
				post("/learning/v2/content/upload/"+node2);

				// Publish created content
				setURI();
				Response R1 = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/publish/"+node2).
				then().
				//log().all().
				extract().response();
				
				JsonPath jp1 = R1.jsonPath();
				String versionKey = jp1.get("result.versionKey");
				
				// Update status as Retired
				setURI();
				jsonUpdateContentValid = jsonUpdateContentValid.replace("Live", "Retired").replace("version_Key", versionKey);
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonUpdateContentValid).
				with().
				contentType("application/json").
				then().
				//log().all().
				patch("/learning/v2/content/"+node2);

			}
			count++;
		}
		// Create collection
		setURI();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
		Response R1 =
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateContentCollection).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/content").
				then().
				//log().all().
				extract().
				response();

		// Get collection and validate
		JsonPath jp1 = R1.jsonPath();
		String collectionNode = jp1.get("result.node_id");

		// Bundle created content
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body("{\"request\": {\"content_identifiers\": [\""+collectionNode+"\"],\"file_name\": \"Testqa_bundle_CollectionLiveAndRetired\"}}").
		when().
		post("learning/v2/content/bundle").
		then().
		//log().all().
		spec(get200ResponseSpec());
	}
	
	// Bundle nested collection
	@Test
	public void bundleNestedCollectionExpectSuccess200(){
		contentCleanUp();
		String node1 = null;
		String node2 = null;
		int count = 1;
		while(count<=2){
			setURI();
			int rn = generateRandomInt(900, 1999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "Test_QAT_"+rn+"").put("name", "Test_QAT-"+rn+"");
			String jsonCreateValidChild = js.toString();
			Response R =
					given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateValidChild).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if(count==1){
				node1 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/uploadContent.zip")).
				when().
				post("/learning/v2/content/upload/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/publish/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

			}
			if(count==2){
				node2 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpec(uploadContentType, validuserId)).
				multiPart(new File(path+"/tweenAndaudioSprite.zip")).
				when().
				post("/learning/v2/content/upload/"+node2).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpec(contentType, validuserId)).
				when().
				get("/learning/v2/content/publish/"+node2).
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
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateContentCollection).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/content").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId1 = jP1.get("result.node_id");

		// Publish collection
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
		get("/learning/v2/content/publish/"+nodeId1).
		then().
		//log().all().
		spec(get200ResponseSpec());

		// Create nested collection
		setURI();
		jsonCreateNestedCollection = jsonCreateNestedCollection.replace("id1", nodeId1);
		Response R3 =
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateNestedCollection).
				with().
				contentType(JSON).
				when().
				post("/learning/v2/content").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP3 = R3.jsonPath();
		String collectionId = jP3.get("result.node_id");

		// Publish collection
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
		get("/learning/v2/content/publish/"+collectionId).
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		// Bundle created content
		setURI();
		Response R2 = 
				given().
				spec(getRequestSpec(contentType, validuserId)).
				body("{\"request\": {\"content_identifiers\": [\""+collectionId+"\"],\"file_name\": \"Testqa_bundle_nestedCollection\"}}").
				when().
				post("learning/v2/content/bundle").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP2 = R2.jsonPath();
		String ecarUrl = jP2.get("result.ECAR_URL");
		//System.out.println(ecarUrl);
		Assert.assertTrue(bundleValidation(ecarUrl));
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

	@SuppressWarnings({ "unused" })
	private boolean bundleValidation(String ecarUrl) throws ClassCastException{
		double manifestVersionActual = 1.2;
		boolean bundleValidation = true;
		try{
			String bundleName = "bundle_"+rn+"";
			
			// Downloading the Ecar from ecar url
			FileUtils.copyURLToFile(new URL(ecarUrl), new File(downloadPath+"/"+bundleName+".zip"));		
			String bundlePath = downloadPath+"/"+bundleName+".zip";
			
			// Setting up extract path
			File bundleExtract = new File(downloadPath+"/"+bundleName);
			String bundleExtractPath = bundleExtract.getPath();
			
			try{
				// Unzip the file
				ZipFile bundleZip = new ZipFile(bundlePath);
				bundleZip.extractAll(bundleExtractPath);
				
				File fileName = new File(bundleExtractPath);
				File[] listofFiles = fileName.listFiles();

				// Validating the folders having zip file or not
				for(File file : listofFiles){
					//System.out.println(file.getName());
					if (file.isFile() && file.getName().endsWith("json")){
						// Reading the manifest		
						File manifest = new File(file.getPath());
						//Gson gson = new Gson();
						JsonParser parser = new JsonParser();
			            JsonElement jsonElement = parser.parse(new FileReader(manifest));
						JsonObject obj = jsonElement.getAsJsonObject();
						JsonElement manifestVersionElement = obj.get("ver"); 
						Double manifestVersion = manifestVersionElement.getAsDouble();
						Assert.assertTrue(manifestVersion.equals(manifestVersionActual));
						
						// Validating expiry and items
						JsonObject arc = obj.getAsJsonObject("archive");
						if (arc.has("expires") && arc.has("items")){
							JsonArray items = arc.getAsJsonArray("items");
							
					        @SuppressWarnings("rawtypes")
							Iterator i = items.iterator();
					        while(i.hasNext()) {
								try {
									
									// Validating download url, status and package version
									JsonObject item = (JsonObject) i.next();
									JsonElement downloadUrlElement = item.get("downloadUrl");
									String contentTypeElement = getStringValue(item, "contentType");
									if(contentTypeElement.equals("Collection")){
										downloadUrlElement = downloadUrlElement.getAsJsonNull();
										Assert.assertTrue(downloadUrlElement.isJsonNull());
									}
									else {
										Assert.assertTrue(downloadUrlElement!=null);
										String downloadUrl = downloadUrlElement.getAsString();
									}
									JsonElement statusElement = item.get("status");
									String status = statusElement.getAsString();
									JsonElement pkgVersionElement = item.get("pkgVersion");
									Float pkgVersion = pkgVersionElement.getAsFloat();
									if (status.equals("draft")||status.equals("review")){
										Assert.assertTrue(pkgVersion.equals("0"));
									}
									}			
									 catch (Exception classCastException){
								        	classCastException.printStackTrace();
											 return false;
								        }
									}
								}
							}
							else if (file.isDirectory()){
								File[] listofsubFiles = file.listFiles();
								for(File newfile : listofsubFiles){
									String fName = newfile.getName();
									if (fName.endsWith(".zip")|| fName.endsWith(".rar")){
										//System.out.println(fName);
									}
								}
							}
						}				
					}
					catch(Exception zipExtract) {
					zipExtract.printStackTrace();
					 return false;
					}
				}
				catch (Exception e){
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

}
