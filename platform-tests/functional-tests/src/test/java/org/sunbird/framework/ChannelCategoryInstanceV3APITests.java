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
@Ignore
public class ChannelCategoryInstanceV3APITests extends BaseTest{
	public String channelId = "Test";
	int rn = generateRandomInt(0, 999999);
	private static String categoryId = "";
	private String jsonCreateChannelCategoryInstance = "";

	String jsonCreateValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_" + rn+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_NFT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
	String jsonUpdateChannelCategoryInstance = "{\"request\":{\"category\":{\"description\":\"LP_FT_category updated description\",\"name\":\"LP_FT_UpdatedName"+rn+"\"}}}";
	String jsonSearchChannelCategoryInstance = "{\"request\":{\"search\":{\"status\":\"Live\"}}}";
	
	@Before
	public void init() {
		if ((StringUtils.isBlank(categoryId)))
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
				// log().all().
				extract().response();
			
		JsonPath jp1 = R.jsonPath();
		categoryId = jp1.get("result.node_id");
	
		jsonCreateChannelCategoryInstance = "{\"request\":{\"category\":{\"name\":\""+categoryId+"\",\"description\":\"Test Channel Category instance\",\"code\":\""+categoryId+"\"}}}";
		}
	
		// Create valid channel category instance
		@Test
		public void createValidChannelCategoryInstanceExpectSuccess200(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateChannelCategoryInstance);
			String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			body(jsonCreateChannelCategoryInstance).
			with().
			contentType(JSON).
			when().
			post("/channel/v3/category/create").
			then().
			// log().all().
			spec(get200ResponseSpec());
			
