/**
 * 
 */
package org.ekstep.assessment.store;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.ekstep.cassandra.connector.util.CassandraConnectorStoreParam;
import org.ekstep.common.exception.ServerException;
import org.ekstep.test.common.CommonTestSetup;
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
			System.out.println("data:::::::::" + data);
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
		List<String> questIds = new ArrayList<>();
		questIds.add("A127");
		questIds.add("A126");
		List<String> properties = new ArrayList<>();
		properties.add("body");
		Map<String, Object> itemsMap = assessmentStore.getItems(questIds, properties);
		assertTrue(!itemsMap.isEmpty());
	}
	
	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();
	
	@Test(expected = ServerException.class)
	public void assessmentStoreTest_05() throws Exception {
		//exceptionRule.expect(ServerException.class);
		//exceptionRule.expectMessage("Error fetching items from cassandra.");
		List<String> questIds = new ArrayList<>();
		questIds.add("A127");
		questIds.add("A126");
		List<String> properties = new ArrayList<>();
		properties.add("body1");
		Map<String, Object> itemsMap = assessmentStore.getItems(questIds, properties);
		
	}


}
