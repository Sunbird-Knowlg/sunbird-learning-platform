/**
 * 
 */
package org.sunbird.framework;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.hasItems;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.platform.domain.BaseTest;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.testng.Assert;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

/**
 * @author Vignesh
 *
 */
public class CategoryTermsV3APITests extends BaseTest{
	int rn = generateRandomInt(0, 999999);
	
	String jsonCreateCategoryTerm = "{\"request\":{\"term\":{\"name\":\"LP_FT_CategoryName_"+rn+"\",\"description\":\"Description of Category.\",\"code\":\"LP_FT_Category_"+rn+"\"}}}";
	String jsonUpdateCategoryTerm = "{\"request\":{\"term\":{\"description\":\"LP_FT_category updated description\",\"name\":\"LP_FT_UpdatedName"+rn+"\"}}}";
	String jsonSearchCategoryTerm = "{\"request\":{\"search\":{\"status\":\"Live\"}}}";
	
	private static String categoryId = "";

	@Before
	public void init() {
		if (StringUtils.isBlank(categoryId))
			createCategory();
			//System.out.println(categoryId);
	}

	private void createCategory() {
		setURI();
		Response R = 
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body("{\"request\":{\"category\":{\"name\":\"LP_FT_CategoryName_"+rn+"\",\"description\":\"Description of Category.\",\"code\":\"LP_FT_Category_"+rn+"\"}}}").
				with().
				contentType(JSON).
				when().
				post("/framework/v3/category/master/create").
				then().
				// log().all().
				extract().response();
		
		JsonPath jp1 = R.jsonPath();
		categoryId = jp1.get("result.node_id");
	}
	
