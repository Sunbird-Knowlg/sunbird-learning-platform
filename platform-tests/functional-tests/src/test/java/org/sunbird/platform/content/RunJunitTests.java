package org.sunbird.platform.content;


import org.sunbird.platform.domain.ConceptAPIV3Tests;
import org.sunbird.platform.domain.DomainV3APITests;
import org.sunbird.platform.domain.MethodV3APITests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
	
	@RunWith(Suite.class)
//	@Suite.SuiteClasses({CompositeSearchV3TestCases.class, ConceptAPIV3Tests.class, DimensionAPIV3Tests.class, DomainAPITests.class, ItemSetAPITests.class, ContentPublishV3TestCases.class, ContentBundleV3TestCases.class})
	@Suite.SuiteClasses({CompositeSearchV3TestCases.class, ConceptAPIV3Tests.class, ContentSuggestionTests.class, DomainV3APITests.class, ItemSetAPITests.class, ContentBundleV3TestCases.class,  ContentFlaggingTests.class, ContentPublishV3TestCases.class, AssesmentItemAPITest.class, MethodV3APITests.class})
	public class RunJunitTests {
	
	}
