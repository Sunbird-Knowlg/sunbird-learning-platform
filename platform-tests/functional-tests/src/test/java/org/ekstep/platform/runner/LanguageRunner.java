/**
 * 
 */
package org.ekstep.platform.runner;

import org.ekstep.platform.language.DictionaryAPITests;
import org.ekstep.platform.language.LessonComplexityTestCases;
import org.ekstep.platform.language.LinkWordsTestCases;
import org.ekstep.platform.language.ToolsAPITests;
import org.ekstep.platform.language.WordPatchUpdateTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author gauraw
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({DictionaryAPITests.class, LessonComplexityTestCases.class, LinkWordsTestCases.class, ToolsAPITests.class, WordPatchUpdateTest.class})
public class LanguageRunner {

}
