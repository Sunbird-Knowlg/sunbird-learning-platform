package org.ekstep.platform.content;

import org.ekstep.lp.domain.BaseTest;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;
import java.util.*;

import org.junit.Test;


public class ContentAPITests extends BaseTest {
	
	String jsonGetContentList = "{\"request\": { \"search\": {\"contentType\": \"Story\"}}}";
	String jsonGetContentListEmptySearch = "{\"request\": { \"search\": {}}}";
	
	String jsonCreateContentValid = "{\"request\": {\"content\": {\"body\": \"<theme></theme>\",\"status\": \"Draft\",\"description\": \"QA\",\"subject\": \"literacy\",\"name\": \"QA\",\"owner\": \"EkStep\",\"s3Key\": \"content/build_a_sentence_1446706423188.zip\",\"downloadUrl\": \"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/build_a_sentence_1446706423188.zip\",\"code\": \"org.ekstep.num.build.sentence\",\"contentType\": \"Worksheet\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"pkgVersion\": 2}}}";
	String jsonCreateContentWithRelations = "{\"request\": {\"content\": {\"identifier\": \"content_collection_8\",\"contentType\": \"Story\",\"body\": \"<theme></theme>\",\"status\": \"Live\",\"description\": \"English Stories Collection\", \"subject\": \"literacy\",\"name\": \"English Stories Collection\", \"owner\": \"EkStep\",\"code\": \"content_collection_7\", \"language\": \"English\", \"mimeType\": \"application/vnd.ekstep.content-collection\",\"tags\": [\"English\"],\"children\": [{ \"identifier\": \"org.ekstep.story.en.vayu\", \"index\": 0},{\"identifier\": \"org.ekstep.story.en.tree\", \"index\": 1} ],\"concepts\": [{ \"identifier\": \"LO52\"}, { \"identifier\": \"LO51\"}]}}}";
	
	String jsonUpdateContentValid = "{\"request\": {\"content\": {\"contentType\": \"Story\",\"body\": \"<theme></theme>\",\"status\": \"Live\",\"description\": \"English Stories Collection\",\"subject\": \"literacy\",\"name\": \"English Stories Collection\",\"owner\": \"EkStep\",\"code\": \"content_collection_7\",\"language\": \"English\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"tags\": [\"English\"],\"children\": [{\"identifier\": \"org.ekstep.story.en.mooncap\", \"index\": 0},{\"identifier\": \"org.ekstep.story.en.vayu\", \"index\": 1}, {\"identifier\": \"org.ekstep.story.en.tree\", \"index\": 2} ],\"concepts\": [{ \"identifier\": \"LO52\"}, { \"identifier\": \"LO51\"}]}}}";
	String jsonCreateAndPublishContentValid = "{\"request\": {\"content\": {\"body\": \"<theme></theme>\",\"status\": \"Live\",\"description\": \"QA\",\"subject\": \"literacy\",\"name\": \"QA\",\"owner\": \"EkStep\",\"s3Key\": \"content/build_a_sentence_1446706423188.zip\",\"downloadUrl\": \"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/build_a_sentence_1446706423188.zip\",\"code\": \"org.qa.publish.content\",\"identifier\":\"org.qa.publish.content\",\"contentType\": \"Worksheet\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"pkgVersion\": 2}}}";
	
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
				patch("content/list").
			then().
				log().all().
				spec(get200ResponseSpec());
			
	}
	
	
	//Get Content List
	@Test
	public void getContentListEmptySearchExpect400()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonGetContentList).
			with().
				contentType("application/json").
			when().
				patch("content/list").
			then().
				log().all().
				spec(get400ResponseSpec());
			
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
				patch("content/search").
			then().
				log().all().
				spec(get200ResponseSpec());
			
	}
	
	@Test
	public void searchContentListEmptySearchExpect400()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonGetContentList).
			with().
				contentType("application/json").
			when().
				patch("content/search").
			then().
				log().all().
				spec(get400ResponseSpec());
			
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
			spec(get200ResponseSpec());
	        
	}
	
	//Create Content
	@Test
	public void createContentValidExpectSuccess200(){
		
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateContentValid).
			with().
				contentType(JSON).
		when().
			post("content").
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
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonUpdateContentValid).
			with().
				contentType("application/json").
			when().
				patch("content/content_collection_8").
			then().
				log().all().
				spec(get200ResponseSpec());
	}
	
	@Test
	public void updateContentNotExistingExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonUpdateContentValid).
			with().
				contentType("application/json").
			when().
				patch("content/not_existing").
			then().
				log().all().
				spec(get400ResponseSpec());
	}
	
	//Upload Content
	
	//Retire Content 
	
	//Publish Content
	@Test
	public void createAndpublishContentExpect200(){		
		
		//Create Content
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateAndPublishContentValid).
			with().
				contentType(JSON).
		when().
			post("content").
		then().
			log().all().
			spec(get200ResponseSpec());
				
		//Publish Content
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(jsonCreateAndPublishContentValid).
		with().
			contentType(JSON).
		when().
			post("content/org.qa.publish.content/publish").
		then().
			log().all().
			spec(get200ResponseSpec());		
	}
	
	
	
	
	
	
	
}
