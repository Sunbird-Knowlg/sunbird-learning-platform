package org.ekstep.platform.runner;

import org.ekstep.framework.CategoryTermsV3APITests;
import org.ekstep.framework.CategoryV3APITests;
import org.ekstep.framework.ChannelCategoryInstanceV3APITests;
import org.ekstep.framework.ChannelTermsV3APITests;
import org.ekstep.framework.ChannelV3APITest;
import org.ekstep.framework.FrameworkAPITest;
import org.ekstep.framework.FrameworkCategoryInstanceV3APITests;
import org.ekstep.framework.FrameworkTermsV3APITests;
import org.ekstep.platform.content.AssesmentItemAPITest;
import org.ekstep.platform.content.AssetV3APITest;
import org.ekstep.platform.content.ChannelWorkflowTests;
import org.ekstep.platform.content.CollectionTestCases;
import org.ekstep.platform.content.CompositeSearchSoftConstraintTests;
import org.ekstep.platform.content.CompositeSearchTests;
import org.ekstep.platform.content.CompositeSearchV3TestCases;
import org.ekstep.platform.content.ContentAPITests;
import org.ekstep.platform.content.ContentBundleFunctionalTestCases;
import org.ekstep.platform.content.ContentBundleV3TestCases;
import org.ekstep.platform.content.ContentFlaggingTests;
import org.ekstep.platform.content.ContentPublishV3Test;
import org.ekstep.platform.content.ContentPublishV3TestCases;
import org.ekstep.platform.content.ContentPublishWorkflowTests;
import org.ekstep.platform.content.ContentRejectV3Test;
import org.ekstep.platform.content.ContentSuggestionTests;
import org.ekstep.platform.content.ContentUploadTest;
import org.ekstep.platform.content.ContentV3DialcodeTest;
import org.ekstep.platform.content.CopyContentV3APITests;
import org.ekstep.platform.content.CreateContentWithNewCategoryTest;
import org.ekstep.platform.content.EnrichmentConsumerTests;
import org.ekstep.platform.content.H5PContentWorkflowTest;
import org.ekstep.platform.content.ImageAssetResolutionTests;
import org.ekstep.platform.content.ItemSetAPITests;
import org.ekstep.platform.content.LicenseValidationTest;
import org.ekstep.platform.content.MimeTypeMgrTests;
import org.ekstep.platform.content.ObjectChannelIdAppIdConsumerIdTest;
import org.ekstep.platform.content.TagWorkflowTests;
import org.ekstep.platform.content.UnlistedPublishTestCases;
import org.ekstep.platform.content.UpdateHierarchyTest;
import org.ekstep.platform.content.YoutubeLicenseValidationTest;
import org.ekstep.platform.content.hierarchyAPITests;
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
