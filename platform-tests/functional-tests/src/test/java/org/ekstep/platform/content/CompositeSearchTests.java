package org.ekstep.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.io.File;
import java.util.ArrayList;

import org.ekstep.platform.domain.BaseTest;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;


public class CompositeSearchTests extends BaseTest {
	
	int rn = generateRandomInt(2000, 2500);

	String jsonSimpleSearchQuery = "{\"request\": {\"query\": \"add\",\"limit\": 10}}";
	String jsonFilteredCompositeSearch = "{\"request\": {\"query\": \"add\",\"filters\": {\"objectType\": [\"content\", \"concept\"]},\"limit\": 10}}"; 
	String jsonCreateValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_"+rn+"\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"pkgVersion\": 3,\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
	String invalidContentId = "TestQa_"+rn+"";
	String jsonCreateValidWord = "{\"request\":{\"words\":[{\"lemma\":\"देख_ी_"+rn+"\",\"sampleUsages\":[\"महिला एक बाघ को देखीै\"],\"pronunciations\":[\"https://s3-ap-southeast-1.amazonaws.com/ekstep-public/language_assets/dekhi.mp3\"],\"pictures\":[\"https://s3-ap-southeast-1.amazonaws.com/ekstep-public/language_assets/seeing.png\"],\"phonologic_complexity\":13.25,\"orthographic_complexity\":0.7}]}}";
	String jsonContentClean = "{\"request\": {\"searchProperty\": \"name\",\"searchOperator\": \"startsWith\",\"searchString\": \"LP_FT_\"}}";
	
	static ClassLoader classLoader = ContentPublishWorkflowTests.class.getClassLoader();
	static File path = new File(classLoader.getResource("UploadFiles/").getFile());

	// Create and search content
	@Test
	public void createAndSearchExpectSuccess200() throws InterruptedException{
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
				////log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String ecmlNode = jp.get("result.node_id");	
		
		// Upload Content
		setURI();
		given().
		spec(getRequestSpec(uploadContentType, validuserId)).
		multiPart(new File(path+"/ecml_with_json.zip")).
		when().
		post("/learning/v2/content/upload/"+ecmlNode).
		then().
		////log().all().
		spec(get200ResponseSpec());

		
		// Publish created content
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		when().
		get("/learning/v2/content/publish/"+ecmlNode).
		then().
		////log().all().
		spec(get200ResponseSpec());
		
		// Searching with query
		Thread.sleep(2000);
		setURI();
		JSONObject js = new JSONObject(jsonFilteredCompositeSearch);
		js.getJSONObject("request").put("query", ecmlNode);
		String jsonSimpleQuery = js.toString();
		//system.out.println(jsonSimpleQuery);
		Response R2 =
		given().
	 		spec(getRequestSpec(contentType, validuserId)).
	 		body(jsonSimpleQuery).
		with().
		 	contentType(JSON).
		when().
		 	post("search/v2/search").
		then().
		 	//log().all().
		 	spec(get200ResponseSpec()).
		 extract().
		 	response();
		
		// Extracting the JSON path and validate the result
		JsonPath jp2 = R2.jsonPath();
		ArrayList<String> name = jp2.get("result.content.name");
		Assert.assertTrue(name.contains(ecmlNode));
		}
		
	@Test
	public void searchInvalidContentExpect200(){
		setURI();
		JSONObject js = new JSONObject(jsonSimpleSearchQuery);
		js.getJSONObject("request").put("query", invalidContentId);
		String jsonSimpleQuery = js.toString();
		given(). 
	 		spec(getRequestSpec(contentType, validuserId)).
	 		body(jsonSimpleQuery).
		with().
		 	contentType(JSON).
		when().
		 	post("search/v2/search").
		then().
		 	//log().all().
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
	 		spec(getRequestSpec(contentType, validuserId)).
	 		body(jsonBlankSearch).
		with().
		 	contentType(JSON).
		when().
		 	post("search/v2/search").
		then().
		 	//log().all().
		 	spec(get200ResponseSpec());
		
		}
	
	// Blank space in query
	@Test
	public void blankSpaceSearchExpect200() {
		setURI();
		JSONObject js = new JSONObject(jsonSimpleSearchQuery);
		js.getJSONObject("request").put("query", "   ");
		String jsonBlankSearch = js.toString();
		given().
	 		spec(getRequestSpec(contentType, validuserId)).
	 		body(jsonBlankSearch).
		with().
		 	contentType(JSON).
		when().
		 	post("search/v2/search"). 
		then().
		 	//log().all().
		 	spec(get200ResponseSpec());
		}
		
	// Blank space before and after query
		
	@Test
	public void createAndSearchWithSpaceExpectSuccess200(){
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
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String ecmlNode = jp.get("result.node_id");
		
		// Space before query
		setURI();
		JSONObject js = new JSONObject(jsonFilteredCompositeSearch);
		js.getJSONObject("request").put("query", "  "+ecmlNode);
		String jsonSimpleQuery = js.toString();
		given().
	 		spec(getRequestSpec(contentType, validuserId)).
	 		body(jsonSimpleQuery).
		with().
		 	contentType(JSON).
		when().
		 	post("search/v2/search").
		then().
		 	//log().all().
		 	spec(get200ResponseSpec());
		
		// Space after query
		setURI();
		JSONObject js1 = new JSONObject(jsonFilteredCompositeSearch);
		js.getJSONObject("request").put("query", ecmlNode+"  ");
		String jsonSpaceAfterQuery = js1.toString();
		given().
	 		spec(getRequestSpec(contentType, validuserId)).
	 		body(jsonSpaceAfterQuery).
		with().
		 	contentType(JSON).
		when().
		 	post("search/v2/search").
		then().
		 	//log().all().
		 	spec(get200ResponseSpec());		
}
	
	// Create and search word without filters in query
	
	//Create Word
	@Test
	public void  createNewWordExpectSuccess200() {
		setURI();
		given().
			//log().all().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidWord).
		with().
			contentType(JSON).
		when().
			post("language/v1/language/dictionary/word/hi/").
		then().
			//log().all().
			spec(get200ResponseSpec());
		
		// Extracting Lemma
		String lemma = "देख_ी_"+rn+"";
		
		// Search for the node
		setURI();
		JSONObject js1 = new JSONObject(jsonSimpleSearchQuery);
		js1.getJSONObject("request").put("query", lemma);
		String jsonSimpleQuery = js1.toString();
		given().
	 		spec(getRequestSpec(contentType, validuserId)).
	 		body(jsonSimpleQuery).
		with().
		 	contentType(JSON).
		when().
		 	post("search/v2/search").
		then().
		 	//log().all().
		 	spec(get200ResponseSpec());
}
	
	// Simple composite search request
	
	@Test
	public void simpleCompositeSearchExpectSuccess200() {
		setURI();
		given().
	 		spec(getRequestSpec(contentType, validuserId)).
	 		body(jsonSimpleSearchQuery).
		with().
		 	contentType(JSON).
		when().
		 	post("search/v2/search").
		then().
		 	//log().all().
		 	spec(get200ResponseSpec());
		}
	
	// Composite search request with filters
	
	@Test
	public void filteredCompositeSearchExpectSuccess200() {
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonFilteredCompositeSearch).
		with().	
			contentType(JSON).
		when().
			post("search/v2/search").
		then().
			//log().all().
			spec(get200ResponseSpec());
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


}
