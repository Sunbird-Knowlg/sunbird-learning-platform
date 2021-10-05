/**
 * 
 */
package org.sunbird.assessment.store;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.sunbird.cassandra.connector.util.CassandraConnectorStoreParam;
import org.sunbird.common.exception.ServerException;
import org.sunbird.test.common.CommonTestSetup;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class AssessmentStoreTest extends CommonTestSetup{

	private static String cassandraScript_1 = "CREATE KEYSPACE IF NOT EXISTS content_store_test WITH replication = {'class': 'SimpleStrategy','replication_factor': '1'};";
	private static String cassandraScript_2 = "CREATE TABLE IF NOT EXISTS content_store_test.question_data_test (question_id text,last_updated_on timestamp,body blob, question blob, solutions blob, editorState blob,PRIMARY KEY (question_id));";

	@Autowired
	private AssessmentStore assessmentStore;

	@BeforeClass
	public static void beforeClass() throws Exception {
		executeScript(cassandraScript_1, cassandraScript_2);
	}
	
	@Test
	public void assessmentStoreTest_01() throws Exception {
		String questId = "A126";
		String body = "Test Data!!!!!!!!!";
		assessmentStore.save(questId, body);
		assertTrue(true);
	}

	@Test
	public void assessmentStoreTest_02() throws Exception {
		String data = "";
		try {
			String questId = "A126";
			data = assessmentStore.read(questId);
			assertTrue(StringUtils.isNotBlank(data));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void assessmentStoreTest_03() throws Exception {
		String questId = "A127";
		String body = "Updated Test Data!!!!!!!!!";
		assessmentStore.update(questId, body);
		assertTrue(true);
	}
	
	@Test
	public void assessmentStoreTest_04() throws Exception {
		Map<String, Object> updateProperties = new HashMap<String, Object>();
		updateProperties.put("body", "A126 Body!!!!!!!!!");
		updateProperties.put("editorstate", "A126 Editorstate!!!!!!!!!");
		assessmentStore.updateAssessmentProperties("A126", updateProperties);
		
		updateProperties = new HashMap<String, Object>();
		updateProperties.put("body", "A127 Body!!!!!!!!!");
		updateProperties.put("editorstate", "A127 Editorstate!!!!!!!!!");
		assessmentStore.updateAssessmentProperties("A127", updateProperties);
		
		List<String> questIds = new ArrayList<>();
		questIds.add("A127");
		questIds.add("A126");
		List<String> readProperties = new ArrayList<>(Arrays.asList("body", "editorstate"));
		Map<String, Object> itemsMap = assessmentStore.getItems(questIds, readProperties);
		assertTrue(!itemsMap.isEmpty());
	}
	
	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();
	@Test(expected = ServerException.class)
	public void assessmentStoreTest_05() throws Exception {
		List<String> questIds = new ArrayList<>();
		questIds.add("A127");
		questIds.add("A126");
		List<String> properties = new ArrayList<>();
		properties.add("body1");
		Map<String, Object> itemsMap = assessmentStore.getItems(questIds, properties);
	}
}
