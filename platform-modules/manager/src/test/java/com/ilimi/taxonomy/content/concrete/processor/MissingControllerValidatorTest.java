package com.ilimi.taxonomy.content.concrete.processor;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ilimi.common.exception.ClientException;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.util.ECRFConversionUtility;

public class MissingControllerValidatorTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void missingController() {
		exception.expect(ClientException.class);
		exception.expectMessage(ContentErrorMessageConstants.MISSING_CONTROLLER_FILES_ERROR);
		ECRFConversionUtility fixture = new ECRFConversionUtility();
		String strContent = getFileString("/testAsset/index.ecml");
		Plugin plugin = fixture.getECRF(strContent);
		PipelineRequestorClient.getPipeline("missingCtrlValidatorProcessor", "src/test/resources/Contents", "test_12")
				.execute(plugin);
	}

	@Test
	public void missingControllerOfTypeData() {
		exception.expect(ClientException.class);
		exception.expectMessage(ContentErrorMessageConstants.MISSING_CONTROLLER_FILES_ERROR);
		ECRFConversionUtility fixture = new ECRFConversionUtility();
		String strContent = getFileString("/Verbs/index.ecml");
		Plugin plugin = fixture.getECRF(strContent);
		PipelineRequestorClient.getPipeline("missingCtrlValidatorProcessor", "src/test/resources/Contents", "test_12")
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
				.getPipeline("missingCtrlValidatorProcessor", "src/test/resources/Contents/Verbs", "test_12")
				.execute(plugin);
	}

	private String getFileString(String fileName) {
		String fileString = "";
		File file = new File(getClass().getResource("/Contents" + fileName).getFile());
		try {
			fileString = FileUtils.readFileToString(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileString;
	}
}
