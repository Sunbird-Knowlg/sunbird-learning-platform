/**
 * 
 */
package org.sunbird.platform.runner;

import org.sunbird.platform.domain.ConceptAPITests;
import org.sunbird.platform.domain.ConceptAPIV3Tests;
import org.sunbird.platform.domain.DataExportImportTestCases;
import org.sunbird.platform.domain.DimensionAPITests;
import org.sunbird.platform.domain.DimensionAPIV3Tests;
import org.sunbird.platform.domain.DomainAPITests;
import org.sunbird.platform.domain.DomainV3APITests;
import org.sunbird.platform.domain.MethodAPITests;
import org.sunbird.platform.domain.MethodV3APITests;
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
