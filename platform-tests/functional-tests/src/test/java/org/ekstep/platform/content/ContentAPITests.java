package org.ekstep.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.ekstep.platform.domain.BaseTest;
import org.hamcrest.CoreMatchers;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;


public class ContentAPITests extends BaseTest {
	
	
int rn = generateRandomInt(0, 500);
	
	String jsonGetContentList = "{\"request\": { \"search\": {\"contentType\": \"Story\", \"limit\":3}}}";
	String jsonGetContentListEmptySearch = "{\"request\": { \"search\": {}}}";
	
	String jsonCreateContentValid = "{\"request\": {\"content\": {\"body\": \"<theme></theme>\",\"status\": \"Draft\",\"description\": \"QA\",\"subject\": \"literacy\",\"name\": \"QA_"+rn+"\",\"owner\": \"EkStep\",\"s3Key\": \"content/build_a_sentence_1446706423188.zip\",\"downloadUrl\": \"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/build_a_sentence_1446706423188.zip\",\"osId\":\"Test_Id\",\"code\": \"org.ekstep.num.build.sentence\",\"contentType\": \"Story\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"pkgVersion\": 2, \"tags\": [\"English\"],\"children\": [{ \"identifier\": \"org.ekstep.story.en.vayu\", \"index\": 0},{\"identifier\": \"org.ekstep.story.en.tree\", \"index\": 1} ],\"concepts\": [{ \"identifier\": \"LO52\"}, { \"identifier\": \"LO51\"}]}}}";
	String jsonCreateContentWithRelations = "{\"request\": {\"content\": {\"identifier\": \"Test QA_"+rn+"\",\"contentType\": \"Story\",\"body\": \"<theme></theme>\",\"status\": \"Live\",\"description\": \"English Stories Collection\", \"subject\": \"literacy\",\"name\": \"English Stories Collection\", \"owner\": \"EkStep\",\"code\": \"content_collection_7\", \"tags\": \"[English]\", \"mimeType\": \"application/vnd.ekstep.content-collection\",\"osId\":\"Test_Id\",\"tags\": [\"English\"],\"children\": [{ \"identifier\": \"org.ekstep.story.en.vayu\", \"index\": 0},{\"identifier\": \"org.ekstep.story.en.tree\", \"index\": 1} ],\"concepts\": [{ \"identifier\": \"LO52\"}, { \"identifier\": \"LO51\"}]}}}";
	
	String jsoncreateNonExistingParentContent = "{\"request\": {\"content\": {\"identifier\": \"content_collection_8\",\"contentType\": \"Story\",\"body\": \"<theme></theme>\",\"status\": \"Live\",\"description\": \"English Stories Collection\", \"subject\": \"literacy\",\"name\": \"English Stories Collection\", \"owner\": \"EkStep\",\"code\": \"content_collection_7\", \"language\": \"English\", \"mimeType\": \"application/vnd.ekstep.content-collection\",\"tags\": [\"English\"],\"parent\": [{ \"identifier\": \"org.ekstep.story.en.vayu\", \"index\": 0},{\"identifier\": \"org.ekstep.story.en.tree\", \"index\": 1} ],\"concepts\": [{ \"identifier\": \"LO52\"}, { \"identifier\": \"LO51\"}]}}}";
	String jsonCreateInvalidContent = "{\"request\": {\"content\": {\"identifier\": \"content_collection_8\",\"contentType\": \"Story\",\"body\": \"<theme></theme>\",\"status\": \"Live\",\"description\": \"English Stories Collection\", \"subject\": \"literacy\",\"name\": \"English Stories Collection\", \"owner\": \"EkStep\",\"code\": \"content_collection_7\", \"language\": \"English\", \"mimeType\": \"application/vnd.ekstep.content-collection\",\"tags\": [\"English\"],\"children\": [{ \"identifier\": \"org.ekstep.story.en.vayu\", \"index\": 0},{\"identifier\": \"org.ekstep.story.en.tree\", \"index\": 1} ],\"concepts\": [{ \"identifier\": \"L102\"}, { \"identifier\": \"LO51\"}]}}}";
	String jsonCreateInvalidConceptsContent = "{\"request\": {\"content\": {\"identifier\": \"content_collection_8\",\"contentType\": \"Story\",\"body\": \"<theme></theme>\",\"status\": \"Live\",\"description\": \"English Stories Collection\", \"subject\": \"literacy\",\"name\": \"English Stories Collection\", \"owner\": \"EkStep\",\"code\": \"content_collection_7\", \"language\": \"English\", \"mimeType\": \"application/vnd.ekstep.content-collection\",\"tags\": [\"English\"],\"children\": [{ \"identifier\": \"org.ekstep.story.en.vayu\", \"index\": 0},{\"identifier\": \"org.ekstep.story.en.tree\", \"index\": 1} ],\"concepts\": [{ \"identifier\": \"LO52\"}, { \"identifier\": \"LO51\"}, {\"identifier\": \"Num:C3:SC1:MC12\"}]}}}";
	
