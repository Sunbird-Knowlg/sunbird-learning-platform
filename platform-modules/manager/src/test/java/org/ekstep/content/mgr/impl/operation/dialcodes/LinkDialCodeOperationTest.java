package org.ekstep.content.mgr.impl.operation.dialcodes;

import org.ekstep.common.dto.Response;
import org.ekstep.common.mgr.BaseManager;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BaseContentManager.class, BaseManager.class})
@PowerMockIgnore({"javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*", "javax.crypto.*"})
public class LinkDialCodeOperationTest {

	private LinkDialCodeOperation operation = new LinkDialCodeOperation();

	@Test
	public void testUpdateDataNode() throws Exception {
		Response response = new Response();
		Node node = new Node();
		node.setIdentifier("do_123");
		node.setObjectType("Content");
		node.setGraphId("domain");
		node.setMetadata(new HashMap<String, Object>() {{
			put("versionKey", "123345");
			put("status", "Live");
		}});
		response.getResult().put("node", node);
		response.getResult().put("versionKey", "123345");
		Map<String, Object> requestMap = new HashMap<String, Object>() {{
			put("dialcodes", new ArrayList<String>() {{
				add("ABC123");
			}});
		}};

		PowerMockito.stub(PowerMockito.method(BaseContentManager.class, "getDefinition")).toReturn(Mockito.anyObject());
		PowerMockito.stub(PowerMockito.method(BaseContentManager.class, "updateNode")).toReturn(Mockito.anyObject());
		PowerMockito.stub(PowerMockito.method(BaseContentManager.class, "clearRedisCache")).toReturn(0);
		PowerMockito.stub(PowerMockito.method(BaseManager.class, "getDataNode")).toReturn(response);
		PowerMockito.stub(PowerMockito.method(BaseContentManager.class, "updateDataNode")).toReturn(response);
		Method method = LinkDialCodeOperation.class.getDeclaredMethod("updateDataNode", String.class, Map.class, String.class);
		method.setAccessible(true);
		Response res = (Response) method.invoke(operation, "do_123", requestMap, null);
		Assert.assertTrue(null != res);
	}
}
