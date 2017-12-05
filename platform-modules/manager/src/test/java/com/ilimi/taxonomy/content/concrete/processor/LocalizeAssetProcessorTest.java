package com.ilimi.taxonomy.content.concrete.processor;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.entity.Plugin;
import org.ekstep.content.util.ECRFConversionUtility;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ilimi.common.exception.ClientException;

// TODO: ignored because jenkins don't have S3 access.
@Ignore
public class LocalizeAssetProcessorTest {

	final static File assetFolder = new File("src/test/resources/Contents/testlocal_01/assets");
    final static File tmpFolder = new File("/data/ContentBundleTest/local");
    
    @Rule
	public ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void init() {
		try {
			if(!tmpFolder.exists()){
				tmpFolder.mkdir();
			}else{
				FileUtils.cleanDirectory(tmpFolder);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		String[] apiUrl = null;
		for (final File fileEntry : assetFolder.listFiles()) {
			try {
				apiUrl = AWSUploader.uploadFile(assetFolder.getPath(), fileEntry);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Test
	public void localizeAssetProcessor_01() {

		exception.expect(ClientException.class);
		exception.expectMessage(ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM);
		ECRFConversionUtility fixture = new ECRFConversionUtility();
		String strContent = getFileString("testglobal_01/index.ecml");
		Plugin plugin = fixture.getECRF(strContent);
		PipelineRequestorClient
				.getPipeline("localizeAssetProcessor", tmpFolder.getAbsolutePath(), "").execute(plugin);
	}

	@Test
	public void localizeAssetProcessor_02() {

		exception.expect(ClientException.class);
		exception.expectMessage(ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM);
		ECRFConversionUtility fixture = new ECRFConversionUtility();
		String strContent = getFileString("testglobal_01/index.ecml");
		Plugin plugin = fixture.getECRF(strContent);
		PipelineRequestorClient
				.getPipeline("localizeAssetProcessor", "" , "").execute(plugin);
	}
	
	@Test
	public void localizeAssetProcessor_03() {
		
		ECRFConversionUtility fixture = new ECRFConversionUtility();
		String strContent = getFileString("testlocal_01/index.ecml");
		Plugin plugin = fixture.getECRF(strContent);
		Plugin result = PipelineRequestorClient
				.getPipeline("localizeAssetProcessor", tmpFolder.getAbsolutePath(), "test_01").execute(plugin);
		
		List<String> expected = new ArrayList<String>();
		for (final File fileEntry : assetFolder.listFiles()) {
			expected.add(fileEntry.getName());
		}
		
		File fol = new File("/data/ContentBundleTest/local/assets");
		List<String> actual = new ArrayList<String>();
		for(File fileEntry : fol.listFiles()) {
			actual.add(fileEntry.getName());
		}
		
		String local = "src/test/resources/Contents/testlocal_01";
		assertEquals(true, new File(local, "index.ecml").exists());
		assertEquals(false, result.getManifest().getMedias().isEmpty());
        assertEquals(true, CollectionUtils.isEqualCollection(actual, expected));
	}
	
	@AfterClass
	public static void deleteFromS3() throws IOException {

		FileUtils.deleteDirectory(tmpFolder);
		for (final File fileEntry : assetFolder.listFiles()) {
			try {
				AWSUploader.deleteFile(fileEntry.getName());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
