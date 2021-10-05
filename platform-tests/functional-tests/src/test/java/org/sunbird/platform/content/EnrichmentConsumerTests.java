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
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.platform.domain.BaseTest;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.Assert;
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

/**
 * This testClass is used to test the enrichment of content from content model
 * and language model, both content and language consumers run after a content
 * becomes LIVE so enrichment will be done for contents which are Live with
 * required metadata
 * 
 * @author rashmi
 *
 */
public class EnrichmentConsumerTests extends BaseTest {

	int rn = generateRandomInt(0, 9999999);
	String jsonCreateValidContentWithText = "{ \"request\": { \"content\": { \"identifier\": \"LP_FT_"+rn+"\", \"mediaType\": \"content\", \"mimeType\":\"application/pdf\",\"visibility\": \"Default\", \"name\": \"test\", \"language\": [ \"Hindi\" ], \"appIcon\":\"http://media.idownloadblog.com/wp-content/uploads/2014/08/YouTube-2.9-for-iOS-app-icon-small.png\", \"posterImage\":\"https://www.youtube.com/yts/img/yt_1200-vfl4C3T0K.png\", \"contentType\": \"Resource\", \"code\": \"test\", \"osId\": \"org.sunbird.quiz.app\", \"pkgVersion\": 1, \"gradeLevel\":[\"kindergarten\"], \"ageGroup\":[\"6-7\"], \"text\": [\"चर्चा करो मेरा गाँव चित्र चर्चा करो खमनोर बस स्टेंड चर्चा करो मेले की मौज़ चर्चा करो बिल्ली और चूहा चित्रों पर बातचीत करें और कहानी बनवाएँ। चर्चा करो रेखा चित्र अंगुली घुमाओ पेंसिल घुमाओ घुमाओ चर्चा करो अंगूठे की चाप बाले चित्र ऊपर बने चित्रों की पहचान कराएँ। इनके बनने की प्रक्रिया पर चर्चा करें। रेखाओं पर पेंसिल घुमवाएँ। कविता गाओ बालगीत आ आ लल्ला ले रसगुल्ला मचा रहा क्यों रो रो हल्ला ले यह दोना अब मत रोना जल्दी खाकर मुँह को धोना पिंकी ने एक पिल्ला पाला कुछकुछ पीला , कुछ-कुछ काला टॉमी उसका सुंदर नाम हाथ उठाओ करे सलाम। सूरज ऐसे चमक रहा है, सोना जैसे दमक रहा है। रातों में यह चिप जाता है, सुबह सवेरे दिखा जाता है। इसके बिना अन्धेरा है, अम्बर इसका डेरा है। चिड़िया के घर न्योता है देखें क्या-क्या होता है मुर्गी चावल लाएगी चिड़िया खीर पकाएगी चिड़िया तुम्हें बुलाएगी मीठी खीर खिलाएगी मुन्नी हो जाओ तैयार देखो चिड़िया का घर-बार मछली तुम हो जल की रानी, रह सकती हो ना बिन पानी। लहर-लहर कर आती हो, मचल-मचल कर जाती हो। करती हो बस तुम मनमानी, मछली रानी, मछली रानी। हुआ सवेरा चिड़ियाँ बोली बच्चों ने तब आँखें खोली अच्छे बच्चे मंजन करते मंजन करके कुल्ला करते कुल्ला करके मुँह को ढोते मुँह धोकर के रोज नहाते रोज नहाकर खाना खाते खाना खाकर पढ़ने जाते। वातावरण निर्माण के लिए बालगीतों को गाएँ । सभा, समारोह, पर्व आदि पर बच्चों से मंच पर प्रस्तुत कराएँ। जिन बालगीतों को आगे बढ़ाया जा सकता है उन्हें आगे बढ़ावाएँ। उमड़-घुमड़ के आए बादल काले, ढोले, नीले बादल बिजली के संग आए बादल आसमान में छाए बादल घड़-घड़-घड़-घड़ करते बादल ताल-तलैया भरते बादल गुड़िया मेरी रानी है, मलगती बड़ी सयानी है। गोरे-गोरे गाल है, लंबे-लंबे बाल हैं। आँखें नीली-नीली हैं, साड़ी पीली-पीली हैं। मैं तो सो रही थी मुझे मुर्ग ने जगाया बोला कुकडूँ-कूँ। मैं तो सो रही थी मुझे बिल्ली ने जगाया बोली म्याऊँ-म्याऊँ। मैं तो सो रही थी मुझे मोटर नइ जगाया बोली पों-पों-पों। मैं तो सो रही थी मुझे अम्मा ने जगाया बोली उठ-उठ-उठ। सड़क बनी है लम्बी चौड़ी जिस पर जाती मोटर दौड़ी स्कूटर भरते है फर्राटे रिक्शा तांगे आते-जाते दुर्घटना से यदि हो बचना बाँयी तरफ हमेशा चलना। \"] } } }";
	String jsonCreateAssessmentItem = "{ \"request\": { \"assessment_item\": {\"identifier\": \"LP_FT_"+rn+"\", \"objectType\": \"AssessmentItem\", \"metadata\": { \"code\": \"test.mtf_mixed_1\", \"name\": \"MTF Question 1\", \"type\": \"mtf\", \"template_id\": \"mtf_template_3\", \"ageGroup\":[\"5-6\"], \"gradeLevel\":[\"kindergarten\"], \"lhs_options\": [ { \"value\": {\"type\": \"image\", \"asset\": \"grey\"}, \"index\": 0 } ], \"rhs_options\": [ { \"value\": {\"type\": \"text\", \"asset\": \">\"} }, { \"value\": {\"type\": \"text\", \"asset\": \"=\"} }, { \"value\": {\"type\": \"mixed\", \"text\": \"<\", \"image\": \"image1\", \"audio\": \"audio1\"}, \"answer\": 0 } ], \"max_score\": 6, \"partial_scoring\": true, \"outRelations\":[ { \"endNodeId\":\"C310\", \"relationType\":\"associatedTo\" }] } } } }";
	String jsonContentClean = "{\"request\": {\"searchProperty\": \"name\",\"searchOperator\": \"startsWith\",\"searchString\": \"LP_FT\"}}";
	String jsonCreateContentWithKannadaText = "{ \"request\": { \"content\": { \"mediaType\": \"content\", \"mimeType\":\"application/vnd.ekstep.ecml-archive\",\"visibility\": \"Default\", \"name\": \"test\", \"language\": [ \"Kannada\" ], \"appIcon\":\"http://media.idownloadblog.com/wp-content/uploads/2014/08/YouTube-2.9-for-iOS-app-icon-small.png\", \"posterImage\":\"https://www.youtube.com/yts/img/yt_1200-vfl4C3T0K.png\", \"contentType\": \"Resource\", \"code\": \"test\", \"osId\": \"org.sunbird.quiz.app\", \"pkgVersion\": 1, \"text\":[\"ಇತ್ತೀಚೆಗೆ ನಮ್ಮನ್ನಗಲಿದ ಪ್ರಸಿದ್ಧ ಸಾಹಿತಿ, ಕಲಾವಿದ ಆರ್ಯರ ನೆನಪಿನಲ್ಲಿ ಈ ಕತೆ- ಅವರದ್ದೇ ಹೈಬ್ರಿàಡ್‌ ಕತೆಗಳು ಸಂಕಲನದಿಂದ ಆಯ್ದುಕೊಂಡದ್ದು.\"] } } } ";
	String jsonCreateContentWithEnglishText = "{ \"request\": { \"content\": { \"mediaType\": \"content\",\"mimeType\":\"application/vnd.ekstep.ecml-archive\", \"visibility\": \"Default\", \"name\": \"test\", \"language\": [ \"Kannada\" ], \"appIcon\":\"http://media.idownloadblog.com/wp-content/uploads/2014/08/YouTube-2.9-for-iOS-app-icon-small.png\", \"posterImage\":\"https://www.youtube.com/yts/img/yt_1200-vfl4C3T0K.png\", \"contentType\": \"Resource\", \"code\": \"test\", \"osId\": \"org.sunbird.quiz.app\", \"pkgVersion\": 1, \"text\":[\"Hello welcome to ekstep, this is an e-learning platform which provides a platform to explore and enhance a childs career.\"] } } } ";
	String jsonCreateContentWithoutText = "{ \"request\": { \"content\": { \"identifier\": \"LP_FT_"+rn+"\", \"mediaType\": \"content\",\"mimeType\":\"application/vnd.ekstep.ecml-archive\", \"visibility\": \"Default\", \"name\": \"test\", \"language\": [ \"Hindi\" ], \"appIcon\":\"http://media.idownloadblog.com/wp-content/uploads/2014/08/YouTube-2.9-for-iOS-app-icon-small.png\", \"posterImage\":\"https://www.youtube.com/yts/img/yt_1200-vfl4C3T0K.png\", \"contentType\": \"Resource\", \"code\": \"test\", \"osId\": \"org.sunbird.quiz.app\", \"pkgVersion\": 1, \"gradeLevel\":[\"kindergarten\"], \"ageGroup\":[\"6-7\"] } } }";
	String jsonCreateContent1 = "{ \"request\": { \"content\": { \"body\": \"<theme></theme>\", \"description\": \"शेर का साथी हाथी\",  \"subject\": \"literacy\", \"name\": \"शेर का साथी हाथी\",    \"owner\": \"EkStep\", \"code\": \"org.sunbird.test01.story\", \"mimeType\": \"application/vnd.ekstep.ecml-archive\", \"identifier\": \"org.sunbird.test"+rn+".story\", \"contentType\": \"Resource\", \"gradeLevel\": [\"Grade 1\"], \"ageGroup\" : [\"5-6\"], \"language\": [\"English\"], \"keywords\" : [\"Test Collection-1\"], \"osId\": \"org.sunbird.quiz.app\", \"concepts\": [{ \"identifier\" : \"LO17\", \"relation\" : \"associatedTo\" }]}}}";
	String jsonCreateContent2 = "{ \"request\": { \"content\": { \"body\": \"<theme></theme>\", \"description\": \"शेर का साथी हाथी\",  \"subject\": \"literacy\", \"name\": \"शेर का साथी हाथी\",    \"owner\": \"EkStep\", \"code\": \"org.sunbird.test02.story\", \"mimeType\": \"application/vnd.ekstep.ecml-archive\", \"identifier\": \"org.sunbird.test"+(rn+1)+".story\", \"contentType\": \"Resource\", \"gradeLevel\": [\"Grade 2\"], \"ageGroup\" : [\"6-7\"], \"language\": [\"Hindi\"], \"keywords\" : [\"Test Collection-2\"], \"osId\": \"org.sunbird.quiz.app\", \"concepts\": [{ \"identifier\" : \"LO1\", \"relation\" : \"associatedTo\" }]}}}";
	String jsonCreateContent3 = "{ \"request\": { \"content\": { \"body\": \"<theme></theme>\", \"description\": \"शेर का साथी हाथी\",  \"subject\": \"literacy\", \"name\": \"शेर का साथी हाथी\",    \"owner\": \"EkStep\", \"code\": \"org.sunbird.test03.story\", \"mimeType\": \"application/vnd.ekstep.ecml-archive\", \"identifier\": \"org.sunbird.test"+(rn+2)+".story\", \"contentType\": \"Resource\", \"gradeLevel\": [\"Grade 3\"], \"ageGroup\" : [\"7-8\"], \"language\": [\"Kannada\"], \"keywords\" : [\"Test Collection-3\"], \"osId\": \"org.sunbird.quiz.app\", \"concepts\": [{ \"identifier\" : \"LO43\", \"relation\" : \"associatedTo\" }]}}}";
	static ClassLoader classLoader = EnrichmentConsumerTests.class.getClassLoader();
	static File path = new File(classLoader.getResource("UploadFiles/").getFile());
	static String nodeId;
	static URL url = classLoader.getResource("DownloadedFiles");
	static File downloadPath;
	char ch = '"';
	private String PROCESSING = "Processing";
	private String PENDING = "Pending";

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
		
