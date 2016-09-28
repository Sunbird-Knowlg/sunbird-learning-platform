package org.ekstep.platform.domain;

import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.http.ContentType.JSON;
import org.junit.Ignore;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;

public class DomainAPITests extends BaseTest
{
	
	String JsonInPutForConceptSearchWithTag = "{ \"request\": {\"search\": {\"tags\": [\"Class 4\"],\"resultSize\": 5 }}}";
	int noOfLiveDomains = 2;
		
	//Get Domains
	@Test
	public void getDomainsExpectSuccess200()
	{
		setURI();
		given().	
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains").
		then().
			log().all().
			spec(get200ResponseSpec()).
	        body("result.domains.size()", is(noOfLiveDomains)).
	        body("result.domains.status", hasItems(liveStatus)).
			body("result.domains.name", hasItems("Numeracy","Literacy V2"));
	}
	
	@Ignore //considered after feb 11th release
	@Test
	public void getDomainsWithInvalidUsernameExpectHTTP4xxError()  
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,invalidUserId)).
		when().
			get("v2/domains").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	@Test
	public void getDomainsWithHashExpectHTTP500Error()  
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains#").
		then().
			log().all().
			spec(get500ResponseSpec());
	}
	
	@Ignore //considered after feb 11th release
	@Test
	public void getDomainsEndingWithPercentileExpect400()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains%").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	@Test
	public void getDomainsWithDollarExpect500Error()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains$").
		then().
			log().all().
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
			get("v2/domains/literacy").
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
			get("v2/domains/nume").
		then().
			log().all().
			spec(get404ResponseSpec());
	}
		
	//Search Domains
	@Ignore
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
			post("v2/domains/numeracy").
		then().
			log().all().
			body("id", equalTo("orchestrator.searchDomainObjects"));
	}
	
	//Get Domain Graph
	@Test
	public void getDomainGraphExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains/graph/literacy").
		then().
			//log().all().
			spec(get200ResponseSpec());
	}

	@Test
	public void getDomainGraphOfNonExistingDomainExpect4xx()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains/graph/xyz").
		then().
			//log().all().
			spec(get404ResponseSpec());
	}
		
	@Test
	public void getDomainGraphWithoutHeaderExpect400()
	{
		setURI();
		given().
			//spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains/graph/literacy").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
}