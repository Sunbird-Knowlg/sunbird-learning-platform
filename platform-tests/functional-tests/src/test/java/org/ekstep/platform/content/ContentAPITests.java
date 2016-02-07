package org.ekstep.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;
import java.util.*;

import org.ekstep.platform.domain.BaseTest;
import org.junit.Test;


public class ContentAPITests extends BaseTest {
	
	String jsonGetContentList = "{\"request\": { \"search\": {\"contentType\": \"Story\"}}}";
	String jsonGetContentListEmptySearch = "{\"request\": { \"search\": {}}}";
	
	String jsonCreateContentValid = "{\"request\": {\"content\": {\"body\": \"<theme></theme>\",\"status\": \"Draft\",\"description\": \"QA\",\"subject\": \"literacy\",\"name\": \"QA\",\"owner\": \"EkStep\",\"s3Key\": \"content/build_a_sentence_1446706423188.zip\",\"downloadUrl\": \"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/build_a_sentence_1446706423188.zip\",\"code\": \"org.ekstep.num.build.sentence\",\"contentType\": \"Worksheet\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"pkgVersion\": 2}}}";
	String jsonCreateContentWithRelations = "{\"request\": {\"content\": {\"identifier\": \"content_collection_8\",\"contentType\": \"Story\",\"body\": \"<theme></theme>\",\"status\": \"Live\",\"description\": \"English Stories Collection\", \"subject\": \"literacy\",\"name\": \"English Stories Collection\", \"owner\": \"EkStep\",\"code\": \"content_collection_7\", \"language\": \"English\", \"mimeType\": \"application/vnd.ekstep.content-collection\",\"tags\": [\"English\"],\"children\": [{ \"identifier\": \"org.ekstep.story.en.vayu\", \"index\": 0},{\"identifier\": \"org.ekstep.story.en.tree\", \"index\": 1} ],\"concepts\": [{ \"identifier\": \"LO52\"}, { \"identifier\": \"LO51\"}]}}}";
	
	String jsoncreateNonExistingParentContent = "{\"request\": {\"content\": {\"identifier\": \"content_collection_8\",\"contentType\": \"Story\",\"body\": \"<theme></theme>\",\"status\": \"Live\",\"description\": \"English Stories Collection\", \"subject\": \"literacy\",\"name\": \"English Stories Collection\", \"owner\": \"EkStep\",\"code\": \"content_collection_7\", \"language\": \"English\", \"mimeType\": \"application/vnd.ekstep.content-collection\",\"tags\": [\"English\"],\"parent\": [{ \"identifier\": \"org.ekstep.story.en.vayu\", \"index\": 0},{\"identifier\": \"org.ekstep.story.en.tree\", \"index\": 1} ],\"concepts\": [{ \"identifier\": \"LO52\"}, { \"identifier\": \"LO51\"}]}}}";
	String jsonCreateInvalidContent = "{\"request\": {\"content\": {\"identifier\": \"content_collection_8\",\"contentType\": \"Story\",\"body\": \"<theme></theme>\",\"status\": \"Live\",\"description\": \"English Stories Collection\", \"subject\": \"literacy\",\"name\": \"English Stories Collection\", \"owner\": \"EkStep\",\"code\": \"content_collection_7\", \"language\": \"English\", \"mimeType\": \"application/vnd.ekstep.content-collection\",\"tags\": [\"English\"],\"children\": [{ \"identifier\": \"org.ekstep.story.en.vayu\", \"index\": 0},{\"identifier\": \"org.ekstep.story.en.tree\", \"index\": 1} ],\"concepts\": [{ \"identifier\": \"L102\"}, { \"identifier\": \"LO51\"}]}}}";
	String jsonCreateInvalidConceptsContent = "{\"request\": {\"content\": {\"identifier\": \"content_collection_8\",\"contentType\": \"Story\",\"body\": \"<theme></theme>\",\"status\": \"Live\",\"description\": \"English Stories Collection\", \"subject\": \"literacy\",\"name\": \"English Stories Collection\", \"owner\": \"EkStep\",\"code\": \"content_collection_7\", \"language\": \"English\", \"mimeType\": \"application/vnd.ekstep.content-collection\",\"tags\": [\"English\"],\"children\": [{ \"identifier\": \"org.ekstep.story.en.vayu\", \"index\": 0},{\"identifier\": \"org.ekstep.story.en.tree\", \"index\": 1} ],\"concepts\": [{ \"identifier\": \"LO52\"}, { \"identifier\": \"LO51\"}, {\"identifier\": \"Num:C3:SC1:MC12\"}]}}}";
	
