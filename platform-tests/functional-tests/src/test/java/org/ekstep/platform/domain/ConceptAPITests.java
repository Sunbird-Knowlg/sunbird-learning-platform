package org.ekstep.platform.domain;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:servlet-context.xml"})
@WebAppConfiguration
public class ConceptAPITests extends BaseTest {

	int noOfConceptsAvailable = 70;
	
	String conceptsUrl = "v2/domains/literacy/concepts";
	String invalidConceptsUrl = "v2/domains/literacy/abc";
	
	String jsonBodyForCreateConcept = "{\"request\": {\"object\": {\"identifier\": \"TEST_CONCEPT\",\"description\": \"Test\",\"name\": \"Test\",\"code\": \"Lit:Dim:98\",\"parent\": [{\"identifier\": \"LD5\",\"name\": \"Reading Comprehension\",\"objectType\": \"Dimension\",\"relation\": \"isParentOf\"}]}}}";
	String jsonBodyForCreateDuplicateConcept = "{\"request\": {\"object\": {\"identifier\": \"TEST_DUPL_CONCEPT\",\"description\": \"Duplicate Test\",\"name\": \"Duplicate_Test\",\"code\": \"Lit:Dim:Dupl\",\"parent\": [{\"identifier\": \"LD5\",\"name\": \"Reading Comprehension\",\"objectType\": \"Dimension\",\"relation\": \"isParentOf\"}]}}}";
	String jsonBodyForCreateConceptWithNoDim = "{\"request\": {\"object\": {\"identifier\": \"TEST_NO_DIM_CONCEPT\",\"description\": \"Concept with no Dimension Test\",\"name\": \"Concept_Test\",\"code\": \"Lit:Dim:LD90\",\"parent\": [{\"identifier\": \"LD100\",\"name\": \"Reading Comprehension\",\"objectType\": \"Dimension\",\"relation\": \"isParentOf\"}]}}}";
	String JsonInPutForConceptSearchWithTag = "{ \"request\": {\"search\": {\"tags\": [\"Class 4\"],\"resultSize\": 5 }}}";
	
	@Test
	public void getConceptsExpectSuccess()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get(conceptsUrl).
		then().
			spec(get200ResponseSpec()).
	        //body("result.concepts.size()", is(noOfConceptsAvailable)).
	        body("result.concepts.status", hasItems(liveStatus)).
			body("result.domains.name", hasItems("Numeracy","Literacy"));
	}
	
	@Test
	public void getConceptsExpect4xxError()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get(invalidConceptsUrl).
		then().
			spec(get404ResponseSpec());
	}
	
	@Test
	public void getSingleExistingConceptExpectSuccess()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains/literacy/LO18").
		then().
			log().all().
			spec(get200ResponseSpec()).
	        //body("result.concept.size()", is(7)).
	        body("result.concept.status", hasItems(liveStatus));		
	}
	
	/***
	 * The following are the negative tests on getConcepts and getConcept API calls. 
	 */
	@Test
	public void getConceptsWithNonExistingDomainExpect404()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains/abc/concepts").
		then().
			spec(get404ResponseSpec());
	}
	
	@Test
	public void getConceptWithInvalidConceptIDExpect404()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType,validuserId)).
		when().
			get("v2/domains/literacy/concepts/xyz").
		then().
			spec(get404ResponseSpec());
	}
	
	@Test
	public void createConceptExpectSuccess()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonBodyForCreateConcept).
			with().
				contentType(JSON).
		when().
			post("v2/domains/literacy/concepts").
		then().
			log().all().
			spec(get200ResponseSpec()).
			body("id", equalTo(""));
	}
	
	@Test
	public void createConceptWithNoDimExpect4xx()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonBodyForCreateConceptWithNoDim).
			with().
				contentType(JSON).
		when().
			post("v2/domains/liter/concepts").
		then().
			log().all().
			spec(get400ResponseSpec());
	}
	
	
	@Test
	public void createDuplicateConceptExpect4xx()
	{
		setURI();
		
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonBodyForCreateDuplicateConcept).
		with().
			contentType(JSON).
		when().
			post("v2/domains/literacy/concepts").
		then().
			log().all().
			spec(get200ResponseSpec());
			
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonBodyForCreateDuplicateConcept).
			with().
				contentType(JSON).
		when().
			post("v2/domains/literacy/concepts").
		then().
			log().all().
			spec(get400ResponseSpec()).
			body("id", equalTo(""));
	}
	
	@Test
	public void searchConceptsExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonInPutForConceptSearchWithTag).
		with().
			contentType("application/json").
		when().
			post("v2/domains/numeracy/concepts/search").
		then().
			log().all().
			spec(get200ResponseSpec()).
			body("result.concepts.size()", is(5)).
			body("id", equalTo("orchestrator.searchDomainObjects"));
	}
	
	//To-do: Need to check if it sends empty result or 4xx response.
	@Test
	public void searchConceptsNotExistingExpect4xx()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonInPutForConceptSearchWithTag).
		with().
			contentType("application/json").
		when().
			post("v2/domains/numeracy/concepts/search").
		then().
			log().all().
			spec(get400ResponseSpec());
			
	}
	
	@Test
	public void searchConceptsWithDomainNotExistingExpect4xx()
	{
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(JsonInPutForConceptSearchWithTag).
		with().
			contentType("application/json").
		when().
			post("v2/domains/abc/concepts/search").
		then().
			log().all().
			spec(get400ResponseSpec());
			
	}
	
	
	
}
