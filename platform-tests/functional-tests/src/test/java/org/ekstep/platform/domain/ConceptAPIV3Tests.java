package org.ekstep.platform.domain;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.hasItems;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.context.web.WebAppConfiguration;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

/*@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:servlet-context.xml"})*/
@WebAppConfiguration
public class ConceptAPIV3Tests extends BaseTest {
	

	int noOfConceptsAvailable = 70;
	int rn = generateRandomInt(0, 9999999);

	
	String conceptsUrl = "/learning/v2/domains/literacy/concepts";
	String invalidConceptsUrl = "/learning/v2/domains/literacy/abc";
	
	String jsonBodyForCreateConcept = "{\"request\":{\"object\":{\"identifier\":\"TEST_CONCEPT_"+rn+"\",\"description\":\"Test\",\"name\":\"Test\",\"code\":\"Test_QA\",\"parent\":[{\"identifier\":\"LD5\",\"name\":\"Reading Comprehension\",\"objectType\":\"Concept\",\"relation\":\"isParentOf\"}]}}}";
	String jsonBodyForCreateDuplicateConcept = "{\"request\": {\"object\": {\"identifier\": \"TEST_DUPL_CONCEPT_"+rn+"\",\"description\": \"Duplicate Test\",\"name\": \"Duplicate_Test\",\"code\": \"Lit:Dim:Dupl\",\"parent\": [{\"identifier\": \"LD5\",\"name\": \"Reading Comprehension\",\"objectType\": \"Dimension\",\"relation\": \"isParentOf\"}]}}}";
	String jsonBodyForCreateConceptWithNoDim = "{\"request\": {\"object\": {\"identifier\": \"TEST_NO_DIM_CONCEPT\",\"description\": \"Concept with no Dimension Test\",\"name\": \"Concept_Test\",\"code\": \"Lit:Dim:LD90\",\"parent\": [{\"identifier\": \"LD100\",\"name\": \"Reading Comprehension\",\"objectType\": \"Dimension\",\"relation\": \"isParentOf\"}]}}}";
	String JsonInPutForConceptSearchWithTag = "{ \"request\": {\"search\": {\"name\": [\"Test\"],\"resultSize\": 5 }}}";
	
	@Before
	public void delay(){
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	// Get list numeracy concepts
	
	@Test
	public void getNumeracyConceptsExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
			get("/domain/v3/numeracy/concepts/list").
		then().
			log().all().
			spec(get200ResponseSpec()).
			body("result.concepts.status", hasItems("Live"));
	}
	
	// Get list literacy concepts
	
		@Test
		public void getLiteracyConceptsExpectSuccess200()
		{
			setURI();
			given().
				spec(getRequestSpecification(contentType,userId,APIToken)).
			when().
				get("/domain/v3/literacy/concepts/list").
			then().
				//log().all().
				spec(get200ResponseSpec()).
				body("result.concepts.status", hasItems("Live"));
		}
	
		// Get list science concepts
		
		@Test
		public void getScienceConceptsExpectSuccess200()
		{
			setURI();
			given().
				spec(getRequestSpecification(contentType,userId,APIToken)).
			when().
				get("/domain/v3/science/concepts/list").
			then().
				//log().all().
				spec(get200ResponseSpec()).
				body("result.concepts.status", hasItems("Live"));
		}
		
	@Test
	public void getConceptsExpect400Error()
	{
		setURI();
		given().
			spec(getRequestSpecification(contentType,userId,APIToken)).
		when().
			get(invalidConceptsUrl).
		then().
			spec(get400ResponseSpec());
	}
	
	/***
	 * The following are the negative tests on getConcepts and getConcept API calls. 
	 */
	@Test
	public void getConceptsWithNonExistingDomainExpect404()
	{
		setURI();
		given().
			spec(getRequestSpecification(contentType,userId,APIToken)).
		when().
			get("/domain/v3/abc/concepts/list").
		then().
			spec(get404ResponseSpec());
	}
	
	@Test
	public void getConceptWithInvalidConceptIDExpect404()
	{
		setURI();
		given().
			spec(getRequestSpecification(contentType,userId,APIToken)).
		when().
			get("/domain/v3/numeracy/concepts/xyz").
		then().
			spec(get404ResponseSpec());
	}
	
