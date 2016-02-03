package org.ekstep.platform.content;

import org.ekstep.lp.domain.BaseTest;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.equalTo;
import org.junit.Test;


public class ContentAPITests extends BaseTest {
	
	String jsonGetContentList = "{\"request\": { \"search\": {\"contentType\": \"Story\"}}}";
	String jsonGetContentListEmptySearch = "{\"request\": { \"search\": {}}}";
	
	//Get Content List
	@Test
	public void getContentListExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonGetContentList).
			with().
				contentType("application/json").
			when().
				patch("content/list").
			then().
				log().all().
				spec(get200ResponseSpec());
			
	}
	
	
	//Get Content List
	@Test
	public void getContentListEmptySearchExpect400()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonGetContentList).
			with().
				contentType("application/json").
			when().
				patch("content/list").
			then().
				log().all().
				spec(get400ResponseSpec());
			
	}
	

	//Search Content List
	@Test
	public void searchContentListExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonGetContentList).
			with().
				contentType("application/json").
			when().
				patch("content/search").
			then().
				log().all().
				spec(get200ResponseSpec());
			
	}
	
	@Test
	public void searchContentListEmptySearchExpect400()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonGetContentList).
			with().
				contentType("application/json").
			when().
				patch("content/search").
			then().
				log().all().
				spec(get400ResponseSpec());
			
	}
	
	
		
	
}