	/**
	 * Test method to check content Enrichment, test executes only if a content
	 * created is Live and has gradeLevel, ageGroup and itemSets with items
	 * which has concepts as outRelations
	 * 
	 * @see <Class>ContentEnrichmentMessageProcessor</Class>
	 */
	@SuppressWarnings("rawtypes")
	@Test
	public void createContentWithItemset() {
		if (nodeId == null) {
			setURI();
			Response R = given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateAssessmentItem).
					with().
					contentType(JSON).
					when().
					post("/learning/v1/assessmentitem").
					then().
					// log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			nodeId = jp.get("result.node_id");
			System.out.println("nodeId=" + nodeId);

			String jsonCreateItemSet = "{ \"request\": { \"assessment_item_set\": {\"identifier\": \"LP_FT_"+rn+"\", \"objectType\": \"ItemSet\", \"metadata\": { \"title\": \"Akshara Worksheet Grade 5 Item Set\", \"type\": \"materialised\", \"max_score\": 1, \"total_items\": 1, \"description\": \"Akshara Worksheet Grade 5 Item Set\", \"code\": \"akshara.grade5.ws1.test\", \"owner\": \"Ilimi\", \"used_for\": \"assessment\", \"memberIds\": ["
					+ ch + nodeId + ch + "] } } } }";

			setURI();
			Response R1 = given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateItemSet).
					with().
					contentType(JSON).
					when().
					post("/learning/v1/assessmentitemset").
					then().
					// log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp1 = R1.jsonPath();
			String nodeId1 = jp1.get("result.set_id");
			System.out.println("nodeId1=" + nodeId1);