	// Create Literacy concept
	@Test
	public void createConceptLiteracyExpectSuccessExpect200()
	{
		setURI();
		Response R =
		given().
			spec(getRequestSpecification(contentType,userId,APIToken)).
			body(jsonBodyForCreateConcept).
			with().
				contentType(JSON).
		when().
			post("/domain/v3/literacy/concepts/create").
		then().
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jp1 = R.jsonPath();
		String conceptId = jp1.get("result.node_id");
		
		//getConcept API call to verify if the above dimension has been created.
		setURI();
		given().
			spec(getRequestSpecification(contentType,userId,APIToken)).
		when().
			get("/domain/v3/literacy/concepts/"+conceptId).
		then().
			//log().all().
			spec(get200ResponseSpec());		
	}
	
	// Create numeracy concept
	
	@Test
	public void createNumeracyConceptExpectSuccess200(){
		setURI();
		Response R =
		given().
			spec(getRequestSpecification(contentType,userId,APIToken)).
			body(jsonBodyForCreateConcept).
			with().
				contentType(JSON).
		when().
			post("/domain/v3/numeracy/concepts/create").
		then().
			//log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jp1 = R.jsonPath();
		String conceptId = jp1.get("result.node_id");
		
		//getDimension API call to verify if the above dimension has been created.
		setURI();
		given().
			spec(getRequestSpecification(contentType,userId,APIToken)).
		when().
			get("/domain/v3/literacy/concepts/"+conceptId).
		then().
			//log().all().
			spec(get200ResponseSpec());
	}
	
	// Create science concept
	
	@Test
	public void createScienceConceptExpectSuccess200(){
		setURI();
		Response R =
		given().
			spec(getRequestSpecification(contentType,userId,APIToken)).
			body(jsonBodyForCreateConcept).
			with().
				contentType(JSON).
		when().
			post("/domain/v3/science/concepts/create").
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jp1 = R.jsonPath();
		String conceptId = jp1.get("result.node_id");
		
		//getDimension API call to verify if the above dimension has been created.
		setURI();
		given().
			spec(getRequestSpecification(contentType,userId,APIToken)).
		when().
			get("/domain/v3/literacy/concepts/"+conceptId).
		then().
			//log().all().
			spec(get200ResponseSpec());
	}
	
	@Test
	public void createConceptWithInvalidDomainExpect4xx()
	{
		setURI();
		given().
			spec(getRequestSpecification(contentType,userId,APIToken)).
			body(jsonBodyForCreateConceptWithNoDim).
			with().
				contentType(JSON).
		when().
			post("/domain/v3/liter/concepts").
		then().
			//log().all().
			spec(get400ResponseSpec());
	}
	
	
	@Test
	public void createDuplicateConceptExpect400()
	{
		setURI();
		
		given().
			spec(getRequestSpecification(contentType,userId,APIToken)).
			body(jsonBodyForCreateDuplicateConcept).
		with().
			contentType(JSON).
		when().
			post("/domain/v3/literacy/concepts/create");
		
		given().
			spec(getRequestSpecification(contentType,userId,APIToken)).
			body(jsonBodyForCreateDuplicateConcept).
			with().
				contentType(JSON).
		when().
			post("/domain/v3/literacy/concepts/create").
		then().
			//log().all().
			spec(get400ResponseSpec());
	}
	
	@Ignore
	public void searchConceptsExpectSuccess200()
	{
		setURI();
		given().
			spec(getRequestSpecification(contentType,userId,APIToken)).
			body(JsonInPutForConceptSearchWithTag).
		with().
			contentType("application/json").
		when().
			post("/learning/v2/domains/numeracy/concepts/search").
		then().
			 log().all().
			spec(get200ResponseSpec());
	}
	
	//To-do: Need to check if it sends empty result.
	@Ignore
	public void searchConceptsNotExistingExpect200()
	{
		setURI();
		given().
			spec(getRequestSpecification(contentType,userId,APIToken)).
			body(JsonInPutForConceptSearchWithTag).
		with().
			contentType("application/json").
		when().
			post("/learning/v2/domains/numeracy/concepts/search").
		then().
			//log().all().
			spec(get200ResponseSpec());
			
	}
	
	@Ignore
	public void searchConceptsWithDomainNotExistingExpect4xx()
	{
		setURI();
		given().
			spec(getRequestSpecification(contentType,userId,APIToken)).
			body(JsonInPutForConceptSearchWithTag).
		with().
			contentType("application/json").
		when().
			post("/learning/v2/domains/kfqlef/concepts/search").
		then().
			log().all().
			spec(get200ResponseSpec());
			
	}
}
