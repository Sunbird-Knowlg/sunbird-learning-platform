

import static com.jayway.restassured.RestAssured.baseURI;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.ilimi.taxonomy.content.common.BaseTestUtil;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class BaseTest 
{
	ResponseSpecBuilder builderres = new ResponseSpecBuilder();
	RequestSpecBuilder builderreq = new RequestSpecBuilder();
	String subURI="v2/";
	/**
	 * sets baseURI
	 */
	public void setURI()
	{
		baseURI = "http://localhost:8080/";
		//baseURI ="http://localhost:9090/taxonomy-service";
		//basePath = "v2/";
	}
	/**
	 * adds the given content_type and user_id to the request header
	 * 
	 * @param content_type - json/xml
	 * @param user_id
	 * @return returns a RequestSpecification object
	 */
	public RequestSpecification getRequestSpec(String content_type,String user_id)
	{
		
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
		builderres.expectBody("params.status", equalTo("successful"));
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
}

