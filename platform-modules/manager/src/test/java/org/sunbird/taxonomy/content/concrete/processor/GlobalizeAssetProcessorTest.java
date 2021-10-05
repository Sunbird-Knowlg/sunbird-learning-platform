package org.sunbird.taxonomy.content.concrete.processor;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.sunbird.common.exception.ClientException;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.entity.Media;
import org.sunbird.content.entity.Plugin;
import org.sunbird.content.util.ECRFConversionUtility;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@Ignore
public class GlobalizeAssetProcessorTest {

	final static File FOLDER = new File("src/test/resources/Contents/testglobal_01");
	final static String AWS = "https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/test_11";

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Ignore
	@Test
	public void globalizeAssetProcessor_01() {

		ECRFConversionUtility fixture = new ECRFConversionUtility();
		String strContent = getFileString("testglobal_01/index.ecml");
		Plugin plugin = fixture.getECRF(strContent);
		Plugin result = PipelineRequestorClient.getPipeline("globalizeAssetProcessor", FOLDER.getPath(), "test_11")
				.execute(plugin);

		List<String> expected = new ArrayList<String>();
		File assetFolder = new File(FOLDER + "/assets");
		for (final File fileEntry : assetFolder.listFiles()) {
			expected.add(AWS + "/" + fileEntry.getName());
		}

		List<Media> medias = new ArrayList<Media>();
		medias = result.getManifest().getMedias();

		List<String> actual = new ArrayList<String>();
		for (Media md : medias) {
			actual.add(md.getSrc());
		}

		assertEquals(true, new File(FOLDER, "index.ecml").exists());
		assertEquals(false, result.getManifest().getMedias().isEmpty());
		assertEquals(expected.contains("icon.png"), actual.contains("icon.png"));
	}

	@Test
	public void globalizeAssetProcessor_02() {

		exception.expect(ClientException.class);
		exception.expectMessage(ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM);
		ECRFConversionUtility fixture = new ECRFConversionUtility();
		String strContent = getFileString("testglobal_01/index.ecml");
		Plugin plugin = fixture.getECRF(strContent);
		PipelineRequestorClient.getPipeline("globalizeAssetProcessor", FOLDER.getPath(), "").execute(plugin);
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
