package com.ilimi.taxonomy.content.concrete.processor;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.entity.Controller;
import org.ekstep.content.entity.Plugin;
import org.ekstep.content.util.ECRFConversionUtility;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ilimi.common.exception.ClientException;
import com.ilimi.taxonomy.content.common.BaseTest;

public class EmbedControllerProcessorTest extends BaseTest {

	final static File folder = new File("src/test/resources/Contents/Verbs_III");

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void EmbedControllerProcessor_01() {

		exception.expect(ClientException.class);
		exception.expectMessage(ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM);
		ECRFConversionUtility fixture = new ECRFConversionUtility();
		String strContent = getFileString("testglobal_01/index.ecml");
		Plugin plugin = fixture.getECRF(strContent);
		PipelineRequestorClient.getPipeline("embedControllerProcessor", folder.getPath(), "").execute(plugin);
	}

	@Test
	public void EmbedControllerProcessor_02() {

		exception.expect(ClientException.class);
		exception.expectMessage(ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM);
		ECRFConversionUtility fixture = new ECRFConversionUtility();
		String strContent = getFileString("testglobal_01/index.ecml");
		Plugin plugin = fixture.getECRF(strContent);
		PipelineRequestorClient.getPipeline("embedControllerProcessor", "", "").execute(plugin);
	}

	@Test
	public void EmbedControllerProcessor_03() {
			ECRFConversionUtility fixture = new ECRFConversionUtility();
			String strContent = getFileString("Verbs_III/index.ecml");
			Plugin plugin = fixture.getECRF(strContent);
			Plugin result = PipelineRequestorClient.getPipeline("embedControllerProcessor", folder.getPath(), "test_12")
					.execute(plugin);
			String expected = getFileString("Verbs_III/items/assessment.json");
			for (Controller controller : result.getControllers()) {
				if (controller.getData().get("type") == "items") {
					assertEquals(expected, controller.getcData());
				}
			}
	}
}
