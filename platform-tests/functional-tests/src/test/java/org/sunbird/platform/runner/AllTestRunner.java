package org.sunbird.platform.runner;

import org.sunbird.framework.CategoryTermsV3APITests;
import org.sunbird.framework.CategoryV3APITests;
import org.sunbird.framework.ChannelCategoryInstanceV3APITests;
import org.sunbird.framework.ChannelTermsV3APITests;
import org.sunbird.framework.ChannelV3APITest;
import org.sunbird.framework.FrameworkAPITest;
import org.sunbird.framework.FrameworkCategoryInstanceV3APITests;
import org.sunbird.framework.FrameworkTermsV3APITests;
import org.sunbird.platform.content.AssesmentItemAPITest;
import org.sunbird.platform.content.AssetV3APITest;
import org.sunbird.platform.content.ChannelWorkflowTests;
import org.sunbird.platform.content.CollectionTestCases;
import org.sunbird.platform.content.CompositeSearchSoftConstraintTests;
import org.sunbird.platform.content.CompositeSearchTests;
import org.sunbird.platform.content.CompositeSearchV3TestCases;
import org.sunbird.platform.content.ContentAPITests;
import org.sunbird.platform.content.ContentBundleFunctionalTestCases;
import org.sunbird.platform.content.ContentBundleV3TestCases;
import org.sunbird.platform.content.ContentFlaggingTests;
import org.sunbird.platform.content.ContentPublishV3Test;
import org.sunbird.platform.content.ContentPublishV3TestCases;
import org.sunbird.platform.content.ContentPublishWorkflowTests;
import org.sunbird.platform.content.ContentRejectV3Test;
import org.sunbird.platform.content.ContentSuggestionTests;
import org.sunbird.platform.content.ContentUploadTest;
import org.sunbird.platform.content.ContentV3DialcodeTest;
import org.sunbird.platform.content.CopyContentV3APITests;
import org.sunbird.platform.content.CreateContentWithNewCategoryTest;
import org.sunbird.platform.content.EnrichmentConsumerTests;
import org.sunbird.platform.content.H5PContentWorkflowTest;
import org.sunbird.platform.content.ImageAssetResolutionTests;
import org.sunbird.platform.content.ItemSetAPITests;
import org.sunbird.platform.content.LicenseValidationTest;
import org.sunbird.platform.content.MimeTypeMgrTests;
import org.sunbird.platform.content.ObjectChannelIdAppIdConsumerIdTest;
import org.sunbird.platform.content.TagWorkflowTests;
import org.sunbird.platform.content.UnlistedPublishTestCases;
import org.sunbird.platform.content.UpdateHierarchyTest;
import org.sunbird.platform.content.YoutubeLicenseValidationTest;
import org.sunbird.platform.content.hierarchyAPITests;
import org.sunbird.platform.dialcode.DialCodeV3APITest;
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
@Suite.SuiteClasses({
	ChannelWorkflowTests.class,  CollectionTestCases.class,  CompositeSearchSoftConstraintTests.class, CompositeSearchTests.class, CompositeSearchV3TestCases.class, ContentAPITests.class, ContentBundleFunctionalTestCases.class, ContentBundleV3TestCases.class, ContentFlaggingTests.class, ContentPublishV3Test.class, ContentPublishV3TestCases.class, ContentPublishWorkflowTests.class, ContentRejectV3Test.class, ContentSuggestionTests.class, ContentUploadTest.class, CopyContentV3APITests.class, CreateContentWithNewCategoryTest.class, EnrichmentConsumerTests.class, H5PContentWorkflowTest.class, hierarchyAPITests.class, ImageAssetResolutionTests.class, MimeTypeMgrTests.class, ObjectChannelIdAppIdConsumerIdTest.class, TagWorkflowTests.class, UnlistedPublishTestCases.class, UpdateHierarchyTest.class, YoutubeLicenseValidationTest.class, AssesmentItemAPITest.class, ItemSetAPITests.class,
	DialCodeV3APITest.class, 
	FrameworkAPITest.class, ChannelV3APITest.class, CategoryV3APITests.class, CategoryTermsV3APITests.class, FrameworkCategoryInstanceV3APITests.class, FrameworkTermsV3APITests.class, ChannelCategoryInstanceV3APITests.class, ChannelTermsV3APITests.class,
	ConceptAPITests.class, ConceptAPIV3Tests.class, DataExportImportTestCases.class, DimensionAPITests.class, DimensionAPIV3Tests.class, DomainAPITests.class, DomainV3APITests.class, MethodAPITests.class, MethodV3APITests.class,
	AssetV3APITest.class, LicenseValidationTest.class, ContentV3DialcodeTest.class
	})
public class AllTestRunner {

}
