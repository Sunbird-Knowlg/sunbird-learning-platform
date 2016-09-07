package com.ilimi.taxonomy.content.concrete.processor;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.ilimi.taxonomy.content.entity.Controller;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.util.ECRFConversionUtility;

public class EmbedControllerProcessorTest {

	@Test
	public void EmbedControllerProcessor() {
		ECRFConversionUtility fixture = new ECRFConversionUtility();
		String strContent = getFileString("index.ecml");
		Plugin plugin = fixture.getECRF(strContent);
		Plugin result = PipelineRequestorClient
				.getPipeline("embedControllerProcessor", "src/test/resources/Contents/Verbs_III", "test_12")
				.execute(plugin);
		String expected = getFileString("items/assessment.json");
		for (Controller controller : result.getControllers()){
			if(controller.getData().get("type") == "items"){
				assertEquals(expected, controller.getcData());
			}
		}
	}
	
	@Test
	public void EmbedControllerProcessor_01(){
		ECRFConversionUtility fixture = new ECRFConversionUtility();
		String strContent = getFileString("index.ecml");
		Plugin plugin = fixture.getECRF(strContent);
		Plugin result = PipelineRequestorClient
				.getPipeline("embedControllerProcessor", "src/test/resources/Contents/Verbs_III", "test_12")
				.execute(plugin);
		String expected = getFileString("items/assessment.json");
		for (Controller controller : result.getControllers()){
			if(controller.getData().get("type") == "items"){
				assertEquals(expected, controller.getcData());
			}
		}
	}

	private String getFileString(String fileName) {
		String fileString = "";
		File file = new File(getClass().getResource("/Contents/Verbs_III/" + fileName).getFile());
		try {
			fileString = FileUtils.readFileToString(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileString;
	}
}
