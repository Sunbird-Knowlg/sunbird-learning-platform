package org.sunbird.platform.domain;

import static com.jayway.restassured.RestAssured.baseURI;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.sunbird.common.Platform;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;
import org.sunbird.platform.content.ContentV3API;
import org.sunbird.platform.test.common.TestConstant;
import org.sunbird.platform.test.common.util.AuthTokenUtil;

public class BaseTest 
{
	public ResponseSpecBuilder builderres = new ResponseSpecBuilder();
	public String liveStatus = "Live";
	public String contentType = "application/json";
	public String UrlContentType = "text/plain";
	public String uploadContentType = "multipart/form-data";
	public String userId = "ilimi";
	public String channelId = "Test";
	public String appId = "test.appId";

	public String APIToken = TestConstant.BEARER + Platform.config.getString("kp_ft_access_key");
	public String validuserId = "functional-tests";
	public String invalidUserId = "abc";

	protected final String IDENTIFIER = "__IDENTIFIER__";
	protected final String VERSION_KEY = "__VERSION_KEY__";

	protected ContentV3API contentV3API = new ContentV3API();

	protected enum ResponseCode {
		CLIENT_ERROR(400), OK(200);

		private int code;

		ResponseCode(int code) { this.code = code; }

		public int code() { return code; }
	}
	
	public void parser(){
		RestAssured.registerParser("text/csv", null);
	}
	
	public void delay(long time){
		try {
			Thread.sleep(time);
		} catch (Exception e) {}
	}
	/**
	 * sets baseURI and basePath
	 */
	public void setURI()
	{
		baseURI = Platform.config.getString("kp_ft_base_uri");
	}
	
		
	/**
	 * adds the given content_type and user_id to the header of RequestSpecBuilder
	 * 
	 * @param content_type - json/xml
	 * @param user_id
	 * @return returns a RequestSpecification object
	 */
	public RequestSpecification getRequestSpec(String content_type,String user_id)
	{
		RequestSpecBuilder builderreq = new RequestSpecBuilder();
		builderreq.addHeader("Content-Type", content_type);
		builderreq.addHeader("user-id", user_id);
		builderreq.addHeaders(getHeaders(true));
		RequestSpecification requestSpec = builderreq.build();
		return requestSpec;
	}
	
	public RequestSpecification getRequestSpecification(String content_type,String user_id, String APIToken)
	{
		RequestSpecBuilder builderreq = new RequestSpecBuilder();
		builderreq.addHeader("Content-Type", content_type);
		builderreq.addHeader("user-id", user_id);
		builderreq.addHeader("Authorization", APIToken);
		builderreq.addHeaders(getHeaders(true));
		RequestSpecification requestSpec = builderreq.build();
		return requestSpec;
	}
	
	public RequestSpecification getRequestSpecification(String content_type,String user_id, String APIToken, String fileUrl)
	{
		RequestSpecBuilder builderreq = new RequestSpecBuilder();
		builderreq.addHeader("user-id", user_id);
		builderreq.addHeader("Authorization", APIToken);
		builderreq.addHeader("fileUrl", fileUrl);
		builderreq.addHeaders(getHeaders(true));
		builderreq.addHeader("Content-Type", content_type);
		RequestSpecification requestSpec = builderreq.build();
		return requestSpec;
	}

	public RequestSpecification getRequestSpec(boolean isAuthRequired) {
		RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder();
		requestSpecBuilder.addHeaders(getHeaders(isAuthRequired));
		RequestSpecification requestSpecification = requestSpecBuilder.build();
		return requestSpecification;
	}

	public Map<String, String> getHeaders(boolean isAuthRequired) {
		Map<String, String> header = null;
		if (isAuthRequired) {
			header = new HashMap<String, String>() {{
				put(TestConstant.AUTHORIZATION, APIToken);
				put(TestConstant.USER_AUTH_TOKEN, AuthTokenUtil.getAuthToken());
				put(TestConstant.CONTENT_TYPE, TestConstant.CONTENT_TYPE_APPLICATION_JSON);
			}};
		} else {
			header = new HashMap<String, String>() {{
				put(TestConstant.AUTHORIZATION, APIToken);
				put(TestConstant.CONTENT_TYPE, TestConstant.CONTENT_TYPE_APPLICATION_JSON);
			}};
		}
		return header;
	}
	
	public RequestSpecification getRequestSpecification(String content_type,String user_id, String APIToken, String channelId, String appId)
	{
		RequestSpecBuilder builderreq = new RequestSpecBuilder();
		builderreq.addHeader("user-id", user_id);
		builderreq.addHeaders(getHeaders(true));
		builderreq.addHeader("Content-Type", content_type);
		builderreq.addHeader("Authorization", APIToken);
		builderreq.addHeader("X-Channel-Id", channelId);
		RequestSpecification requestSpec = builderreq.build();
		return requestSpec;
	}
	
	/**
	 * checks whether response statuscode is 200,param size is 5, param.status is successful and param.errmsg is null
	 * 
	 * @return ResponseSpecification object
	 */
	public ResponseSpecification get200ResponseSpec()
	{
		ResponseSpecBuilder builderres = new ResponseSpecBuilder();
		builderres.expectStatusCode(200);
		builderres.expectBody("params.size()", is(5));
		builderres.expectBody("params.status", equalTo("successful"));
		builderres.expectBody("params.errmsg", equalTo(null));
		builderres.expectBody("responseCode", equalTo("OK"));
		ResponseSpecification responseSpec = builderres.build();
		return responseSpec;
	}
	