	// Create and validate valid category term
	@Test
	public void createValidCategoryTermExpectSuccess200(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateCategoryTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId, appId)).
		body(jsonCreateCategoryTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/term/create?category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
		
		// Validate the created category term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/framework/v3/category/term/read/"+termId+"?category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
	}
	
	// Create category term with blank category
	@Test
	public void createCategoryTermWithBlankCategoryAndFrameworkExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateCategoryTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/term/create?framework=&category=").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	
	// Create category term with and invalid category
	@Test
	public void createCategoryTermValidFrameworkInvalidCategoryExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateCategoryTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/term/create?category=lkvpasdgs").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	// Create category term without code
	@Test
	public void createCategoryTermWithoutCodeExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("{\"request\":{\"term\":{\"name\":\"LP_FT_CategoryName_"+rn+"\",\"description\":\"Description of Category.\",\"code\":\"\"}}}").
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/term/create?category="+categoryId+"").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	// Create existing categoryTerm
	@Test
	public void createExistingCategoryTermExpect4xx(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateCategoryTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateCategoryTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/term/create?category="+categoryId+"");
		
		// Create with existing id
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("{\"request\":{\"term\":{\"name\":\"LP_FT_CategoryName_"+rn+"\",\"description\":\"Description of Category.\",\"code\":\""+termId+"\"}}}").
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/term/create?category="+categoryId+"").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	// Read valid category term
	@Test
	public void readValidCategoryTermExpectSuccess200(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateCategoryTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateCategoryTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/term/create?category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
		
		// Validate the created category term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/framework/v3/category/term/read/"+termId+"?category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
	}
	
	// Read invalid category term
	@Test
	public void readInvalidCategoryTermExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/framework/v3/category/term/read/acdsnjva?category="+categoryId+"").
		then().
		// log().all().
		spec(get404ResponseSpec());
	}
	
	// Read category term with invalid category
	@Test
	public void readCategoryTermWithValidFrameworkInvalidCategoryExpect4xx(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateCategoryTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateCategoryTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/term/create?category="+categoryId+"");
//		then().
		// log().all().
//		spec(get200ResponseSpec());
		
		// Validate the created category term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/framework/v3/category/term/read/"+termId+"?category=asdnlkv").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	// Read category term with blank category and framework and blank category term
	@Test
	public void readBlankCategoryTermWithBlankCategoryFrameworkExpect5xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/framework/v3/category/term/read/?framework=&category=").
		then().
		// log().all().
		spec(get500ResponseSpec());
	}
	
	// Search category terms with valid request
	@Test
	public void searchCategoryTermValidRequestExpectSuccess200(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateCategoryTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/term/create?category="+categoryId+"").
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonSearchCategoryTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/term/search?category="+categoryId+"").
		then().
		//log().all().
		spec(get200ResponseSpec()).
		body("result.terms.status", hasItems("Live"));
	}
	
	// Search category terms with invalid request
	@Test
	public void searchCategoryTermInvalidRequestExpectSuccess200(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("{\"request\":{\"search\":{\"status\":\"Test\"}}}").
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/term/search?category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
	}
	
	// Search category terms with invalid category
	@Test
	public void searchCategoryTermValidFrameworkInvalidCategoryExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonSearchCategoryTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/term/search?category=ajkdvjks").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	
	// Search category term with blank category and framework
	@Test
	public void searchCategoryTermWithBlankCategoryFrameworkExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("").
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/term/search?category=").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	// Create and search category term
	@Test
	public void createAndSearchCategoryTermExpectSuccess200(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateCategoryTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateCategoryTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/term/create?category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
		
		// Search the created category term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("{\"request\":{\"search\":{\"code\":\""+termId+"\", \"status\":\"Live\"}}}").
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/term/search?category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
	}
	
	// Update valid category term
	@Test
	public void updateValidCategoryTermExpectSuccess200(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateCategoryTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateCategoryTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/term/create?category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
		
		// Update the category term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonUpdateCategoryTerm).
		with().
		contentType(JSON).
		when().
		patch("/framework/v3/category/term/update/"+termId+"?category="+categoryId+"").
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		// Read channel and validate
		setURI();
		Response R1 =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
			when().
			get("/framework/v3/category/term/read/"+termId+"?category="+categoryId+"").
			then().
			//log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp1 = R1.jsonPath();
		String description = jp1.get("result.term.description");
		Assert.assertTrue(description.contains("updated description"));
	}
	
	// Update invalid category term(Non-existing)
	@Test
	public void updateInvalidCategoryTermExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("").
		with().
		contentType(JSON).
		when().
		patch("/framework/v3/category/term/update/caskasl?category="+categoryId+"").
		then().
		//log().all().
		spec(get400ResponseSpec());
	}
	
	// Update category term with invalid category
	@Test
	public void updateCategoryTermWithInvalidCategoryExpect4xx(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateCategoryTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateCategoryTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/term/create?category="+categoryId+"");
//		then().
//		log().all().
//		spec(get200ResponseSpec());
		
		// Update the category term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonUpdateCategoryTerm).
		with().
		contentType(JSON).
		when().
		patch("/framework/v3/category/term/update/"+termId+"?category=acksdlfls").
		then().
		//log().all().
		spec(get400ResponseSpec());
	}
	
	// Update category term with blank category
	@Test
	public void updateCategoryTermWithBlankCategoryExpect5xx(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateCategoryTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateCategoryTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/term/create?category="+categoryId+"");
//		then().
//		log().all().
//		spec(get200ResponseSpec());
		
		// Update the category term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonUpdateCategoryTerm).
		with().
		contentType(JSON).
		when().
		patch("/framework/v3/category/term/update/"+termId+"?category=").
		then().
		//log().all().
		spec(get400ResponseSpec());
	}
	
	// Update category term with code
	@Test
	public void updateCategoryTermCodeExpect4xx(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateCategoryTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateCategoryTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/term/create?category="+categoryId+"");
//		then().
//		log().all().
//		spec(get200ResponseSpec());
		
		// Update the category term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("{\"request\":{\"term\":{\"description\":\"LP_FT_category updated description\",\"code\":\"LP_FT_UpdatedCode"+rn+"\"}}}").
		with().
		contentType(JSON).
		when().
		patch("/framework/v3/category/term/update/"+termId+"?category="+categoryId+"").
		then().
		//log().all().
		spec(get400ResponseSpec());
	}
	
	// Retire valid category term
	@Test
	public void retireValidCategoryTermExpectSuccess200(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateCategoryTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateCategoryTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/term/create?category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
		
		// Retire the created category term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/framework/v3/category/term/retire/"+termId+"?category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
		
		// Read and validate the status
		setURI();
		Response R1 =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
			when().
			get("/framework/v3/category/term/read/"+termId+"?category="+categoryId+"").
			then().
			//log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp1 = R1.jsonPath();
		String status = jp1.get("result.term.status");
		Assert.assertTrue(status.equals("Retired"));
	}
	
	// Retire invalid category term
	@Test
	public void retireInvalidCategoryTermExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/framework/v3/category/term/retire/aksjdjv?category="+categoryId+"").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	// Retire category term withinvalid category
	@Test
	public void retireCategoryTermWithInvalidCategoryExpect4xx(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateCategoryTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateCategoryTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/term/create?category="+categoryId+"");
//		then().
//		log().all().
//		spec(get200ResponseSpec());
		
		// Retire the created category term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/framework/v3/category/term/retire/"+termId+"?category=avsdkans").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	// Retire category term with blank category and blank category term
	@Test
	public void retireBlankCategoryTermWithBlankCategoryFrameworkExpect5xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/framework/v3/category/term/retire/?framework=&category=").
		then().
		// log().all().
		spec(get500ResponseSpec());
	}
	
	// Retire retired category term
	@Test
	public void retireRetiredCategoryTermExpectSuccess200(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateCategoryTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateCategoryTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/term/create?category="+categoryId+"");
//		then().
		// log().all().
//		spec(get200ResponseSpec());
		
		// Retire the created category term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/framework/v3/category/term/retire/"+termId+"?category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
		
		// Retire the retired category term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/framework/v3/category/term/retire/"+termId+"?category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
	}
}
