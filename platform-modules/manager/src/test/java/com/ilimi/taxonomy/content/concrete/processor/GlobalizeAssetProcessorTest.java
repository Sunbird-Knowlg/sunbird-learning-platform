package com.ilimi.taxonomy.content.concrete.processor;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.entity.Media;
import org.ekstep.content.entity.Plugin;
import org.ekstep.content.util.ECRFConversionUtility;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ilimi.common.exception.ClientException;
import com.ilimi.taxonomy.content.common.BaseTest;

public class GlobalizeAssetProcessorTest extends BaseTest {

	final static File folder = new File("src/test/resources/Contents/testglobal_01");
	final static String AWS = "https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/test_11";

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void globalizeAssetProcessor_01() {

		ECRFConversionUtility fixture = new ECRFConversionUtility();
		String strContent = getFileString("testglobal_01/index.ecml");
		Plugin plugin = fixture.getECRF(strContent);
		Plugin result = PipelineRequestorClient.getPipeline("globalizeAssetProcessor", folder.getPath(), "test_11")
				.execute(plugin);

		List<String> expected = new ArrayList<String>();
		File assetFolder = new File(folder + "/assets");
		for (final File fileEntry : assetFolder.listFiles()) {
			expected.add(AWS + "/" + fileEntry.getName());
		}

		List<Media> medias = new ArrayList<Media>();
		medias = result.getManifest().getMedias();

		List<String> actual = new ArrayList<String>();
		for (Media md : medias) {
			actual.add(md.getSrc());
		}

		assertEquals(true, new File(folder, "index.ecml").exists());
		assertEquals(false, result.getManifest().getMedias().isEmpty());
		assertEquals(true, CollectionUtils.isEqualCollection(expected, actual));

	}

	@Test
	public void globalizeAssetProcessor_02() {

		exception.expect(ClientException.class);
		exception.expectMessage(ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM);
		ECRFConversionUtility fixture = new ECRFConversionUtility();
		String strContent = getFileString("testglobal_01/index.ecml");
		Plugin plugin = fixture.getECRF(strContent);
		PipelineRequestorClient.getPipeline("globalizeAssetProcessor", folder.getPath(), "").execute(plugin);
	}

	@Test
	public void globalizeAssetProcessor_03() {

		exception.expect(ClientException.class);
		exception.expectMessage(ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM);
		ECRFConversionUtility fixture = new ECRFConversionUtility();
		String strContent = getFileString("testglobal_01/index.ecml");
		Plugin plugin = fixture.getECRF(strContent);
		PipelineRequestorClient.getPipeline("globalizeAssetProcessor", "", "").execute(plugin);
	}
}
