package org.sunbird.taxonomy.content.util;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.sunbird.content.entity.Plugin;
import org.sunbird.content.util.ECRFToJSONConvertor;
import org.sunbird.content.util.JSONContentParser;
import org.sunbird.taxonomy.content.common.BaseTestUtil;
import org.junit.Ignore;
import org.junit.Test;

import net.javacrumbs.jsonunit.JsonAssert;
import net.javacrumbs.jsonunit.core.Option;

public class ECRFToJSONConvertorTest {
	
	private static final String WELL_FORMED_JSON_FILE_NAME =  "Sample_JSON_1.json";
	
	private static final String TEMP_OUTPUT_FILE_NAME =  "Sample_JSON_1_Output.json";
	
	static {
		JsonAssert.setOptions(Option.IGNORING_ARRAY_ORDER, 
				Option.TREATING_NULL_AS_ABSENT,
				Option.IGNORING_EXTRA_FIELDS);
	}
	
	/*
	 * JUnit TestCase for Checking if the returned JSON String Equal to the input json,
	 * It means the Conversion is Loss-less  
	 */
	@Test
	@Ignore
	public void getContentJsonString_Test01() {
		try {
			ECRFToJSONConvertor fixture = new ECRFToJSONConvertor();
			JSONContentParser parser = new JSONContentParser();
			String json = BaseTestUtil.getFileString(WELL_FORMED_JSON_FILE_NAME);
			Plugin ecrf = parser.parseContent(json);
			String contentJsonString = fixture.getContentJsonString(ecrf);
			BaseTestUtil.writeStringToFile(TEMP_OUTPUT_FILE_NAME, contentJsonString, false);
			assertJsonEquals(contentJsonString, json);
		} catch (IOException e) {
			assertTrue("IO Exception while getting differences in JSON.", false);
		}
	}
	
	/*
	 * JUnit TestCase for Checking if the returned JSON String is Well Formed
	 */
	@Test
	public void getContentJsonString_Test02() {
		ECRFToJSONConvertor fixture = new ECRFToJSONConvertor();
		JSONContentParser parser = new JSONContentParser();
		String json = BaseTestUtil.getFileString(WELL_FORMED_JSON_FILE_NAME);
		Plugin ecrf = parser.parseContent(json);
		String contentJsonString = fixture.getContentJsonString(ecrf);
		assertTrue(BaseTestUtil.isValidJsonString(contentJsonString));
	}
}
