package org.sunbird.platform.domain;

import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.http.ContentType.JSON;
import org.junit.Ignore;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;

public class DomainAPITests extends BaseTest
{
	
	String JsonInPutForConceptSearchWithTag = "{\"request\":{\"search\":{\"status\":\"Live\",\"limit\":10}}}";
	int noOfLiveDomains = 2;
		
	//Get Domains
	@Test
	public void getDomainsExpectSuccess200()
	{
		setURI();
		given().	
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("/learning/v2/domains").
		then().
			//log().all()
			spec(get200ResponseSpec()).
	        body("result.domains.status", hasItems("Live"));
		}
	
	@Test
	public void getDomainsWithHashExpectHTTP500Error()  
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("/learning/v2/domains#").
		then().
			//log().all()
			spec(get500ResponseSpec());
	}
	
	@Test
	public void getDomainsEndingWithPercentileExpect500()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("/learning/v2/domains%").
		then().
			//log().all()
			spec(get500ResponseSpec());
	}
	
	@Test
	public void getDomainsWithDollarExpect500Error()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("/learning/v2/domains$").
		then().
			//log().all()
			spec(get500ResponseSpec());
	}
	
	//Get Domain
	@Test
	public void getSingleDomainExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("/learning/v2/domains/literacy").
		then().
			spec(get200ResponseSpec()).
			body("result.domain.status", equalTo(liveStatus));
	}
	
	@Test
	public void getNonExistingDomainExpect404()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("/learning/v2/domains/nume").
		then().
			//log().all()
			spec(get404ResponseSpec());
	}
		
	//Search Domains
	@Test
	public void searchDomainsExpectSuccess()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonInPutForConceptSearchWithTag).
			with().
				contentType(JSON).
		when().
			post("/learning/v2/domains/numeracy/methods/search").
		then().
			spec(get200ResponseSpec());
	}
}