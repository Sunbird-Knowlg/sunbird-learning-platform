package com.ilimi.taxonomy.content.util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.ilimi.taxonomy.content.common.BaseTest;
import com.ilimi.taxonomy.content.entity.Plugin;

public class JSONContentParserTest extends BaseTest {
	
	private static final String WELL_FORMED_JSON_FILE_NAME =  "Sample_JSON_1.json";
	
	private static final String INVALID_JSON_FILE_NAME =  "Sample_JSON_1_ERROR.json";
	
	// TODO: All the below values should be fetched from Input Files itself. 
	private static final int WELL_FORMED_JSON_FILE_CONTROLLER_COUNT = 2;
	private static final int WELL_FORMED_JSON_FILE_MEDIA_COUNT = 15;
	private static final int WELL_FORMED_JSON_FILE_TOP_LEVEL_PLUGIN_COUNT = 10;
	
	@Test
	public void parseContent_Test01() {
		JSONContentParser fixture = new JSONContentParser();
		String json = getFileString(WELL_FORMED_JSON_FILE_NAME);
		Plugin ecrf = fixture.parseContent(json);
		assertNotNull(ecrf.getManifest()); 
		assertEquals(WELL_FORMED_JSON_FILE_CONTROLLER_COUNT, ecrf.getControllers().size());
		assertEquals(WELL_FORMED_JSON_FILE_MEDIA_COUNT, ecrf.getManifest().getMedias().size());
		assertEquals(WELL_FORMED_JSON_FILE_TOP_LEVEL_PLUGIN_COUNT, ecrf.getChildrenPlugin().size());
	}

}
