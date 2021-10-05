package org.sunbird.graph.engine.loadtest;

import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sunbird.common.dto.Request;
import org.sunbird.graph.common.enums.GraphHeaderParams;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.engine.router.GraphEngineManagers;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import scala.concurrent.Future;

public class UpdateNodeTest {
    ActorRef reqRouter = null;
    // String graphId = "GRAPH_" + System.currentTimeMillis();
    String graphId = "JAVA_CS";
    String SCENARIO_NAME = "UPDATE_NODE";

    private static final Logger logger = LogManager.getLogger("PerformanceTestLogger");

    //@BeforeTest
    public void init() throws Exception {
        String logFileName = SCENARIO_NAME + "_" + System.currentTimeMillis();
        logger.info("Logs are captured in " + logFileName + ".log file.");
        LoggerUtil.config(logFileName);
        reqRouter = TestUtil.initReqRouter();
        Thread.sleep(5000);
    }

    //@AfterTest
    public void destroy() throws Exception {
        Thread.sleep(10000);
    }

    //@Test(threadPoolSize = 500, invocationCount = 500)
    public void testUpdateNode1() {
        try {
            String nodeId = "JAVA_CS_" + getNodeId();
            String objectType = "COURSE";
            Map<String, Object> metadata = TestUtil.getMetadata((int) Thread.currentThread().getId() % 10);
            Future<Object> nodeReq = updateDataNode(reqRouter, graphId, nodeId, objectType, metadata);
            TestUtil.handleFutureBlock(nodeReq, "createDataNode", GraphDACParams.node_id.name());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void testUpdateNode2() {
        try {
            logger.info("Sleep for 10 seconds");
            logger.info("****************************************************************************************************");
            logger.info("");
            Thread.sleep(10000);
            logger.info("");
            logger.info("");
            logger.info("****************************************************************************************************");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //@Test(threadPoolSize = 500, invocationCount = 500)
    public void testUpdateNode3() {
        try {
            String nodeId = "JAVA_CS_" + getNodeId();
            String objectType = "COURSE";
            Map<String, Object> metadata = TestUtil.getMetadata((int) Thread.currentThread().getId() % 10);
            Future<Object> nodeReq = updateDataNode(reqRouter, graphId, nodeId, objectType, metadata);
            TestUtil.handleFutureBlock(nodeReq, "createDataNode", GraphDACParams.node_id.name());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getNodeId() {
        Random r = new Random();
        int id = r.nextInt(500) + 500;
        return "" + id;
    }

    private Future<Object> updateDataNode(ActorRef reqRouter, String graphId, String nodeId, String objectType, Map<String, Object> metadata) {
        Request request = new Request();
        request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
        request.getContext().put(GraphHeaderParams.request_id.name(), "REQUEST_" + Thread.currentThread().getId());
        request.getContext().put(GraphHeaderParams.scenario_name.name(), SCENARIO_NAME);
        request.setManagerName(GraphEngineManagers.NODE_MANAGER);
        request.setOperation("updateDataNode");
        Node node = new Node(graphId, metadata);
        node.setObjectType(objectType);
        request.put(GraphDACParams.node.name(), node);
        request.put(GraphDACParams.node_id.name(), nodeId);
        Future<Object> req = Patterns.ask(reqRouter, request, TestUtil.timeout);
        return req;
    }

}
