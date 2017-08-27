package org.ekstep.platform.content;

import org.ekstep.platform.domain.BaseTest;
import static com.jayway.restassured.RestAssured.given;
import org.junit.Test;
import org.testng.Assert;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

import static com.jayway.restassured.http.ContentType.JSON;

import java.util.ArrayList;

import com.jayway.restassured.specification.RequestSpecification;

public class hierarchyAPITests extends BaseTest {
	
	public RequestSpecification getRequestSpecification(String content_type,String user_id, String APIToken)
	{
		RequestSpecBuilder builderreq = new RequestSpecBuilder();
		builderreq.addHeader("Content-Type", content_type);
		builderreq.addHeader("user-id", user_id);
		builderreq.addHeader("Authorization", APIToken);
		RequestSpecification requestSpec = builderreq.build();
		return requestSpec;
	}
	int rn = generateRandomInt(0, 9999999);
	String jsonCreateValidTextBook = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_"+rn+"\",\"osId\": \"org.ekstep.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"TextBook\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
	String jsonUpdateHierarchyExisting = "{\"request\":{\"data\":{\"nodesModified\":{\"textBookId\":{\"metadata\":{\"name\":\"Textbook1\",\"contentType\":\"TextBook\",\"code\":\"textbook1\",\"concepts\":[{\"identifier\":\"LO1\"}],\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"root\":true,\"isNew\":false},\"TBUnit1\":{\"metadata\":{\"name\":\"Textbook Unit 1\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunit1\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"isNew\":true},\"TBUnit2\":{\"metadata\":{\"name\":\"Textbook Unit 2\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunit2\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"isNew\":true},\"textbookunitchild1\":{\"metadata\":{\"name\":\"Textbook Unit 2.1\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunitchild1\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"isNew\":true},\"textbookunitchild2\":{\"metadata\":{\"name\":\"Textbook Unit 2.2\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunitchild2\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"root\":false,\"isNew\":true}},\"hierarchy\":{\"textBookId\":{\"children\":[\"TBUnit1\",\"TBUnit2\"],\"root\":true},\"TBUnit1\":{\"children\":[\"do_11228762110033100813\"],\"root\":false},\"TBUnit2\":{\"children\":[\"textbookunitchild1\",\"textbookunitchild2\"]},\"textbookunitchild1\":{\"children\":[]}},\"mutations\":{}}}}";
	String jsonUpdateHierarchyExistingNewCollection = "{\"request\":{\"data\":{\"nodesModified\":{\"textBookId\":{\"metadata\":{\"name\":\"Textbook1\",\"contentType\":\"TextBook\",\"code\":\"textbook1\",\"concepts\":[{\"identifier\":\"LO1\"}],\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"root\":true,\"isNew\":false},\"TBUnit1\":{\"metadata\":{\"name\":\"Textbook Unit 1\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunit1\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"isNew\":true},\"TBUnit2\":{\"metadata\":{\"name\":\"Textbook Unit 2\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunit2\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"isNew\":true},\"textbookunitchild1\":{\"metadata\":{\"name\":\"Textbook Unit 2.1\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunitchild1\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"isNew\":true},\"textbookunitchild2\":{\"metadata\":{\"name\":\"Textbook Unit 2.2\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunitchild2\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"root\":false,\"isNew\":true}},\"hierarchy\":{\"textBookId\":{\"children\":[\"TBUnit1\",\"TBUnit2\"],\"root\":true},\"TBUnit1\":{\"children\":[\"do_11228762110033100813\"],\"root\":false},\"TBUnit2\":{\"children\":[\"textbookunitchild1\",\"textbookunitchild2\"]},\"textbookunitchild1\":{\"children\":[]}},\"mutations\":{}}}}";
	String jsonUpdateHierarchyNew = "{\"request\":{\"data\":{\"nodesModified\":{\"textBookId\":{\"metadata\":{\"name\":\"Textbook1\",\"contentType\":\"TextBook\",\"code\":\"textbook1\",\"concepts\":[{\"identifier\":\"LO1\"}],\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"root\":true,\"isNew\":true},\"TBUnit1\":{\"metadata\":{\"name\":\"Textbook Unit 1\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunit1\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"isNew\":true},\"TBUnit2\":{\"metadata\":{\"name\":\"Textbook Unit 2\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunit2\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"isNew\":true},\"textbookunitchild1\":{\"metadata\":{\"name\":\"Textbook Unit 2.1\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunitchild1\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"isNew\":true},\"textbookunitchild2\":{\"metadata\":{\"name\":\"Textbook Unit 2.2\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunitchild2\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"root\":false,\"isNew\":true}},\"hierarchy\":{\"textBookId\":{\"children\":[\"TBUnit1\",\"TBUnit2\"],\"root\":true},\"TBUnit1\":{\"children\":[\"do_11228762110033100813\"],\"root\":false},\"TBUnit2\":{\"children\":[\"textbookunitchild1\",\"textbookunitchild2\"]},\"textbookunitchild1\":{\"children\":[]}},\"mutations\":{}}}}";
	String jsonUpdateHierarchy2roots = "{\"request\":{\"data\":{\"nodesModified\":{\"textBookId\":{\"metadata\":{\"name\":\"Textbook1\",\"contentType\":\"TextBook\",\"code\":\"textbook1\",\"concepts\":[{\"identifier\":\"LO1\"}],\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"root\":true,\"isNew\":true},\"TBUnit1\":{\"metadata\":{\"name\":\"Textbook Unit 1\",\"contentType\":\"TextBook\",\"code\":\"textbookunit1\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"root\":true,\"isNew\":true},\"TBUnit2\":{\"metadata\":{\"name\":\"Textbook Unit 2\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunit2\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"isNew\":true},\"textbookunitchild1\":{\"metadata\":{\"name\":\"Textbook Unit 2.1\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunitchild1\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"isNew\":true},\"textbookunitchild2\":{\"metadata\":{\"name\":\"Textbook Unit 2.2\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunitchild2\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"root\":false,\"isNew\":true}},\"hierarchy\":{\"textBookId\":{\"children\":[\"TBUnit1\",\"TBUnit2\"],\"root\":true},\"TBUnit1\":{\"children\":[\"do_11228762110033100813\"],\"root\":true},\"TBUnit2\":{\"children\":[\"textbookunitchild1\",\"textbookunitchild2\"]},\"textbookunitchild1\":{\"children\":[]}},\"mutations\":{}}}}";
	
