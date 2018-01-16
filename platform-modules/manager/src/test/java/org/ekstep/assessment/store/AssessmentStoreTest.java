/**
 * 
 */
package org.ekstep.assessment.store;

import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * @author gauraw
 *
 */

// @Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class AssessmentStoreTest {

	@Autowired
	private AssessmentStore assessmentStore;

	@Test
	public void assessmentStoreTest_01() throws Exception {
		String questId = "A124";
		String body = "Test Data!!!!!!!!!";
		assessmentStore.save(questId, body);
		assertTrue(true);
	}

	@Test
	public void assessmentStoreTest_02() throws Exception {
		String data = "";
		try {
			String questId = "A123";
			data = assessmentStore.read(questId);
			assertTrue(StringUtils.isNotBlank(data));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void assessmentStoreTest_03() throws Exception {
		String questId = "A123";
		String body = "Updated Test Data!!!!!!!!!";
		assessmentStore.update(questId, body);
		assertTrue(true);
	}


}
