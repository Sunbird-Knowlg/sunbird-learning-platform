package org.sunbird.framework;

import org.sunbird.platform.domain.BaseTest;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.hasItems;

import org.junit.Ignore;
import org.junit.Test;
import org.testng.Assert;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

/**
 * @author Vignesh
 *
 */
public class CategoryV3APITests extends BaseTest{
	int rn = generateRandomInt(0, 999999);
	String jsonCreateCategoryRequest = "{\"request\":{\"category\":{\"name\":\"LP_FT_CategoryName_"+rn+"\",\"description\":\"Description of Category.\",\"code\":\"LP_FT_Category_"+rn+"\"}}}";
	String jsonUpdateCategoryRequest = "{\"request\":{\"category\":{\"description\":\"LP_FT_category updated description\",\"name\":\"LP_FT_UpdatedName"+rn+"\"}}}";
	
	public void retireCreatedCategory(String categoryId){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/framework/v3/category/master/retire/"+categoryId).
		then().
		//log().all().
		spec(get200ResponseSpec());
	}
	
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
			//log().all().
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
		//log().all().
		spec(get200ResponseSpec());
		retireCreatedCategory(categoryId);
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
			//log().all().
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
		retireCreatedCategory(categoryId);
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
		//log().all().
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
		//log().all().
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
			//log().all().
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
		//log().all().
		spec(get200ResponseSpec());
		retireCreatedCategory(categoryId);
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
		//log().all().
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
		//log().all().
		spec(get500ResponseSpec());
	}
	
	// Update category with valid category id
	@Test
	public void updateValidCategoryExpectSuccess200(){
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
			//log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp = R.jsonPath();
		String categoryId = jp.get("result.node_id");
		
		// Update the created category
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonUpdateCategoryRequest).
		with().
		contentType(JSON).
		when().
		patch("/framework/v3/category/master/update/"+categoryId).
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		// Read category and validate
		setURI();
		Response R1 =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
			when().
			get("/framework/v3/category/master/read/"+categoryId).
			then().
			//log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp1 = R1.jsonPath();
		String description = jp1.get("result.category.description");
		Assert.assertTrue(description.contains("updated description"));
		retireCreatedCategory(categoryId);
	}
	
	// Update invalid category(non-existing)
	@Test
	public void updateInvalidCategoryExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonUpdateCategoryRequest).
		with().
		contentType(JSON).
		when().
		patch("/framework/v3/category/master/update/fashdk").
		then().
		//log().all().
		spec(get400ResponseSpec());
	}
	
	// Update without request body
	
	// Search category
	@Test
	public void searchCategoryExpectSuccess200(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("{\"request\":{\"search\":{\"status\":\"Live\"}}}").
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/master/search").
		then().
		//log().all().
		spec(get200ResponseSpec()).
		body("result.categories.status", hasItems("Live"));
	}
	
	// Create and search category
	@Test
	public void createAndSearchCategoryExpectSuccess200(){
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
			//log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp = R.jsonPath();
		String categoryId = jp.get("result.node_id");
		
		// Search created category
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("{\"request\":{\"search\":{\"code\":\""+categoryId+"\",\"status\":\"Live\"}}}").
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/master/search").
		then().
		//log().all().
		spec(get200ResponseSpec()).
		body("result.categories.identifier", hasItems(categoryId));
		retireCreatedCategory(categoryId);
	}
	
	// Search category with invalid request expect null response
	@Test
	public void searchCategoryInvalidReqExpectSuccess200(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("{\"request\":{\"search\":{\"status\":\"Draft\"}}}").
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/master/search").
		then().
		//log().all().
		spec(get200ResponseSpec());
	}	
	
	// Delete valid category
	@Test
	public void retireValidCategoryExpectSuccess200(){
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
			//log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp = R.jsonPath();
		String categoryId = jp.get("result.node_id");
		
		// Retire the created category
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/framework/v3/category/master/retire/"+categoryId).
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		// Read category and validate
		setURI();
		Response R1 =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
			body(jsonCreateCategoryRequest).
			with().
			contentType(JSON).			
			when().
			get("/framework/v3/category/master/read/"+categoryId).
			then().
			//log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp1 = R1.jsonPath();
		String status = jp1.get("result.category.status");
		Assert.assertTrue(status.equals("Retired"));
	}
	
	// Retire non existing category
	@Test
	public void retireInvalidCategoryExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/framework/v3/category/master/retire/kbfjfaehkajg").
		then().
		//log().all().
		spec(get400ResponseSpec());
	}
	
	// Retire already retired category
	@Test
	public void retireRetiredCategoryExpectSuccess200(){
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
			//log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp = R.jsonPath();
		String categoryId = jp.get("result.node_id");
		
		// Retire the created category
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/framework/v3/category/master/retire/"+categoryId).
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		// Read category and validate
		setURI();
		Response R1 =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
			body(jsonCreateCategoryRequest).
			with().
			contentType(JSON).			
			when().
			get("/framework/v3/category/master/read/"+categoryId).
			then().
			//log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp1 = R1.jsonPath();
		String status = jp1.get("result.category.status");
		Assert.assertTrue(status.equals("Retired"));
		
		// Retire the retired category
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/framework/v3/category/master/retire/"+categoryId).
		then().
		//log().all().
		spec(get200ResponseSpec());
	}
	
	// Retire category with blank request
	@Test
	public void retireBlankCategoryExpect5xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/framework/v3/category/master/retire/").
		then().
		//log().all().
		spec(get500ResponseSpec());
	}
}