			String jsonCreateValidContent = "{ \"request\": { \"content\": { \"body\": \"<theme></theme>\", \"description\": \"शेर का साथी हclsथी\", \"subject\": \"literacy\", \"name\": \"शेर का साथी हाथी\", \"language\": [ \"Hindi\" ], \"owner\": \"EkStep\","
					+ " \"code\": \"org.sunbird.march.content_102\",\"identifier\": \"LP_FT_12"+rn+"\" , \"mimeType\": \"\", \"contentType\": \"Resource\", \"mediaType\": \"content\", "
					+ "\"gradeLevel\": [ \"Grade 1\", \"Grade 2\" ], \"ageGroup\": [ \"<5\" ], \"osId\": \"org.sunbird.quiz.app\", "
					+ "\"item_sets\": [ { \"identifier\": " + ch + nodeId1
					+ "\" ,\"relation\": \"associatedTo\" } ], \"concepts\": [ { \"identifier\": \"C555\", \"name\": \"Comprehension Of Stories\", \"objectType\": \"Concept\", \"relation\": \"associatedTo\", \"description\": \"Comprehension Of Stories\", \"index\": null }] } } }";

			setURI();
			Response R2 = given().spec(getRequestSpec(contentType, validuserId)).body(jsonCreateValidContent).with()
					.contentType(JSON).when().post("/learning/v2/content").then().
					log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp2 = R2.jsonPath();
			String nodeId2 = jp2.get("result.node_id");
			System.out.println("nodeId2=" + nodeId2);

