package org.ekstep.lp.domain;

import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.http.ContentType.JSON;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;


//import com.jayway.restassured.itest.java.support.WithJetty;

import org.junit.Test;

//import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.http.ContentType.JSON;
//import static com.jayway.restassured.http.ContentType.URLENC;
//import static java.util.Arrays.asList;
//import static org.apache.commons.lang3.ArrayUtils.toObject;
//import static org.apache.commons.lang3.StringUtils.join;
//import static org.hamcrest.Matchers.*;
//import static org.junit.Assert.assertThat;

public class GetDomainsTests extends BaseTest
{
	
	String contentType = "application/json";
	String userIdValid = "rayuluv";
	
	
	String JsonInPutForConceptSearchWithTag = "{ \"request\": {\"search\": {\"tags\": [\"Class 4\"],\"resultSize\": 5 }}}";
	
	int noOfDomains = 2;
	

	
	@Test
	public void getAllDomainsExpect200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, userIdValid)).
		when().
	        get(subURI+"domains").
	    then().
	    	log().all().
	        spec(get200ResponseSpec()).
	        body("result.domains", is(2));
	}
	
	@Test
	public void getSingleExistingDomainExpect200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, userIdValid)).
		when().
	        get(subURI+"domains/literacy").
	    then().
	    	log().all().
	        spec(get200ResponseSpec());
	        
	}
	
	
	@Test
	public void getDomainWrongUrlExpect500()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, userIdValid)).
		when().
	        get(subURI+"domain").
	    then().
	    	log().all().
	        spec(get500ResponseSpec());
	}
	
	@Test
	public void getDomainsNotExistingExpect404()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, userIdValid)).
		when().
	        get(subURI+"domains/liter").
	    then().
	    	log().all().
	        spec(get404ResponseSpec());
		
	}
	
	@Test
	public void getDomainMalFormedRequestExpect404()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, userIdValid)).
		when().
	        get(subURI+"domains/literacy$").
	    then().
	    	log().all().
	        spec(get404ResponseSpec()); //TO-DO: Need to see if it will be 404 or 200. 
		
	}
	
	@Test
	public void searchDomains()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, userIdValid)).
			body(JsonInPutForConceptSearchWithTag).
			with().
				contentType(JSON).
		when().
			post(subURI+"domains/numeracy/concepts/search").
		then().
			//log().all().
			//spec(get200ResponseSpec()).
			body("id", equalTo("orchestrator.searchDomainObjects"));
	}
}

