package com.ilimi.taxonomy.content.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.ilimi.taxonomy.content.common.BaseTest;
import com.ilimi.taxonomy.content.entity.Content;

public class EcrfToXmlConvertorTest extends BaseTest {
	
	private static final String WELL_FORMED_XML_FILE_NAME =  "Sample_XML_1.ecml";
	
	private static final String TEMP_OUTPUT_FILE_NAME =  "Sample_XML_1_Output.xml";
	
	/*
	 * JUnit TestCase for Checking if the returned XML String Equal to the input xml,
	 * It means the Conversion is Loss-less  
	 */
	@Test
	public void getContentXmlString_Test01() {
		try {
			EcrfToXmlConvertor fixture = new EcrfToXmlConvertor();
			XmlContentParser parser = new XmlContentParser();
			String xml = getFileString(WELL_FORMED_XML_FILE_NAME);
			Content ecrf = parser.parseContent(xml);
			String contentXmlString = fixture.getContentXmlString(ecrf);
			writeStringToFile(TEMP_OUTPUT_FILE_NAME, contentXmlString, false);
			XMLUnit.setIgnoreWhitespace(true);
	        XMLUnit.setIgnoreAttributeOrder(true);
	        DetailedDiff diff = new DetailedDiff(XMLUnit.compareXML(xml, contentXmlString));
			List<?> allDifferences = diff.getAllDifferences();
	        assertEquals("Differences found: "+ diff.toString(), 0, allDifferences.size());
	    } catch (SAXException e) {
			assertTrue("SAX Exception while getting differences.", false);
		} catch (IOException e) {
			assertTrue("IO Exception while getting differences.", false);
		}
	}
	
	/*
	 * JUnit TestCase for Checking if the returned XML String is Well Formed
	 */
	@Test
	public void getContentXmlString_Test02() {
		EcrfToXmlConvertor fixture = new EcrfToXmlConvertor();
		XmlContentParser parser = new XmlContentParser();
		String xml = getFileString(WELL_FORMED_XML_FILE_NAME);
		Content ecrf = parser.parseContent(xml);
		String contentXmlString = fixture.getContentXmlString(ecrf);
		assertTrue(isValidXmlString(contentXmlString));
	}

}
