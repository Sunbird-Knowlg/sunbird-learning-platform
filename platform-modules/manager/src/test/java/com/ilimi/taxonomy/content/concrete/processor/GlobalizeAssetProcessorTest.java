package com.ilimi.taxonomy.content.concrete.processor;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.ilimi.taxonomy.content.entity.Media;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.util.ECRFConversionUtility;

public class GlobalizeAssetProcessorTest {
final static File folder = new File("src/test/resources/Contents/testglobal_01/assets");
	
	@Test
	public void globalizeAssetProcessor() {
		ECRFConversionUtility fixture = new ECRFConversionUtility();
		String strContent = getFileString("index.ecml");
		Plugin plugin = fixture.getECRF(strContent);
		Plugin result = PipelineRequestorClient
				.getPipeline("globalizeAssetProcessor", "/data/ContentBundleTest/global", "test_11").execute(plugin);
		File global = new File("src/test/resources/Contents/testglobal_01");
		List<String> expected = new ArrayList<String>();
		for (final File fileEntry : folder.listFiles()) {
			expected.add(fileEntry.getName());
		}
	    List<Media> media = new ArrayList<Media>();
	    media.add((Media) result.getManifest().getMedias().iterator().next());
		assertEquals(true, new File(global, "index.ecml").exists());
		assertEquals(false, result.getManifest().getMedias().isEmpty());
	    assertEquals(expected.size(),result.getManifest().getMedias().size());
		assertEquals(plugin.getManifest().getMedias().iterator().next().getSrc(), result.getManifest().getMedias().iterator().next().getSrc());
		assertEquals(plugin.getManifest().getMedias().iterator().next().getType(), result.getManifest().getMedias().iterator().next().getType());
		}

	private String getFileString(String fileName) {
		String fileString = "";
		File file = new File(getClass().getResource("/Contents/testglobal_01/" + fileName).getFile());
		try {
			fileString = FileUtils.readFileToString(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileString;
	}
}
