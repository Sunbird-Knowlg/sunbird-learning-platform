/**
 * 
 */
package org.sunbird.platform.runner;

import org.sunbird.framework.CategoryTermsV3APITests;
import org.sunbird.framework.CategoryV3APITests;
import org.sunbird.framework.ChannelCategoryInstanceV3APITests;
import org.sunbird.framework.ChannelTermsV3APITests;
import org.sunbird.framework.ChannelV3APITest;
import org.sunbird.framework.FrameworkAPITest;
import org.sunbird.framework.FrameworkCategoryInstanceV3APITests;
import org.sunbird.framework.FrameworkTermsV3APITests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author gauraw
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({FrameworkAPITest.class, ChannelV3APITest.class, CategoryV3APITests.class, CategoryTermsV3APITests.class, FrameworkCategoryInstanceV3APITests.class, FrameworkTermsV3APITests.class, ChannelCategoryInstanceV3APITests.class, ChannelTermsV3APITests.class})
public class FrameworkRunner {

}
