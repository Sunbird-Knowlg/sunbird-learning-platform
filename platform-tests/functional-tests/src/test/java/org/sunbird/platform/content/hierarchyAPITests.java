package org.sunbird.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import org.sunbird.platform.domain.BaseTest;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.testng.Assert;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
@Ignore
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
	String jsonCreateValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_" + rn+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_NFT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Resource\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
	String jsonCreateContentCollection = "{\"request\": {\"content\": {\"identifier\": \"LP_NFT_Collection_" + rn+ "\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"description\": \"Test_QA\",\"name\": \"LP_NFT_"+ rn+ "\",\"language\":[\"English\"],\"contentType\": \"Collection\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"owner\": \"EkStep\", \"children\": [{ \"identifier\": \"id1\"}, { \"identifier\": \"id2\"}]}}}";
	String jsonCreateValidTextBook = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_"+rn+"\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"TextBook\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
	String jsonUpdateHierarchyExisting = "{\"request\":{\"data\":{\"nodesModified\":{\"textBookId\":{\"metadata\":{\"name\":\"Textbook1\",\"contentType\":\"TextBook\",\"code\":\"textbook1\",\"concepts\":[{\"identifier\":\"LO1\"}],\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"root\":true,\"isNew\":false},\"TBUnit1\":{\"metadata\":{\"name\":\"Textbook Unit 1\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunit1\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"isNew\":true},\"TBUnit2\":{\"metadata\":{\"name\":\"Textbook Unit 2\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunit2\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"isNew\":true},\"textbookunitchild1\":{\"metadata\":{\"name\":\"Textbook Unit 2.1\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunitchild1\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"isNew\":true},\"textbookunitchild2\":{\"metadata\":{\"name\":\"Textbook Unit 2.2\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunitchild2\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"root\":false,\"isNew\":true}},\"hierarchy\":{\"textBookId\":{\"children\":[\"TBUnit1\",\"TBUnit2\"],\"root\":true},\"TBUnit1\":{\"children\":[\"do_11228762110033100813\"],\"root\":false},\"TBUnit2\":{\"children\":[\"textbookunitchild1\",\"textbookunitchild2\"]},\"textbookunitchild1\":{\"children\":[]}},\"mutations\":{}}}}";
	String jsonUpdateHierarchyExistingNewCollection = "{\"request\":{\"data\":{\"nodesModified\":{\"textBookId\":{\"metadata\":{\"name\":\"Textbook1\",\"contentType\":\"TextBook\",\"code\":\"textbook1\",\"concepts\":[{\"identifier\":\"LO1\"}],\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"root\":true,\"isNew\":false},\"TBUnit1\":{\"metadata\":{\"name\":\"Textbook Unit 1\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunit1\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"isNew\":true},\"TBUnit2\":{\"metadata\":{\"name\":\"Textbook Unit 2\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunit2\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"isNew\":true},\"textbookunitchild1\":{\"metadata\":{\"name\":\"Textbook Unit 2.1\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunitchild1\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"isNew\":true},\"textbookunitchild2\":{\"metadata\":{\"name\":\"Textbook Unit 2.2\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunitchild2\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"root\":false,\"isNew\":true}},\"hierarchy\":{\"textBookId\":{\"children\":[\"TBUnit1\",\"TBUnit2\"],\"root\":true},\"TBUnit1\":{\"children\":[\"do_11228762110033100813\"],\"root\":false},\"TBUnit2\":{\"children\":[\"textbookunitchild1\",\"textbookunitchild2\"]},\"textbookunitchild1\":{\"children\":[]}},\"mutations\":{}}}}";
	String jsonUpdateHierarchyNew = "{\"request\":{\"data\":{\"nodesModified\":{\"textBookId\":{\"metadata\":{\"name\":\"Textbook1\",\"contentType\":\"TextBook\",\"code\":\"textbook1\",\"concepts\":[{\"identifier\":\"LO1\"}],\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"root\":true,\"isNew\":true},\"TBUnit1\":{\"metadata\":{\"name\":\"Textbook Unit 1\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunit1\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"isNew\":true},\"TBUnit2\":{\"metadata\":{\"name\":\"Textbook Unit 2\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunit2\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"isNew\":true},\"textbookunitchild1\":{\"metadata\":{\"name\":\"Textbook Unit 2.1\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunitchild1\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"isNew\":true},\"textbookunitchild2\":{\"metadata\":{\"name\":\"Textbook Unit 2.2\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunitchild2\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"root\":false,\"isNew\":true}},\"hierarchy\":{\"textBookId\":{\"children\":[\"TBUnit1\",\"TBUnit2\"],\"root\":true},\"TBUnit1\":{\"children\":[\"do_11228762110033100813\"],\"root\":false},\"TBUnit2\":{\"children\":[\"textbookunitchild1\",\"textbookunitchild2\"]},\"textbookunitchild1\":{\"children\":[]}},\"mutations\":{}}}}";
	String jsonUpdateHierarchy2roots = "{\"request\":{\"data\":{\"nodesModified\":{\"textBookId\":{\"metadata\":{\"name\":\"Textbook1\",\"contentType\":\"TextBook\",\"code\":\"textbook1\",\"concepts\":[{\"identifier\":\"LO1\"}],\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"root\":true,\"isNew\":true},\"TBUnit1\":{\"metadata\":{\"name\":\"Textbook Unit 1\",\"contentType\":\"TextBook\",\"code\":\"textbookunit1\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"root\":true,\"isNew\":true},\"TBUnit2\":{\"metadata\":{\"name\":\"Textbook Unit 2\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunit2\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"isNew\":true},\"textbookunitchild1\":{\"metadata\":{\"name\":\"Textbook Unit 2.1\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunitchild1\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"isNew\":true},\"textbookunitchild2\":{\"metadata\":{\"name\":\"Textbook Unit 2.2\",\"contentType\":\"TextBookUnit\",\"code\":\"textbookunitchild2\",\"mimeType\":\"application/vnd.ekstep.content-collection\"},\"root\":false,\"isNew\":true}},\"hierarchy\":{\"textBookId\":{\"children\":[\"TBUnit1\",\"TBUnit2\"],\"root\":true},\"TBUnit1\":{\"children\":[\"do_11228762110033100813\"],\"root\":true},\"TBUnit2\":{\"children\":[\"textbookunitchild1\",\"textbookunitchild2\"]},\"textbookunitchild1\":{\"children\":[]}},\"mutations\":{}}}}";

	static ClassLoader classLoader = ContentPublishWorkflowTests.class.getClassLoader();
	static URL url = classLoader.getResource("DownloadedFiles");
	static File downloadPath;
	static File path = new File(classLoader.getResource("UploadFiles/").getFile());

	@BeforeClass
	public static void setup() throws URISyntaxException {
		downloadPath = new File(url.toURI().getPath());
	}


	// Read hierarchy for valid content
	@Test
	public void getValidHierarchyExpectSuccess200(){
		// Create Collection
		String node1 = null;
		String node2 = null;
		int count = 1;
		while (count <= 2) {
			setURI();
			int rn = generateRandomInt(9999, 1999999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_NFTT_" + rn + "").put("name","LP_NFTT-" + rn + "");
			String jsonCreateValidChild = js.toString();
			Response R = 
					given().
					spec(getRequestSpecification(contentType, userId, APIToken)).
					body(jsonCreateValidChild).
					with().
					contentType(JSON).
					when().
					post("content/v3/create").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if (count == 1) {
				node1 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpecification(uploadContentType, userId, APIToken)).
				multiPart(new File(path + "/uploadContent.zip")).
				when().
				post("/content/v3/upload/" + node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
				when().
				post("/content/v3/publish/" + node1).
				then().
				//log().all().
				spec(get200ResponseSpec());
			}
			if (count == 2) {
				node2 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpecification(uploadContentType, userId, APIToken)).
				multiPart(new File(path + "/tweenAndaudioSprite.zip")).
				when().
				post("/content/v3/upload/" + node2).
				then().
				//log().all().
				spec(get200ResponseSpec());

				// Publish created content
				setURI();
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
				when().
				post("/content/v3/publish/" + node2).
				then().
				//log().all().
				spec(get200ResponseSpec());
			}
			count++;
		}
		// Create collection
		setURI();
		jsonCreateContentCollection = jsonCreateContentCollection.replace("id1", node1).replace("id2", node2);
		Response R1 = 
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateContentCollection).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				//log().all().
				spec(get200ResponseSpec()).
				extract().response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");

		// Publish collection
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
		when().
		post("/content/v3/publish/" + nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec());
		
		// Get Hierarchy
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		get("/content/v3/hierarchy/"+nodeId).
		then().
		log().all().
		spec(get200ResponseSpec());
	}
	
	// Read Hierarchy for invalid content
	@Test
	public void readHierarchyInvalidContentExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		get("/content/v3/hierarchy/naslkc").
		then().
		//log().all().
		spec(get404ResponseSpec());
	}

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
