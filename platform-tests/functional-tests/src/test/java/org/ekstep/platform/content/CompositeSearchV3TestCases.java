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


public class CompositeSearchV3TestCases extends BaseTest {
	
	int rn = generateRandomInt(2000, 2500000);

	String jsonSimpleSearchQuery = "{\"request\": {\"filters\":{},\"query\": \"add\",\"limit\": 10}}";
	String jsonFilteredCompositeSearch = "{\"request\": {\"filters\": {\"objectType\": [\"content\", \"concept\"], \"identifier\":[\"identifierNew\"]},\"limit\": 10}}"; 
	String jsonSearchWithFilter = "{\"request\":{\"filters\":{\"objectType\":[\"Concept\",\"Word\",\"Domain\",\"Dimension\",\"AssessmentItem\",\"Content\",\"Method\"]}}}";
	String jsonSearchQueryAndFilter = "{\"request\":{\"query\":\"lion\",\"filters\":{\"objectType\":[\"Concept\",\"Word\",\"Domain\",\"Dimension\",\"AssessmentItem\",\"Content\",\"Method\"]}}}";
	String jsonSearchLogicalReq = "{\"request\":{\"filters\":{\"objectType\":[\"Word\"],\"graph_id\":[\"hi\"],\"orthographic_complexity\":{\"<=\":2,\">\":0.5},\"syllableCount\":{\">\":2,\"<=\":5}}}}";
	String jsonSearchWithStartsEnds = "{\"request\":{\"filters\":{\"objectType\":[\"Word\"],\"graph_id\":[\"en\"],\"lemma\":{\"startsWith\":\"e\",\"endsWith\":\"t\"}}}}";
	String jsonSearchWithEquals = "{\"request\":{\"filters\":{\"lemma\":\"eat\"}}}";
	String jsonSearchWithFacets = "{\"request\":{\"filters\":{\"objectType\":[\"Concept\",\"Word\",\"Domain\",\"Dimension\",\"AssessmentItem\",\"Content\",\"Method\"]},\"facets\":[\"contentType\",\"domain\",\"ageGroup\",\"language\",\"gradeLevel\"]}}";
	String jsonSearchWithSortByAsc = "{\"request\":{\"filters\":{\"objectType\":[\"Concept\",\"Word\",\"Domain\",\"Dimension\",\"AssessmentItem\",\"Content\",\"Method\"]},\"facets\":[\"contentType\",\"domain\",\"ageGroup\",\"language\",\"gradeLevel\"],\"sort_by\":{\"name\":\"asc\"}}}";
	String jsonSearchWithSortByDsc = "{\"request\":{\"filters\":{\"objectType\":[\"Concept\",\"Word\",\"Domain\",\"Dimension\",\"AssessmentItem\",\"Content\",\"Method\"]},\"facets\":[\"contentType\",\"domain\",\"ageGroup\",\"language\",\"gradeLevel\"],\"sort_by\":{\"name\":\"desc\"}}}";
	
	String jsonCreateValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_"+rn+"\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_NFT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"pkgVersion\": 3,\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
	String invalidContentId = "TestQa_"+rn+"";
	String jsonCreateValidWord = "{\"request\":{\"words\":[{\"lemma\":\"देख_ी_"+rn+"\",\"sampleUsages\":[\"महिला एक बाघ को देखीै\"],\"pronunciations\":[\"https://s3-ap-southeast-1.amazonaws.com/ekstep-public/language_assets/dekhi.mp3\"],\"pictures\":[\"https://s3-ap-southeast-1.amazonaws.com/ekstep-public/language_assets/seeing.png\"],\"phonologic_complexity\":13.25,\"orthographic_complexity\":0.7}]}}";
	String jsonContentClean = "{\"request\": {\"searchProperty\": \"name\",\"searchOperator\": \"startsWith\",\"searchString\": \"LP_NFT_\"}}";
	
	static ClassLoader classLoader = ContentPublishWorkflowTests.class.getClassLoader();
	static File path = new File(classLoader.getResource("UploadFiles/").getFile());

	// Create and search content
	@Test
	public void createAndSearchExpectSuccess200() throws InterruptedException{
		//contentCleanUp();
		setURI();
		Response R =
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("/content/v3/create").
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
		spec(getRequestSpecification(uploadContentType, userId, APIToken)).
		multiPart(new File(path+"/uploadContent.zip")).
		when().
		post("/content/v3/upload/"+ecmlNode).
		then().
		//log().all().
		spec(get200ResponseSpec());

		
		// Publish created content
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
		when().
		post("/content/v3/publish/"+ecmlNode).
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		// Searching with query
		try{Thread.sleep(10000);}catch(InterruptedException e){System.out.println(e);} 
		setURI();
		String jsonSimpleQuery = jsonFilteredCompositeSearch.replace("identifierNew", ecmlNode);
		System.out.println(jsonSimpleQuery);
		Response R2 =
		given().
	 		spec(getRequestSpecification(contentType, userId, APIToken)).
	 		body(jsonSimpleQuery).
		with().
		 	contentType(JSON).
		when().
		 	post("/composite/v3/search").
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
	 		spec(getRequestSpecification(contentType, userId, APIToken)).
	 		body(jsonSimpleQuery).
		with().
		 	contentType(JSON).
		when().
		 	post("/composite/v3/search").
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
	 		spec(getRequestSpecification(contentType, userId, APIToken)).
	 		body(jsonBlankSearch).
		with().
		 	contentType(JSON).
		when().
		 	post("/composite/v3/search").
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
	 		spec(getRequestSpecification(contentType, userId, APIToken)).
	 		body(jsonBlankSearch).
		with().
		 	contentType(JSON).
		when().
		 	post("/composite/v3/search"). 
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
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body(jsonCreateValidContent).
		with().
			contentType(JSON).
		when().
			post("/content/v3/create").
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
	 		spec(getRequestSpecification(contentType, userId, APIToken)).
	 		body(jsonSimpleQuery).
		with().
		 	contentType(JSON).
		when().
		 	post("/composite/v3/search").
		then().
		 	//log().all().
		 	spec(get200ResponseSpec());
		
		// Space after query
		setURI();
		JSONObject js1 = new JSONObject(jsonFilteredCompositeSearch);
		js.getJSONObject("request").put("query", ecmlNode+"  ");
		String jsonSpaceAfterQuery = js1.toString();
		given().
	 		spec(getRequestSpecification(contentType, userId, APIToken)).
	 		body(jsonSpaceAfterQuery).
		with().
		 	contentType(JSON).
		when().
		 	post("/composite/v3/search").
		then().
		 	//log().all().
		 	spec(get200ResponseSpec());		
}
	
