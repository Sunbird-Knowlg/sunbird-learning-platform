package org.sunbird.platform.content;

import org.sunbird.platform.domain.BaseTest;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;

import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;
import org.testng.Assert;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

public class DefinitionsV3Tests extends BaseTest {

	String jsonUpdateMethodDefinition = "{\"definitionNodes\":[{\"objectType\":\"Domain\",\"properties\":[{\"propertyName\":\"name\",\"title\":\"Name\",\"description\":\"Name of the domain\",\"category\":\"general\",\"dataType\":\"Text\",\"required\":true,\"displayProperty\":\"Editable\",\"defaultValue\":\"\",\"renderingHints\":\"{'inputType': 'text', 'order': 1}\",\"indexed\":true},{\"propertyName\":\"code\",\"title\":\"Code\",\"description\":\"Unique code to identify the domain\",\"category\":\"general\",\"dataType\":\"Text\",\"required\":true,\"displayProperty\":\"Editable\",\"defaultValue\":\"\",\"renderingHints\":\"{'inputType': 'text', 'order': 2}\",\"indexed\":true},{\"propertyName\":\"keywords\",\"title\":\"Keywords\",\"description\":\"Keywords related to this domain\",\"category\":\"general\",\"dataType\":\"List\",\"required\":false,\"displayProperty\":\"Editable\",\"defaultValue\":\"\",\"renderingHints\":\"{'inputType': 'table', 'order': 7}\",\"indexed\":true},{\"propertyName\":\"status\",\"title\":\"Status\",\"description\":\"Status of the domain\",\"category\":\"lifeCycle\",\"dataType\":\"Select\",\"range\":[\"Draft\",\"Live\",\"Retired\"],\"required\":false,\"displayProperty\":\"Editable\",\"defaultValue\":\"Draft\",\"renderingHints\":\"{'inputType': 'select', 'order': 9}\",\"indexed\":true},{\"propertyName\":\"numDimensions\",\"title\":\"No of dimensions in this domain\",\"description\":\"\",\"category\":\"analytics\",\"dataType\":\"Number\",\"required\":false,\"displayProperty\":\"Readonly\",\"defaultValue\":\"\",\"renderingHints\":\"{'order': 19}\",\"indexed\":true},{\"propertyName\":\"numConcepts\",\"title\":\"Number of concepts in this domain\",\"description\":\"\",\"category\":\"analytics\",\"dataType\":\"Number\",\"required\":false,\"displayProperty\":\"Readonly\",\"defaultValue\":\"\",\"renderingHints\":\"{'order': 20}\",\"indexed\":true},{\"propertyName\":\"numMethods\",\"title\":\"Number of methods in this domain\",\"description\":\"\",\"category\":\"analytics\",\"dataType\":\"Number\",\"required\":false,\"displayProperty\":\"Readonly\",\"defaultValue\":\"\",\"renderingHints\":\"{'order': 20}\",\"indexed\":true},{\"propertyName\":\"numContent\",\"title\":\"Number of content items in this domain\",\"description\":\"\",\"category\":\"analytics\",\"dataType\":\"Number\",\"required\":false,\"displayProperty\":\"Readonly\",\"defaultValue\":\"\",\"renderingHints\":\"{'order': 20}\",\"indexed\":true},{\"required\":false,\"dataType\":\"URL\",\"propertyName\":\"thumbnail\",\"title\":\"Thumbnail\",\"description\":\"Thumbnail\",\"category\":\"General\",\"displayProperty\":\"Editable\",\"defaultValue\":\"\",\"renderingHints\":\"{ 'inputType': 'text', 'order': 7 }\",\"indexed\":true,\"draft\":false},{\"propertyName\":\"description\",\"title\":\"Description\",\"description\":\"Description of the node in 2-3 sentences.\",\"category\":\"general\",\"dataType\":\"Text\",\"required\":false,\"displayProperty\":\"Editable\",\"defaultValue\":\"\",\"renderingHints\":\"{'inputType': 'textarea', 'order': 4}\",\"indexed\":true},{\"propertyName\":\"createdBy\",\"title\":\"Created By\",\"description\":\"\",\"category\":\"Lifecycle\",\"dataType\":\"Text\",\"required\":false,\"displayProperty\":\"Readonly\",\"defaultValue\":\"\",\"renderingHints\":\"{'order': 21}\",\"indexed\":false},{\"propertyName\":\"createdOn\",\"title\":\"Created On\",\"description\":\"\",\"category\":\"Lifecycle\",\"dataType\":\"Date\",\"required\":false,\"displayProperty\":\"Readonly\",\"defaultValue\":\"\",\"renderingHints\":\"{'order': 22}\",\"indexed\":false},{\"propertyName\":\"lastUpdatedBy\",\"title\":\"Last Updated By\",\"description\":\"\",\"category\":\"audit\",\"dataType\":\"Text\",\"required\":false,\"displayProperty\":\"Readonly\",\"defaultValue\":\"\",\"renderingHints\":\"{'order': 21}\",\"indexed\":false},{\"propertyName\":\"lastUpdatedOn\",\"title\":\"Last Updated On\",\"description\":\"\",\"category\":\"audit\",\"dataType\":\"Date\",\"required\":false,\"displayProperty\":\"Readonly\",\"defaultValue\":\"\",\"renderingHints\":\"{'order': 22}\",\"indexed\":false},{\"propertyName\":\"version\",\"title\":\"Version\",\"description\":\"\",\"category\":\"audit\",\"dataType\":\"Number\",\"required\":false,\"displayProperty\":\"Readonly\",\"defaultValue\":\"\",\"renderingHints\":\"{'order': 23}\",\"indexed\":false},{\"propertyName\":\"versionDate\",\"title\":\"Version Date\",\"description\":\"\",\"category\":\"audit\",\"dataType\":\"Date\",\"required\":false,\"displayProperty\":\"Readonly\",\"defaultValue\":\"\",\"renderingHints\":\"{'order': 24}\",\"indexed\":false},{\"propertyName\":\"versionCreatedBy\",\"title\":\"Version Created By\",\"description\":\"\",\"category\":\"audit\",\"dataType\":\"Text\",\"required\":false,\"displayProperty\":\"Readonly\",\"defaultValue\":\"\",\"renderingHints\":\"{'order': 25}\",\"indexed\":false}],\"inRelations\":[],\"outRelations\":[{\"relationName\":\"isParentOf\",\"title\":\"children\",\"description\":\"Dimensions that are part of this domain\",\"required\":false,\"objectTypes\":[\"Dimension\"],\"renderingHints\":\"{'order': 27}\"}],\"systemTags\":[]}]}";
	// Get Definitions list
	@Test
	public void getDefinitionsListExpectSuccess200(){
		
		setURI();
		Response R =
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			when().
			get("/meta/v3/definitions/list").
			then().
			//log().all().
			spec(get200ResponseSpec()).
			extract().response();	
			
		JsonPath jP = R.jsonPath();
		ArrayList<String> objType = jP.get("result.definition_nodes.objectType");
		Assert.assertTrue(objType.contains("Content"));
		}
	