	@Test
	public void updateHierarchyExistingTextbookExpectSuccess200(){
		
		// Create Textbook
		setURI();
		Response R =
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body(jsonCreateValidTextBook).
		with().
			contentType(JSON).
		when().
			post("/content/v3/create").
		then().
		log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jp1 = R.jsonPath();
		String tbId = jp1.get("result.node_id");
		
		// Update hierarchy
		setURI();
		jsonUpdateHierarchyExisting = jsonUpdateHierarchyExisting.replace("textBookId", tbId);
		Response R2 =
		given().
			spec(getRequestSpec(contentType, userId)).
			body(jsonUpdateHierarchyExisting).
		with().
			contentType(JSON).
		when().
			patch("/learning/v2/content/hierarchy/update").
		then().
		log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jp2 = R2.jsonPath();
		String TBU1 = jp2.get("result.identifiers.TBUnit1");
		System.out.println(TBU1);
//		String TBUC1 = jp2.get("result.identifiers.textbookunitchild1");
//		String TBU2 = jp2.get("result.identifiers.TBUnit2");
//		String TBUC2 = jp2.get("result.identifiers.textbookunitchild2");
		
		// Get Hierarchy
		setURI();
		Response R3 =
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
			get("/content/v3/hierarchy/"+tbId).
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().response();
		
		JsonPath jp3 = R3.jsonPath();
		ArrayList<String> identifiers = jp3.get("result.content.children.children.collections.identifier");
		System.out.println(identifiers);
		Assert.assertTrue(identifiers.contains(TBU1));
	}
	
	// Update Hierarchy with new textbook and new textbookUnit
	@Test
	public void updateHierarchyNewTextbookExpectSuccess200(){
		
		// Update hierarchy
		setURI();
		Response R2 =
		given().
			spec(getRequestSpec(contentType, userId)).
			body(jsonUpdateHierarchyNew).
		with().
			contentType(JSON).
		when().
			patch("/learning/v2/content/hierarchy/update").
		then().
		log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jp2 = R2.jsonPath();
		String tbId = jp2.get("result.identifiers.textBookId");
		String TBU1 = jp2.get("result.identifiers.TBUnit1");
		//String TBUC1 = jp2.get("result.identifiers.textbookunitchild1");
		//String TBU2 = jp2.get("result.identifiers.TBUnit2");
		//String TBUC2 = jp2.get("result.identifiers.textbookunitchild2");
		
		// Get Hierarchy
		setURI();
		Response R3 =
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
			get("/content/v3/hierarchy/"+tbId).
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().response();
		
		JsonPath jp3 = R3.jsonPath();
		ArrayList<String> identifiers = jp3.get("result.content.children.children.collections.identifier");
		System.out.println(identifiers);
		Assert.assertTrue(identifiers.contains(TBU1));		
	}	
	