	String jsonCreatenewConceptContent = "{\"request\": {\"content\": {\"body\": \"<theme></theme>\",\"status\": \"Draft\",\"description\": \"QA\",\"subject\": \"literacy\",\"name\": \"QA\",\"owner\": \"EkStep\",\"s3Key\": \"content/build_a_sentence_1446706423188.zip\",\"downloadUrl\": \"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/build_a_sentence_1446706423188.zip\",\"code\": \"org.ekstep.num.build.sentence\",\"contentType\": \"Story\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"pkgVersion\": 2, \"tags\": [\"English\"],\"children\": [{ \"identifier\": \"org.ekstep.story.en.vayu\", \"index\": 0},{\"identifier\": \"org.ekstep.story.en.tree\", \"index\": 1} ],\"concepts\": [{ \"identifier\": \"LO52\"}, { \"identifier\": \"LO51\"}]}}}";
	String jsonGetUpdatedContentList = "{\"request\": { \"search\": {\"name\": \"contentname\"}}}";
	String jsonUpdateContentValid = "{\"request\": {\"content\": {\"body\": \"<theme></theme>\",\"status\": \"Draft\",\"description\": \"QA_Updated test\",\"subject\": \"literacy\",\"name\": \"QA_Updated Test\",\"owner\": \"EkStep\",\"s3Key\": \"content/build_a_sentence_1446706423188.zip\",\"downloadUrl\": \"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/build_a_sentence_1446706423188.zip\",\"osId\":\"Test_Id\",\"code\": \"org.ekstep.num.build.sentence\",\"contentType\": \"Story\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"pkgVersion\": 2, \"tags\": [\"English\"],\"children\": [{ \"identifier\": \"org.ekstep.story.en.vayu\", \"index\": 0},{\"identifier\": \"org.ekstep.story.en.tree\", \"index\": 1} ],\"concepts\": [{ \"identifier\": \"LO52\"}, { \"identifier\": \"LO51\"}]}}}";
	String jsonCreateAndPublishContentValid = "{\"request\": {\"content\": {\"body\": \"<theme></theme>\",\"status\": \"Live\",\"description\": \"QA\",\"subject\": \"literacy\",\"name\": \"QA2\",\"owner\": \"EkStep\",\"s3Key\": \"content/build_a_sentence_1446706423188.zip\",\"downloadUrl\": \"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/build_a_sentence_1446706423188.zip\",\"code\": \"org.qa2.publish.content\",\"identifier\":\"org.qa2.publish.content\",\"contentType\": \"Worksheet\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"pkgVersion\": 2}}}";
	
