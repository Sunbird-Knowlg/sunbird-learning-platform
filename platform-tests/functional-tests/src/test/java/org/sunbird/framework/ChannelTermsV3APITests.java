package org.sunbird.framework;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.hasItems;

import org.sunbird.platform.domain.BaseTest;
import org.junit.Ignore;
import org.junit.Test;
import org.testng.Assert;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
@Ignore
public class ChannelTermsV3APITests extends BaseTest{

	String categoryId;
	
	// Create valid channel term
	@Test
	public void createValidChannelTermExpectSuccess200(){
		setURI();
		Response R =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
			body("").
			with().
			contentType(JSON).
			when().
			post("/channel/v3/term/create?category="+categoryId+"").
			then().
			// log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp = R.jsonPath();
		String termId = jp.get("result.node_id");
		
		// Validate the created channel term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/channel/v3/term/read/"+termId+"?category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());		
	}
	
	// Create channel term without code
	@Test
	public void createChannelTermWithoutCodeExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("").
		with().
		contentType(JSON).
		when().
		post("/channel/v3/term/create?category="+categoryId+"").
		then().
		// log().all().
		spec(get400ResponseSpec());
		
	}
	
	// Create channel term with invalid category id
	@Test
	public void createChannelTermWithInvalidCategoryExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("").
		with().
		contentType(JSON).
		when().
		post("/channel/v3/term/create?category=slk;fna").
		then().
		// log().all().
		spec(get400ResponseSpec());		
	}
	
	// Create channel term with blank category id
	@Test
	public void createChannelTermWithBlankCategoryExpect5xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("").
		with().
		contentType(JSON).
		when().
		post("/channel/v3/term/create?category=").
		then().
		// log().all().
		spec(get500ResponseSpec());
	}
	
	// Create existing channel term
	@Test
	public void createExistingChannelTermExpect4xx(){
		setURI();
		Response R =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
			body("").
			with().
			contentType(JSON).
			when().
			post("/channel/v3/term/create?category="+categoryId+"").
			then().
			// log().all().
			extract().response();
		
		JsonPath jp = R.jsonPath();
		String termId = jp.get("result.node_id");
		
		// Create channel term with created term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("").
		with().
		contentType(JSON).
		when().
		post("/channel/v3/term/create?category="+categoryId+"").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	// Read valid channel term
	@Test
	public void readValidChannelTermExpectSuccess200(){
		setURI();
		Response R =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
			body("").
			with().
			contentType(JSON).
			when().
			post("/channel/v3/term/create?category="+categoryId+"").
			then().
			// log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp = R.jsonPath();
		String termId = jp.get("result.node_id");
		
		// Validate the created channel term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/channel/v3/term/read/"+termId+"?category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
	}
	
	// Read valid channel term with invalid category id
	@Test
	public void readChannelTermWithInvalidCategoryExpect4xx(){
		setURI();
		Response R =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
			body("").
			with().
			contentType(JSON).
			when().
			post("/channel/v3/term/create?category=aksjfcas").
			then().
			// log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp = R.jsonPath();
		String termId = jp.get("result.node_id");
		
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/channel/v3/term/read/"+termId+"?category=falnf;la").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	// Read invalid channel term(non existing)
	@Test
	public void readInvalidChannelTermExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/channel/v3/term/read/,bcadj,?category="+categoryId+"").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	// Read blank channel term
	@Test
	public void readBlankChannelTermExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/channel/v3/term/read/?category="+categoryId+"").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	// Read channel term with invalid path
	@Test
	public void readChannelTermInvalidPathExpect5xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/channel/v3/term/read/?category=").
		then().
		// log().all().
		spec(get500ResponseSpec());
	}
	
	// Search valid channel terms
	@Test
	public void searchValidChannelTermsExpectSuccess200(){
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("").
		with().
		contentType(JSON).
		when().
		post("/channel/v3/term/search?category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec()).
		body("result.categories.status", hasItems("Live"));

	}
	
	// Search with invalid request and valid category id
	@Test
	public void searchWithInvalidRequestExpectSuccess200(){
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("").
		with().
		contentType(JSON).
		when().
		post("/channel/v3/term/search?category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
	}
	
	// Search with valid request and invalid category id
	@Test 
	public void searchWithInvalidCategoryExpect4xx(){
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("").
		with().
		contentType(JSON).
		when().
		post("/channel/v3/term/search?category=aksdkfh").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	// Create and search channel term
	@Test
	public void createAndSearchValidChannelTermExpect200(){
		setURI();
		Response R =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
			body("").
			with().
			contentType(JSON).
			when().
			post("/channel/v3/term/create?category="+categoryId+"").
			then().
			// log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp = R.jsonPath();
		String termId = jp.get("result.node_id");
		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("").
		with().
		contentType(JSON).
		when().
		post("/channel/v3/term/search?category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec()).
		body("result.terms.identifier", hasItems(termId));
	}
	
	// Update valid channel term with valid category id
	@Test
	public void updateValidChannelTermExpectSuccess200(){
		setURI();
		Response R =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
			body("").
			with().
			contentType(JSON).
			when().
			post("/channel/v3/term/create?category="+categoryId+"").
			then().
			// log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp = R.jsonPath();
		String termId = jp.get("result.node_id");
		
		// Update Channel term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("").
		with().
		contentType(JSON).
		when().
		patch("/channel/v3/term/update/"+termId+"?category="+categoryId+"").
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		// Read channel and validate
		setURI();
		Response R1 =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
			when().
			get("/channel/v3/term/read/?category="+categoryId+"").
			then().
			//log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp1 = R1.jsonPath();
		String description = jp1.get("result.category.description");
		Assert.assertTrue(description.contains("updated description"));
	}
	
	// Update invalid channel term with valid category id
	@Test
	public void updateInvalidChannelTermExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("").
		with().
		contentType(JSON).
		when().
		patch("/channel/v3/term/update/aslcndj?category="+categoryId+"").
		then().
		//log().all().
		spec(get200ResponseSpec());

	}
	
	// Update valid channel term with invalid category id
	@Test
	public void updateChannelTermWithInvalidCategoryExpect4xx(){
		setURI();
		Response R =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
			body("").
			with().
			contentType(JSON).
			when().
			post("/channel/v3/term/create?category="+categoryId+"").
			then().
			// log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp = R.jsonPath();
		String termId = jp.get("result.node_id");
		
		// Update Channel term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("").
		with().
		contentType(JSON).
		when().
		patch("/channel/v3/term/update/"+termId+"?category=ajskdvhiua").
		then().
		//log().all().
		spec(get400ResponseSpec());
	}
	
	// Update channel term with invalid request
	@Test
	public void updateChannelTermWithInvalidRequestExpect4xx(){
		setURI();
		Response R =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
			body("").
			with().
			contentType(JSON).
			when().
			post("/channel/v3/term/create?category="+categoryId+"").
			then().
			// log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp = R.jsonPath();
		String termId = jp.get("result.node_id");
		
		// Update Channel term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("").
		with().
		contentType(JSON).
		when().
		patch("/channel/v3/term/update/"+termId+"?category="+categoryId+"").
		then().
		//log().all().
		spec(get400ResponseSpec());
	}
	
	// Update channel term with invalid path(Blank channel term)
	@Test
	public void updateChannelTermWithInvalidPathExpect5xx(){		
		// Update Channel term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("").
		with().
		contentType(JSON).
		when().
		patch("/channel/v3/term/update/?category="+categoryId+"").
		then().
		//log().all().
		spec(get400ResponseSpec());
	}
	
	// Retire valid channel term
	@Test
	public void retireValidChannelTermExpectSuccess200(){
		setURI();
		Response R =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
			body("").
			with().
			contentType(JSON).
			when().
			post("/channel/v3/term/create?category="+categoryId+"").
			then().
			// log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp = R.jsonPath();
		String termId = jp.get("result.node_id");
		
		// Retire the created channel term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/channel/v3/term/retire/"+termId+"?category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
		
		// Read and validate the status
		setURI();
		Response R1 =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
			when().
			get("/channel/v3/term/read/"+termId+"?category="+categoryId+"").
			then().
			//log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp1 = R1.jsonPath();
		String status = jp1.get("result.category.term.status");
		Assert.assertTrue(status.equals("Retired"));
	}
	
	// Retire valid channel term with invalid category id
	@Test
	public void retireChannelTermWithInvalidCategoryExpect4xx(){
		setURI();
		Response R =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
			body("").
			with().
			contentType(JSON).
			when().
			post("/channel/v3/term/create?category="+categoryId+"").
			then().
			// log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp = R.jsonPath();
		String termId = jp.get("result.node_id");
		
		// Retire the created channel term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/channel/v3/term/retire/"+termId+"?category=asdfkj").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	// Retire invalid channel term(non existing)
	@Test
	public void retireInvalidChannelTermExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/channel/v3/term/retire/akdvsfl?category="+categoryId+"").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	// Retire blank channel term
	@Test
	public void retireBlankChannelTermExpect5xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/channel/v3/term/retire/?category="+categoryId+"").
		then().
		// log().all().
		spec(get500ResponseSpec());
	}
	
	// Retire channel term with retire status
	@Test
	public void retireRetiredChannelTermExpectSuccess200(){
		setURI();
		Response R =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
			body("").
			with().
			contentType(JSON).
			when().
			post("/channel/v3/term/create?category="+categoryId+"").
			then().
			// log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp = R.jsonPath();
		String termId = jp.get("result.node_id");
		
		// Retire the created channel term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/channel/v3/term/retire/"+termId+"?category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
		
		// Retire retired channel term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/channel/v3/term/retire/"+termId+"?category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());		
	}
}
