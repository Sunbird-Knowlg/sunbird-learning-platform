package org.sunbird.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sunbird.platform.domain.BaseTest;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class AssesmentItemAPITest extends BaseTest {
	int rn = generateRandomInt(0, 9999999);

	String jsonCreateAssessmentItemMCQ = "{\"request\":{\"assessment_item\":{\"identifier\":\"LP_NFT_AS_" + rn
			+ "\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"code\":\"LP_NFT\",\"name\":\"LP_NFT_AS_" + rn
			+ "\",\"type\":\"mcq\",\"num_answers\":1,\"template\":\"mcq_template_2\",\"template_id\":\"mcq_template_2\",\"qlevel\":\"MEDIUM\",\"owner\":\"Test\",\"title\":\"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ.  ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\",\"question\":\"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ.ಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\",\"model\":{\"img\":{\"type\":\"image\",\"asset\":\"perimeter\"},\"img2\":{\"type\":\"image\",\"asset\":\"smallSquare\"},\"subtext\":\"(= 1  ಚದರ ಸೆಂ.ಮೀ)\"},\"options\":[{\"value\":{\"type\":\"text\",\"asset\":\"12&10\",\"font\":\"Verdana\",\"color\":\"white\",\"fontsize\":\"240\"}},{\"value\":{\"type\":\"text\",\"asset\":\"14&7\",\"font\":\"Verdana\",\"color\":\"white\",\"fontsize\":\"240\"}},{\"value\":{\"type\":\"text\",\"asset\":\"16&8\",\"font\":\"Verdana\",\"color\":\"white\",\"fontsize\":\"240\"}},{\"value\":{\"type\":\"text\",\"asset\":\"12&7\",\"font\":\"Verdana\",\"color\":\"white\",\"fontsize\":\"240\"},\"score\":1}],\"max_score\":1,\"partial_scoring\":false,\"feedback\":\"\"}}}}";
	String jsonCreateInvalidAssessmentItemMCQ = "{\"request\":{\"assessment_item\":{\"identifier\":\"LP_NFT_AS_" + rn
			+ "\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"code\":\"LP_NFT\",\"name\":\"LP_NFT_AS_" + rn
			+ "\",\"type\":\"mcq\",\"num_answers\":1,\"template\":\"mcq_template_2\",\"template_id\":\"mcq_template_2\",\"qlevel\":\"MEDIUM\",\"owner\":\"Test\",\"title\":\"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ.  ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\",\"question\":\"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ.ಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\",\"model\":{\"img\":{\"type\":\"image\",\"asset\":\"perimeter\"},\"img2\":{\"type\":\"image\",\"asset\":\"smallSquare\"},\"subtext\":\"(= 1  ಚದರ ಸೆಂ.ಮೀ)\"},\"options\":[{\"value\":{\"type\":\"text\",\"asset\":\"12&10\",\"font\":\"Verdana\",\"color\":\"white\",\"fontsize\":\"240\"}},{\"value\":{\"type\":\"text\",\"asset\":\"14&7\",\"font\":\"Verdana\",\"color\":\"white\",\"fontsize\":\"240\"}},{\"value\":{\"type\":\"text\",\"asset\":\"16&8\",\"font\":\"Verdana\",\"color\":\"white\",\"fontsize\":\"240\"}},{\"value\":{\"type\":\"text\",\"asset\":\"12&7\",\"font\":\"Verdana\",\"color\":\"white\",\"fontsize\":\"240\"},\"score\":1}],\"max_score\":1,\"partial_scoring\":false,\"feedback\":\"\"}, \"outRelations\": [{\"endNodeId\": \"gcyrdrt\",\"relationType\": \"associatedTo\"}]}}}";
	String jsonCreateAssessmentItemFTB = "{\"request\":{\"assessment_item\":{\"identifier\":\"LP_NFT_AS_" + rn
			+ "\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"name\":\"LP_NFT_AS_" + rn
			+ "\",\"framework\":\"Test\",\"template\":\"ftb_template_1\",\"template_id\":\"ftb_template_1\",\"type\":\"ftb\",\"num_answers\":1,\"model\":{},\"answer\":{\"ans1\":{\"value\":5,\"score\":1}},\"code\":\"aq1_set_1_2\",\"qlevel\":\"MEDIUM\",\"owner\":\"EkStep\",\"used_for\":\"worksheet\",\"max_score\":1,\"partial_scoring\":true}}}}";
	String jsonCreateInvalidAssessmentItemFTB = "{\"request\":{\"assessment_item\":{\"identifier\":\"LP_NFT_AS_" + rn
			+ "\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"name\":\"LP_NFT_AS_" + rn
			+ "\",\"template\":\"ftb_template_1\",\"template_id\":\"ftb_template_1\",\"type\":\"ftb\",\"num_answers\":1,\"model\":{},\"answer\":{\"ans1\":{\"value\":5,\"score\":1}},\"code\":\"aq1_set_1_2\",\"qlevel\":\"MEDIUM\",\"owner\":\"EkStep\",\"used_for\":\"worksheet\",\"max_score\":1,\"partial_scoring\":true}, \"outRelations\": [{\"endNodeId\": \"gcyrdrt\",\"relationType\": \"associatedTo\"}]}}}";
	String jsonCreateAssessmentItemMTF = "{\"request\":{\"assessment_item\":{\"objectType\":\"AssessmentItem\",\"identifier\":\"LP_NFT_AS_"
			+ rn + "\",\"metadata\":{\"code\":\"LP_NFT\",\"name\":\"LP_NFT_AS_" + rn
			+ "\",\"type\":\"mtf\",\"template_id\":\"mtf_template_3\",\"lhs_options\":[{\"value\":{\"type\":\"image\",\"asset\":\"grey\"},\"index\":0}],\"rhs_options\":[{\"value\":{\"type\":\"text\",\"asset\":\">\"}},{\"value\":{\"type\":\"text\",\"asset\":\"=\"}},{\"value\":{\"type\":\"mixed\",\"text\":\"<\",\"image\":\"image1\",\"audio\":\"audio1\"},\"answer\":0}],\"max_score\":6,\"partial_scoring\":true}}}}";
	String jsonCreateInvalidAssessmentItemMTF = "{\"request\":{\"assessment_item\":{\"objectType\":\"AssessmentItem\",\"identifier\":\"LP_NFT_AS_"
			+ rn + "\",\"metadata\":{\"code\":\"LP_NFT\",\"name\":\"LP_NFT_AS_" + rn
			+ "\",\"type\":\"mtf\",\"template_id\":\"mtf_template_3\",\"lhs_options\":[{\"value\":{\"type\":\"image\",\"asset\":\"grey\"},\"index\":0}],\"rhs_options\":[{\"value\":{\"type\":\"text\",\"asset\":\">\"}},{\"value\":{\"type\":\"text\",\"asset\":\"=\"}},{\"value\":{\"type\":\"mixed\",\"text\":\"<\",\"image\":\"image1\",\"audio\":\"audio1\"},\"answer\":0}],\"max_score\":6,\"partial_scoring\":true}, \"outRelations\": [{\"endNodeId\": \"gcyrdrt\",\"relationType\": \"associatedTo\"}]}}}";

	String jsonUpdateValidAssessment = "{\"request\":{\"assessment_item\":{\"identifier\":\"LP_NFT_AS_" + rn
			+ "\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"code\":\"LP_NFT\",\"name\":\"New Updated Name_" + rn
			+ "\",\"type\":\"mcq\",\"num_answers\":1,\"template\":\"mcq_template_2\",\"template_id\":\"mcq_template_2\",\"qlevel\":\"MEDIUM\",\"owner\":\"Test\",\"title\":\"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ.  ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\",\"question\":\"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ.ಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\",\"model\":{\"img\":{\"type\":\"image\",\"asset\":\"perimeter\"},\"img2\":{\"type\":\"image\",\"asset\":\"smallSquare\"},\"subtext\":\"(= 1  ಚದರ ಸೆಂ.ಮೀ)\"},\"options\":[{\"value\":{\"type\":\"text\",\"asset\":\"12&10\",\"font\":\"Verdana\",\"color\":\"white\",\"fontsize\":\"240\"}},{\"value\":{\"type\":\"text\",\"asset\":\"14&7\",\"font\":\"Verdana\",\"color\":\"white\",\"fontsize\":\"240\"}},{\"value\":{\"type\":\"text\",\"asset\":\"16&8\",\"font\":\"Verdana\",\"color\":\"white\",\"fontsize\":\"240\"}},{\"value\":{\"type\":\"text\",\"asset\":\"12&7\",\"font\":\"Verdana\",\"color\":\"white\",\"fontsize\":\"240\"},\"score\":1}],\"max_score\":1,\"partial_scoring\":false,\"feedback\":\"\"}}}}";

	String jsonSearchValidAssessmentItem = "{\"request\":{\"metadata\":{\"filters\":[{\"property\":\"identifier\",\"operator\":\"=\",\"value\":\"ActualValue\"}]},\"sortOrder\":[{\"sortField\":\"code\",\"sortOrder\":\"ASC\"}],\"startPosition\":0,\"resultSize\":10}}";
	String jsonSearchInvalidAssessmentItem = "{ \"request\": { \"metadata\": {\"filters\":[{\"property\" : \"type\", \"operator\": \"!=\", \"value\": \"mcr\"},{\"property\" : \"owner\", \"operator\": \"=\", \"value\": \"ajsdghj\"}]}}}";
	String jsonCreateAssessmentItemMCQFramework = "{\"request\":{\"assessment_item\":{\"identifier\":\"LP_NFT_AS_FW_"
			+ rn + "\",\"objectType\":\"AssessmentItem\",\"metadata\":{\"code\":\"LP_NFT\",\"name\":\"LP_NFT_AS_" + rn
			+ "\",\"type\":\"mcq\",\"num_answers\":1,\"template\":\"mcq_template_2\",\"template_id\":\"mcq_template_2\",\"qlevel\":\"MEDIUM\",\"owner\":\"Test\",\"title\":\"ಈ ಚಿತ್ರದ ವಿಸ್ತೀರ್ಣವನ್ನು ಹಾಗೂ ಸುತ್ತಳತೆಯನ್ನು ಲೆಕ್ಕ ಮಾಡಿ.  ಸೂಕ್ತ ಉತ್ತರವನ್ನು ಆರಿಸಿರಿ.\",\"question\":\"ವಿಸ್ತೀರ್ಣ = ___________ ಚದರ ಸೆಂ.ಮೀ.ಸುತ್ತಳತೆ= __________ ಚದರ ಸೆಂ.ಮೀ.\",\"model\":{\"img\":{\"type\":\"image\",\"asset\":\"perimeter\"},\"img2\":{\"type\":\"image\",\"asset\":\"smallSquare\"},\"subtext\":\"(= 1  ಚದರ ಸೆಂ.ಮೀ)\"},\"options\":[{\"value\":{\"type\":\"text\",\"asset\":\"12&10\",\"font\":\"Verdana\",\"color\":\"white\",\"fontsize\":\"240\"}},{\"value\":{\"type\":\"text\",\"asset\":\"14&7\",\"font\":\"Verdana\",\"color\":\"white\",\"fontsize\":\"240\"}},{\"value\":{\"type\":\"text\",\"asset\":\"16&8\",\"font\":\"Verdana\",\"color\":\"white\",\"fontsize\":\"240\"}},{\"value\":{\"type\":\"text\",\"asset\":\"12&7\",\"font\":\"Verdana\",\"color\":\"white\",\"fontsize\":\"240\"},\"score\":1}],\"max_score\":1,\"partial_scoring\":false,\"feedback\":\"\"}}}}";

	//Create valid AssessmentItem MCQ
	@Test
	public void createAssessmentItemMCQExpectSuccess200() {
		setURI();
		Response R = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateAssessmentItemMCQ).with().contentType(JSON).when().post("/assessment/v3/items/create")
				.then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");

		// Read and validate the assessment
		setURI();
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/assessment/v3/items/read/" + nodeId).then().spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.assessment_item.identifier");
		String framework = jP1.get("result.assessment_item.framework");
		Assert.assertTrue(framework.equals("NCF"));
		Assert.assertTrue(identifier.equals(nodeId));
	}

	// Create invalid Assessment Item MCQ
	@Test
	public void createInvalidAssessmentItemMCQExpect400() {
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateInvalidAssessmentItemMCQ)
				.with().contentType(JSON).when().post("/assessment/v3/items/create").then().
				//log().all().
				spec(get400ResponseSpec());
	}

	//Create Assessment FTB
	@Test
	public void createAssessmentItemFTBExpectSuccess200() {
		setURI();
		Response R = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateAssessmentItemFTB).with().contentType(JSON).when().post("/assessment/v3/items/create")
				.then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");

		// Read and validate the assessment
		setURI();
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/assessment/v3/items/read/" + nodeId).then().spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.assessment_item.identifier");
		String framework = jP1.get("result.assessment_item.framework");
		Assert.assertTrue(framework.equals("Test"));
		Assert.assertTrue(identifier.equals(nodeId));
	}

	// Create assessment item without framework with proper grade level(With respect to NCF)
	@Test
	public void createAssessmentItemWithValidGradeLevelExpectSuccess200() {
		setURI();
		JSONObject js = new JSONObject(jsonCreateAssessmentItemMCQ);
		js.getJSONObject("request").getJSONObject("assessment_item").getJSONObject("metadata").put("gradeLevel",
				Arrays.asList("Class 1"));
		String jsonCreateAssessmentItemMCQ = js.toString();
		Response R = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateAssessmentItemMCQ).with().contentType(JSON).when().post("/assessment/v3/items/create")
				.then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");

		// Read and validate the assessment
		setURI();
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/assessment/v3/items/read/" + nodeId).then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.assessment_item.identifier");
		List<String> gradeLevel = (List<String>) jP1.get("result.assessment_item.gradeLevel");
		String framework = jP1.get("result.assessment_item.framework");
		Assert.assertTrue(framework.equals("NCF"));
		Assert.assertTrue(gradeLevel.get(0).equals("Class 1"));
		Assert.assertTrue(identifier.equals(nodeId));
	}

	// Create assessment item without framework with invalid grade level(With respect to NCF)
	@Test
	public void createValidAssessmentItemWithInvalidGradeLevelExpect4xx() {
		setURI();
		JSONObject js = new JSONObject(jsonCreateAssessmentItemMCQ);
		js.getJSONObject("request").getJSONObject("assessment_item").getJSONObject("metadata").put("gradeLevel",
				Arrays.asList("Grade 1"));
		String jsonCreateAssessmentItemMCQ = js.toString();
		given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateAssessmentItemMCQ).with()
				.contentType(JSON).when().post("/assessment/v3/items/create").then().
				//log().all().
				spec(get400ResponseSpec());
	}

	// Create assessment item without framework with proper topics(With respect to NCF)
	@Test
	public void createAssessmentItemWithValidTopicsExpectSuccess200() {
		setURI();
		JSONObject js = new JSONObject(jsonCreateAssessmentItemMCQ);
		js.getJSONObject("request").getJSONObject("assessment_item").getJSONObject("metadata")
				.put("topic", Arrays.asList("Topic 1")).put("framework", "NCFCOPY");
		String jsonCreateAssessmentItemMCQ = js.toString();
		Response R = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateAssessmentItemMCQ).with().contentType(JSON).when().post("/assessment/v3/items/create")
				.then()
				//.log().all()
				.spec(get200ResponseSpec()).extract().response();

		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");

		// Read and validate the assessment
		setURI();
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/assessment/v3/items/read/" + nodeId).then()
				//.log().all()
				.spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.assessment_item.identifier");
		List<String> topics = (List<String>) jP1.get("result.assessment_item.topic");
		String framework = jP1.get("result.assessment_item.framework");
		Assert.assertTrue(framework.equals("NCFCOPY"));
		Assert.assertTrue(topics.get(0).equals("Topic 1"));
		Assert.assertTrue(identifier.equals(nodeId));
	}

	// Create assessment item without framework with invalid topics(With respect to NCF)
	@Test
	public void createValidAssessmentItemWithInvalidTopicExpect4xx() {
		setURI();
		JSONObject js = new JSONObject(jsonCreateAssessmentItemMCQ);
		js.getJSONObject("request").getJSONObject("assessment_item").getJSONObject("metadata").put("topic",
				Arrays.asList("Topic 4"));
		String jsonCreateAssessmentItemMCQ = js.toString();
		given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateAssessmentItemMCQ).with()
				.contentType(JSON).when().post("/assessment/v3/items/create").then().
				//log().all().
				spec(get400ResponseSpec());
	}

	// Create assessment item without framework with proper board (With respect to NCF)
	@Test
	public void createAssessmentItemWithValidBoardExpectSuccess200() {
		setURI();
		JSONObject js = new JSONObject(jsonCreateAssessmentItemMCQ);
		js.getJSONObject("request").getJSONObject("assessment_item").getJSONObject("metadata").put("board", "NCERT");
		String jsonCreateAssessmentItemMCQ = js.toString();
		Response R = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateAssessmentItemMCQ).with().contentType(JSON).when().post("/assessment/v3/items/create")
				.then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");

		// Read and validate the assessment
		setURI();
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/assessment/v3/items/read/" + nodeId).then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.assessment_item.identifier");
		String framework = jP1.get("result.assessment_item.framework");
		String board = jP1.get("result.assessment_item.board");
		Assert.assertTrue(framework.equals("NCF"));
		Assert.assertTrue(board.equals("NCERT"));
		Assert.assertTrue(identifier.equals(nodeId));
	}

	// Create assessment item without framework with invalid grade level(With respect to NCF)
	@Test
	public void createValidAssessmentItemWithInvalidBoardExpect4xx() {
		setURI();
		JSONObject js = new JSONObject(jsonCreateAssessmentItemMCQ);
		js.getJSONObject("request").getJSONObject("assessment_item").getJSONObject("metadata").put("board",
				"TestBoard01");
		String jsonCreateAssessmentItemMCQ = js.toString();
		given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateAssessmentItemMCQ).with()
				.contentType(JSON).when().post("/assessment/v3/items/create").then().
				//log().all().
				spec(get400ResponseSpec());
	}

	// Create assessment item without framework with proper subject (With respect to NCF)
	@Test
	public void createAssessmentItemWithValidSubjectExpectSuccess200() {
		setURI();
		JSONObject js = new JSONObject(jsonCreateAssessmentItemMCQ);
		js.getJSONObject("request").getJSONObject("assessment_item").getJSONObject("metadata").put("subject",
				"English");
		String jsonCreateAssessmentItemMCQ = js.toString();
		Response R = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateAssessmentItemMCQ).with().contentType(JSON).when().post("/assessment/v3/items/create")
				.then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");

		// Read and validate the assessment
		setURI();
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/assessment/v3/items/read/" + nodeId).then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.assessment_item.identifier");
		String framework = jP1.get("result.assessment_item.framework");
		String subject = jP1.get("result.assessment_item.subject");
		Assert.assertTrue(framework.equals("NCF"));
		Assert.assertTrue(subject.equals("English"));
		Assert.assertTrue(identifier.equals(nodeId));
	}

	// Create assessment item without framework with invalid grade level(With respect to NCF)
	@Test
	public void createValidAssessmentItemWithInvalidSubjectExpect4xx() {
		setURI();
		JSONObject js = new JSONObject(jsonCreateAssessmentItemMCQ);
		js.getJSONObject("request").getJSONObject("assessment_item").getJSONObject("metadata").put("subject",
				"Subject 1");
		String jsonCreateAssessmentItemMCQ = js.toString();
		given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateAssessmentItemMCQ).with()
				.contentType(JSON).when().post("/assessment/v3/items/create").then().
				//log().all().
				spec(get400ResponseSpec());
	}

	// Create assessment item without framework with proper subject (With respect to NCF)
	@Test
	public void createAssessmentItemWithValidMediumExpectSuccess200() {
		setURI();
		JSONObject js = new JSONObject(jsonCreateAssessmentItemMCQ);
		js.getJSONObject("request").getJSONObject("assessment_item").getJSONObject("metadata").put("medium", "English");
		String jsonCreateAssessmentItemMCQ = js.toString();
		Response R = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateAssessmentItemMCQ).with().contentType(JSON).when().post("/assessment/v3/items/create")
				.then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");

		// Read and validate the assessment
		setURI();
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/assessment/v3/items/read/" + nodeId).then().log().all().spec(get200ResponseSpec()).extract()
				.response();

		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.assessment_item.identifier");
		String framework = jP1.get("result.assessment_item.framework");
		String medium = jP1.get("result.assessment_item.medium");
		Assert.assertTrue(framework.equals("NCF"));
		Assert.assertTrue(medium.equals("English"));
		Assert.assertTrue(identifier.equals(nodeId));
	}

	// Create assessment item without framework with invalid grade level(With respect to NCF)
	@Test
	public void createValidAssessmentItemWithInvalidMediumExpect4xx() {
		setURI();
		JSONObject js = new JSONObject(jsonCreateAssessmentItemMCQ);
		js.getJSONObject("request").getJSONObject("assessment_item").getJSONObject("metadata").put("medium", "med 1");
		String jsonCreateAssessmentItemMCQ = js.toString();
		given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateAssessmentItemMCQ).with()
				.contentType(JSON).when().post("/assessment/v3/items/create").then().
				//log().all().
				spec(get400ResponseSpec());
	}

	// Create invalid Assessment Item
	@Test
	public void createInvalidAssessmentItemFTBExpect400() {
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateInvalidAssessmentItemFTB)
				.with().contentType(JSON).when().post("/assessment/v3/items/create").then().
				//log().all().
				spec(get400ResponseSpec());
	}

	//Create Valid MTF Assessment
	@Test
	public void createAssessmentItemMTFExpectSuccess200() {
		setURI();
		Response R = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateAssessmentItemMTF).with().contentType(JSON).when().post("/assessment/v3/items/create")
				.then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");

		// Read and validate the assessment
		setURI();
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/assessment/v3/items/read/" + nodeId).then().spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.assessment_item.identifier");
		Assert.assertTrue(identifier.equals(nodeId));
	}

	// Create Invalid MTF Assessment
	@Test
	public void createInvalidAssessmentItemMTFExpectSuccess200() {
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonCreateInvalidAssessmentItemMTF)
				.with().contentType(JSON).when().post("/assessment/v3/items/create").then().
				//log().all().
				spec(get400ResponseSpec());
	}

	//Update Assessment
	@Test
	public void updateValidAssessmentExpectSuccess200() {
		setURI();
		Response R = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateAssessmentItemMCQ).with().contentType(JSON).when().post("/assessment/v3/items/create")
				.then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");

		// Update the assessment item name
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonUpdateValidAssessment).with()
				.contentType(JSON).when().patch("/assessment/v3/items/update/" + nodeId).then().
				//log().all().
				spec(get200ResponseSpec());

		// Read and validate the assessment
		setURI();
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/assessment/v3/items/read/" + nodeId).then().spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String name = jP1.get("result.assessment_item.name");
		Assert.assertTrue(name.contains("New Updated Name"));

	}

	// Update Invalid Assessment Item
	@Test
	public void updateInvalidAssessmentExpect400() {
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken)).body(jsonUpdateValidAssessment).with()
				.contentType("application/json").when().patch("/assessment/v3/items/update/vamsmQA7678789").then().
				//log().all().
				spec(get404ResponseSpec());
	}

	//Read Assessment
	@Test
	public void getValidAssessmentItemExpectSuccess200() {
		setURI();
		Response R = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateAssessmentItemMCQ).with().contentType(JSON).when().post("/assessment/v3/items/create")
				.then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");

		// Read and validate the assessment
		setURI();
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/assessment/v3/items/read/" + nodeId).then().spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String identifier = jP1.get("result.assessment_item.identifier");
		Assert.assertTrue(identifier.equals(nodeId));
	}

	// Get invalid assessment item
	@Test
	public void getInvalidAssessmentItemExpect400() {
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/assessment/v3/items/read/am,gnsdk").then().
				////log().all().
				spec(get404ResponseSpec());
	}

	// Get list assessment items
	@Test
	public void listAssessmentItemsExpectSuccess200() {
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken)).body("{\"request\":{}}").when()
				.post("/assessment/v3/items/list").then().
				//log().all().
				spec(get200ResponseSpec());
	}

	//Search Assessment with valid request
	@Test
	public void searchValidAssessmentItemExpectSuccess200() {
		setURI();
		Response R = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateAssessmentItemMCQ).with().contentType(JSON).when().post("/assessment/v3/items/create")
				.then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");

		// Search and validate the assessment
		jsonSearchValidAssessmentItem = jsonSearchValidAssessmentItem.replace("ActualValue", nodeId);
		setURI();
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonSearchValidAssessmentItem).with().contentType(JSON).when().post("/assessment/v3/items/search")
				.then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		ArrayList<String> name = jP1.get("result.assessment_items.name");
		ArrayList<String> status = jP1.get("result.assessment_items.status");
		Assert.assertTrue(status.contains("Live"));
		Assert.assertTrue(name.contains(nodeId));
	}

	// Search assessment with invalid request
	@Test
	public void searchInvalidValidAssessmentItemExpect200() {
		setURI();
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonSearchValidAssessmentItem).with().contentType(JSON).when().post("/assessment/v3/items/search")
				.then().
				////log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		ArrayList<String> assessmentItems = jP1.get("result.assessment_items");
		Assert.assertTrue(assessmentItems.isEmpty());
	}

	//Delete Assessment with valid id
	@Test
	public void deleteValidAssessmentExpectSuccess200() {
		setURI();
		Response R = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateAssessmentItemMCQ).with().contentType(JSON).when().post("/assessment/v3/items/create")
				.then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");

		// Read and validate the assessment
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/assessment/v3/items/read/" + nodeId).then().spec(get200ResponseSpec());

		//Retiring the assessment
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.delete("/assessment/v3/items/retire/" + nodeId).then().
				//log().all().
				spec(get200ResponseSpec());
	}

	//Create valid AssessmentItem MCQ and validate default framework
	@Test
	public void createAssessmentItemMCQExpectFramework() {
		setURI();
		Response R = given().spec(getRequestSpecification(contentType, userId, APIToken))
				.body(jsonCreateAssessmentItemMCQFramework).with().contentType(JSON).when()
				.post("/assessment/v3/items/create").then().
				//log().all().
				spec(get200ResponseSpec()).extract().response();

		JsonPath jP = R.jsonPath();
		String nodeId = jP.get("result.node_id");

		// Read and validate the assessment
		setURI();
		Response R1 = given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.get("/assessment/v3/items/read/" + nodeId).then().spec(get200ResponseSpec()).extract().response();

		JsonPath jP1 = R1.jsonPath();
		String framework = jP1.get("result.assessment_item.framework");
		Assert.assertTrue(framework.equals("NCF"));

		//Retiring the assessment
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.delete("/assessment/v3/items/retire/" + nodeId).then().
				//log().all().
				spec(get200ResponseSpec());
	}

	// Delete Assessment with invalid id
	@Ignore
	public void deleteInvalidAssessmentExpect404() {
		setURI();
		given().spec(getRequestSpecification(contentType, userId, APIToken)).when()
				.delete("/assessment/v3/items/retire/akcjdsjfbas").then().
				//log().all().
				spec(get404ResponseSpec());
	}
}
