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
import org.junit.Ignore;
import org.junit.Test;
import org.testng.Assert;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

/**
 * @author Vignesh
 *
 */
public class FrameworkCategoryInstanceV3APITests extends BaseTest {
	
	int rn = generateRandomInt(0, 999999);
	private static String frameworkId = "";
	private static String categoryId = "";
	private String jsonCreateFrameworkCategoryInstance = "";

	String jsonCreateValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_" + rn+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_NFT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
	String jsonCreateValidFramework = "{\"request\":{\"framework\":{\"name\":\"LP_FT_Framework_"+ rn +"\",\"description\":\"LP_FT_Framework\",\"code\":\"LP_FT_Framework_"+ rn +"\"}}}";
	String jsonUpdateFrameworkCategoryInstance = "{\"request\":{\"category\":{\"description\":\"LP_FT_category updated description\",\"name\":\"LP_FT_UpdatedName"+rn+"\"}}}";
	String jsonSearchFrameworkCategoryInstance = "{\"request\":{\"search\":{\"status\":\"Live\"}}}";
	
	@Before
	public void init() {
		if ((StringUtils.isBlank(frameworkId)) && (StringUtils.isBlank(categoryId)))
			createCategory();
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
	
	jsonCreateFrameworkCategoryInstance = "{\"request\":{\"category\":{\"name\":\""+categoryId+"\",\"description\":\"Test framework category instance\",\"code\":\""+categoryId+"\"}}}";
	}	
	
