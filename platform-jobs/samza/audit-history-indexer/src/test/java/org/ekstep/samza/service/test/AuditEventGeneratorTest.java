/**
 * 
 */
package org.ekstep.samza.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.ekstep.jobs.samza.service.AuditEventGenerator;
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
	String messageWithStatusChange = "{\"ets\":1518517878987,\"nodeUniqueId\":\"do_11243969846440755213\",\"requestId\":null,\"transactionData\":{\"properties\":{\"code\":{\"ov\":null,\"nv\":\"test_code\"},\"keywords\":{\"ov\":null,\"nv\":[\"colors\",\"games\"]},\"channel\":{\"ov\":null,\"nv\":\"in.ekstep\"},\"language\":{\"ov\":null,\"nv\":[\"English\"]},\"mimeType\":{\"ov\":null,\"nv\":\"application/pdf\"},\"idealScreenSize\":{\"ov\":null,\"nv\":\"normal\"},\"createdOn\":{\"ov\":null,\"nv\":\"2018-02-13T16:01:18.947+0530\"},\"contentDisposition\":{\"ov\":null,\"nv\":\"inline\"},\"contentEncoding\":{\"ov\":null,\"nv\":\"identity\"},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2018-02-13T16:01:18.947+0530\"},\"contentType\":{\"ov\":null,\"nv\":\"Story\"},\"audience\":{\"ov\":null,\"nv\":[\"Learner\"]},\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"os\":{\"ov\":null,\"nv\":[\"All\"]},\"visibility\":{\"ov\":null,\"nv\":\"Default\"},\"mediaType\":{\"ov\":null,\"nv\":\"content\"},\"osId\":{\"ov\":null,\"nv\":\"org.ekstep.quiz.app\"},\"versionKey\":{\"ov\":null,\"nv\":\"1518517878947\"},\"idealScreenDensity\":{\"ov\":null,\"nv\":\"hdpi\"},\"framework\":{\"ov\":null,\"nv\":\"NCF\"},\"compatibilityLevel\":{\"ov\":null,\"nv\":1.0},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Content\"},\"name\":{\"ov\":null,\"nv\":\"Untitled Resource\"},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"do_11243969846440755213\"},\"status\":{\"ov\":null,\"nv\":\"Draft\"},\"resourceType\":{\"ov\":null,\"nv\":\"Story\"}}},\"operationType\":\"CREATE\",\"nodeGraphId\":113603,\"label\":\"Untitled Resource\",\"graphId\":\"domain\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2018-02-13T16:01:18.987+0530\",\"objectType\":\"Content\"}";

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
}
