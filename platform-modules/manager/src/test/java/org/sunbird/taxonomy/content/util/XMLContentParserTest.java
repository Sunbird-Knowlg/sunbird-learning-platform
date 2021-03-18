package org.sunbird.taxonomy.content.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.sunbird.content.entity.Plugin;
import org.sunbird.content.util.XMLContentParser;
import org.sunbird.taxonomy.content.common.BaseTestUtil;
import org.junit.Test;

public class XMLContentParserTest {
	
	private static final String WELL_FORMED_XML_FILE_NAME =  "Sample_XML_2.ecml";
	
	// TODO: All the below values should be fetched from Input Files itself. 
		private static final int WELL_FORMED_XML_FILE_CONTROLLER_COUNT = 3;
		private static final int WELL_FORMED_XML_FILE_MEDIA_COUNT = 144;
		private static final int WELL_FORMED_XML_FILE_TOP_LEVEL_PLUGIN_COUNT = 27;
		
	@Test	
	public void xmlContentParser_01(){
		XMLContentParser fixture = new XMLContentParser();
		String xml = BaseTestUtil.getFileString(WELL_FORMED_XML_FILE_NAME);
		Plugin ecrf = fixture.parseContent(xml);
		assertNotNull(ecrf.getManifest()); 
		assertEquals(WELL_FORMED_XML_FILE_CONTROLLER_COUNT, ecrf.getControllers().size());
		assertEquals(WELL_FORMED_XML_FILE_MEDIA_COUNT, ecrf.getManifest().getMedias().size());
		assertEquals(WELL_FORMED_XML_FILE_TOP_LEVEL_PLUGIN_COUNT, ecrf.getChildrenPlugin().size());
	}
}
