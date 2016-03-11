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
			
		//Publish Content
		@Test
		public void createAndpLublishContentExpect200(){		
			
			//Create Content
			
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
			
			//Getting the content Id
			
			JsonPath jP = R.jsonPath();
			String nodeId = jP.get("result.node_id");
			System.out.println(nodeId);
			
					
			//Publish Content
			
			setURI();
			given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonCreateAndPublishContentValid).
			with().
				contentType(JSON).
			when().
				post("content/publish/"+nodeId).
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
		public void createThreeConceptsContentExpects400()
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
			body(jsonCreatenewConceptContent).
			with().
				content(JSON).
			when().
				post("content").
			then().
				log().all().
				spec(get200ResponseSpec());
		}	
}
