package org.sunbird.platform.domain;

import static com.jayway.restassured.RestAssured.given;

import java.io.File;

import org.sunbird.platform.content.ContentPublishWorkflowTests;
import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
@Ignore
public class DataExportImportTestCases extends BaseTest{
	
	static ClassLoader classLoader = ContentPublishWorkflowTests.class.getClassLoader();
	static File path = new File(classLoader.getResource("UploadFiles/").getFile());
	
	String jsonExportContentValid = "{\"request\":{\"search_criteria\":{\"nodeType\":\"DATA_NODE\",\"objectType\":\"Content\",\"metadata\":[{\"filters\":[{\"property\":\"contentType\",\"operator\":\"in\",\"value\":[\"Story\"]},{\"property\":\"gradeLevel\",\"operator\":\"in\",\"value\":\"Grade 5\"},{\"property\":\"language\",\"operator\":\"in\",\"value\":\"Telugu\"}]}]}}}";
	String jsonExportQueryValid = "{\"request\":{\"search_criteria\":{\"nodeType\":\"DATA_NODE\",\"objectType\":\"Content\",\"metadata\":[{\"filters\":[{blank}]}]}}}";
	String jsonExportQueryNoFilter = "{\"request\":{\"search_criteria\":{\"nodeType\":\"DATA_NODE\",\"objectType\":\"Content\",\"metadata\":[]}}}";
	
	@BeforeClass
	public static void init() throws JSONException{
		//RestAssured.registerParser("text/csv", Parser.JSON);
	}
	
	// Upload valid csv file
	
	@Test
	public void importValidCSVExpectSuccess200(){
		setURI();
		given().
			spec(getRequestSpec(uploadContentType, validuserId)).
			multiPart(new File(path+"/ValidDomain.csv")).
			when().
			post("/learning/taxonomy/domain").
			then().
			//log().all().
			spec(get200ResponseSpec())	;			
	}
	
	// Upload blank csv file
	
	@Test
	public void importBlankCSVExpect400(){
		setURI();
		given().
			spec(getRequestSpec(uploadContentType, validuserId)).
			multiPart(new File(path+"/BlankFile.csv")).
			when().
			post("/learning/taxonomy/domain").
			then().
			spec(get200ResponseSpec());
	}
	
	// Upload csv file with valid and irrelevant data
	
	@Test
	public void importCSVValidAndInvalidData(){
		setURI();
		given().
			spec(getRequestSpec(uploadContentType, validuserId)).
			multiPart(new File(path+"/IrrelevantData.csv")).
			when().
			post("learning/taxonomy/domain").
			then().
			spec(get200ResponseSpec());
	}
	
	// Upload csv file with blank and valid data
	
	@Test
	public void importCSVValidAndBlankData(){
		setURI();
		given().
			spec(getRequestSpec(uploadContentType, validuserId)).
			multiPart(new File(path+"/ValidAndBlank.csv")).
			when().
			post("learning/taxonomy/domain").
			then().
			spec(get200ResponseSpec());
	}
	
	// Upload unsupported file formats
	
	@Test
	public void importUnsupportedFileFormatExpect400(){
		setURI();
		given().
			spec(getRequestSpec(uploadContentType, validuserId)).
			multiPart(new File(path+"/carpenter.png")).
			when().
			post("learning/taxonomy/domain").
			then().
			spec(get400ResponseSpec());
	}
	
	// Export content data
	
	@Test
	public void exportContentValidExpectSuccess200(){
		setURI();
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonExportContentValid).
			when().
			post("learning/taxonomy/domain/export").
			then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	// Export Assets data
	
	@Test 
	public void exportAssetsValidExpectSuccess200(){
		setURI();
		String jsonExportAssetsValid = jsonExportQueryValid.replace("blank", "\"property\" : \"contentType\", \"operator\": \"in\", \"value\": [\"Asset\"]");
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonExportAssetsValid).
			when().
			post("learning/taxonomy/domain/export").
			then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	// Export Templates data
	
	@Test
	public void exportTemplatesValidExpectSuccess200(){
		setURI();
		String jsonExportTemplateValid = jsonExportQueryValid.replace("blank", "\"property\" : \"contentType\", \"operator\": \"in\", \"value\": [\"Template\"]");
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonExportTemplateValid).
			when().
			post("learning/taxonomy/domain/export").
			then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	// Export AssessmentItems data
	
	@Test
	public void exportItemsValidExpectSuccess200(){
		setURI();
		String jsonExportItemsValid = jsonExportQueryValid.replace("Content", "AssessmentItem").replace("blank", "\"property\" : \"type\", \"operator\": \"in\", \"value\": [\"ftb\"]");
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonExportItemsValid).
			when().
			post("learning/taxonomy/domain/export").
			then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	// Export Hindi words data

	@Test
	public void exportHindiWordExpectSuccess200(){
		setURI();
		String jsonExportHindiWordsValid = jsonExportQueryValid.replace("Content", "Word").replace("blank", "\"property\" : \"pos\", \"operator\": \"in\", \"value\": [\"adverb\"]");
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonExportHindiWordsValid).
			when().
			post("language/v1/hi/export?format=csv").
			then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	// Export Kannada words data

	@Test
	public void exportKannadaWordExpectSuccess200(){
		setURI();
		String jsonExportHindiWordsValid = jsonExportQueryValid.replace("Content", "Word").replace("blank", "\"property\" : \"pos\", \"operator\": \"in\", \"value\": [\"adverb\"]");
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonExportHindiWordsValid).
			when().
			post("language/v1/ka/export?format=csv").
			then().
			log().all().
			spec(get200ResponseSpec());
	}

	// Export English words data
	
	@Test
	public void exportEnglishWordExpectSuccess200(){
		setURI();
		String jsonExportHindiWordsValid = jsonExportQueryValid.replace("Content", "Word").replace("blank", "\"property\" : \"pos\", \"operator\": \"in\", \"value\": [\"pronoun\"]");
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonExportHindiWordsValid).
			when().
			post("language/v1/en/export?format=csv").
			then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	// Export Concepts data

	@Test
	public void exportConceptValidExpectSuccess200(){
		setURI();
		String  jsonExportConceptsValid = jsonExportQueryValid.replace("Content", "Concept").replace("blank", "\"property\" : \"subject\", \"operator\": \"in\", \"value\": [\"numeracy\"]");
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonExportConceptsValid).
			when().
			post("learning/taxonomy/domain/export").
			then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	// Export Methods data

	@Test
	public void exportMethodValidExpectSuccess200(){
		setURI();
		String  jsonExportMethodValid = jsonExportQueryValid.replace("Content", "Method").replace("blank", "\"property\" : \"complexity\", \"operator\": \"in\", \"value\": [\"Medium\"]");
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonExportMethodValid).
			when().
			post("learning/taxonomy/domain/export").
			then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	// Export domain data with invalid request
	
	@Test
	public void exportDomainDataInvalid(){
		setURI();
		String  jsonExportDomainInvalid = jsonExportQueryNoFilter.replace("Content", "testQA");
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonExportDomainInvalid).
			when().
			post("learning/taxonomy/domain/export").
			then().
			log().all().
			spec(get200ResponseSpec());
	}
	
	// Export language data with invalid request
	
	@Test
	public void exportLanguageDataInvalid(){
		setURI();
		String jsonExportLanguageInvalid = jsonExportQueryNoFilter.replace("Content", "testQA");
		given().
			spec(getRequestSpec(contentType, validuserId)).
			body(jsonExportLanguageInvalid).
			when().
			post("language/v1/en/export?format=csv").
			then().
			log().all().
			spec(get200ResponseSpec());
	}
}

