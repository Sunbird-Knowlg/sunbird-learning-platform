package org.ekstep.platform.content;


	import org.junit.runners.Suite;
import org.ekstep.platform.domain.ConceptAPITests;
import org.ekstep.platform.domain.DimensionAPITests;
import org.ekstep.platform.domain.DomainAPITests;
import org.ekstep.platform.domain.ItemSetAPITests;
import org.junit.runner.RunWith;
	
	@RunWith(Suite.class)
	@Suite.SuiteClasses({CompositeSearchTests.class, ConceptAPITests.class, DimensionAPITests.class, DomainAPITests.class, ItemSetAPITests.class, ContentPublishV3TestCases.class, ContentBundleV3TestCases.class})
	public class RunJunitTests {
	
	}
