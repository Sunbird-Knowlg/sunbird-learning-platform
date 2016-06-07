package com.ilimi.taxonomy.content.util;

import static org.junit.Assert.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.ilimi.taxonomy.content.common.BaseTest;
import com.ilimi.taxonomy.content.entity.Content;

public class EcrfToXmlConvertorTest extends BaseTest {
	
	private static final String WELL_FORMED_XML_FILE_NAME =  "Sample_XML_1.xml";
	
	/*
	 * JUnit TestCase for Checking if the returned XML String Equal to the input xml,
	 * It means the Conversion is Loss-less  
	 */
	@Test
	public void getContentXmlString_Test01() {
		EcrfToXmlConvertor fixture = new EcrfToXmlConvertor();
		JsonContentParser parser = new JsonContentParser();
		String json = getFileString(WELL_FORMED_XML_FILE_NAME);
		Content ecrf = parser.parseContent(json);
		String contentXmlString = fixture.getContentXmlString(ecrf);
		assertTrue(StringUtils.equalsIgnoreCase(contentXmlString, json));
	}
	
	/*
	 * JUnit TestCase for Checking if the returned XML String is Well Formed
	 */
	@Test
	public void getContentXmlString_Test02() {
		EcrfToXmlConvertor fixture = new EcrfToXmlConvertor();
		JsonContentParser parser = new JsonContentParser();
		String json = getFileString(WELL_FORMED_XML_FILE_NAME);
		Content ecrf = parser.parseContent(json);
		String contentXmlString = fixture.getContentXmlString(ecrf);
		assertTrue(isValidXmlString(contentXmlString));
	}

}
