/**
 * 
 */
package org.ekstep.platform.runner;

import org.ekstep.platform.domain.ConceptAPITests;
import org.ekstep.platform.domain.ConceptAPIV3Tests;
import org.ekstep.platform.domain.DataExportImportTestCases;
import org.ekstep.platform.domain.DimensionAPITests;
import org.ekstep.platform.domain.DimensionAPIV3Tests;
import org.ekstep.platform.domain.DomainAPITests;
import org.ekstep.platform.domain.DomainV3APITests;
import org.ekstep.platform.domain.MethodAPITests;
import org.ekstep.platform.domain.MethodV3APITests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author gauraw
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ConceptAPITests.class, ConceptAPIV3Tests.class, DataExportImportTestCases.class, DimensionAPITests.class, DimensionAPIV3Tests.class, DomainAPITests.class, DomainV3APITests.class, MethodAPITests.class, MethodV3APITests.class})
public class DomainRunner {

}
