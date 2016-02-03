package org.ekstep.lp.domain;

import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.http.ContentType.JSON;

import org.junit.Ignore;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;
import org.junit.Test;
import static com.jayway.restassured.http.ContentType.JSON;


public class DomainAPITests extends BaseTest


{
	
	String JsonInPutForConceptSearchWithTag = "{ \"request\": {\"search\": {\"tags\": [\"Class 4\"],\"resultSize\": 5 }}}";
	
	int noOfLiveDomains = 2;
		
	/***
	 * The following are the positive tests on getDomains and getDomain API calls. 
	 */
	@Test
	public void getDomainsExpectSuccess()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("domains").
		then().
			spec(get200ResponseSpec()).
	        body("result.domains.size()", is(noOfLiveDomains)).
	        body("result.domains.status", hasItems(liveStatus)).
			body("result.domains.name", hasItems("Numeracy","Literacy"));
	}
	
	@Test
	public void getSingleDomainExpectSuccess()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("domains/literacy").
		then().
			spec(get200ResponseSpec()).
			body("result.domain.status", equalTo(liveStatus));
	}
	
	
	/***
	 * The following are the negative tests on getDomains and getDomain API calls, triggering failures on the HTTP stack. 
	 */
	
	//The following test case fails because of a bug. 
	@Test
	public void getDomainsWithInvalidUsernameExpectHTTP4xxError()  
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,invalidUserId)).
		when().
			get("domains").
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
			get("domains#").
		then().
			log().all().
			spec(get500ResponseSpec());
	}
	
	@Test
	public void getDomainsEndingWithPercentileExpect400()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("domains%").
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
			get("domains$").
		then().
			log().all().
			spec(get500HTMLResponseSpec());
	}
	
	/***
	 * The following are the negative tests on getDomains and getDomain API calls, triggering failures on the GraphDB. 
	 */
	
	@Test
	public void getNonExistingDomainExpect404()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("domains/nume").
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
			post("domains/numeracy").
		then().
			log().all().
			body("id", equalTo("orchestrator.searchDomainObjects"));
	}
	
	//TO_DO: getDomainGraph 
}