	String jsonCreateValidExtractContent = "{\"request\": {\"content\": {\"identifier\": \"Test_QA_"+rn+"\",\"status\": \"Live\", \"mediaType\": \"content\", \"visibility\": \"Default\", \"name\": \"Test_QA_"+rn+"\", \"downloadUrl\": \"https://s3-ap-southeast-1.amazonaws.com/ekstep-public/content/1452241166851_PrathamStories_Day_2_JAN_8_2016.zip\", \"language\": [\"English\"], \"contentType\": \"Story\", \"code\": \"Test_QA_"+rn+"\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\", \"osId\": \"org.ekstep.quiz.app\", \"pkgVersion\": 1}}}";
	String jsonCreateValidExtractContentWithRelation = "{\"request\": {\"content\": {\"identifier\": \"Test_QA_"+rn+"\",\"status\": \"Live\", \"mediaType\": \"content\", \"visibility\": \"Default\", \"name\": \"Test_QA_"+rn+"\", \"downloadUrl\": \"https://s3-ap-southeast-1.amazonaws.com/ekstep-public/content/1452241166851_PrathamStories_Day_2_JAN_8_2016.zip\", \"language\": [\"English\"], \"contentType\": \"Story\", \"code\": \"Test_QA_"+rn+"\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\", \"children\": [{\"identifier\": \"org.ekstep.story.en.vayu\", \"index\": 0},{\"identifier\":\"org.ekstep.story.en.tree\", \"index\": 1}],\"osId\": \"org.ekstep.quiz.app\", \"pkgVersion\": 1}}}";

	
	String jsonCreateValidContentEcml = "{\"request\": {\"content\": {\"identifier\": \"Test_QA_"+rn+"\",\"osId\": \"org.ekstep.quiz.app\",\"status\": \"Live\",\"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"Test_QA_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"pkgVersion\": 3,\"owner\": \"EkStep\",\"body\":{\"theme\":{\"manifest\":{\"media\":[{\"id\":\"barber_img\",\"src\":\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/barber_1454918396799.png\",\"type\":\"image\"},{\"id\":\"tailor_img\",\"src\":\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/tailor_1454918475261.png\",\"type\":\"image\"},{\"id\":\"carpenter_img\",\"src\":\"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/carpenter_1454918523295.png\",\"type\":\"image\"}]}}}}}}";
	String invalidContentId = "TestQa_"+rn+"";
	
