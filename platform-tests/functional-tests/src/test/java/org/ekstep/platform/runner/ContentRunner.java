/**
 * 
 */
package org.ekstep.platform.runner;

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
import org.ekstep.platform.content.CopyContentV3APITests;
import org.ekstep.platform.content.CreateContentWithNewCategoryTest;
import org.ekstep.platform.content.DefinitionsV3Tests;
import org.ekstep.platform.content.EnrichmentConsumerTests;
import org.ekstep.platform.content.H5PContentWorkflowTest;
import org.ekstep.platform.content.ImageAssetResolutionTests;
import org.ekstep.platform.content.MimeTypeMgrTests;
import org.ekstep.platform.content.ObjectChannelIdAppIdConsumerIdTest;
import org.ekstep.platform.content.TagWorkflowTests;
import org.ekstep.platform.content.UnlistedPublishTestCases;
import org.ekstep.platform.content.UpdateHierarchyTest;
import org.ekstep.platform.content.YoutubeLicenseValidationTest;
import org.ekstep.platform.content.hierarchyAPITests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author gauraw
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ChannelWorkflowTests.class, CollectionTestCases.class, CompositeSearchSoftConstraintTests.class,CompositeSearchTests.class,CompositeSearchV3TestCases.class,ContentAPITests.class,ContentBundleFunctionalTestCases.class,ContentBundleV3TestCases.class,ContentFlaggingTests.class,ContentPublishV3Test.class,ContentPublishV3TestCases.class,ContentPublishWorkflowTests.class,ContentRejectV3Test.class,ContentSuggestionTests.class,ContentUploadTest.class,CopyContentV3APITests.class,CreateContentWithNewCategoryTest.class,DefinitionsV3Tests.class,EnrichmentConsumerTests.class,H5PContentWorkflowTest.class,hierarchyAPITests.class,ImageAssetResolutionTests.class,MimeTypeMgrTests.class,ObjectChannelIdAppIdConsumerIdTest.class,TagWorkflowTests.class,UnlistedPublishTestCases.class,UpdateHierarchyTest.class,YoutubeLicenseValidationTest.class})
//@Suite.SuiteClasses(ContentAPITests.class)
public class ContentRunner {

}
