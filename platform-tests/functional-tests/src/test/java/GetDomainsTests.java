

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.Test;

public class GetDomainsTests extends BaseTest
{
	
	String contentType = "application/json";
	String userIdValid = "rayuluv";
	
	
	String JsonInPutForDomainSearch = "{ \"request\": {\"search\": {\"tags\": [\"Class 4\"],\"resultSize\": 5 }}}";
	

	
	@Test
	public void getDomainsExpectSuccess()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, userIdValid)).
		when().
	        get(subURI+"domains").
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
	public void searchDomains()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, userIdValid)).
			body(JsonInPutForDomainSearch).
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

