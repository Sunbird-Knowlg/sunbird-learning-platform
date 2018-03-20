package org.ekstep.framework;

import org.ekstep.platform.domain.BaseTest;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import org.junit.Ignore;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class CategoryV3APITests extends BaseTest{
	int rn = generateRandomInt(0, 999999);
	String jsonCreateCategoryRequest = "{\"request\":{\"category\":{\"name\":\"LP_FT_CategoryName_"+rn+"\",\"description\":\"Description of Category.\",\"code\":\"LP_FT_Category_"+rn+"\"}}}";

	// Create valid category and validate
	@Test
	public void createValidCategoryExpectSuccess200(){
		setURI();
		Response R =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
			body(jsonCreateCategoryRequest).
			with().
			contentType(JSON).			
			when().
			post("/framework/v3/category/master/create").
			then().
			log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp = R.jsonPath();
		String categoryId = jp.get("result.node_id");
		
		// Validate the category id
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/framework/v3/category/master/read/"+categoryId).
		then().
		log().all().
		spec(get200ResponseSpec());
	}
	
	// Create category with existing category id
	@Test
	public void createExistingCategoryExpect4xx(){
		setURI();
		Response R =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
			body(jsonCreateCategoryRequest).
			with().
			contentType(JSON).			
			when().
			post("/framework/v3/category/master/create").
			then().
			log().all().
			//spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp = R.jsonPath();
		String categoryId = jp.get("result.node_id");
		
		// Create category with existing id
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("{\"request\":{\"category\":{\"name\":\"LP_FT_CategoryName_"+rn+"\",\"description\":\"Description of Category.\",\"code\":\""+categoryId+"\"}}}").
		with().
		contentType(JSON).			
		when().
		post("/framework/v3/category/master/create").
		then().
		log().all().
		spec(get400ResponseSpec());		
	}
	
	// Create category without code
	@Test
	public void createCategoryWithBlankCodeExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("{\"request\":{\"category\":{\"name\":\"LP_FT_CategoryName_"+rn+"\",\"description\":\"Description of Category.\",\"code\":\"}}}").
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/master/create").
		then().
		log().all().
		spec(get400ResponseSpec());		
	}
	
	// Create Category with invalid path
	@Ignore
	public void createCategoryWithInvalidPathExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateCategoryRequest).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/master/creat").
		then().
		log().all().
		spec(get404ResponseSpec());		
	}
	
	// Read Category with valid category
	@Test
	public void readCategoryWithValidCategoryIdExpectSuccess200(){
		setURI();
		Response R =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
			body(jsonCreateCategoryRequest).
			with().
			contentType(JSON).			
			when().
			post("/framework/v3/category/master/create").
			then().
			log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp = R.jsonPath();
		String categoryId = jp.get("result.node_id");
		
		// Validate the category id
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/framework/v3/category/master/read/"+categoryId).
		then().
		log().all().
		spec(get200ResponseSpec());
	}
	
	// Read category with invalid category id
	@Test
	public void readCategoryWithInvalidIdExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/framework/v3/category/master/read/akldshf").
		then().
		log().all().
		spec(get404ResponseSpec());
	}
	
	// Read category with no category id
	@Test
	public void readCategoryWithBlankCategoryIdExpect5xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/framework/v3/category/master/read/").
		then().
		log().all().
		spec(get500ResponseSpec());
	}
	
}
