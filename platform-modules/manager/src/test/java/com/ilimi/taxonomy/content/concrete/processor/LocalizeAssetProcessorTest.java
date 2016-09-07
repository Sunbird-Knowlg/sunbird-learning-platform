package com.ilimi.taxonomy.content.concrete.processor;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.ekstep.common.util.AWSUploader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.util.ECRFConversionUtility;

public class LocalizeAssetProcessorTest {

	final static File folder = new File("src/test/resources/Contents/testlocal_01/assets");

	@BeforeClass
	public static void init() {
		try {
			FileUtils.cleanDirectory(new File("/data/ContentBundleTest/local"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		String[] apiUrl = null;
		for (final File fileEntry : folder.listFiles()) {
			try {
				apiUrl = AWSUploader.uploadFile("ekstep-public", "contents/testlocal_01/assets", fileEntry);
				for (String url : apiUrl) {
					System.out.println(url);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void localizeAssetProcessor() {
		ECRFConversionUtility fixture = new ECRFConversionUtility();
		String strContent = getFileString("index.ecml");
		Plugin plugin = fixture.getECRF(strContent);
		Plugin result = PipelineRequestorClient
				.getPipeline("localizeAssetProcessor", "/data/ContentBundleTest/local", "test_01").execute(plugin);
		File local = new File("src/test/resources/Contents/testlocal_01");
		List<String> expected = new ArrayList<String>();
		for (final File fileEntry : folder.listFiles()) {
			expected.add(fileEntry.getName());
		}
		
		assertEquals(true, new File(local, "index.ecml").exists());
		assertEquals(false, result.getManifest().getMedias().isEmpty());
	    assertEquals(expected.size(),result.getManifest().getMedias().size());
		assertEquals(plugin.getManifest().getMedias().iterator().next().getSrc(), result.getManifest().getMedias().iterator().next().getSrc());
        assertEquals(plugin.getManifest().getMedias().iterator().next().getType(), result.getManifest().getMedias().iterator().next().getType());
	}

	private String getFileString(String fileName) {
		String fileString = "";
		File file = new File(getClass().getResource("/Contents/testlocal_01/" + fileName).getFile());
		try {
			fileString = FileUtils.readFileToString(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileString;
	}

	@AfterClass
	public static void deleteFromS3() {

		for (final File fileEntry : folder.listFiles()) {
			try {
				System.out.println("deleting file " + fileEntry.getName());
				AWSUploader.deleteFile("ekstep-public", fileEntry.getName());
				System.out.println("file " + fileEntry.getName() + "deleted");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
