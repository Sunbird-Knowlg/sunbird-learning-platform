package com.ilimi.taxonomy.content.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.ilimi.taxonomy.content.entity.Content;

public class EcrfConversionUtilityTest {
	
	@SuppressWarnings("unused")
	@Test
	public void getECRF_Test01() {
		EcrfConversionUtility fixture = new EcrfConversionUtility();
//		String strContent = getFileString("Sample_XML_1.ecml");
//		Content content = fixture.getECRF(strContent);
		String strContent = getFileString("Sample_JSON_1.json");
		Content content = fixture.getEcrfFromJson(strContent);
	}
	
	private String getFileString(String fileName) {
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
