package org.sunbird.taxonomy.content.concrete.processor;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.sunbird.common.exception.ClientException;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.entity.Plugin;
import org.sunbird.content.util.ECRFConversionUtility;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MissingControllerValidatorTest {

	final static File FOLDER = new File("src/test/resources/Contents/Verbs");
	
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void MissingAssetValidatorTest_01() {
			exception.expect(ClientException.class);
			exception.expectMessage(ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM);
			
			ECRFConversionUtility fixture = new ECRFConversionUtility();
			String strContent = getFileString("Verbs_III/index.ecml");
			Plugin plugin = fixture.getECRF(strContent);
			PipelineRequestorClient.getPipeline("missingCtrlValidatorProcessor", FOLDER.getPath(), "")
			.execute(plugin);
	}
	
	@Test
	public void MissingAssetValidatorTest_02() {
			exception.expect(ClientException.class);
			exception.expectMessage(ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM);
			
			ECRFConversionUtility fixture = new ECRFConversionUtility();
			String strContent = getFileString("Verbs_III/index.ecml");
			Plugin plugin = fixture.getECRF(strContent);
			PipelineRequestorClient.getPipeline("missingCtrlValidatorProcessor", "", "")
			.execute(plugin);
	}
	
	@Test
	public void missingController() {
		exception.expect(ClientException.class);
		exception.expectMessage(ContentErrorMessageConstants.MISSING_CONTROLLER_FILES_ERROR);
		ECRFConversionUtility fixture = new ECRFConversionUtility();
		String strContent = getFileString("testAsset/index.ecml");
		Plugin plugin = fixture.getECRF(strContent);
		PipelineRequestorClient.getPipeline("missingCtrlValidatorProcessor", "src/test/resources/Contents/testAsset", "test_12")
				.execute(plugin);
	}

	@Test
	public void missingControllerOfTypeData() {
		exception.expect(ClientException.class);
		exception.expectMessage(ContentErrorMessageConstants.MISSING_CONTROLLER_FILES_ERROR);
		ECRFConversionUtility fixture = new ECRFConversionUtility();
		String strContent = getFileString("Verbs/index.ecml");
		Plugin plugin = fixture.getECRF(strContent);
		PipelineRequestorClient.getPipeline("missingCtrlValidatorProcessor", FOLDER.getPath(), "test_12")
				.execute(plugin);
	}

	@Test
	public void duplicateController() {
		exception.expect(ClientException.class);
		exception.expectMessage(ContentErrorMessageConstants.DUPLICATE_CONTROLLER_ID_ERROR);
		ECRFConversionUtility fixture = new ECRFConversionUtility();
		String strContent = getFileString("/Sample_XML_1_ERROR_DUPLICATE_CONTROLLER.ecml");
		Plugin plugin = fixture.getECRF(strContent);
		PipelineRequestorClient
				.getPipeline("missingCtrlValidatorProcessor", FOLDER.getPath(), "test_12")
				.execute(plugin);
	}
	
	public String getFileString(String fileName) {
		String fileString = "";
		File file = new File(getClass().getResource("/Contents/" + fileName).getFile());
		try {
			fileString = FileUtils.readFileToString(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileString;
	}
}
