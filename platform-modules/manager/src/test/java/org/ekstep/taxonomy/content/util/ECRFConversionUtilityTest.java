package org.ekstep.taxonomy.content.util;

import org.ekstep.content.entity.Plugin;
import org.ekstep.content.util.ECRFConversionUtility;
import org.ekstep.taxonomy.content.common.BaseTestUtil;
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