			// Read and validate the category instance
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			when().
			get("/channel/v3/category/read/"+instanceId).
			then().
			// log().all().
			spec(get200ResponseSpec());
		}

		// Create channel category instance without code
		@Test
		public void createChannelCategoryInstanceWithoutCodeExpect4xx(){
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			body("{\"request\":{\"category\":{\"name\":\""+categoryId+"\",\"description\":\"Test Channel Category instance\",\"code\":\"\"}}}").
			with().
			contentType(JSON).
			when().
			post("/channel/v3/category/create").
			then().
			// log().all().
			spec(get400ResponseSpec());
		}
		
		// Create channel category instance with invalid channel id
		@Test
		public void createChannelCategoryInstanceWithInvalidChannelIdExpect4xx(){
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, "cdsafj", "")).
			body(jsonCreateChannelCategoryInstance).
			with().
			contentType(JSON).
			when().
			post("/channel/v3/category/create").
			then().
			// log().all().
			spec(get400ResponseSpec());
		}
		
		// Create channel category instance without channel id
		@Test
		public void createChannelCategoryInstanceWithBlankChannelIdExpect4xx(){
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body(jsonCreateChannelCategoryInstance).
			with().
			contentType(JSON).
			when().
			post("/channel/v3/category/create").
			then().
			// log().all().
			spec(get400ResponseSpec());
		}
		
		// Create channel category instance with existing code
		@Test
		public void createChannelCategoryInstanceWithExistingCodeExpect4xx(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateChannelCategoryInstance);
			String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			body(jsonCreateChannelCategoryInstance).
			with().
			contentType(JSON).
			when().
			post("/channel/v3/category/create").
			then().
			// log().all().
			spec(get200ResponseSpec());
			
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			body("{\"request\":{\"category\":{\"name\":\""+instanceId+"\",\"description\":\"Test Channel Category instance\",\"code\":\""+instanceId+"\"}}}").
			with().
			contentType(JSON).
			when().
			post("/channel/v3/category/create").
			then().
			// log().all().
			spec(get400ResponseSpec());
		}
		
		// Create channel category instance with invalid path
		@Test
		public void createChannelCategoryInstanceWithInvalidPathExpect4xx(){
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			body(jsonCreateChannelCategoryInstance).
			with().
			contentType(JSON).
			when().
			post("/channel/v3/category/creat").
			then().
			// log().all().
			spec(get400ResponseSpec());
		}
		
		// Read channel category instance 
		@Test
		public void readValidChannelCategoryInstanceExpectSuccess200(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateChannelCategoryInstance);
			String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			body(jsonCreateChannelCategoryInstance).
			with().
			contentType(JSON).
			when().
			post("/channel/v3/category/create").
			then().
			// log().all().
			spec(get200ResponseSpec());
			
			// Read and validate the category instance
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			when().
			get("/channel/v3/category/read/"+instanceId).
			then().
			// log().all().
			spec(get200ResponseSpec());
		}
		
		// Read channel category instance with invalid identifier
		@Test
		public void readInvalidChannelCategoryInstanceExpect4xx(){
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			when().
			get("/channel/v3/category/read/famdsnfkj").
			then().
			// log().all().
			spec(get404ResponseSpec());
		}
		
		// Read channel category instance without identifier
		@Test
		public void readBlankChannelCategoryInstanceExpect5xx(){
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			when().
			get("/channel/v3/category/read/").
			then().
			// log().all().
			spec(get500ResponseSpec());
		}
		
		// Read channel category instance with invalid channel id
		@Test
		public void readChannelCategoryInstanceWithInvalidChannelIdExpect4xx(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateChannelCategoryInstance);
			String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			body(jsonCreateChannelCategoryInstance).
			with().
			contentType(JSON).
			when().
			post("/channel/v3/category/create").
			then().
			// log().all().
			spec(get200ResponseSpec());
			
			// Read and validate the category instance
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, "alkvjgs", "")).
			when().
			get("/channel/v3/category/read/"+instanceId).
			then().
			// log().all().
			spec(get400ResponseSpec());
		}
		
		// Read channel category instance without channel id
		@Test
		public void readChannelCategoryInstanceWithoutChannelIdExpect4xx(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateChannelCategoryInstance);
			String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			body(jsonCreateChannelCategoryInstance).
			with().
			contentType(JSON).
			when().
			post("/channel/v3/category/create").
			then().
			// log().all().
			spec(get200ResponseSpec());
			
			// Read and validate the category instance
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, "", "")).
			when().
			get("/channel/v3/category/read/"+instanceId).
			then().
			// log().all().
			spec(get400ResponseSpec());
		}
		
		// Read channel category instance with other object types
		@Test
		public void readChannelCategoryInstanceWithOtherObjTypesExpect4xx(){
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
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			when().
			get("/channel/v3/category/read/"+nodeId).
			then().
			// log().all().
			spec(get400ResponseSpec());
			
		}
		
		// Update channel category instance 
		@Test
		public void updateValidChannelCategoryInstanceExpectSuccess200(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateChannelCategoryInstance);
			String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			body(jsonCreateChannelCategoryInstance).
			with().
			contentType(JSON).
			when().
			post("/channel/v3/category/create").
			then().
			// log().all().
			spec(get200ResponseSpec());
			
			// Update framework category instance
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			body(jsonUpdateChannelCategoryInstance).
			with().
			contentType(JSON).
			when().
			patch("/channel/v3/category/update/"+instanceId).
			then().
			//log().all().
			spec(get200ResponseSpec());
			
			// Read and validate the category instance
			setURI();
			Response R1 = 
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			when().
			get("/channel/v3/category/read"+instanceId).
			then().
			// log().all().
			spec(get200ResponseSpec()).
			extract().response();
			
			JsonPath jp1 = R1.jsonPath();
			String description = jp1.get("result.category.description");
			Assert.assertTrue(description.contains("updated description"));
		}
		
		// Update channel category instance with invalid identifier
		@Test
		public void updateInvalidChannelCategoryInstanceExpect4xx(){
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			body(jsonUpdateChannelCategoryInstance).
			with().
			contentType(JSON).
			when().
			patch("/channel/v3/category/update/laksng").
			then().
			//log().all().
			spec(get400ResponseSpec());
		}
		
		// Update channel category instance with code
		@Test
		public void updateChannelCategoryInstanceWithCodeExpect4xx(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateChannelCategoryInstance);
			String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			body(jsonCreateChannelCategoryInstance).
			with().
			contentType(JSON).
			when().
			post("/channel/v3/category/create").
			then().
			// log().all().
			spec(get200ResponseSpec());
			
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			body("{\"request\":{\"category\":{\"name\":\""+instanceId+"\",\"description\":\"Test framework category instance\",\"code\":\"TestCode_"+rn+"\"}}}").
			with().
			contentType(JSON).
			when().
			patch("/channel/v3/category/update/laksng").
			then().
			//log().all().
			spec(get400ResponseSpec());
		}
		
		// Update channel category instance with invalid channel id
		@Test
		public void updateChannelCategoryInstanceWithInvalidChannelIdExpect4xx(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateChannelCategoryInstance);
			String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			body(jsonCreateChannelCategoryInstance).
			with().
			contentType(JSON).
			when().
			post("/channel/v3/category/create").
			then().
			// log().all().
			spec(get200ResponseSpec());
			
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, "adkjsv", "")).
			body("{\"request\":{\"category\":{\"name\":\""+instanceId+"\",\"description\":\"Test framework category instance\",\"code\":\"TestCode_"+rn+"\"}}}").
			with().
			contentType(JSON).
			when().
			patch("/channel/v3/category/update/"+instanceId).
			then().
			//log().all().
			spec(get400ResponseSpec());
		}
		
		// Update channel category instance with blank channel id
		@Test
		public void updateChannelCategoryInstanceWithBlankChannelIdExpect4xx(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateChannelCategoryInstance);
			String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			body(jsonCreateChannelCategoryInstance).
			with().
			contentType(JSON).
			when().
			post("/channel/v3/category/create").
			then().
			// log().all().
			spec(get200ResponseSpec());
			
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body("{\"request\":{\"category\":{\"name\":\""+instanceId+"\",\"description\":\"Test framework category instance\",\"code\":\"TestCode_"+rn+"\"}}}").
			with().
			contentType(JSON).
			when().
			patch("/channel/v3/category/update/"+instanceId).
			then().
			//log().all().
			spec(get400ResponseSpec());
		}
		
		// Search channel category instance with valid request
		@Test
		public void searchChannelCategoryInstanceWithValidRequestExpectSuccess200(){
			setURI();
//			JSONObject js = new JSONObject(jsonCreateChannelCategoryInstance);
//			String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			body(jsonCreateChannelCategoryInstance).
			with().
			contentType(JSON).
			when().
			post("/channel/v3/category/create").
			then().
			// log().all().
			spec(get200ResponseSpec());
			
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			body(jsonSearchChannelCategoryInstance).
			with().
			contentType(JSON).
			when().
			post("/channel/v3/category/search").
			then().
			// log().all().
			spec(get200ResponseSpec()).
			body("result.terms.status", hasItems("Live"));
		}
		
		// Search channel category instance with invalid request
		@Test
		public void searchChannelCategoryInstanceWithInvalidRequestExpect4xx(){
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			body("{\"request\":{\"search\":{\"status\":\"Test\"}}}").
			with().
			contentType(JSON).
			when().
			post("/channel/v3/category/search").
			then().
			// log().all().
			spec(get200ResponseSpec());
		}
		
		// Search channel category instance with invalid channel id
		@Test
		public void searchChannelCategoryInstanceWithInvalidChannelIdExpect4xx(){
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, "akshd", "")).
			body(jsonSearchChannelCategoryInstance).
			with().
			contentType(JSON).
			when().
			post("/channel/v3/category/search").
			then().
			// log().all().
			spec(get400ResponseSpec());
		}
		
		// Search created channel category instance
		@Test
		public void searchCreatedChannelCategoryInstanceExpectSuccess200(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateChannelCategoryInstance);
			String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			body(jsonCreateChannelCategoryInstance).
			with().
			contentType(JSON).
			when().
			post("/channel/v3/category/create").
			then().
			// log().all().
			spec(get200ResponseSpec());
			
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
			body("{\"request\":{\"search\":{\"code\":\""+instanceId+"\", \"status\":\"Live\"}}}").
			with().
			contentType(JSON).
			when().
			post("/channel/v3/category/search").
			then().
			// log().all().
			spec(get200ResponseSpec());
		}
		
		// Retire channel category instance
		@Test
		public void retireValidChannelCategoryInstanceExpectSuccess200(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateChannelCategoryInstance);
			String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			body(jsonCreateChannelCategoryInstance).
			with().
			contentType(JSON).
			when().
			post("/channel/v3/category/create").
			then().
			// log().all().
			spec(get200ResponseSpec());
			
			// Retire the category instance
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			when().
			delete("/channel/v3/category/retire/"+instanceId).
			then().
			// log().all().
			spec(get200ResponseSpec());
			
			// Read and validate the status
			setURI();
			Response R1 =
				given().
				spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
				when().
				get("/channel/v3/category/read/"+instanceId).
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().response();
			
			JsonPath jp1 = R1.jsonPath();
			String status = jp1.get("result.category.status");
			Assert.assertTrue(status.equals("Retired"));
		}
		
		// Retire invalid channel category instance
		@Test
		public void retireInvalidChannelCategoryInstanceExpect4xx(){
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			when().
			delete("/channel/v3/category/retire/adjsfi").
			then().
			// log().all().
			spec(get404ResponseSpec());
		}
		
		// Retire blank channel category instance
		@Test
		public void retireBlankChannelCategoryInstanceExpect5xx(){
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			when().
			delete("/channel/v3/category/retire/").
			then().
			// log().all().
			spec(get500ResponseSpec());
		}
		
		// Retire channel category instance with invalid channel id
		@Test
		public void retireChannelCategoryInstanceWithInvalidChannelIdExpect4xx(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateChannelCategoryInstance);
			String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			body(jsonCreateChannelCategoryInstance).
			with().
			contentType(JSON).
			when().
			post("/channel/v3/category/create").
			then().
			// log().all().
			spec(get200ResponseSpec());
			
			// Retire the category instance
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, "ajfajk", "")).
			when().
			delete("/channel/v3/category/retire/"+instanceId).
			then().
			// log().all().
			spec(get400ResponseSpec());
		}
		
		// Retire channel category instance without channel id
		@Test
		public void retireChannelCategoryInstanceWithoutChannelIdExpect4xx(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateChannelCategoryInstance);
			String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			body(jsonCreateChannelCategoryInstance).
			with().
			contentType(JSON).
			when().
			post("/channel/v3/category/create").
			then().
			// log().all().
			spec(get200ResponseSpec());
			
			// Retire the category instance
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			when().
			delete("/channel/v3/category/retire/"+instanceId).
			then().
			// log().all().
			spec(get200ResponseSpec());
		}
		
		// Retire channel category instance with other object types identifier
		@Test
		public void retireChannelCategoryInstanceWithOtherObjTypesExpect4xx(){
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
			
			// Retire the object
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			when().
			get("/channel/v3/category/retire/"+nodeId).
			then().
			// log().all().
			spec(get400ResponseSpec());
		}
		
		// Retire already retired channel category instance
		@Test
		public void retireRetiredChannelCategoryInstanceExpectSuccess200(){
			setURI();
			JSONObject js = new JSONObject(jsonCreateChannelCategoryInstance);
			String instanceId = js.getJSONObject("request").getJSONObject("category").get("code").toString();		
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			body(jsonCreateChannelCategoryInstance).
			with().
			contentType(JSON).
			when().
			post("/channel/v3/category/create").
			then().
			// log().all().
			spec(get200ResponseSpec());
			
			// Retire the category instance
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			when().
			delete("/channel/v3/category/retire/"+instanceId).
			then().
			// log().all().
			spec(get200ResponseSpec());
			
			// Retire the retired category instance
			setURI();
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId, "")).
			when().
			delete("/channel/v3/category/retire/"+instanceId).
			then().
			// log().all().
			spec(get200ResponseSpec());
		}	
}


