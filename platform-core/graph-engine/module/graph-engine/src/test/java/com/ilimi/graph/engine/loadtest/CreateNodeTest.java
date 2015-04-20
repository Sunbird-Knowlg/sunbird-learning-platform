package com.ilimi.graph.engine.loadtest;

import java.util.Map;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.pattern.Patterns;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.dto.BaseValueObjectMap;
import com.ilimi.graph.common.dto.StringValue;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.engine.router.GraphEngineManagers;

public class CreateNodeTest {
    ActorRef reqRouter = null;
//    String graphId = "GRAPH_" + System.currentTimeMillis();
    String graphId = "SEARCH_GRAPH";
    String SCENARIO_NAME ="CREATE_NODE";
    
//    @BeforeTest
    public void init() throws Exception {
        String logFileName = SCENARIO_NAME +"_" + System.currentTimeMillis();
        System.out.println("Logs are captured in "+logFileName+".log file.");
        LoggerUtil.config(logFileName);
        reqRouter = TestUtil.initReqRouter();
        createGraph(reqRouter, graphId);
        Thread.sleep(5000);
        Future<Object> defNodeRes = saveDefinitionNode(reqRouter, graphId);
        TestUtil.handleFutureBlock(defNodeRes, "saveDefinitionNode", GraphDACParams.NODE_ID.name());
        Thread.sleep(5000);
    }
    
    private Future<Object> saveDefinitionNode(ActorRef reqRouter2, String graphId2) {
        Request request = new Request();
        request.getContext().put(GraphHeaderParams.GRAPH_ID.name(), graphId);
        request.setManagerName(GraphEngineManagers.NODE_MANAGER);
        request.setOperation("saveDefinitionNode");
        request.put(GraphDACParams.OBJECT_TYPE.name(), new StringValue("COURSE"));
        Future<Object> req = Patterns.ask(reqRouter, request, TestUtil.timeout);
        return req;
    }

//    @AfterTest
    public void destroy() throws Exception {
        Thread.sleep(10000);
    }
        
//    @Test(threadPoolSize=1000, invocationCount=1000)
    public void testCreateNode() {
        try {
            String nodeId = "Node_"+System.currentTimeMillis()+"_"+Thread.currentThread().getId();
            String objectType = "COURSE";
            Map<String, Object> metadata = TestUtil.getMetadata((int)Thread.currentThread().getId()%10);
            Future<Object> nodeReq = createDataNode(reqRouter, graphId, nodeId, objectType, metadata);
            TestUtil.handleFutureBlock(nodeReq, "createDataNode", GraphDACParams.NODE_ID.name());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private Future<Object> createDataNode(ActorRef reqRouter, String graphId, String nodeId, String objectType, Map<String, Object> metadata) {
        Request request = new Request();
        request.getContext().put(GraphHeaderParams.GRAPH_ID.name(), graphId);
        request.getContext().put(GraphHeaderParams.REQUEST_ID.name(), "REQUEST_"+Thread.currentThread().getId());
        request.getContext().put(GraphHeaderParams.SCENARIO_NAME.name(), SCENARIO_NAME);
        request.setManagerName(GraphEngineManagers.NODE_MANAGER);
        request.setOperation("createDataNode");
        request.put(GraphDACParams.NODE_ID.name(), new StringValue(nodeId));
        request.put(GraphDACParams.OBJECT_TYPE.name(), new StringValue(objectType));
        request.put(GraphDACParams.METADATA.name(), new BaseValueObjectMap<Object>(metadata));
        Future<Object> req = Patterns.ask(reqRouter, request, TestUtil.timeout);
        return req;
    }
    
    private Future<Object> createGraph(ActorRef reqRouter, String graphId) {
        Request request = new Request();
        request.getContext().put(GraphHeaderParams.REQUEST_ID.name(), "REQUEST_"+Thread.currentThread().getId());
        request.getContext().put(GraphHeaderParams.SCENARIO_NAME.name(), "CREATE_GRAPH");
        request.getContext().put(GraphHeaderParams.GRAPH_ID.name(), graphId);
        request.setManagerName(GraphEngineManagers.GRAPH_MANAGER);
        request.setOperation("createGraph");
        Future<Object> req = Patterns.ask(reqRouter, request, TestUtil.timeout);
        return req;
    }
}
