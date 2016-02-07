package org.ekstep.platform.domain;


import static com.jayway.restassured.RestAssured.baseURI;
import static com.jayway.restassured.RestAssured.basePath;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;
import static org.hamcrest.CoreMatchers.*;

public class BaseTest 
{
	ResponseSpecBuilder builderres = new ResponseSpecBuilder();
	
	public String liveStatus = "Live";
	public String contentType = "application/json";
	public String validuserId = "rayuluv";
	public String invalidUserId = "abc";
	//public String apiVersion = "v2";
	
	/**
	 * sets baseURI and basePath
	 */
	public void setURI()
	{
		//TO-DO: This will be read from config file, soon.
		baseURI ="http://lp-sandbox.ekstep.org:8080/taxonomy-service"; 
		//baseURI ="http://localhost:9090/ekstep-service"; 
		//basePath = apiVersion;
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
		builderres.expectStatusCode(200);
		builderres.expectBody("params.size()", is(5));
		builderres.expectBody("params.status", equalTo("successful"));
		builderres.expectBody("params.errmsg", equalTo(null));
		builderres.expectBody("responseCode", equalTo("OK"));
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
		builderres.expectBody("params.errmsg", equalTo(errmsg));
		builderres.expectBody("params.status", equalTo("failed"));
		builderres.expectBody("responseCode", equalTo(responseCode));
		//builderres.expectBody("result.messages", equalTo("Invalid Relation")); //TO-DO: How to get the list and how deep can be the list? 
		ResponseSpecification responseSpec = builderres.build();
		return responseSpec;
	}
	
}

