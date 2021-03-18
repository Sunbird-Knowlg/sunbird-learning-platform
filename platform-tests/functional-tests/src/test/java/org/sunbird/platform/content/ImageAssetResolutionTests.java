package org.sunbird.platform.content;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

import java.io.File;

import org.sunbird.platform.domain.BaseTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
@Ignore
public class ImageAssetResolutionTests extends BaseTest {


	int rn = generateRandomInt(0, 9999999);
	//String jsonCreateValidContent = "{ \"owner\": \"EkStep\", \"identifier\": \"LP_FT_"+rn+"\", \"code\": \"assetMedia\", \"visibility\": \"Default\", \"description\": \"test asset1\", \"language\": [ \"English\" ], \"mediaType\": \"content\", \"mimeType\": \"image jpeg\", \"osId\": \"org.sunbird.quiz.app\", \"languageCode\": \"en\", \"name\": \"test asset1\", \"contentType\": \"Asset\", }";
	String jsonCreateValidContent = "{ \"request\": { \"content\": { \"owner\": \"EkStep\", \"identifier\": \"LP_FT_"+rn+"\", \"code\": \"assetMedia\", \"visibility\": \"Default\", \"description\": \"test asset1\", \"language\": [ \"English\" ], \"mediaType\": \"image\", \"mimeType\": \"image/jpeg\", \"osId\": \"org.sunbird.quiz.app\", \"languageCode\": \"en\", \"name\": \"test asset1\", \"contentType\": \"Asset\" } } }";
	
	static ClassLoader classLoader = ImageAssetResolutionTests.class.getClassLoader();
	static File path = new File(classLoader.getResource("UploadFiles/").getFile());
	static String nodeId;
	
	@Before
	public void createNewContent(){
		if(nodeId==null){
			setURI();
			Response R =
					given().
					spec(getRequestSpec(contentType, validuserId)).
					body(jsonCreateValidContent).
					with().
					contentType(JSON).
					when().
					post("/learning/v2/content").
					then().
					//log().all().
					spec(get200ResponseSpec()).
					extract().
					response();

			// Extracting the JSON path
			JsonPath jp = R.jsonPath();
			 nodeId = jp.get("result.node_id");
			 System.out.println("nodeId="+nodeId);
		}

	}
	
