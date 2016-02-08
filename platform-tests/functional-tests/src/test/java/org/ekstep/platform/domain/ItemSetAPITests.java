package org.ekstep.platform.domain;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.*;
import org.junit.Test;

public class ItemSetAPITests extends BaseTest 
{
	String JsonCreateItemSetValid = "{\"request\": {\"assessment_item_set\": {\"objectType\": \"ItemSet\",\"metadata\": {\"title\": \"QA_Testing_ItemSet - MCQQ_{{$randomInt}}.\",\"type\": \"materialised\",\"max_score\": 4,\"total_items\": 4,\"description\": \"QA of ItemSet Using AssessmentItems\",\"code\": \"QA_ItemSet_{{$randomInt}}\",\"difficulty_level\": \"low\",\"owner\": \"Ilimi\",\"used_for\": \"assessment\",\"memberIds\": [\"ek.n.q935\"]}}}}";
	String JsonCreateItemSetWithConcept = "{\"request\": {\"assessment_item_set\": {\"objectType\": \"ItemSet\",\"metadata\": {\"title\": \"Testing_ItemSet - MCQQ_{{$randomInt}}.\",\"type\": \"materialised\",\"identifier\": \"domain_905\",\"max_score\": 3,\"total_items\": 3,\"description\": \"Testing of ItemSet Using AssessmentItems\",\"code\": \"Test_ItemSet_{{$randomInt}}\",\"difficulty_level\":\"low\",\"owner\": \"Ilimi\",\"used_for\": \"assessment\",\"gradeLevel\": [\"Grade 1\"],\"memberIds\": [\"ek.n.q935\"],\"concepts\": [{\"relationName\": \"associatedTo\",\"identifier\": \"LO17\"}]}}}}";
	String JsonCreateItemSetWithInvalidMembers = "{\"request\": {\"assessment_item_set\": {\"objectType\": \"ItemSet\",\"metadata\": {\"title\": \"QA_Testing_ItemSet - MCQQ_{{$randomInt}}.\",\"type\": \"materialised\",\"max_score\": 4,\"total_items\": 4,\"description\": \"QA of ItemSet Using AssessmentItems\",\"code\": \"QA_ItemSet_{{$randomInt}}\",\"difficulty_level\": \"low\",\"owner\": \"Ilimi\",\"used_for\": \"assessment\",\"memberIds\": [\"ek.n.q931\"]}}}}";
	String JsonSearchValid = "{\"request\": {\"resultSize\": 10}}";
	String JsonSearchWithInValidSearchcriteria = "{\"request\": {\"max_score\": 3,\"resultSize\": 40}}";
	String JsonUpdateItemSetValid= "{\"request\": {\"assessment_item_set\": {\"objectType\": \"ItemSet\",\"metadata\": {\"title\": \"Testing ItemSet - MCQQ_650.\",\"type\": \"materialised\",\"description\": \"Testing of ItemSet Using AssessmentItems\",\"code\": \"ItemSet_650\",\"difficulty_level\": \"high\",\"owner\": \"Ilimi\",\"used_for\": \"assessment\",\"max_score\": 3,\"memberIds\": [\"MCQQ_509\",\"MCQQ_901\",\"MCQQ_622\",\"MMCQQ_577\",\"MMCQQ_44\",\"FTBQ_173\",\"FTBQ_878\"]}}}}";
	String JsonUpdateItemSetWithoutMandatoryFields = "{\"request\": {\"assessment_item_set\": {\"objectType\": \"ItemSet\",\"metadata\": {\"title\": \"Testing ItemSet - MCQQ_650.\",\"memberIds\": [\"MCQQ_509\",\"MCQQ_901\",\"MCQQ_622\",\"MMCQQ_577\",\"MMCQQ_44\",\"FTBQ_173\",\"FTBQ_878\"]}}}}";
	String JsonCreateItemSetWithConceptsAsMembers = "{\"request\": {\"assessment_item_set\": {\"objectType\": \"ItemSet\",\"metadata\": {\"title\": \"Testing_ItemSet - MCQQ_{{$randomInt}}.\",\"type\": \"materialised\",\"max_score\": 3,\"total_items\": 3,\"description\": \"Testing of ItemSet Using AssessmentItems\",\"code\": \"Test_ItemSet_{{$randomInt}}\",\"difficulty_level\": \"low\",\"owner\": \"Ilimi\",\"used_for\": \"assessment\",\"memberIds\": [\"ek.n.q935\",\"LO17\"]}}}}";
	
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
			post("v1/assessmentitemset").
		then().
			log().all().
			spec(get200ResponseSpec());		
	}
	
	@Test
	public void createItemSetWithInvalidURLExpect500()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
			body(JsonCreateItemSetValid).
		with().
			contentType(JSON).
		when().
			post("v1/assessmeitemset").
		then().
			//log().all().
			spec(get500ResponseSpec());
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
			post("v1/assessmentitemset").
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
			post("v1/assessmentitemset").
		then().
			//log().all().
			spec(get400ResponseSpec());
	}
	
	//Get ItemSet
	//Needs change - create item, get the domain id from result and then get that. 
	@Test
	public void getItemSetExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v1/assessmentitemset/domain_740").
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
			get("v1/assessmentitemset/domain_472").
		then().
			//log().all().
			spec(get404ResponseSpec());
	}
	
	@Test
	public void getItemSetWithMisspelledURLExpect500()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v1/assessmitemset/domain_740").
		then().
			//log().all().
			spec(get500ResponseSpec());
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
			post("v1/assessmentitemset/search").
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
			post("v1/assessmentitemset/search").
		then().
			//log().all().
			spec(get500ResponseSpec());
	}
	
	//Update ItemSet
	//To-do: create itemset, get the domain id from the result and then send an update request for it. 
	@Test
	public void updateItemSetValidInputsExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonUpdateItemSetValid).
		with().
			contentType("application/json").
		when().
			patch("v1/assessmentitemset/domain_802").
		then().
			//log().all().
			spec(get200ResponseSpec());
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
			patch("v1/assessmentitemset/domain_1111").
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
			patch("v1/assessmentitemset/domain_804").
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
			patch("v1/assessmentitemset/domain_804").
		then().
			//log().all().
			spec(get400ResponseSpec());
	}
	
	//Delete ItemSet
	@Test
	public void deleteItemSetExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			delete("v1/assessmentitemset/domain_835").
		then().
			//log().all().
			spec(get200ResponseSpec());
	}
	
	@Test
	public void deleteNonExistingItemSetExpect4xx()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
		when().
			delete("v1/assessmentitemset/domain_840").
		then().
			//log().all().
			spec(get400ResponseSpec());
	}
}