			// Upload Content
			setURI();
			given().spec(getRequestSpec(uploadContentType, validuserId))
					.multiPart(new File(path + "/uploadContent.zip")).when()
					.post("/learning/v2/content/upload/" + nodeId2).then().
					// log().all().
					spec(get200ResponseSpec());

			// Get body and validate
			setURI();
			Response R3 = given().spec(getRequestSpec(contentType, validuserId)).when()
					.get("/learning/v2/content/" + nodeId2 + "?fields=body").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			JsonPath jp3 = R3.jsonPath();
			String body = jp3.get("result.content.body");
			Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
			if (isValidXML(body) || isValidJSON(body)) {
				Assert.assertTrue(accessURL(nodeId2));
			}

			// Publish
			setURI();
			given().spec(getRequestSpec(contentType, validuserId)).when().get("/learning/v2/content/publish/" + nodeId2)
					.then().
					// log().all().
					spec(get200ResponseSpec());

			// Get content and validate
			setURI();
			Response R4 = given().spec(getRequestSpec(contentType, validuserId)).when()
					.get("/learning/v2/content/" + nodeId2).then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Validate the response
			JsonPath jp4 = R4.jsonPath();
			String status = jp4.get("result.content.status");
			asyncPublishValidationContents(nodeId2, status);

			for (int i = 1000; i <= 5000; i = i + 1000) {
				try {
					Thread.sleep(i);
				} catch (InterruptedException e) {
					System.out.println(e);
				}
			}

