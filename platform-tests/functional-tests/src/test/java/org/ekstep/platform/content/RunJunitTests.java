package org.ekstep.platform.content;


	import org.junit.runners.Suite;
import org.ekstep.platform.domain.ConceptAPITests;
import org.ekstep.platform.domain.ConceptAPIV3Tests;
import org.ekstep.platform.domain.DimensionAPITests;
import org.ekstep.platform.domain.DimensionAPIV3Tests;
import org.ekstep.platform.domain.DomainAPITests;
import org.ekstep.platform.domain.ItemSetAPITests;
import org.junit.runner.RunWith;
	
	@RunWith(Suite.class)
	//@Suite.SuiteClasses({CompositeSearchV3TestCases.class, ConceptAPIV3Tests.class, DimensionAPIV3Tests.class, DomainAPITests.class, ItemSetAPITests.class, ContentPublishV3TestCases.class, ContentBundleV3TestCases.class})
	@Suite.SuiteClasses({CompositeSearchV3TestCases.class, ConceptAPIV3Tests.class, ContentPublishV3TestCases.class, ContentSuggestionTests.class})
	public class RunJunitTests {
	
	}
