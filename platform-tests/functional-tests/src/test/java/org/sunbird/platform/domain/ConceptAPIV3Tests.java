package org.sunbird.platform.domain;

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
	String invalidConceptsUrl = "/domain/v3/literacy/concepts/read/adcsad";

	String jsonBodyForCreateConcept = "{\"request\":{\"object\":{\"identifier\":\"TEST_CONCEPT_"+rn+"\",\"description\":\"Test\",\"name\":\"Test\",\"code\":\"Test_QA\",\"parent\":[{\"identifier\":\"LD5\",\"name\":\"Reading Comprehension\",\"objectType\":\"Concept\",\"relation\":\"isParentOf\"}]}}}";
	String jsonBodyForCreateDuplicateConcept = "{\"request\": {\"object\": {\"identifier\": \"TEST_DUPL_CONCEPT_"+rn+"\",\"description\": \"Duplicate Test\",\"name\": \"Duplicate_Test\",\"code\": \"Lit:Dim:Dupl\",\"parent\": [{\"identifier\": \"LD5\",\"name\": \"Reading Comprehension\",\"objectType\": \"Dimension\",\"relation\": \"isParentOf\"}]}}}";
	String jsonBodyForCreateConceptWithNoDim = "{\"request\": {\"object\": {\"identifier\": \"TEST_NO_DIM_CONCEPT\",\"description\": \"Concept with no Dimension Test\",\"name\": \"Concept_Test\",\"code\": \"Lit:Dim:LD90\",\"parent\": [{\"identifier\": \"LD100\",\"name\": \"Reading Comprehension\",\"objectType\": \"Dimension\",\"relation\": \"isParentOf\"}]}}}";
	String JsonInPutForConceptSearchWithTag = "{ \"request\": {\"search\": {\"name\": [\"Test\"],\"resultSize\": 5 }}}";

	@Before
	public void delay(){
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// Get list numeracy concepts

	// TODO: check and fix.
	@Ignore
	@Test
	public void getNumeracyConceptsExpectSuccess200()
	{
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		get("/domain/v3/numeracy/concepts/list").
		then().
		//log().all().
		spec(get200ResponseSpec()).
		body("result.concepts.status", hasItems("Live"));
	}

	// Get list literacy concepts

	@Ignore
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
	public void getConceptsExpect404Error()
	{
		setURI();
		given().
		spec(getRequestSpecification(contentType,userId,APIToken)).
		when().
		get(invalidConceptsUrl).
		then().
		spec(get404ResponseSpec());
	}

	/***
	 * The following are the negative tests on getConcepts and getConcept API calls. 
	 */
	@Ignore
	public void getConceptsWithNonExistingDomainExpect404()
	{
		setURI();
		given().
		spec(getRequestSpecification(contentType,userId,APIToken)).
		when().
		get("/domain/v3/lite/concepts/list").
		then().
		//log().all().
		spec(get404ResponseSpec());
	}

	@Test
	public void getConceptWithInvalidConceptIDExpect404()
	{
		setURI();
		given().
		spec(getRequestSpecification(contentType,userId,APIToken)).
		when().
		get("/domain/v3/numeracy/concepts/read/xyz").
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
		try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);} 
		given().
		spec(getRequestSpecification(contentType,userId,APIToken)).
		when().
		get("/domain/v3/literacy/concepts/read/"+conceptId).
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
		try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);} 
		given().
		spec(getRequestSpecification(contentType,userId,APIToken)).
		when().
		get("/domain/v3/numeracy/concepts/read/"+conceptId).
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
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jp1 = R.jsonPath();
		String conceptId = jp1.get("result.node_id");

		//getDimension API call to verify if the above dimension has been created.
		setURI();
		try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);} 
		given().
		spec(getRequestSpecification(contentType,userId,APIToken)).
		when().
		get("/domain/v3/science/concepts/read/"+conceptId).
		then().
		//log().all().
		spec(get200ResponseSpec());
	}

	// Create concept with invalid domain
	@Ignore
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

	// Create duplicate concept
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
				
		setURI();
		try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);} 
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
	
	//Retire literacy concept
	@Test
	public void deleteLiteracyConceptExpectSuccess200(){
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
		try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);} 
		given().
		spec(getRequestSpecification(contentType,userId,APIToken)).
		when().
		get("/domain/v3/literacy/concepts/read/"+conceptId).
		then().
		//log().all().
		spec(get200ResponseSpec());		

		// Retire created concept
		setURI();
		try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);}  		
		given().
		spec(getRequestSpecification(contentType,userId,APIToken)).
		body("{\"request\":{}}").
		when().
		delete("/domain/v3/literacy/concepts/retire/"+conceptId).
		then().
		//log().all().
		spec(get200ResponseSpec());
	}
	
	//Retire numeracy concept
	@Test
	public void deleteNumeracyConceptExpectSuccess200(){
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
		try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);} 
		given().
		spec(getRequestSpecification(contentType,userId,APIToken)).
		when().
		get("/domain/v3/numeracy/concepts/read/"+conceptId).
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		// Retire created concept
		setURI();
		try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);} 
		given().
		spec(getRequestSpecification(contentType,userId,APIToken)).
		body("{\"request\":{}}").
		when().
		delete("/domain/v3/numeracy/concepts/retire/"+conceptId).
		then().
		//log().all().
		spec(get200ResponseSpec());
	}
	
	//Retire Science Concept
	@Test
	public void deleteScienceConceptExpectSuccess200(){
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
				//log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jp1 = R.jsonPath();
		String conceptId = jp1.get("result.node_id");

		//getDimension API call to verify if the above dimension has been created.
		setURI();
		try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);} 
		given().
		spec(getRequestSpecification(contentType,userId,APIToken)).
		when().
		get("/domain/v3/science/concepts/read/"+conceptId).
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		// Retire created concept
		setURI();
		try{Thread.sleep(5000);}catch(InterruptedException e){System.out.println(e);} 
		given().
		spec(getRequestSpecification(contentType,userId,APIToken)).
		body("{\"request\":{}}").
		when().
		delete("/domain/v3/science/concepts/retire/"+conceptId).
		then().
		//log().all().
		spec(get200ResponseSpec());
	}
	
	// Retire invalid Concept
	@Test
	public void deleteInvalidConceptExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType,userId,APIToken)).
		body("{\"request\":{}}").
		when().
		delete("/domain/v3/science/concepts/retire/fajdsvjd").
		then().
		//log().all().
		spec(get404ResponseSpec());
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
		//log().all().
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
		//log().all().
		spec(get200ResponseSpec());

	}
}
