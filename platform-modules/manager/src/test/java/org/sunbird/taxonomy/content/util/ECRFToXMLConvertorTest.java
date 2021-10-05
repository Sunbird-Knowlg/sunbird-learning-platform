package org.sunbird.taxonomy.content.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.sunbird.content.entity.Plugin;
import org.sunbird.content.util.ECRFToXMLConvertor;
import org.sunbird.content.util.XMLContentParser;
import org.sunbird.taxonomy.content.common.BaseTestUtil;
import org.junit.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelectors;

public class ECRFToXMLConvertorTest {
	
	private static final String WELL_FORMED_XML_FILE_NAME =  "Sample_XML_1.ecml";
	
	private static final String TEMP_OUTPUT_FILE_NAME =  "Sample_XML_1_Output.xml";
	
	/*
	 * JUnit TestCase for Checking if the returned XML String Equal to the input xml,
	 * It means the Conversion is Loss-less  
	 */
	@Test
	public void getContentXmlString_Test01() {
		try {
			ECRFToXMLConvertor fixture = new ECRFToXMLConvertor();
			XMLContentParser parser = new XMLContentParser();
			String xml = BaseTestUtil.getFileString(WELL_FORMED_XML_FILE_NAME);
			Plugin ecrf = parser.parseContent(xml);
			String contentXmlString = fixture.getContentXmlString(ecrf);
			BaseTestUtil.writeStringToFile(TEMP_OUTPUT_FILE_NAME, contentXmlString, false);
	        Diff diff = DiffBuilder.compare(contentXmlString).withTest(xml)
	        		.ignoreComments()
	        		.ignoreWhitespace()
	        		.normalizeWhitespace()
	                .checkForSimilar() // a different order is always 'similar' not equals.
	                .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndText))
	                .build();
	        assertFalse("XML similar " + diff.toString(), diff.hasDifferences());
	    } catch (IOException e) {
			assertTrue("IO Exception while getting differences in XML.", false);
		}
	}
	
	/*
	 * JUnit TestCase for Checking if the returned XML String is Well Formed
	 */
	@Test
	public void getContentXmlString_Test02() {
		ECRFToXMLConvertor fixture = new ECRFToXMLConvertor();
		XMLContentParser parser = new XMLContentParser();
		String xml = BaseTestUtil.getFileString(WELL_FORMED_XML_FILE_NAME);
		Plugin ecrf = parser.parseContent(xml);
		String contentXmlString = fixture.getContentXmlString(ecrf);
		assertTrue(BaseTestUtil.isValidXmlString(contentXmlString));
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
