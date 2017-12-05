package com.ilimi.taxonomy.content.concrete.processor;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.entity.Plugin;
import org.ekstep.content.util.ECRFConversionUtility;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ilimi.common.exception.ClientException;

public class MissingAssetValidatorTest {

	final static File folder = new File("src/test/resources/Contents/Verbs_III");
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Test
	public void MissingAssetValidatorTest_01() {
			exception.expect(ClientException.class);
			exception.expectMessage(ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM);
			
			ECRFConversionUtility fixture = new ECRFConversionUtility();
			String strContent = getFileString("Verbs_III/index.ecml");
			Plugin plugin = fixture.getECRF(strContent);
			PipelineRequestorClient
					.getPipeline("missingAssetValidatorProcessor", folder.getPath(), "")
					.execute(plugin);
	}
	
	@Test
	public void MissingAssetValidatorTest_02() {
			exception.expect(ClientException.class);
			exception.expectMessage(ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM);
			
			ECRFConversionUtility fixture = new ECRFConversionUtility();
			String strContent = getFileString("Verbs_III/index.ecml");
			Plugin plugin = fixture.getECRF(strContent);
			PipelineRequestorClient
					.getPipeline("missingAssetValidatorProcessor", "", "")
					.execute(plugin);
	}
	
	@Test
	public void missingAsset() {
			exception.expect(ClientException.class);
			exception.expectMessage(ContentErrorMessageConstants.MISSING_ASSETS_ERROR);
			
			ECRFConversionUtility fixture = new ECRFConversionUtility();
			String strContent = getFileString("Verbs_III/index.ecml");
			Plugin plugin = fixture.getECRF(strContent);
			PipelineRequestorClient
					.getPipeline("missingAssetValidatorProcessor", folder.getPath(), "test_12")
					.execute(plugin);
	}
	
	@Test
	public void missingAssetInWidget() {
			exception.expect(ClientException.class);
			exception.expectMessage(ContentErrorMessageConstants.MISSING_ASSETS_ERROR);
			
			ECRFConversionUtility fixture = new ECRFConversionUtility();
			String strContent = getFileString("Verbs_III/index.ecml");
			Plugin plugin = fixture.getECRF(strContent);
			PipelineRequestorClient
					.getPipeline("missingAssetValidatorProcessor", folder.getPath(), "test_12")
					.execute(plugin);
	}

	@Test
	public void duplicateAsset() {
			exception.expect(ClientException.class);
			exception.expectMessage(ContentErrorMessageConstants.DUPLICATE_ASSET_ID_ERROR);
			ECRFConversionUtility fixture = new ECRFConversionUtility();
			String strContent = getFileString("testAsset/index.ecml");
			Plugin plugin = fixture.getECRF(strContent);
			PipelineRequestorClient
					.getPipeline("missingAssetValidatorProcessor", "src/test/resources/Contents/testAsset", "test_12")
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