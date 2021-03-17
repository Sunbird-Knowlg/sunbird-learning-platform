package org.sunbird.framework;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.hasItems;

import org.sunbird.platform.domain.BaseTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class FrameworkAPITest extends BaseTest{

	int rn = generateRandomInt(0, 999999);
	public String channelId = "in.ekstep";
	public String invalidChannelId = "in.ekstep.invalid";
	String jsonCreateValidFramework = "{\"request\":{\"framework\":{\"name\":\"LP_FT_Framework_"+ rn +"\",\"description\":\"FT_Framework\",\"code\":\"LP_FT_Framework_"+ rn +"\",\"translations\":{\"hi\":\"टेस्ट फ़्रेम्वर्क\",\"ka\":\"ೂಾೇೂ ಿೀೋಸಾೈದೀಕ\"}}}";
	String jsonUpdateFrameworkRequest = "{\"request\":{\"framework\":{\"description\":\"LP_FT_category updated description\",\"name\":\"LP_FT_UpdatedName"+rn+"\"}}}";
	String jsonSearchFrameworkRequest = "{\"request\":{\"search\":{\"status\":\"Live\"}}}";
	//String jsonCreateValidFramework = "{\"request\":{\"framework\":{\"name\":\"LP_FR_"+ rn +"\",\"description\":\"LP_FR\",\"code\":\"LP_FR_"+ rn +"\",\"channels\":[{\"identifier\":\"Test\"}]}}}";
	
	// Create and get Framework
	@Test
	public void createValidFrameworkExpectSuccess200(){
		setURI();
		Response R =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(jsonCreateValidFramework).
				with().
				contentType(JSON).
				when().
				post("/framework/v3/create").
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String frameworkNode = jp.get("result.node_id");
		
		// Get Framework and validate
		setURI();
		Response R1 =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				when().
				get("/framework/v3/read/"+frameworkNode).
				then().
				spec(get200ResponseSpec()).
				log().all().
				extract().
				response();

		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.framework.identifier");
		String versionKey = jP1.get("result.framework.versionKey");
		Assert.assertTrue(versionKey!=null);
		Assert.assertEquals(frameworkNode, identifier);
	}
		
	// Create Framework without channelId
	@Test
	public void createValidFrameworkWithNoChannelIdExpectFail400(){
		setURI();
		given().spec(getRequestSpecification(contentType, validuserId, APIToken)).
		body(jsonCreateValidFramework).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/create").
		then().
		spec(get400ResponseSpec()).
		extract().
		response();
	}
	
	// Create Framework with invalid channelId
	@Test
	public void createValidFrameworkWithInvalidChannelIdExpectFail400(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken, invalidChannelId, appId)).
		body(jsonCreateValidFramework).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/create").
		then().
		spec(get400ResponseSpec()).
		extract().
		response();
	}
	
	// Create existing framework
	@Test
	public void createExistingFrameworkExpectSuccess400(){
		setURI();
		Response R =
				given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(jsonCreateValidFramework).
				with().
				contentType(JSON).
				when().
				post("/framework/v3/create").
				then().
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String frameworkNode = jp.get("result.node_id");
		System.out.println(frameworkNode);
		
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
		body("{\"request\":{\"framework\":{\"name\":\""+ frameworkNode +"\",\"description\":\"LP_FR\",\"code\":\""+ frameworkNode +"\"}}}").
		with().
		contentType(JSON).
		when().
		post("/framework/v3/create").
		then().
		spec(get400ResponseSpec()).
		extract().
		response();
	}
	
	// Create framework without code
	@Test
	public void createFrameworkWithoutCodeExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
		body("{\"request\":{\"framework\":{\"name\":\"LP_FT_Framework_"+ rn +"\",\"description\":\"LP_FR\",\"code\":\"\"}}}").
		with().
		contentType(JSON).
		when().
		post("/framework/v3/create").
		then().
		spec(get400ResponseSpec()).
		extract().
		response();
	}
	
	// Read valid frameworkId
	@Test
	public void readValidFrameworkExpectSuccess200(){
		setURI();
		Response R =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(jsonCreateValidFramework).
				with().
				contentType(JSON).
				when().
				post("/framework/v3/create").
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String frameworkNode = jp.get("result.node_id");
		
		//Publish Framework
		String publishReq="{}";
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
		body(publishReq).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/publish").
		then().
		spec(get200ResponseSpec());

		// Get Framework and validate
		setURI();
		Response R1 =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				when().
				get("/framework/v3/read/"+frameworkNode).
				then().
				spec(get200ResponseSpec()).
				log().all().
				extract().
				response();

		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.framework.identifier");
		String versionKey = jP1.get("result.framework.versionKey");
		String code=jP1.getString("result.framework.code");
		String desc=jP1.getString("result.framework.description");
		Assert.assertTrue(versionKey!=null);
		Assert.assertEquals(frameworkNode, identifier);
		Assert.assertEquals(frameworkNode, code);
		Assert.assertEquals("FT_Framework", desc);
		Assert.assertEquals("टेस्ट फ़्रेम्वर्क", jP1.getString("result.framework.translations.hi"));
		Assert.assertEquals("ೂಾೇೂ ಿೀೋಸಾೈದೀಕ", jP1.getString("result.framework.translations.ka"));
	}
	
	// Read blank framework
	@Test
	public void readBlankFrameworkExpect5xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken)).
		when().
		get("/framework/v3/read/").
		then().
		spec(get500ResponseSpec());
	}
	
	// Read invalid framework
	@Test
	public void readInvalidFrameworkExpect404(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken)).
		when().
		get("/framework/v3/read/F;NDSAF").
		then().
		spec(get404ResponseSpec());
	}
	
	// Update valid framework
	@Ignore
	@Test
	public void updateValidFrameworkExpectSuccess200(){
		setURI();
		Response R =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(jsonCreateValidFramework).
				with().
				contentType(JSON).
				when().
				post("/framework/v3/create").
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String frameworkNode = jp.get("result.node_id");
		
		// Update metadata
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId, appId)).
		body(jsonUpdateFrameworkRequest).
		with().
		contentType(JSON).
		when().
		patch("/framework/v3/update/"+frameworkNode).
		then().
		log().all().
		spec(get200ResponseSpec());
		
		// Read the framework and validate
		setURI();
		Response R1 =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken, channelId)).
			when().
			get("/framework/v3/read/"+frameworkNode).
			then().
			//log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jp1 = R1.jsonPath();
		String description = jp1.get("result.category.description");
		Assert.assertTrue(description.contains("updated description"));
	}
	
	// Update invalid framework
	@Ignore
	@Test
	public void updateInvalidFrameworkExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId, appId)).
		body("{\"request\":{\"category\":{\"description\":\"LP_FT_category updated description\",\"name\":\"LP_FT_UpdatedName"+rn+"\"}}}").
		with().
		contentType(JSON).
		when().
		patch("/framework/v3/update/akjsdbk").
		then().
		log().all().
		spec(get400ResponseSpec());
	}
	
	// Update framework code
	@Ignore
	@Test
	public void updateFrameworkCodeExpect4xx(){
		setURI();
		Response R =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(jsonCreateValidFramework).
				with().
				contentType(JSON).
				when().
				post("/framework/v3/create").
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String frameworkNode = jp.get("result.node_id");
		
		// Update metadata
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId, appId)).
		body("{\"request\":{\"framework\":{\"description\":\"LP_FT_category updated description\",\"code\":\"LP_FT_UpdatedCode"+rn+"\"}}}").
		with().
		contentType(JSON).
		when().
		patch("/framework/v3/update/"+frameworkNode).
		then().
		log().all().
		spec(get400ResponseSpec());
	}
	
	// Update framework without channel id
	@Test
	public void updateFrameworkWithoutChannelIdExpect4xx(){
		setURI();
		Response R =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(jsonCreateValidFramework).
				with().
				contentType(JSON).
				when().
				post("/framework/v3/create").
				then().
				log().all().
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String frameworkNode = jp.get("result.node_id");
		
		// Update metadata
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonUpdateFrameworkRequest).
		with().
		contentType(JSON).
		when().
		patch("/framework/v3/update/"+frameworkNode).
		then().
		log().all().
		spec(get400ResponseSpec());
	}
	
	// Update framework with invalid channelId
	@Test
	public void updateFrameworkWithInvalidChannelExpect4xx(){
		setURI();
		Response R =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(jsonCreateValidFramework).
				with().
				contentType(JSON).
				when().
				post("/framework/v3/create").
				then().
				log().all().
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String frameworkNode = jp.get("result.node_id");
		
		// Update metadata
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, "newChannel")).
		body(jsonUpdateFrameworkRequest).
		with().
		contentType(JSON).
		when().
		patch("/framework/v3/update/"+frameworkNode).
		then().
		log().all().
		spec(get400ResponseSpec());
	}
	
	// Search framework with valid request
	@Test
	public void searchFrameworkWithValidReqExpectSuccess200(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId, appId)).
		body(jsonSearchFrameworkRequest).
		with().
		contentType(JSON).
		when().
		post("/framework/v3/list/").
		then().
		log().all().
		spec(get200ResponseSpec()).
		body("result.frameworks.status", hasItems("Live"));
	}
	
	// Search framework with invalid request
	@Test
	public void searchFrameworkWithInvalidReqExpectSuccess200(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId, appId)).
		body("{\"request\":{\"search\":{\"status\":\"Test\"}}}").
		with().
		contentType(JSON).
		when().
		post("/framework/v3/list/").
		then().
		log().all().
		spec(get200ResponseSpec());
	}
	
	// Create and search framework
	@Test
	public void searchCreatedFrameworkExpectSuccess200(){
		setURI();
		Response R =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(jsonCreateValidFramework).
				with().
				contentType(JSON).
				when().
				post("/framework/v3/create").
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String frameworkNode = jp.get("result.node_id");
		
		// Search created framework
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken, channelId, appId)).
		body("{\"request\":{\"search\":{\"code\":\""+frameworkNode+"\", \"status\":\"Live\"}}}").
		with().
		contentType(JSON).
		when().
		post("/framework/v3/list/").
		then().
		log().all().
		spec(get200ResponseSpec());
	}
	
	// Retire valid framework
	@Ignore
	@Test
	public void retireValidFrameworkExpectSuccess200(){
		setURI();
		Response R =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(jsonCreateValidFramework).
				with().
				contentType(JSON).
				when().
				post("/framework/v3/create").
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String frameworkNode = jp.get("result.node_id");

		// Retire created framework
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken)).
		when().
		delete("/framework/v3/retire/"+frameworkNode).
		then().
		log().all().
		spec(get200ResponseSpec());
		
		// Get Framework and validate
		setURI();
		Response R1 =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken)).
				when().
				get("/framework/v3/read/"+frameworkNode).
				then().
				spec(get200ResponseSpec()).
				log().all().
				extract().
				response();

		JsonPath jP1 = R1.jsonPath();
		String status = jP1.get("result.framework.status");
		Assert.assertEquals(status, "Retired");
	}
	
	// Retire invalid framework
	@Ignore
	@Test
	public void retireInvalidFrameworkExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken)).
		when().
		delete("/framework/v3/retire/avlndsui").
		then().
		log().all().
		spec(get404ResponseSpec());	
	}
	
	// Retire blank framework
	@Test
	public void retireBlankFrameworkExpect5xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken)).
		when().
		delete("/framework/v3/retire/").
		then().
		log().all().
		spec(get500ResponseSpec());	
	}
	
	// Retire retired framework
	@Ignore
	@Test
	public void retireRetiredFrameworkExpectSuccess200(){
		setURI();
		Response R =
				given().
				spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
				body(jsonCreateValidFramework).
				with().
				contentType(JSON).
				when().
				post("/framework/v3/create").
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		// Extracting the JSON path
		JsonPath jp = R.jsonPath();
		String frameworkNode = jp.get("result.node_id");

		// Retire created framework
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken)).
		when().
		delete("/framework/v3/retire/"+frameworkNode).
		then().
		log().all().
		spec(get200ResponseSpec());
		
		// Retire retired framework
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken)).
		when().
		delete("/framework/v3/retire/"+frameworkNode).
		then().
		log().all().
		spec(get200ResponseSpec());
	}
}
