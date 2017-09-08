package org.ekstep.platform.content;


	import static com.jayway.restassured.RestAssured.given;
	import static com.jayway.restassured.http.ContentType.JSON;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
	import java.io.IOException;
import java.io.InputStreamReader;
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
	//import org.hamcrest.CoreMatchers;
	import org.json.JSONObject;
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
	import com.jayway.restassured.builder.RequestSpecBuilder;
	import com.jayway.restassured.path.json.JsonPath;
	import com.jayway.restassured.response.Response;
	import com.jayway.restassured.specification.RequestSpecification;

import net.lingala.zip4j.core.ZipFile;


	public class ContentPublishV3TestCases extends BaseTest{
	
		public RequestSpecification getRequestSpecification(String content_type,String user_id, String APIToken)
		{
			RequestSpecBuilder builderreq = new RequestSpecBuilder();
			builderreq.addHeader("Content-Type", content_type);
			builderreq.addHeader("user-id", user_id);
			builderreq.addHeader("Authorization", APIToken);
			RequestSpecification requestSpec = builderreq.build();
			return requestSpec;
		}
		int rn = generateRandomInt(0, 9999999);
		String jsonCreateValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_"+rn+"\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_NFT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
		String jsonCreateValidContentWithConcept = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_"+rn+"\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\"\"concepts\":[{\"identifier\":\"LO1\",\"name\":\"Word Meaning\",\"objectType\":\"Concept\",\"relation\":\"associatedTo\",\"description\":\"Understanding meaning of words\",\"index\":null,\"status\":null,\"depth\":null,\"mimeType\":null,\"visibility\":null}],,\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_NFT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
		String jsonCreateContentCollection = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_Collection_"+rn+"\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"description\": \"Test_QA\",\"name\": \"LP_NFT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Collection\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"owner\": \"EkStep\", \"children\": [{ \"identifier\": \"id1\"}, { \"identifier\": \"id2\"}]}}}";
		String jsonCreateTextbookUnit = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_Collection_"+rn+"\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_NFT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"TextBookUnit\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"owner\": \"EkStep\", \"children\": [{\"identifier\": \"id1\"},{ \"identifier\": \"id2\"},{\"identifier\": \"id3\"},{\"identifier\": \"id4\"}]}}}";
		String jsonUpdateContentValid = "{\"request\": {\"content\": {\"versionKey\": \"version_Key\", \"status\": \"Live\"}}}";
		String jsonGetContentList = "{\"request\": { \"search\": {\"tags\":[\"LP_functionalTest\"],\"limit\": 5000}}}";
		String jsonCreateNestedCollection = "{\"request\": {\"content\": {\"identifier\": \"Test_QANested_"+rn+"\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_NFT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Collection\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"owner\": \"EkStep\", \"children\": [{ \"identifier\": \"id1\"}]}}}";
		String jsonCreateInvalidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_"+rn+"\",\"osId\": \"org.ekstep.app\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_NFT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.archive\",\"pkgVersion\": 3,\"tags\":[\"LP_functionalTest\"]}}}";
		String jsonUpdateATContentBody = "{\"request\": {\"content\": {\"versionKey\": \"version_Key\", \"body\": {\"theme\":{\"manifest\":{\"media\":[{\"id\":\"tick\",\"type\":\"image\",\"src\":\"https://qa.ekstep.in/assets/public/content/1455104185970tick.png\",\"assetId\":\"tick\"},{\"id\":\"domain_2890\",\"type\":\"audio\",\"src\":\"https://qa.ekstep.in/assets/public/content/%C3%A0%C2%B2%C2%9A_1463054756900.mp3\",\"assetId\":\"domain_2890\"},{\"id\":\"validate\",\"src\":\"https://qa.ekstep.in/assets/public/content/btn_ok_highlights_1460705843676.png\",\"type\":\"image\",\"assetId\":\"domain_38852\"},{\"id\":\"popupTint\",\"src\":\"https://qa.ekstep.in/assets/public/content/PopupTint_1460636175572.png\",\"type\":\"image\",\"assetId\":\"domain_38606\"},{\"id\":\"goodjobBg\",\"src\":\"https://qa.ekstep.in/assets/public/content/goodjobBg_1460727428389.png\",\"type\":\"image\",\"assetId\":\"domain_38939\"},{\"id\":\"retryBg\",\"src\":\"https://qa.ekstep.in/assets/public/content/retryBg_1460727370746.png\",\"type\":\"image\",\"assetId\":\"domain_38938\"},{\"id\":\"retry_audio\",\"src\":\"https://qa.ekstep.in/assets/public/content/retry_1460636610607.mp3\",\"type\":\"sound\",\"assetId\":\"domain_38624\"},{\"id\":\"goodjob_audio\",\"src\":\"https://qa.ekstep.in/assets/public/content/goodJob_1460636677521.mp3\",\"type\":\"sound\",\"assetId\":\"domain_38625\"},{\"id\":\"next\",\"src\":\"https://qa.ekstep.in/assets/public/content/btn_next_1461401649059.png\",\"type\":\"image\",\"assetId\":\"domain_40358\"},{\"id\":\"previous\",\"src\":\"https://qa.ekstep.in/assets/public/content/btn_back_1461401700215.png\",\"type\":\"image\",\"assetId\":\"domain_40359\"},{\"id\":\"submit\",\"src\":\"https://qa.ekstep.in/assets/public/content/icon_submit_1459243202199.png\",\"type\":\"image\",\"assetId\":\"domain_14524\"},{\"id\":\"home\",\"src\":\"https://qa.ekstep.in/assets/public/content/icon_home_1459242981364.png\",\"type\":\"image\",\"assetId\":\"domain_14519\"},{\"id\":\"reload\",\"src\":\"https://qa.ekstep.in/assets/public/content/icon_reload_1459243110661.png\",\"type\":\"image\",\"assetId\":\"domain_14522\"},{\"id\":\"icon_hint\",\"src\":\"https://qa.ekstep.in/assets/public/content/icon_hint_1454918891133.png\",\"type\":\"image\",\"assetId\":\"domain_799\"},{\"id\":\"bg\",\"src\":\"https://qa.ekstep.in/assets/public/content/background_1458729298020.png\",\"type\":\"image\"}]},\"id\":\"theme\",\"ver\":0.2,\"startStage\":\"Stage\",\"controller\":[{\"name\":\"dictionary\",\"type\":\"data\",\"id\":\"dictionary\",\"__cdata\":{}}],\"template\":[{\"image\":[{\"event\":{\"action\":{\"type\":\"command\",\"command\":\"show\",\"asset\":\"retryDialog\"},\"type\":\"click\"},\"asset\":\"popupTint\",\"x\":-100,\"y\":-150,\"w\":550,\"h\":600,\"visible\":true,\"id\":\"popup-Tint\"},{\"asset\":\"retryBg\",\"x\":0,\"y\":0,\"w\":150,\"h\":150,\"visible\":true,\"id\":\"right\"}],\"shape\":[{\"event\":{\"action\":[{\"type\":\"command\",\"command\":\"hide\",\"asset\":\"retryDialog\"},{\"type\":\"command\",\"command\":\"SHOWHTMLELEMENTS\",\"asset\":\"retry\"}],\"type\":\"click\"},\"type\":\"roundrect\",\"x\":72,\"y\":25,\"w\":50,\"h\":65,\"visible\":true,\"id\":\"retry\",\"hitArea\":true},{\"event\":{\"action\":{\"type\":\"command\",\"command\":\"transitionTo\",\"asset\":\"theme\",\"param\":\"next\",\"effect\":\"fadein\",\"direction\":\"left\",\"ease\":\"linear\",\"duration\":100},\"type\":\"click\"},\"type\":\"roundrect\",\"x\":110,\"y\":100,\"w\":25,\"h\":35,\"visible\":true,\"id\":\"continue\",\"hitArea\":true}],\"id\":\"retry\"},{\"g\":{\"image\":[{\"asset\":\"popupTint\",\"x\":0,\"y\":0,\"w\":100,\"h\":100,\"visible\":true,\"id\":\"popup-Tint\"}],\"text\":[{\"x\":25,\"y\":25,\"w\":50,\"h\":9,\"visible\":true,\"editable\":true,\"model\":\"word.lemma\",\"weight\":\"normal\",\"font\":\"helvetica\",\"color\":\"rgb(0,0,0)\",\"fontstyle\":\"\",\"fontsize\":75,\"align\":\"left\",\"z-index\":1,\"id\":\"lemma\"},{\"x\":25,\"y\":35,\"w\":50,\"h\":40,\"visible\":true,\"editable\":true,\"model\":\"word.gloss\",\"weight\":\"normal\",\"font\":\"helvetica\",\"color\":\"rgb(0,0,0)\",\"fontstyle\":\"\",\"fontsize\":43,\"align\":\"left\",\"z-index\":2,\"id\":\"gloss\"}],\"shape\":[{\"x\":20,\"y\":20,\"w\":60,\"h\":60,\"visible\":true,\"editable\":true,\"type\":\"roundrect\",\"radius\":10,\"opacity\":1,\"fill\":\"#45b3a5\",\"stroke-width\":1,\"z-index\":0,\"id\":\"textBg\"}],\"x\":0,\"y\":0,\"w\":100,\"h\":100,\"event\":{\"action\":[{\"type\":\"command\",\"command\":\"SHOWHTMLELEMENTS\",\"asset\":\"textBg\"},{\"type\":\"command\",\"command\":\"hide\",\"parent\":true}],\"type\":\"click\"}},\"id\":\"infoTemplate\"},{\"image\":[{\"event\":{\"action\":{\"type\":\"command\",\"command\":\"show\",\"asset\":\"\"},\"type\":\"click\"},\"asset\":\"popupTint\",\"x\":-100,\"y\":-150,\"w\":550,\"h\":600,\"visible\":true,\"id\":\"popup-Tint\"},{\"event\":{\"action\":[{\"type\":\"command\",\"command\":\"transitionTo\",\"asset\":\"theme\",\"param\":\"next\",\"effect\":\"fadein\",\"direction\":\"left\",\"ease\":\"linear\",\"duration\":500}],\"type\":\"click\"},\"asset\":\"goodjobBg\",\"x\":0,\"y\":0,\"w\":150,\"h\":150,\"visible\":true,\"id\":\"continue\"}],\"id\":\"goodjob\"}],\"stage\":[{\"id\":\"Stage\",\"x\":0,\"y\":0,\"w\":100,\"h\":100,\"param\":[{\"name\":\"next\",\"value\":\"scene3e8f3e6d-72db-45db-aca6-e88d95cb87c8\"}],\"events\":{\"event\":[]},\"image\":[{\"x\":0,\"y\":0,\"w\":71.11111111111111,\"h\":77.77777777777779,\"visible\":true,\"editable\":true,\"asset\":\"tick\",\"z-index\":0}],\"text\":[],\"shape\":[],\"hotspot\":[],\"embed\":[],\"div\":[],\"audio\":[],\"scribble\":[],\"htext\":[],\"g\":[],\"preload\":true},{\"id\":\"scene3e8f3e6d-72db-45db-aca6-e88d95cb87c8\",\"x\":0,\"y\":0,\"w\":100,\"h\":100,\"param\":[{\"name\":\"previous\",\"value\":\"Stage\"}],\"events\":{\"event\":[{\"action\":{\"type\":\"command\",\"command\":\"play\",\"asset\":\"domain_2890\",\"loop\":1},\"type\":\"enter\"},{\"action\":{\"type\":\"command\",\"command\":\"stop\",\"asset\":\"domain_2890\",\"loop\":1},\"type\":\"exit\"}]},\"image\":[],\"text\":[],\"shape\":[{\"x\":24.583333333333332,\"y\":17.555555555555554,\"w\":13.88888888888889,\"h\":22.22222222222222,\"visible\":true,\"editable\":true,\"type\":\"roundrect\",\"radius\":1,\"opacity\":1,\"fill\":\"rgb(255, 255, 0)\",\"stroke-width\":1,\"z-index\":0},{\"x\":39.72222222222222,\"y\":12.222222222222221,\"w\":27.77777777777778,\"h\":44.44444444444444,\"visible\":true,\"editable\":true,\"type\":\"ellipse\",\"opacity\":1,\"fill\":\"rgb(0,255,0)\",\"stroke-width\":1,\"z-index\":1},{\"x\":48.333333333333336,\"y\":58.22222222222222,\"w\":13.88888888888889,\"h\":22.22222222222222,\"visible\":true,\"editable\":true,\"type\":\"roundrect\",\"radius\":10,\"opacity\":1,\"fill\":\"red\",\"stroke-width\":1,\"z-index\":2}],\"hotspot\":[{\"x\":27.63888888888889,\"y\":47.77777777777778,\"w\":13.88888888888889,\"h\":22.22222222222222,\"visible\":true,\"editable\":true,\"type\":\"roundrect\",\"radius\":1,\"fill\":\"red\",\"stroke-width\":1,\"keyword\":\"\",\"hitArea\":true,\"z-index\":3}],\"embed\":[],\"div\":[],\"audio\":[{\"asset\":\"domain_2890\"}],\"scribble\":[],\"htext\":[],\"g\":[]}]}}}}}";
		String jsonUpdateChildren	= "{\"request\":{\"content\":{\"children\":[],\"versionKey\":\"version_Key\"}}}";
		String jsonPublishContent = "{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}";
		
		String invalidContentId = "LP_NFT"+rn+"";
		String malformedXMLBody = "xml version=\"1.0\" ";
		String malformedJSONBody = "{\"theme\":{\"manifes77\",\"scribble\":[],\"htext\":[],\"g\":[]}";
		String jsonContentClean = "{\"request\": {\"searchProperty\": \"name\",\"searchOperator\": \"startsWith\",\"searchString\": \"LP_NFT\"}}";
		
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
		
		@Before
		public void cleanup() {
			contentCleanUp();
		}
		
		
//		@Test
//		public void getContentType(){
//		File file = new File("/Users/purnima/Desktop/contentPublish.txt");
//			FileInputStream fis = null;
//			BufferedReader reader = null;
//			try {
//				fis = new FileInputStream(file);
//				reader = new BufferedReader(new InputStreamReader(fis));
//				int count = 1;
//				System.out.println(count);
//				for (int i=1; i<=count; i++){
//					String identifier = reader.readLine();
//					setURI();
//					Response R =
//					given().
//					spec(getRequestSpecification(contentType, validuserId, APIToken)).
//					body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Ekstep\"}}}").
//					when().
//					post("/content/v3/publish/" + identifier).
//					then().
//					log().all().
//					extract().response();
//					
//					JsonPath jp = R.jsonPath();
//					String nodeId = jp.get("result.node_id");
//				}
//			}
//				catch (IOException e) {
//					e.printStackTrace();
//				} 
//				}
//					if(domain == null){
//						System.out.println("Identifer"+identifier+ "Domain:NULL");
//						setURI();
//						given().
//						spec(getRequestSpecification(contentType, userId, APIToken)).
//						body("{\"request\":{\"content\":{\"versionKey\":\"oM2wLF8Eq3vRnt3qZ0+Y/sWQnH5pz3bHpciFo3EIifZIcP5RW4qHw8sZztCzv87M\",\"contentType\":\"Story\"}}}").
//						when().
//						patch("/content/v3/update/"+identifier).
//						then().
//						log().all().
//						spec(get200ResponseSpec());				
//						
//					}
//					else if(domain.contains("literacy")||domain.contains("science")){
//						System.out.println("Identifer"+identifier+ "Domain:"+domain);
//						setURI();
//						given().
//						spec(getRequestSpecification(contentType, userId, APIToken)).
//						body("{\"request\":{\"content\":{\"versionKey\":\"oM2wLF8Eq3vRnt3qZ0+Y/sWQnH5pz3bHpciFo3EIifZIcP5RW4qHw8sZztCzv87M\",\"contentType\":\"Story\"}}}").
//						when().
//						patch("/content/v3/update/"+identifier).
//						then().
//						log().all().
//						spec(get200ResponseSpec());				
//					}
//					else if(domain.contains("numeracy")){
//						System.out.println("Identifer"+identifier+ "Domain:"+domain);
//						setURI();
//						given().
//						spec(getRequestSpecification(contentType, userId, APIToken)).
//						body("{\"request\":{\"content\":{\"versionKey\":\"oM2wLF8Eq3vRnt3qZ0+Y/sWQnH5pz3bHpciFo3EIifZIcP5RW4qHw8sZztCzv87M\",\"contentType\":\"Worksheet\"}}}").
//						when().
//						patch("/content/v3/update/"+identifier).
//						then().
//						log().all().
//						spec(get200ResponseSpec());
//					}
//				}
//			} 
//	            catch (IOException e) {
//				e.printStackTrace();
//			} finally {
//				try {
//					if (fis != null)
//						fis.close();
//				} catch (IOException ex) {
//					ex.printStackTrace();
//				}
//			}
//	     }
	
				
		// Create and get ECML Content
		@Test
		public void createValidEcmlContentExpectSuccess200(){
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
					log().all().
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
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP1 = R1.jsonPath();
			String identifier = jP1.get("result.content.identifier");
			String versionKey = jP1.get("result.content.versionKey");
			Assert.assertTrue(versionKey!=null);
			Assert.assertEquals(ecmlNode, identifier);
		}

		@Ignore
		public void createContentWithValidConceptExpectSuccess200(){
			setURI();
			Response R =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonCreateValidContentWithConcept).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					log().all().
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
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP1 = R1.jsonPath();
			String identifier = jP1.get("result.content.identifier");
			String versionKey = jP1.get("result.content.versionKey");
			Assert.assertTrue(versionKey!=null);
			Assert.assertEquals(ecmlNode, identifier);

		}
		// Create and get valid HTML
		@Test
		public void createValidHTMLContentExpectSuccess200(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("mimeType", "application/vnd.ekstep.html-archive");
			String jsonCreateValidContentHtml = js.toString();
			Response R =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonCreateValidContentHtml).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String htmlNode = jp.get("result.node_id");

			// Get content and check
			setURI();
			Response R1 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+htmlNode).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP1 = R1.jsonPath();
			String status = jP1.get("result.content.status");
			String versionKey = jP1.get("result.content.versionKey");
			Assert.assertTrue(versionKey!=null);
			Assert.assertEquals(status, "Draft");
		}
		
		// Create and get valid APK
		@Test
		public void createValidAPKContentExpectSuccess200(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("mimeType", "application/vnd.android.package-archive");
			String jsonCreateValidContentAPK = js.toString();
			Response R =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonCreateValidContentAPK).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String apkNode = jp.get("result.node_id");

			// Get content and check
			setURI();
			Response R1 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+apkNode).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP1 = R1.jsonPath();
			String identifier = jP1.get("result.content.identifier");
			String versionKey = jP1.get("result.content.versionKey");
			Assert.assertTrue(versionKey!=null);
			Assert.assertEquals(apkNode, identifier);
		}

		// Create and get new collection
		@Test
		public void createValidCollectionExpectSuccess200(){
			String node1 = null;
			String node2 = null;
			int count = 1;
			while(count<=2){
				setURI();
				int rn = generateRandomInt(2000, 29999);
				JSONObject js = new JSONObject(jsonCreateValidContent);
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFTT_"+rn+"").put("name", "LP_NFTT-"+rn+"");
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
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				// Extracting the JSON path
				JsonPath jp = R.jsonPath();
				String nodeId = jp.get("result.node_id");
				if(count==1){
					node1 = nodeId;
				}
				if(count==2){
					node2 = nodeId;
				}
				count++;
			}
			// Create collection
			setURI();
			jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
			Response R1 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonCreateContentCollection).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Get collection and validate
			JsonPath jp1 = R1.jsonPath();
			String collectionNode = jp1.get("result.node_id");

			// Get collection
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+collectionNode).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP2 = R2.jsonPath();
			ArrayList<String> identifiers = jP2.get("result.content.children.identifier");
			String versionKey = jP2.get("result.content.versionKey");
			Assert.assertTrue(versionKey!=null);
			Assert.assertTrue(identifiers.contains(node1)&&identifiers.contains(node2));		
		}
		
		// Create Invalid content
		@Test
		public void createInvalidContentExpects400()
		{
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body(jsonCreateInvalidContent).
			with().
				contentType(JSON).
			when().
				post("learning/v2/content").
			then().
				spec(get400ResponseSpec());
		}
		
		// Create content with invalid mimeType
		@Test
		public void createInValidmimeTypeContentExpectSuccess200(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("mimeType", "application/vnd.ekstep.-archive");
			String jsonCreateValidContentHtml = js.toString();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body(jsonCreateValidContentHtml).
			with().
			contentType(JSON).
			when().
			post("content/v3/create").
			then().
			//log().all().
			spec(get400ResponseSpec());
		}
		
		// Create content with invalid contentType
		@Test
		public void createInValidcontentTypeContentExpectSuccess200(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("contentType", "TestContentType01");
			String jsonCreateValidContentHtml = js.toString();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body(jsonCreateValidContentHtml).
			with().
			contentType(JSON).
			when().
			post("content/v3/create").
			then().
			//log().all().
			spec(get400ResponseSpec());
		}

		
		// Create Existing content
		@Test
		public void createExistingContentExpect400(){
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
			String ecmlNode = jp.get("result.node_id");

			// Create content with the same identifier
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", ecmlNode);
			String jsonCreateExistingContent = js.toString();
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body(jsonCreateExistingContent).
			with().
			contentType(JSON).
			when().
			post("content/v3/create").
			then().
			//log().all().
			spec(get400ResponseSpec());


		}
		
		// Create collection with invalid content

		// Create content
		@Test
		public void createInvalidCollectionExpect400(){
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
			String ecmlNode = jp.get("result.node_id");

			// Create collection
			setURI();
			jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", ecmlNode).replace("id2", invalidContentId);
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body(jsonCreateContentCollection).
			with().
			contentType(JSON).
			when().
			post("content/v3/create").
			then().
			//log().all().
			spec(get400ResponseSpec());
		}		
		
		// Update and get list
		@Test
		public void updateValidContentExpectSuccess200(){
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

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			String versionKey = jp.get("result.versionKey");
			
			// Update content status to live
			setURI();
			jsonUpdateContentValid = jsonUpdateContentValid.replace("version_Key", versionKey);
			Response nR = 
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body(jsonUpdateContentValid).
			with().
			contentType("application/json").
			when().
			patch("/content/v3/update/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec()).
			extract().response();
			
			// Extracting the JSON path
			JsonPath njP = nR.jsonPath();
			String versionKey1 = njP.get("result.versionKey");
			Assert.assertFalse(versionKey.equals(versionKey1));

			// Get content list and check for content
			setURI();
			Response R1 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonGetContentList).
					with().
					contentType("application/json").
					when().
					post("/content/v3/list").
					then().
					extract().
					response();

			// Validate the response
			JsonPath jp1 = R1.jsonPath();
			ArrayList<String> identifier = jp1.get("result.content.identifier");
			System.out.println(identifier);
			Assert.assertTrue((identifier).contains(nodeId));

			// Update status as Retired
			setURI();
			jsonUpdateContentValid = jsonUpdateContentValid.replace("Live", "Retired").replace(versionKey, versionKey1);
			Response nR1 =
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body(jsonUpdateContentValid).
			with().
			contentType("application/json").
			when().
			patch("/content/v3/update/"+nodeId).
			then().
			//log().all().
			//spec(get200ResponseSpec()).
			extract().
			response();
			
			// Extracting the JSON path
			JsonPath njP1 = nR1.jsonPath();
			String versionKey2 = njP1.get("result.versionKey");
			Assert.assertFalse(versionKey1.equals(versionKey2));

			// Get content list and check for content
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonGetContentList).
					with().
					contentType("application/json").
					when().
					post("/content/v3/list").
					then().
					extract().
					response();

			// Validate the response
			JsonPath jp2 = R2.jsonPath();
			ArrayList<String> identifier2 = jp2.get("result.content.identifier");
			System.out.println(nodeId +identifier2);
			Assert.assertTrue((identifier2).contains(nodeId));

			// Update content with Review status
			setURI();
			jsonUpdateContentValid = jsonUpdateContentValid.replace("Live", "Review").replace(versionKey1, versionKey2);
			Response nR2 =
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body(jsonUpdateContentValid).
			with().
			contentType("application/json").
			when().
			patch("/content/v3/update/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec()).
			extract().
			response();
			
			// Extracting the JSON path
			JsonPath njP2 = nR2.jsonPath();
			String versionKey3 = njP2.get("result.versionKey");
			Assert.assertFalse(versionKey2.equals(versionKey3));
			
			// Get content list and check for content
			setURI();
			Response R3 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonGetContentList).
					with().
					contentType("application/json").
					when().
					post("/content/v3/list").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Validate the response
			JsonPath jp3 = R3.jsonPath();
			ArrayList<String> identifier3 = jp3.get("result.content.identifier");
			Assert.assertTrue((identifier3).contains(nodeId));
		}
		
		// Update content with metadata changes
		@Test
		public void updateMetaDataExpectSuccess200(){
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

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			
			// Upload Content
			setURI();
			given().
			spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
			multiPart(new File(path+"/uploadContent.zip")).
			when().
			post("/content/v3/upload/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Publish created content
			setURI();
			Response R1 =
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec()).
			extract().response();
			
			JsonPath jP1 = R1.jsonPath();
			String versionKey = jP1.get("result.versionKey");

			// Update content metadata
			setURI();
			JSONObject js = new JSONObject(jsonUpdateContentValid);
			js.getJSONObject("request").getJSONObject("content").put("language", "[\"Tamil\", \"Telugu\"]").remove("status");
			jsonUpdateContentValid = jsonUpdateContentValid.replace("version_Key", versionKey);
			System.out.println(jsonUpdateContentValid);
			Response nR = 
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body(jsonUpdateContentValid).
			with().
			contentType("application/json").
			when().
			patch("/content/v3/update/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec()).
			extract().response();
			
			// Extracting the JSON path
			JsonPath njP = nR.jsonPath();
			String versionKey1 = njP.get("result.versionKey");
			Assert.assertFalse(versionKey.equals(versionKey1));
			
			// Publish the content
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());
			
			// Get and validate the change
			setURI();
			Response R2 =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			when().
			get("content/v3/read/"+nodeId).
			then().
			log().all().
			spec(get200ResponseSpec()).
			extract().response();
			
			JsonPath jP2 = R2.jsonPath();
			ArrayList<String> language = jP2.get("result.content.language");
			Assert.assertTrue(language.contains("Tamil") && (language.contains("Telugu")));
		}
		
		// Upload file without index
		
		//Create content
		@Test
		public void uploadContentWithoutIndexExpect400(){
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
		
			// Upload Content
			setURI();
			given().
			spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
			multiPart(new File(path+"/UploadWithoutIndex.zip")).
			when().
			post("/content/v3/upload/"+nodeId).
			then().
			//log().all().
			spec(get400ResponseSpec());
		}

		// Upload file with invalid ecml
		
		//Create content
		@Test
		public void uploadContentWithInvalidEcmlExpect400(){
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
		
			// Upload Content
			setURI();
			given().
			spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
			multiPart(new File(path+"/uploadInvalidEcml.zip")).
			when().
			post("/content/v3/upload/"+nodeId).
			then().
			//log().all().
			spec(get400ResponseSpec());
		}

		//Upload html content without index.html
		
		//Create content
		@Test
		public void uploadHTMLContentWithoutIndexExpect400(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("mimeType", "application/vnd.ekstep.html-archive");
			String jsonCreateValidContentHtml = js.toString();
			Response R =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonCreateValidContentHtml).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					//log().all().
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
			multiPart(new File(path+"/Build-a-sentence.zip")).
			when().
			post("/content/v3/upload/"+nodeId).
			then().
			//log().all().
			spec(get400ResponseSpec());
		}
		
		// Upload file with invalid mimeType
		
		//Create content
		@Test
		public void uploadContentWithInvalidmimeTypeExpect400(){
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
		
			// Upload Content
			setURI();
			given().
			spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
			multiPart(new File(path+"/uploadApk.apk")).
			when().
			post("/content/v3/upload/"+nodeId).
			then().
			//log().all().
			spec(get400ResponseSpec());
		}

		// Upload file with Empty zip
		
		//Create content
		@Test
		public void uploadContentWithEmptyZipExpect400(){
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
		
			// Upload Content
			setURI();
			given().
			spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
			multiPart(new File(path+"/haircut_empty.zip")).
			when().
			post("/content/v3/upload/"+nodeId).
			then().
			//log().all().
			spec(get400ResponseSpec());
		}
		
		// Upload with invalid json
		
			//Create content
			@Test
			public void uploadContentWithInvalidJSONExpect400(){
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
			
				// Upload Content
				setURI();
				given().
				spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
				multiPart(new File(path+"/ecmlCorruptedJSON.zip")).
				when().
				post("/content/v3/upload/"+nodeId).
				then().
				//log().all().
				spec(get400ResponseSpec());
			}

		//Upload file more than 50 MB and assets above 20 MB
		
		//Create content
		@Ignore
		public void uploadContentAboveLimitExpect400(){
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
		
			// Upload Content
			setURI();
			given().
			spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
			multiPart(new File(path+"/contentAbove50MB.zip")).
			when().
			post("/content/v3/upload/"+nodeId).
			then().
			log().all().
			spec(get400ResponseSpec());
		}

		// Upload File with missing assets
		//Create content
		@Test
		public void uploadContentWithMissingAssetsExpect400(){
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
		
			// Upload Content
			setURI();
			given().
			spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
			multiPart(new File(path+"/haircut_withoutAssets.zip")).
			when().
			post("/content/v3/upload/"+nodeId).
			then().
			//log().all().
			spec(get400ResponseSpec());
		}

		// Upload invalid file
		
		//Create content
		@Test
		public void uploadContentInvalidFileExpect400(){
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
		
			// Upload Content
			setURI();
			given().
			spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
			multiPart(new File(path+"/carpenter.png")).
			when().
			post("/content/v3/upload/"+nodeId).
			then().
			//log().all().
			spec(get400ResponseSpec());
		}

		// Create and upload image asset valid
		
		/*// Create and upload Invalid audio asset
		
		//Create content
		@Test
		public void uploadandPublishInvalidAudioAssetExpect4xx(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("contentType", "Asset").put("mimeType", "audio/mp3");
			String jsonCreateImageAssetInvalid = js.toString();
			Response R =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonCreateImageAssetInvalid).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");
		
		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
		multiPart(new File(path+"/pngImage.png")).
		when().	
		post("/content/v3/upload/"+nodeId).
		then().
		//log().all().
		spec(get400ResponseSpec());
		}
		
		// Create and upload invalid mimeType audio asset
		
		//Create content
		@Test
		public void uploadandPublishInvalidmimeTypeAudioAssetExpect4xx(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("contentType", "Asset").put("mimeType", "audio/mp3");
			String jsonCreateImageAssetInvalid = js.toString();
			Response R =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonCreateImageAssetInvalid).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");
		
		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
		multiPart(new File(path+"/Oggaudio.ogg")).
		when().	
		post("/content/v3/upload/"+nodeId).
		then().
		//log().all().
		spec(get400ResponseSpec());
		} */
		
		// Upload valid content expect success
		
		//Create content
		@Test
		public void uploadandPublishContentExpectSuccess200(){
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
					spec(get200ResponseSpec()).
					extract().
					response();
		
			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
		
			// Upload Content
			setURI();
			//System.out.println(path);
			given().
			spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
			multiPart(new File(path+"/uploadContent.zip")).
			when().	
			post("/content/v3/upload/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());
		
			// Get body and validate
		
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId+"?fields=body").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();
		
			JsonPath jP2 = R2.jsonPath();
			String body = jP2.get("result.content.body");
			Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
			if (isValidXML(body) || isValidJSON(body)){
				Assert.assertTrue(accessURL(nodeId));
			}
		}

		// Create, upload and review with valid content
		
		//Create content
		@Test
		public void reviewContentExpectSuccess200(){
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
					spec(get200ResponseSpec()).
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
			when().
			post("/content/v3/upload/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());
		
			// Get body and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId+"?fields=body").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();
		
			JsonPath jP2 = R2.jsonPath();
			String body = jP2.get("result.content.body");
			Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
			if (isValidXML(body) || isValidJSON(body)){
				
				// Setting status to review
				setURI();
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body("{\"request\":{\"content\":{}}}").
				when().
				post("/content/v3/review/"+nodeId).
				then().
				//log().all().
				spec(get200ResponseSpec());
				
				// Get content and validate
				setURI();
				Response R1 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						when().
						get("/content/v3/read/"+nodeId).
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();
				
				JsonPath jP1 = R1.jsonPath();
				String status = jP1.get("result.content.status");
				System.out.println(status);
				Assert.assertEquals(status, "Review");
			}
		}

		// Review invalid content
		@Test
		public void reviewInvalidContentExpect4xx(){
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body("{\"request\":{\"content\":{}}}").
			when().
			post("content/v3/review/asfdkfa").
			then().
			//log().all().
			spec(get400ResponseSpec());
		}

		// Review content without body
		@Test
		public void reviewContentWithoutBodyExpect4xx(){
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
					//spec(get200ResponseSpec()).
					extract().
					response();
		
			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			
			// Review the content
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body("{\"request\":{\"content\":{}}}").
			when().
			post("/content/v3/review/"+nodeId).
			then().
			//log().all().
			spec(get400ResponseSpec());
		}

		// Review authoring tool created content
		@Test
		public void reivewATContentExpectSuccess200(){
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
					log().all().
					//spec(get200ResponseSpec()).
					extract().
					response();
		
			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			String versionKey = jp.get("result.versionKey");
			
			// Update content body
			setURI();
			jsonUpdateATContentBody = jsonUpdateATContentBody.replace("version_Key", versionKey);
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body(jsonUpdateATContentBody).
			with().
			contentType("application/json").
			when().
			patch("/content/v3/update/"+nodeId).
			then().
			log().all().
			spec(get200ResponseSpec());	
			
			// Get content and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId+"?fields=body").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();
			
			JsonPath jP2 = R2.jsonPath();
			String body = jP2.get("result.content.body");
			System.out.println(body);
		
			// Setting status to review
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body("{\"request\":{\"content\":{}}}").
			when().
			post("/content/v3/review/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());
			
			// Get content and validate
			setURI();
			Response R1 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();
			
			JsonPath jP1 = R1.jsonPath();
			String status = jP1.get("result.content.status");
			System.out.println(status);
			Assert.assertEquals(status, "Review");
		}
		
		// Create, upload, publish and validate ECML content
		
		//Create content
		@Test
		public void publishContentExpectSuccess200(){
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
					log().all().
					spec(get200ResponseSpec()).
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
			when().
			post("/content/v3/upload/"+nodeId).
			then().
			log().all().
			spec(get200ResponseSpec());
		
			// Get body and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId+"?fields=body").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();
		
			JsonPath jP2 = R2.jsonPath();
			String body = jP2.get("result.content.body");
			Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
			if (isValidXML(body) || isValidJSON(body)){
				Assert.assertTrue(accessURL(nodeId));
			}
		}

		//Create content
		@Test
		public void publishValidImageAssetExpectSuccess200(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("contentType", "Asset").put("mimeType", "image/jpeg");
			String jsonCreateImageAssetValid = js.toString();
			Response R =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonCreateImageAssetValid).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
		multiPart(new File(path+"/jpegImage.jpeg")).
		when().	
		post("/content/v3/upload/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpecUpload());

		// Publish the created asset
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken)).
		body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
		when().
		post("/content/v3/publish/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec());
		}	
		
		/*// Create and upload image asset Invalid
		
		//Create content
		@Test
		public void uploadandPublishInValidImageAssetExpectSuccess200(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("contentType", "Asset").put("mimeType", "image/jpeg");
			String jsonCreateImageAssetInvalid = js.toString();
			Response R =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonCreateImageAssetInvalid).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
		multiPart(new File(path+"/Verbs_test.zip")).
		when().	
		post("/content/v3/upload/"+nodeId).
		then().
		//log().all().
		spec(get400ResponseSpec());
		}
		
		// Create and upload image asset Invalid

		//Create content
		@Test
		public void uploadandPublishPngInvalidImageAssetExpect4xx(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("contentType", "Asset").put("mimeType", "image/jpeg");
			String jsonCreateImageAssetInvalid = js.toString();
			Response R =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonCreateImageAssetInvalid).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
		multiPart(new File(path+"/pngImage.png")).
		when().	
		post("/content/v3/upload/"+nodeId).
		then().
		//log().all().
		spec(get400ResponseSpec());
		}*/
		
		// Create and upload audio asset Valid
		
		//Create content
		@Test
		public void publishValidAudioAssetExpectSuccess200(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("contentType", "Asset").put("mimeType", "audio/mp3");
			String jsonCreateImageAssetInvalid = js.toString();
			Response R =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonCreateImageAssetInvalid).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");

			// Upload Content
			setURI();
			given().
			spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
			multiPart(new File(path+"/sample.mp3")).
			when().	
			post("/content/v3/upload/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Publish the created asset
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());
			}	
		
		/*// Create and upload Invalid audio asset

		//Create content
		@Test
		public void uploadandPublishInvalidAudioAssetExpect4xx(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("contentType", "Asset").put("mimeType", "audio/mp3");
			String jsonCreateImageAssetInvalid = js.toString();
			Response R =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonCreateImageAssetInvalid).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
		multiPart(new File(path+"/pngImage.png")).
		when().	
		post("/content/v3/upload/"+nodeId).
		then().
		//log().all().
		spec(get400ResponseSpec());
		}
		
		// Create and upload invalid mimeType audio asset

		//Create content
		@Test
		public void uploadandPublishInvalidmimeTypeAudioAssetExpect4xx(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("contentType", "Asset").put("mimeType", "audio/mp3");
			String jsonCreateImageAssetInvalid = js.toString();
			Response R =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body(jsonCreateImageAssetInvalid).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");

		// Upload Content
		setURI();
		given().
		spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
		multiPart(new File(path+"/Oggaudio.ogg")).
		when().	
		post("/content/v3/upload/"+nodeId).
		then().
		//log().all().
		spec(get400ResponseSpec());
		} */
		
		// Upload valid content expect success

		

		// Upload valid content with special characters expect success

			//Create content
			@Test
			public void publishContentWithSpecialCharactersExpectSuccess200(){
					setURI();
				JSONObject js = new JSONObject(jsonCreateValidContent);
				js.getJSONObject("request").getJSONObject("content").put("name", ".TestContent!23.");
				String jsonCreateValidContentSpclChar = js.toString();
				Response R =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body(jsonCreateValidContentSpclChar).
						with().
						contentType(JSON).
						when().
						post("content/v3/create").
						then().
						//log().all().
						spec(get200ResponseSpec()).
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
				when().	
				post("/content/v3/upload/"+nodeId).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Get body and validate

				setURI();
				Response R2 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						when().
						get("/content/v3/read/"+nodeId+"?fields=body").
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				JsonPath jP2 = R2.jsonPath();
				String body = jP2.get("result.content.body");
				Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
				if (isValidXML(body) || isValidJSON(body)){
					Assert.assertTrue(accessURL(nodeId));
				}
			}
		// Upload content with valid Ecml (With external JSON for item data, another controller with __cdata item data )

		//Create content
		@Test
		public void publishContentWithExternaJSONItemDataCDataExpectSuccess200(){
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
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");

			// Upload Content
			setURI();
			given().
			spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
			multiPart(new File(path+"/ExternalJsonItemDataCdata.zip")).
			when().
			post("/content/v3/upload/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Get body and validate

			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId+"?fields=body").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP2 = R2.jsonPath();
			String body = jP2.get("result.content.body");
			Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
			if (isValidXML(body) || isValidJSON(body)){
				Assert.assertTrue(accessURL(nodeId));
			}
		}

		// Upload content with valid Ecml containing JSON item data

		//Create content
		@Test
		public void publishContentWithJSONItemDataExpectSuccess200(){
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
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");

			// Upload Content
			setURI();
			given().
			spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
			multiPart(new File(path+"/Item_json.zip")).
			when().
			post("/content/v3/upload/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Get body and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId+"?fields=body").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP2 = R2.jsonPath();
			String body = jP2.get("result.content.body");
			Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
			if (isValidXML(body) || isValidJSON(body)){
				Assert.assertTrue(accessURL(nodeId));
			}
		}

		// Upload content with valid ECML containing data JSONs

		//Create content
		@Test
		public void publishContentWithDataJSONExpectSuccess200(){
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
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");

			// Upload Content
			setURI();
			given().
			spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
			multiPart(new File(path+"/Data_json_ecml.zip")).
			when().
			post("/content/v3/upload/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Get body and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId+"?fields=body").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP2 = R2.jsonPath();
			String body = jP2.get("result.content.body");
			Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
			if (isValidXML(body) || isValidJSON(body)){
				Assert.assertTrue(accessURL(nodeId));
			}
		}

		// Upload Content with valid ECML containing custom plugin

		//Create content
		@Test
		public void publishContentWithCustomPluginExpectSuccess200(){
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

			// Upload Content
			setURI();
			given().
			spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
			multiPart(new File(path+"/Custom_Plugin.zip")).
			when().
			post("/content/v3/upload/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Get body and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId+"?fields=body").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP2 = R2.jsonPath();
			String body = jP2.get("result.content.body");
			Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
			if (isValidXML(body) || isValidJSON(body)){
				Assert.assertTrue(accessURL(nodeId));
			}
		}

		// Upload Files with Tween animation, audio sprites and Image sprite

		//Create content
		@Test
		public void publishContentWithAudioImageSpriteTweenAnimationExpectSuccess200(){
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
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");

			// Upload Content
			setURI();
			given().
			spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
			multiPart(new File(path+"/tweenAndaudioSprite.zip")).
			when().
			post("/content/v3/upload/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Get body and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId+"?fields=body").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP2 = R2.jsonPath();
			String body = jP2.get("result.content.body");
			Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
			if (isValidXML(body) || isValidJSON(body)){
				Assert.assertTrue(accessURL(nodeId));
			}
		}

		// Upload File with valid JSON ecml
		//Create content
		@Test
		public void publishContentWithJSONEcmlExpectSuccess200(){
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
					spec(get200ResponseSpec()).
					extract().
					response();
		
			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
		
			// Upload Content
			setURI();
			given().
			spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
			multiPart(new File(path+"/ecml_with_json.zip")).
			when().
			post("/content/v3/upload/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());
		
			// Get body and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId+"?fields=body").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();
		
			JsonPath jP2 = R2.jsonPath();
			String body = jP2.get("result.content.body");
			Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
			if (isValidXML(body) || isValidJSON(body)){
				Assert.assertTrue(accessURL(nodeId));
			}
		}

		// Upload File with valid JSON ecml
		//Create content
		@Test
		public void publishContentWithoutAssetsExpectSuccess200(){
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
					spec(get200ResponseSpec()).
					extract().
					response();
		
			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
		
			// Upload Content
			setURI();
			given().
			spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
			multiPart(new File(path+"/Ecml_without_asset.zip")).
			when().
			post("/content/v3/upload/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());
		
			// Get body and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId+"?fields=body").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();
		
			JsonPath jP2 = R2.jsonPath();
			String body = jP2.get("result.content.body");
			Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
			if (isValidXML(body) || isValidJSON(body)){
				Assert.assertTrue(accessURL(nodeId));
			}
		}

		// Upload multiple files

		//Create content
		@Test
		public void publishContentMultipleExpectSuccess200(){
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

			// Upload Content
			setURI();
			given().
			spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
			multiPart(new File(path+"/uploadContent.zip")).
			when().
			post("/content/v3/upload/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Upload Content
			setURI();
			given().
			spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
			multiPart(new File(path+"/uploadContent.zip")).
			when().
			post("/content/v3/upload/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Get body and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId+"?fields=body").
					then().
					extract().
					response();

			JsonPath jP2 = R2.jsonPath();
			String body = jP2.get("result.content.body");
			Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
			if (isValidXML(body) || isValidJSON(body)){
				Assert.assertTrue(accessURL(nodeId));
			}
		}
		
		// Create, update and publish content
		 
		// Create content
		@Test
		public void publishATContentExpectSuccess200(){
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
					//spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			String versionKey = jp.get("result.versionKey");
			
			// Update content body
			setURI();
			jsonUpdateATContentBody = jsonUpdateATContentBody.replace("version_Key", versionKey);
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body(jsonUpdateATContentBody).
			with().
			contentType("application/json").
			then().
			patch("/content/v3/update/"+nodeId);
//			then().
//			//log().all().
//			spec(get200ResponseSpec());	
			
			// Publish created content
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Get content and validate
			for (int i=1000; i<=5000; i=i+1000){
				try{Thread.sleep(i);}catch(InterruptedException e){System.out.println(e);} 
				setURI();
				Response R3 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						when().
						get("/content/v3/read/"+nodeId).
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				// Validate the response
				JsonPath jp3 = R3.jsonPath();
				String statusUpdated = jp3.get("result.content.status");
				//System.out.println(statusUpdated);
				if (statusUpdated.equals("Processing")){
					i=i+1000;
				}
				if (statusUpdated.equals("Live")){
					break;
				}
			}
		}

		// Create, upload and publish worksheet

		//Create content
		@Test
		public void publishWorksheetExpectSuccess200(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("contentType", "Worksheet");
			String jsonCreateValidWorksheet = js.toString();
			Response R =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonCreateValidWorksheet).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");

			// Upload Content
			setURI();
			given().
			spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
			multiPart(new File(path+"/Akshara_worksheet.zip")).
			when().
			post("/content/v3/upload/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Get body and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId+"?fields=body").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP2 = R2.jsonPath();
			String body = jP2.get("result.content.body");
			Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
			if (isValidXML(body) || isValidJSON(body)){
				Assert.assertTrue(accessURL(nodeId));
			}
		}

		// Create, upload, publish and validate HTML Content

		//Create content
		@Test
		public void publishHTMLContentExpectSuccess200(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("mimeType", "application/vnd.ekstep.html-archive");
			String jsonCreateValidContentHtml = js.toString();
			Response R =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonCreateValidContentHtml).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");

			// Upload Content
			setURI();
			given().
			spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
			multiPart(new File(path+"/uploadHtml.zip")).
			when().
			post("/content/v3/upload/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Get body and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId+"?fields=body").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP2 = R2.jsonPath();
			String body = jP2.get("result.content.body");
			Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
			if (isValidXML(body) || isValidJSON(body)){
				Assert.assertTrue(accessURL(nodeId));
			}
		}

		// Create, upload, publish and validate APK Content

		//Create content
		@Test
		public void publishAPKContentExpectSuccess200(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("osId", "org.ekstep.aser").put("mimeType", "application/vnd.android.package-archive");
			String jsonCreateValidContentAPK = js.toString();
			Response R =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonCreateValidContentAPK).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");

			// Upload Content
			setURI();
			given().
			spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
			multiPart(new File(path+"/uploadAPK.apk")).
			when().
			post("/content/v3/upload/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());


			// Get body and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId+"?fields=body").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP2 = R2.jsonPath();
			String body = jP2.get("result.content.body");
			Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
			if (isValidXML(body) || isValidJSON(body)){
				Assert.assertTrue(accessURL(nodeId));
			}
		}	

		// Create, upload, publish and validate valid collection
		@Test
		public void publishValidCollectionExpectSuccess200(){
			String node1 = null;
			String node2 = null;
			int count = 1;
			while(count<=2){
				setURI();
				int rn = generateRandomInt(9999, 1999999);
				JSONObject js = new JSONObject(jsonCreateValidContent);
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFTT_"+rn+"").put("name", "LP_NFTT-"+rn+"");
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
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/uploadContent.zip")).
					when().
					post("/content/v3/upload/"+node1).
					then().
					//log().all().
					spec(get200ResponseSpec());

					// Publish created content
					setURI();
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
					when().
					post("/content/v3/publish/"+node1).
					then().
					//log().all().
					spec(get200ResponseSpec());

				}
				if(count==2){
					node2 = nodeId;

					// Upload Content
					setURI();
					given().
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/tweenAndaudioSprite.zip")).
					when().
					post("/content/v3/upload/"+node2).
					then().
					//log().all().
					spec(get200ResponseSpec());

					// Publish created content
					setURI();
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
					when().
					post("/content/v3/publish/"+node2).
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
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonCreateContentCollection).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP1 = R1.jsonPath();
			String nodeId = jP1.get("result.node_id");

			// Publish collection
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Get content and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Validate the response
			JsonPath jp2 = R2.jsonPath();
			String status = jp2.get("result.content.status");
			String c_identifier = jp2.get("result.content.identifier");
			//String downloadUrl = jp2.get("result.content.downloadUrl");
			ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
			asyncPublishValidations(identifier1, status, nodeId, c_identifier, node1, node2);
		}
		
		// Create, upload, publish and validate valid collection
			@Test
			public void publishCollectionWithRetiredContentExpectSuccess200(){
					String node1 = null;
				String node2 = null;
				int count = 1;
				while(count<=2){
					setURI();
					int rn = generateRandomInt(9999, 1999999);
					JSONObject js = new JSONObject(jsonCreateValidContent);
					js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFTT_"+rn+"").put("name", "LP_NFTT-"+rn+"");
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
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/uploadContent.zip")).
						when().
						post("/content/v3/upload/"+node1).
						then().
						//log().all().
						spec(get200ResponseSpec());

						// Publish created content
						setURI();
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
						when().
						post("/content/v3/publish/"+node1).
						then().
						//log().all().
						spec(get200ResponseSpec());

					}
					if(count==2){
						node2 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node2).
						then().
						//log().all().
						spec(get200ResponseSpec());

						// Publish created content
						setURI();
						Response R1 = 
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
						when().
						post("/content/v3/publish/"+node2).
						then().
						extract().response();
						
						JsonPath jp1 = R1.jsonPath();
						String versionKey = jp1.get("result.versionKey");
						
						// Update status as Retired
						setURI();
						jsonUpdateContentValid = jsonUpdateContentValid.replace("Live", "Retired").replace("version_Key", versionKey);
						System.out.println(jsonUpdateContentValid);
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body(jsonUpdateContentValid).
						with().
						contentType("application/json").
						then().
						//log().all().
						patch("/content/v3/update/"+node2);
									
					}
					count++;
				}
				// Create collection
				setURI();
				jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
				Response R1 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body(jsonCreateContentCollection).
						with().
						contentType(JSON).
						when().
						post("content/v3/create").
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				JsonPath jP1 = R1.jsonPath();
				String nodeId = jP1.get("result.node_id");

				// Publish collection
				setURI();
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
				when().
				post("/content/v3/publish/"+nodeId).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Get content and validate
				setURI();
				Response R2 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						when().
						get("/content/v3/read/"+nodeId).
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				// Validate the response
				JsonPath jp2 = R2.jsonPath();
				String status = jp2.get("result.content.status");
				String c_identifier = jp2.get("result.content.identifier");
				//String downloadUrl = jp2.get("result.content.downloadUrl");
				ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
				asyncPublishValidations(identifier1, status, nodeId, c_identifier, node1, node2);
			}

		
		// Create, upload and publish collection of different mimeTypes
		
		@Test
		public void publishCollectionDiffMimeTypesExpectSuccess200(){
			String node1 = null;
			String node2 = null;
			int count = 1;
			while(count<=2){
				setURI();
				int rn = generateRandomInt(9999, 1999999);
				if(count==1){
					JSONObject js = new JSONObject(jsonCreateValidContent);
					js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFTT_"+rn+"").put("name", "LP_NFTT-"+rn+"").put("mimeType", "application/vnd.ekstep.html-archive");
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
							//log().all().
							spec(get200ResponseSpec()).
							extract().
							response();

					// Extracting the JSON path
					JsonPath jp = R.jsonPath();
					String nodeId = jp.get("result.node_id");
					node1 = nodeId;

					// Upload Content
					setURI();
					given().
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/uploadHtml.zip")).
					when().
					post("/content/v3/upload/"+node1).
					then().
					//log().all().
					spec(get200ResponseSpec());

					// Publish created content
					setURI();
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
					when().
					post("/content/v3/publish/"+node1).
					then().
					//log().all().
					spec(get200ResponseSpec());

				}
				if(count==2){
					JSONObject js = new JSONObject(jsonCreateValidContent);
					js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFTT_"+rn+"").put("name", "LP_NFTT-"+rn+"").put("osId", "org.ekstep.aser").put("mimeType", "application/vnd.android.package-archive");
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
							//log().all().
							spec(get200ResponseSpec()).
							extract().
							response();

					// Extracting the JSON path
					JsonPath jp = R.jsonPath();
					String nodeId = jp.get("result.node_id");
					node2 = nodeId;

					// Upload Content
					setURI();
					given().
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/uploadAPK.apk")).
					when().
					post("/content/v3/upload/"+node2).
					then().
					//log().all().
					spec(get200ResponseSpec());

					// Publish created content
					setURI();
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
					when().
					post("/content/v3/publish/"+node2).
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
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonCreateContentCollection).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP1 = R1.jsonPath();
			String nodeId = jP1.get("result.node_id");

			// Publish collection
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Get content and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Validate the response
			JsonPath jp2 = R2.jsonPath();
			String status = jp2.get("result.content.status");
			String c_identifier = jp2.get("result.content.identifier");
			//String downloadUrl = jp2.get("result.content.downloadUrl");
			ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
			asyncPublishValidations(identifier1, status, nodeId, c_identifier, node1, node2);
		}

		// Create, upload, publish and validate valid collection with contents created from authoring tool
		@Test
		public void publishValidCollectionWithATContentsExpectSuccess200(){
			String node1 = null;
			String node2 = null;
			int count = 1;
			while(count<=2){
				setURI();
				int rn = generateRandomInt(500, 999);
				JSONObject js = new JSONObject(jsonCreateValidContent);
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFTT_"+rn+"").put("name", "LP_NFTT-"+rn+"");
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
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/uploadContent.zip")).
					when().
					post("/content/v3/upload/"+node1).
					then().
					//log().all().
					spec(get200ResponseSpec());

					// Publish created content
					setURI();
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
					when().
					post("/content/v3/publish/"+node1).
					then().
					//log().all().
					spec(get200ResponseSpec());

				}
				if(count==2){
					node2 = nodeId;

					// Get node_id
					JsonPath jP = R.jsonPath();
					String versionKey = jP.get("result.versionKey");

					// Update content body
					setURI();
					jsonUpdateATContentBody = jsonUpdateATContentBody.replace("version_Key", versionKey);
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonUpdateATContentBody).
					with().
					contentType("application/json").
					when().
					patch("/content/v3/update/"+nodeId).
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
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonCreateContentCollection).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP1 = R1.jsonPath();
			String nodeId = jP1.get("result.node_id");

			// Publish collection
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Get content and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Validate the response
			JsonPath jp2 = R2.jsonPath();
			String status = jp2.get("result.content.status");
			//System.out.println(status);
			String c_identifier = jp2.get("result.content.identifier");
			ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
			asyncPublishValidations(identifier1, status, nodeId, c_identifier, node1, node2);
		}

		// Publish collection with live and retired content
		@Test
		public void publishCollectionWithLiveandRetiredContentExpectSuccess200(){
			String node1 = null;
			String node2 = null;
			int count = 1;
			while(count<=2){
				setURI();
				int rn = generateRandomInt(500, 999);
				JSONObject js = new JSONObject(jsonCreateValidContent);
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");
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
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/uploadContent.zip")).
					then().
					post("/content/v3/upload/"+node1);

					// Publish created content
					setURI();
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
					then().
					post("/content/v3/publish/"+node1);

				}
				if(count==2){
					node2 = nodeId;

					// Upload Content
					setURI();
					given().
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/ExternalJsonItemDataCdata.zip")).
					then().
					post("/content/v3/upload/"+node2);

					// Publish created content
					setURI();
					Response R1 = 
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
					when().
					post("/content/v3/publish/"+node2).
					then().
					//log().all().
					extract().response();
					
					JsonPath jp1 = R1.jsonPath();
					String versionKey = jp1.get("result.versionKey");
					
					// Update status as Retired
					setURI();
					jsonUpdateContentValid = jsonUpdateContentValid.replace("Live", "Retired").replace("version_Key", versionKey);
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonUpdateContentValid).
					with().
					contentType("application/json").
					then().
					//log().all().
					patch("/content/v3/update/"+node2);

				}
				count++;
			}
			// Create collection
			setURI();
			jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
			Response R1 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonCreateContentCollection).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					//log().all().
					extract().
					response();

			// Get collection and validate
			JsonPath jp1 = R1.jsonPath();
			String collectionNode = jp1.get("result.node_id");
			
			// Publish created content
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/"+collectionNode).
			then().
			//log().all().
			spec(get200ResponseSpec());
			
			// Get content and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+collectionNode).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Validate the response
			JsonPath jp2 = R2.jsonPath();
			String status = jp2.get("result.content.status");
			String c_identifier = jp2.get("result.content.identifier");
			//String downloadUrl = jp2.get("result.content.downloadUrl");
			ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
			asyncPublishValidations(identifier1, status, collectionNode, c_identifier, node1, node2);
		}
		
		// Create upload and publish collection and remove children
		
		@Test
		public void publishandRemoveChildrenCollectionExpect4xx(){
			String node1 = null;
			String node2 = null;
			int count = 1;
			while(count<=2){
				setURI();
				int rn = generateRandomInt(900, 1999);
				JSONObject js = new JSONObject(jsonCreateValidContent);
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFTT_"+rn+"").put("name", "LP_NFTT-"+rn+"");
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
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				// Extracting the JSON path
				JsonPath jp = R.jsonPath();
				String nodeId1 = jp.get("result.node_id");
				if(count==1){
					node1 = nodeId1;

					// Upload Content
					setURI();
					given().
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/uploadContent.zip")).
					when().
					post("/content/v3/upload/"+node1).
					then().
					//log().all().
					spec(get200ResponseSpec());

					// Publish created content
					setURI();
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
					when().
					post("/content/v3/publish/"+node1).
					then().
					//log().all().
					spec(get200ResponseSpec());

				}
				if(count==2){
					node2 = nodeId1;

					// Upload Content
					setURI();
					given().
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/tweenAndaudioSprite.zip")).
					when().
					post("/content/v3/upload/"+node2).
					then().
					//log().all().
					spec(get200ResponseSpec());

					// Publish created content
					setURI();
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
					when().
					post("/content/v3/publish/"+node2).
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
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonCreateContentCollection).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP1 = R1.jsonPath();
			String nodeId = jP1.get("result.node_id");

			// Publish collection
			setURI();
			Response R2 =
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec()).
			extract().
			response();
			
			JsonPath jP2 = R2.jsonPath();
			String versionKey = jP2.get("result.versionKey");

			// Update the children content
			setURI();
			System.out.println(jsonUpdateChildren);
			jsonUpdateChildren =  jsonUpdateChildren.replace("[]", "[{\"identifier\":\""+node2+"\"}]").replace("version_Key", versionKey);
			System.out.println(jsonUpdateChildren);
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body(jsonUpdateChildren).	
			when().
			patch("/content/v3/update/"+nodeId).
			then().
			log().all().
			spec(get200ResponseSpec());
		}
		
		
		// Create, upload and publish nested collection
		@Test
	public void publishNestedCollectionExpectSuccess200(){
			String node1 = null;
			String node2 = null;
			int count = 1;
			while(count<=2){
				setURI();
				int rn = generateRandomInt(900, 1999);
				JSONObject js = new JSONObject(jsonCreateValidContent);
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFTT_"+rn+"").put("name", "LP_NFTT-"+rn+"");
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
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				// Extracting the JSON path
				JsonPath jp = R.jsonPath();
				String nodeId1 = jp.get("result.node_id");
				if(count==1){
					node1 = nodeId1;

					// Upload Content
					setURI();
					given().
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/uploadContent.zip")).
					when().
					post("/content/v3/upload/"+node1).
					then().
					//log().all().
					spec(get200ResponseSpec());

					// Publish created content
					setURI();
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
					when().
					post("/content/v3/publish/"+node1).
					then().
					//log().all().
					spec(get200ResponseSpec());

				}
				if(count==2){
					node2 = nodeId1;

					// Upload Content
					setURI();
					given().
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/tweenAndaudioSprite.zip")).
					when().
					post("/content/v3/upload/"+node2).
					then().
					//log().all().
					spec(get200ResponseSpec());

					// Publish created content
					setURI();
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
					when().
					post("/content/v3/publish/"+node2).
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
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonCreateContentCollection).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP1 = R1.jsonPath();
			String nodeId = jP1.get("result.node_id");

			// Publish collection
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Get content and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Validate the response
			JsonPath jp2 = R2.jsonPath();
			String status = jp2.get("result.content.status");
			String c_identifier = jp2.get("result.content.identifier");
			//String downloadUrl = jp2.get("result.content.downloadUrl");
			ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
			asyncPublishValidations(identifier1, status, nodeId, c_identifier, node1, node2);

			// Create nested collection
			setURI();
			jsonCreateNestedCollection = jsonCreateNestedCollection.replace("id1", nodeId);
			Response R3 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonCreateNestedCollection).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
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
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/"+collectionId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Get content and validate
			setURI();
			Response R4 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+collectionId).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Validate the response
			JsonPath jp4 = R4.jsonPath();
			String n_status = jp4.get("result.content.status");
			String n_identifier = jp4.get("result.content.identifier");
			ArrayList<String> n_identifier1 = jp4.get("result.content.children.identifier");
			Assert.assertTrue (n_status.equals("Live")||n_status.equals("Processing")&&n_identifier.equals(collectionId)&&n_identifier1.contains(nodeId));
		}
		
		// Publish Text book with draft children with visibility - Default
		@Test
		public void publishTextBookDraftChildrenExpectSuccess200(){
			String node1 = null;
			String node2 = null;
			int count = 1;
			while(count<=2){
				setURI();
				int rn = generateRandomInt(9999, 1999999);
				JSONObject js = new JSONObject(jsonCreateValidContent);
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFTT_"+rn+"").put("name", "LP_NFTT-"+rn+"");
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
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/uploadContent.zip")).
					when().
					post("/content/v3/upload/"+node1).
					then().
					//log().all().
					spec(get200ResponseSpec());

				}
				if(count==2){
					node2 = nodeId;

					// Upload Content
					setURI();
					given().
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/tweenAndaudioSprite.zip")).
					when().
					post("/content/v3/upload/"+node2).
					then().
					//log().all().
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
			Response R1 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonCreateContentCollection).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP1 = R1.jsonPath();
			String nodeId = jP1.get("result.node_id");

			// Publish textbook
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Get content and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Validate the response
			JsonPath jp2 = R2.jsonPath();
			String status = jp2.get("result.content.status");
			String c_identifier = jp2.get("result.content.identifier");
			//String downloadUrl = jp2.get("result.content.downloadUrl");
			ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
			asyncPublishValidations(identifier1, status, nodeId, c_identifier, node1, node2);
		}

		// Publish Text book with draft children with visibility - Parent
		@Test
		public void publishParentTextBookDraftChildrenExpectSuccess200(){
			String node1 = null;
			String node2 = null;
			int count = 1;
			while(count<=2){
				setURI();
				int rn = generateRandomInt(9999, 1999999);
				JSONObject js = new JSONObject(jsonCreateValidContent);
				js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("identifier", "LP_NFTT_"+rn+"").put("name", "LP_NFTT-"+rn+"");
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
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/uploadContent.zip")).
					when().
					post("/content/v3/upload/"+node1).
					then().
					//log().all().
					spec(get200ResponseSpec());
				}
				if(count==2){
					node2 = nodeId;

					// Upload Content
					setURI();
					given().
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/tweenAndaudioSprite.zip")).
					when().
					post("/content/v3/upload/"+node2).
					then().
					//log().all().
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
			Response R1 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonCreateContentCollection).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP1 = R1.jsonPath();
			String nodeId = jP1.get("result.node_id");

			// Publish textbook
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Get content and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Validate the response
			JsonPath jp2 = R2.jsonPath();
			String status = jp2.get("result.content.status");
			String c_identifier = jp2.get("result.content.identifier");
			//String downloadUrl = jp2.get("result.content.downloadUrl");
			ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
			asyncPublishValidations(identifier1, status, nodeId, c_identifier, node1, node2);
		}

		// Publish Text book with live and draft children with visibility - Parent
		@Test
		public void publishParentTextBookLiveDraftChildrenExpectSuccess200(){
			String node1 = null;
			String node2 = null;
			int count = 1;
			while(count<=2){
				setURI();
				int rn = generateRandomInt(9999, 1999999);
				JSONObject js = new JSONObject(jsonCreateValidContent);
				js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("identifier", "LP_NFTT_"+rn+"").put("name", "LP_NFTT-"+rn+"");
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
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/uploadContent.zip")).
					when().
					post("/content/v3/upload/"+node1).
					then().
					//log().all().
					spec(get200ResponseSpec());
					
					// Publish created content
					setURI();
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
					when().
					post("/content/v3/publish/"+node1).
					then().
					//log().all().
					spec(get200ResponseSpec());

				}
				if(count==2){
					node2 = nodeId;

					// Upload Content
					setURI();
					given().
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/tweenAndaudioSprite.zip")).
					when().
					post("/content/v3/upload/"+node2).
					then().
					//log().all().
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
			Response R1 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonCreateContentCollection).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP1 = R1.jsonPath();
			String nodeId = jP1.get("result.node_id");

			// Publish textbook
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Get content and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Validate the response
			JsonPath jp2 = R2.jsonPath();
			String status = jp2.get("result.content.status");
			String c_identifier = jp2.get("result.content.identifier");
			//String downloadUrl = jp2.get("result.content.downloadUrl");
			ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
			asyncPublishValidations(identifier1, status, nodeId, c_identifier, node1, node2);
		}
		
		// Publish Text book with Live children with visibility - Default
		@Test
		public void publishTextBookLiveChildrenExpectSuccess200(){
			String node1 = null;
			String node2 = null;
			int count = 1;
			while(count<=2){
				setURI();
				int rn = generateRandomInt(9999, 1999999);
				JSONObject js = new JSONObject(jsonCreateValidContent);
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFTT_"+rn+"").put("name", "LP_NFTT-"+rn+"");
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
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/uploadContent.zip")).
					when().
					post("/content/v3/upload/"+node1).
					then().
					//log().all().
					spec(get200ResponseSpec());
					
					// Publish created content
					setURI();
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
					when().
					post("/content/v3/publish/"+node1).
					then().
					//log().all().
					spec(get200ResponseSpec());

				}
				if(count==2){
					node2 = nodeId;

					// Upload Content
					setURI();
					given().
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/tweenAndaudioSprite.zip")).
					when().
					post("/content/v3/upload/"+node2).
					then().
					//log().all().
					spec(get200ResponseSpec());
					
					// Publish created content
					setURI();
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
					when().
					post("/content/v3/publish/"+node2).
					then().
					//log().all().
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
			Response R1 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonCreateContentCollection).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP1 = R1.jsonPath();
			String nodeId = jP1.get("result.node_id");

			// Publish textbook
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Get content and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Validate the response
			JsonPath jp2 = R2.jsonPath();
			String status = jp2.get("result.content.status");
			String c_identifier = jp2.get("result.content.identifier");
			//String downloadUrl = jp2.get("result.content.downloadUrl");
			ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
			asyncPublishValidations(identifier1, status, nodeId, c_identifier, node1, node2);
		}
		
		// Publish Text book with Live and Draft children with visibility - Default
		@Test
		public void publishTextBookLiveDraftChildrenExpectSuccess200(){
			String node1 = null;
			String node2 = null;
			int count = 1;
			while(count<=2){
				setURI();
				int rn = generateRandomInt(9999, 1999999);
				JSONObject js = new JSONObject(jsonCreateValidContent);
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFTT_"+rn+"").put("name", "LP_NFTT-"+rn+"");
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
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/uploadContent.zip")).
					when().
					post("/content/v3/upload/"+node1).
					then().
					//log().all().
					spec(get200ResponseSpec());
					
					// Publish created content
					setURI();
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
					when().
					post("/content/v3/publish/"+node1).
					then().
					//log().all().
					spec(get200ResponseSpec());

				}
				if(count==2){
					node2 = nodeId;

					// Upload Content
					setURI();
					given().
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/tweenAndaudioSprite.zip")).
					when().
					post("/content/v3/upload/"+node2).
					then().
					//log().all().
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
			Response R1 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonCreateContentCollection).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP1 = R1.jsonPath();
			String nodeId = jP1.get("result.node_id");

			// Publish textbook
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Get content and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Validate the response
			JsonPath jp2 = R2.jsonPath();
			String status = jp2.get("result.content.status");
			String c_identifier = jp2.get("result.content.identifier");
			//String downloadUrl = jp2.get("result.content.downloadUrl");
			ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
			asyncPublishValidations(identifier1, status, nodeId, c_identifier, node1, node2);
		}
		
		// Publish Text book with Live children with visibility - Parent
		@Test
		public void publishParentTextBookLiveChildrenExpectSuccess200(){
			String node1 = null;
			String node2 = null;
			int count = 1;
			while(count<=2){
				setURI();
				int rn = generateRandomInt(9999, 1999999);
				JSONObject js = new JSONObject(jsonCreateValidContent);
				js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("identifier", "LP_NFTT_"+rn+"").put("name", "LP_NFTT-"+rn+"");
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
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/uploadContent.zip")).
					when().
					post("/content/v3/upload/"+node1).
					then().
					//log().all().
					spec(get200ResponseSpec());
					
					// Publish created content
					setURI();
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
					when().
					post("/content/v3/publish/"+node1).
					then().
					//log().all().
					spec(get200ResponseSpec());

				}
				if(count==2){
					node2 = nodeId;

					// Upload Content
					setURI();
					given().
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/tweenAndaudioSprite.zip")).
					when().
					post("/content/v3/upload/"+node2).
					then().
					//log().all().
					spec(get200ResponseSpec());
					
					// Publish created content
					setURI();
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
					when().
					post("/content/v3/publish/"+node2).
					then().
					//log().all().
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
			Response R1 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonCreateContentCollection).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP1 = R1.jsonPath();
			String nodeId = jP1.get("result.node_id");

			// Publish textbook
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Get content and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Validate the response
			JsonPath jp2 = R2.jsonPath();
			String status = jp2.get("result.content.status");
			String c_identifier = jp2.get("result.content.identifier");
			//String downloadUrl = jp2.get("result.content.downloadUrl");
			ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
			asyncPublishValidations(identifier1, status, nodeId, c_identifier, node1, node2);
		}
		
		// Publish Text book with Live and Retired children with visibility - Parent
		@Test
		public void publishTextBookLiveandRetiredChildrenExpectSuccess200(){
			String node1 = null;
			String node2 = null;
			int count = 1;
			while(count<=2){
				setURI();
				int rn = generateRandomInt(9999, 1999999);
				JSONObject js = new JSONObject(jsonCreateValidContent);
				js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("identifier", "LP_NFTT_"+rn+"").put("name", "LP_NFTT-"+rn+"");
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
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/uploadContent.zip")).
					when().
					post("/content/v3/upload/"+node1).
					then().
					//log().all().
					spec(get200ResponseSpec());
					
					// Publish created content
					setURI();
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
					when().
					post("/content/v3/publish/"+node1).
					then().
					//log().all().
					spec(get200ResponseSpec());

				}
				if(count==2){
					node2 = nodeId;

					// Upload Content
					setURI();
					given().
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/tweenAndaudioSprite.zip")).
					when().
					post("/content/v3/upload/"+node2).
					then().
					//log().all().
					spec(get200ResponseSpec());
					
					// Publish created content
					setURI();
					Response R1 = 
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
					when().
					post("/content/v3/publish/"+node2).
					then().
					extract().response();
					
					JsonPath jp1 = R1.jsonPath();
					String versionKey = jp1.get("result.versionKey");
					
					// Update status as Retired
					setURI();
					jsonUpdateContentValid = jsonUpdateContentValid.replace("Live", "Retired").replace("version_Key", versionKey);
					System.out.println(jsonUpdateContentValid);
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonUpdateContentValid).
					with().
					contentType("application/json").
					then().
					//log().all().
					patch("/content/v3/update/"+node2);
				}
				count++;
			}
			// Create Textbook
			setURI();
			JSONObject js = new JSONObject(jsonCreateContentCollection);
			js.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
			String jsonCreateContentCollection = js.toString();
			jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
			Response R1 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonCreateContentCollection).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP1 = R1.jsonPath();
			String nodeId = jP1.get("result.node_id");

			// Publish textbook
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Get content and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Validate the response
			JsonPath jp2 = R2.jsonPath();
			String status = jp2.get("result.content.status");
			String c_identifier = jp2.get("result.content.identifier");
			//String downloadUrl = jp2.get("result.content.downloadUrl");
			ArrayList<String> identifier1 = jp2.get("result.content.children.identifier");
			asyncPublishValidations(identifier1, status, nodeId, c_identifier, node1, node2);
		}
		
		// Create, upload and publish nested textbook with collection as child
			@Test
			public void publishNestedTextBookWithCollectionExpectSuccess200(){
					String node1 = null;
				String node2 = null;
				String node3 = null;
				String node4 = null;
				int count = 1;
				while(count<=4){
					setURI();
					int rn = generateRandomInt(9999, 1999999);
					JSONObject js = new JSONObject(jsonCreateValidContent);
					if(count==1){
						js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==2){
						js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==3){
						js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==4){
						js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
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
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/uploadContent.zip")).
						when().
						post("/content/v3/upload/"+node1).
						then().
						//log().all().
						spec(get200ResponseSpec());

					}
					if(count==2){
						node2 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node2).
						then().
						//log().all().
						spec(get200ResponseSpec());
					}
					if(count==3){
						node3 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node3).
						then().
						//log().all().
						spec(get200ResponseSpec());
						
						// Publish content
						setURI();
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
						when().
						post("/content/v3/publish/"+node3).
						then().
						//log().all().
						spec(get200ResponseSpec());
					}
					if(count==4){
						node4 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node4).
						then().
						//log().all().
						spec(get200ResponseSpec());
						
						// Publish content
						setURI();
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
						when().
						post("/content/v3/publish/"+node4).
						then().
						//log().all().
						spec(get200ResponseSpec());
					}
					count++;
				}
				// Create TextbookUnit
				setURI();
				JSONObject js2 = new JSONObject(jsonCreateTextbookUnit);
				js2.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("contentType", "collection");
				String jsonCreateTextbookUnit = js2.toString();
				jsonCreateTextbookUnit = jsonCreateTextbookUnit.replace("id1", node1).replace("id2", node2).replace("id3", node3).replace("id4", node4);
				Response R1 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body(jsonCreateTextbookUnit).
						with().
						contentType(JSON).
						when().
						post("content/v3/create").
						then().
						log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				JsonPath jP1 = R1.jsonPath();
				String nodeId = jP1.get("result.node_id");

				// Create Textbook
				setURI();
				JSONObject js1 = new JSONObject(jsonCreateNestedCollection);
				js1.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
				String jsonCreateNestedCollection = js1.toString();
				jsonCreateNestedCollection = jsonCreateNestedCollection.replace("id1", nodeId);
				Response R3 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body(jsonCreateNestedCollection).
						with().
						contentType(JSON).
						when().
						post("content/v3/create").
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				JsonPath jP3 = R3.jsonPath();
				String textBookId = jP3.get("result.node_id");

				// Publish textbook
				setURI();
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
				when().
				post("/content/v3/publish/"+textBookId).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Get content and validate
				setURI();
				Response R4 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						when().
						get("/content/v3/read/"+textBookId).
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				// Validate the response
				JsonPath jp4 = R4.jsonPath();
				String n_status = jp4.get("result.content.status");
				String n_identifier = jp4.get("result.content.identifier");
				ArrayList<String> n_identifier1 = jp4.get("result.content.children.identifier");
				Assert.assertTrue (n_status.equals("Live")||n_status.equals("Processing")&&n_identifier.equals(textBookId)&&n_identifier1.contains(nodeId));
			}

		
		// Create, upload and publish nested textbook with draft textbook unit and visibility-Parent
		@Test
		public void publishNestedTextBookDraftTBUnitParentExpectSuccess200(){
			String node1 = null;
			String node2 = null;
			String node3 = null;
			String node4 = null;
			int count = 1;
			while(count<=4){
				setURI();
				int rn = generateRandomInt(9999, 1999999);
				JSONObject js = new JSONObject(jsonCreateValidContent);
				if(count==1){
					js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
				}
				if(count==2){
					js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
				}
				if(count==3){
					js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
				}
				if(count==4){
					js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
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
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/uploadContent.zip")).
					when().
					post("/content/v3/upload/"+node1).
					then().
					//log().all().
					spec(get200ResponseSpec());

				}
				if(count==2){
					node2 = nodeId;

					// Upload Content
					setURI();
					given().
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/uploadContent.zip")).
					when().
					post("/content/v3/upload/"+node2).
					then().
					//log().all().
					spec(get200ResponseSpec());
				}
				if(count==3){
					node3 = nodeId;

					// Upload Content
					setURI();
					given().
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/uploadContent.zip")).
					when().
					post("/content/v3/upload/"+node3).
					then().
					//log().all().
					spec(get200ResponseSpec());
					
					// Publish content
					setURI();
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
					when().
					post("/content/v3/publish/"+node3).
					then().
					//log().all().
					spec(get200ResponseSpec());
				}
				if(count==4){
					node4 = nodeId;

					// Upload Content
					setURI();
					given().
					spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
					multiPart(new File(path+"/tweenAndaudioSprite.zip")).
					when().
					post("/content/v3/upload/"+node4).
					then().
					//log().all().
					spec(get200ResponseSpec());
					
					// Publish content
					setURI();
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
					when().
					post("/content/v3/publish/"+node4).
					then().
					//log().all().
					spec(get200ResponseSpec());
				}
				count++;
			}
			// Create TextbookUnit
			setURI();
			JSONObject js2 = new JSONObject(jsonCreateTextbookUnit);
			js2.getJSONObject("request").getJSONObject("content").put("visibility", "Parent");
			String jsonCreateTextbookUnit = js2.toString();
			jsonCreateTextbookUnit = jsonCreateTextbookUnit.replace("id1", node1).replace("id2", node2).replace("id3", node3).replace("id4", node4);
			Response R1 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonCreateTextbookUnit).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP1 = R1.jsonPath();
			String nodeId = jP1.get("result.node_id");

			// Create Textbook
			setURI();
			JSONObject js1 = new JSONObject(jsonCreateNestedCollection);
			js1.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
			String jsonCreateNestedCollection = js1.toString();
			jsonCreateNestedCollection = jsonCreateNestedCollection.replace("id1", nodeId);
			Response R3 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					body(jsonCreateNestedCollection).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP3 = R3.jsonPath();
			String textBookId = jP3.get("result.node_id");

			// Publish textbook
			setURI();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
			when().
			post("/content/v3/publish/"+textBookId).
			then().
			log().all().
			spec(get200ResponseSpec());

			// Get content and validate
			setURI();
			Response R4 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+textBookId).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Validate the response
			JsonPath jp4 = R4.jsonPath();
			String n_status = jp4.get("result.content.status");
			String n_identifier = jp4.get("result.content.identifier");
			ArrayList<String> n_identifier1 = jp4.get("result.content.children.identifier");
			Assert.assertTrue (n_status.equals("Live")||n_status.equals("Processing")&&n_identifier.equals(textBookId)&&n_identifier1.contains(nodeId));
		}
		
		// Create, upload and publish nested textbook with draft textbook unit and visibility-Default
			@Test
			public void publishNestedTextBookDraftTBUnitDefaultExpectSuccess200(){
					String node1 = null;
				String node2 = null;
				String node3 = null;
				String node4 = null;
				int count = 1;
				while(count<=4){
					setURI();
					int rn = generateRandomInt(9999, 1999999);
					JSONObject js = new JSONObject(jsonCreateValidContent);
					if(count==1){
						js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==2){
						js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==3){
						js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==4){
						js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
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
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/uploadContent.zip")).
						when().
						post("/content/v3/upload/"+node1).
						then().
						//log().all().
						spec(get200ResponseSpec());

					}
					if(count==2){
						node2 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node2).
						then().
						//log().all().
						spec(get200ResponseSpec());
					}
					if(count==3){
						node3 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node3).
						then().
						//log().all().
						spec(get200ResponseSpec());
						
						// Publish content
						setURI();
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
						when().
						post("/content/v3/publish/"+node3).
						then().
						//log().all().
						spec(get200ResponseSpec());
					}
					if(count==4){
						node4 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node4).
						then().
						//log().all().
						spec(get200ResponseSpec());
						
						// Publish content
						setURI();
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
						when().
						post("/content/v3/publish/"+node4).
						then().
						//log().all().
						spec(get200ResponseSpec());
					}
					count++;
				}
				// Create TextbookUnit
				setURI();
				jsonCreateTextbookUnit = jsonCreateTextbookUnit.replace("id1", node1).replace("id2", node2).replace("id3", node3).replace("id4", node4);
				System.out.println(jsonCreateTextbookUnit);
				Response R1 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body(jsonCreateTextbookUnit).
						with().
						contentType(JSON).
						when().
						post("content/v3/create").
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				JsonPath jP1 = R1.jsonPath();
				String nodeId = jP1.get("result.node_id");

				// Create Textbook
				setURI();
				JSONObject js1 = new JSONObject(jsonCreateNestedCollection);
				js1.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
				String jsonCreateNestedCollection = js1.toString();
				jsonCreateNestedCollection = jsonCreateNestedCollection.replace("id1", nodeId);
				System.out.println(jsonCreateNestedCollection);
				Response R3 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body(jsonCreateNestedCollection).
						with().
						contentType(JSON).
						when().
						post("content/v3/create").
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				JsonPath jP3 = R3.jsonPath();
				String textBookId = jP3.get("result.node_id");

				// Publish textbook
				setURI();
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
				when().
				post("/content/v3/publish/"+textBookId).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Get content and validate
				setURI();
				Response R4 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						when().
						get("/content/v3/read/"+textBookId).
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				// Validate the response
				JsonPath jp4 = R4.jsonPath();
				String n_status = jp4.get("result.content.status");
				String n_identifier = jp4.get("result.content.identifier");
				ArrayList<String> n_identifier1 = jp4.get("result.content.children.identifier");
				Assert.assertTrue (n_status.equals("Live")||n_status.equals("Processing")&&n_identifier.equals(textBookId)&&n_identifier1.contains(nodeId));
			}
			
			// Create, upload and publish nested textbook with Live textbook unit and visibility-Parent
			@Test
			public void publishNestedTextBookLiveTBUnitParentExpectSuccess200(){
					String node1 = null;
				String node2 = null;
				String node3 = null;
				String node4 = null;
				int count = 1;
				while(count<=4){
					setURI();
					int rn = generateRandomInt(9999, 1999999);
					JSONObject js = new JSONObject(jsonCreateValidContent);
					if(count==1){
						js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==2){
						js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==3){
						js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==4){
						js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
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
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/uploadContent.zip")).
						when().
						post("/content/v3/upload/"+node1).
						then().
						//log().all().
						spec(get200ResponseSpec());

					}
					if(count==2){
						node2 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node2).
						then().
						//log().all().
						spec(get200ResponseSpec());
					}
					if(count==3){
						node3 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node3).
						then().
						//log().all().
						spec(get200ResponseSpec());
						
						// Publish content
						setURI();
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
						when().
						post("/content/v3/publish/"+node3).
						then().
						//log().all().
						spec(get200ResponseSpec());
					}
					if(count==4){
						node4 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node4).
						then().
						//log().all().
						spec(get200ResponseSpec());
						
						// Publish content 4
						setURI();
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
						when().
						post("/content/v3/publish/"+node4).
						then().
						//log().all().
						spec(get200ResponseSpec());
					}
					count++;
				}
				// Create TextbookUnit
				setURI();
				JSONObject js2 = new JSONObject(jsonCreateTextbookUnit);
				js2.getJSONObject("request").getJSONObject("content").put("visibility", "Parent");
				String jsonCreateTextbookUnit = js2.toString();
				jsonCreateTextbookUnit = jsonCreateTextbookUnit.replace("id1", node1).replace("id2", node2).replace("id3", node3).replace("id4", node4);
				Response R1 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body(jsonCreateTextbookUnit).
						with().
						contentType(JSON).
						when().
						post("content/v3/create").
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				JsonPath jP1 = R1.jsonPath();
				String nodeId = jP1.get("result.node_id");
				
				//Publish Textbook unit
				setURI();
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
				when().
				post("/content/v3/publish/"+nodeId).
				then().
				//log().all().
				spec(get200ResponseSpec());


				// Create Textbook
				setURI();
				JSONObject js1 = new JSONObject(jsonCreateNestedCollection);
				js1.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
				String jsonCreateNestedCollection = js1.toString();
				jsonCreateNestedCollection = jsonCreateNestedCollection.replace("id1", nodeId);
				Response R3 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body(jsonCreateNestedCollection).
						with().
						contentType(JSON).
						when().
						post("content/v3/create").
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				JsonPath jP3 = R3.jsonPath();
				String textBookId = jP3.get("result.node_id");

				// Publish textbook
				setURI();
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
				when().
				post("/content/v3/publish/"+textBookId).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Get content and validate
				setURI();
				Response R4 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						when().
						get("/content/v3/read/"+textBookId).
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				// Validate the response
				JsonPath jp4 = R4.jsonPath();
				String n_status = jp4.get("result.content.status");
				String n_identifier = jp4.get("result.content.identifier");
				ArrayList<String> n_identifier1 = jp4.get("result.content.children.identifier");
				Assert.assertTrue (n_status.equals("Live")||n_status.equals("Processing")&&n_identifier.equals(textBookId)&&n_identifier1.contains(nodeId));
			}
			
			// Create, upload and publish nested textbook with Live textbook unit and visibility-Parent
			@Test
			public void publishNestedTextBookLiveTBUnitDefaultExpectSuccess200(){
					String node1 = null;
				String node2 = null;
				String node3 = null;
				String node4 = null;
				int count = 1;
				while(count<=4){
					setURI();
					int rn = generateRandomInt(9999, 1999999);
					JSONObject js = new JSONObject(jsonCreateValidContent);
					if(count==1){
						js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==2){
						js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==3){
						js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==4){
						js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
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
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/uploadContent.zip")).
						when().
						post("/content/v3/upload/"+node1).
						then().
						//log().all().
						spec(get200ResponseSpec());

					}
					if(count==2){
						node2 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node2).
						then().
						//log().all().
						spec(get200ResponseSpec());
					}
					if(count==3){
						node3 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node3).
						then().
						//log().all().
						spec(get200ResponseSpec());
						
						// Publish content
						setURI();
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
						when().
						post("/content/v3/publish/"+node3).
						then().
						//log().all().
						spec(get200ResponseSpec());
					}
					if(count==4){
						node4 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node4).
						then().
						//log().all().
						spec(get200ResponseSpec());
						
						// Publish content
						setURI();
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
						when().
						post("/content/v3/publish/"+node4).
						then().
						//log().all().
						spec(get200ResponseSpec());
					}
					count++;
				}
				// Create TextbookUnit
				setURI();
				jsonCreateTextbookUnit = jsonCreateTextbookUnit.replace("id1", node1).replace("id2", node2).replace("id3", node3).replace("id4", node4);
				Response R1 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body(jsonCreateTextbookUnit).
						with().
						contentType(JSON).
						when().
						post("content/v3/create").
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				JsonPath jP1 = R1.jsonPath();
				String nodeId = jP1.get("result.node_id");
				
				//Publish Textbook unit
				setURI();
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
				when().
				post("/content/v3/publish/"+nodeId).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Create Textbook
				setURI();
				JSONObject js1 = new JSONObject(jsonCreateNestedCollection);
				js1.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
				String jsonCreateNestedCollection = js1.toString();
				jsonCreateNestedCollection = jsonCreateNestedCollection.replace("id1", nodeId);
				Response R3 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body(jsonCreateNestedCollection).
						with().
						contentType(JSON).
						when().
						post("content/v3/create").
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				JsonPath jP3 = R3.jsonPath();
				String textBookId = jP3.get("result.node_id");

				// Publish textbook
				setURI();
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
				when().
				post("/content/v3/publish/"+textBookId).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Get content and validate
				setURI();
				Response R4 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						when().
						get("/content/v3/read/"+textBookId).
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				// Validate the response
				JsonPath jp4 = R4.jsonPath();
				String n_status = jp4.get("result.content.status");
				String n_identifier = jp4.get("result.content.identifier");
				ArrayList<String> n_identifier1 = jp4.get("result.content.children.identifier");
				Assert.assertTrue (n_status.equals("Live")||n_status.equals("Processing")&&n_identifier.equals(textBookId)&&n_identifier1.contains(nodeId));
			}	
		
			// Create, upload and publish nested textbook with Draft textbook unit and visibility-Default with draft children
			@Test
			public void publishNestedTextBookDraftTBUnitDraftChildrenExpectSuccess200(){
					String node1 = null;
				String node2 = null;
				String node3 = null;
				String node4 = null;
				int count = 1;
				while(count<=4){
					setURI();
					int rn = generateRandomInt(9999, 1999999);
					JSONObject js = new JSONObject(jsonCreateValidContent);
					if(count==1){
						js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==2){
						js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==3){
						js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==4){
						js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
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
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/uploadContent.zip")).
						when().
						post("/content/v3/upload/"+node1).
						then().
						//log().all().
						spec(get200ResponseSpec());

					}
					if(count==2){
						node2 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node2).
						then().
						//log().all().
						spec(get200ResponseSpec());
					}
					if(count==3){
						node3 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node3).
						then().
						//log().all().
						spec(get200ResponseSpec());
						
					}
					if(count==4){
						node4 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node4).
						then().
						//log().all().
						spec(get200ResponseSpec());
					}
					count++;
				}
				// Create TextbookUnit
				setURI();
				jsonCreateTextbookUnit = jsonCreateTextbookUnit.replace("id1", node1).replace("id2", node2).replace("id3", node3).replace("id4", node4);
				Response R1 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body(jsonCreateTextbookUnit).
						with().
						contentType(JSON).
						when().
						post("content/v3/create").
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				JsonPath jP1 = R1.jsonPath();
				String nodeId = jP1.get("result.node_id");
				
				// Create Textbook
				setURI();
				JSONObject js1 = new JSONObject(jsonCreateNestedCollection);
				js1.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
				String jsonCreateNestedCollection = js1.toString();
				jsonCreateNestedCollection = jsonCreateNestedCollection.replace("id1", nodeId);
				Response R3 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body(jsonCreateNestedCollection).
						with().
						contentType(JSON).
						when().
						post("content/v3/create").
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				JsonPath jP3 = R3.jsonPath();
				String textBookId = jP3.get("result.node_id");

				// Publish textbook
				setURI();
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
				when().
				post("/content/v3/publish/"+textBookId).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Get content and validate
				setURI();
				Response R4 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						when().
						get("/content/v3/read/"+textBookId).
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				// Validate the response
				JsonPath jp4 = R4.jsonPath();
				String n_status = jp4.get("result.content.status");
				String n_identifier = jp4.get("result.content.identifier");
				ArrayList<String> n_identifier1 = jp4.get("result.content.children.identifier");
				Assert.assertTrue (n_status.equals("Live")||n_status.equals("Processing")&&n_identifier.equals(textBookId)&&n_identifier1.contains(nodeId));
			}	
			
			// Create, upload and publish nested textbook with Draft textbook unit and visibility-Parent with draft children
			@Test
			public void publishNestedTextBookDraftTBUnitParentDraftChildrenExpectSuccess200(){
					String node1 = null;
				String node2 = null;
				String node3 = null;
				String node4 = null;
				int count = 1;
				while(count<=4){
					setURI();
					int rn = generateRandomInt(9999, 1999999);
					JSONObject js = new JSONObject(jsonCreateValidContent);
					if(count==1){
						js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==2){
						js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==3){
						js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==4){
						js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
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
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/uploadContent.zip")).
						when().
						post("/content/v3/upload/"+node1).
						then().
						//log().all().
						spec(get200ResponseSpec());

					}
					if(count==2){
						node2 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node2).
						then().
						//log().all().
						spec(get200ResponseSpec());
					}
					if(count==3){
						node3 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node3).
						then().
						//log().all().
						spec(get200ResponseSpec());
						
					}
					if(count==4){
						node4 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node4).
						then().
						//log().all().
						spec(get200ResponseSpec());
					}
					count++;
				}
				// Create TextbookUnit
				setURI();
				JSONObject js2 = new JSONObject(jsonCreateTextbookUnit);
				js2.getJSONObject("request").getJSONObject("content").put("visibility", "Parent");
				String jsonCreateTextbookUnit = js2.toString();
				jsonCreateTextbookUnit = jsonCreateTextbookUnit.replace("id1", node1).replace("id2", node2).replace("id3", node3).replace("id4", node4);
				Response R1 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body(jsonCreateTextbookUnit).
						with().
						contentType(JSON).
						when().
						post("content/v3/create").
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				JsonPath jP1 = R1.jsonPath();
				String nodeId = jP1.get("result.node_id");
				
				// Create Textbook
				setURI();
				JSONObject js1 = new JSONObject(jsonCreateNestedCollection);
				js1.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
				String jsonCreateNestedCollection = js1.toString();
				jsonCreateNestedCollection = jsonCreateNestedCollection.replace("id1", nodeId);
				Response R3 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body(jsonCreateNestedCollection).
						with().
						contentType(JSON).
						when().
						post("content/v3/create").
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				JsonPath jP3 = R3.jsonPath();
				String textBookId = jP3.get("result.node_id");

				// Publish textbook
				setURI();
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
				when().
				post("/content/v3/publish/"+textBookId).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Get content and validate
				setURI();
				Response R4 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						when().
						get("/content/v3/read/"+textBookId).
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				// Validate the response
				JsonPath jp4 = R4.jsonPath();
				String n_status = jp4.get("result.content.status");
				String n_identifier = jp4.get("result.content.identifier");
				ArrayList<String> n_identifier1 = jp4.get("result.content.children.identifier");
				Assert.assertTrue (n_status.equals("Live")||n_status.equals("Processing")&&n_identifier.equals(textBookId)&&n_identifier1.contains(nodeId));
			}	
			
			// Create, upload and publish nested textbook with Live textbook unit and visibility-Default and live children
			@Test
			public void publishNestedTextBookLiveTBUnitDefaultLiveChildrenExpectSuccess200(){
					String node1 = null;
				String node2 = null;
				String node3 = null;
				String node4 = null;
				int count = 1;
				while(count<=4){
					setURI();
					int rn = generateRandomInt(9999, 1999999);
					JSONObject js = new JSONObject(jsonCreateValidContent);
					if(count==1){
						js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==2){
						js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==3){
						js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==4){
						js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
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
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/uploadContent.zip")).
						when().
						post("/content/v3/upload/"+node1).
						then().
						//log().all().
						spec(get200ResponseSpec());
						
						// Publish content
						setURI();
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
						when().
						post("/content/v3/publish/"+node1).
						then().
						//log().all().
						spec(get200ResponseSpec());

					}
					if(count==2){
						node2 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node2).
						then().
						//log().all().
						spec(get200ResponseSpec());

						// Publish content
						setURI();
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
						when().
						post("/content/v3/publish/"+node2).
						then().
						//log().all().
						spec(get200ResponseSpec());
					}
					if(count==3){
						node3 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node3).
						then().
						//log().all().
						spec(get200ResponseSpec());
						
						// Publish content
						setURI();
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
						when().
						post("/content/v3/publish/"+node3).
						then().
						//log().all().
						spec(get200ResponseSpec());
					}
					if(count==4){
						node4 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node4).
						then().
						//log().all().
						spec(get200ResponseSpec());
						
						// Publish content
						setURI();
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
						when().
						post("/content/v3/publish/"+node4).
						then().
						//log().all().
						spec(get200ResponseSpec());
					}
					count++;
				}
				// Create TextbookUnit
				setURI();
				jsonCreateTextbookUnit = jsonCreateTextbookUnit.replace("id1", node1).replace("id2", node2).replace("id3", node3).replace("id4", node4);
				Response R1 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body(jsonCreateTextbookUnit).
						with().
						contentType(JSON).
						when().
						post("content/v3/create").
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				JsonPath jP1 = R1.jsonPath();
				String nodeId = jP1.get("result.node_id");
				
				//Publish Textbook unit
				setURI();
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
				when().
				post("/content/v3/publish/"+node3).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Create Textbook
				setURI();
				JSONObject js1 = new JSONObject(jsonCreateNestedCollection);
				js1.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
				String jsonCreateNestedCollection = js1.toString();
				jsonCreateNestedCollection = jsonCreateNestedCollection.replace("id1", nodeId);
				Response R3 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body(jsonCreateNestedCollection).
						with().
						contentType(JSON).
						when().
						post("content/v3/create").
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				JsonPath jP3 = R3.jsonPath();
				String textBookId = jP3.get("result.node_id");

				// Publish textbook
				setURI();
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
				when().
				post("/content/v3/publish/"+textBookId).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Get content and validate
				setURI();
				Response R4 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						when().
						get("/content/v3/read/"+textBookId).
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				// Validate the response
				JsonPath jp4 = R4.jsonPath();
				String n_status = jp4.get("result.content.status");
				String n_identifier = jp4.get("result.content.identifier");
				ArrayList<String> n_identifier1 = jp4.get("result.content.children.identifier");
				Assert.assertTrue (n_status.equals("Live")||n_status.equals("Processing")&&n_identifier.equals(textBookId)&&n_identifier1.contains(nodeId));
			}	
			
			// Create, upload and publish nested textbook with Live textbook unit and visibility-Parent and live children
			@Test
			public void publishNestedTextBookLiveTBUnitParentLiveChildrenExpectSuccess200(){
				String node1 = null;
				String node2 = null;
				String node3 = null;
				String node4 = null;
				int count = 1;
				while(count<=4){
					setURI();
					int rn = generateRandomInt(9999, 1999999);
					JSONObject js = new JSONObject(jsonCreateValidContent);
					if(count==1){
						js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==2){
						js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==3){
						js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==4){
						js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
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
							log().all().
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
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/uploadContent.zip")).
						when().
						post("/content/v3/upload/"+node1).
						then().
						//log().all().
						spec(get200ResponseSpec());
						
						// Publish content
						setURI();
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
						when().
						post("/content/v3/publish/"+node1).
						then().
						//log().all().
						spec(get200ResponseSpec());

					}
					if(count==2){
						node2 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node2).
						then().
						//log().all().
						spec(get200ResponseSpec());

						// Publish content
						setURI();
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
						when().
						post("/content/v3/publish/"+node2).
						then().
						//log().all().
						spec(get200ResponseSpec());
					}
					if(count==3){
						node3 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node3).
						then().
						//log().all().
						spec(get200ResponseSpec());
						
						// Publish content
						setURI();
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
						when().
						post("/content/v3/publish/"+node3).
						then().
						//log().all().
						spec(get200ResponseSpec());
					}
					if(count==4){
						node4 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node4).
						then().
						//log().all().
						spec(get200ResponseSpec());
						
						// Publish content
						setURI();
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
						when().
						post("/content/v3/publish/"+node4).
						then().
						//log().all().
						spec(get200ResponseSpec());
					}
					count++;
				}
				// Create TextbookUnit
				setURI();
				JSONObject js2 = new JSONObject(jsonCreateTextbookUnit);
				js2.getJSONObject("request").getJSONObject("content").put("visibility", "Parent");
				String jsonCreateTextbookUnit = js2.toString();
				jsonCreateTextbookUnit = jsonCreateTextbookUnit.replace("id1", node1).replace("id2", node2).replace("id3", node3).replace("id4", node4);
				Response R1 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body(jsonCreateTextbookUnit).
						with().
						contentType(JSON).
						when().
						post("content/v3/create").
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				JsonPath jP1 = R1.jsonPath();
				String nodeId = jP1.get("result.node_id");
				
				//Publish Textbook unit
				setURI();
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
				when().
				post("/content/v3/publish/"+nodeId).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Create Textbook
				setURI();
				JSONObject js1 = new JSONObject(jsonCreateNestedCollection);
				js1.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
				String jsonCreateNestedCollection = js1.toString();
				jsonCreateNestedCollection = jsonCreateNestedCollection.replace("id1", nodeId);
				Response R3 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body(jsonCreateNestedCollection).
						with().
						contentType(JSON).
						when().
						post("content/v3/create").
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				JsonPath jP3 = R3.jsonPath();
				String textBookId = jP3.get("result.node_id");

				// Publish textbook
				setURI();
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
				when().
				post("/content/v3/publish/"+textBookId).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Get content and validate
				setURI();
				Response R4 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						when().
						get("/content/v3/read/"+textBookId).
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				// Validate the response
				JsonPath jp4 = R4.jsonPath();
				String n_status = jp4.get("result.content.status");
				String n_identifier = jp4.get("result.content.identifier");
				ArrayList<String> n_identifier1 = jp4.get("result.content.children.identifier");
				Assert.assertTrue (n_status.equals("Live")||n_status.equals("Processing")&&n_identifier.equals(textBookId)&&n_identifier1.contains(nodeId));
			}
			
			
			// Create, upload and publish nested textbook with Live textbook unit and visibility-Parent and live children
			@Test
			public void publishNestedTextBookWithMultipleTBUnitParentExpectSuccess200(){
					String node1 = null;
				String node2 = null;
				String node3 = null;
				String node4 = null;
				int count = 1;
				while(count<=4){
					setURI();
					int rn = generateRandomInt(9999, 1999999);
					JSONObject js = new JSONObject(jsonCreateValidContent);
					if(count==1){
						js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==2){
						js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==3){
						js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
					}
					if(count==4){
						js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("identifier", "LP_NFT_T_"+rn+"").put("name", "LP_NFT_T-"+rn+"");	
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
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/uploadContent.zip")).
						when().
						post("/content/v3/upload/"+node1).
						then().
						//log().all().
						spec(get200ResponseSpec());
						
						// Publish content
						setURI();
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
						when().
						post("/content/v3/publish/"+node1).
						then().
						//log().all().
						spec(get200ResponseSpec());

					}
					if(count==2){
						node2 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node2).
						then().
						//log().all().
						spec(get200ResponseSpec());

						// Publish content
						setURI();
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
						when().
						post("/content/v3/publish/"+node2).
						then().
						//log().all().
						spec(get200ResponseSpec());
					}
					if(count==3){
						node3 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node3).
						then().
						//log().all().
						spec(get200ResponseSpec());
						
						// Publish content
						setURI();
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
						when().
						post("/content/v3/publish/"+node3).
						then().
						//log().all().
						spec(get200ResponseSpec());
					}
					if(count==4){
						node4 = nodeId;

						// Upload Content
						setURI();
						given().
						spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
						multiPart(new File(path+"/tweenAndaudioSprite.zip")).
						when().
						post("/content/v3/upload/"+node4).
						then().
						//log().all().
						spec(get200ResponseSpec());
						
						// Publish content
						setURI();
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
						when().
						post("/content/v3/publish/"+node4).
						then().
						//log().all().
						spec(get200ResponseSpec());
					}
					count++;
				}
				// Create TextbookUnit1
				setURI();
				JSONObject js2 = new JSONObject(jsonCreateTextbookUnit);
				js2.getJSONObject("request").getJSONObject("content").put("visibility", "Parent");
				String jsonCreateTextbookUnit = js2.toString();
				jsonCreateTextbookUnit = jsonCreateTextbookUnit.replace("id1", node1).replace("id2", node2).replace("id3", node3).replace("id4", node4);
				Response R1 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body(jsonCreateTextbookUnit).
						with().
						contentType(JSON).
						when().
						post("content/v3/create").
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				JsonPath jP1 = R1.jsonPath();
				String nodeId = jP1.get("result.node_id");
				
				//Publish Textbook unit 1
				setURI();
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
				when().
				post("/content/v3/publish/"+nodeId).
				then().
				//log().all().
				spec(get200ResponseSpec());
				
				// Create TextbookUnit 2
				setURI();
				JSONObject js3 = new JSONObject(jsonCreateNestedCollection);
				js3.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
				String jsonCreateNestedCollection2 = js3.toString();
				jsonCreateNestedCollection2 = jsonCreateNestedCollection2.replace("id1", nodeId).replace("Test_QANested_", "Test_Textbook2_");
				Response R2 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body(jsonCreateNestedCollection2).
						with().
						contentType(JSON).
						when().
						post("content/v3/create").
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				JsonPath jP2 = R2.jsonPath();
				String nodeId2 = jP2.get("result.node_id");
				
				//Publish Textbook unit
				setURI();
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
				when().
				post("/content/v3/publish/"+nodeId2).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Create Textbook
				setURI();
				JSONObject js1 = new JSONObject(jsonCreateNestedCollection);
				js1.getJSONObject("request").getJSONObject("content").put("contentType", "TextBook");
				String jsonCreateNestedCollection = js1.toString();
				jsonCreateNestedCollection = jsonCreateNestedCollection.replace("id1", nodeId2);
				Response R3 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						body(jsonCreateNestedCollection).
						with().
						contentType(JSON).
						when().
						post("content/v3/create").
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				JsonPath jP3 = R3.jsonPath();
				String textBookId = jP3.get("result.node_id");

				// Publish textbook
				setURI();
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
				when().
				post("/content/v3/publish/"+textBookId).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Get content and validate
				setURI();
				Response R4 =
						given().
						spec(getRequestSpecification(contentType, validuserId, APIToken)).
						when().
						get("/content/v3/read/"+textBookId).
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				// Validate the response
				JsonPath jp4 = R4.jsonPath();
				String n_status = jp4.get("result.content.status");
				String n_identifier = jp4.get("result.content.identifier");
				ArrayList<String> n_identifier1 = jp4.get("result.content.children.identifier");
				Assert.assertTrue (n_status.equals("Live")||n_status.equals("Processing")&&n_identifier.equals(textBookId)&&n_identifier1.contains(nodeId2));
			}
			
		// Publish content with malformed XML body

		//Create content
		@Test
		public void publishMalformedJSONContentExpect4xx(){
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

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");

			// Upload Content
			setURI();
			Response R1 = 
			given().
			spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
			multiPart(new File(path+"/uploadContent.zip")).
			when().
			post("/content/v3/upload/"+nodeId).
			then().
			spec(get200ResponseSpec()).
			//log().all().
			extract().response();
			
			// Extracting the JSON path
			JsonPath jp1 = R1.jsonPath();
			String versionKey1 = jp1.get("result.versionKey");


			// Update the body with malformed XML
			setURI();
			JSONObject js = new JSONObject(jsonUpdateContentValid);
			js.getJSONObject("request").getJSONObject("content").put("versionKey", versionKey1).put("body", malformedJSONBody).remove("status");
			jsonUpdateContentValid = js.toString();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body(jsonUpdateContentValid).
			with().
			contentType("application/json").
			when().
			patch("/content/v3/update/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Get body and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId+"?fields=body").
					then().
					extract().
					response();

			JsonPath jP2 = R2.jsonPath();
			String body = jP2.get("result.content.body");
			Assert.assertFalse((isValidJSON(body) || isValidXML(body)));
		}

		// Publish content with malformed XML body

		//Create content
		@Test
		public void publishMalformedXMLContentExpect4xx(){
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
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");

			// Upload Content
			setURI();
			Response R1 = 
			given().
			spec(getRequestSpecification(uploadContentType, validuserId, APIToken)).
			multiPart(new File(path+"/uploadContent.zip")).
			when().
			post("/content/v3/upload/"+nodeId).
			then().
			spec(get200ResponseSpec()).
			//log().all().
			extract().response();
			
			// Extracting the JSON path
			JsonPath jp1 = R1.jsonPath();
			String versionKey1 = jp1.get("result.versionKey");

			// Update the body with malformed XML
			setURI();
			JSONObject js = new JSONObject(jsonUpdateContentValid);
			js.getJSONObject("request").getJSONObject("content").put("versionKey", versionKey1).put("body", malformedXMLBody).remove("status");
			jsonUpdateContentValid = js.toString();
			given().
			spec(getRequestSpecification(contentType, validuserId, APIToken)).
			body(jsonUpdateContentValid).
			with().
			contentType("application/json").
			when().
			patch("/content/v3/update/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Get body and validate
			setURI();
			Response R2 =
					given().
					spec(getRequestSpecification(contentType, validuserId, APIToken)).
					when().
					get("/content/v3/read/"+nodeId+"?fields=body").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP2 = R2.jsonPath();
			String body = jP2.get("result.content.body");
			Assert.assertFalse((isValidJSON(body) || isValidXML(body)));
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

		// Private Members
		private boolean isValidXML(String body) {
			boolean isValid = true;
			if (!StringUtils.isBlank(body)) {
				try {
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					dBuilder.parse(new InputSource(new StringReader(body)));
				} catch(ParserConfigurationException | SAXException | IOException e) {
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
		private boolean accessURL(String nodeId) throws ClassCastException{
			boolean accessURL = false;
					
			// Publish created content
			setURI();
			given().
			spec(getRequestSpec(contentType, validuserId)).
			when().
			get("/learning/v2/content/publish/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());

			// Get content and validate
			setURI();
			Response R5 =
					given().
					spec(getRequestSpec(contentType, validuserId)).
					when().
					get("/learning/v2/content/"+nodeId).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			JsonPath jP5 = R5.jsonPath();
			String statusActual = jP5.get("result.content.status");	

			try{
			// Validating the status
			if (statusActual.equals("Processing")){
				for (int i=1000; i<=30000; i=i+1000){
					try{Thread.sleep(i);}catch(InterruptedException e){System.out.println(e);} 
					setURI();
					Response R3 =
							given().
							spec(getRequestSpec(contentType, validuserId)).
							when().
							get("/learning/v2/content/"+nodeId).
							then().
							//log().all().
							spec(get200ResponseSpec()).
							extract().
							response();

					// Validate the response
					JsonPath jp3 = R3.jsonPath();
					String statusUpdated = jp3.get("result.content.status");
					//System.out.println(statusUpdated);
					if (statusUpdated.equals("Processing")){
						i=i+1000;
					}
					if (statusUpdated.equals("Live")){
						break;
					}
				}
			}	
			
			// Get content and validate
			setURI();
			Response R1 =
					given().
					spec(getRequestSpec(contentType, validuserId)).
					when().
					get("/learning/v2/content/"+nodeId).
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

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
			//Float pkgVersionActual = jP1.get("result.content.pkgVersion");
			//System.out.println(pkgVersionActual);
			Float size = jP1.get("result.content.size");
			// Downloading the zip file from artifact url and ecar from download url and saving with different name

			String ecarName = "ecar_"+rn+"";
			String uploadFile = "upload_"+rn+"";

			FileUtils.copyURLToFile(new URL(artifactUrl), new File(downloadPath+"/"+uploadFile+".zip"));
			String uploadSource = downloadPath+"/"+uploadFile+".zip";

			FileUtils.copyURLToFile(new URL(downloadUrl), new File(downloadPath+"/"+ecarName+".zip"));		
			String source = downloadPath+"/"+ecarName+".zip";

			File Destination = new File(downloadPath+"/"+ecarName+"");
			String Dest = Destination.getPath();
			
			try {

				// Extracting the uploaded file using artifact url
				ZipFile zipUploaded = new ZipFile(uploadSource);
				zipUploaded.extractAll(Dest);

				// Downloaded from artifact url
				File uploadAssetsPath = new File(Dest+"/assets");
				File[] uploadListFiles = uploadAssetsPath.listFiles();

				// Extracting the ecar file
				ZipFile zip = new ZipFile(source);
				zip.extractAll(Dest);

				String folderName = nodeId;
				String dirName = Dest+"/"+folderName;


				File fileName = new File(dirName);
				File[] listofFiles = fileName.listFiles();

				for(File file : listofFiles){

					// Validating the ecar file

					if(file.isFile()){
						String fPath = file.getAbsolutePath();
						String fName = file.getName();
						//System.out.println(fName);
						
						if (fName.endsWith(".zip")|| fName.endsWith(".rar")|| fName.endsWith(".apk")){
							ZipFile ecarZip = new ZipFile(fPath);
							ecarZip.extractAll(dirName);

							// Fetching the assets
							File assetsPath = new File(dirName+"/assets");
							File[] extractedAssets = assetsPath.listFiles();						
							if (assetsPath.exists()){

								int assetCount = assetsPath.listFiles().length;
								//System.out.println(assetCount);

								int uploadAssetsCount = uploadAssetsPath.listFiles().length;
								//System.out.println(uploadAssetsCount);

								// Asserting the assets count in uploaded zip file and ecar file
								Assert.assertEquals(assetCount, uploadAssetsCount);

								// Compare the files in both of the folders are same
								compareFiles(uploadListFiles, extractedAssets);
							}
						}
						else{
							System.out.println("No zip file found");
						}
					}
					else{
						System.out.println("No zip file exists");
					}
				}

				// Validating the manifest 
				File manifest = new File(Dest+"/manifest.json");
				
				Gson gson = new Gson();
				JsonParser parser = new JsonParser();
	            
				JsonElement jsonElement = parser.parse(new FileReader(manifest));
				JsonObject obj = jsonElement.getAsJsonObject();
				
				JsonObject arc = obj.getAsJsonObject("archive");
				JsonArray items = arc.getAsJsonArray("items");
				
				@SuppressWarnings("rawtypes")

				// Extracting the metadata from manifest and assert with api response

				Iterator i = items.iterator();
				while(i.hasNext()) {
					try {
						JsonObject item = (JsonObject) i.next();
						String name = getStringValue(item, "name");						
						String mimeType = getStringValue(item, "mimeType");
						Assert.assertEquals(mimeTypeActual, mimeType);
						String status =  getStringValue(item, "status");
						Assert.assertEquals(statusUpdated, status);
						String code =  getStringValue(item, "code");
						Assert.assertEquals(codeActual, code);
						String osID =  getStringValue(item, "osId");
						Assert.assertEquals(osIdActual, osID);
						String contentType =  getStringValue(item, "contentType");
						Assert.assertEquals(contentTypeActual, contentType);
						String mediaType =  getStringValue(item, "mediaType");
						Assert.assertEquals(mediaTypeActual, mediaType);
						String description =  getStringValue(item, "description");
						Assert.assertEquals(descriptionActual, description);
						String pkgVersion =  getStringValue(item, "pkgVersion");
						//Assert.assertNotSame(pkgVersionActual, pkgVersion);
						Assert.assertTrue(artifactUrl.endsWith(".zip")||artifactUrl.endsWith(".apk")&&downloadUrl.endsWith(".ecar")&&statusUpdated.equals("Live"));
					}
					catch(JSONException jse){
						accessURL = false;
						//jse.printStackTrace();
					}
				}
			}				
			catch (Exception x){
				accessURL = false;
				//x.printStackTrace();	
			}
		}
		catch (Exception e) {
			accessURL = false;
			//e.printStackTrace();
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
		
		/*/ Async publish validations - Other contents
		public void asyncPublishValidationContents(String nodeId, String statusActual){
			for (int i=1000; i<=5000; i=i+1000){
				try{Thread.sleep(i);}catch(InterruptedException e){System.out.println(e);} 
				setURI();
				Response R3 =
						given().
						spec(getRequestSpec(contentType, validuserId)).
						when().
						get("/learning/v2/content/"+nodeId).
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				// Validate the response
				JsonPath jp3 = R3.jsonPath();
				String statusUpdated = jp3.get("result.content.status");
				//System.out.println(statusUpdated);
				if (statusUpdated.equals("Processing")){
					i=i+1000;
				}
				if (statusUpdated.equals("Live")){
					System.out.println(statusUpdated);
				}
			}
		}*/
		
		// Async Publish validations - Collection
		public void asyncPublishValidations(ArrayList<String> identifier1, String status, String nodeId, String c_identifier, String node1, String node2){
			if(status.equals("Processing")){
				for (int i=1000; i<=30000; i=i+1000){
					try{Thread.sleep(i);}catch(InterruptedException e){System.out.println(e);} 
					setURI();
					Response R3 =
							given().
							spec(getRequestSpec(contentType, validuserId)).
							when().
							get("/learning/v2/content/"+nodeId).
							then().
							//log().all().
							spec(get200ResponseSpec()).
							extract().
							response();

					// Validate the response
					JsonPath jp3 = R3.jsonPath();
					String statusUpdated = jp3.get("result.content.status");
					if (statusUpdated.equals("Processing")){
						//System.out.println(statusUpdated);
						i++;
					}
					if (statusUpdated.equals("Live")){
						Assert.assertTrue(c_identifier.equals(nodeId)&&identifier1.contains(node1)&&identifier1.contains(node2));
						break;
					}
				}
			}
			else if (status.equals("Live")){
				Assert.assertTrue(c_identifier.equals(nodeId)&&identifier1.contains(node1)&&identifier1.contains(node2));
			}
		}
		
		
		// Compare the files extracted from artifact URL and ECAR

		public String compareFiles(File[] uploadListFiles, File[] extractedAssets)
		{
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
			//Assert.assertTrue(final_status);
			if (final_status) {
				//System.out.println("Files are same");
				return "success";
			} else {
				//System.out.println(filesnotpresent);
				return filesnotpresent;
			}
		}
	}


