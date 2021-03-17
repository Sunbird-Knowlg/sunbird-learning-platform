package org.sunbird.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.sunbird.platform.domain.BaseTest;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

@Ignore
public class CollectionTestCases extends BaseTest{
	
	private String PROCESSING = "Processing";
	private String PENDING = "Pending";
	
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
	String jsonCreateValidContent = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_"+rn+"\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Story\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.ecml-archive\",\"tags\":[\"LP_functionalTest\"], \"owner\": \"EkStep\"}}}";
	String jsonCreateContentCollection = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_Collection_"+rn+"\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"Collection\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"owner\": \"EkStep\", \"children\": [{ \"identifier\": \"id1\", \"index\":0}, { \"identifier\": \"id2\",\"index\":1}]}}}";
	String jsonCreateCollectionWithIndex = "{\"request\": {\"content\": {\"identifier\": \"LP_FT_Collection_"+rn+"\",\"osId\": \"org.sunbird.quiz.app\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_QA\",\"name\": \"LP_FT_"+rn+"\",\"language\":[\"English\"],\"contentType\": \"TextBookUnit\",\"code\": \"Test_QA\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"owner\": \"EkStep\", \"children\": [{\"identifier\": \"id1\",\"index\":0},{ \"identifier\": \"id2\",\"index\":1},{\"identifier\": \"id3\",\"index\":2}]}}}";
	String jsonContentClean = "{\"request\": {\"searchProperty\": \"name\",\"searchOperator\": \"startsWith\",\"searchString\": \"LP_FT\"}}";
	
	static ClassLoader classLoader = ContentPublishWorkflowTests.class.getClassLoader();
	static URL url = classLoader.getResource("DownloadedFiles");
	static File downloadPath;
	static File path = new File(classLoader.getResource("UploadFiles/").getFile());
	
	@BeforeClass
	public static void setup() throws URISyntaxException{
		downloadPath = new File(url.toURI().getPath());		
	}	
	
	@AfterClass
	public static void end() throws IOException{
		FileUtils.cleanDirectory(downloadPath);
		
	}
	
	// Create Collection with Index
	
