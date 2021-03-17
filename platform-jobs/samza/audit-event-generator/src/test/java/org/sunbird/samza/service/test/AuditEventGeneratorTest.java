/**
 * 
 */
package org.sunbird.samza.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotEquals;

import java.util.Map;

import org.sunbird.jobs.samza.service.AuditEventGenerator;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author gauraw
 *
 */
public class AuditEventGeneratorTest {

	AuditEventGenerator auditService = new AuditEventGenerator();
	ObjectMapper mapper = new ObjectMapper();
	String messageWithStatusChange = "{\"ets\":1518517878987,\"nodeUniqueId\":\"do_11243969846440755213\",\"requestId\":null,\"transactionData\":{\"properties\":{\"code\":{\"ov\":null,\"nv\":\"test_code\"},\"keywords\":{\"ov\":null,\"nv\":[\"colors\",\"games\"]},\"channel\":{\"ov\":null,\"nv\":\"in.ekstep\"},\"language\":{\"ov\":null,\"nv\":[\"English\"]},\"mimeType\":{\"ov\":null,\"nv\":\"application/pdf\"},\"idealScreenSize\":{\"ov\":null,\"nv\":\"normal\"},\"createdOn\":{\"ov\":null,\"nv\":\"2018-02-13T16:01:18.947+0530\"},\"contentDisposition\":{\"ov\":null,\"nv\":\"inline\"},\"contentEncoding\":{\"ov\":null,\"nv\":\"identity\"},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2018-02-13T16:01:18.947+0530\"},\"contentType\":{\"ov\":null,\"nv\":\"Story\"},\"audience\":{\"ov\":null,\"nv\":[\"Learner\"]},\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"os\":{\"ov\":null,\"nv\":[\"All\"]},\"visibility\":{\"ov\":null,\"nv\":\"Default\"},\"mediaType\":{\"ov\":null,\"nv\":\"content\"},\"osId\":{\"ov\":null,\"nv\":\"org.sunbird.quiz.app\"},\"versionKey\":{\"ov\":null,\"nv\":\"1518517878947\"},\"idealScreenDensity\":{\"ov\":null,\"nv\":\"hdpi\"},\"framework\":{\"ov\":null,\"nv\":\"NCF\"},\"compatibilityLevel\":{\"ov\":null,\"nv\":1.0},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Content\"},\"name\":{\"ov\":null,\"nv\":\"Untitled Resource\"},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"do_11243969846440755213\"},\"status\":{\"ov\":null,\"nv\":\"Draft\"},\"resourceType\":{\"ov\":null,\"nv\":\"Story\"}}},\"operationType\":\"CREATE\",\"nodeGraphId\":113603,\"label\":\"Untitled Resource\",\"graphId\":\"domain\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2018-02-13T16:01:18.987+0530\",\"objectType\":\"Content\"}";

	@Test
	public void testAuditService_001() throws Exception {
		Map<String, Object> messageData = mapper.readValue(messageWithStatusChange,
				new TypeReference<Map<String, Object>>() {
				});
		Map<String, Object> map = auditService.getAuditMessage(messageData);
		assertEquals("AUDIT", map.get("eid"));
		assertEquals("3.0", map.get("ver"));
		assertNotNull(map.get("edata"));
	}

	@Test
	public void testAuditServiceExpectDurationOfStatusChange() throws Exception {
		String event = "{\"ets\":1552464504681,\"channel\":\"in.ekstep\",\"transactionData\":{\"properties\":{\"lastStatusChangedOn\":{\"ov\":\"2019-03-13T13:25:43.129+0530\",\"nv\":\"2019-03-13T13:38:24.358+0530\"},\"lastSubmittedOn\":{\"ov\":null,\"nv\":\"2019-03-13T13:38:21.901+0530\"},\"lastUpdatedOn\":{\"ov\":\"2019-03-13T13:36:20.093+0530\",\"nv\":\"2019-03-13T13:38:24.399+0530\"},\"status\":{\"ov\":\"Draft\",\"nv\":\"Review\"},\"versionKey\":{\"ov\":\"1552464380093\",\"nv\":\"1552464504399\"}}},\"label\":\"Resource Content 1\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2019-03-13T13:38:24.680+0530\",\"objectType\":\"Content\",\"nodeUniqueId\":\"do_11271778298376192013\",\"requestId\":null,\"operationType\":\"UPDATE\",\"nodeGraphId\":590883,\"graphId\":\"domain\"}";
		Map<String, Object> messageData = mapper.readValue(event,
				new TypeReference<Map<String, Object>>() {
				});
		Map<String, Object> map = auditService.getAuditMessage(messageData);
		assertEquals("AUDIT", map.get("eid"));
		assertEquals("3.0", map.get("ver"));
		assertNotNull(map.get("edata"));
		int duration = (int) ((Map<String, Object>) map.get("edata")).get("duration");
		assertNotEquals(0, duration);
		assertEquals(761, duration);
	}