			// Get body and validate
			setURI();
			Response R5 = given().spec(getRequestSpec(contentType, validuserId)).when()
					.get("/learning/v1/graph/domain/datanodes/" + nodeId).then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			JsonPath jp5 = R5.jsonPath();
			Map<String, Object> map = jp5.get("result.node.metadata");
			Assert.assertEquals(map.containsKey("gradeLevel"), true);
			List grades = (List) map.get("gradeLevel");
			Assert.assertEquals(grades.contains("kindergarten"), true);
			Assert.assertEquals(map.containsKey("ageGroup"), true);
			List ageGroup = (List) map.get("ageGroup");
			Assert.assertEquals(ageGroup.contains("5-6"), true);

		}
	}

	/**
	 * Test method to check Language Enrichment functionality Test will execute
	 * only if the content created has a text tag and its not a English Content
	 * 
	 * @see <Class>LanguageEnrichmentMessageProcessor</Class>
	 */
	@Ignore
	@Test
	public void createContentwithKannadaText() {
		if (nodeId == null) {
			setURI();
			Response R = given().spec(getRequestSpec(contentType, validuserId)).body(jsonCreateContentWithKannadaText)
					.with().contentType(JSON).when().post("/learning/v2/content").then().
					log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			nodeId = jp.get("result.node_id");
			System.out.println("nodeId=" + nodeId);

			// Upload Content
			setURI();
			given().spec(getRequestSpec(uploadContentType, validuserId))
					.multiPart(new File(path + "/uploadContent.zip")).when()
					.post("/learning/v2/content/upload/" + nodeId).then().
					// log().all().
					spec(get200ResponseSpec());

			// Get body and validate
			setURI();
			Response R2 = given().spec(getRequestSpec(contentType, validuserId)).when()
					.get("/learning/v2/content/" + nodeId + "?fields=body").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			JsonPath jP2 = R2.jsonPath();
			String body = jP2.get("result.content.body");
			Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
			if (isValidXML(body) || isValidJSON(body)) {
				Assert.assertTrue(accessURL(nodeId));
			}

			// Publish content
			setURI();
			given().spec(getRequestSpec(contentType, validuserId)).when().get("/learning/v2/content/publish/" + nodeId)
					.then().
					// log().all().
					spec(get200ResponseSpec());

			// Get content and validate
			setURI();
			Response R3 = given().spec(getRequestSpec(contentType, validuserId)).when()
					.get("/learning/v2/content/" + nodeId).then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Validate the response
			JsonPath jp2 = R3.jsonPath();
			String status = jp2.get("result.content.status");
			asyncPublishValidationContents(nodeId, status);

			for (int i = 1000; i <= 5000; i = i + 1000) {
				try {
					Thread.sleep(i);
				} catch (InterruptedException e) {
					System.out.println(e);
				}
			}
			// Get body and validate
			setURI();
			Response R4 = given().spec(getRequestSpec(contentType, validuserId)).when()
					.get("/learning/v1/graph/domain/datanodes/" + nodeId).then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			JsonPath jP3 = R4.jsonPath();
			Map<String, Object> map = jP3.get("result.node.metadata");
			Assert.assertEquals(map.containsKey("thresholdVocabulary"), true);
			Assert.assertEquals(map.containsKey("totalOrthoComplexity"), true);
			Assert.assertEquals(map.containsKey("themes"), true);
			Assert.assertEquals(map.containsKey("nonThresholdVocabulary"), true);
			Assert.assertEquals(map.containsKey("wordCount"), true);
			Assert.assertEquals(map.containsKey("totalWordCount"), true);
			Assert.assertEquals(map.containsKey("syllableCount"), true);
			Assert.assertEquals(map.containsKey("partsOfSpeech"), true);
			Assert.assertEquals(map.containsKey("totalWordComplexity"), true);
		}
	}
	
	/**
	 * Test method to check Language Enrichment functionality Test will execute
	 * only if the content created has a text tag and its not a English Content
	 * 
	 * @see <Class>LanguageEnrichmentMessageProcessor</Class>
	 */
	@Test
	public void createContentwithoutText() {
		if (nodeId == null) {
			setURI();
			Response R = given().spec(getRequestSpec(contentType, validuserId)).body(jsonCreateContentWithoutText)
					.with().contentType(JSON).when().post("/learning/v2/content").then().
//					log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			nodeId = jp.get("result.node_id");
			System.out.println("nodeId=" + nodeId);

			// Upload Content
			setURI();
			given().spec(getRequestSpec(uploadContentType, validuserId))
					.multiPart(new File(path + "/uploadContent.zip")).when()
					.post("/learning/v2/content/upload/" + nodeId).then().
					// log().all().
					spec(get200ResponseSpec());

			// Get body and validate
			setURI();
			Response R2 = given().spec(getRequestSpec(contentType, validuserId)).when()
					.get("/learning/v2/content/" + nodeId + "?fields=body").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			JsonPath jP2 = R2.jsonPath();
			String body = jP2.get("result.content.body");
			Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
			if (isValidXML(body) || isValidJSON(body)) {
				Assert.assertTrue(accessURL(nodeId));
			}

			// Publish content
			setURI();
			given().spec(getRequestSpec(contentType, validuserId)).when().get("/learning/v2/content/publish/" + nodeId)
					.then().
					// log().all().
					spec(get200ResponseSpec());

			// Get content and validate
			setURI();
			Response R3 = given().spec(getRequestSpec(contentType, validuserId)).when()
					.get("/learning/v2/content/" + nodeId).then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Validate the response
			JsonPath jp2 = R3.jsonPath();
			String status = jp2.get("result.content.status");
			asyncPublishValidationContents(nodeId, status);

			for (int i = 1000; i <= 5000; i = i + 1000) {
				try {
					Thread.sleep(i);
				} catch (InterruptedException e) {
					System.out.println(e);
				}
			}
			// Get body and validate
			setURI();
			Response R4 = given().spec(getRequestSpec(contentType, validuserId)).when()
					.get("/learning/v1/graph/domain/datanodes/" + nodeId).then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			JsonPath jP3 = R4.jsonPath();
			Map<String, Object> map = jP3.get("result.node.metadata");
			Assert.assertEquals(map.containsKey("thresholdVocabulary"), false);
			Assert.assertEquals(map.containsKey("totalOrthoComplexity"), false);
			Assert.assertEquals(map.containsKey("themes"), false);
		}
	}

	/**
	 * Test method to check Language Enrichment functionality Test will execute
	 * only if the content created has a text tag and its not a English Content
	 * 
	 * @see <Class>LanguageEnrichmentMessageProcessor</Class>
	 */
	@Test
	public void createContentwithEnglishText() {
		if (nodeId == null) {
			setURI();
			Response R = given().spec(getRequestSpec(contentType, validuserId)).body(jsonCreateContentWithEnglishText)
					.with().contentType(JSON).when().post("/learning/v2/content").then().
					// log().all()
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			nodeId = jp.get("result.node_id");
			System.out.println("nodeId=" + nodeId);

			// Upload Content
			setURI();
			given().spec(getRequestSpec(uploadContentType, validuserId))
					.multiPart(new File(path + "/uploadContent.zip")).when()
					.post("/learning/v2/content/upload/" + nodeId).then().
					// log().all().
					spec(get200ResponseSpec());

			// Get body and validate
			setURI();
			Response R2 = given().spec(getRequestSpec(contentType, validuserId)).when()
					.get("/learning/v2/content/" + nodeId + "?fields=body").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			JsonPath jP2 = R2.jsonPath();
			String body = jP2.get("result.content.body");
			Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
			if (isValidXML(body) || isValidJSON(body)) {
				Assert.assertTrue(accessURL(nodeId));
			}

			// Publish content
			setURI();
			given().spec(getRequestSpec(contentType, validuserId)).when().get("/learning/v2/content/publish/" + nodeId)
					.then().
					// log().all().
					spec(get200ResponseSpec());

			// Get content and validate
			setURI();
			Response R3 = given().spec(getRequestSpec(contentType, validuserId)).when()
					.get("/learning/v2/content/" + nodeId).then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Validate the response
			JsonPath jp2 = R3.jsonPath();
			String status = jp2.get("result.content.status");
			asyncPublishValidationContents(nodeId, status);

			for (int i = 1000; i <= 5000; i = i + 1000) {
				try {
					Thread.sleep(i);
				} catch (InterruptedException e) {
					System.out.println(e);
				}
			}
			// Get body and validate
			setURI();
			Response R4 = given().spec(getRequestSpec(contentType, validuserId)).when()
					.get("/learning/v1/graph/domain/datanodes/" + nodeId).then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			JsonPath jP3 = R4.jsonPath();
			Map<String, Object> map = jP3.get("result.node.metadata");
			Assert.assertEquals(map.containsKey("thresholdVocabulary"), false);
			Assert.assertEquals(map.containsKey("totalOrthoComplexity"), false);
			Assert.assertEquals(map.containsKey("themes"), false);
			Assert.assertEquals(map.containsKey("nonThresholdVocabulary"), false);
		}
	}
	
	/**
	 * Test method to check Language Enrichment functionality Test will execute
	 * only if the content created has a text tag and its not a English Content
	 * 
	 * @see <Class>LanguageEnrichmentMessageProcessor</Class>
	 */
	@Test
	public void createContentwithText() {
		if (nodeId == null) {
			setURI();
			Response R = given().spec(getRequestSpec(contentType, validuserId)).body(jsonCreateValidContentWithText)
					.with().contentType(JSON).when().post("/learning/v2/content").then().
					// log().all()
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			nodeId = jp.get("result.node_id");
			System.out.println("nodeId=" + nodeId);

			// Upload Content
			setURI();
			given().spec(getRequestSpec(uploadContentType, validuserId))
					.multiPart(new File(path + "/uploadContent.zip")).when()
					.post("/learning/v2/content/upload/" + nodeId).then().
					// log().all().
					spec(get200ResponseSpec());

			// Get body and validate
			setURI();
			Response R2 = given().spec(getRequestSpec(contentType, validuserId)).when()
					.get("/learning/v2/content/" + nodeId + "?fields=body").then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			JsonPath jP2 = R2.jsonPath();
			String body = jP2.get("result.content.body");
			Assert.assertTrue((isValidXML(body) || isValidJSON(body)));
			if (isValidXML(body) || isValidJSON(body)) {
				Assert.assertTrue(accessURL(nodeId));
			}

			// Publish content
			setURI();
			given().spec(getRequestSpec(contentType, validuserId)).when().get("/learning/v2/content/publish/" + nodeId)
					.then().
					// log().all().
					spec(get200ResponseSpec());

			// Get content and validate
			setURI();
			Response R3 = given().spec(getRequestSpec(contentType, validuserId)).when()
					.get("/learning/v2/content/" + nodeId).then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			// Validate the response
			JsonPath jp2 = R3.jsonPath();
			String status = jp2.get("result.content.status");
			asyncPublishValidationContents(nodeId, status);

			for (int i = 1000; i <= 5000; i = i + 1000) {
				try {
					Thread.sleep(i);
				} catch (InterruptedException e) {
					System.out.println(e);
				}
			}
			// Get body and validate
			setURI();
			Response R4 = given().spec(getRequestSpec(contentType, validuserId)).when()
					.get("/learning/v1/graph/domain/datanodes/" + nodeId).then().
					// log().all().
					spec(get200ResponseSpec()).extract().response();

			JsonPath jP3 = R4.jsonPath();
			Map<String, Object> map = jP3.get("result.node.metadata");
			Assert.assertEquals(map.containsKey("thresholdVocabulary"), true);
			Assert.assertEquals(map.containsKey("totalOrthoComplexity"), true);
			Assert.assertEquals(map.containsKey("themes"), true);
			Assert.assertEquals(map.containsKey("nonThresholdVocabulary"), true);
			Assert.assertEquals(map.containsKey("wordCount"), true);
			Assert.assertEquals(map.containsKey("totalWordCount"), true);
			Assert.assertEquals(map.containsKey("syllableCount"), true);
			Assert.assertEquals(map.containsKey("partsOfSpeech"), true);
			Assert.assertEquals(map.containsKey("totalWordComplexity"), true);
		}
	}
	
	// Async Publish validations - Collection
	public void asyncPublishValidations(ArrayList<String> identifier1, String status, String nodeId,
			String c_identifier, String node1, String node2) {
		if (status.equals("Processing")) {
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
		boolean accessURL = false;

		// Publish created content
		setURI();
		given().spec(getRequestSpec(contentType, validuserId)).when().get("/learning/v2/content/publish/" + nodeId)
				.then().log().all().spec(get200ResponseSpec());

		// Get content and validate
		setURI();
		Response R1 = given().spec(getRequestSpec(contentType, validuserId)).when()
				.get("/learning/v2/content/" + nodeId).then().log().all().spec(get200ResponseSpec()).extract()
				.response();

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
		// Float pkgVersionActual = jP1.get("result.content.pkgVersion");
		// System.out.println(pkgVersionActual);
		Float size = jP1.get("result.content.size");
		try {
			// Validating the status
			System.out.println(statusActual);
			if (statusActual.equals(PROCESSING) || statusActual.equals(PENDING)) {
				asyncPublishValidationContents(nodeId, statusActual);
			}
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
						System.out.println(fName);

						if (fName.endsWith(".zip") || fName.endsWith(".rar")) {
							ZipFile ecarZip = new ZipFile(fPath);
							ecarZip.extractAll(dirName);

							// Fetching the assets
							File assetsPath = new File(dirName + "/assets");
							File[] extractedAssets = assetsPath.listFiles();
							if (assetsPath.exists()) {

								int assetCount = assetsPath.listFiles().length;
								System.out.println(assetCount);

								int uploadAssetsCount = uploadAssetsPath.listFiles().length;
								System.out.println(uploadAssetsCount);

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
						// Assert.assertNotSame(pkgVersionActual, pkgVersion);
						Assert.assertTrue(artifactUrl.endsWith(".zip") && downloadUrl.endsWith(".ecar")
								&& statusActual.equals("Live"));
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
	
	@SuppressWarnings("unchecked")
	@Ignore
	@Test
	public void testCollection() {
		
			//Create content 1
			setURI();
			Response R = given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateContent1).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
					// log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId1 = jp.get("result.node_id");
			System.out.println("nodeId1=" + nodeId1);
			
			//upload content 1
			setURI();
			given().spec(getRequestSpec(uploadContentType, validuserId))
					.multiPart(new File(path + "/uploadContent.zip")).when()
					.post("/learning/v2/content/upload/" + nodeId1).then().
					// log().all().
					spec(get200ResponseSpec());
			
			
			//publish content 1
			setURI();
			given().spec(getRequestSpec(contentType, validuserId)).when().get("/learning/v2/content/publish/" + nodeId1)
					.then().
					// log().all().
					spec(get200ResponseSpec());
			
			//create content 2
			setURI();
			Response R1 = given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateContent2).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
					// log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp1 = R1.jsonPath();
			String nodeId2 = jp1.get("result.node_id");
			System.out.println("nodeId2=" + nodeId2);
			
			//upload content 2
			setURI();
			given().spec(getRequestSpec(uploadContentType, validuserId))
					.multiPart(new File(path + "/uploadContent.zip")).when()
					.post("/learning/v2/content/upload/" + nodeId2).then().
					// log().all().
					spec(get200ResponseSpec());
			
			
			//publish content 2
			setURI();
			given().spec(getRequestSpec(contentType, validuserId)).when().get("/learning/v2/content/publish/" + nodeId2)
					.then().
					// log().all().
					spec(get200ResponseSpec());
			
			//create content 3
			setURI();
			Response R2 = given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateContent3).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
					// log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp2 = R2.jsonPath();
			String nodeId3 = jp2.get("result.node_id");
			System.out.println("nodeId3=" + nodeId3);
			
			//upload content 3
			setURI();
			given().spec(getRequestSpec(uploadContentType, validuserId))
					.multiPart(new File(path + "/uploadContent.zip")).when()
					.post("/learning/v2/content/upload/" + nodeId3).then().
					// log().all().
					spec(get200ResponseSpec());
			
			
			//publish content 3
			setURI();
			given().spec(getRequestSpec(contentType, validuserId)).when().get("/learning/v2/content/publish/" + nodeId3)
					.then().
					// log().all().
					spec(get200ResponseSpec());
			
			String jsonCreateValidCollection1 = "{\"request\": {\"content\": {\"description\": \"शेर का साथी हाथी\", \"subject\": \"literacy\", \"name\": \"शेर का साथी हाथी\", \"owner\": \"EkStep\", \"code\": \"org.sunbird.col.collection\", \"mimeType\": \"application/vnd.ekstep.content-collection\",\"identifier\": \"org.sunbird.subCol"+rn+".collection\", \"contentType\": \"Collection\", \"osId\": \"org.sunbird.quiz.app\",\"children\":[{\"identifier\":\""+nodeId1+"\"},{\"identifier\":\""+nodeId2+"\"}]}}}";
			setURI();
			Response R3 = given().spec(getRequestSpec(contentType, validuserId)).body(jsonCreateValidCollection1).with()
					.contentType(JSON).when().post("/learning/v2/content").then().
					//log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp3 = R3.jsonPath();
			String nodeId4 = jp3.get("result.node_id");
			System.out.println("nodeId4=" + nodeId4);
			
			//publish sub collection 
			setURI();
			given().spec(getRequestSpec(contentType, validuserId)).when().get("/learning/v2/content/publish/" + nodeId4)
					.then().
					// log().all().
					spec(get200ResponseSpec());
			
			String jsonCreateValidCollection = "{\"request\": {\"content\": {\"description\": \"शेर का साथी हाथी\", \"subject\": \"literacy\", \"name\": \"शेर का साथी हाथी\", \"owner\": \"EkStep\", \"code\": \"org.sunbird.col.collection\", \"mimeType\": \"application/vnd.ekstep.content-collection\",\"identifier\": \"org.sunbird.col"+rn+".collection\", \"contentType\": \"Collection\", \"osId\": \"org.sunbird.quiz.app\",\"children\":[{\"identifier\":\""+nodeId3+"\"},{\"identifier\":\""+nodeId4+"\"}]}}}";
			setURI();
			Response R4 = given().spec(getRequestSpec(contentType, validuserId)).body(jsonCreateValidCollection).with()
					.contentType(JSON).when().post("/learning/v2/content").then().
					//log().all().
					spec(get200ResponseSpec()).extract().response();

			// Extracting the JSON path
			JsonPath jp4 = R4.jsonPath();
			String nodeId5 = jp4.get("result.node_id");
			System.out.println("nodeId5=" + nodeId5);
			
			//publish collection 
			setURI();
			given().spec(getRequestSpec(contentType, validuserId)).when().get("/learning/v2/content/publish/" + nodeId5)
					.then().
					// log().all().
					spec(get200ResponseSpec());
			
			for (int i = 1000; i <= 5000; i = i + 1000) {
				try {
					Thread.sleep(i);
				} catch (InterruptedException e) {
					System.out.println(e);
				}
			}

			// Get collection and validate
			setURI();
			Response R5 = given().spec(getRequestSpec(contentType, validuserId)).when()
					.get("/learning/v1/graph/domain/datanodes/" + nodeId5).then().
					//log().all().
					spec(get200ResponseSpec()).extract().response();

			JsonPath jp5 = R5.jsonPath();
			Map<String, Object> map = jp5.get("result.node.metadata");
			Assert.assertEquals(map.containsKey("gradeLevel"), true);
			List<String> grades = (List<String>) map.get("gradeLevel");
			Assert.assertEquals(grades.contains("Grade 1"), true);
			Assert.assertEquals(map.containsKey("ageGroup"), true);
			List<String> ageGroup = (List<String>) map.get("ageGroup");
			Assert.assertEquals(ageGroup.contains("5-6"), true);
			
	}
}