	@Test
	public void ultrahighResolutionImageUpload(){
		try {
			// Upload Content
			setURI();
			given().
			spec(getRequestSpec(uploadContentType, validuserId)).
			multiPart(new File(path+"/ultrahd.jpg")).
			when().
			post("/learning/v2/content/upload/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());
	
			Thread.sleep(2000);
			
			// Upload Content
			setURI();
			Response R1 = 
			given().
			spec(getRequestSpec(uploadContentType, validuserId)).
			when().
			get("/learning/v2/content/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec()).
			extract().
			response();
			
			// Extracting the JSON path
			JsonPath jp1 = R1.jsonPath();
			String highVariant = jp1.get("result.content.variants.high");
			String mediumVariant = jp1.get("result.content.variants.medium");
			String lowVariant = jp1.get("result.content.variants.low");

			String expectedHighVariantUrl  = "https://.*/content/.*.high.jpg";
			String expectedMediumVariantUrl  = "https://.*/content/.*.medium.jpg";
			String expectedLowVariantUrl  = "https://.*/content/.*.low.jpg";
			
			Assert.assertTrue(highVariant.matches(expectedHighVariantUrl));
			Assert.assertTrue(mediumVariant.matches(expectedMediumVariantUrl));
			Assert.assertTrue(lowVariant.matches(expectedLowVariantUrl));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Test
	public void highResolutionImageUpload(){
		try {
			// Upload Content
			setURI();
			given().
			spec(getRequestSpec(uploadContentType, validuserId)).
			multiPart(new File(path+"/highQ.jpg")).
			when().
			post("/learning/v2/content/upload/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());
	
			Thread.sleep(2000);
			
			// Upload Content
			setURI();
			Response R1 = 
			given().
			spec(getRequestSpec(uploadContentType, validuserId)).
			when().
			get("/learning/v2/content/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec()).
			extract().
			response();
			
			// Extracting the JSON path
			JsonPath jp1 = R1.jsonPath();
			String highVariant = jp1.get("result.content.variants.high");
			String mediumVariant = jp1.get("result.content.variants.medium");
			String lowVariant = jp1.get("result.content.variants.low");
			String actual = jp1.get("result.content.downloadUrl");
			
			String expectedHighVariantUrl  = "https://.*/content/.*.high.jpg";
			String expectedMediumVariantUrl  = "https://.*/content/.*.medium.jpg";
			String expectedLowVariantUrl  = "https://.*/content/.*.low.jpg";
			
			Assert.assertFalse(highVariant.matches(expectedHighVariantUrl));
			Assert.assertTrue(highVariant.equalsIgnoreCase(actual));
			Assert.assertTrue(mediumVariant.matches(expectedMediumVariantUrl));
			Assert.assertTrue(lowVariant.matches(expectedLowVariantUrl));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void mediumResolutionImageUpload(){
		try {
			// Upload Content
			setURI();
			given().
			spec(getRequestSpec(uploadContentType, validuserId)).
			multiPart(new File(path+"/medQ.jpg")).
			when().
			post("/learning/v2/content/upload/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());
	
			Thread.sleep(2000);
			
			// Upload Content
			setURI();
			Response R1 = 
			given().
			spec(getRequestSpec(uploadContentType, validuserId)).
			when().
			get("/learning/v2/content/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec()).
			extract().
			response();
			
			// Extracting the JSON path
			JsonPath jp1 = R1.jsonPath();
			String highVariant = jp1.get("result.content.variants.high");
			String mediumVariant = jp1.get("result.content.variants.medium");
			String lowVariant = jp1.get("result.content.variants.low");
			String actual = jp1.get("result.content.downloadUrl");
			
			String expectedHighVariantUrl  = "https://.*/content/.*.high.jpg";
			String expectedMediumVariantUrl  = "https://.*/content/.*.medium.jpg";
			String expectedLowVariantUrl  = "https://.*/content/.*.low.jpg";
			
			Assert.assertFalse(highVariant.matches(expectedHighVariantUrl));
			Assert.assertTrue(highVariant.equalsIgnoreCase(actual));
			Assert.assertFalse(mediumVariant.matches(expectedMediumVariantUrl));
			Assert.assertTrue(mediumVariant.equalsIgnoreCase(actual));
			Assert.assertTrue(lowVariant.matches(expectedLowVariantUrl));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void extralowResolutionImageUpload(){
		try {
			// Upload Content
			setURI();
			given().
			spec(getRequestSpec(uploadContentType, validuserId)).
			multiPart(new File(path+"/extra-min.jpg")).
			when().
			post("/learning/v2/content/upload/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());
	
			Thread.sleep(2000);
			
			// Upload Content
			setURI();
			Response R1 = 
			given().
			spec(getRequestSpec(uploadContentType, validuserId)).
			when().
			get("/learning/v2/content/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec()).
			extract().
			response();
			
			// Extracting the JSON path
			JsonPath jp1 = R1.jsonPath();
			String highVariant = jp1.get("result.content.variants.high");
			String mediumVariant = jp1.get("result.content.variants.medium");
			String lowVariant = jp1.get("result.content.variants.low");
			String actual = jp1.get("result.content.downloadUrl");
			
			String expectedHighVariantUrl  = "https://.*/content/.*.high.jpg";
			String expectedMediumVariantUrl  = "https://.*/content/.*.medium.jpg";
			String expectedLowVariantUrl  = "https://.*/content/.*.low.jpg";
			
			Assert.assertFalse(highVariant.matches(expectedHighVariantUrl));
			Assert.assertTrue(highVariant.equalsIgnoreCase(actual));
			Assert.assertFalse(mediumVariant.matches(expectedMediumVariantUrl));
			Assert.assertTrue(mediumVariant.equalsIgnoreCase(actual));
			Assert.assertFalse(lowVariant.matches(expectedLowVariantUrl));
			Assert.assertTrue(lowVariant.equalsIgnoreCase(actual));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void audioUpload(){
		try {
			// Upload Content
			setURI();
			given().
			spec(getRequestSpec(uploadContentType, validuserId)).
			multiPart(new File(path+"/sample.mp3")).
			when().
			post("/learning/v2/content/upload/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());
	
			Thread.sleep(2000);
			
			// Upload Content
			setURI();
			Response R1 = 
			given().
			spec(getRequestSpec(uploadContentType, validuserId)).
			when().
			get("/learning/v2/content/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec()).
			extract().
			response();
			
			// Extracting the JSON path
			JsonPath jp1 = R1.jsonPath();
			String highVariant = jp1.get("result.content.variants.high");
			String mediumVariant = jp1.get("result.content.variants.medium");
			String lowVariant = jp1.get("result.content.variants.low");
			String actual = jp1.get("result.content.downloadUrl");
						
			Assert.assertNotNull(actual);
			Assert.assertNull(highVariant);
			Assert.assertNull(mediumVariant);
			Assert.assertNull(lowVariant);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void notSupportedImagesUpload(){
		try {
			// Upload Content
			setURI();
			given().
			spec(getRequestSpec(uploadContentType, validuserId)).
			multiPart(new File(path+"/bike.bmp")).
			when().
			post("/learning/v2/content/upload/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec());
	
			Thread.sleep(2000);
			
			// Upload Content
			setURI();
			Response R1 = 
			given().
			spec(getRequestSpec(uploadContentType, validuserId)).
			when().
			get("/learning/v2/content/"+nodeId).
			then().
			//log().all().
			spec(get200ResponseSpec()).
			extract().
			response();
			
			// Extracting the JSON path
			JsonPath jp1 = R1.jsonPath();
			String highVariant = jp1.get("result.content.variants.high");
			String mediumVariant = jp1.get("result.content.variants.medium");
			String lowVariant = jp1.get("result.content.variants.low");
			String actual = jp1.get("result.content.downloadUrl");
						
			Assert.assertNotNull(actual);
			Assert.assertNull(highVariant);
			Assert.assertNull(mediumVariant);
			Assert.assertNull(lowVariant);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
