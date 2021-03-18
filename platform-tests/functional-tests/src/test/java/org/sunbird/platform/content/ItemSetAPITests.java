	package org.sunbird.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.is;

import org.sunbird.platform.domain.BaseTest;
import org.junit.Ignore;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class ItemSetAPITests extends BaseTest 
{
	int rn = generateRandomInt(0, 500);
	
	String JsonCreateItemSetValid = "{\"request\": {\"assessment_item_set\": {\"objectType\": \"ItemSet\",\"metadata\": {\"title\": \"QA_Testing_ItemSet - MCQQ_{{$randomInt}}.\",\"type\": \"materialised\",\"max_score\": 4,\"total_items\": 1,\"description\": \"QA of ItemSet Using AssessmentItems\",\"code\": \"QA_ItemSet_"+rn+"\",\"difficulty_level\": \"low\",\"used_for\": \"assessment\",\"memberIds\": [\"G5Q1\"]}}}}";
	String JsonCreateItemSetWithtotal_itemsNotEqualToItemsSize = "{\"request\": {\"assessment_item_set\": {\"objectType\": \"ItemSet\",\"metadata\": {\"title\": \"QA_Testing_ItemSet - MCQQ_{{$randomInt}}.\",\"type\": \"materialised\",\"max_score\": 4,\"total_items\": 4,\"description\": \"QA of ItemSet Using AssessmentItems\",\"code\": \"QA_ItemSet_{{$randomInt}}\",\"difficulty_level\": \"low\",\"owner\": \"Ilimi\",\"used_for\": \"assessment\",\"memberIds\": [\"G5Q1\"]}}}}";
	String JsonCreateItemSetWithConcept = "{\"request\": {\"assessment_item_set\": {\"objectType\": \"ItemSet\",\"metadata\": {\"title\": \"Testing_ItemSet - MCQQ_{{$randomInt}}.\",\"type\": \"materialised\",\"identifier\": \"domain_905\",\"max_score\": 3,\"total_items\": 3,\"description\": \"Testing of ItemSet Using AssessmentItems\",\"code\": \"Test_ItemSet_{{$randomInt}}\",\"difficulty_level\":\"low\",\"owner\": \"Ilimi\",\"used_for\": \"assessment\",\"gradeLevel\": [\"Grade 1\"],\"memberIds\": [\"ek.n.q935\"],\"concepts\": [{\"relationName\": \"associatedTo\",\"identifier\": \"LO17\"}]}}}}";
	String JsonCreateItemSetWithInvalidMembers = "{\"request\": {\"assessment_item_set\": {\"objectType\": \"ItemSet\",\"metadata\": {\"title\": \"QA_Testing_ItemSet - MCQQ_{{$randomInt}}.\",\"type\": \"materialised\",\"max_score\": 4,\"total_items\": 4,\"description\": \"QA of ItemSet Using AssessmentItems\",\"code\": \"QA_ItemSet_{{$randomInt}}\",\"difficulty_level\": \"low\",\"owner\": \"Ilimi\",\"used_for\": \"assessment\",\"memberIds\": [\"G5Q1\"]}}}}";
	String JsonSearchValid = "{\"request\": {\"resultSize\": 10}}";
	String JsonSearchWithInValidSearchcriteria = "{\"request\": {\"max_score\": 3,\"resultSize\": 40}}";
	String JsonUpdateItemSetValid= "{\"request\": {\"assessment_item_set\": {\"objectType\": \"ItemSet\",\"metadata\": {\"title\": \"Testing ItemSet - MCQQ_650.\",\"type\": \"materialised\",\"description\": \"Testing of ItemSet Using AssessmentItems\",\"code\": \"ItemSet_650\",\"difficulty_level\": \"high\",\"used_for\": \"assessment\",\"max_score\": 3,\"memberIds\": [\"MCQQ_509\",\"MCQQ_901\",\"MCQQ_622\",\"MMCQQ_577\",\"MMCQQ_44\",\"FTBQ_173\",\"FTBQ_878\"]}}}}";
	String JsonUpdateItemSetWithoutMandatoryFields = "{\"request\": {\"assessment_item_set\": {\"objectType\": \"ItemSet\",\"metadata\": {\"title\": \"Testing ItemSet - MCQQ_650.\",\"memberIds\": [\"MCQQ_509\",\"MCQQ_901\",\"MCQQ_622\",\"MMCQQ_577\",\"MMCQQ_44\",\"FTBQ_173\",\"FTBQ_878\"]}}}}";
	String JsonCreateItemSetWithConceptsAsMembers = "{\"request\": {\"assessment_item_set\": {\"objectType\": \"ItemSet\",\"metadata\": {\"title\": \"Testing_ItemSet - MCQQ_{{$randomInt}}.\",\"type\": \"materialised\",\"max_score\": 3,\"total_items\": 3,\"description\": \"Testing of ItemSet Using AssessmentItems\",\"code\": \"Test_ItemSet_{{$randomInt}}\",\"difficulty_level\": \"low\",\"owner\": \"Ilimi\",\"used_for\": \"assessment\",\"memberIds\": [\"ek.n.q935\",\"LO17\"]}}}}";
	String JsonCreateItemSetWithGivenIdentifier = "{\"request\": {\"assessment_item_set\": {\"objectType\": \"ItemSet\",\"metadata\": {\"title\": \"Testing_ItemSet - MCQQ_{{$randomInt}}.\",\"type\": \"materialised\",\"identifier\": \"domain_1000\",\"max_score\": 3,\"total_items\": 1,\"description\": \"Testing of ItemSet Using AssessmentItems\",\"code\": \"Test_ItemSet_{{$randomInt}}\",\"difficulty_level\": \"low\",\"owner\": \"Ilimi\",\"used_for\": \"assessment\",\"memberIds\": [\"G5Q1\"]}}}}";
	
	//Create ItemSet
	@Test
	public void createItemSetExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
			body(JsonCreateItemSetValid).
		with().
			contentType(JSON).
		when().
			post("/learning/v1/assessmentitemset").
		then().
//			log().all().
			spec(get200ResponseSpec());		
	}
	
	
	@Test
	public void createItemSetWithInvalidMembersExpect400()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
			body(JsonCreateItemSetWithInvalidMembers).
		with().
			contentType(JSON).
		when().
			post("/learning/v1/assessmentitemset").
		then().
			//log().all().
			spec(get400ResponseSpec());
	}
	
	@Test
	public void createItemSetWithConceptsAsMembersExpect400()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
			body(JsonCreateItemSetWithConceptsAsMembers).
		with().
			contentType(JSON).
		when().
			post("/learning/v1/assessmentitemset").
		then().
			//log().all().
			spec(get400ResponseSpec());
	}
	
	@Test
	public void createItemSetWithtotal_itemsNotEqualToItemsSizeExpect400()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
			body(JsonCreateItemSetWithtotal_itemsNotEqualToItemsSize).
		with().
			contentType(JSON).
		when().
			post("/learning/v1/assessmentitemset").
		then().
			//log().all().
			spec(get400ResponseSpec());		
	}
	
	@Test
	public void createItemSetWithGivenIdentifierExpectSuccess200()
	{
		setURI();
		Response R =
		given().
			spec(getRequestSpec(contentType,validuserId)).
			body(JsonCreateItemSetWithGivenIdentifier).
		with().
			contentType(JSON).
		when().
			post("/learning/v1/assessmentitemset").
		then().
//			log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp = R.jsonPath();
		String set_id = jp.get("result.set_id");
		
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("/learning/v1/assessmentitemset/"+set_id). 
    	then().
//			log().all().
			spec(get200ResponseSpec());
	}
	
	//Get ItemSet
	@Test
	public void getItemSetExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("/learning/v1/assessmentitemset/domain_3250").
		then().
			//log().all().
			spec(get200ResponseSpec());
	}
	
	@Test
	public void getItemSetOfNonExistingDomainExpect4xx()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("/learning/v1/assessmentitemset/domain_472").
		then().
			//log().all().
			spec(get404ResponseSpec());
	}
	
	//Search ItemSet
	@Test
	public void searchItemSetExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
			body(JsonSearchValid).
		with().
			contentType(JSON).
		when().
			post("/learning/v1/assessmentitemset/search").
		then().
			//log().all().
			spec(get200ResponseSpec()).
			body("result.assessment_item_sets.size()", is(10));
	}
	
	@Test
	public void searchItemSetWithInvalidSearchcriteriaExpect500()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
			body(JsonSearchWithInValidSearchcriteria).
		with().
			contentType(JSON).
		when().
			post("/learning/v1/assessmentitemset/search").
		then().
			//log().all().
			spec(get500ResponseSpec());
	}
	
	@Test
	public void UpdateNonExistingItemSetExpect4xx()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonUpdateItemSetValid).
		with().
			contentType("application/json").
		when().
			patch("/learning/v1/assessmentitemset/domain_1111").
		then().
			//log().all().
			spec(get404ResponseSpec());
	}
	
	@Test
	public void UpdateItemSetWithoutMandatoryFieldsExpect4xx()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonUpdateItemSetWithoutMandatoryFields).
		with().
			contentType("application/json").
		when().
			patch("/learning/v1/assessmentitemset/domain_804").
		then().
			//log().all().
			spec(get400ResponseSpec());
	}
	
	@Test
	public void UpdateItemSetWithoutBodyExpect4xx()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		with().
			contentType("application/json").
		when().
			patch("/learning/v1/assessmentitemset/domain_804").
		then().
			//log().all().
			spec(get400ResponseSpec());
	}
	
	//Delete ItemSet
	// TODO: Need to fix it
	@Ignore
	@Test
	public void deleteItemSetExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			delete("/learning/v1/assessmentitemset/domain_835").
		then().
			//log().all().
			spec(get200ResponseSpec());
	}

	// TODO: Need to fix it 
	@Ignore
	@Test
	public void deleteNonExistingItemSetExpect200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			delete("/learning/v1/assessmentitemset/domain_840").
		then().
			//log().all().
			spec(get200ResponseSpec());
	}
}
