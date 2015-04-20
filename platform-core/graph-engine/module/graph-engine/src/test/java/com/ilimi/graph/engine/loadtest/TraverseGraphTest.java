package com.ilimi.graph.engine.loadtest;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import scala.concurrent.Await;
import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.pattern.Patterns;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.model.RelationTraversal;
import com.ilimi.graph.dac.model.Traverser;
import com.ilimi.graph.dac.util.Neo4jGraphFactory;
import com.ilimi.graph.engine.router.GraphEngineManagers;

/**
 * 
 * TEMP_CS has - No. of Nodes:1365 (includes data and sequence nodes) No. of
 * Relations:1543
 * 
 * @author mahesh
 * 
 */

public class TraverseGraphTest {

    ActorRef reqRouter = null;
    private String graphId = "TEMP_CS";
    private String startNodeId = "COURSE_TEMP_CS";
    String SCENARIO_NAME = "TRAVERSE_GRAPH";

//    @BeforeTest
    public void init() throws Exception {
        String logFileName = SCENARIO_NAME + "_" + System.currentTimeMillis();
        System.out.println("Logs are captured in " + logFileName + ".log file.");
        LoggerUtil.config(logFileName);
        reqRouter = TestUtil.initReqRouter();
        Thread.sleep(5000);

    }

//    @Test(threadPoolSize = 100, invocationCount = 1)
    public void traverseTest() {
        try {
            Neo4jGraphFactory.getGraphDb(graphId);
            Future<Object> traverseRes = traverse(reqRouter, graphId);
            Object object = Await.result(traverseRes, TestUtil.timeout.duration());
            // ObjectMapper mapper = new ObjectMapper();
            // System.out.println("Result:"+mapper.writeValueAsString(object));
            TestUtil.handleFutureBlock(traverseRes, SCENARIO_NAME, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Future<Object> traverse(ActorRef reqRouter, String graphId) {
        Traverser traverser = new Traverser(graphId, startNodeId);
        RelationTraversal relationTraversal = new RelationTraversal(RelationTypes.HIERARCHY.relationName(), RelationTraversal.DIRECTION_OUT);
        traverser.traverseRelation(relationTraversal);
        Request request = new Request();
        request.getContext().put(GraphHeaderParams.GRAPH_ID.name(), graphId);
        request.getContext().put(GraphHeaderParams.REQUEST_ID.name(), "REQUEST_" + Thread.currentThread().getId());
        request.getContext().put(GraphHeaderParams.SCENARIO_NAME.name(), SCENARIO_NAME);
        request.put(GraphDACParams.TRAVERSAL_DESCRIPTION.name(), traverser);
        request.setManagerName(GraphEngineManagers.SEARCH_MANAGER);
        request.setOperation("traverse");
        Future<Object> resp = Patterns.ask(reqRouter, request, TestUtil.timeout);
        return resp;
    }
}
