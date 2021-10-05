package org.sunbird.taxonomy.content.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.sunbird.common.exception.ClientException;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.entity.Plugin;
import org.sunbird.content.util.JSONContentParser;
import org.sunbird.taxonomy.content.common.BaseTestUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JSONContentParserTest {
	
	private static final String WELL_FORMED_JSON_FILE_NAME =  "Sample_JSON_1.json";
	
	private static final String INVALID_JSON_FILE_NAME =  "Sample_JSON_1_ERROR.json";
	
	// TODO: All the below values should be fetched from Input Files itself. 
	private static final int WELL_FORMED_JSON_FILE_CONTROLLER_COUNT = 2;
	private static final int WELL_FORMED_JSON_FILE_MEDIA_COUNT = 15;
	private static final int WELL_FORMED_JSON_FILE_TOP_LEVEL_PLUGIN_COUNT = 10;
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	 
	@Test
	public void parseContent_Test01() {
		JSONContentParser fixture = new JSONContentParser();
		String json = BaseTestUtil.getFileString(WELL_FORMED_JSON_FILE_NAME);
		Plugin ecrf = fixture.parseContent(json);
		assertNotNull(ecrf.getManifest()); 
		assertEquals(WELL_FORMED_JSON_FILE_CONTROLLER_COUNT, ecrf.getControllers().size());
		assertEquals(WELL_FORMED_JSON_FILE_MEDIA_COUNT, ecrf.getManifest().getMedias().size());
		assertEquals(WELL_FORMED_JSON_FILE_TOP_LEVEL_PLUGIN_COUNT, ecrf.getChildrenPlugin().size());
	}

	@Test
	public void parseContent_Test02(){
		exception.expect(ClientException.class);
		exception.expectMessage(ContentErrorMessageConstants.JSON_PARSE_CONFIG_ERROR);
		JSONContentParser fixture = new JSONContentParser();
		String json = BaseTestUtil.getFileString(INVALID_JSON_FILE_NAME);
		Plugin ecrf = fixture.parseContent(json);
	}
}