	// Create valid framework category instance
	@Ignore
	@Test
	public void createValidFrameworkCategoryInstanceExpectSuccess200(){		
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkCategoryInstance);
		String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkCategoryInstance).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/create?framework="+frameworkId+"").
		then().
		log().all().
		spec(get200ResponseSpec());
		
		// Read and validate the category instance
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/framework/v3/category/read/"+instanceId+"?framework="+frameworkId+"").
		then().
		log().all().
		spec(get200ResponseSpec());
	}

	// Create framework category instance without code
	@Test
	public void createFrameworkCategoryInstanceWithoutCodeExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("{\"request\":{\"category\":{\"name\":\""+categoryId+"\",\"description\":\"Test framework category instance\",\"code\":\"\"}}}").
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/create?framework="+frameworkId+"").
		then().
		log().all().
		spec(get400ResponseSpec());
	}
	
	// Create framework category instance with invalid framework
	@Test
	public void createFrameworkCategoryInstanceWithInvalidFrameworkExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkCategoryInstance).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/create?framework=ajsbvkdbk").
		then().
		log().all().
		spec(get400ResponseSpec());
	}
	
	// Create framework category instance without framework
	@Test
	public void createFrameworkCategoryInstanceWithBlankFrameworkExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkCategoryInstance).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/create?framework=").
		then().
		log().all().
		spec(get400ResponseSpec());
	}
	
	// Create framework category instance with existing code
	@Ignore
	@Test
	public void createFrameworkCategoryInstanceWithExistingCodeExpect4xx(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkCategoryInstance);
		String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkCategoryInstance).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/create?framework="+frameworkId+"").
		then().
		log().all();
		
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("{\"request\":{\"category\":{\"name\":\""+instanceId+"\",\"description\":\"Test framework category instance\",\"code\":\""+instanceId+"\"}}}").
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/create?framework="+frameworkId+"").
		then().
		log().all().
		spec(get400ResponseSpec());
	}
	
	// Create framework category instance with no channel id
	@Ignore
	@Test
	public void createFrameworkCategoryInstanceWithNoChannelIdExpectSuccess200(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkCategoryInstance);
		String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonCreateFrameworkCategoryInstance).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/create?framework="+frameworkId+"").
		then().
		log().all().
		spec(get200ResponseSpec());
		
		// Read and validate the category instance
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/framework/v3/category/read/"+instanceId+"?framework="+frameworkId+"").
		then().
		log().all().
		spec(get200ResponseSpec());
	}
	
	// Create framework category instance with different objectType as framework
	@Ignore
	@Test
	public void createFrameworkCategoryInstanceWithDiffObjTypeExpect4xx(){
		setURI();
		Response R = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");
		
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkCategoryInstance).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/create?framework="+nodeId+"").
		then().
		log().all().
		spec(get400ResponseSpec());
	}
	
	// Create framework category instance with other object type as category
	@Ignore
	@Test
	public void createFrameworkCategoryInstanceWithDiffObjTypeAsCategoryExpect4xx(){
		setURI();
		Response R = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");
		
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("{\"request\":{\"category\":{\"name\":\""+nodeId+"\",\"description\":\"Test framework category instance\",\"code\":\""+nodeId+"\"}}}").
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/create?framework="+frameworkId+"").
		then().
		log().all().
		spec(get400ResponseSpec());
	}
	
	// Read framework category instance 
	@Ignore
	@Test
	public void readValidFrameworkCategoryInstanceExpectSuccess200(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkCategoryInstance);
		String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkCategoryInstance).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/create?framework="+frameworkId+"").
		then().
		log().all().
		spec(get200ResponseSpec());
		
		// Read and validate the category instance
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/framework/v3/category/read/"+instanceId+"?framework="+frameworkId+"").
		then().
		log().all().
		spec(get200ResponseSpec());
	}
	
	// Read framework category instance with invalid identifier
	@Ignore
	@Test
	public void readInvalidFrameworkCategoryInstanceExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/framework/v3/category/read/al;kds?framework="+frameworkId+"").
		then().
		log().all().
		spec(get404ResponseSpec());
	}
	
	// Read framework category instance without identifier
	@Test
	public void readBlankFrameworkCategoryInstanceExpect5xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/framework/v3/category/read/?framework="+frameworkId+"").
		then().
		log().all().
		spec(get500ResponseSpec());
	}
	
	// Read framework category instance with invalid framework
	@Ignore
	@Test
	public void readFrameworkCategoryInstanceWithInvalidFrameworkExpect4xx(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkCategoryInstance);
		String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkCategoryInstance).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/create?framework="+frameworkId+"").
		then().
		log().all().
		spec(get200ResponseSpec());
		
		// Read and validate the category instance
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/framework/v3/category/read/"+instanceId+"?framework=vasjkhvdka").
		then().
		log().all().
		spec(get200ResponseSpec());
	}
	
	// Read framework category instance without framework
	@Ignore
	@Test
	public void readFrameworkcategoryInstanceWithoutFrameworkExpect4xx(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkCategoryInstance);
		String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkCategoryInstance).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/create?framework="+frameworkId+"").
		then().
		log().all().
		spec(get200ResponseSpec());
		
		// Read and validate the category instance
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/framework/v3/category/read/"+instanceId+"?framework=").
		then().
		log().all().
		spec(get200ResponseSpec());
	}
	
	// Read framework category instance with other object types
	@Ignore
	@Test
	public void readFrameworkCategoryInstanceWithOtherObjTypesExpect4xx(){
		setURI();
		Response R = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateValidContent).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String nodeId = jp.get("result.node_id");
		
		// Read and validate the category instance
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/framework/v3/category/read/"+nodeId+"?framework="+frameworkId+"").
		then().
		log().all().
		spec(get200ResponseSpec());
	}
	
	// Update framework category instance 
	@Ignore
	@Test
	public void updateValidFrameworkCategoryInstanceExpectSuccess200(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkCategoryInstance);
		String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkCategoryInstance).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/create?framework="+frameworkId+"").
		then().
		log().all().
		spec(get200ResponseSpec());
		
		// Update framework category instance
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonUpdateFrameworkCategoryInstance).
		with().
		contentType(JSON).
		when().
		patch("/framework/v3/category/update/"+instanceId+"?framework="+frameworkId+"").
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		// Read and validate the category instance
		setURI();
		Response R1 = 
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		get("/framework/v3/category/read/"+instanceId+"?framework="+frameworkId).
		then().
		log().all().
		spec(get200ResponseSpec()).
		extract().response();
		
		JsonPath jp1 = R1.jsonPath();
		String description = jp1.get("result.category.description");
		Assert.assertTrue(description.contains("updated description"));
	}
	
	// Update framework category instance with invalid identifier
	@Test
	public void updateInvalidFrameworkCategoryInstanceExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonUpdateFrameworkCategoryInstance).
		with().
		contentType(JSON).
		when().
		patch("/framework/v3/category/update/alkdls?framework="+frameworkId+"").
		then().
		//log().all().
		spec(get400ResponseSpec());
	}
	
	// Update framework category instance with code
	@Ignore
	@Test
	public void updateFrameworkCategoryInstanceWithCodeExpect4xx(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkCategoryInstance);
		String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkCategoryInstance).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/create?framework="+frameworkId+"").
		then().
		log().all();
		
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("{\"request\":{\"category\":{\"name\":\""+instanceId+"\",\"description\":\"Test framework category instance\",\"code\":\"TestCode_"+rn+"\"}}}").
		with().
		contentType(JSON).
		when().
		patch("/framework/v3/category/update/"+instanceId+"?framework="+frameworkId+"").
		then().
		log().all().
		spec(get400ResponseSpec());
	}
	
	// Update framework category instance with invalid framework
	@Ignore
	@Test
	public void updateFrameworkCategoryInstanceWithInvalidFrameworkExpect4xx(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkCategoryInstance);
		String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkCategoryInstance).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/create?framework="+frameworkId+"").
		then().
		log().all().
		spec(get200ResponseSpec());
		
		// Update framework category instance
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonUpdateFrameworkCategoryInstance).
		with().
		contentType(JSON).
		when().
		patch("/framework/v3/category/update/"+instanceId+"?framework=akdhvd").
		then().
		//log().all().
		spec(get400ResponseSpec());
	}
	
	// Update framework category instance with blank framework
	@Ignore
	@Test
	public void updateFrameworkCategoryInstanceWithBlankFrameworkExpect4xx(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkCategoryInstance);
		String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkCategoryInstance).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/create?framework="+frameworkId+"").
		then().
		log().all().
		spec(get200ResponseSpec());
		
		// Update framework category instance
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonUpdateFrameworkCategoryInstance).
		with().
		contentType(JSON).
		when().
		patch("/framework/v3/category/update/"+instanceId+"?framework=").
		then().
		//log().all().
		spec(get400ResponseSpec());
	}
	
	// Search framework category instance with valid request
	@Ignore
	@Test
	public void searchFrameworkCategoryInstanceWithValidRequestExpectSuccess200(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkCategoryInstance).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/create?framework="+frameworkId+"").
		then().
		log().all().
		spec(get200ResponseSpec());
		
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonSearchFrameworkCategoryInstance).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/search/?framework="+frameworkId).
		then().
		log().all().
		spec(get200ResponseSpec()).
		body("result.terms.status", hasItems("Live"));
	}
	
	// Search framework category instance with invalid request
	@Ignore
	@Test
	public void searchFrameworkCategoryInstanceWithInvalidRequestExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("{\"request\":{\"search\":{\"status\":\"Test\"}}}").
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/search/?framework="+frameworkId).
		then().
		log().all().
		spec(get200ResponseSpec());
	}
	
	// Search framework category instance with invalid framework
	@Test
	public void searchFrameworkCategoryInstanceWithInvalidFrameworkExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonSearchFrameworkCategoryInstance).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/search/?framework=jvasoid").
		then().
		log().all().
		spec(get400ResponseSpec());
	}
	
	// Search created framework category instance
	@Ignore
	@Test
	public void searchCreatedFrameworkCategoryInstanceExpectSuccess200(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkCategoryInstance);
		String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkCategoryInstance).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/create?framework="+frameworkId+"").
		then().
		log().all().
		spec(get200ResponseSpec());
		
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body("{\"request\":{\"search\":{\"code\":\""+instanceId+"\", \"status\":\"Live\"}}}").
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/search/?framework="+frameworkId).
		then().
		log().all().
		spec(get200ResponseSpec());
	}
	
	// Retire framework category instance
	@Ignore
	@Test
	public void retireValidFrameworkCategoryInstanceExpectSuccess200(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkCategoryInstance);
		String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkCategoryInstance).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/create?framework="+frameworkId+"").
		then().
		log().all().
		spec(get200ResponseSpec());
		
		// Retire and validate the category instance
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/framework/v3/category/retire/"+instanceId+"?framework="+frameworkId+"").
		then().
		log().all().
		spec(get200ResponseSpec());
		
		// Read and validate the status
		setURI();
		Response R1 =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
			when().
			get("/framework/v3/category/read/"+instanceId+"?framework="+frameworkId).
			then().
			//log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp1 = R1.jsonPath();
		String status = jp1.get("result.category.status");
		Assert.assertTrue(status.equals("Retired"));
	}
	
	// Retire framework category instance with invalid framework category instance
	@Ignore
	@Test
	public void retireInvalidFrameworkCategoryInstanceExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/framework/v3/category/retire/vakhsa?framework="+frameworkId+"").
		then().
		log().all().
		spec(get404ResponseSpec());
	}
	
	// Retire blank framework category instance
	@Test
	public void retireBlankFrameworkCategoryInstanceExpect5xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/framework/v3/category/retire/?framework="+frameworkId+"").
		then().
		log().all().
		spec(get500ResponseSpec());
	}
	
	// Retire framework category instance with invalid framework
	@Test
	public void retireFrameworkCategoryInstanceWithInvalidFrameworkExpect4xx(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkCategoryInstance);
		String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkCategoryInstance).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/create?framework="+frameworkId+"");
		
		// Retire and validate the category instance
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/framework/v3/category/retire/"+instanceId+"?framework=vakvsdjf").
		then()
		.spec(get400ResponseSpec());
	}
	
	// Retire framework category instance without framework
	@Ignore
	@Test
	public void retireFrameworkcategoryInstanceWithoutFrameworkExpect4xx(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkCategoryInstance);
		String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkCategoryInstance).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/create?framework="+frameworkId+"");
		
		// Retire and validate the category instance
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/framework/v3/category/retire/"+instanceId+"?framework=").
		then().
		//log().all().
		spec(get400ResponseSpec());
	}
	
	// Retire framework category instance with other object types identifier
	@Ignore
	@Test
	public void retireFrameworkCategoryInstanceWithOtherObjTypesExpect4xx(){
		setURI();
		JSONObject js = new JSONObject(jsonCreateFrameworkCategoryInstance);
		String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		body(jsonCreateFrameworkCategoryInstance).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/category/create?framework="+frameworkId+"").
		then().
		log().all().
		spec(get200ResponseSpec());
		
		// Retire and validate the category instance
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/framework/v3/category/retire/"+instanceId+"?framework="+frameworkId).
		then().
		log().all().
		spec(get200ResponseSpec());
		
		// Retire and validate the category instance
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
		when().
		delete("/framework/v3/category/retire/"+instanceId+"?framework="+frameworkId).
		then().
		log().all().
		spec(get200ResponseSpec());
	}
}
