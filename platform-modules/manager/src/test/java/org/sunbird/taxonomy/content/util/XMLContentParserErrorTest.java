package org.sunbird.taxonomy.content.util;

import org.sunbird.common.exception.ClientException;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.util.XMLContentParser;
import org.sunbird.taxonomy.content.common.BaseTestUtil;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class XMLContentParserErrorTest {
	
	private static final String XML_FOR_PARSE_CONFIG_EXCEPTION =  "Sample_XML_1_ERROR_PARSECONFIGEXCEPTION.ecml";
	
	private static final String XML_FOR_IO_EXCEPTION =  "ECMLFileDoesNotExist.ecml";
	
	private static final String XML_FOR_SAX_EXCEPTION =  "Sample_XML_1_ERROR_SAXEXCEPTION.ecml";
	
	private static final String XML_FOR_INVALID_MEDIA_ERROR_1 =  "Sample_XML_1_ERROR_INVALID_MEDIA_1.ecml";
	
	private static final String XML_FOR_INVALID_MEDIA_ERROR_2 =  "Sample_XML_1_ERROR_INVALID_MEDIA_2.ecml";
	
	private static final String XML_FOR_INVALID_MEDIA_ERROR_3 =  "Sample_XML_1_ERROR_INVALID_MEDIA_3.ecml";
	
	private static final String XML_FOR_MULTIPLE_MANIFEST_ERROR =  "Sample_XML_1_ERROR_MULTIPLE_MANIFEST.ecml";
	
	private static final String XML_FOR_INVALID_CONTROLLER_ERROR_1 =  "Sample_XML_1_ERROR_INVALID_CONTROLLER_1.ecml";
	
	private static final String XML_FOR_INVALID_CONTROLLER_ERROR_2 =  "Sample_XML_1_ERROR_INVALID_CONTROLLER_2.ecml";
	
	private static final String XML_FOR_INVALID_CONTROLLER_ERROR_3 =  "Sample_XML_1_ERROR_INVALID_CONTROLLER_3.ecml";
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	/*
	 * TestCase For Ensuring the 'ParserConfigurationException'
	 */
	@Test
	public void parseContentTest_01() {
		exception.expect(ClientException.class);

		XMLContentParser fixture = new XMLContentParser();
		String xml = BaseTestUtil.getFileString(XML_FOR_PARSE_CONFIG_EXCEPTION);
		fixture.parseContent(xml);
	}
	
	/*
	 * TestCase For Ensuring the 'SAXException'
	 */
	@Test
	public void parseContentTest_02() {
		exception.expect(ClientException.class);

		XMLContentParser fixture = new XMLContentParser();
		String xml = BaseTestUtil.getFileString(XML_FOR_SAX_EXCEPTION);
		fixture.parseContent(xml);
	}
	
	/*
	 * TestCase For Ensuring the 'IOException'
	 */
	@Test
	public void parseContentTest_03() {
		exception.expect(ClientException.class);

		XMLContentParser fixture = new XMLContentParser();
		String xml = BaseTestUtil.getFileString(XML_FOR_IO_EXCEPTION);
		fixture.parseContent(xml);
	}
	
	/*
	 * TestCase For Ensuring the 'ClientException' 
	 * When media 'id' attribute is not given.
	 */
	@Test
	public void parseContentTest_04() {
		exception.expect(ClientException.class);

		XMLContentParser fixture = new XMLContentParser();
		String xml = BaseTestUtil.getFileString(XML_FOR_INVALID_MEDIA_ERROR_1);
		fixture.parseContent(xml);
	}
	
	/*
	 * TestCase For Ensuring the 'ClientException' 
	 * When media 'type' attribute is not given.
	 */
	@Test
	public void parseContentTest_05() {
		exception.expect(ClientException.class);

		XMLContentParser fixture = new XMLContentParser();
		String xml = BaseTestUtil.getFileString(XML_FOR_INVALID_MEDIA_ERROR_2);
		fixture.parseContent(xml);
	}
	
	/*
	 * TestCase For Ensuring the 'ClientException' 
	 * When media 'src' attribute is not given.
	 */
	@Test
	public void parseContentTest_06() {
		exception.expect(ClientException.class);

		XMLContentParser fixture = new XMLContentParser();
		String xml = BaseTestUtil.getFileString(XML_FOR_INVALID_MEDIA_ERROR_3);
		fixture.parseContent(xml);
	}
	
	/*
	 * TestCase For Ensuring the 'ClientException' 
	 * When multiple 'manifest' section is given.
	 */
	@Test
	@Ignore
	public void parseContentTest_07() {
		exception.expect(ClientException.class);
		exception.expectMessage(ContentErrorMessageConstants.MORE_THAN_ONE_MANIFEST_SECTION_ERROR);

		XMLContentParser fixture = new XMLContentParser();
		String xml = BaseTestUtil.getFileString(XML_FOR_MULTIPLE_MANIFEST_ERROR);
		fixture.parseContent(xml);
	}
	
	/*
	 * TestCase For Ensuring the 'ClientException' 
	 * When Controller 'id' attribute not is given.
	 */
	@Test
	public void parseContentTest_08() {
		exception.expect(ClientException.class);

		XMLContentParser fixture = new XMLContentParser();
		String xml = BaseTestUtil.getFileString(XML_FOR_INVALID_CONTROLLER_ERROR_1);
		fixture.parseContent(xml);
	}
	
	/*
	 * TestCase For Ensuring the 'ClientException' 
	 * When Controller 'type' attribute not is given.
	 */
	@Test
	public void parseContentTest_09() {
		exception.expect(ClientException.class);

		XMLContentParser fixture = new XMLContentParser();
		String xml = BaseTestUtil.getFileString(XML_FOR_INVALID_CONTROLLER_ERROR_2);
		fixture.parseContent(xml);
	}
	
	/*
	 * TestCase For Ensuring the 'ClientException' 
	 * When Controller invalid 'type' attribute is given.
	 */
	@Test
	public void parseContentTest_10() {
		exception.expect(ClientException.class);

		XMLContentParser fixture = new XMLContentParser();
		String xml = BaseTestUtil.getFileString(XML_FOR_INVALID_CONTROLLER_ERROR_3);
		fixture.parseContent(xml);
	}

}
