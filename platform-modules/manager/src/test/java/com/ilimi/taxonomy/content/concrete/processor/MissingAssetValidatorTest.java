package com.ilimi.taxonomy.content.concrete.processor;

import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ilimi.common.exception.ClientException;
import com.ilimi.taxonomy.content.common.BaseTest;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.util.ECRFConversionUtility;

public class MissingAssetValidatorTest extends BaseTest {

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
}