	Map<String, Object>  jsonAsMap = new HashMap<String, Object>();
	
	
	//jsonAsMap.put("firstName", "John");
	//jsonAsMap.put("lastName", "Doe");
	
	
	//Get Content List
	@Test
	public void getContentListExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonGetContentList).
			with().
				contentType("application/json").
			when().
				post("content/list").
			then().
				log().all().
				spec(get200ResponseSpec());
			
	}
	
	
	//Get Content List
	@Test
	public void getContentListEmptySearchExpect200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonGetContentListEmptySearch).
			with().
				contentType("application/json").
			when().
				post("content/list").
			then().
				log().all().
				spec(get200ResponseSpec());
			
	}
	

	//Search Content List
	@Test
	public void searchContentListExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonGetContentList).
			with().
				contentType("application/json").
			when().
				post("content/search").
			then().
				log().all().
				spec(get200ResponseSpec());
			
	}
	
	@Test
	public void searchContentListEmptySearchExpect200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonGetContentListEmptySearch).
			with().
				contentType("application/json").
			when().
				post("content/search").
			then().
				log().all().
				spec(get200ResponseSpec());
			
	}
	
	@Test
	public void getContentExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("content/org.ekstep.num.addition.by.grouping").
		then().
			log().all().
			spec(get200ResponseSpec());
	        
	}
	
	//Create Content
	@Test
	public void createContentValidExpectSuccess200(){
		
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateContentValid).
			with().
				contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		//Getting the node id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		System.out.println(nodeId);
		
		//Getting the created content
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/"+nodeId).
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	@Test
	public void createContentWithRelationsToConceptsExpect200(){		
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(jsonCreateContentWithRelations).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec());		
	}
	
	//Update Content 
	@Test
	public void updateContentValidInputsExpectSuccess200()
	{
		//Create new content
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateContentValid).
			with().
				contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		//Getting the nodeID
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		System.out.println(nodeId);
		
		// Update Content
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonUpdateContentValid).
			with().
				contentType("application/json").
			when().
				patch("content/"+nodeId).
			then().
				log().all().
				spec(get200ResponseSpec());
	}
	
	@Test
	public void updateContentNotExistingExpect400()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonUpdateContentValid).
			with().
				contentType("application/json").
			when().
				patch("content/Test_Qa_not existing1").
			then().
				log().all().
				spec(get400ResponseSpec());
	}
	
	@Test
	public void createNonExistingParentContentExpects400()
	{
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(jsoncreateNonExistingParentContent).		
		with().
			contentType(JSON).
		when().
			post("content").	
		then().
			log().all().
			spec(get400ResponseSpec());		
	}
	
	@Test
	public void createInvalidContentExpects400()
	{
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(jsonCreateInvalidContent).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	@Test
	public void createInvalidConceptsContentExpects400()
	{
	setURI();
	given().
	spec(getRequestSpec(contentType, validuserId)).
	body(jsonCreateInvalidConceptsContent).
	with().
		content(JSON).
	when().
		post("content").
	then().
		log().all().
		spec(get400ResponseSpec());
	}

	@Test
	public void createNewConceptContentExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidContentEcml).
		with().
			content(JSON).
		when().
			post("content").
		then().	
			log().all().
			spec(get200ResponseSpec());
	}
	
	// Create and Publish content Ecml
	
	//Create Content
	@Test
	public void createAndPublishValidContentEcmlExpectSuccess200() 
	{
		setURI();
		Response R = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidContentEcml).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Get node_id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		System.out.println(nodeId);
		
		// Publish created content
		setURI();
		Response R1 = 
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/publish/"+nodeId).
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP1 = R1.jsonPath();
		String contentURL = jP1.get("result.content_url");
	
		// Get Content
		setURI();
		Response R2 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/"+nodeId).
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jP2 = R2.jsonPath();
		String downloadURL = jP2.get("result.content.downloadUrl");
		Assert.assertEquals(contentURL, downloadURL);
		}
	
	// Create and Publish content Html
	
		//Create Content
		@Test
		public void createAndPublishValidContentHtmlExpectSuccess200() 
		{
			setURI();
			JSONObject js = new JSONObject(jsonCreateValidContentEcml);
			js.getJSONObject("request").getJSONObject("content").put("mimeType", "application/vnd.ekstep.html-archive");
			String jsonCreateValidContentHtml = js.toString();
			Response R = 
			given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidContentHtml).
			with().
				contentType(JSON).
			when().
				post("content").
			then().
				log().all().
				spec(get200ResponseSpec()).
			extract().
				response();
			
			// Get node_id
			JsonPath jP = R.jsonPath();
			String nodeId = jP.get("result.node_id");
			System.out.println(nodeId);
			
			// Publish created content
			setURI();
			Response R1 = 
			given().
				spec(getRequestSpec(contentType, validuserId)).
			when().
				get("content/publish/"+nodeId).
			then().
				log().all().
				spec(get200ResponseSpec()).
			extract().
				response();
			
			JsonPath jP1 = R1.jsonPath();
			String contentURL = jP1.get("result.content_url");
		
			// Get Content
			setURI();
			Response R2 =
			given().
				spec(getRequestSpec(contentType, validuserId)).
			when().
				get("content/"+nodeId).
			then().
				log().all().
				spec(get200ResponseSpec()).
			extract().
				response();
			
			JsonPath jP2 = R2.jsonPath();
			String downloadURL = jP2.get("result.content.downloadUrl");
			Assert.assertEquals(contentURL, downloadURL);
			}
		
		// Create and Publish content APK
		
		//Create Content
		@Test
		public void createAndPublishValidContentAPKExpectSuccess200() 
		{
			setURI();
			JSONObject js = new JSONObject(jsonCreateValidContentEcml);
			js.getJSONObject("request").getJSONObject("content").getJSONObject("metadata").put("mimeType", "application/vnd.android.package-archive");
			String jsonCreateValidContentapk = js.toString();
			Response R = 
			given().
				spec(getRequestSpec(contentType, validuserId)).
				body(jsonCreateValidContentapk).
			with().
				contentType(JSON).
			when().
				post("content").
			then().
				log().all().
				spec(get200ResponseSpec()).
			extract().
				response(); 
			
			// Get node_id
			JsonPath jP = R.jsonPath();
			String nodeId = jP.get("result.node_id");
			System.out.println(nodeId);
			
			// Publish created content
			setURI();
			Response R1 = 
			given().
				spec(getRequestSpec(contentType, validuserId)).
			when().
				get("content/publish/"+nodeId).
			then().
				log().all().
				spec(get200ResponseSpec()).
			extract().
				response();
			
			JsonPath jP1 = R1.jsonPath();
			String contentURL = jP1.get("result.content_url");
		
			// Get Content
			setURI();
			Response R2 =
			given().
				spec(getRequestSpec(contentType, validuserId)).
			when().
				get("content/"+nodeId).
			then().
				log().all().
				spec(get200ResponseSpec()).
			extract().
				response();
			
			JsonPath jP2 = R2.jsonPath();
			String downloadURL = jP2.get("result.content.downloadUrl");
			Assert.assertEquals(contentURL, downloadURL);
			}
		
		// Create and Publish content Collections
		
		//Create Content
		@Test
		public void createAndPublishValidContentCollectionsExpectSuccess200() 
		{
			setURI();
			JSONObject js = new JSONObject(jsonCreateValidContentEcml);
			js.getJSONObject("request").getJSONObject("content").put("mimeType", "application/vnd.ekstep.content-collection");
			String jsonCreateValidContentCollection = js.toString();
			Response R = 
			given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidContentCollection).
			with().
				contentType(JSON).
			when().
				post("content").
			then().
				log().all().
				spec(get200ResponseSpec()).
			extract().
				response();
			
			// Get node_id
			JsonPath jP = R.jsonPath();
			String nodeId = jP.get("result.node_id");
			System.out.println(nodeId);
			
			// Publish created content
			setURI();
			Response R1 = 
			given().
				spec(getRequestSpec(contentType, validuserId)).
			when().
				get("content/publish/"+nodeId).
			then().
				log().all().
				spec(get200ResponseSpec()).
			extract().
				response();
			
			JsonPath jP1 = R1.jsonPath();
			String contentURL = jP1.get("result.content_url");
		
			// Get Content
			setURI();
			Response R2 =
			given().
				spec(getRequestSpec(contentType, validuserId)).
			when().
				get("content/"+nodeId).
			then().
				log().all().
				spec(get200ResponseSpec()).
			extract().
				response();
			
			JsonPath jP2 = R2.jsonPath();
			String downloadURL = jP2.get("result.content.downloadUrl");
			Assert.assertEquals(contentURL, downloadURL);
			}
		
		// Create and Publish content Assets
		
		//Create Content
		@Test
		public void createAndPublishValidContentExpectSuccess200() 
		{
			setURI();
			JSONObject js = new JSONObject(jsonCreateValidContentEcml);
			js.getJSONObject("request").getJSONObject("content").put("mimeType", "");
			String jsonCreateValidContentAssets = js.toString();
			Response R = 
			given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidContentAssets).
			with().
				contentType(JSON).
			when().
				post("content").
			then().
				log().all().
				spec(get200ResponseSpec()).
			extract().
				response();
			
			// Get node_id
			JsonPath jP = R.jsonPath();
			String nodeId = jP.get("result.node_id");
			System.out.println(nodeId);
			
			// Publish created content
			setURI();
			Response R1 = 
			given().
				spec(getRequestSpec(contentType, validuserId)).
			when().
				get("content/publish/"+nodeId).
			then().
				log().all().
				spec(get200ResponseSpec()).
			extract().
				response();
			
			JsonPath jP1 = R1.jsonPath();
			String contentURL = jP1.get("result.content_url");
		
			// Get Content
			setURI();
			Response R2 =
			given().
				spec(getRequestSpec(contentType, validuserId)).
			when().
				get("content/"+nodeId).
			then().
				log().all().
				spec(get200ResponseSpec()).
			extract().
				response();
			
			JsonPath jP2 = R2.jsonPath();
			String downloadURL = jP2.get("result.content.downloadUrl");
			Assert.assertEquals(contentURL, downloadURL);
			}
	
	// Invalid content id
	
	// Invalid content Id(Valid Assessment Id)
		
		@Test
		public void publishInvalidContentExpect500() 
		{
			setURI();
			given().
			spec(getRequestSpec(contentType, validuserId)).
			when().
				get("content/publish/ek.n.q_QAFTB_88").
			then().
				log().all().
				spec(get500ResponseSpec());
		}


	@Test
	public void publishInvalidContentIdExpect404() 
	{
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/publish/"+invalidContentId).
		then().
			log().all().
			spec(get404ResponseSpec());
	}
	
	// Invalid content Id(Valid Assessment Id)
	
	
	
	// Publish Invalid Path
	
	@Test
	public void publishInvalidPathExpect500() 
	{
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
			get("domain/publish/testqa18").
		then().
			log().all().
			spec(get500ResponseSpec());
	}
		
	//Create and Extract Content
	
	// Create Content
	@Test
	public void createAndExtractValidContentExpectSuccess200() 
	{
		setURI();
		Response R = 
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(jsonCreateValidExtractContent).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Get Node Id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		
		// Extract created content
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/extract/"+nodeId).
		then().
			log().all().
			spec(get200ResponseSpec());
		
		// Get the updated node and check for zip file
		setURI();
		Response R2 =
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/"+nodeId).
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();

		// Check for zip file
		JsonPath jP2 = R2.jsonPath();
		String downloadUrl = jP2.get("result.content.downloadUrl");
		Assert.assertThat(downloadUrl, CoreMatchers.endsWith(".zip"));
	}
	
	// Create, Extract and Publish Content
	
	//Create Content
	@Test
	public void createExtractAndPublishValidContentExpectSuccess200() 
	{
		setURI();
		Response R = 
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(jsonCreateValidExtractContent).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Get Node Id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		
		// Extract created content
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/extract/"+nodeId).
		then().
			log().all().	
			spec(get200ResponseSpec());
		
		// Publish the extracted content and getting the Content Url
		setURI();
		Response R2 = 
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/publish/"+nodeId).
		then().
			log().all().	
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Get the content URL and check for Ecar file
		JsonPath jP2 = R2.jsonPath();
		String content_url = jP2.get("result.content_url");
		Assert.assertThat(content_url, CoreMatchers.endsWith(".ecar"));
	}
	
	//Extract published content
	
	//Create Content
	@Test
	public void extractPublishedContentExpect500() 
	{
		setURI();
		Response R = 
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(jsonCreateValidExtractContent).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Get node_id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		System.out.println(nodeId);
		
		// Publish created content
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/publish/"+nodeId).
		then().
			log().all();
		
		//Extract Published Content
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/extract/"+nodeId).
		then().
			log().all().
			spec(get500ResponseSpec());
	}
	
	//Extract invalid content (Invalid content id)
	
	@Test
	public void extractInvalidContentIdExpect404() 
	{		
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/extract/"+invalidContentId).
		then().
			log().all().
			spec(get404ResponseSpec());
	}
	
	//Extract invalid content (Valid assessment id)
	
	@Test
	public void extractInvalidContentExpect500() 
	{
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
			get("content/extract/ek.n.q_QAFTB_88").
		then().
			log().all().
			spec(get500ResponseSpec());
	}
	
	// Extract Invalid Path
	
	@Test
	public void extractInvalidPathExpect500() 
	{
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
			get("domain/extract/testqa18").
		then().
			log().all().
			spec(get500ResponseSpec());
	}
	
	// Upload Valid Content
	
	@Test
	public void uploadValidContentExpectSuccess200() {
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidExtractContent);
		js.getJSONObject("request").getJSONObject("content").put("identifier", "Test_QAT_"+rn+"").put("name", "Test_QAT-"+rn+"");
		String jsonCreateAndUploadContent = js.toString();
		Response R = 
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(jsonCreateAndUploadContent).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Get node_id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		System.out.println(nodeId);
		
		// Upload content
		
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File("/Users/purnima/Vignesh Files/Stories/the_moon_and_the_cap.zip")).
		when().
			post("content/upload/"+nodeId).
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	// Create content and Upload invalid file
	
	@Test
	public void uploadInvalidContentExpect4xx() {
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidExtractContent);
		js.getJSONObject("request").getJSONObject("content").put("identifier", "Test_QAT_"+rn+"").put("name", "Test_QAT-"+rn+"");
		String jsonCreateAndUploadContent = js.toString();
		Response R = 
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(jsonCreateAndUploadContent).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Get node_id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		System.out.println(nodeId);
		
		// Upload content
		
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File("/Users/purnima/Vignesh Files/Stories/Selection_001.png")).
		when().
			post("content/upload/"+nodeId).
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	//Create invalid content and upload valid file
	
	@Test
	public void createInvalidContentUploadExpect4xx() {
	// Upload content			
	setURI();
	given().
	spec(getRequestSpec(uploadContentType, validuserId)).
	multiPart(new File("/Users/purnima/Vignesh Files/Stories/the_moon_and_the_cap.zip")).
	when().
		post("content/upload/"+invalidContentId).
	then().
		log().all().
		spec(get200ResponseSpec());

	}
	
	// Create and bundle content
	
	// Create content
	@Test
	public void createAndBundleContentExpectSuccess200() 
	{
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidExtractContent);
		js.getJSONObject("request").getJSONObject("content").put("identifier", "Test_QAT_"+rn+"").put("name", "Test_QAT-"+rn+"");
		String jsonCreateAndBundleContent = js.toString();
		Response R = 
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(jsonCreateAndBundleContent).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Get node_id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		System.out.println(nodeId);
		
		// Upload content
		
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File("/Users/purnima/Vignesh Files/Stories/the_moon_and_the_cap.zip")).
		when().
			post("content/upload/"+nodeId).
		then().
			log().all().
			spec(get200ResponseSpec());
		
		// Bundle created content
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body("{\"request\": {\"content_identifiers\": [\""+nodeId+"\"],\"file_name\": \"Testqa_bundle\"}}").
		when().
			post("content/bundle").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	
	// Create and bundle invalid content
	
	@Test
	public void createAndBundleRetiredContentExpect400() 
	{
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidExtractContent);
		js.getJSONObject("request").getJSONObject("content").put("identifier", "Test_QAT_"+rn+"").put("name", "Test_QAT-"+rn+"");
		String jsonCreateAndBundleContent = js.toString();
		Response R = 
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(jsonCreateAndBundleContent).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Get node_id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		System.out.println(nodeId);
		
		// Upload content
		
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File("/Users/purnima/Vignesh Files/Stories/the_moon_and_the_cap.zip")).
		when().
			post("content/upload/"+nodeId).
		then().
			log().all().
			spec(get200ResponseSpec());

		// Update Status
		
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body("{\"request\": {\"content\": {\"status\": \"Retired\"}}}").
		with().
			contentType(JSON).
		when().
			patch("content/"+nodeId).
		then().
			log().all().
			spec(get200ResponseSpec());
		
		// Bundle created content
		
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body("{\"request\": {\"content_identifiers\": [\""+nodeId+"\"],\"file_name\": \"Testqa_bundle\"}}").
		when().
			post("content/bundle").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	//Create and Bundle multiple contents
	
	// Create content
	@Test
	public void createAndBundleMultipleContentsExpectsSuccess200()
	{
		StringBuilder nodeIds = new StringBuilder();
		int count =1;
		while(count<=3){
			setURI();
			int rn = generateRandomInt(500, 999);
			JSONObject js = new JSONObject(jsonCreateValidExtractContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "Test_QAT_"+rn+"").put("name", "Test_QAT-"+rn+"");
			String jsonCreateAndBundleContent = js.toString();
			Response R = 
			given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateAndBundleContent).
			with().
				contentType(JSON).
			when().
				post("content").
			then().
				log().all().
				spec(get200ResponseSpec()).
			extract().
				response();
		
			//Getting the node ID
			JsonPath jP = R.jsonPath();
			String nodeId = jP.get("result.node_id");
			System.out.println(nodeId);
			nodeIds.append('"'+nodeId+'"');
			System.out.println(nodeIds);
			count++;
		}
		
		// Bundle created content
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body("{\"request\": {\"content_identifiers\": [\""+nodeIds+"\"],\"file_name\": \"Testqa_bundle\"}}").
		when().
			post("content/bundle").
		then().
			log().all().
			spec(get200ResponseSpec());
		}
	
	// Create and bundle content with relationship
	
	// Create content
	@Test
	public void createAndBundleContentWithRelationExpectSuccess200() 
	{
		setURI();
			/*JSONObject js = new JSONObject(jsonCreateValidExtractContent);
		js.getJSONObject("request").getJSONObject("content").put("identifier", "Test_QAT_"+rn+"").put("name", "Test_QAT-"+rn+"");
		String jsonCreateAndBundleContentWithRelation = js.toString();*/
		Response R = 
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(jsonCreateValidExtractContentWithRelation).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Get node_id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");
		System.out.println(nodeId);
		
		// Bundle created content
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body("{\"request\": {\"content_identifiers\": [\""+nodeId+"\"],\"file_name\": \"Testqa_bundle\"}}").
		when().
			post("content/bundle").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	// Bundle Invalid and Valid Content Ids 
	
	// Create content
	@Test
	public void bundleValidandInvalidContentExpect404() 
	{
		setURI();
		JSONObject js = new JSONObject(jsonCreateValidExtractContent);
		js.getJSONObject("request").getJSONObject("content").put("identifier", "Test_QAT_"+rn+"").put("name", "Test_QAT-"+rn+"");
		String jsonCreateAndBundleContent = js.toString();
		Response R = 
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(jsonCreateAndBundleContent).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Get node_id
		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id	");
		System.out.println(nodeId);
		
		// Bundle created content and invalid content
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body("{\"request\": {\"content_identifiers\": [\""+nodeId+", abcdaf124, Test_Invalid\"],\"file_name\": \"Testqa_bundle\"}}").
		when().
			post("content/bundle").
		then().
			log().all().
			spec(get404ResponseSpec());
		}
}	

