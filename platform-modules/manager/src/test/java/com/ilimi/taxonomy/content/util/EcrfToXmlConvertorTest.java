package com.ilimi.taxonomy.content.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.xml.sax.SAXException;
import org.xmlunit.XMLUnitException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelectors;

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
	        Diff diff = DiffBuilder.compare(contentXmlString).withTest(xml)
	        		.ignoreComments()
	        		.ignoreWhitespace()
	        		.normalizeWhitespace()
	                .checkForSimilar() // a different order is always 'similar' not equals.
	                .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndText))
	                .build();
	        assertFalse("XML similar " + diff.toString(), diff.hasDifferences());
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
	
	/*
	 * Dummy Test Case 
	 */
	@Test
	public void dummy_test(){
		String controlXml = "<flowers><flower>Roses</flower><flower>Daisy</flower><flower>Crocus</flower></flowers>";
		String testXml = "<flowers><flower>Daisy</flower><flower>Roses</flower><flower>Crocus</flower></flowers>";

		Diff myDiff = DiffBuilder.compare(controlXml).withTest(testXml)
		        .checkForSimilar() // a different order is always 'similar' not equals.
		        .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndText))
		        .build();

		assertFalse("XML similar " + myDiff.toString(), myDiff.hasDifferences());
	}

}
