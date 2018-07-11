/**
 * 
 */
package org.ekstep.platform.runner;

import org.ekstep.framework.CategoryTermsV3APITests;
import org.ekstep.framework.CategoryV3APITests;
import org.ekstep.framework.ChannelCategoryInstanceV3APITests;
import org.ekstep.framework.ChannelTermsV3APITests;
import org.ekstep.framework.ChannelV3APITest;
import org.ekstep.framework.FrameworkAPITest;
import org.ekstep.framework.FrameworkCategoryInstanceV3APITests;
import org.ekstep.framework.FrameworkTermsV3APITests;
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
