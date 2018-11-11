package org.ekstep.platform.runner;

import org.ekstep.framework.CategoryTermsV3APITests;
import org.ekstep.framework.CategoryV3APITests;
import org.ekstep.framework.ChannelCategoryInstanceV3APITests;
import org.ekstep.framework.ChannelTermsV3APITests;
import org.ekstep.framework.ChannelV3APITest;
import org.ekstep.framework.FrameworkAPITest;
import org.ekstep.framework.FrameworkCategoryInstanceV3APITests;
import org.ekstep.framework.FrameworkTermsV3APITests;
import org.ekstep.platform.content.*;
import org.ekstep.platform.dialcode.DialCodeV3APITest;
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
@Suite.SuiteClasses({
	ChannelWorkflowTests.class,  CollectionTestCases.class,  CompositeSearchSoftConstraintTests.class, CompositeSearchTests.class, CompositeSearchV3TestCases.class, ContentAPITests.class, ContentBundleFunctionalTestCases.class, ContentBundleV3TestCases.class, ContentFlaggingTests.class, ContentPublishV3Test.class, ContentPublishV3TestCases.class, ContentPublishWorkflowTests.class, ContentRejectV3Test.class, ContentSuggestionTests.class, ContentUploadTest.class, CopyContentV3APITests.class, CreateContentWithNewCategoryTest.class, EnrichmentConsumerTests.class, H5PContentWorkflowTest.class, hierarchyAPITests.class, ImageAssetResolutionTests.class, MimeTypeMgrTests.class, ObjectChannelIdAppIdConsumerIdTest.class, TagWorkflowTests.class, UnlistedPublishTestCases.class, UpdateHierarchyTest.class, YoutubeLicenseValidationTest.class, AssesmentItemAPITest.class, ItemSetAPITests.class,
	DialCodeV3APITest.class, 
	FrameworkAPITest.class, ChannelV3APITest.class, CategoryV3APITests.class, CategoryTermsV3APITests.class, FrameworkCategoryInstanceV3APITests.class, FrameworkTermsV3APITests.class, ChannelCategoryInstanceV3APITests.class, ChannelTermsV3APITests.class,
	ConceptAPITests.class, ConceptAPIV3Tests.class, DataExportImportTestCases.class, DimensionAPITests.class, DimensionAPIV3Tests.class, DomainAPITests.class, DomainV3APITests.class, MethodAPITests.class, MethodV3APITests.class,
	AssetV3APITest.class, LicenseValidationTest.class, ContentV3DialcodeTest.class
	})
public class AllTestRunner {

}
