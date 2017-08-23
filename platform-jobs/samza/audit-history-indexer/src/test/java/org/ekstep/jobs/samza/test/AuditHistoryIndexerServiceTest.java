package org.ekstep.jobs.samza.test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;

import org.ekstep.jobs.samza.service.AuditHistoryIndexerService;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.dac.dto.AuditHistoryRecord;

import info.aduna.io.FileUtil;

public class AuditHistoryIndexerServiceTest {

	String validMessage = "{\"nodeUniqueId\":\"org.ekstep.jul03.story.test01\",\"requestId\":\"110dc48a-b0ee-4a64-b822-1053ab7ef276\",\"transactionData\":{\"properties\":{\"owner\":{\"ov\":null,\"nv\":\"EkStep\"},\"code\":{\"ov\":null,\"nv\":\"org.ekstep.jul03.story.test01\"},\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"visibility\":{\"ov\":null,\"nv\":\"Default\"},\"os\":{\"ov\":null,\"nv\":[\"All\"]},\"subject\":{\"ov\":null,\"nv\":\"literacy\"},\"portalOwner\":{\"ov\":null,\"nv\":\"EkStep\"},\"description\":{\"ov\":null,\"nv\":\"शेर का साथी हाथी\"},\"language\":{\"ov\":null,\"nv\":[\"English\"]},\"mediaType\":{\"ov\":null,\"nv\":\"content\"},\"osId\":{\"ov\":null,\"nv\":\"org.ekstep.quiz.app\"},\"mimeType\":{\"ov\":null,\"nv\":\"application/vnd.ekstep.ecml-archive\"},\"ageGroup\":{\"ov\":null,\"nv\":[\"5-6\"]},\"idealScreenSize\":{\"ov\":null,\"nv\":\"normal\"},\"createdOn\":{\"ov\":null,\"nv\":\"2016-07-03T15:39:34.570+0530\"},\"idealScreenDensity\":{\"ov\":null,\"nv\":\"hdpi\"},\"gradeLevel\":{\"ov\":null,\"nv\":[\"Grade 1\"]},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Content\"},\"name\":{\"ov\":null,\"nv\":\"शेर का साथी हाथी\"},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2016-07-03T15:39:34.570+0530\"},\"developer\":{\"ov\":null,\"nv\":\"EkStep\"},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"org.ekstep.jul03.story.test01\"},\"contentType\":{\"ov\":null,\"nv\":\"Story\"},\"status\":{\"ov\":null,\"nv\":\"Mock\"}}},\"operationType\":\"CREATE\",\"nodeGraphId\":974,\"label\":\"शेर का साथी हाथी\",\"graphId\":\"domain\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2016-07-03T15:39:34.570+0530\",\"objectType\":\"Content\"}";
	String invalidMessage = "{\"requestId\":\"110dc48a-b0ee-4a64-b822-1053ab7ef276\",\"transactionData\":{\"properties\":{\"owner\":{\"ov\":null,\"nv\":\"EkStep\"},\"code\":{\"ov\":null,\"nv\":\"org.ekstep.jul03.story.test01\"},\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"visibility\":{\"ov\":null,\"nv\":\"Default\"},\"os\":{\"ov\":null,\"nv\":[\"All\"]},\"subject\":{\"ov\":null,\"nv\":\"literacy\"},\"portalOwner\":{\"ov\":null,\"nv\":\"EkStep\"},\"description\":{\"ov\":null,\"nv\":\"शेर का साथी हाथी\"},\"language\":{\"ov\":null,\"nv\":[\"English\"]},\"mediaType\":{\"ov\":null,\"nv\":\"content\"},\"osId\":{\"ov\":null,\"nv\":\"org.ekstep.quiz.app\"},\"mimeType\":{\"ov\":null,\"nv\":\"application/vnd.ekstep.ecml-archive\"},\"ageGroup\":{\"ov\":null,\"nv\":[\"5-6\"]},\"idealScreenSize\":{\"ov\":null,\"nv\":\"normal\"},\"createdOn\":{\"ov\":null,\"nv\":\"2016-07-03T15:39:34.570+0530\"},\"idealScreenDensity\":{\"ov\":null,\"nv\":\"hdpi\"},\"gradeLevel\":{\"ov\":null,\"nv\":[\"Grade 1\"]},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Content\"},\"name\":{\"ov\":null,\"nv\":\"शेर का साथी हाथी\"},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2016-07-03T15:39:34.570+0530\"},\"developer\":{\"ov\":null,\"nv\":\"EkStep\"},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"org.ekstep.jul03.story.test01\"},\"contentType\":{\"ov\":null,\"nv\":\"Story\"},\"status\":{\"ov\":null,\"nv\":\"Mock\"}}},\"operationType\":\"CREATE\",\"nodeGraphId\":974,\"label\":\"शेर का साथी हाथी\",\"graphId\":\"domain\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2016-07-03T15:39:34.570+0530\",\"objectType\":\"Content\"}";
	AuditHistoryIndexerService service = new AuditHistoryIndexerService();
	static File tempDir = null;
	static Settings settings = null;
	static Node server = null;
	static String clusterName = "";
	Map<String, Object> messageData = new HashMap<String, Object>();
	ObjectMapper mapper = new ObjectMapper();

	public void AuditHistoryTest(String message) throws JsonParseException, JsonMappingException, IOException {
		messageData = mapper.readValue(message, new TypeReference<Map<String, Object>>() {
		});
	}

	@BeforeClass
	public static void beforeClass(){
		tempDir = new File(System.getProperty("user.dir") + "/tmp");
		settings = Settings.builder()
				.put("path.home", tempDir.getAbsolutePath())
				.put("transport.tcp.port","9500")
				.build();
		server = NodeBuilder.nodeBuilder().settings(settings).build();
		clusterName = server.settings().get("cluster.name");
	}
	
	@AfterClass
	public static void afterClass() throws IOException{
		FileUtil.deleteDir(tempDir);
	}
	
	@Test
	public void getAuditRecordWithValidRequest() throws Exception {
		AuditHistoryTest(validMessage);
		AuditHistoryRecord map = service.getAuditHistory(messageData);
		assertEquals(map.getObjectType(), "Content");
		assertEquals(map.getGraphId(), "domain");
		assertEquals(map.getObjectId(), "org.ekstep.jul03.story.test01");
	}

	@Test
	public void getValidAuditRecordWithInvalidRequest() throws Exception {
		AuditHistoryTest(invalidMessage);
		AuditHistoryRecord record = service.getAuditHistory(messageData);
		assertEquals(record.getObjectId(), null);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testSummaryData() throws Exception {
		AuditHistoryTest(validMessage);
		String summary = service.setSummaryData(messageData);
		Map summaryMap = mapper.readValue(summary, new TypeReference<Map<String, Object>>() {
		});
		assertEquals(true, summaryMap.containsKey("properties"));
		Map<String, Object> properties = (Map) summaryMap.get("properties");
		assertEquals(properties.containsKey("fields"), true);
		assertEquals(properties.containsKey("count"), true);
	}

	@Test
	public void addRecordToEs() throws Exception {
		server.start();
		Thread.sleep(2000);
		Client client = server.client();
		ElasticSearchUtil util = new ElasticSearchUtil(client);
		AuditHistoryTest(validMessage);
		AuditHistoryRecord record = service.getAuditHistory(messageData);
		util.add(record);
		Thread.sleep(2000);
		Map<String, Object> result = util.findById("org.ekstep.jul03.story.test01");
		assertEquals(result.get("graphId"), "domain");
		assertEquals(result.containsKey("operation"), true);
		server.close();
	}
}