	// Simple composite search request
	
	@Test
	public void simpleCompositeSearchExpectSuccess200() {
		setURI();
		given().
	 		spec(getRequestSpecification(contentType, userId, APIToken)).
	 		body(jsonSimpleSearchQuery).
		with().
		 	contentType(JSON).
		when().
		 	post("/composite/v3/search").
		then().
		 	//log().all().
		 	spec(get200ResponseSpec());
		}
	
	// Search with filters

	@Test
	public void searchWithFiltersExpectSuccess200(){
		setURI();
		given().
	 		spec(getRequestSpecification(contentType, userId, APIToken)).
	 		body(jsonSearchWithFilter).
		with().
		 	contentType(JSON).
		when().
		 	post("/composite/v3/search").
		then().
		 	//log().all().
		 	spec(get200ResponseSpec());
	}
	
	// Search with query and filters
	
	@Test
	public void searchWithQueryAndFiltersExpectSuccess200(){
		setURI();
		given().
	 		spec(getRequestSpecification(contentType, userId, APIToken)).
 		body(jsonSearchQueryAndFilter).
		with().
		 	contentType(JSON).
		when().
		 	post("/composite/v3/search").
		then().
		 	//log().all().
		 	spec(get200ResponseSpec());
	}
	
	// Search with Logical Request
	
	@Test
	public void searchWithLogicalRequestExpectSuccess200(){
	setURI();
	given().
 		spec(getRequestSpecification(contentType, userId, APIToken)).
 		body(jsonSearchLogicalReq).
	with().
	 	contentType(JSON).
	when().
	 	post("/composite/v3/search").
	then().
	 	//log().all().
	 	spec(get200ResponseSpec());
	}
	
	// Search with starts and ends with
	
	@Test
	public void searchWithStartsAndEndsExpectSuccess200(){
		setURI();
		given().
	 		spec(getRequestSpecification(contentType, userId, APIToken)).
	 		body(jsonSearchWithStartsEnds).
		with().
		 	contentType(JSON).
		when().
		 	post("/composite/v3/search").
		then().
		 	//log().all().
		 	spec(get200ResponseSpec());
	}
	
	// Search with Equals query
	
	public void searchWithEqualsExpectSuccess200(){
		setURI();
		given().
	 		spec(getRequestSpecification(contentType, userId, APIToken)).
	 		body(jsonSearchWithEquals).
		with().
		 	contentType(JSON).
		when().
		 	post("/composite/v3/search").
		then().
		 	//log().all().
		 	spec(get200ResponseSpec());
	}
	
	// Search with Facets
	
	@Test
	public void searchWithFacetsExpectSuccess200(){
		setURI();
		given().
	 		spec(getRequestSpecification(contentType, userId, APIToken)).
	 		body(jsonSearchWithFacets).
		with().
		 	contentType(JSON).
		when().
		 	post("/composite/v3/search").
		then().
		 	//log().all().
		 	spec(get200ResponseSpec());
	}
	
	// Search with Sortby Ascending
	
	@Test
	public void searchWithSortAscExpectSuccess200(){
		setURI();
		given().
	 		spec(getRequestSpecification(contentType, userId, APIToken)).
	 		body(jsonSearchWithSortByAsc).
		with().
		 	contentType(JSON).
		when().
		 	post("/composite/v3/search").
		then().
		 	//log().all().
		 	spec(get200ResponseSpec());
	}
	
	// Search with sort by descending
	
	@Test
	public void searchWithSortDescExpectSuccess200(){
		setURI();
		given().
	 		spec(getRequestSpecification(contentType, userId, APIToken)).
	 		body(jsonSearchWithSortByDsc).
		with().
		 	contentType(JSON).
		when().
		 	post("/composite/v3/search").
		then().
		 	//log().all().
		 	spec(get200ResponseSpec());
	}
	
	// Composite search request with filters
	
	@Test
	public void filteredCompositeSearchExpectSuccess200() {
		setURI();
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body(jsonFilteredCompositeSearch).
		with().	
			contentType(JSON).
		when().
			post("/composite/v3/search").
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
