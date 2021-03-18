
package org.sunbird.framework;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.hasItems;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.platform.domain.BaseTest;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.testng.Assert;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;


/**
 * @author Vignesh
 *
 */
public class FrameworkTermsV3APITests extends BaseTest {
	int rn = generateRandomInt(0, 999999);
	
	String jsonCreateValidFramework = "{\"request\":{\"framework\":{\"name\":\"LP_FT_Framework_"+ rn +"\",\"description\":\"LP_FT_Framework\",\"code\":\"LP_FT_Framework_"+ rn +"\"}}}";
	String jsonCreateFrameworkTerm = "{\"request\":{\"term\":{\"name\":\"LP_FWTerm_"+rn+"\",\"code\":\"LP_FWTerm_"+rn+"\",\"description\":\"LP_FWTerm_\"}}}";
	String jsonSearchFrameworkTerm = "{\"request\":{\"search\":{\"status\":\"Live\"}}}";
	String jsonUpdateFrameworkTerm = "{\"request\":{\"term\":{\"description\":\"LP_FT_category updated description\",\"name\":\"LP_FT_UpdatedName"+rn+"\"}}}";
	private static String categoryId = "";
	private static String frameworkId = "";
	
	@Before
	public void init() {
		if ((StringUtils.isBlank(frameworkId)) && (StringUtils.isBlank(categoryId)))
			createCategory();
			//System.out.println(frameworkId);
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
				extract().response();
		
		JsonPath jp1 = R.jsonPath();
		categoryId = jp1.get("result.node_id");
		
		setURI();
		Response R1 =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(jsonCreateValidFramework).
				with().
				contentType(JSON).
				when().
				post("/framework/v3/create").
				then().
				extract().
				response();
		
		JsonPath jp2 = R1.jsonPath();
		frameworkId = jp2.get("result.node_id");
		
		// Create framework category instance
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("{\"request\":{\"category\":{\"name\":\""+categoryId+"\",\"description\":\"Test framework category instance\",\"code\":\""+categoryId+"\"}}}").
		with().
		contentType(JSON).
		then().
		post("/framework/v3/category/create?framework="+frameworkId);
	}

	// Create and validate valid framework term
	@Test
	public void createValidFrameworkTermExpectSuccess200(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/create?framework="+frameworkId+"&category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
		
		// Validate the created framework term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/framework/v3/term/read/"+termId+"?framework="+frameworkId+"&category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
	}
	
	// Create framework term with blank category and framework
	@Test
	public void createFrameworkTermWithBlankCategoryAndFrameworkExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/create?framework=&category=").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	// Create framework term with valid category and invalid framework
	@Test
	public void createFrameworkTermWithValidCategoryInvalidFrameworkExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/create?framework=avdhvsafd&category="+categoryId+"").
		then().
		// log().all().
		spec(get400ResponseSpec());

	}
	
	// Create framework term with valid framework and invalid category
	@Test
	public void createFrameworkTermValidFrameworkInvalidCategoryExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/create?framework="+frameworkId+"&category=lkvpasdgs").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	// Create framework term without code
	@Test
	public void createFrameworkTermWithoutCodeExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("{\"request\":{\"term\":{\"name\":\"LP_FWTerm_"+rn+"\",\"code\":\"\",\"description\":\"LP_FWTerm_\"}}}").
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/create?framework="+frameworkId+"&category="+categoryId+"").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	// Create existing frameworkTerm
	@Test
	public void createExistingFrameworkTermExpect4xx(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/create?framework="+frameworkId+"&category="+categoryId+"");
//		then().
//		log().all();
//		spec(get200ResponseSpec());
		
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("{\"request\":{\"term\":{\"name\":\""+termId+"\",\"code\":\""+termId+"\",\"description\":\"LP_FWTerm_\"}}}").
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/create?framework="+frameworkId+"&category="+categoryId+"").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	// Create framework term without channel Id
	@Test
	public void createFrameworkTermWithoutChannelIdExpect200(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonCreateFrameworkTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/create?framework="+frameworkId+"&category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
	}
	
	// Read valid framework term
	@Test
	public void readValidFrameworkTermExpectSuccess200(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/create?framework="+frameworkId+"&category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
		
		// Validate the created framework term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/framework/v3/term/read/"+termId+"?framework="+frameworkId+"&category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
	}
	
	// Read invalid framework term
	@Test
	public void readInvalidFrameworkTermExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/framework/v3/term/read/acdsnjva?framework="+frameworkId+"&category="+categoryId+"").
		then().
		// log().all().
		spec(get404ResponseSpec());
	}
	
	// Read framework term with valid framework and invalid category
	@Test
	public void readFrameworkTermWithValidFrameworkInvalidCategoryExpect4xx(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/create?framework="+frameworkId+"&category="+categoryId+"");
//		then().
//		log().all();
//		spec(get200ResponseSpec());
		
		// Validate the created framework term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/framework/v3/term/read/"+termId+"?framework="+frameworkId+"&category=asdnlkv").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	// Read framework term with valid category and invalid framework
	@Test
	public void readFrameworkTermWithValidCategoryInvalidFrameworkExpect4xx(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/create?framework="+frameworkId+"&category="+categoryId+"");
//		then().
//		log().all();
//		spec(get200ResponseSpec());
		
		// Validate the created framework term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/framework/v3/term/read/"+termId+"?framework=adsnjva&category="+categoryId+"").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	// Read framework term with blank category and framework and blank framework term
	@Test
	public void readBlankFrameworkTermWithBlankCategoryFrameworkExpect5xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/framework/v3/term/read/?framework=&category=").
		then().
		// log().all().
		spec(get500ResponseSpec());
	}
	
	// Search framework terms with valid request
	@Test
	public void searchFrameworkTermValidRequestExpectSuccess200(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/create?framework="+frameworkId+"&category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
	
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonSearchFrameworkTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/search?framework="+frameworkId+"&category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec()).
		body("result.terms.status", hasItems("Live"));
	}
	
	// Search framework terms with invalid request
	@Test
	public void searchFrameworkTermInvalidRequestExpectSuccess200(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("{\"request\":{\"search\":{\"status\":\"Test\"}}}").
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/search?framework="+frameworkId+"&category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
	}
	
	// Search framework terms with valid framework and invalid category
	@Test
	public void searchFrameworkTermValidFrameworkInvalidCategoryExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/create?framework="+frameworkId+"&category="+categoryId+"");
