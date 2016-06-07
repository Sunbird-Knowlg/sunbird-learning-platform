package com.ilimi.taxonomy.content.util;

import static org.junit.Assert.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.ilimi.taxonomy.content.common.BaseTest;
import com.ilimi.taxonomy.content.entity.Content;

public class EcrfToJsonConvertorTest extends BaseTest {
	
	private static final String WELL_FORMED_JSON_FILE_NAME =  "Sample_JSON_1.json";
	
	/*
	 * JUnit TestCase for Checking if the returned JSON String Equal to the input json,
	 * It means the Conversion is Loss-less  
	 */
	@Test
	public void getContentJsonString_Test01() {
		EcrfToJsonConvertor fixture = new EcrfToJsonConvertor();
		JsonContentParser parser = new JsonContentParser();
		String json = getFileString(WELL_FORMED_JSON_FILE_NAME);
		Content ecrf = parser.parseContent(json);
		String contentJsonString = fixture.getContentJsonString(ecrf);
		assertTrue(StringUtils.equalsIgnoreCase(contentJsonString, json));
	}
	
	/*
	 * JUnit TestCase for Checking if the returned JSON String is Well Formed
	 */
	@Test
	public void getContentJsonString_Test02() {
		EcrfToJsonConvertor fixture = new EcrfToJsonConvertor();
		JsonContentParser parser = new JsonContentParser();
		String json = getFileString(WELL_FORMED_JSON_FILE_NAME);
		Content ecrf = parser.parseContent(json);
		String contentJsonString = fixture.getContentJsonString(ecrf);
		assertTrue(isValidJsonString(contentJsonString));
	}

}
