package org.sunbird.taxonomy.content.util;

import org.sunbird.content.entity.Plugin;
import org.sunbird.content.util.ECRFConversionUtility;
import org.sunbird.taxonomy.content.common.BaseTestUtil;
import org.junit.Test;

public class ECRFConversionUtilityTest {

	@SuppressWarnings("unused")
	@Test
	public void getECRF_Test01() {
		ECRFConversionUtility fixture = new ECRFConversionUtility();
		String strContent = BaseTestUtil.getFileString("Sample_XML_1.ecml");
		Plugin plugin = fixture.getECRF(strContent);
	}
}