	public ResponseSpecification get200ResponseSpecUpload()
	{
		ResponseSpecBuilder builderres = new ResponseSpecBuilder();
		builderres.expectStatusCode(200);
		builderres.expectBody("params.size()", is(5));
		//builderres.expectBody("params.status", equalTo("successful"));
		builderres.expectBody("params.errmsg", equalTo(null));
		builderres.expectBody("responseCode", equalTo("OK"));
		builderres.expectBody("result.size()", is(3));
		ResponseSpecification responseSpec = builderres.build();
		return responseSpec;
	}
	
	/**
	 * checks whether response statuscode is 500,param size is 5, param.status is failed and responsecode is SERVER_ERROR
	 * 
	 * @return ResponseSpecification object
	 */
	public ResponseSpecification get500ResponseSpec()
	{
		ResponseSpecBuilder builderres = new ResponseSpecBuilder();
		builderres.expectStatusCode(500);
		builderres.expectBody("params.size()", is(5));
		builderres.expectBody("params.status", equalTo("failed"));
		builderres.expectBody("responseCode", equalTo("SERVER_ERROR"));
		ResponseSpecification responseSpec = builderres.build();
		return responseSpec;
	}
	
	
	/**
	 * checks whether HTML response statuscode is 500 and param size is 1
	 * 
	 * @return ResponseSpecification object
	 */
	public ResponseSpecification get500HTMLResponseSpec()
	{
		ResponseSpecBuilder builderres = new ResponseSpecBuilder();
		builderres.expectStatusCode(500);
		builderres.expectBody("params.size()", is(1));
		ResponseSpecification responseSpec = builderres.build();
		return responseSpec;
	}
	
	
	
	/**
	 * checks whether response statuscode is 400 
	 * 
	 * @return ResponseSpecification object
	 */
	public ResponseSpecification get400ResponseSpec()
	{
		ResponseSpecBuilder builderres = new ResponseSpecBuilder();
		builderres.expectStatusCode(400);
		ResponseSpecification responseSpec = builderres.build();
		return responseSpec;
	}
	
	
	/**
	 * checks whether response statuscode is 404,param size is 5, param.status is failed and responsecode is RESOURCE_NOT_FOUND
	 * 
	 * @return ResponseSpecification object
	 */
	public ResponseSpecification get404ResponseSpec()
	{
		ResponseSpecBuilder builderres = new ResponseSpecBuilder();
		builderres.expectStatusCode(404);
		builderres.expectBody("params.size()", is(5));
		builderres.expectBody("params.status", equalTo("failed"));
		builderres.expectBody("responseCode", equalTo("RESOURCE_NOT_FOUND"));
		ResponseSpecification responseSpec = builderres.build();
		return responseSpec;
	}
	
	/**
	 * checks for mandatory fields required to create concepts or dimensions (checks whether response params.errmsg is Validation Errors)
	 * 
	 * @return ResponseSpecification object
	 */
	public ResponseSpecification get400ValidationErrorResponseSpec()
	{
		ResponseSpecBuilder builderres = new ResponseSpecBuilder();
		builderres.expectBody("params.errmsg", equalTo("Validation Errors"));
		ResponseSpecification responseSpec = builderres.build();
		return responseSpec;
	}
	
	/**
	 * checks for duplicates(checks whether response params.errmsg is Node Creation Error)
	 * 
	 * @return ResponseSpecification object
	 */
	public ResponseSpecification verify400DetailedResponseSpec(String errmsg, String responseCode, String resMessages)
	{
		ResponseSpecBuilder builderres = new ResponseSpecBuilder();
		builderres.expectBody("params.errmsg", equalTo(errmsg));
		builderres.expectBody("params.status", equalTo("failed"));
		builderres.expectBody("responseCode", equalTo(responseCode));
		//builderres.expectBody("result.messages", equalTo("Invalid Relation")); //TO-DO: How to get the list and how deep can be the list? 
		ResponseSpecification responseSpec = builderres.build();
		return responseSpec;
	}
	
	/**
	 * generates random integer between min and max
	 * 
	 * @param min
	 * @param max
	 * @return random integer
	 */
	public int generateRandomInt(int min, int max)
	{
		Random random = new Random();
		int randomInt = random.nextInt((max - min) + 1) + min;
		return randomInt;
		
	}
	
	public void contentCleanUp(String jsonContentClean){
		setURI();
		given().
		spec(getRequestSpec(contentType, validuserId)).
		body(jsonContentClean).
	with().
		contentType(JSON).
	when().
		post("v1/exec/content_qe_deleteContentBySearchStringInField").
	then().
		log().all().
		spec(get200ResponseSpec());	
	}
	
	public ResponseSpecification get207ResponseSpec()
	{
		ResponseSpecBuilder builderres = new ResponseSpecBuilder();
		builderres.expectStatusCode(207);
		builderres.expectBody("params.size()", is(5));
		builderres.expectBody("params.status", equalTo("partial successful"));
		builderres.expectBody("responseCode", equalTo("PARTIAL_SUCCESS"));
		ResponseSpecification responseSpec = builderres.build();
		return responseSpec;
	}

}
