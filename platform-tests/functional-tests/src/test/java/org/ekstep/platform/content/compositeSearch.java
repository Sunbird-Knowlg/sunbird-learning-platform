package org.ekstep.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.util.ArrayList;

import org.ekstep.platform.domain.BaseTest;
import org.json.JSONObject;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;


public class compositeSearch extends BaseTest {
	
	int rn = generateRandomInt(2000, 2500);

	String jsonSimpleSearchQuery = "{\"request\": {\"query\": \"Test\",\"limit\": 10}}";
	String jsonFilteredCompositeSearch = "{\"request\": {\"query\": \"Lion\",\"fields\": [\"name\", \"title\", \"identifier\"],\"filters\": [{\"objectType\": [\"content\", \"concept\"]}],\"limit\": 10}}"; 
	String jsonCreateValidContent = "{\"request\": {\"content\": {\"identifier\": \"Test_QA_"+rn+"\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"Test_QA_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"pkgVersion\": 3,\"owner\": \"EkStep\"}}}";
	String invalidContentId = "TestQa_"+rn+"";
	String jsonCreateValidWord = "{\"request\": {\"words\": [{\"lemma\": \"देखी\",\"phonologic_complexity\": 13.25,\"orthographic_complexity\": 0.7}]}}";
	
	// Create and search content
	@Test
	public void createAndSearchExpectSuccess200(){
		setURI();
		Response R =
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidContent).
		with().
			contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String ecmlNode = jp.get("result.node_id");
		
		setURI();
		JSONObject js = new JSONObject(jsonFilteredCompositeSearch);
		js.getJSONObject("request").put("query", ecmlNode);
		String jsonSimpleQuery = js.toString();
		given().
	 		spec(getRequestSpec(contentType, validuserId)).
	 		body(jsonSimpleQuery).
		with().
		 	contentType(JSON).
		when().
		 	post("search").
		then().
		 	log().all().
		 	spec(get200ResponseSpec());
		}
		
	@Test
	public void searchInvalidContentExpect400(){
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
		 	post("search").
		then().
		 	log().all().
		 	spec(get400ResponseSpec());		
	}
	
	// Blank composite search query
	@Test
	public void blankCompositeSearchExpect400() {
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
		 	post("search").
		then().
		 	log().all().
		 	spec(get400ResponseSpec());
		}
	
	// Blank space in query
	@Test
	public void blankSpaceSearchExpect400() {
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
		 	post("search"). 
		then().
		 	log().all().
		 	spec(get400ResponseSpec());
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
			post("content").
		then().
			log().all().
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
		 	post("search").
		then().
		 	log().all().
		 	spec(get400ResponseSpec());
		
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
		 	post("search").
		then().
		 	log().all().
		 	spec(get400ResponseSpec());		
}
	
	// Create and search word without filters in query
	
	//Create Word
	@Test
	public void  createNewWordExpectSuccess200() {
		setURI();
		given().
			log().all().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateValidWord).
		with().
			contentType(JSON).
		when().
			post("language/dictionary/word/en/").
		then().
			log().all().
			spec(get200ResponseSpec());
		
		// Extracting Lemma
		JSONObject js = new JSONObject(jsonCreateValidWord);
		js.getJSONObject("request").get("lemma");
		String lemma = js.toString();
		
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
		 	post("search").
		then().
		 	log().all().
		 	spec(get200ResponseSpec());
}
	
	// Simple composite search request
	
	@Test
	public void simpleCompositeSearchExpectSuccess200() {
		setURI();
		given().
	 		spec(getRequestSpec(contentType, validuserId)).
	 		//body(jsonSimpleCompositeSearch).
		with().
		 	contentType(JSON).
		when().
		 	post("search").
		then().
		 	log().all().
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
			post("search").
		then().
			log().all().
			spec(get200ResponseSpec());
	}	
}
