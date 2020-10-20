package org.ekstep.content.mgr.impl.operation.event;

import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.mgr.BaseManager;
import org.ekstep.common.mgr.ConvertToGraphNode;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BaseContentManager.class, BaseManager.class, ConvertToGraphNode.class})
@PowerMockIgnore({"javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*", "javax.crypto.*"})
public class RejectFlagOperationTest {
    private static RejectFlagOperation operation = new RejectFlagOperation();

    @Test
    public void testFlag() throws Exception {
        Response response = new Response();
        Node node = new Node();
        node.setIdentifier("do_123");
        node.setObjectType("Content");
        node.setGraphId("domain");
        node.setMetadata(new HashMap<String, Object>() {{
            put("versionKey", "123345");
            put("status", "Flagged");
        }});
        response.getResult().put("node", node);
        response.getResult().put("versionKey", "123345");

        Response responseError = new Response();
        responseError.setResponseCode(ResponseCode.RESOURCE_NOT_FOUND);
        ResponseParams param = new ResponseParams();
        param.setStatus("failed");
        responseError.setParams(param);

        PowerMockito.replace(PowerMockito.method(BaseManager.class, "getDataNode")).with(
                new InvocationHandler() {
                    public Object invoke(Object object, Method method,
                                         Object[] arguments) throws Throwable {
                        if (arguments[1].equals("do_123.img")) {
                            return responseError;
                        } else {
                            return response;
                        }
                    }
                });

        PowerMockito.stub(PowerMockito.method(BaseContentManager.class, "getDefinition")).toReturn(Mockito.anyObject());
        PowerMockito.stub(PowerMockito.method(BaseContentManager.class, "updateNode")).toReturn(Mockito.anyObject());
        PowerMockito.stub(PowerMockito.method(ConvertToGraphNode.class, "convertToGraphNode")).toReturn(Mockito.anyObject());

        Response res = operation.rejectFlag("do_123");
    }
}