	@Test
	public void createCollectionWithIndexExpectSuccess200(){
		contentCleanUp();
		String node1 = null;
		String node2 = null;
		String node3 = null;
		int count = 1;
		while(count<=4){
			setURI();
			int rn = generateRandomInt(999, 1999);
			JSONObject js = new JSONObject(jsonCreateValidContent);
			if(count==1){
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_FT_T_"+rn+"").put("name", "LP_FT_T-"+rn+"");	
			}
			if(count==2){
				js.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("identifier", "LP_FT_T_"+rn+"").put("name", "LP_FT_T-"+rn+"");	
			}
			if(count==3){
				js.getJSONObject("request").getJSONObject("content").put("identifier", "LP_FT_T_"+rn+"").put("name", "LP_FT_T-"+rn+"");	
			}
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
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			String nodeId = jp.get("result.node_id");
			if(count==1){
				node1 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpecification(uploadContentType, userId, APIToken)).
				multiPart(new File(path+"/uploadContent.zip")).
				when().
				post("/content/v3/upload/"+node1).
				then().
				//log().all().
				spec(get200ResponseSpec());

			}
			if(count==2){
				node2 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpecification(uploadContentType, userId, APIToken)).
				multiPart(new File(path+"/tweenAndaudioSprite.zip")).
				when().
				post("/content/v3/upload/"+node2).
				then().
				//log().all().
				spec(get200ResponseSpec());
			}
			if(count==3){
				node3 = nodeId;

				// Upload Content
				setURI();
				given().
				spec(getRequestSpecification(uploadContentType, userId, APIToken)).
				multiPart(new File(path+"/tweenAndaudioSprite.zip")).
				when().
				post("/content/v3/upload/"+node3).
				then().
				//log().all().
				spec(get200ResponseSpec());
				
				// Publish content
				setURI();
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body("{\"request\":{\"content\":{\"lastPublishedBy\":\"Test\"}}}").
				when().
				post("/content/v3/publish/"+node3).
				then().
				//log().all().
				spec(get200ResponseSpec());
			
			}
			count++;
		}
		// Create Collection with index
		setURI();
		JSONObject js2 = new JSONObject(jsonCreateCollectionWithIndex);
		js2.getJSONObject("request").getJSONObject("content").put("visibility", "Parent").put("contentType", "collection");
		String jsonCreateCollectionWithIndex = js2.toString();
		jsonCreateCollectionWithIndex = jsonCreateCollectionWithIndex.replace("id1", node1).replace("id2", node2).replace("id3", node3);
		Response R1 =
				given().
				spec(getRequestSpecification(contentType, userId, APIToken)).
				body(jsonCreateCollectionWithIndex).
				with().
				contentType(JSON).
				when().
				post("content/v3/create").
				then().
				log().all().
				spec(get200ResponseSpec()).
				extract().
				response();

		JsonPath jP1 = R1.jsonPath();
		String nodeId = jP1.get("result.node_id");
		validateIndex(nodeId, node1, node2, node3);
		
		// Review the Collection
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken)).
		body("{\"request\":{\"content\":{}}}").
		when().
		post("/content/v3/review/"+nodeId).
		then().
		//log().all().
		spec(get200ResponseSpec());
		validateIndex(nodeId, node1, node2, node3);
		
		// Publish the Collection
		setURI();
		given().
		spec(getRequestSpecification(contentType, validuserId, APIToken)).
		then().
		get("/learning/v2/content/publish/" + nodeId);
		// log().all().
		//spec(get200ResponseSpec());
		validateIndex(nodeId, node1, node2, node3);

	}
	
	// Validate collection order
	
	public void validateIndex(String nodeId, String node1, String node2, String node3){
	
	// Get content and validate the index
	setURI();
	Response R2 = 
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			when().
			get("content/v3/read/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec()).
			extract().
			response();
	
	JsonPath jP2 = R2.jsonPath();
	ArrayList<String> identifier = jP2.get("result.content.children.identifier");
	ArrayList<String> index = jP2.get("result.content.children.index");
 	if(identifier.equals(node1)){
		Assert.assertTrue(index.equals(0));
	}
 	if(identifier.equals(node2)){
		Assert.assertTrue(index.equals(1));
	}
 	if(identifier.equals(node3)){
		Assert.assertTrue(index.equals(2));
	}
}
	// Async Publish validations - Collection
	public void asyncPublishValidations(ArrayList<String> identifier1, String status, String nodeId, String c_identifier, String node1, String node2){
		if(status.equals(PROCESSING) || status.equals(PENDING)){
			for (int i=1000; i<=30000; i=i+1000){
				try{Thread.sleep(i);}catch(InterruptedException e){System.out.println(e);} 
				setURI();
				Response R3 =
						given().
						spec(getRequestSpecification(userId, contentType, APIToken)).
						when().
						get("/content/v3/read"+nodeId).
						then().
						//log().all().
						spec(get200ResponseSpec()).
						extract().
						response();

				// Validate the response
				JsonPath jp3 = R3.jsonPath();
				String statusUpdated = jp3.get("result.content.status");
				if (statusUpdated.equals(PROCESSING) || statusUpdated.equals(PENDING)){
					//System.out.println(statusUpdated);
					i++;
				}
				if (statusUpdated.equals("Live")){
					Assert.assertTrue(c_identifier.equals(nodeId)&&identifier1.contains(node1)&&identifier1.contains(node2));
					break;
				}
			}
		}
		else if (status.equals("Live")){
			Assert.assertTrue(c_identifier.equals(nodeId)&&identifier1.contains(node1)&&identifier1.contains(node2));
		}
	}

	
	// Content clean up	
	public void contentCleanUp(){
		setURI();
		given().
		body(jsonContentClean).
		with().
		contentType(JSON).
		when().
		post("learning/v1/exec/content_qe_deleteContentBySearchStringInField");
	}

	
}
