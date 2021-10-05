package org.sunbird.platform.domain;

import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.http.ContentType.JSON;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

import static org.hamcrest.CoreMatchers.*;

import java.util.ArrayList;
import java.util.HashMap;

@Ignore
public class DomainV3APITests extends BaseTest
{

	String JsonInPutForConceptSearchWithTag = "{\"request\":{\"search\":{\"status\":\"Live\",\"limit\":10}}}";
	int noOfLiveDomains = 2;

	//Get Domains
	@Test
	public void getDomainsExpectSuccess200()
	{
		setURI();
		given().	
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		get("/domain/v3/list").
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
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		get("/learning/v2/domains$").
		then().
		//log().all().
		spec(get500ResponseSpec());
	}

	@Test
	public void getDomainsEndingWithPercentileExpect500()
	{
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
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
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		get("/learning/v2/domains$").
		then().
		//log().all()
		spec(get500ResponseSpec());
	}

	//Get Domain
	@Test
	public void getliteracyDomainExpectSuccess200()
	{
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		get("/domain/v3/literacy/read").
		then().
		spec(get200ResponseSpec()).
		body("result.domain.status", equalTo(liveStatus));
	}

	@Test
	public void getNumeracyDomainExpectSuccess200(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		get("/domain/v3/numeracy/read").
		then().
		spec(get200ResponseSpec()).
		body("result.domain.status", equalTo(liveStatus));
	}

	// Read Science Domain
	@Test
	public void getScienceDomainExpectSuccess200(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		get("/domain/v3/science/read").
		then().
		spec(get200ResponseSpec()).
		body("result.domain.status", equalTo(liveStatus));
	}

	//Read Invalid domain
	@Test
	public void getNonExistingDomainExpect404()
	{
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		get("/learning/v2/domains/nume").
		then().
		//log().all().
		spec(get404ResponseSpec());
	}
	
	// Get Terms list
	@Test
	public void getTermsListExpectSuccess200(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		get("/domain/v3/terms/list").
		then().
		spec(get200ResponseSpec());		
	}
	
	// Get terms list with parameters
	@Test
	public void getContentTermsListExpectSuccess200(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		get("/domain/v3/terms/list?language=mr").
		then().
		spec(get200ResponseSpec());	
	}
}