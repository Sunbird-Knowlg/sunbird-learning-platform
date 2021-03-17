package org.sunbird.job.samza.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ShallowPublishUtilTest {

	ShallowPublishUtil publishUtil = new ShallowPublishUtil();

	@Test
	public void testGeneratePublishEvent() throws Exception {
		Map<String, Object> metadata = new HashMap<String, Object>() {{
			put("channel", "test");
			put("versionKey", 12345);
			put("pkgVersion", 1.0);
			put("mimeType", "application/vnd.ekstep.content-collection");
			put("lastPublishedBy", "test-user");
			put("contentType", "TextBook");
		}};
		Method method = ShallowPublishUtil.class.getDeclaredMethod("generatePublishEvent", String.class, Map.class, String.class);
		method.setAccessible(true);
		Map<String, Object> event = (Map<String, Object>) method.invoke(publishUtil, "do_1234", metadata, "Live");
		assertEquals("BE_JOB_REQUEST", event.get("eid"));
		assertTrue(event.containsKey("actor"));
		assertTrue(event.containsKey("context"));
		assertEquals("test", ((Map<String, Object>) event.get("context")).get("channel"));
		assertEquals("do_1234", ((Map<String, Object>) event.get("object")).get("id"));
		Map<String, Object> edata = (Map<String, Object>) event.get("edata");
		assertEquals("public", edata.get("publish_type"));
		assertEquals("publish", edata.get("action"));
	}

	@Test
	public void testIsShallowCopy() throws Exception {
		Map<String, Object> input = new HashMap<String, Object>(){{
			put("originData", "{\"name\":\"TS-G-002\",\"copyType\":\"shallow\",\"license\":\"CC BY 4.0\",\"organisation\":[\"Sunbird\"],\"pkgVersion\":6.0}");
		}};
		Method method = ShallowPublishUtil.class.getDeclaredMethod("isShallowCopy", Map.class);
		method.setAccessible(true);
		Boolean result = (Boolean) method.invoke(publishUtil, input);
		assertTrue(result);
	}
}
