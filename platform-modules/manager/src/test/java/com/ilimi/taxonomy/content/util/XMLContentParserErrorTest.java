package com.ilimi.taxonomy.content.util;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xml.sax.SAXException;

import com.ilimi.taxonomy.content.common.BaseTest;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;

public class XMLContentParserErrorTest extends BaseTest {
	
	private static final String XML_FOR_PARSE_CONFIG_EXCEPTION =  "";
	
	private static final String XML_FOR_IO_EXCEPTION =  "";
	
	private static final String XML_FOR_SAX_EXCEPTION =  "";
	
	private static final String XML_FOR_INVALID_MEDIA_ERROR_1 =  "";
	
	private static final String XML_FOR_INVALID_MEDIA_ERROR_2 =  "";
	
	private static final String XML_FOR_INVALID_MEDIA_ERROR_3 =  "";
	
	private static final String XML_FOR_MULTIPLE_MANIFEST_ERROR =  "";
	
	private static final String XML_FOR_INVALID_CONTROLLER_ERROR =  "";
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	/*
	 * TestCase For Ensuring the 'ParserConfigurationException'
	 */
	@Test
	public void parseContentTest_01() {
		exception.expect(ParserConfigurationException.class);
		exception.expectMessage(ContentErrorMessageConstants.XML_PARSE_CONFIG_ERROR);

		XMLContentParser fixture = new XMLContentParser();
		String xml = getFileString(XML_FOR_PARSE_CONFIG_EXCEPTION);
		fixture.parseContent(xml);
	}
	
	/*
	 * TestCase For Ensuring the 'SAXException'
	 */
	@Test
	public void parseContentTest_02() {
		exception.expect(SAXException.class);
		exception.expectMessage(ContentErrorMessageConstants.XML_NOT_WELL_FORMED_ERROR);

		XMLContentParser fixture = new XMLContentParser();
		String xml = getFileString(XML_FOR_SAX_EXCEPTION);
		fixture.parseContent(xml);
	}
	
	/*
	 * TestCase For Ensuring the 'IOException'
	 */
	@Test
	public void parseContentTest_03() {
		exception.expect(IOException.class);
		exception.expectMessage(ContentErrorMessageConstants.XML_IO_ERROR);

		XMLContentParser fixture = new XMLContentParser();
		String xml = getFileString(XML_FOR_IO_EXCEPTION);
		fixture.parseContent(xml);
	}
	
	// RAW
	
	@Test
	public void parseContentTest_04() {
		exception.expect(IOException.class);
		exception.expectMessage(ContentErrorMessageConstants.XML_IO_ERROR);

		XMLContentParser fixture = new XMLContentParser();
		String xml = getFileString(XML_FOR_INVALID_MEDIA_ERROR_1);
		fixture.parseContent(xml);
	}

}