	// Get valid Definition
	@Test
	public void getValidDefinitionExpectSuccess200(){
		
		setURI();
		Response R =	
			given().
			spec(getRequestSpecification(contentType, userId, APIToken)).
			when().
			get("meta/v3/definitions/read/Content").
			then().
			//log().all().
			spec(get200ResponseSpec()).
			extract().response();
		
		JsonPath jP = R.jsonPath();
		String objType = jP.get("result.definition_node.objectType");
		Assert.assertTrue(objType.equals("Content"));
	}
	
	// Get Invalid Definition
	@Test
	public void getInvalidDefinitionExpect4xx(){
		
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		when().
		get("meta/v3/definitions/read/afnakdl").
		then().
		//log().all().
		spec(get404ResponseSpec());
	}
	
	// Update valid definition
	@Ignore
	public void updateValidDefinitionExpectSuccess200(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body("").
		when().
		patch("meta/v3/definitions/update/").
		then().
		log().all().
		spec(get200ResponseSpec());
	}
	
	// Update Invalid Definition
	@Ignore
	public void updateInvalidDefinitionExpect4xx(){
		setURI();
		given().
		spec(getRequestSpecification(contentType, userId, APIToken)).
		body(jsonUpdateMethodDefinition).
		with().
		contentType(JSON).
		when().
		patch("meta/v3/definitions/update/ackjdsfu").
		then().
		log().all().
		spec(get404ResponseSpec());
	}
}
