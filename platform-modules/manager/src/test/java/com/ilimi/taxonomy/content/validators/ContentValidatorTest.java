package com.ilimi.taxonomy.content.validators;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.validator.ContentValidator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.engine.mgr.impl.NodeManagerImpl;
import com.ilimi.taxonomy.content.common.BaseTest;

public class ContentValidatorTest extends BaseTest {
	
	ContentValidator validator = new ContentValidator();
	
	@Autowired
	NodeManagerImpl node;
	
	@Rule
	public ExpectedException exception = ExpectedException.none();

	public static File valid_content_package_file = new File("src/test/resources/Contents/Verbs.zip");
	public static File invalid_content_package_file = new File("src/test/resources/Contents/Verbs.jar");
	public static File invalid_content_package_structure = new File("src/test/resources/Contents/packageValidators.zip");
	public static File invalid_package_structure = new File("src/test/resources/Contents/content_validator_01.zip");
	public static File invalid_package_size = new File("src/test/resources/Contents/packageSize_validator.zip");
	public static File invalid_package_mimetype = new File("src/test/resources/Contents/Verbs");
	public static File invalid_package = new File("src/test/resources/Contents/package_validator.zip");

	//checks for given input is zip file with index.ecml/index.json present at the root folder with proper package structure
	@Test
	 public void validContentPackage(){
		 try{
		  Boolean result = validator.isValidContentPackage(valid_content_package_file);
		  assertEquals(result, true);
		 }catch(Exception e){
			 e.printStackTrace();
		 }
	 }
	
	//input is a file with not a zip extension
	@Test
	public void invalidContentPackage(){
			 exception.expect(ClientException.class);
			 exception.expectMessage(ContentErrorMessageConstants.INVALID_CONTENT_PACKAGE_FILE_MIME_TYPE_ERROR);
			 validator.isValidContentPackage(invalid_content_package_file);
	}
	
	//input is zip file without index.ecml/index.json
	@Test
	public void invalidContentPackageStructure_01(){
		 exception.expect(ClientException.class);
		 exception.expectMessage(ContentErrorMessageConstants.INVALID_CONTENT_PACKAGE_STRUCTURE_ERROR);
		 validator.isValidContentPackage(invalid_content_package_structure);
	}
	
	//input is zip file with index.ecml & index.json 
	@Test
	public void invalidContentPackageStructure_02(){
		 exception.expect(ClientException.class);
		 exception.expectMessage(ContentErrorMessageConstants.INVALID_CONTENT_PACKAGE_STRUCTURE_ERROR);
		 validator.isValidContentPackage(invalid_package_structure);
	}
	
	//input is zip file with filesize greater than 50mb
	@Test
	public void invalidContentPackageStructure_03(){
		 exception.expect(ClientException.class);
		 exception.expectMessage(ContentErrorMessageConstants.INVALID_CONTENT_PACKAGE_SIZE_ERROR);
		 validator.isValidContentPackage(invalid_package_size);
	}
    
	//input is zip file with another zip inside
	@Test
	public void invalidContentPackageStructure_04(){
		 exception.expect(ClientException.class);
		 exception.expectMessage(ContentErrorMessageConstants.INVALID_CONTENT_PACKAGE_STRUCTURE_ERROR);
		 validator.isValidContentPackage(invalid_package);
	}
	
//	@Test
	public void createNode(){
		Request request = new Request();
		request.setId(TEST_GRAPH);
		request.getContext().put("body", "<theme></theme>");
		request.getContext().put("code","org.ekstep.mar8.story");
		request.getContext().put("status", "Mock");
		request.getContext().put("description", "शेर का साथी हा");
		request.getContext().put("subject", "literacy");
		request.getContext().put("name", "शेर का साथी हाथ");
		request.getContext().put("owner", "EkStep");
		request.getContext().put("mimeType", "application/vnd.ekstep.ecml-archive");
		request.getContext().put("identifier","org.ekstep.mar8.story");
		request.getContext().put("contentType", "Story");
		request.getContext().put("osId", "org.ekstep.quiz.app");
		node.createDataNode(request);
	}
	public static String readFileString(String fileName) {
		String fileString = "";
		File file = new File("src/test/resources/Contents/" + fileName);
		try {
			fileString = FileUtils.readFileToString(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileString;
	}
}