	// Update request with more than one root
	
	@Test
	public void updateInvalidRootsExpect400(){
		// Update hierarchy
		setURI();
		Response R2 =
		given().
			spec(getRequestSpec(contentType, userId)).
			body(jsonUpdateHierarchy2roots).
		with().
			contentType(JSON).
		when().
			patch("/learning/v2/content/hierarchy/update").
		then().
		log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jp2 = R2.jsonPath();
		String tbId = jp2.get("result.identifiers.textBookId");
		
		// Get Hierarchy
		setURI();
		Response R3 =
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
			get("/content/v3/hierarchy/"+tbId).
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().response();
		
		JsonPath jp3 = R3.jsonPath();
		ArrayList<String> identifiers = jp3.get("result.content.children.children.collections.identifier");
		System.out.println(identifiers);
	}
	
	// Update existing textbook with new textbook unit and new collection
	
	@Test
	public void updateExistingTBwithNewTextbookAndCollectionExpectSuccess200(){
		// Create Textbook
		setURI();
		Response R =
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body(jsonCreateValidTextBook).
		with().
			contentType(JSON).
		when().
			post("/content/v3/create").
		then().
		log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jp1 = R.jsonPath();
		String tbId = jp1.get("result.node_id");
		
		// Update hierarchy
		setURI();
		jsonUpdateHierarchyExisting = jsonUpdateHierarchyExisting.replace("textBookId", tbId);
		Response R2 =
		given().
			spec(getRequestSpec(contentType, userId)).
			body(jsonUpdateHierarchyExisting).
		with().
			contentType(JSON).
		when().
			patch("/learning/v2/content/hierarchy/update").
		then().
		log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jp2 = R2.jsonPath();
		String TBU1 = jp2.get("result.identifiers.TBUnit1");
		System.out.println(TBU1);
//				String TBUC1 = jp2.get("result.identifiers.textbookunitchild1");
//				String TBU2 = jp2.get("result.identifiers.TBUnit2");
//				String TBUC2 = jp2.get("result.identifiers.textbookunitchild2");
		
		// Get Hierarchy
		setURI();
		Response R3 =
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
			get("/content/v3/hierarchy/"+tbId).
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().response();
		
		JsonPath jp3 = R3.jsonPath();
		ArrayList<String> identifiers = jp3.get("result.content.children.children.collections.identifier");
		System.out.println(identifiers);
		Assert.assertTrue(identifiers.contains(TBU1));
	}
	
	// Update existing textbook with new textbookunit and existing collection
	
	@Test
	public void updateExistingTBwithExistingTextbookAndCollectionExpectSuccess200(){
		setURI();
		Response R =
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			body(jsonCreateValidTextBook).
		with().
			contentType(JSON).
		when().
			post("/content/v3/create").
		then().
		log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jp1 = R.jsonPath();
		String tbId = jp1.get("result.node_id");
		
		// Update hierarchy
		setURI();
		jsonUpdateHierarchyExisting = jsonUpdateHierarchyExisting.replace("textBookId", tbId);
		Response R2 =
		given().
			spec(getRequestSpec(contentType, userId)).
			body(jsonUpdateHierarchyExisting).
		with().
			contentType(JSON).
		when().
			patch("/learning/v2/content/hierarchy/update").
		then().
		log().all().
			spec(get200ResponseSpec()).
		extract().
			response();
		
		JsonPath jp2 = R2.jsonPath();
		String TBU1 = jp2.get("result.identifiers.TBUnit1");
		System.out.println(TBU1);
//				String TBUC1 = jp2.get("result.identifiers.textbookunitchild1");
//				String TBU2 = jp2.get("result.identifiers.TBUnit2");
//				String TBUC2 = jp2.get("result.identifiers.textbookunitchild2");
		
		// Get Hierarchy
		setURI();
		Response R3 =
		given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
			get("/content/v3/hierarchy/"+tbId).
		then().
			log().all().
			spec(get200ResponseSpec()).
		extract().response();
		
		JsonPath jp3 = R3.jsonPath();
		ArrayList<String> identifiers = jp3.get("result.content.children.children.collections.identifier");
		System.out.println(identifiers);
		Assert.assertTrue(identifiers.contains(TBU1));
	}
}
