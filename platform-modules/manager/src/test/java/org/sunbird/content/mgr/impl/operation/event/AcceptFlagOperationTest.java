package org.sunbird.content.mgr.impl.operation.event;

import org.sunbird.common.dto.ResponseParams;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.taxonomy.mgr.impl.BaseContentManager;
import org.sunbird.common.mgr.BaseManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.common.dto.Response;

import java.util.HashMap;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BaseContentManager.class, BaseManager.class})
@PowerMockIgnore({"javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*", "javax.crypto.*"})
public class AcceptFlagOperationTest {

    private static AcceptFlagOperation operation = new AcceptFlagOperation();

    @Test
    public void testAcceptFlag() throws Exception {
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
        PowerMockito.stub(PowerMockito.method(BaseContentManager.class, "validateAndGetNodeResponseForOperation")).toReturn(response);
        PowerMockito.stub(PowerMockito.method(BaseContentManager.class, "getDefinition")).toReturn(Mockito.anyObject());
        PowerMockito.stub(PowerMockito.method(BaseContentManager.class, "getExternalPropsList")).toReturn(Mockito.anyObject());
        PowerMockito.stub(PowerMockito.method(BaseContentManager.class, "updateDataNode")).toReturn(response);
        PowerMockito.stub(PowerMockito.method(BaseContentManager.class, "clearRedisCache")).toReturn(0);
        ;
        PowerMockito.stub(PowerMockito.method(BaseManager.class, "getDataNode")).toReturn(response);
        Response res = operation.acceptFlag("do_123");
    }

    @Test
    public void testAcceptFlagImg() throws Exception {
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
        PowerMockito.stub(PowerMockito.method(BaseContentManager.class, "validateAndGetNodeResponseForOperation")).toReturn(response);
        PowerMockito.stub(PowerMockito.method(BaseContentManager.class, "getDefinition")).toReturn(Mockito.anyObject());
        PowerMockito.stub(PowerMockito.method(BaseContentManager.class, "getExternalPropsList")).toReturn(Mockito.anyObject());
        PowerMockito.stub(PowerMockito.method(BaseContentManager.class, "updateDataNode")).toReturn(response);
        PowerMockito.stub(PowerMockito.method(BaseContentManager.class, "clearRedisCache")).toReturn(0);
        PowerMockito.stub(PowerMockito.method(BaseContentManager.class, "createDataNode", Node.class)).toReturn(response);
        PowerMockito.stub(PowerMockito.method(BaseContentManager.class, "updateContentProperties")).toReturn(response);
        PowerMockito.stub(PowerMockito.method(BaseContentManager.class, "getContentProperties")).toReturn(response);

        Response responseError = new Response();
        responseError.setResponseCode(ResponseCode.RESOURCE_NOT_FOUND);
        ResponseParams param = new ResponseParams();
        param.setStatus("failed");
        responseError.setParams(param);
        PowerMockito.stub(PowerMockito.method(BaseManager.class, "getDataNode")).toReturn(responseError);
        PowerMockito.stub(PowerMockito.method(BaseManager.class, "getDataNode")).toReturn(response);

        Response res = operation.acceptFlag("do_123");
    }
}