	@Test
	public void testAuditServiceForNormalUpdateExpectDurationNull() throws Exception {
		String event = "{\"ets\":1552464380225,\"channel\":\"in.ekstep\",\"transactionData\":{\"properties\":{\"s3Key\":{\"ov\":null,\"nv\":\"content/do_11271778298376192013/artifact/pdf_1552464372724.pdf\"},\"size\":{\"ov\":null,\"nv\":433994.0},\"artifactUrl\":{\"ov\":null,\"nv\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/do_11271778298376192013/artifact/pdf_1552464372724.pdf\"},\"lastUpdatedOn\":{\"ov\":\"2019-03-13T13:25:43.129+0530\",\"nv\":\"2019-03-13T13:36:20.093+0530\"},\"versionKey\":{\"ov\":\"1552463743129\",\"nv\":\"1552464380093\"}}},\"label\":\"Resource Content 1\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2019-03-13T13:36:20.223+0530\",\"objectType\":\"Content\",\"nodeUniqueId\":\"do_11271778298376192013\",\"requestId\":null,\"operationType\":\"UPDATE\",\"nodeGraphId\":590883,\"graphId\":\"domain\"}";
		Map<String, Object> messageData = mapper.readValue(event,
				new TypeReference<Map<String, Object>>() {
				});
		Map<String, Object> map = auditService.getAuditMessage(messageData);
		assertEquals("AUDIT", map.get("eid"));
		assertEquals("3.0", map.get("ver"));
		assertNotNull(map.get("edata"));
		int duration = (int) ((Map<String, Object>) map.get("edata")).getOrDefault("duration", 0);
		assertEquals(0, duration);
	}

	@Test
	public void testAuditServiceForCreateContent() throws Exception {
		String event = "{\"ets\":1552645516180,\"channel\":\"in.ekstep\",\"transactionData\":{\"properties\":{\"ownershipType\":{\"ov\":null,\"nv\":[\"createdBy\"]},\"code\":{\"ov\":null,\"nv\":\"test.res.1\"},\"channel\":{\"ov\":null,\"nv\":\"in.ekstep\"},\"language\":{\"ov\":null,\"nv\":[\"English\"]},\"mimeType\":{\"ov\":null,\"nv\":\"application/pdf\"},\"idealScreenSize\":{\"ov\":null,\"nv\":\"normal\"},\"createdOn\":{\"ov\":null,\"nv\":\"2019-03-15T15:55:16.071+0530\"},\"contentDisposition\":{\"ov\":null,\"nv\":\"inline\"},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2019-03-15T15:55:16.071+0530\"},\"contentEncoding\":{\"ov\":null,\"nv\":\"identity\"},\"dialcodeRequired\":{\"ov\":null,\"nv\":\"No\"},\"contentType\":{\"ov\":null,\"nv\":\"Resource\"},\"lastStatusChangedOn\":{\"ov\":null,\"nv\":\"2019-03-15T15:55:16.071+0530\"},\"audience\":{\"ov\":null,\"nv\":[\"Learner\"]},\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"visibility\":{\"ov\":null,\"nv\":\"Default\"},\"os\":{\"ov\":null,\"nv\":[\"All\"]},\"mediaType\":{\"ov\":null,\"nv\":\"content\"},\"osId\":{\"ov\":null,\"nv\":\"org.sunbird.quiz.app\"},\"versionKey\":{\"ov\":null,\"nv\":\"1552645516071\"},\"idealScreenDensity\":{\"ov\":null,\"nv\":\"hdpi\"},\"framework\":{\"ov\":null,\"nv\":\"NCF\"},\"compatibilityLevel\":{\"ov\":null,\"nv\":1.0},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Content\"},\"name\":{\"ov\":null,\"nv\":\"Resource Content 1\"},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"do_11271927206783385611\"},\"status\":{\"ov\":null,\"nv\":\"Draft\"}}},\"mid\":\"9ea9ae7a-9cc1-493d-aac3-3c66cd9ff01b\",\"label\":\"Resource Content 1\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2019-03-15T15:55:16.178+0530\",\"objectType\":\"Content\",\"nodeUniqueId\":\"do_11271927206783385611\",\"requestId\":null,\"operationType\":\"CREATE\",\"nodeGraphId\":590921,\"graphId\":\"domain\"}";
		Map<String, Object> messageData = mapper.readValue(event,
				new TypeReference<Map<String, Object>>() {
				});
		Map<String, Object> map = auditService.getAuditMessage(messageData);
		assertEquals("AUDIT", map.get("eid"));
		assertEquals("3.0", map.get("ver"));
		assertNotNull(map.get("edata"));
		int duration = (int) ((Map<String, Object>) map.get("edata")).getOrDefault("duration", 0);
		assertEquals(0, duration);

	}

	@Test
	public void testComputeDuration() {
		String ov = "2019-03-13T13:25:43.129+0530";
		String nv = "2019-03-13T13:38:24.358+0530";
		long duration = auditService.computeDuration(ov, nv);
		assertEquals(761, duration);
	}
}