	String jsonCreatenewConceptContent = "{\"request\": {\"content\": {\"body\": \"<theme></theme>\",\"status\": \"Draft\",\"description\": \"QA\",\"subject\": \"literacy\",\"name\": \"QA\",\"owner\": \"EkStep\",\"s3Key\": \"content/build_a_sentence_1446706423188.zip\",\"downloadUrl\": \"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/build_a_sentence_1446706423188.zip\",\"code\": \"org.ekstep.num.build.sentence\",\"contentType\": \"Story\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"pkgVersion\": 2, \"tags\": [\"English\"],\"children\": [{ \"identifier\": \"org.ekstep.story.en.vayu\", \"index\": 0},{\"identifier\": \"org.ekstep.story.en.tree\", \"index\": 1} ],\"concepts\": [{ \"identifier\": \"LO52\"}, { \"identifier\": \"LO51\"}]}}}";
	String jsonGetUpdatedContentList = "{\"request\": { \"search\": {\"name\": \"contentname\"}}}";
	String jsonUpdateContentValid = "{\"request\": {\"content\": {\"contentType\": \"Story\",\"body\": \"<theme></theme>\",\"status\": \"Live\",\"description\": \"English Stories Collection\",\"subject\": \"literacy\",\"name\": \"English Stories Collection\",\"owner\": \"EkStep\",\"code\": \"content_collection_7\",\"language\": \"English\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"tags\": [\"English\"],\"children\": [{\"identifier\": \"org.ekstep.story.en.mooncap\", \"index\": 0},{\"identifier\": \"org.ekstep.story.en.vayu\", \"index\": 1}, {\"identifier\": \"org.ekstep.story.en.tree\", \"index\": 2} ],\"concepts\": [{ \"identifier\": \"LO52\"}, { \"identifier\": \"LO51\"}]}}}";
	String jsonCreateAndPublishContentValid = "{\"request\": {\"content\": {\"body\": \"<theme></theme>\",\"status\": \"Live\",\"description\": \"QA\",\"subject\": \"literacy\",\"name\": \"QA2\",\"owner\": \"EkStep\",\"s3Key\": \"content/build_a_sentence_1446706423188.zip\",\"downloadUrl\": \"https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/build_a_sentence_1446706423188.zip\",\"code\": \"org.qa2.publish.content\",\"identifier\":\"org.qa2.publish.content\",\"contentType\": \"Worksheet\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"pkgVersion\": 2}}}";
	
	
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
				patch("v2/content/list").
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
				patch("v2/content/list").
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
				patch("v2/content/search").
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
				patch("v2/content/search").
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
			get("v2/content/org.ekstep.num.addition.by.grouping").
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
			post("v2/content").
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
			post("v2/content").
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
				patch("v2/content/content_collection_8").
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
				patch("v2/content/not_existing").
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
			post("v2/content").
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
			post("v2/content").
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
			post("v2/content").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	@Test
	public void createContentWithMultipleInvalidConceptsExpect400()
	{
	setURI();
	given().
	spec(getRequestSpec(contentType, validuserId)).
	body(jsonCreateInvalidConceptsContent).
	with().
		content(JSON).
	when().
		post("v2/content").
	then().
		log().all().
		spec(get400ResponseSpec());
	}

	@Test
	public void createContentWithConceptExpectSuccess200()
	{
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(jsonCreatenewConceptContent).
		with().
			content(JSON).
		when().
			post("v2/content").
		then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	
	
	
	
	
}