//		then().
//		log().all();
//		spec(get200ResponseSpec());
		
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonSearchFrameworkTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/search?framework="+frameworkId+"&category=ajkdvjks").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	// Search framework terms with valid category and invalid framework
	@Test
	public void searchFrameworkTermValidCategoryInvalidFrameworkExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/create?framework="+frameworkId+"&category="+categoryId+"");
//		then().
//		log().all();
//		spec(get200ResponseSpec());
		
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonSearchFrameworkTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/search?framework=asdkska&category="+categoryId+"").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	// Search framework term with blank category and framework
	@Test
	public void searchFrameworkTermWithBlankCategoryFrameworkExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonSearchFrameworkTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/search?framework=&category=").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	// Create and search framework term
	@Test
	public void createAndSearchFrameworkTermExpectSuccess200(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/create?framework="+frameworkId+"&category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
		
		// Search the created framework term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("{\"request\":{\"search\":{\"code\":\""+termId+"\", \"status\":\"Live\"}}}").
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/search?framework="+frameworkId+"&category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
	}
	
	// Update valid framework term
	@Test
	public void updateValidFrameworkTermExpectSuccess200(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/create?framework="+frameworkId+"&category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
		
		// Update the framework term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonUpdateFrameworkTerm).
		with().
		contentType(JSON).
		when().
		patch("/framework/v3/term/update/"+termId+"?framework="+frameworkId+"&category="+categoryId+"").
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		// Read channel and validate
		setURI();
		Response R1 =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
			when().
			get("/framework/v3/term/read/"+termId+"?framework="+frameworkId+"&category="+categoryId+"").
			then().
			//log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp1 = R1.jsonPath();
		String description = jp1.get("result.term.description");
		Assert.assertTrue(description.contains("updated description"));
	}
	
	// Update invalid framework term(Non-existing)
	@Test
	public void updateInvalidFrameworkTermExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonUpdateFrameworkTerm).
		with().
		contentType(JSON).
		when().
		patch("/framework/v3/term/update/caskasl?framework="+frameworkId+"&category="+categoryId+"").
		then().
		//log().all().
		spec(get400ResponseSpec());
	}
	
	// Update framework term with valid framework and invalid category
	@Test
	public void updateFrameworkTermWithValidFrameworkInvalidCategoryExpect4xx(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/create?framework="+frameworkId+"&category="+categoryId+"");
//		then().
//		log().all();
//		spec(get200ResponseSpec());
		
		// Update the framework term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonUpdateFrameworkTerm).
		with().
		contentType(JSON).
		when().
		patch("/framework/v3/term/update/"+termId+"?framework="+frameworkId+"&category=acksdlfls").
		then().
		//log().all().
		spec(get400ResponseSpec());
	}
	
	// Update framework term with valid category and invalid framework
	@Test
	public void updateFrameworkTermWithValidCategoryInvalidFrameworkExpect4xx(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/create?framework="+frameworkId+"&category="+categoryId+"");
//		then().
//		log().all();
//		spec(get200ResponseSpec());
		
		// Update the framework term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("").
		with().
		contentType(JSON).
		when().
		patch("/framework/v3/term/update/"+termId+"?framework=asdfaoisdf&category="+categoryId+"").
		then().
		//log().all().
		spec(get400ResponseSpec());
	}
	
	// Update framework term with blank category and framework
	@Test
	public void updateFrameworkTermWithBlankCategoryFrameworkExpect4xx(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/create?framework="+frameworkId+"&category="+categoryId+"");
//		then().
//		log().all();
//		spec(get200ResponseSpec());
		
		// Update the framework term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("").
		with().
		contentType(JSON).
		when().
		patch("/framework/v3/term/update/"+termId+"?framework=&category=").
		then().
		//log().all().
		spec(get400ResponseSpec());
	}
	
	// Update framework term with code
	@Test
	public void updateFrameworkTermCodeExpect4xx(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/create?framework="+frameworkId+"&category="+categoryId+"");
//		then().
//		log().all();
//		spec(get200ResponseSpec());
		
		// Update the framework term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("{\"request\":{\"term\":{\"description\":\"LP_FT_category updated description\",\"code\":\"LP_FT_UpdatedCode"+rn+"\"}}}").
		with().
		contentType(JSON).
		when().
		patch("/framework/v3/term/update/"+termId+"?framework="+frameworkId+"&category="+categoryId+"").
		then().
		//log().all().
		spec(get400ResponseSpec());
	}
	
	// Retire valid framework term
	@Test
	public void retireValidFrameworkTermExpectSuccess200(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/create?framework="+frameworkId+"&category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
		
		// Retire the created framework term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/framework/v3/term/retire/"+termId+"?framework="+frameworkId+"&category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
		
		// Read and validate the status
		setURI();
		Response R1 =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
			when().
			get("/framework/v3/term/read/"+termId+"?framework="+frameworkId+"&category="+categoryId+"").
			then().
			//log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp1 = R1.jsonPath();
		String status = jp1.get("result.term.status");
		Assert.assertTrue(status.equals("Retired"));
	}
	
	// Retire invalid framework term
	@Test
	public void retireInvalidFrameworkTermExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/framework/v3/term/retire/aksjdjv?framework="+frameworkId+"&category="+categoryId+"").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	// Retire framework term with valid framework and invalid category
	@Test
	public void retireFrameworkTermWithValidFrameworkInvalidCategoryExpect4xx(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/create?framework="+frameworkId+"&category="+categoryId+"");
//		then().
//		log().all();
//		spec(get200ResponseSpec());
		
		// Retire the created framework term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/framework/v3/term/retire/"+termId+"?framework="+frameworkId+"&category=avsdkans").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	// Retire framework term with valid category and invalid framework
	@Test
	public void retireFrameworkTermWithValidCategoryInvalidFrameworkExpect4xx(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkTerm).
		with().
		contentType(JSON).
		then().
		post("/framework/v3/term/create?framework="+frameworkId+"&category="+categoryId+"");
//		then().
//		log().all();
//		spec(get200ResponseSpec());
		
		// Retire the created framework term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/framework/v3/term/retire/"+termId+"?framework=vakdslvn&category="+categoryId+"").
		then().
		// log().all().
		spec(get400ResponseSpec());
	}
	
	// Retire framework term with blank category and framework and blank framework term
	@Ignore
	@Test
	public void retireBlankFrameworkTermWithBlankCategoryFrameworkExpect5xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/framework/v3/term/retire/?framework=&category=").
		then().
		// log().all().
		spec(get500ResponseSpec());
	}
	
	// Retire retired framework term
	@Test
	public void retireRetiredFrameworkTermExpectSuccess200(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkTerm);
		String termId = js.getJSONObject("request").getJSONObject("term").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkTerm).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/term/create?framework="+frameworkId+"&category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
		
		// Retire the created framework term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/framework/v3/term/retire/"+termId+"?framework="+frameworkId+"&category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
		
		// Retire the retired framework term
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/framework/v3/term/retire/"+termId+"?framework="+frameworkId+"&category="+categoryId+"").
		then().
		// log().all().
		spec(get200ResponseSpec());
	}
}
