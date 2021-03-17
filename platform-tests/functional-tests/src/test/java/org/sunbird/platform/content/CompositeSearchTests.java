package org.sunbird.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.io.File;
import java.util.ArrayList;

import org.sunbird.platform.domain.BaseTest;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;


public class CompositeSearchTests extends BaseTest {
	
	int rn = generateRandomInt(2000, 2500);

	String jsonSimpleSearchQuery = "{\"request\": {\"filters\":{},\"query\": \"add\",\"limit\": 10}}";
	String jsonFilteredCompositeSearch = "{\"request\": {\"query\": \"add\",\"filters\": {\"objectType\": [\"content\", \"concept\"]},\"limit\": 10}}"; 
	String jsonSearchWithFilter = "{\"request\":{\"filters\":{\"objectType\":[\"Concept\",\"Word\",\"Domain\",\"Dimension\",\"AssessmentItem\",\"Content\",\"Method\"]}}}";
	String jsonSearchQueryAndFilter = "{\"request\":{\"query\":\"lion\",\"filters\":{\"objectType\":[\"Concept\",\"Word\",\"Domain\",\"Dimension\",\"AssessmentItem\",\"Content\",\"Method\"]}}}";
	String jsonSearchLogicalReq = "{\"request\":{\"filters\":{\"objectType\":[\"Word\"],\"graph_id\":[\"hi\"],\"orthographic_complexity\":{\"<=\":2,\">\":0.5},\"syllableCount\":{\">\":2,\"<=\":5}}}}";
	String jsonSearchWithStartsEnds = "{\"request\":{\"filters\":{\"objectType\":[\"Word\"],\"graph_id\":[\"en\"],\"lemma\":{\"startsWith\":\"e\",\"endsWith\":\"t\"}}}}";
	String jsonSearchWithEquals = "{\"request\":{\"filters\":{\"lemma\":\"eat\"}}}";
	String jsonSearchWithFacets = "{\"request\":{\"filters\":{\"objectType\":[\"Concept\",\"Word\",\"Domain\",\"Dimension\",\"AssessmentItem\",\"Content\",\"Method\"]},\"facets\":[\"contentType\",\"domain\",\"ageGroup\",\"language\",\"gradeLevel\"]}}";
	String jsonSearchWithSortByAsc = "{\"request\":{\"filters\":{\"objectType\":[\"Concept\",\"Word\",\"Domain\",\"Dimension\",\"AssessmentItem\",\"Content\",\"Method\"]},\"facets\":[\"contentType\",\"domain\",\"ageGroup\",\"language\",\"gradeLevel\"],\"sort_by\":{\"name\":\"asc\"}}}";
	String jsonSearchWithSortByDsc = "{\"request\":{\"filters\":{\"objectType\":[\"Concept\",\"Word\",\"Domain\",\"Dimension\",\"AssessmentItem\",\"Content\",\"Method\"]},\"facets\":[\"contentType\",\"domain\",\"ageGroup\",\"language\",\"gradeLevel\"],\"sort_by\":{\"name\":\"desc\"}}}";
	
	String jsonCreateValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_"+rn+"\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"pkgVersion\": 3,\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
	String invalidContentId = "TestQa_"+rn+"";
	String jsonCreateValidWord = "{\"request\":{\"words\":[{\"lemma\":\"देख_ी_"+rn+"\",\"sampleUsages\":[\"महिला एक बाघ को देखीै\"],\"pronunciations\":[\"https://s3-ap-southeast-1.amazonaws.com/ekstep-public/language_assets/dekhi.mp3\"],\"pictures\":[\"https://s3-ap-southeast-1.amazonaws.com/ekstep-public/language_assets/seeing.png\"],\"phonologic_complexity\":13.25,\"orthographic_complexity\":0.7}]}}";
	String jsonContentClean = "{\"request\": {\"searchProperty\": \"name\",\"searchOperator\": \"startsWith\",\"searchString\": \"LP_FT_\"}}";
	
	static ClassLoader classLoader = ContentPublishWorkflowTests.class.getClassLoader();
	static File path = new File(classLoader.getResource("UploadFiles/").getFile());

	// Create and search content
	@Ignore
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
		js.getJSONObject("request").put("query", "");
		String jsonBlankSearch = js.toString();
		//System.out.println(jsonBlankSearch);
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
	
	// Search with filters

	@Test
	public void searchWithFiltersExpectSuccess200(){
		setURI();
		given().
	 		spec(getRequestSpec(contentType, validuserId)).
	 		body(jsonSearchWithFilter).
		with().
		 	contentType(JSON).
		when().
		 	post("search/v2/search").
		then().
		 	//log().all().
		 	spec(get200ResponseSpec());
	}
	
	// Search with query and filters
	
	@Test
	public void searchWithQueryAndFiltersExpectSuccess200(){
		setURI();
		given().
	 		spec(getRequestSpec(contentType, validuserId)).
 		body(jsonSearchQueryAndFilter).
		with().
		 	contentType(JSON).
		when().
		 	post("search/v2/search").
		then().
		 	//log().all().
		 	spec(get200ResponseSpec());
	}
	
	// Search with Logical Request
	
	@Test
	public void searchWithLogicalRequestExpectSuccess200(){
	setURI();
	given().
 		spec(getRequestSpec(contentType, validuserId)).
 		body(jsonSearchLogicalReq).
	with().
	 	contentType(JSON).
	when().
	 	post("search/v2/search").
	then().
	 	//log().all().
	 	spec(get200ResponseSpec());
	}
	
	// Search with starts and ends with
	
	@Test
	public void searchWithStartsAndEndsExpectSuccess200(){
		setURI();
		given().
	 		spec(getRequestSpec(contentType, validuserId)).
	 		body(jsonSearchWithStartsEnds).
		with().
		 	contentType(JSON).
		when().
		 	post("search/v2/search").
		then().
		 	//log().all().
		 	spec(get200ResponseSpec());
	}
	
	// Search with Equals query
	
	public void searchWithEqualsExpectSuccess200(){
		setURI();
		given().
	 		spec(getRequestSpec(contentType, validuserId)).
	 		body(jsonSearchWithEquals).
		with().
		 	contentType(JSON).
		when().
		 	post("search/v2/search").
		then().
		 	//log().all().
		 	spec(get200ResponseSpec());
	}
	
	// Search with Facets
	
	@Test
	public void searchWithFacetsExpectSuccess200(){
		setURI();
		given().
	 		spec(getRequestSpec(contentType, validuserId)).
	 		body(jsonSearchWithFacets).
		with().
		 	contentType(JSON).
		when().
		 	post("search/v2/search").
		then().
		 	//log().all().
		 	spec(get200ResponseSpec());
	}
	
	// Search with Sortby Ascending
	
	@Test
	public void searchWithSortAscExpectSuccess200(){
		setURI();
		given().
	 		spec(getRequestSpec(contentType, validuserId)).
	 		body(jsonSearchWithSortByAsc).
		with().
		 	contentType(JSON).
		when().
		 	post("search/v2/search").
		then().
		 	//log().all().
		 	spec(get200ResponseSpec());
	}
	
	// Search with sort by descending
	@Ignore
	@Test
	public void searchWithSortDescExpectSuccess200(){
		setURI();
		given().
	 		spec(getRequestSpec(contentType, validuserId)).
	 		body(jsonSearchWithSortByDsc).
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
